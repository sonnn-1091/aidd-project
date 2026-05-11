package com.example.aiddproject.awarddetail.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.aiddproject.core.locale.LocaleViewModel
import com.example.aiddproject.home.ui.components.HomeNavTab

/**
 * Stateful entry point for the Award Detail route. Owns the
 * [AwardDetailViewModel] + the shared [LocaleViewModel]; forwards
 * navigation callbacks to the NavHost layer.
 *
 * **Phase 3 scope**: every header / bottom-nav / Sun*Kudos / dropdown
 * callback is wired to the navigation-level lambdas passed in by
 * [AppNavigation]. The category dropdown trigger is a no-op until
 * Phase 4 wires the real popup. Sticky chrome is in place from this
 * phase forward (FR-014).
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
                // Awards re-tap is a no-op for Phase 3; Phase 5 wires scroll-to-top.
                HomeNavTab.Awards -> Unit
                HomeNavTab.Saa2025 -> onNavigateToHome()
                HomeNavTab.Kudos -> onNavigateToKudosOverview()
                HomeNavTab.Profile -> onNavigateToProfile()
            }
        },
        onCategorySelected = { award -> viewModel.onCategorySelected(award.id) },
        // Per Resolved Q3 the Sun*Kudos block's Chi tiết destination is
        // the same as the bottom-nav Kudos tab — both fire
        // [onNavigateToKudosOverview].
        onKudosChiTietClick = onNavigateToKudosOverview,
    )
}
