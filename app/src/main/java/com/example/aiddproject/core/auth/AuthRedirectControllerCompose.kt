package com.example.aiddproject.core.auth

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

/**
 * Compose-side accessor for the [AuthRedirectController] singleton. The
 * controller is a `@Singleton` (not a ViewModel), so composables resolve it
 * through a Hilt entry-point off the application context — the standard
 * pattern for reaching SingletonComponent bindings from a composable that
 * isn't a `@HiltViewModel`.
 *
 * Used by both `AppNavigation` (collects events to drive 401/403 navigation)
 * and `LoginScreen` (collects `sessionExpiredHint` to surface the
 * `error_oauth_session_expired` snackbar after a 401 bounce).
 */
@Composable
fun rememberAuthRedirectController(): AuthRedirectController {
    val context = LocalContext.current.applicationContext
    return EntryPointAccessors
        .fromApplication(context, AuthRedirectControllerEntryPoint::class.java)
        .authRedirectController()
}

@EntryPoint
@InstallIn(SingletonComponent::class)
internal interface AuthRedirectControllerEntryPoint {
    fun authRedirectController(): AuthRedirectController
}
