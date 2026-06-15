package com.tripplanner.android.feature.itinerary

import com.tripplanner.android.feature.optimizer.PlannedTrip
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class StoriesTest {

    private val trip = PlannedTrip(5, "Bali", LocalDate.of(2026, 6, 1), tripDays = 3, leaveDaysNeeded = 2)
    private val itinerary = SampleItinerary.forTrip(trip, Currency.SGD)

    @Test
    fun `flatten assigns sequential indices across all days`() {
        val entries = flattenStories(itinerary)
        val total = itinerary.days.sumOf { it.activities.size }
        assertEquals(total, entries.size)
        assertEquals(List(total) { it }, entries.map { it.flatIndex })
    }

    @Test
    fun `flatten preserves day grouping order`() {
        val entries = flattenStories(itinerary)
        // Day numbers must be non-decreasing as flatIndex increases.
        val dayNumbers = entries.map { it.dayNumber }
        assertEquals(dayNumbers.sorted(), dayNumbers)
        assertEquals(itinerary.days.first().activities.first().id, entries.first().activity.id)
    }

    @Test
    fun `storyIndexOf returns the flat index of an activity`() {
        val secondDay = itinerary.days[1]
        val firstOfDay2 = secondDay.activities.first()
        val expected = itinerary.days[0].activities.size
        assertEquals(expected, storyIndexOf(itinerary, firstOfDay2.id))
    }

    @Test
    fun `storyIndexOf returns zero for an unknown activity`() {
        assertEquals(0, storyIndexOf(itinerary, "does-not-exist"))
    }
}
