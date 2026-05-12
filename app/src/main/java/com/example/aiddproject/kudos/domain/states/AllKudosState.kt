package com.example.aiddproject.kudos.domain.states

import androidx.annotation.StringRes
import com.example.aiddproject.kudos.domain.Kudos

/**
 * All Kudos feed state machine (spec § US1). Pagination metadata
 * lives in the Loaded variant so the UI can render the "View all
 * Kudos" tail link (US14) without consulting the repository again.
 */
sealed interface AllKudosState {
    data object Loading : AllKudosState

    data object Empty : AllKudosState

    data class Loaded(
        val items: List<Kudos>,
        val hasMore: Boolean,
        val nextPage: Int?,
    ) : AllKudosState

    data class Error(
        @param:StringRes val messageRes: Int,
    ) : AllKudosState
}
