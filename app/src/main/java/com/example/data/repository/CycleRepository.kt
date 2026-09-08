package com.example.data.repository

import com.example.data.dao.CycleDao
import com.example.data.dao.DailyLogDao
import com.example.data.dao.UserSettingsDao
import com.example.data.model.Cycle
import com.example.data.model.DailyLog
import com.example.data.model.UserSettings
import com.example.domain.model.LocalDateCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class CycleRepository(
    private val cycleDao: CycleDao,
    private val dailyLogDao: DailyLogDao,
    private val userSettingsDao: UserSettingsDao
) {
    val allCycles: Flow<List<Cycle>> = cycleDao.getAllCyclesFlow()
    val allDailyLogs: Flow<List<DailyLog>> = dailyLogDao.getAllLogsFlow()
    val userSettings: Flow<UserSettings?> = userSettingsDao.getSettingsFlow()

    suspend fun getSettingsSync(): UserSettings {
        return userSettingsDao.getSettings() ?: UserSettings()
    }

    suspend fun saveSettings(settings: UserSettings) = withContext(Dispatchers.IO) {
        userSettingsDao.insertOrUpdate(settings)
    }

    suspend fun getLogForDate(date: String): DailyLog? = withContext(Dispatchers.IO) {
        dailyLogDao.getLogByDate(date)
    }

    fun getLogForDateFlow(date: String): Flow<DailyLog?> {
        return dailyLogDao.getLogByDateFlow(date)
    }

    suspend fun saveDailyLog(log: DailyLog) = withContext(Dispatchers.IO) {
        dailyLogDao.insertLog(log)
        // If this log updated a period state, sync cycles
        syncCyclesFromPeriodLogs()
    }

    suspend fun deleteDailyLog(date: String) = withContext(Dispatchers.IO) {
        dailyLogDao.deleteLogByDate(date)
        syncCyclesFromPeriodLogs()
    }

    suspend fun insertCycle(cycle: Cycle) = withContext(Dispatchers.IO) {
        cycleDao.insertCycle(cycle)
    }

    suspend fun deleteCycle(id: Long) = withContext(Dispatchers.IO) {
        cycleDao.deleteCycleById(id)
    }

    suspend fun getAllCyclesList(): List<Cycle> = withContext(Dispatchers.IO) {
        cycleDao.getAllCyclesList()
    }

    suspend fun getAllDailyLogsList(): List<DailyLog> = withContext(Dispatchers.IO) {
        dailyLogDao.getAllLogsList()
    }

    /**
     * Intelligently groups logged period days into cohesive cycles.
     * When days are consecutive or within 2 days of spotting/bleeding, they belong to the same period.
     */
    suspend fun syncCyclesFromPeriodLogs() = withContext(Dispatchers.IO) {
        val periodLogs = dailyLogDao.getAllPeriodDays()
            .mapNotNull { log -> LocalDateCompat.parseOrNull(log.date)?.let { it to log } }
            .sortedBy { it.first } // oldest first

        if (periodLogs.isEmpty()) return@withContext

        // Group consecutive period days into discrete episodes
        val episodes = mutableListOf<MutableList<LocalDateCompat>>()
        var currentEpisode = mutableListOf<LocalDateCompat>()

        for ((date, _) in periodLogs) {
            if (currentEpisode.isEmpty()) {
                currentEpisode.add(date)
            } else {
                val lastDate = currentEpisode.last()
                val gap = lastDate.daysUntil(date)
                // If gap is 1 or 2 days, count as continuation of the same menstrual episode
                if (gap <= 2) {
                    currentEpisode.add(date)
                } else {
                    episodes.add(currentEpisode)
                    currentEpisode = mutableListOf(date)
                }
            }
        }
        if (currentEpisode.isNotEmpty()) {
            episodes.add(currentEpisode)
        }

        // Reconstruct cycles from episodes
        val existingCycles = cycleDao.getAllCyclesList()
        val existingMap = existingCycles.associateBy { it.startDate }

        for (i in episodes.indices) {
            val ep = episodes[i]
            val startDate = ep.first().toIsoString()
            val endDate = ep.last().toIsoString()
            val periodLength = (ep.first().daysUntil(ep.last()).toInt() + 1).coerceAtLeast(1)

            // Cycle length is days to next period episode start
            val cycleLength = if (i < episodes.size - 1) {
                val nextStart = episodes[i + 1].first()
                ep.first().daysUntil(nextStart).toInt()
            } else {
                null
            }

            val existing = existingMap[startDate]
            val newCycle = Cycle(
                id = existing?.id ?: 0,
                startDate = startDate,
                endDate = endDate,
                periodLength = periodLength,
                cycleLength = cycleLength
            )
            cycleDao.insertCycle(newCycle)
        }
    }

    suspend fun clearAllData() = withContext(Dispatchers.IO) {
        cycleDao.clearAll()
        dailyLogDao.clearAll()
        userSettingsDao.clear()
    }

    suspend fun exportDataAsCsv(): String = withContext(Dispatchers.IO) {
        val sb = StringBuilder()
        sb.append("=== PERIOD TRACKER DATA EXPORT ===\n\n")

        sb.append("--- CYCLES ---\n")
        sb.append("ID,Start Date,End Date,Period Length (Days),Cycle Length (Days)\n")
        val cycles = cycleDao.getAllCyclesList()
        for (c in cycles) {
            sb.append("${c.id},${c.startDate},${c.endDate ?: ""},${c.periodLength},${c.cycleLength ?: ""}\n")
        }

        sb.append("\n--- DAILY LOGS ---\n")
        sb.append("Date,Is Period,Flow,Symptoms,Moods,Discharge,BBT,Sexual Activity,Notes\n")
        val logs = dailyLogDao.getAllLogsList()
        for (l in logs) {
            val symptomsJoined = l.symptoms.joinToString(";")
            val moodsJoined = l.moods.joinToString(";")
            val cleanNotes = l.notes.replace(",", " ").replace("\n", " ")
            sb.append("${l.date},${l.isPeriodDay},${l.flowIntensity},\"$symptomsJoined\",\"$moodsJoined\",${l.discharge},${l.bbt ?: ""},${l.sexualActivity},\"$cleanNotes\"\n")
        }

        sb.toString()
    }
}
