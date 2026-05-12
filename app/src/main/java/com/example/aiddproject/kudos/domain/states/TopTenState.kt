package com.example.aiddproject.kudos.domain.states

import androidx.annotation.StringRes
import com.example.aiddproject.kudos.domain.GiftRecipient

/** Top 10 recent gift recipients state machine (spec § US12). */
sealed interface TopTenState {
    data object Loading : TopTenState

    data object Empty : TopTenState

    data class Loaded(
        val items: List<GiftRecipient>,
    ) : TopTenState

    data class Error(
        @param:StringRes val messageRes: Int,
    ) : TopTenState
}
