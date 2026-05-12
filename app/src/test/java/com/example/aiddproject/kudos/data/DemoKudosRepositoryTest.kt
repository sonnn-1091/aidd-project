package com.example.aiddproject.kudos.data

import com.example.aiddproject.kudos.domain.KudosFilter
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Pinning test for the DEMO fixture (spec § Phase 2 T027).
 *
 * Locks the seed shape — list-highlight ordering, pagination,
 * spotlight total, personal stats canonical values, secret-box
 * one-shot behavior — so any future drift surfaces in CI before
 * landing in QA. Failing this test means a Phase 3 ViewModel test
 * is about to flake.
 */
class DemoKudosRepositoryTest {
    private lateinit var repo: DemoKudosRepository

    @Before
    fun setUp() {
        repo = DemoKudosRepository()
    }

    @Test
    fun list_highlight_returns_top_five_sorted_by_heart_count_desc() =
        runTest {
            val highlight = repo.listHighlight(KudosFilter()).getOrThrow()
            assertEquals(5, highlight.size)
            val counts = highlight.map { it.heartCount }
            assertEquals(counts.sortedDescending(), counts)
        }

    @Test
    fun list_kudos_paginates() =
        runTest {
            val firstPage = repo.listKudos(KudosFilter(), page = 0, limit = 5).getOrThrow()
            assertEquals(5, firstPage.items.size)
            assertTrue(firstPage.hasMore)
            assertEquals(1, firstPage.nextPage)

            val secondPage = repo.listKudos(KudosFilter(), page = 1, limit = 5).getOrThrow()
            assertEquals(5, secondPage.items.size)
            assertFalse(secondPage.hasMore)
            assertNull(secondPage.nextPage)
        }

    @Test
    fun detail_returns_match() =
        runTest {
            val first = repo.listHighlight(KudosFilter()).getOrThrow().first()
            val detail = repo.detail(first.id).getOrThrow()
            assertEquals(first.id, detail.id)
        }

    @Test
    fun detail_unknown_id_fails() =
        runTest {
            val result = repo.detail("nonexistent")
            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull() is NoSuchElementException)
        }

    @Test
    fun reactions_succeed() =
        runTest {
            assertTrue(repo.addReaction("k01").isSuccess)
            assertTrue(repo.removeReaction("k01").isSuccess)
        }

    @Test
    fun list_hashtags_returns_seed() =
        runTest {
            val hashtags = repo.listHashtags().getOrThrow()
            assertEquals(5, hashtags.size)
        }

    @Test
    fun list_departments_returns_seed() =
        runTest {
            val departments = repo.listDepartments().getOrThrow()
            assertEquals(5, departments.size)
        }

    @Test
    fun spotlight_graph_carries_total_count() =
        runTest {
            val graph = repo.loadSpotlightGraph().getOrThrow()
            assertEquals(388, graph.totalKudosCount)
            assertEquals(8, graph.nodes.size)
            assertTrue(graph.edges.isNotEmpty())
        }

    @Test
    fun personal_stats_canonical_values() =
        runTest {
            val stats = repo.personalStats().getOrThrow()
            assertEquals(42, stats.kudosReceived)
            assertEquals(18, stats.kudosSent)
            assertEquals(156, stats.heartsReceived)
            assertEquals(3, stats.secretBoxesOpened)
            assertEquals(2, stats.secretBoxesUnopened)
        }

    @Test
    fun system_flags_default_off() =
        runTest {
            val flags = repo.systemFlags().getOrThrow()
            assertFalse(flags.specialDayActive)
            assertFalse(flags.x2BonusActive)
        }

    @Test
    fun secret_box_open_consumes_pending() =
        runTest {
            val initial = repo.nextUnopenedBox().getOrThrow()
            assertNotNull(initial)
            val reward = repo.openSecretBox(initial!!.id).getOrThrow()
            assertEquals(initial.id, reward.boxId)
            assertNull(repo.nextUnopenedBox().getOrThrow())
        }

    @Test
    fun top_ten_recipients_seed() =
        runTest {
            val recipients = repo.listRecentGiftRecipients().getOrThrow()
            assertEquals(8, recipients.size)
        }
}
