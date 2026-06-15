package com.tripplanner.android.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.tripplanner.android.ui.theme.LocalTripColors
import com.tripplanner.android.ui.theme.TripPlannerTheme

@Composable
fun TripTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    supportingText: String? = null,
    isError: Boolean = false,
    enabled: Boolean = true,
    singleLine: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
) {
    val inputBg = LocalTripColors.current.inputBackground
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        enabled = enabled,
        label = label?.let { { Text(it) } },
        placeholder = placeholder?.let { { Text(it) } },
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        supportingText = supportingText?.let { { Text(it) } },
        isError = isError,
        singleLine = singleLine,
        keyboardOptions = keyboardOptions,
        visualTransformation = visualTransformation,
        shape = MaterialTheme.shapes.medium,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = inputBg,
            unfocusedContainerColor = inputBg,
            disabledContainerColor = inputBg,
            errorContainerColor = inputBg,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            errorIndicatorColor = Color.Transparent,
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            errorLabelColor = MaterialTheme.colorScheme.error,
        ),
    )
}

@Preview(name = "TextField · Light", showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun TextFieldLightPreview() {
    TripPlannerTheme(darkTheme = false) {
        var text by remember { mutableStateOf("") }
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(16.dp),
        ) {
            TripTextField(value = text, onValueChange = { text = it }, placeholder = "Search destinations")
            TripTextField(value = "Paris", onValueChange = {}, label = "Destination")
            TripTextField(value = "", onValueChange = {}, label = "Destination", isError = true, supportingText = "Required")
        }
    }
}

@Preview(name = "TextField · Dark", showBackground = true, backgroundColor = 0xFF1C1C1C)
@Composable
private fun TextFieldDarkPreview() {
    TripPlannerTheme(darkTheme = true) {
        TripTextField(
            value = "Tokyo",
            onValueChange = {},
            label = "Destination",
            modifier = Modifier.padding(16.dp),
        )
    }
}
