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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aiddproject.R
import com.example.aiddproject.core.ui.rememberSingleClickHandler
import com.example.aiddproject.kudos.domain.Kudos
import com.example.aiddproject.kudos.ui.KudosTestTags
import com.example.aiddproject.ui.theme.SaaCream

/**
 * Single All Kudos feed card (spec § US1, US5, US7, US8, US13).
 *
 * Differs from HighlightCard: 5-line message truncation, denser
 * layout, no carousel concerns. Same callback set so the screen can
 * wire one handler per action.
 */
@Composable
fun KudosFeedCard(
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
                .heightIn(min = 140.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White.copy(alpha = 0.06f))
                .clickable(onClick = cardClick)
                .padding(12.dp)
                .testTag("${KudosTestTags.FEED_CARD}_${kudos.id}"),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            val senderName =
                if (kudos.senderVisibleToMe) {
                    kudos.sender.fullName
                } else {
                    kudos.anonymousNickname ?: stringResource(R.string.kudos_anonymous_nickname_fallback)
                }
            AvatarPill(name = senderName, onTap = senderClick, modifier = Modifier.weight(1f))
            Text(
                text = "→",
                color = SaaCream,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
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
        Text(
            text = kudos.message,
            color = Color.White.copy(alpha = 0.85f),
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, lineHeight = 16.sp),
            maxLines = 5,
        )
        if (kudos.hashtags.isNotEmpty()) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
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
            horizontalArrangement = Arrangement.spacedBy(8.dp),
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
            ) {
                Icon(
                    imageVector = Icons.Filled.ContentCopy,
                    contentDescription = stringResource(R.string.a11y_kudos_copy_link),
                    tint = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.size(14.dp),
                )
            }
            Box(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun AvatarPill(
    name: String,
    onTap: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val base = modifier.clip(RoundedCornerShape(10.dp)).padding(vertical = 2.dp)
    val tappable = if (onTap != null) base.clickable(onClick = onTap) else base
    Row(
        modifier = tappable,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Box(
            modifier =
                Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(SaaCream.copy(alpha = 0.3f)),
        )
        Text(
            text = name,
            color = Color.White,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, fontWeight = FontWeight.SemiBold),
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
        style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
        modifier =
            Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(SaaCream.copy(alpha = 0.10f))
                .clickable(onClick = click)
                .padding(horizontal = 6.dp, vertical = 3.dp),
    )
}
