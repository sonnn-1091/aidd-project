@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.example.aiddproject.home.domain

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlin.time.Instant

/**
 * Single source of truth for the SAA 2025 countdown target time.
 *
 * Per Q-Home-1: 2025-12-26 00:00:00 in Asia/Ho_Chi_Minh (UTC+7), the moment the awards
 * ceremony starts. The Instant is computed once and shared by [CountdownEngine] and any
 * UI that needs to render the target.
 */
val SaaCountdownTarget: Instant =
    LocalDateTime(2025, 12, 26, 0, 0).toInstant(TimeZone.of("Asia/Ho_Chi_Minh"))
