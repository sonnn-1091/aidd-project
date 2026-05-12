package com.example.aiddproject.kudos.domain.states

import androidx.annotation.StringRes
import com.example.aiddproject.kudos.domain.SpotlightGraph

/** Spotlight Board state machine (spec § US9). */
sealed interface SpotlightState {
    data object Loading : SpotlightState

    data object Empty : SpotlightState

    data class Loaded(
        val graph: SpotlightGraph,
    ) : SpotlightState

    data class Error(
        @param:StringRes val messageRes: Int,
    ) : SpotlightState
}
