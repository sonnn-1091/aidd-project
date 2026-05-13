package com.example.aiddproject.kudos.compose.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AssistChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aiddproject.R
import com.example.aiddproject.kudos.compose.domain.WriteKudoValidators
import com.example.aiddproject.kudos.compose.ui.WriteKudoTestTags
import com.example.aiddproject.kudos.domain.Hashtag

/**
 * E — "Hashtag *" label LEFT + tag area RIGHT (Figma `6885:9324`).
 *
 * Section row height: 32dp (smaller than the 40dp B.2 input rows).
 * Label width: ~70dp. Tag area: 229dp.
 *
 * Tag area renders either: (a) FlowRow of `AssistChip`s for the picked
 * tags, OR (b) the "+ Hashtag (Tối đa 5)" pill trigger when no tag
 * is added yet (matches Figma — the Figma shows the trigger pill in
 * its empty state).
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
        modifier = modifier.fillMaxWidth().testTag(WriteKudoTestTags.HASHTAG_SECTION),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth().height(32.dp),
        ) {
            KudosFieldLabel(
                text = stringResource(R.string.write_kudo_hashtag_label),
                required = true,
                width = FormFieldTokens.HashtagLabelWidth,
            )
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                if (tags.isEmpty()) {
                    AddPill(
                        label =
                            "${stringResource(R.string.write_kudo_hashtag_add)} (${
                                stringResource(R.string.write_kudo_hashtag_limit_note)
                            })",
                        enabled = !atLimit,
                        onClick = onAddTap,
                        testTag = WriteKudoTestTags.HASHTAG_ADD_BUTTON,
                    )
                } else {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        tags.forEach { tag ->
                            AssistChip(
                                onClick = { onRemoveTag(tag) },
                                label = { Text("#${tag.tagName}") },
                                trailingIcon = {
                                    Text(
                                        text = "✕",
                                        style = MaterialTheme.typography.labelSmall,
                                        modifier = Modifier.padding(start = 2.dp),
                                    )
                                },
                                modifier = Modifier.testTag(WriteKudoTestTags.HASHTAG_CHIP_PREFIX + tag.id),
                            )
                        }
                        if (!atLimit) {
                            AddPill(
                                label = "+",
                                enabled = true,
                                onClick = onAddTap,
                                testTag = WriteKudoTestTags.HASHTAG_ADD_BUTTON,
                            )
                        }
                    }
                }
            }
        }
        if (errorRes != null) {
            Text(
                text = stringResource(errorRes),
                style = MaterialTheme.typography.bodySmall,
                color = FormFieldTokens.RequiredRed,
                modifier = Modifier.padding(start = FormFieldTokens.HashtagLabelWidth + 12.dp),
            )
        }
    }
}

@Composable
private fun AddPill(
    label: String,
    enabled: Boolean,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .height(32.dp)
                .kudosFieldBox()
                .clickable(enabled = enabled, onClick = onClick)
                .padding(horizontal = 12.dp)
                .testTag(testTag),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            color = FormFieldTokens.PlaceholderColor,
            fontSize = 12.sp,
            lineHeight = 16.sp,
        )
    }
}
