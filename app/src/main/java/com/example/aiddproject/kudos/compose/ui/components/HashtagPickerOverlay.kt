package com.example.aiddproject.kudos.compose.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aiddproject.R
import com.example.aiddproject.kudos.compose.ui.HashtagPickerState
import com.example.aiddproject.kudos.compose.ui.WriteKudoTestTags
import com.example.aiddproject.kudos.domain.Hashtag

/**
 * Sub-flow `aKWA2klsnt` — hashtag picker as an anchored Material 3
 * [DropdownMenu], matching the [RecipientPickerOverlay] chrome
 * (surface `#00070C`, 1dp `#998C5F` border, 8dp radius, 6dp inset).
 *
 * Multi-select: tapping a row toggles add/remove via the parent's
 * callbacks. Selected rows show a trailing check icon — no checkbox,
 * per request.
 */
@Composable
fun HashtagPickerOverlay(
    state: HashtagPickerState.Open,
    selected: List<String>,
    contentWidth: Dp,
    onAdd: (Hashtag) -> Unit,
    onRemove: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    DropdownMenu(
        expanded = true,
        onDismissRequest = onDismiss,
        modifier = modifier.testTag(WriteKudoTestTags.HASHTAG_OVERLAY),
        shape = RoundedCornerShape(8.dp),
        containerColor = MenuSurfaceColor,
        border = BorderStroke(1.dp, MenuBorderColor),
    ) {
        Box(
            modifier =
                Modifier
                    .let {
                        if (contentWidth > 0.dp) {
                            it.width(contentWidth)
                        } else {
                            it.widthIn(min = 220.dp, max = 320.dp)
                        }
                    }
                    .heightIn(min = 80.dp, max = 320.dp)
                    .padding(horizontal = 8.dp, vertical = 6.dp),
        ) {
            when (val r = state.results) {
                HashtagPickerState.ResultState.Loading -> CenteredSpinner()
                HashtagPickerState.ResultState.Empty ->
                    CenteredMessage(stringResource(R.string.write_kudo_hashtag_empty))
                is HashtagPickerState.ResultState.Error ->
                    CenteredMessage(stringResource(r.messageRes))
                is HashtagPickerState.ResultState.Loaded ->
                    Column(
                        verticalArrangement = Arrangement.spacedBy(1.dp),
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState()),
                    ) {
                        r.items.forEach { tag ->
                            HashtagRow(
                                tag = tag,
                                checked = selected.contains(tag.id),
                                onClick = {
                                    if (selected.contains(tag.id)) onRemove(tag.id) else onAdd(tag)
                                },
                            )
                        }
                    }
            }
        }
    }
}

@Composable
private fun HashtagRow(
    tag: Hashtag,
    checked: Boolean,
    onClick: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(4.dp))
                .then(
                    if (checked) Modifier.background(MenuSelectedRowColor) else Modifier,
                )
                .clickable(onClick = onClick)
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .testTag(WriteKudoTestTags.HASHTAG_OVERLAY_ROW_PREFIX + tag.id),
    ) {
        Text(
            text = "#${tag.tagName}",
            color = Color.White,
            fontSize = 14.sp,
            lineHeight = 18.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f),
        )
        if (checked) {
            Icon(
                painter = painterResource(R.drawable.ic_write_kudo_checked_hashtag),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.size(24.dp),
            )
        }
    }
}

@Composable
private fun CenteredSpinner() {
    Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = MenuGoldText)
    }
}

@Composable
private fun CenteredMessage(text: String) {
    Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
        Text(text = text, color = MenuTextSecondary, fontSize = 12.sp, lineHeight = 16.sp)
    }
}

// ── Dropdown chrome tokens — match RecipientPickerOverlay. ──────────

private val MenuSurfaceColor: Color = Color(0xFF00070C)
private val MenuBorderColor: Color = Color(0xFF998C5F)
private val MenuTextSecondary: Color = Color(0xFFB8B8B8)
private val MenuGoldText: Color = Color(0xFFFFEA9E)
private val MenuSelectedRowColor: Color = Color(0x33FFEA9E)
