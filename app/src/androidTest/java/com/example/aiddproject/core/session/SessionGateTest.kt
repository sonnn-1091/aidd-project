package com.example.aiddproject.core.session

import androidx.compose.ui.test.junit4.createComposeRule
import com.example.aiddproject.ui.theme.AIDDProjectTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * SessionGate routing-effect coverage (T050, US2).
 *
 * Drives [SessionGateContent] directly with each terminal [AuthState] and asserts the
 * correct one-shot navigation callback fires:
 *  - Authenticated → onAuthenticated (Home)
 *  - Unauthenticated → onUnauthenticated (Login)
 *  - Loading → no callback yet (splash visible)
 *  - Error → onUnauthenticated (Login; caller may surface session-expired snackbar)
 */
class SessionGateTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun authenticated_state_invokes_onAuthenticated() {
        var authCalls = 0
        var unauthCalls = 0
        composeRule.setContent {
            AIDDProjectTheme {
                SessionGateContent(
                    state = AuthState.Authenticated(userId = "alice"),
                    onAuthenticated = { authCalls++ },
                    onUnauthenticated = { unauthCalls++ },
                )
            }
        }
        composeRule.waitForIdle()
        assertEquals(1, authCalls)
        assertEquals(0, unauthCalls)
    }

    @Test
    fun unauthenticated_state_invokes_onUnauthenticated() {
        var authCalls = 0
        var unauthCalls = 0
        composeRule.setContent {
            AIDDProjectTheme {
                SessionGateContent(
                    state = AuthState.Unauthenticated,
                    onAuthenticated = { authCalls++ },
                    onUnauthenticated = { unauthCalls++ },
                )
            }
        }
        composeRule.waitForIdle()
        assertEquals(0, authCalls)
        assertEquals(1, unauthCalls)
    }

    @Test
    fun loading_state_invokes_neither_callback() {
        var authCalls = 0
        var unauthCalls = 0
        composeRule.setContent {
            AIDDProjectTheme {
                SessionGateContent(
                    state = AuthState.Loading,
                    onAuthenticated = { authCalls++ },
                    onUnauthenticated = { unauthCalls++ },
                )
            }
        }
        composeRule.waitForIdle()
        assertEquals(0, authCalls)
        assertEquals(0, unauthCalls)
    }

    @Test
    fun error_state_routes_to_unauthenticated() {
        var authCalls = 0
        var unauthCalls = 0
        composeRule.setContent {
            AIDDProjectTheme {
                SessionGateContent(
                    state = AuthState.Error(AuthErrorReason.SessionExpired),
                    onAuthenticated = { authCalls++ },
                    onUnauthenticated = { unauthCalls++ },
                )
            }
        }
        composeRule.waitForIdle()
        assertEquals(0, authCalls)
        assertTrue("Error state must route to Unauthenticated", unauthCalls >= 1)
    }
}
