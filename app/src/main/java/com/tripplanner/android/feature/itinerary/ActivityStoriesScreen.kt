@file:OptIn(ExperimentalSharedTransitionApi::class)

package com.tripplanner.android.feature.itinerary

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tripplanner.android.feature.optimizer.PlannedTrip
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter
import java.util.Locale

private const val AUTO_ADVANCE_MS = 5000f
private val storyTimeFmt = DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH)
private const val MAX_DESCRIPTION = 120

/** Flattened story entry: an activity plus its day context and global index. */
data class StoryEntry(val activity: ActivityItem, val dayNumber: Int, val flatIndex: Int)

fun flattenStories(itinerary: Itinerary): List<StoryEntry> {
    var idx = 0
    return itinerary.days.flatMap { day ->
        day.activities.map { activity -> StoryEntry(activity, day.dayNumber, idx++) }
    }
}

/** Global index of an activity within the flattened itinerary, or 0 if absent. */
fun storyIndexOf(itinerary: Itinerary, activityId: String): Int =
    flattenStories(itinerary).firstOrNull { it.activity.id == activityId }?.flatIndex ?: 0

@Composable
fun ActivityStoriesScreen(
    trip: PlannedTrip,
    currency: Currency,
    startIndex: Int,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    onClose: () -> Unit,
) {
    val itinerary = remember(trip.id, currency) { SampleItinerary.forTrip(trip, currency) }
    val entries = remember(itinerary) { flattenStories(itinerary) }

    if (entries.isEmpty()) {
        Box(Modifier.fillMaxSize().background(Color.Black))
        return
    }

    val pagerState = rememberPagerState(
        initialPage = startIndex.coerceIn(0, entries.lastIndex),
    ) { entries.size }
    var paused by remember { mutableStateOf(false) }
    var progress by remember { mutableFloatStateOf(0f) }
    val scope = rememberCoroutineScope()

    // Auto-advance: integrate progress per frame, honoring pause; advance at full.
    LaunchedEffect(pagerState.currentPage) {
        progress = 0f
        var last = withFrameMillis { it }
        while (progress < 1f) {
            val now = withFrameMillis { it }
            val dt = (now - last).toFloat()
            last = now
            if (!paused) progress = (progress + dt / AUTO_ADVANCE_MS).coerceAtMost(1f)
        }
        val next = pagerState.currentPage + 1
        if (next <= entries.lastIndex) pagerState.animateScrollToPage(next)
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
            val entry = entries[page]
            StoryCard(
                entry = entry,
                currency = currency,
                isShared = entry.flatIndex == startIndex,
                sharedTransitionScope = sharedTransitionScope,
                animatedContentScope = animatedContentScope,
                onPauseChange = { paused = it },
                onPrev = { scope.launch { if (pagerState.currentPage > 0) pagerState.animateScrollToPage(pagerState.currentPage - 1) } },
                onNext = { scope.launch { if (pagerState.currentPage < entries.lastIndex) pagerState.animateScrollToPage(pagerState.currentPage + 1) } },
            )
        }

        // Segmented progress for the current day.
        val current = entries[pagerState.currentPage]
        val dayEntries = remember(entries, current.dayNumber) {
            entries.filter { it.dayNumber == current.dayNumber }
        }
        val indexWithinDay = dayEntries.indexOfFirst { it.flatIndex == pagerState.currentPage }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(horizontal = 12.dp, vertical = 8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("Day ${current.dayNumber}", style = androidx.compose.material3.MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.8f))
                Text("${indexWithinDay + 1} / ${dayEntries.size}", style = androidx.compose.material3.MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.8f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
                dayEntries.forEachIndexed { i, _ ->
                    val fill = when {
                        i < indexWithinDay -> 1f
                        i == indexWithinDay -> progress
                        else -> 0f
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(3.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.3f)),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(fill)
                                .height(3.dp)
                                .clip(CircleShape)
                                .background(Color.White),
                        )
                    }
                }
            }
        }

        // Close button.
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .windowInsetsPadding(WindowInsets.systemBars)
                .padding(top = 28.dp, end = 16.dp)
                .size(36.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.4f))
                .pointerInput(Unit) { detectTapGestures { onClose() } },
        ) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White, modifier = Modifier.size(20.dp))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun StoryCard(
    entry: StoryEntry,
    currency: Currency,
    isShared: Boolean,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    onPauseChange: (Boolean) -> Unit,
    onPrev: () -> Unit,
    onNext: () -> Unit,
) {
    val activity = entry.activity
    val category = activity.category
    var expanded by remember(activity.id) { mutableStateOf(false) }
    val context = LocalContext.current

    val isLong = activity.description.length > MAX_DESCRIPTION
    val description = if (isLong && !expanded) {
        activity.description.take(MAX_DESCRIPTION) + "…"
    } else {
        activity.description
    }

    val gradient = Brush.verticalGradient(
        listOf(lerp(category.color, Color.Black, 0.15f), lerp(category.color, Color.Black, 0.7f)),
    )

    var cardModifier = Modifier
        .fillMaxSize()
        .background(gradient)
    if (isShared) {
        with(sharedTransitionScope) {
            cardModifier = cardModifier.sharedBounds(
                sharedContentState = rememberSharedContentState(key = "activity-${activity.id}"),
                animatedVisibilityScope = animatedContentScope,
                resizeMode = SharedTransitionScope.ResizeMode.RemeasureToBounds,
            )
        }
    }

    Box(
        modifier = cardModifier.pointerInput(activity.id) {
            detectTapGestures(
                onPress = {
                    onPauseChange(true)
                    tryAwaitRelease()
                    onPauseChange(false)
                },
                onTap = { offset -> if (offset.x < size.width * 0.3f) onPrev() else onNext() },
            )
        },
    ) {
        // Bottom scrim for legibility.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0.4f to Color.Transparent,
                        1f to Color.Black.copy(alpha = 0.75f),
                    ),
                ),
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .windowInsetsPadding(WindowInsets.systemBars)
                .padding(20.dp),
        ) {
            // Category badge.
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .clip(CircleShape)
                    .background(category.color.copy(alpha = 0.85f))
                    .padding(horizontal = 10.dp, vertical = 4.dp),
            ) {
                Icon(category.icon, contentDescription = null, tint = category.onColor, modifier = Modifier.size(14.dp))
                Text(category.label, style = androidx.compose.material3.MaterialTheme.typography.labelSmall, color = category.onColor)
            }

            Spacer(Modifier.height(12.dp))
            Text(
                activity.name,
                style = androidx.compose.material3.MaterialTheme.typography.headlineSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                description,
                style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.85f),
            )
            if (isLong && !expanded) {
                Text(
                    "Read more",
                    style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .padding(top = 2.dp)
                        .pointerInput(Unit) { detectTapGestures { expanded = true } },
                )
            }

            Spacer(Modifier.height(12.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                StoryMeta(Icons.Default.Schedule, "${activity.startTime.format(storyTimeFmt)} – ${activity.endTime.format(storyTimeFmt)}")
                StoryMeta(Icons.Default.Place, activity.location.address)
                StoryMeta(null, currency.format(activity.estimatedCost.amount))
            }

            Spacer(Modifier.height(16.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White.copy(alpha = 0.18f))
                    .pointerInput(activity.id) {
                        detectTapGestures {
                            val uri = Uri.parse("geo:${activity.location.lat},${activity.location.lng}?q=${Uri.encode(activity.name)}")
                            runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, uri)) }
                        }
                    }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
            ) {
                Icon(Icons.Default.Place, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                Text("View on Google Maps", style = androidx.compose.material3.MaterialTheme.typography.labelMedium, color = Color.White)
            }
        }
    }
}

@Composable
private fun StoryMeta(icon: androidx.compose.ui.graphics.vector.ImageVector?, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = Color.White.copy(alpha = 0.75f), modifier = Modifier.size(14.dp))
        }
        Text(text, style = androidx.compose.material3.MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.75f))
    }
}
