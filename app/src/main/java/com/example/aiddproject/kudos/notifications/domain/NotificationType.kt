package com.example.aiddproject.kudos.notifications.domain

/**
 * Seven canonical notification types. Each maps 1:1 to a Figma icon
 * (componentId 6885:8273…8313) and to a navigation destination — see
 * `NotificationsViewModel.onRowTap` for the routing branch (T035).
 *
 * Phase-0 icon audit lives in T036; until then, callers fall back to a
 * placeholder Material icon resolved from the row composable.
 */
enum class NotificationType {
    KUDOS_RECEIVED,
    HEART_RECEIVED,
    SECRET_BOX_UNLOCK,
    LEVEL_UP,
    CONTENT_HIDDEN,
    BADGE_COLLECTED,
    REVIEW_REQUEST,
}
