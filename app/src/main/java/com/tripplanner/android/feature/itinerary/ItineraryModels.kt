package com.tripplanner.android.feature.itinerary

import com.tripplanner.android.ui.components.TripCategory
import java.time.LocalDate
import java.time.LocalTime

/** Supported itinerary currencies, mirroring the web `BudgetEstimate.currency`. */
enum class Currency(val symbol: String) {
    SGD("S$"),
    IDR("Rp"),
}

/** Formats a whole-unit amount with locale-ish grouping. e.g. 1234 → "S$1,234". */
fun Currency.format(amount: Long): String {
    val grouped = groupThousands(amount, if (this == Currency.IDR) '.' else ',')
    return if (this == Currency.IDR) "$symbol $grouped" else "$symbol$grouped"
}

private fun groupThousands(value: Long, separator: Char): String {
    val digits = kotlin.math.abs(value).toString()
    val sb = StringBuilder()
    for ((i, c) in digits.withIndex()) {
        if (i > 0 && (digits.length - i) % 3 == 0) sb.append(separator)
        sb.append(c)
    }
    return (if (value < 0) "-" else "") + sb
}

data class BudgetEstimate(val currency: Currency, val amount: Long)

data class GeoLocation(val lat: Double, val lng: Double, val address: String)

data class ActivityItem(
    val id: String,
    val name: String,
    val description: String,
    val location: GeoLocation,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val category: TripCategory,
    val estimatedCost: BudgetEstimate,
)

data class ItineraryDay(
    val dayNumber: Int,
    val date: LocalDate,
    val activities: List<ActivityItem>,
)

data class Itinerary(
    val tripId: Long,
    val destination: String,
    val days: List<ItineraryDay>,
    val currency: Currency,
) {
    val totalBudget: Long get() = days.sumOf { dayBudget(it) }
}

/** Sum of a single day's activity costs. */
fun dayBudget(day: ItineraryDay): Long = day.activities.sumOf { it.estimatedCost.amount }

/**
 * Budget grouped by category across the whole itinerary, descending by amount.
 * Drives the budget-breakdown chart.
 */
fun Itinerary.budgetByCategory(): List<Pair<TripCategory, Long>> =
    days.asSequence()
        .flatMap { it.activities.asSequence() }
        .groupBy { it.category }
        .map { (category, items) -> category to items.sumOf { it.estimatedCost.amount } }
        .sortedByDescending { it.second }
