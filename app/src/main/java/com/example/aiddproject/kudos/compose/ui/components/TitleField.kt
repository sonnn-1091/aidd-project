package com.example.aiddproject.kudos.compose.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.aiddproject.R
import com.example.aiddproject.kudos.compose.ui.WriteKudoTestTags

/**
 * B.3 + B.4 — "Danh hiệu" label + input (Figma `6885:9299` / `6885:9302`).
 *
 * Required marker `*` rendered via Text with the localised marker. The
 * input's `supportingText` slot wires the optional [errorRes] for the
 * inline error message; `isError` flips the M3 error treatment when
 * a validation error is shown.
 */
@Composable
fun TitleField(
    value: String,
    onValueChange: (String) -> Unit,
    @StringRes errorRes: Int?,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)) {
        Row(
            label = stringResource(R.string.write_kudo_title_label),
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            placeholder = { Text(stringResource(R.string.write_kudo_title_placeholder)) },
            isError = errorRes != null,
            supportingText = errorRes?.let { { Text(stringResource(it)) } },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .testTag(WriteKudoTestTags.TITLE_INPUT),
        )
    }
}

@Composable
private fun Row(label: String) {
    Text(
        text = "$label ${stringResource(R.string.write_kudo_required_marker)}",
        style = MaterialTheme.typography.labelMedium,
        modifier = Modifier.padding(bottom = 4.dp),
    )
}
