package com.example.aiddproject.home.data

import com.example.aiddproject.home.domain.NotificationsSummary

/** Demo build fixture: 2 unread notifications matching the seed file. */
class DemoNotificationsSummaryRepository : NotificationsSummaryRepository {
    override suspend fun get(): Result<NotificationsSummary> = Result.success(NotificationsSummary(unreadCount = 2))
}
