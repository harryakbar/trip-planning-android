package com.tripplanner.android.feature.optimizer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tripplanner.android.core.holidays.CountryCode
import com.tripplanner.android.core.holidays.HolidayRepository
import com.tripplanner.android.core.holidays.ResolvedHoliday
import com.tripplanner.android.core.optimizer.LeaveOptimization
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    val trips: List<PlannedTrip> = emptyList(),
    val isLoading: Boolean = true,
) {
    val leaveUsed: Int get() = trips.sumOf { it.leaveDaysNeeded }
    val leaveRemaining: Int get() = annualLeave - leaveUsed
    val supportedCountries: List<CountryCode> get() = HolidayRepository.getSupportedCountries()
}

/**
 * Drives the optimizer home screen. Holds country / year / annual-leave / trip-day
 * selections and recomputes engine suggestions whenever inputs change. Mirrors the
 * web `tripStore` (Zustand) state shape, minus the deferred group/cloud concerns.
 */
class OptimizerViewModel : ViewModel() {

    private val _state = MutableStateFlow(initialState())
    val state: StateFlow<OptimizerUiState> = _state.asStateFlow()

    private var nextTripId = 1L

    init {
        recomputeSuggestions()
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
        val trip = PlannedTrip(
            id = nextTripId++,
            destination = destination.ifBlank { "Trip ${_state.value.trips.size + 1}" },
            startDate = suggestion.startDate,
            tripDays = suggestion.tripDays,
            leaveDaysNeeded = suggestion.leaveDaysNeeded,
        )
        _state.update { it.copy(trips = it.trips + trip) }
    }

    fun removeTrip(id: Long) {
        _state.update { s -> s.copy(trips = s.trips.filterNot { it.id == id }) }
    }

    private fun recomputeSuggestions() {
        val snapshot = _state.value
        _state.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            val suggestions = withContext(Dispatchers.Default) {
                val holidays: List<ResolvedHoliday> =
                    HolidayRepository.getHolidaysForYear(snapshot.country, snapshot.year)
                val scores = LeaveOptimization.calculateOptimizationScores(
                    tripDays = snapshot.tripDays,
                    year = snapshot.year,
                    holidays = holidays,
                )
                OptimizerEngine.pickTopSuggestions(scores, snapshot.tripDays)
            }
            // Only apply if inputs haven't changed since this computation started.
            _state.update {
                if (it.country == snapshot.country && it.year == snapshot.year &&
                    it.tripDays == snapshot.tripDays
                ) {
                    it.copy(suggestions = suggestions, isLoading = false)
                } else {
                    it
                }
            }
        }
    }
}
