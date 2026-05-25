package si.um.feri.budzetko.data.repository

import kotlinx.coroutines.flow.Flow
import si.um.feri.budzetko.data.dao.ExpenseDao
import si.um.feri.budzetko.data.entity.ExpenseEntity
import si.um.feri.budzetko.data.entity.SyncStatus

class ExpenseRepository(
    private val expenseDao: ExpenseDao,
    private val firestoreRepository: FirestoreRepository = FirestoreRepository()
) {

    fun observeExpenses(userId: String): Flow<List<ExpenseEntity>> {
        return expenseDao.observeExpenses(userId)
    }

    suspend fun insertExpense(
        amount: Double,
        date: Long,
        description: String,
        userId: String,
        categoryId: Long
    ): Long {
        val expense = ExpenseEntity(
            amount = amount,
            date = date,
            description = description,
            userId = userId,
            categoryId = categoryId,
            syncStatus = SyncStatus.PENDING,
            updatedAt = System.currentTimeMillis()
        )

        val localId = expenseDao.insertExpense(expense)

        firestoreRepository.saveExpense(
            expense.copy(
                id = localId,
                syncStatus = SyncStatus.SYNCED
            )
        )

        return localId
    }

    suspend fun updateExpense(expense: ExpenseEntity) {
        val updatedExpense = expense.copy(
            syncStatus = SyncStatus.PENDING,
            updatedAt = System.currentTimeMillis()
        )

        expenseDao.updateExpense(updatedExpense)

        firestoreRepository.saveExpense(
            updatedExpense.copy(syncStatus = SyncStatus.SYNCED)
        )
    }

    suspend fun deleteExpense(expense: ExpenseEntity) {
        expenseDao.deleteExpense(expense)
        firestoreRepository.deleteExpense(expense)
    }
}