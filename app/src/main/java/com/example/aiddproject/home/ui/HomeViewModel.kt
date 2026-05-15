@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.example.aiddproject.home.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aiddproject.core.locale.Language
import com.example.aiddproject.core.locale.LanguagePreferenceRepository
import com.example.aiddproject.home.data.AwardsRepository
import com.example.aiddproject.home.data.KudosSummaryRepository
import com.example.aiddproject.home.domain.CountdownEngine
import com.example.aiddproject.home.domain.states.AwardsState
import com.example.aiddproject.home.domain.states.KudosState
import com.example.aiddproject.kudos.notifications.data.NotificationsCountFlow
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
 * Hilt-injected ViewModel for `HomeScreen`. Drives three independent section flows
 * (`awards`, `kudos`, `countdown`) plus the shared `language` preference and the
 * process-singleton `NotificationsCountFlow`, exposed as a single [HomeUiState]
 * aggregate via [uiState].
 *
 * The two repository fetches are fired in parallel on construction and on
 * [refreshAll] (Q-Home-7); each section keeps its own state machine so a failure
 * in one section never blocks the others. The countdown ticker is started/stopped
 * explicitly by the screen via [startCountdown]/[stopCountdown] so it pauses on
 * `STOPPED` (TR-004).
 *
 * The notifications unread count is no longer a per-host fetch: the
 * Notifications screen migration (spec `_b68CBWKl5`) replaced the bell-tap sheet
 * with a dedicated route, and the unread count is owned by a process-singleton
 * `NotificationsCountFlow` shared across Home/Kudos/AwardDetail. HomeViewModel
 * only triggers a one-shot `refreshFromServer()` on construction so a cold-start
 * lands on the canonical server value.
 */
@HiltViewModel
class HomeViewModel
    @Inject
    constructor(
        private val awardsRepository: AwardsRepository,
        private val kudosRepository: KudosSummaryRepository,
        private val countdownEngine: CountdownEngine,
        private val notificationsCountFlow: NotificationsCountFlow,
        languageRepository: LanguagePreferenceRepository,
    ) : ViewModel() {
        private val awardsState = MutableStateFlow<AwardsState>(AwardsState.Loading)
        private val kudosState = MutableStateFlow<KudosState>(KudosState.Loading)
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
                languageState,
                notificationsCountFlow.count,
            ) { countdown, awards, kudos, language, unread ->
                HomeUiState(countdown, awards, kudos, language, unread)
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.Eagerly,
                initialValue =
                    HomeUiState(
                        countdown = countdownState.value,
                        awards = AwardsState.Loading,
                        kudos = KudosState.Loading,
                        language = Language.Default,
                        unreadCount = notificationsCountFlow.count.value,
                    ),
            )

        private var countdownJob: Job? = null

        init {
            refreshAll()
        }

        /**
         * Re-fires section fetches in parallel + refreshes the bell counter
         * from the server. Called on construction and on Home `STARTED` re-entry.
         */
        fun refreshAll() {
            viewModelScope.launch { loadAwards() }
            viewModelScope.launch { loadKudos() }
            viewModelScope.launch { notificationsCountFlow.refreshFromServer() }
        }

        /**
         * US2 Retry intent — re-fires only the awards fetch. Each section's state
         * machine is independent (Q-Home-7), so a transient awards failure recovers
         * without re-triggering kudos calls.
         */
        fun onRetryAwards() {
            Timber.tag(TELEMETRY_TAG).i("home.awards.retry")
            viewModelScope.launch { loadAwards() }
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

        override fun onCleared() {
            super.onCleared()
            countdownJob = null
        }

        private companion object {
            const val TELEMETRY_TAG = "HomeTelemetry"
        }
    }
