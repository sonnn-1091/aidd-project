package com.example.aiddproject.kudos.notifications.data

import app.cash.turbine.test
import com.example.aiddproject.home.data.NotificationsSummaryRepository
import com.example.aiddproject.home.domain.NotificationsSummary
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Behavioral guards for the process-wide notifications counter that
 * keeps the bell badge in sync across Home / Kudos / AwardDetail.
 *
 *  - `setTo(value)` is the authoritative setter (used by the
 *    `markAllRead` optimistic path); `decrement(by)` powers the
 *    per-row optimistic mark-read path.
 *  - `refreshFromServer()` calls the existing summary repository
 *    contract — failure leaves the count untouched (offline tolerance).
 */
class NotificationsCountFlowTest {
    private val stubSummaryRepository =
        object : NotificationsSummaryRepository {
            var nextResult: Result<NotificationsSummary> = Result.success(NotificationsSummary(unreadCount = 5))
            var callCount: Int = 0

            override suspend fun get(): Result<NotificationsSummary> {
                callCount += 1
                return nextResult
            }
        }

    private val flow = NotificationsCountFlow(stubSummaryRepository)

    @Test
    fun `setTo updates emission`() =
        runTest {
            flow.count.test {
                assertEquals(0, awaitItem())
                flow.setTo(4)
                assertEquals(4, awaitItem())
                cancelAndConsumeRemainingEvents()
            }
        }

    @Test
    fun `decrement subtracts and clamps at zero`() =
        runTest {
            flow.setTo(3)
            flow.count.test {
                assertEquals(3, awaitItem())
                flow.decrement()
                assertEquals(2, awaitItem())
                flow.decrement(by = 5)
                assertEquals(0, awaitItem())
                cancelAndConsumeRemainingEvents()
            }
        }

    @Test
    fun `setTo clamps negatives to zero`() =
        runTest {
            flow.setTo(-3)
            assertEquals(0, flow.count.value)
        }

    @Test
    fun `refreshFromServer pushes the summary repository value`() =
        runTest {
            stubSummaryRepository.nextResult = Result.success(NotificationsSummary(unreadCount = 7))
            flow.refreshFromServer()
            assertEquals(7, flow.count.value)
            assertEquals(1, stubSummaryRepository.callCount)
        }

    @Test
    fun `refreshFromServer failure leaves the count untouched`() =
        runTest {
            flow.setTo(2)
            stubSummaryRepository.nextResult = Result.failure(RuntimeException("network down"))
            flow.refreshFromServer()
            assertEquals(2, flow.count.value)
        }
}
