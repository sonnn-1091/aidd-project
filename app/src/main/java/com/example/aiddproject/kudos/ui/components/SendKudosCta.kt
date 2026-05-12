package com.example.aiddproject.kudos.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aiddproject.R
import com.example.aiddproject.core.ui.rememberSingleClickHandler
import com.example.aiddproject.kudos.ui.KudosTestTags
import com.example.aiddproject.ui.theme.SaaCream

/**
 * Send Kudos pill — Figma `A.1` row sitting between the hero and the
 * Highlight section. Phase 3 wires the click to a callback only;
 * navigation lands in Phase 8 (US6).
 */
@Composable
fun SendKudosCta(
    onSendKudos: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val click = rememberSingleClickHandler { onSendKudos() }
    val a11y = stringResource(R.string.a11y_kudos_send_pill)
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .heightIn(min = 48.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(SaaCream.copy(alpha = 0.10f))
                .border(width = 1.dp, color = SaaCream.copy(alpha = 0.4f), shape = RoundedCornerShape(24.dp))
                .clickable(onClick = click)
                .semantics {
                    role = Role.Button
                    contentDescription = a11y
                }.padding(horizontal = 16.dp, vertical = 12.dp)
                .testTag(KudosTestTags.SEND_KUDOS_CTA),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.Send,
            contentDescription = null,
            tint = SaaCream,
        )
        Text(
            text = stringResource(R.string.kudos_send_placeholder),
            color = Color.White,
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
        )
    }
}
