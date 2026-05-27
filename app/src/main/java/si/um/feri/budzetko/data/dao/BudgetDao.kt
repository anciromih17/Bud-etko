package si.um.feri.budzetko.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import si.um.feri.budzetko.data.entity.BudgetCategoryEntity
import si.um.feri.budzetko.data.entity.BudgetEntity
import si.um.feri.budzetko.data.entity.SyncStatus
import si.um.feri.budzetko.data.model.BudgetCategoryProgress
import si.um.feri.budzetko.data.model.CategoryWithMonthlyLimit

@Dao
interface BudgetDao {
    @Query(
        """
        SELECT * FROM budgets
        WHERE user_id = :userId AND month = :month AND year = :year
        LIMIT 1
        """
    )
    fun observeBudget(userId: String, month: Int, year: Int): Flow<BudgetEntity?>

    @Query(
        """
        SELECT * FROM budgets
        WHERE user_id = :userId
        ORDER BY year DESC, month DESC
        """
    )
    fun observeBudgets(userId: String): Flow<List<BudgetEntity>>

    @Query(
        """
        SELECT * FROM budgets
        WHERE user_id = :userId
        ORDER BY year DESC, month DESC
        """
    )
    suspend fun getBudgets(userId: String): List<BudgetEntity>

    @Query(
        """
        SELECT * FROM budgets
        WHERE user_id = :userId AND month = :month AND year = :year
        LIMIT 1
        """
    )
    suspend fun getBudget(userId: String, month: Int, year: Int): BudgetEntity?

    @Query("SELECT COUNT(*) FROM budgets WHERE user_id = :userId AND sync_status IN (:statuses)")
    suspend fun countBudgetsBySyncStatus(userId: String, statuses: List<SyncStatus>): Int

    @Query(
        """
        SELECT COUNT(*) FROM budget_categories
        INNER JOIN budgets ON budgets.id = budget_categories.budget_id
        WHERE budgets.user_id = :userId AND budget_categories.sync_status IN (:statuses)
        """
    )
    suspend fun countBudgetCategoriesBySyncStatus(userId: String, statuses: List<SyncStatus>): Int

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertBudget(budget: BudgetEntity): Long

    @Upsert
    suspend fun upsertBudget(budget: BudgetEntity)

    @Update
    suspend fun updateBudget(budget: BudgetEntity)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertBudgetCategory(budgetCategory: BudgetCategoryEntity): Long

    @Upsert
    suspend fun upsertBudgetCategory(budgetCategory: BudgetCategoryEntity)

    @Update
    suspend fun updateBudgetCategory(budgetCategory: BudgetCategoryEntity)

    @Query(
        """
        SELECT * FROM budget_categories
        WHERE budget_id = :budgetId AND category_id = :categoryId
        LIMIT 1
        """
    )
    suspend fun getBudgetCategory(budgetId: Long, categoryId: Long): BudgetCategoryEntity?

    @Query("SELECT * FROM budget_categories WHERE budget_id = :budgetId")
    suspend fun getBudgetCategories(budgetId: Long): List<BudgetCategoryEntity>

    @Query(
        """
        SELECT categories.id AS id,
               categories.name AS name,
               categories.emoji AS emoji,
               categories.color_index AS color_index,
               categories.budget_role AS budget_role,
               budget_categories.limit_amount AS limit_amount
        FROM categories
        LEFT JOIN budgets
            ON budgets.user_id = categories.user_id
            AND budgets.month = :month
            AND budgets.year = :year
        LEFT JOIN budget_categories
            ON budget_categories.budget_id = budgets.id
            AND budget_categories.category_id = categories.id
        WHERE categories.user_id = :userId
        ORDER BY categories.name COLLATE NOCASE
        """
    )
    fun observeCategoriesWithMonthlyLimit(
        userId: String,
        month: Int,
        year: Int
    ): Flow<List<CategoryWithMonthlyLimit>>

    @Query("DELETE FROM budget_categories WHERE id = :budgetCategoryId")
    suspend fun deleteBudgetCategory(budgetCategoryId: Long)

    @Query(
        """
        DELETE FROM budget_categories
        WHERE budget_id = :budgetId
            AND sync_status = 'SYNCED'
            AND category_id NOT IN (:cloudCategoryIds)
        """
    )
    suspend fun deleteSyncedBudgetCategoriesMissingFromCloud(
        budgetId: Long,
        cloudCategoryIds: List<Long>
    ): Int

    @Query(
        """
        DELETE FROM budget_categories
        WHERE budget_id = :budgetId
            AND sync_status = 'SYNCED'
        """
    )
    suspend fun deleteAllSyncedBudgetCategoriesMissingFromCloud(budgetId: Long): Int

    @Query(
        """
        DELETE FROM budgets
        WHERE user_id = :userId
            AND sync_status = 'SYNCED'
            AND id NOT IN (:cloudIds)
        """
    )
    suspend fun deleteSyncedBudgetsMissingFromCloud(userId: String, cloudIds: List<Long>): Int

    @Query(
        """
        DELETE FROM budgets
        WHERE user_id = :userId
            AND sync_status = 'SYNCED'
        """
    )
    suspend fun deleteAllSyncedBudgetsMissingFromCloud(userId: String): Int

    @Query(
        """
        DELETE FROM budget_categories
        WHERE budget_id IN (
            SELECT id FROM budgets WHERE user_id = :userId
        )
        """
    )
    suspend fun deleteAllBudgetCategoriesForUser(userId: String)

    @Query("DELETE FROM budgets WHERE user_id = :userId")
    suspend fun deleteAllBudgetsForUser(userId: String)

    @Query(
        """
        SELECT budget_categories.category_id AS category_id,
               categories.name AS category_name,
               budget_categories.limit_amount AS limit_amount,
               COALESCE(SUM(expenses.amount), 0.0) AS spent_amount
        FROM budget_categories
        INNER JOIN budgets ON budgets.id = budget_categories.budget_id
        INNER JOIN categories ON categories.id = budget_categories.category_id
        LEFT JOIN expenses
            ON expenses.category_id = budget_categories.category_id
            AND expenses.user_id = budgets.user_id
            AND expenses.date BETWEEN :startDate AND :endDate
        WHERE budgets.user_id = :userId
            AND budgets.month = :month
            AND budgets.year = :year
        GROUP BY budget_categories.category_id, categories.name, budget_categories.limit_amount
        ORDER BY categories.name COLLATE NOCASE
        """
    )
    fun observeBudgetProgress(
        userId: String,
        month: Int,
        year: Int,
        startDate: Long,
        endDate: Long
    ): Flow<List<BudgetCategoryProgress>>

    @Query(
        """
        SELECT budget_categories.category_id AS category_id,
               categories.name AS category_name,
               budget_categories.limit_amount AS limit_amount,
               COALESCE(SUM(expenses.amount), 0.0) AS spent_amount
        FROM budget_categories
        INNER JOIN budgets ON budgets.id = budget_categories.budget_id
        INNER JOIN categories ON categories.id = budget_categories.category_id
        LEFT JOIN expenses
            ON expenses.category_id = budget_categories.category_id
            AND expenses.user_id = budgets.user_id
            AND expenses.date BETWEEN :startDate AND :endDate
        WHERE budgets.user_id = :userId
            AND budgets.month = :month
            AND budgets.year = :year
        GROUP BY budget_categories.category_id, categories.name, budget_categories.limit_amount
        ORDER BY categories.name COLLATE NOCASE
        """
    )
    suspend fun getBudgetProgress(
        userId: String,
        month: Int,
        year: Int,
        startDate: Long,
        endDate: Long
    ): List<BudgetCategoryProgress>
}
