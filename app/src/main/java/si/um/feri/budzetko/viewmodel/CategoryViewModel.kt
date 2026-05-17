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
import si.um.feri.budzetko.data.entity.CategoryBudgetRole
import si.um.feri.budzetko.data.entity.CategoryEntity
import si.um.feri.budzetko.data.repository.BudgetRepository
import si.um.feri.budzetko.data.repository.CategoryRepository
import si.um.feri.budzetko.data.repository.UserRepository
import si.um.feri.budzetko.data.repository.UserRepository.Companion.DEMO_USER_ID
import si.um.feri.budzetko.domain.budget.BudgetSuggestionEngine

class CategoryViewModel(
    private val categoryRepository: CategoryRepository,
    private val budgetRepository: BudgetRepository,
    private val userRepository: UserRepository
) : ViewModel() {
    private val formState = MutableStateFlow(CategoryUiState())
    private val today = LocalDate.now()

    val uiState: StateFlow<CategoryUiState> = combine(
        categoryRepository.observeCategories(DEMO_USER_ID),
        budgetRepository.observeBudget(
            userId = DEMO_USER_ID,
            month = today.monthValue,
            year = today.year
        ),
        budgetRepository.observeCategoriesWithMonthlyLimit(
            userId = DEMO_USER_ID,
            month = today.monthValue,
            year = today.year
        ),
        formState
    ) { categories, currentBudget, categoriesWithLimits, form ->
        val limitsByCategoryId = categoriesWithLimits.associateBy { it.id }
        val categoryItems = categories.map { category ->
            CategoryListItem(
                category = category,
                monthlyLimit = limitsByCategoryId[category.id]?.limitAmount
            )
        }
        form.copy(
            categories = categories,
            categoryItems = categoryItems,
            currentMonthIncome = currentBudget?.income
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = CategoryUiState()
    )

    init {
        viewModelScope.launch {
            userRepository.ensureDemoUser()
        }
    }

    fun openCreateDialog() {
        formState.update {
            it.copy(
                nameInput = "",
                limitInput = "50",
                limitSliderValue = 50f,
                selectedEmoji = null,
                selectedColorIndex = 0,
                selectedBudgetRole = CategoryBudgetRole.OTHER,
                isEmojiPickerOpen = false,
                editingCategory = null,
                isDialogOpen = true,
                errorMessage = null
            )
        }
    }

    fun openEditDialog(item: CategoryListItem) {
        val category = item.category
        val limit = item.monthlyLimit
        val limitInput = limit?.toInt()?.toString() ?: "50"
        val sliderValue = limit?.toFloat() ?: 50f
        formState.update {
            it.copy(
                nameInput = category.name,
                limitInput = limitInput,
                limitSliderValue = sliderValue,
                selectedEmoji = category.emoji,
                selectedColorIndex = category.colorIndex,
                selectedBudgetRole = category.budgetRole,
                isEmojiPickerOpen = false,
                editingCategory = category,
                isDialogOpen = true,
                errorMessage = null
            )
        }
    }

    fun closeDialog() {
        formState.update {
            it.copy(
                nameInput = "",
                limitInput = "50",
                limitSliderValue = 50f,
                selectedEmoji = null,
                selectedColorIndex = 0,
                selectedBudgetRole = CategoryBudgetRole.OTHER,
                isEmojiPickerOpen = false,
                editingCategory = null,
                isDialogOpen = false,
                errorMessage = null
            )
        }
    }

    fun onNameChange(value: String) {
        formState.update {
            it.copy(
                nameInput = value,
                errorMessage = null
            )
        }
    }

    fun onLimitInputChange(value: String) {
        val cleanValue = value.filter { it.isDigit() }.take(5)
        val maxLimit = uiState.value.currentMonthIncome?.toFloat()?.coerceAtLeast(1f) ?: 2_000f
        val sliderValue = cleanValue.toFloatOrNull()?.coerceIn(0f, maxLimit) ?: 0f
        formState.update {
            it.copy(
                limitInput = cleanValue,
                limitSliderValue = sliderValue
            )
        }
    }

    fun onLimitSliderChange(value: Float) {
        val roundedValue = value.toInt().toString()
        formState.update {
            it.copy(
                limitInput = roundedValue,
                limitSliderValue = value
            )
        }
    }

    fun suggestCurrentCategoryLimit() {
        val state = uiState.value
        val income = state.currentMonthIncome
        if (income == null || income <= 0.0) {
            formState.update { it.copy(errorMessage = "Najprej nastavi mesečni proračun.") }
            return
        }

        val editingCategory = state.editingCategory
        val suggestionCategory = editingCategory ?: CategoryEntity(
            id = NEW_CATEGORY_SUGGESTION_ID,
            name = state.nameInput.ifBlank { "Nova kategorija" },
            emoji = state.selectedEmoji,
            colorIndex = state.selectedColorIndex,
            budgetRole = state.selectedBudgetRole,
            userId = DEMO_USER_ID
        )
        val categoriesForSuggestion = if (editingCategory == null) {
            state.categories + suggestionCategory
        } else {
            state.categories.map {
                if (it.id == editingCategory.id) {
                    it.copy(budgetRole = state.selectedBudgetRole)
                } else {
                    it
                }
            }
        }

        val suggestedDraft = BudgetSuggestionEngine
            .suggestLimits(categoriesForSuggestion, income)
            .firstOrNull { it.category.id == suggestionCategory.id }
            ?: return

        formState.update {
            it.copy(
                limitInput = suggestedDraft.limitAmount.toInt().toString(),
                limitSliderValue = suggestedDraft.limitAmount.toFloat(),
                errorMessage = null
            )
        }
    }

    private companion object {
        const val NEW_CATEGORY_SUGGESTION_ID = -1L
    }

    fun onColorSelected(index: Int) {
        formState.update {
            it.copy(
                selectedColorIndex = index,
                selectedEmoji = null
            )
        }
    }

    fun onBudgetRoleSelected(role: CategoryBudgetRole) {
        formState.update {
            it.copy(selectedBudgetRole = role)
        }
    }

    fun openEmojiPicker() {
        formState.update { it.copy(isEmojiPickerOpen = true) }
    }

    fun closeEmojiPicker() {
        formState.update { it.copy(isEmojiPickerOpen = false) }
    }

    fun onEmojiSelected(emoji: String) {
        formState.update {
            it.copy(
                selectedEmoji = emoji,
                isEmojiPickerOpen = false
            )
        }
    }

    fun saveCategory() {
        viewModelScope.launch {
            val state = formState.value
            val name = state.nameInput.trim()
            if (name.isBlank()) {
                formState.update { it.copy(errorMessage = "Vnesi ime kategorije.") }
                return@launch
            }

            runCatching {
                val editingCategory = state.editingCategory
                if (editingCategory == null) {
                    val newCategoryId = categoryRepository.addCategory(
                        name = name,
                        userId = DEMO_USER_ID,
                        emoji = state.selectedEmoji,
                        colorIndex = state.selectedColorIndex,
                        budgetRole = state.selectedBudgetRole
                    )
                    val limitAmount = state.limitInput.toDoubleOrNull()
                    val income = uiState.value.currentMonthIncome
                    if (limitAmount != null && income != null && income > 0.0) {
                        validateCurrentMonthLimit(limitAmount)
                        val newCategory = CategoryEntity(
                            id = newCategoryId,
                            name = name,
                            emoji = state.selectedEmoji,
                            colorIndex = state.selectedColorIndex,
                            budgetRole = state.selectedBudgetRole,
                            userId = DEMO_USER_ID
                        )
                        saveCurrentMonthLimitAndRedistribute(
                            categoryId = newCategoryId,
                            limitAmount = limitAmount,
                            categoriesOverride = uiState.value.categories + newCategory
                        )
                    }
                } else {
                    val limitAmount = state.limitInput.toDoubleOrNull()
                    validateCurrentMonthLimit(limitAmount)
                    categoryRepository.updateCategory(
                        category = editingCategory,
                        name = name,
                        emoji = state.selectedEmoji,
                        colorIndex = state.selectedColorIndex,
                        budgetRole = state.selectedBudgetRole
                    )
                    saveCurrentMonthLimitAndRedistribute(
                        categoryId = editingCategory.id,
                        limitAmount = limitAmount,
                        categoriesOverride = null
                    )
                }
            }.onSuccess {
                closeDialog()
            }.onFailure { throwable ->
                formState.update {
                    it.copy(errorMessage = throwable.message ?: "Kategorije ni bilo mogoče shraniti.")
                }
            }
        }
    }

    private suspend fun saveCurrentMonthLimitAndRedistribute(
        categoryId: Long,
        limitAmount: Double?,
        categoriesOverride: List<CategoryEntity>?
    ) {
        if (limitAmount == null || limitAmount < 0.0) return
        val currentIncome = uiState.value.currentMonthIncome
        val income = currentIncome?.takeIf { it > 0.0 }
        if (income == null) {
            formState.update { it.copy(errorMessage = "Najprej nastavi mesečni proračun.") }
            return
        }
        if (limitAmount > income) {
            formState.update { it.copy(errorMessage = "Limit ne sme biti višji od mesečnega dohodka.") }
            return
        }

        val existingLimits = budgetRepository.getBudgetCategoriesForMonth(
            userId = DEMO_USER_ID,
            month = today.monthValue,
            year = today.year
        )
        val categories = categoriesOverride ?: uiState.value.categories
        val baselineDrafts = categories.map { category ->
            val currentLimit = existingLimits[category.id] ?: 0.0
            BudgetLimitDraft(
                category = category,
                percent = ((currentLimit / income) * 100.0).toInt().coerceIn(0, 100),
                limitAmount = currentLimit
            )
        }
        val redistributedDrafts = BudgetSuggestionEngine.redistributeAfterExactLimitAmountChange(
            drafts = baselineDrafts,
            changedCategoryId = categoryId,
            changedLimitAmount = limitAmount,
            income = income
        )
        budgetRepository.saveBudgetWithLimits(
            userId = DEMO_USER_ID,
            month = today.monthValue,
            year = today.year,
            income = income,
            limits = redistributedDrafts.associate { it.category.id to it.limitAmount }
        )
    }

    private fun validateCurrentMonthLimit(limitAmount: Double?) {
        val income = uiState.value.currentMonthIncome?.takeIf { it > 0.0 }
            ?: throw IllegalStateException("Najprej nastavi mesečni proračun.")
        if (limitAmount == null || limitAmount < 0.0) {
            throw IllegalArgumentException("Vnesi veljaven limit.")
        }
        if (limitAmount > income) {
            throw IllegalArgumentException("Limit ne sme biti višji od mesečnega dohodka.")
        }
    }

    fun requestDeleteCategory(category: CategoryEntity) {
        formState.update {
            it.copy(
                categoryPendingDelete = category,
                errorMessage = null
            )
        }
    }

    fun cancelDeleteCategory() {
        formState.update {
            it.copy(
                categoryPendingDelete = null,
                errorMessage = null
            )
        }
    }

    fun confirmDeleteCategory() {
        val category = formState.value.categoryPendingDelete ?: return
        viewModelScope.launch {
            runCatching {
                val income = uiState.value.currentMonthIncome?.takeIf { it > 0.0 }
                val remainingCategories = uiState.value.categories.filterNot { it.id == category.id }
                val existingLimits = if (income != null && remainingCategories.isNotEmpty()) {
                    budgetRepository.getBudgetCategoriesForMonth(
                        userId = DEMO_USER_ID,
                        month = today.monthValue,
                        year = today.year
                    )
                } else {
                    emptyMap()
                }

                categoryRepository.deleteCategory(category)

                if (income != null && remainingCategories.isNotEmpty()) {
                    val baselineDrafts = remainingCategories.map { remainingCategory ->
                        val currentLimit = existingLimits[remainingCategory.id] ?: 0.0
                        BudgetLimitDraft(
                            category = remainingCategory,
                            percent = ((currentLimit / income) * 100.0).toInt().coerceIn(0, 100),
                            limitAmount = currentLimit
                        )
                    }
                    val redistributedDrafts = BudgetSuggestionEngine.redistributeAfterCategoryRemoval(
                        remainingDrafts = baselineDrafts,
                        income = income
                    )
                    budgetRepository.saveBudgetWithLimits(
                        userId = DEMO_USER_ID,
                        month = today.monthValue,
                        year = today.year,
                        income = income,
                        limits = redistributedDrafts.associate { it.category.id to it.limitAmount }
                    )
                }
            }.onFailure { throwable ->
                formState.update {
                    it.copy(errorMessage = throwable.message ?: "Kategorije ni bilo mogoče izbrisati.")
                }
            }.onSuccess {
                formState.update {
                    it.copy(
                        categoryPendingDelete = null,
                        errorMessage = null
                    )
                }
            }
        }
    }

    class Factory(
        private val categoryRepository: CategoryRepository,
        private val budgetRepository: BudgetRepository,
        private val userRepository: UserRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(CategoryViewModel::class.java)) {
                return CategoryViewModel(categoryRepository, budgetRepository, userRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
