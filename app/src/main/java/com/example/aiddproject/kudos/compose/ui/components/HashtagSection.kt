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
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aiddproject.R
import com.example.aiddproject.kudos.compose.domain.WriteKudoValidators
import com.example.aiddproject.kudos.compose.ui.HashtagPickerState
import com.example.aiddproject.kudos.compose.ui.WriteKudoTestTags
import com.example.aiddproject.kudos.domain.Hashtag

/**
 * E — "Hashtag *" label LEFT + tag area RIGHT (Figma `6885:9324`).
 *
 * Right column stacks selected chips (each with an ✕ dismiss icon)
 * ABOVE a persistent `+ Hashtag (Tối đa 5)` trigger so the user can
 * keep picking. Trigger anchors the [HashtagPickerOverlay] dropdown.
 */
@Composable
fun HashtagSection(
    tags: List<Hashtag>,
    pickerState: HashtagPickerState,
    onAddTap: () -> Unit,
    onPickerDismiss: () -> Unit,
    onHashtagAdd: (Hashtag) -> Unit,
    onRemoveTag: (String) -> Unit,
    @StringRes errorRes: Int?,
    modifier: Modifier = Modifier,
) {
    val atLimit = tags.size >= WriteKudoValidators.MAX_HASHTAGS

    var rowWidthPx by remember { mutableIntStateOf(0) }
    val density = LocalDensity.current
    val rowWidth = with(density) { rowWidthPx.toDp() }

    Column(
        modifier = modifier.fillMaxWidth().testTag(WriteKudoTestTags.HASHTAG_SECTION),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .onSizeChanged { rowWidthPx = it.width },
        ) {
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                // Label cell — fixed 32dp so it vertical-centers against
                // the first content row (chips OR trigger).
                Box(
                    modifier = Modifier.height(32.dp),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    KudosFieldLabel(
                        text = stringResource(R.string.write_kudo_hashtag_label),
                        required = true,
                        width = FormFieldTokens.HashtagLabelWidth,
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    if (tags.isNotEmpty()) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            tags.forEach { tag ->
                                AssistChip(
                                    onClick = { onRemoveTag(tag.id) },
                                    label = {
                                        Text(
                                            text = "#${tag.tagName}",
                                            color = FormFieldTokens.LabelColor,
                                        )
                                    },
                                    trailingIcon = {
                                        Text(
                                            text = "✕",
                                            color = FormFieldTokens.LabelColor,
                                            style = MaterialTheme.typography.labelSmall,
                                            modifier = Modifier.padding(start = 2.dp),
                                        )
                                    },
                                    colors =
                                        AssistChipDefaults.assistChipColors(
                                            containerColor = FormFieldTokens.FieldFill,
                                            labelColor = FormFieldTokens.LabelColor,
                                            trailingIconContentColor = FormFieldTokens.LabelColor,
                                        ),
                                    border = BorderStroke(0.5.dp, FormFieldTokens.BorderGold),
                                    modifier = Modifier.testTag(WriteKudoTestTags.HASHTAG_CHIP_PREFIX + tag.id),
                                )
                            }
                        }
                    }
                    if (!atLimit) {
                        AddPill(
                            label =
                                "${stringResource(R.string.write_kudo_hashtag_add)} (${
                                    stringResource(R.string.write_kudo_hashtag_limit_note)
                                })",
                            enabled = true,
                            onClick = onAddTap,
                            testTag = WriteKudoTestTags.HASHTAG_ADD_BUTTON,
                        )
                    }
                }
            }
            if (pickerState is HashtagPickerState.Open) {
                HashtagPickerOverlay(
                    state = pickerState,
                    selected = tags.map { it.id },
                    contentWidth = rowWidth,
                    onAdd = onHashtagAdd,
                    onRemove = onRemoveTag,
                    onDismiss = onPickerDismiss,
                )
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
