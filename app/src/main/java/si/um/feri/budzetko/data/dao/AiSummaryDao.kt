package si.um.feri.budzetko.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import si.um.feri.budzetko.data.entity.AiSummaryEntity
import si.um.feri.budzetko.data.entity.SyncStatus

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
        WHERE user_id = :userId
        ORDER BY year DESC, month DESC
        """
    )
    suspend fun getSummaries(userId: String): List<AiSummaryEntity>

    @Query(
        """
        SELECT * FROM ai_summaries
        WHERE user_id = :userId AND month = :month AND year = :year
        LIMIT 1
        """
    )
    suspend fun getSummary(userId: String, month: Int, year: Int): AiSummaryEntity?

    @Query("SELECT COUNT(*) FROM ai_summaries WHERE user_id = :userId AND sync_status IN (:statuses)")
    suspend fun countBySyncStatus(userId: String, statuses: List<SyncStatus>): Int

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertSummary(summary: AiSummaryEntity): Long

    @Upsert
    suspend fun upsertSummary(summary: AiSummaryEntity)

    @Update
    suspend fun updateSummary(summary: AiSummaryEntity)

    @Query(
        """
        DELETE FROM ai_summaries
        WHERE user_id = :userId
            AND sync_status = 'SYNCED'
            AND id NOT IN (:cloudIds)
        """
    )
    suspend fun deleteSyncedMissingFromCloud(userId: String, cloudIds: List<Long>): Int

    @Query(
        """
        DELETE FROM ai_summaries
        WHERE user_id = :userId
            AND sync_status = 'SYNCED'
        """
    )
    suspend fun deleteAllSyncedMissingFromCloud(userId: String): Int

    @Query("DELETE FROM ai_summaries WHERE user_id = :userId")
    suspend fun deleteAllForUser(userId: String)
}
