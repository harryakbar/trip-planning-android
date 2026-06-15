package com.tripplanner.android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tripplanner.android.ui.theme.Chart1
import com.tripplanner.android.ui.theme.Chart2
import com.tripplanner.android.ui.theme.LightDestructive
import com.tripplanner.android.ui.theme.TripPlannerTheme

enum class TripBadgeVariant { Default, Success, Destructive, Warning }

@Composable
fun TripBadge(
    label: String,
    modifier: Modifier = Modifier,
    variant: TripBadgeVariant = TripBadgeVariant.Default,
) {
    val (bg, fg) = when (variant) {
        TripBadgeVariant.Default -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
        TripBadgeVariant.Success -> Chart2 to Color.White
        TripBadgeVariant.Destructive -> MaterialTheme.colorScheme.error to MaterialTheme.colorScheme.onError
        TripBadgeVariant.Warning -> Chart1 to Color.White
    }
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .background(color = bg, shape = MaterialTheme.shapes.extraSmall)
            .padding(horizontal = 8.dp, vertical = 2.dp),
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = fg)
    }
}

@Composable
fun TripDotBadge(
    modifier: Modifier = Modifier,
    color: Color = LightDestructive,
) {
    Box(
        modifier = modifier
            .size(8.dp)
            .background(color = color, shape = CircleShape),
    )
}

@Preview(name = "Badge · Light", showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun BadgeLightPreview() {
    TripPlannerTheme(darkTheme = false) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(16.dp),
        ) {
            TripBadge("Default")
            TripBadge("Saved", variant = TripBadgeVariant.Success)
            TripBadge("Overdue", variant = TripBadgeVariant.Destructive)
            TripBadge("Pending", variant = TripBadgeVariant.Warning)
        }
    }
}

@Preview(name = "Badge · Dark", showBackground = true, backgroundColor = 0xFF1C1C1C)
@Composable
private fun BadgeDarkPreview() {
    TripPlannerTheme(darkTheme = true) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(16.dp),
        ) {
            TripBadge("Default")
            TripBadge("Saved", variant = TripBadgeVariant.Success)
            TripBadge("Overdue", variant = TripBadgeVariant.Destructive)
        }
    }
}
