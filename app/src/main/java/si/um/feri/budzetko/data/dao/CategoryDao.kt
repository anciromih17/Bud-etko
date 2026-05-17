package si.um.feri.budzetko.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import si.um.feri.budzetko.data.entity.CategoryEntity

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories WHERE user_id = :userId ORDER BY name COLLATE NOCASE")
    fun observeCategories(userId: String): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE id = :categoryId LIMIT 1")
    suspend fun getCategory(categoryId: Long): CategoryEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertCategory(category: CategoryEntity): Long

    @Update
    suspend fun updateCategory(category: CategoryEntity)

    @Delete
    suspend fun deleteCategory(category: CategoryEntity)

    @Query("DELETE FROM categories WHERE id = :categoryId AND user_id = :userId")
    suspend fun deleteCategoryById(categoryId: Long, userId: String)
}
