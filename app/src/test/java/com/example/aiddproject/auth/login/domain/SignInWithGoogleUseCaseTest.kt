package com.example.aiddproject.auth.login.domain

import android.content.Context
import com.example.aiddproject.auth.login.data.AuthRepository
import com.example.aiddproject.auth.login.data.GoogleCredentialProvider
import com.example.aiddproject.auth.login.data.GoogleSignInCancelledException
import com.example.aiddproject.auth.login.data.NoGoogleAccountException
import com.example.aiddproject.auth.login.data.UsersRepository
import com.example.aiddproject.auth.login.ui.LoginError
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.IOException

class SignInWithGoogleUseCaseTest {
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
    private val activityContext: Context = mockk(relaxed = true)

    @Test
    fun `happy path returns Success`() =
        runTest {
            coEvery { credentialProvider.getIdToken(activityContext) } returns Result.success("id-tok")
            coEvery { authRepository.signInWithIdToken("id-tok") } returns Result.success(Unit)
            every { authRepository.currentUserId() } returns "alice"
            coEvery { usersRepository.isRegisteredSunner("alice") } returns Result.success(true)

            assertEquals(SignInOutcome.Success, useCase(activityContext))
        }

    @Test
    fun `not a Sunner returns NotASunner and triggers signOut`() =
        runTest {
            coEvery { credentialProvider.getIdToken(activityContext) } returns Result.success("id-tok")
            coEvery { authRepository.signInWithIdToken("id-tok") } returns Result.success(Unit)
            every { authRepository.currentUserId() } returns "bob"
            coEvery { usersRepository.isRegisteredSunner("bob") } returns Result.success(false)

            assertEquals(SignInOutcome.NotASunner, useCase(activityContext))
            coVerify(exactly = 1) { authRepository.signOut() }
        }

    @Test
    fun `user cancellation maps to Cancelled`() =
        runTest {
            coEvery {
                credentialProvider.getIdToken(activityContext)
            } returns Result.failure(GoogleSignInCancelledException())

            assertEquals(SignInOutcome.Cancelled, useCase(activityContext))
            coVerify(exactly = 0) { authRepository.signInWithIdToken(any()) }
        }

    @Test
    fun `no Google account maps to PlayServicesUnavailable failure`() =
        runTest {
            coEvery {
                credentialProvider.getIdToken(activityContext)
            } returns Result.failure(NoGoogleAccountException())

            assertEquals(
                SignInOutcome.Failure(LoginError.PlayServicesUnavailable),
                useCase(activityContext),
            )
        }

    @Test
    fun `IOException during credential fetch maps to Network failure`() =
        runTest {
            coEvery {
                credentialProvider.getIdToken(activityContext)
            } returns Result.failure(IOException("offline"))

            assertEquals(SignInOutcome.Failure(LoginError.Network), useCase(activityContext))
        }

    @Test
    fun `Supabase IOException maps to Network failure`() =
        runTest {
            coEvery { credentialProvider.getIdToken(activityContext) } returns Result.success("id-tok")
            coEvery {
                authRepository.signInWithIdToken("id-tok")
            } returns Result.failure(IOException("offline"))

            assertEquals(SignInOutcome.Failure(LoginError.Network), useCase(activityContext))
        }

    @Test
    fun `Supabase code-expired error maps to OAuthCodeExpired`() =
        runTest {
            coEvery { credentialProvider.getIdToken(activityContext) } returns Result.success("id-tok")
            coEvery {
                authRepository.signInWithIdToken("id-tok")
            } returns Result.failure(RuntimeException("authorization code expired"))

            assertEquals(SignInOutcome.Failure(LoginError.OAuthCodeExpired), useCase(activityContext))
        }

    @Test
    fun `Supabase account-disabled error maps to AccountDisabled`() =
        runTest {
            coEvery { credentialProvider.getIdToken(activityContext) } returns Result.success("id-tok")
            coEvery {
                authRepository.signInWithIdToken("id-tok")
            } returns Result.failure(RuntimeException("account disabled"))

            assertEquals(SignInOutcome.Failure(LoginError.AccountDisabled), useCase(activityContext))
        }

    @Test
    fun `unclassified Supabase error maps to Generic`() =
        runTest {
            coEvery { credentialProvider.getIdToken(activityContext) } returns Result.success("id-tok")
            coEvery {
                authRepository.signInWithIdToken("id-tok")
            } returns Result.failure(RuntimeException("something weird"))

            assertEquals(SignInOutcome.Failure(LoginError.Generic), useCase(activityContext))
        }

    @Test
    fun `null current user after signIn surfaces Generic failure`() =
        runTest {
            coEvery { credentialProvider.getIdToken(activityContext) } returns Result.success("id-tok")
            coEvery { authRepository.signInWithIdToken("id-tok") } returns Result.success(Unit)
            every { authRepository.currentUserId() } returns null

            assertEquals(SignInOutcome.Failure(LoginError.Generic), useCase(activityContext))
        }

    @Test
    fun `membership lookup failure maps to Generic failure`() =
        runTest {
            coEvery { credentialProvider.getIdToken(activityContext) } returns Result.success("id-tok")
            coEvery { authRepository.signInWithIdToken("id-tok") } returns Result.success(Unit)
            every { authRepository.currentUserId() } returns "alice"
            coEvery { usersRepository.isRegisteredSunner("alice") } returns
                Result.failure(IOException("offline"))

            assertEquals(SignInOutcome.Failure(LoginError.Generic), useCase(activityContext))
        }
}
