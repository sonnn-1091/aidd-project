package com.example.aiddproject.core.auth

import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.observer.ResponseObserver
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Ktor HttpClient observer that catches 401 / 403 responses on the Supabase
 * data plane (Postgrest / RPCs) and emits them to errors for
 * AuthRedirectController to act on.
 *
 * Whitelist: requests against the auth/v1 prefix are skipped so an in-flight
 * signOut request (which itself can return 401 if the token already expired)
 * does not re-trigger the redirect and produce a loop.
 */
@Singleton
class AuthErrorInterceptor
    @Inject
    constructor() {
        private val mutableErrors: MutableSharedFlow<AuthError> =
            MutableSharedFlow(replay = 0, extraBufferCapacity = 1)
        val errors: SharedFlow<AuthError> = mutableErrors.asSharedFlow()

        fun installInto(httpConfig: HttpClientConfig<*>) {
            httpConfig.install(ResponseObserver) {
                onResponse { response ->
                    val status = response.status.value
                    val path = response.call.request.url.encodedPath
                    classifyAuthError(status, path)?.let(::emit)
                }
            }
        }

        /** Test seam: lets unit tests drive the flow without spinning up Ktor. */
        fun emit(error: AuthError) {
            mutableErrors.tryEmit(error)
        }
    }

/**
 * Pure classifier: folds an HTTP status + path into the matching AuthError
 * or null when no redirect is needed. Whitelists the auth/v1 path prefix so
 * the interceptor does not fire on signOut round-trips.
 */
internal fun classifyAuthError(
    status: Int,
    path: String,
): AuthError? {
    if (path.startsWith("/auth/v1/")) return null
    return when (status) {
        401 -> AuthError.Unauthenticated(path)
        403 -> AuthError.Forbidden(path)
        else -> null
    }
}
