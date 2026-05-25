package si.um.feri.budzetko.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import si.um.feri.budzetko.data.repository.BudgetRepository
import si.um.feri.budzetko.data.repository.CategoryRepository
import si.um.feri.budzetko.data.repository.ExpenseRepository

class DashboardViewModel(
    budgetRepository: BudgetRepository,
    categoryRepository: CategoryRepository,
    expenseRepository: ExpenseRepository
) : ViewModel() {

    private val currentUserId: String =
        FirebaseAuth.getInstance().currentUser?.uid ?: "unknown-user"

    private val today = LocalDate.now()
    private val monthStart = today.withDayOfMonth(1)
    private val nextMonthStart = monthStart.plusMonths(1)
    private val startMillis = monthStart.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    private val endMillis = nextMonthStart.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() - 1

    val uiState = combine(
        budgetRepository.observeBudget(currentUserId, today.monthValue, today.year),
        budgetRepository.observeCategoriesWithMonthlyLimit(currentUserId, today.monthValue, today.year),
        categoryRepository.observeCategories(currentUserId),
        expenseRepository.observeExpenses(currentUserId)
    ) { budget, categoriesWithLimits, categories, expenses ->

        val currentMonthExpenses = expenses.filter { it.date in startMillis..endMillis }
        val categoryById = categories.associateBy { it.id }
        val limitsById = categoriesWithLimits.associateBy { it.id }

        val spentByCategory = currentMonthExpenses
            .groupBy { it.categoryId }
            .mapValues { entry -> entry.value.sumOf { it.amount } }

        val categorySpending = categories.map { category ->
            DashboardCategorySpending(
                categoryId = category.id,
                categoryName = category.name,
                emoji = category.emoji,
                colorIndex = category.colorIndex,
                spentAmount = spentByCategory[category.id] ?: 0.0,
                limitAmount = limitsById[category.id]?.limitAmount
            )
        }.sortedByDescending { it.spentAmount }

        val recentTransactions = expenses.take(4).map { expense ->
            val category = categoryById[expense.categoryId]

            DashboardTransaction(
                expense = expense,
                categoryName = category?.name ?: "Brez kategorije",
                categoryEmoji = category?.emoji,
                categoryColorIndex = category?.colorIndex ?: 0
            )
        }

        DashboardUiState(
            month = today.monthValue,
            year = today.year,
            totalBudget = budget?.income ?: categoriesWithLimits.sumOf { it.limitAmount ?: 0.0 },
            totalSpent = currentMonthExpenses.sumOf { it.amount },
            categorySpending = categorySpending,
            recentTransactions = recentTransactions
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DashboardUiState(month = today.monthValue, year = today.year)
    )

    class Factory(
        private val budgetRepository: BudgetRepository,
        private val categoryRepository: CategoryRepository,
        private val expenseRepository: ExpenseRepository
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
                return DashboardViewModel(
                    budgetRepository = budgetRepository,
                    categoryRepository = categoryRepository,
                    expenseRepository = expenseRepository
                ) as T
            }

            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}