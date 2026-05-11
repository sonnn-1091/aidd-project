package com.example.aiddproject.home

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.example.aiddproject.awarddetail.domain.AwardDetail
import com.example.aiddproject.awarddetail.domain.states.AwardDetailState
import com.example.aiddproject.awarddetail.ui.AwardDetailScreenContent
import com.example.aiddproject.awarddetail.ui.AwardDetailUiState
import com.example.aiddproject.core.locale.Language
import com.example.aiddproject.home.domain.Award
import com.example.aiddproject.home.domain.states.AwardsState
import com.example.aiddproject.home.ui.components.HomeNavTab
import com.example.aiddproject.home.ui.components.testTagForTab
import com.example.aiddproject.ui.theme.AIDDProjectTheme
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/**
 * Compose UI tests for the bottom-nav **Awards** tab entry-point on
 * the Award Detail screen. Drives the stateless
 * [AwardDetailScreenContent] directly (no Hilt; no full navigation
 * graph). The "tap Awards from another screen → mount Award Detail"
 * journey is covered indirectly: the equivalent stateless contract
 * is "Awards tap → `onTabSelect(HomeNavTab.Awards)` fires" — i.e.
 * the navigation callback is wired correctly. Full navigation graph
 * integration is verified by manual smoke (canonical commit
 * `0293084`).
 *
 * Two scenarios:
 * - **Awards re-tap scrolls body to top** (canonical T101 acceptance,
 *   shipped in `0293084`). Mirrors Home's SAA-tab re-tap behaviour.
 * - **Awards tap fires onTabSelect callback** — the basic wiring
 *   guarantee so the screen's host can route to the right destination
 *   regardless of which screen the tap originated on.
 */
class BottomNavAwardsTabTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val topTalent =
        AwardDetail(
            id = "a01",
            name = "Top Talent",
            description = "Test description.",
            quantity = 10,
            quantityUnit = "Cá nhân",
            prizeValue = "7.000.000 VNĐ",
            imageUrl = null,
            sortOrder = 1,
        )

    private val populatedCategories =
        AwardsState.Populated(
            items =
                listOf(
                    Award(id = "a01", name = "Top Talent Award", thumbnailUrl = null, sortOrder = 1),
                    Award(id = "a02", name = "Top Project Award", thumbnailUrl = null, sortOrder = 2),
                ),
        )

    private fun loadedState(): AwardDetailUiState =
        AwardDetailUiState(
            activeAwardId = topTalent.id,
            detail = AwardDetailState.Loaded(topTalent),
            categories = populatedCategories,
            unreadCount = 0,
            language = Language.VN,
        )

    @Test
    fun awards_tab_tap_fires_onTabSelect_callback_with_awards() {
        var lastTab: HomeNavTab? = null
        composeRule.setContent {
            AIDDProjectTheme {
                AwardDetailScreenContent(
                    state = loadedState(),
                    onRetry = {},
                    onLanguageSelected = {},
                    onSearchClick = {},
                    onBellClick = {},
                    onTabSelect = { lastTab = it },
                    onCategorySelected = {},
                    onKudosChiTietClick = {},
                )
            }
        }

        composeRule.onNodeWithTag(testTagForTab(HomeNavTab.Awards)).assertIsDisplayed()
        composeRule.onNodeWithTag(testTagForTab(HomeNavTab.Awards)).performClick()
        composeRule.waitForIdle()

        assertEquals(HomeNavTab.Awards, lastTab)
    }

    @Test
    fun awards_tab_retap_scrolls_body_to_top() =
        runTest {
            val lazyListState = LazyListState(firstVisibleItemIndex = 3, firstVisibleItemScrollOffset = 0)
            composeRule.setContent {
                AIDDProjectTheme {
                    AwardDetailScreenContent(
                        state = loadedState(),
                        onRetry = {},
                        onLanguageSelected = {},
                        onSearchClick = {},
                        onBellClick = {},
                        onTabSelect = {},
                        onCategorySelected = {},
                        onKudosChiTietClick = {},
                        lazyListState = lazyListState,
                    )
                }
            }

            composeRule.onNodeWithTag(testTagForTab(HomeNavTab.Awards)).performClick()
            composeRule.waitForIdle()
            // animateScrollToItem(0) inside tabSelectHandler completes under
            // the test clock; firstVisibleItemIndex returns to 0.
            assertEquals(0, lazyListState.firstVisibleItemIndex)
        }
}
