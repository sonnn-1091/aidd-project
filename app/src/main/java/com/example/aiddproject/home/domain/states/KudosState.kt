package com.example.aiddproject.home.domain.states

import com.example.aiddproject.home.domain.KudosSummary

/**
 * Kudos section state. Hidden when the server feature flag is false
 * (Q-Home-9 — only the lower section is gated, not the FAB S/Kudos / NavBar Kudos).
 */
sealed interface KudosState {
    data object Hidden : KudosState

    data object Loading : KudosState

    data class Loaded(
        val summary: KudosSummary,
    ) : KudosState

    data class Error(
        val message: String?,
    ) : KudosState
}
