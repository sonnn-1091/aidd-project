package com.example.aiddproject.kudos.notifications.data

import com.example.aiddproject.home.data.NotificationsSummaryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Process-singleton bell-badge unread counter shared across the 3
 * hosts that render the bell icon (Home, Kudos hub, AwardDetail).
 *
 * The Notifications screen mutates it (decrement on row tap, setTo(0)
 * on mark-all-read); each host VM observes it via `count` to keep the
 * badge in sync without polling the server.
 *
 * On a hard cold start, `refreshFromServer()` reseeds the value from
 * the canonical `notifications_summary()` RPC. Demo builds resolve to
 * `DemoNotificationsSummaryRepository` which returns the seed count.
 */
@Singleton
class NotificationsCountFlow
    @Inject
    constructor(
        private val summaryRepository: NotificationsSummaryRepository,
    ) {
        private val _count = MutableStateFlow(0)
        val count: StateFlow<Int> = _count.asStateFlow()

        suspend fun refreshFromServer() {
            summaryRepository.get().onSuccess { summary ->
                _count.value = summary.unreadCount
            }
        }

        fun setTo(value: Int) {
            _count.value = value.coerceAtLeast(0)
        }

        fun decrement(by: Int = 1) {
            _count.value = (_count.value - by).coerceAtLeast(0)
        }
    }
