package com.example.aiddproject.core.auth

import com.example.aiddproject.core.di.ApplicationScope
import kotlinx.coroutines.CoroutineScope
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

        init {
            scope.launch {
                interceptor.errors.collect { error ->
                    _events.tryEmit(error.toRedirectEvent())
                }
            }
        }

        private fun AuthError.toRedirectEvent(): AuthRedirectEvent =
            when (this) {
                is AuthError.Unauthenticated -> AuthRedirectEvent.SessionExpired
                is AuthError.Forbidden -> AuthRedirectEvent.Forbidden
            }
    }
