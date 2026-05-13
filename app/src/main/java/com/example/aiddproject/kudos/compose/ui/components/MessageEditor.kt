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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.aiddproject.R
import com.example.aiddproject.kudos.compose.domain.RichTextValue
import com.example.aiddproject.kudos.compose.domain.WriteKudoValidators
import com.example.aiddproject.kudos.compose.ui.ToolbarAction
import com.example.aiddproject.kudos.compose.ui.WriteKudoTestTags

/**
 * D — Message textarea (Figma `6885:9322`) + C formatting toolbar +
 * D.1 hint label + live character counter.
 *
 * Tracks the textarea's selection via [TextFieldValue] so the parent
 * VM can apply toolbar transforms to the right range. Detects the
 * `@xyz` mention trigger pattern (an `@` followed by at least one
 * non-whitespace char at the cursor) and emits the query / offset to
 * the parent so the [MentionSuggestionOverlay] can open.
 */
@Composable
fun MessageEditor(
    value: RichTextValue,
    onValueChange: (RichTextValue) -> Unit,
    onSelectionChange: (IntRange) -> Unit,
    onMentionQueryChange: (query: String, triggerOffset: Int) -> Unit,
    onMentionDismiss: () -> Unit,
    onToolbarAction: (ToolbarAction) -> Unit,
    onLinkTap: () -> Unit,
    onCommunityStandardsTap: () -> Unit,
    @StringRes errorRes: Int?,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
) {
    // Track the textarea's selection locally so we can pass it to the VM
    // without re-routing the whole TextFieldValue through state.
    var fieldValue by remember(value.plainText) {
        mutableStateOf(TextFieldValue(text = value.plainText, selection = TextRange(value.plainText.length)))
    }
    val length = value.plainText.length
    val isOverLimit = length > WriteKudoValidators.MAX_MESSAGE_LENGTH
    val displayError = errorRes != null || isOverLimit

    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        FormattingToolbar(
            onAction = onToolbarAction,
            onLinkTap = onLinkTap,
            onCommunityStandardsTap = onCommunityStandardsTap,
            enabled = enabled,
        )
        OutlinedTextField(
            value = fieldValue,
            onValueChange = { next ->
                fieldValue = next
                if (next.text != value.plainText) {
                    onValueChange(RichTextValue.ofPlainText(next.text))
                }
                val sel = next.selection
                onSelectionChange(sel.start until sel.end.coerceAtLeast(sel.start))
                handleMentionTrigger(
                    text = next.text,
                    caret = sel.start,
                    onMentionQueryChange = onMentionQueryChange,
                    onMentionDismiss = onMentionDismiss,
                )
            },
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

/**
 * Pure helper — looks back from [caret] for the start of an `@…`
 * token in [text]. If the cursor is inside one, emit the query +
 * the offset of the `@`. Otherwise dismiss the overlay.
 */
private fun handleMentionTrigger(
    text: String,
    caret: Int,
    onMentionQueryChange: (query: String, triggerOffset: Int) -> Unit,
    onMentionDismiss: () -> Unit,
) {
    if (caret == 0) {
        onMentionDismiss()
        return
    }
    var i = caret - 1
    while (i >= 0) {
        val ch = text[i]
        if (ch.isWhitespace()) {
            onMentionDismiss()
            return
        }
        if (ch == '@') {
            val query = text.substring(i + 1, caret)
            if (query.isEmpty()) {
                onMentionDismiss()
            } else {
                onMentionQueryChange(query, i)
            }
            return
        }
        i -= 1
    }
    onMentionDismiss()
}
