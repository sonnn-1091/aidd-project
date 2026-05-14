package com.example.aiddproject.kudos.search.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aiddproject.kudos.data.KudosRepository
import com.example.aiddproject.kudos.domain.SunnerNode
import com.example.aiddproject.kudos.search.data.RecentSunnerRepository
import com.example.aiddproject.kudos.search.domain.RecentSunner
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Hilt ViewModel for the Search Sunner screen.
 *
 * Owns:
 *  - `state: StateFlow<SearchSunnerUiState>` — collected by the UI.
 *  - Intent dispatchers: `onToggleViewAll`, `onRemove`, `onRowTap`,
 *    `onSearchQueryChange`.
 *  - A debounced search flow that fires [KudosRepository.searchSunner]
 *    500ms after the user stops typing, mirroring the result into
 *    `state.searchResults`.
 *
 * Search-result-row taps go through `onRowTap` like recent-row taps —
 * the tapped Sunner is promoted to position 0 of the recent list
 * BEFORE the navigation callback fires (spec FR-005).
 */
@OptIn(FlowPreview::class)
@HiltViewModel
class SearchSunnerViewModel
    @Inject
    constructor(
        private val recentSunnerRepository: RecentSunnerRepository,
        private val kudosRepository: KudosRepository,
    ) : ViewModel() {
        private val _state = MutableStateFlow(SearchSunnerUiState())
        val state: StateFlow<SearchSunnerUiState> = _state.asStateFlow()

        /** All `SunnerNode`s currently displayed (recent list + active result set) — used by row-tap. */
        private val displayedNodes: MutableMap<String, SunnerNode> = mutableMapOf()

        init {
            // Mirror the recent list from the repo into state.
            viewModelScope.launch {
                recentSunnerRepository.observeAll().collect { list ->
                    _state.update { it.copy(recentSunners = list) }
                }
            }

            // Debounced search pipeline: 500ms after the last keystroke,
            // fire the repository search. Empty/whitespace queries reset
            // to Idle and short-circuit (no API call).
            viewModelScope.launch {
                _state
                    .map { it.searchQuery }
                    .distinctUntilChanged()
                    .debounce(SEARCH_DEBOUNCE_MS)
                    .collect { query ->
                        if (query.isBlank()) {
                            displayedNodes.clear()
                            _state.update { it.copy(searchResults = SearchResultsState.Idle) }
                            return@collect
                        }
                        _state.update { it.copy(searchResults = SearchResultsState.Loading) }
                        val result = kudosRepository.searchSunner(query, limit = MAX_RESULTS)
                        val next =
                            result.fold(
                                onSuccess = { matches ->
                                    val nodes = matches.map { it.node }
                                    // Re-check the query — if the user typed
                                    // more characters while we were waiting,
                                    // a newer collect will overwrite this.
                                    nodes.forEach { displayedNodes[it.id] = it }
                                    if (nodes.isEmpty()) SearchResultsState.Empty
                                    else SearchResultsState.Loaded(nodes)
                                },
                                onFailure = { SearchResultsState.Error },
                            )
                        _state.update { current ->
                            // Drop the result if the query has changed since
                            // we started (rare race; collect dispatches in
                            // order but the network round-trip may overtake).
                            if (current.searchQuery == query) {
                                current.copy(searchResults = next)
                            } else {
                                current
                            }
                        }
                    }
            }
        }

        fun onToggleViewAll() {
            _state.update { it.copy(isViewingAll = !it.isViewingAll) }
        }

        fun onSearchQueryChange(query: String) {
            // Clip to FR-009's 1..100 char range (Figma's design-item
            // spec on `mms_2_Search bar` 6891:22074 sets maxLength=100).
            val clipped = query.take(MAX_QUERY_LENGTH)
            _state.update { it.copy(searchQuery = clipped) }
        }

        fun onRemove(userId: String) {
            viewModelScope.launch {
                recentSunnerRepository.remove(userId)
            }
        }

        /**
         * Promotes the tapped Sunner to the head of the recent list,
         * then invokes [onPromoted] (typically navigates to Profile).
         *
         * Works for BOTH recent-row taps (entry exists in
         * `state.recentSunners`) AND search-result-row taps (entry
         * exists in `displayedNodes`). If neither has it, no-op +
         * onPromoted fires anyway so navigation isn't lost.
         */
        fun onRowTap(userId: String, onPromoted: () -> Unit) {
            val entry =
                _state.value.recentSunners.firstOrNull { it.userId == userId }
                    ?: displayedNodes[userId]?.toRecentSunner()
            if (entry != null) {
                viewModelScope.launch {
                    recentSunnerRepository.addOrPromote(entry)
                }
            }
            onPromoted()
        }

        private fun SunnerNode.toRecentSunner(): RecentSunner =
            RecentSunner(
                userId = id,
                fullName = fullName,
                departmentName = department?.name,
                avatarUrl = null,
                lastSearchedAtMillis = System.currentTimeMillis(),
            )

        private companion object {
            const val MAX_QUERY_LENGTH: Int = 100
            const val MAX_RESULTS: Int = 20
            const val SEARCH_DEBOUNCE_MS: Long = 500L
        }
    }
