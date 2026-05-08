package com.example.aiddproject.core.session

/**
 * Single source of truth for app-wide authentication state.
 *
 * - [Loading]: SDK is hydrating the persisted session (cold start, before SessionGate
 *   decides where to navigate).
 * - [Unauthenticated]: no valid session — show Login.
 * - [Authenticated]: a valid Supabase session is active — Sunner verification still
 *   runs separately to gate Home vs Access denied.
 * - [Error]: terminal failure for the current sign-in attempt.
 */
sealed interface AuthState {
    data object Loading : AuthState

    data object Unauthenticated : AuthState

    data class Authenticated(
        val userId: String,
    ) : AuthState

    data class Error(
        val reason: AuthErrorReason,
    ) : AuthState
}

/** Reasons an auth attempt or session can fail. Maps 1:1 to the error string keys
 *  defined in spec § Localized Copy. */
enum class AuthErrorReason {
    NotASunner,
    Network,
    AccountDisabled,
    OAuthCodeExpired,
    PlayServicesUnavailable,
    SessionExpired,
    Generic,
}
