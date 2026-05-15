package com.example.aiddproject.kudos.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.aiddproject.R
import com.example.aiddproject.core.locale.LocaleViewModel
import com.example.aiddproject.home.ui.components.HomeNavTab
import com.example.aiddproject.navigation.KUDO_SUBMITTED_FLAG

/**
 * Hilt-injected entry composable for the Sun*Kudos hub
 * (`Routes.KUDOS_OVERVIEW`).
 *
 * Wires the VM to nav callbacks and the Compose-local clipboard
 * manager (US13). Phase 10 will inject the debounced spotlight
 * search.
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
    onNavigateToNotifications: () -> Unit,
    submitSignalSavedStateHandle: SavedStateHandle? = null,
    viewModel: KudosViewModel = hiltViewModel(),
    localeViewModel: LocaleViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val filterResetTick by viewModel.filterResetTick.collectAsStateWithLifecycle()
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current

    // Cross-screen submit signal — when the Viết Kudo composer sets the
    // kudoSubmitted flag on this entry's savedStateHandle, refresh the
    // hub so the new card appears at the top of the feed. First cross-
    // screen savedStateHandle use in the codebase — pattern in plan
    // § Notes. The flag is reset to false after the refresh fires so
    // re-entering the hub later doesn't double-refresh.
    if (submitSignalSavedStateHandle != null) {
        val submitted by submitSignalSavedStateHandle
            .getStateFlow(KUDO_SUBMITTED_FLAG, false)
            .collectAsStateWithLifecycle()
        LaunchedEffect(submitted) {
            if (submitted) {
                viewModel.onPullToRefresh()
                submitSignalSavedStateHandle[KUDO_SUBMITTED_FLAG] = false
            }
        }
    }

    KudosScreenContent(
        state = state,
        filterResetTick = filterResetTick,
        onPullToRefresh = viewModel::onPullToRefresh,
        onLanguageSelected = { localeViewModel.setLanguage(it) },
        onSearchClick = onNavigateToSearch,
        onBellClick = onNavigateToNotifications,
        onTabSelect = { tab ->
            when (tab) {
                HomeNavTab.Saa2025 -> onNavigateToHome()
                HomeNavTab.Awards -> onNavigateToAwardsOverview()
                HomeNavTab.Kudos -> Unit
                HomeNavTab.Profile -> onNavigateToProfile()
            }
        },
        onSendKudos = onNavigateToSendKudos,
        onSelectHashtagId = viewModel::onSelectHashtag,
        onSelectDepartmentId = viewModel::onSelectDepartment,
        onCardTap = { _ -> onNavigateToKudosDetail() },
        onHeartTap = viewModel::onHeartTap,
        onCopyLink = { kudosId ->
            val url = context.getString(R.string.kudos_copy_link_url_template, kudosId)
            clipboard.setText(AnnotatedString(url))
            viewModel.onCopyLink(kudosId)
        },
        onHashtagChipTap = viewModel::onHashtagChipTap,
        onProfileTap = { _ -> onNavigateToProfile() },
        onViewAllKudos = onNavigateToAllKudos,
        onOpenSecretBox = onNavigateToSecretBoxOpen,
        onSpotlightSearchChange = viewModel::onSpotlightSearchChange,
        onSnackbarDismissed = viewModel::onSnackbarDismissed,
    )
}
