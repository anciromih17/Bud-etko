package si.um.feri.budzetko.data.repository

import kotlinx.coroutines.flow.Flow
import si.um.feri.budzetko.data.dao.CategoryDao
import si.um.feri.budzetko.data.entity.CategoryBudgetRole
import si.um.feri.budzetko.data.entity.CategoryEntity
import si.um.feri.budzetko.data.entity.SyncStatus

class CategoryRepository(
    private val categoryDao: CategoryDao,
    private val firestoreRepository: FirestoreRepository = FirestoreRepository()
) {
    fun observeCategories(userId: String): Flow<List<CategoryEntity>> {
        return categoryDao.observeCategories(userId)
    }

    suspend fun addCategory(
        name: String,
        userId: String,
        emoji: String?,
        colorIndex: Int,
        budgetRole: CategoryBudgetRole
    ): Long {
        val cleanName = name.trim()
        require(cleanName.isNotBlank()) { "Ime kategorije ne sme biti prazno." }

        val category = CategoryEntity(
            name = cleanName,
            emoji = emoji,
            colorIndex = colorIndex,
            budgetRole = budgetRole,
            syncStatus = SyncStatus.PENDING,
            updatedAt = System.currentTimeMillis(),
            userId = userId
        )

        val localId = categoryDao.insertCategory(category)
        val categoryWithId = category.copy(id = localId)

        runCatching {
            firestoreRepository.saveCategory(categoryWithId)
        }.onSuccess {
            categoryDao.updateCategory(categoryWithId.copy(syncStatus = SyncStatus.SYNCED))
        }.onFailure {
            categoryDao.updateCategory(categoryWithId.copy(syncStatus = SyncStatus.FAILED))
        }

        return localId
    }

    suspend fun updateCategory(
        category: CategoryEntity,
        name: String,
        emoji: String?,
        colorIndex: Int,
        budgetRole: CategoryBudgetRole
    ) {
        val cleanName = name.trim()
        require(cleanName.isNotBlank()) { "Ime kategorije ne sme biti prazno." }

        val updatedCategory = category.copy(
            name = cleanName,
            emoji = emoji,
            colorIndex = colorIndex,
            budgetRole = budgetRole,
            syncStatus = SyncStatus.PENDING,
            updatedAt = System.currentTimeMillis()
        )

        categoryDao.updateCategory(updatedCategory)
        runCatching {
            firestoreRepository.saveCategory(updatedCategory)
        }.onSuccess {
            categoryDao.updateCategory(updatedCategory.copy(syncStatus = SyncStatus.SYNCED))
        }.onFailure {
            categoryDao.updateCategory(updatedCategory.copy(syncStatus = SyncStatus.FAILED))
        }
    }

    suspend fun deleteCategory(category: CategoryEntity) {
        categoryDao.deleteCategory(category)
        runCatching {
            firestoreRepository.deleteCategory(category)
        }
    }
}
