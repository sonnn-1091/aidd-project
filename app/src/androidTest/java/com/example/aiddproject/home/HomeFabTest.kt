package com.example.aiddproject.home

import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import com.example.aiddproject.R
import com.example.aiddproject.core.locale.Language
import com.example.aiddproject.core.locale.LanguageProvider
import com.example.aiddproject.home.ui.components.HomeFab
import com.example.aiddproject.home.ui.components.TEST_TAG_HOME_FAB_PENCIL
import com.example.aiddproject.home.ui.components.TEST_TAG_HOME_FAB_SKUDOS
import com.example.aiddproject.ui.theme.AIDDProjectTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/**
 * Compose UI coverage for the floating action button (US5, T076):
 *  - Pencil rendered ONLY when `isKudosAvailable=true` (Q-Home-2).
 *  - S/Kudos rendered regardless of the flag (Q-Home-9).
 *  - Both icons surface their localized accessibility content descriptions
 *    (TR-009).
 *  - Double-tap on the pencil yields exactly one navigation callback —
 *    the back-stack invariant for SC-002.
 */
class HomeFabTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val ctx = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun pencil_is_hidden_when_kudos_flag_is_false() {
        setContent(isKudosAvailable = false)
        composeRule.onNodeWithTag(TEST_TAG_HOME_FAB_PENCIL).assertDoesNotExist()
    }

    @Test
    fun pencil_is_visible_when_kudos_flag_is_true() {
        setContent(isKudosAvailable = true)
        composeRule.onNodeWithTag(TEST_TAG_HOME_FAB_PENCIL).assertIsDisplayed()
    }

    @Test
    fun s_kudos_is_visible_when_kudos_flag_is_false() {
        setContent(isKudosAvailable = false)
        composeRule.onNodeWithTag(TEST_TAG_HOME_FAB_SKUDOS).assertIsDisplayed()
    }

    @Test
    fun s_kudos_is_visible_when_kudos_flag_is_true() {
        setContent(isKudosAvailable = true)
        composeRule.onNodeWithTag(TEST_TAG_HOME_FAB_SKUDOS).assertIsDisplayed()
    }

    @Test
    fun pencil_uses_localized_compose_kudo_a11y_label() {
        setContent(isKudosAvailable = true)
        composeRule
            .onNodeWithTag(TEST_TAG_HOME_FAB_PENCIL)
            .assertContentDescriptionEquals(ctx.getString(R.string.a11y_home_fab_compose_kudo))
    }

    @Test
    fun s_kudos_uses_localized_kudos_feed_a11y_label() {
        setContent(isKudosAvailable = false)
        composeRule
            .onNodeWithTag(TEST_TAG_HOME_FAB_SKUDOS)
            .assertContentDescriptionEquals(ctx.getString(R.string.a11y_home_fab_kudos_feed))
    }

    @Test
    fun pencil_double_tap_yields_exactly_one_callback() {
        var pencilTaps = 0
        setContent(
            isKudosAvailable = true,
            onPencilClick = { pencilTaps++ },
        )
        // Two clicks in the same recomposition window — the second must be
        // dropped by the in-flight guard (TR-005, SC-002).
        composeRule.onNodeWithTag(TEST_TAG_HOME_FAB_PENCIL).performClick()
        composeRule.onNodeWithTag(TEST_TAG_HOME_FAB_PENCIL).performClick()
        assertEquals(1, pencilTaps)
    }

    @Test
    fun s_kudos_double_tap_yields_exactly_one_callback() {
        var sKudosTaps = 0
        setContent(
            isKudosAvailable = false,
            onSKudosClick = { sKudosTaps++ },
        )
        composeRule.onNodeWithTag(TEST_TAG_HOME_FAB_SKUDOS).performClick()
        composeRule.onNodeWithTag(TEST_TAG_HOME_FAB_SKUDOS).performClick()
        assertEquals(1, sKudosTaps)
    }

    private fun setContent(
        isKudosAvailable: Boolean,
        onPencilClick: () -> Unit = {},
        onSKudosClick: () -> Unit = {},
    ) {
        composeRule.setContent {
            AIDDProjectTheme {
                LanguageProvider(language = Language.VN) {
                    HomeFab(
                        isKudosAvailable = isKudosAvailable,
                        onPencilClick = onPencilClick,
                        onSKudosClick = onSKudosClick,
                    )
                }
            }
        }
    }
}
