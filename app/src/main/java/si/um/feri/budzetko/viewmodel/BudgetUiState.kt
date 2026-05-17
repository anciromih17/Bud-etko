package si.um.feri.budzetko.viewmodel

import si.um.feri.budzetko.data.entity.CategoryEntity

data class BudgetUiState(
    val month: Int = 1,
    val year: Int = 2026,
    val incomeInput: String = "2000",
    val categories: List<CategoryEntity> = emptyList(),
    val proposedLimits: List<BudgetLimitDraft> = emptyList(),
    val editBaselinePercents: Map<Long, Int> = emptyMap(),
    val isBudgetDialogOpen: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)
