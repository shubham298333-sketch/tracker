package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Daily record for logging symptoms, flow, mood, cervical discharge, BBT, and notes.
 * [date] - ISO string "YYYY-MM-DD" acts as primary key ensuring one log per calendar day.
 */
@Entity(tableName = "daily_logs")
data class DailyLog(
    @PrimaryKey
    val date: String,
    val isPeriodDay: Boolean = false,
    val flowIntensity: String = FLOW_NONE, // none, spotting, light, medium, heavy
    val symptoms: List<String> = emptyList(),
    val moods: List<String> = emptyList(),
    val discharge: String = DISCHARGE_NONE, // none, sticky, creamy, watery, egg_white
    val bbt: Float? = null,
    val sexualActivity: String = SEX_NONE, // none, protected, unprotected
    val notes: String = ""
) {
    companion object {
        const val FLOW_NONE = "none"
        const val FLOW_SPOTTING = "spotting"
        const val FLOW_LIGHT = "light"
        const val FLOW_MEDIUM = "medium"
        const val FLOW_HEAVY = "heavy"

        const val DISCHARGE_NONE = "none"
        const val DISCHARGE_STICKY = "sticky"
        const val DISCHARGE_CREAMY = "creamy"
        const val DISCHARGE_WATERY = "watery"
        const val DISCHARGE_EGG_WHITE = "egg_white"

        const val SEX_NONE = "none"
        const val SEX_PROTECTED = "protected"
        const val SEX_UNPROTECTED = "unprotected"

        val DEFAULT_SYMPTOMS = listOf(
            "Cramps", "Headache", "Backache", "Bloating", "Tender breasts",
            "Fatigue", "Acne", "Nausea", "Cravings", "Insomnia"
        )

        val DEFAULT_MOODS = listOf(
            "Happy", "Calm", "Energetic", "Sensitive",
            "Sad", "Anxious", "Irritable"
        )
    }
}
