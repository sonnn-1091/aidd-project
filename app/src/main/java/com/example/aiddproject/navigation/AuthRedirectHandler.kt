package com.example.aiddproject.navigation

import com.example.aiddproject.core.auth.AuthRedirectEvent

/**
 * Pure navigation strategy for [AuthRedirectEvent], extracted from `AppNavigation`
 * so it can be unit-tested without the Compose runtime / NavController.
 *
 * Per Q-Plan-3, both branches replace the back stack via `popUpTo(GATE) {
 * inclusive = true }`, which the [navigateToLogin] / [navigateToAccessDenied]
 * lambdas encode.
 *
 * - `SessionExpired`: `signOut()` first (so the SDK clears any cached tokens
 *   before the next request), then route to Login. We swallow signOut failures
 *   on purpose — the bounce must happen even if the SDK call itself returns
 *   401 (which the [com.example.aiddproject.core.auth.AuthErrorInterceptor]
 *   whitelist already prevents from looping).
 * - `Forbidden`: route to Access Denied without signing out — the principal
 *   may regain access on a different surface.
 */
internal suspend fun handleAuthRedirect(
    event: AuthRedirectEvent,
    signOut: suspend () -> Unit,
    navigateToLogin: () -> Unit,
    navigateToAccessDenied: () -> Unit,
) {
    when (event) {
        AuthRedirectEvent.SessionExpired -> {
            runCatching { signOut() }
            navigateToLogin()
        }
        AuthRedirectEvent.Forbidden -> {
            navigateToAccessDenied()
        }
    }
}
