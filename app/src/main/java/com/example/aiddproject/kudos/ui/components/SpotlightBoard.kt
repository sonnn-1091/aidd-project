package com.example.aiddproject.kudos.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aiddproject.R
import com.example.aiddproject.kudos.domain.SpotlightSearchResult
import com.example.aiddproject.kudos.domain.states.SpotlightState
import com.example.aiddproject.kudos.ui.KudosTestTags
import com.example.aiddproject.ui.theme.SaaCream

/**
 * Spotlight Board section (spec § US9).
 *
 * Renders section header + total counter + pan/zoom canvas + live
 * search input. Search results: Loading → spinner-like text, Match
 * → canvas highlights the node, NoMatch → "Không tìm thấy kết quả".
 */
@Composable
fun SpotlightBoard(
    state: SpotlightState,
    searchQuery: String,
    searchResult: SpotlightSearchResult,
    onSpotlightSearchChange: (String) -> Unit,
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
            modifier = Modifier.padding(bottom = 8.dp),
        )
        when (state) {
            SpotlightState.Loading -> SectionPlaceholder(text = stringResource(R.string.kudos_loading))
            SpotlightState.Empty -> SectionPlaceholder(text = stringResource(R.string.kudos_section_empty_generic))
            is SpotlightState.Error -> SectionPlaceholder(text = stringResource(state.messageRes))
            is SpotlightState.Loaded -> {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = stringResource(R.string.kudos_spotlight_total_label),
                        color = Color.White.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                    )
                    Text(
                        text = state.graph.totalKudosCount.toString(),
                        color = SaaCream,
                        style = MaterialTheme.typography.titleLarge.copy(fontSize = 24.sp, fontWeight = FontWeight.Bold),
                    )
                }
                SpotlightSearchInput(
                    query = searchQuery,
                    onQueryChange = onSpotlightSearchChange,
                )
                if (searchResult is SpotlightSearchResult.NoMatch) {
                    Text(
                        text = stringResource(R.string.kudos_spotlight_no_results),
                        color = Color.White.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                        modifier = Modifier.padding(vertical = 4.dp),
                    )
                }
                SpotlightCanvas(
                    graph = state.graph,
                    searchResult = searchResult,
                )
            }
        }
    }
}
