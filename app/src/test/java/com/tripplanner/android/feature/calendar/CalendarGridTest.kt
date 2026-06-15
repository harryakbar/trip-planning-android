package com.tripplanner.android.feature.calendar

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate

class CalendarGridTest {

    @Test
    fun `grid is padded to whole weeks`() {
        val cells = CalendarGrid.monthCells(2026, 1)
        assertEquals(0, cells.size % 7)
    }

    @Test
    fun `grid starts on Sunday and ends on Saturday`() {
        val cells = CalendarGrid.monthCells(2026, 1)
        assertEquals(DayOfWeek.SUNDAY, cells.first().dayOfWeek)
        assertEquals(DayOfWeek.SATURDAY, cells.last().dayOfWeek)
    }

    @Test
    fun `January 2026 grid begins on the preceding Sunday`() {
        // Jan 1 2026 is a Thursday → grid starts Sun Dec 28 2025.
        val cells = CalendarGrid.monthCells(2026, 1)
        assertEquals(LocalDate.of(2025, 12, 28), cells.first())
    }

    @Test
    fun `grid contains every day of the target month`() {
        val cells = CalendarGrid.monthCells(2026, 2).toSet()
        var d = LocalDate.of(2026, 2, 1)
        val end = LocalDate.of(2026, 2, 28)
        while (!d.isAfter(end)) {
            assertTrue("missing $d", cells.contains(d))
            d = d.plusDays(1)
        }
    }

    @Test
    fun `month starting on Sunday has no leading padding`() {
        // Feb 1 2026 is a Sunday.
        val cells = CalendarGrid.monthCells(2026, 2)
        assertEquals(LocalDate.of(2026, 2, 1), cells.first())
    }
}
