package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_settings")
data class UserSettings(
    @PrimaryKey
    val id: Int = 1,
    val isOnboardingCompleted: Boolean = false,
    val lastPeriodStartDate: String = "",
    val avgCycleLength: Int = 28,
    val avgPeriodLength: Int = 5,
    val goal: String = GOAL_TRACK,
    val birthYear: Int? = null,
    val onBirthControl: Boolean = false,
    val manualCycleOverride: Boolean = false,

    // Notifications
    val notifyPeriodSoon: Boolean = true,
    val notifyPeriodSoonDays: Int = 2,
    val notifyPeriodLate: Boolean = true,
    val notifyFertileWindow: Boolean = false,
    val notifyOvulation: Boolean = false,
    val notifyDailyLog: Boolean = false,
    val dailyLogTime: String = "20:00",

    // Preferences & Lock
    val temperatureUnit: String = UNIT_CELSIUS,
    val appTheme: String = THEME_LIGHT,
    val accentPalette: String = PALETTE_ROSE,
    val appLockPin: String? = null,

    // Monetization & Ad Preferences
    val adFreeUntilTimestamp: Long = 0L,
    val unlockedExtendedForecast: Boolean = false,
    val unlockedSymptomInsights: Boolean = false
) {
    fun isAdFreeActive(): Boolean {
        return adFreeUntilTimestamp > System.currentTimeMillis()
    }
    companion object {
        const val GOAL_TRACK = "track_cycle"
        const val GOAL_AVOID = "avoid_pregnancy"
        const val GOAL_TTC = "trying_to_conceive"

        const val UNIT_CELSIUS = "C"
        const val UNIT_FAHRENHEIT = "F"

        const val THEME_SYSTEM = "system"
        const val THEME_LIGHT = "light"
        const val THEME_DARK = "dark"

        const val PALETTE_ROSE = "rose"
        const val PALETTE_LAVENDER = "lavender"
        const val PALETTE_SAGE = "sage"
        const val PALETTE_OCEAN = "ocean"
    }
}
