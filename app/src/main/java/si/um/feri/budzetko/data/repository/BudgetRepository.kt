package si.um.feri.budzetko.data.repository

import java.time.LocalDate
import java.time.ZoneId
import si.um.feri.budzetko.data.dao.BudgetDao
import si.um.feri.budzetko.data.entity.BudgetCategoryEntity
import si.um.feri.budzetko.data.entity.BudgetEntity
import si.um.feri.budzetko.data.model.BudgetCategoryProgress
import si.um.feri.budzetko.data.model.CategoryWithMonthlyLimit
import kotlinx.coroutines.flow.Flow

class BudgetRepository(
    private val budgetDao: BudgetDao
) {
    suspend fun getBudget(userId: String, month: Int, year: Int): BudgetEntity? {
        return budgetDao.getBudget(userId, month, year)
    }

    fun observeCategoriesWithMonthlyLimit(
        userId: String,
        month: Int,
        year: Int
    ): Flow<List<CategoryWithMonthlyLimit>> {
        return budgetDao.observeCategoriesWithMonthlyLimit(userId, month, year)
    }

    fun observeBudget(userId: String, month: Int, year: Int): Flow<BudgetEntity?> {
        return budgetDao.observeBudget(userId, month, year)
    }

    fun observeBudgets(userId: String): Flow<List<BudgetEntity>> {
        return budgetDao.observeBudgets(userId)
    }

    suspend fun getBudgets(userId: String): List<BudgetEntity> {
        return budgetDao.getBudgets(userId)
    }

    suspend fun getBudgetProgressForMonth(
        userId: String,
        month: Int,
        year: Int
    ): List<BudgetCategoryProgress> {
        val monthStart = LocalDate.of(year, month, 1)
        val nextMonthStart = monthStart.plusMonths(1)
        val startMillis = monthStart.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endMillis = nextMonthStart.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() - 1
        return budgetDao.getBudgetProgress(
            userId = userId,
            month = month,
            year = year,
            startDate = startMillis,
            endDate = endMillis
        )
    }

    suspend fun getBudgetCategoriesForMonth(
        userId: String,
        month: Int,
        year: Int
    ): Map<Long, Double> {
        val budget = budgetDao.getBudget(userId, month, year) ?: return emptyMap()
        return budgetDao.getBudgetCategories(budget.id).associate {
            it.categoryId to it.limitAmount
        }
    }

    suspend fun saveBudgetWithLimits(
        userId: String,
        month: Int,
        year: Int,
        income: Double,
        limits: Map<Long, Double>
    ) {
        val budget = budgetDao.getBudget(userId, month, year)
        val budgetId = if (budget == null) {
            budgetDao.insertBudget(
                BudgetEntity(
                    month = month,
                    year = year,
                    income = income,
                    userId = userId
                )
            )
        } else {
            budgetDao.updateBudget(budget.copy(income = income))
            budget.id
        }

        limits.forEach { (categoryId, limitAmount) ->
            val existingLimit = budgetDao.getBudgetCategory(budgetId, categoryId)
            if (existingLimit == null) {
                budgetDao.insertBudgetCategory(
                    BudgetCategoryEntity(
                        limitAmount = limitAmount,
                        budgetId = budgetId,
                        categoryId = categoryId
                    )
                )
            } else {
                budgetDao.updateBudgetCategory(existingLimit.copy(limitAmount = limitAmount))
            }
        }
    }
}
