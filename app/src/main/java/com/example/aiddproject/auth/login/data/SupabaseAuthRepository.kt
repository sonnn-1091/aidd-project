package com.example.aiddproject.auth.login.data

import com.example.aiddproject.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.IDToken
import javax.inject.Singleton

/**
 * Narrow JVM-mockable seam over the Supabase SDK's `auth` plugin. The repository owns
 * Result wrapping; this gateway owns the actual SDK calls. Splitting the two lets us
 * unit-test the repository against a fake gateway while leaving the real SDK exercise to
 * the instrumented integration test (T048) per Constitution Principle V.
 */
interface SupabaseAuthGateway {
    suspend fun signInWithGoogleIdToken(token: String)

    suspend fun signOut()

    fun currentUserId(): String?
}

class DefaultSupabaseAuthGateway(
    private val supabaseClient: SupabaseClient,
) : SupabaseAuthGateway {
    override suspend fun signInWithGoogleIdToken(token: String) {
        supabaseClient.auth.signInWith(IDToken) {
            idToken = token
            provider = Google
        }
    }

    override suspend fun signOut() {
        supabaseClient.auth.signOut()
    }

    override fun currentUserId(): String? = supabaseClient.auth.currentUserOrNull()?.id
}

/** Default [AuthRepository]: thin Result-wrapper over [SupabaseAuthGateway]. */
class SupabaseAuthRepository(
    private val gateway: SupabaseAuthGateway,
) : AuthRepository {
    override suspend fun signInWithIdToken(token: String): Result<Unit> =
        runCatching {
            gateway.signInWithGoogleIdToken(token)
        }

    override suspend fun signOut() {
        gateway.signOut()
    }

    override fun currentUserId(): String? = gateway.currentUserId()
}

@Module
@InstallIn(SingletonComponent::class)
object AuthRepositoryModule {
    @Provides
    @Singleton
    fun provideSupabaseAuthGateway(supabaseClient: SupabaseClient): SupabaseAuthGateway =
        if (BuildConfig.DEMO_MODE) {
            DemoSupabaseAuthGateway()
        } else {
            DefaultSupabaseAuthGateway(supabaseClient)
        }

    @Provides
    @Singleton
    fun provideAuthRepository(gateway: SupabaseAuthGateway): AuthRepository = SupabaseAuthRepository(gateway)
}
