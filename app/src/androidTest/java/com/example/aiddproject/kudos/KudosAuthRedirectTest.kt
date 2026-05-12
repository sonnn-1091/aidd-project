package com.example.aiddproject.kudos

import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalResources
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
 * Instrumented coverage for Sun*Kudos's 401/403 auth-redirect path
 * (spec § US2, T056). Mirrors [com.example.aiddproject.home.HomeAuthRedirectTest]
 * — same wiring strategy: a real [AuthErrorInterceptor] +
 * [AuthRedirectController] pair driving a tiny Compose graph that
 * mirrors `AppNavigation`'s `handleAuthRedirect` collector +
 * `LoginScreen`'s session-expired snackbar binder.
 *
 * What this proves for the Sun*Kudos hub specifically:
 *  - 401 from each kudos data plane path (`/rest/v1/kudos`,
 *    `/rest/v1/reactions`, `/rest/v1/spotlight_graph`,
 *    `/rest/v1/user_stats`, `/rest/v1/secret_boxes`) emits
 *    `SessionExpired` → fake `signOut()` runs + the Login redirect
 *    fires.
 *  - 403 emits `Forbidden` → Access Denied redirect; `signOut()`
 *    must NOT fire.
 *
 * No VM-level wiring is required (T054/T055 no-op): the
 * `AuthErrorInterceptor` is installed on the Ktor HttpClient at
 * Hilt-module construction time (see `core/di/SupabaseModule.kt`),
 * so every Supabase Postgrest call funnels through it before the
 * repository even returns. The Phase 3 [KudosViewModel] inherits
 * that bounce for free — matching the canonical Award Detail
 * pattern.
 */
class KudosAuthRedirectTest {
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
    // 401 — one test per Sun*Kudos API surface
    // -----------------------------------------------------------------------

    @Test
    fun unauthenticated_on_kudos_list_signs_out_and_routes_to_login() {
        verify401SurfacesSnackbarAndRedirect(path = "/rest/v1/kudos")
    }

    @Test
    fun unauthenticated_on_reactions_signs_out_and_routes_to_login() {
        verify401SurfacesSnackbarAndRedirect(path = "/rest/v1/reactions")
    }

    @Test
    fun unauthenticated_on_spotlight_graph_signs_out_and_routes_to_login() {
        verify401SurfacesSnackbarAndRedirect(path = "/rest/v1/spotlight_graph")
    }

    @Test
    fun unauthenticated_on_user_stats_signs_out_and_routes_to_login() {
        verify401SurfacesSnackbarAndRedirect(path = "/rest/v1/user_stats")
    }

    @Test
    fun unauthenticated_on_secret_boxes_signs_out_and_routes_to_login() {
        verify401SurfacesSnackbarAndRedirect(path = "/rest/v1/secret_boxes")
    }

    // -----------------------------------------------------------------------
    // 403 — Forbidden routes to Access Denied; does NOT sign out
    // -----------------------------------------------------------------------

    @Test
    fun forbidden_on_kudos_paths_routes_to_access_denied() {
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

        interceptor.emit(AuthError.Forbidden("/rest/v1/kudos"))

        composeRule.waitUntil(timeoutMillis = 2_000) { accessDeniedCalls == 1 }
        assertEquals("signOut must NOT run on 403", 0, signOutCalls)
        assertEquals(0, loginCalls)
        assertEquals(1, accessDeniedCalls)
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
 *  - Collects `controller.events` and routes through [handleAuthRedirect].
 *  - Hosts a `SnackbarHost` that surfaces `controller.sessionExpiredHint`
 *    via the `error_oauth_session_expired` string.
 */
@Composable
private fun ScopedRedirectGraph(
    controller: AuthRedirectController,
    signOut: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToAccessDenied: () -> Unit,
) {
    val resources = LocalResources.current
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
                message = resources.getString(R.string.error_oauth_session_expired),
            )
            controller.consumeSessionExpiredHint()
        }
    }

    @Suppress("UnusedMaterial3ScaffoldPaddingParameter")
    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        content = { _ -> },
    )
}
