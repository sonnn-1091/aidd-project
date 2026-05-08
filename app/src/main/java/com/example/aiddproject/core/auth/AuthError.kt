package com.example.aiddproject.core.auth

/**
 * Auth-related HTTP failure observed by [AuthErrorInterceptor]. Kept narrow:
 * we only care about responses that should bounce the user out of the
 * authenticated app shell (Constitution Principle IV — RLS + 401/403 are
 * authoritative).
 *
 * - [Unauthenticated] (HTTP 401): session expired or revoked. Triggers
 *   `signOut()` + redirect to Login + `error_oauth_session_expired` snackbar.
 * - [Forbidden] (HTTP 403): session valid but the principal isn't allowed
 *   (e.g. RLS denied the row, account disabled). Routes to Access Denied
 *   without signing the user out — they may regain access later.
 */
sealed interface AuthError {
    val path: String

    data class Unauthenticated(
        override val path: String,
    ) : AuthError

    data class Forbidden(
        override val path: String,
    ) : AuthError
}
