package com.tripplanner.android.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tripplanner.android.ui.theme.TripPlannerTheme

/**
 * A compact -/+ stepper with a spring-animated value readout. The number rolls
 * up when incremented and down when decremented.
 */
@Composable
fun Stepper(
    value: Int,
    onValueChange: (Int) -> Unit,
    range: IntRange,
    modifier: Modifier = Modifier,
    suffix: String? = null,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier,
    ) {
        StepperButton(
            icon = Icons.Default.Remove,
            contentDescription = "Decrease",
            enabled = value > range.first,
            onClick = { onValueChange((value - 1).coerceIn(range)) },
        )

        AnimatedContent(
            targetState = value,
            transitionSpec = {
                val up = targetState > initialState
                val enter = slideInVertically(spring()) { if (up) it else -it } + fadeIn()
                val exit = slideOutVertically(spring()) { if (up) -it else it } + fadeOut()
                enter togetherWith exit
            },
            label = "stepper_value",
            modifier = Modifier.widthIn(min = 56.dp).padding(horizontal = 8.dp),
        ) { v ->
            Text(
                text = if (suffix != null) "$v $suffix" else "$v",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
        }

        StepperButton(
            icon = Icons.Default.Add,
            contentDescription = "Increase",
            enabled = value < range.last,
            onClick = { onValueChange((value + 1).coerceIn(range)) },
        )
    }
}

@Composable
private fun StepperButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(40.dp)
            .border(
                width = 1.dp,
                color = if (enabled) MaterialTheme.colorScheme.outline
                        else MaterialTheme.colorScheme.outlineVariant,
                shape = CircleShape,
            ),
    ) {
        IconButton(onClick = onClick, enabled = enabled) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = if (enabled) MaterialTheme.colorScheme.onSurface
                       else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Preview(name = "Stepper · Light", showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun StepperLightPreview() {
    TripPlannerTheme(darkTheme = false) {
        var value by remember { mutableIntStateOf(5) }
        Stepper(
            value = value,
            onValueChange = { value = it },
            range = 1..30,
            suffix = "days",
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(name = "Stepper · Dark", showBackground = true, backgroundColor = 0xFF1C1C1C)
@Composable
private fun StepperDarkPreview() {
    TripPlannerTheme(darkTheme = true) {
        var value by remember { mutableIntStateOf(14) }
        Stepper(
            value = value,
            onValueChange = { value = it },
            range = 0..60,
            modifier = Modifier.padding(16.dp),
        )
    }
}
