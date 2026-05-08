package com.example.aiddproject.home.domain.states

/**
 * Bell badge state (FR-010). Loaded(0) → no badge; Loaded(>0) → red dot;
 * Error → no badge but bell still tappable (spec edge case).
 */
sealed interface NotificationsState {
    data object Loading : NotificationsState

    data class Loaded(
        val unreadCount: Int,
    ) : NotificationsState

    data object Error : NotificationsState
}
