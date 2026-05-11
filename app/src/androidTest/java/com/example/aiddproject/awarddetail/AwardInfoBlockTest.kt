package com.example.aiddproject.awarddetail

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
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
    ) {
        composeRule.setContent {
            AIDDProjectTheme {
                AwardInfoBlock(
                    awardName = "Top Project",
                    description = "Test description body.",
                    quantity = quantity,
                    quantityUnit = quantityUnit,
                    prizeValue = prizeValue,
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
}
