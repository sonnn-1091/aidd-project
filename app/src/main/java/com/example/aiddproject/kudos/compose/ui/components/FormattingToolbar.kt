package com.example.aiddproject.kudos.compose.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aiddproject.R
import com.example.aiddproject.core.ui.rememberSingleClickHandler
import com.example.aiddproject.kudos.compose.ui.ToolbarAction
import com.example.aiddproject.kudos.compose.ui.WriteKudoTestTags

/**
 * C — Formatting toolbar (Figma `mms_C_Chức năng` `6885:9306`).
 *
 * Segmented strip layout — 6 fixed 24dp cells (Bold / Italic /
 * Strikethrough / NumberedList / Link / Quote) + 1 flex-grow cell
 * (Tiêu chuẩn cộng đồng). Each cell carries a 0.5dp `#998C5F` gold
 * border, transparent background, 4dp padding around a 16dp icon
 * (or the 10sp coral-red label for the standards cell).
 *
 * The strip's leftmost cell rounds its top-left corner 3.5dp; the
 * rightmost (standards) rounds its top-right corner 3.5dp. All
 * other cells stay square because they connect to neighbors.
 * Bottom corners are square — the strip is attached to the textarea
 * compound below.
 */
@Composable
fun FormattingToolbar(
    onAction: (ToolbarAction) -> Unit,
    onLinkTap: () -> Unit,
    onCommunityStandardsTap: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .height(24.dp)
                .testTag(WriteKudoTestTags.FORMATTING_TOOLBAR),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ToolbarCell(
            painter = painterResource(R.drawable.ic_write_kudo_toolbar_bold),
            descRes = R.string.a11y_write_kudo_toolbar_bold,
            tag = WriteKudoTestTags.BOLD_BUTTON,
            shape = ShapeLeftEdge,
            enabled = enabled,
            onClick = { onAction(ToolbarAction.Bold) },
        )
        ToolbarCell(
            painter = painterResource(R.drawable.ic_write_kudo_toolbar_italic),
            descRes = R.string.a11y_write_kudo_toolbar_italic,
            tag = WriteKudoTestTags.ITALIC_BUTTON,
            shape = ShapeMiddle,
            enabled = enabled,
            onClick = { onAction(ToolbarAction.Italic) },
        )
        ToolbarCell(
            painter = painterResource(R.drawable.ic_write_kudo_toolbar_strike),
            descRes = R.string.a11y_write_kudo_toolbar_strike,
            tag = WriteKudoTestTags.STRIKE_BUTTON,
            shape = ShapeMiddle,
            enabled = enabled,
            onClick = { onAction(ToolbarAction.Strikethrough) },
        )
        ToolbarCell(
            painter = painterResource(R.drawable.ic_write_kudo_toolbar_numbered_list),
            descRes = R.string.a11y_write_kudo_toolbar_numbered_list,
            tag = WriteKudoTestTags.NUMBERED_LIST_BUTTON,
            shape = ShapeMiddle,
            enabled = enabled,
            onClick = { onAction(ToolbarAction.NumberedList) },
        )
        ToolbarCell(
            painter = painterResource(R.drawable.ic_write_kudo_toolbar_link),
            descRes = R.string.a11y_write_kudo_toolbar_link,
            tag = WriteKudoTestTags.LINK_BUTTON,
            shape = ShapeMiddle,
            enabled = enabled,
            onClick = onLinkTap,
        )
        ToolbarCell(
            painter = painterResource(R.drawable.ic_write_kudo_toolbar_quote),
            descRes = R.string.a11y_write_kudo_toolbar_quote,
            tag = WriteKudoTestTags.QUOTE_BUTTON,
            shape = ShapeMiddle,
            enabled = enabled,
            onClick = { onAction(ToolbarAction.Quote) },
        )
        StandardsCell(
            enabled = enabled,
            onClick = onCommunityStandardsTap,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun ToolbarCell(
    painter: Painter,
    descRes: Int,
    tag: String,
    shape: Shape,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val click = rememberSingleClickHandler(onClick = onClick)
    val desc = stringResource(descRes)
    Box(
        modifier =
            Modifier
                .size(24.dp)
                .clip(shape)
                .background(Color.Transparent)
                .border(0.5.dp, FormFieldTokens.BorderGold, shape)
                .clickable(enabled = enabled, onClick = click)
                .padding(4.dp)
                .testTag(tag)
                .semantics { contentDescription = desc },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painter,
            contentDescription = null,
            tint = FormFieldTokens.LabelColor,
            modifier = Modifier.size(16.dp),
        )
    }
}

@Composable
private fun StandardsCell(
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val click = rememberSingleClickHandler(onClick = onClick)
    Box(
        modifier =
            modifier
                .height(24.dp)
                .clip(ShapeRightEdge)
                .background(Color.Transparent)
                .border(0.5.dp, FormFieldTokens.BorderGold, ShapeRightEdge)
                .clickable(enabled = enabled, onClick = click)
                .padding(horizontal = 7.dp, vertical = 4.dp)
                .testTag(WriteKudoTestTags.COMMUNITY_STANDARDS_LINK),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(R.string.write_kudo_community_standards_link),
            color = StandardsRed,
            fontSize = 10.sp,
            lineHeight = 16.sp,
            fontWeight = FontWeight.Normal,
            textDecoration = TextDecoration.Underline,
        )
    }
}

// Figma `mms_C` cell shapes — leftmost rounds top-left, rightmost
// rounds top-right, middle cells stay square. Bottom corners square
// throughout (the strip is attached to the textarea below).
private val ShapeLeftEdge: Shape = RoundedCornerShape(topStart = 3.5.dp, topEnd = 0.dp, bottomStart = 0.dp, bottomEnd = 0.dp)
private val ShapeMiddle: Shape = RoundedCornerShape(0.dp)
private val ShapeRightEdge: Shape = RoundedCornerShape(topStart = 0.dp, topEnd = 3.5.dp, bottomStart = 0.dp, bottomEnd = 0.dp)

// Figma "Tiêu chuẩn cộng đồng" red — coral, lighter than the form-
// validation red (#CF1322). Keep the two reds distinct.
private val StandardsRed: Color = Color(0xFFE46060)
