package com.example.aiddproject.auth.login.domain

import com.example.aiddproject.auth.login.data.AuthRepository
import com.example.aiddproject.auth.login.data.UsersRepository
import com.example.aiddproject.auth.login.ui.LoginError
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class VerifySunnerMembershipUseCaseTest {
    private val usersRepository: UsersRepository = mockk()
    private val authRepository: AuthRepository = mockk(relaxed = true)
    private val useCase = VerifySunnerMembershipUseCase(usersRepository, authRepository)

    @Test
    fun `Sunner row found returns IsSunner and does not sign out`() =
        runTest {
            coEvery { usersRepository.isRegisteredSunner("alice") } returns Result.success(true)

            val result = useCase("alice")

            assertEquals(MembershipResult.IsSunner, result)
            coVerify(exactly = 0) { authRepository.signOut() }
        }

    @Test
    fun `empty result triggers signOut and returns NotASunner`() =
        runTest {
            coEvery { usersRepository.isRegisteredSunner("bob") } returns Result.success(false)

            val result = useCase("bob")

            assertEquals(MembershipResult.NotASunner, result)
            coVerify(exactly = 1) { authRepository.signOut() }
        }

    @Test
    fun `lookup failure returns Failed Generic without signing out`() =
        runTest {
            coEvery {
                usersRepository.isRegisteredSunner(any())
            } returns Result.failure(RuntimeException("boom"))

            val result = useCase("carol")

            assertEquals(MembershipResult.Failed(LoginError.Generic), result)
            coVerify(exactly = 0) { authRepository.signOut() }
        }
}
