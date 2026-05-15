@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.example.aiddproject.home.ui

import com.example.aiddproject.core.locale.Language
import com.example.aiddproject.core.locale.LanguagePreferenceRepository
import com.example.aiddproject.home.data.AwardsRepository
import com.example.aiddproject.home.data.KudosSummaryRepository
import com.example.aiddproject.home.data.NotificationsSummaryRepository
import com.example.aiddproject.home.domain.Award
import com.example.aiddproject.home.domain.CountdownEngine
import com.example.aiddproject.home.domain.KudosSummary
import com.example.aiddproject.home.domain.NotificationsSummary
import com.example.aiddproject.home.domain.SaaCountdownTarget
import com.example.aiddproject.home.domain.states.AwardsState
import com.example.aiddproject.home.domain.states.CountdownState
import com.example.aiddproject.home.domain.states.KudosState
import com.example.aiddproject.kudos.notifications.data.NotificationsCountFlow
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {
    private val awardsRepository: AwardsRepository = mockk()
    private val kudosRepository: KudosSummaryRepository = mockk()
    private val languageRepository: LanguagePreferenceRepository = mockk()

    /**
     * The summary repository is the upstream source for the shared
     * `NotificationsCountFlow` — it's no longer injected directly into
     * HomeViewModel after the Notifications screen migration (spec
     * `_b68CBWKl5`). HomeViewModel only triggers a refresh through the
     * singleton; this mock lets us verify the refresh count + drive
     * different unread-count seeds.
     */
    private val summaryRepository: NotificationsSummaryRepository = mockk()
    private val notificationsCountFlow get() = NotificationsCountFlow(summaryRepository)

    /** Pre-event clock so the countdown snapshot is non-zero in every test. */
    private val fixedClock =
        object : Clock {
            override fun now(): Instant = SaaCountdownTarget - 5.days - 3.minutes
        }

    private val countdownEngine = CountdownEngine(fixedClock)

    private val sampleAward = Award(id = "a1", name = "Top Talent Award", thumbnailUrl = null, sortOrder = 0)
    private val sampleKudos =
        KudosSummary(
            isKudosAvailable = true,
            bannerImageUrl = null,
            badgeText = "FUN",
            descriptionText = "Recognise teammates who carried you.",
        )

    @Before
    fun setUp() {
        coEvery { languageRepository.language } returns flowOf(Language.VN)
        coEvery { awardsRepository.list() } returns Result.success(listOf(sampleAward))
        coEvery { kudosRepository.get() } returns Result.success(sampleKudos)
        coEvery { summaryRepository.get() } returns Result.success(NotificationsSummary(unreadCount = 2))
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun newViewModel(countFlow: NotificationsCountFlow = notificationsCountFlow) =
        HomeViewModel(
            awardsRepository = awardsRepository,
            kudosRepository = kudosRepository,
            countdownEngine = countdownEngine,
            notificationsCountFlow = countFlow,
            languageRepository = languageRepository,
        )

    @Test
    fun `awards section state is Loading synchronously at construction (TR-003)`() =
        runTest {
            // StandardTestDispatcher with no `runCurrent()` — init { refreshAll() } is queued
            // but never dispatched, so we observe the truly initial state of the StateFlow.
            Dispatchers.setMain(StandardTestDispatcher(testScheduler))
            val vm = newViewModel()

            val state = vm.uiState.value
            assertEquals(AwardsState.Loading, state.awards)
            assertEquals(KudosState.Loading, state.kudos)
            assertEquals(0, state.unreadCount)
            assertTrue(state.countdown.isPreEvent)
        }

    @Test
    fun `refreshAll populates each section after coroutines run`() =
        runTest {
            Dispatchers.setMain(StandardTestDispatcher(testScheduler))
            val vm = newViewModel()

            // Run the init refreshAll launches.
            testScheduler.runCurrent()

            val state = vm.uiState.value
            assertEquals(AwardsState.Populated(listOf(sampleAward)), state.awards)
            assertEquals(KudosState.Loaded(sampleKudos), state.kudos)
            assertEquals(2, state.unreadCount)
        }

    @Test
    fun `awards empty result maps to AwardsState_Empty`() =
        runTest {
            coEvery { awardsRepository.list() } returns Result.success(emptyList())
            val vm = newViewModel()
            assertEquals(AwardsState.Empty, vm.uiState.value.awards)
        }

    @Test
    fun `awards failure maps to AwardsState_Error with the throwable's message`() =
        runTest {
            coEvery { awardsRepository.list() } returns Result.failure(RuntimeException("boom"))
            val vm = newViewModel()
            assertEquals(AwardsState.Error("boom"), vm.uiState.value.awards)
        }

    @Test
    fun `onRetryAwards re-fires awards fetch — Error then success transitions to Populated (US2)`() =
        runTest {
            // First call fails, retry call succeeds.
            coEvery { awardsRepository.list() } returnsMany
                listOf(
                    Result.failure(RuntimeException("transient")),
                    Result.success(listOf(sampleAward)),
                )
            val vm = newViewModel()
            assertEquals(AwardsState.Error("transient"), vm.uiState.value.awards)

            vm.onRetryAwards()
            assertEquals(AwardsState.Populated(listOf(sampleAward)), vm.uiState.value.awards)
        }

    @Test
    fun `onRetryAwards reloads only awards — kudos and notifications are not re-fetched`() =
        runTest {
            coEvery { awardsRepository.list() } returns Result.success(listOf(sampleAward))
            val vm = newViewModel()

            vm.onRetryAwards()

            coVerify(exactly = 2) { awardsRepository.list() } // initial + retry
            coVerify(exactly = 1) { kudosRepository.get() } // unchanged
            coVerify(exactly = 1) { summaryRepository.get() } // unchanged
        }

    @Test
    fun `kudos with isKudosAvailable=false maps to KudosState_Hidden`() =
        runTest {
            coEvery { kudosRepository.get() } returns
                Result.success(sampleKudos.copy(isKudosAvailable = false))
            val vm = newViewModel()
            assertEquals(KudosState.Hidden, vm.uiState.value.kudos)
        }

    @Test
    fun `kudos failure maps to KudosState_Error`() =
        runTest {
            coEvery { kudosRepository.get() } returns Result.failure(IllegalStateException("rpc 500"))
            val vm = newViewModel()
            assertEquals(KudosState.Error("rpc 500"), vm.uiState.value.kudos)
        }

    @Test
    fun `notifications failure leaves unread count at zero - bell still tappable`() =
        runTest {
            coEvery { summaryRepository.get() } returns Result.failure(RuntimeException("offline"))
            val vm = newViewModel()
            // refreshFromServer's failure path is silent (offline tolerance) —
            // the count stays at its prior value, which is the initial 0.
            assertEquals(0, vm.uiState.value.unreadCount)
        }

    @Test
    fun `notifications zero unread propagates to unreadCount`() =
        runTest {
            coEvery { summaryRepository.get() } returns
                Result.success(NotificationsSummary(unreadCount = 0))
            val vm = newViewModel()
            assertEquals(0, vm.uiState.value.unreadCount)
        }

    @Test
    fun `unreadCount mirrors NotificationsCountFlow mutations after the Notifications screen acts`() =
        runTest {
            // Simulates: user opens Notifications → marks rows read → returns
            // to Home, where the bell badge must reflect the new count without
            // a server refresh. The singleton mutations propagate via the
            // combine pipeline.
            val countFlow = notificationsCountFlow
            val vm = newViewModel(countFlow = countFlow)
            assertEquals(2, vm.uiState.value.unreadCount)

            countFlow.decrement()
            assertEquals(1, vm.uiState.value.unreadCount)

            countFlow.setTo(0)
            assertEquals(0, vm.uiState.value.unreadCount)
        }

    @Test
    fun `language flow propagates persisted preference into uiState`() =
        runTest {
            coEvery { languageRepository.language } returns flowOf(Language.EN)
            val vm = newViewModel()
            assertEquals(Language.EN, vm.uiState.value.language)
        }

    @Test
    fun `startCountdown drives countdownState - stopCountdown cancels the ticker (TR-004)`() =
        runTest {
            Dispatchers.setMain(StandardTestDispatcher(testScheduler))
            val mutableClock =
                object : Clock {
                    var now: Instant = SaaCountdownTarget - 5.days - 3.minutes

                    override fun now(): Instant = now
                }
            val engine = CountdownEngine(mutableClock)
            val vm =
                HomeViewModel(
                    awardsRepository = awardsRepository,
                    kudosRepository = kudosRepository,
                    countdownEngine = engine,
                    notificationsCountFlow = notificationsCountFlow,
                    languageRepository = languageRepository,
                )

            // Initial snapshot reads off the same clock — no dispatch yet so the init's
            // refreshAll launches sit queued without affecting the countdown slot.
            assertEquals(
                CountdownState(days = 5, hours = 0, minutes = 3, isPreEvent = true),
                vm.uiState.value.countdown,
            )

            vm.startCountdown()
            mutableClock.now = SaaCountdownTarget - 5.days - 1.minutes
            advanceTimeBy(1.seconds)
            testScheduler.runCurrent()
            assertEquals(1, vm.uiState.value.countdown.minutes)

            vm.stopCountdown()
            mutableClock.now = SaaCountdownTarget + 1.seconds
            assertEquals(1, vm.uiState.value.countdown.minutes)
        }

    @Test
    fun `startCountdown is idempotent - second call does not crash`() =
        runTest {
            Dispatchers.setMain(StandardTestDispatcher(testScheduler))
            val vm = newViewModel()
            vm.startCountdown()
            vm.startCountdown()
            testScheduler.runCurrent()
            vm.stopCountdown()
        }

    @Test
    fun `language StateFlow is collected eagerly so first paint matches stored preference`() =
        runTest {
            // Use a hot StateFlow upstream so we can verify that `stateIn(Eagerly)` propagates.
            val upstream = MutableStateFlow(Language.VN)
            coEvery { languageRepository.language } returns upstream
            val vm = newViewModel()
            assertEquals(Language.VN, vm.uiState.value.language)

            upstream.value = Language.EN
            assertEquals(Language.EN, vm.uiState.value.language)
        }
}
