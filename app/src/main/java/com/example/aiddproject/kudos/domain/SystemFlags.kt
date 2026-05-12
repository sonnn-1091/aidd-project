package com.example.aiddproject.kudos.domain

/**
 * Server-toggled feature flags read at hub mount + on pull-to-refresh.
 *
 * - [specialDayActive] drives the heart math (+1 vs +2) per Q-K-1.
 * - [x2BonusActive] drives the fire badge that appears on the stats
 *   panel (spec § US10). Independent of [specialDayActive] — operators
 *   can run an x2 bonus without flipping the heart-math flag.
 */
data class SystemFlags(
    val specialDayActive: Boolean = false,
    val x2BonusActive: Boolean = false,
)
