package com.example.aiddproject.kudos.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aiddproject.R
import com.example.aiddproject.kudos.domain.Kudos
import com.example.aiddproject.kudos.domain.states.AllKudosState
import com.example.aiddproject.kudos.ui.KudosTestTags
import com.example.aiddproject.ui.theme.SaaCream

/**
 * All Kudos feed stub (Figma `C` section).
 *
 * Phase 3 MVP renders the section title + state-aware list (up to 5
 * placeholder cards in Loaded). Real KudosFeedCard with heart toggle,
 * Copy Link, hashtag chips, photos lands in Phases 7/8/9.
 */
@Composable
fun AllKudosFeed(
    state: AllKudosState,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .testTag(KudosTestTags.FEED),
    ) {
        Text(
            text = stringResource(R.string.kudos_section_all_title),
            color = SaaCream,
            style = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
            modifier = Modifier.padding(bottom = 12.dp),
        )
        when (state) {
            AllKudosState.Loading -> SectionPlaceholder(text = stringResource(R.string.kudos_loading))
            AllKudosState.Empty -> SectionPlaceholder(text = stringResource(R.string.kudos_empty))
            is AllKudosState.Error -> SectionPlaceholder(text = stringResource(state.messageRes))
            is AllKudosState.Loaded -> {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    state.items.take(5).forEach { kudos ->
                        FeedStubCard(kudos)
                    }
                }
            }
        }
    }
}

@Composable
private fun FeedStubCard(kudos: Kudos) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .heightIn(min = 100.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White.copy(alpha = 0.06f))
                .padding(12.dp),
    ) {
        val sender =
            if (kudos.senderVisibleToMe) {
                kudos.sender.fullName
            } else {
                kudos.anonymousNickname ?: kudos.sender.fullName
            }
        Text(
            text = "$sender → ${kudos.recipient.fullName}",
            color = Color.White,
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp, fontWeight = FontWeight.SemiBold),
        )
        Text(
            text = kudos.message,
            color = Color.White.copy(alpha = 0.8f),
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
            maxLines = 2,
            modifier = Modifier.padding(top = 4.dp),
        )
        Text(
            text = "${kudos.heartCount} ♥",
            color = Color(0xFFFF6B6B),
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, fontWeight = FontWeight.SemiBold),
            modifier = Modifier.padding(top = 6.dp),
        )
    }
}
