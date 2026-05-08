package com.example.aiddproject.auth.login.ui.components

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.aiddproject.core.locale.Language
import com.example.aiddproject.ui.theme.AIDDProjectTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

/**
 * UI coverage for [LanguageSelector] (T054, US3).
 *
 * Drives the stateless component directly. The test installs a small wrapper that holds
 * the current selection in `remember` so we can verify the visible label updates after
 * a selection — proving the round-trip from menu tap → onSelect → recomposition.
 */
class LanguageSelectorTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun anchor_renders_with_current_language_two_letter_code() {
        composeRule.setContent {
            AIDDProjectTheme {
                LanguageSelector(selected = Language.VN, onSelect = {})
            }
        }
        composeRule.onNodeWithTag(TEST_TAG_ANCHOR).assertIsDisplayed()
        composeRule.onNodeWithText("VN").assertIsDisplayed()
    }

    @Test
    fun tapping_anchor_opens_menu_with_only_supported_languages() {
        composeRule.setContent {
            AIDDProjectTheme {
                LanguageSelector(selected = Language.VN, onSelect = {})
            }
        }

        // Menu hidden initially.
        composeRule.onNodeWithTag(menuItemTag(Language.EN)).assertIsNotDisplayed()

        composeRule.onNodeWithTag(TEST_TAG_ANCHOR).performClick()

        // All three supported languages render with their native names (FR-013).
        composeRule.onNodeWithTag(menuItemTag(Language.VN)).assertIsDisplayed()
        composeRule.onNodeWithTag(menuItemTag(Language.EN)).assertIsDisplayed()
        composeRule.onNodeWithTag(menuItemTag(Language.JA)).assertIsDisplayed()

        composeRule.onNodeWithText(Language.VN.nativeName).assertIsDisplayed()
        composeRule.onNodeWithText(Language.EN.nativeName).assertIsDisplayed()
        composeRule.onNodeWithText(Language.JA.nativeName).assertIsDisplayed()
    }

    @Test
    fun selecting_EN_emits_callback_and_updates_visible_label() {
        var lastSelected: Language? = null
        composeRule.setContent {
            AIDDProjectTheme {
                var current by remember { mutableStateOf(Language.VN) }
                LanguageSelector(
                    selected = current,
                    onSelect = {
                        lastSelected = it
                        current = it
                    },
                )
            }
        }

        composeRule.onNodeWithText("VN").assertIsDisplayed()
        composeRule.onNodeWithTag(TEST_TAG_ANCHOR).performClick()
        composeRule.onNodeWithTag(menuItemTag(Language.EN)).performClick()

        assertEquals(Language.EN, lastSelected)
        composeRule.onNodeWithText("EN").assertIsDisplayed()
    }

    @Test
    fun selecting_already_active_language_does_not_emit_callback() {
        var lastSelected: Language? = null
        composeRule.setContent {
            AIDDProjectTheme {
                LanguageSelector(
                    selected = Language.VN,
                    onSelect = { lastSelected = it },
                )
            }
        }

        composeRule.onNodeWithTag(TEST_TAG_ANCHOR).performClick()
        composeRule.onNodeWithTag(menuItemTag(Language.VN)).performClick()

        assertNull("Re-selecting current language must not re-emit", lastSelected)
    }
}
