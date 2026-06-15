package com.tripplanner.android.feature.timeline

import com.tripplanner.android.core.holidays.ResolvedHoliday
import com.tripplanner.android.feature.optimizer.PlannedTrip
import java.time.LocalDate

/**
 * A single chronological entry on the timeline — either a resolved holiday or a
 * planned trip. Mirrors the merged holiday/trip rendering in the web
 * `TimelineView.tsx`, reshaped into a vertical, mobile-first list.
 */
sealed interface TimelineEntry {
    val date: LocalDate

    data class Holiday(val holiday: ResolvedHoliday) : TimelineEntry {
        override val date: LocalDate get() = holiday.date
    }

    data class Trip(val trip: PlannedTrip, val colorIndex: Int) : TimelineEntry {
        override val date: LocalDate get() = trip.startDate
    }
}

/**
 * Builds the chronological entry list for a year: every holiday plus every trip,
 * sorted by date (holidays before trips on the same day). Trips carry a stable
 * colour index matching the calendar's per-trip palette.
 */
fun buildTimeline(
    holidays: List<ResolvedHoliday>,
    trips: List<PlannedTrip>,
): List<TimelineEntry> {
    val entries = ArrayList<TimelineEntry>(holidays.size + trips.size)
    holidays.forEach { entries.add(TimelineEntry.Holiday(it)) }
    trips.forEachIndexed { index, trip -> entries.add(TimelineEntry.Trip(trip, index)) }
    return entries.sortedWith(
        compareBy<TimelineEntry> { it.date }.thenBy { it is TimelineEntry.Trip },
    )
}

/**
 * Index of the first entry on or after [today], or the entry count if every entry
 * is in the past. Used to drive scroll-to-today and the "today" marker position.
 */
fun todayAnchorIndex(entries: List<TimelineEntry>, today: LocalDate): Int {
    val idx = entries.indexOfFirst { !it.date.isBefore(today) }
    return if (idx == -1) entries.size else idx
}
