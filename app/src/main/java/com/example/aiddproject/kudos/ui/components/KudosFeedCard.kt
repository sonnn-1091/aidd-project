package com.example.aiddproject.kudos.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
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
 * All Kudos feed card — same Figma component as HighlightCard
 * (`mms_B.3_KUDO`) but used in the vertical feed below the carousel.
 *
 * Differences from HighlightCard:
 *  - Full content width — sender/recipient row stays at 62dp tall
 *  - 5-line message truncation (vs 3 in the carousel)
 *  - Slightly tighter vertical rhythm
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
    val detailClick = rememberSingleClickHandler { onCardTap(kudos) }
    val heartClick = rememberSingleClickHandler { onHeartTap(kudos.id) }
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
                .clip(RoundedCornerShape(8.dp))
                .background(CardSurface)
                .border(width = 1.dp, color = SaaCream, shape = RoundedCornerShape(8.dp))
                .clickable(onClick = cardClick)
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .testTag("${KudosTestTags.FEED_CARD}_${kudos.id}"),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        SenderRecipientRow(
            kudos = kudos,
            onSenderTap = senderClick,
            onRecipientTap = recipientClick,
        )
        InternalDivider()
        ContentBlock(kudos = kudos, onHashtagTap = onHashtagChipTap, maxLines = 5)
        InternalDivider()
        ActionRow(
            kudos = kudos,
            onHeartTap = heartClick,
            onCopyLink = copyClick,
            onViewDetail = detailClick,
        )
    }
}

@Composable
private fun SenderRecipientRow(
    kudos: Kudos,
    onSenderTap: (() -> Unit)?,
    onRecipientTap: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().heightIn(min = 62.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        val senderName =
            if (kudos.senderVisibleToMe) {
                kudos.sender.fullName
            } else {
                kudos.anonymousNickname ?: stringResource(R.string.kudos_anonymous_nickname_fallback)
            }
        ProfileBlock(
            name = senderName,
            onTap = onSenderTap,
            modifier = Modifier.weight(1f),
        )
        Image(
            painter = painterResource(R.drawable.ic_kudos_card_arrow),
            contentDescription = null,
            modifier = Modifier.size(16.dp),
        )
        ProfileBlock(
            name = kudos.recipient.fullName,
            onTap = onRecipientTap,
            starTier = kudos.recipient.starTier,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun ProfileBlock(
    name: String,
    onTap: (() -> Unit)?,
    modifier: Modifier = Modifier,
    starTier: Int = 0,
) {
    val tappable = if (onTap != null) modifier.clickable(onClick = onTap) else modifier
    Column(
        modifier = tappable.padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Box(
            modifier =
                Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(SaaCream),
        )
        Text(
            text = name,
            color = CardDarkText,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, fontWeight = FontWeight.SemiBold),
            maxLines = 1,
        )
        if (starTier > 0) {
            StarTierBadge(tier = starTier)
        }
    }
}

@Composable
private fun ContentBlock(
    kudos: Kudos,
    onHashtagTap: (hashtagId: String) -> Unit,
    maxLines: Int,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = kudos.createdAt,
            color = CardMuted,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Medium),
        )
        if (kudos.title != null) {
            Text(
                text = kudos.title.uppercase(),
                color = CardDarkText,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.23.sp),
            )
        }
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(5.5.dp))
                    .background(SaaCream.copy(alpha = 0.40f))
                    .border(width = 0.5.dp, color = SaaCream, shape = RoundedCornerShape(5.5.dp))
                    .padding(4.dp),
        ) {
            Text(
                text = kudos.message,
                color = CardDarkText,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp, lineHeight = 12.sp),
                maxLines = maxLines,
            )
        }
        if (kudos.hashtags.isNotEmpty()) {
            Text(
                text = kudos.hashtags.joinToString(" ") { "#${it.tagName}" },
                color = CardRed,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clickable {
                            val first = kudos.hashtags.firstOrNull() ?: return@clickable
                            onHashtagTap(first.id)
                        },
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun ActionRow(
    kudos: Kudos,
    onHeartTap: () -> Unit,
    onCopyLink: () -> Unit,
    onViewDetail: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().heightIn(min = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            modifier =
                Modifier
                    .clickable(enabled = !kudos.likeDisabledForMe, onClick = onHeartTap)
                    .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = kudos.heartCount.toString(),
                color = CardDarkText,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
            )
            Image(
                painter = painterResource(R.drawable.ic_kudos_heart),
                contentDescription = null,
                modifier = Modifier.size(16.dp),
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        TinyPillButton(
            label = stringResource(R.string.a11y_kudos_copy_link),
            iconRes = R.drawable.ic_kudos_copy,
            onTap = onCopyLink,
        )
        Spacer(modifier = Modifier.size(4.dp))
        TinyPillButton(
            label = stringResource(R.string.kudos_card_view_detail_label),
            iconRes = R.drawable.ic_kudos_view_detail_arrow,
            onTap = onViewDetail,
        )
    }
}

@Composable
private fun TinyPillButton(
    label: String,
    @androidx.annotation.DrawableRes iconRes: Int,
    onTap: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .clip(RoundedCornerShape(2.dp))
                .background(CardDarkText.copy(alpha = 0.06f))
                .clickable(onClick = onTap)
                .padding(horizontal = 6.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = label,
            color = CardDarkText,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
            maxLines = 1,
        )
        Image(
            painter = painterResource(iconRes),
            contentDescription = null,
            modifier = Modifier.size(12.dp),
        )
    }
}

@Composable
private fun InternalDivider() {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(SaaCream),
    )
}

private val CardSurface: Color = Color(0xFFFFF8E1)
private val CardDarkText: Color = Color(0xFF00101A)
private val CardMuted: Color = Color(0xFF999999)
private val CardRed: Color = Color(0xFFD4271D)
