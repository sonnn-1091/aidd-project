package com.example.aiddproject.navigation

import com.example.aiddproject.core.auth.AuthRedirectEvent
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests for [handleAuthRedirect] (T073). Covers the navigation strategy
 * extracted from `AppNavigation` so we can assert the exact `signOut()` +
 * `navigate*` ordering without spinning up a real NavController.
 */
class AuthRedirectHandlerTest {
    @Test
    fun `SessionExpired calls signOut then navigateToLogin only`() =
        runTest {
            val signOut: suspend () -> Unit = mockk(relaxed = true)
            val calls = mutableListOf<String>()

            handleAuthRedirect(
                event = AuthRedirectEvent.SessionExpired,
                signOut = {
                    signOut()
                    calls += "signOut"
                },
                navigateToLogin = { calls += "navigateToLogin" },
                navigateToAccessDenied = { calls += "navigateToAccessDenied" },
            )

            // signOut MUST run before the redirect so the SDK clears its
            // tokens before the next request — order matters here.
            assertEquals(listOf("signOut", "navigateToLogin"), calls)
            coVerify(exactly = 1) { signOut() }
        }

    @Test
    fun `SessionExpired still navigates if signOut throws`() =
        runTest {
            val calls = mutableListOf<String>()

            handleAuthRedirect(
                event = AuthRedirectEvent.SessionExpired,
                signOut = { throw IllegalStateException("network down") },
                navigateToLogin = { calls += "navigateToLogin" },
                navigateToAccessDenied = { calls += "navigateToAccessDenied" },
            )

            // The bounce MUST happen even if the SDK's signOut itself fails;
            // otherwise the user is stuck on a screen they can't authenticate
            // back into. The interceptor's auth/v1 whitelist already prevents
            // a 401 on signOut from re-firing the redirect.
            assertEquals(listOf("navigateToLogin"), calls)
        }

    @Test
    fun `Forbidden navigates to Access Denied without signing out`() =
        runTest {
            val signOut: suspend () -> Unit = mockk(relaxed = true)
            val calls = mutableListOf<String>()

            handleAuthRedirect(
                event = AuthRedirectEvent.Forbidden,
                signOut = {
                    signOut()
                    calls += "signOut"
                },
                navigateToLogin = { calls += "navigateToLogin" },
                navigateToAccessDenied = { calls += "navigateToAccessDenied" },
            )

            assertEquals(listOf("navigateToAccessDenied"), calls)
            // The principal may regain access on a different surface — do NOT
            // sign them out (per AuthRedirectEvent.Forbidden contract).
            coVerify(exactly = 0) { signOut() }
        }
}
