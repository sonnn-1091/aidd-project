package com.example.aiddproject.kudos.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aiddproject.R
import com.example.aiddproject.core.ui.rememberSingleClickHandler
import com.example.aiddproject.kudos.domain.GiftRecipient
import com.example.aiddproject.kudos.domain.states.TopTenState
import com.example.aiddproject.kudos.ui.KudosTestTags
import com.example.aiddproject.ui.theme.SaaCream

/**
 * Top 10 latest gift recipients — Figma `mms_D.3_10 SUNNER nhận quà`
 * (`6885:9255`).
 *
 * Single dark container (#00070C + 0.794dp #998C5F border, 8dp radius,
 * 12dp padding) with the title centered at top in SaaCream + rows
 * stacked vertically below. Each row:
 *  - 32×32dp circular avatar (1.483dp white border).
 *  - Text column: Name 14sp Bold SaaCream + reward 12sp Regular white
 *    right-aligned.
 */
@Composable
fun TopTenRecipients(
    state: TopTenState,
    onProfileTap: (userId: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .testTag(KudosTestTags.TOP_TEN),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(ContainerColor)
                    .border(width = 1.dp, color = BorderColor, shape = RoundedCornerShape(8.dp))
                    .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(R.string.kudos_top_ten_title),
                color = SaaCream,
                style =
                    MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            when (state) {
                TopTenState.Loading -> SectionPlaceholder(text = stringResource(R.string.kudos_loading))
                TopTenState.Empty -> SectionPlaceholder(text = stringResource(R.string.kudos_section_empty_generic))
                is TopTenState.Error -> SectionPlaceholder(text = stringResource(state.messageRes))
                is TopTenState.Loaded -> {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        state.items.forEachIndexed { index, recipient ->
                            RecipientRow(
                                recipient = recipient,
                                avatarRes = avatarForIndex(index),
                                onProfileTap = onProfileTap,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RecipientRow(
    recipient: GiftRecipient,
    @androidx.annotation.DrawableRes avatarRes: Int,
    onProfileTap: (userId: String) -> Unit,
) {
    val click = rememberSingleClickHandler { onProfileTap(recipient.userId) }
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .clickable(onClick = click),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Image(
            painter = painterResource(avatarRes),
            contentDescription = null,
            modifier =
                Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .border(width = 1.dp, color = Color.White, shape = CircleShape),
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = recipient.fullName,
                color = SaaCream,
                style =
                    MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                maxLines = 1,
            )
            Text(
                text = stringResource(R.string.kudos_top_ten_reward_prefix, recipient.rewardName),
                color = Color.White,
                style =
                    MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        fontWeight = FontWeight.Normal,
                    ),
                maxLines = 1,
            )
        }
    }
}

/**
 * Alternate the two Figma sample portraits across rows so the list
 * has visible avatar variation in demo mode. Production should
 * replace this with `recipient.avatarUrl` rendered via Coil.
 */
@androidx.annotation.DrawableRes
private fun avatarForIndex(index: Int): Int =
    if (index % 2 == 0) {
        R.drawable.kudos_avatar_sender
    } else {
        R.drawable.kudos_avatar_recipient
    }

private val ContainerColor: Color = Color(0xFF00070C)
private val BorderColor: Color = Color(0xFF998C5F)
