package com.example.aiddproject.kudos.search.ui

import com.example.aiddproject.kudos.search.domain.RecentSunner

/**
 * Single read-model for the Search Sunner default-state screen.
 *
 * `recentSunners` is mirrored from the repository's flow (most-recent
 * first). `isViewingAll` flips on tap of "View all"/"Collapse" per
 * spec FR-008. The derived [visibleSunners] is what the UI renders —
 * 2 rows when collapsed (spec FR-007 / Q-S-8), all rows when expanded.
 */
data class SearchSunnerUiState(
    val recentSunners: List<RecentSunner> = emptyList(),
    val isViewingAll: Boolean = false,
    /** Live text typed into the search field. Drives the Searching state in a future spec. */
    val searchQuery: String = "",
) {
    val visibleSunners: List<RecentSunner>
        get() = if (isViewingAll) recentSunners else recentSunners.take(COLLAPSED_VISIBLE_COUNT)

    /** True iff the recent list exceeds the collapsed window — i.e. there is something to expand. */
    val showViewAllButton: Boolean
        get() = recentSunners.size > COLLAPSED_VISIBLE_COUNT

    companion object {
        /** Per Q-S-8 / spec FR-007. */
        const val COLLAPSED_VISIBLE_COUNT: Int = 2
    }
}
