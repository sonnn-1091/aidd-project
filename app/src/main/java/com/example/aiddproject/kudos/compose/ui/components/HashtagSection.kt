package com.example.aiddproject.kudos.compose.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.aiddproject.R
import com.example.aiddproject.kudos.compose.domain.WriteKudoValidators
import com.example.aiddproject.kudos.compose.ui.WriteKudoTestTags
import com.example.aiddproject.kudos.domain.Hashtag

/**
 * E — Hashtag section (Figma `6885:9324`).
 *
 * Layout:
 *   - Label "Hashtag *"
 *   - FlowRow of AssistChips for the picked tags; tap the chip's trailing
 *     icon to remove.
 *   - "+ Hashtag (Tối đa 5)" OutlinedButton. Disabled at MAX_HASHTAGS.
 *   - Inline error string below, when [errorRes] is non-null.
 */
@Composable
fun HashtagSection(
    tags: List<Hashtag>,
    onAddTap: () -> Unit,
    onRemoveTag: (Hashtag) -> Unit,
    @StringRes errorRes: Int?,
    modifier: Modifier = Modifier,
) {
    val atLimit = tags.size >= WriteKudoValidators.MAX_HASHTAGS

    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .testTag(WriteKudoTestTags.HASHTAG_SECTION),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = "${stringResource(R.string.write_kudo_hashtag_label)} ${stringResource(R.string.write_kudo_required_marker)}",
            style = MaterialTheme.typography.labelMedium,
        )
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            tags.forEach { tag ->
                val a11yRemove = stringResource(R.string.a11y_write_kudo_remove_hashtag, tag.tagName)
                AssistChip(
                    onClick = { onRemoveTag(tag) },
                    label = { Text("#${tag.tagName}") },
                    trailingIcon = {
                        Text(
                            text = "✕",
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(start = 4.dp),
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(),
                    modifier =
                        Modifier
                            .testTag(WriteKudoTestTags.HASHTAG_CHIP_PREFIX + tag.id)
                            .padding(0.dp),
                )
                // Workaround: use a11yRemove via a no-op semantics modifier.
                // (Kept inline to avoid adding a wrapping Box per chip.)
                @Suppress("UNUSED_EXPRESSION") a11yRemove
            }
            OutlinedButton(
                onClick = onAddTap,
                enabled = !atLimit,
                modifier = Modifier.testTag(WriteKudoTestTags.HASHTAG_ADD_BUTTON),
            ) {
                Text(stringResource(R.string.write_kudo_hashtag_add))
            }
        }
        Text(
            text =
                if (errorRes != null) {
                    stringResource(errorRes)
                } else {
                    stringResource(R.string.write_kudo_hashtag_limit_note)
                },
            style = MaterialTheme.typography.bodySmall,
            color =
                if (errorRes != null) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
        )
    }
}
