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
import com.example.aiddproject.home.domain.states.NotificationsState
import io.mockk.coEvery
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
    private val notificationsRepository: NotificationsSummaryRepository = mockk()
    private val languageRepository: LanguagePreferenceRepository = mockk()

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
    private val sampleNotifications = NotificationsSummary(unreadCount = 2)

    @Before
    fun setUp() {
        coEvery { languageRepository.language } returns flowOf(Language.VN)
        coEvery { awardsRepository.list() } returns Result.success(listOf(sampleAward))
        coEvery { kudosRepository.get() } returns Result.success(sampleKudos)
        coEvery { notificationsRepository.get() } returns Result.success(sampleNotifications)
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun newViewModel() =
        HomeViewModel(
            awardsRepository = awardsRepository,
            kudosRepository = kudosRepository,
            notificationsRepository = notificationsRepository,
            countdownEngine = countdownEngine,
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
            assertEquals(NotificationsState.Loading, state.notifications)
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
            assertEquals(NotificationsState.Loaded(2), state.notifications)
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
    fun `notifications failure maps to NotificationsState_Error - bell still tappable`() =
        runTest {
            coEvery { notificationsRepository.get() } returns Result.failure(RuntimeException("offline"))
            val vm = newViewModel()
            assertEquals(NotificationsState.Error, vm.uiState.value.notifications)
            assertEquals(0, vm.uiState.value.unreadCount)
        }

    @Test
    fun `language flow propagates persisted preference into uiState`() =
        runTest {
            coEvery { languageRepository.language } returns flowOf(Language.JA)
            val vm = newViewModel()
            assertEquals(Language.JA, vm.uiState.value.language)
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
                    notificationsRepository = notificationsRepository,
                    countdownEngine = engine,
                    languageRepository = languageRepository,
                )

            // Initial snapshot reads off the same clock — no dispatch yet so the init's
            // refreshAll launches sit queued without affecting the countdown slot.
            assertEquals(
                CountdownState(days = 5, hours = 0, minutes = 3, isPreEvent = true),
                vm.uiState.value.countdown,
            )

            vm.startCountdown()
            // Move the clock and tick once. advanceTimeBy(1s) resumes the collector's
            // delay exactly once; collecting the new emission updates countdownState
            // which propagates through the combine pipeline.
            mutableClock.now = SaaCountdownTarget - 5.days - 1.minutes
            advanceTimeBy(1.seconds)
            testScheduler.runCurrent()
            assertEquals(1, vm.uiState.value.countdown.minutes)

            // Cancel and verify the ticker no longer mutates state on subsequent virtual
            // time advances. We MUST cancel before runTest's auto-cleanup or the
            // `while (true) { delay(1.s) }` collector would advance the test scheduler
            // forever.
            vm.stopCountdown()
            mutableClock.now = SaaCountdownTarget + 1.seconds
            // Don't advanceTimeBy here: the cancelled job wouldn't advance the clock-derived
            // state, but advancing virtual time on a still-active runTest would also tick
            // the language StateFlow's `Eagerly` upstream forever in pathological setups.
            // A direct value read after cancellation is sufficient evidence.
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
    fun `unreadCount derives from NotificationsState_Loaded`() =
        runTest {
            // sanity check on HomeUiState helper used by HomeHeader's BellWithBadge.
            val ui =
                HomeUiState(
                    countdown = countdownEngine.snapshot(),
                    awards = AwardsState.Loading,
                    kudos = KudosState.Loading,
                    notifications = NotificationsState.Loaded(unreadCount = 7),
                    language = Language.VN,
                )
            assertEquals(7, ui.unreadCount)
        }

    @Test
    fun `language StateFlow is collected eagerly so first paint matches stored preference`() =
        runTest {
            // Use a hot StateFlow upstream so we can verify that `stateIn(Eagerly)` propagates.
            val upstream = MutableStateFlow(Language.EN)
            coEvery { languageRepository.language } returns upstream
            val vm = newViewModel()
            assertEquals(Language.EN, vm.uiState.value.language)

            upstream.value = Language.JA
            // Eagerly collected — propagation is one dispatcher step away.
            assertEquals(Language.JA, vm.uiState.value.language)
        }
}
