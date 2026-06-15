package com.tripplanner.android.feature.timeline

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BeachAccess
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.Timeline
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tripplanner.android.feature.optimizer.OptimizerViewModel
import com.tripplanner.android.feature.optimizer.PlannedTrip
import com.tripplanner.android.ui.theme.LocalTripColors
import com.tripplanner.android.ui.theme.TripPlannerTheme
import java.time.LocalDate
import java.time.Month
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

private val timelineDateFmt: DateTimeFormatter = DateTimeFormatter.ofPattern("EEE, MMM d", Locale.ENGLISH)

@Composable
private fun tripPalette(): List<Color> {
    val c = LocalTripColors.current
    return listOf(c.chart1, c.chart2, c.chart3, c.chart4, c.chart5)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimelineScreen(
    onBack: () -> Unit = {},
    onTripClick: (PlannedTrip) -> Unit = {},
    viewModel: OptimizerViewModel = viewModel(),
    today: LocalDate = LocalDate.now(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val entries = remember(state.holidays, state.trips) {
        buildTimeline(state.holidays, state.trips)
    }
    // Where "today" sits within the chronological list (for the marker + scroll).
    val anchor = remember(entries, state.year, today) { todayAnchorIndex(entries, today) }
    val showToday = state.year == today.year

    val listState = rememberLazyListState()
    // Smoothly bring the current point in the year into view on first load.
    LaunchedEffect(state.year, entries.size) {
        if (showToday && entries.isNotEmpty()) {
            listState.animateScrollToItem(anchor.coerceIn(0, entries.lastIndex))
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("${state.year} Timeline", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
        if (state.isLoading) {
            TimelineLoading(Modifier.padding(padding))
            return@Scaffold
        }
        if (entries.isEmpty()) {
            TimelineEmpty(Modifier.padding(padding))
            return@Scaffold
        }

        val palette = tripPalette()
        LazyColumn(
            state = listState,
            contentPadding = padding,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
        ) {
            item { Spacer(Modifier.height(8.dp)) }

            itemsIndexed(entries, key = { _, e -> entryKey(e) }) { index, entry ->
                val prev = entries.getOrNull(index - 1)
                val showMonth = prev == null || prev.date.month != entry.date.month
                val isTodayBoundary = showToday && index == anchor

                Column {
                    if (showMonth) {
                        MonthHeader(entry.date.month)
                    }
                    if (isTodayBoundary) {
                        TodayMarker()
                    }
                    StaggeredTimelineRow(index = index) {
                        TimelineNode(
                            entry = entry,
                            palette = palette,
                            isFirst = index == 0,
                            isLast = index == entries.lastIndex,
                            onClick = { (entry as? TimelineEntry.Trip)?.let { onTripClick(it.trip) } },
                        )
                    }
                }
            }

            item { Spacer(Modifier.height(32.dp)) }
        }
    }
}

@Composable
private fun MonthHeader(month: Month) {
    Text(
        month.getDisplayName(TextStyle.FULL, Locale.ENGLISH),
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 44.dp, top = 12.dp, bottom = 4.dp),
    )
}

@Composable
private fun TodayMarker() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
    ) {
        Box(
            modifier = Modifier
                .width(36.dp)
                .height(2.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary),
        )
        Spacer(Modifier.width(8.dp))
        Text(
            "Today",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
        )
        Spacer(Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .weight(1f)
                .height(2.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
        )
    }
}

/**
 * A connector-and-dot node. The vertical connector line "draws in" on first
 * composition; the dot springs to full size, and trips are tappable.
 */
@Composable
private fun TimelineNode(
    entry: TimelineEntry,
    palette: List<Color>,
    isFirst: Boolean,
    isLast: Boolean,
    onClick: () -> Unit,
) {
    val accent = when (entry) {
        is TimelineEntry.Holiday ->
            if (entry.holiday.isCutiBersama) palette[0] else MaterialTheme.colorScheme.error
        is TimelineEntry.Trip -> palette[entry.colorIndex % palette.size]
    }
    val lineColor = MaterialTheme.colorScheme.outline

    // Connector draw-in.
    var drawn by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { drawn = true }
    val lineProgress by animateFloatAsState(
        targetValue = if (drawn) 1f else 0f,
        animationSpec = tween(400, easing = LinearOutSlowInEasing),
        label = "connector_draw",
    )
    // Dot spring-in.
    val dotScale by animateFloatAsState(
        targetValue = if (drawn) 1f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "dot_scale",
    )

    val tappable = entry is TimelineEntry.Trip
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (tappable) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(vertical = 6.dp),
    ) {
        // Gutter: connector line + node dot.
        Box(
            contentAlignment = Alignment.TopCenter,
            modifier = Modifier
                .width(28.dp)
                .drawBehind {
                    val cx = size.width / 2f
                    val top = if (isFirst) size.height * 0.5f else 0f
                    val bottom = if (isLast) size.height * 0.5f else size.height
                    drawLine(
                        color = lineColor,
                        start = Offset(cx, top),
                        end = Offset(cx, top + (bottom - top) * lineProgress),
                        strokeWidth = 3f,
                    )
                },
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .padding(top = 4.dp)
                    .size(20.dp)
                    .graphicsLayer { scaleX = dotScale; scaleY = dotScale }
                    .clip(CircleShape)
                    .background(accent),
            ) {
                Icon(
                    imageVector = if (entry is TimelineEntry.Trip) Icons.Default.BeachAccess else Icons.Default.EventBusy,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(12.dp),
                )
            }
        }

        Spacer(Modifier.width(12.dp))

        TimelineCard(entry = entry, accent = accent)
    }
}

@Composable
private fun TimelineCard(entry: TimelineEntry, accent: Color) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surface)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        when (entry) {
            is TimelineEntry.Holiday -> {
                Text(entry.holiday.name, style = MaterialTheme.typography.titleSmall)
                Text(
                    buildString {
                        append(entry.holiday.date.format(timelineDateFmt))
                        if (entry.holiday.isCutiBersama) append(" · Cuti Bersama")
                        else append(" · Public holiday")
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            is TimelineEntry.Trip -> {
                val trip = entry.trip
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(trip.destination, style = MaterialTheme.typography.titleSmall)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(accent.copy(alpha = 0.16f))
                            .padding(horizontal = 8.dp, vertical = 2.dp),
                    ) {
                        Text(
                            "${trip.leaveDaysNeeded} leave",
                            style = MaterialTheme.typography.labelSmall,
                            color = accent,
                        )
                    }
                }
                Text(
                    "${trip.startDate.format(timelineDateFmt)} – ${trip.endDate.format(timelineDateFmt)} · ${trip.tripDays} days",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

/** Fades + slides a row in from the right, delayed by its position. */
@Composable
private fun StaggeredTimelineRow(index: Int, content: @Composable () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay((index % 8) * 45L)
        visible = true
    }
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(280, easing = LinearOutSlowInEasing)) +
            slideInHorizontally(tween(280, easing = LinearOutSlowInEasing)) { it / 6 },
    ) {
        content()
    }
}

@Composable
private fun TimelineLoading(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        repeat(6) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
            )
        }
    }
}

@Composable
private fun TimelineEmpty(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            Icons.Default.Timeline,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(40.dp),
        )
        Spacer(Modifier.height(12.dp))
        Text(
            "Nothing on the timeline yet.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            "Add a trip to see it here alongside holidays.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private fun entryKey(entry: TimelineEntry): String = when (entry) {
    is TimelineEntry.Holiday -> "h-${entry.holiday.date}-${entry.holiday.name}"
    is TimelineEntry.Trip -> "t-${entry.trip.id}"
}

@androidx.compose.ui.tooling.preview.Preview(name = "Timeline · Light", showBackground = true)
@Composable
private fun TimelineLightPreview() {
    TripPlannerTheme(darkTheme = false) { TimelineScreen() }
}

@androidx.compose.ui.tooling.preview.Preview(name = "Timeline · Dark", showBackground = true, backgroundColor = 0xFF1C1C1C)
@Composable
private fun TimelineDarkPreview() {
    TripPlannerTheme(darkTheme = true) { TimelineScreen() }
}
