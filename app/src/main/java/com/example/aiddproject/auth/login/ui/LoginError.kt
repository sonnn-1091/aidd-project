package com.example.aiddproject.auth.login.ui

import androidx.annotation.StringRes
import com.example.aiddproject.R

/**
 * One variant per error string key in spec § Localized Copy. Each variant carries the
 * Android resource id for its localized snackbar message — the screen does
 * `stringResource(error.messageRes)` and never sees the raw text.
 *
 * `NotASunner` is intentionally NOT modeled here: account-not-authorized is a navigation
 * outcome (route to Access denied), not a Login-screen message. See [SignInWithGoogleUseCase]
 * outcome types.
 */
sealed class LoginError(
    @StringRes val messageRes: Int,
) {
    data object Network : LoginError(R.string.error_oauth_network)

    data object AccountDisabled : LoginError(R.string.error_oauth_account_disabled)

    data object OAuthCodeExpired : LoginError(R.string.error_oauth_code_expired)

    data object PlayServicesUnavailable : LoginError(R.string.error_oauth_play_services)

    data object SessionExpired : LoginError(R.string.error_oauth_session_expired)

    data object Generic : LoginError(R.string.error_oauth_generic)
}
