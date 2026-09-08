package com.example.domain.prediction

import com.example.data.model.Cycle
import com.example.domain.model.LocalDateCompat
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Pure, deterministic menstrual cycle prediction engine.
 *
 * Core Assumptions & Medical Grounding:
 * 1. Luteal Phase Assumption: The luteal phase (from ovulation to the next period onset)
 *    is relatively constant across healthy cycles at approximately 14 days (typically 12-16 days).
 *    Therefore, predicted Ovulation = nextPeriodStart - 14 days.
 * 2. Fertile Window: Sperm can survive in fertile cervical fluid up to 5 days, and an unfertilized
 *    ovum survives 12-24 hours after ovulation. Thus, the fertile window spans:
 *    [ovulation - 5 days] through [ovulation + 1 day] (7 days total).
 * 3. Priority Order for Daily Status:
 *    Active period bleeding > Ovulation Day > Fertile Window > Days-until-next-period / Late flag.
 * 4. Local calendar math: Uses [LocalDateCompat] to avoid any UTC or DST off-by-one day bugs.
 */
object CyclePredictionEngine {

    private const val LUTEAL_PHASE_DAYS = 14
    private const val FERTILE_DAYS_BEFORE_OVULATION = 5
    private const val FERTILE_DAYS_AFTER_OVULATION = 1
    private const val RECENT_CYCLES_SAMPLE_SIZE = 6
    private const val IRREGULAR_VARIANCE_THRESHOLD = 7

    /**
     * Computes the cycle prediction based on cycle history, active logging for today,
     * user settings, and reference date.
     *
     * @param pastCycles List of recorded past cycles (sorted newest first or oldest first)
     * @param defaultCycleLength Fallback average cycle length from user settings (e.g. 28)
     * @param defaultPeriodLength Fallback average period length from user settings (e.g. 5)
     * @param fallbackLastPeriodStart Starting date of last period (from onboarding or manual setting)
     * @param today Reference date (usually today's date)
     * @param isPeriodLoggedToday Whether the user has actively logged period bleeding today
     * @param userGoal "track_cycle", "avoid_pregnancy", or "trying_to_conceive"
     * @param manualCycleOverride If true, force usage of defaultCycleLength/defaultPeriodLength
     */
    fun predict(
        pastCycles: List<Cycle>,
        defaultCycleLength: Int = 28,
        defaultPeriodLength: Int = 5,
        fallbackLastPeriodStart: LocalDateCompat,
        today: LocalDateCompat = LocalDateCompat.now(),
        isPeriodLoggedToday: Boolean = false,
        userGoal: String = "track_cycle",
        manualCycleOverride: Boolean = false
    ): CyclePredictionResult {
        // Sort cycles by startDate ascending
        val sortedCycles = pastCycles
            .filter { LocalDateCompat.parseOrNull(it.startDate) != null }
            .sortedBy { LocalDateCompat.parse(it.startDate) }

        // Determine effective last period start
        val lastPeriodStart = if (sortedCycles.isNotEmpty()) {
            LocalDateCompat.parse(sortedCycles.last().startDate)
        } else {
            fallbackLastPeriodStart
        }

        // Calculate average period length
        val recordedPeriodLengths = sortedCycles.map { it.periodLength }.filter { it in 2..15 }
        val avgPeriodLength = if (manualCycleOverride || recordedPeriodLengths.isEmpty()) {
            defaultPeriodLength.coerceIn(2, 10)
        } else {
            recordedPeriodLengths.takeLast(RECENT_CYCLES_SAMPLE_SIZE).average().roundToInt()
                .coerceIn(2, 10)
        }

        // Calculate cycle lengths between consecutive period start dates
        val cycleLengths = mutableListOf<Int>()
        for (i in 0 until sortedCycles.size - 1) {
            val c1 = LocalDateCompat.parse(sortedCycles[i].startDate)
            val c2 = LocalDateCompat.parse(sortedCycles[i + 1].startDate)
            val daysBetween = c1.daysUntil(c2).toInt()
            if (daysBetween in 18..60) {
                cycleLengths.add(daysBetween)
            }
        }

        // If recorded cycles has an explicit cycleLength stored on the last closed cycles, include them
        sortedCycles.mapNotNull { it.cycleLength }.filter { it in 18..60 }.forEach {
            if (!cycleLengths.contains(it)) cycleLengths.add(it)
        }

        // Determine average cycle length
        val avgCycleLength = if (manualCycleOverride || cycleLengths.isEmpty()) {
            defaultCycleLength.coerceIn(21, 45)
        } else {
            cycleLengths.takeLast(RECENT_CYCLES_SAMPLE_SIZE).average().roundToInt()
                .coerceIn(21, 45)
        }

        // Calculate variability / irregularity
        val varianceDays = if (cycleLengths.size >= 2) {
            val minLen = cycleLengths.minOrNull() ?: avgCycleLength
            val maxLen = cycleLengths.maxOrNull() ?: avgCycleLength
            abs(maxLen - minLen)
        } else {
            0
        }

        val regularity = when {
            cycleLengths.size < 2 -> CycleRegularity.REGULAR // Not enough data, assume standard
            varianceDays > IRREGULAR_VARIANCE_THRESHOLD -> CycleRegularity.IRREGULAR
            varianceDays > 4 -> CycleRegularity.MODERATE_VARIABILITY
            else -> CycleRegularity.REGULAR
        }

        val confidence = when {
            manualCycleOverride -> 75
            cycleLengths.size >= 4 && regularity == CycleRegularity.REGULAR -> 95
            cycleLengths.size >= 2 && regularity == CycleRegularity.REGULAR -> 90
            regularity == CycleRegularity.MODERATE_VARIABILITY -> 78
            regularity == CycleRegularity.IRREGULAR -> 60
            else -> 70 // default fallback
        }

        // Next predicted period start date: lastPeriodStart + avgCycleLength
        val nextPeriodStart = lastPeriodStart.plusDays(avgCycleLength.toLong())

        // Predicted period duration
        val predictedPeriodDays = (0 until avgPeriodLength).map { offset ->
            nextPeriodStart.plusDays(offset.toLong())
        }

        // Ovulation = nextPeriodStart - 14 days (fixed luteal assumption)
        val ovulationDay = nextPeriodStart.minusDays(LUTEAL_PHASE_DAYS.toLong())

        // Fertile window = (ovulationDay - 5) .. (ovulationDay + 1)
        val fertileWindowDays = (-FERTILE_DAYS_BEFORE_OVULATION..FERTILE_DAYS_AFTER_OVULATION).map { offset ->
            ovulationDay.plusDays(offset.toLong())
        }

        // Current cycle day = (today - lastPeriodStart) + 1
        val rawCycleDay = lastPeriodStart.daysUntil(today).toInt() + 1
        val currentCycleDay = if (rawCycleDay < 1) 1 else rawCycleDay

        // Check if period is late: today > nextPeriodStart + avgPeriodLength and no new period logged
        val periodOverdueDate = nextPeriodStart.plusDays(avgPeriodLength.toLong())
        val isLate = today > periodOverdueDate && !isPeriodLoggedToday

        val daysUntilNext = today.daysUntil(nextPeriodStart).toInt()

        // Determine current phase and daily status headline
        val isTodayInActivePeriod = isPeriodLoggedToday ||
                (today >= lastPeriodStart && today < lastPeriodStart.plusDays(avgPeriodLength.toLong()))

        val isTodayOvulation = today == ovulationDay
        val isTodayFertile = fertileWindowDays.contains(today)

        val (statusType, headline, subtitle, phaseName) = when {
            // Prioritize: 1. Active Period
            isTodayInActivePeriod -> {
                val periodDay = (lastPeriodStart.daysUntil(today).toInt() + 1).coerceAtLeast(1)
                Quad(
                    CycleStatusType.ACTIVE_PERIOD,
                    "Period Day $periodDay",
                    if (userGoal == "trying_to_conceive") "Chances of conception are low"
                    else "Rest, stay hydrated, and take it easy",
                    "Menstrual"
                )
            }
            // 2. Ovulation Day
            isTodayOvulation -> {
                Quad(
                    CycleStatusType.OVULATION_DAY,
                    "Ovulation Day",
                    when (userGoal) {
                        "trying_to_conceive" -> "Peak fertility! Ideal window for conception"
                        "avoid_pregnancy" -> "Highest risk of pregnancy today! Use protection"
                        else -> "Peak fertility window today"
                    },
                    "Ovulatory"
                )
            }
            // 3. Fertile Window
            isTodayFertile -> {
                Quad(
                    CycleStatusType.FERTILE_WINDOW,
                    "Fertile Window",
                    when (userGoal) {
                        "trying_to_conceive" -> "High chance of getting pregnant"
                        "avoid_pregnancy" -> "High pregnancy chance — use protection"
                        else -> "Fertile phase of your cycle"
                    },
                    "Ovulatory"
                )
            }
            // 4. Period Late
            isLate -> {
                val daysLate = today.daysUntil(nextPeriodStart).toInt() // negative
                Quad(
                    CycleStatusType.PERIOD_MAY_BE_LATE,
                    "Period may be late",
                    "Predicted ${abs(daysLate)} days ago. Log a period if it started",
                    "Luteal"
                )
            }
            // 5. Period Due Soon (1-3 days away)
            daysUntilNext in 0..3 -> {
                val text = if (daysUntilNext == 0) "Period expected today"
                else if (daysUntilNext == 1) "Period in 1 day"
                else "Period in $daysUntilNext days"
                Quad(
                    CycleStatusType.PERIOD_DUE_SOON,
                    text,
                    "PMS symptoms and mild cramping may be normal now",
                    "Luteal"
                )
            }
            // 6. Follicular Phase (between period end and fertile window)
            today < fertileWindowDays.first() -> {
                Quad(
                    CycleStatusType.FOLLICULAR_PHASE,
                    "Period in $daysUntilNext days",
                    "Energy levels typically rising in the follicular phase",
                    "Follicular"
                )
            }
            // 7. Luteal Phase (post-ovulation until next period)
            else -> {
                Quad(
                    CycleStatusType.LUTEAL_PHASE,
                    "Period in $daysUntilNext days",
                    "Progesterone is active; focus on gentle movement and nourishing meals",
                    "Luteal"
                )
            }
        }

        return CyclePredictionResult(
            lastPeriodStart = lastPeriodStart,
            currentCycleDay = currentCycleDay,
            avgCycleLength = avgCycleLength,
            avgPeriodLength = avgPeriodLength,
            nextPeriodStart = nextPeriodStart,
            predictedPeriodDays = predictedPeriodDays,
            ovulationDay = ovulationDay,
            fertileWindowDays = fertileWindowDays,
            isPeriodLate = isLate,
            daysUntilNextPeriod = daysUntilNext,
            cycleStatusType = statusType,
            statusHeadline = headline,
            statusSubtitle = subtitle,
            regularity = regularity,
            cycleVarianceDays = varianceDays,
            confidencePercent = confidence,
            phaseName = phaseName
        )
    }

    private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
}
