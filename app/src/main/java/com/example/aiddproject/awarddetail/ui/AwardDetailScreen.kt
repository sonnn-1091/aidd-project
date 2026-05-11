package com.example.aiddproject.awarddetail.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.aiddproject.core.locale.LocaleViewModel
import com.example.aiddproject.home.ui.components.HomeNavTab
import com.example.aiddproject.home.ui.components.NotificationsSheet

/**
 * Stateful entry point for the Award Detail route. Owns the
 * [AwardDetailViewModel] + the shared [LocaleViewModel] + the
 * Notifications sheet `mutableStateOf` flag.
 *
 * Header / bottom-nav / Sun*Kudos / dropdown callbacks were stubbed
 * in Phase 3 and progressively wired through Phase 4 (dropdown) and
 * Phase 6 (search / bell / language). Per Resolved Q3 the Sun*Kudos
 * Chi tiết and the bottom-nav Kudos tab share one
 * [onNavigateToKudosOverview] lambda so both surfaces funnel into the
 * Kudos hub.
 *
 * The bell opens a `NotificationsSheet` mirroring Home's pattern —
 * the sheet is mounted at the screen root so a 401-driven NavHost
 * tear-down implicitly removes it.
 */
@Composable
fun AwardDetailScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToKudosOverview: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToSearch: () -> Unit,
    viewModel: AwardDetailViewModel = hiltViewModel(),
    localeViewModel: LocaleViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var notificationsSheetVisible by rememberSaveable { mutableStateOf(false) }

    AwardDetailScreenContent(
        state = state,
        onRetry = viewModel::onRetry,
        onLanguageSelected = { localeViewModel.setLanguage(it) },
        onSearchClick = onNavigateToSearch,
        onBellClick = { notificationsSheetVisible = true },
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

    // Sheet mounted at the screen root so a 401-driven popUpTo(GATE)
    // tear-down implicitly removes it (mirrors HomeScreen's pattern).
    if (notificationsSheetVisible) {
        NotificationsSheet(
            onDismissRequest = { notificationsSheetVisible = false },
        )
    }
}
