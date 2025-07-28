package klubi.plussipoisid.justputitin.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [PuttSession::class, TrainingRun::class], version = 2)
abstract class PuttDatabase : RoomDatabase() {
    abstract fun puttSessionDao(): PuttSessionDao

    companion object {
        @Volatile
        private var INSTANCE: PuttDatabase? = null

        fun getDatabase(context: Context): PuttDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PuttDatabase::class.java,
                    "putt_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
} 