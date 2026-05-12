package com.example.aiddproject.kudos.domain.states

import androidx.annotation.StringRes
import com.example.aiddproject.kudos.domain.PersonalStats

/**
 * Personal stats panel state machine (spec § US10).
 *
 * Empty isn't expressible — every authenticated Sunner has a row in
 * `user_stats` (seeded with zeros) — so the state collapses to
 * Loading/Loaded/Error only.
 */
sealed interface PersonalStatsState {
    data object Loading : PersonalStatsState

    data class Loaded(
        val stats: PersonalStats,
    ) : PersonalStatsState

    data class Error(
        @param:StringRes val messageRes: Int,
    ) : PersonalStatsState
}
