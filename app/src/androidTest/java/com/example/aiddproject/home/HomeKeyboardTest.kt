package com.example.aiddproject.home

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.test.requestFocus
import com.example.aiddproject.core.locale.Language
import com.example.aiddproject.core.locale.LanguageProvider
import com.example.aiddproject.home.domain.Award
import com.example.aiddproject.home.domain.KudosSummary
import com.example.aiddproject.home.domain.states.AwardsState
import com.example.aiddproject.home.domain.states.CountdownState
import com.example.aiddproject.home.domain.states.KudosState
import com.example.aiddproject.home.ui.HomeScreenContent
import com.example.aiddproject.home.ui.HomeUiState
import com.example.aiddproject.home.ui.components.HomeNavTab
import com.example.aiddproject.home.ui.components.TEST_TAG_HOME_BELL
import com.example.aiddproject.home.ui.components.TEST_TAG_HOME_FAB_PENCIL
import com.example.aiddproject.home.ui.components.TEST_TAG_HOME_FAB_SKUDOS
import com.example.aiddproject.home.ui.components.TEST_TAG_HOME_SEARCH
import com.example.aiddproject.home.ui.components.testTagForTab
import com.example.aiddproject.ui.theme.AIDDProjectTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * T095 — Keyboard parity (spec § Behavioral Accessibility): every interactive
 * control on Home MUST be reachable + activatable via keyboard. Mirrors
 * Login's `LoginKeyboardTest` pattern: focus the node programmatically, press
 * `Enter`, and assert the same callback fires as a tap.
 *
 * `requestFocus()` is annotated `@ExperimentalTestApi`; opt-in is at the
 * class level.
 */
@OptIn(ExperimentalTestApi::class)
class HomeKeyboardTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val populatedState =
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
            unreadCount = 2,
            language = Language.VN,
        )

    @Test
    fun pressing_Enter_on_focused_search_icon_fires_search_callback() {
        var taps = 0
        setHomeContent(onSearchClick = { taps++ })
        pressEnterOnTag(TEST_TAG_HOME_SEARCH)
        assertTrue("Enter on focused search must invoke onSearchClick (got $taps)", taps >= 1)
    }

    @Test
    fun pressing_Enter_on_focused_bell_fires_bell_callback() {
        var taps = 0
        setHomeContent(onBellClick = { taps++ })
        pressEnterOnTag(TEST_TAG_HOME_BELL)
        assertTrue("Enter on focused bell must invoke onBellClick (got $taps)", taps >= 1)
    }

    @Test
    fun pressing_Enter_on_focused_fab_pencil_fires_pencil_callback() {
        var taps = 0
        setHomeContent(onPencilClick = { taps++ })
        pressEnterOnTag(TEST_TAG_HOME_FAB_PENCIL)
        assertTrue("Enter on focused FAB pencil must invoke onPencilClick (got $taps)", taps >= 1)
    }

    @Test
    fun pressing_Enter_on_focused_fab_skudos_fires_skudos_callback() {
        var taps = 0
        setHomeContent(onSKudosClick = { taps++ })
        pressEnterOnTag(TEST_TAG_HOME_FAB_SKUDOS)
        assertTrue("Enter on focused FAB S/Kudos must invoke onSKudosClick (got $taps)", taps >= 1)
    }

    @Test
    fun pressing_Enter_on_focused_awards_navbar_tab_fires_tab_select() {
        val taps = mutableListOf<HomeNavTab>()
        setHomeContent(onTabSelect = { taps += it })
        pressEnterOnTag(testTagForTab(HomeNavTab.Awards))
        assertTrue(
            "Enter on focused Awards tab must invoke onTabSelect (got $taps)",
            taps.contains(HomeNavTab.Awards),
        )
    }

    private fun pressEnterOnTag(tag: String) {
        composeRule.onNodeWithTag(tag).requestFocus()
        composeRule.onNodeWithTag(tag).performKeyInput { pressKey(Key.Enter) }
    }

    private fun setHomeContent(
        onSearchClick: () -> Unit = {},
        onBellClick: () -> Unit = {},
        onPencilClick: () -> Unit = {},
        onSKudosClick: () -> Unit = {},
        onTabSelect: (HomeNavTab) -> Unit = {},
    ) {
        composeRule.setContent {
            AIDDProjectTheme {
                LanguageProvider(language = Language.VN) {
                    HomeScreenContent(
                        state = populatedState,
                        selectedTab = HomeNavTab.Saa2025,
                        onLanguageSelected = {},
                        onSearchClick = onSearchClick,
                        onBellClick = onBellClick,
                        onAboutAwardClick = {},
                        onAboutKudosClick = {},
                        onAwardChiTietTap = {},
                        onAwardsRetry = {},
                        onKudosChiTietClick = {},
                        onPencilClick = onPencilClick,
                        onSKudosClick = onSKudosClick,
                        onTabSelect = onTabSelect,
                    )
                }
            }
        }
    }
}
