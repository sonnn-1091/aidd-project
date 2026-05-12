package com.example.aiddproject.kudos.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.aiddproject.R
import com.example.aiddproject.kudos.domain.Kudos
import com.example.aiddproject.kudos.domain.states.AllKudosState
import com.example.aiddproject.kudos.ui.KudosTestTags

/**
 * All Kudos feed (spec § US1).
 *
 * Renders the first page of [AllKudosState.Loaded] as a vertical
 * stack of [KudosFeedCard]s. Pagination beyond the first page lands
 * in Phase 9 (US14 — "View all Kudos" link routes to the dedicated
 * feed screen instead).
 */
@Composable
fun AllKudosFeed(
    state: AllKudosState,
    onHeartTap: (kudosId: String) -> Unit,
    onCopyLink: (kudosId: String) -> Unit,
    onCardTap: (Kudos) -> Unit,
    onHashtagChipTap: (hashtagId: String) -> Unit,
    onProfileTap: (userId: String) -> Unit,
    onViewAllKudos: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .testTag(KudosTestTags.FEED),
    ) {
        when (state) {
            AllKudosState.Loading -> SectionPlaceholder(text = stringResource(R.string.kudos_loading))
            AllKudosState.Empty -> SectionPlaceholder(text = stringResource(R.string.kudos_empty))
            is AllKudosState.Error -> SectionPlaceholder(text = stringResource(state.messageRes))
            is AllKudosState.Loaded -> {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    state.items.forEach { kudos ->
                        KudosFeedCard(
                            kudos = kudos,
                            onHeartTap = onHeartTap,
                            onCopyLink = onCopyLink,
                            onCardTap = onCardTap,
                            onHashtagChipTap = onHashtagChipTap,
                            onProfileTap = onProfileTap,
                        )
                    }
                    if (state.hasMore) {
                        ViewAllKudosLink(onViewAllKudos = onViewAllKudos)
                    }
                }
            }
        }
    }
}
