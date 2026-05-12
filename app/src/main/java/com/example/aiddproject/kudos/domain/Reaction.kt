package com.example.aiddproject.kudos.domain

/**
 * Optimistic local reaction record retained briefly during the
 * apply-then-confirm-or-rollback window in
 * [com.example.aiddproject.kudos.ui.KudosViewModel.onHeartTap].
 *
 * Persisted to the server via
 * [com.example.aiddproject.kudos.data.KudosRepository.addReaction] /
 * [com.example.aiddproject.kudos.data.KudosRepository.removeReaction];
 * the server enforces uniqueness via `reactions.unique(user_id, kudos_id)`
 * (plan § Constitution Compliance — IV OWASP).
 */
data class Reaction(
    val kudosId: String,
    val userId: String,
    val liked: Boolean,
)
