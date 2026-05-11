package com.example.aiddproject.awarddetail

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.test.platform.app.InstrumentationRegistry
import com.example.aiddproject.R
import com.example.aiddproject.awarddetail.ui.components.AwardInfoBlock
import com.example.aiddproject.ui.theme.AIDDProjectTheme
import org.junit.Rule
import org.junit.Test

/**
 * Regression guards for the Q-TP-2 fix in [AwardInfoBlock] —
 * `"%02d".format(quantity)` zero-pad applied to the quantity row so
 * single-digit counts match Figma node `6885:10475` (Top Project `02`,
 * Top Heart `08`, etc.). Top Talent's `10` masked the formatting bug
 * until the Top Project delta-spec audit.
 *
 * Each test renders the block in isolation with the canonical theme
 * and asserts the formatted quantity text + unit visible on screen.
 * The `%02d` Kotlin directive formats AT LEAST 2 digits — 3+ digit
 * counts pass through unchanged; the `quantity = 100` case guards
 * against a regression to fixed-width formatting (`%2d` would still
 * be 100 but `Locale`-dependent grouping like `%,d` would not).
 *
 * Pinned 2026-05-11 per delta-spec `FQoJZLkG_d` Q-TP-2 resolution.
 */
class AwardInfoBlockTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val ctx = InstrumentationRegistry.getInstrumentation().targetContext

    private fun setContent(
        quantity: Int?,
        quantityUnit: String? = "Cá nhân",
        prizeValue: String? = "7.000.000 VNĐ",
        prizeCaption: String? = null,
        prizeValueTeam: String? = null,
        prizeCaptionTeam: String? = null,
    ) {
        composeRule.setContent {
            AIDDProjectTheme {
                AwardInfoBlock(
                    awardName = "Top Project",
                    description = "Test description body.",
                    quantity = quantity,
                    quantityUnit = quantityUnit,
                    prizeValue = prizeValue,
                    prizeCaption = prizeCaption,
                    prizeValueTeam = prizeValueTeam,
                    prizeCaptionTeam = prizeCaptionTeam,
                )
            }
        }
    }

    @Test
    fun renders_zero_padded_02_when_quantity_is_2() {
        setContent(quantity = 2, quantityUnit = "Tập thể", prizeValue = "15.000.000 VNĐ")

        composeRule.onNodeWithText("02").assertIsDisplayed()
        composeRule.onNodeWithText("Tập thể").assertIsDisplayed()
    }

    @Test
    fun renders_zero_padded_08_when_quantity_is_8() {
        setContent(quantity = 8, quantityUnit = "Cá nhân")

        composeRule.onNodeWithText("08").assertIsDisplayed()
    }

    @Test
    fun renders_10_unchanged_when_quantity_is_10() {
        setContent(quantity = 10, quantityUnit = "Cá nhân")

        composeRule.onNodeWithText("10").assertIsDisplayed()
    }

    @Test
    fun renders_00_when_quantity_is_0_edge_case() {
        setContent(quantity = 0, quantityUnit = "Cá nhân")

        composeRule.onNodeWithText("00").assertIsDisplayed()
    }

    @Test
    fun renders_three_digits_unchanged_when_quantity_is_100() {
        setContent(quantity = 100, quantityUnit = "Cá nhân")

        composeRule.onNodeWithText("100").assertIsDisplayed()
    }

    @Test
    fun renders_em_dash_placeholder_when_quantity_is_null() {
        setContent(quantity = null, quantityUnit = null)

        val placeholder = ctx.getString(R.string.award_detail_placeholder_value)
        composeRule.onNodeWithText(placeholder).assertIsDisplayed()
    }

    // -----------------------------------------------------------------------
    // Q-MVP-1 — custom prize caption override (delta-spec b2BuS8HYIt)
    // -----------------------------------------------------------------------

    @Test
    fun renders_default_prize_caption_when_prize_caption_is_null() {
        setContent(quantity = 10, prizeValue = "7.000.000 VNĐ", prizeCaption = null)

        val defaultCaption = ctx.getString(R.string.award_detail_prize_caption)
        composeRule.onNodeWithText(defaultCaption).assertIsDisplayed()
    }

    @Test
    fun renders_custom_prize_caption_when_provided() {
        // MVP path: caption override "cho giải cá nhân"
        setContent(
            quantity = 1,
            prizeValue = "15.000.000 VNĐ",
            prizeCaption = "cho giải cá nhân",
        )

        composeRule.onNodeWithText("cho giải cá nhân").assertIsDisplayed()
        // The default caption must NOT render when an override is provided.
        val defaultCaption = ctx.getString(R.string.award_detail_prize_caption)
        composeRule.onAllNodesWithText(defaultCaption).assertCountEquals(0)
    }

    // -----------------------------------------------------------------------
    // Q-SIG-1 — dual prize-value rows (delta-spec O98TwiHaJe)
    // -----------------------------------------------------------------------

    @Test
    fun renders_only_first_prize_row_when_team_fields_are_null() {
        setContent(
            quantity = 10,
            prizeValue = "7.000.000 VNĐ",
            prizeValueTeam = null,
            prizeCaptionTeam = null,
        )

        composeRule.onNodeWithText("7.000.000 VNĐ").assertIsDisplayed()
        // No "tập thể" caption should render in single-prize mode.
        composeRule.onAllNodesWithText("cho giải tập thể").assertCountEquals(0)
        // Single prize title — the second section MUST NOT render
        // when team fields are null.
        val prizeTitle = ctx.getString(R.string.award_detail_prize_label)
        composeRule.onAllNodesWithText(prizeTitle).assertCountEquals(1)
    }

    @Test
    fun renders_both_prize_rows_when_team_fields_provided() {
        // Signature 2025 — Creator path: 5M cá nhân + 8M tập thể.
        // Each prize value lives in its own labeled section per Figma
        // frame O98TwiHaJe (the two sections share the same
        // "Giá trị giải thưởng" title, separated by an InfoDivider).
        setContent(
            quantity = 1,
            quantityUnit = "Cá nhân hoặc tập thể",
            prizeValue = "5.000.000 VNĐ",
            prizeCaption = "cho giải cá nhân",
            prizeValueTeam = "8.000.000 VNĐ",
            prizeCaptionTeam = "cho giải tập thể",
        )

        composeRule.onNodeWithText("5.000.000 VNĐ").assertIsDisplayed()
        composeRule.onNodeWithText("cho giải cá nhân").assertIsDisplayed()
        composeRule.onNodeWithText("8.000.000 VNĐ").assertIsDisplayed()
        composeRule.onNodeWithText("cho giải tập thể").assertIsDisplayed()
        // Two prize titles — one above each value row (Bug D fix).
        val prizeTitle = ctx.getString(R.string.award_detail_prize_label)
        composeRule.onAllNodesWithText(prizeTitle).assertCountEquals(2)
    }

    @Test
    fun second_row_does_not_render_when_only_team_value_provided() {
        // Defensive: both team fields must be set; missing caption → skip.
        setContent(
            quantity = 1,
            prizeValue = "5.000.000 VNĐ",
            prizeValueTeam = "8.000.000 VNĐ",
            prizeCaptionTeam = null,
        )

        composeRule.onAllNodesWithText("8.000.000 VNĐ").assertCountEquals(0)
    }
}
