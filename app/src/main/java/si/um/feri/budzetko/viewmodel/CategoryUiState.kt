package si.um.feri.budzetko.viewmodel

import si.um.feri.budzetko.data.entity.CategoryEntity
import si.um.feri.budzetko.data.entity.CategoryBudgetRole

data class CategoryUiState(
    val categories: List<CategoryEntity> = emptyList(),
    val categoryItems: List<CategoryListItem> = emptyList(),
    val currentMonthIncome: Double? = null,
    val nameInput: String = "",
    val limitInput: String = "50",
    val limitSliderValue: Float = 50f,
    val selectedEmoji: String? = null,
    val selectedColorIndex: Int = 0,
    val selectedBudgetRole: CategoryBudgetRole = CategoryBudgetRole.OTHER,
    val isEmojiPickerOpen: Boolean = false,
    val editingCategory: CategoryEntity? = null,
    val categoryPendingDelete: CategoryEntity? = null,
    val isDialogOpen: Boolean = false,
    val errorMessage: String? = null
)
