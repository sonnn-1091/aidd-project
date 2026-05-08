package com.example.aiddproject.core.session

import app.cash.turbine.test
import io.github.jan.supabase.auth.status.RefreshFailureCause
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.auth.user.UserInfo
import io.github.jan.supabase.auth.user.UserSession
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.time.ExperimentalTime

/**
 * US2 cold-start coverage for [SessionRepository].
 *
 * Extends the basic mappings in `SessionRepositoryTest` with the three transitions
 * that the Phase-4 `SessionGate` routes on:
 *  1. Hydrating with a valid token  → caller goes to Home (never sees Login).
 *  2. Hydrating with an expired one → caller sees Login (via SessionExpired).
 *  3. Post-logout                   → caller sees Login.
 */
@OptIn(ExperimentalCoroutinesApi::class, ExperimentalTime::class)
class SessionRepositoryColdStartTest {
    private fun userSession(userId: String = "alice"): UserSession =
        UserSession(
            accessToken = "access-token",
            refreshToken = "refresh-token",
            providerRefreshToken = null,
            providerToken = null,
            expiresIn = 3600L,
            tokenType = "bearer",
            user = UserInfo(id = userId, aud = "authenticated"),
            type = "implicit",
        )

    @Test
    fun `cold start with valid persisted token resolves to Authenticated without Unauthenticated emission`() =
        runTest {
            val source = MutableStateFlow<SessionStatus>(SessionStatus.Initializing)
            val repo = SessionRepository(source, backgroundScope)

            repo.state.test {
                assertEquals(AuthState.Loading, awaitItem())

                source.value = SessionStatus.Authenticated(session = userSession("alice"))
                assertEquals(AuthState.Authenticated(userId = "alice"), awaitItem())

                // No further emissions — Login is never a destination on this path
                // (TR-005: no Login flash).
                expectNoEvents()
                cancelAndConsumeRemainingEvents()
            }
        }

    @Test
    fun `cold start with expired token surfaces SessionExpired error`() =
        runTest {
            val source = MutableStateFlow<SessionStatus>(SessionStatus.Initializing)
            val repo = SessionRepository(source, backgroundScope)

            repo.state.test {
                assertEquals(AuthState.Loading, awaitItem())

                source.value =
                    SessionStatus.RefreshFailure(
                        cause = RefreshFailureCause.NetworkError(Throwable("expired")),
                    )
                assertEquals(AuthState.Error(AuthErrorReason.SessionExpired), awaitItem())

                cancelAndConsumeRemainingEvents()
            }
        }

    @Test
    fun `post logout cold start emits Unauthenticated`() =
        runTest {
            val source = MutableStateFlow<SessionStatus>(SessionStatus.NotAuthenticated())
            val repo = SessionRepository(source, backgroundScope)

            repo.state.test {
                assertEquals(AuthState.Loading, awaitItem())
                assertEquals(AuthState.Unauthenticated, awaitItem())
                cancelAndConsumeRemainingEvents()
            }
        }
}
