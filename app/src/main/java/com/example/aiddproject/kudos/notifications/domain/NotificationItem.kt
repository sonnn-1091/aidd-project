@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.example.aiddproject.kudos.notifications.domain

import kotlin.time.Instant

/**
 * Domain projection of a single notification row.
 *
 * `displayBody` is the pre-resolved Vietnamese body string (spec FR-005)
 * — admin authors it server-side so the client doesn't reinvent copy.
 * `createdAt` feeds the relative-time formatter (FR-010 ladder).
 */
data class NotificationItem(
    val id: String,
    val type: NotificationType,
    val isRead: Boolean,
    val createdAt: Instant,
    val payload: NotificationPayload,
    val displayBody: String,
)
