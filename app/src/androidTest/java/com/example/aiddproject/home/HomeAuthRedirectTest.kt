package com.example.aiddproject.home

import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.platform.app.InstrumentationRegistry
import com.example.aiddproject.R
import com.example.aiddproject.core.auth.AuthError
import com.example.aiddproject.core.auth.AuthErrorInterceptor
import com.example.aiddproject.core.auth.AuthRedirectController
import com.example.aiddproject.navigation.handleAuthRedirect
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Instrumented coverage for the Phase 6 401/403 redirect path on Home (T071).
 *
 * Hilt-test infrastructure isn't wired up in this project yet, so we drive the
 * wiring pattern that `AppNavigation` and `LoginScreen` use directly:
 *
 *  1. A real [AuthErrorInterceptor] + [AuthRedirectController] pair (no DI).
 *  2. A tiny Compose graph that mirrors `AppNavigation`'s redirect handler and
 *     `LoginScreen`'s session-expired snackbar binder.
 *
 * What this proves:
 *  - 401 from `/rest/v1/awards`, `/rest/v1/kudos_summary`, and
 *    `/rest/v1/notifications_summary` each emit `SessionExpired` → fake
 *    `signOut()` runs + the navigate-to-Login callback fires.
 *  - The `error_oauth_session_expired` snackbar surfaces in the localized copy.
 *  - 403 emits `Forbidden` → the navigate-to-Access-Denied callback fires;
 *    `signOut()` is NOT called.
 *
 * The full end-to-end Compose-renders-LoginScreen path is deferred until the
 * project adopts `HiltAndroidTest`. The unit test
 * `AuthRedirectHandlerTest` covers the strategy in isolation; together with
 * `AuthRedirectControllerTest`, this gives full branch coverage for the auth
 * bounce.
 */
class HomeAuthRedirectTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val ctx = InstrumentationRegistry.getInstrumentation().targetContext
    private lateinit var controllerScope: CoroutineScope

    @Before
    fun setUp() {
        controllerScope = CoroutineScope(Dispatchers.Main + Job())
    }

    @After
    fun tearDown() {
        controllerScope.cancel()
    }

    // -----------------------------------------------------------------------
    // 401 — one test per Home API surface (awards / kudos / notifications)
    // -----------------------------------------------------------------------

    @Test
    fun unauthenticated_on_awards_signs_out_and_routes_to_login() {
        verify401SurfacesSnackbarAndRedirect(path = "/rest/v1/awards")
    }

    @Test
    fun unauthenticated_on_kudos_signs_out_and_routes_to_login() {
        verify401SurfacesSnackbarAndRedirect(path = "/rest/v1/kudos_summary")
    }

    @Test
    fun unauthenticated_on_notifications_signs_out_and_routes_to_login() {
        verify401SurfacesSnackbarAndRedirect(path = "/rest/v1/notifications_summary")
    }

    // -----------------------------------------------------------------------
    // 403 — Forbidden does NOT sign out, routes to Access Denied
    // -----------------------------------------------------------------------

    @Test
    fun forbidden_routes_to_access_denied_and_does_not_sign_out() {
        val interceptor = AuthErrorInterceptor()
        val controller = AuthRedirectController(interceptor, controllerScope)
        var signOutCalls = 0
        var loginCalls = 0
        var accessDeniedCalls = 0

        composeRule.setContent {
            ScopedRedirectGraph(
                controller = controller,
                signOut = { signOutCalls++ },
                onNavigateToLogin = { loginCalls++ },
                onNavigateToAccessDenied = { accessDeniedCalls++ },
            )
        }
        composeRule.waitForIdle()

        interceptor.emit(AuthError.Forbidden("/rest/v1/awards"))

        composeRule.waitUntil(timeoutMillis = 2_000) { accessDeniedCalls == 1 }
        assertEquals("signOut must NOT run on 403", 0, signOutCalls)
        assertEquals(0, loginCalls)
        assertEquals(1, accessDeniedCalls)
    }

    // -----------------------------------------------------------------------
    // sessionExpiredHint pipeline (controller-level, drives the snackbar)
    // -----------------------------------------------------------------------

    @Test
    fun forbidden_does_not_enqueue_session_expired_hint() {
        val interceptor = AuthErrorInterceptor()
        val controller = AuthRedirectController(interceptor, controllerScope)
        var loginCalls = 0
        var accessDeniedCalls = 0

        composeRule.setContent {
            ScopedRedirectGraph(
                controller = controller,
                signOut = { /* no-op */ },
                onNavigateToLogin = { loginCalls++ },
                onNavigateToAccessDenied = { accessDeniedCalls++ },
            )
        }
        composeRule.waitForIdle()

        interceptor.emit(AuthError.Forbidden("/rest/v1/awards"))
        composeRule.waitUntil(timeoutMillis = 2_000) { accessDeniedCalls == 1 }

        // Snackbar must NOT appear on 403 — it's only for session-expired.
        assertEquals(0, loginCalls)
        assertTrue(
            "Forbidden must not enqueue a sessionExpiredHint",
            controller.sessionExpiredHint.replayCache.isEmpty(),
        )
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private fun verify401SurfacesSnackbarAndRedirect(path: String) {
        val interceptor = AuthErrorInterceptor()
        val controller = AuthRedirectController(interceptor, controllerScope)
        var signOutCalls = 0
        var loginCalls = 0
        var accessDeniedCalls = 0

        composeRule.setContent {
            ScopedRedirectGraph(
                controller = controller,
                signOut = { signOutCalls++ },
                onNavigateToLogin = { loginCalls++ },
                onNavigateToAccessDenied = { accessDeniedCalls++ },
            )
        }
        composeRule.waitForIdle()

        interceptor.emit(AuthError.Unauthenticated(path))

        composeRule.waitUntil(timeoutMillis = 2_000) { loginCalls == 1 }
        assertEquals("signOut must run exactly once on 401", 1, signOutCalls)
        assertEquals(1, loginCalls)
        assertEquals(0, accessDeniedCalls)

        composeRule
            .onNodeWithText(ctx.getString(R.string.error_oauth_session_expired))
            .assertIsDisplayed()
    }
}

/**
 * Mirrors the wiring `AppNavigation` + `LoginScreen` install at runtime:
 *  - Collects `controller.events` and routes through `handleAuthRedirect`.
 *  - Hosts a `SnackbarHost` that surfaces `controller.sessionExpiredHint`
 *    via the `error_oauth_session_expired` string.
 *
 * Kept private to this test file — production wiring lives in
 * `AppNavigation.AppNavigation` and `LoginScreen.LoginScreen`.
 */
@androidx.compose.runtime.Composable
private fun ScopedRedirectGraph(
    controller: AuthRedirectController,
    signOut: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToAccessDenied: () -> Unit,
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(controller) {
        controller.events.collect { event ->
            handleAuthRedirect(
                event = event,
                signOut = { signOut() },
                navigateToLogin = onNavigateToLogin,
                navigateToAccessDenied = onNavigateToAccessDenied,
            )
        }
    }
    LaunchedEffect(controller) {
        controller.sessionExpiredHint.collect {
            snackbarHostState.showSnackbar(
                message = context.getString(R.string.error_oauth_session_expired),
            )
            controller.consumeSessionExpiredHint()
        }
    }

    // Scaffold body is empty — the test only asserts the snackbar text.
    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        content = { _ -> },
    )
}
