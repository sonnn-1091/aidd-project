package com.example.aiddproject.kudos.compose.ui.components

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import com.example.aiddproject.R
import com.example.aiddproject.core.ui.rememberSingleClickHandler
import com.example.aiddproject.kudos.compose.ui.WriteKudoTestTags

/**
 * Sticky bottom action bar holding H (Cancel) + I (Send) (Figma
 * `6891:16834` + `6891:16833`).
 *
 * Implements the **novel "tap-reveals-errors-on-disabled-Send"
 * pattern** documented in plan § Notes: M3's `Button(enabled = false)`
 * swallows its own `onClick`, so the outer `Box(...pointerInput {
 * detectTapGestures(onTap = ...) })` intercepts the tap regardless of
 * disabled state. The inner `Button(enabled = state.isSubmitEnabled)`
 * keeps the M3 visual treatment + a11y disabled state. The tap is
 * always routed to [onSend]; the VM forks on `isSubmitEnabled` to
 * either submit OR reveal the field errors (the `0le8xKnFE_`
 * contract).
 *
 * Cancel (H) remains enabled even while `isSending` per
 * TC_VIETKUDO_FUN_060 — the in-flight cancel is the only remaining
 * action while submit is in flight.
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

    Surface(
        tonalElevation = 2.dp,
        modifier =
            modifier
                .fillMaxWidth()
                .testTag(WriteKudoTestTags.BOTTOM_ACTION_BAR),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier =
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            TextButton(
                onClick = cancelClick,
                modifier =
                    Modifier
                        .testTag(WriteKudoTestTags.CANCEL_BUTTON)
                        .semantics { contentDescription = sendDescription },
            ) {
                Text(stringResource(R.string.write_kudo_cancel))
            }
            // Send button wrapped in an outer Box that always intercepts
            // taps — including when the inner M3 Button is disabled.
            Box(
                modifier =
                    Modifier
                        .weight(1f)
                        .testTag(WriteKudoTestTags.SEND_BUTTON_TAP_LAYER)
                        .pointerInput(isSending) {
                            detectTapGestures(
                                onTap = {
                                    if (!isSending) onSend()
                                },
                            )
                        },
                contentAlignment = Alignment.CenterEnd,
            ) {
                Button(
                    onClick = onSend,
                    enabled = isSubmitEnabled && !isSending,
                    modifier =
                        Modifier
                            .testTag(WriteKudoTestTags.SEND_BUTTON)
                            .semantics {
                                contentDescription = sendDescription
                                stateDescription = sendState
                            },
                ) {
                    if (isSending) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp,
                            modifier = Modifier.padding(end = 8.dp),
                        )
                    }
                    Text(stringResource(R.string.write_kudo_send))
                }
            }
        }
    }
}
