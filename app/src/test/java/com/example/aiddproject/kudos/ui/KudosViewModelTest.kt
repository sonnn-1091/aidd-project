@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.example.aiddproject.kudos.ui

import androidx.lifecycle.SavedStateHandle
import com.example.aiddproject.core.locale.Language
import com.example.aiddproject.core.locale.LanguagePreferenceRepository
import com.example.aiddproject.kudos.data.DemoKudosRepository
import com.example.aiddproject.kudos.data.KudosRepository
import com.example.aiddproject.kudos.domain.Department
import com.example.aiddproject.kudos.domain.GiftRecipient
import com.example.aiddproject.kudos.domain.Hashtag
import com.example.aiddproject.kudos.domain.Kudos
import com.example.aiddproject.kudos.domain.KudosFilter
import com.example.aiddproject.kudos.domain.KudosPage
import com.example.aiddproject.kudos.domain.PersonalStats
import com.example.aiddproject.kudos.domain.SecretBoxRef
import com.example.aiddproject.kudos.domain.SecretBoxReward
import com.example.aiddproject.kudos.domain.SpotlightGraph
import com.example.aiddproject.kudos.domain.SunnerMatch
import com.example.aiddproject.kudos.domain.SystemFlags
import com.example.aiddproject.kudos.domain.states.AllKudosState
import com.example.aiddproject.kudos.domain.states.KudosHighlightState
import com.example.aiddproject.kudos.domain.states.PersonalStatsState
import com.example.aiddproject.kudos.domain.states.SpotlightState
import com.example.aiddproject.kudos.domain.states.TopTenState
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * State-machine assertions for [KudosViewModel] (spec § US1).
 *
 * Phase 3 MVP scope:
 *  - init triggers 5 parallel fetches; every section flips
 *    Loading → Loaded with the DEMO seed.
 *  - A section repository failure isolates to that section's Error
 *    state; others still reach Loaded.
 *  - onPullToRefresh re-runs the 5 fetches and gates concurrent
 *    invocations via `isRefreshing` per plan's snippet.
 *
 * Later phases extend this file with US3/US5/US13 assertions.
 */
class KudosViewModelTest {
    private val languageRepository: LanguagePreferenceRepository = mockk(relaxed = true)

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        every { languageRepository.language } returns flowOf(Language.Default)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun init_loads_every_section() =
        runTest {
            val viewModel = newViewModel(DemoKudosRepository())

            val final = viewModel.uiState.value
            assertTrue(final.highlight is KudosHighlightState.Loaded)
            assertTrue(final.allKudos is AllKudosState.Loaded)
            assertTrue(final.spotlight is SpotlightState.Loaded)
            assertTrue(final.stats is PersonalStatsState.Loaded)
            assertTrue(final.topTen is TopTenState.Loaded)
        }

    @Test
    fun section_failure_isolates_to_that_section_error() =
        runTest {
            val viewModel = newViewModel(FailingHighlightRepo(DemoKudosRepository()))

            val final = viewModel.uiState.value
            assertTrue(final.highlight is KudosHighlightState.Error)
            assertTrue(final.allKudos is AllKudosState.Loaded)
            assertTrue(final.spotlight is SpotlightState.Loaded)
            assertTrue(final.stats is PersonalStatsState.Loaded)
            assertTrue(final.topTen is TopTenState.Loaded)
        }

    @Test
    fun pull_to_refresh_re_runs_and_clears_flag() =
        runTest {
            val viewModel = newViewModel(DemoKudosRepository())

            viewModel.onPullToRefresh()

            assertEquals(false, viewModel.uiState.value.isRefreshing)
            assertTrue(viewModel.uiState.value.highlight is KudosHighlightState.Loaded)
        }

    @Test
    fun pull_to_refresh_gates_concurrent_calls() =
        runTest {
            val viewModel = newViewModel(DemoKudosRepository())

            // Fire two calls — the second short-circuits on isRefreshing.
            viewModel.onPullToRefresh()
            viewModel.onPullToRefresh()

            assertEquals(false, viewModel.uiState.value.isRefreshing)
        }

    private fun newViewModel(repo: KudosRepository): KudosViewModel =
        KudosViewModel(
            savedStateHandle = SavedStateHandle(),
            repository = repo,
            languagePreferenceRepository = languageRepository,
        )

    /** Wraps DEMO repo but forces listHighlight to fail. */
    private class FailingHighlightRepo(
        private val delegate: KudosRepository,
    ) : KudosRepository {
        override suspend fun listHighlight(filter: KudosFilter): Result<List<Kudos>> = Result.failure(IllegalStateException("forced"))

        override suspend fun listKudos(
            filter: KudosFilter,
            page: Int,
            limit: Int,
        ): Result<KudosPage> = delegate.listKudos(filter, page, limit)

        override suspend fun detail(kudosId: String): Result<Kudos> = delegate.detail(kudosId)

        override suspend fun addReaction(kudosId: String): Result<Unit> = delegate.addReaction(kudosId)

        override suspend fun removeReaction(kudosId: String): Result<Unit> = delegate.removeReaction(kudosId)

        override suspend fun listHashtags(): Result<List<Hashtag>> = delegate.listHashtags()

        override suspend fun listDepartments(): Result<List<Department>> = delegate.listDepartments()

        override suspend fun loadSpotlightGraph(): Result<SpotlightGraph> = delegate.loadSpotlightGraph()

        override suspend fun searchSunner(
            query: String,
            limit: Int,
        ): Result<List<SunnerMatch>> = delegate.searchSunner(query, limit)

        override suspend fun personalStats(): Result<PersonalStats> = delegate.personalStats()

        override suspend fun systemFlags(): Result<SystemFlags> = delegate.systemFlags()

        override suspend fun nextUnopenedBox(): Result<SecretBoxRef?> = delegate.nextUnopenedBox()

        override suspend fun openSecretBox(boxId: String): Result<SecretBoxReward> = delegate.openSecretBox(boxId)

        override suspend fun listRecentGiftRecipients(limit: Int): Result<List<GiftRecipient>> = delegate.listRecentGiftRecipients(limit)
    }
}
