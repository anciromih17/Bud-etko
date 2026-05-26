package si.um.feri.budzetko.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import java.time.Instant
import java.time.ZoneId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import si.um.feri.budzetko.data.entity.AiSummaryEntity
import si.um.feri.budzetko.data.entity.BudgetEntity
import si.um.feri.budzetko.data.entity.ExpenseEntity
import si.um.feri.budzetko.data.repository.AiSummaryRepository
import si.um.feri.budzetko.data.repository.BudgetRepository
import si.um.feri.budzetko.data.repository.ExpenseRepository

class BudgetHistoryViewModel(
    budgetRepository: BudgetRepository,
    expenseRepository: ExpenseRepository,
    aiSummaryRepository: AiSummaryRepository
) : ViewModel() {
    private val currentUserId: String =
        FirebaseAuth.getInstance().currentUser?.uid ?: "unknown-user"

    private val searchQuery = MutableStateFlow("")
    private val expandedMonthKey = MutableStateFlow<String?>(null)

    private val historySources = combine(
        budgetRepository.observeBudgets(currentUserId),
        expenseRepository.observeExpenses(currentUserId),
        aiSummaryRepository.observeSummaries(currentUserId)
    ) { budgets, expenses, summaries ->
        BudgetHistorySources(
            budgets = budgets,
            expenses = expenses,
            summaries = summaries
        )
    }

    val uiState: StateFlow<BudgetHistoryUiState> = combine(
        historySources,
        searchQuery,
        expandedMonthKey
    ) { sources, search, expandedKey ->
        val summariesByMonth = sources.summaries.associateBy { it.monthKey }
        val allItems = sources.budgets.map { budget ->
            val monthExpenses = sources.expenses.filterForBudgetMonth(budget)
            val spent = monthExpenses.sumOf { it.amount }
            val progressPercent = if (budget.income > 0.0) {
                ((spent / budget.income) * 100.0).toInt().coerceAtLeast(0)
            } else {
                0
            }
            BudgetHistoryItem(
                month = budget.month,
                year = budget.year,
                income = budget.income,
                spent = spent,
                progressPercent = progressPercent,
                aiSummary = summariesByMonth[budget.monthKey]?.summary
            )
        }

        val filteredItems = if (search.isBlank()) {
            allItems
        } else {
            allItems.filter { item ->
                monthName(item.month).contains(search, ignoreCase = true) ||
                    item.year.toString().contains(search) ||
                    item.aiSummary?.contains(search, ignoreCase = true) == true
            }
        }

        BudgetHistoryUiState(
            searchQuery = search,
            expandedMonthKey = expandedKey,
            items = filteredItems
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = BudgetHistoryUiState()
    )

    fun onSearchChange(value: String) {
        searchQuery.value = value
    }

    fun toggleMonth(month: Int, year: Int) {
        val key = monthKey(month, year)
        expandedMonthKey.update { currentKey ->
            if (currentKey == key) null else key
        }
    }

    class Factory(
        private val budgetRepository: BudgetRepository,
        private val expenseRepository: ExpenseRepository,
        private val aiSummaryRepository: AiSummaryRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(BudgetHistoryViewModel::class.java)) {
                return BudgetHistoryViewModel(
                    budgetRepository = budgetRepository,
                    expenseRepository = expenseRepository,
                    aiSummaryRepository = aiSummaryRepository
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

data class BudgetHistoryUiState(
    val searchQuery: String = "",
    val expandedMonthKey: String? = null,
    val items: List<BudgetHistoryItem> = emptyList()
)

data class BudgetHistoryItem(
    val month: Int,
    val year: Int,
    val income: Double,
    val spent: Double,
    val progressPercent: Int,
    val aiSummary: String?
) {
    val monthKey: String
        get() = monthKey(month, year)

    val remaining: Double
        get() = (income - spent).coerceAtLeast(0.0)
}

private val BudgetEntity.monthKey: String
    get() = monthKey(month, year)

private data class BudgetHistorySources(
    val budgets: List<BudgetEntity>,
    val expenses: List<ExpenseEntity>,
    val summaries: List<AiSummaryEntity>
)

private val AiSummaryEntity.monthKey: String
    get() = monthKey(month, year)

private fun List<ExpenseEntity>.filterForBudgetMonth(budget: BudgetEntity): List<ExpenseEntity> {
    return filter { expense ->
        val date = Instant.ofEpochMilli(expense.date).atZone(ZoneId.systemDefault()).toLocalDate()
        date.monthValue == budget.month && date.year == budget.year
    }.sortedByDescending { it.date }
}

private fun monthKey(month: Int, year: Int): String = "$year-$month"

fun monthName(month: Int): String {
    return when (month) {
        1 -> "Januar"
        2 -> "Februar"
        3 -> "Marec"
        4 -> "April"
        5 -> "Maj"
        6 -> "Junij"
        7 -> "Julij"
        8 -> "Avgust"
        9 -> "September"
        10 -> "Oktober"
        11 -> "November"
        12 -> "December"
        else -> month.toString()
    }
}
