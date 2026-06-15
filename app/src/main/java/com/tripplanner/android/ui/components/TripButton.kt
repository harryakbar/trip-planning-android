package com.tripplanner.android.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tripplanner.android.ui.theme.TripPlannerTheme

private val buttonHeight = Modifier.height(44.dp)
private val hPad = PaddingValues(horizontal = 20.dp, vertical = 0.dp)

@Composable
fun TripButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = modifier.then(buttonHeight),
        enabled = enabled,
        shape = MaterialTheme.shapes.medium,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
        contentPadding = hPad,
        content = content,
    )
}

@Composable
fun TripOutlinedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.then(buttonHeight),
        enabled = enabled,
        shape = MaterialTheme.shapes.medium,
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.primary,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (enabled) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.outline,
        ),
        contentPadding = hPad,
        content = content,
    )
}

@Composable
fun TripTextButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit,
) {
    TextButton(
        onClick = onClick,
        modifier = modifier.then(buttonHeight),
        enabled = enabled,
        shape = MaterialTheme.shapes.medium,
        colors = ButtonDefaults.textButtonColors(
            contentColor = MaterialTheme.colorScheme.primary,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
        content = content,
    )
}

@Composable
fun TripDestructiveButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = modifier.then(buttonHeight),
        enabled = enabled,
        shape = MaterialTheme.shapes.medium,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.error,
            contentColor = MaterialTheme.colorScheme.onError,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
        contentPadding = hPad,
        content = content,
    )
}

@Preview(name = "Buttons · Light", showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun ButtonsLightPreview() {
    TripPlannerTheme(darkTheme = false) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(16.dp),
        ) {
            TripButton(onClick = {}) { Text("Primary") }
            TripOutlinedButton(onClick = {}) { Text("Secondary") }
            TripTextButton(onClick = {}) { Text("Ghost") }
            TripDestructiveButton(onClick = {}) { Text("Destructive") }
            TripButton(onClick = {}, enabled = false) { Text("Disabled") }
        }
    }
}

@Preview(name = "Buttons · Dark", showBackground = true, backgroundColor = 0xFF1C1C1C)
@Composable
private fun ButtonsDarkPreview() {
    TripPlannerTheme(darkTheme = true) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(16.dp),
        ) {
            TripButton(onClick = {}) { Text("Primary") }
            TripOutlinedButton(onClick = {}) { Text("Secondary") }
            TripTextButton(onClick = {}) { Text("Ghost") }
            TripDestructiveButton(onClick = {}) { Text("Destructive") }
        }
    }
}
