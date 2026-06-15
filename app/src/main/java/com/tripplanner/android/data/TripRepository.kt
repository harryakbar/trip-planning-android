package com.tripplanner.android.data

import com.tripplanner.android.data.local.TripDao
import com.tripplanner.android.feature.optimizer.PlannedTrip
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Local source of truth for planned trips, backed by Room. Exposes domain
 * [PlannedTrip]s and hides the entity/epoch-day mapping. Cloud sync (Supabase)
 * will layer on top of this without changing the ViewModel contract.
 */
class TripRepository(private val dao: TripDao) {

    fun observeTrips(): Flow<List<PlannedTrip>> =
        dao.observeAll().map { rows -> rows.map { it.toDomain() } }

    /** Inserts a new trip and returns the Room-assigned id. */
    suspend fun addTrip(trip: PlannedTrip): Long = dao.insert(trip.toEntity(id = 0))

    suspend fun removeTrip(id: Long) = dao.deleteById(id)

    suspend fun updateNotes(id: Long, notes: String) = dao.updateNotes(id, notes)
}
