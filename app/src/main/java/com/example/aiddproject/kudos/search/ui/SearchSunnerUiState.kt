package com.example.aiddproject.kudos.search.ui

import com.example.aiddproject.kudos.search.domain.RecentSunner
import com.example.aiddproject.kudos.domain.SunnerNode

/**
 * Single read model for the Search Sunner screen.
 *
 *  - When `searchQuery.isEmpty()`: the UI renders the "Recent" list
 *    (or hides it entirely if `recentSunners.isEmpty()`).
 *  - When `searchQuery.isNotEmpty()`: the UI renders [searchResults]
 *    (Loading → Loaded / Empty / Error). The Recent section is hidden
 *    in this state to keep the screen focused on the active query.
 *
 * `isViewingAll` flips on tap of the "View all"/"Collapse" toggle
 * (FR-008). Only meaningful when `searchQuery.isEmpty()`.
 */
data class SearchSunnerUiState(
    val recentSunners: List<RecentSunner> = emptyList(),
    val isViewingAll: Boolean = false,
    val searchQuery: String = "",
    val searchResults: SearchResultsState = SearchResultsState.Idle,
) {
    val visibleSunners: List<RecentSunner>
        get() = if (isViewingAll) recentSunners else recentSunners.take(COLLAPSED_VISIBLE_COUNT)

    /** True iff the recent list exceeds the collapsed window — i.e. there is something to expand. */
    val showViewAllButton: Boolean
        get() = recentSunners.size > COLLAPSED_VISIBLE_COUNT

    /** True iff the user has typed something and the screen should render search results. */
    val isSearching: Boolean
        get() = searchQuery.isNotBlank()

    companion object {
        /** Per Q-S-8 / spec FR-007. */
        const val COLLAPSED_VISIBLE_COUNT: Int = 2
    }
}

/**
 * Lifecycle of the live-search results.
 *
 *  - [Idle] — query is empty; the search flow is not active.
 *  - [Loading] — query has triggered (debounced), result not yet back.
 *  - [Loaded] — at least one match returned.
 *  - [Empty] — query returned zero matches.
 *  - [Error] — the repository call failed (network / DB / RLS / etc).
 */
sealed interface SearchResultsState {
    data object Idle : SearchResultsState

    data object Loading : SearchResultsState

    data class Loaded(val matches: List<SunnerNode>) : SearchResultsState

    data object Empty : SearchResultsState

    data object Error : SearchResultsState
}
