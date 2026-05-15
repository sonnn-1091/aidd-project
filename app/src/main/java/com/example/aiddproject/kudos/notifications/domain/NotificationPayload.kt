package com.example.aiddproject.kudos.notifications.domain

/**
 * Type-specific payload carrying navigation arguments for a notification.
 * Sealed so the row-tap routing branch in `NotificationsViewModel` exhausts
 * every variant at compile time (Q-N-3 mapping).
 *
 * - [KudoRef] — KUDOS_RECEIVED, HEART_RECEIVED, CONTENT_HIDDEN (kudoId + anon flag for the recipient screen)
 * - [SecretBox] — SECRET_BOX_UNLOCK (no args; routes to the unlock screen)
 * - [Profile] — LEVEL_UP, BADGE_COLLECTED (no args; routes to the user's own profile)
 * - [Review] — REVIEW_REQUEST (routes to admin review with a pending count badge)
 */
sealed interface NotificationPayload {
    data class KudoRef(val kudoId: String, val isAnonymous: Boolean) : NotificationPayload

    data object SecretBox : NotificationPayload

    data object Profile : NotificationPayload

    data class Review(val reviewCount: Int) : NotificationPayload
}
