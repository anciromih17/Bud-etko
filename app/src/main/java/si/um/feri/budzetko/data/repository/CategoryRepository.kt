package si.um.feri.budzetko.data.repository

import kotlinx.coroutines.flow.Flow
import si.um.feri.budzetko.data.dao.CategoryDao
import si.um.feri.budzetko.data.entity.CategoryBudgetRole
import si.um.feri.budzetko.data.entity.CategoryEntity

class CategoryRepository(
    private val categoryDao: CategoryDao
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
        return categoryDao.insertCategory(
            CategoryEntity(
                name = cleanName,
                emoji = emoji,
                colorIndex = colorIndex,
                budgetRole = budgetRole,
                userId = userId
            )
        )
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
        categoryDao.updateCategory(
            category.copy(
                name = cleanName,
                emoji = emoji,
                colorIndex = colorIndex,
                budgetRole = budgetRole
            )
        )
    }

    suspend fun deleteCategory(category: CategoryEntity) {
        categoryDao.deleteCategory(category)
    }
}
