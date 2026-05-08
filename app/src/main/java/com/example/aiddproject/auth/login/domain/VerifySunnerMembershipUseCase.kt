package com.example.aiddproject.auth.login.domain

import com.example.aiddproject.auth.login.data.AuthRepository
import com.example.aiddproject.auth.login.data.UsersRepository
import com.example.aiddproject.auth.login.ui.LoginError
import javax.inject.Inject

/**
 * Result of post-OAuth Sunner verification.
 *
 * - [IsSunner]: a `users` row exists for the authenticated id — proceed to Home.
 * - [NotASunner]: no row (empty / RLS-forbidden) — caller has been signed out and should
 *   navigate to `[iOS] Access denied` (FR-005).
 * - [Failed]: network or server failure during the lookup. Surfaced to the user as a
 *   snackbar; session is left as-is so the next CTA tap can retry.
 */
sealed interface MembershipResult {
    data object IsSunner : MembershipResult

    data object NotASunner : MembershipResult

    data class Failed(
        val error: LoginError,
    ) : MembershipResult
}

/**
 * Verifies that the authenticated Supabase user is a registered Sunner by selecting their
 * row in `public.users`. RLS scopes the query to the caller's own row — a non-Sunner JWT
 * sees an empty result, never another Sunner's row (Constitution Security Requirements;
 * spec FR-005).
 *
 * On [MembershipResult.NotASunner] the use case proactively calls
 * [AuthRepository.signOut] so [com.example.aiddproject.core.session.SessionRepository] does
 * not stay in `Authenticated` after a rejected gate check.
 */
class VerifySunnerMembershipUseCase
    @Inject
    constructor(
        private val usersRepository: UsersRepository,
        private val authRepository: AuthRepository,
    ) {
        suspend operator fun invoke(userId: String): MembershipResult =
            usersRepository.isRegisteredSunner(userId).fold(
                onSuccess = { isSunner ->
                    if (isSunner) {
                        MembershipResult.IsSunner
                    } else {
                        authRepository.signOut()
                        MembershipResult.NotASunner
                    }
                },
                onFailure = { MembershipResult.Failed(LoginError.Generic) },
            )
    }
