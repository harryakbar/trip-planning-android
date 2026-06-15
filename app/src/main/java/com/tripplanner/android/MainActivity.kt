package com.tripplanner.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tripplanner.android.ui.components.TripButton
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

private enum class Screen { Home, Catalog }

@Composable
fun AppRoot() {
    var screen by remember { mutableStateOf(Screen.Home) }

    AnimatedContent(
        targetState = screen,
        transitionSpec = {
            val enter = fadeIn(tween(300)) + slideInHorizontally(tween(300)) {
                if (targetState == Screen.Catalog) it else -it
            }
            val exit = fadeOut(tween(300)) + slideOutHorizontally(tween(300)) {
                if (targetState == Screen.Catalog) -it else it
            }
            enter togetherWith exit
        },
        label = "screen_transition",
    ) { target ->
        when (target) {
            Screen.Home -> HomeScreen(onOpenCatalog = { screen = Screen.Catalog })
            Screen.Catalog -> ThemeCatalogScreen(onBack = { screen = Screen.Home })
        }
    }
}

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onOpenCatalog: () -> Unit = {},
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(600)) + slideInVertically(tween(600)) { it / 4 },
    ) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Trip Planner",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = "Plan smarter. Travel further.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(Modifier.height(32.dp))
            TripButton(onClick = onOpenCatalog) {
                Text("Design Catalog")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    TripPlannerTheme {
        HomeScreen()
    }
}
