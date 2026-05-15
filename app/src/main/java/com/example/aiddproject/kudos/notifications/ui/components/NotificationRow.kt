@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.example.aiddproject.kudos.notifications.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aiddproject.R
import com.example.aiddproject.core.ui.rememberSingleClickHandler
import com.example.aiddproject.kudos.notifications.domain.NotificationItem
import com.example.aiddproject.kudos.notifications.domain.NotificationType
import com.example.aiddproject.kudos.notifications.ui.NotificationsTestTags
import kotlin.time.Clock
import kotlin.time.Instant

/**
 * A single notification list row.
 *
 *  - Type-keyed Material icon placeholder on the left (40dp circle).
 *    Phase-0 audit (T036) swaps these for Figma-exported drawables.
 *  - Body text (newest first) + relative-time below.
 *  - Inline "Tiêu chuẩn cộng đồng" link rendered only when
 *    `type == CONTENT_HIDDEN` (US3 / T040).
 *  - Red unread-dot indicator on the right when `!isRead`.
 *
 * Accessibility: the entire row is a single focusable button; the
 * inline link is a *separate* focusable button whose pointer-input
 * consumes the tap before bubbling to the parent. The merged
 * `contentDescription` reads "{body}, {time}, {read|unread}".
 */
@Composable
fun NotificationRow(
    item: NotificationItem,
    onRowTap: () -> Unit,
    onInlineCommunityStandardsTap: () -> Unit,
    modifier: Modifier = Modifier,
    now: Instant = Clock.System.now(),
) {
    val resources = LocalContext.current.resources
    val timeLabel = resources.formatRelativeTime(item.createdAt, now)
    val statusLabel =
        stringResource(
            if (item.isRead) R.string.a11y_notifications_read else R.string.a11y_notifications_unread,
        )
    val rowA11y =
        stringResource(
            R.string.a11y_notifications_row,
            item.displayBody,
            timeLabel,
            statusLabel,
        )
    val rowClick = rememberSingleClickHandler(onClick = onRowTap)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier =
            modifier
                .fillMaxWidth()
                .clickable(role = Role.Button) { rowClick() }
                .padding(horizontal = 20.dp, vertical = 14.dp)
                .testTag(NotificationsTestTags.rowTag(item.id))
                .semantics { contentDescription = rowA11y },
    ) {
        TypeIcon(item.type)
        Spacer(Modifier.width(12.dp))
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = item.displayBody,
                color = Color.White,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                fontWeight = if (item.isRead) FontWeight.Normal else FontWeight.Medium,
            )
            Text(
                text = timeLabel,
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 12.sp,
                lineHeight = 16.sp,
                fontWeight = FontWeight.Normal,
            )
            if (item.type == NotificationType.CONTENT_HIDDEN) {
                TextButton(
                    onClick = onInlineCommunityStandardsTap,
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
                    modifier =
                        Modifier
                            .height(28.dp)
                            .testTag(NotificationsTestTags.inlineCommunityStandardsTag(item.id)),
                ) {
                    Text(
                        text = stringResource(R.string.notifications_inline_community_standards),
                        color = InlineLinkColor,
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }
        Spacer(Modifier.width(8.dp))
        if (!item.isRead) {
            UnreadDot(notificationId = item.id)
        } else {
            // Reserve the dot space so rows align consistently.
            Spacer(Modifier.size(8.dp))
        }
    }
}

@Composable
private fun TypeIcon(type: NotificationType) {
    val (icon, tint) = iconAndTintFor(type)
    Box(
        modifier =
            Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(tint.copy(alpha = 0.18f)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(20.dp),
        )
    }
}

@Composable
private fun iconAndTintFor(type: NotificationType): Pair<ImageVector, Color> =
    when (type) {
        NotificationType.KUDOS_RECEIVED -> Icons.Filled.CardGiftcard to MaterialTheme.colorScheme.primary
        NotificationType.HEART_RECEIVED -> Icons.Filled.Favorite to Color(0xFFE6705A)
        NotificationType.SECRET_BOX_UNLOCK -> Icons.Filled.Inventory2 to Color(0xFFE0BE5C)
        NotificationType.LEVEL_UP -> Icons.Filled.TrendingUp to Color(0xFF6CB07B)
        NotificationType.CONTENT_HIDDEN -> Icons.Filled.WarningAmber to Color(0xFFD17A4E)
        NotificationType.BADGE_COLLECTED -> Icons.Filled.EmojiEvents to Color(0xFFE6B23A)
        NotificationType.REVIEW_REQUEST -> Icons.Filled.RateReview to Color(0xFF7CA5D1)
    }

@Composable
private fun UnreadDot(notificationId: String) {
    Box(
        modifier =
            Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(UnreadDotColor)
                .testTag(NotificationsTestTags.unreadDotTag(notificationId)),
    )
}

private val InlineLinkColor: Color = Color(0xFFFFD27A)
private val UnreadDotColor: Color = Color(0xFFE6705A)
