package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.model.Cycle
import com.example.data.model.DailyLog
import com.example.data.model.UserSettings
import com.example.data.repository.CycleRepository
import com.example.domain.model.LocalDateCompat
import com.example.domain.prediction.CyclePredictionEngine
import com.example.domain.prediction.CyclePredictionResult
import com.example.notifications.NotificationHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CycleViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: CycleRepository

    val userSettings: StateFlow<UserSettings?>
    val allCycles: StateFlow<List<Cycle>>
    val allDailyLogs: StateFlow<List<DailyLog>>

    val selectedDate = MutableStateFlow(LocalDateCompat.now())

    val predictionResult: StateFlow<CyclePredictionResult?>
    val todayLog: StateFlow<DailyLog?>

    val isAppLocked = MutableStateFlow(false)

    init {
        val db = AppDatabase.getDatabase(application)
        repository = CycleRepository(db.cycleDao(), db.dailyLogDao(), db.userSettingsDao())

        userSettings = repository.userSettings.stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            null
        )

        allCycles = repository.allCycles.stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            emptyList()
        )

        allDailyLogs = repository.allDailyLogs.stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            emptyList()
        )

        val todayIso = LocalDateCompat.now().toIsoString()
        todayLog = allDailyLogs.combine(MutableStateFlow(todayIso)) { logs, today ->
            logs.firstOrNull { it.date == today }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

        predictionResult = combine(allCycles, userSettings, todayLog) { cycles, settings, todayLogItem ->
            val set = settings ?: UserSettings()
            val fallbackStart = LocalDateCompat.parseOrNull(set.lastPeriodStartDate) ?: LocalDateCompat.now().minusDays(14)
            val isPeriodToday = todayLogItem?.isPeriodDay == true

            CyclePredictionEngine.predict(
                pastCycles = cycles,
                defaultCycleLength = set.avgCycleLength,
                defaultPeriodLength = set.avgPeriodLength,
                fallbackLastPeriodStart = fallbackStart,
                today = LocalDateCompat.now(),
                isPeriodLoggedToday = isPeriodToday,
                userGoal = set.goal,
                manualCycleOverride = set.manualCycleOverride
            )
        }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

        // Initialize lock state if PIN is enabled
        viewModelScope.launch {
            val settings = repository.getSettingsSync()
            if (!settings.appLockPin.isNullOrBlank()) {
                isAppLocked.value = true
            }
        }
    }

    fun completeOnboarding(
        lastPeriodStartDate: LocalDateCompat,
        avgCycleLength: Int,
        avgPeriodLength: Int,
        goal: String,
        birthYear: Int?,
        onBirthControl: Boolean
    ) {
        viewModelScope.launch {
            val initialSettings = UserSettings(
                isOnboardingCompleted = true,
                lastPeriodStartDate = lastPeriodStartDate.toIsoString(),
                avgCycleLength = avgCycleLength,
                avgPeriodLength = avgPeriodLength,
                goal = goal,
                birthYear = birthYear,
                onBirthControl = onBirthControl
            )
            repository.saveSettings(initialSettings)

            // Auto-create first cycle and initial period daily logs
            val firstCycle = Cycle(
                startDate = lastPeriodStartDate.toIsoString(),
                endDate = lastPeriodStartDate.plusDays((avgPeriodLength - 1).toLong()).toIsoString(),
                periodLength = avgPeriodLength
            )
            repository.insertCycle(firstCycle)

            for (i in 0 until avgPeriodLength) {
                val pDate = lastPeriodStartDate.plusDays(i.toLong()).toIsoString()
                val flow = if (i == 0 || i == avgPeriodLength - 1) DailyLog.FLOW_LIGHT else DailyLog.FLOW_MEDIUM
                repository.saveDailyLog(
                    DailyLog(
                        date = pDate,
                        isPeriodDay = true,
                        flowIntensity = flow
                    )
                )
            }

            // Schedule background notifications
            NotificationHelper.scheduleAllAlarms(getApplication())
        }
    }

    fun saveDailyLog(log: DailyLog) {
        viewModelScope.launch {
            repository.saveDailyLog(log)
            NotificationHelper.scheduleAllAlarms(getApplication())
        }
    }

    fun deleteDailyLog(date: String) {
        viewModelScope.launch {
            repository.deleteDailyLog(date)
            NotificationHelper.scheduleAllAlarms(getApplication())
        }
    }

    fun updateSettings(newSettings: UserSettings) {
        viewModelScope.launch {
            repository.saveSettings(newSettings)
            NotificationHelper.scheduleAllAlarms(getApplication())
        }
    }

    fun verifyPin(enteredPin: String): Boolean {
        val currentPin = userSettings.value?.appLockPin
        if (currentPin == enteredPin) {
            isAppLocked.value = false
            return true
        }
        return false
    }

    fun unlockApp() {
        isAppLocked.value = false
    }

    fun exportDataCsv(onResult: (String) -> Unit) {
        viewModelScope.launch {
            val csv = repository.exportDataAsCsv()
            onResult(csv)
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.clearAllData()
        }
    }

    fun triggerTestNotification() {
        NotificationHelper.showNotification(
            getApplication(),
            "Period Tracker Test",
            "Reminders are active and working properly! Predictions will notify you on time."
        )
    }
}
