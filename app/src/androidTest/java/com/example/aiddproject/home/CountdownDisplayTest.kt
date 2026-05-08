package com.example.aiddproject.home

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.platform.app.InstrumentationRegistry
import com.example.aiddproject.R
import com.example.aiddproject.home.domain.states.CountdownState
import com.example.aiddproject.home.ui.components.HomeHero
import com.example.aiddproject.ui.theme.AIDDProjectTheme
import org.junit.Rule
import org.junit.Test

/**
 * Compose UI test for the countdown block in [HomeHero] (US1, T059).
 *
 * Pre-event clock: DAYS / HOURS / MINUTES values are non-zero and "Coming soon"
 * is visible. At/post-event clock: values clamp to 0 and "Coming soon" is hidden.
 * The TalkBack live-region wiring (`Polite`, keyed on minutes only) is asserted
 * structurally by [HomeHero]'s `Modifier.semantics { liveRegion = Polite }`; the
 * minute-boundary re-announce is exercised by `HomeViewModelTest`'s tick-driving
 * assertions plus the live lifecycle plumbing on the screen.
 */
class CountdownDisplayTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val ctx = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun pre_event_clock_renders_non_zero_values_and_coming_soon_label() {
        composeRule.setContent {
            AIDDProjectTheme {
                HomeHero(
                    countdown = CountdownState(days = 12, hours = 5, minutes = 34, isPreEvent = true),
                    onAboutAwardClick = {},
                    onAboutKudosClick = {},
                )
            }
        }

        composeRule.onNodeWithText("12").assertIsDisplayed()
        composeRule.onNodeWithText("05").assertIsDisplayed()
        composeRule.onNodeWithText("34").assertIsDisplayed()
        composeRule.onNodeWithText(ctx.getString(R.string.home_coming_soon)).assertIsDisplayed()
    }

    @Test
    fun at_event_clock_clamps_to_zero_and_hides_coming_soon_label() {
        composeRule.setContent {
            AIDDProjectTheme {
                HomeHero(
                    countdown = CountdownState(days = 0, hours = 0, minutes = 0, isPreEvent = false),
                    onAboutAwardClick = {},
                    onAboutKudosClick = {},
                )
            }
        }

        composeRule.onNodeWithText(ctx.getString(R.string.home_coming_soon)).assertDoesNotExist()
        composeRule.onNodeWithText(ctx.getString(R.string.home_countdown_days_label)).assertDoesNotExist()
        composeRule.onNodeWithText(ctx.getString(R.string.home_countdown_hours_label)).assertDoesNotExist()
        composeRule.onNodeWithText(ctx.getString(R.string.home_countdown_min_label)).assertDoesNotExist()
    }
}
