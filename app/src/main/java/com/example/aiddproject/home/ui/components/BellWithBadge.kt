package com.example.aiddproject.home.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.aiddproject.R
import com.example.aiddproject.core.ui.rememberSingleClickHandler

/**
 * Notification bell with M3 [BadgedBox] showing a red dot when [unreadCount] > 0.
 * Bell tap is enabled in every notifications state (Loading / Loaded / Error)
 * so a transient API failure doesn't block opening the panel (spec edge case).
 *
 * The click is wrapped in [rememberSingleClickHandler] so a finger-bounce
 * double-tap can never push two sheet invocations on the back stack (TR-005).
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
    val bellClick = rememberSingleClickHandler(onClick = onClick)
    BadgedBox(
        modifier =
            modifier
                .testTag(TEST_TAG_HOME_BELL)
                .semantics { contentDescription = description },
        badge = {
            if (unreadCount > 0) {
                Badge(modifier = Modifier.testTag(TEST_TAG_HOME_BELL_BADGE))
            }
        },
    ) {
        IconButton(onClick = bellClick) {
            Icon(
                painter = painterResource(R.drawable.ic_bell),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(24.dp),
            )
        }
    }
}

const val TEST_TAG_HOME_BELL: String = "home_bell"
const val TEST_TAG_HOME_BELL_BADGE: String = "home_bell_badge"
