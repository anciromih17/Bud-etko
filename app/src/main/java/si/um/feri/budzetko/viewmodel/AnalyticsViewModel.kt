package si.um.feri.budzetko.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import java.time.YearMonth
import java.time.ZoneId
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import si.um.feri.budzetko.data.entity.AiSummaryEntity
import si.um.feri.budzetko.data.entity.AiSummarySource
import si.um.feri.budzetko.data.entity.BudgetEntity
import si.um.feri.budzetko.data.entity.CategoryEntity
import si.um.feri.budzetko.data.entity.ExpenseEntity
import si.um.feri.budzetko.data.model.CategoryWithMonthlyLimit
import si.um.feri.budzetko.data.repository.AiSummaryRepository
import si.um.feri.budzetko.data.repository.BudgetRepository
import si.um.feri.budzetko.data.repository.CategoryRepository
import si.um.feri.budzetko.data.repository.ExpenseRepository
import si.um.feri.budzetko.domain.ai.AiRecommendationService
import si.um.feri.budzetko.domain.ai.AiRecommendationSource

@OptIn(ExperimentalCoroutinesApi::class)
class AnalyticsViewModel(
    private val budgetRepository: BudgetRepository,
    private val categoryRepository: CategoryRepository,
    private val expenseRepository: ExpenseRepository,
    private val aiSummaryRepository: AiSummaryRepository,
    private val aiRecommendationService: AiRecommendationService
) : ViewModel() {
    private val currentUserId: String =
        FirebaseAuth.getInstance().currentUser?.uid ?: "unknown-user"

    private val selectedMonth = MutableStateFlow(YearMonth.now())
    private val isAiSummaryGenerating = MutableStateFlow(false)

    private fun analyticsSources(month: YearMonth) = combine(
        budgetRepository.observeBudget(currentUserId, month.monthValue, month.year),
        budgetRepository.observeCategoriesWithMonthlyLimit(currentUserId, month.monthValue, month.year),
        categoryRepository.observeCategories(currentUserId),
        expenseRepository.observeExpenses(currentUserId),
        aiSummaryRepository.observeSummary(currentUserId, month.monthValue, month.year)
    ) { budget, categoriesWithLimits, categories, expenses, aiSummary ->
        AnalyticsSources(
            month = month,
            budget = budget,
            categoriesWithLimits = categoriesWithLimits,
            categories = categories,
            expenses = expenses,
            aiSummary = aiSummary
        )
    }

    val uiState = selectedMonth
        .flatMapLatest { month ->
            combine(
                analyticsSources(month),
                isAiSummaryGenerating
            ) { sources, isGenerating ->
                sources.toUiState(isGenerating)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = DashboardUiState(
                month = selectedMonth.value.monthValue,
                year = selectedMonth.value.year
            )
        )

    fun previousMonth() {
        selectedMonth.update { it.minusMonths(1) }
    }

    fun nextMonth() {
        selectedMonth.update { it.plusMonths(1) }
    }

    fun generateAndSaveAiSummary(snapshot: DashboardUiState) {
        viewModelScope.launch {
            isAiSummaryGenerating.update { true }
            try {
                val result = aiRecommendationService.generate(snapshot)
                aiSummaryRepository.saveSummary(
                    userId = currentUserId,
                    month = snapshot.month,
                    year = snapshot.year,
                    summary = result.summary,
                    source = when (result.source) {
                        AiRecommendationSource.GEMINI -> AiSummarySource.GEMINI
                        AiRecommendationSource.FALLBACK -> AiSummarySource.FALLBACK
                    }
                )
            } finally {
                isAiSummaryGenerating.update { false }
            }
        }
    }

    private fun AnalyticsSources.toUiState(isGenerating: Boolean): DashboardUiState {
        val monthStart = month.atDay(1)
        val nextMonthStart = month.plusMonths(1).atDay(1)
        val startMillis = monthStart.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endMillis = nextMonthStart.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() - 1
        val monthExpenses = expenses.filter { it.date in startMillis..endMillis }
        val categoryById = categories.associateBy { it.id }
        val limitsById = categoriesWithLimits.associateBy { it.id }
        val spentByCategory = monthExpenses.groupBy { it.categoryId }
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
        }.sortedWith(
            compareByDescending<DashboardCategorySpending> { it.spentAmount }
                .thenBy { it.categoryName.lowercase() }
        )

        val recentTransactions = monthExpenses
            .sortedByDescending { it.date }
            .take(4)
            .map { expense ->
                val category = categoryById[expense.categoryId]
                DashboardTransaction(
                    expense = expense,
                    categoryName = category?.name ?: "Brez kategorije",
                    categoryEmoji = category?.emoji,
                    categoryColorIndex = category?.colorIndex ?: 0
                )
            }

        return DashboardUiState(
            month = month.monthValue,
            year = month.year,
            totalBudget = budget?.income ?: categoriesWithLimits.sumOf { it.limitAmount ?: 0.0 },
            totalSpent = monthExpenses.sumOf { it.amount },
            aiSummary = aiSummary?.summary,
            aiSummarySource = aiSummary?.source,
            isAiSummaryGenerating = isGenerating,
            categorySpending = categorySpending,
            recentTransactions = recentTransactions
        )
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
            if (modelClass.isAssignableFrom(AnalyticsViewModel::class.java)) {
                return AnalyticsViewModel(
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

private data class AnalyticsSources(
    val month: YearMonth,
    val budget: BudgetEntity?,
    val categoriesWithLimits: List<CategoryWithMonthlyLimit>,
    val categories: List<CategoryEntity>,
    val expenses: List<ExpenseEntity>,
    val aiSummary: AiSummaryEntity?
)
