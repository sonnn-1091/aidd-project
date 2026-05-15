@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.example.aiddproject.kudos.notifications.ui.components

import android.content.res.Resources
import com.example.aiddproject.R
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

/**
 * Pure relative-time formatter implementing the FR-010 ladder (Q-N-5):
 * - `< 60 min` → "{n} phút trước"
 * - `< 24 hr`  → "{n} giờ trước"
 * - `< 30 ngày` → "{n} ngày trước"
 * - `< 12 tháng` → "{n} tháng trước"
 * - else → "{n} năm trước"
 *
 * The function takes [now] as a parameter (rather than calling
 * `Clock.System.now()` internally) so tests can supply a deterministic
 * instant.
 *
 * Two entry points:
 *  - `formatRelativeTime(..., quantityString)` — pure, takes a lookup
 *    lambda so JVM unit tests can pass a fake without pulling Robolectric
 *    onto the test classpath.
 *  - `Resources.formatRelativeTime(...)` — the production extension that
 *    delegates to `Resources.getQuantityString` for plural-aware,
 *    locale-aware rendering.
 */
fun formatRelativeTime(
    createdAt: Instant,
    now: Instant,
    quantityString: (pluralResId: Int, count: Int) -> String,
): String {
    val elapsed = now - createdAt
    if (elapsed < 60.minutes) {
        val value = elapsed.inWholeMinutes.toInt().coerceAtLeast(1)
        return quantityString(R.plurals.relative_time_minutes_ago, value)
    }
    if (elapsed < 24.hours) {
        val value = elapsed.inWholeHours.toInt()
        return quantityString(R.plurals.relative_time_hours_ago, value)
    }
    if (elapsed < 30.days) {
        val value = elapsed.inWholeDays.toInt()
        return quantityString(R.plurals.relative_time_days_ago, value)
    }
    val totalDays = elapsed.inWholeDays
    if (totalDays < 365) {
        val value = (totalDays / 30).toInt().coerceAtLeast(1)
        return quantityString(R.plurals.relative_time_months_ago, value)
    }
    val years = (totalDays / 365).toInt().coerceAtLeast(1)
    return quantityString(R.plurals.relative_time_years_ago, years)
}

fun Resources.formatRelativeTime(
    createdAt: Instant,
    now: Instant,
): String =
    formatRelativeTime(createdAt, now) { resId, count ->
        getQuantityString(resId, count, count)
    }
