package com.tripplanner.android.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tripplanner.android.ui.components.SegmentedControl
import com.tripplanner.android.ui.components.TripBadge
import com.tripplanner.android.ui.components.TripBadgeVariant
import com.tripplanner.android.ui.components.TripBottomSheet
import com.tripplanner.android.ui.components.TripButton
import com.tripplanner.android.ui.components.TripCard
import com.tripplanner.android.ui.components.TripCategory
import com.tripplanner.android.ui.components.TripDestructiveButton
import com.tripplanner.android.ui.components.TripDotBadge
import com.tripplanner.android.ui.components.TripFilterChip
import com.tripplanner.android.ui.components.TripOutlinedButton
import com.tripplanner.android.ui.components.TripSuggestionChip
import com.tripplanner.android.ui.components.TripTextField
import com.tripplanner.android.ui.components.TripTextButton
import com.tripplanner.android.ui.theme.LocalTripColors
import com.tripplanner.android.ui.theme.TripPlannerTheme

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ThemeCatalogScreen(onBack: () -> Unit = {}) {
    var countryIndex by remember { mutableIntStateOf(0) }
    var yearIndex by remember { mutableIntStateOf(1) }
    var searchText by remember { mutableStateOf("") }
    var chipSelected by remember { mutableStateOf(setOf("Singapore")) }
    var showSheet by remember { mutableStateOf(false) }
    val chartColors = LocalTripColors.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Design Catalog") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
        LazyColumn(
            contentPadding = padding,
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
        ) {
            item { Spacer(Modifier.height(8.dp)) }

            // ── Typography ────────────────────────────────────────────────────
            item {
                SectionHeader("Typography")
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Headline Large", style = MaterialTheme.typography.headlineLarge)
                    Text("Headline Medium", style = MaterialTheme.typography.headlineMedium)
                    Text("Headline Small", style = MaterialTheme.typography.headlineSmall)
                    Text("Title Large", style = MaterialTheme.typography.titleLarge)
                    Text("Title Medium", style = MaterialTheme.typography.titleMedium)
                    Text("Title Small", style = MaterialTheme.typography.titleSmall)
                    Text("Body Large", style = MaterialTheme.typography.bodyLarge)
                    Text("Body Medium", style = MaterialTheme.typography.bodyMedium)
                    Text("Body Small", style = MaterialTheme.typography.bodySmall)
                    Text("Label Large", style = MaterialTheme.typography.labelLarge)
                    Text("Label Medium", style = MaterialTheme.typography.labelMedium)
                    Text("Label Small", style = MaterialTheme.typography.labelSmall)
                }
            }

            // ── Buttons ───────────────────────────────────────────────────────
            item {
                SectionHeader("Buttons")
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    TripButton(onClick = {}, modifier = Modifier.fillMaxWidth()) { Text("Primary") }
                    TripOutlinedButton(onClick = {}, modifier = Modifier.fillMaxWidth()) { Text("Secondary (Outlined)") }
                    TripTextButton(onClick = {}, modifier = Modifier.fillMaxWidth()) { Text("Ghost (Text)") }
                    TripDestructiveButton(onClick = {}, modifier = Modifier.fillMaxWidth()) { Text("Destructive") }
                    TripButton(onClick = {}, modifier = Modifier.fillMaxWidth(), enabled = false) { Text("Disabled") }
                    TripButton(onClick = { showSheet = true }, modifier = Modifier.fillMaxWidth()) {
                        Text("Show Bottom Sheet")
                    }
                }
            }

            // ── Segmented controls ────────────────────────────────────────────
            item {
                SectionHeader("Segmented Control")
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SegmentedControl(
                        options = listOf("Singapore", "Indonesia"),
                        selectedIndex = countryIndex,
                        onSelect = { countryIndex = it },
                    )
                    SegmentedControl(
                        options = listOf("2025", "2026", "2027"),
                        selectedIndex = yearIndex,
                        onSelect = { yearIndex = it },
                    )
                }
            }

            // ── Text field ────────────────────────────────────────────────────
            item {
                SectionHeader("Text Field")
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    TripTextField(
                        value = searchText,
                        onValueChange = { searchText = it },
                        placeholder = "Search destinations…",
                    )
                    TripTextField(
                        value = "Tokyo",
                        onValueChange = {},
                        label = "Destination",
                    )
                    TripTextField(
                        value = "",
                        onValueChange = {},
                        label = "Required field",
                        isError = true,
                        supportingText = "This field is required",
                    )
                }
            }

            // ── Chips ─────────────────────────────────────────────────────────
            item {
                SectionHeader("Chips")
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    listOf("Singapore", "Japan", "Thailand").forEach { c ->
                        TripFilterChip(
                            selected = c in chipSelected,
                            onSelectedChange = { on ->
                                chipSelected = if (on) chipSelected + c else chipSelected - c
                            },
                            label = c,
                        )
                    }
                    TripSuggestionChip(label = "Suggestion", onClick = {})
                }
            }

            // ── Badges ────────────────────────────────────────────────────────
            item {
                SectionHeader("Badges")
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TripBadge("Default")
                        TripBadge("Saved", variant = TripBadgeVariant.Success)
                        TripBadge("Overdue", variant = TripBadgeVariant.Destructive)
                        TripBadge("Pending", variant = TripBadgeVariant.Warning)
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        TripDotBadge()
                        Text("Dot badge", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            // ── Cards ─────────────────────────────────────────────────────────
            item {
                SectionHeader("Cards")
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    TripCard(modifier = Modifier.fillMaxWidth()) {
                        Text("Static card", style = MaterialTheme.typography.titleSmall)
                        Text("With supporting text", style = MaterialTheme.typography.bodyMedium)
                    }
                    TripCard(onClick = {}, modifier = Modifier.fillMaxWidth()) {
                        Text("Clickable card", style = MaterialTheme.typography.titleSmall)
                        Text("Tap for action", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            // ── Category styles ───────────────────────────────────────────────
            item {
                SectionHeader("Category Colors")
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    TripCategory.entries.forEach { cat ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(cat.color),
                            ) {
                                Icon(
                                    imageVector = cat.icon,
                                    contentDescription = cat.label,
                                    tint = cat.onColor,
                                    modifier = Modifier.size(24.dp),
                                )
                            }
                            Text(cat.label, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }

            // ── Chart palette ─────────────────────────────────────────────────
            item {
                SectionHeader("Chart Palette")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        chartColors.chart1, chartColors.chart2, chartColors.chart3,
                        chartColors.chart4, chartColors.chart5,
                    ).forEachIndexed { i, color ->
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .clip(MaterialTheme.shapes.small)
                                .background(color),
                        ) {
                            Text(
                                "${i + 1}",
                                style = MaterialTheme.typography.labelMedium,
                                color = androidx.compose.ui.graphics.Color.White,
                            )
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(32.dp)) }
        }
    }

    if (showSheet) {
        TripBottomSheet(onDismiss = { showSheet = false }) {
            Text(
                "Bottom Sheet",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
            )
            Text(
                "This is a reusable modal bottom sheet component.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 20.dp),
            )
            Spacer(Modifier.height(16.dp))
            TripButton(
                onClick = { showSheet = false },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 16.dp),
            ) { Text("Close") }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Column {
        Text(
            title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
    }
}

@Preview(name = "ThemeCatalog · Light", showBackground = true)
@Composable
private fun CatalogLightPreview() {
    TripPlannerTheme(darkTheme = false) {
        ThemeCatalogScreen()
    }
}

@Preview(name = "ThemeCatalog · Dark", showBackground = true, backgroundColor = 0xFF1C1C1C)
@Composable
private fun CatalogDarkPreview() {
    TripPlannerTheme(darkTheme = true) {
        ThemeCatalogScreen()
    }
}
