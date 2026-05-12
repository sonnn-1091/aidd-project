package com.example.aiddproject.kudos.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.aiddproject.R
import com.example.aiddproject.core.locale.LocaleViewModel
import com.example.aiddproject.home.ui.components.HomeNavTab

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
    viewModel: KudosViewModel = hiltViewModel(),
    localeViewModel: LocaleViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val filterResetTick by viewModel.filterResetTick.collectAsStateWithLifecycle()
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current

    KudosScreenContent(
        state = state,
        filterResetTick = filterResetTick,
        onPullToRefresh = viewModel::onPullToRefresh,
        onLanguageSelected = { localeViewModel.setLanguage(it) },
        onSearchClick = onNavigateToSearch,
        onBellClick = { /* Notifications sheet wires in Phase 13 polish. */ },
        onTabSelect = { tab ->
            when (tab) {
                HomeNavTab.Saa2025 -> onNavigateToHome()
                HomeNavTab.Awards -> onNavigateToAwardsOverview()
                HomeNavTab.Kudos -> Unit
                HomeNavTab.Profile -> onNavigateToProfile()
            }
        },
        onSendKudos = onNavigateToSendKudos,
        onSelectHashtag = viewModel::onSelectHashtag,
        onSelectDepartment = viewModel::onSelectDepartment,
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
