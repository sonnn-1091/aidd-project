package com.example.aiddproject.auth.login

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.test.requestFocus
import com.example.aiddproject.auth.login.ui.LoginScreenContent
import com.example.aiddproject.auth.login.ui.LoginUiState
import com.example.aiddproject.auth.login.ui.components.TEST_TAG
import com.example.aiddproject.ui.theme.AIDDProjectTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * T064 — Keyboard parity (spec § Behavioral Accessibility): every interactive control
 * MUST be reachable + activatable via keyboard. We focus the CTA programmatically and
 * confirm `Enter` triggers the same `onSignInTap` callback as a tap.
 *
 * `requestFocus()` from compose-ui-test is annotated `@ExperimentalTestApi` since
 * it programmatically grabs the keyboard focus from the test thread; we opt in
 * here and at every Phase-10 keyboard-parity test.
 */
@OptIn(ExperimentalTestApi::class)
class LoginKeyboardTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun pressing_Enter_on_focused_CTA_triggers_sign_in() {
        var taps = 0
        composeRule.setContent {
            AIDDProjectTheme {
                LoginScreenContent(
                    state = LoginUiState(),
                    snackbarHostState = remember { SnackbarHostState() },
                    onSignInTap = { taps++ },
                )
            }
        }

        composeRule.onNodeWithTag(TEST_TAG).requestFocus()
        composeRule.onNodeWithTag(TEST_TAG).performKeyInput { pressKey(Key.Enter) }

        assertTrue("Enter on focused CTA must invoke onSignInTap (got $taps)", taps >= 1)
    }
}
