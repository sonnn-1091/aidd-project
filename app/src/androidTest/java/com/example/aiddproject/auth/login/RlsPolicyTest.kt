package com.example.aiddproject.auth.login

import com.example.aiddproject.BuildConfig
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import org.junit.Assert.assertEquals
import org.junit.Assume.assumeTrue
import org.junit.Test

/**
 * RLS denial proof for the `public.users` table (T049, plan risk row #3).
 *
 * Skips when no Supabase host is configured — Q4 in plan tracks the CI strategy. When the
 * staging Supabase + seed are wired up, drop the [Assume] guard and provision the two
 * test accounts (`SUPABASE_TEST_SUNNER_EMAIL/PASSWORD` and
 * `SUPABASE_TEST_NON_SUNNER_EMAIL/PASSWORD`) so this test gates merges.
 */
class RlsPolicyTest {
    private val configured: Boolean
        get() =
            BuildConfig.SUPABASE_URL.isNotBlank() &&
                BuildConfig.SUPABASE_ANON_KEY.isNotBlank() &&
                !System.getenv("SUPABASE_TEST_NON_SUNNER_EMAIL").isNullOrBlank()

    @Test
    fun non_sunner_jwt_cannot_select_any_users_row() =
        runBlocking {
            assumeTrue("Supabase not configured for RLS test — see plan Q4", configured)

            val nonSunnerEmail = checkNotNull(System.getenv("SUPABASE_TEST_NON_SUNNER_EMAIL"))
            val nonSunnerPassword = checkNotNull(System.getenv("SUPABASE_TEST_NON_SUNNER_PASSWORD"))

            val client =
                createSupabaseClient(
                    supabaseUrl = BuildConfig.SUPABASE_URL,
                    supabaseKey = BuildConfig.SUPABASE_ANON_KEY,
                ) {
                    install(Auth)
                    install(Postgrest)
                }

            client.auth.signInWith(io.github.jan.supabase.auth.providers.builtin.Email) {
                email = nonSunnerEmail
                password = nonSunnerPassword
            }

            val response = client.from("users").select()
            val rows = Json.parseToJsonElement(response.data).jsonArray

            assertEquals(
                "Non-Sunner JWT must see zero rows under users_select_own RLS policy",
                0,
                rows.size,
            )

            client.auth.signOut()
        }
}
