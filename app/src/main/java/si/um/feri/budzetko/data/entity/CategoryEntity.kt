package si.um.feri.budzetko.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "categories",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["user_id"],
            childColumns = ["user_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["user_id"]),
        Index(value = ["user_id", "name"], unique = true)
    ]
)
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val emoji: String? = null,
    @ColumnInfo(name = "color_index")
    val colorIndex: Int = 0,
    @ColumnInfo(name = "budget_role")
    val budgetRole: CategoryBudgetRole = CategoryBudgetRole.OTHER,
    @ColumnInfo(name = "user_id")
    val userId: String
)
