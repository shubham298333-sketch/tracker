package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a menstrual cycle recording.
 * [startDate] - ISO date string "YYYY-MM-DD" of period onset.
 * [endDate] - ISO date string "YYYY-MM-DD" when period bleeding ended (nullable if ongoing).
 * [periodLength] - Number of period days (bleed duration).
 * [cycleLength] - Total days until the next period began (null if current active cycle).
 */
@Entity(tableName = "cycles")
data class Cycle(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val startDate: String,
    val endDate: String? = null,
    val periodLength: Int = 5,
    val cycleLength: Int? = null
)
