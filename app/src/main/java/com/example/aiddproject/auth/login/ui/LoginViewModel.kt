package com.example.aiddproject.auth.login.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aiddproject.auth.login.domain.SignInOutcome
import com.example.aiddproject.auth.login.domain.SignInWithGoogleUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * One-shot navigation / snackbar events emitted by [LoginViewModel].
 *
 * Kept separate from [LoginUiState] so they don't replay on configuration change — the
 * snackbar shouldn't pop again every time the activity rotates.
 */
sealed interface LoginEvent {
    data object NavigateToHome : LoginEvent

    data object NavigateToAccessDenied : LoginEvent

    data class ShowError(
        val error: LoginError,
    ) : LoginEvent
}

/**
 * Holds [LoginUiState] and dispatches one-shot [LoginEvent]s in response to
 * `onSignInTap`. The use case ([SignInWithGoogleUseCase]) does the actual work; this VM
 * only translates outcomes into UI signals.
 *
 * - **Double-tap suppression (FR-003)**: a tap while `isLoading == true` is ignored before
 *   any work is launched, so exactly one auth request flies regardless of how rapidly the
 *   user taps.
 * - **Cancellation**: user-cancelled OAuth resolves silently (no snackbar) per spec edge cases.
 */
@HiltViewModel
class LoginViewModel
    @Inject
    constructor(
        private val signInWithGoogle: SignInWithGoogleUseCase,
    ) : ViewModel() {
        private val _state = MutableStateFlow(LoginUiState())
        val state: StateFlow<LoginUiState> = _state.asStateFlow()

        private val _events = MutableSharedFlow<LoginEvent>(replay = 0, extraBufferCapacity = 1)
        val events: SharedFlow<LoginEvent> = _events.asSharedFlow()

        fun onSignInTap(activityContext: Context) {
            if (_state.value.isLoading) return
            if (!_state.value.playServicesAvailable) {
                _events.tryEmit(LoginEvent.ShowError(LoginError.PlayServicesUnavailable))
                return
            }
            viewModelScope.launch {
                _state.update { it.copy(isLoading = true, error = null) }
                when (val outcome = signInWithGoogle(activityContext)) {
                    SignInOutcome.Success -> {
                        _state.update { it.copy(isLoading = false) }
                        _events.emit(LoginEvent.NavigateToHome)
                    }
                    SignInOutcome.NotASunner -> {
                        _state.update { it.copy(isLoading = false) }
                        _events.emit(LoginEvent.NavigateToAccessDenied)
                    }
                    SignInOutcome.Cancelled -> {
                        _state.update { it.copy(isLoading = false) }
                    }
                    is SignInOutcome.Failure -> {
                        _state.update { it.copy(isLoading = false, error = outcome.error) }
                        _events.emit(LoginEvent.ShowError(outcome.error))
                    }
                }
            }
        }

        fun setPlayServicesAvailable(available: Boolean) {
            _state.update { it.copy(playServicesAvailable = available) }
        }
    }
