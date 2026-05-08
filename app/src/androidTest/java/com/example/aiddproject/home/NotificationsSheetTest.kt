package com.example.aiddproject.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.example.aiddproject.core.locale.Language
import com.example.aiddproject.core.locale.LanguageProvider
import com.example.aiddproject.home.ui.components.BellWithBadge
import com.example.aiddproject.home.ui.components.NotificationsSheet
import com.example.aiddproject.home.ui.components.TEST_TAG_HOME_BELL
import com.example.aiddproject.home.ui.components.TEST_TAG_HOME_BELL_BADGE
import com.example.aiddproject.home.ui.components.TEST_TAG_NOTIFICATIONS_SHEET
import com.example.aiddproject.ui.theme.AIDDProjectTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/**
 * Compose UI coverage for the bell + Notifications sheet wiring (US6, T084):
 *  - Bell renders the badge node only when `unreadCount > 0`.
 *  - Bell is clickable in every notifications state (Loading / Loaded / Error)
 *    so a transient API failure doesn't block opening the panel (spec edge
 *    case "Notifications API timeout → bell renders without a badge; tapping
 *    still opens the panel").
 *  - Tapping the bell opens the [NotificationsSheet].
 *  - Bell tap is double-tap-suppressed (TR-005).
 *
 * The dismiss path (`onDismissRequest` → `viewModel.onNotificationsSheetDismissed()`)
 * cannot be reliably driven through the Compose harness across SDK levels —
 * tapping ModalBottomSheet's scrim is non-deterministic. Coverage of that
 * contract lives in `HomeViewModelTest.onNotificationsSheetDismissed re-fires
 * only the notifications fetch`.
 */
class NotificationsSheetTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun badge_node_renders_when_unread_count_positive() {
        setBellOnly(unreadCount = 2)
        composeRule.onNodeWithTag(TEST_TAG_HOME_BELL_BADGE).assertIsDisplayed()
    }

    @Test
    fun badge_node_does_not_render_when_unread_count_is_zero() {
        setBellOnly(unreadCount = 0)
        composeRule.onNodeWithTag(TEST_TAG_HOME_BELL_BADGE).assertDoesNotExist()
    }

    @Test
    fun bell_is_clickable_when_loading_state_has_zero_unread() {
        // Loading uses unread = 0 (no badge yet) and bell stays tappable.
        setBellOnly(unreadCount = 0)
        composeRule.onNodeWithTag(TEST_TAG_HOME_BELL).assertHasClickAction()
    }

    @Test
    fun bell_is_clickable_when_loaded_with_unread_count() {
        setBellOnly(unreadCount = 5)
        composeRule.onNodeWithTag(TEST_TAG_HOME_BELL).assertHasClickAction()
    }

    @Test
    fun bell_tap_fires_callback_once() {
        var taps = 0
        setBellOnly(unreadCount = 2, onClick = { taps++ })

        composeRule.onNodeWithTag(TEST_TAG_HOME_BELL).performClick()

        assertEquals(1, taps)
    }

    @Test
    fun bell_double_tap_yields_exactly_one_callback() {
        var taps = 0
        setBellOnly(unreadCount = 2, onClick = { taps++ })

        composeRule.onNodeWithTag(TEST_TAG_HOME_BELL).performClick()
        composeRule.onNodeWithTag(TEST_TAG_HOME_BELL).performClick()

        // The single-click guard suppresses the second tap (TR-005, SC-002).
        assertEquals(1, taps)
    }

    @Test
    fun bell_tap_opens_notifications_sheet() {
        setBellAndSheet()

        composeRule.onNodeWithTag(TEST_TAG_NOTIFICATIONS_SHEET).assertDoesNotExist()
        composeRule.onNodeWithTag(TEST_TAG_HOME_BELL).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag(TEST_TAG_NOTIFICATIONS_SHEET).assertIsDisplayed()
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private fun setBellOnly(
        unreadCount: Int,
        onClick: () -> Unit = {},
    ) {
        composeRule.setContent {
            AIDDProjectTheme {
                LanguageProvider(language = Language.VN) {
                    BellWithBadge(unreadCount = unreadCount, onClick = onClick)
                }
            }
        }
    }

    private fun setBellAndSheet() {
        composeRule.setContent {
            AIDDProjectTheme {
                LanguageProvider(language = Language.VN) {
                    BellAndSheetHost()
                }
            }
        }
    }
}

@Composable
private fun BellAndSheetHost() {
    var visible by remember { mutableStateOf(false) }
    BellWithBadge(unreadCount = 2, onClick = { visible = true })
    if (visible) {
        NotificationsSheet(
            onDismissRequest = { visible = false },
        )
    }
}
