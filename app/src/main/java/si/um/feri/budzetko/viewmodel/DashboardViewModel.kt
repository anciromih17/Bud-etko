package si.um.feri.budzetko.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import si.um.feri.budzetko.data.entity.BudgetEntity
import si.um.feri.budzetko.data.entity.CategoryEntity
import si.um.feri.budzetko.data.entity.ExpenseEntity
import si.um.feri.budzetko.data.entity.AiSummaryEntity
import si.um.feri.budzetko.data.entity.AiSummarySource
import si.um.feri.budzetko.data.model.CategoryWithMonthlyLimit
import kotlinx.coroutines.launch
import si.um.feri.budzetko.data.repository.AiSummaryRepository
import si.um.feri.budzetko.data.repository.BudgetRepository
import si.um.feri.budzetko.data.repository.CategoryRepository
import si.um.feri.budzetko.data.repository.ExpenseRepository
import si.um.feri.budzetko.data.repository.UserRepository.Companion.DEMO_USER_ID
import si.um.feri.budzetko.domain.ai.AiRecommendationService

class DashboardViewModel(
    budgetRepository: BudgetRepository,
    categoryRepository: CategoryRepository,
    expenseRepository: ExpenseRepository,
    private val aiSummaryRepository: AiSummaryRepository,
    private val aiRecommendationService: AiRecommendationService
) : ViewModel() {
    private val today = LocalDate.now()
    private val monthStart = today.withDayOfMonth(1)
    private val nextMonthStart = monthStart.plusMonths(1)
    private val startMillis = monthStart.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    private val endMillis = nextMonthStart.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() - 1
    private val isAiSummaryGenerating = MutableStateFlow(false)

    private val dashboardSources = combine(
        budgetRepository.observeBudget(DEMO_USER_ID, today.monthValue, today.year),
        budgetRepository.observeCategoriesWithMonthlyLimit(DEMO_USER_ID, today.monthValue, today.year),
        categoryRepository.observeCategories(DEMO_USER_ID),
        expenseRepository.observeExpenses(DEMO_USER_ID),
        aiSummaryRepository.observeSummary(DEMO_USER_ID, today.monthValue, today.year)
    ) { budget, categoriesWithLimits, categories, expenses, aiSummary ->
        DashboardSources(
            budget = budget,
            categoriesWithLimits = categoriesWithLimits,
            categories = categories,
            expenses = expenses,
            aiSummary = aiSummary
        )
    }

    val uiState = combine(
        dashboardSources,
        isAiSummaryGenerating
    ) { sources, isGenerating ->
        val budget = sources.budget
        val categoriesWithLimits = sources.categoriesWithLimits
        val categories = sources.categories
        val expenses = sources.expenses
        val aiSummary = sources.aiSummary
        val currentMonthExpenses = expenses.filter { it.date in startMillis..endMillis }
        val categoryById = categories.associateBy { it.id }
        val limitsById = categoriesWithLimits.associateBy { it.id }
        val spentByCategory = currentMonthExpenses.groupBy { it.categoryId }
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
            aiSummary = aiSummary?.summary,
            aiSummarySource = aiSummary?.source,
            isAiSummaryGenerating = isGenerating,
            categorySpending = categorySpending,
            recentTransactions = recentTransactions
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DashboardUiState(month = today.monthValue, year = today.year)
    )

    fun saveAiSummary(summary: String) {
        viewModelScope.launch {
            aiSummaryRepository.saveSummary(
                userId = DEMO_USER_ID,
                month = today.monthValue,
                year = today.year,
                summary = summary,
                source = AiSummarySource.FALLBACK
            )
        }
    }

    fun generateAndSaveAiSummary(snapshot: DashboardUiState) {
        viewModelScope.launch {
            isAiSummaryGenerating.update { true }
            try {
                val result = aiRecommendationService.generate(snapshot)
                aiSummaryRepository.saveSummary(
                    userId = DEMO_USER_ID,
                    month = today.monthValue,
                    year = today.year,
                    summary = result.summary,
                    source = when (result.source) {
                        si.um.feri.budzetko.domain.ai.AiRecommendationSource.GEMINI -> AiSummarySource.GEMINI
                        si.um.feri.budzetko.domain.ai.AiRecommendationSource.FALLBACK -> AiSummarySource.FALLBACK
                    }
                )
            } finally {
                isAiSummaryGenerating.update { false }
            }
        }
    }

    class Factory(
        private val budgetRepository: BudgetRepository,
        private val categoryRepository: CategoryRepository,
        private val expenseRepository: ExpenseRepository,
        private val aiSummaryRepository: AiSummaryRepository,
        private val aiRecommendationService: AiRecommendationService
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
                return DashboardViewModel(
                    budgetRepository = budgetRepository,
                    categoryRepository = categoryRepository,
                    expenseRepository = expenseRepository,
                    aiSummaryRepository = aiSummaryRepository,
                    aiRecommendationService = aiRecommendationService
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

private data class DashboardSources(
    val budget: BudgetEntity?,
    val categoriesWithLimits: List<CategoryWithMonthlyLimit>,
    val categories: List<CategoryEntity>,
    val expenses: List<ExpenseEntity>,
    val aiSummary: AiSummaryEntity?
)
