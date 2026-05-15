package com.example.aiddproject.home

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import com.example.aiddproject.R
import com.example.aiddproject.core.locale.Language
import com.example.aiddproject.core.locale.LanguageProvider
import com.example.aiddproject.home.domain.Award
import com.example.aiddproject.home.domain.KudosSummary
import com.example.aiddproject.home.domain.states.AwardsState
import com.example.aiddproject.home.domain.states.CountdownState
import com.example.aiddproject.home.domain.states.KudosState
import com.example.aiddproject.home.ui.HomeScreenContent
import com.example.aiddproject.home.ui.HomeUiState
import com.example.aiddproject.home.ui.components.HomeBottomBar
import com.example.aiddproject.home.ui.components.HomeNavTab
import com.example.aiddproject.home.ui.components.testTagForTab
import com.example.aiddproject.ui.theme.AIDDProjectTheme
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * Compose UI tests for the bottom NavBar (US3, T066). Covers:
 *  - 4 tabs render with localized labels and SAA 2025 selected by default
 *    (`mms_7_nav bar` § Component Behavior).
 *  - Each tab's `Role.Tab` content description matches the active/inactive a11y
 *    string (TR-009).
 *  - Tap on each tab fires the callback with the correct [HomeNavTab] enum.
 *  - Re-tap of the active SAA 2025 tab scrolls the Home `LazyColumn` to top
 *    (Q-Home-3) when driven through [HomeScreenContent].
 */
class HomeBottomBarTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val ctx = InstrumentationRegistry.getInstrumentation().targetContext

    // -----------------------------------------------------------------------
    // 1. Direct HomeBottomBar tests — labels, role semantics, callback wiring
    // -----------------------------------------------------------------------

    @Test
    fun all_four_tabs_render_with_localized_labels() {
        setBarContent()
        listOf(
            R.string.home_navbar_saa_2025,
            R.string.home_navbar_awards,
            R.string.home_navbar_kudos,
            R.string.home_navbar_profile,
        ).forEach { stringId ->
            composeRule.onNodeWithText(ctx.getString(stringId)).assertIsDisplayed()
        }
    }

    @Test
    fun saa_2025_is_selected_by_default() {
        setBarContent(selected = HomeNavTab.Saa2025)
        composeRule.onNodeWithTag(testTagForTab(HomeNavTab.Saa2025)).assertIsSelected()
    }

    @Test
    fun selected_tab_uses_active_content_description() {
        setBarContent(selected = HomeNavTab.Saa2025)
        val expected =
            ctx.getString(
                R.string.a11y_home_navbar_tab_active,
                ctx.getString(R.string.home_navbar_saa_2025),
            )
        composeRule
            .onNodeWithTag(testTagForTab(HomeNavTab.Saa2025))
            .assertContentDescriptionEquals(expected)
    }

    @Test
    fun unselected_tab_uses_inactive_content_description() {
        setBarContent(selected = HomeNavTab.Saa2025)
        val expected =
            ctx.getString(
                R.string.a11y_home_navbar_tab_inactive,
                ctx.getString(R.string.home_navbar_awards),
            )
        composeRule
            .onNodeWithTag(testTagForTab(HomeNavTab.Awards))
            .assertContentDescriptionEquals(expected)
    }

    @Test
    fun every_tab_exposes_role_tab_semantics() {
        setBarContent()
        HomeNavTab.entries.forEach { tab ->
            composeRule
                .onNodeWithTag(testTagForTab(tab))
                .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Tab))
        }
    }

    @Test
    fun tap_each_tab_emits_callback_with_correct_enum() {
        val taps = mutableListOf<HomeNavTab>()
        setBarContent(onTabSelect = { taps += it })

        composeRule.onNodeWithTag(testTagForTab(HomeNavTab.Awards)).performClick()
        composeRule.onNodeWithTag(testTagForTab(HomeNavTab.Kudos)).performClick()
        composeRule.onNodeWithTag(testTagForTab(HomeNavTab.Profile)).performClick()

        assertEquals(listOf(HomeNavTab.Awards, HomeNavTab.Kudos, HomeNavTab.Profile), taps)
    }

    @Test
    fun retap_of_active_saa_tab_emits_callback_with_saa_2025() {
        val taps = mutableListOf<HomeNavTab>()
        setBarContent(selected = HomeNavTab.Saa2025, onTabSelect = { taps += it })

        composeRule.onNodeWithTag(testTagForTab(HomeNavTab.Saa2025)).performClick()

        assertEquals(listOf(HomeNavTab.Saa2025), taps)
    }

    // -----------------------------------------------------------------------
    // 2. HomeScreenContent integration — scroll-to-top
    // -----------------------------------------------------------------------

    @Test
    fun retap_of_saa_scrolls_lazy_column_to_top() {
        val capturedState = setHomeContent()

        // Programmatically advance the scroll past the top so the re-tap can return.
        runBlocking { capturedState.scrollToItem(5) }
        composeRule.waitForIdle()
        assertTrue(
            "Expected pre-scroll to advance firstVisibleItemIndex away from 0",
            capturedState.firstVisibleItemIndex > 0,
        )

        composeRule.onNodeWithTag(testTagForTab(HomeNavTab.Saa2025)).performClick()
        composeRule.waitUntil(timeoutMillis = 2_000) {
            capturedState.firstVisibleItemIndex == 0
        }

        assertEquals(0, capturedState.firstVisibleItemIndex)
        assertEquals(0, capturedState.firstVisibleItemScrollOffset)
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private fun setBarContent(
        selected: HomeNavTab = HomeNavTab.Saa2025,
        onTabSelect: (HomeNavTab) -> Unit = {},
    ) {
        composeRule.setContent {
            AIDDProjectTheme {
                LanguageProvider(language = Language.VN) {
                    HomeBottomBar(selected = selected, onTabSelect = onTabSelect)
                }
            }
        }
    }

    /**
     * Builds a [HomeScreenContent] under test and returns the [LazyListState]
     * captured from its root LazyColumn so the caller can drive scroll position
     * deterministically.
     */
    private fun setHomeContent(
        onTabSelect: (HomeNavTab) -> Unit = {},
    ): LazyListState {
        lateinit var captured: LazyListState
        composeRule.setContent {
            AIDDProjectTheme {
                LanguageProvider(language = Language.VN) {
                    val lazyListState = rememberLazyListState()
                    captured = lazyListState
                    HomeScreenContent(
                        state = preEventState(),
                        selectedTab = HomeNavTab.Saa2025,
                        onLanguageSelected = {},
                        onSearchClick = {},
                        onBellClick = {},
                        onAboutAwardClick = {},
                        onAboutKudosClick = {},
                        onAwardChiTietTap = {},
                        onAwardsRetry = {},
                        onKudosChiTietClick = {},
                        onPencilClick = {},
                        onSKudosClick = {},
                        onTabSelect = onTabSelect,
                        lazyListState = lazyListState,
                    )
                }
            }
        }
        composeRule.waitForIdle()
        return captured
    }

    private fun preEventState(): HomeUiState =
        HomeUiState(
            countdown = CountdownState(days = 233, hours = 7, minutes = 42, isPreEvent = true),
            awards =
                AwardsState.Populated(
                    items =
                        (1..10).map { i ->
                            Award(id = "a$i", name = "Award $i", thumbnailUrl = null, sortOrder = i)
                        },
                ),
            kudos = KudosState.Loaded(KudosSummary(isKudosAvailable = true, descriptionText = "")),
            unreadCount = 2,
            language = Language.VN,
        )
}
