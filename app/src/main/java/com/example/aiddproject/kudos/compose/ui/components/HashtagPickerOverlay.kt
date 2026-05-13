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
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.aiddproject.R
import com.example.aiddproject.kudos.compose.ui.HashtagPickerState
import com.example.aiddproject.kudos.compose.ui.WriteKudoTestTags
import com.example.aiddproject.kudos.domain.Hashtag

/**
 * Sub-flow `aKWA2klsnt` — hashtag picker overlay (Figma).
 *
 * Multi-select checklist. Checked = currently in the form's tag list.
 * Tapping toggles add/remove via the parent's callbacks. Dismiss
 * without selecting leaves the list unchanged.
 */
@Composable
fun HashtagPickerOverlay(
    state: HashtagPickerState.Open,
    selected: List<String>,
    onAdd: (Hashtag) -> Unit,
    onRemove: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.write_kudo_hashtag_label)) },
        text = {
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .heightIn(min = 120.dp, max = 320.dp)
                        .testTag(WriteKudoTestTags.HASHTAG_OVERLAY),
            ) {
                when (val r = state.results) {
                    HashtagPickerState.ResultState.Loading ->
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    HashtagPickerState.ResultState.Empty ->
                        CenteredText(stringResource(R.string.write_kudo_hashtag_empty))
                    is HashtagPickerState.ResultState.Error ->
                        CenteredText(stringResource(r.messageRes))
                    is HashtagPickerState.ResultState.Loaded ->
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(2.dp),
                        ) {
                            items(r.items, key = { it.id }) { tag ->
                                HashtagRow(
                                    tag = tag,
                                    checked = selected.contains(tag.id),
                                    onToggle = { checked ->
                                        if (checked) onAdd(tag) else onRemove(tag.id)
                                    },
                                )
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
private fun HashtagRow(
    tag: Hashtag,
    checked: Boolean,
    onToggle: (Boolean) -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier =
            Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .clickable { onToggle(!checked) }
                .padding(horizontal = 4.dp)
                .testTag(WriteKudoTestTags.HASHTAG_OVERLAY_ROW_PREFIX + tag.id),
    ) {
        Checkbox(checked = checked, onCheckedChange = null)
        Text(
            text = "#${tag.tagName}",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(start = 8.dp),
        )
    }
}

@Composable
private fun CenteredText(text: String) {
    Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
        Text(text = text, style = MaterialTheme.typography.bodyMedium)
    }
}
