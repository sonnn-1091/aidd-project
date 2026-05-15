@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.example.aiddproject.kudos.notifications.data

import com.example.aiddproject.kudos.notifications.domain.NotificationType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Behavioral guards for [DemoNotificationRepository] — every UI test
 * that mounts the Notifications screen in demo mode depends on this
 * seed shape, so the test pins:
 *  - one row per NotificationType (the row composable's icon-mapping
 *    audit needs all 7 to be exercised)
 *  - `markRead` flips a single row and emits a new list
 *  - `markAllRead` returns the count of rows that flipped and emits
 *    a fully-read list
 *  - `loadMore` returns an empty page (demo holds 7 in-memory rows; no
 *    pagination needed).
 */
class DemoNotificationRepositoryTest {
    private val repository = DemoNotificationRepository()

    @Test
    fun `observeRecent emits seed with one of every notification type`() =
        runTest {
            val first = repository.observeRecent().first()
            assertEquals(7, first.size)
            assertEquals(
                NotificationType.entries.toSet(),
                first.map { it.type }.toSet(),
            )
        }

    @Test
    fun `seed has at least one unread row`() =
        runTest {
            val first = repository.observeRecent().first()
            assertTrue(first.any { !it.isRead })
        }

    @Test
    fun `markRead flips a single row from unread to read`() =
        runTest {
            val initial = repository.observeRecent().first()
            val unreadId = initial.first { !it.isRead }.id

            val result = repository.markRead(unreadId)
            assertTrue(result.isSuccess)

            val updated = repository.observeRecent().first()
            assertTrue(updated.first { it.id == unreadId }.isRead)
        }

    @Test
    fun `markAllRead returns the count of rows that flipped and zeros remaining`() =
        runTest {
            val initial = repository.observeRecent().first()
            val initialUnreadCount = initial.count { !it.isRead }

            val result = repository.markAllRead()
            assertTrue(result.isSuccess)
            assertEquals(initialUnreadCount, result.getOrNull())

            val updated = repository.observeRecent().first()
            assertEquals(0, updated.count { !it.isRead })
        }

    @Test
    fun `markAllRead on a fully-read list returns zero and is idempotent`() =
        runTest {
            repository.markAllRead()
            val second = repository.markAllRead()
            assertEquals(0, second.getOrNull())
        }

    @Test
    fun `loadMore returns empty page since demo seed fits in one screen`() =
        runTest {
            val page = repository.loadMore(cursor = null).getOrThrow()
            assertTrue(page.items.isEmpty())
            assertEquals(null, page.nextCursor)
        }
}
