package com.example.aiddproject.awarddetail.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.aiddproject.core.locale.LocaleViewModel
import com.example.aiddproject.home.ui.components.HomeNavTab

/**
 * Stateful entry point for the Award Detail route. Owns the
 * [AwardDetailViewModel] + the shared [LocaleViewModel].
 *
 * Header / bottom-nav / Sun*Kudos / dropdown callbacks were stubbed
 * in Phase 3 and progressively wired through Phase 4 (dropdown) and
 * Phase 6 (search / bell / language). Per Resolved Q3 the Sun*Kudos
 * Chi tiết and the bottom-nav Kudos tab share one
 * [onNavigateToKudosOverview] lambda so both surfaces funnel into the
 * Kudos hub.
 *
 * The bell tap routes to the dedicated `Routes.NOTIFICATIONS` screen
 * (spec `_b68CBWKl5` Q-N-11) — replaces the previous bottom-sheet
 * pattern. Migration shared with Home and Kudos hub.
 */
@Composable
fun AwardDetailScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToKudosOverview: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    viewModel: AwardDetailViewModel = hiltViewModel(),
    localeViewModel: LocaleViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    AwardDetailScreenContent(
        state = state,
        onRetry = viewModel::onRetry,
        onLanguageSelected = { localeViewModel.setLanguage(it) },
        onSearchClick = onNavigateToSearch,
        onBellClick = onNavigateToNotifications,
        onTabSelect = { tab ->
            when (tab) {
                // Awards re-tap scrolls to top — handled in
                // AwardDetailScreenContent (which owns the LazyListState).
                HomeNavTab.Awards -> Unit
                HomeNavTab.Saa2025 -> onNavigateToHome()
                HomeNavTab.Kudos -> onNavigateToKudosOverview()
                HomeNavTab.Profile -> onNavigateToProfile()
            }
        },
        onCategorySelected = { award -> viewModel.onCategorySelected(award.id) },
        onKudosChiTietClick = onNavigateToKudosOverview,
    )
}
