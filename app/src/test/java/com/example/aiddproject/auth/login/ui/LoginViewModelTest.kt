package com.example.aiddproject.auth.login.ui

import android.content.Context
import app.cash.turbine.test
import com.example.aiddproject.auth.login.domain.SignInOutcome
import com.example.aiddproject.auth.login.domain.SignInWithGoogleUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {
    private val signInUseCase: SignInWithGoogleUseCase = mockk()
    private val activityContext: Context = mockk(relaxed = true)
    private lateinit var viewModel: LoginViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        viewModel = LoginViewModel(signInUseCase)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is idle, no error, play services available`() =
        runTest {
            val s = viewModel.state.value
            assertFalse(s.isLoading)
            assertEquals(null, s.error)
            assertTrue(s.playServicesAvailable)
        }

    @Test
    fun `tap drives loading then idle on Success and emits NavigateToHome`() =
        runTest {
            coEvery { signInUseCase(activityContext) } returns SignInOutcome.Success

            viewModel.events.test {
                viewModel.onSignInTap(activityContext)
                assertEquals(LoginEvent.NavigateToHome, awaitItem())
                cancelAndConsumeRemainingEvents()
            }
            assertFalse(viewModel.state.value.isLoading)
        }

    @Test
    fun `NotASunner outcome emits NavigateToAccessDenied`() =
        runTest {
            coEvery { signInUseCase(activityContext) } returns SignInOutcome.NotASunner

            viewModel.events.test {
                viewModel.onSignInTap(activityContext)
                assertEquals(LoginEvent.NavigateToAccessDenied, awaitItem())
                cancelAndConsumeRemainingEvents()
            }
            assertFalse(viewModel.state.value.isLoading)
        }

    @Test
    fun `Cancelled outcome silently returns to idle without emitting any event`() =
        runTest {
            coEvery { signInUseCase(activityContext) } returns SignInOutcome.Cancelled

            viewModel.events.test {
                viewModel.onSignInTap(activityContext)
                expectNoEvents()
                cancelAndConsumeRemainingEvents()
            }
            val s = viewModel.state.value
            assertFalse(s.isLoading)
            assertEquals(null, s.error)
        }

    @Test
    fun `Failure outcome stores error and emits ShowError`() =
        runTest {
            coEvery { signInUseCase(activityContext) } returns
                SignInOutcome.Failure(LoginError.Network)

            viewModel.events.test {
                viewModel.onSignInTap(activityContext)
                assertEquals(LoginEvent.ShowError(LoginError.Network), awaitItem())
                cancelAndConsumeRemainingEvents()
            }
            val s = viewModel.state.value
            assertFalse(s.isLoading)
            assertEquals(LoginError.Network, s.error)
        }

    @Test
    fun `double-tap during loading is suppressed - exactly one auth request flies (FR-003)`() =
        runTest {
            // Use a StandardTestDispatcher and manually drive it so we can pause the in-flight
            // call between the two taps and verify the second tap is dropped.
            Dispatchers.setMain(StandardTestDispatcher(testScheduler))
            viewModel = LoginViewModel(signInUseCase)

            val gate = CompletableDeferred<SignInOutcome>()
            coEvery { signInUseCase(activityContext) } coAnswers { gate.await() }

            viewModel.onSignInTap(activityContext)
            testScheduler.runCurrent()
            assertTrue(viewModel.state.value.isLoading)

            // Tap again while still loading — must be ignored before any work is launched.
            viewModel.onSignInTap(activityContext)
            testScheduler.runCurrent()

            gate.complete(SignInOutcome.Success)
            testScheduler.runCurrent()

            coVerify(exactly = 1) { signInUseCase(activityContext) }
            assertFalse(viewModel.state.value.isLoading)
        }

    @Test
    fun `tap when play services unavailable emits ShowError without invoking use case`() =
        runTest {
            viewModel.setPlayServicesAvailable(false)

            viewModel.events.test {
                viewModel.onSignInTap(activityContext)
                assertEquals(
                    LoginEvent.ShowError(LoginError.PlayServicesUnavailable),
                    awaitItem(),
                )
                cancelAndConsumeRemainingEvents()
            }
            coVerify(exactly = 0) { signInUseCase(any()) }
        }

    @Test
    fun `state transitions are loading then idle with success outcome`() =
        runTest {
            Dispatchers.setMain(StandardTestDispatcher(testScheduler))
            viewModel = LoginViewModel(signInUseCase)

            val gate = CompletableDeferred<SignInOutcome>()
            coEvery { signInUseCase(activityContext) } coAnswers { gate.await() }

            viewModel.state.test {
                assertEquals(LoginUiState(), awaitItem())

                viewModel.onSignInTap(activityContext)
                testScheduler.runCurrent()
                assertEquals(
                    LoginUiState(isLoading = true, error = null, playServicesAvailable = true),
                    awaitItem(),
                )

                gate.complete(SignInOutcome.Success)
                testScheduler.runCurrent()
                assertEquals(LoginUiState(), awaitItem())

                cancelAndConsumeRemainingEvents()
            }
        }

    @Test
    fun `error from previous tap is cleared when next tap starts`() =
        runTest {
            // First tap fails.
            coEvery { signInUseCase(activityContext) } returns
                SignInOutcome.Failure(LoginError.Network)
            viewModel.onSignInTap(activityContext)
            assertEquals(LoginError.Network, viewModel.state.value.error)

            // Second tap succeeds — error cleared as soon as loading flips on.
            coEvery { signInUseCase(activityContext) } returns SignInOutcome.Success
            viewModel.onSignInTap(activityContext)
            assertEquals(null, viewModel.state.value.error)
        }

    @Test
    fun `gate ordering — loading flag is set BEFORE use case runs`() =
        runTest {
            Dispatchers.setMain(StandardTestDispatcher(testScheduler))
            viewModel = LoginViewModel(signInUseCase)

            val checkpoints = mutableListOf<String>()
            coEvery { signInUseCase(activityContext) } coAnswers {
                checkpoints += "useCase(isLoading=${viewModel.state.value.isLoading})"
                SignInOutcome.Success
            }

            viewModel.onSignInTap(activityContext)
            testScheduler.runCurrent()

            assertEquals(listOf("useCase(isLoading=true)"), checkpoints)
        }
}
