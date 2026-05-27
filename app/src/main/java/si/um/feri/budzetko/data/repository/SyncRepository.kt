package si.um.feri.budzetko.data.repository

import si.um.feri.budzetko.data.dao.AiSummaryDao
import si.um.feri.budzetko.data.dao.BudgetDao
import si.um.feri.budzetko.data.dao.CategoryDao
import si.um.feri.budzetko.data.dao.ExpenseDao
import si.um.feri.budzetko.data.dao.UserDao
import si.um.feri.budzetko.data.entity.AiSummaryEntity
import si.um.feri.budzetko.data.entity.CategoryEntity
import si.um.feri.budzetko.data.entity.ExpenseEntity
import si.um.feri.budzetko.data.entity.SyncStatus

class SyncRepository(
    private val userDao: UserDao,
    private val categoryDao: CategoryDao,
    private val expenseDao: ExpenseDao,
    private val budgetDao: BudgetDao,
    private val aiSummaryDao: AiSummaryDao,
    private val firestoreRepository: FirestoreRepository = FirestoreRepository()
) {
    suspend fun getLocalSyncSummary(userId: String): LocalSyncSummary {
        val pendingStatuses = listOf(SyncStatus.PENDING)
        val failedStatuses = listOf(SyncStatus.FAILED)

        return LocalSyncSummary(
            pendingCategories = categoryDao.countBySyncStatus(userId, pendingStatuses),
            pendingExpenses = expenseDao.countBySyncStatus(userId, pendingStatuses),
            pendingBudgets = budgetDao.countBudgetsBySyncStatus(userId, pendingStatuses),
            pendingBudgetLimits = budgetDao.countBudgetCategoriesBySyncStatus(userId, pendingStatuses),
            pendingAiSummaries = aiSummaryDao.countBySyncStatus(userId, pendingStatuses),
            failedCategories = categoryDao.countBySyncStatus(userId, failedStatuses),
            failedExpenses = expenseDao.countBySyncStatus(userId, failedStatuses),
            failedBudgets = budgetDao.countBudgetsBySyncStatus(userId, failedStatuses),
            failedBudgetLimits = budgetDao.countBudgetCategoriesBySyncStatus(userId, failedStatuses),
            failedAiSummaries = aiSummaryDao.countBySyncStatus(userId, failedStatuses)
        )
    }

    suspend fun pushAllLocalData(userId: String): Result<SyncReport> {
        return runCatching {
            userDao.getUser(userId)?.let { user ->
                firestoreRepository.saveUser(user)
            }

            pullCloudData(userId)

            val categories = categoryDao.getCategories(userId)
            val expenses = expenseDao.getExpenses(userId)
            val budgets = budgetDao.getBudgets(userId)
            val summaries = aiSummaryDao.getSummaries(userId)
            val pendingCategories = categories.filter { it.syncStatus != SyncStatus.SYNCED }
            val pendingExpenses = expenses.filter { it.syncStatus != SyncStatus.SYNCED }
            val pendingBudgets = budgets.filter { budget ->
                budget.syncStatus != SyncStatus.SYNCED ||
                    budgetDao.getBudgetCategories(budget.id).any { it.syncStatus != SyncStatus.SYNCED }
            }
            val pendingSummaries = summaries.filter { it.syncStatus != SyncStatus.SYNCED }

            var uploadedCategories = 0
            var uploadedExpenses = 0
            var uploadedBudgets = 0
            var uploadedSummaries = 0
            var failedCategories = 0
            var failedExpenses = 0
            var failedBudgets = 0
            var failedSummaries = 0

            pendingCategories.forEach { category ->
                runCatching {
                    firestoreRepository.saveCategory(category)
                    firestoreRepository.deleteLegacyLocalCategory(category)
                }.onSuccess {
                    uploadedCategories += 1
                    categoryDao.updateCategory(category.copy(syncStatus = SyncStatus.SYNCED))
                }.onFailure {
                    failedCategories += 1
                    categoryDao.updateCategory(category.copy(syncStatus = SyncStatus.FAILED))
                }
            }

            pendingExpenses.forEach { expense ->
                runCatching {
                    firestoreRepository.saveExpense(expense.copy(syncStatus = SyncStatus.SYNCED))
                    firestoreRepository.deleteLegacyLocalExpense(expense)
                }.onSuccess {
                    uploadedExpenses += 1
                    expenseDao.updateExpense(expense.copy(syncStatus = SyncStatus.SYNCED))
                }.onFailure {
                    failedExpenses += 1
                    expenseDao.updateExpense(expense.copy(syncStatus = SyncStatus.FAILED))
                }
            }

            pendingBudgets.forEach { budget ->
                runCatching {
                    firestoreRepository.saveBudget(
                        budget = budget,
                        limits = budgetDao.getBudgetCategories(budget.id)
                    )
                }.onSuccess {
                    uploadedBudgets += 1
                    budgetDao.updateBudget(budget.copy(syncStatus = SyncStatus.SYNCED))
                    budgetDao.getBudgetCategories(budget.id).forEach { limit ->
                        budgetDao.updateBudgetCategory(limit.copy(syncStatus = SyncStatus.SYNCED))
                    }
                }.onFailure {
                    failedBudgets += 1
                    budgetDao.updateBudget(budget.copy(syncStatus = SyncStatus.FAILED))
                    budgetDao.getBudgetCategories(budget.id).forEach { limit ->
                        budgetDao.updateBudgetCategory(limit.copy(syncStatus = SyncStatus.FAILED))
                    }
                }
            }

            pendingSummaries.forEach { summary ->
                runCatching {
                    firestoreRepository.saveAiSummary(summary)
                }.onSuccess {
                    uploadedSummaries += 1
                    aiSummaryDao.updateSummary(summary.copy(syncStatus = SyncStatus.SYNCED))
                }.onFailure {
                    failedSummaries += 1
                    aiSummaryDao.updateSummary(summary.copy(syncStatus = SyncStatus.FAILED))
                }
            }

            val report = SyncReport(
                localCategories = categories.size,
                localExpenses = expenses.size,
                localBudgets = budgets.size,
                localAiSummaries = summaries.size,
                uploadedCategories = uploadedCategories,
                uploadedExpenses = uploadedExpenses,
                uploadedBudgets = uploadedBudgets,
                uploadedAiSummaries = uploadedSummaries,
                failedCategories = failedCategories,
                failedExpenses = failedExpenses,
                failedBudgets = failedBudgets,
                failedAiSummaries = failedSummaries
            )

            firestoreRepository.saveSyncMetadata(userId, report.toMetadata())

            report
        }
    }

    private suspend fun pullCloudData(userId: String) {
        val cloudCategories = firestoreRepository.fetchCategories(userId)
        val cloudExpenses = firestoreRepository.fetchExpenses(userId)
        val cloudBudgetsWithLimits = firestoreRepository.fetchBudgetsWithLimits(userId)
        val cloudSummaries = firestoreRepository.fetchAiSummaries(userId)

        deleteLocalSyncedRecordsMissingFromCloud(
            userId = userId,
            cloudCategories = cloudCategories,
            cloudExpenses = cloudExpenses,
            cloudBudgetsWithLimits = cloudBudgetsWithLimits,
            cloudSummaries = cloudSummaries
        )

        cloudCategories.forEach { category ->
            categoryDao.upsertCategory(
                category.copy(
                    userId = userId,
                    syncStatus = SyncStatus.SYNCED
                )
            )
        }

        cloudExpenses.forEach { expense ->
            expenseDao.upsertExpense(
                expense.copy(
                    userId = userId,
                    syncStatus = SyncStatus.SYNCED
                )
            )
        }

        cloudBudgetsWithLimits.forEach { budgetWithLimits ->
            val budget = budgetWithLimits.budget.copy(userId = userId)
            budgetDao.upsertBudget(
                budget.copy(syncStatus = SyncStatus.SYNCED)
            )

            budgetWithLimits.limits.forEach { limit ->
                budgetDao.upsertBudgetCategory(
                    limit.copy(
                        budgetId = budget.id,
                        syncStatus = SyncStatus.SYNCED
                    )
                )
            }
        }

        cloudSummaries.forEach { summary ->
            aiSummaryDao.upsertSummary(
                summary.copy(
                    userId = userId,
                    syncStatus = SyncStatus.SYNCED
                )
            )
        }
    }

    private suspend fun deleteLocalSyncedRecordsMissingFromCloud(
        userId: String,
        cloudCategories: List<CategoryEntity>,
        cloudExpenses: List<ExpenseEntity>,
        cloudBudgetsWithLimits: List<CloudBudgetWithLimits>,
        cloudSummaries: List<AiSummaryEntity>
    ) {
        val cloudExpenseIds = cloudExpenses.map { it.id }
        if (cloudExpenseIds.isEmpty()) {
            expenseDao.deleteAllSyncedMissingFromCloud(userId)
        } else {
            expenseDao.deleteSyncedMissingFromCloud(userId, cloudExpenseIds)
        }

        cloudBudgetsWithLimits.forEach { budgetWithLimits ->
            val cloudLimitCategoryIds = budgetWithLimits.limits.map { it.categoryId }
            if (cloudLimitCategoryIds.isEmpty()) {
                budgetDao.deleteAllSyncedBudgetCategoriesMissingFromCloud(budgetWithLimits.budget.id)
            } else {
                budgetDao.deleteSyncedBudgetCategoriesMissingFromCloud(
                    budgetId = budgetWithLimits.budget.id,
                    cloudCategoryIds = cloudLimitCategoryIds
                )
            }
        }

        val cloudBudgetIds = cloudBudgetsWithLimits.map { it.budget.id }
        if (cloudBudgetIds.isEmpty()) {
            budgetDao.deleteAllSyncedBudgetsMissingFromCloud(userId)
        } else {
            budgetDao.deleteSyncedBudgetsMissingFromCloud(userId, cloudBudgetIds)
        }

        val cloudSummaryIds = cloudSummaries.map { it.id }
        if (cloudSummaryIds.isEmpty()) {
            aiSummaryDao.deleteAllSyncedMissingFromCloud(userId)
        } else {
            aiSummaryDao.deleteSyncedMissingFromCloud(userId, cloudSummaryIds)
        }

        val cloudCategoryIds = cloudCategories.map { it.id }
        if (cloudCategoryIds.isEmpty()) {
            categoryDao.deleteAllSyncedMissingFromCloud(userId)
        } else {
            categoryDao.deleteSyncedMissingFromCloud(userId, cloudCategoryIds)
        }
    }
}

data class LocalSyncSummary(
    val pendingCategories: Int,
    val pendingExpenses: Int,
    val pendingBudgets: Int,
    val pendingBudgetLimits: Int,
    val pendingAiSummaries: Int,
    val failedCategories: Int,
    val failedExpenses: Int,
    val failedBudgets: Int,
    val failedBudgetLimits: Int,
    val failedAiSummaries: Int
) {
    val pendingTotal: Int
        get() = pendingCategories + pendingExpenses + pendingBudgets + pendingBudgetLimits + pendingAiSummaries

    val failedTotal: Int
        get() = failedCategories + failedExpenses + failedBudgets + failedBudgetLimits + failedAiSummaries

    val hasIssues: Boolean
        get() = pendingTotal > 0 || failedTotal > 0

    fun toUserMessage(): String {
        return when {
            failedTotal > 0 -> "Nekateri zapisi niso sinhronizirani: $failedTotal neuspelih, $pendingTotal čakajočih. Klikni Sync."
            pendingTotal > 0 -> "Čaka na sinhronizacijo: $pendingTotal zapisov. Klikni Sync."
            else -> "Ni lokalnih sprememb za sinhronizacijo."
        }
    }
}

data class SyncReport(
    val localCategories: Int,
    val localExpenses: Int,
    val localBudgets: Int,
    val localAiSummaries: Int,
    val uploadedCategories: Int,
    val uploadedExpenses: Int,
    val uploadedBudgets: Int,
    val uploadedAiSummaries: Int,
    val failedCategories: Int,
    val failedExpenses: Int,
    val failedBudgets: Int,
    val failedAiSummaries: Int
) {
    private val hasFailures: Boolean
        get() = failedCategories + failedExpenses + failedBudgets + failedAiSummaries > 0

    private val uploadedTotal: Int
        get() = uploadedCategories + uploadedExpenses + uploadedBudgets + uploadedAiSummaries

    fun toUserMessage(): String {
        return when {
            hasFailures -> "Delno sinhronizirano: neuspešni zapisi - kategorije $failedCategories, stroški $failedExpenses, proračuni $failedBudgets, AI povzetki $failedAiSummaries."
            uploadedTotal > 0 -> "Podatki so preverjeni s Firestore. Naloženo: kategorije $uploadedCategories, stroški $uploadedExpenses, proračuni $uploadedBudgets, AI povzetki $uploadedAiSummaries."
            else -> "Podatki so preverjeni s Firestore. Ni lokalnih sprememb za sinhronizacijo."
        }
    }

    fun toMetadata(): Map<String, Any?> {
        return mapOf(
            "lastSyncAt" to System.currentTimeMillis(),
            "lastSyncStatus" to if (hasFailures) "PARTIAL" else "SUCCESS",
            "lastSyncLocalCounts" to mapOf(
                "categories" to localCategories,
                "expenses" to localExpenses,
                "budgets" to localBudgets,
                "aiSummaries" to localAiSummaries
            ),
            "lastSyncUploadedCounts" to mapOf(
                "categories" to uploadedCategories,
                "expenses" to uploadedExpenses,
                "budgets" to uploadedBudgets,
                "aiSummaries" to uploadedAiSummaries
            ),
            "lastSyncFailedCounts" to mapOf(
                "categories" to failedCategories,
                "expenses" to failedExpenses,
                "budgets" to failedBudgets,
                "aiSummaries" to failedAiSummaries
            )
        )
    }
}
