package com.example.aiddproject.awarddetail.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aiddproject.R
import com.example.aiddproject.awarddetail.domain.states.AwardDetailState
import com.example.aiddproject.awarddetail.ui.components.AwardHeroBlock
import com.example.aiddproject.awarddetail.ui.components.AwardInfoBlock
import com.example.aiddproject.awarddetail.ui.components.HighlightBlock
import com.example.aiddproject.awarddetail.ui.components.KvKudosBanner
import com.example.aiddproject.core.locale.Language
import com.example.aiddproject.core.ui.rememberSingleClickHandler
import com.example.aiddproject.home.domain.Award
import com.example.aiddproject.home.domain.KudosSummary
import com.example.aiddproject.home.domain.states.KudosState
import com.example.aiddproject.home.ui.components.HomeBottomBar
import com.example.aiddproject.home.ui.components.HomeHeader
import com.example.aiddproject.home.ui.components.HomeNavTab
import com.example.aiddproject.home.ui.components.KudosSection
import com.example.aiddproject.ui.theme.SaaCream
import kotlinx.coroutines.launch

/**
 * Stateless layout for the Award Detail screen. The full Scaffold +
 * sticky chrome lands here in Phase 3; Phases 5/6 swap stub callbacks
 * for real navigation handlers without touching this composable.
 *
 * Body sections (top → bottom):
 *  1. KV Kudos banner (Figma `mms_A_KV Kudos` `6885:10266`)
 *  2. Highlight block (Figma `mms_B_Highlight` `6885:10283` + header
 *     `6885:10284`)
 *  3. Hero block — badge image + title row (Figma `mms_2.3_award`
 *     `6885:10292`)
 *  4. Info block — description + recipient count + prize value
 *  5. Sun*Kudos promo block (`KudosSection` reused from `home/ui/`)
 *
 * Sticky chrome: `topBar = HomeHeader`, `bottomBar = HomeBottomBar`
 * per FR-014. The body inside the Scaffold scrolls; the chrome stays.
 */
@Composable
fun AwardDetailScreenContent(
    state: AwardDetailUiState,
    onRetry: () -> Unit,
    onLanguageSelected: (Language) -> Unit,
    onSearchClick: () -> Unit,
    onBellClick: () -> Unit,
    onTabSelect: (HomeNavTab) -> Unit,
    onCategorySelected: (Award) -> Unit,
    onKudosChiTietClick: () -> Unit,
    modifier: Modifier = Modifier,
    lazyListState: LazyListState = rememberLazyListState(),
) {
    val scope = rememberCoroutineScope()
    val tabSelectHandler: (HomeNavTab) -> Unit = { tab ->
        // Re-tap of the active Awards tab scrolls the body to the top
        // per Resolved Q2 / US3 acceptance scenario 4. Mirrors Home's
        // SAA-tab re-tap behaviour.
        if (tab == HomeNavTab.Awards) {
            scope.launch { lazyListState.animateScrollToItem(0) }
        }
        onTabSelect(tab)
    }
    Box(modifier = modifier.fillMaxSize().testTag(TEST_TAG_AWARD_DETAIL_SCREEN)) {
        // Full-bleed keyvisual + top gradient overlay sit OUTSIDE the
        // Scaffold so they render under topBar + body + bottomBar.
        // This is what lets HomeBottomBar's 15% SaaCream tint composite
        // visibly over the keyvisual (T097) and gives HomeHeader its
        // dark gradient backdrop (T096). Mirrors the pattern Home uses.
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
                            colors =
                                listOf(
                                    Color(0xE600101A),
                                    Color.Transparent,
                                ),
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
                    unreadCount = state.unreadCount,
                    // Scaffold's topBar slot doesn't auto-apply status-bar
                    // insets — without this, HomeHeader renders under the
                    // system status bar and the OS intercepts taps on the
                    // bell / search / language pill.
                    modifier = Modifier.statusBarsPadding(),
                )
            },
            bottomBar = {
                HomeBottomBar(
                    selected = HomeNavTab.Awards,
                    onTabSelect = tabSelectHandler,
                )
            },
        ) { padding ->
            Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                // Render body keyed on the detail state — Loading/Error
                // sections occupy the same vertical region as the populated
                // body so the screen height is stable across transitions.
                when (val detail = state.detail) {
                    is AwardDetailState.Loading -> BodyLoading()
                    is AwardDetailState.Error -> BodyError(messageRes = detail.messageRes, onRetry = onRetry)
                    is AwardDetailState.Loaded -> {
                        val activeName = detail.detail.name
                        LazyColumn(
                            state = lazyListState,
                            modifier = Modifier.fillMaxSize(),
                        ) {
                            item { KvKudosBanner() }
                            item {
                                HighlightBlock(
                                    categories = state.categories,
                                    activeAwardId = state.activeAwardId,
                                    onCategorySelected = onCategorySelected,
                                )
                            }
                            item {
                                AwardHeroBlock(
                                    awardName = activeName,
                                    imageUrl = detail.detail.imageUrl,
                                )
                            }
                            item {
                                AwardInfoBlock(
                                    awardName = activeName,
                                    description = detail.detail.description,
                                    quantity = detail.detail.quantity,
                                    quantityUnit = detail.detail.quantityUnit,
                                    prizeValue = detail.detail.prizeValue,
                                    prizeCaption = detail.detail.prizeCaption,
                                    prizeValueTeam = detail.detail.prizeValueTeam,
                                    prizeCaptionTeam = detail.detail.prizeCaptionTeam,
                                )
                            }
                            item { Spacer(Modifier.height(16.dp)) }
                            item {
                                KudosSection(
                                    state =
                                        KudosState.Loaded(
                                            KudosSummary(
                                                isKudosAvailable = true,
                                                bannerImageUrl = null,
                                                badgeText = null,
                                                descriptionText = "",
                                            ),
                                        ),
                                    onChiTietClick = onKudosChiTietClick,
                                )
                            }
                            item { Spacer(Modifier.height(24.dp)) }
                        }
                        // `state.categories` is consumed by the dropdown wired in Phase 4.
                    }
                }
            }
        }
    }
}

@Composable
private fun BodyLoading() {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .testTag(TEST_TAG_AWARD_DETAIL_LOADING),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        CircularProgressIndicator(color = SaaCream)
    }
}

@Composable
private fun BodyError(
    messageRes: Int,
    onRetry: () -> Unit,
) {
    val retryClick = rememberSingleClickHandler(onClick = onRetry)
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .testTag(TEST_TAG_AWARD_DETAIL_ERROR),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(messageRes),
            color = Color.White,
            style =
                MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                ),
        )
        Spacer(Modifier.height(16.dp))
        TextButton(
            onClick = retryClick,
            modifier = Modifier.testTag(TEST_TAG_AWARD_DETAIL_RETRY),
        ) {
            Text(
                text = stringResource(R.string.home_action_retry),
                color = SaaCream,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            )
        }
    }
}

const val TEST_TAG_AWARD_DETAIL_SCREEN: String = "award_detail_screen"
const val TEST_TAG_AWARD_DETAIL_LOADING: String = "award_detail_loading"
const val TEST_TAG_AWARD_DETAIL_ERROR: String = "award_detail_error"
const val TEST_TAG_AWARD_DETAIL_RETRY: String = "award_detail_retry"
