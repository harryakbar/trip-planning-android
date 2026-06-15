package com.tripplanner.android.feature.timeline

import com.tripplanner.android.core.holidays.ResolvedHoliday
import com.tripplanner.android.feature.optimizer.PlannedTrip
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class TimelineModelsTest {

    private fun holiday(month: Int, day: Int, name: String = "H", cuti: Boolean = false) =
        ResolvedHoliday(LocalDate.of(2026, month, day), name, isCutiBersama = cuti)

    private fun trip(id: Long, month: Int, day: Int, days: Int = 3) = PlannedTrip(
        id = id,
        destination = "Trip $id",
        startDate = LocalDate.of(2026, month, day),
        tripDays = days,
        leaveDaysNeeded = days,
    )

    @Test
    fun `timeline merges holidays and trips sorted by date`() {
        val entries = buildTimeline(
            holidays = listOf(holiday(3, 1), holiday(1, 1)),
            trips = listOf(trip(1, 2, 1)),
        )
        assertEquals(3, entries.size)
        assertEquals(LocalDate.of(2026, 1, 1), entries[0].date)
        assertEquals(LocalDate.of(2026, 2, 1), entries[1].date)
        assertEquals(LocalDate.of(2026, 3, 1), entries[2].date)
    }

    @Test
    fun `holiday sorts before trip on the same day`() {
        val entries = buildTimeline(
            holidays = listOf(holiday(5, 10, "Labour Day")),
            trips = listOf(trip(1, 5, 10)),
        )
        assertTrue(entries[0] is TimelineEntry.Holiday)
        assertTrue(entries[1] is TimelineEntry.Trip)
    }

    @Test
    fun `trips carry a stable colour index`() {
        val entries = buildTimeline(
            holidays = emptyList(),
            trips = listOf(trip(1, 1, 1), trip(2, 6, 1)),
        )
        val tripEntries = entries.filterIsInstance<TimelineEntry.Trip>()
        assertEquals(0, tripEntries.first { it.trip.id == 1L }.colorIndex)
        assertEquals(1, tripEntries.first { it.trip.id == 2L }.colorIndex)
    }

    @Test
    fun `today anchor is first entry on or after today`() {
        val entries = buildTimeline(
            holidays = listOf(holiday(1, 1), holiday(6, 1), holiday(12, 1)),
            trips = emptyList(),
        )
        assertEquals(1, todayAnchorIndex(entries, LocalDate.of(2026, 5, 1)))
    }

    @Test
    fun `today anchor equals size when all entries are in the past`() {
        val entries = buildTimeline(
            holidays = listOf(holiday(1, 1), holiday(2, 1)),
            trips = emptyList(),
        )
        assertEquals(entries.size, todayAnchorIndex(entries, LocalDate.of(2026, 12, 31)))
    }

    @Test
    fun `empty inputs produce empty timeline`() {
        assertTrue(buildTimeline(emptyList(), emptyList()).isEmpty())
    }
}
