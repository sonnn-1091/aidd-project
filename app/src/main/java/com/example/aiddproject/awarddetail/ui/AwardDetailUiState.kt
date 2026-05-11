package com.example.aiddproject.awarddetail.ui

import com.example.aiddproject.awarddetail.domain.states.AwardDetailState
import com.example.aiddproject.core.locale.Language
import com.example.aiddproject.home.domain.states.AwardsState

/**
 * Aggregate UI state for the Award Detail screen (spec `c-QM3_zjkG`).
 *
 * The shape mirrors Home's `HomeUiState`: one immutable value class
 * combining every sub-state the screen reads. The screen body's state
 * machine lives in [detail]; the category dropdown's list comes from
 * [categories] (re-using Home's `AwardsState`); the bell badge / locale
 * pill read [unreadCount] + [language] which are sourced from the same
 * repositories Home consumes.
 *
 * Each field has a documented role tied to a user story:
 * - [activeAwardId]: nullable until the VM's init coroutine resolves
 *   the navigation-arg or list-fallback; FR-001 + Resolved Q1.
 * - [detail]: US1 body (Loading / Loaded / Error).
 * - [categories]: US2 dropdown (Loading / Populated / Empty / Error).
 * - [unreadCount]: US6 bell badge (0 = no badge).
 * - [language]: US5 chrome locale.
 */
data class AwardDetailUiState(
    val activeAwardId: String?,
    val detail: AwardDetailState,
    val categories: AwardsState,
    val unreadCount: Int,
    val language: Language,
) {
    companion object {
        val Empty: AwardDetailUiState =
            AwardDetailUiState(
                activeAwardId = null,
                detail = AwardDetailState.Loading,
                categories = AwardsState.Loading,
                unreadCount = 0,
                language = Language.Default,
            )
    }
}
