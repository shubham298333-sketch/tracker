package com.example.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LocalDateCompatTest {

    @Test
    fun testLeapYearCalculation() {
        assertTrue(LocalDateCompat.isLeapYear(2024))
        assertTrue(LocalDateCompat.isLeapYear(2000))
        assertFalse(LocalDateCompat.isLeapYear(1900))
        assertFalse(LocalDateCompat.isLeapYear(2023))
        assertFalse(LocalDateCompat.isLeapYear(2025))
    }

    @Test
    fun testDaysInMonth() {
        assertEquals(31, LocalDateCompat.daysInMonth(2024, 1))
        assertEquals(29, LocalDateCompat.daysInMonth(2024, 2)) // Leap year
        assertEquals(28, LocalDateCompat.daysInMonth(2023, 2)) // Normal year
        assertEquals(30, LocalDateCompat.daysInMonth(2024, 4))
    }

    @Test
    fun testPlusMinusDaysAcrossMonthBoundary() {
        val jan30 = LocalDateCompat(2024, 1, 30)
        val feb2 = jan30.plusDays(3)
        assertEquals(2024, feb2.year)
        assertEquals(2, feb2.month)
        assertEquals(2, feb2.dayOfMonth)

        val backToJan = feb2.minusDays(3)
        assertEquals(jan30, backToJan)
    }

    @Test
    fun testDaysUntil() {
        val d1 = LocalDateCompat(2024, 3, 1)
        val d2 = LocalDateCompat(2024, 3, 29)
        assertEquals(28L, d1.daysUntil(d2))
    }

    @Test
    fun testIsoFormattingAndParsing() {
        val d = LocalDateCompat(2024, 5, 8)
        assertEquals("2024-05-08", d.toIsoString())
        val parsed = LocalDateCompat.parse("2024-05-08")
        assertEquals(d, parsed)
    }
}
