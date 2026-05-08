package com.example.aiddproject.auth.login.data

import com.example.aiddproject.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import javax.inject.Singleton

/**
 * Domain-facing seam for `public.users` reads. Pulled out of the use case so unit tests
 * can mock the postgrest call without spinning up the SDK; the live implementation is
 * exercised by the instrumented integration test (T048) against a local Supabase stack.
 */
interface UsersRepository {
    /**
     * Returns `Result.success(true)` if a `users` row exists for [userId] under the
     * caller's RLS context, `Result.success(false)` if the result set is empty (i.e., not
     * a Sunner / RLS-denied), and `Result.failure` on transport or server errors.
     */
    suspend fun isRegisteredSunner(userId: String): Result<Boolean>
}

class SupabasePostgrestUsersRepository(
    private val supabaseClient: SupabaseClient,
) : UsersRepository {
    override suspend fun isRegisteredSunner(userId: String): Result<Boolean> =
        runCatching {
            val response =
                supabaseClient.from("users").select {
                    filter { eq("id", userId) }
                    limit(1)
                }
            // Avoid kotlinx.serialization plugin reliance: parse the raw JSON body.
            val rows = Json.parseToJsonElement(response.data).jsonArray
            rows.isNotEmpty()
        }
}

@Module
@InstallIn(SingletonComponent::class)
object UsersRepositoryModule {
    @Provides
    @Singleton
    fun provideUsersRepository(supabaseClient: SupabaseClient): UsersRepository =
        if (BuildConfig.DEMO_MODE) {
            DemoUsersRepository()
        } else {
            SupabasePostgrestUsersRepository(supabaseClient)
        }
}
