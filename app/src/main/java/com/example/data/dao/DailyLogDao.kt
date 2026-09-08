package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.DailyLog
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyLogDao {
    @Query("SELECT * FROM daily_logs ORDER BY date DESC")
    fun getAllLogsFlow(): Flow<List<DailyLog>>

    @Query("SELECT * FROM daily_logs ORDER BY date DESC")
    suspend fun getAllLogsList(): List<DailyLog>

    @Query("SELECT * FROM daily_logs WHERE date = :date LIMIT 1")
    fun getLogByDateFlow(date: String): Flow<DailyLog?>

    @Query("SELECT * FROM daily_logs WHERE date = :date LIMIT 1")
    suspend fun getLogByDate(date: String): DailyLog?

    @Query("SELECT * FROM daily_logs WHERE date >= :startDate AND date <= :endDate ORDER BY date ASC")
    fun getLogsBetweenFlow(startDate: String, endDate: String): Flow<List<DailyLog>>

    @Query("SELECT * FROM daily_logs WHERE date >= :startDate AND date <= :endDate ORDER BY date ASC")
    suspend fun getLogsBetween(startDate: String, endDate: String): List<DailyLog>

    @Query("SELECT * FROM daily_logs WHERE isPeriodDay = 1 ORDER BY date DESC")
    suspend fun getAllPeriodDays(): List<DailyLog>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: DailyLog)

    @Query("DELETE FROM daily_logs WHERE date = :date")
    suspend fun deleteLogByDate(date: String)

    @Query("DELETE FROM daily_logs")
    suspend fun clearAll()
}
