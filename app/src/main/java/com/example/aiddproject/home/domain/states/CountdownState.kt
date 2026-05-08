package com.example.aiddproject.home.domain.states

/**
 * Countdown display state — minute-precision toward `2025-12-26T00:00:00+07:00`
 * (Q-Home-1). Recomputed every 1s by the future `CountdownEngine`; this UI-pass
 * scaffold uses a static instance.
 */
data class CountdownState(
    val days: Int,
    val hours: Int,
    val minutes: Int,
    val isPreEvent: Boolean,
)
