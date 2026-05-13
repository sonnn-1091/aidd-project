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
import androidx.compose.foundation.layout.fillMaxSize
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
 * Highlight carousel card — Figma `mms_B.3_KUDO - Highlight`
 * (`6885:8424` component, instance `6885:9092`).
 *
 * Specs:
 *  - Background `#FFF8E1` (light cream) — NOT the dark surface
 *  - 1dp #FFEA9E (SaaCream) border, 8dp radius
 *  - 8px top/bottom + 12px horizontal padding
 *  - Internal sections separated by 1dp SaaCream dividers
 *  - Body text is DARK on cream (10sp Montserrat for most copy,
 *    title is 10sp Bold)
 *  - Hashtags are red (#D4271D, 10sp Regular)
 *  - Action row has tiny pill buttons (24dp tall, 2dp radius)
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
                .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        SenderRecipientRow(
            kudos = kudos,
            onSenderTap = senderClick,
            onRecipientTap = recipientClick,
        )
        InternalDivider()
        ContentBlock(kudos = kudos, onHashtagTap = onHashtagChipTap)
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
            avatarRes = R.drawable.kudos_avatar_sender,
            departmentCode = departmentCodeFor(kudos.sender),
            starTier = kudos.sender.starTier,
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
            avatarRes = R.drawable.kudos_avatar_recipient,
            departmentCode = departmentCodeFor(kudos.recipient),
            starTier = kudos.recipient.starTier,
            onTap = onRecipientTap,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun ProfileBlock(
    name: String,
    @androidx.annotation.DrawableRes avatarRes: Int,
    departmentCode: String,
    starTier: Int,
    onTap: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val tappable = if (onTap != null) modifier.clickable(onClick = onTap) else modifier
    Column(
        modifier = tappable.padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Image(
            painter = painterResource(avatarRes),
            contentDescription = null,
            modifier =
                Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .border(width = 1.dp, color = Color.White, shape = CircleShape),
        )
        Text(
            text = name,
            color = CardDarkText,
            style =
                MaterialTheme.typography.bodySmall.copy(
                    fontSize = 10.sp,
                    lineHeight = 14.sp,
                    fontWeight = FontWeight.Normal,
                ),
            maxLines = 1,
        )
        // Stack dept code + tier badge vertically — the carousel
        // column width (~107dp per profile after the 32dp side peek
        // and middle arrow) can't fit "CECU01 · Rising Hero" on one
        // line, so the labels were overlapping. Vertical layout gives
        // each its own line.
        Text(
            text = departmentCode,
            color = CardMuted,
            style =
                MaterialTheme.typography.bodySmall.copy(
                    fontSize = 8.sp,
                    lineHeight = 10.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.05.sp,
                ),
        )
        if (starTier > 0) {
            TierBadge(tier = starTier)
        }
    }
}

@Composable
private fun TierBadge(tier: Int) {
    val label =
        when (tier) {
            1 -> "Rising Hero"
            2 -> "Champion"
            3 -> "Legend"
            else -> return
        }
    // Badge is enlarged to 64×14dp + 8sp label so the text breathes
    // inside the pill (Figma's 45×9 / 6sp was too cramped at Android
    // density and clipped descenders).
    Box(
        modifier = Modifier.size(width = 64.dp, height = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(R.drawable.kudos_tier_badge),
            contentDescription = null,
            contentScale = androidx.compose.ui.layout.ContentScale.FillBounds,
            modifier = Modifier.fillMaxSize(),
        )
        Text(
            text = label,
            color = Color.White,
            style =
                MaterialTheme.typography.bodySmall.copy(
                    fontSize = 8.sp,
                    lineHeight = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.05.sp,
                ),
            maxLines = 1,
        )
    }
}

/**
 * Demo department code derived from the user id (no API contract
 * yet for the per-user code). Format: `CEC` + last 2 chars of id
 * uppercased.
 */
private fun departmentCodeFor(sunner: com.example.aiddproject.kudos.domain.SunnerNode): String = "CEC" + sunner.id.takeLast(2).uppercase()

@Composable
private fun ContentBlock(
    kudos: Kudos,
    onHashtagTap: (hashtagId: String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = formatKudosTimestamp(kudos.createdAt),
            color = CardMuted,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Medium, letterSpacing = 0.23.sp),
        )
        if (kudos.title != null) {
            Text(
                text = kudos.title.uppercase(),
                color = CardDarkText,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.23.sp),
                modifier = Modifier.fillMaxWidth(),
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
                maxLines = 3,
            )
        }
        if (kudos.hashtags.isNotEmpty()) {
            Text(
                text = kudos.hashtags.joinToString(" ") { "#${it.tagName}" },
                color = CardRed,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp, letterSpacing = 0.23.sp),
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
                .clickable(onClick = onTap)
                .padding(horizontal = 4.dp, vertical = 4.dp)
                .testTag(KudosTestTags.HIGHLIGHT_CARD),
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

/**
 * Format the kudos creation ISO-8601 timestamp into Figma's
 * card-row format "HH:mm - dd/MM/yyyy" (e.g. "10:00 - 12/05/2026").
 * Falls back to the raw input if parsing fails so a bad payload
 * never crashes the card.
 */
internal fun formatKudosTimestamp(iso: String): String =
    runCatching {
        val instant = java.time.Instant.parse(iso)
        val zoned = instant.atZone(java.time.ZoneId.systemDefault())
        val time =
            zoned.format(
                java.time.format.DateTimeFormatter
                    .ofPattern("HH:mm"),
            )
        val date =
            zoned.format(
                java.time.format.DateTimeFormatter
                    .ofPattern("dd/MM/yyyy"),
            )
        "$time - $date"
    }.getOrDefault(iso)
