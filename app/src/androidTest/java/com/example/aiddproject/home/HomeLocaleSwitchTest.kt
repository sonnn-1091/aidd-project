package com.example.aiddproject.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import com.example.aiddproject.R
import com.example.aiddproject.auth.login.ui.components.TEST_TAG_ANCHOR
import com.example.aiddproject.auth.login.ui.components.menuItemTag
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
import com.example.aiddproject.home.ui.components.HomeNavTab
import com.example.aiddproject.ui.theme.AIDDProjectTheme
import org.junit.Rule
import org.junit.Test

/**
 * Locale-switch coverage for Home (US7, T090). Asserts that flipping the
 * [Language] state inside [LanguageProvider] re-renders every localizable
 * string in the same composition tree — no Activity recreation, the same
 * `LocalConfiguration`/`LocalContext` overlay handles it (SC-004).
 *
 * Three representative strings are sampled:
 *  - `home_theme_paragraph` (long body copy in the hero)
 *  - `home_navbar_awards` (NavBar tab label)
 *  - `home_link_chi_tiet` (Chi tiết / Details — used in Awards + Kudos)
 *
 * `home_section_awards_caption` ("Sun* Annual Awards 2025") is brand-fixed
 * and identical in every locale, so a string-equality check can't tell the
 * locales apart — we don't sample it here. Phase 11's new
 * `home_section_awards_title` ("Hệ thống giải thưởng" → "Awards system")
 * does differ across locales but the EN/JA values are still
 * translator-pickup placeholders, so the strict string assertion is left
 * to the next translation pass.
 */
class HomeLocaleSwitchTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val ctx = InstrumentationRegistry.getInstrumentation().targetContext

    private val homeState =
        HomeUiState(
            countdown = CountdownState(days = 200, hours = 5, minutes = 30, isPreEvent = true),
            awards =
                AwardsState.Populated(
                    items =
                        listOf(
                            Award(id = "a1", name = "Top Talent", thumbnailUrl = null, sortOrder = 0),
                        ),
                ),
            kudos = KudosState.Loaded(KudosSummary(isKudosAvailable = true, descriptionText = "")),
            notifications = NotificationsState.Loaded(unreadCount = 0),
            language = Language.VN,
        )

    @Test
    fun switching_VN_to_EN_re_renders_localizable_text_without_recreation() {
        composeRule.setContent {
            // Local mutable language so we can flip it from the test thread —
            // simulates the LocalePreference DataStore signal that drives
            // LanguageProvider in production.
            var language by remember { mutableStateOf(Language.VN) }
            AIDDProjectTheme {
                LanguageProvider(language = language) {
                    HomeScreenContent(
                        state = homeState.copy(language = language),
                        selectedTab = HomeNavTab.Saa2025,
                        onLanguageSelected = { language = it },
                        onSearchClick = {},
                        onBellClick = {},
                        onAboutAwardClick = {},
                        onAboutKudosClick = {},
                        onAwardChiTietTap = {},
                        onAwardsRetry = {},
                        onKudosChiTietClick = {},
                        onPencilClick = {},
                        onSKudosClick = {},
                        onTabSelect = {},
                    )
                }
            }
        }

        // VN baseline — first words of the theme paragraph + VN navbar label
        // + VN Chi tiết link.
        composeRule
            .onNodeWithText("Không đơn thuần", substring = true)
            .assertIsDisplayed()
        composeRule
            .onNodeWithText(stringForLocale(R.string.home_navbar_awards, Language.VN))
            .assertIsDisplayed()
        composeRule
            .onNodeWithText(stringForLocale(R.string.home_link_chi_tiet, Language.VN))
            .assertIsDisplayed()

        // Open the dropdown and select EN through the same callback the screen
        // wires — this routes through the overlay providers without an Activity
        // recreation.
        composeRule.onNodeWithTag(TEST_TAG_ANCHOR).performClick()
        composeRule.onNodeWithTag(menuItemTag(Language.EN)).performClick()
        composeRule.waitForIdle()

        // After the flip, every localizable string in the same composition tree
        // must read its EN value.
        composeRule
            .onNodeWithText("More than just a name", substring = true)
            .assertIsDisplayed()
        composeRule
            .onNodeWithText(stringForLocale(R.string.home_navbar_awards, Language.EN))
            .assertIsDisplayed()
        composeRule
            .onNodeWithText(stringForLocale(R.string.home_link_chi_tiet, Language.EN))
            .assertIsDisplayed()
    }

    /**
     * Resolves [resId] against the locale we want to compare against, regardless
     * of the device locale the test happens to run under. Mirrors the same
     * `createConfigurationContext()` trick `LanguageProvider` uses internally.
     */
    private fun stringForLocale(
        resId: Int,
        language: Language,
    ): String {
        val configuration = android.content.res.Configuration(ctx.resources.configuration)
        configuration.setLocale(language.toLocale())
        return ctx.createConfigurationContext(configuration).getString(resId)
    }
}
