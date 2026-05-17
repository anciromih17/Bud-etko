package si.um.feri.budzetko.data.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import si.um.feri.budzetko.data.entity.AiSummaryEntity

@Dao
interface AiSummaryDao {
    @Query(
        """
        SELECT * FROM ai_summaries
        WHERE user_id = :userId AND month = :month AND year = :year
        LIMIT 1
        """
    )
    fun observeSummary(userId: String, month: Int, year: Int): Flow<AiSummaryEntity?>

    @Upsert
    suspend fun upsertSummary(summary: AiSummaryEntity)
}
