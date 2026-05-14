package com.example.aiddproject.kudos.search.ui

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.aiddproject.home.ui.components.HomeNavTab

/**
 * Hilt entry composable for the Search Sunner screen.
 *
 * Wires:
 *  - `BackHandler { onNavigateBack() }` so the system-back gesture
 *    routes to the same callback as the top-bar back arrow (spec
 *    FR-013).
 *  - Search-bar input → VM's `onSearchQueryChange`. The typed text
 *    lives in `SearchSunnerUiState.searchQuery`. The Searching state
 *    (live results) is a sibling spec `hldqjHoSRH` and not yet
 *    authored; for MVP the field captures input but nothing actually
 *    searches.
 *  - Row tap → VM promotes the entry to position 0 of the recent
 *    list BEFORE invoking [onNavigateToProfile], so the recent-list
 *    ordering is persisted before Profile mounts.
 */
@Composable
fun SearchSunnerScreen(
    onNavigateBack: () -> Unit,
    onNavigateToProfile: (userId: String) -> Unit,
    onSelectBottomTab: (HomeNavTab) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SearchSunnerViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    BackHandler(onBack = onNavigateBack)

    SearchSunnerContent(
        state = state,
        callbacks =
            SearchSunnerCallbacks(
                onNavigateBack = onNavigateBack,
                onSearchQueryChange = viewModel::onSearchQueryChange,
                onToggleViewAll = viewModel::onToggleViewAll,
                onRemove = viewModel::onRemove,
                onRowTap = { userId ->
                    viewModel.onRowTap(userId) { onNavigateToProfile(userId) }
                },
                onSelectBottomTab = onSelectBottomTab,
            ),
        modifier = modifier,
    )
}
