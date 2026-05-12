package com.example.aiddproject.kudos.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aiddproject.R
import com.example.aiddproject.core.locale.Language
import com.example.aiddproject.core.locale.LanguagePreferenceRepository
import com.example.aiddproject.kudos.data.KudosRepository
import com.example.aiddproject.kudos.domain.KudosFilter
import com.example.aiddproject.kudos.domain.SystemFlags
import com.example.aiddproject.kudos.domain.states.AllKudosState
import com.example.aiddproject.kudos.domain.states.KudosHighlightState
import com.example.aiddproject.kudos.domain.states.PersonalStatsState
import com.example.aiddproject.kudos.domain.states.SpotlightState
import com.example.aiddproject.kudos.domain.states.TopTenState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * Hilt-injected ViewModel for the Sun*Kudos hub (spec § US1).
 *
 * Phase 3 MVP scope:
 *  - init kicks off 5 parallel section fetches via [coroutineScope] +
 *    [async]/[awaitAll] (plan § Pull-to-refresh snippet, simplified to
 *    drop the [isRefreshing] gate since first load has nothing to gate).
 *  - Each section failure isolates: the section's […State.Error] is
 *    set, other sections still reach Loaded. No screen-wide error.
 *  - [onPullToRefresh] re-runs all 5 fetches under the [isRefreshing]
 *    gate so concurrent invocations (rapid pull-down spam) short-
 *    circuit cleanly.
 *  - [systemFlags] gates the heart-math `+1` vs `+2` (Q-K-1) — read
 *    once at mount + on refresh into `state.specialDayActive`.
 *
 * Future phases extend this VM with filter/heart/copy-link/profile/
 * spotlight-search/secret-box mechanics.
 */
@HiltViewModel
class KudosViewModel
    @Inject
    constructor(
        private val savedStateHandle: SavedStateHandle,
        private val repository: KudosRepository,
        languagePreferenceRepository: LanguagePreferenceRepository,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(KudosUiState.Empty)
        val uiState: StateFlow<KudosUiState> = _uiState.asStateFlow()

        private val languageState: StateFlow<Language> =
            languagePreferenceRepository.language.stateIn(
                scope = viewModelScope,
                started = SharingStarted.Eagerly,
                initialValue = Language.Default,
            )

        init {
            Timber.tag(TELEMETRY_TAG).i("kudos_hub.entered")
            viewModelScope.launch { refreshAll() }
        }

        /**
         * User-initiated refresh per Q-K-2. Gates concurrent calls via
         * [KudosUiState.isRefreshing] — a second pull-down before the
         * first completes is a no-op.
         */
        fun onPullToRefresh() {
            if (_uiState.value.isRefreshing) return
            Timber.tag(TELEMETRY_TAG).i("kudos_hub.pull_to_refresh")
            viewModelScope.launch {
                _uiState.update { it.copy(isRefreshing = true) }
                try {
                    refreshAll()
                } finally {
                    _uiState.update { it.copy(isRefreshing = false) }
                }
            }
        }

        private suspend fun refreshAll() {
            val filter = currentFilter()
            coroutineScope {
                val h = async { fetchHighlight(filter) }
                val a = async { fetchAllKudos(filter) }
                val s = async { fetchSpotlight() }
                val p = async { fetchPersonalStats() }
                val t = async { fetchTopTen() }
                val f = async { fetchSystemFlags() }
                awaitAll(h, a, s, p, t, f)
            }
            // Mirror the latest locale into state so the screen body
            // re-renders with the right strings without observing the
            // flow again.
            _uiState.update { it.copy(language = languageState.value) }
        }

        private suspend fun fetchHighlight(filter: KudosFilter) {
            repository.listHighlight(filter).fold(
                onSuccess = { items ->
                    _uiState.update {
                        it.copy(
                            highlight =
                                if (items.isEmpty()) {
                                    KudosHighlightState.Empty
                                } else {
                                    KudosHighlightState.Loaded(items)
                                },
                        )
                    }
                },
                onFailure = {
                    _uiState.update {
                        it.copy(highlight = KudosHighlightState.Error(R.string.kudos_error))
                    }
                },
            )
        }

        private suspend fun fetchAllKudos(filter: KudosFilter) {
            repository.listKudos(filter, page = 0, limit = ALL_KUDOS_PAGE_SIZE).fold(
                onSuccess = { page ->
                    _uiState.update {
                        it.copy(
                            allKudos =
                                if (page.items.isEmpty()) {
                                    AllKudosState.Empty
                                } else {
                                    AllKudosState.Loaded(
                                        items = page.items,
                                        hasMore = page.hasMore,
                                        nextPage = page.nextPage,
                                    )
                                },
                        )
                    }
                },
                onFailure = {
                    _uiState.update {
                        it.copy(allKudos = AllKudosState.Error(R.string.kudos_error))
                    }
                },
            )
        }

        private suspend fun fetchSpotlight() {
            repository.loadSpotlightGraph().fold(
                onSuccess = { graph ->
                    _uiState.update {
                        it.copy(
                            spotlight =
                                if (graph.nodes.isEmpty()) {
                                    SpotlightState.Empty
                                } else {
                                    SpotlightState.Loaded(graph)
                                },
                        )
                    }
                },
                onFailure = {
                    _uiState.update {
                        it.copy(spotlight = SpotlightState.Error(R.string.kudos_error))
                    }
                },
            )
        }

        private suspend fun fetchPersonalStats() {
            repository.personalStats().fold(
                onSuccess = { stats ->
                    _uiState.update { it.copy(stats = PersonalStatsState.Loaded(stats)) }
                },
                onFailure = {
                    _uiState.update {
                        it.copy(stats = PersonalStatsState.Error(R.string.kudos_error))
                    }
                },
            )
        }

        private suspend fun fetchTopTen() {
            repository.listRecentGiftRecipients().fold(
                onSuccess = { items ->
                    _uiState.update {
                        it.copy(
                            topTen =
                                if (items.isEmpty()) {
                                    TopTenState.Empty
                                } else {
                                    TopTenState.Loaded(items)
                                },
                        )
                    }
                },
                onFailure = {
                    _uiState.update {
                        it.copy(topTen = TopTenState.Error(R.string.kudos_error))
                    }
                },
            )
        }

        private suspend fun fetchSystemFlags() {
            repository.systemFlags().fold(
                onSuccess = { flags: SystemFlags ->
                    _uiState.update { it.copy(specialDayActive = flags.specialDayActive) }
                },
                // SystemFlags failure is non-fatal — fall back to defaults
                // (no special-day x2, no x2 fire badge) so the rest of the
                // hub still renders.
                onFailure = { /* leave defaults */ },
            )
        }

        private fun currentFilter(): KudosFilter =
            KudosFilter(
                hashtagId = _uiState.value.selectedHashtagId,
                departmentId = _uiState.value.selectedDepartmentId,
            )

        @Suppress("unused")
        private fun keepSavedStateHandle(): SavedStateHandle = savedStateHandle

        private companion object {
            const val TELEMETRY_TAG: String = "KudosTelemetry"
            const val ALL_KUDOS_PAGE_SIZE: Int = 5
        }
    }
