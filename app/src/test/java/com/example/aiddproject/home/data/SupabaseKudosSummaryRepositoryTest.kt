package com.example.aiddproject.home.data

import com.example.aiddproject.home.domain.KudosSummary
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SupabaseKudosSummaryRepositoryTest {
    private val gateway: SupabaseKudosSummaryGateway = mockk(relaxed = true)
    private val repository = SupabaseKudosSummaryRepository(gateway)

    @Test
    fun `get returns Result success with the gateway summary`() =
        runTest {
            val summary =
                KudosSummary(
                    isKudosAvailable = true,
                    bannerImageUrl = "https://cdn/banner.png",
                    badgeText = "FUN",
                    descriptionText = "Trao Kudos!",
                )
            coEvery { gateway.fetchKudosSummary() } returns summary

            val result = repository.get()

            assertTrue(result.isSuccess)
            assertEquals(summary, result.getOrNull())
        }

    @Test
    fun `get wraps a 404 exception as Result failure`() =
        runTest {
            val cause = RuntimeException("404 kudos_summary not found")
            coEvery { gateway.fetchKudosSummary() } throws cause

            val result = repository.get()

            assertTrue(result.isFailure)
            assertEquals(cause, result.exceptionOrNull())
        }
}
