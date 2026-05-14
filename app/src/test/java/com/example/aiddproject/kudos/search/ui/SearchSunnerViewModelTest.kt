package com.example.aiddproject.kudos.search.ui

import app.cash.turbine.test
import com.example.aiddproject.kudos.data.DemoKudosRepository
import com.example.aiddproject.kudos.data.KudosRepository
import com.example.aiddproject.kudos.search.data.RecentSunnerRepository
import com.example.aiddproject.kudos.search.domain.RecentSunner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * State-machine unit tests for [SearchSunnerViewModel] verifying the
 * two behaviors confirmed by the user:
 *
 *  1. **Click-to-save**: tapping a row (recent OR search-result)
 *     promotes that Sunner to position 0 of the recent list and
 *     persists the change via the repository.
 *
 *  2. **Empty-box-shows-recent**: when `searchQuery.isBlank()`, the
 *     state's `searchResults` is `Idle` and the UI renders the
 *     persisted recent list. Typing flips to live search results;
 *     clearing brings the recent list back (with any taps preserved).
 *
 * Uses the real [DemoKudosRepository] for `searchSunner()` (already
 * filters the 16-entry DEMO_SUNNERS seed by name + dept) and an
 * in-memory [FakeRecentSunnerRepository] for the persistence layer.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SearchSunnerViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)
    private lateinit var kudosRepo: DemoKudosRepository
    private lateinit var recentRepo: FakeRecentSunnerRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        kudosRepo = DemoKudosRepository()
        recentRepo = FakeRecentSunnerRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun newVm(): SearchSunnerViewModel = SearchSunnerViewModel(recentRepo, kudosRepo as KudosRepository)

    // ── Initial state ───────────────────────────────────────────────

    @Test
    fun initialState_emptyRecent_idleSearchResults() =
        testScope.runTest {
            val vm = newVm()
            advanceUntilIdle()
            val state = vm.state.value
            assertTrue("recent list should be empty", state.recentSunners.isEmpty())
            assertEquals(SearchResultsState.Idle, state.searchResults)
            assertEquals("", state.searchQuery)
            assertEquals(false, state.isViewingAll)
            assertEquals(false, state.isSearching)
        }

    // ── Empty-box → Recent (Behavior 2) ─────────────────────────────

    @Test
    fun whenQueryEmpty_recentListFlowsThroughState() =
        testScope.runTest {
            // Seed the repo with 2 recent entries.
            recentRepo.seed(
                listOf(
                    recent("u01", "Nguyễn An", "CECV1", millis = 200),
                    recent("u02", "Trần Bình", "CECV1", millis = 100),
                ),
            )
            val vm = newVm()
            advanceUntilIdle()

            val state = vm.state.value
            assertEquals(2, state.recentSunners.size)
            assertEquals("u01", state.recentSunners[0].userId) // most-recent first
            assertEquals(SearchResultsState.Idle, state.searchResults) // empty query → idle
            assertEquals(false, state.isSearching)
        }

    // ── Debounced live search (the wiring, not the click-to-save) ───

    @Test
    fun typingTriggersDebouncedSearch_afterHalfASecond() =
        testScope.runTest {
            val vm = newVm()
            advanceUntilIdle()

            vm.onSearchQueryChange("Nguy")
            // BEFORE 500ms: still Idle (or transient — debounce hasn't fired)
            advanceTimeBy(400)
            assertEquals(SearchResultsState.Idle, vm.state.value.searchResults)

            // AFTER 500ms: search fires + result comes back
            advanceTimeBy(200) // total 600ms, past the 500ms debounce
            advanceUntilIdle()
            val result = vm.state.value.searchResults
            assertTrue(
                "expected Loaded after debounce, got: $result",
                result is SearchResultsState.Loaded,
            )
            val matches = (result as SearchResultsState.Loaded).matches
            assertTrue(
                "search 'Nguy' should match at least one Nguyễn",
                matches.any { it.fullName.contains("Nguy") },
            )
        }

    @Test
    fun clearingQuery_resetsToIdle_andRecentReappears() =
        testScope.runTest {
            // Seed recent + start a search.
            recentRepo.seed(listOf(recent("u01", "Nguyễn An", "CECV1", millis = 100)))
            val vm = newVm()
            advanceUntilIdle()

            vm.onSearchQueryChange("Nguy")
            advanceTimeBy(600)
            advanceUntilIdle()
            assertTrue(vm.state.value.searchResults is SearchResultsState.Loaded)
            assertEquals(true, vm.state.value.isSearching)

            // Clear the box.
            vm.onSearchQueryChange("")
            advanceTimeBy(600)
            advanceUntilIdle()
            val final = vm.state.value
            assertEquals(SearchResultsState.Idle, final.searchResults)
            assertEquals(false, final.isSearching)
            // Recent list is still there (not blown away by clearing the search).
            assertEquals(1, final.recentSunners.size)
            assertEquals("u01", final.recentSunners[0].userId)
        }

    // ── Click-to-save (Behavior 1) ──────────────────────────────────

    @Test
    fun tapSearchResult_persistsToRecentList() =
        testScope.runTest {
            val vm = newVm()
            advanceUntilIdle()

            // Start with an empty recent list.
            assertTrue(vm.state.value.recentSunners.isEmpty())

            // Trigger a search so the result rows populate `displayedNodes`.
            vm.onSearchQueryChange("Nguy")
            advanceTimeBy(600)
            advanceUntilIdle()
            val loaded = vm.state.value.searchResults as SearchResultsState.Loaded
            val firstMatchUserId = loaded.matches.first().id

            // Tap the first search result. `onPromoted` is the navigation
            // lambda — capture invocation count.
            var onPromotedCalls = 0
            vm.onRowTap(firstMatchUserId) { onPromotedCalls++ }
            advanceUntilIdle()

            // Verify both legs of the contract:
            //  (a) the recent list now contains the tapped Sunner at pos 0
            //  (b) the navigation callback fired exactly once
            val recent = vm.state.value.recentSunners
            assertEquals(1, recent.size)
            assertEquals(firstMatchUserId, recent[0].userId)
            assertEquals(1, onPromotedCalls)
        }

    @Test
    fun tapRecentRow_promotesToPositionZero() =
        testScope.runTest {
            // Seed two recents — u01 newer than u02.
            recentRepo.seed(
                listOf(
                    recent("u01", "Nguyễn An", "CECV1", millis = 200),
                    recent("u02", "Trần Bình", "CECV1", millis = 100),
                ),
            )
            val vm = newVm()
            advanceUntilIdle()
            // Before tap: u01 at pos 0.
            assertEquals("u01", vm.state.value.recentSunners[0].userId)

            // Tap u02. Expectation: u02 moves to pos 0; u01 drops to pos 1.
            vm.onRowTap("u02") { /* no-op */ }
            advanceUntilIdle()
            val recent = vm.state.value.recentSunners
            assertEquals(2, recent.size)
            assertEquals("u02", recent[0].userId)
            assertEquals("u01", recent[1].userId)
        }

    @Test
    fun searchTap_thenClearBox_recentListShowsTappedSunner() =
        testScope.runTest {
            // The end-to-end flow the user described: search → tap → clear → recent shows tapped.
            val vm = newVm()
            advanceUntilIdle()
            assertTrue("starts empty", vm.state.value.recentSunners.isEmpty())

            // 1. Type to search.
            vm.onSearchQueryChange("Nguy")
            advanceTimeBy(600)
            advanceUntilIdle()
            val tapped = (vm.state.value.searchResults as SearchResultsState.Loaded).matches.first()

            // 2. Tap a search result.
            vm.onRowTap(tapped.id) { /* navigates to Profile in prod */ }
            advanceUntilIdle()

            // 3. Clear the search box.
            vm.onSearchQueryChange("")
            advanceTimeBy(600)
            advanceUntilIdle()

            // 4. Final state: search idle, recent list contains the tapped Sunner.
            val final = vm.state.value
            assertEquals(false, final.isSearching)
            assertEquals(SearchResultsState.Idle, final.searchResults)
            assertEquals(1, final.recentSunners.size)
            assertEquals(tapped.id, final.recentSunners[0].userId)
            assertEquals(tapped.fullName, final.recentSunners[0].fullName)
        }

    // ── Helpers ─────────────────────────────────────────────────────

    private fun recent(
        userId: String,
        fullName: String,
        dept: String?,
        millis: Long,
    ): RecentSunner =
        RecentSunner(
            userId = userId,
            fullName = fullName,
            departmentName = dept,
            avatarUrl = null,
            lastSearchedAtMillis = millis,
        )

    /**
     * In-memory fake repository. Mirrors [RecentSunnerRepository]
     * semantics: dedupe by userId, cap at 5, sort newest-first on
     * read.
     */
    private class FakeRecentSunnerRepository : RecentSunnerRepository {
        private val backing = MutableStateFlow<List<RecentSunner>>(emptyList())

        fun seed(items: List<RecentSunner>) {
            backing.value = items.sortedByDescending { it.lastSearchedAtMillis }
        }

        override fun observeAll(): Flow<List<RecentSunner>> = backing

        override suspend fun addOrPromote(entry: RecentSunner) {
            val current = backing.value
            val withoutDup = current.filterNot { it.userId == entry.userId }
            val updated =
                (listOf(entry.copy(lastSearchedAtMillis = System.currentTimeMillis())) + withoutDup)
                    .take(RecentSunnerRepository.MAX_ENTRIES)
            backing.value = updated
        }

        override suspend fun remove(userId: String) {
            backing.value = backing.value.filterNot { it.userId == userId }
        }

        override suspend fun clear() {
            backing.value = emptyList()
        }
    }
}
