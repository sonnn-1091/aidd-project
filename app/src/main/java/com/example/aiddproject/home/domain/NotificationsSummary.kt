package com.example.aiddproject.home.domain

/**
 * Bell-badge projection from `notifications_summary()` RPC. The unread count is
 * authoritative — RLS in the RPC enforces `user_id = auth.uid()` so the value reflects
 * only the caller's own unread notifications.
 */
data class NotificationsSummary(
    val unreadCount: Int,
)
