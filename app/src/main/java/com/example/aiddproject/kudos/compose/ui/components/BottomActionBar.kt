package com.example.aiddproject.kudos.compose.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aiddproject.R
import com.example.aiddproject.core.ui.rememberSingleClickHandler
import com.example.aiddproject.kudos.compose.ui.WriteKudoTestTags

/**
 * Sticky bottom action bar — Figma `actions` (`6891:16832`).
 *
 * Two pill buttons with 4dp radius + icons:
 *   - Cancel (`Huỷ`): semi-transparent gold (`#FFEA9E` @ 10%) over the
 *     KV background with 1dp `#998C5F` border + WHITE label + Close
 *     icon. Grows to fill remaining width (`weight(1f)`).
 *   - Send (`Gửi đi`): solid `#FFEA9E` gold with dark `#00101A` label
 *     + paper-plane icon. Fixed 160dp wide.
 *
 * Implements the **novel "tap-reveals-errors-on-disabled-Send"**
 * pattern (plan § Notes): M3's `Button(enabled = false)` swallows
 * onClick, so the outer Box `pointerInput.detectTapGestures`
 * intercepts the tap regardless of disabled state and the VM forks
 * on `isSubmitEnabled` to either submit or reveal field errors.
 *
 * Cancel stays enabled even while sending (TC_VIETKUDO_FUN_060) —
 * the in-flight cancel behavior is wired in the VM.
 */
@Composable
fun BottomActionBar(
    isSubmitEnabled: Boolean,
    isSending: Boolean,
    onCancel: () -> Unit,
    onSend: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val cancelClick = rememberSingleClickHandler(onClick = onCancel)

    val cancelDescription = stringResource(R.string.a11y_write_kudo_cancel)
    val sendDescription =
        stringResource(
            when {
                isSending -> R.string.a11y_write_kudo_send_sending
                isSubmitEnabled -> R.string.a11y_write_kudo_send_enabled
                else -> R.string.a11y_write_kudo_send_disabled_hint
            },
        )
    val sendState =
        when {
            isSending -> "sending"
            isSubmitEnabled -> "enabled"
            else -> "disabled"
        }

    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .testTag(WriteKudoTestTags.BOTTOM_ACTION_BAR),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // ── Cancel (Huỷ) — outlined, gold @ 10% over the KV bg ──
        OutlinedButton(
            onClick = cancelClick,
            shape = RoundedCornerShape(4.dp),
            border = BorderStroke(1.dp, FormBorderGold),
            colors =
                ButtonDefaults.outlinedButtonColors(
                    containerColor = CancelGoldOverlay,
                    contentColor = Color.White,
                ),
            modifier =
                Modifier
                    .weight(1f)
                    .height(40.dp)
                    .testTag(WriteKudoTestTags.CANCEL_BUTTON)
                    .semantics { contentDescription = cancelDescription },
        ) {
            Text(
                text = stringResource(R.string.write_kudo_cancel),
                color = Color.White,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(end = 8.dp),
            )
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(24.dp),
            )
        }

        // ── Send (Gửi đi) — solid gold, tap-reveals-errors wrapper ──
        Box(
            modifier =
                Modifier
                    .testTag(WriteKudoTestTags.SEND_BUTTON_TAP_LAYER)
                    .pointerInput(isSending) {
                        detectTapGestures(
                            onTap = { if (!isSending) onSend() },
                        )
                    },
        ) {
            Button(
                onClick = onSend,
                enabled = isSubmitEnabled && !isSending,
                shape = RoundedCornerShape(4.dp),
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = SendGold,
                        contentColor = SendTextDark,
                        disabledContainerColor = SendGold.copy(alpha = 0.5f),
                        disabledContentColor = SendTextDark.copy(alpha = 0.5f),
                    ),
                modifier =
                    Modifier
                        .width(160.dp)
                        .height(40.dp)
                        .testTag(WriteKudoTestTags.SEND_BUTTON)
                        .semantics {
                            contentDescription = sendDescription
                            stateDescription = sendState
                        },
            ) {
                if (isSending) {
                    CircularProgressIndicator(
                        color = SendTextDark,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(16.dp).padding(end = 4.dp),
                    )
                }
                Text(
                    text = stringResource(R.string.write_kudo_send),
                    color = SendTextDark,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(end = 8.dp),
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = null,
                    tint = SendTextDark,
                    modifier = Modifier.size(24.dp),
                )
            }
        }
    }
}

// ── Figma tokens (queried 2026-05-13) ───────────────────────────────

private val FormBorderGold: Color = Color(0xFF998C5F)
private val CancelGoldOverlay: Color = Color(0x1AFFEA9E) // #FFEA9E @ 10%
private val SendGold: Color = Color(0xFFFFEA9E)
private val SendTextDark: Color = Color(0xFF00101A)
