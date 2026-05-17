package si.um.feri.budzetko.data.model

import androidx.room.ColumnInfo

data class BudgetCategoryProgress(
    @ColumnInfo(name = "category_id")
    val categoryId: Long,
    @ColumnInfo(name = "category_name")
    val categoryName: String,
    @ColumnInfo(name = "limit_amount")
    val limitAmount: Double,
    @ColumnInfo(name = "spent_amount")
    val spentAmount: Double
) {
    val progress: Float
        get() = if (limitAmount <= 0.0) 0f else (spentAmount / limitAmount).toFloat()

    val isNearLimit: Boolean
        get() = progress >= 0.9f && progress < 1f

    val isOverLimit: Boolean
        get() = progress >= 1f
}
