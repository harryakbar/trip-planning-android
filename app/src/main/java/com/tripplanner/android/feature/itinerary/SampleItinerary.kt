package com.tripplanner.android.feature.itinerary

import com.tripplanner.android.feature.optimizer.PlannedTrip
import com.tripplanner.android.ui.components.TripCategory
import java.time.LocalTime
import kotlin.random.Random

/**
 * Builds a plausible, **deterministic** sample itinerary for a trip. Stands in
 * for the real AI generation flow (#11) so the viewer can be built and reviewed
 * without the backend. Same trip → same itinerary (seeded by trip id), so the
 * UI is stable across recompositions and process death.
 */
object SampleItinerary {

    private data class Template(val name: String, val description: String, val category: TripCategory)

    private val templates = listOf(
        Template("Breakfast at a local cafe", "Start the day with regional coffee and pastries.", TripCategory.Food),
        Template("Old Town walking tour", "Wander historic streets and landmark squares.", TripCategory.Sightseeing),
        Template("Museum of Modern Art", "Rotating exhibits and a rooftop sculpture garden.", TripCategory.Sightseeing),
        Template("Street food market", "Graze through stalls of local specialties.", TripCategory.Food),
        Template("Harbour ferry ride", "Scenic crossing with skyline views.", TripCategory.Transport),
        Template("Botanic gardens", "Shaded trails and a glasshouse of orchids.", TripCategory.Activity),
        Template("Sunset viewpoint", "Golden-hour panorama over the city.", TripCategory.Sightseeing),
        Template("Cooking class", "Hands-on session making three local dishes.", TripCategory.Activity),
        Template("Riverside dinner", "Seasonal tasting menu by the water.", TripCategory.Food),
        Template("Check in to the hotel", "Drop bags and freshen up before exploring.", TripCategory.Accommodation),
    )

    // Rough cost bands per category, in SGD whole units; scaled up for IDR.
    private fun baseCost(category: TripCategory, rng: Random): Long = when (category) {
        TripCategory.Food -> 15L + rng.nextInt(40)
        TripCategory.Sightseeing -> 10L + rng.nextInt(30)
        TripCategory.Transport -> 5L + rng.nextInt(20)
        TripCategory.Accommodation -> 90L + rng.nextInt(120)
        TripCategory.Activity -> 25L + rng.nextInt(60)
    }

    fun forTrip(trip: PlannedTrip, currency: Currency): Itinerary {
        val rng = Random(trip.id * 31 + trip.destination.hashCode())
        val days = (0 until trip.tripDays).map { dayIndex ->
            val date = trip.startDate.plusDays(dayIndex.toLong())
            val count = 3 + rng.nextInt(3) // 3..5 activities
            var clock = LocalTime.of(8, 30)
            val activities = (0 until count).map { actIndex ->
                val template = templates[rng.nextInt(templates.size)]
                val start = clock
                val durationMin = 60L + rng.nextInt(4) * 30L // 60..150 min
                val end = start.plusMinutes(durationMin)
                // Gap before the next activity.
                clock = end.plusMinutes(30L + rng.nextInt(3) * 15L)
                val sgd = baseCost(template.category, rng)
                val amount = if (currency == Currency.IDR) sgd * 11_500L else sgd
                ActivityItem(
                    id = "${trip.id}-$dayIndex-$actIndex",
                    name = template.name,
                    description = template.description,
                    location = GeoLocation(
                        lat = 1.29 + rng.nextDouble() * 0.05,
                        lng = 103.82 + rng.nextDouble() * 0.05,
                        address = "${10 + rng.nextInt(200)} ${trip.destination} District",
                    ),
                    startTime = start,
                    endTime = end,
                    category = template.category,
                    estimatedCost = BudgetEstimate(currency, amount),
                )
            }.sortedBy { it.startTime }
            ItineraryDay(dayNumber = dayIndex + 1, date = date, activities = activities)
        }
        return Itinerary(
            tripId = trip.id,
            destination = trip.destination,
            days = days,
            currency = currency,
        )
    }
}
