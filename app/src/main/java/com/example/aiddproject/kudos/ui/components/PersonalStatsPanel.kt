package com.example.aiddproject.kudos.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
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
import com.example.aiddproject.kudos.domain.PersonalStats
import com.example.aiddproject.kudos.domain.states.PersonalStatsState
import com.example.aiddproject.kudos.ui.KudosTestTags
import com.example.aiddproject.ui.theme.SaaCream

/**
 * Personal stats panel — Figma `mms_D.1_Thống kê tổng quat`
 * (`6885:9223`).
 *
 * Single dark container card with 1dp SaaCream border, 8dp radius,
 * 12dp padding. Five stat rows (label left, value right-aligned)
 * with a 1dp divider before the secret-box rows. The x2 fire badge
 * appears inline on the Hearts row when [x2BonusActive].
 *
 * The "Mở Secret Box" button at the bottom is part of this same
 * container per Figma.
 */
@Composable
fun PersonalStatsPanel(
    state: PersonalStatsState,
    x2BonusActive: Boolean,
    hasUnopenedBox: Boolean,
    onOpenSecretBox: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .testTag(KudosTestTags.STATS),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(StatsContainer)
                    .border(width = 1.dp, color = ContainerBorderColor, shape = RoundedCornerShape(8.dp))
                    .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            when (state) {
                PersonalStatsState.Loading -> SectionPlaceholder(text = stringResource(R.string.kudos_loading))
                is PersonalStatsState.Error -> SectionPlaceholder(text = stringResource(state.messageRes))
                is PersonalStatsState.Loaded -> StatsRows(state.stats, x2BonusActive)
            }
            OpenSecretBoxCta(
                hasUnopenedBox = hasUnopenedBox,
                onOpenSecretBox = onOpenSecretBox,
            )
        }
    }
}

@Composable
private fun StatsRows(
    stats: PersonalStats,
    x2BonusActive: Boolean,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        StatRow(label = stringResource(R.string.kudos_stats_received), value = stats.kudosReceived)
        StatRow(label = stringResource(R.string.kudos_stats_sent), value = stats.kudosSent)
        StatRow(
            label = stringResource(R.string.kudos_stats_hearts),
            value = stats.heartsReceived,
            inlineBadge = if (x2BonusActive) ({ X2Badge() }) else null,
        )
        StatDivider()
        StatRow(label = stringResource(R.string.kudos_stats_boxes_opened), value = stats.secretBoxesOpened)
        StatRow(label = stringResource(R.string.kudos_stats_boxes_unopened), value = stats.secretBoxesUnopened)
    }
}

@Composable
private fun StatRow(
    label: String,
    value: Int,
    inlineBadge: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = Modifier.fillMaxWidth().heightIn(min = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "$label:",
            color = Color.White,
            style =
                MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    fontWeight = FontWeight.Light,
                    letterSpacing = 0.25.sp,
                ),
            modifier = Modifier.weight(1f),
        )
        if (inlineBadge != null) {
            inlineBadge()
        }
        Text(
            text = value.toString(),
            color = SaaCream,
            style =
                MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.25.sp,
                ),
        )
    }
}

@Composable
private fun StatDivider() {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(DividerColor),
    )
}

/**
 * x2 fire badge — Figma `mms_S_Group 435` (`6885:9239`). The
 * source asset is a textured fire glyph with the "x2" label baked
 * over it; we replicate the visual with a tinted Material fire
 * icon as the backdrop + a centered bold "x2" Text with a dark
 * outline so the white label still reads against the orange flame.
 */
@Composable
private fun X2Badge() {
    Box(
        modifier = Modifier.size(width = 24.dp, height = 28.dp),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Filled.LocalFireDepartment,
            contentDescription = null,
            tint = X2BadgeColor,
            modifier = Modifier.fillMaxSize(),
        )
        Text(
            text = "x2",
            color = Color.White,
            style =
                MaterialTheme.typography.bodySmall.copy(
                    fontSize = 11.sp,
                    lineHeight = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.03.sp,
                    shadow =
                        androidx.compose.ui.graphics.Shadow(
                            color = Color.Black,
                            offset =
                                androidx.compose.ui.geometry
                                    .Offset(0f, 0.5f),
                            blurRadius = 1f,
                        ),
                ),
        )
    }
}

private val StatsContainer: Color = Color(0xFF00070C)
private val ContainerBorderColor: Color = Color(0xFF998C5F)
private val DividerColor: Color = Color(0xFF2E3940)
private val X2BadgeColor: Color = Color(0xFFFF8A3D)
