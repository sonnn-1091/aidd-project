package com.example.aiddproject.kudos.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.aiddproject.R
import com.example.aiddproject.kudos.domain.SpotlightSearchResult
import com.example.aiddproject.kudos.domain.states.SpotlightState
import com.example.aiddproject.kudos.ui.KudosTestTags

/**
 * Spotlight Board section (spec § US9, Figma `6885:9099`).
 *
 * The baked Figma image (`kudos_spotlight.png`) already contains
 * the "388 KUDOS" title, the "Tìm kiếm" search field treatment, the
 * Sunner network cloud, and the recent-activity ribbon. We render
 * only the Compose section header above the image — the prior
 * Compose-side total counter + search input were duplicates of what
 * the image renders, so they've been removed in this pass.
 *
 * The interactive search (US9 live debounce) is temporarily inert
 * while the canvas is a static image. When a proper interactive
 * canvas lands, the search input + match-highlight logic can move
 * back into this composable.
 */
@Composable
fun SpotlightBoard(
    state: SpotlightState,
    @Suppress("UNUSED_PARAMETER") searchQuery: String,
    @Suppress("UNUSED_PARAMETER") searchResult: SpotlightSearchResult,
    @Suppress("UNUSED_PARAMETER") onSpotlightSearchChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .testTag(KudosTestTags.SPOTLIGHT),
    ) {
        KudosSectionHeader(
            title = stringResource(R.string.kudos_section_spotlight_title),
            modifier = Modifier.padding(bottom = 12.dp),
        )
        when (state) {
            SpotlightState.Loading -> SectionPlaceholder(text = stringResource(R.string.kudos_loading))
            SpotlightState.Empty -> SectionPlaceholder(text = stringResource(R.string.kudos_section_empty_generic))
            is SpotlightState.Error -> SectionPlaceholder(text = stringResource(state.messageRes))
            is SpotlightState.Loaded -> SpotlightCanvas()
        }
    }
}
