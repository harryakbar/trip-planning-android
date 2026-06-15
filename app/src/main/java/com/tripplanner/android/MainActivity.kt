package com.tripplanner.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
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
import com.tripplanner.android.feature.calendar.YearCalendarScreen
import com.tripplanner.android.feature.optimizer.OptimizerHomeScreen
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

private enum class Screen { Optimizer, Calendar, Catalog }

@Composable
fun AppRoot() {
    var screen by remember { mutableStateOf(Screen.Optimizer) }

    AnimatedContent(
        targetState = screen,
        transitionSpec = {
            // Optimizer is the root; other screens slide in from the right.
            val forward = targetState != Screen.Optimizer
            val enter = fadeIn(tween(300)) + slideInHorizontally(tween(300)) {
                if (forward) it else -it
            }
            val exit = fadeOut(tween(300)) + slideOutHorizontally(tween(300)) {
                if (forward) -it else it
            }
            enter togetherWith exit
        },
        label = "screen_transition",
    ) { target ->
        when (target) {
            Screen.Optimizer -> OptimizerHomeScreen(
                onOpenCatalog = { screen = Screen.Catalog },
                onOpenCalendar = { screen = Screen.Calendar },
            )
            Screen.Calendar -> YearCalendarScreen(onBack = { screen = Screen.Optimizer })
            Screen.Catalog -> ThemeCatalogScreen(onBack = { screen = Screen.Optimizer })
        }
    }
}
