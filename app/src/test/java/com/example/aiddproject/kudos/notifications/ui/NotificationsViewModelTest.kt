@file:OptIn(kotlin.time.ExperimentalTime::class, kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package com.example.aiddproject.kudos.notifications.ui

import com.example.aiddproject.home.data.NotificationsSummaryRepository
import com.example.aiddproject.home.domain.NotificationsSummary
import com.example.aiddproject.kudos.notifications.data.NotificationRepository
import com.example.aiddproject.kudos.notifications.data.NotificationsCountFlow
import com.example.aiddproject.kudos.notifications.domain.NotificationItem
import com.example.aiddproject.kudos.notifications.domain.NotificationPayload
import com.example.aiddproject.kudos.notifications.domain.NotificationType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.time.Clock

/**
 * Pure JVM unit tests for [NotificationsViewModel]. Drives the VM with
 * a [FakeNotificationRepository] + real [NotificationsCountFlow] backed
 * by a stub summary repo, so the count-flow plumbing is exercised
 * end-to-end alongside the row/read-all semantics.
 */
class NotificationsViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state transitions from Loading to Loaded after observeRecent emits`() =
        runTest(dispatcher) {
            val (repo, vm) = build(seed = listOf(unread("a"), read("b")))
            assertEquals(NotificationsListState.Loading, vm.state.value.listState)
            dispatcher.scheduler.advanceUntilIdle()
            val loaded = vm.state.value.listState as NotificationsListState.Loaded
            assertEquals(2, loaded.items.size)
            assertEquals(1, repo.observeCallCount)
        }

    @Test
    fun `onRowTap on unread row fires markRead and decrements the count flow`() =
        runTest(dispatcher) {
            val (repo, vm) = build(seed = listOf(unread("a")), initialCount = 1)
            advanceUntilLoaded(vm)

            val capture = RouterCapture()
            val tapped = (vm.state.value.listState as NotificationsListState.Loaded).items.first()
            vm.onRowTap(tapped, capture.router)
            dispatcher.scheduler.advanceUntilIdle()

            assertEquals("k-a", capture.kudoDetailKudoId)
            assertEquals(listOf("a"), repo.markReadIds)
            assertEquals(0, vm.state.value.unreadCount)
            assertTrue(
                (vm.state.value.listState as NotificationsListState.Loaded).items.first { it.id == "a" }.isRead,
            )
        }

    @Test
    fun `onRowTap on already-read row routes without firing markRead`() =
        runTest(dispatcher) {
            val (repo, vm) = build(seed = listOf(read("b")), initialCount = 0)
            advanceUntilLoaded(vm)

            val capture = RouterCapture()
            val tapped = (vm.state.value.listState as NotificationsListState.Loaded).items.first()
            vm.onRowTap(tapped, capture.router)
            dispatcher.scheduler.advanceUntilIdle()

            assertEquals("k-b", capture.kudoDetailKudoId)
            assertTrue(repo.markReadIds.isEmpty())
            assertEquals(0, vm.state.value.unreadCount)
        }

    @Test
    fun `onReadAll happy path clears unread and decrements count to zero`() =
        runTest(dispatcher) {
            val (repo, vm) = build(seed = listOf(unread("a"), unread("b"), read("c")), initialCount = 2)
            advanceUntilLoaded(vm)

            vm.onReadAll()
            dispatcher.scheduler.advanceUntilIdle()

            assertEquals(0, vm.state.value.unreadCount)
            val items = (vm.state.value.listState as NotificationsListState.Loaded).items
            assertTrue(items.all { it.isRead })
            assertEquals(1, repo.markAllReadCount)
        }

    @Test
    fun `onReadAll on empty unread is a no-op and fires no API call`() =
        runTest(dispatcher) {
            val (repo, vm) = build(seed = listOf(read("a")), initialCount = 0)
            advanceUntilLoaded(vm)

            vm.onReadAll()
            dispatcher.scheduler.advanceUntilIdle()

            assertEquals(0, repo.markAllReadCount)
        }

    @Test
    fun `onReadAll rollback restores items and unread count on failure`() =
        runTest(dispatcher) {
            val (repo, vm) = build(seed = listOf(unread("a"), unread("b")), initialCount = 2)
            advanceUntilLoaded(vm)

            repo.markAllReadResult = Result.failure(RuntimeException("network"))
            vm.onReadAll()
            dispatcher.scheduler.advanceUntilIdle()

            assertEquals(2, vm.state.value.unreadCount)
            val items = (vm.state.value.listState as NotificationsListState.Loaded).items
            assertTrue(items.none { it.isRead })
            assertNotNull(vm.state.value.snackbar)
        }

    @Test
    fun `onConsumeSnackbar clears the snackbar slot`() =
        runTest(dispatcher) {
            val (repo, vm) = build(seed = listOf(unread("a")), initialCount = 1)
            advanceUntilLoaded(vm)
            repo.markAllReadResult = Result.failure(RuntimeException("x"))
            vm.onReadAll()
            dispatcher.scheduler.advanceUntilIdle()
            assertNotNull(vm.state.value.snackbar)

            vm.onConsumeSnackbar()
            assertNull(vm.state.value.snackbar)
        }

    @Test
    fun `empty seed emits Empty list state`() =
        runTest(dispatcher) {
            val (_, vm) = build(seed = emptyList())
            dispatcher.scheduler.advanceUntilIdle()
            assertEquals(NotificationsListState.Empty, vm.state.value.listState)
        }

    // ── Helpers ─────────────────────────────────────────────────────

    private fun TestScope.advanceUntilLoaded(vm: NotificationsViewModel) {
        dispatcher.scheduler.advanceUntilIdle()
        val state = vm.state.value.listState
        assertFalse(
            "expected non-Loading state, got $state",
            state is NotificationsListState.Loading,
        )
    }

    private fun build(
        seed: List<NotificationItem>,
        initialCount: Int = 0,
    ): Pair<FakeNotificationRepository, NotificationsViewModel> {
        val repo = FakeNotificationRepository(seed)
        val stubSummary =
            object : NotificationsSummaryRepository {
                override suspend fun get(): Result<NotificationsSummary> =
                    Result.success(NotificationsSummary(unreadCount = initialCount))
            }
        val countFlow = NotificationsCountFlow(stubSummary)
        val vm = NotificationsViewModel(repo, countFlow)
        return repo to vm
    }

    private fun unread(id: String): NotificationItem =
        NotificationItem(
            id = id,
            type = NotificationType.KUDOS_RECEIVED,
            isRead = false,
            createdAt = Clock.System.now(),
            payload = NotificationPayload.KudoRef(kudoId = "k-$id", isAnonymous = false),
            displayBody = "body $id",
        )

    private fun read(id: String): NotificationItem = unread(id).copy(isRead = true)
}

/**
 * Captures which routing callback fired and with what args. The VM's
 * `onRowTap` calls into a [NotificationRouter] which fans out to one
 * of four destinations; the test reads the captured args to verify
 * routing decisions per type.
 */
private class RouterCapture {
    var kudoDetailKudoId: String? = null
    var kudoDetailAnon: Boolean? = null
    var secretBoxCount: Int = 0
    var profileCount: Int = 0
    var adminReviewCount: Int = 0

    val router: NotificationRouter =
        NotificationRouter(
            onNavigateToKudoDetail = { id, anon ->
                kudoDetailKudoId = id
                kudoDetailAnon = anon
            },
            onNavigateToSecretBoxOpen = { secretBoxCount += 1 },
            onNavigateToProfile = { profileCount += 1 },
            onNavigateToAdminReview = { adminReviewCount += 1 },
        )
}

/**
 * Minimal fake — holds a mutable seed, records mutations, returns the
 * `markAllReadResult` override if set. Mirrors the repository contract
 * so the VM's optimistic + rollback paths can be exercised.
 */
private class FakeNotificationRepository(seed: List<NotificationItem>) : NotificationRepository {
    private val state = MutableStateFlow(seed)
    var observeCallCount: Int = 0
    val markReadIds: MutableList<String> = mutableListOf()
    var markAllReadCount: Int = 0
    var markAllReadResult: Result<Int>? = null

    override fun observeRecent(): Flow<List<NotificationItem>> {
        observeCallCount += 1
        return state.asStateFlow()
    }

    override suspend fun loadMore(cursor: String?): Result<NotificationRepository.Page> =
        Result.success(NotificationRepository.Page(emptyList(), null))

    override suspend fun markRead(id: String): Result<Unit> {
        markReadIds += id
        state.update { current -> current.map { if (it.id == id) it.copy(isRead = true) else it } }
        return Result.success(Unit)
    }

    override suspend fun markAllRead(): Result<Int> {
        markAllReadCount += 1
        markAllReadResult?.let { return it }
        val flipped = state.value.count { !it.isRead }
        state.update { current -> current.map { it.copy(isRead = true) } }
        return Result.success(flipped)
    }
}
