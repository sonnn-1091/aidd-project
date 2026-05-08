package com.example.aiddproject.home.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.aiddproject.R

/**
 * Notification bell with M3 [BadgedBox] showing a red dot when [unreadCount] > 0.
 * Bell tap is enabled in every notifications state (Loading / Loaded / Error)
 * so a transient API failure doesn't block opening the panel (spec edge case).
 */
@Composable
fun BellWithBadge(
    unreadCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val description =
        if (unreadCount > 0) {
            stringResource(R.string.a11y_home_bell_badge, unreadCount)
        } else {
            stringResource(R.string.a11y_home_bell_no_badge)
        }
    BadgedBox(
        modifier = modifier.semantics { contentDescription = description },
        badge = {
            if (unreadCount > 0) {
                Badge()
            }
        },
    ) {
        IconButton(onClick = onClick) {
            Icon(
                painter = painterResource(R.drawable.ic_bell),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(24.dp),
            )
        }
    }
}
