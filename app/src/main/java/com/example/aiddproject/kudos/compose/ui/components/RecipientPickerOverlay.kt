package com.example.aiddproject.kudos.compose.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.aiddproject.R
import com.example.aiddproject.kudos.compose.ui.RecipientPickerState
import com.example.aiddproject.kudos.compose.ui.WriteKudoTestTags
import com.example.aiddproject.kudos.domain.SunnerNode

/**
 * Sub-flow `5MU728Tjck` — recipient picker overlay (Figma).
 *
 * Modal `AlertDialog`-backed overlay (rather than M3 `DropdownMenu`)
 * so the search field has the room it needs and the lazy result list
 * scrolls cleanly. Renders one of four branches per
 * [RecipientPickerState.ResultState]: loading / error / no-results /
 * loaded.
 */
@Composable
fun RecipientPickerOverlay(
    state: RecipientPickerState.Open,
    onQueryChange: (String) -> Unit,
    onPick: (SunnerNode) -> Unit,
    onDismiss: () -> Unit,
    onRetry: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.write_kudo_recipient_label)) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.testTag(WriteKudoTestTags.RECIPIENT_OVERLAY),
            ) {
                OutlinedTextField(
                    value = state.query,
                    onValueChange = onQueryChange,
                    placeholder = { Text(stringResource(R.string.write_kudo_recipient_placeholder)) },
                    singleLine = true,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .testTag(WriteKudoTestTags.RECIPIENT_SEARCH_INPUT),
                )
                Box(modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp, max = 320.dp)) {
                    when (val result = state.results) {
                        RecipientPickerState.ResultState.Loading -> CenteredSpinner()
                        RecipientPickerState.ResultState.NoResults ->
                            CenteredText(stringResource(R.string.write_kudo_recipient_no_results))
                        is RecipientPickerState.ResultState.Error ->
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.align(Alignment.Center),
                            ) {
                                Text(
                                    text = stringResource(result.messageRes),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.error,
                                )
                                TextButton(onClick = onRetry) {
                                    Text(stringResource(R.string.write_kudo_recipient_retry))
                                }
                            }
                        is RecipientPickerState.ResultState.Loaded ->
                            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                                items(result.items, key = { it.id }) { node ->
                                    RecipientRow(node = node, onPick = onPick)
                                }
                            }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.write_kudo_cancel))
            }
        },
    )
}

@Composable
private fun RecipientRow(
    node: SunnerNode,
    onPick: (SunnerNode) -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier =
            Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .clickable { onPick(node) }
                .padding(horizontal = 4.dp, vertical = 8.dp)
                .testTag(WriteKudoTestTags.RECIPIENT_ROW_PREFIX + node.id),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = node.fullName, style = MaterialTheme.typography.bodyMedium)
            node.department?.name?.let { deptName ->
                Text(
                    text = deptName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun CenteredSpinner() {
    Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun CenteredText(text: String) {
    Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
        Text(text = text, style = MaterialTheme.typography.bodyMedium)
    }
}
