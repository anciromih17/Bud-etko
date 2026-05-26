package si.um.feri.budzetko.data.database

import androidx.room.TypeConverter
import si.um.feri.budzetko.data.entity.AiSummarySource
import si.um.feri.budzetko.data.entity.CategoryBudgetRole
import si.um.feri.budzetko.data.entity.SyncStatus

class BudzetkoTypeConverters {
    @TypeConverter
    fun fromSyncStatus(status: SyncStatus): String = status.name

    @TypeConverter
    fun toSyncStatus(value: String): SyncStatus = SyncStatus.valueOf(value)

    @TypeConverter
    fun fromCategoryBudgetRole(role: CategoryBudgetRole): String = role.name

    @TypeConverter
    fun toCategoryBudgetRole(value: String): CategoryBudgetRole = CategoryBudgetRole.valueOf(value)

    @TypeConverter
    fun fromAiSummarySource(source: AiSummarySource): String = source.name

    @TypeConverter
    fun toAiSummarySource(value: String): AiSummarySource = AiSummarySource.valueOf(value)
}
