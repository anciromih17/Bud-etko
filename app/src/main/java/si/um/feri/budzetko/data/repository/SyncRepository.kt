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

            var syncedCategories = 0
            var syncedExpenses = 0
            var syncedBudgets = 0
            var syncedSummaries = 0
            var failedCategories = 0
            var failedExpenses = 0
            var failedBudgets = 0
            var failedSummaries = 0

            pendingCategories.forEach { category ->
                runCatching {
                    firestoreRepository.saveCategory(category)
                    firestoreRepository.deleteLegacyLocalCategory(category)
                }.onSuccess {
                    syncedCategories += 1
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
                    syncedExpenses += 1
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
                    syncedBudgets += 1
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
                    syncedSummaries += 1
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
                syncedCategories = categories.size - failedCategories,
                syncedExpenses = expenses.size - failedExpenses,
                syncedBudgets = budgets.size - failedBudgets,
                syncedAiSummaries = summaries.size - failedSummaries,
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
            else -> "Vsi lokalni podatki so sinhronizirani s Firestore."
        }
    }
}

data class SyncReport(
    val localCategories: Int,
    val localExpenses: Int,
    val localBudgets: Int,
    val localAiSummaries: Int,
    val syncedCategories: Int,
    val syncedExpenses: Int,
    val syncedBudgets: Int,
    val syncedAiSummaries: Int,
    val failedCategories: Int,
    val failedExpenses: Int,
    val failedBudgets: Int,
    val failedAiSummaries: Int
) {
    private val hasFailures: Boolean
        get() = failedCategories + failedExpenses + failedBudgets + failedAiSummaries > 0

    fun toUserMessage(): String {
        val prefix = if (hasFailures) "Delno sinhronizirano" else "Sinhronizirano"
        return "$prefix: kategorije $syncedCategories/$localCategories, stroški $syncedExpenses/$localExpenses, proračuni $syncedBudgets/$localBudgets, AI povzetki $syncedAiSummaries/$localAiSummaries."
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
            "lastSyncSyncedCounts" to mapOf(
                "categories" to syncedCategories,
                "expenses" to syncedExpenses,
                "budgets" to syncedBudgets,
                "aiSummaries" to syncedAiSummaries
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
