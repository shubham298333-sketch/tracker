package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.data.dao.CycleDao
import com.example.data.dao.DailyLogDao
import com.example.data.dao.UserSettingsDao
import com.example.data.model.Cycle
import com.example.data.model.DailyLog
import com.example.data.model.UserSettings

@Database(
    entities = [Cycle::class, DailyLog::class, UserSettings::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cycleDao(): CycleDao
    abstract fun dailyLogDao(): DailyLogDao
    abstract fun userSettingsDao(): UserSettingsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "period_tracker_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
