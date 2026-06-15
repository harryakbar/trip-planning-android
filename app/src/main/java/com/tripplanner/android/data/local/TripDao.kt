package com.tripplanner.android.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TripDao {

    /** All trips, chronologically by start date (then insertion order). */
    @Query("SELECT * FROM trips ORDER BY startDateEpochDay ASC, createdAt ASC")
    fun observeAll(): Flow<List<TripEntity>>

    @Insert
    suspend fun insert(trip: TripEntity): Long

    @Query("DELETE FROM trips WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE trips SET notes = :notes WHERE id = :id")
    suspend fun updateNotes(id: Long, notes: String)
}
