package com.example.aiddproject.kudos.ui

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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
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
import com.example.aiddproject.kudos.ui.components.DepartmentFilterDropdown
import com.example.aiddproject.kudos.ui.components.HashtagFilterDropdown
import com.example.aiddproject.kudos.ui.components.HighlightCarousel
import com.example.aiddproject.kudos.ui.components.HighlightFilterRow
import com.example.aiddproject.kudos.ui.components.KudosHeroBanner
import com.example.aiddproject.kudos.ui.components.OpenSecretBoxCta
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
    onSelectHashtag: (hashtagId: String?) -> Unit,
    onSelectDepartment: (departmentId: String?) -> Unit,
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
    var hashtagSheetVisible by rememberSaveable { mutableStateOf(false) }
    var departmentSheetVisible by rememberSaveable { mutableStateOf(false) }

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
                .semantics { contentDescription = a11yScreenLabel }
                .testTag(KudosTestTags.SCREEN),
    ) {
        Image(
            painter = painterResource(R.drawable.bg_home),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0xE600101A), Color.Transparent),
                        ),
                    ),
        )
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
                        HighlightFilterRow(
                            selectedHashtagLabel = activeHashtagLabel,
                            selectedDepartmentLabel = activeDepartmentLabel,
                            onHashtagTriggerTap = { hashtagSheetVisible = true },
                            onDepartmentTriggerTap = { departmentSheetVisible = true },
                        )
                    }
                    item {
                        HighlightCarousel(
                            state = state.highlight,
                            filterResetTick = filterResetTick,
                            onHeartTap = onHeartTap,
                            onCopyLink = onCopyLink,
                            onCardTap = onCardTap,
                            onHashtagChipTap = onHashtagChipTap,
                            onProfileTap = onProfileTap,
                        )
                    }
                    item { SpotlightBoard(state = state.spotlight) }
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
                    item { PersonalStatsPanel(state = state.stats) }
                    item {
                        val loadedStats = state.stats as? PersonalStatsState.Loaded
                        val unopened = loadedStats?.stats?.secretBoxesUnopened ?: 0
                        OpenSecretBoxCta(
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
                    item { Spacer(Modifier.height(24.dp)) }
                }
            }
        }
    }
    if (hashtagSheetVisible) {
        HashtagFilterDropdown(
            hashtags = state.hashtags,
            activeHashtagId = state.selectedHashtagId,
            onSelect = { hashtag -> onSelectHashtag(hashtag?.id) },
            onDismiss = { hashtagSheetVisible = false },
        )
    }
    if (departmentSheetVisible) {
        DepartmentFilterDropdown(
            departments = state.departments,
            activeDepartmentId = state.selectedDepartmentId,
            onSelect = { department -> onSelectDepartment(department?.id) },
            onDismiss = { departmentSheetVisible = false },
        )
    }
    // Suppress unused-parameter lint for Phase 10 spotlight search.
    @Suppress("UNUSED_EXPRESSION")
    SaaCream
    @Suppress("UNUSED_EXPRESSION", "UnusedExpressions")
    listOf(onSpotlightSearchChange)
}
