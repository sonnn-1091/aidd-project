package com.example.aiddproject.auth.login.data

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for the Result-wrapping logic in [SupabaseAuthRepository]. The actual SDK
 * call path is exercised end-to-end by `LoginIntegrationTest` against a local Supabase
 * stack (Constitution Principle V — repository layer tested against real Supabase, not
 * unconditional mocks; the gateway here is the test seam, not the SDK itself).
 */
class SupabaseAuthRepositoryTest {
    private val gateway: SupabaseAuthGateway = mockk(relaxed = true)
    private val repository = SupabaseAuthRepository(gateway)

    @Test
    fun `signInWithIdToken returns success when gateway succeeds`() =
        runTest {
            coEvery { gateway.signInWithGoogleIdToken("tok") } returns Unit

            val result = repository.signInWithIdToken("tok")

            assertTrue(result.isSuccess)
            coVerify(exactly = 1) { gateway.signInWithGoogleIdToken("tok") }
        }

    @Test
    fun `signInWithIdToken wraps gateway exception as Result failure`() =
        runTest {
            val cause = RuntimeException("auth boom")
            coEvery { gateway.signInWithGoogleIdToken(any()) } throws cause

            val result = repository.signInWithIdToken("tok")

            assertTrue(result.isFailure)
            assertEquals(cause, result.exceptionOrNull())
        }

    @Test
    fun `signOut delegates to gateway`() =
        runTest {
            repository.signOut()

            coVerify(exactly = 1) { gateway.signOut() }
        }

    @Test
    fun `currentUserId delegates to gateway`() {
        every { gateway.currentUserId() } returns "alice"

        assertEquals("alice", repository.currentUserId())
    }

    @Test
    fun `currentUserId returns null when gateway has no session`() {
        every { gateway.currentUserId() } returns null

        assertEquals(null, repository.currentUserId())
    }
}
