package com.example.aiddproject.core.session

import com.example.aiddproject.core.di.ApplicationScope
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Maps Supabase's [SessionStatus] stream onto the app-wide [AuthState].
 *
 * The Supabase SDK is the source of truth for session lifecycle; we observe and
 * translate, never inspect tokens or persist the session ourselves (Constitution
 * Principle IV — no parallel cache).
 */
@Singleton
class SessionRepository
    @Inject
    constructor(
        // @JvmSuppressWildcards: Hilt would otherwise see this as Flow<? extends
        // SessionStatus> (Java covariant projection of an invariant Kotlin generic)
        // and fail to match the `Flow<SessionStatus>` provider in SupabaseModule.
        sessionStatus: @JvmSuppressWildcards Flow<SessionStatus>,
        @ApplicationScope scope: CoroutineScope,
    ) {
        val state: StateFlow<AuthState> =
            sessionStatus
                .map { it.toAuthState() }
                .stateIn(
                    scope = scope,
                    started = SharingStarted.Eagerly,
                    initialValue = AuthState.Loading,
                )

        private fun SessionStatus.toAuthState(): AuthState =
            when (this) {
                is SessionStatus.Initializing -> AuthState.Loading
                is SessionStatus.Authenticated ->
                    AuthState.Authenticated(
                        userId = session.user?.id.orEmpty(),
                    )
                is SessionStatus.NotAuthenticated -> AuthState.Unauthenticated
                is SessionStatus.RefreshFailure -> AuthState.Error(AuthErrorReason.SessionExpired)
            }
    }
