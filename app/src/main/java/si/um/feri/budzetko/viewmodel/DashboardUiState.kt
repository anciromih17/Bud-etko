package si.um.feri.budzetko.viewmodel

import si.um.feri.budzetko.data.entity.ExpenseEntity

data class DashboardUiState(
    val month: Int,
    val year: Int,
    val totalBudget: Double = 0.0,
    val totalSpent: Double = 0.0,
    val categorySpending: List<DashboardCategorySpending> = emptyList(),
    val recentTransactions: List<DashboardTransaction> = emptyList()
) {
    val available: Double
        get() = (totalBudget - totalSpent).coerceAtLeast(0.0)

    val hasBudget: Boolean
        get() = totalBudget > 0.0
}

data class DashboardCategorySpending(
    val categoryId: Long,
    val categoryName: String,
    val emoji: String?,
    val colorIndex: Int,
    val spentAmount: Double,
    val limitAmount: Double?
) {
    val progress: Float
        get() = if (limitAmount == null || limitAmount <= 0.0) {
            0f
        } else {
            (spentAmount / limitAmount).toFloat().coerceAtLeast(0f)
        }
}

data class DashboardTransaction(
    val expense: ExpenseEntity,
    val categoryName: String,
    val categoryEmoji: String?,
    val categoryColorIndex: Int
)
