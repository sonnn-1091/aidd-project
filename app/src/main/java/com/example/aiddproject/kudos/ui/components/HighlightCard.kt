package com.example.aiddproject.kudos.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aiddproject.R
import com.example.aiddproject.core.ui.rememberSingleClickHandler
import com.example.aiddproject.kudos.domain.Kudos
import com.example.aiddproject.ui.theme.SaaCream

/**
 * Single Highlight-carousel card (spec § US4, US5, US7, US8, US13).
 *
 * Composition (top → bottom):
 *  - Sender → recipient identity row (with star-tier on the
 *    recipient + anonymous-aware sender label per Q-K-3)
 *  - Title (optional) + message body (3-line truncate)
 *  - Hashtag chip row
 *  - Action row: HeartIcon + Copy Link + Xem chi tiết
 *
 * Phase 6 wires the layout. Heart toggle + Copy Link + chip taps +
 * profile nav fan in via callbacks; the actual side effects land in
 * Phases 7/9.
 */
@Composable
fun HighlightCard(
    kudos: Kudos,
    onHeartTap: (kudosId: String) -> Unit,
    onCopyLink: (kudosId: String) -> Unit,
    onCardTap: (Kudos) -> Unit,
    onHashtagChipTap: (hashtagId: String) -> Unit,
    onProfileTap: (userId: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val cardClick = rememberSingleClickHandler { onCardTap(kudos) }
    val copyClick = rememberSingleClickHandler { onCopyLink(kudos.id) }
    val senderClick =
        if (kudos.senderVisibleToMe) {
            rememberSingleClickHandler { onProfileTap(kudos.sender.id) }
        } else {
            null
        }
    val recipientClick = rememberSingleClickHandler { onProfileTap(kudos.recipient.id) }

    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .heightIn(min = 240.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White.copy(alpha = 0.08f))
                .clickable(onClick = cardClick)
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // Sender + recipient row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            val senderName =
                if (kudos.senderVisibleToMe) {
                    kudos.sender.fullName
                } else {
                    kudos.anonymousNickname ?: stringResource(R.string.kudos_anonymous_nickname_fallback)
                }
            AvatarPill(
                name = senderName,
                onTap = senderClick,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = "→",
                color = SaaCream,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 16.sp, fontWeight = FontWeight.Bold),
            )
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AvatarPill(
                    name = kudos.recipient.fullName,
                    onTap = recipientClick,
                    modifier = Modifier.weight(1f, fill = false),
                )
                if (kudos.recipient.starTier > 0) {
                    StarTierBadge(tier = kudos.recipient.starTier)
                }
            }
        }

        if (kudos.title != null) {
            Text(
                text = kudos.title,
                color = SaaCream,
                style = MaterialTheme.typography.titleSmall.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold),
            )
        }
        Text(
            text = kudos.message,
            color = Color.White.copy(alpha = 0.85f),
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, lineHeight = 16.sp),
            maxLines = 3,
        )

        if (kudos.hashtags.isNotEmpty()) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                kudos.hashtags.take(3).forEach { hashtag ->
                    HashtagChip(
                        tagName = hashtag.tagName,
                        onTap = { onHashtagChipTap(hashtag.id) },
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            HeartIcon(
                liked = kudos.likedByCurrentUser,
                count = kudos.heartCount,
                disabled = kudos.likeDisabledForMe,
                onTap = { onHeartTap(kudos.id) },
            )
            Row(
                modifier =
                    Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .clickable(onClick = copyClick)
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.ContentCopy,
                    contentDescription = stringResource(R.string.a11y_kudos_copy_link),
                    tint = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.size(16.dp),
                )
            }
            Box(modifier = Modifier.weight(1f))
            Text(
                text = stringResource(R.string.kudos_card_view_detail_label),
                color = SaaCream,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, fontWeight = FontWeight.SemiBold),
                modifier =
                    Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .clickable(onClick = cardClick)
                        .padding(horizontal = 8.dp, vertical = 6.dp),
            )
        }
    }
}

@Composable
private fun AvatarPill(
    name: String,
    onTap: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val baseModifier =
        modifier
            .clip(RoundedCornerShape(12.dp))
            .padding(horizontal = 4.dp, vertical = 4.dp)
    val tappable = if (onTap != null) baseModifier.clickable(onClick = onTap) else baseModifier
    Row(
        modifier = tappable,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            modifier =
                Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(SaaCream.copy(alpha = 0.3f)),
        )
        Text(
            text = name,
            color = Color.White,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, fontWeight = FontWeight.SemiBold),
            maxLines = 1,
        )
    }
}

@Composable
private fun HashtagChip(
    tagName: String,
    onTap: () -> Unit,
) {
    val click = rememberSingleClickHandler { onTap() }
    Text(
        text = "#$tagName",
        color = SaaCream,
        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
        modifier =
            Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(SaaCream.copy(alpha = 0.10f))
                .clickable(onClick = click)
                .padding(horizontal = 8.dp, vertical = 4.dp),
    )
}
