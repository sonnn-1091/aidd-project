package com.example.aiddproject.home

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
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
import com.example.aiddproject.home.domain.states.NotificationsState
import com.example.aiddproject.home.ui.HomeScreenContent
import com.example.aiddproject.home.ui.HomeUiState
import com.example.aiddproject.home.ui.TEST_TAG_HOME_SCREEN
import com.example.aiddproject.home.ui.components.HomeNavTab
import com.example.aiddproject.ui.theme.AIDDProjectTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/**
 * Compose UI tests for the stateless [HomeScreenContent] (US1, T058). Drives content
 * directly with a fixed [HomeUiState] so the test never touches DI or the real ViewModel
 * — `HomeViewModelTest` covers the VM and `HomeIntegrationTest` will cover the live
 * wire-up. Asserts every hub element from US1 acceptance scenario 4 is visible:
 * SAA logo, ROOT FURTHER tagline, countdown values, theme paragraph, ABOUT AWARD /
 * ABOUT KUDOS buttons, awards section title, and the bottom NavBar with SAA 2025 active.
 */
class HomeScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val ctx = InstrumentationRegistry.getInstrumentation().targetContext

    private fun preEventState(): HomeUiState =
        HomeUiState(
            countdown = CountdownState(days = 233, hours = 7, minutes = 42, isPreEvent = true),
            awards =
                AwardsState.Populated(
                    items =
                        listOf(
                            Award(id = "1", name = "Top Talent Award", thumbnailUrl = null, sortOrder = 0),
                        ),
                ),
            kudos =
                KudosState.Loaded(
                    summary =
                        KudosSummary(
                            isKudosAvailable = true,
                            descriptionText = "",
                        ),
                ),
            notifications = NotificationsState.Loaded(unreadCount = 2),
            language = Language.VN,
        )

    private fun setContent(
        state: HomeUiState = preEventState(),
        onSearchClick: () -> Unit = {},
        onAboutAwardClick: () -> Unit = {},
        onAboutKudosClick: () -> Unit = {},
        onTabSelect: (HomeNavTab) -> Unit = {},
    ) {
        composeRule.setContent {
            AIDDProjectTheme {
                LanguageProvider(language = state.language) {
                    HomeScreenContent(
                        state = state,
                        selectedTab = HomeNavTab.Saa2025,
                        onLanguageSelected = {},
                        onSearchClick = onSearchClick,
                        onBellClick = {},
                        onAboutAwardClick = onAboutAwardClick,
                        onAboutKudosClick = onAboutKudosClick,
                        onAwardChiTietTap = {},
                        onAwardsRetry = {},
                        onKudosChiTietClick = {},
                        onPencilClick = {},
                        onSKudosClick = {},
                        onTabSelect = onTabSelect,
                    )
                }
            }
        }
    }

    @Test
    fun home_screen_renders_with_test_tag() {
        setContent()
        composeRule.onNodeWithTag(TEST_TAG_HOME_SCREEN).assertIsDisplayed()
    }

    @Test
    fun root_further_tagline_is_announced_via_content_description() {
        // Image is brand-fixed; contentDescription is the localized "ROOT FURTHER" string
        // so TalkBack reads it (HomeHero spec § Behavioral Accessibility).
        setContent()
        composeRule
            .onNodeWithContentDescription(ctx.getString(R.string.brand_root_further))
            .assertIsDisplayed()
    }

    @Test
    fun countdown_renders_DAYS_HOURS_MINUTES_labels_and_live_region_label_pre_event() {
        setContent()
        // Phase 11: the countdown digits are split into per-cell Text nodes,
        // so we assert via the live-region merged contentDescription which
        // encodes "233 DAYS, 7 HOURS, 42 MINUTES".
        val daysLabel = ctx.getString(R.string.home_countdown_days_label)
        composeRule
            .onNodeWithContentDescription("233 $daysLabel", substring = true)
            .assertIsDisplayed()
        composeRule.onNodeWithText(ctx.getString(R.string.home_countdown_days_label)).assertIsDisplayed()
        composeRule.onNodeWithText(ctx.getString(R.string.home_countdown_hours_label)).assertIsDisplayed()
        composeRule.onNodeWithText(ctx.getString(R.string.home_countdown_min_label)).assertIsDisplayed()
        composeRule.onNodeWithText(ctx.getString(R.string.home_coming_soon)).assertIsDisplayed()
    }

    @Test
    fun about_award_and_about_kudos_buttons_are_displayed() {
        setContent()
        composeRule.onNodeWithText(ctx.getString(R.string.home_btn_about_award)).assertIsDisplayed()
        composeRule.onNodeWithText(ctx.getString(R.string.home_btn_about_kudos)).assertIsDisplayed()
    }

    @Test
    fun about_award_tap_fires_navigation_callback() {
        var taps = 0
        setContent(onAboutAwardClick = { taps++ })
        composeRule.onNodeWithText(ctx.getString(R.string.home_btn_about_award)).performClick()
        assertEquals(1, taps)
    }

    @Test
    fun theme_paragraph_renders_in_active_locale() {
        setContent()
        // First few words of the VN copy — robust against minor whitespace tweaks.
        composeRule
            .onNodeWithText("Không đơn thuần", substring = true)
            .assertIsDisplayed()
    }

    @Test
    fun awards_section_header_renders_caption_and_title() {
        setContent()
        // Phase 11: section header is two-line (caption + big cream title).
        composeRule
            .onNodeWithText(ctx.getString(R.string.home_section_awards_caption))
            .assertIsDisplayed()
        composeRule
            .onNodeWithText(ctx.getString(R.string.home_section_awards_title))
            .assertIsDisplayed()
    }

    @Test
    fun event_info_block_renders_when_pre_event() {
        // Phase 11 (T107): Thời gian / Địa điểm / livestream tagline.
        setContent()
        composeRule
            .onNodeWithText(ctx.getString(R.string.home_event_time_label))
            .assertIsDisplayed()
        composeRule
            .onNodeWithText(ctx.getString(R.string.home_event_date_value))
            .assertIsDisplayed()
        composeRule
            .onNodeWithText(ctx.getString(R.string.home_event_location_value))
            .assertIsDisplayed()
        composeRule
            .onNodeWithText(ctx.getString(R.string.home_event_livestream))
            .assertIsDisplayed()
    }

    @Test
    fun navbar_renders_SAA_2025_label() {
        setContent()
        composeRule
            .onNodeWithText(ctx.getString(R.string.home_navbar_saa_2025))
            .assertIsDisplayed()
    }

    @Test
    fun search_icon_tap_fires_navigation_callback() {
        var taps = 0
        setContent(onSearchClick = { taps++ })
        composeRule
            .onNodeWithContentDescription(ctx.getString(R.string.a11y_home_search))
            .performClick()
        assertEquals(1, taps)
    }

    @Test
    fun search_icon_double_tap_yields_exactly_one_callback() {
        // TR-005 / SC-002: rapid double-tap must not push two SEARCH destinations
        // on the back stack. The single-click guard on the search IconButton
        // drops the second invocation within the guard window.
        var taps = 0
        setContent(onSearchClick = { taps++ })
        composeRule
            .onNodeWithContentDescription(ctx.getString(R.string.a11y_home_search))
            .performClick()
        composeRule
            .onNodeWithContentDescription(ctx.getString(R.string.a11y_home_search))
            .performClick()
        assertEquals(1, taps)
    }
}
