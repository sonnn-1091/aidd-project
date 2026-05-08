package com.example.aiddproject.home

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import com.example.aiddproject.R
import com.example.aiddproject.home.domain.Award
import com.example.aiddproject.home.domain.states.AwardsState
import com.example.aiddproject.home.ui.components.AwardsSection
import com.example.aiddproject.ui.theme.AIDDProjectTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/**
 * Compose UI tests for the awards carousel (US2, T061). Drives [AwardsSection]
 * directly across all four [AwardsState] branches — populated / loading / empty /
 * error — and verifies the Chi tiết tap fires the navigation callback with the
 * correct [Award.id] (FR-004) and the Retry tap fires the retry callback (FR-003).
 */
class AwardsCarouselTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val ctx = InstrumentationRegistry.getInstrumentation().targetContext

    private val firstAward = Award(id = "award-1", name = "Top Talent Award", thumbnailUrl = null, sortOrder = 0)
    private val secondAward = Award(id = "award-2", name = "Top Project Award", thumbnailUrl = null, sortOrder = 1)

    @Test
    fun populated_state_renders_each_card_name() {
        setContent(state = AwardsState.Populated(listOf(firstAward, secondAward)))

        composeRule.onNodeWithText(firstAward.name).assertIsDisplayed()
        composeRule.onNodeWithText(secondAward.name).assertIsDisplayed()
    }

    @Test
    fun populated_chi_tiet_tap_fires_callback_with_correct_award() {
        var tapped: Award? = null
        setContent(
            state = AwardsState.Populated(listOf(firstAward, secondAward)),
            onChiTietTap = { tapped = it },
        )

        // Two "Chi tiết" links exist (one per card). Tap the first.
        composeRule
            .onAllNodesWithText(ctx.getString(R.string.home_link_chi_tiet))[0]
            .performClick()

        assertEquals(firstAward, tapped)
    }

    @Test
    fun loading_state_renders_progress_indicator_and_no_cards() {
        setContent(state = AwardsState.Loading)

        // No card name visible because no Populated items.
        composeRule.onNodeWithText(firstAward.name).assertDoesNotExist()
        // Section title still rendered above the spinner.
        // Phase 11: section header now renders the brand caption "Sun* Annual
        // Awards 2025" above the cream title — assert the caption since it's
        // the same value across locales.
        composeRule
            .onNodeWithText(ctx.getString(R.string.home_section_awards_caption))
            .assertIsDisplayed()
    }

    @Test
    fun empty_state_renders_localized_empty_message() {
        setContent(state = AwardsState.Empty)

        composeRule
            .onNodeWithText(ctx.getString(R.string.home_awards_empty))
            .assertIsDisplayed()
    }

    @Test
    fun error_state_renders_message_and_retry_button() {
        setContent(state = AwardsState.Error(message = "boom"))

        composeRule
            .onNodeWithText(ctx.getString(R.string.home_awards_error))
            .assertIsDisplayed()
        composeRule
            .onNodeWithText(ctx.getString(R.string.home_action_retry))
            .assertIsDisplayed()
    }

    @Test
    fun error_state_retry_tap_fires_callback() {
        var retries = 0
        setContent(
            state = AwardsState.Error(message = null),
            onRetry = { retries++ },
        )

        composeRule
            .onNodeWithText(ctx.getString(R.string.home_action_retry))
            .performClick()

        assertEquals(1, retries)
    }

    private fun setContent(
        state: AwardsState,
        onChiTietTap: (Award) -> Unit = {},
        onRetry: () -> Unit = {},
    ) {
        composeRule.setContent {
            AIDDProjectTheme {
                AwardsSection(
                    state = state,
                    onChiTietTap = onChiTietTap,
                    onRetry = onRetry,
                )
            }
        }
    }
}
