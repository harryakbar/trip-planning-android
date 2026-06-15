package com.tripplanner.android.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tripplanner.android.ui.theme.TripPlannerTheme

@Composable
fun TripFilterChip(
    selected: Boolean,
    onSelectedChange: (Boolean) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    leadingIcon: @Composable (() -> Unit)? = null,
) {
    FilterChip(
        selected = selected,
        onClick = { onSelectedChange(!selected) },
        label = { Text(label, style = MaterialTheme.typography.labelLarge) },
        modifier = modifier,
        leadingIcon = leadingIcon,
        shape = MaterialTheme.shapes.extraLarge,
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primary,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
            selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary,
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            labelColor = MaterialTheme.colorScheme.onSurface,
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = MaterialTheme.colorScheme.outline,
            selectedBorderColor = MaterialTheme.colorScheme.primary,
        ),
    )
}

@Composable
fun TripSuggestionChip(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: @Composable (() -> Unit)? = null,
) {
    SuggestionChip(
        onClick = onClick,
        label = { Text(label, style = MaterialTheme.typography.labelLarge) },
        modifier = modifier,
        icon = leadingIcon,
        shape = MaterialTheme.shapes.extraLarge,
        colors = SuggestionChipDefaults.suggestionChipColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            labelColor = MaterialTheme.colorScheme.onSurface,
        ),
        border = SuggestionChipDefaults.suggestionChipBorder(
            enabled = true,
            borderColor = MaterialTheme.colorScheme.outline,
        ),
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Preview(name = "Chips · Light", showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun ChipsLightPreview() {
    TripPlannerTheme(darkTheme = false) {
        var selected by remember { mutableStateOf(setOf("Singapore")) }
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(16.dp),
        ) {
            listOf("Singapore", "Japan", "Thailand", "Vietnam").forEach { country ->
                TripFilterChip(
                    selected = country in selected,
                    onSelectedChange = { on ->
                        selected = if (on) selected + country else selected - country
                    },
                    label = country,
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Preview(name = "Chips · Dark", showBackground = true, backgroundColor = 0xFF1C1C1C)
@Composable
private fun ChipsDarkPreview() {
    TripPlannerTheme(darkTheme = true) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(16.dp),
        ) {
            TripFilterChip(selected = true, onSelectedChange = {}, label = "Selected")
            TripFilterChip(selected = false, onSelectedChange = {}, label = "Unselected")
            TripSuggestionChip(label = "Suggestion", onClick = {})
        }
    }
}
