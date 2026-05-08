package com.example.aiddproject.auth.login.domain

import android.content.Context
import com.example.aiddproject.auth.login.data.AuthRepository
import com.example.aiddproject.auth.login.data.GoogleCredentialProvider
import com.example.aiddproject.auth.login.data.GoogleSignInCancelledException
import com.example.aiddproject.auth.login.data.NoGoogleAccountException
import com.example.aiddproject.auth.login.ui.LoginError
import java.io.IOException
import javax.inject.Inject

/**
 * Outcome of a single Sign-in-with-Google attempt orchestrated by [SignInWithGoogleUseCase].
 *
 * - [Success]: Supabase session minted AND the Sunner membership check passed → navigate to Home.
 * - [NotASunner]: Authenticated at Google + Supabase, but no `users` row → already signed
 *   out by [VerifySunnerMembershipUseCase]; route to Access denied (FR-005).
 * - [Cancelled]: user dismissed the Google account chooser — silent return; no snackbar.
 * - [Failure]: any other terminal error, classified into a [LoginError] for snackbar display.
 */
sealed interface SignInOutcome {
    data object Success : SignInOutcome

    data object NotASunner : SignInOutcome

    data object Cancelled : SignInOutcome

    data class Failure(
        val error: LoginError,
    ) : SignInOutcome
}

/**
 * Drives the full sign-in chain:
 *   Credential Manager → Supabase `signInWithIdToken` → `users` membership check.
 *
 * All side effects are surfaced via [SignInOutcome]; the caller (LoginViewModel) decides
 * whether to navigate or emit a snackbar.
 */
class SignInWithGoogleUseCase
    @Inject
    constructor(
        private val credentialProvider: GoogleCredentialProvider,
        private val authRepository: AuthRepository,
        private val verifySunnerMembership: VerifySunnerMembershipUseCase,
    ) {
        suspend operator fun invoke(activityContext: Context): SignInOutcome {
            val tokenResult = credentialProvider.getIdToken(activityContext)
            tokenResult.exceptionOrNull()?.let { error ->
                return when (error) {
                    is GoogleSignInCancelledException -> SignInOutcome.Cancelled
                    is NoGoogleAccountException -> SignInOutcome.Failure(LoginError.PlayServicesUnavailable)
                    is IOException -> SignInOutcome.Failure(LoginError.Network)
                    else -> SignInOutcome.Failure(LoginError.Generic)
                }
            }
            val token = tokenResult.getOrThrow()

            val supabaseResult = authRepository.signInWithIdToken(token)
            supabaseResult.exceptionOrNull()?.let { error ->
                return SignInOutcome.Failure(error.toLoginError())
            }

            val userId =
                authRepository.currentUserId()
                    ?: return SignInOutcome.Failure(LoginError.Generic)

            return when (val membership = verifySunnerMembership(userId)) {
                MembershipResult.IsSunner -> SignInOutcome.Success
                MembershipResult.NotASunner -> SignInOutcome.NotASunner
                is MembershipResult.Failed -> SignInOutcome.Failure(membership.error)
            }
        }

        private fun Throwable.toLoginError(): LoginError =
            when (this) {
                is IOException -> LoginError.Network
                else -> {
                    val message = message.orEmpty().lowercase()
                    when {
                        "expired" in message || "code_expired" in message -> LoginError.OAuthCodeExpired
                        "disabled" in message || "locked" in message -> LoginError.AccountDisabled
                        else -> LoginError.Generic
                    }
                }
            }
    }
