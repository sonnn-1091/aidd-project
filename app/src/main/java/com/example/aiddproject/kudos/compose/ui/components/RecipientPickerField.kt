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
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
 * The trigger box now hosts the M3 `DropdownMenu`-backed picker
 * overlay as a child so the dropdown anchors directly beneath the
 * trigger (matches the hub's HashtagFilterDropdown chrome) — the
 * old AlertDialog modal is gone.
 */
@Composable
fun RecipientPickerField(
    recipientName: String?,
    pickerState: RecipientPickerState,
    onOpenPicker: () -> Unit,
    onDismissPicker: () -> Unit,
    onQueryChange: (String) -> Unit,
    onPick: (SunnerNode) -> Unit,
    onRetry: () -> Unit,
    @StringRes errorRes: Int?,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(2.dp),
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
            // Anchor box — DropdownMenu auto-positions below this Box.
            Box(modifier = Modifier.width(FormFieldTokens.InputColumnWidth)) {
                Box(
                    modifier =
                        Modifier
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
                if (pickerState is RecipientPickerState.Open) {
                    RecipientPickerOverlay(
                        state = pickerState,
                        onQueryChange = onQueryChange,
                        onPick = onPick,
                        onDismiss = onDismissPicker,
                        onRetry = onRetry,
                    )
                }
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
