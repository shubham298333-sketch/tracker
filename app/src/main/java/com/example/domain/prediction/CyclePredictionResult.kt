package com.example.domain.prediction

import com.example.domain.model.LocalDateCompat

enum class CycleStatusType {
    ACTIVE_PERIOD,
    OVULATION_DAY,
    FERTILE_WINDOW,
    PERIOD_DUE_SOON,
    PERIOD_MAY_BE_LATE,
    FOLLICULAR_PHASE,
    LUTEAL_PHASE
}

enum class CycleRegularity {
    REGULAR,
    MODERATE_VARIABILITY,
    IRREGULAR
}

data class CyclePredictionResult(
    val lastPeriodStart: LocalDateCompat,
    val currentCycleDay: Int,
    val avgCycleLength: Int,
    val avgPeriodLength: Int,
    val nextPeriodStart: LocalDateCompat,
    val predictedPeriodDays: List<LocalDateCompat>,
    val ovulationDay: LocalDateCompat,
    val fertileWindowDays: List<LocalDateCompat>,
    val isPeriodLate: Boolean,
    val daysUntilNextPeriod: Int, // Can be negative if late
    val cycleStatusType: CycleStatusType,
    val statusHeadline: String,
    val statusSubtitle: String,
    val regularity: CycleRegularity,
    val cycleVarianceDays: Int,
    val confidencePercent: Int, // High (90-95%) for regular, Lower (60-70%) if irregular or only 1 cycle
    val phaseName: String // "Menstrual", "Follicular", "Ovulatory", "Luteal"
)
