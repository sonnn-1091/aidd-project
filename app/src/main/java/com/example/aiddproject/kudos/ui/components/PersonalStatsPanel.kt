package com.example.aiddproject.kudos.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.layout.ContentScale
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
 * Personal stats panel stub (Figma `D.1`).
 *
 * Phase 3 MVP renders 5 stat tiles when Loaded; placeholder when
 * Loading/Error. x2 fire badge from [specialDayActive] /
 * `x2BonusActive` is wired in Phase 11.
 */
@Composable
fun PersonalStatsPanel(
    state: PersonalStatsState,
    x2BonusActive: Boolean,
    modifier: Modifier = Modifier,
) {
    @Suppress("UNUSED_VARIABLE", "unused")
    val keepContentScale = ContentScale.Crop
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .testTag(KudosTestTags.STATS),
    ) {
        when (state) {
            PersonalStatsState.Loading -> SectionPlaceholder(text = stringResource(R.string.kudos_loading))
            is PersonalStatsState.Error -> SectionPlaceholder(text = stringResource(state.messageRes))
            is PersonalStatsState.Loaded -> StatsGrid(state.stats, x2BonusActive = x2BonusActive)
        }
    }
}

@Composable
private fun StatsGrid(
    stats: PersonalStats,
    x2BonusActive: Boolean,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (x2BonusActive) {
            X2BadgeRow()
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            StatTile(value = stats.kudosReceived, label = stringResource(R.string.kudos_stats_received))
            StatTile(value = stats.kudosSent, label = stringResource(R.string.kudos_stats_sent))
            StatTile(value = stats.heartsReceived, label = stringResource(R.string.kudos_stats_hearts))
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            StatTile(value = stats.secretBoxesOpened, label = stringResource(R.string.kudos_stats_boxes_opened))
            StatTile(value = stats.secretBoxesUnopened, label = stringResource(R.string.kudos_stats_boxes_unopened))
        }
    }
}

private val X2BadgeColor: Color = Color(0xFFFF8A3D)

@Composable
private fun X2BadgeRow() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(
            imageVector = Icons.Filled.LocalFireDepartment,
            contentDescription = null,
            tint = X2BadgeColor,
            modifier = Modifier.heightIn(min = 16.dp),
        )
        Text(
            text = "x2",
            color = X2BadgeColor,
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp, fontWeight = FontWeight.Bold),
        )
    }
}

@Composable
private fun StatTile(
    value: Int,
    label: String,
) {
    Box(
        modifier =
            Modifier
                .heightIn(min = 72.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(SaaCream.copy(alpha = 0.10f))
                .padding(horizontal = 12.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = value.toString(),
                color = SaaCream,
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp, fontWeight = FontWeight.Bold),
            )
            Text(
                text = label,
                color = Color.White.copy(alpha = 0.8f),
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
            )
        }
    }
}
