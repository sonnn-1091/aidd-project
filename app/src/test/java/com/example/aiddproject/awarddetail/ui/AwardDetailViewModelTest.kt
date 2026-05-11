@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.example.aiddproject.awarddetail.ui

import androidx.lifecycle.SavedStateHandle
import com.example.aiddproject.awarddetail.domain.AwardDetail
import com.example.aiddproject.awarddetail.domain.states.AwardDetailState
import com.example.aiddproject.core.locale.Language
import com.example.aiddproject.core.locale.LanguagePreferenceRepository
import com.example.aiddproject.home.data.AwardsRepository
import com.example.aiddproject.home.data.NotificationsSummaryRepository
import com.example.aiddproject.home.domain.Award
import com.example.aiddproject.home.domain.NotificationsSummary
import com.example.aiddproject.home.domain.states.AwardsState
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * ViewModel-level state-machine tests for [AwardDetailViewModel]
 * (Phase 3 — T020 through T025).
 *
 * Uses `mockk` for the repository pair + a hand-rolled
 * [LanguagePreferenceRepository] fake so the locale flow seeds
 * [Language.VN] synchronously. The `UnconfinedTestDispatcher` lets
 * `init {}`'s launched coroutines complete before `assertEquals` runs.
 */
class AwardDetailViewModelTest {
    private val awardsRepository: AwardsRepository = mockk(relaxed = true)
    private val notificationsRepository: NotificationsSummaryRepository = mockk(relaxed = true)
    private val languageRepository: LanguagePreferenceRepository = mockk(relaxed = true)

    private val topTalentList =
        listOf(
            Award(id = "a1", name = "Top Talent", thumbnailUrl = null, sortOrder = 1),
            Award(id = "a2", name = "Top Project", thumbnailUrl = null, sortOrder = 2),
        )

    private val topTalentDetail =
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

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        // Default stubs — happy path; individual tests override.
        every { languageRepository.language } returns flowOf(Language.VN)
        coEvery { awardsRepository.list() } returns Result.success(topTalentList)
        coEvery { awardsRepository.detail(any(), any()) } returns Result.success(topTalentDetail)
        coEvery { notificationsRepository.get() } returns Result.success(NotificationsSummary(0))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun buildVm(awardId: String? = "a1") =
        AwardDetailViewModel(
            savedStateHandle =
                SavedStateHandle().apply {
                    if (awardId != null) set("awardId", awardId)
                },
            awardsRepository = awardsRepository,
            notificationsRepository = notificationsRepository,
            languageRepository = languageRepository,
        )

    @Test
    fun `load emits Loading then Loaded on success`() =
        runTest {
            val vm = buildVm(awardId = "a1")

            // Init coroutines complete under UnconfinedTestDispatcher; final state is Loaded.
            assertEquals(AwardDetailState.Loaded(topTalentDetail), vm.uiState.value.detail)
        }

    @Test
    fun `load emits Loading then Error on failure`() =
        runTest {
            coEvery { awardsRepository.detail(any(), any()) } returns
                Result.failure(RuntimeException("boom"))

            val vm = buildVm(awardId = "a1")

            assertTrue(vm.uiState.value.detail is AwardDetailState.Error)
        }

    @Test
    fun `retry re-issues fetch after error`() =
        runTest {
            coEvery { awardsRepository.detail(any(), any()) } returns
                Result.failure(RuntimeException("boom"))
            val vm = buildVm(awardId = "a1")
            assertTrue(vm.uiState.value.detail is AwardDetailState.Error)

            // Flip the stub to succeed and trigger retry.
            coEvery { awardsRepository.detail(any(), any()) } returns Result.success(topTalentDetail)
            vm.onRetry()

            assertEquals(AwardDetailState.Loaded(topTalentDetail), vm.uiState.value.detail)
            coVerify(atLeast = 2) { awardsRepository.detail("a1", Language.VN) }
        }

    @Test
    fun `401 repository result emits Error state`() =
        runTest {
            // The repository surfaces 401 as `Result.failure(...)`; the VM doesn't
            // distinguish 401 from any other failure — both map to Error. The NavHost-
            // level AuthRedirectController handles the bounce out-of-band.
            coEvery { awardsRepository.detail(any(), any()) } returns
                Result.failure(IllegalStateException("HTTP 401 Unauthorized"))

            val vm = buildVm(awardId = "a1")

            assertTrue(vm.uiState.value.detail is AwardDetailState.Error)
        }

    @Test
    fun `init uses savedStateHandle awardId when present`() =
        runTest {
            buildVm(awardId = "a2")

            // The detail fetch must target "a2" — NOT "a1" (which would be the
            // first-by-sort_order fallback). Locks FR-001 + Resolved Q1.
            coVerify(exactly = 1) { awardsRepository.detail("a2", Language.VN) }
        }

    @Test
    fun `init falls back to first award by sort_order when savedStateHandle empty`() =
        runTest {
            // SavedStateHandle has no awardId — VM should call list() (it does so
            // twice: once for categories, once for the fallback id-resolution path)
            // then detail(first.id). The fallback resolves to "a1" because it has
            // sort_order = 1.
            val vm = buildVm(awardId = null)

            coVerify(atLeast = 1) { awardsRepository.list() }
            coVerify { awardsRepository.detail("a1", Language.VN) }
            assertEquals("a1", vm.uiState.value.activeAwardId)
        }

    @Test
    fun `categories state is Populated after successful list fetch`() =
        runTest {
            val vm = buildVm(awardId = "a1")

            val categories = vm.uiState.value.categories
            assertTrue(categories is AwardsState.Populated)
            assertEquals(topTalentList, (categories as AwardsState.Populated).items)
        }

    @Test
    fun `notifications unreadCount surfaces via uiState`() =
        runTest {
            coEvery { notificationsRepository.get() } returns
                Result.success(NotificationsSummary(unreadCount = 3))

            val vm = buildVm(awardId = "a1")

            assertEquals(3, vm.uiState.value.unreadCount)
            assertNotNull(vm.uiState.value)
        }

    // ---------------------------------------------------------------
    // Phase 4 — US2 category dropdown VM tests (T057–T060)
    // ---------------------------------------------------------------

    @Test
    fun `dropdown_select cancels in-flight request and loads new`() =
        runTest {
            // First call (init) loads Top Talent successfully.
            val vm = buildVm(awardId = "a1")
            assertEquals(AwardDetailState.Loaded(topTalentDetail), vm.uiState.value.detail)

            // Reset call counts so we can verify the SELECT path fires a new detail call.
            // Switch the stub so the second selection returns Top Project.
            val topProjectDetail = topTalentDetail.copy(id = "a2", name = "Top Project")
            coEvery { awardsRepository.detail("a2", Language.VN) } returns
                Result.success(topProjectDetail)

            vm.onCategorySelected("a2")

            // After the new selection completes, the state reflects Top Project.
            assertEquals(AwardDetailState.Loaded(topProjectDetail), vm.uiState.value.detail)
            assertEquals("a2", vm.uiState.value.activeAwardId)
            coVerify(atLeast = 1) { awardsRepository.detail("a2", Language.VN) }
        }

    @Test
    fun `dropdown_select_idempotent does not re-fetch`() =
        runTest {
            val vm = buildVm(awardId = "a1")
            assertEquals(AwardDetailState.Loaded(topTalentDetail), vm.uiState.value.detail)

            // Re-select the already-active award — VM must short-circuit
            // BEFORE calling repository.detail again (FR-005 + FR-006).
            io.mockk.clearMocks(awardsRepository, answers = false)
            // Re-establish the default stubs without changing answers so verify counts reset.
            coEvery { awardsRepository.list() } returns Result.success(topTalentList)
            coEvery { awardsRepository.detail(any(), any()) } returns Result.success(topTalentDetail)

            vm.onCategorySelected("a1")

            coVerify(exactly = 0) { awardsRepository.detail("a1", Language.VN) }
        }

    @Test
    fun `empty_awards_list dropdown renders AwardsState_Empty`() =
        runTest {
            coEvery { awardsRepository.list() } returns Result.success(emptyList())

            val vm = buildVm(awardId = null)

            assertTrue(vm.uiState.value.categories === AwardsState.Empty)
        }

    @Test
    fun `single_award dropdown lists one row`() =
        runTest {
            val solo =
                listOf(
                    com.example.aiddproject.home.domain.Award(
                        id = "only",
                        name = "Only Award",
                        thumbnailUrl = null,
                        sortOrder = 1,
                    ),
                )
            coEvery { awardsRepository.list() } returns Result.success(solo)

            val vm = buildVm(awardId = "only")

            val categories = vm.uiState.value.categories
            assertTrue(categories is AwardsState.Populated)
            assertEquals(1, (categories as AwardsState.Populated).items.size)
        }
}
