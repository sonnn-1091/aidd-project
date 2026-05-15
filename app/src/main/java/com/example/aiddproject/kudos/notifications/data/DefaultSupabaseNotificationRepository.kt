package com.example.aiddproject.kudos.notifications.data

import com.example.aiddproject.kudos.notifications.domain.NotificationItem
import io.github.jan.supabase.SupabaseClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Production skeleton. The 4 Postgrest endpoints (list / mark-read /
 * mark-all / unread-count) and the `notifications` table schema are
 * owned by the backend team — until they land, every method throws.
 * `BuildConfig.DEMO_MODE` keeps this class out of demo builds; non-demo
 * builds are blocked from the notifications feature until backend ships.
 */
class DefaultSupabaseNotificationRepository(
    @Suppress("unused") private val supabaseClient: SupabaseClient,
) : NotificationRepository {
    override fun observeRecent(): Flow<List<NotificationItem>> =
        flow { throw NotImplementedError(BACKEND_PENDING) }

    override suspend fun loadMore(cursor: String?): Result<NotificationRepository.Page> =
        throw NotImplementedError(BACKEND_PENDING)

    override suspend fun markRead(id: String): Result<Unit> = throw NotImplementedError(BACKEND_PENDING)

    override suspend fun markAllRead(): Result<Int> = throw NotImplementedError(BACKEND_PENDING)

    private companion object {
        const val BACKEND_PENDING: String =
            "Notifications backend not yet shipped — production builds should not reach this class."
    }
}
