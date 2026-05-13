package com.example.aiddproject.kudos.compose.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aiddproject.R
import com.example.aiddproject.kudos.compose.ui.RecipientPickerState
import com.example.aiddproject.kudos.compose.ui.WriteKudoTestTags
import com.example.aiddproject.kudos.domain.SunnerNode

/**
 * Sub-flow `5MU728Tjck` — recipient picker as an anchored
 * Material 3 [DropdownMenu] (matches the hub's
 * HashtagFilterDropdown / DepartmentFilterDropdown chrome:
 * surface `#00070C`, 1dp `#998C5F` border, 8dp radius, 6dp inset).
 *
 * Layout per Figma — list of result rows only, no in-dropdown search
 * input. Each row is 56dp tall:
 *   `Avatar 32dp` + 12dp gap + `Column { fullName (white SemiBold
 *   14sp), department (gray 12sp) }`.
 *
 * The anchor is the parent Box around the trigger in
 * [RecipientPickerField]. DropdownMenu auto-positions itself below
 * the trigger and matches its width.
 */
@Composable
fun RecipientPickerOverlay(
    state: RecipientPickerState.Open,
    onPick: (SunnerNode) -> Unit,
    onDismiss: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    DropdownMenu(
        expanded = true,
        onDismissRequest = onDismiss,
        modifier = modifier.testTag(WriteKudoTestTags.RECIPIENT_OVERLAY),
        shape = RoundedCornerShape(8.dp),
        containerColor = MenuSurfaceColor,
        border = BorderStroke(1.dp, MenuBorderColor),
    ) {
        Box(
            modifier =
                Modifier
                    .widthIn(min = 220.dp, max = 320.dp)
                    .heightIn(min = 80.dp, max = 320.dp)
                    .padding(horizontal = 8.dp, vertical = 6.dp),
        ) {
            when (val r = state.results) {
                RecipientPickerState.ResultState.Loading -> CenteredSpinner()
                RecipientPickerState.ResultState.NoResults ->
                    CenteredMessage(stringResource(R.string.write_kudo_recipient_no_results))
                is RecipientPickerState.ResultState.Error ->
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.align(Alignment.Center),
                    ) {
                        Text(
                            text = stringResource(r.messageRes),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MenuTextSecondary,
                        )
                        TextButton(onClick = onRetry) {
                            Text(
                                text = stringResource(R.string.write_kudo_recipient_retry),
                                color = MenuGoldText,
                            )
                        }
                    }
                is RecipientPickerState.ResultState.Loaded ->
                    // DropdownMenu wraps its content in SubcomposeLayout
                    // which doesn't support intrinsic measurements — so
                    // LazyColumn crashes. Use a plain Column with
                    // verticalScroll instead (same pattern as the hub's
                    // HashtagFilterDropdown).
                    Column(
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState()),
                    ) {
                        r.items.forEach { node ->
                            RecipientRow(
                                node = node,
                                onPick = {
                                    onPick(node)
                                    onDismiss()
                                },
                            )
                        }
                    }
            }
        }
    }
}

@Composable
private fun RecipientRow(
    node: SunnerNode,
    onPick: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier =
            Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
                .clickable(onClick = onPick)
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .testTag(WriteKudoTestTags.RECIPIENT_ROW_PREFIX + node.id),
    ) {
        Avatar(node = node)
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = node.fullName,
                color = Color.White,
                fontSize = 14.sp,
                lineHeight = 18.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
            )
            Text(
                text = node.department?.name ?: "",
                color = MenuTextSecondary,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun Avatar(node: SunnerNode) {
    // DEMO placeholder — alternate between the two seed images already
    // on disk for the hub. Production should render `node.avatarUrl`
    // via Coil.
    val resId =
        if (node.id.hashCode() % 2 == 0) {
            R.drawable.kudos_avatar_sender
        } else {
            R.drawable.kudos_avatar_recipient
        }
    Image(
        painter = painterResource(resId),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier =
            Modifier
                .size(32.dp)
                .clip(CircleShape)
                .border(1.dp, Color.White, CircleShape),
    )
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

// ── Dropdown chrome tokens — match the hub filter dropdowns. ────────

private val MenuSurfaceColor: Color = Color(0xFF00070C)
private val MenuBorderColor: Color = Color(0xFF998C5F)
private val MenuTextSecondary: Color = Color(0xFFB8B8B8)
private val MenuGoldText: Color = Color(0xFFFFEA9E)
