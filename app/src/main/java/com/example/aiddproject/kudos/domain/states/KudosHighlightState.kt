package com.example.aiddproject.kudos.domain.states

import androidx.annotation.StringRes
import com.example.aiddproject.kudos.domain.Kudos

/**
 * Highlight carousel state machine (spec § US4).
 * Mirrors Home's [com.example.aiddproject.home.domain.states.AwardsState]
 * shape so the section UI follows the same Loading/Loaded/Empty/Error
 * dispatch.
 */
sealed interface KudosHighlightState {
    data object Loading : KudosHighlightState

    data object Empty : KudosHighlightState

    data class Loaded(
        val items: List<Kudos>,
    ) : KudosHighlightState

    data class Error(
        @param:StringRes val messageRes: Int,
    ) : KudosHighlightState
}
