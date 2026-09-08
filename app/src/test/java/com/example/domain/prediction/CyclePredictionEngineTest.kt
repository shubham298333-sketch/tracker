package com.example.domain.prediction

import com.example.data.model.Cycle
import com.example.domain.model.LocalDateCompat
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CyclePredictionEngineTest {

    @Test
    fun testPredictionWithZeroPastCyclesUsesFallback() {
        val lastStart = LocalDateCompat(2024, 6, 1)
        val today = LocalDateCompat(2024, 6, 10)

        val result = CyclePredictionEngine.predict(
            pastCycles = emptyList(),
            defaultCycleLength = 28,
            defaultPeriodLength = 5,
            fallbackLastPeriodStart = lastStart,
            today = today
        )

        assertEquals(28, result.avgCycleLength)
        assertEquals(5, result.avgPeriodLength)
        assertEquals(LocalDateCompat(2024, 6, 29), result.nextPeriodStart)
        // Ovulation = nextPeriod - 14 = June 29 - 14 = June 15
        assertEquals(LocalDateCompat(2024, 6, 15), result.ovulationDay)
        // Current cycle day: June 10 - June 1 + 1 = 10
        assertEquals(10, result.currentCycleDay)
        // Fertile window: June 10 to June 16 (ovulation - 5 to + 1)
        assertEquals(LocalDateCompat(2024, 6, 10), result.fertileWindowDays.first())
        assertEquals(LocalDateCompat(2024, 6, 16), result.fertileWindowDays.last())
        // Since today is June 10 which is fertile start:
        assertEquals(CycleStatusType.FERTILE_WINDOW, result.cycleStatusType)
    }

    @Test
    fun testPredictionActivePeriodPrioritized() {
        val lastStart = LocalDateCompat(2024, 6, 1)
        val today = LocalDateCompat(2024, 6, 2)

        val result = CyclePredictionEngine.predict(
            pastCycles = emptyList(),
            defaultCycleLength = 28,
            defaultPeriodLength = 5,
            fallbackLastPeriodStart = lastStart,
            today = today,
            isPeriodLoggedToday = true
        )

        assertEquals(CycleStatusType.ACTIVE_PERIOD, result.cycleStatusType)
        assertEquals("Period Day 2", result.statusHeadline)
    }

    @Test
    fun testPredictionOvulationDayPrioritized() {
        val lastStart = LocalDateCompat(2024, 6, 1)
        val today = LocalDateCompat(2024, 6, 15) // Ovulation day

        val result = CyclePredictionEngine.predict(
            pastCycles = emptyList(),
            defaultCycleLength = 28,
            defaultPeriodLength = 5,
            fallbackLastPeriodStart = lastStart,
            today = today
        )

        assertEquals(CycleStatusType.OVULATION_DAY, result.cycleStatusType)
        assertEquals("Ovulation Day", result.statusHeadline)
    }

    @Test
    fun testPredictionLatePeriodFlagged() {
        val lastStart = LocalDateCompat(2024, 5, 1)
        // Next period was May 29. Expected end was June 3.
        // If today is June 6 and no period logged:
        val today = LocalDateCompat(2024, 6, 6)

        val result = CyclePredictionEngine.predict(
            pastCycles = emptyList(),
            defaultCycleLength = 28,
            defaultPeriodLength = 5,
            fallbackLastPeriodStart = lastStart,
            today = today,
            isPeriodLoggedToday = false
        )

        assertTrue(result.isPeriodLate)
        assertEquals(CycleStatusType.PERIOD_MAY_BE_LATE, result.cycleStatusType)
    }

    @Test
    fun testPredictionWithMultiplePastCyclesAveragesLengths() {
        // Cycle 1: Jan 1 -> Jan 30 (29 days)
        // Cycle 2: Jan 30 -> Feb 26 (27 days)
        // Cycle 3: Feb 26 -> Mar 26 (29 days)
        val cycles = listOf(
            Cycle(id = 1, startDate = "2024-01-01", endDate = "2024-01-05", periodLength = 5, cycleLength = 29),
            Cycle(id = 2, startDate = "2024-01-30", endDate = "2024-02-03", periodLength = 5, cycleLength = 27),
            Cycle(id = 3, startDate = "2024-02-26", endDate = "2024-03-02", periodLength = 6, cycleLength = 29)
        )

        val result = CyclePredictionEngine.predict(
            pastCycles = cycles,
            defaultCycleLength = 28,
            defaultPeriodLength = 5,
            fallbackLastPeriodStart = LocalDateCompat(2024, 2, 26),
            today = LocalDateCompat(2024, 3, 5)
        )

        // Average of 29, 27, 29 is 28.33 -> 28
        assertEquals(28, result.avgCycleLength)
        assertEquals(CycleRegularity.REGULAR, result.regularity)
        assertEquals(LocalDateCompat(2024, 2, 26), result.lastPeriodStart)
        assertEquals(LocalDateCompat(2024, 3, 25), result.nextPeriodStart)
    }
}
