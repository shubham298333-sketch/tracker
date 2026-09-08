package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Cycle
import kotlinx.coroutines.flow.Flow

@Dao
interface CycleDao {
    @Query("SELECT * FROM cycles ORDER BY startDate DESC")
    fun getAllCyclesFlow(): Flow<List<Cycle>>

    @Query("SELECT * FROM cycles ORDER BY startDate DESC")
    suspend fun getAllCyclesList(): List<Cycle>

    @Query("SELECT * FROM cycles ORDER BY startDate DESC LIMIT 1")
    suspend fun getLatestCycle(): Cycle?

    @Query("SELECT * FROM cycles WHERE startDate = :date LIMIT 1")
    suspend fun getCycleByStartDate(date: String): Cycle?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCycle(cycle: Cycle): Long

    @Update
    suspend fun updateCycle(cycle: Cycle)

    @Query("DELETE FROM cycles WHERE id = :id")
    suspend fun deleteCycleById(id: Long)

    @Query("DELETE FROM cycles")
    suspend fun clearAll()
}
