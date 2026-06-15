@file:OptIn(ExperimentalSharedTransitionApi::class)

package com.tripplanner.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tripplanner.android.feature.calendar.YearCalendarScreen
import com.tripplanner.android.feature.optimizer.OptimizerHomeScreen
import com.tripplanner.android.feature.optimizer.OptimizerViewModel
import com.tripplanner.android.feature.optimizer.PlannedTrip
import com.tripplanner.android.feature.timeline.TimelineScreen
import com.tripplanner.android.feature.trips.TripDetailScreen
import com.tripplanner.android.ui.screen.ThemeCatalogScreen
import com.tripplanner.android.ui.theme.TripPlannerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TripPlannerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    AppRoot()
                }
            }
        }
    }
}

private sealed class Screen {
    data object Optimizer : Screen()
    data object Calendar : Screen()
    data object Timeline : Screen()
    data object Catalog : Screen()
    data class TripDetail(val trip: PlannedTrip) : Screen()
}

@Composable
fun AppRoot() {
    var screen by remember { mutableStateOf<Screen>(Screen.Optimizer) }
    val sharedViewModel: OptimizerViewModel = viewModel()

    SharedTransitionLayout {
        AnimatedContent(
            targetState = screen,
            transitionSpec = {
                val isForward = targetState != Screen.Optimizer
                val enter = fadeIn(tween(300)) + slideInHorizontally(tween(300)) {
                    if (isForward) it else -it
                }
                val exit = fadeOut(tween(300)) + slideOutHorizontally(tween(300)) {
                    if (isForward) -it else it
                }
                enter togetherWith exit
            },
            label = "screen_transition",
        ) { target ->
            when (target) {
                Screen.Optimizer -> OptimizerHomeScreen(
                    viewModel = sharedViewModel,
                    sharedTransitionScope = this@SharedTransitionLayout,
                    animatedContentScope = this@AnimatedContent,
                    onOpenCatalog = { screen = Screen.Catalog },
                    onOpenCalendar = { screen = Screen.Calendar },
                    onOpenTimeline = { screen = Screen.Timeline },
                    onTripClick = { trip -> screen = Screen.TripDetail(trip) },
                )
                Screen.Calendar -> YearCalendarScreen(
                    viewModel = sharedViewModel,
                    onBack = { screen = Screen.Optimizer },
                )
                Screen.Timeline -> TimelineScreen(
                    viewModel = sharedViewModel,
                    onBack = { screen = Screen.Optimizer },
                    onTripClick = { trip -> screen = Screen.TripDetail(trip) },
                )
                Screen.Catalog -> ThemeCatalogScreen(onBack = { screen = Screen.Optimizer })
                is Screen.TripDetail -> TripDetailScreen(
                    trip = target.trip,
                    sharedTransitionScope = this@SharedTransitionLayout,
                    animatedContentScope = this@AnimatedContent,
                    onBack = { screen = Screen.Optimizer },
                    onDelete = { sharedViewModel.removeTrip(target.trip.id) },
                    onSaveNotes = { notes -> sharedViewModel.updateTripNotes(target.trip.id, notes) },
                )
            }
        }
    }
}
