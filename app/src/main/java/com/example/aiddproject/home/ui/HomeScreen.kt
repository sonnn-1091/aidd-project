package com.example.aiddproject.home.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
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
import kotlinx.coroutines.launch

/**
 * Stateful entry point for the Home route. Hoists [HomeViewModel]'s aggregate
 * [HomeUiState] and the shared [LocaleViewModel]'s language preference, then forwards
 * them to the stateless [HomeScreenContent]. The countdown ticker is bound to the
 * `STARTED` lifecycle state via [LifecycleStartEffect] so it pauses on background
 * (TR-004); the section refreshes also re-fire on every `STARTED` re-entry per
 * Q-Home-5 (no longer-lived in-memory cache).
 *
 * SAA 2025 is always the active tab on Home (the other tabs navigate away). Tapping
 * a non-SAA tab routes to the corresponding placeholder; re-tap of SAA scrolls the
 * Home `LazyColumn` to top — handled inside [HomeScreenContent] so the
 * `LazyListState` doesn't need to leak through this stateful layer.
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
    onNavigateToNotifications: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
    localeViewModel: LocaleViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LifecycleStartEffect(viewModel) {
        viewModel.startCountdown()
        viewModel.refreshAll()
        onStopOrDispose {
            viewModel.stopCountdown()
        }
    }

    HomeScreenContent(
        state = uiState,
        selectedTab = HomeNavTab.Saa2025,
        onLanguageSelected = { localeViewModel.setLanguage(it) },
        onSearchClick = onNavigateToSearch,
        onBellClick = onNavigateToNotifications,
        onAboutAwardClick = onNavigateToAwardsOverview,
        onAboutKudosClick = onNavigateToKudosOverview,
        onAwardChiTietTap = onNavigateToAwardDetail,
        onAwardsRetry = viewModel::onRetryAwards,
        onKudosChiTietClick = onNavigateToKudosDetail,
        onPencilClick = onNavigateToWriteKudo,
        onSKudosClick = onNavigateToKudosFeed,
        onTabSelect = { tab ->
            when (tab) {
                // SAA re-tap is handled internally by HomeScreenContent (scroll-to-top).
                HomeNavTab.Saa2025 -> Unit
                HomeNavTab.Awards -> onNavigateToAwardsOverview()
                // Sun*Kudos hub is `Routes.KUDOS_OVERVIEW` — the
                // dedicated feed (`KUDOS_FEED`) only opens via the
                // "Xem tất cả Kudos" link inside the hub.
                HomeNavTab.Kudos -> onNavigateToKudosOverview()
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
    lazyListState: LazyListState = rememberLazyListState(),
) {
    val scope = rememberCoroutineScope()
    val tabSelectHandler: (HomeNavTab) -> Unit = { tab ->
        // Re-tap of the active SAA 2025 tab scrolls the Home content to the top
        // (Q-Home-3).
        if (tab == HomeNavTab.Saa2025 && selectedTab == HomeNavTab.Saa2025) {
            scope.launch { lazyListState.animateScrollToItem(0) }
        }
        onTabSelect(tab)
    }
    Scaffold(
        modifier =
            modifier
                .fillMaxSize()
                .testTag(TEST_TAG_HOME_SCREEN),
        containerColor = Color.Transparent,
        // T102 (c-QM3_zjkG spec): lift HomeHeader into the sticky topBar
        // slot so Home's header pins on scroll like Award Detail's, with
        // statusBarsPadding so the OS doesn't intercept bell/search/lang
        // taps. The Figma header gradient stays drawn behind it (140dp
        // band in the body Box below) so the visual reads identically.
        topBar = {
            HomeHeader(
                selectedLanguage = state.language,
                onLanguageSelected = onLanguageSelected,
                onSearchClick = onSearchClick,
                onBellClick = onBellClick,
                unreadCount = state.unreadCount,
                modifier = Modifier.statusBarsPadding(),
            )
        },
        bottomBar = {
            HomeBottomBar(
                selected = selectedTab,
                onTabSelect = tabSelectHandler,
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
            // Header gradient overlay — Figma `mms_1_header` (`6885:9057`)
            // background: linear-gradient(180deg, #00101A 0%, transparent 100%)
            // at 0.9 opacity. The Figma frame anchors the gradient at y=0,
            // covering the iOS 44dp status bar AND the 60dp action area
            // (104dp total). On Android we draw the gradient from the screen
            // top (no systemBarsPadding) so the status bar text reads against
            // the dark band — height bumped to 140dp so the gradient still
            // covers the action row on devices with a taller system status
            // bar inset.
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .background(HeaderGradient),
            )
            LazyColumn(
                state = lazyListState,
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(padding),
            ) {
                // HomeHeader is now in Scaffold's topBar slot (T102) — no
                // longer a LazyColumn item. The 40dp gap that previously
                // sat between the in-list header and the hero is preserved
                // so the hero countdown still anchors at its Figma y=144
                // position.
                item { Spacer(Modifier.height(40.dp)) }
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

/**
 * Vertical gradient that mirrors Figma `mms_1_header`'s 0.9-opacity
 * 104dp top band. The Figma stop list interpolates between #00101A 100%
 * and the same colour at 0% opacity through several intermediate alphas;
 * Compose's three-stop linear gradient produces a visually identical
 * smooth fade.
 */
private val HeaderGradient: Brush =
    Brush.verticalGradient(
        colors =
            listOf(
                Color(0xE6001019), // ≈ #00101A at 0.9 opacity (top)
                Color(0x4D00101A),
                Color(0x00001019), // transparent (bottom)
            ),
    )
