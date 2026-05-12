package com.example.aiddproject.kudos.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.aiddproject.core.locale.LocaleViewModel
import com.example.aiddproject.home.ui.components.HomeNavTab

/**
 * Hilt-injected entry composable for the Sun*Kudos hub
 * (`Routes.KUDOS_OVERVIEW`).
 *
 * Phase 3 MVP wires: VM state collection, nav callbacks for Send
 * Kudos / View Detail / View All / Profile / Secret Box / bottom-
 * nav. Callbacks for filter/heart/copy-link/spotlight-search route
 * to no-op stubs at this layer until Phases 5/7/9/10 land.
 */
@Composable
fun KudosScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToAwardsOverview: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToSendKudos: () -> Unit,
    onNavigateToKudosDetail: () -> Unit,
    onNavigateToAllKudos: () -> Unit,
    onNavigateToSecretBoxOpen: () -> Unit,
    viewModel: KudosViewModel = hiltViewModel(),
    localeViewModel: LocaleViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    KudosScreenContent(
        state = state,
        onPullToRefresh = viewModel::onPullToRefresh,
        onLanguageSelected = { localeViewModel.setLanguage(it) },
        onSearchClick = onNavigateToSearch,
        onBellClick = { /* Notifications sheet wires in Phase 13 polish. */ },
        onTabSelect = { tab ->
            when (tab) {
                HomeNavTab.Saa2025 -> onNavigateToHome()
                HomeNavTab.Awards -> onNavigateToAwardsOverview()
                HomeNavTab.Kudos -> Unit // Active tab: re-tap scrolls to top inside content.
                HomeNavTab.Profile -> onNavigateToProfile()
            }
        },
        onSendKudos = onNavigateToSendKudos,
        // Phase 5 wires filter bottom sheets.
        onHashtagTriggerTap = {},
        onDepartmentTriggerTap = {},
        onCardTap = { _ -> onNavigateToKudosDetail() },
        // Phase 7 wires optimistic reaction.
        onHeartTap = { _ -> },
        // Phase 9 wires clipboard + Snackbar.
        onCopyLink = { _ -> },
        // Phase 5 re-applies hashtag filter on chip tap.
        onHashtagChipTap = { _ -> },
        onProfileTap = { _ -> onNavigateToProfile() },
        onViewAllKudos = onNavigateToAllKudos,
        onOpenSecretBox = onNavigateToSecretBoxOpen,
        // Phase 10 wires debounced sunner search.
        onSpotlightSearchChange = { _ -> },
        // Phase 9 clears snackbar slot via the VM.
        onSnackbarDismissed = {},
    )
}
