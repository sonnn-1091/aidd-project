package com.example.aiddproject.auth.login.ui

/**
 * Screen-local state owned by [LoginViewModel].
 *
 * - [isLoading]: true while the OAuth + Sunner-verification chain is in flight. Drives the
 *   spinner inside the CTA and gates double-tap suppression (FR-003, FR-004).
 * - [error]: the most recent terminal failure for surfacing copy via `stringResource`.
 *   Cleared on the next successful tap. Snackbar one-shots are emitted through a separate
 *   `SharedFlow<LoginError>` so they survive recomposition without being re-shown.
 * - [playServicesAvailable]: false on devices missing / outdated Google Play Services.
 *   Disables the CTA and surfaces an actionable error (Phase 7 wires the runtime check).
 */
data class LoginUiState(
    val isLoading: Boolean = false,
    val error: LoginError? = null,
    val playServicesAvailable: Boolean = true,
)
