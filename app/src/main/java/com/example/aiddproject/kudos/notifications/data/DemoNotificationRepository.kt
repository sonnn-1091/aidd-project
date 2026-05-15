@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.example.aiddproject.kudos.notifications.data

import com.example.aiddproject.kudos.notifications.domain.NotificationItem
import com.example.aiddproject.kudos.notifications.domain.NotificationPayload
import com.example.aiddproject.kudos.notifications.domain.NotificationType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

/**
 * In-memory demo repository seeded with one notification of each
 * [NotificationType], spanning the FR-010 relative-time ladder
 * (minutes → years) so the formatter is visibly exercised in demo
 * builds. `installDemoDebug` swaps this in via `BuildConfig.DEMO_MODE`
 * — production code never sees this class.
 */
class DemoNotificationRepository(
    clock: Clock = Clock.System,
) : NotificationRepository {
    private val now = clock.now()

    private val seed: List<NotificationItem> =
        listOf(
            NotificationItem(
                id = "n1",
                type = NotificationType.KUDOS_RECEIVED,
                isRead = false,
                createdAt = now - 12.minutes,
                payload = NotificationPayload.KudoRef(kudoId = "kudo-demo-1", isAnonymous = false),
                displayBody = "Bạn vừa nhận Kudo từ Hồng Nhung",
            ),
            NotificationItem(
                id = "n2",
                type = NotificationType.HEART_RECEIVED,
                isRead = false,
                createdAt = now - 3.hours,
                payload = NotificationPayload.KudoRef(kudoId = "kudo-demo-2", isAnonymous = true),
                displayBody = "Một Sunner ẩn danh đã thả tim cho Kudo của bạn",
            ),
            NotificationItem(
                id = "n3",
                type = NotificationType.SECRET_BOX_UNLOCK,
                isRead = true,
                createdAt = now - 1.days,
                payload = NotificationPayload.SecretBox,
                displayBody = "Bạn vừa mở khoá một Hộp bí mật",
            ),
            NotificationItem(
                id = "n4",
                type = NotificationType.LEVEL_UP,
                isRead = false,
                createdAt = now - 5.days,
                payload = NotificationPayload.Profile,
                displayBody = "Chúc mừng! Bạn đã thăng cấp.",
            ),
            NotificationItem(
                id = "n5",
                type = NotificationType.CONTENT_HIDDEN,
                isRead = false,
                createdAt = now - 20.days,
                payload = NotificationPayload.KudoRef(kudoId = "kudo-demo-5", isAnonymous = false),
                displayBody = "Một bài viết của bạn đã bị ẩn vì vi phạm",
            ),
            NotificationItem(
                id = "n6",
                type = NotificationType.BADGE_COLLECTED,
                isRead = true,
                createdAt = now - 90.days,
                payload = NotificationPayload.Profile,
                displayBody = "Bạn vừa nhận được huy hiệu mới",
            ),
            NotificationItem(
                id = "n7",
                type = NotificationType.REVIEW_REQUEST,
                isRead = false,
                createdAt = now - 500.days,
                payload = NotificationPayload.Review(reviewCount = 3),
                displayBody = "Có 3 nội dung đang chờ bạn duyệt",
            ),
        )

    private val state: MutableStateFlow<List<NotificationItem>> = MutableStateFlow(seed)

    override fun observeRecent(): Flow<List<NotificationItem>> = state.asStateFlow()

    override suspend fun loadMore(cursor: String?): Result<NotificationRepository.Page> =
        Result.success(NotificationRepository.Page(items = emptyList(), nextCursor = null))

    override suspend fun markRead(id: String): Result<Unit> {
        state.update { current ->
            current.map { if (it.id == id) it.copy(isRead = true) else it }
        }
        return Result.success(Unit)
    }

    override suspend fun markAllRead(): Result<Int> {
        var flipped = 0
        state.update { current ->
            current.map { item ->
                if (!item.isRead) {
                    flipped += 1
                    item.copy(isRead = true)
                } else {
                    item
                }
            }
        }
        return Result.success(flipped)
    }
}
