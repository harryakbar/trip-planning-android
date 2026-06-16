@file:OptIn(ExperimentalSharedTransitionApi::class)

package com.tripplanner.android.feature.optimizer

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.LinearOutSlowInEasing
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
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Sailing
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tripplanner.android.R
import com.tripplanner.android.core.holidays.CountryCode
import com.tripplanner.android.core.i18n.AppLanguage
import com.tripplanner.android.core.i18n.LocaleManager
import com.tripplanner.android.feature.trips.DestinationDialog
import com.tripplanner.android.ui.components.SegmentedControl
import com.tripplanner.android.ui.components.Stepper
import com.tripplanner.android.ui.components.SwipeToDeleteContainer
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
    onOpenTimeline: () -> Unit = {},
    onTripClick: (PlannedTrip) -> Unit = {},
    viewModel: OptimizerViewModel = viewModel(),
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedContentScope: AnimatedContentScope? = null,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var pendingSuggestion by remember { mutableStateOf<TripSuggestion?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(stringResource(R.string.optimizer_title, state.year), style = MaterialTheme.typography.titleLarge)
                        Text(
                            stringResource(R.string.optimizer_leave_summary, state.leaveRemaining, state.annualLeave),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                actions = {
                    LanguageMenu()
                    IconButton(onClick = onOpenTimeline) {
                        Icon(Icons.Default.Timeline, contentDescription = stringResource(R.string.action_timeline))
                    }
                    IconButton(onClick = onOpenCalendar) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = stringResource(R.string.action_year_calendar))
                    }
                    IconButton(onClick = onOpenCatalog) {
                        Icon(Icons.Default.Palette, contentDescription = stringResource(R.string.action_design_catalog))
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

            // ── Controls ────────────────────────────────────────────────────────────────
            item {
                ControlsCard(
                    state = state,
                    onCountry = viewModel::setCountry,
                    onYear = viewModel::setYear,
                    onAnnualLeave = viewModel::setAnnualLeave,
                    onTripDays = viewModel::setTripDays,
                )
            }

            // ── Suggestions ──────────────────────────────────────────────────────────────
            item {
                Text(
                    stringResource(R.string.smart_suggestions),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }

            if (state.isLoading) {
                items(3) { SuggestionSkeleton() }
            } else if (state.suggestions.isEmpty()) {
                item { EmptySuggestions() }
            } else {
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
                                        overlapName = state.overlappingTrip(
                                            suggestion.startDate, suggestion.endDate,
                                        )?.destination,
                                        onAdd = { pendingSuggestion = suggestion },
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ── Planned trips ────────────────────────────────────────────────────────────
            if (state.trips.isNotEmpty()) {
                item {
                    Text(
                        stringResource(R.string.your_trips),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
                items(state.trips, key = { it.id }) { trip ->
                    SwipeToDeleteContainer(
                        onDelete = { viewModel.removeTrip(trip.id) },
                        modifier = Modifier.animateItem(),
                    ) {
                        PlannedTripRow(
                            trip = trip,
                            onClick = { onTripClick(trip) },
                            onRemove = { viewModel.removeTrip(trip.id) },
                            sharedTransitionScope = sharedTransitionScope,
                            animatedContentScope = animatedContentScope,
                        )
                    }
                }
            }

            item { Spacer(Modifier.height(24.dp)) }
        }
    }

    pendingSuggestion?.let { suggestion ->
        DestinationDialog(
            onConfirm = { destination ->
                viewModel.addTrip(suggestion, destination)
                pendingSuggestion = null
            },
            onDismiss = { pendingSuggestion = null },
        )
    }
}

/** Language picker in the top bar: switches the per-app locale and recreates. */
@Composable
private fun LanguageMenu() {
    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) }
    val current = remember { LocaleManager.getLanguage(context) }

    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(Icons.Default.Language, contentDescription = stringResource(R.string.action_language))
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            AppLanguage.entries.forEach { language ->
                val labelRes = when (language) {
                    AppLanguage.English -> R.string.language_english
                    AppLanguage.Indonesian -> R.string.language_indonesian
                }
                DropdownMenuItem(
                    text = { Text(stringResource(labelRes)) },
                    onClick = {
                        expanded = false
                        if (language != current) {
                            LocaleManager.setLanguage(context, language)
                            (context as? android.app.Activity)?.recreate()
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun Modifier.maybeSharedBounds(
    scope: SharedTransitionScope?,
    animScope: AnimatedContentScope?,
    key: Any,
): Modifier {
    if (scope == null || animScope == null) return this
    return with(scope) {
        this@maybeSharedBounds.sharedBounds(
            sharedContentState = rememberSharedContentState(key = key),
            animatedVisibilityScope = animScope,
            resizeMode = SharedTransitionScope.ResizeMode.RemeasureToBounds,
        )
    }
}

@Composable
private fun Modifier.maybeSharedElement(
    scope: SharedTransitionScope?,
    animScope: AnimatedContentScope?,
    key: Any,
): Modifier {
    if (scope == null || animScope == null) return this
    return with(scope) {
        this@maybeSharedElement.sharedElement(
            state = rememberSharedContentState(key = key),
            animatedVisibilityScope = animScope,
        )
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
            LabeledControl(stringResource(R.string.label_country)) {
                val countries = state.supportedCountries
                SegmentedControl(
                    options = countries.map { it.label },
                    selectedIndex = countries.indexOf(state.country).coerceAtLeast(0),
                    onSelect = { onCountry(countries[it]) },
                )
            }

            LabeledControl(stringResource(R.string.label_year)) {
                SegmentedControl(
                    options = state.availableYears.map { it.toString() },
                    selectedIndex = state.availableYears.indexOf(state.year).coerceAtLeast(0),
                    onSelect = { onYear(state.availableYears[it]) },
                )
            }

            val unitDays = stringResource(R.string.unit_days)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(stringResource(R.string.label_annual_leave), style = MaterialTheme.typography.labelLarge)
                Stepper(
                    value = state.annualLeave,
                    onValueChange = onAnnualLeave,
                    range = 0..60,
                    suffix = unitDays,
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(stringResource(R.string.label_trip_length), style = MaterialTheme.typography.labelLarge)
                Stepper(
                    value = state.tripDays,
                    onValueChange = onTripDays,
                    range = 1..30,
                    suffix = unitDays,
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
private fun SuggestionCard(suggestion: TripSuggestion, overlapName: String?, onAdd: () -> Unit) {
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
                stringResource(R.string.saves_days, suggestion.savedDays),
                variant = TripBadgeVariant.Success,
            )
        }

        Spacer(Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            Metric(value = "${suggestion.daysOff}", label = stringResource(R.string.metric_days_off))
            Metric(value = "${suggestion.leaveDaysNeeded}", label = stringResource(R.string.metric_leave_spent))
            Metric(value = String.format(Locale.ENGLISH, "%.1f×", suggestion.efficiency), label = stringResource(R.string.metric_efficiency))
        }

        if (suggestion.nearHolidays.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                suggestion.nearHolidays.take(3).forEach { name ->
                    TripBadge(name, variant = TripBadgeVariant.Default)
                }
            }
        }

        if (overlapName != null) {
            Spacer(Modifier.height(12.dp))
            TripBadge(
                stringResource(R.string.overlap_warning, overlapName),
                variant = TripBadgeVariant.Warning,
            )
        }

        Spacer(Modifier.height(12.dp))
        TripButton(onClick = onAdd, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.add_as_trip))
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
private fun PlannedTripRow(
    trip: PlannedTrip,
    onClick: () -> Unit,
    onRemove: () -> Unit,
    sharedTransitionScope: SharedTransitionScope? = null,
    animatedContentScope: AnimatedContentScope? = null,
) {
    TripCard(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .maybeSharedBounds(sharedTransitionScope, animatedContentScope, key = "trip-card-${trip.id}"),
    ) {
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
                Text(
                    trip.destination,
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.maybeSharedElement(
                        sharedTransitionScope, animatedContentScope, key = "trip-title-${trip.id}",
                    ),
                )
                Text(
                    "${trip.startDate.format(dateFmt)} – ${trip.endDate.format(dateFmt)} · ${trip.leaveDaysNeeded} leave",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(onClick = onRemove) {
                Icon(Icons.Default.Close, contentDescription = stringResource(R.string.remove_trip))
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
                stringResource(R.string.empty_no_windows),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                stringResource(R.string.empty_try_different),
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
