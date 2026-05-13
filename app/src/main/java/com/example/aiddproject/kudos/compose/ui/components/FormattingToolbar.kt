package com.example.aiddproject.kudos.compose.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.FormatStrikethrough
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.aiddproject.R
import com.example.aiddproject.kudos.compose.ui.ToolbarAction
import com.example.aiddproject.kudos.compose.ui.WriteKudoTestTags

/**
 * C — Formatting toolbar (Figma `6885:9306`). Six toggle buttons for
 * the Markdown subset transforms (Q-W-1). The Link button is unique
 * — it opens [LinkInsertDialog] rather than firing a transform
 * directly.
 *
 * Active-state highlighting (per spec US4 Sc1 "the toolbar button
 * reflects the active state for the caret position") would require
 * inspecting the markdown at the caret — deferred to Phase 10 polish.
 * MVP renders all buttons as inactive; the transform still applies
 * correctly on tap.
 */
@Composable
fun FormattingToolbar(
    onAction: (ToolbarAction) -> Unit,
    onLinkTap: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .testTag(WriteKudoTestTags.FORMATTING_TOOLBAR),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        ToolbarButton(
            icon = Icons.Filled.FormatBold,
            descRes = R.string.a11y_write_kudo_toolbar_bold,
            tag = WriteKudoTestTags.BOLD_BUTTON,
            enabled = enabled,
            onClick = { onAction(ToolbarAction.Bold) },
        )
        ToolbarButton(
            icon = Icons.Filled.FormatItalic,
            descRes = R.string.a11y_write_kudo_toolbar_italic,
            tag = WriteKudoTestTags.ITALIC_BUTTON,
            enabled = enabled,
            onClick = { onAction(ToolbarAction.Italic) },
        )
        ToolbarButton(
            icon = Icons.Filled.FormatStrikethrough,
            descRes = R.string.a11y_write_kudo_toolbar_strike,
            tag = WriteKudoTestTags.STRIKE_BUTTON,
            enabled = enabled,
            onClick = { onAction(ToolbarAction.Strikethrough) },
        )
        ToolbarButton(
            icon = Icons.Filled.FormatListNumbered,
            descRes = R.string.a11y_write_kudo_toolbar_numbered_list,
            tag = WriteKudoTestTags.NUMBERED_LIST_BUTTON,
            enabled = enabled,
            onClick = { onAction(ToolbarAction.NumberedList) },
        )
        ToolbarButton(
            icon = Icons.Filled.Link,
            descRes = R.string.a11y_write_kudo_toolbar_link,
            tag = WriteKudoTestTags.LINK_BUTTON,
            enabled = enabled,
            onClick = onLinkTap,
        )
        ToolbarButton(
            icon = Icons.Filled.FormatQuote,
            descRes = R.string.a11y_write_kudo_toolbar_quote,
            tag = WriteKudoTestTags.QUOTE_BUTTON,
            enabled = enabled,
            onClick = { onAction(ToolbarAction.Quote) },
        )
    }
}

@Composable
private fun ToolbarButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    descRes: Int,
    tag: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val desc = stringResource(descRes)
    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier =
            Modifier
                .testTag(tag)
                .semantics { contentDescription = desc },
    ) {
        Icon(imageVector = icon, contentDescription = null)
    }
}
