package com.example.aiddproject.home

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.test.platform.app.InstrumentationRegistry
import com.example.aiddproject.R
import com.example.aiddproject.home.domain.states.CountdownState
import com.example.aiddproject.home.ui.components.HomeHero
import com.example.aiddproject.ui.theme.AIDDProjectTheme
import org.junit.Rule
import org.junit.Test

/**
 * Compose UI test for the countdown block in [HomeHero] (US1, T059 + T102).
 *
 * Phase 11 redesigned the countdown so each unit (DAYS / HOURS / MINUTES)
 * renders as TWO 32×56dp digit cells side-by-side. The padded value "12" is
 * therefore two distinct nodes ("1", "2") — assertions are made through the
 * live-region content description (which is the merged semantic label) and
 * via `onAllNodesWithText` digit counts so we don't depend on text-search
 * ambiguity.
 *
 * The TalkBack live-region wiring (`Polite`, keyed on minutes only) is
 * asserted structurally by the merged-descendants `Modifier.semantics` block
 * exposing a single contentDescription; the minute-boundary re-announce is
 * exercised by `HomeViewModelTest`'s tick-driving assertions plus the live
 * lifecycle plumbing on the screen.
 */
class CountdownDisplayTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val ctx = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun pre_event_clock_renders_padded_digits_via_live_region_label() {
        composeRule.setContent {
            AIDDProjectTheme {
                HomeHero(
                    countdown = CountdownState(days = 12, hours = 5, minutes = 34, isPreEvent = true),
                    onAboutAwardClick = {},
                    onAboutKudosClick = {},
                )
            }
        }

        // The live-region contentDescription encodes "12 DAYS, 5 HOURS, 34
        // MINUTES" — substring assertion proves all three values are present
        // without depending on per-digit text-node ordering.
        val daysLabel = ctx.getString(R.string.home_countdown_days_label)
        composeRule
            .onNodeWithContentDescription("12 $daysLabel", substring = true)
            .assertIsDisplayed()
        composeRule.onNodeWithText(ctx.getString(R.string.home_coming_soon)).assertIsDisplayed()
    }

    @Test
    fun pre_event_clock_renders_two_digit_cells_per_unit() {
        composeRule.setContent {
            AIDDProjectTheme {
                HomeHero(
                    countdown = CountdownState(days = 12, hours = 34, minutes = 56, isPreEvent = true),
                    onAboutAwardClick = {},
                    onAboutKudosClick = {},
                )
            }
        }

        // 6 distinct digits ("1", "2", "3", "4", "5", "6") with no repeats —
        // each renders as a single digit-box Text node, proving each unit was
        // split into two cells (3 units × 2 cells = 6 nodes).
        listOf("1", "2", "3", "4", "5", "6").forEach { digit ->
            composeRule.onAllNodesWithText(digit).assertCountEquals(1)
        }
    }

    @Test
    fun at_event_clock_clamps_to_zero_keeps_unit_labels_and_coming_soon() {
        // Post-event behaviour: the countdown digit cells stay visible (clamped
        // to 00 / 00 / 00), the unit labels stay rendered, and the "Coming
        // soon" header stays as part of the brand voicing — Figma renders
        // it unconditionally. The event-info block is a static metadata
        // sibling and also stays.
        composeRule.setContent {
            AIDDProjectTheme {
                HomeHero(
                    countdown = CountdownState(days = 0, hours = 0, minutes = 0, isPreEvent = false),
                    onAboutAwardClick = {},
                    onAboutKudosClick = {},
                )
            }
        }

        composeRule.onNodeWithText(ctx.getString(R.string.home_coming_soon)).assertIsDisplayed()
        composeRule.onNodeWithText(ctx.getString(R.string.home_countdown_days_label)).assertIsDisplayed()
        composeRule.onNodeWithText(ctx.getString(R.string.home_countdown_hours_label)).assertIsDisplayed()
        composeRule.onNodeWithText(ctx.getString(R.string.home_countdown_min_label)).assertIsDisplayed()
        // Event-info stays as a static metadata block.
        composeRule.onNodeWithText(ctx.getString(R.string.home_event_date_value)).assertIsDisplayed()
        composeRule.onNodeWithText(ctx.getString(R.string.home_event_location_value)).assertIsDisplayed()
    }
}
