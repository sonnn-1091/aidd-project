package com.example.aiddproject.home.ui

import com.example.aiddproject.core.locale.Language
import com.example.aiddproject.home.domain.states.AwardsState
import com.example.aiddproject.home.domain.states.CountdownState
import com.example.aiddproject.home.domain.states.KudosState
import com.example.aiddproject.home.domain.states.NotificationsState

/**
 * Aggregate UI state for `HomeScreen`. Each section drives an independent state
 * machine (Q-Home-7 — failure in one slot does not block the others), but the
 * stateless `HomeScreenContent` consumes them in a single bundle so previews and
 * UI tests can drive every combination without DI or a real ViewModel.
 *
 * The `awards` slot starts at [AwardsState.Loading] at construction so the first
 * paint of the carousel is the loading skeleton (TR-003 first-paint guarantee).
 */
data class HomeUiState(
    val countdown: CountdownState,
    val awards: AwardsState,
    val kudos: KudosState,
    val notifications: NotificationsState,
    val language: Language,
) {
    val unreadCount: Int
        get() = (notifications as? NotificationsState.Loaded)?.unreadCount ?: 0
}
