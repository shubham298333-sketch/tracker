package com.example.domain.model

import java.util.Calendar
import java.util.Locale

/**
 * Pure, timezone-safe calendar date representation (Year, Month 1-12, Day 1-31).
 * Avoids any UTC / local-time offset shifts or daylight saving time off-by-one bugs.
 */
data class LocalDateCompat(
    val year: Int,
    val month: Int, // 1 - 12
    val dayOfMonth: Int // 1 - 31
) : Comparable<LocalDateCompat> {

    init {
        require(month in 1..12) { "Month must be between 1 and 12, got $month" }
        require(dayOfMonth in 1..daysInMonth(year, month)) {
            "Day $dayOfMonth is invalid for month $month in year $year"
        }
    }

    fun toIsoString(): String {
        return String.format(Locale.US, "%04d-%02d-%02d", year, month, dayOfMonth)
    }

    fun toEpochDay(): Long {
        var y = year.toLong()
        var m = month.toLong()
        if (m <= 2) {
            y -= 1
            m += 12
        }
        val era = if (y >= 0) y / 400 else (y - 399) / 400
        val yoe = y - era * 400
        val doy = (153 * (m - 3) + 2) / 5 + dayOfMonth - 1
        val doe = yoe * 365 + yoe / 4 - yoe / 100 + doy
        return era * 146097 + doe - 719468
    }

    fun plusDays(days: Long): LocalDateCompat {
        if (days == 0L) return this
        return fromEpochDay(toEpochDay() + days)
    }

    fun minusDays(days: Long): LocalDateCompat {
        return plusDays(-days)
    }

    fun daysUntil(other: LocalDateCompat): Long {
        return other.toEpochDay() - this.toEpochDay()
    }

    fun dayOfWeek(): Int {
        // 1 = Monday, 7 = Sunday
        val epochDay = toEpochDay()
        val dow = ((epochDay + 3) % 7).toInt()
        return if (dow < 0) dow + 7 + 1 else dow + 1
    }

    override fun compareTo(other: LocalDateCompat): Int {
        val cmpYear = year.compareTo(other.year)
        if (cmpYear != 0) return cmpYear
        val cmpMonth = month.compareTo(other.month)
        if (cmpMonth != 0) return cmpMonth
        return dayOfMonth.compareTo(other.dayOfMonth)
    }

    fun formatDisplay(): String {
        val monthNames = arrayOf(
            "Jan", "Feb", "Mar", "Apr", "May", "Jun",
            "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
        )
        return "${monthNames[month - 1]} $dayOfMonth, $year"
    }

    fun formatShortMonthDay(): String {
        val monthNames = arrayOf(
            "Jan", "Feb", "Mar", "Apr", "May", "Jun",
            "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
        )
        return "${monthNames[month - 1]} $dayOfMonth"
    }

    companion object {
        fun isLeapYear(year: Int): Boolean {
            return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)
        }

        fun daysInMonth(year: Int, month: Int): Int {
            return when (month) {
                1, 3, 5, 7, 8, 10, 12 -> 31
                4, 6, 9, 11 -> 30
                2 -> if (isLeapYear(year)) 29 else 28
                else -> 30
            }
        }

        fun now(): LocalDateCompat {
            val cal = Calendar.getInstance()
            return LocalDateCompat(
                year = cal.get(Calendar.YEAR),
                month = cal.get(Calendar.MONTH) + 1,
                dayOfMonth = cal.get(Calendar.DAY_OF_MONTH)
            )
        }

        fun parse(isoString: String): LocalDateCompat {
            val parts = isoString.trim().split("-")
            require(parts.size == 3) { "Invalid ISO date string: $isoString" }
            return LocalDateCompat(
                year = parts[0].toInt(),
                month = parts[1].toInt(),
                dayOfMonth = parts[2].toInt()
            )
        }

        fun parseOrNull(isoString: String?): LocalDateCompat? {
            if (isoString.isNullOrBlank()) return null
            return try {
                parse(isoString)
            } catch (e: Exception) {
                null
            }
        }

        fun fromEpochDay(epochDay: Long): LocalDateCompat {
            val z = epochDay + 719468
            val era = if (z >= 0) z / 146097 else (z - 146096) / 146097
            val doe = z - era * 146097
            val yoe = (doe - doe / 1460 + doe / 36524 - doe / 146096) / 365
            val y = yoe + era * 400
            val doy = doe - (365 * yoe + yoe / 4 - yoe / 100)
            val mp = (5 * doy + 2) / 153
            val d = doy - (153 * mp + 2) / 5 + 1
            val m = mp + if (mp < 10) 3 else -9
            val year = (y + if (m <= 2) 1 else 0).toInt()
            return LocalDateCompat(year, m.toInt(), d.toInt())
        }
    }
}
