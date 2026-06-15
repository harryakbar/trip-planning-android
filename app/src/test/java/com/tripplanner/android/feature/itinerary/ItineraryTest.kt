package com.tripplanner.android.feature.itinerary

import com.tripplanner.android.feature.optimizer.PlannedTrip
import com.tripplanner.android.ui.components.TripCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime

class ItineraryTest {

    private fun activity(category: TripCategory, amount: Long, hour: Int) = ActivityItem(
        id = "a$hour",
        name = "n",
        description = "d",
        location = GeoLocation(0.0, 0.0, "addr"),
        startTime = LocalTime.of(hour, 0),
        endTime = LocalTime.of(hour + 1, 0),
        category = category,
        estimatedCost = BudgetEstimate(Currency.SGD, amount),
    )

    @Test
    fun `currency formats with grouping`() {
        assertEquals("S$1,234", Currency.SGD.format(1234))
        assertEquals("S$0", Currency.SGD.format(0))
        assertEquals("Rp 1.250.000", Currency.IDR.format(1_250_000))
    }

    @Test
    fun `day budget sums activity costs`() {
        val day = ItineraryDay(
            dayNumber = 1,
            date = LocalDate.of(2026, 6, 1),
            activities = listOf(activity(TripCategory.Food, 30, 9), activity(TripCategory.Transport, 12, 11)),
        )
        assertEquals(42L, dayBudget(day))
    }

    @Test
    fun `total budget sums all days`() {
        val itinerary = Itinerary(
            tripId = 1,
            destination = "Bali",
            currency = Currency.SGD,
            days = listOf(
                ItineraryDay(1, LocalDate.of(2026, 6, 1), listOf(activity(TripCategory.Food, 30, 9))),
                ItineraryDay(2, LocalDate.of(2026, 6, 2), listOf(activity(TripCategory.Activity, 50, 10))),
            ),
        )
        assertEquals(80L, itinerary.totalBudget)
    }

    @Test
    fun `budget by category groups and sorts descending`() {
        val itinerary = Itinerary(
            tripId = 1,
            destination = "Bali",
            currency = Currency.SGD,
            days = listOf(
                ItineraryDay(
                    1, LocalDate.of(2026, 6, 1),
                    listOf(
                        activity(TripCategory.Food, 20, 9),
                        activity(TripCategory.Food, 30, 12),
                        activity(TripCategory.Transport, 100, 15),
                    ),
                ),
            ),
        )
        val breakdown = itinerary.budgetByCategory()
        assertEquals(TripCategory.Transport to 100L, breakdown[0])
        assertEquals(TripCategory.Food to 50L, breakdown[1])
    }

    @Test
    fun `sample itinerary has one day per trip day with sequential dates`() {
        val trip = PlannedTrip(7, "Tokyo", LocalDate.of(2026, 3, 10), tripDays = 4, leaveDaysNeeded = 2)
        val itinerary = SampleItinerary.forTrip(trip, Currency.SGD)
        assertEquals(4, itinerary.days.size)
        assertEquals(LocalDate.of(2026, 3, 10), itinerary.days.first().date)
        assertEquals(LocalDate.of(2026, 3, 13), itinerary.days.last().date)
        assertEquals(listOf(1, 2, 3, 4), itinerary.days.map { it.dayNumber })
    }

    @Test
    fun `sample itinerary is deterministic for the same trip`() {
        val trip = PlannedTrip(42, "Seoul", LocalDate.of(2026, 5, 1), tripDays = 3, leaveDaysNeeded = 2)
        val a = SampleItinerary.forTrip(trip, Currency.SGD)
        val b = SampleItinerary.forTrip(trip, Currency.SGD)
        assertEquals(a, b)
    }

    @Test
    fun `sample activities are time-ordered within a day`() {
        val trip = PlannedTrip(3, "Bali", LocalDate.of(2026, 7, 1), tripDays = 2, leaveDaysNeeded = 1)
        val itinerary = SampleItinerary.forTrip(trip, Currency.IDR)
        itinerary.days.forEach { day ->
            val times = day.activities.map { it.startTime }
            assertEquals(times.sorted(), times)
            assertTrue(day.activities.all { it.estimatedCost.currency == Currency.IDR })
        }
    }
}
