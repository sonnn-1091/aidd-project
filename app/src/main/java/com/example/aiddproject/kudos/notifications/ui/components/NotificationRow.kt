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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.CardGiftcard
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.MarkEmailUnread
import androidx.compose.material.icons.outlined.NorthEast
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
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
 * A single notification row matching Figma `mms_B.1_Noti` /
 * componentSet `6885:8819`.
 *
 * Layout (335dp card width, 8dp inner padding, 16dp gap between icon
 * and content):
 *  - 24×24 type-keyed outlined icon at the top-left, stroke-colored
 *    per [iconAndTintFor] — no circle background (Figma renders the
 *    icon glyph directly on the card surface).
 *  - Body text 14sp Montserrat / weight 700 for unread, 400 for read,
 *    white, lineHeight 20.
 *  - Timestamp 12sp Montserrat 400, color #999999, lineHeight 16.
 *  - For `CONTENT_HIDDEN` rows: inline "Tiêu chuẩn cộng đồng" link
 *    with NE-arrow icon rendered between body + timestamp. Its own
 *    `clickable` consumes the gesture before bubbling to the row.
 *  - For unread rows: an 8×8 red dot (#D4271D) anchored at top-right
 *    of the row content area (Figma `mms_B.1.3_Group 425`).
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

    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .clickable(role = Role.Button) { rowClick() }
                .testTag(NotificationsTestTags.rowTag(item.id))
                .semantics { contentDescription = rowA11y },
    ) {
        Row(
            verticalAlignment = Alignment.Top,
            modifier = Modifier.padding(8.dp),
        ) {
            TypeIcon(item.type)
            Spacer(Modifier.width(16.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = bodyText(item),
                    color = Color.White,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                )
                if (item.type == NotificationType.CONTENT_HIDDEN) {
                    InlineCommunityStandardsLink(
                        notificationId = item.id,
                        onClick = onInlineCommunityStandardsTap,
                    )
                }
                Text(
                    text = timeLabel,
                    color = TimestampColor,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    fontWeight = FontWeight.Normal,
                )
            }
        }
        if (!item.isRead) {
            Box(
                modifier =
                    Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 8.dp, end = 8.dp)
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(UnreadDotColor)
                        .testTag(NotificationsTestTags.unreadDotTag(item.id)),
            )
        }
    }
}

/** Bold body for unread rows; regular weight for read rows. */
private fun bodyText(item: NotificationItem) =
    buildAnnotatedString {
        withStyle(
            SpanStyle(
                fontWeight = if (item.isRead) FontWeight.Normal else FontWeight.Bold,
            ),
        ) {
            append(item.displayBody)
        }
    }

@Composable
private fun TypeIcon(type: NotificationType) {
    val (icon, tint) = iconAndTintFor(type)
    Icon(
        imageVector = icon,
        contentDescription = null,
        tint = tint,
        modifier = Modifier.size(24.dp),
    )
}

/**
 * Material outlined approximations of the 7 Figma icon nodes
 * (`6885:8273`…`6885:8313`). Stroke colors are sampled from the
 * Figma frame.
 */
@Composable
private fun iconAndTintFor(type: NotificationType): Pair<ImageVector, Color> =
    when (type) {
        NotificationType.KUDOS_RECEIVED -> Icons.Outlined.MarkEmailUnread to Color(0xFF33CDDC)
        NotificationType.HEART_RECEIVED -> Icons.Outlined.FavoriteBorder to Color(0xFFE6705A)
        NotificationType.SECRET_BOX_UNLOCK -> Icons.Outlined.CardGiftcard to Color(0xFF5BC178)
        NotificationType.LEVEL_UP -> Icons.Outlined.AutoAwesome to Color(0xFF34D0DC)
        NotificationType.CONTENT_HIDDEN -> Icons.Outlined.WarningAmber to Color(0xFFE5C03A)
        NotificationType.BADGE_COLLECTED -> Icons.AutoMirrored.Outlined.Assignment to Color(0xFF5BC178)
        NotificationType.REVIEW_REQUEST -> Icons.Outlined.Flag to Color(0xFFD74877)
    }

@Composable
private fun InlineCommunityStandardsLink(
    notificationId: String,
    onClick: () -> Unit,
) {
    val click = rememberSingleClickHandler(onClick = onClick)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier =
            Modifier
                .clickable(role = Role.Button) { click() }
                .padding(end = 16.dp, top = 8.dp, bottom = 8.dp)
                .testTag(NotificationsTestTags.inlineCommunityStandardsTag(notificationId)),
    ) {
        Text(
            text = stringResource(R.string.notifications_inline_community_standards),
            color = Color.White,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            fontWeight = FontWeight.Medium,
            textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline,
        )
        Icon(
            imageVector = Icons.Outlined.NorthEast,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(16.dp),
        )
    }
}

private val TimestampColor: Color = Color(0xFF999999)
private val UnreadDotColor: Color = Color(0xFFD4271D)
