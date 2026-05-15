package com.example.aiddproject.kudos.notifications.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aiddproject.R
import com.example.aiddproject.core.ui.rememberSingleClickHandler
import com.example.aiddproject.kudos.notifications.ui.NotificationsTestTags

/**
 * "Đánh dấu đọc tất cả" action button in the TopAppBar.
 *
 * Per Q-N-10 the button is *always visible* — when there are no unread
 * notifications it renders dimmed and the VM short-circuits on tap. The
 * dimming is driven by [enabled] so the screen owns the unread-count
 * check rather than this component.
 */
@Composable
fun ReadAllButton(
    onClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    val click = rememberSingleClickHandler(onClick = onClick)
    val a11y = stringResource(R.string.a11y_notifications_mark_all_read)
    TextButton(
        onClick = { click() },
        enabled = enabled,
        modifier =
            modifier
                .padding(end = 4.dp)
                .testTag(NotificationsTestTags.MARK_ALL_READ_BUTTON)
                .semantics { contentDescription = a11y },
    ) {
        Text(
            text = stringResource(R.string.notifications_mark_all_read),
            color = if (enabled) Color.White else Color.White.copy(alpha = 0.4f),
            fontSize = 14.sp,
            lineHeight = 20.sp,
            fontWeight = FontWeight.Medium,
        )
    }
}
