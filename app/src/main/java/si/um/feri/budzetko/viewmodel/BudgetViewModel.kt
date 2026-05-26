package si.um.feri.budzetko.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import si.um.feri.budzetko.data.entity.BudgetEntity
import si.um.feri.budzetko.data.entity.CategoryEntity
import si.um.feri.budzetko.data.repository.BudgetRepository
import si.um.feri.budzetko.data.repository.CategoryRepository
import si.um.feri.budzetko.data.repository.UserRepository
import si.um.feri.budzetko.data.repository.UserRepository.Companion.DEMO_USER_ID
import si.um.feri.budzetko.domain.budget.BudgetSuggestionEngine
import si.um.feri.budzetko.domain.budget.BudgetTrendStat

class BudgetViewModel(
    private val budgetRepository: BudgetRepository,
    private val categoryRepository: CategoryRepository,
    private val userRepository: UserRepository
) : ViewModel() {
    private val today = LocalDate.now()
    private val formState = MutableStateFlow(
        BudgetUiState(
            month = today.monthValue,
            year = today.year
        )
    )

    val uiState: StateFlow<BudgetUiState> = combine(
        categoryRepository.observeCategories(DEMO_USER_ID),
        formState
    ) { categories, form ->
        val formWithCategories = form.copy(categories = categories)
        if (form.proposedLimits.isEmpty()) {
            formWithCategories
        } else {
            val currentIds = form.proposedLimits.map { it.category.id }.toSet()
            val missingCategories = categories.filterNot { it.id in currentIds }
            if (missingCategories.isEmpty()) {
                formWithCategories
            } else {
                formWithCategories.copy(
                    errorMessage = "Dodana je nova kategorija. Ponovno predlagaj limite, da ostane budget uravnotežen."
                )
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = formState.value
    )

    init {
        viewModelScope.launch {
            userRepository.ensureDemoUser()
        }
    }

    fun openBudgetDialog() {
        loadBudgetForCurrentMonth()
        formState.update {
            it.copy(
                isBudgetDialogOpen = true,
                errorMessage = null,
                successMessage = null
            )
        }
    }

    fun closeBudgetDialog() {
        formState.update {
            it.copy(
                isBudgetDialogOpen = false,
                errorMessage = null,
                successMessage = null
            )
        }
    }

    fun onIncomeChange(value: String) {
        val cleanValue = value.filter { it.isDigit() || it == '.' }.take(10)
        formState.update {
            it.copy(
                incomeInput = cleanValue,
                errorMessage = null,
                successMessage = null
            )
        }
    }

    fun previousMonth() {
        val state = formState.value
        val newMonth = if (state.month == 1) 12 else state.month - 1
        val newYear = if (state.month == 1) state.year - 1 else state.year
        formState.update {
            it.copy(
                month = newMonth,
                year = newYear,
                proposedLimits = emptyList(),
                editBaselinePercents = emptyMap()
            )
        }
        loadBudgetForCurrentMonth()
    }

    fun nextMonth() {
        val state = formState.value
        val newMonth = if (state.month == 12) 1 else state.month + 1
        val newYear = if (state.month == 12) state.year + 1 else state.year
        formState.update {
            it.copy(
                month = newMonth,
                year = newYear,
                proposedLimits = emptyList(),
                editBaselinePercents = emptyMap()
            )
        }
        loadBudgetForCurrentMonth()
    }

    fun proposeLimits(categories: List<CategoryEntity>) {
        val income = formState.value.incomeInput.toDoubleOrNull()
        if (income == null || income <= 0.0) {
            formState.update { it.copy(errorMessage = "Vnesi veljaven mesečni dohodek.") }
            return
        }
        if (categories.isEmpty()) {
            formState.update { it.copy(errorMessage = "Najprej dodaj vsaj eno kategorijo.") }
            return
        }

        viewModelScope.launch {
            val state = formState.value
            val baseSuggestions = BudgetSuggestionEngine.suggestLimits(categories, income)
            val trendStats = loadTrendStats(
                selectedMonth = state.month,
                selectedYear = state.year
            )
            val adjustedSuggestions = BudgetSuggestionEngine.applyTrendAdjustments(
                drafts = baseSuggestions,
                trendStats = trendStats,
                income = income
            )
            val trendMessage = if (trendStats.isEmpty()) {
                "Limiti so predlagani po pravilu 50/30/20. Za trendno prilagoditev dodaj vsaj en pretekli proračun s stroški."
            } else {
                "Limiti so prilagojeni glede na preteklo porabo."
            }
            formState.update {
                it.copy(
                    proposedLimits = adjustedSuggestions,
                    editBaselinePercents = emptyMap(),
                    errorMessage = null,
                    successMessage = trendMessage
                )
            }
        }
    }

    private suspend fun loadTrendStats(
        selectedMonth: Int,
        selectedYear: Int
    ): Map<Long, BudgetTrendStat> {
        val selectedDate = LocalDate.of(selectedYear, selectedMonth, 1)
        val budgets = budgetRepository.getBudgets(DEMO_USER_ID)
            .filter {
                LocalDate.of(it.year, it.month, 1).isBefore(selectedDate)
            }
            .sortedWith(compareByDescending<BudgetEntity> { it.year }.thenByDescending { it.month })
            .take(3)

        val ratiosByCategory = mutableMapOf<Long, MutableList<Double>>()
        budgets.forEach { budget ->
            budgetRepository.getBudgetProgressForMonth(
                userId = DEMO_USER_ID,
                month = budget.month,
                year = budget.year
            ).forEach { progress ->
                if (progress.limitAmount > 0.0) {
                    ratiosByCategory.getOrPut(progress.categoryId) { mutableListOf() }
                        .add(progress.spentAmount / progress.limitAmount)
                }
            }
        }

        return ratiosByCategory.mapValues { (categoryId, ratios) ->
            BudgetTrendStat(
                categoryId = categoryId,
                averageUsageRatio = ratios.average(),
                monthCount = ratios.size
            )
        }
    }

    fun startEditingLimit(categoryId: Long) {
        formState.update { state ->
            val baseline = state.proposedLimits.associate { it.category.id to it.percent }
            state.copy(
                proposedLimits = state.proposedLimits.map {
                    if (it.category.id == categoryId) it.copy(isEditing = true) else it
                },
                editBaselinePercents = baseline
            )
        }
    }

    fun confirmEditingLimit(categoryId: Long) {
        formState.update { state ->
            state.copy(
                proposedLimits = state.proposedLimits.map {
                    if (it.category.id == categoryId) it.copy(isEditing = false) else it
                },
                editBaselinePercents = emptyMap()
            )
        }
    }

    fun updateLimitPercent(categoryId: Long, percent: Float) {
        val income = formState.value.incomeInput.toDoubleOrNull() ?: return
        val roundedPercent = percent.toInt().coerceIn(0, 100)
        formState.update { state ->
            val adjustedDrafts = BudgetSuggestionEngine.redistributeAfterManualChange(
                drafts = state.proposedLimits,
                baselinePercents = state.editBaselinePercents.takeIf { it.isNotEmpty() }
                    ?: state.proposedLimits.associate { it.category.id to it.percent },
                changedCategoryId = categoryId,
                changedPercent = roundedPercent,
                income = income
            )
            state.copy(
                proposedLimits = adjustedDrafts,
                errorMessage = null,
                successMessage = null
            )
        }
    }

    fun saveBudget() {
        viewModelScope.launch {
            val state = formState.value
            val income = state.incomeInput.toDoubleOrNull()
            if (income == null || income <= 0.0) {
                formState.update { it.copy(errorMessage = "Vnesi veljaven mesečni dohodek.") }
                return@launch
            }
            if (state.proposedLimits.isEmpty()) {
                formState.update { it.copy(errorMessage = "Najprej predlagaj limite.") }
                return@launch
            }
            val totalPercent = state.proposedLimits.sumOf { it.percent }
            if (totalPercent > 100) {
                formState.update {
                    it.copy(errorMessage = "Vsota limitov je ${totalPercent} %. Znižaj enega izmed limitov.")
                }
                return@launch
            }

            runCatching {
                budgetRepository.saveBudgetWithLimits(
                    userId = DEMO_USER_ID,
                    month = state.month,
                    year = state.year,
                    income = income,
                    limits = state.proposedLimits.associate { it.category.id to it.limitAmount }
                )
            }.onSuccess {
                formState.update { it.copy(successMessage = "Proračun je shranjen.", errorMessage = null) }
            }.onFailure { throwable ->
                formState.update {
                    it.copy(errorMessage = throwable.message ?: "Proračuna ni bilo mogoče shraniti.")
                }
            }
        }
    }

    private fun loadBudgetForCurrentMonth() {
        viewModelScope.launch {
            val state = formState.value
            val budget = budgetRepository.getBudget(DEMO_USER_ID, state.month, state.year)
            if (budget == null) {
                formState.update {
                    it.copy(
                        proposedLimits = emptyList(),
                        editBaselinePercents = emptyMap(),
                        errorMessage = null,
                        successMessage = null
                    )
                }
                return@launch
            }

            val savedLimits = budgetRepository.getBudgetCategoriesForMonth(
                userId = DEMO_USER_ID,
                month = state.month,
                year = state.year
            )
            val categories = uiState.value.categories
            val drafts = categories.mapNotNull { category ->
                val limit = savedLimits[category.id] ?: return@mapNotNull null
                BudgetLimitDraft(
                    category = category,
                    percent = ((limit / budget.income) * 100.0).toInt().coerceIn(0, 100),
                    limitAmount = limit
                )
            }
            formState.update {
                it.copy(
                    incomeInput = budget.income.toInt().toString(),
                    proposedLimits = drafts,
                    editBaselinePercents = emptyMap(),
                    errorMessage = null,
                    successMessage = null
                )
            }
        }
    }

    class Factory(
        private val budgetRepository: BudgetRepository,
        private val categoryRepository: CategoryRepository,
        private val userRepository: UserRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(BudgetViewModel::class.java)) {
                return BudgetViewModel(budgetRepository, categoryRepository, userRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
