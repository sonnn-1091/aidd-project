package com.example.aiddproject.kudos.ui

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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.aiddproject.R
import com.example.aiddproject.core.locale.Language
import com.example.aiddproject.home.ui.components.HomeBottomBar
import com.example.aiddproject.home.ui.components.HomeHeader
import com.example.aiddproject.home.ui.components.HomeNavTab
import com.example.aiddproject.kudos.domain.Kudos
import com.example.aiddproject.kudos.domain.states.PersonalStatsState
import com.example.aiddproject.kudos.ui.components.AllKudosFeed
import com.example.aiddproject.kudos.ui.components.CopyLinkSnackbarHost
import com.example.aiddproject.kudos.ui.components.HighlightCarousel
import com.example.aiddproject.kudos.ui.components.KudosHeroBanner
import com.example.aiddproject.kudos.ui.components.PersonalStatsPanel
import com.example.aiddproject.kudos.ui.components.SendKudosCta
import com.example.aiddproject.kudos.ui.components.SpotlightBoard
import com.example.aiddproject.kudos.ui.components.TopTenRecipients
import com.example.aiddproject.ui.theme.SaaCream
import kotlinx.coroutines.launch

/**
 * Stateless layout for the Sun*Kudos hub (spec § US1).
 *
 * Phase 3 MVP wiring:
 *  - Scaffold + HomeHeader/HomeBottomBar chrome (reused from
 *    home/ui/components).
 *  - Body inside `PullToRefreshBox` so user-pull triggers the
 *    [onPullToRefresh] callback (Q-K-2 contract).
 *  - LazyColumn assembling the 5 section blocks + 2 CTAs +
 *    snackbar host in vertical order.
 *
 * Callback set is intentionally exhaustive for the full 14-user-
 * story hub — Phases 4..12 wire each lambda to its destination
 * without re-touching this composable.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KudosScreenContent(
    state: KudosUiState,
    onPullToRefresh: () -> Unit,
    onLanguageSelected: (Language) -> Unit,
    onSearchClick: () -> Unit,
    onBellClick: () -> Unit,
    onTabSelect: (HomeNavTab) -> Unit,
    onSendKudos: () -> Unit,
    onSelectHashtagId: (hashtagId: String?) -> Unit,
    onSelectDepartmentId: (departmentId: String?) -> Unit,
    filterResetTick: Int,
    onCardTap: (Kudos) -> Unit,
    onHeartTap: (kudosId: String) -> Unit,
    onCopyLink: (kudosId: String) -> Unit,
    onHashtagChipTap: (hashtagId: String) -> Unit,
    onProfileTap: (userId: String) -> Unit,
    onViewAllKudos: () -> Unit,
    onOpenSecretBox: () -> Unit,
    onSpotlightSearchChange: (String) -> Unit,
    onSnackbarDismissed: () -> Unit,
    modifier: Modifier = Modifier,
    lazyListState: LazyListState = rememberLazyListState(),
) {
    val scope = rememberCoroutineScope()
    val snackbarHost = remember { SnackbarHostState() }
    val pullState = rememberPullToRefreshState()
    val a11yScreenLabel = stringResource(R.string.a11y_kudos_screen)

    val activeHashtagLabel: String? =
        state.hashtags.firstOrNull { it.id == state.selectedHashtagId }?.let { "#${it.tagName}" }
    val activeDepartmentLabel: String? =
        state.departments.firstOrNull { it.id == state.selectedDepartmentId }?.name

    val tabSelectHandler: (HomeNavTab) -> Unit = { tab ->
        // Re-tap of the active Kudos tab scrolls the body to the top
        // — mirrors Home's SAA-tab + Award Detail's Awards-tab pattern.
        if (tab == HomeNavTab.Kudos) {
            scope.launch { lazyListState.animateScrollToItem(0) }
        }
        onTabSelect(tab)
    }

    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(Color(0xFF00070C))
                .semantics { contentDescription = a11yScreenLabel }
                .testTag(KudosTestTags.SCREEN),
    ) {
        // KV background is now owned by KudosHeroBanner itself
        // (deviation D1 fix) — the rest of the screen renders on
        // the solid `#00070C` background applied to this outer Box.
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            topBar = {
                HomeHeader(
                    selectedLanguage = state.language,
                    onLanguageSelected = onLanguageSelected,
                    onSearchClick = onSearchClick,
                    onBellClick = onBellClick,
                    unreadCount = 0,
                    modifier = Modifier.statusBarsPadding(),
                )
            },
            bottomBar = {
                HomeBottomBar(
                    selected = HomeNavTab.Kudos,
                    onTabSelect = tabSelectHandler,
                )
            },
            snackbarHost = {
                CopyLinkSnackbarHost(
                    hostState = snackbarHost,
                    message = state.snackbar,
                    onDismissed = onSnackbarDismissed,
                )
            },
        ) { padding ->
            PullToRefreshBox(
                state = pullState,
                isRefreshing = state.isRefreshing,
                onRefresh = onPullToRefresh,
                modifier = Modifier.fillMaxSize().padding(padding),
            ) {
                LazyColumn(
                    state = lazyListState,
                    modifier = Modifier.fillMaxSize().testTag(KudosTestTags.LAZY_COLUMN),
                ) {
                    item { KudosHeroBanner() }
                    item { SendKudosCta(onSendKudos = onSendKudos) }
                    item {
                        HighlightCarousel(
                            state = state.highlight,
                            filterResetTick = filterResetTick,
                            selectedHashtagLabel = activeHashtagLabel,
                            selectedDepartmentLabel = activeDepartmentLabel,
                            hashtags = state.hashtags,
                            departments = state.departments,
                            activeHashtagId = state.selectedHashtagId,
                            activeDepartmentId = state.selectedDepartmentId,
                            onSelectHashtag = { hashtag -> onSelectHashtagId(hashtag?.id) },
                            onSelectDepartment = { department -> onSelectDepartmentId(department?.id) },
                            onHeartTap = onHeartTap,
                            onCopyLink = onCopyLink,
                            onCardTap = onCardTap,
                            onHashtagChipTap = onHashtagChipTap,
                            onProfileTap = onProfileTap,
                        )
                    }
                    item {
                        SpotlightBoard(
                            state = state.spotlight,
                            searchQuery = state.spotlightSearchQuery,
                            searchResult = state.spotlightSearchResult,
                            onSpotlightSearchChange = onSpotlightSearchChange,
                        )
                    }
                    // ALL KUDOS block — per Figma `mms_C_All kudos`
                    // (`6885:9220`): section header → Stats → Top 10 →
                    // feed cards → View all link.
                    item { AllKudosSectionHeader() }
                    item {
                        val loadedStats = state.stats as? PersonalStatsState.Loaded
                        val unopened = loadedStats?.stats?.secretBoxesUnopened ?: 0
                        PersonalStatsPanel(
                            state = state.stats,
                            x2BonusActive = state.x2BonusActive,
                            hasUnopenedBox = unopened > 0,
                            onOpenSecretBox = onOpenSecretBox,
                        )
                    }
                    item {
                        TopTenRecipients(
                            state = state.topTen,
                            onProfileTap = onProfileTap,
                        )
                    }
                    item {
                        AllKudosFeed(
                            state = state.allKudos,
                            onHeartTap = onHeartTap,
                            onCopyLink = onCopyLink,
                            onCardTap = onCardTap,
                            onHashtagChipTap = onHashtagChipTap,
                            onProfileTap = onProfileTap,
                            onViewAllKudos = onViewAllKudos,
                        )
                    }
                    item { Spacer(Modifier.height(24.dp)) }
                }
            }
        }
    }
    // Keep SaaCream import alive if unused later.
    @Suppress("UNUSED_EXPRESSION")
    SaaCream
}

/**
 * Shared "ALL KUDOS" block header — Figma `mms_C_All kudos / header`
 * (`6885:9221`). Rendered ONCE above the Stats + Top 10 + feed sub-
 * sections per Figma layout.
 */
@Composable
private fun AllKudosSectionHeader() {
    com.example.aiddproject.kudos.ui.components
        .KudosSectionHeader(
            title = stringResource(R.string.kudos_section_all_title),
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
        )
}
