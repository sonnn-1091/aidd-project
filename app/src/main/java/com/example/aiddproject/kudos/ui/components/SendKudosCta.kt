package com.example.aiddproject.kudos.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
 * Send Kudos pill — Figma `mms_A.1_Button ghi nhận` (`6885:9083`).
 *
 * Specs: 335×40dp, 1px #998C5F border, 10px padding, 10% SaaCream
 * fill, 4dp radius. 24×24dp airplane icon + 8dp gap + 14sp
 * Montserrat Medium white label. The Compose pill expands to
 * fillMaxWidth so the 335dp Figma width is preserved with 20dp
 * horizontal screen padding (=375-40).
 */
@Composable
fun SendKudosCta(
    onSendKudos: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val click = rememberSingleClickHandler { onSendKudos() }
    val a11y = stringResource(R.string.a11y_kudos_send_pill)
    // Outer touch target wrapper — keeps the 48dp Constitution III
    // minimum without forcing the pill itself to be taller than the
    // 40dp Figma spec.
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 4.dp)
                .heightIn(min = 48.dp)
                .semantics {
                    role = Role.Button
                    contentDescription = a11y
                }.clickable(onClick = click)
                .testTag(KudosTestTags.SEND_KUDOS_CTA),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(SaaCream.copy(alpha = 0.10f))
                    .border(width = 1.dp, color = PillBorderColor, shape = RoundedCornerShape(4.dp))
                    .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Send,
                contentDescription = null,
                tint = SaaCream,
                modifier = Modifier.size(24.dp),
            )
            Text(
                text = stringResource(R.string.kudos_send_placeholder),
                color = Color.White,
                style =
                    MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                    ),
            )
        }
    }
}

private val PillBorderColor: Color = Color(0xFF998C5F)
