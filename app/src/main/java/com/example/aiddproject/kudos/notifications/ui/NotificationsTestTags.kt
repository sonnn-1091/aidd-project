package com.example.aiddproject.kudos.notifications.ui

/**
 * Compose testTags for the Notifications screen — referenced by both
 * `NotificationsScreenTest` (instrumented, T039) and any future preview
 * tests. Keep names stable; the test asserts on these literals.
 */
object NotificationsTestTags {
    const val SCREEN: String = "notifications_screen"
    const val BACK_BUTTON: String = "notifications_back_button"
    const val MARK_ALL_READ_BUTTON: String = "notifications_mark_all_read_button"
    const val LIST: String = "notifications_list"
    const val EMPTY_PLACEHOLDER: String = "notifications_empty_placeholder"
    const val ERROR_RETRY: String = "notifications_error_retry"

    fun rowTag(notificationId: String): String = "notifications_row_$notificationId"

    fun unreadDotTag(notificationId: String): String = "notifications_unread_dot_$notificationId"

    fun inlineCommunityStandardsTag(notificationId: String): String =
        "notifications_inline_cs_$notificationId"
}
