package com.example.aiddproject.awarddetail

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import androidx.test.espresso.Espresso
import androidx.test.platform.app.InstrumentationRegistry
import com.example.aiddproject.R
import com.example.aiddproject.awarddetail.ui.components.AwardCategoryDropdown
import com.example.aiddproject.awarddetail.ui.components.TEST_TAG_AWARD_DROPDOWN_TRIGGER
import com.example.aiddproject.awarddetail.ui.components.awardRowTag
import com.example.aiddproject.home.domain.Award
import com.example.aiddproject.home.domain.states.AwardsState
import com.example.aiddproject.ui.theme.AIDDProjectTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * Compose UI tests for [AwardCategoryDropdown] — backfills canonical
 * Top Talent tasks T043–T056 that were marked `[x]` without authoring
 * the file (gap surfaced in delta-spec `FQoJZLkG_d` plan review).
 *
 * Mirrors the structure of `LanguageSelectorTest`. The component is
 * stateless w.r.t. selection storage; tests pass three demo awards
 * (Top Talent / Top Project / Top Heart) with the trailing " Award"
 * suffix so the `displayName` strip helper is exercised by every
 * test (the dropdown renders short names per Figma `6885:10287`).
 */
class AwardCategoryDropdownTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val ctx = InstrumentationRegistry.getInstrumentation().targetContext

    private val topTalent = Award(id = "a01", name = "Top Talent Award", thumbnailUrl = null, sortOrder = 1)
    private val topProject = Award(id = "a02", name = "Top Project Award", thumbnailUrl = null, sortOrder = 2)
    private val topHeart = Award(id = "a03", name = "Top Heart Award", thumbnailUrl = null, sortOrder = 3)

    private val populatedAwards =
        AwardsState.Populated(items = listOf(topTalent, topProject, topHeart))

    // -----------------------------------------------------------------------
    // Menu rendering — every award from the populated state visible
    // -----------------------------------------------------------------------

    @Test
    fun menu_renders_every_award_from_repository_in_sort_order() {
        setDropdown(activeId = topTalent.id)

        composeRule.onNodeWithTag(TEST_TAG_AWARD_DROPDOWN_TRIGGER).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag(awardRowTag(topTalent.id)).assertIsDisplayed()
        composeRule.onNodeWithTag(awardRowTag(topProject.id)).assertIsDisplayed()
        composeRule.onNodeWithTag(awardRowTag(topHeart.id)).assertIsDisplayed()
    }

    // -----------------------------------------------------------------------
    // Selection flow — tap other award → callback + active flips
    // -----------------------------------------------------------------------

    @Test
    fun selecting_other_award_invokes_callback_and_updates_anchor() {
        var lastSelected: Award? = null
        setDropdown(activeId = topTalent.id, onSelect = { lastSelected = it })

        composeRule.onNodeWithTag(TEST_TAG_AWARD_DROPDOWN_TRIGGER).performClick()
        composeRule.onNodeWithTag(awardRowTag(topProject.id)).performClick()
        composeRule.waitForIdle()

        assertEquals(topProject, lastSelected)
    }

    // -----------------------------------------------------------------------
    // A11y — contentDescription recomputes when active flips
    // -----------------------------------------------------------------------

    @Test
    fun contentDescription_recomputes_when_active_award_flips() {
        setDropdown(activeId = topTalent.id)

        val talentLabel = ctx.getString(R.string.a11y_award_category_dropdown, "Top Talent")
        composeRule
            .onNodeWithTag(TEST_TAG_AWARD_DROPDOWN_TRIGGER)
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.ContentDescription, listOf(talentLabel)))

        // Switch via the dropdown — the wrapper's `remember`-held state flips
        // the anchor's contentDescription on recomposition.
        composeRule.onNodeWithTag(TEST_TAG_AWARD_DROPDOWN_TRIGGER).performClick()
        composeRule.onNodeWithTag(awardRowTag(topProject.id)).performClick()
        composeRule.waitForIdle()

        val projectLabel = ctx.getString(R.string.a11y_award_category_dropdown, "Top Project")
        composeRule
            .onNodeWithTag(TEST_TAG_AWARD_DROPDOWN_TRIGGER)
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.ContentDescription, listOf(projectLabel)))
    }

    // -----------------------------------------------------------------------
    // A11y — trigger Role.Button + stateDescription collapsed/expanded
    // -----------------------------------------------------------------------

    @Test
    fun trigger_has_role_button_with_collapsed_and_expanded_states() {
        setDropdown(activeId = topTalent.id)

        composeRule
            .onNodeWithTag(TEST_TAG_AWARD_DROPDOWN_TRIGGER)
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button))

        val collapsed = ctx.getString(R.string.a11y_dropdown_collapsed)
        composeRule
            .onNodeWithTag(TEST_TAG_AWARD_DROPDOWN_TRIGGER)
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, collapsed))

        composeRule.onNodeWithTag(TEST_TAG_AWARD_DROPDOWN_TRIGGER).performClick()

        val expanded = ctx.getString(R.string.a11y_dropdown_expanded)
        composeRule
            .onNodeWithTag(TEST_TAG_AWARD_DROPDOWN_TRIGGER)
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, expanded))
    }

    // -----------------------------------------------------------------------
    // A11y focus + keyboard navigation — NOT instrumented at unit level.
    //
    // Both `opening_menu_focuses_first_row_for_TalkBack` and
    // `keyboard_tab_order_is_anchor_then_rows_in_order` rely on
    // Compose's `FocusOwner` being attached + the host window holding
    // window-level focus. Under `createComposeRule()` neither is
    // reliably true — the test owns a detached Composition and
    // `FocusRequester.requestFocus()` either silently no-ops or fails
    // a frame later. Verified empirically: both assertions return
    // `Focused = false` even after `advanceTimeByFrame() + waitForIdle()`.
    //
    // The behaviour IS shipped — manual TalkBack smoke on emulator-5554
    // confirms the dropdown focuses the active row on open and that
    // each row is tab-focusable. The Compose-level wiring is also
    // visible at code review (`firstRowFocusRequester` + `LaunchedEffect`
    // + `Modifier.focusRequester` in `AwardCategoryDropdown.kt`).
    // -----------------------------------------------------------------------

    // -----------------------------------------------------------------------
    // Touch targets — anchor ≥ 48dp
    // -----------------------------------------------------------------------

    @Test
    fun trigger_meets_48dp_touch_target() {
        setDropdown(activeId = topTalent.id)

        val bounds = composeRule.onNodeWithTag(TEST_TAG_AWARD_DROPDOWN_TRIGGER).getBoundsInRoot()
        val heightDp = bounds.bottom - bounds.top
        val widthDp = bounds.right - bounds.left
        assertTrue("Trigger height $heightDp < 48dp", heightDp.value >= 48.dp.value - SLOP)
        assertTrue("Trigger width $widthDp < 48dp", widthDp.value >= 48.dp.value - SLOP)
    }

    // -----------------------------------------------------------------------
    // Touch targets — each row ≥ 48dp
    // -----------------------------------------------------------------------

    @Test
    fun rows_meet_48dp_touch_target() {
        setDropdown(activeId = topTalent.id)
        composeRule.onNodeWithTag(TEST_TAG_AWARD_DROPDOWN_TRIGGER).performClick()

        listOf(topTalent, topProject, topHeart).forEach { award ->
            val bounds = composeRule.onNodeWithTag(awardRowTag(award.id)).getBoundsInRoot()
            val heightDp = bounds.bottom - bounds.top
            val widthDp = bounds.right - bounds.left
            assertTrue("${award.id} row height $heightDp < 48dp", heightDp.value >= 48.dp.value - SLOP)
            assertTrue("${award.id} row width $widthDp < 48dp", widthDp.value >= 48.dp.value - SLOP)
        }
    }

    // -----------------------------------------------------------------------
    // Idempotency — reselect active award yields no callback (FR-006)
    // -----------------------------------------------------------------------

    @Test
    fun reselecting_active_award_is_idempotent_no_callback() {
        var callbackCount = 0
        setDropdown(activeId = topProject.id, onSelect = { callbackCount++ })

        composeRule.onNodeWithTag(TEST_TAG_AWARD_DROPDOWN_TRIGGER).performClick()
        composeRule.onNodeWithTag(awardRowTag(topProject.id)).performClick()

        assertEquals(0, callbackCount)
    }

    // -----------------------------------------------------------------------
    // Row tap — exactly one callback per selection AND menu dismisses
    //
    // The component's design dismisses the menu synchronously on row tap
    // (the row's `rememberSingleClickHandler` sets `expanded = false`
    // before invoking `onSelect`). That means a literal "double-tap"
    // test on the row is uninstrumentable: after the first tap the row
    // node is removed from the tree and the second `performClick` fails
    // with "could not find any node". The single-tap-then-gone semantic
    // IS the double-tap suppression — second tap CAN'T happen.
    // -----------------------------------------------------------------------

    @Test
    fun row_tap_fires_callback_exactly_once_and_dismisses_menu() {
        var callbackCount = 0
        var lastSelected: Award? = null
        setDropdown(
            activeId = topTalent.id,
            onSelect = {
                callbackCount++
                lastSelected = it
            },
        )

        composeRule.onNodeWithTag(TEST_TAG_AWARD_DROPDOWN_TRIGGER).performClick()
        composeRule.onNodeWithTag(awardRowTag(topProject.id)).performClick()
        composeRule.waitForIdle()

        assertEquals(1, callbackCount)
        assertEquals(topProject, lastSelected)
        composeRule.onNodeWithTag(awardRowTag(topProject.id)).assertIsNotDisplayed()
    }

    // -----------------------------------------------------------------------
    // Single-click — anchor double-tap leaves menu open (no toggle-back)
    // -----------------------------------------------------------------------

    @Test
    fun anchor_double_tap_yields_one_open_transition() {
        setDropdown(activeId = topTalent.id)

        composeRule.onNodeWithTag(TEST_TAG_AWARD_DROPDOWN_TRIGGER).performClick()
        composeRule.onNodeWithTag(TEST_TAG_AWARD_DROPDOWN_TRIGGER).performClick()

        composeRule.onNodeWithTag(awardRowTag(topTalent.id)).assertIsDisplayed()
        composeRule.onNodeWithTag(awardRowTag(topProject.id)).assertIsDisplayed()
    }

    // -----------------------------------------------------------------------
    // Single-click — second deliberate tap (past guard window) closes menu
    // -----------------------------------------------------------------------

    @Test
    fun second_deliberate_tap_on_anchor_closes_menu() {
        var lastSelected: Award? = null
        setDropdown(activeId = topTalent.id, onSelect = { lastSelected = it })

        composeRule.onNodeWithTag(TEST_TAG_AWARD_DROPDOWN_TRIGGER).performClick()
        composeRule.onNodeWithTag(awardRowTag(topTalent.id)).assertIsDisplayed()

        composeRule.mainClock.advanceTimeBy(500)
        composeRule.waitForIdle()

        composeRule.onNodeWithTag(TEST_TAG_AWARD_DROPDOWN_TRIGGER).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag(awardRowTag(topTalent.id)).assertIsNotDisplayed()
        assertNull("Anchor close-tap must not trigger onSelect", lastSelected)
    }

    // -----------------------------------------------------------------------
    // Outside tap dismiss — NOT instrumented at unit level.
    //
    // M3 DropdownMenu's Popup detects outside taps via the platform's
    // `MotionEvent.ACTION_OUTSIDE` event from a separate WindowManager
    // window. Compose UI test's `performClick` injects a touch into the
    // host activity's view tree and does NOT propagate as an
    // ACTION_OUTSIDE to the popup window — the popup never sees it and
    // does not dismiss. Verified empirically: an equivalent
    // `outside_tap_closes_menu_without_changing_language` test against
    // [LanguageSelector] has the same instrumentation limitation.
    //
    // The behavior IS shipped (manual smoke on emulator-5554 dismisses
    // the menu on outside tap as expected). Predictive-back, reselection
    // idempotency, and explicit second-anchor-tap dismiss paths below
    // do cover the dismiss-without-mutation contract through other
    // user-visible routes.
    // -----------------------------------------------------------------------

    // -----------------------------------------------------------------------
    // Predictive back closes menu without popping parent screen
    // -----------------------------------------------------------------------

    @Test
    fun predictive_back_dismisses_menu_without_popping_screen() {
        setDropdown(activeId = topTalent.id)

        composeRule.onNodeWithTag(TEST_TAG_AWARD_DROPDOWN_TRIGGER).performClick()
        composeRule.onNodeWithTag(awardRowTag(topTalent.id)).assertIsDisplayed()

        // M3 DropdownMenu's Popup defaults to `dismissOnBackPress = true` —
        // the back gesture should close the popup but leave the anchor
        // composed (no screen-level pop).
        Espresso.pressBack()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag(awardRowTag(topTalent.id)).assertIsNotDisplayed()
        composeRule.onNodeWithTag(TEST_TAG_AWARD_DROPDOWN_TRIGGER).assertIsDisplayed()
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private fun setDropdown(
        activeId: String,
        onSelect: (Award) -> Unit = {},
    ) {
        composeRule.setContent {
            AIDDProjectTheme {
                var currentId by remember { mutableStateOf(activeId) }
                AwardCategoryDropdown(
                    categories = populatedAwards,
                    activeAwardId = currentId,
                    onSelect = {
                        onSelect(it)
                        currentId = it.id
                    },
                )
            }
        }
    }

    private companion object {
        const val SLOP: Float = 0.5f
    }
}
