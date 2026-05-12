package com.example.aiddproject.kudos.ui

import com.example.aiddproject.core.locale.Language
import com.example.aiddproject.kudos.domain.Department
import com.example.aiddproject.kudos.domain.Hashtag
import com.example.aiddproject.kudos.domain.SnackbarMessage
import com.example.aiddproject.kudos.domain.SpotlightSearchResult
import com.example.aiddproject.kudos.domain.states.AllKudosState
import com.example.aiddproject.kudos.domain.states.KudosHighlightState
import com.example.aiddproject.kudos.domain.states.PersonalStatsState
import com.example.aiddproject.kudos.domain.states.SpotlightState
import com.example.aiddproject.kudos.domain.states.TopTenState

/**
 * Aggregate UI state for the Sun*Kudos hub (spec fO0Kt19sZZ § US1).
 *
 * One immutable value class combining every sub-state the screen
 * reads — five independent section-level sealed states plus filter
 * selections, spotlight search input + result, pull-to-refresh
 * latch, snackbar slot, system flags ([specialDayActive] for Q-K-1
 * heart math), and locale.
 *
 * The shape mirrors Award Detail's `AwardDetailUiState` (one
 * StateFlow, parametric content composable). Phase 3 MVP wires the
 * five section states; filter/search/snackbar/specialDayActive land
 * in Phases 5/7/9/11.
 */
data class KudosUiState(
    val highlight: KudosHighlightState,
    val allKudos: AllKudosState,
    val spotlight: SpotlightState,
    val stats: PersonalStatsState,
    val topTen: TopTenState,
    val selectedHashtagId: String? = null,
    val selectedDepartmentId: String? = null,
    val hashtags: List<Hashtag> = emptyList(),
    val departments: List<Department> = emptyList(),
    val spotlightSearchQuery: String = "",
    val spotlightSearchResult: SpotlightSearchResult = SpotlightSearchResult.Idle,
    val isRefreshing: Boolean = false,
    val snackbar: SnackbarMessage? = null,
    val specialDayActive: Boolean = false,
    val x2BonusActive: Boolean = false,
    val language: Language = Language.Default,
) {
    companion object {
        val Empty: KudosUiState =
            KudosUiState(
                highlight = KudosHighlightState.Loading,
                allKudos = AllKudosState.Loading,
                spotlight = SpotlightState.Loading,
                stats = PersonalStatsState.Loading,
                topTen = TopTenState.Loading,
            )
    }
}
