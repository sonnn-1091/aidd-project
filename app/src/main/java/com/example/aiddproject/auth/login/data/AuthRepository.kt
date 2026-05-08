package com.example.aiddproject.auth.login.data

/**
 * Thin domain-facing seam over Supabase Auth so the use cases can be unit-tested without
 * spinning up a live Supabase client.
 *
 * - [signInWithIdToken]: exchanges a Google ID token for a Supabase session. Returns
 *   [Result.failure] on any SDK / network failure; the use case classifies the throwable
 *   into a [com.example.aiddproject.auth.login.ui.LoginError].
 * - [signOut]: clears the persisted session. Called when membership verification rejects a
 *   non-Sunner so [com.example.aiddproject.core.session.SessionRepository] does not stay
 *   in `Authenticated` after a failed gate check.
 * - [currentUserId]: read once after [signInWithIdToken] succeeds; used to scope the
 *   `users` row lookup. Non-suspend because the SDK exposes it synchronously.
 */
interface AuthRepository {
    suspend fun signInWithIdToken(token: String): Result<Unit>

    suspend fun signOut()

    fun currentUserId(): String?
}
