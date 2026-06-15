package com.tripplanner.android.feature.optimizer

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Sailing
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tripplanner.android.core.holidays.CountryCode
import com.tripplanner.android.ui.components.SegmentedControl
import com.tripplanner.android.ui.components.Stepper
import com.tripplanner.android.ui.components.TripBadge
import com.tripplanner.android.ui.components.TripBadgeVariant
import com.tripplanner.android.ui.components.TripButton
import com.tripplanner.android.ui.components.TripCard
import com.tripplanner.android.ui.theme.TripPlannerTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

private val dateFmt: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM d", Locale.ENGLISH)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OptimizerHomeScreen(
    onOpenCatalog: () -> Unit = {},
    onOpenCalendar: () -> Unit = {},
    viewModel: OptimizerViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Leave Optimizer ${state.year}", style = MaterialTheme.typography.titleLarge)
                        Text(
                            "${state.leaveRemaining} of ${state.annualLeave} leave days left",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onOpenCalendar) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = "Year calendar")
                    }
                    IconButton(onClick = onOpenCatalog) {
                        Icon(Icons.Default.Palette, contentDescription = "Design catalog")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                ),
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        LazyColumn(
            contentPadding = padding,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
        ) {
            item { Spacer(Modifier.height(0.dp)) }

            // ── Controls ──────────────────────────────────────────────────────
            item {
                ControlsCard(
                    state = state,
                    onCountry = viewModel::setCountry,
                    onYear = viewModel::setYear,
                    onAnnualLeave = viewModel::setAnnualLeave,
                    onTripDays = viewModel::setTripDays,
                )
            }

            // ── Suggestions ───────────────────────────────────────────────────
            item {
                Text(
                    "Smart suggestions",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }

            if (state.isLoading) {
                items(3) { SuggestionSkeleton() }
            } else if (state.suggestions.isEmpty()) {
                item { EmptySuggestions() }
            } else {
                // Animate the set as a whole when country/year/tripDays change.
                item {
                    AnimatedContent(
                        targetState = Triple(state.country, state.year, state.tripDays),
                        transitionSpec = {
                            (fadeIn(tween(250)) togetherWith fadeOut(tween(150)))
                        },
                        label = "suggestions_set",
                    ) { _ ->
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            state.suggestions.forEachIndexed { index, suggestion ->
                                StaggeredReveal(index = index) {
                                    SuggestionCard(
                                        suggestion = suggestion,
                                        onAdd = { viewModel.addTrip(suggestion, destination = "") },
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ── Planned trips ─────────────────────────────────────────────────
            if (state.trips.isNotEmpty()) {
                item {
                    Text(
                        "Your trips",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
                items(state.trips, key = { it.id }) { trip ->
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn() + slideInVertically(spring()) { it / 3 },
                    ) {
                        PlannedTripRow(trip = trip, onRemove = { viewModel.removeTrip(trip.id) })
                    }
                }
            }

            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun ControlsCard(
    state: OptimizerUiState,
    onCountry: (CountryCode) -> Unit,
    onYear: (Int) -> Unit,
    onAnnualLeave: (Int) -> Unit,
    onTripDays: (Int) -> Unit,
) {
    TripCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            LabeledControl("Country") {
                val countries = state.supportedCountries
                SegmentedControl(
                    options = countries.map { it.label },
                    selectedIndex = countries.indexOf(state.country).coerceAtLeast(0),
                    onSelect = { onCountry(countries[it]) },
                )
            }

            LabeledControl("Year") {
                SegmentedControl(
                    options = state.availableYears.map { it.toString() },
                    selectedIndex = state.availableYears.indexOf(state.year).coerceAtLeast(0),
                    onSelect = { onYear(state.availableYears[it]) },
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Annual leave", style = MaterialTheme.typography.labelLarge)
                Stepper(
                    value = state.annualLeave,
                    onValueChange = onAnnualLeave,
                    range = 0..60,
                    suffix = "days",
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Trip length", style = MaterialTheme.typography.labelLarge)
                Stepper(
                    value = state.tripDays,
                    onValueChange = onTripDays,
                    range = 1..30,
                    suffix = "days",
                )
            }
        }
    }
}

@Composable
private fun LabeledControl(label: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, style = MaterialTheme.typography.labelLarge)
        content()
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SuggestionCard(suggestion: TripSuggestion, onAdd: () -> Unit) {
    TripCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    "${suggestion.startDate.format(dateFmt)} – ${suggestion.endDate.format(dateFmt)}",
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    weekdayRange(suggestion.startDate, suggestion.endDate),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            TripBadge(
                "Saves ${suggestion.savedDays}",
                variant = TripBadgeVariant.Success,
            )
        }

        Spacer(Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            Metric(value = "${suggestion.daysOff}", label = "days off")
            Metric(value = "${suggestion.leaveDaysNeeded}", label = "leave spent")
            Metric(value = String.format(Locale.ENGLISH, "%.1f×", suggestion.efficiency), label = "efficiency")
        }

        if (suggestion.nearHolidays.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                suggestion.nearHolidays.take(3).forEach { name ->
                    TripBadge(name, variant = TripBadgeVariant.Default)
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        TripButton(onClick = onAdd, modifier = Modifier.fillMaxWidth()) {
            Text("Add as trip")
        }
    }
}

@Composable
private fun Metric(value: String, label: String) {
    Column {
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun PlannedTripRow(trip: PlannedTrip, onRemove: () -> Unit) {
    TripCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape),
            ) {
                Icon(
                    Icons.Default.Sailing,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
            Spacer(Modifier.size(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(trip.destination, style = MaterialTheme.typography.titleSmall)
                Text(
                    "${trip.startDate.format(dateFmt)} – ${trip.endDate.format(dateFmt)} · ${trip.leaveDaysNeeded} leave",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(onClick = onRemove) {
                Icon(Icons.Default.Close, contentDescription = "Remove trip")
            }
        }
    }
}

@Composable
private fun EmptySuggestions() {
    TripCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
        ) {
            Icon(
                Icons.Default.CalendarMonth,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(32.dp),
            )
            Text(
                "No efficient windows for this trip length.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                "Try a different trip length or year.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SuggestionSkeleton() {
    TripCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            ShimmerBar(widthFraction = 0.5f, height = 20.dp)
            ShimmerBar(widthFraction = 0.3f, height = 14.dp)
            Spacer(Modifier.height(4.dp))
            ShimmerBar(widthFraction = 0.9f, height = 36.dp)
        }
    }
}

@Composable
private fun ShimmerBar(widthFraction: Float, height: androidx.compose.ui.unit.Dp) {
    Box(
        modifier = Modifier
            .fillMaxWidth(widthFraction)
            .height(height)
            .clip(MaterialTheme.shapes.small)
            .background(MaterialTheme.colorScheme.surfaceVariant),
    )
}

/** Fades + slides a list item in, delayed by its position for a staggered reveal. */
@Composable
private fun StaggeredReveal(index: Int, content: @Composable () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    androidx.compose.runtime.LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(index * 60L)
        visible = true
    }
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(300, easing = LinearOutSlowInEasing)) +
            slideInVertically(tween(300, easing = LinearOutSlowInEasing)) { it / 4 },
    ) {
        content()
    }
}

private fun weekdayRange(start: LocalDate, end: LocalDate): String {
    val s = start.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.ENGLISH)
    val e = end.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.ENGLISH)
    return "$s → $e"
}

@Preview(name = "Optimizer · Light", showBackground = true)
@Composable
private fun OptimizerLightPreview() {
    TripPlannerTheme(darkTheme = false) {
        OptimizerHomeScreen()
    }
}

@Preview(name = "Optimizer · Dark", showBackground = true, backgroundColor = 0xFF1C1C1C)
@Composable
private fun OptimizerDarkPreview() {
    TripPlannerTheme(darkTheme = true) {
        OptimizerHomeScreen()
    }
}
