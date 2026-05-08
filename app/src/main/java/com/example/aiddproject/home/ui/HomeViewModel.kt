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
            awardsState.value = AwardsState.Loading
            awardsRepository.list().fold(
                onSuccess = { items ->
                    awardsState.value =
                        if (items.isEmpty()) AwardsState.Empty else AwardsState.Populated(items)
                },
                onFailure = { error -> awardsState.value = AwardsState.Error(error.message) },
            )
        }

        private suspend fun loadKudos() {
            kudosRepository.get().fold(
                onSuccess = { summary ->
                    kudosState.value =
                        if (summary.isKudosAvailable) {
                            KudosState.Loaded(summary)
                        } else {
                            KudosState.Hidden
                        }
                },
                onFailure = { error -> kudosState.value = KudosState.Error(error.message) },
            )
        }

        private suspend fun loadNotifications() {
            notificationsRepository.get().fold(
                onSuccess = { summary ->
                    notificationsState.value = NotificationsState.Loaded(summary.unreadCount)
                },
                onFailure = { notificationsState.value = NotificationsState.Error },
            )
        }

        override fun onCleared() {
            super.onCleared()
            countdownJob = null
        }
    }
