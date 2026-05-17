package si.um.feri.budzetko.data.model

import androidx.room.ColumnInfo

data class CategorySpending(
    @ColumnInfo(name = "category_id")
    val categoryId: Long,
    @ColumnInfo(name = "category_name")
    val categoryName: String,
    @ColumnInfo(name = "spent_amount")
    val spentAmount: Double
)
