package com.example.aiddproject.core.session

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import com.example.aiddproject.R
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

/**
 * Routing gate that observes the persisted Supabase session and dispatches the user to
 * Home (auto-login, US2) or Login. Renders a splash while the SDK is hydrating so the
 * Login screen never flashes for an already-signed-in user (TR-005, SC-002).
 *
 * - [onAuthenticated]: invoked exactly once when the session resolves successfully.
 * - [onUnauthenticated]: invoked on `Unauthenticated` or any error state (the caller
 *   may surface an "expired session" snackbar based on the reason if it cares to).
 *
 * The gate itself is stateless w.r.t. navigation — it sends one signal per terminal
 * state and otherwise renders the splash. Navigation order (popUpTo) is the caller's
 * responsibility.
 */
@Composable
fun SessionGate(
    onAuthenticated: () -> Unit,
    onUnauthenticated: () -> Unit,
    viewModel: SessionGateViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    SessionGateContent(
        state = state,
        onAuthenticated = onAuthenticated,
        onUnauthenticated = onUnauthenticated,
    )
}

@Composable
fun SessionGateContent(
    state: AuthState,
    onAuthenticated: () -> Unit,
    onUnauthenticated: () -> Unit,
) {
    LaunchedEffect(state) {
        when (state) {
            AuthState.Loading -> Unit
            AuthState.Unauthenticated -> onUnauthenticated()
            is AuthState.Authenticated -> onAuthenticated()
            is AuthState.Error -> onUnauthenticated()
        }
    }
    Splash(visible = state is AuthState.Loading)
}

/**
 * Splash placeholder shown only while [AuthState.Loading]. Mirrors the Login keyvisual
 * tone so the transition into Login (if needed) doesn't feel like a flash either.
 */
@Composable
private fun Splash(visible: Boolean) {
    if (!visible) return
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(R.drawable.bg_keyvisual),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
        CircularProgressIndicator(
            modifier = Modifier.size(40.dp),
            color = MaterialTheme.colorScheme.onPrimary,
        )
    }
}

@HiltViewModel
class SessionGateViewModel
    @Inject
    constructor(
        sessionRepository: SessionRepository,
    ) : ViewModel() {
        val state: StateFlow<AuthState> = sessionRepository.state
    }
