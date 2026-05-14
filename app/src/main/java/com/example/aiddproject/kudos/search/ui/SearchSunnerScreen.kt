package com.example.aiddproject.kudos.search.ui

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.aiddproject.R
import com.example.aiddproject.home.ui.components.HomeNavTab

/**
 * Hilt entry composable for the Search Sunner screen.
 *
 * Wires:
 *  - `BackHandler { onNavigateBack() }` so the system-back gesture
 *    routes to the same callback as the top-bar back arrow (spec
 *    FR-013).
 *  - Search-bar tap → `Toast` "Tính năng đang được phát triển" for
 *    MVP. The sibling Searching-state spec (`hldqjHoSRH`) is not yet
 *    authored; this stub keeps the screen shippable. Replace with
 *    `onNavigateToSearching()` once that spec lands (plan Phase 4
 *    step 1).
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
    val context = LocalContext.current
    val comingSoonMessage = stringResource(R.string.search_sunner_coming_soon)

    BackHandler(onBack = onNavigateBack)

    SearchSunnerContent(
        state = state,
        callbacks =
            SearchSunnerCallbacks(
                onNavigateBack = onNavigateBack,
                onSearchBarTap = {
                    // TODO(searching-state, spec hldqjHoSRH): replace with
                    //  onNavigateToSearching() once the active-typing
                    //  frame is implemented. For MVP, surface a Toast so
                    //  the tap is not silently lost.
                    Toast.makeText(context, comingSoonMessage, Toast.LENGTH_SHORT).show()
                },
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
