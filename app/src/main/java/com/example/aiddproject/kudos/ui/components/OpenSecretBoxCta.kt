package com.example.aiddproject.kudos.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
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
 * Open Secret Box CTA stub (Figma `D.2`).
 *
 * Phase 3 MVP renders the button + click callback. Disabled state
 * (`hasUnopenedBox = false`) + actual navigation to
 * `Routes.SECRET_BOX_OPEN` land in Phase 11 (US11).
 */
@Composable
fun OpenSecretBoxCta(
    hasUnopenedBox: Boolean,
    onOpenSecretBox: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val click = rememberSingleClickHandler { onOpenSecretBox() }
    val a11y = stringResource(R.string.a11y_kudos_secret_box_open)
    val alpha = if (hasUnopenedBox) 1f else 0.4f
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .heightIn(min = 48.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(SaaCream.copy(alpha = 0.20f * alpha))
                .let { base -> if (hasUnopenedBox) base.clickable(onClick = click) else base }
                .semantics {
                    role = Role.Button
                    contentDescription = a11y
                }.padding(horizontal = 16.dp, vertical = 12.dp)
                .testTag(KudosTestTags.OPEN_SECRET_BOX_CTA),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(
            imageVector = Icons.Filled.CardGiftcard,
            contentDescription = null,
            tint = SaaCream.copy(alpha = alpha),
        )
        Text(
            text = stringResource(R.string.kudos_secret_box_open),
            color = Color.White.copy(alpha = alpha),
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
        )
    }
}
