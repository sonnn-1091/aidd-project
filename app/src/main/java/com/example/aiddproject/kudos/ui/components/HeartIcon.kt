package com.example.aiddproject.kudos.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
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

/**
 * Heart toggle (spec § US5).
 *
 * - liked + non-disabled: filled red icon, count rendered next to it.
 * - liked + disabled: grayed filled icon, no tap.
 * - unliked + non-disabled: outlined icon, count.
 * - unliked + disabled: grayed outlined icon, no tap.
 *
 * Disabled covers two cases (Q-K-5): viewer is the sender, OR viewer
 * is the recipient. The repository enforces this server-side via
 * `like_disabled_for_me`; the icon just renders the result.
 */
@Composable
fun HeartIcon(
    liked: Boolean,
    count: Int,
    disabled: Boolean,
    onTap: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tint =
        when {
            disabled -> Color.White.copy(alpha = 0.3f)
            liked -> Color(0xFFFF6B6B)
            else -> Color.White.copy(alpha = 0.7f)
        }
    val a11y =
        when {
            disabled -> stringResource(R.string.a11y_kudos_heart_disabled)
            liked -> stringResource(R.string.a11y_kudos_heart_liked, count)
            else -> stringResource(R.string.a11y_kudos_heart_unliked, count)
        }
    val click = rememberSingleClickHandler { onTap() }
    Row(
        modifier =
            modifier
                .heightIn(min = 32.dp)
                .clip(RoundedCornerShape(16.dp))
                .let { base -> if (disabled) base else base.clickable(onClick = click) }
                .padding(horizontal = 6.dp, vertical = 4.dp)
                .semantics {
                    role = Role.Button
                    contentDescription = a11y
                }.testTag(KudosTestTags.HEART_ICON),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(
            imageVector = if (liked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(18.dp),
        )
        Text(
            text = count.toString(),
            color = tint,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
        )
    }
}
