package com.example.aiddproject.core.auth

/**
 * One-shot navigation directive emitted by [AuthRedirectController] to the
 * NavHost. Subscribers `collect` from a `SharedFlow` (replay = 0) so a
 * configuration change doesn't replay the redirect.
 *
 * - [SessionExpired]: the SDK should `signOut()` and the NavHost should
 *   replace the entire stack with Login (Q-Plan-3 — `popUpTo(GATE) {
 *   inclusive = true }`). Login surfaces an `error_oauth_session_expired`
 *   snackbar so the user understands the bounce (FR-014).
 * - [Forbidden]: navigate to Access Denied; do NOT sign out (the session may
 *   still be valid for other surfaces).
 */
sealed interface AuthRedirectEvent {
    data object SessionExpired : AuthRedirectEvent

    data object Forbidden : AuthRedirectEvent
}
