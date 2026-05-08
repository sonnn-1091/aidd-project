package com.example.aiddproject.home.domain.states

import com.example.aiddproject.home.domain.Award

/**
 * Awards section state machine (FR-003): Loading → Populated / Empty / Error;
 * Retry re-fires the awards fetch.
 */
sealed interface AwardsState {
    data object Loading : AwardsState

    data object Empty : AwardsState

    data class Error(
        val message: String?,
    ) : AwardsState

    data class Populated(
        val items: List<Award>,
    ) : AwardsState
}
