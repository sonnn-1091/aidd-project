package com.example.aiddproject.auth.login

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.test.platform.app.InstrumentationRegistry
import com.example.aiddproject.R
import com.example.aiddproject.auth.login.ui.LoginScreenContent
import com.example.aiddproject.auth.login.ui.LoginUiState
import com.example.aiddproject.auth.login.ui.TEST_TAG_LOGO
import com.example.aiddproject.ui.theme.AIDDProjectTheme
import org.junit.Rule
import org.junit.Test

/**
 * T062 — smoke coverage for FR-014: every render of the Login screen MUST display the
 * four static branding elements (logo, ROOT FURTHER tagline, localized description,
 * localized copyright).
 *
 * Drives `LoginScreenContent` directly so the assertion is independent of DI / VM state.
 */
class LoginBrandingTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun all_four_static_branding_elements_render() {
        composeRule.setContent {
            AIDDProjectTheme {
                LoginScreenContent(
                    state = LoginUiState(),
                    snackbarHostState = remember { SnackbarHostState() },
                    onSignInTap = {},
                )
            }
        }

        // 1) SAA logo — decorative for assistive tech (no contentDescription); located by tag.
        composeRule.onNodeWithTag(TEST_TAG_LOGO).assertIsDisplayed()

        // 2) ROOT FURTHER tagline — Image with brand-fixed contentDescription.
        composeRule
            .onNodeWithContentDescription(context.getString(R.string.brand_root_further))
            .assertIsDisplayed()

        // 3) Localized description (VN by default).
        composeRule
            .onNodeWithText(context.getString(R.string.login_description))
            .assertIsDisplayed()

        // 4) Localized copyright.
        composeRule
            .onNodeWithText(context.getString(R.string.login_copyright))
            .assertIsDisplayed()
    }
}
