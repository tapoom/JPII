package klubi.plussipoisid.justputitin.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [PuttSession::class, TrainingRun::class], version = 3)
abstract class PuttDatabase : RoomDatabase() {
    abstract fun puttSessionDao(): PuttSessionDao

    companion object {
        @Volatile
        private var INSTANCE: PuttDatabase? = null

        // Migration from version 1 to 2: Add TrainingRun table
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create the training_runs table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `training_runs` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `date` INTEGER NOT NULL,
                        `minDistance` INTEGER NOT NULL,
                        `maxDistance` INTEGER NOT NULL,
                        `maxPuttsPerDistance` INTEGER NOT NULL,
                        `totalPutts` INTEGER NOT NULL,
                        `completedPutts` INTEGER NOT NULL DEFAULT 0,
                        `madePutts` INTEGER NOT NULL DEFAULT 0,
                        `style` TEXT NOT NULL DEFAULT '',
                        `isCompleted` INTEGER NOT NULL DEFAULT 0
                    )
                """)
            }
        }

        // Migration from version 2 to 3: Add trackingMode field
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add trackingMode column to training_runs table
                database.execSQL("ALTER TABLE `training_runs` ADD COLUMN `trackingMode` TEXT NOT NULL DEFAULT 'Per Putt'")
            }
        }

        fun getDatabase(context: Context): PuttDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PuttDatabase::class.java,
                    "putt_database"
                )
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
} 