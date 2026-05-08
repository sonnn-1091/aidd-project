package com.example.aiddproject.home.data

import com.example.aiddproject.home.domain.NotificationsSummary

/** Bell-badge unread count via the `notifications_summary()` RPC. */
interface NotificationsSummaryRepository {
    suspend fun get(): Result<NotificationsSummary>
}
