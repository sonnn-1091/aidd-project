package com.example.aiddproject.home.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.aiddproject.R
import com.example.aiddproject.core.locale.Language
import com.example.aiddproject.core.locale.LocaleViewModel
import com.example.aiddproject.home.domain.Award
import com.example.aiddproject.home.domain.states.KudosState
import com.example.aiddproject.home.ui.components.AwardsSection
import com.example.aiddproject.home.ui.components.HomeBottomBar
import com.example.aiddproject.home.ui.components.HomeFab
import com.example.aiddproject.home.ui.components.HomeHeader
import com.example.aiddproject.home.ui.components.HomeHero
import com.example.aiddproject.home.ui.components.HomeNavTab
import com.example.aiddproject.home.ui.components.KudosSection
import com.example.aiddproject.home.ui.components.ThemeParagraph

/**
 * Stateful entry point for the Home route. Hoists [HomeViewModel]'s aggregate
 * [HomeUiState] and the shared [LocaleViewModel]'s language preference, then forwards
 * them to the stateless [HomeScreenContent]. The countdown ticker is bound to the
 * `STARTED` lifecycle state via [LifecycleStartEffect] so it pauses on background
 * (TR-004); the section refreshes also re-fire on every `STARTED` re-entry per
 * Q-Home-5 (no longer-lived in-memory cache).
 */
@Composable
fun HomeScreen(
    onNavigateToAwardsOverview: () -> Unit,
    onNavigateToKudosOverview: () -> Unit,
    onNavigateToKudosFeed: () -> Unit,
    onNavigateToKudosDetail: () -> Unit,
    onNavigateToWriteKudo: () -> Unit,
    onNavigateToAwardDetail: (Award) -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToProfile: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
    localeViewModel: LocaleViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedTab by rememberSaveable { mutableStateOf(HomeNavTab.Saa2025) }

    LifecycleStartEffect(viewModel) {
        viewModel.startCountdown()
        viewModel.refreshAll()
        onStopOrDispose {
            viewModel.stopCountdown()
        }
    }

    HomeScreenContent(
        state = uiState,
        selectedTab = selectedTab,
        onLanguageSelected = { localeViewModel.setLanguage(it) },
        onSearchClick = onNavigateToSearch,
        onBellClick = { /* sheet wiring lands in US6 / Phase 8 */ },
        onAboutAwardClick = onNavigateToAwardsOverview,
        onAboutKudosClick = onNavigateToKudosOverview,
        onAwardChiTietTap = onNavigateToAwardDetail,
        onAwardsRetry = { /* re-fetch lands in US2 / Phase 4 */ },
        onKudosChiTietClick = onNavigateToKudosDetail,
        onPencilClick = onNavigateToWriteKudo,
        onSKudosClick = onNavigateToKudosFeed,
        onTabSelect = { tab ->
            selectedTab = tab
            when (tab) {
                HomeNavTab.Saa2025 -> Unit // already on Home
                HomeNavTab.Awards -> onNavigateToAwardsOverview()
                HomeNavTab.Kudos -> onNavigateToKudosFeed()
                HomeNavTab.Profile -> onNavigateToProfile()
            }
        },
    )
}

@Composable
fun HomeScreenContent(
    state: HomeUiState,
    selectedTab: HomeNavTab,
    onLanguageSelected: (Language) -> Unit,
    onSearchClick: () -> Unit,
    onBellClick: () -> Unit,
    onAboutAwardClick: () -> Unit,
    onAboutKudosClick: () -> Unit,
    onAwardChiTietTap: (Award) -> Unit,
    onAwardsRetry: () -> Unit,
    onKudosChiTietClick: () -> Unit,
    onPencilClick: () -> Unit,
    onSKudosClick: () -> Unit,
    onTabSelect: (HomeNavTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier =
            modifier
                .fillMaxSize()
                .testTag(TEST_TAG_HOME_SCREEN),
        containerColor = Color.Transparent,
        bottomBar = {
            HomeBottomBar(
                selected = selectedTab,
                onTabSelect = onTabSelect,
            )
        },
        floatingActionButton = {
            val isKudosAvailable = (state.kudos as? KudosState.Loaded)?.summary?.isKudosAvailable == true
            HomeFab(
                isKudosAvailable = isKudosAvailable,
                onPencilClick = onPencilClick,
                onSKudosClick = onSKudosClick,
            )
        },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(R.drawable.bg_home),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
            LazyColumn(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .systemBarsPadding(),
            ) {
                item {
                    HomeHeader(
                        selectedLanguage = state.language,
                        onLanguageSelected = onLanguageSelected,
                        onSearchClick = onSearchClick,
                        onBellClick = onBellClick,
                        unreadCount = state.unreadCount,
                    )
                }
                item { Spacer(Modifier.height(24.dp)) }
                item {
                    HomeHero(
                        countdown = state.countdown,
                        onAboutAwardClick = onAboutAwardClick,
                        onAboutKudosClick = onAboutKudosClick,
                    )
                }
                item { ThemeParagraph() }
                item {
                    AwardsSection(
                        state = state.awards,
                        onChiTietTap = onAwardChiTietTap,
                        onRetry = onAwardsRetry,
                    )
                }
                item { Spacer(Modifier.height(32.dp)) }
                item {
                    KudosSection(
                        state = state.kudos,
                        onChiTietClick = onKudosChiTietClick,
                    )
                }
                item { Spacer(Modifier.height(96.dp)) } // breathing room above FAB / NavBar
            }
        }
    }
}

const val TEST_TAG_HOME_SCREEN: String = "home_screen"
