package com.example.aiddproject.kudos.domain

import androidx.annotation.StringRes
import com.example.aiddproject.R

/**
 * Hub-level Snackbar state. Single sealed type so the view model can
 * set one slot and the [com.example.aiddproject.kudos.ui.components.CopyLinkSnackbarHost]
 * collects it without branching on string literals.
 *
 * - [LinkCopied] surfaces after a successful Copy Link write to the
 *   clipboard (US13).
 * - [ReactionFailed] surfaces after the optimistic heart write was
 *   rolled back due to a network/RLS failure (US5 mechanic).
 */
sealed interface SnackbarMessage {
    @get:StringRes val messageRes: Int

    data object LinkCopied : SnackbarMessage {
        @get:StringRes override val messageRes: Int = R.string.kudos_copy_link_snackbar
    }

    data object ReactionFailed : SnackbarMessage {
        @get:StringRes override val messageRes: Int = R.string.kudos_reaction_failed_snackbar
    }
}
