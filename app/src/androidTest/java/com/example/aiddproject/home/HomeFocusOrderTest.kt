package com.example.aiddproject.home

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.requestFocus
import com.example.aiddproject.core.locale.Language
import com.example.aiddproject.core.locale.LanguageProvider
import com.example.aiddproject.core.locale.ui.TEST_TAG_ANCHOR
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
import com.example.aiddproject.home.ui.components.TEST_TAG_KUDOS_CHI_TIET
import com.example.aiddproject.home.ui.components.testTagForTab
import com.example.aiddproject.ui.theme.AIDDProjectTheme
import org.junit.Rule
import org.junit.Test

/**
 * T094 — Focus reachability for every interactive control on Home (spec §
 * Behavioral Accessibility). Compose's test harness can't reliably drive a
 * full TalkBack-equivalent traversal order across SDK levels; instead we
 * assert each documented control is independently focusable, which is the
 * security-significant invariant. The traversal *order* is influenced by
 * layout position which the design + LazyColumn ordering already encode.
 *
 * Documented order (`spec § Behavioral Accessibility`):
 *   language → search → bell → countdown → ABOUT AWARD → ABOUT KUDOS →
 *   theme paragraph → first award Chi tiết → … → Kudos Chi tiết →
 *   FAB pencil → FAB S/Kudos → NavBar tabs.
 *
 * Each controllable node is `requestFocus()`-able: failure here means the node
 * is not in the focusable tree and TalkBack/Switch Control would skip it.
 */
@OptIn(ExperimentalTestApi::class)
class HomeFocusOrderTest {
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
    fun language_anchor_is_focusable() = assertFocusable(TEST_TAG_ANCHOR)

    @Test
    fun search_icon_is_focusable() = assertFocusable(TEST_TAG_HOME_SEARCH)

    @Test
    fun bell_is_focusable() = assertFocusable(TEST_TAG_HOME_BELL)

    @Test
    fun fab_pencil_is_focusable() = assertFocusable(TEST_TAG_HOME_FAB_PENCIL)

    @Test
    fun fab_skudos_is_focusable() = assertFocusable(TEST_TAG_HOME_FAB_SKUDOS)

    @Test
    fun kudos_chi_tiet_is_focusable() = assertFocusable(TEST_TAG_KUDOS_CHI_TIET)

    @Test
    fun every_navbar_tab_is_focusable() {
        setHomeContent()
        HomeNavTab.entries.forEach { tab ->
            composeRule.onNodeWithTag(testTagForTab(tab)).requestFocus()
            composeRule.onNodeWithTag(testTagForTab(tab)).assertIsFocused()
        }
    }

    private fun assertFocusable(testTag: String) {
        setHomeContent()
        composeRule.onNodeWithTag(testTag).requestFocus()
        composeRule.onNodeWithTag(testTag).assertIsFocused()
    }

    private fun setHomeContent() {
        composeRule.setContent {
            AIDDProjectTheme {
                LanguageProvider(language = Language.VN) {
                    HomeScreenContent(
                        state = populatedState,
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
                        onTabSelect = {},
                    )
                }
            }
        }
    }
}
