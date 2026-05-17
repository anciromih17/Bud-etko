package si.um.feri.budzetko.data.model

import androidx.room.ColumnInfo
import si.um.feri.budzetko.data.entity.CategoryBudgetRole

data class CategoryWithMonthlyLimit(
    val id: Long,
    val name: String,
    val emoji: String?,
    @ColumnInfo(name = "color_index")
    val colorIndex: Int,
    @ColumnInfo(name = "budget_role")
    val budgetRole: CategoryBudgetRole,
    @ColumnInfo(name = "limit_amount")
    val limitAmount: Double?
)
