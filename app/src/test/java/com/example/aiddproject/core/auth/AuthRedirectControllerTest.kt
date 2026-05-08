package com.example.aiddproject.core.auth

import app.cash.turbine.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Coverage for the auth-error pipeline (T024):
 *
 * 1. Pure classifier ([classifyAuthError]) — fast unit tests of status + path
 *    mapping without any Ktor / coroutine machinery.
 * 2. End-to-end via [AuthErrorInterceptor.emit] → [AuthRedirectController.events].
 *    Uses runTest's `backgroundScope` so the controller's collector lives only
 *    for the test's lifetime.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AuthRedirectControllerTest {
    // ---- Classifier (pure function) ----

    @Test
    fun `401 on data path classifies as Unauthenticated`() {
        val out = classifyAuthError(status = 401, path = "/rest/v1/awards")
        assertEquals(AuthError.Unauthenticated("/rest/v1/awards"), out)
    }

    @Test
    fun `403 on data path classifies as Forbidden`() {
        val out = classifyAuthError(status = 403, path = "/rest/v1/notifications")
        assertEquals(AuthError.Forbidden("/rest/v1/notifications"), out)
    }

    @Test
    fun `auth slash v1 paths are whitelisted - 401 is NOT classified`() {
        // signOut response 401 must NOT re-trigger the redirect (loop guard).
        val out = classifyAuthError(status = 401, path = "/auth/v1/logout")
        assertNull(out)
    }

    @Test
    fun `auth slash v1 paths are whitelisted - 403 is NOT classified`() {
        val out = classifyAuthError(status = 403, path = "/auth/v1/token")
        assertNull(out)
    }

    @Test
    fun `2xx responses are not classified`() {
        assertNull(classifyAuthError(status = 200, path = "/rest/v1/awards"))
        assertNull(classifyAuthError(status = 204, path = "/rest/v1/awards"))
    }

    @Test
    fun `5xx responses are not classified - server error is not an auth bounce`() {
        assertNull(classifyAuthError(status = 500, path = "/rest/v1/awards"))
        assertNull(classifyAuthError(status = 503, path = "/rest/v1/awards"))
    }

    @Test
    fun `404 - 422 - other 4xx codes do NOT classify - only 401 and 403 are auth-bounces`() {
        assertNull(classifyAuthError(status = 404, path = "/rest/v1/awards"))
        assertNull(classifyAuthError(status = 422, path = "/rest/v1/awards"))
        assertNull(classifyAuthError(status = 400, path = "/rest/v1/awards"))
    }

    // ---- End-to-end interceptor → controller ----

    @Test
    fun `interceptor 401 emit propagates to SessionExpired event`() =
        runTest(UnconfinedTestDispatcher()) {
            val interceptor = AuthErrorInterceptor()
            val controller = AuthRedirectController(interceptor, backgroundScope)

            controller.events.test {
                interceptor.emit(AuthError.Unauthenticated("/rest/v1/awards"))
                assertEquals(AuthRedirectEvent.SessionExpired, awaitItem())
                cancelAndConsumeRemainingEvents()
            }
        }

    @Test
    fun `interceptor 403 emit propagates to Forbidden event`() =
        runTest(UnconfinedTestDispatcher()) {
            val interceptor = AuthErrorInterceptor()
            val controller = AuthRedirectController(interceptor, backgroundScope)

            controller.events.test {
                interceptor.emit(AuthError.Forbidden("/rest/v1/notifications"))
                assertEquals(AuthRedirectEvent.Forbidden, awaitItem())
                cancelAndConsumeRemainingEvents()
            }
        }

    @Test
    fun `multiple emits produce one event each in order`() =
        runTest(UnconfinedTestDispatcher()) {
            val interceptor = AuthErrorInterceptor()
            val controller = AuthRedirectController(interceptor, backgroundScope)

            controller.events.test {
                interceptor.emit(AuthError.Unauthenticated("/rest/v1/awards"))
                assertEquals(AuthRedirectEvent.SessionExpired, awaitItem())
                interceptor.emit(AuthError.Forbidden("/rest/v1/notifications"))
                assertEquals(AuthRedirectEvent.Forbidden, awaitItem())
                cancelAndConsumeRemainingEvents()
            }
        }
}
