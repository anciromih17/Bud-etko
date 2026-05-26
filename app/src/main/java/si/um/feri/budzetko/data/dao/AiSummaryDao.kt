package si.um.feri.budzetko.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
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

    @Query(
        """
        SELECT * FROM ai_summaries
        WHERE user_id = :userId
        ORDER BY year DESC, month DESC
        """
    )
    fun observeSummaries(userId: String): Flow<List<AiSummaryEntity>>

    @Query(
        """
        SELECT * FROM ai_summaries
        WHERE user_id = :userId AND month = :month AND year = :year
        LIMIT 1
        """
    )
    suspend fun getSummary(userId: String, month: Int, year: Int): AiSummaryEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertSummary(summary: AiSummaryEntity): Long

    @Update
    suspend fun updateSummary(summary: AiSummaryEntity)
}
