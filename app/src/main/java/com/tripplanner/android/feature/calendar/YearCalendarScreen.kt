package com.tripplanner.android.feature.calendar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tripplanner.android.R
import com.tripplanner.android.core.holidays.CountryCode
import com.tripplanner.android.core.holidays.ResolvedHoliday
import com.tripplanner.android.feature.optimizer.OptimizerUiState
import com.tripplanner.android.feature.optimizer.OptimizerViewModel
import com.tripplanner.android.feature.optimizer.PlannedTrip
import com.tripplanner.android.feature.optimizer.TripSuggestion
import com.tripplanner.android.feature.trips.DestinationDialog
import com.tripplanner.android.ui.components.TripButton
import com.tripplanner.android.ui.components.TripTextButton
import com.tripplanner.android.ui.theme.Chart1
import com.tripplanner.android.ui.theme.LocalTripColors
import com.tripplanner.android.ui.theme.TripPlannerTheme
import java.time.LocalDate
import java.time.Month
import java.time.format.TextStyle
import java.util.Locale

private fun localeFor(country: CountryCode): Locale =
    if (country == CountryCode.ID) Locale("in", "ID") else Locale.ENGLISH

/** Round-robin trip colours drawn from the chart palette. */
@Composable
private fun tripColor(index: Int): Color {
    val c = LocalTripColors.current
    val palette = listOf(c.chart1, c.chart2, c.chart3, c.chart4, c.chart5)
    return palette[index % palette.size]
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YearCalendarScreen(
    onBack: () -> Unit = {},
    viewModel: OptimizerViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var selStart by remember { mutableStateOf<LocalDate?>(null) }
    var selEnd by remember { mutableStateOf<LocalDate?>(null) }
    var showDestDialog by remember { mutableStateOf(false) }

    // Clear an out-of-year selection when the year changes.
    LaunchedEffect(state.year) { selStart = null; selEnd = null }

    val locale = localeFor(state.country)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.calendar_title, state.year), style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
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
        Box(modifier = Modifier.fillMaxSize()) {
            // Fast lookups for cell classification.
            val holidayByDate: Map<LocalDate, ResolvedHoliday> =
                remember(state.holidays) { state.holidays.associateBy { it.date } }
            val tripColorByDate: Map<LocalDate, Color> = buildTripColorMap(state.trips)
            val suggestionDates: Set<LocalDate> =
                remember(state.suggestions) { suggestionSpan(state.suggestions) }

            LazyColumn(
                contentPadding = padding,
                verticalArrangement = Arrangement.spacedBy(20.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
            ) {
                item { Legend(state) }

                items(12) { i ->
                    MonthGrid(
                        year = state.year,
                        month = i + 1,
                        locale = locale,
                        holidayByDate = holidayByDate,
                        tripColorByDate = tripColorByDate,
                        suggestionDates = suggestionDates,
                        selStart = selStart,
                        selEnd = selEnd,
                        onDayClick = { day ->
                            val s = selStart
                            if (s == null || selEnd != null) {
                                selStart = day; selEnd = null
                            } else if (day.isBefore(s)) {
                                selStart = day
                            } else {
                                selEnd = day
                            }
                        },
                    )
                }

                item { Spacer(Modifier.height(96.dp)) }
            }

            // Selection action bar.
            val start = selStart
            val end = selEnd
            AnimatedVisibility(
                visible = start != null && end != null,
                enter = fadeIn() + slideInVertically(spring()) { it },
                exit = fadeOut() + slideOutVertically(tween(150)) { it },
                modifier = Modifier.align(Alignment.BottomCenter),
            ) {
                if (start != null && end != null) {
                    SelectionBar(
                        state = state,
                        start = start,
                        end = end,
                        onClear = { selStart = null; selEnd = null },
                        onCreate = { showDestDialog = true },
                    )
                }
            }

            // Destination dialog shown after tapping "Create trip".
            if (showDestDialog && start != null && end != null) {
                DestinationDialog(
                    onConfirm = { destination ->
                        viewModel.addTripFromRange(start, end, destination = destination)
                        showDestDialog = false
                        selStart = null
                        selEnd = null
                    },
                    onDismiss = { showDestDialog = false },
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Legend(state: OptimizerUiState) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            .padding(12.dp),
    ) {
        Text(stringResource(R.string.legend), style = MaterialTheme.typography.titleSmall)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            LegendItem(MaterialTheme.colorScheme.error.copy(alpha = 0.18f), stringResource(R.string.legend_public_holiday))
            LegendItem(Chart1.copy(alpha = 0.22f), stringResource(R.string.legend_cuti_bersama))
            LegendItem(MaterialTheme.colorScheme.surfaceVariant, stringResource(R.string.legend_weekend))
            LegendItem(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f), stringResource(R.string.legend_suggested))
            state.trips.forEachIndexed { i, trip ->
                LegendItem(tripColor(i), trip.destination)
            }
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(color),
        )
        Text(label, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun MonthGrid(
    year: Int,
    month: Int,
    locale: Locale,
    holidayByDate: Map<LocalDate, ResolvedHoliday>,
    tripColorByDate: Map<LocalDate, Color>,
    suggestionDates: Set<LocalDate>,
    selStart: LocalDate?,
    selEnd: LocalDate?,
    onDayClick: (LocalDate) -> Unit,
) {
    val cells = remember(year, month) { CalendarGrid.monthCells(year, month) }
    val monthName = Month.of(month).getDisplayName(TextStyle.FULL, locale)

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            "$monthName $year",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
        )

        // Sunday-first weekday headers.
        Row(modifier = Modifier.fillMaxWidth()) {
            val days = listOf(
                java.time.DayOfWeek.SUNDAY, java.time.DayOfWeek.MONDAY, java.time.DayOfWeek.TUESDAY,
                java.time.DayOfWeek.WEDNESDAY, java.time.DayOfWeek.THURSDAY, java.time.DayOfWeek.FRIDAY,
                java.time.DayOfWeek.SATURDAY,
            )
            days.forEach { dow ->
                Text(
                    dow.getDisplayName(TextStyle.NARROW, locale),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        // Weeks.
        cells.chunked(7).forEach { week ->
            Row(modifier = Modifier.fillMaxWidth()) {
                week.forEach { day ->
                    DayCell(
                        day = day,
                        inMonth = day.monthValue == month,
                        holiday = holidayByDate[day],
                        tripColor = tripColorByDate[day],
                        isSuggested = day in suggestionDates,
                        selStart = selStart,
                        selEnd = selEnd,
                        onClick = { onDayClick(day) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    day: LocalDate,
    inMonth: Boolean,
    holiday: ResolvedHoliday?,
    tripColor: Color?,
    isSuggested: Boolean,
    selStart: LocalDate?,
    selEnd: LocalDate?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isWeekend = day.dayOfWeek == java.time.DayOfWeek.SATURDAY || day.dayOfWeek == java.time.DayOfWeek.SUNDAY
    val inSelection = selStart != null &&
        (if (selEnd != null) !day.isBefore(selStart) && !day.isAfter(selEnd) else day == selStart)

    // Resolve background + foreground by priority.
    val scheme = MaterialTheme.colorScheme
    val (bg, fg) = when {
        inSelection -> scheme.primary to scheme.onPrimary
        tripColor != null -> tripColor to Color.White
        holiday?.isCutiBersama == true -> Chart1.copy(alpha = 0.22f) to scheme.onSurface
        holiday != null -> scheme.error.copy(alpha = 0.18f) to scheme.error
        isSuggested && inMonth -> scheme.primary.copy(alpha = 0.14f) to scheme.onSurface
        isWeekend && inMonth -> scheme.surfaceVariant to scheme.onSurfaceVariant
        else -> Color.Transparent to scheme.onSurface
    }

    // Spring "pop" when a cell becomes part of the selection.
    val scale by animateFloatAsState(
        targetValue = if (inSelection) 1f else 0.92f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "cell_scale",
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .graphicsLayer {
                if (inSelection) {
                    scaleX = scale; scaleY = scale
                }
            }
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .clickable { onClick() },
    ) {
        Text(
            text = day.dayOfMonth.toString(),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (holiday != null || tripColor != null || inSelection) FontWeight.SemiBold else FontWeight.Normal,
            color = if (!inMonth) fg.copy(alpha = 0.3f) else fg,
        )
        // Holiday dot.
        if (holiday != null && tripColor == null && !inSelection) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 3.dp)
                    .size(3.dp)
                    .clip(CircleShape)
                    .background(if (holiday.isCutiBersama) Chart1 else MaterialTheme.colorScheme.error),
            )
        }
    }
}

@Composable
private fun SelectionBar(
    state: OptimizerUiState,
    start: LocalDate,
    end: LocalDate,
    onClear: () -> Unit,
    onCreate: () -> Unit,
) {
    val tripDays = (java.time.temporal.ChronoUnit.DAYS.between(start, end) + 1).toInt()
    val leave = remember(start, end, state.holidays, state.year) {
        val prefix = com.tripplanner.android.core.optimizer.LeaveOptimization
            .buildWorkingDayPrefix(state.year, state.holidays)
        com.tripplanner.android.core.optimizer.LeaveOptimization
            .calculateWorkingDaysNeeded(start, tripDays, prefix)
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.surface)
            .padding(12.dp),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                stringResource(R.string.calendar_selection_summary, tripDays, leave),
                style = MaterialTheme.typography.titleSmall,
            )
            Text(
                stringResource(R.string.calendar_tap_second_day),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        TripTextButton(onClick = onClear) { Text(stringResource(R.string.action_clear)) }
        Spacer(Modifier.size(8.dp))
        TripButton(onClick = onCreate) { Text(stringResource(R.string.action_create_trip)) }
    }
}

@Composable
private fun buildTripColorMap(trips: List<PlannedTrip>): Map<LocalDate, Color> {
    val map = HashMap<LocalDate, Color>()
    trips.forEachIndexed { i, trip ->
        val color = tripColor(i)
        var d = trip.startDate
        while (!d.isAfter(trip.endDate)) {
            map[d] = color
            d = d.plusDays(1)
        }
    }
    return map
}

private fun suggestionSpan(suggestions: List<TripSuggestion>): Set<LocalDate> {
    val set = HashSet<LocalDate>()
    suggestions.forEach { s ->
        var d = s.startDate
        while (!d.isAfter(s.endDate)) {
            set.add(d)
            d = d.plusDays(1)
        }
    }
    return set
}

@androidx.compose.ui.tooling.preview.Preview(name = "Calendar · Light", showBackground = true)
@Composable
private fun CalendarLightPreview() {
    TripPlannerTheme(darkTheme = false) { YearCalendarScreen() }
}

@androidx.compose.ui.tooling.preview.Preview(name = "Calendar · Dark", showBackground = true, backgroundColor = 0xFF1C1C1C)
@Composable
private fun CalendarDarkPreview() {
    TripPlannerTheme(darkTheme = true) { YearCalendarScreen() }
}
