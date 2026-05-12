package com.example.aiddproject.kudos.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aiddproject.R
import com.example.aiddproject.core.ui.rememberSingleClickHandler
import com.example.aiddproject.kudos.ui.KudosTestTags
import com.example.aiddproject.ui.theme.SaaCream

/**
 * Open Secret Box CTA — Figma `Button` (`6885:9254`).
 *
 * 40dp tall, full-width, SaaCream filled with 4dp radius. Label is
 * 14sp Montserrat Medium dark (#00101A) + 24dp gift-box icon on the
 * right. Disabled state dims to 40% alpha and suppresses the tap.
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
                .heightIn(min = 48.dp)
                .padding(top = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(SaaCream.copy(alpha = alpha))
                    .let { base -> if (hasUnopenedBox) base.clickable(onClick = click) else base }
                    .semantics {
                        role = Role.Button
                        contentDescription = a11y
                    }.padding(horizontal = 12.dp)
                    .testTag(KudosTestTags.OPEN_SECRET_BOX_CTA),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Text(
                text = stringResource(R.string.kudos_secret_box_open),
                color = ButtonDarkText.copy(alpha = alpha),
                style =
                    MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        fontWeight = FontWeight.Medium,
                    ),
            )
            Icon(
                imageVector = Icons.Filled.CardGiftcard,
                contentDescription = null,
                tint = ButtonDarkText.copy(alpha = alpha),
                modifier =
                    Modifier
                        .padding(start = 8.dp)
                        .size(24.dp),
            )
        }
    }
}

private val ButtonDarkText: Color = Color(0xFF00101A)
