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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.aiddproject.R
import com.example.aiddproject.core.locale.Language
import com.example.aiddproject.core.locale.LocaleViewModel
import com.example.aiddproject.home.domain.Award
import com.example.aiddproject.home.domain.KudosSummary
import com.example.aiddproject.home.domain.states.AwardsState
import com.example.aiddproject.home.domain.states.CountdownState
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
 * Stateful entry point for the Home route. This UI-implementation pass renders
 * the screen with hardcoded stub state (Phase 2 of the broader plan wires real
 * data via `HomeViewModel` + `AuthErrorInterceptor`). Behavior of the FAB +
 * NavBar + bell sheet is plumbed through callbacks so the structural fidelity
 * matches `OuH1BUTYT0`.
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
    localeViewModel: LocaleViewModel = hiltViewModel(),
) {
    val language by localeViewModel.language.collectAsState()

    // Stub state for visual scaffolding — Phase 2 will replace with real flows.
    val countdown =
        remember {
            CountdownState(days = 233, hours = 7, minutes = 42, isPreEvent = true)
        }
    val awards =
        remember {
            AwardsState.Populated(
                items =
                    listOf(
                        Award(id = "1", name = "Top Talent Award", sortOrder = 0),
                        Award(id = "2", name = "Top Project", sortOrder = 1),
                        Award(id = "3", name = "Top Heart Award", sortOrder = 2),
                    ),
            )
        }
    val kudos =
        remember {
            KudosState.Loaded(
                summary =
                    KudosSummary(
                        isKudosAvailable = true,
                        descriptionText = "",
                    ),
            )
        }
    val unreadCount = 2
    var selectedTab by rememberSaveable { mutableStateOf(HomeNavTab.Saa2025) }

    HomeScreenContent(
        language = language,
        countdown = countdown,
        awards = awards,
        kudos = kudos,
        unreadCount = unreadCount,
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
    language: Language,
    countdown: CountdownState,
    awards: AwardsState,
    kudos: KudosState,
    unreadCount: Int,
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
            val isKudosAvailable = (kudos as? KudosState.Loaded)?.summary?.isKudosAvailable == true
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
                        selectedLanguage = language,
                        onLanguageSelected = onLanguageSelected,
                        onSearchClick = onSearchClick,
                        onBellClick = onBellClick,
                        unreadCount = unreadCount,
                    )
                }
                item { Spacer(Modifier.height(24.dp)) }
                item {
                    HomeHero(
                        countdown = countdown,
                        onAboutAwardClick = onAboutAwardClick,
                        onAboutKudosClick = onAboutKudosClick,
                    )
                }
                item { ThemeParagraph() }
                item {
                    AwardsSection(
                        state = awards,
                        onChiTietTap = onAwardChiTietTap,
                        onRetry = onAwardsRetry,
                    )
                }
                item { Spacer(Modifier.height(32.dp)) }
                item {
                    KudosSection(
                        state = kudos,
                        onChiTietClick = onKudosChiTietClick,
                    )
                }
                item { Spacer(Modifier.height(96.dp)) } // breathing room above FAB / NavBar
            }
        }
    }
}

const val TEST_TAG_HOME_SCREEN: String = "home_screen"
