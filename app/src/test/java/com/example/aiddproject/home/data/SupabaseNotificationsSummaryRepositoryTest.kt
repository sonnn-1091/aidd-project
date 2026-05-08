package com.example.aiddproject.home.data

import com.example.aiddproject.home.domain.NotificationsSummary
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SupabaseNotificationsSummaryRepositoryTest {
    private val gateway: SupabaseNotificationsSummaryGateway = mockk(relaxed = true)
    private val repository = SupabaseNotificationsSummaryRepository(gateway)

    @Test
    fun `get returns the gateway's unread count wrapped in Result success`() =
        runTest {
            coEvery { gateway.fetchNotificationsSummary() } returns NotificationsSummary(unreadCount = 7)

            val result = repository.get()

            assertTrue(result.isSuccess)
            assertEquals(7, result.getOrNull()?.unreadCount)
        }

    @Test
    fun `get returns zero when the gateway reports no unread - boundary path`() =
        runTest {
            coEvery { gateway.fetchNotificationsSummary() } returns NotificationsSummary(unreadCount = 0)

            val result = repository.get()

            assertTrue(result.isSuccess)
            assertEquals(0, result.getOrNull()?.unreadCount)
        }

    @Test
    fun `get wraps an RPC exception as Result failure`() =
        runTest {
            val cause = RuntimeException("rpc 500")
            coEvery { gateway.fetchNotificationsSummary() } throws cause

            val result = repository.get()

            assertTrue(result.isFailure)
            assertEquals(cause, result.exceptionOrNull())
        }
}
