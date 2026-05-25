package si.um.feri.budzetko.data.repository

import kotlinx.coroutines.flow.Flow
import si.um.feri.budzetko.data.dao.CategoryDao
import si.um.feri.budzetko.data.entity.CategoryBudgetRole
import si.um.feri.budzetko.data.entity.CategoryEntity

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
            userId = userId
        )

        val localId = categoryDao.insertCategory(category)
        val categoryWithId = category.copy(id = localId)

        firestoreRepository.saveCategory(categoryWithId)

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
            budgetRole = budgetRole
        )

        categoryDao.updateCategory(updatedCategory)
        firestoreRepository.saveCategory(updatedCategory)
    }

    suspend fun deleteCategory(category: CategoryEntity) {
        categoryDao.deleteCategory(category)
        firestoreRepository.deleteCategory(category)
    }
}