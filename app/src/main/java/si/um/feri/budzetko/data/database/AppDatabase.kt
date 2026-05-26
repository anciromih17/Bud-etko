package si.um.feri.budzetko.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import si.um.feri.budzetko.data.dao.AiSummaryDao
import si.um.feri.budzetko.data.dao.BudgetDao
import si.um.feri.budzetko.data.dao.CategoryDao
import si.um.feri.budzetko.data.dao.ExpenseDao
import si.um.feri.budzetko.data.dao.UserDao
import si.um.feri.budzetko.data.entity.AiSummaryEntity
import si.um.feri.budzetko.data.entity.BudgetCategoryEntity
import si.um.feri.budzetko.data.entity.BudgetEntity
import si.um.feri.budzetko.data.entity.CategoryEntity
import si.um.feri.budzetko.data.entity.ExpenseEntity
import si.um.feri.budzetko.data.entity.UserEntity

@Database(
    entities = [
        UserEntity::class,
        CategoryEntity::class,
        ExpenseEntity::class,
        BudgetEntity::class,
        BudgetCategoryEntity::class,
        AiSummaryEntity::class
    ],
    version = 4,
    exportSchema = true
)
@TypeConverters(BudzetkoTypeConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun categoryDao(): CategoryDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun budgetDao(): BudgetDao
    abstract fun aiSummaryDao(): AiSummaryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "budzetko.db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                    .build()
                    .also { INSTANCE = it }
            }
        }

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE categories ADD COLUMN emoji TEXT")
                db.execSQL("ALTER TABLE categories ADD COLUMN color_index INTEGER NOT NULL DEFAULT 0")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE categories ADD COLUMN budget_role TEXT NOT NULL DEFAULT 'OTHER'")
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE ai_summaries ADD COLUMN source TEXT NOT NULL DEFAULT 'FALLBACK'")
            }
        }
    }
}
