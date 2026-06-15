package com.tripplanner.android.data

import com.tripplanner.android.data.local.TripEntity
import com.tripplanner.android.feature.optimizer.PlannedTrip
import java.time.LocalDate

/** Pure mapping between the persisted [TripEntity] and the domain [PlannedTrip]. */
fun TripEntity.toDomain(): PlannedTrip = PlannedTrip(
    id = id,
    destination = destination,
    startDate = LocalDate.ofEpochDay(startDateEpochDay),
    tripDays = tripDays,
    leaveDaysNeeded = leaveDaysNeeded,
    notes = notes,
)

/**
 * Maps a domain trip to a row for insertion. [id] defaults to 0 so Room assigns
 * the primary key; pass an existing id only when updating a known row.
 */
fun PlannedTrip.toEntity(id: Long = this.id): TripEntity = TripEntity(
    id = id,
    destination = destination,
    startDateEpochDay = startDate.toEpochDay(),
    tripDays = tripDays,
    leaveDaysNeeded = leaveDaysNeeded,
    notes = notes,
)
