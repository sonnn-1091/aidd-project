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

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalTime::class)
class SessionRepositoryTest {
    private fun userSession(userId: String = "user-1"): UserSession =
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
    fun `Initializing maps to Loading`() =
        runTest {
            val source = MutableStateFlow<SessionStatus>(SessionStatus.Initializing)
            val repo = SessionRepository(source, backgroundScope)

            repo.state.test {
                assertEquals(AuthState.Loading, awaitItem())
                cancelAndConsumeRemainingEvents()
            }
        }

    @Test
    fun `Authenticated maps to Authenticated with user id`() =
        runTest {
            val source =
                MutableStateFlow<SessionStatus>(
                    SessionStatus.Authenticated(session = userSession(userId = "alice")),
                )
            val repo = SessionRepository(source, backgroundScope)

            repo.state.test {
                // First emission is the seeded `Loading` initial value from `stateIn`; the
                // mapped Authenticated value follows once the upstream collector is advanced.
                assertEquals(AuthState.Loading, awaitItem())
                assertEquals(AuthState.Authenticated(userId = "alice"), awaitItem())
                cancelAndConsumeRemainingEvents()
            }
        }

    @Test
    fun `NotAuthenticated maps to Unauthenticated`() =
        runTest {
            val source = MutableStateFlow<SessionStatus>(SessionStatus.NotAuthenticated())
            val repo = SessionRepository(source, backgroundScope)

            repo.state.test {
                assertEquals(AuthState.Loading, awaitItem())
                assertEquals(AuthState.Unauthenticated, awaitItem())
                cancelAndConsumeRemainingEvents()
            }
        }

    @Test
    fun `RefreshFailure maps to Error SessionExpired`() =
        runTest {
            val source =
                MutableStateFlow<SessionStatus>(
                    SessionStatus.RefreshFailure(
                        cause = RefreshFailureCause.NetworkError(Throwable("token expired")),
                    ),
                )
            val repo = SessionRepository(source, backgroundScope)

            repo.state.test {
                assertEquals(AuthState.Loading, awaitItem())
                assertEquals(AuthState.Error(AuthErrorReason.SessionExpired), awaitItem())
                cancelAndConsumeRemainingEvents()
            }
        }

    @Test
    fun `state flow tracks transitions Loading then Authenticated then Unauthenticated`() =
        runTest {
            val source = MutableStateFlow<SessionStatus>(SessionStatus.Initializing)
            val repo = SessionRepository(source, backgroundScope)

            repo.state.test {
                assertEquals(AuthState.Loading, awaitItem())

                source.value = SessionStatus.Authenticated(session = userSession("alice"))
                assertEquals(AuthState.Authenticated(userId = "alice"), awaitItem())

                source.value = SessionStatus.NotAuthenticated()
                assertEquals(AuthState.Unauthenticated, awaitItem())

                cancelAndConsumeRemainingEvents()
            }
        }
}
