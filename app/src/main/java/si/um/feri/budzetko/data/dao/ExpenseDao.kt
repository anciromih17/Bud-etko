package si.um.feri.budzetko.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import si.um.feri.budzetko.data.entity.ExpenseEntity
import si.um.feri.budzetko.data.entity.SyncStatus
import si.um.feri.budzetko.data.model.CategorySpending

@Dao
interface ExpenseDao {
    @Query("SELECT * FROM expenses WHERE user_id = :userId ORDER BY date DESC, updated_at DESC")
    fun observeExpenses(userId: String): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE id = :expenseId LIMIT 1")
    suspend fun getExpense(expenseId: Long): ExpenseEntity?

    @Query(
        """
        SELECT * FROM expenses
        WHERE user_id = :userId
        ORDER BY date DESC, updated_at DESC
        LIMIT :limit
        """
    )
    fun observeRecentExpenses(userId: String, limit: Int): Flow<List<ExpenseEntity>>

    @Query(
        """
        SELECT COALESCE(SUM(amount), 0.0) FROM expenses
        WHERE user_id = :userId AND date BETWEEN :startDate AND :endDate
        """
    )
    fun observeTotalSpent(userId: String, startDate: Long, endDate: Long): Flow<Double>

    @Query(
        """
        SELECT categories.id AS category_id,
               categories.name AS category_name,
               COALESCE(SUM(expenses.amount), 0.0) AS spent_amount
        FROM categories
        LEFT JOIN expenses
            ON expenses.category_id = categories.id
            AND expenses.user_id = :userId
            AND expenses.date BETWEEN :startDate AND :endDate
        WHERE categories.user_id = :userId
        GROUP BY categories.id, categories.name
        ORDER BY spent_amount DESC
        """
    )
    fun observeSpendingByCategory(
        userId: String,
        startDate: Long,
        endDate: Long
    ): Flow<List<CategorySpending>>

    @Query("SELECT * FROM expenses WHERE sync_status IN (:statuses) ORDER BY updated_at ASC")
    suspend fun getExpensesBySyncStatus(statuses: List<SyncStatus>): List<ExpenseEntity>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertExpense(expense: ExpenseEntity): Long

    @Update
    suspend fun updateExpense(expense: ExpenseEntity)

    @Delete
    suspend fun deleteExpense(expense: ExpenseEntity)
}
