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
import com.example.aiddproject.kudos.domain.GiftRecipient
import com.example.aiddproject.kudos.domain.states.TopTenState
import com.example.aiddproject.kudos.ui.KudosTestTags
import com.example.aiddproject.ui.theme.SaaCream

/**
 * Top 10 recent gift recipients stub (Figma `D.3`).
 *
 * Phase 3 MVP renders 10 rows when Loaded; placeholder otherwise.
 * Row tap → onProfileTap wired in Phase 9 (US8 + US12).
 */
@Composable
fun TopTenRecipients(
    state: TopTenState,
    onProfileTap: (userId: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .testTag(KudosTestTags.TOP_TEN),
    ) {
        Text(
            text = stringResource(R.string.kudos_top_ten_title),
            color = SaaCream,
            style = MaterialTheme.typography.titleMedium.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(bottom = 8.dp),
        )
        when (state) {
            TopTenState.Loading -> SectionPlaceholder(text = stringResource(R.string.kudos_loading))
            TopTenState.Empty -> SectionPlaceholder(text = stringResource(R.string.kudos_section_empty_generic))
            is TopTenState.Error -> SectionPlaceholder(text = stringResource(state.messageRes))
            is TopTenState.Loaded -> {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    state.items.forEach { recipient ->
                        RecipientRow(recipient = recipient, onProfileTap = onProfileTap)
                    }
                }
            }
        }
    }
}

@Composable
private fun RecipientRow(
    recipient: GiftRecipient,
    onProfileTap: (userId: String) -> Unit,
) {
    val click = rememberSingleClickHandler { onProfileTap(recipient.userId) }
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White.copy(alpha = 0.05f))
                .clickable(onClick = click)
                .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier =
                Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(SaaCream.copy(alpha = 0.2f)),
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = recipient.fullName,
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp, fontWeight = FontWeight.SemiBold),
            )
            Text(
                text = recipient.rewardName,
                color = Color.White.copy(alpha = 0.7f),
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
            )
        }
    }
}
