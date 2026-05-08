package com.example.aiddproject.home.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import com.example.aiddproject.home.ui.components.NotificationsSheet
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
    viewModel: HomeViewModel = hiltViewModel(),
    localeViewModel: LocaleViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var notificationsSheetVisible by rememberSaveable { mutableStateOf(false) }

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
        onBellClick = { notificationsSheetVisible = true },
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
                HomeNavTab.Kudos -> onNavigateToKudosFeed()
                HomeNavTab.Profile -> onNavigateToProfile()
            }
        },
        notificationsSheetVisible = notificationsSheetVisible,
    )

    // Sheet is mounted at the screen root, NOT inside HomeScreenContent — so a
    // 401-driven popUpTo(GATE) navigation tear-down (Q-Plan-3) implicitly
    // removes the sheet without an explicit dismiss sequencing.
    if (notificationsSheetVisible) {
        NotificationsSheet(
            onDismissRequest = {
                notificationsSheetVisible = false
                viewModel.onNotificationsSheetDismissed()
            },
        )
    }
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
    notificationsSheetVisible: Boolean = false,
) {
    val scope = rememberCoroutineScope()
    val tabSelectHandler: (HomeNavTab) -> Unit = { tab ->
        // Re-tap of the active SAA 2025 tab scrolls the Home content to the top
        // (Q-Home-3). Suppressed when the Notifications sheet is open so the sheet
        // gesture continues to take precedence (risk register).
        if (tab == HomeNavTab.Saa2025 && selectedTab == HomeNavTab.Saa2025 && !notificationsSheetVisible) {
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
            // at 0.9 opacity, 104dp tall, sitting above the keyvisual so the
            // logo + actions row read against a dark band. We extend it with
            // the system-bar inset on top so the status bar text also reads
            // against the dark gradient instead of the bright keyvisual.
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .systemBarsPadding()
                        .height(104.dp)
                        .background(HeaderGradient),
            )
            LazyColumn(
                state = lazyListState,
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
                // 40dp gap between header end (y=104 in Figma) and hero start
                // (y=144) per `mms_1_header` → `mms_2_content` distance.
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
