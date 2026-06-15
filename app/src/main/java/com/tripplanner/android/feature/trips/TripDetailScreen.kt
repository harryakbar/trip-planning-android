@file:OptIn(ExperimentalSharedTransitionApi::class)

package com.tripplanner.android.feature.trips

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EventNote
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tripplanner.android.feature.optimizer.PlannedTrip
import com.tripplanner.android.ui.components.TripButton
import com.tripplanner.android.ui.components.TripCard
import com.tripplanner.android.ui.components.TripDestructiveButton
import com.tripplanner.android.ui.components.TripTextField
import java.time.format.DateTimeFormatter
import java.util.Locale

private val detailDateFmt = DateTimeFormatter.ofPattern("EEE, MMM d", Locale.ENGLISH)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripDetailScreen(
    trip: PlannedTrip,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    onBack: () -> Unit,
    onDelete: () -> Unit,
    onSaveNotes: (String) -> Unit = {},
    onGenerateItinerary: () -> Unit = {},
) {
    var notes by remember(trip.id) { mutableStateOf(trip.notes) }

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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Spacer(Modifier.height(4.dp))

            with(sharedTransitionScope) {
                TripCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .sharedBounds(
                            sharedContentState = rememberSharedContentState(key = "trip-card-${trip.id}"),
                            animatedVisibilityScope = animatedContentScope,
                            resizeMode = SharedTransitionScope.ResizeMode.RemeasureToBounds,
                        ),
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            DetailMetric(
                                label = "Start",
                                value = trip.startDate.format(detailDateFmt),
                            )
                            DetailMetric(
                                label = "End",
                                value = trip.endDate.format(detailDateFmt),
                                alignment = Alignment.End,
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                            DetailMetric(label = "Total days", value = "${trip.tripDays}")
                            DetailMetric(label = "Leave days", value = "${trip.leaveDaysNeeded}")
                            DetailMetric(
                                label = "Efficiency",
                                value = String.format(
                                    Locale.ENGLISH,
                                    "%.1f×",
                                    trip.tripDays.toDouble() / trip.leaveDaysNeeded.coerceAtLeast(1),
                                ),
                            )
                        }
                    }
                }
            }

            TripCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Icon(
                            Icons.Default.EventNote,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text("Notes", style = MaterialTheme.typography.titleSmall)
                    }
                    TripTextField(
                        value = notes,
                        onValueChange = { notes = it; onSaveNotes(it) },
                        placeholder = "Add notes, packing list, ideas…",
                        singleLine = false,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            TripCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Itinerary", style = MaterialTheme.typography.titleSmall)
                    Text(
                        "Preview a day-by-day plan with activities and a budget breakdown.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    TripButton(
                        onClick = onGenerateItinerary,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("View itinerary")
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            TripDestructiveButton(
                onClick = { onDelete(); onBack() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
            ) {
                Text("Delete trip")
            }
        }
    }
}

@Composable
private fun DetailMetric(
    label: String,
    value: String,
    alignment: Alignment.Horizontal = Alignment.Start,
) {
    Column(horizontalAlignment = alignment) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
    }
}
