package si.um.feri.budzetko.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import si.um.feri.budzetko.data.entity.CategoryEntity
import si.um.feri.budzetko.data.entity.SyncStatus

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories WHERE user_id = :userId ORDER BY name COLLATE NOCASE")
    fun observeCategories(userId: String): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE user_id = :userId ORDER BY name COLLATE NOCASE")
    suspend fun getCategories(userId: String): List<CategoryEntity>

    @Query("SELECT * FROM categories WHERE id = :categoryId LIMIT 1")
    suspend fun getCategory(categoryId: Long): CategoryEntity?

    @Query("SELECT COUNT(*) FROM categories WHERE user_id = :userId AND sync_status IN (:statuses)")
    suspend fun countBySyncStatus(userId: String, statuses: List<SyncStatus>): Int

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertCategory(category: CategoryEntity): Long

    @Upsert
    suspend fun upsertCategory(category: CategoryEntity)

    @Update
    suspend fun updateCategory(category: CategoryEntity)

    @Delete
    suspend fun deleteCategory(category: CategoryEntity)

    @Query("DELETE FROM categories WHERE id = :categoryId AND user_id = :userId")
    suspend fun deleteCategoryById(categoryId: Long, userId: String)

    @Query(
        """
        DELETE FROM categories
        WHERE user_id = :userId
            AND sync_status = 'SYNCED'
            AND id NOT IN (:cloudIds)
            AND NOT EXISTS (
                SELECT 1 FROM expenses
                WHERE expenses.category_id = categories.id
            )
        """
    )
    suspend fun deleteSyncedMissingFromCloud(userId: String, cloudIds: List<Long>): Int

    @Query(
        """
        DELETE FROM categories
        WHERE user_id = :userId
            AND sync_status = 'SYNCED'
            AND NOT EXISTS (
                SELECT 1 FROM expenses
                WHERE expenses.category_id = categories.id
            )
        """
    )
    suspend fun deleteAllSyncedMissingFromCloud(userId: String): Int

    @Query("DELETE FROM categories WHERE user_id = :userId")
    suspend fun deleteAllForUser(userId: String)
}
