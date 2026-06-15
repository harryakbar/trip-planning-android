package com.tripplanner.android.data

import com.tripplanner.android.data.local.TripEntity
import com.tripplanner.android.feature.optimizer.PlannedTrip
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class TripMapperTest {

    @Test
    fun `entity maps to domain with date restored from epoch day`() {
        val entity = TripEntity(
            id = 7,
            destination = "Bali",
            startDateEpochDay = LocalDate.of(2026, 6, 1).toEpochDay(),
            tripDays = 5,
            leaveDaysNeeded = 3,
            notes = "snorkeling",
        )
        val trip = entity.toDomain()
        assertEquals(7L, trip.id)
        assertEquals("Bali", trip.destination)
        assertEquals(LocalDate.of(2026, 6, 1), trip.startDate)
        assertEquals(5, trip.tripDays)
        assertEquals(3, trip.leaveDaysNeeded)
        assertEquals("snorkeling", trip.notes)
        assertEquals(LocalDate.of(2026, 6, 5), trip.endDate)
    }

    @Test
    fun `domain maps to entity zeroing id for insertion`() {
        val trip = PlannedTrip(
            id = 42,
            destination = "Tokyo",
            startDate = LocalDate.of(2026, 12, 24),
            tripDays = 4,
            leaveDaysNeeded = 2,
            notes = "ski",
        )
        val entity = trip.toEntity(id = 0)
        assertEquals(0L, entity.id)
        assertEquals("Tokyo", entity.destination)
        assertEquals(LocalDate.of(2026, 12, 24).toEpochDay(), entity.startDateEpochDay)
        assertEquals(4, entity.tripDays)
        assertEquals(2, entity.leaveDaysNeeded)
        assertEquals("ski", entity.notes)
    }

    @Test
    fun `round trip preserves the domain trip`() {
        val original = PlannedTrip(
            id = 0,
            destination = "Seoul",
            startDate = LocalDate.of(2026, 3, 15),
            tripDays = 6,
            leaveDaysNeeded = 4,
            notes = "",
        )
        // Simulate Room assigning id 1 on insert.
        val stored = original.toEntity(id = 0).copy(id = 1)
        val restored = stored.toDomain()
        assertEquals(original.copy(id = 1), restored)
    }
}
