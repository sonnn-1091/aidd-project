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
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.aiddproject.R
import com.example.aiddproject.kudos.compose.ui.MentionOverlayState
import com.example.aiddproject.kudos.compose.ui.RecipientPickerState
import com.example.aiddproject.kudos.compose.ui.WriteKudoTestTags
import com.example.aiddproject.kudos.domain.SunnerNode

/**
 * @mention suggestion overlay (US4). Rendered inline below the
 * [MessageEditor] when the user types `@xyz` in the textarea —
 * **not** anchored to the caret position (caret-anchored Popups in
 * Compose require manual offset calculation; deferred to Phase 10).
 *
 * Reuses [RecipientPickerState.ResultState] so it renders the same
 * four sealed branches (Loading / NoResults / Error / Loaded) as
 * [RecipientPickerOverlay] does.
 */
@Composable
fun MentionSuggestionOverlay(
    state: MentionOverlayState.Open,
    onPick: (SunnerNode) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier =
            modifier
                .fillMaxWidth()
                .heightIn(min = 80.dp, max = 240.dp)
                .padding(horizontal = 16.dp)
                .testTag(WriteKudoTestTags.MENTION_OVERLAY),
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            when (val r = state.results) {
                RecipientPickerState.ResultState.Loading ->
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        contentAlignment = Alignment.Center,
                    ) { CircularProgressIndicator() }
                RecipientPickerState.ResultState.NoResults ->
                    CenteredText(stringResource(R.string.write_kudo_recipient_no_results))
                is RecipientPickerState.ResultState.Error ->
                    CenteredText(stringResource(r.messageRes))
                is RecipientPickerState.ResultState.Loaded ->
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        items(r.items, key = { it.id }) { node ->
                            MentionRow(node = node, onPick = onPick)
                        }
                    }
            }
        }
    }
}

@Composable
private fun MentionRow(
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
                .padding(horizontal = 12.dp, vertical = 4.dp),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = "@${node.fullName}", style = MaterialTheme.typography.bodyMedium)
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
private fun CenteredText(text: String) {
    Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
        Text(text = text, style = MaterialTheme.typography.bodyMedium)
    }
}
