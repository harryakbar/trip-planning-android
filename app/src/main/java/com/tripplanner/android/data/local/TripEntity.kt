package com.tripplanner.android.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Persisted representation of a planned trip. Dates are stored as epoch-day
 * longs to keep the schema primitive and timezone-free; the domain layer maps
 * them back to `LocalDate`. Mirrors the trip fields the optimizer cares about
 * (the wider web `Trip` shape — userId/type/country — is deferred to the
 * cloud-sync work).
 */
@Entity(tableName = "trips")
data class TripEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val destination: String,
    val startDateEpochDay: Long,
    val tripDays: Int,
    val leaveDaysNeeded: Int,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
)
