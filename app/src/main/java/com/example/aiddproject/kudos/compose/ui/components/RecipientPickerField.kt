package com.example.aiddproject.kudos.compose.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.aiddproject.R
import com.example.aiddproject.kudos.compose.ui.WriteKudoTestTags

/**
 * B.1 + B.2 — "Người nhận *" label + dropdown trigger button (Figma
 * `6885:9294` + `6885:9297`).
 *
 * The trigger shows either the chosen recipient's name or the
 * "Tìm kiếm" placeholder + a chevron. Tap opens the recipient picker
 * overlay sub-flow (`5MU728Tjck`); the overlay itself is rendered by
 * [RecipientPickerOverlay] and gated by the parent's state.
 */
@Composable
fun RecipientPickerField(
    recipientName: String?,
    onOpenPicker: () -> Unit,
    @StringRes errorRes: Int?,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = "${stringResource(R.string.write_kudo_recipient_label)} ${stringResource(R.string.write_kudo_required_marker)}",
            style = MaterialTheme.typography.labelMedium,
        )
        OutlinedCard(
            onClick = onOpenPicker,
            enabled = enabled,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .testTag(WriteKudoTestTags.RECIPIENT_FIELD),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
            ) {
                Text(
                    text = recipientName ?: stringResource(R.string.write_kudo_recipient_placeholder),
                    style = MaterialTheme.typography.bodyMedium,
                    color =
                        if (recipientName == null) {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                    modifier = Modifier.weight(1f),
                )
                Icon(
                    imageVector = Icons.Filled.KeyboardArrowDown,
                    contentDescription = null,
                )
            }
        }
        if (errorRes != null) {
            Text(
                text = stringResource(errorRes),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }
    }
}
