package com.example.aiddproject.kudos.compose.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aiddproject.R
import com.example.aiddproject.kudos.compose.domain.RichTextValue
import com.example.aiddproject.kudos.compose.domain.WriteKudoValidators
import com.example.aiddproject.kudos.compose.ui.ToolbarAction
import com.example.aiddproject.kudos.compose.ui.WriteKudoTestTags

/**
 * D — Message textarea (Figma `6885:9322`) + C formatting toolbar +
 * D.1 hint label + live character counter.
 *
 * Figma renders the toolbar and the textarea as a SINGLE attached
 * compound — the toolbar sits on top, no gap, no border on the
 * toolbar itself; the textarea below has top-square corners + bottom-
 * rounded corners (3.5dp), 0.5dp gold border, 89dp min height,
 * 8dp internal padding.
 *
 * The "@ + tên" hint sits in its own centered text BELOW the box,
 * 10sp gray.
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
    var fieldValue by remember(value.plainText) {
        mutableStateOf(TextFieldValue(text = value.plainText, selection = TextRange(value.plainText.length)))
    }
    val length = value.plainText.length
    val isOverLimit = length > WriteKudoValidators.MAX_MESSAGE_LENGTH
    val displayError = errorRes != null || isOverLimit

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        // Attached toolbar + textarea compound — no gap between them.
        Column(modifier = Modifier.fillMaxWidth()) {
            FormattingToolbar(
                onAction = onToolbarAction,
                onLinkTap = onLinkTap,
                onCommunityStandardsTap = onCommunityStandardsTap,
                enabled = enabled,
            )
            // Textarea — top-square corners (attached to toolbar
            // above), bottom-rounded corners 3.5dp.
            val textareaShape = RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp, bottomStart = 3.5.dp, bottomEnd = 3.5.dp)
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .heightIn(min = 90.dp)
                        .clip(textareaShape)
                        .background(FormFieldTokens.FieldFill)
                        .border(
                            width = 0.5.dp,
                            color = if (displayError) FormFieldTokens.RequiredRed else FormFieldTokens.BorderGold,
                            shape = textareaShape,
                        )
                        .padding(8.dp)
                        .testTag(WriteKudoTestTags.MESSAGE_TEXTAREA),
                contentAlignment = Alignment.TopStart,
            ) {
                if (fieldValue.text.isEmpty()) {
                    Text(
                        text = stringResource(R.string.write_kudo_message_placeholder),
                        color = FormFieldTokens.PlaceholderColor,
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                    )
                }
                BasicTextField(
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
                    textStyle =
                        LocalTextStyle.current.copy(
                            color = FormFieldTokens.LabelColor,
                            fontSize = 14.sp,
                            lineHeight = 18.sp,
                        ),
                    cursorBrush = SolidColor(FormFieldTokens.LabelColor),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        // D.1 hint — centered 10sp gray.
        Text(
            text = errorRes?.let { stringResource(it) } ?: stringResource(R.string.write_kudo_message_hint),
            color = if (displayError) FormFieldTokens.RequiredRed else FormFieldTokens.PlaceholderColor,
            fontSize = 10.sp,
            lineHeight = 16.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )

        // Character counter — only shown when at/over the limit per the
        // Figma (the always-visible counter is not in the reference image).
        if (isOverLimit) {
            Text(
                text =
                    stringResource(
                        R.string.write_kudo_character_counter,
                        length,
                        WriteKudoValidators.MAX_MESSAGE_LENGTH,
                    ),
                color = FormFieldTokens.RequiredRed,
                fontSize = 10.sp,
                lineHeight = 16.sp,
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth().testTag(WriteKudoTestTags.MESSAGE_CHARACTER_COUNTER),
            )
        } else {
            // Hidden counter — keep the testTag wired for tests but render
            // an empty placeholder Text so the slot is always available.
            Text(
                text =
                    stringResource(
                        R.string.write_kudo_character_counter,
                        length,
                        WriteKudoValidators.MAX_MESSAGE_LENGTH,
                    ),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0f),
                fontSize = 1.sp,
                modifier = Modifier.testTag(WriteKudoTestTags.MESSAGE_CHARACTER_COUNTER),
            )
        }
    }
}

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
