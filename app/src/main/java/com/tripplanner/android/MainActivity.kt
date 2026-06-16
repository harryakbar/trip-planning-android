@file:OptIn(ExperimentalSharedTransitionApi::class)

package com.tripplanner.android

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tripplanner.android.core.holidays.CountryCode
import com.tripplanner.android.feature.calendar.YearCalendarScreen
import com.tripplanner.android.feature.itinerary.ActivityStoriesScreen
import com.tripplanner.android.feature.itinerary.Currency
import com.tripplanner.android.feature.itinerary.ItineraryViewerScreen
import com.tripplanner.android.feature.itinerary.TripMapScreen
import com.tripplanner.android.feature.optimizer.OptimizerHomeScreen
import com.tripplanner.android.feature.optimizer.OptimizerViewModel
import com.tripplanner.android.feature.optimizer.PlannedTrip
import com.tripplanner.android.feature.timeline.TimelineScreen
import com.tripplanner.android.feature.trips.TripDetailScreen
import com.tripplanner.android.ui.screen.ThemeCatalogScreen
import com.tripplanner.android.ui.theme.TripPlannerTheme

class MainActivity : ComponentActivity() {
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(com.tripplanner.android.core.i18n.LocaleManager.wrap(newBase))
    }

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
    data class Itinerary(val trip: PlannedTrip) : Screen()
    data class Stories(val trip: PlannedTrip, val startIndex: Int) : Screen()
    data class Map(val trip: PlannedTrip) : Screen()
}

@Composable
fun AppRoot() {
    var screen by remember { mutableStateOf<Screen>(Screen.Optimizer) }
    val sharedViewModel: OptimizerViewModel = viewModel()

    // Only `country` affects this composable. Reading the raw state here would recompose
    // the whole nav host (and recreate every navigation lambda) on every trip/suggestion
    // update, even mid-transition; deriving it isolates that churn.
    val optimizerState = sharedViewModel.state.collectAsStateWithLifecycle()
    val country by remember { derivedStateOf { optimizerState.value.country } }
    val currency = if (country == CountryCode.ID) Currency.IDR else Currency.SGD

    BackHandler(enabled = screen != Screen.Optimizer) {
        screen = navigateBack(screen)
    }

    SharedTransitionLayout {
        AnimatedContent(
            targetState = screen,
            transitionSpec = {
                if (initialState.inSharedElementChain() && targetState.inSharedElementChain()) {
                    // Both ends already animate via sharedBounds/sharedElement; a competing
                    // full-screen slide fights that bounds interpolation and looks stuttery.
                    fadeIn(tween(220)) togetherWith fadeOut(tween(160))
                } else {
                    val isForward = targetState != Screen.Optimizer
                    val enter = fadeIn(tween(300)) + slideInHorizontally(tween(300)) {
                        if (isForward) it else -it
                    }
                    val exit = fadeOut(tween(300)) + slideOutHorizontally(tween(300)) {
                        if (isForward) -it else it
                    }
                    enter togetherWith exit
                }
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
                    onBack = { screen = navigateBack(screen) },
                )
                Screen.Timeline -> TimelineScreen(
                    viewModel = sharedViewModel,
                    onBack = { screen = navigateBack(screen) },
                    onTripClick = { trip -> screen = Screen.TripDetail(trip) },
                )
                Screen.Catalog -> ThemeCatalogScreen(onBack = { screen = navigateBack(screen) })
                is Screen.TripDetail -> TripDetailScreen(
                    trip = target.trip,
                    sharedTransitionScope = this@SharedTransitionLayout,
                    animatedContentScope = this@AnimatedContent,
                    onBack = { screen = navigateBack(screen) },
                    onDelete = { sharedViewModel.removeTrip(target.trip.id) },
                    onSaveNotes = { notes -> sharedViewModel.updateTripNotes(target.trip.id, notes) },
                    onGenerateItinerary = { screen = Screen.Itinerary(target.trip) },
                )
                is Screen.Itinerary -> ItineraryViewerScreen(
                    trip = target.trip,
                    currency = currency,
                    sharedTransitionScope = this@SharedTransitionLayout,
                    animatedContentScope = this@AnimatedContent,
                    onBack = { screen = navigateBack(screen) },
                    onActivityClick = { startIndex -> screen = Screen.Stories(target.trip, startIndex) },
                    onOpenMap = { screen = Screen.Map(target.trip) },
                )
                is Screen.Map -> TripMapScreen(
                    trip = target.trip,
                    currency = currency,
                    onBack = { screen = navigateBack(screen) },
                )
                is Screen.Stories -> ActivityStoriesScreen(
                    trip = target.trip,
                    currency = currency,
                    startIndex = target.startIndex,
                    sharedTransitionScope = this@SharedTransitionLayout,
                    animatedContentScope = this@AnimatedContent,
                    onClose = { screen = navigateBack(screen) },
                )
            }
        }
    }
}

/** Mirrors each screen's own back action, so the system back gesture/button and the
 *  in-app back arrow always agree on where "back" goes. */
private fun navigateBack(current: Screen): Screen = when (current) {
    Screen.Optimizer -> Screen.Optimizer
    Screen.Calendar, Screen.Timeline, Screen.Catalog -> Screen.Optimizer
    is Screen.TripDetail -> Screen.Optimizer
    is Screen.Itinerary -> Screen.TripDetail(current.trip)
    is Screen.Map -> Screen.Itinerary(current.trip)
    is Screen.Stories -> Screen.Itinerary(current.trip)
}

/** Screens linked by a sharedBounds/sharedElement chain (trip card/title, activity card). */
private fun Screen.inSharedElementChain(): Boolean = when (this) {
    Screen.Optimizer, is Screen.TripDetail, is Screen.Itinerary, is Screen.Stories -> true
    Screen.Calendar, Screen.Timeline, Screen.Catalog, is Screen.Map -> false
}
