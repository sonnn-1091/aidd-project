package com.example.aiddproject.awarddetail.domain.states

import com.example.aiddproject.R
import com.example.aiddproject.awarddetail.domain.AwardDetail
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

/**
 * Sealed-interface smoke tests for [AwardDetailState] (T012 + T013).
 *
 * These don't drive the VM — they lock the type's shape so any
 * accidental refactor (adding a new variant, renaming a property)
 * surfaces as a compile / test failure rather than a silent
 * behavioural drift.
 */
class AwardDetailStateTest {
    private val sampleDetail =
        AwardDetail(
            id = "a1",
            name = "Top Talent",
            description = "...",
            quantity = 10,
            quantityUnit = "Cá nhân",
            prizeValue = "7.000.000 VNĐ",
            imageUrl = null,
            sortOrder = 1,
        )

    @Test
    fun `transitions Loading to Loaded on success`() {
        val before: AwardDetailState = AwardDetailState.Loading
        val after: AwardDetailState = AwardDetailState.Loaded(sampleDetail)

        assertNotEquals(before, after)
        assertEquals(AwardDetailState.Loaded(sampleDetail), after)
    }

    @Test
    fun `transitions Loading to Error on failure`() {
        val before: AwardDetailState = AwardDetailState.Loading
        val after: AwardDetailState = AwardDetailState.Error(R.string.award_detail_error)

        assertNotEquals(before, after)
        assertEquals(AwardDetailState.Error(R.string.award_detail_error), after)
    }

    @Test
    fun `Loading is a singleton object`() {
        assertEquals(AwardDetailState.Loading, AwardDetailState.Loading)
    }
}
