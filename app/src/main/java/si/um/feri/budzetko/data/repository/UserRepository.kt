package si.um.feri.budzetko.data.repository

import kotlinx.coroutines.flow.Flow
import si.um.feri.budzetko.data.dao.AiSummaryDao
import si.um.feri.budzetko.data.dao.BudgetDao
import si.um.feri.budzetko.data.dao.CategoryDao
import si.um.feri.budzetko.data.dao.ExpenseDao
import si.um.feri.budzetko.data.dao.UserDao
import si.um.feri.budzetko.data.entity.UserEntity

class UserRepository(
    private val userDao: UserDao,
    private val categoryDao: CategoryDao? = null,
    private val expenseDao: ExpenseDao? = null,
    private val budgetDao: BudgetDao? = null,
    private val aiSummaryDao: AiSummaryDao? = null,
    private val firestoreRepository: FirestoreRepository = FirestoreRepository()
) {
    fun observeUser(userId: String): Flow<UserEntity?> {
        return userDao.observeUser(userId)
    }

    suspend fun upsertFirebaseUser(user: UserEntity) {
        userDao.upsertUser(user)
        runCatching {
            firestoreRepository.saveUser(user)
        }
    }

    suspend fun ensureDemoUser() {
        if (userDao.getUser(DEMO_USER_ID) == null) {
            userDao.upsertUser(
                UserEntity(
                    userId = DEMO_USER_ID,
                    email = "ana@budzetko.local",
                    username = "Ana"
                )
            )
        }
    }

    suspend fun deleteAccountData(userId: String) {
        runCatching {
            firestoreRepository.deleteUserData(userId)
        }

        expenseDao?.deleteAllForUser(userId)
        budgetDao?.deleteAllBudgetCategoriesForUser(userId)
        budgetDao?.deleteAllBudgetsForUser(userId)
        aiSummaryDao?.deleteAllForUser(userId)
        categoryDao?.deleteAllForUser(userId)
        userDao.deleteUser(userId)
    }

    companion object {
        const val DEMO_USER_ID = "demo-user"
    }
}
