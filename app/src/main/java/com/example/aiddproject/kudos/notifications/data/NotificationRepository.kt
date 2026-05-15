package com.example.aiddproject.kudos.notifications.data

import com.example.aiddproject.kudos.notifications.domain.NotificationItem
import kotlinx.coroutines.flow.Flow

/**
 * Notifications data port. Demo and Supabase implementations both
 * conform to this single contract so the UI layer never branches on
 * `BuildConfig.DEMO_MODE`.
 *
 * `observeRecent()` is the canonical source — emits the cached
 * newest-first slice whenever it changes (after `markRead`, `markAllRead`,
 * pagination, or push). `loadMore()` appends an older page via cursor.
 */
interface NotificationRepository {
    fun observeRecent(): Flow<List<NotificationItem>>

    suspend fun loadMore(cursor: String?): Result<Page>

    suspend fun markRead(id: String): Result<Unit>

    /** Returns the count of rows that were transitioned unread → read. */
    suspend fun markAllRead(): Result<Int>

    data class Page(val items: List<NotificationItem>, val nextCursor: String?)
}
