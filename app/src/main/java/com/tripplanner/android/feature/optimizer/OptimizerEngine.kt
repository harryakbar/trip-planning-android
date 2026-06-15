package com.tripplanner.android.feature.optimizer

import com.tripplanner.android.core.optimizer.DateOptimization
import com.tripplanner.android.core.util.datesOverlap
import java.time.LocalDate

/** A trip the user has committed to, derived from a suggestion or manual entry. */
data class PlannedTrip(
    val id: Long,
    val destination: String,
    val startDate: LocalDate,
    val tripDays: Int,
    val leaveDaysNeeded: Int,
) {
    val endDate: LocalDate get() = startDate.plusDays((tripDays - 1).toLong())
}

/** A recommended trip window produced by the optimizer. */
data class TripSuggestion(
    val startDate: LocalDate,
    val tripDays: Int,
    val leaveDaysNeeded: Int,
    val nearHolidays: List<String>,
) {
    val endDate: LocalDate get() = startDate.plusDays((tripDays - 1).toLong())

    /** Total consecutive calendar days off (the trip span). */
    val daysOff: Int get() = tripDays

    /** Days off per leave day spent — higher is more efficient. */
    val efficiency: Double get() = tripDays.toDouble() / leaveDaysNeeded

    /** How many leave days this window saves versus a plain block of the same length. */
    val savedDays: Int get() = tripDays - leaveDaysNeeded
}

/**
 * Pure selection logic over the engine's per-date scores. Kept free of Android
 * and ViewModel deps so it can be unit-tested directly.
 */
object OptimizerEngine {

    /**
     * Picks the most efficient, non-overlapping trip windows. Only windows that
     * beat a plain leave block (efficiency > 1, i.e. they fold in weekends or
     * holidays) are considered. Greedy by efficiency, then chronological.
     */
    fun pickTopSuggestions(
        scores: List<DateOptimization>,
        tripDays: Int,
        limit: Int = 5,
    ): List<TripSuggestion> {
        val sorted = scores.sortedWith(
            compareByDescending<DateOptimization> { it.efficiency }.thenBy { it.date },
        )
        val picked = ArrayList<TripSuggestion>(limit)
        for (score in sorted) {
            if (score.efficiency <= 1.0) continue
            val end = score.date.plusDays((tripDays - 1).toLong())
            val overlaps = picked.any { datesOverlap(score.date, end, it.startDate, it.endDate) }
            if (overlaps) continue
            picked.add(
                TripSuggestion(
                    startDate = score.date,
                    tripDays = tripDays,
                    leaveDaysNeeded = score.leaveDaysNeeded,
                    nearHolidays = score.nearHolidays,
                ),
            )
            if (picked.size >= limit) break
        }
        return picked.sortedBy { it.startDate }
    }

    fun defaultYear(available: List<Int>, today: LocalDate = LocalDate.now()): Int =
        if (today.year in available) today.year else available.firstOrNull() ?: today.year
}
