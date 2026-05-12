package com.example.aiddproject.kudos.domain

/**
 * Per-user counter tiles in the bottom stats panel (spec § US10).
 *
 * Read from `user_stats` view (or table) keyed by current user id;
 * RLS scopes the row to the requesting Sunner only.
 */
data class PersonalStats(
    val kudosReceived: Int,
    val kudosSent: Int,
    val heartsReceived: Int,
    val secretBoxesOpened: Int,
    val secretBoxesUnopened: Int,
)
