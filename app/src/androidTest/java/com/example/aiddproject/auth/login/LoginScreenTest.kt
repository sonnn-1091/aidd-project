package com.example.aiddproject.auth.login

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import com.example.aiddproject.R
import com.example.aiddproject.auth.login.ui.LoginError
import com.example.aiddproject.auth.login.ui.LoginScreenContent
import com.example.aiddproject.auth.login.ui.LoginUiState
import com.example.aiddproject.auth.login.ui.components.SPINNER_TEST_TAG
import com.example.aiddproject.auth.login.ui.components.TEST_TAG
import com.example.aiddproject.ui.theme.AIDDProjectTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/**
 * Compose UI tests for the [LoginScreenContent] (T047). Drives the stateless content
 * directly so the test never touches DI or the real ViewModel — `LoginViewModelTest` and
 * `LoginIntegrationTest` cover the VM and the live wire-up respectively.
 */
class LoginScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val ctaLabel: String =
        InstrumentationRegistry
            .getInstrumentation()
            .targetContext
            .getString(R.string.login_cta_label)

    @Test
    fun cta_renders_with_brand_label_and_is_clickable_when_idle() {
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

        composeRule.onNodeWithText(ctaLabel).assertIsDisplayed()
        composeRule
            .onNodeWithTag(TEST_TAG)
            .assertIsEnabled()
            .assertHasClickAction()
            .performClick()
        assertEquals(1, taps)
    }

    @Test
    fun cta_disabled_and_spinner_visible_while_loading() {
        composeRule.setContent {
            AIDDProjectTheme {
                LoginScreenContent(
                    state = LoginUiState(isLoading = true),
                    snackbarHostState = remember { SnackbarHostState() },
                    onSignInTap = {},
                )
            }
        }

        composeRule.onNodeWithTag(TEST_TAG).assertIsNotEnabled()
        composeRule.onNodeWithTag(SPINNER_TEST_TAG).assertIsDisplayed()
        // FR-004: label remains visible during loading
        composeRule.onNodeWithText(ctaLabel).assertIsDisplayed()
    }

    @Test
    fun cta_disabled_when_play_services_unavailable() {
        composeRule.setContent {
            AIDDProjectTheme {
                LoginScreenContent(
                    state = LoginUiState(playServicesAvailable = false),
                    snackbarHostState = remember { SnackbarHostState() },
                    onSignInTap = {},
                )
            }
        }

        composeRule.onNodeWithTag(TEST_TAG).assertIsNotEnabled()
    }

    @Test
    fun double_tap_in_loading_state_is_visually_suppressed() {
        // Although the FR-003 suppression lives in the VM, the screen must not let a
        // disabled CTA register clicks either. This is a defense-in-depth check.
        var taps = 0
        composeRule.setContent {
            AIDDProjectTheme {
                LoginScreenContent(
                    state = LoginUiState(isLoading = true),
                    snackbarHostState = remember { SnackbarHostState() },
                    onSignInTap = { taps++ },
                )
            }
        }

        composeRule.onNodeWithTag(TEST_TAG).performClick()
        composeRule.onNodeWithTag(TEST_TAG).performClick()
        assertEquals(0, taps)
    }

    @Test
    fun snackbar_shows_localized_error_message() {
        val expected =
            InstrumentationRegistry
                .getInstrumentation()
                .targetContext
                .getString(LoginError.Network.messageRes)
        composeRule.setContent {
            AIDDProjectTheme {
                val host = remember { SnackbarHostState() }
                LaunchedEffect(Unit) { host.showSnackbar(expected) }
                LoginScreenContent(
                    state = LoginUiState(error = LoginError.Network),
                    snackbarHostState = host,
                    onSignInTap = {},
                )
            }
        }

        composeRule.onNodeWithText(expected).assertIsDisplayed()
    }
}
