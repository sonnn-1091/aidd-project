package com.example.aiddproject.kudos.compose.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.aiddproject.R
import com.example.aiddproject.kudos.compose.domain.RichTextValue
import com.example.aiddproject.kudos.compose.domain.WriteKudoValidators
import com.example.aiddproject.kudos.compose.ui.WriteKudoTestTags

/**
 * D — Message textarea (Figma `6885:9322`) + D.1 hint label + live
 * character counter.
 *
 * **Phase 3 MVP**: plain-text only. The `FormattingToolbar` (C.*) and
 * `MentionSuggestionOverlay` ship in Phase 6 / US4.
 *
 * The counter renders below the textarea via the `supportingText` slot;
 * when the message error is non-null OR the user is over the limit, the
 * counter takes the M3 error treatment.
 */
@Composable
fun MessageEditor(
    value: RichTextValue,
    onValueChange: (RichTextValue) -> Unit,
    @StringRes errorRes: Int?,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
) {
    val length = value.plainText.length
    val isOverLimit = length > WriteKudoValidators.MAX_MESSAGE_LENGTH
    val displayError = errorRes != null || isOverLimit

    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        OutlinedTextField(
            value = value.plainText,
            onValueChange = { onValueChange(RichTextValue.ofPlainText(it)) },
            enabled = enabled,
            placeholder = { Text(stringResource(R.string.write_kudo_message_placeholder)) },
            isError = displayError,
            supportingText = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = errorRes?.let { stringResource(it) } ?: stringResource(R.string.write_kudo_message_hint),
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Text(
                        text =
                            stringResource(
                                R.string.write_kudo_character_counter,
                                length,
                                WriteKudoValidators.MAX_MESSAGE_LENGTH,
                            ),
                        style = MaterialTheme.typography.bodySmall,
                        color =
                            if (displayError) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                        textAlign = TextAlign.End,
                        modifier = Modifier.testTag(WriteKudoTestTags.MESSAGE_CHARACTER_COUNTER),
                    )
                }
            },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = 120.dp)
                    .testTag(WriteKudoTestTags.MESSAGE_TEXTAREA),
        )
    }
}
