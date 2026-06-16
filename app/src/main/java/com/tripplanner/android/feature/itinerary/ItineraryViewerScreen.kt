@file:OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3Api::class)

package com.tripplanner.android.feature.itinerary

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Schedule
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tripplanner.android.R
import com.tripplanner.android.feature.optimizer.PlannedTrip
import com.tripplanner.android.ui.components.TripCard
import com.tripplanner.android.ui.theme.TripPlannerTheme
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter
import java.util.Locale

private val dayDateFmt = DateTimeFormatter.ofPattern("EEE, MMM d", Locale.ENGLISH)
private val timeFmt = DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH)

@Composable
fun ItineraryViewerScreen(
    trip: PlannedTrip,
    currency: Currency,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    onBack: () -> Unit,
    onActivityClick: (Int) -> Unit = {},
    onOpenMap: () -> Unit = {},
) {
    val itinerary = remember(trip.id, currency) { SampleItinerary.forTrip(trip, currency) }
    val pagerState = rememberPagerState(pageCount = { itinerary.days.size })
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    with(sharedTransitionScope) {
                        Text(
                            trip.destination,
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.sharedElement(
                                state = rememberSharedContentState(key = "trip-title-${trip.id}"),
                                animatedVisibilityScope = animatedContentScope,
                            ),
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                },
                actions = {
                    IconButton(onClick = onOpenMap) {
                        Icon(Icons.Default.Map, contentDescription = stringResource(R.string.action_open_map))
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
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Overview: total budget + breakdown.
            OverviewCard(itinerary)

            // Day tabs synced to the pager. Flat pills to match the app's design
            // language (no Material tab state-layer / indicator background).
            DayTabs(
                dayNumbers = itinerary.days.map { it.dayNumber },
                selectedPage = pagerState.currentPage,
                onSelect = { index -> scope.launch { pagerState.animateScrollToPage(index) } },
            )

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
            ) { page ->
                DayPage(
                    itinerary = itinerary,
                    day = itinerary.days[page],
                    currency = currency,
                    sharedTransitionScope = sharedTransitionScope,
                    animatedContentScope = animatedContentScope,
                    onActivityClick = onActivityClick,
                )
            }
        }
    }
}

@Composable
private fun OverviewCard(itinerary: Itinerary) {
    TripCard(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(stringResource(R.string.itinerary_estimated_budget), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    itinerary.currency.format(itinerary.totalBudget),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            Text(
                stringResource(R.string.days_count, itinerary.days.size),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.height(16.dp))
        BudgetByCategoryChart(
            breakdown = itinerary.budgetByCategory(),
            currency = itinerary.currency,
        )
    }
}

@Composable
private fun DayPage(
    itinerary: Itinerary,
    day: ItineraryDay,
    currency: Currency,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    onActivityClick: (Int) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(day.date.format(dayDateFmt), style = MaterialTheme.typography.titleMedium)
                Text(
                    currency.format(dayBudget(day)),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        itemsIndexed(day.activities, key = { _, a -> a.id }) { _, activity ->
            ActivityCard(
                activity = activity,
                currency = currency,
                sharedTransitionScope = sharedTransitionScope,
                animatedContentScope = animatedContentScope,
                onClick = { onActivityClick(storyIndexOf(itinerary, activity.id)) },
            )
        }
    }
}

/** Horizontally-scrollable flat day pills; the selected pill animates its tint. */
@Composable
private fun DayTabs(
    dayNumbers: List<Int>,
    selectedPage: Int,
    onSelect: (Int) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        dayNumbers.forEachIndexed { index, dayNumber ->
            val selected = index == selectedPage
            val bg by animateColorAsState(
                targetValue = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                animationSpec = tween(200),
                label = "day_pill_bg",
            )
            val fg by animateColorAsState(
                targetValue = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                animationSpec = tween(200),
                label = "day_pill_fg",
            )
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(bg)
                    .clickable { onSelect(index) }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                Text(
                    stringResource(R.string.day_label, dayNumber),
                    style = MaterialTheme.typography.labelLarge,
                    color = fg,
                )
            }
        }
    }
}

@Composable
private fun ActivityCard(
    activity: ActivityItem,
    currency: Currency,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    onClick: () -> Unit,
) {
    val cardModifier = with(sharedTransitionScope) {
        Modifier
            .fillMaxWidth()
            .sharedBounds(
                sharedContentState = rememberSharedContentState(key = "activity-${activity.id}"),
                animatedVisibilityScope = animatedContentScope,
                resizeMode = SharedTransitionScope.ResizeMode.RemeasureToBounds,
            )
    }
    TripCard(onClick = onClick, modifier = cardModifier) {
        Row(verticalAlignment = Alignment.Top) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(activity.category.color),
            ) {
                Icon(
                    activity.category.icon,
                    contentDescription = stringResource(activity.category.labelRes),
                    tint = activity.category.onColor,
                    modifier = Modifier.size(22.dp),
                )
            }
            Spacer(Modifier.size(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top,
                ) {
                    Text(activity.name, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                    Spacer(Modifier.size(8.dp))
                    Text(
                        currency.format(activity.estimatedCost.amount),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                Spacer(Modifier.height(4.dp))
                MetaRow(Icons.Default.Schedule, "${activity.startTime.format(timeFmt)} – ${activity.endTime.format(timeFmt)}")
                MetaRow(Icons.Default.Place, activity.location.address)
                Spacer(Modifier.height(6.dp))
                Text(
                    activity.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun MetaRow(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(14.dp),
        )
        Spacer(Modifier.size(6.dp))
        Text(text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

