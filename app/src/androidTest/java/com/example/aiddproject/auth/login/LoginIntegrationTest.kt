package com.example.aiddproject.auth.login

import com.example.aiddproject.auth.login.data.AuthRepository
import com.example.aiddproject.auth.login.data.GoogleCredentialProvider
import com.example.aiddproject.auth.login.data.UsersRepository
import com.example.aiddproject.auth.login.domain.MembershipResult
import com.example.aiddproject.auth.login.domain.SignInOutcome
import com.example.aiddproject.auth.login.domain.SignInWithGoogleUseCase
import com.example.aiddproject.auth.login.domain.VerifySunnerMembershipUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * End-to-end exercise of the SignInWithGoogle pipeline against the production wiring of
 * domain + data classes — Credential Manager and Supabase are faked at the seams since
 * Phase 3 ships before Q4 (CI Supabase strategy) and Q7 (Google OAuth SHA-256 registration)
 * are resolved per `plan.md`. Once those land, this test will be promoted to drive the
 * real Supabase + faked Google credential path.
 *
 * Marked as instrumented so it runs on a connected device; it does not yet require Play
 * Services or a Supabase host.
 */
class LoginIntegrationTest {
    private val credentialProvider: GoogleCredentialProvider = mockk()
    private val authRepository: AuthRepository = mockk(relaxed = true)
    private val usersRepository: UsersRepository = mockk()
    private val verifyMembership = VerifySunnerMembershipUseCase(usersRepository, authRepository)
    private val useCase =
        SignInWithGoogleUseCase(
            credentialProvider = credentialProvider,
            authRepository = authRepository,
            verifySunnerMembership = verifyMembership,
        )
    private val activityContext: android.content.Context = mockk(relaxed = true)

    @Test
    fun sunner_login_reaches_Success() =
        runBlocking {
            coEvery { credentialProvider.getIdToken(activityContext) } returns Result.success("tok")
            coEvery { authRepository.signInWithIdToken("tok") } returns Result.success(Unit)
            every { authRepository.currentUserId() } returns "alice"
            coEvery { usersRepository.isRegisteredSunner("alice") } returns Result.success(true)

            assertEquals(SignInOutcome.Success, useCase(activityContext))
            coVerify(exactly = 0) { authRepository.signOut() }
        }

    @Test
    fun non_sunner_signs_out_and_routes_to_NotASunner() =
        runBlocking {
            coEvery { credentialProvider.getIdToken(activityContext) } returns Result.success("tok")
            coEvery { authRepository.signInWithIdToken("tok") } returns Result.success(Unit)
            every { authRepository.currentUserId() } returns "outsider"
            coEvery {
                usersRepository.isRegisteredSunner("outsider")
            } returns Result.success(false)

            assertEquals(SignInOutcome.NotASunner, useCase(activityContext))
            coVerify(exactly = 1) { authRepository.signOut() }
        }

    @Test
    fun verify_membership_returns_NotASunner_when_users_lookup_is_empty() =
        runBlocking {
            coEvery { usersRepository.isRegisteredSunner("ghost") } returns Result.success(false)

            val result = verifyMembership("ghost")

            assertEquals(MembershipResult.NotASunner, result)
            coVerify(exactly = 1) { authRepository.signOut() }
        }
}
