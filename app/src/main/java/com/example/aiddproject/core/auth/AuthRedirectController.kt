package com.example.aiddproject.core.auth

import com.example.aiddproject.core.di.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Singleton that observes [AuthErrorInterceptor.errors] and exposes a stream
 * of [AuthRedirectEvent]s for the NavHost to act on. Per Q-Plan-3, the NavHost
 * reacts with `popUpTo(GATE) { inclusive = true }` so any modal sheet (e.g.
 * the Notifications sheet on Home) is dismissed implicitly with the route
 * replacement.
 *
 * Subscribers `collect` from a `SharedFlow` (replay = 0) — a configuration
 * change should NOT replay the redirect.
 *
 * In addition, every [AuthRedirectEvent.SessionExpired] also enqueues a one-shot
 * marker on [sessionExpiredHint] (replay = 1). LoginScreen subscribes to that
 * stream when it composes after the bounce, surfaces the
 * `error_oauth_session_expired` snackbar (FR-014), and clears the cached marker
 * via [consumeSessionExpiredHint] so a configuration change doesn't re-show it.
 */
@Singleton
class AuthRedirectController
    @Inject
    constructor(
        private val interceptor: AuthErrorInterceptor,
        @ApplicationScope private val scope: CoroutineScope,
    ) {
        private val _events: MutableSharedFlow<AuthRedirectEvent> =
            MutableSharedFlow(
                replay = 0,
                extraBufferCapacity = 1,
            )
        val events: SharedFlow<AuthRedirectEvent> = _events.asSharedFlow()

        private val _sessionExpiredHint: MutableSharedFlow<Unit> =
            MutableSharedFlow(
                replay = 1,
                extraBufferCapacity = 0,
                onBufferOverflow = BufferOverflow.DROP_OLDEST,
            )
        val sessionExpiredHint: SharedFlow<Unit> = _sessionExpiredHint.asSharedFlow()

        init {
            scope.launch {
                interceptor.errors.collect { error ->
                    val event = error.toRedirectEvent()
                    if (event is AuthRedirectEvent.SessionExpired) {
                        _sessionExpiredHint.tryEmit(Unit)
                    }
                    _events.tryEmit(event)
                }
            }
        }

        /**
         * Drops the latest replayed [sessionExpiredHint] so a configuration change
         * after Login has already surfaced the snackbar doesn't re-show it.
         */
        fun consumeSessionExpiredHint() {
            _sessionExpiredHint.resetReplayCache()
        }

        private fun AuthError.toRedirectEvent(): AuthRedirectEvent =
            when (this) {
                is AuthError.Unauthenticated -> AuthRedirectEvent.SessionExpired
                is AuthError.Forbidden -> AuthRedirectEvent.Forbidden
            }
    }
