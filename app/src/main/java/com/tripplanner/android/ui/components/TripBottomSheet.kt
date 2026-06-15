package com.tripplanner.android.ui.components

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tripplanner.android.ui.theme.TripPlannerTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripBottomSheet(
    onDismiss: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    content: @Composable ColumnScope.() -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(
            topStart = 16.dp,
            topEnd = 16.dp,
        ),
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        windowInsets = WindowInsets(0),
    ) {
        content()
        androidx.compose.foundation.layout.Spacer(
            modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(name = "BottomSheet · Light", showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun BottomSheetLightPreview() {
    TripPlannerTheme(darkTheme = false) {
        val scope = rememberCoroutineScope()
        val state = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        TripBottomSheet(onDismiss = {}, sheetState = state) {
            Text(
                "Filter options",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
            )
            TripButton(
                onClick = { scope.launch { state.hide() } },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 16.dp),
            ) { Text("Apply") }
        }
    }
}
