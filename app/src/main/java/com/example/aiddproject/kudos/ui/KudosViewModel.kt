package com.example.aiddproject.kudos.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aiddproject.R
import com.example.aiddproject.core.locale.Language
import com.example.aiddproject.core.locale.LanguagePreferenceRepository
import com.example.aiddproject.kudos.data.KudosRepository
import com.example.aiddproject.kudos.domain.KudosFilter
import com.example.aiddproject.kudos.domain.SpotlightSearchResult
import com.example.aiddproject.kudos.domain.SystemFlags
import com.example.aiddproject.kudos.domain.states.AllKudosState
import com.example.aiddproject.kudos.domain.states.KudosHighlightState
import com.example.aiddproject.kudos.domain.states.PersonalStatsState
import com.example.aiddproject.kudos.domain.states.SpotlightState
import com.example.aiddproject.kudos.domain.states.TopTenState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

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
@OptIn(FlowPreview::class)
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

        /**
         * Tracks the in-flight filter-driven refetch so a rapid filter
         * change cancels the previous fetch before issuing the new one
         * (US3 Edge Case — "Network slow → fast race").
         */
        private var filterFetchJob: Job? = null

        /**
         * One-shot signal to the Highlight carousel that the filter
         * changed — the carousel collects it inside a `LaunchedEffect`
         * and resets `pagerState.currentPage = 0` (US3 scenario 3).
         * Bumped by every successful filter mutation.
         */
        private val _filterResetTick = MutableStateFlow(0)
        val filterResetTick: StateFlow<Int> = _filterResetTick.asStateFlow()

        /** Spotlight search input stream — debounced into a fetch. */
        private val spotlightSearchInput = MutableStateFlow("")

        init {
            Timber.tag(TELEMETRY_TAG).i("kudos_hub.entered")
            viewModelScope.launch { refreshAll() }
            viewModelScope.launch { loadFilters() }
            viewModelScope.launch { collectSpotlightSearch() }
        }

        /** Update the spotlight search query — fed into the debounce stream. */
        fun onSpotlightSearchChange(query: String) {
            val trimmed = if (query.length > SPOTLIGHT_SEARCH_MAX_LENGTH) query.take(SPOTLIGHT_SEARCH_MAX_LENGTH) else query
            _uiState.update { it.copy(spotlightSearchQuery = trimmed) }
            spotlightSearchInput.value = trimmed
        }

        private suspend fun collectSpotlightSearch() {
            spotlightSearchInput
                .debounce(SPOTLIGHT_SEARCH_DEBOUNCE_MS.milliseconds)
                .collectLatest { query ->
                    if (query.isBlank()) {
                        _uiState.update {
                            it.copy(spotlightSearchResult = SpotlightSearchResult.Idle)
                        }
                        return@collectLatest
                    }
                    _uiState.update {
                        it.copy(spotlightSearchResult = SpotlightSearchResult.Loading)
                    }
                    repository.searchSunner(query).fold(
                        onSuccess = { matches ->
                            val top = matches.firstOrNull()
                            val nextResult =
                                if (top != null) {
                                    SpotlightSearchResult.Match(top.node)
                                } else {
                                    SpotlightSearchResult.NoMatch
                                }
                            _uiState.update { it.copy(spotlightSearchResult = nextResult) }
                        },
                        onFailure = {
                            _uiState.update {
                                it.copy(spotlightSearchResult = SpotlightSearchResult.NoMatch)
                            }
                        },
                    )
                }
        }

        /**
         * Apply a hashtag filter (US3). Pass `null` to clear. Cancels
         * any in-flight filter fetch + bumps the carousel reset tick so
         * the pager scrolls to page 0.
         */
        fun onSelectHashtag(hashtagId: String?) {
            val current = _uiState.value.selectedHashtagId
            if (current == hashtagId) return
            Timber.tag(TELEMETRY_TAG).i("kudos_hub.select_hashtag id=%s", hashtagId)
            _uiState.update { it.copy(selectedHashtagId = hashtagId) }
            triggerFilterFetch()
        }

        /**
         * Apply a department filter (US3). AND-combined with the
         * active hashtag.
         */
        fun onSelectDepartment(departmentId: String?) {
            val current = _uiState.value.selectedDepartmentId
            if (current == departmentId) return
            Timber.tag(TELEMETRY_TAG).i("kudos_hub.select_department id=%s", departmentId)
            _uiState.update { it.copy(selectedDepartmentId = departmentId) }
            triggerFilterFetch()
        }

        /**
         * Tap a hashtag chip inside any feed card (US3 scenario 4).
         * Behaves like the bottom-sheet selection — does NOT navigate.
         */
        fun onHashtagChipTap(hashtagId: String) {
            onSelectHashtag(hashtagId)
        }

        /**
         * Surface the "Link copied" Snackbar (spec § US13). The
         * clipboard write itself happens in the Composable layer
         * (`KudosScreen`) since [ClipboardManager] requires the
         * Compose composition local — keeping it out of the VM
         * preserves Constitution II (no Android system imports in
         * `domain/`/`data/`).
         */
        fun onCopyLink(kudosId: String) {
            Timber.tag(TELEMETRY_TAG).i("kudos_hub.copy_link id=%s", kudosId)
            _uiState.update {
                it.copy(snackbar = com.example.aiddproject.kudos.domain.SnackbarMessage.LinkCopied)
            }
        }

        /** Clears the snackbar slot once the host has consumed it. */
        fun onSnackbarDismissed() {
            _uiState.update { it.copy(snackbar = null) }
        }

        /**
         * Toggle a heart with optimistic update + rollback on failure
         * (spec § US5, plan § Optimistic reaction rollback).
         *
         * - If `kudos.likeDisabledForMe` is true (Q-K-5: viewer is the
         *   sender or recipient), the tap is a silent no-op.
         * - Pre-compute the optimistic patch: heart count ±1, or ±2
         *   when [KudosUiState.specialDayActive] (Q-K-1).
         * - Apply the patch locally in both Highlight + AllKudos slices
         *   (single mutation via [applyKudosLocally]).
         * - Persist via repo.addReaction / repo.removeReaction.
         * - On failure, restore the pre-tap snapshot + raise the
         *   ReactionFailed Snackbar.
         */
        fun onHeartTap(kudosId: String) {
            val current = findKudos(kudosId) ?: return
            if (current.likeDisabledForMe) return
            val wasLiked = current.likedByCurrentUser
            val delta = if (_uiState.value.specialDayActive) 2 else 1
            val optimistic =
                current.copy(
                    likedByCurrentUser = !wasLiked,
                    heartCount = (current.heartCount + if (wasLiked) -delta else delta).coerceAtLeast(0),
                )
            applyKudosLocally(optimistic)
            viewModelScope.launch {
                val result =
                    if (wasLiked) {
                        repository.removeReaction(kudosId)
                    } else {
                        repository.addReaction(kudosId)
                    }
                if (result.isFailure) {
                    applyKudosLocally(current)
                    _uiState.update {
                        it.copy(snackbar = com.example.aiddproject.kudos.domain.SnackbarMessage.ReactionFailed)
                    }
                }
            }
        }

        private fun findKudos(kudosId: String): com.example.aiddproject.kudos.domain.Kudos? {
            val highlight = (_uiState.value.highlight as? KudosHighlightState.Loaded)?.items.orEmpty()
            val feed = (_uiState.value.allKudos as? AllKudosState.Loaded)?.items.orEmpty()
            return highlight.firstOrNull { it.id == kudosId }
                ?: feed.firstOrNull { it.id == kudosId }
        }

        /**
         * Patch the same Kudos in every section that contains it so
         * the two feeds stay in sync — single mutation point per
         * plan § Optimistic mechanic.
         */
        private fun applyKudosLocally(kudos: com.example.aiddproject.kudos.domain.Kudos) {
            _uiState.update { state ->
                val newHighlight =
                    when (val h = state.highlight) {
                        is KudosHighlightState.Loaded -> h.copy(items = h.items.replaceById(kudos))
                        else -> h
                    }
                val newAllKudos =
                    when (val a = state.allKudos) {
                        is AllKudosState.Loaded -> a.copy(items = a.items.replaceById(kudos))
                        else -> a
                    }
                state.copy(highlight = newHighlight, allKudos = newAllKudos)
            }
        }

        private fun List<com.example.aiddproject.kudos.domain.Kudos>.replaceById(
            kudos: com.example.aiddproject.kudos.domain.Kudos,
        ): List<com.example.aiddproject.kudos.domain.Kudos> = map { if (it.id == kudos.id) kudos else it }

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

        /**
         * Fetch Highlight + All Kudos for the current filter selection.
         * Cancels any in-flight filter fetch first (US3 race). Bumps
         * the carousel reset tick so the pager rewinds to page 0.
         */
        private fun triggerFilterFetch() {
            filterFetchJob?.cancel()
            filterFetchJob =
                viewModelScope.launch {
                    val filter = currentFilter()
                    _uiState.update {
                        it.copy(
                            highlight = KudosHighlightState.Loading,
                            allKudos = AllKudosState.Loading,
                        )
                    }
                    coroutineScope {
                        val h = async { fetchHighlight(filter) }
                        val a = async { fetchAllKudos(filter) }
                        awaitAll(h, a)
                    }
                    _filterResetTick.update { it + 1 }
                }
        }

        private suspend fun loadFilters() {
            val hashtags = repository.listHashtags().getOrNull().orEmpty()
            val departments = repository.listDepartments().getOrNull().orEmpty()
            _uiState.update { it.copy(hashtags = hashtags, departments = departments) }
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
            const val SPOTLIGHT_SEARCH_DEBOUNCE_MS: Long = 300
            const val SPOTLIGHT_SEARCH_MAX_LENGTH: Int = 100
        }
    }
