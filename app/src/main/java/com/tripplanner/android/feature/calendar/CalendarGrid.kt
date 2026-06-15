package com.tripplanner.android.feature.calendar

import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

/**
 * Pure month-grid geometry for the year calendar. Sunday-first weeks, matching
 * the web `YearCalendar` (`startOfWeek` … `endOfWeek`). Returns the full grid of
 * cells including leading days from the previous month and trailing days from the
 * next, so every row has seven entries.
 */
object CalendarGrid {

    /** All day cells for [month] (1–12) of [year], padded to whole weeks. */
    fun monthCells(year: Int, month: Int): List<LocalDate> {
        val first = LocalDate.of(year, month, 1)
        val last = first.with(TemporalAdjusters.lastDayOfMonth())

        // DayOfWeek.value: Mon=1 … Sun=7. `% 7` maps Sunday→0 for a Sunday-first week.
        val leading = first.dayOfWeek.value % 7
        val trailing = 6 - (last.dayOfWeek.value % 7)

        val gridStart = first.minusDays(leading.toLong())
        val gridEnd = last.plusDays(trailing.toLong())

        val cells = ArrayList<LocalDate>()
        var d = gridStart
        while (!d.isAfter(gridEnd)) {
            cells.add(d)
            d = d.plusDays(1)
        }
        return cells
    }
}
