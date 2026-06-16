package com.tripplanner.android.feature.itinerary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import com.tripplanner.android.R
import com.tripplanner.android.feature.optimizer.PlannedTrip
import java.time.format.DateTimeFormatter
import java.util.Locale

private val mapTimeFmt = DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH)

// Distinct marker hues per day (red, blue, green, violet, amber), reused round-robin.
private val dayHues = listOf(0f, 210f, 130f, 280f, 40f)

private fun dayHue(dayNumber: Int): Float = dayHues[(dayNumber - 1).mod(dayHues.size)]
private fun dayColor(dayNumber: Int): Color = Color.hsv(dayHue(dayNumber), 0.65f, 0.85f)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripMapScreen(
    trip: PlannedTrip,
    currency: Currency,
    onBack: () -> Unit,
) {
    val itinerary = remember(trip.id, currency) { SampleItinerary.forTrip(trip, currency) }
    var selectedDay by remember { mutableStateOf<Int?>(null) }
    var mapLoaded by remember { mutableStateOf(false) }

    val visibleDays = remember(itinerary, selectedDay) {
        if (selectedDay == null) itinerary.days else itinerary.days.filter { it.dayNumber == selectedDay }
    }
    val visiblePoints = remember(visibleDays) {
        visibleDays.flatMap { d -> d.activities.map { LatLng(it.location.lat, it.location.lng) } }
    }

    val cameraPositionState = rememberCameraPositionState {
        val first = itinerary.days.firstOrNull()?.activities?.firstOrNull()?.location
        if (first != null) {
            position = CameraPosition.fromLatLngZoom(LatLng(first.lat, first.lng), 12f)
        }
    }

    // Fit the camera to the visible activities once the map is ready / the filter changes.
    LaunchedEffect(mapLoaded, selectedDay) {
        if (mapLoaded && visiblePoints.isNotEmpty()) {
            val boundsBuilder = LatLngBounds.builder()
            visiblePoints.forEach { boundsBuilder.include(it) }
            runCatching {
                cameraPositionState.animate(
                    CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 120),
                )
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.map_title), style = MaterialTheme.typography.titleLarge) },
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
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            DayFilterRow(
                dayNumbers = itinerary.days.map { it.dayNumber },
                selectedDay = selectedDay,
                onSelect = { selectedDay = it },
            )

            if (visiblePoints.isEmpty()) {
                MapEmptyState(Modifier.fillMaxSize())
            } else {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    onMapLoaded = { mapLoaded = true },
                ) {
                    visibleDays.forEach { day ->
                        val points = day.activities.map { LatLng(it.location.lat, it.location.lng) }
                        if (points.size >= 2) {
                            Polyline(points = points, color = dayColor(day.dayNumber), width = 8f)
                        }
                        day.activities.forEach { activity ->
                            Marker(
                                state = rememberMarkerState(
                                    key = activity.id,
                                    position = LatLng(activity.location.lat, activity.location.lng),
                                ),
                                title = activity.name,
                                snippet = "${activity.startTime.format(mapTimeFmt)} · ${currency.format(activity.estimatedCost.amount)}",
                                icon = BitmapDescriptorFactory.defaultMarker(dayHue(day.dayNumber)),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DayFilterRow(
    dayNumbers: List<Int>,
    selectedDay: Int?,
    onSelect: (Int?) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        FilterChip(
            selected = selectedDay == null,
            onClick = { onSelect(null) },
            label = { Text(stringResource(R.string.map_all_days)) },
        )
        dayNumbers.forEach { day ->
            FilterChip(
                selected = selectedDay == day,
                onClick = { onSelect(day) },
                label = { Text(stringResource(R.string.day_label, day)) },
            )
        }
    }
}

@Composable
private fun MapEmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            Icons.Default.Place,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(40.dp),
        )
        Spacer(Modifier.height(12.dp))
        Text(
            stringResource(R.string.map_no_locations),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
