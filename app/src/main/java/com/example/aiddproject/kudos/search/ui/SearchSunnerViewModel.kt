package com.example.aiddproject.kudos.search.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aiddproject.kudos.search.data.RecentSunnerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Hilt ViewModel for the Search Sunner default-state screen.
 *
 * Owns:
 *  - `state: StateFlow<SearchSunnerUiState>` — collected by the UI.
 *  - Intent dispatchers: `onToggleViewAll`, `onRemove`, `onRowTap`.
 *
 * The repository's flow is the source of truth for the recent list;
 * the `init {}` block mirrors it into the state. Row-tap promotes the
 * tapped entry BEFORE invoking the navigation callback (spec FR-005 +
 * FR-010 — the promotion must persist before Profile mounts).
 */
@HiltViewModel
class SearchSunnerViewModel
    @Inject
    constructor(
        private val recentSunnerRepository: RecentSunnerRepository,
    ) : ViewModel() {
        private val _state = MutableStateFlow(SearchSunnerUiState())
        val state: StateFlow<SearchSunnerUiState> = _state.asStateFlow()

        init {
            viewModelScope.launch {
                recentSunnerRepository.observeAll().collect { list ->
                    _state.update { it.copy(recentSunners = list) }
                }
            }
        }

        fun onToggleViewAll() {
            _state.update { it.copy(isViewingAll = !it.isViewingAll) }
        }

        fun onSearchQueryChange(query: String) {
            // Clip to FR-009's 1..100 char range (the Searching-state spec
            // owns the live-search ITSELF; this screen owns the field
            // input lifecycle).
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
         * The repository write fires-and-forgets in `viewModelScope` —
         * navigation does NOT wait for the persistence to flush since
         * the recent list is local and the next screen doesn't depend
         * on it.
         */
        fun onRowTap(userId: String, onPromoted: () -> Unit) {
            val tapped = _state.value.recentSunners.firstOrNull { it.userId == userId }
            if (tapped != null) {
                viewModelScope.launch {
                    recentSunnerRepository.addOrPromote(tapped)
                }
            }
            onPromoted()
        }

        private companion object {
            const val MAX_QUERY_LENGTH: Int = 100
        }
    }
