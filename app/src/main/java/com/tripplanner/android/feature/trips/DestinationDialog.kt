package com.tripplanner.android.feature.trips

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.tripplanner.android.ui.components.TripButton
import com.tripplanner.android.ui.components.TripTextField
import com.tripplanner.android.ui.components.TripTextButton

@Composable
fun DestinationDialog(
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var text by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Name your trip") },
        text = {
            TripTextField(
                value = text,
                onValueChange = { text = it },
                placeholder = "e.g. Bali, Tokyo…",
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = {
            TripButton(onClick = { onConfirm(text) }) {
                Text("Add trip")
            }
        },
        dismissButton = {
            TripTextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}
