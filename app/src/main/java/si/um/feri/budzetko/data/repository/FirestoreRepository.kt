package si.um.feri.budzetko.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import si.um.feri.budzetko.data.entity.AiSummaryEntity
import si.um.feri.budzetko.data.entity.AiSummarySource
import si.um.feri.budzetko.data.entity.BudgetCategoryEntity
import si.um.feri.budzetko.data.entity.BudgetEntity
import si.um.feri.budzetko.data.entity.CategoryBudgetRole
import si.um.feri.budzetko.data.entity.CategoryEntity
import si.um.feri.budzetko.data.entity.ExpenseEntity
import si.um.feri.budzetko.data.entity.SyncStatus
import si.um.feri.budzetko.data.entity.UserEntity

class FirestoreRepository {

    private val db = FirebaseFirestore.getInstance()

    suspend fun saveUser(user: UserEntity) {
        db.collection("users")
            .document(user.userId)
            .set(
                mapOf(
                    "userId" to user.userId,
                    "email" to user.email,
                    "username" to user.username
                )
            )
            .await()
    }

    suspend fun saveExpense(expense: ExpenseEntity) {
        db.collection("users")
            .document(expense.userId)
            .collection("expenses")
            .document(expense.id.toString())
            .set(
                mapOf(
                    "id" to expense.id,
                    "amount" to expense.amount,
                    "date" to expense.date,
                    "description" to expense.description,
                    "cloudId" to expense.cloudId,
                    "syncStatus" to expense.syncStatus.name,
                    "updatedAt" to expense.updatedAt,
                    "userId" to expense.userId,
                    "categoryId" to expense.categoryId
                )
            )
            .await()
    }

    suspend fun deleteExpense(expense: ExpenseEntity) {
        db.collection("users")
            .document(expense.userId)
            .collection("expenses")
            .document(expense.id.toString())
            .delete()
            .await()
    }

    suspend fun deleteLegacyLocalExpense(expense: ExpenseEntity) {
        db.collection("users")
            .document(expense.userId)
            .collection("expenses")
            .document("local_${expense.id}")
            .delete()
            .await()
    }

    suspend fun saveCategory(category: CategoryEntity) {
        db.collection("users")
            .document(category.userId)
            .collection("categories")
            .document(category.id.toString())
            .set(
                mapOf(
                    "id" to category.id,
                    "name" to category.name,
                    "emoji" to category.emoji,
                    "colorIndex" to category.colorIndex,
                    "budgetRole" to category.budgetRole.name,
                    "syncStatus" to category.syncStatus.name,
                    "updatedAt" to category.updatedAt,
                    "userId" to category.userId
                )
            )
            .await()
    }

    suspend fun deleteCategory(category: CategoryEntity) {
        db.collection("users")
            .document(category.userId)
            .collection("categories")
            .document(category.id.toString())
            .delete()
            .await()
    }

    suspend fun deleteLegacyLocalCategory(category: CategoryEntity) {
        db.collection("users")
            .document(category.userId)
            .collection("categories")
            .document("local_${category.id}")
            .delete()
            .await()
    }

    suspend fun fetchCategories(userId: String): List<CategoryEntity> {
        return db.collection("users")
            .document(userId)
            .collection("categories")
            .get()
            .await()
            .documents
            .mapNotNull { document ->
                val id = document.getLongValue("id") ?: document.id.toLongOrNull() ?: return@mapNotNull null
                val name = document.getString("name") ?: return@mapNotNull null
                val budgetRole = document.getString("budgetRole")
                    ?.let { runCatching { CategoryBudgetRole.valueOf(it) }.getOrNull() }
                    ?: CategoryBudgetRole.OTHER

                CategoryEntity(
                    id = id,
                    name = name,
                    emoji = document.getString("emoji"),
                    colorIndex = document.getLongValue("colorIndex")?.toInt() ?: 0,
                    budgetRole = budgetRole,
                    syncStatus = document.getString("syncStatus")
                        ?.let { runCatching { SyncStatus.valueOf(it) }.getOrNull() }
                        ?: SyncStatus.SYNCED,
                    updatedAt = document.getLongValue("updatedAt") ?: System.currentTimeMillis(),
                    userId = document.getString("userId") ?: userId
                )
            }
    }

    suspend fun fetchExpenses(userId: String): List<ExpenseEntity> {
        return db.collection("users")
            .document(userId)
            .collection("expenses")
            .get()
            .await()
            .documents
            .mapNotNull { document ->
                val id = document.getLongValue("id") ?: document.id.toLongOrNull() ?: return@mapNotNull null
                val amount = document.getDoubleValue("amount") ?: return@mapNotNull null
                val date = document.getLongValue("date") ?: return@mapNotNull null
                val categoryId = document.getLongValue("categoryId") ?: return@mapNotNull null
                val syncStatus = document.getString("syncStatus")
                    ?.let { runCatching { SyncStatus.valueOf(it) }.getOrNull() }
                    ?: SyncStatus.SYNCED

                ExpenseEntity(
                    id = id,
                    amount = amount,
                    date = date,
                    description = document.getString("description").orEmpty(),
                    cloudId = document.getString("cloudId"),
                    syncStatus = syncStatus,
                    updatedAt = document.getLongValue("updatedAt") ?: System.currentTimeMillis(),
                    userId = document.getString("userId") ?: userId,
                    categoryId = categoryId
                )
            }
    }

    suspend fun saveBudget(
        budget: BudgetEntity,
        limits: List<BudgetCategoryEntity>
    ) {
        val budgetDocId = "${budget.year}_${budget.month}"

        db.collection("users")
            .document(budget.userId)
            .collection("budgets")
            .document(budgetDocId)
            .set(
                mapOf(
                    "id" to budget.id,
                    "month" to budget.month,
                    "year" to budget.year,
                    "income" to budget.income,
                    "syncStatus" to budget.syncStatus.name,
                    "updatedAt" to budget.updatedAt,
                    "userId" to budget.userId
                )
            )
            .await()

        limits.forEach { limit ->
            db.collection("users")
                .document(budget.userId)
                .collection("budgets")
                .document(budgetDocId)
                .collection("limits")
                .document(limit.categoryId.toString())
                .set(
                    mapOf(
                        "id" to limit.id,
                        "limitAmount" to limit.limitAmount,
                        "syncStatus" to limit.syncStatus.name,
                        "updatedAt" to limit.updatedAt,
                        "budgetId" to limit.budgetId,
                        "categoryId" to limit.categoryId
                    )
                )
                .await()
        }
    }

    suspend fun fetchBudgetsWithLimits(userId: String): List<CloudBudgetWithLimits> {
        return db.collection("users")
            .document(userId)
            .collection("budgets")
            .get()
            .await()
            .documents
            .mapNotNull { budgetDocument ->
                val budgetId = budgetDocument.getLongValue("id") ?: return@mapNotNull null
                val month = budgetDocument.getLongValue("month")?.toInt() ?: return@mapNotNull null
                val year = budgetDocument.getLongValue("year")?.toInt() ?: return@mapNotNull null
                val income = budgetDocument.getDoubleValue("income") ?: return@mapNotNull null

                val limits = budgetDocument.reference
                    .collection("limits")
                    .get()
                    .await()
                    .documents
                    .mapNotNull { limitDocument ->
                        val limitId = limitDocument.getLongValue("id") ?: return@mapNotNull null
                        val limitAmount = limitDocument.getDoubleValue("limitAmount") ?: return@mapNotNull null
                        val categoryId = limitDocument.getLongValue("categoryId") ?: return@mapNotNull null

                        BudgetCategoryEntity(
                            id = limitId,
                            limitAmount = limitAmount,
                            syncStatus = limitDocument.getString("syncStatus")
                                ?.let { runCatching { SyncStatus.valueOf(it) }.getOrNull() }
                                ?: SyncStatus.SYNCED,
                            updatedAt = limitDocument.getLongValue("updatedAt") ?: System.currentTimeMillis(),
                            budgetId = limitDocument.getLongValue("budgetId") ?: budgetId,
                            categoryId = categoryId
                        )
                    }

                CloudBudgetWithLimits(
                    budget = BudgetEntity(
                        id = budgetId,
                        month = month,
                        year = year,
                        income = income,
                        syncStatus = budgetDocument.getString("syncStatus")
                            ?.let { runCatching { SyncStatus.valueOf(it) }.getOrNull() }
                            ?: SyncStatus.SYNCED,
                        updatedAt = budgetDocument.getLongValue("updatedAt") ?: System.currentTimeMillis(),
                        userId = budgetDocument.getString("userId") ?: userId
                    ),
                    limits = limits
                )
            }
    }

    suspend fun saveAiSummary(summary: AiSummaryEntity) {
        val summaryDocId = "${summary.year}_${summary.month}"

        db.collection("users")
            .document(summary.userId)
            .collection("ai_summaries")
            .document(summaryDocId)
            .set(
                mapOf(
                    "id" to summary.id,
                    "month" to summary.month,
                    "year" to summary.year,
                    "summary" to summary.summary,
                    "source" to summary.source.name,
                    "syncStatus" to summary.syncStatus.name,
                    "createdAt" to summary.createdAt,
                    "updatedAt" to summary.updatedAt,
                    "userId" to summary.userId
                )
            )
            .await()
    }

    suspend fun fetchAiSummaries(userId: String): List<AiSummaryEntity> {
        return db.collection("users")
            .document(userId)
            .collection("ai_summaries")
            .get()
            .await()
            .documents
            .mapNotNull { document ->
                val id = document.getLongValue("id") ?: return@mapNotNull null
                val month = document.getLongValue("month")?.toInt() ?: return@mapNotNull null
                val year = document.getLongValue("year")?.toInt() ?: return@mapNotNull null
                val summary = document.getString("summary") ?: return@mapNotNull null
                val source = document.getString("source")
                    ?.let { runCatching { AiSummarySource.valueOf(it) }.getOrNull() }
                    ?: AiSummarySource.FALLBACK

                AiSummaryEntity(
                    id = id,
                    month = month,
                    year = year,
                    summary = summary,
                    source = source,
                    syncStatus = document.getString("syncStatus")
                        ?.let { runCatching { SyncStatus.valueOf(it) }.getOrNull() }
                        ?: SyncStatus.SYNCED,
                    createdAt = document.getLongValue("createdAt") ?: System.currentTimeMillis(),
                    updatedAt = document.getLongValue("updatedAt") ?: System.currentTimeMillis(),
                    userId = document.getString("userId") ?: userId
                )
            }
    }

    suspend fun saveSyncMetadata(userId: String, metadata: Map<String, Any?>) {
        db.collection("users")
            .document(userId)
            .set(metadata, SetOptions.merge())
            .await()
    }

    suspend fun deleteUserData(userId: String) {
        val userDocument = db.collection("users").document(userId)

        userDocument.collection("expenses").get().await().documents.forEach { document ->
            document.reference.delete().await()
        }

        userDocument.collection("categories").get().await().documents.forEach { document ->
            document.reference.delete().await()
        }

        userDocument.collection("ai_summaries").get().await().documents.forEach { document ->
            document.reference.delete().await()
        }

        userDocument.collection("budgets").get().await().documents.forEach { budgetDocument ->
            budgetDocument.reference.collection("limits").get().await().documents.forEach { limitDocument ->
                limitDocument.reference.delete().await()
            }
            budgetDocument.reference.delete().await()
        }

        userDocument.delete().await()
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.getLongValue(field: String): Long? {
        return when (val value = get(field)) {
            is Long -> value
            is Int -> value.toLong()
            is Double -> value.toLong()
            is Number -> value.toLong()
            else -> null
        }
    }

    private fun com.google.firebase.firestore.DocumentSnapshot.getDoubleValue(field: String): Double? {
        return when (val value = get(field)) {
            is Double -> value
            is Float -> value.toDouble()
            is Long -> value.toDouble()
            is Int -> value.toDouble()
            is Number -> value.toDouble()
            else -> null
        }
    }
}

data class CloudBudgetWithLimits(
    val budget: BudgetEntity,
    val limits: List<BudgetCategoryEntity>
)
