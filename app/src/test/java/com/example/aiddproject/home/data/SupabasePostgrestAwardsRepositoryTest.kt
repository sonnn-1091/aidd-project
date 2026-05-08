package com.example.aiddproject.home.data

import com.example.aiddproject.home.domain.Award
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Repository-level tests against a fake [SupabasePostgrestAwardsGateway]. Mirrors the
 * gateway pattern used by [SupabaseAuthRepositoryTest] in Login — the actual SDK call
 * path is exercised by the instrumented integration test that hits a local Supabase
 * stack with the seeded migration data (Constitution Principle V).
 */
class SupabasePostgrestAwardsRepositoryTest {
    private val gateway: SupabasePostgrestAwardsGateway = mockk(relaxed = true)
    private val repository = SupabasePostgrestAwardsRepository(gateway)

    @Test
    fun `list maps the gateway's list and returns Result success`() =
        runTest {
            val awards =
                listOf(
                    Award(id = "a1", name = "Top Talent", thumbnailUrl = null, sortOrder = 1),
                    Award(id = "a2", name = "Top Project", thumbnailUrl = null, sortOrder = 2),
                )
            coEvery { gateway.listAwards() } returns awards

            val result = repository.list()

            assertTrue(result.isSuccess)
            assertEquals(awards, result.getOrNull())
            coVerify(exactly = 1) { gateway.listAwards() }
        }

    @Test
    fun `list maps an empty result to Result success with empty list`() =
        runTest {
            coEvery { gateway.listAwards() } returns emptyList()

            val result = repository.list()

            assertTrue(result.isSuccess)
            assertEquals(emptyList<Award>(), result.getOrNull())
        }

    @Test
    fun `list wraps a network exception as Result failure`() =
        runTest {
            val cause = RuntimeException("network down")
            coEvery { gateway.listAwards() } throws cause

            val result = repository.list()

            assertTrue(result.isFailure)
            assertEquals(cause, result.exceptionOrNull())
        }
}
