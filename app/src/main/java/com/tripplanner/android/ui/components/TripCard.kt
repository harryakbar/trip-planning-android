package com.tripplanner.android.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tripplanner.android.ui.theme.TripPlannerTheme

@Composable
fun TripCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
    )
    val border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)

    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = modifier,
            shape = MaterialTheme.shapes.large,
            colors = colors,
            border = border,
        ) {
            Column(modifier = Modifier.padding(16.dp), content = content)
        }
    } else {
        Card(
            modifier = modifier,
            shape = MaterialTheme.shapes.large,
            colors = colors,
            border = border,
        ) {
            Column(modifier = Modifier.padding(16.dp), content = content)
        }
    }
}

@Preview(name = "Card · Light", showBackground = true, backgroundColor = 0xFFECECF0)
@Composable
private fun CardLightPreview() {
    TripPlannerTheme(darkTheme = false) {
        TripCard(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text("Card title", style = MaterialTheme.typography.titleMedium)
            Text("Supporting text", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Preview(name = "Card · Dark", showBackground = true, backgroundColor = 0xFF1C1C1C)
@Composable
private fun CardDarkPreview() {
    TripPlannerTheme(darkTheme = true) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(16.dp),
        ) {
            TripCard(modifier = Modifier.fillMaxWidth()) {
                Text("Card title", style = MaterialTheme.typography.titleMedium)
                Text("Supporting text", style = MaterialTheme.typography.bodyMedium)
            }
            TripCard(onClick = {}, modifier = Modifier.fillMaxWidth()) {
                Text("Clickable card", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}
