package com.tripplanner.android.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.tripplanner.android.ui.theme.TripPlannerTheme

@Composable
fun SegmentedControl(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    var containerSize by remember { mutableIntStateOf(0) }
    val density = LocalDensity.current

    val segmentWidth = if (containerSize > 0 && options.isNotEmpty()) {
        with(density) { (containerSize / options.size).toDp() }
    } else {
        0.dp
    }

    val thumbOffset by animateDpAsState(
        targetValue = segmentWidth * selectedIndex,
        animationSpec = spring(stiffness = 400f),
        label = "segmented_thumb",
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(40.dp)
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .onSizeChanged { containerSize = it.width },
    ) {
        // Sliding thumb
        if (segmentWidth > 0.dp) {
            Box(
                modifier = Modifier
                    .offset(x = thumbOffset)
                    .width(segmentWidth)
                    .fillMaxHeight()
                    .padding(3.dp)
                    .clip(RoundedCornerShape(7.dp))
                    .background(MaterialTheme.colorScheme.surface),
            )
        }

        // Labels
        Row(modifier = Modifier.fillMaxWidth()) {
            options.forEachIndexed { index, label ->
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable { onSelect(index) },
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelLarge,
                        color = if (index == selectedIndex)
                            MaterialTheme.colorScheme.onSurface
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

@Preview(name = "SegmentedControl · Light", showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun SegmentedControlLightPreview() {
    TripPlannerTheme(darkTheme = false) {
        var selected by remember { mutableIntStateOf(0) }
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(16.dp),
        ) {
            SegmentedControl(
                options = listOf("Singapore", "Indonesia"),
                selectedIndex = selected,
                onSelect = { selected = it },
            )
            SegmentedControl(
                options = listOf("2025", "2026", "2027"),
                selectedIndex = 1,
                onSelect = {},
            )
        }
    }
}

@Preview(name = "SegmentedControl · Dark", showBackground = true, backgroundColor = 0xFF1C1C1C)
@Composable
private fun SegmentedControlDarkPreview() {
    TripPlannerTheme(darkTheme = true) {
        SegmentedControl(
            options = listOf("Singapore", "Indonesia"),
            selectedIndex = 1,
            onSelect = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}
