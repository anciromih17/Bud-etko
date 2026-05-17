package si.um.feri.budzetko.viewmodel

import si.um.feri.budzetko.data.entity.CategoryEntity

data class BudgetLimitDraft(
    val category: CategoryEntity,
    val percent: Int,
    val limitAmount: Double,
    val isEditing: Boolean = false
)
