@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.example.aiddproject.home.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aiddproject.core.locale.Language
import com.example.aiddproject.core.locale.LanguagePreferenceRepository
import com.example.aiddproject.home.data.AwardsRepository
import com.example.aiddproject.home.data.KudosSummaryRepository
import com.example.aiddproject.home.data.NotificationsSummaryRepository
import com.example.aiddproject.home.domain.CountdownEngine
import com.example.aiddproject.home.domain.states.AwardsState
import com.example.aiddproject.home.domain.states.KudosState
import com.example.aiddproject.home.domain.states.NotificationsState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * Hilt-injected ViewModel for `HomeScreen`. Drives four independent section flows
 * (`awards`, `kudos`, `notifications`, `countdown`) plus the shared `language`
 * preference, exposed as a single [HomeUiState] aggregate via [uiState].
 *
 * The three data fetches are fired in parallel on construction and on [refreshAll]
 * (Q-Home-7); each section keeps its own state machine so a failure in one section
 * never blocks the others. The countdown ticker is started/stopped explicitly by the
 * screen via [startCountdown]/[stopCountdown] so it pauses on `STOPPED` (TR-004).
 *
 * The initial value of every section StateFlow is the `Loading` variant — that way
 * the very first paint of the screen renders the loading skeleton synchronously
 * (TR-003), with no `Idle`/`Initial` predecessor.
 */
@HiltViewModel
class HomeViewModel
    @Inject
    constructor(
        private val awardsRepository: AwardsRepository,
        private val kudosRepository: KudosSummaryRepository,
        private val notificationsRepository: NotificationsSummaryRepository,
        private val countdownEngine: CountdownEngine,
        languageRepository: LanguagePreferenceRepository,
    ) : ViewModel() {
        private val awardsState = MutableStateFlow<AwardsState>(AwardsState.Loading)
        private val kudosState = MutableStateFlow<KudosState>(KudosState.Loading)
        private val notificationsState = MutableStateFlow<NotificationsState>(NotificationsState.Loading)
        private val countdownState = MutableStateFlow(countdownEngine.snapshot())
        private val languageState: StateFlow<Language> =
            languageRepository.language.stateIn(
                scope = viewModelScope,
                started = SharingStarted.Eagerly,
                initialValue = Language.Default,
            )

        val uiState: StateFlow<HomeUiState> =
            combine(
                countdownState,
                awardsState,
                kudosState,
                notificationsState,
                languageState,
            ) { countdown, awards, kudos, notifications, language ->
                HomeUiState(countdown, awards, kudos, notifications, language)
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.Eagerly,
                initialValue =
                    HomeUiState(
                        countdown = countdownState.value,
                        awards = AwardsState.Loading,
                        kudos = KudosState.Loading,
                        notifications = NotificationsState.Loading,
                        language = Language.Default,
                    ),
            )

        private var countdownJob: Job? = null

        init {
            refreshAll()
        }

        /**
         * Re-fires all three section fetches in parallel. Called on construction, on
         * Home `STARTED` re-entry, and (in later phases) on Retry / sheet dismissal.
         */
        fun refreshAll() {
            viewModelScope.launch { loadAwards() }
            viewModelScope.launch { loadKudos() }
            viewModelScope.launch { loadNotifications() }
        }

        /**
         * US2 Retry intent — re-fires only the awards fetch. Each section's state
         * machine is independent (Q-Home-7), so a transient awards failure recovers
         * without re-triggering kudos / notifications calls.
         */
        fun onRetryAwards() {
            // Telemetry breadcrumb (T099). Real telemetry SDK choice carries over
            // from Login Phase 7; until that lands, Timber is the conduit and the
            // SecureTimberTree scrubber covers PII keys (TR-007).
            Timber.tag(TELEMETRY_TAG).i("home.awards.retry")
            viewModelScope.launch { loadAwards() }
        }

        /**
         * US6 — re-fires only the notifications-summary fetch when the
         * Notifications sheet is dismissed, so the badge reflects newly-read
         * notifications without forcing a full Home refresh (Q-Home-6).
         */
        fun onNotificationsSheetDismissed() {
            Timber.tag(TELEMETRY_TAG).i("home.notifications.sheet_dismissed")
            viewModelScope.launch { loadNotifications() }
        }

        /**
         * Starts the 1Hz countdown ticker. No-op if already running. The screen calls
         * this from `LifecycleStartEffect` and pairs it with [stopCountdown] so the
         * coroutine doesn't run while Home is off-screen (TR-004).
         */
        fun startCountdown() {
            if (countdownJob?.isActive == true) return
            countdownJob =
                viewModelScope.launch {
                    countdownEngine.ticks().collect { state -> countdownState.value = state }
                }
        }

        fun stopCountdown() {
            countdownJob?.cancel()
            countdownJob = null
        }

        private suspend fun loadAwards() {
            Timber.tag(TELEMETRY_TAG).i("home.awards.loading")
            awardsState.value = AwardsState.Loading
            awardsRepository.list().fold(
                onSuccess = { items ->
                    awardsState.value =
                        if (items.isEmpty()) AwardsState.Empty else AwardsState.Populated(items)
                    // SecureTimberTree scrubs `award.name` / `award.description` keys
                    // before they hit Logcat — we only emit the count for telemetry.
                    Timber.tag(TELEMETRY_TAG).i("home.awards.success count=%d", items.size)
                },
                onFailure = { error ->
                    awardsState.value = AwardsState.Error(error.message)
                    Timber.tag(TELEMETRY_TAG).w(error, "home.awards.error")
                },
            )
        }

        private suspend fun loadKudos() {
            Timber.tag(TELEMETRY_TAG).i("home.kudos.loading")
            kudosRepository.get().fold(
                onSuccess = { summary ->
                    kudosState.value =
                        if (summary.isKudosAvailable) {
                            KudosState.Loaded(summary)
                        } else {
                            KudosState.Hidden
                        }
                    Timber.tag(TELEMETRY_TAG).i(
                        "home.kudos.success isKudosAvailable=%b",
                        summary.isKudosAvailable,
                    )
                },
                onFailure = { error ->
                    kudosState.value = KudosState.Error(error.message)
                    Timber.tag(TELEMETRY_TAG).w(error, "home.kudos.error")
                },
            )
        }

        private suspend fun loadNotifications() {
            Timber.tag(TELEMETRY_TAG).i("home.notifications.loading")
            notificationsRepository.get().fold(
                onSuccess = { summary ->
                    notificationsState.value = NotificationsState.Loaded(summary.unreadCount)
                    Timber.tag(TELEMETRY_TAG).i(
                        "home.notifications.success unreadCount=%d",
                        summary.unreadCount,
                    )
                },
                onFailure = { error ->
                    notificationsState.value = NotificationsState.Error
                    Timber.tag(TELEMETRY_TAG).w(error, "home.notifications.error")
                },
            )
        }

        override fun onCleared() {
            super.onCleared()
            countdownJob = null
        }

        private companion object {
            // Tag for the four-state-transitions-per-section breadcrumbs (T099).
            // The real telemetry SDK choice is still pending Login Phase 7
            // carry-over; until that lands, Timber is the conduit and the
            // SecureTimberTree scrubber covers any PII keys we accidentally pass.
            const val TELEMETRY_TAG = "HomeTelemetry"
        }
    }
