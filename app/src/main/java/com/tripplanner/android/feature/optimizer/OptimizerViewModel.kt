package com.tripplanner.android.feature.optimizer

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tripplanner.android.core.holidays.CountryCode
import com.tripplanner.android.core.holidays.HolidayRepository
import com.tripplanner.android.core.holidays.ResolvedHoliday
import com.tripplanner.android.core.optimizer.LeaveOptimization
import com.tripplanner.android.data.TripRepository
import com.tripplanner.android.data.local.TripDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

data class OptimizerUiState(
    val country: CountryCode = CountryCode.SG,
    val year: Int = LocalDate.now().year,
    val annualLeave: Int = CountryCode.SG.defaultAnnualLeave,
    val tripDays: Int = 5,
    val availableYears: List<Int> = emptyList(),
    val suggestions: List<TripSuggestion> = emptyList(),
    val holidays: List<ResolvedHoliday> = emptyList(),
    val trips: List<PlannedTrip> = emptyList(),
    val isLoading: Boolean = true,
) {
    val leaveUsed: Int get() = trips.sumOf { it.leaveDaysNeeded }
    val leaveRemaining: Int get() = annualLeave - leaveUsed
    val supportedCountries: List<CountryCode> get() = HolidayRepository.getSupportedCountries()
}

/**
 * Drives the optimizer/calendar/timeline screens. Trips are persisted locally
 * via [TripRepository] (Room) and observed as a flow, so they survive process
 * death and app restarts; the rest of the state (country/year/leave/suggestions)
 * is ephemeral UI state. Cloud sync will extend the repository, not this class.
 */
class OptimizerViewModel(app: Application) : AndroidViewModel(app) {

    private val trips: TripRepository =
        TripRepository(TripDatabase.getInstance(app).tripDao())

    private val _state = MutableStateFlow(initialState())
    val state: StateFlow<OptimizerUiState> = _state.asStateFlow()

    init {
        recomputeSuggestions()
        // Keep UI state's trip list in sync with the persisted source of truth.
        trips.observeTrips()
            .onEach { persisted -> _state.update { it.copy(trips = persisted) } }
            .launchIn(viewModelScope)
    }

    private fun initialState(): OptimizerUiState {
        val years = HolidayRepository.getAvailableYears(CountryCode.SG)
        return OptimizerUiState(
            country = CountryCode.SG,
            year = OptimizerEngine.defaultYear(years),
            annualLeave = CountryCode.SG.defaultAnnualLeave,
            availableYears = years,
        )
    }

    fun setCountry(country: CountryCode) {
        if (country == _state.value.country) return
        val years = HolidayRepository.getAvailableYears(country)
        _state.update {
            it.copy(
                country = country,
                availableYears = years,
                year = if (it.year in years) it.year else OptimizerEngine.defaultYear(years),
                annualLeave = country.defaultAnnualLeave,
            )
        }
        recomputeSuggestions()
    }

    fun setYear(year: Int) {
        if (year == _state.value.year) return
        _state.update { it.copy(year = year) }
        recomputeSuggestions()
    }

    fun setAnnualLeave(days: Int) {
        _state.update { it.copy(annualLeave = days.coerceIn(0, 60)) }
    }

    fun setTripDays(days: Int) {
        val clamped = days.coerceIn(1, 30)
        if (clamped == _state.value.tripDays) return
        _state.update { it.copy(tripDays = clamped) }
        recomputeSuggestions()
    }

    fun addTrip(suggestion: TripSuggestion, destination: String) {
        val name = destination.ifBlank { "Trip ${_state.value.trips.size + 1}" }
        val trip = PlannedTrip(
            id = 0,
            destination = name,
            startDate = suggestion.startDate,
            tripDays = suggestion.tripDays,
            leaveDaysNeeded = suggestion.leaveDaysNeeded,
        )
        viewModelScope.launch { trips.addTrip(trip) }
    }

    fun removeTrip(id: Long) {
        viewModelScope.launch { trips.removeTrip(id) }
    }

    fun updateTripNotes(id: Long, notes: String) {
        viewModelScope.launch { trips.updateNotes(id, notes) }
    }

    fun addTripFromRange(start: LocalDate, end: LocalDate, destination: String) {
        if (end.isBefore(start)) return
        val snapshot = _state.value
        val tripDays = (java.time.temporal.ChronoUnit.DAYS.between(start, end) + 1).toInt()
        val prefix = LeaveOptimization.buildWorkingDayPrefix(snapshot.year, snapshot.holidays)
        val leave = LeaveOptimization.calculateWorkingDaysNeeded(start, tripDays, prefix)
        val trip = PlannedTrip(
            id = 0,
            destination = destination.ifBlank { "Trip ${snapshot.trips.size + 1}" },
            startDate = start,
            tripDays = tripDays,
            leaveDaysNeeded = leave,
        )
        viewModelScope.launch { trips.addTrip(trip) }
    }

    private fun recomputeSuggestions() {
        val snapshot = _state.value
        _state.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            val result = withContext(Dispatchers.Default) {
                val holidays: List<ResolvedHoliday> =
                    HolidayRepository.getHolidaysForYear(snapshot.country, snapshot.year)
                val scores = LeaveOptimization.calculateOptimizationScores(
                    tripDays = snapshot.tripDays,
                    year = snapshot.year,
                    holidays = holidays,
                )
                holidays to OptimizerEngine.pickTopSuggestions(scores, snapshot.tripDays)
            }
            val (holidays, suggestions) = result
            _state.update {
                if (it.country == snapshot.country && it.year == snapshot.year &&
                    it.tripDays == snapshot.tripDays
                ) {
                    it.copy(suggestions = suggestions, holidays = holidays, isLoading = false)
                } else {
                    it
                }
            }
        }
    }
}
