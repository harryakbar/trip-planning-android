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
import androidx.compose.ui.res.stringResource
import com.tripplanner.android.R
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
        title = { Text(stringResource(R.string.dialog_name_trip_title)) },
        text = {
            TripTextField(
                value = text,
                onValueChange = { text = it },
                placeholder = stringResource(R.string.dialog_name_trip_placeholder),
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = {
            TripButton(onClick = { onConfirm(text) }) {
                Text(stringResource(R.string.dialog_add_trip))
            }
        },
        dismissButton = {
            TripTextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) }
        },
    )
}
