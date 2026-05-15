package com.example.aiddproject.kudos.notifications.ui

import androidx.annotation.StringRes
import com.example.aiddproject.kudos.notifications.domain.NotificationItem

/**
 * Top-level UI state for the Notifications screen.
 *
 *  - [listState] holds the per-state branch (Loading / Empty / Error / Loaded).
 *  - [unreadCount] mirrors `NotificationsCountFlow.count` so the
 *    mark-all-read button can early-return (Q-N-10).
 *  - [snackbar] surfaces transient error messages — currently used by
 *    the read-all-rollback path (T033).
 */
data class NotificationsUiState(
    val listState: NotificationsListState = NotificationsListState.Loading,
    val unreadCount: Int = 0,
    val snackbar: SnackbarMessage? = null,
)

sealed interface NotificationsListState {
    data object Loading : NotificationsListState

    data class Loaded(
        val items: List<NotificationItem>,
        val hasMore: Boolean = false,
    ) : NotificationsListState

    data object Empty : NotificationsListState

    data class Error(@StringRes val messageRes: Int) : NotificationsListState
}

data class SnackbarMessage(@StringRes val messageRes: Int)
