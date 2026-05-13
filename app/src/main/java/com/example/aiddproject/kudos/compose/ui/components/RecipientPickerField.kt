package com.example.aiddproject.kudos.compose.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
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
import com.example.aiddproject.kudos.compose.ui.RecipientPickerState
import com.example.aiddproject.kudos.compose.ui.WriteKudoTestTags
import com.example.aiddproject.kudos.domain.SunnerNode

/**
 * B.1 + B.2 — "Người nhận *" label LEFT + dropdown trigger RIGHT
 * (Figma `6885:9293` / `6885:9297`).
 *
 * Trigger fills horizontal width (weight 1f after the label). The
 * dropdown anchors to the outer Box wrapping the FULL row so it spans
 * the entire card width when open (including the label gutter on the
 * left).
 */
@Composable
fun RecipientPickerField(
    recipientName: String?,
    pickerState: RecipientPickerState,
    onOpenPicker: () -> Unit,
    onDismissPicker: () -> Unit,
    onPick: (SunnerNode) -> Unit,
    onRetry: () -> Unit,
    @StringRes errorRes: Int?,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
) {
    var rowWidthPx by remember { mutableIntStateOf(0) }
    val density = LocalDensity.current
    val rowWidth = with(density) { rowWidthPx.toDp() }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .onSizeChanged { rowWidthPx = it.width },
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth().height(FormFieldTokens.FieldHeight),
            ) {
                KudosFieldLabel(
                    text = stringResource(R.string.write_kudo_recipient_label),
                    required = true,
                )
                Box(
                    modifier =
                        Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .kudosFieldBox()
                            .clickable(enabled = enabled, onClick = onOpenPicker)
                            .padding(
                                horizontal = FormFieldTokens.FieldHorizontalPadding,
                                vertical = FormFieldTokens.FieldVerticalPadding,
                            )
                            .testTag(WriteKudoTestTags.RECIPIENT_FIELD),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = recipientName ?: stringResource(R.string.write_kudo_recipient_placeholder),
                            color =
                                if (recipientName == null) {
                                    FormFieldTokens.PlaceholderColor
                                } else {
                                    FormFieldTokens.LabelColor
                                },
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            modifier = Modifier.padding(end = 8.dp),
                        )
                        Icon(
                            imageVector = Icons.Filled.KeyboardArrowDown,
                            contentDescription = null,
                            tint = FormFieldTokens.LabelColor,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                }
            }
            if (pickerState is RecipientPickerState.Open) {
                RecipientPickerOverlay(
                    state = pickerState,
                    contentWidth = rowWidth,
                    onPick = onPick,
                    onDismiss = onDismissPicker,
                    onRetry = onRetry,
                )
            }
        }
        if (errorRes != null) {
            Text(
                text = stringResource(errorRes),
                style = MaterialTheme.typography.bodySmall,
                color = FormFieldTokens.RequiredRed,
                modifier = Modifier.padding(start = FormFieldTokens.LabelWidth + 8.dp),
            )
        }
    }
}
