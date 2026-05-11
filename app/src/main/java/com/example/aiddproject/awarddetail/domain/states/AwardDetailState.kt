package com.example.aiddproject.awarddetail.domain.states

import androidx.annotation.StringRes
import com.example.aiddproject.awarddetail.domain.AwardDetail

/**
 * Body-region state machine for the Award Detail screen (spec
 * `c-QM3_zjkG` § US1 acceptance scenarios 2 + 3 + FR-003/FR-004).
 *
 * - [Loading]: in-flight `repository.detail(id, locale)` fetch; UI renders
 *   a spinner / skeleton.
 * - [Loaded]: payload arrived; UI renders the full body.
 * - [Error]: the fetch failed; UI renders a friendly localized message
 *   plus a Retry button. The exception text is NEVER surfaced (TR-002 +
 *   constitution § OWASP) — only [messageRes].
 *
 * Mirrors Home's domain-state pattern (`AwardsState`, `KudosState`) so
 * the project keeps a uniform shape across feature packages.
 */
sealed interface AwardDetailState {
    data object Loading : AwardDetailState

    data class Loaded(
        val detail: AwardDetail,
    ) : AwardDetailState

    data class Error(
        @StringRes val messageRes: Int,
    ) : AwardDetailState
}
