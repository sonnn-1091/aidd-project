@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.example.aiddproject.kudos.notifications.ui

import com.example.aiddproject.R
import com.example.aiddproject.kudos.notifications.ui.components.formatRelativeTime
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

/**
 * Covers every threshold in the FR-010 ladder + the boundary on each
 * step (59 min, 60 min, 23 hr, 24 hr, 29 d, 30 d, 11 m, 12 m). The fake
 * `quantityString` lambda returns a deterministic "{resId}:{count}"
 * format so the test asserts on the bucket + count without depending
 * on Android Resources.
 */
class RelativeTimeFormatterTest {
    private val now: Instant = Instant.fromEpochMilliseconds(1_700_000_000_000L)
    private val fakeQuantityString: (Int, Int) -> String = { resId, count -> "$resId:$count" }

    @Test
    fun `1 minute ago picks the minutes plural`() {
        val created = now - 1.minutes
        val result = formatRelativeTime(created, now, fakeQuantityString)
        assertEquals("${R.plurals.relative_time_minutes_ago}:1", result)
    }

    @Test
    fun `0 minutes ago floors up to 1 minute`() {
        // The ladder displays "1 phút trước" rather than "0" to avoid
        // the awkward "0 phút trước" wording.
        val created = now
        val result = formatRelativeTime(created, now, fakeQuantityString)
        assertEquals("${R.plurals.relative_time_minutes_ago}:1", result)
    }

    @Test
    fun `59 minutes ago stays in minutes bucket`() {
        val created = now - 59.minutes
        val result = formatRelativeTime(created, now, fakeQuantityString)
        assertEquals("${R.plurals.relative_time_minutes_ago}:59", result)
    }

    @Test
    fun `60 minutes ago crosses into hours bucket`() {
        val created = now - 60.minutes
        val result = formatRelativeTime(created, now, fakeQuantityString)
        assertEquals("${R.plurals.relative_time_hours_ago}:1", result)
    }

    @Test
    fun `23 hours ago stays in hours bucket`() {
        val created = now - 23.hours
        val result = formatRelativeTime(created, now, fakeQuantityString)
        assertEquals("${R.plurals.relative_time_hours_ago}:23", result)
    }

    @Test
    fun `24 hours ago crosses into days bucket`() {
        val created = now - 24.hours
        val result = formatRelativeTime(created, now, fakeQuantityString)
        assertEquals("${R.plurals.relative_time_days_ago}:1", result)
    }

    @Test
    fun `29 days ago stays in days bucket`() {
        val created = now - 29.days
        val result = formatRelativeTime(created, now, fakeQuantityString)
        assertEquals("${R.plurals.relative_time_days_ago}:29", result)
    }

    @Test
    fun `30 days ago crosses into months bucket`() {
        val created = now - 30.days
        val result = formatRelativeTime(created, now, fakeQuantityString)
        assertEquals("${R.plurals.relative_time_months_ago}:1", result)
    }

    @Test
    fun `364 days ago stays in months bucket`() {
        val created = now - 364.days
        val result = formatRelativeTime(created, now, fakeQuantityString)
        // 364 / 30 = 12 → still months bucket (the cutoff is < 365 days)
        assertEquals("${R.plurals.relative_time_months_ago}:12", result)
    }

    @Test
    fun `365 days ago crosses into years bucket`() {
        val created = now - 365.days
        val result = formatRelativeTime(created, now, fakeQuantityString)
        assertEquals("${R.plurals.relative_time_years_ago}:1", result)
    }

    @Test
    fun `730 days ago renders 2 years`() {
        val created = now - 730.days
        val result = formatRelativeTime(created, now, fakeQuantityString)
        assertEquals("${R.plurals.relative_time_years_ago}:2", result)
    }
}
