package com.example.aiddproject.home.data

import com.example.aiddproject.awarddetail.domain.AwardDetail
import com.example.aiddproject.core.locale.Language
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Gateway-double tests for [SupabasePostgrestAwardsRepository.detail]
 * (Phase 2 — T008 through T011). Mirrors the gateway pattern from
 * [SupabasePostgrestAwardsRepositoryTest] so the real Supabase SDK call
 * path is exercised by an instrumented integration test against a
 * seeded local Postgrest (Constitution Principle V).
 */
class SupabaseAwardsRepositoryDetailTest {
    private val gateway: SupabasePostgrestAwardsGateway = mockk(relaxed = true)
    private val repository = SupabasePostgrestAwardsRepository(gateway)

    private val topTalent =
        AwardDetail(
            id = "a1",
            name = "Top Talent",
            description = "Giải thưởng Top Talent vinh danh…",
            quantity = 10,
            quantityUnit = "Cá nhân",
            prizeValue = "7.000.000 VNĐ",
            imageUrl = "https://example/top-talent.png",
            sortOrder = 1,
        )

    @Test
    fun `detail returns full payload when id matches`() =
        runTest {
            coEvery { gateway.detailAward("a1", Language.VN) } returns topTalent

            val result = repository.detail("a1", Language.VN)

            assertTrue(result.isSuccess)
            assertEquals(topTalent, result.getOrNull())
            coVerify(exactly = 1) { gateway.detailAward("a1", Language.VN) }
        }

    @Test
    fun `detail returns failure when id missing`() =
        runTest {
            val cause = NoSuchElementException("Award not found for id=missing")
            coEvery { gateway.detailAward("missing", Language.VN) } throws cause

            val result = repository.detail("missing", Language.VN)

            assertTrue(result.isFailure)
            assertEquals(cause, result.exceptionOrNull())
        }

    @Test
    fun `detail propagates locale to gateway`() =
        runTest {
            val idSlot = slot<String>()
            val localeSlot = slot<Language>()
            coEvery { gateway.detailAward(capture(idSlot), capture(localeSlot)) } returns topTalent

            repository.detail("a1", Language.EN)

            assertEquals("a1", idSlot.captured)
            assertEquals(Language.EN, localeSlot.captured)
        }

    @Test
    fun `detail wraps a 401-style exception as failure for AuthRedirectController`() =
        runTest {
            // The Supabase SDK throws RestException / HttpRequestException family on 401;
            // we only assert the repository propagates the exception verbatim so the
            // NavHost-level AuthRedirectController can recognize it. The actual type
            // matching lives in the controller; here we just lock the propagation.
            val cause = IllegalStateException("HTTP 401 Unauthorized")
            coEvery { gateway.detailAward(any(), any()) } throws cause

            val result = repository.detail("a1", Language.VN)

            assertTrue(result.isFailure)
            assertNotNull(result.exceptionOrNull())
            assertEquals(cause, result.exceptionOrNull())
        }
}
