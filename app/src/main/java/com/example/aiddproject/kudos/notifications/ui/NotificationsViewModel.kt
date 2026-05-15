@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.example.aiddproject.kudos.notifications.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aiddproject.R
import com.example.aiddproject.kudos.notifications.data.NotificationRepository
import com.example.aiddproject.kudos.notifications.data.NotificationsCountFlow
import com.example.aiddproject.kudos.notifications.domain.NotificationItem
import com.example.aiddproject.kudos.notifications.domain.NotificationPayload
import com.example.aiddproject.kudos.notifications.domain.NotificationType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Notifications screen.
 *
 * On `init`:
 *  - Refreshes the bell-badge count from the existing summary RPC so
 *    the bell badge across Home/Kudos/AwardDetail is consistent on
 *    cold start.
 *  - Collects `repo.observeRecent()` into [state].listState.
 *
 * Intents:
 *  - [onRowTap]: optimistic local mutation (`isRead = true`), fire
 *    `repo.markRead`, decrement the bell counter, route via [router].
 *  - [onReadAll]: snapshot → set everything read locally → fire repo →
 *    on failure restore snapshot + emit Snackbar (T031).
 *  - [onRefresh]: re-pulls the seed (collector continues independently).
 *  - [onLoadMore]: cursor-based pagination — demo repo returns empty.
 *  - [onConsumeSnackbar]: clears the transient snackbar after display.
 *
 * Routing decisions for [onRowTap] are factored into [NotificationRouter]
 * so the VM stays test-friendly — the screen instantiates a router that
 * wraps the nav lambdas.
 */
@HiltViewModel
class NotificationsViewModel
    @Inject
    constructor(
        private val repository: NotificationRepository,
        private val countFlow: NotificationsCountFlow,
    ) : ViewModel() {
        private val _state = MutableStateFlow(NotificationsUiState())
        val state: StateFlow<NotificationsUiState> = _state.asStateFlow()

        init {
            viewModelScope.launch { countFlow.refreshFromServer() }
            viewModelScope.launch {
                countFlow.count.collect { count ->
                    _state.update { it.copy(unreadCount = count) }
                }
            }
            viewModelScope.launch {
                runCatching {
                    repository.observeRecent().collect { items ->
                        _state.update { current ->
                            current.copy(
                                listState =
                                    if (items.isEmpty()) {
                                        NotificationsListState.Empty
                                    } else {
                                        NotificationsListState.Loaded(items)
                                    },
                            )
                        }
                    }
                }.onFailure {
                    _state.update {
                        it.copy(listState = NotificationsListState.Error(R.string.notifications_error))
                    }
                }
            }
        }

        fun onRowTap(
            item: NotificationItem,
            router: NotificationRouter,
        ) {
            if (!item.isRead) {
                mutateRow(item.id) { it.copy(isRead = true) }
                countFlow.decrement()
                viewModelScope.launch { repository.markRead(item.id) }
            }
            router.route(item)
        }

        fun onReadAll() {
            // Q-N-10: empty-unread → no-op + no API call.
            if (_state.value.unreadCount == 0) return

            val snapshot = (_state.value.listState as? NotificationsListState.Loaded)?.items
            val previousCount = _state.value.unreadCount

            // Optimistic local mutation
            snapshot?.let {
                _state.update { current ->
                    current.copy(
                        listState = NotificationsListState.Loaded(it.map { row -> row.copy(isRead = true) }),
                    )
                }
            }
            countFlow.setTo(0)

            viewModelScope.launch {
                repository.markAllRead().onFailure {
                    // Rollback
                    snapshot?.let {
                        _state.update { current -> current.copy(listState = NotificationsListState.Loaded(it)) }
                    }
                    countFlow.setTo(previousCount)
                    _state.update { it.copy(snackbar = SnackbarMessage(R.string.notifications_error)) }
                }
            }
        }

        fun onRefresh() {
            // The flow collector remains active; we just trigger a server
            // refresh of the bell count. For the demo repo this is a no-op
            // but keeps the contract aligned with the production path.
            viewModelScope.launch { countFlow.refreshFromServer() }
        }

        fun onLoadMore() {
            val current = _state.value.listState as? NotificationsListState.Loaded ?: return
            if (!current.hasMore) return
            viewModelScope.launch {
                repository.loadMore(cursor = null).onSuccess { page ->
                    if (page.items.isEmpty()) return@onSuccess
                    _state.update {
                        it.copy(
                            listState =
                                NotificationsListState.Loaded(
                                    items = current.items + page.items,
                                    hasMore = page.nextCursor != null,
                                ),
                        )
                    }
                }
            }
        }

        fun onConsumeSnackbar() {
            _state.update { it.copy(snackbar = null) }
        }

        private fun mutateRow(
            id: String,
            transform: (NotificationItem) -> NotificationItem,
        ) {
            val loaded = _state.value.listState as? NotificationsListState.Loaded ?: return
            _state.update { current ->
                current.copy(
                    listState =
                        NotificationsListState.Loaded(
                            loaded.items.map { if (it.id == id) transform(it) else it },
                            hasMore = loaded.hasMore,
                        ),
                )
            }
        }
    }

/**
 * Routing fan-out for row-tap. Lives outside the VM so each nav lambda
 * can be captured cleanly from `NotificationsScreen` without leaking
 * NavController into the test classpath.
 */
class NotificationRouter(
    val onNavigateToKudoDetail: (kudoId: String, isAnonymous: Boolean) -> Unit,
    val onNavigateToSecretBoxOpen: () -> Unit,
    val onNavigateToProfile: () -> Unit,
    val onNavigateToAdminReview: () -> Unit,
) {
    fun route(item: NotificationItem) {
        when (item.type) {
            NotificationType.KUDOS_RECEIVED,
            NotificationType.HEART_RECEIVED,
            NotificationType.CONTENT_HIDDEN,
            -> {
                val payload = item.payload as? NotificationPayload.KudoRef ?: return
                onNavigateToKudoDetail(payload.kudoId, payload.isAnonymous)
            }
            NotificationType.SECRET_BOX_UNLOCK -> onNavigateToSecretBoxOpen()
            NotificationType.LEVEL_UP,
            NotificationType.BADGE_COLLECTED,
            -> onNavigateToProfile()
            NotificationType.REVIEW_REQUEST -> onNavigateToAdminReview()
        }
    }
}
