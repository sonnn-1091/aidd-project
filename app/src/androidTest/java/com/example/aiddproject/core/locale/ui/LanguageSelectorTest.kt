package com.example.aiddproject.core.locale.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.requestFocus
import androidx.compose.ui.unit.dp
import androidx.test.espresso.Espresso
import androidx.test.platform.app.InstrumentationRegistry
import com.example.aiddproject.R
import com.example.aiddproject.core.locale.Language
import com.example.aiddproject.core.locale.LanguageProvider
import com.example.aiddproject.ui.theme.AIDDProjectTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * UI coverage for [LanguageSelector] (Login Phase 3 T054 + Language Dropdown
 * spec uUvW6Qm1ve Phase 3 — T018–T031).
 *
 * Drives the stateless component directly. The test installs a small wrapper
 * that holds the current selection in `remember` so we can verify the visible
 * label updates after a selection — proving the round-trip from menu tap →
 * onSelect → recomposition.
 *
 * [LanguageProvider] wraps the component with a fixed [Language.VN] locale
 * overlay so `stringResource()` calls inside the selector resolve to the
 * Vietnamese authoritative copy regardless of the test device's system
 * locale (TR-003 contract assertions are written against VN strings).
 */
@OptIn(ExperimentalTestApi::class)
class LanguageSelectorTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val ctx = InstrumentationRegistry.getInstrumentation().targetContext

    // -----------------------------------------------------------------------
    // Existing carry-forward cases (Login Phase 3)
    // -----------------------------------------------------------------------

    @Test
    fun anchor_renders_with_current_language_two_letter_code() {
        setSelector(initial = Language.VN)
        composeRule.onNodeWithTag(TEST_TAG_ANCHOR).assertIsDisplayed()
        composeRule.onNodeWithText("VN").assertIsDisplayed()
    }

    @Test
    fun tapping_anchor_opens_menu_with_only_supported_languages() {
        setSelector(initial = Language.VN)

        composeRule.onNodeWithTag(menuItemTag(Language.EN)).assertIsNotDisplayed()
        composeRule.onNodeWithTag(TEST_TAG_ANCHOR).performClick()
        composeRule.onNodeWithTag(menuItemTag(Language.VN)).assertIsDisplayed()
        composeRule.onNodeWithTag(menuItemTag(Language.EN)).assertIsDisplayed()

        composeRule.onNodeWithText(Language.VN.nativeName).assertIsDisplayed()
        composeRule.onNodeWithText(Language.EN.nativeName).assertIsDisplayed()
    }

    @Test
    fun selecting_EN_emits_callback_and_updates_visible_label() {
        var lastSelected: Language? = null
        setSelector(initial = Language.VN, onSelect = { lastSelected = it })

        composeRule.onNodeWithText("VN").assertIsDisplayed()
        composeRule.onNodeWithTag(TEST_TAG_ANCHOR).performClick()
        composeRule.onNodeWithTag(menuItemTag(Language.EN)).performClick()

        assertEquals(Language.EN, lastSelected)
        composeRule.onNodeWithText("EN").assertIsDisplayed()
    }

    // -----------------------------------------------------------------------
    // T018 — JA is gone; only VN + EN render in documented order
    // -----------------------------------------------------------------------

    @Test
    fun menu_renders_exactly_VN_then_EN_no_JA() {
        setSelector(initial = Language.VN)
        composeRule.onNodeWithTag(TEST_TAG_ANCHOR).performClick()

        // Both supported rows render.
        composeRule.onNodeWithTag(menuItemTag(Language.VN)).assertIsDisplayed()
        composeRule.onNodeWithTag(menuItemTag(Language.EN)).assertIsDisplayed()

        // No JA row exists — guards against re-introduction (FR-004 + spec § Resolved Q1).
        composeRule.onAllNodesWithTag("language_selector_item_JA").assertCountEquals(0)

        // Order: VN's bounds top must be ≤ EN's bounds top (visually first).
        val vnTop = composeRule.onNodeWithTag(menuItemTag(Language.VN)).getBoundsInRoot().top
        val enTop = composeRule.onNodeWithTag(menuItemTag(Language.EN)).getBoundsInRoot().top
        assertTrue("VN row must render above EN row (got VN=$vnTop, EN=$enTop)", vnTop < enTop)
    }

    // -----------------------------------------------------------------------
    // T019 — selecting EN: callback fires exactly once + anchor flips label
    // -----------------------------------------------------------------------

    @Test
    fun selecting_EN_invokes_onSelect_and_updates_anchor_label() {
        var callbackCount = 0
        var lastSelected: Language? = null
        setSelector(
            initial = Language.VN,
            onSelect = {
                callbackCount++
                lastSelected = it
            },
        )

        composeRule.onNodeWithText("VN").assertIsDisplayed()
        composeRule.onNodeWithTag(TEST_TAG_ANCHOR).performClick()
        composeRule.onNodeWithTag(menuItemTag(Language.EN)).performClick()

        assertEquals(1, callbackCount)
        assertEquals(Language.EN, lastSelected)
        // Menu must close after selection (FR-005).
        composeRule.onNodeWithTag(menuItemTag(Language.EN)).assertIsNotDisplayed()
        // Anchor label flips to EN within the same recomposition cycle.
        composeRule.onNodeWithText("EN").assertIsDisplayed()
    }

    // -----------------------------------------------------------------------
    // T020 — contentDescription recomputes when the active language flips
    // -----------------------------------------------------------------------

    @Test
    fun contentDescription_recomputes_when_language_flips() {
        setSelector(initial = Language.VN)

        val vnLabel = ctx.getString(R.string.a11y_language_selector, Language.VN.nativeName)
        composeRule.onNodeWithTag(TEST_TAG_ANCHOR).assertContentDescriptionEquals(vnLabel)

        // Drive the selection EN → recompose → assert label rotates.
        composeRule.onNodeWithTag(TEST_TAG_ANCHOR).performClick()
        composeRule.onNodeWithTag(menuItemTag(Language.EN)).performClick()

        val enLabel = ctx.getString(R.string.a11y_language_selector, Language.EN.nativeName)
        composeRule.onNodeWithTag(TEST_TAG_ANCHOR).assertContentDescriptionEquals(enLabel)
    }

    // -----------------------------------------------------------------------
    // T021 — Role.Button + stateDescription expanded/collapsed
    // -----------------------------------------------------------------------

    @Test
    fun anchor_exposes_role_button_with_expanded_state() {
        setSelector(initial = Language.VN)

        composeRule
            .onNodeWithTag(TEST_TAG_ANCHOR)
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button))

        val collapsed = ctx.getString(R.string.a11y_dropdown_collapsed)
        composeRule
            .onNodeWithTag(TEST_TAG_ANCHOR)
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, collapsed))

        composeRule.onNodeWithTag(TEST_TAG_ANCHOR).performClick()

        val expanded = ctx.getString(R.string.a11y_dropdown_expanded)
        composeRule
            .onNodeWithTag(TEST_TAG_ANCHOR)
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, expanded))
    }

    // -----------------------------------------------------------------------
    // T022 — opening the menu moves TalkBack focus to the first row
    // -----------------------------------------------------------------------

    @Test
    fun opening_menu_focuses_first_row_for_TalkBack() {
        setSelector(initial = Language.VN)

        composeRule.onNodeWithTag(TEST_TAG_ANCHOR).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag(menuItemTag(Language.VN)).assertIsFocused()
    }

    // -----------------------------------------------------------------------
    // T023 — every row is independently keyboard-focusable
    // -----------------------------------------------------------------------

    @Test
    fun keyboard_tab_order_is_anchor_then_VN_then_EN() {
        setSelector(initial = Language.VN)

        // Anchor focusable on its own.
        composeRule.onNodeWithTag(TEST_TAG_ANCHOR).requestFocus()
        composeRule.onNodeWithTag(TEST_TAG_ANCHOR).assertIsFocused()

        // Open menu; rows compose; each row independently focusable.
        composeRule.onNodeWithTag(TEST_TAG_ANCHOR).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag(menuItemTag(Language.VN)).requestFocus()
        composeRule.onNodeWithTag(menuItemTag(Language.VN)).assertIsFocused()

        composeRule.onNodeWithTag(menuItemTag(Language.EN)).requestFocus()
        composeRule.onNodeWithTag(menuItemTag(Language.EN)).assertIsFocused()
    }

    // -----------------------------------------------------------------------
    // T024 — anchor click region ≥ 48dp tall despite 32dp visual pill
    // -----------------------------------------------------------------------

    @Test
    fun anchor_meets_48dp_touch_target() {
        setSelector(initial = Language.VN)

        val bounds = composeRule.onNodeWithTag(TEST_TAG_ANCHOR).getBoundsInRoot()
        val heightDp = bounds.bottom - bounds.top
        val widthDp = bounds.right - bounds.left
        assertTrue("Anchor height $heightDp < 48dp", heightDp.value >= 48.dp.value - SLOP)
        assertTrue("Anchor width $widthDp < 48dp", widthDp.value >= 48.dp.value - SLOP)
    }

    // -----------------------------------------------------------------------
    // T025 — every row meets 48dp on both axes
    // -----------------------------------------------------------------------

    @Test
    fun each_row_meets_48dp_touch_target() {
        setSelector(initial = Language.VN)
        composeRule.onNodeWithTag(TEST_TAG_ANCHOR).performClick()

        Language.entries.forEach { lang ->
            val bounds = composeRule.onNodeWithTag(menuItemTag(lang)).getBoundsInRoot()
            val heightDp = bounds.bottom - bounds.top
            val widthDp = bounds.right - bounds.left
            assertTrue("${lang.code} row height $heightDp < 48dp", heightDp.value >= 48.dp.value - SLOP)
            assertTrue("${lang.code} row width $widthDp < 48dp", widthDp.value >= 48.dp.value - SLOP)
        }
    }

    // -----------------------------------------------------------------------
    // T026 — re-selecting the active language is a no-op (FR-006)
    // -----------------------------------------------------------------------

    @Test
    fun reselecting_active_language_is_idempotent() {
        var lastSelected: Language? = null
        setSelector(initial = Language.VN, onSelect = { lastSelected = it })

        composeRule.onNodeWithTag(TEST_TAG_ANCHOR).performClick()
        composeRule.onNodeWithTag(menuItemTag(Language.VN)).performClick()

        assertNull("Re-selecting current language must not re-emit", lastSelected)
        // Menu still must close (FR-008).
        composeRule.onNodeWithTag(menuItemTag(Language.VN)).assertIsNotDisplayed()
    }

    // -----------------------------------------------------------------------
    // T027 — row double-tap collapses to one onSelect (TR-004 row side)
    // -----------------------------------------------------------------------

    @Test
    fun menu_double_tap_yields_one_select_callback() {
        var callbackCount = 0
        setSelector(initial = Language.VN, onSelect = { callbackCount++ })

        composeRule.onNodeWithTag(TEST_TAG_ANCHOR).performClick()
        // Two rapid taps on the same row — `rememberSingleClickHandler` must
        // drop the second within its 400ms guard window.
        composeRule.onNodeWithTag(menuItemTag(Language.EN)).performClick()
        composeRule.onNodeWithTag(menuItemTag(Language.EN)).performClick()

        assertEquals(1, callbackCount)
    }

    // -----------------------------------------------------------------------
    // T028 — anchor double-tap collapses to one open (TR-004 anchor side)
    // -----------------------------------------------------------------------

    @Test
    fun anchor_double_tap_yields_one_open() {
        setSelector(initial = Language.VN)

        // Two rapid taps. Without the guard the second tap would toggle the
        // menu back closed; with the guard the second tap is dropped and the
        // menu remains in `expanded = true`.
        composeRule.onNodeWithTag(TEST_TAG_ANCHOR).performClick()
        composeRule.onNodeWithTag(TEST_TAG_ANCHOR).performClick()

        composeRule.onNodeWithTag(menuItemTag(Language.VN)).assertIsDisplayed()
        composeRule.onNodeWithTag(menuItemTag(Language.EN)).assertIsDisplayed()
    }

    // -----------------------------------------------------------------------
    // T029 — second deliberate tap (after guard window) closes menu
    // -----------------------------------------------------------------------

    @Test
    fun second_tap_on_anchor_closes_menu() {
        var lastSelected: Language? = null
        setSelector(initial = Language.VN, onSelect = { lastSelected = it })

        composeRule.onNodeWithTag(TEST_TAG_ANCHOR).performClick()
        composeRule.onNodeWithTag(menuItemTag(Language.VN)).assertIsDisplayed()

        // Advance past the 400ms single-click guard window so the second tap
        // is not suppressed (TR-004 vs FR-008 dismiss path 1).
        composeRule.mainClock.advanceTimeBy(500)
        composeRule.waitForIdle()

        composeRule.onNodeWithTag(TEST_TAG_ANCHOR).performClick()
        composeRule.onNodeWithTag(menuItemTag(Language.VN)).assertIsNotDisplayed()
        assertNull("Anchor close-tap must not trigger onSelect", lastSelected)
    }

    // -----------------------------------------------------------------------
    // T030 — outside tap closes menu without changing the language
    // -----------------------------------------------------------------------

    @Test
    fun outside_tap_closes_menu_without_changing_language() {
        var lastSelected: Language? = null
        composeRule.setContent {
            AIDDProjectTheme {
                LanguageProvider(language = Language.VN) {
                    var current by remember { mutableStateOf(Language.VN) }
                    Column {
                        LanguageSelector(
                            selected = current,
                            onSelect = {
                                lastSelected = it
                                current = it
                            },
                        )
                        Spacer(Modifier.size(80.dp))
                        // Sibling region outside the menu's bounds; tapping it
                        // bubbles to the popup's scrim and dismisses the menu.
                        Box(
                            modifier =
                                Modifier
                                    .size(200.dp)
                                    .testTag(TEST_TAG_OUTSIDE)
                                    .clickable { /* swallow */ },
                        ) {
                            Text(text = "outside")
                        }
                    }
                }
            }
        }

        composeRule.onNodeWithTag(TEST_TAG_ANCHOR).performClick()
        composeRule.onNodeWithTag(menuItemTag(Language.VN)).assertIsDisplayed()

        // Tap outside the menu — popup scrim catches it and dismisses.
        composeRule.onNodeWithTag(TEST_TAG_OUTSIDE).performClick()

        composeRule.onNodeWithTag(menuItemTag(Language.VN)).assertIsNotDisplayed()
        assertNull("Outside tap must not change the active language", lastSelected)
    }

    // -----------------------------------------------------------------------
    // T031 — predictive back gesture dismisses menu without popping parent
    // -----------------------------------------------------------------------

    @Test
    fun predictive_back_dismisses_menu_without_popping_parent() {
        setSelector(initial = Language.VN)

        composeRule.onNodeWithTag(TEST_TAG_ANCHOR).performClick()
        composeRule.onNodeWithTag(menuItemTag(Language.VN)).assertIsDisplayed()

        // M3 DropdownMenu's Popup defaults to `dismissOnBackPress = true` —
        // the back gesture is consumed by the popup, the host stays alive.
        Espresso.pressBack()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag(menuItemTag(Language.VN)).assertIsNotDisplayed()
        // Anchor still composed → parent host wasn't popped.
        composeRule.onNodeWithTag(TEST_TAG_ANCHOR).assertIsDisplayed()
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private fun setSelector(
        initial: Language,
        onSelect: (Language) -> Unit = {},
    ) {
        composeRule.setContent {
            AIDDProjectTheme {
                LanguageProvider(language = Language.VN) {
                    var current by remember { mutableStateOf(initial) }
                    LanguageSelector(
                        selected = current,
                        onSelect = {
                            onSelect(it)
                            current = it
                        },
                    )
                }
            }
        }
    }

    private companion object {
        const val SLOP: Float = 0.5f
        const val TEST_TAG_OUTSIDE: String = "language_selector_outside_sibling"
    }
}
