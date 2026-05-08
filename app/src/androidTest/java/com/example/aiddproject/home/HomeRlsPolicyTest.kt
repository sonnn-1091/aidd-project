package com.example.aiddproject.home

import com.example.aiddproject.BuildConfig
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
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
 * RLS denial proofs for the Home backend tables (T072), mirroring Login's
 * `RlsPolicyTest`. Three policies are exercised in one class:
 *
 *  - `awards_select_authenticated`: an unauthenticated client must not be
 *    able to read the catalog.
 *  - `kudos_settings_select_authenticated`: same gate on the kudos config row.
 *  - `notifications_select_own`: an authenticated non-self JWT (the
 *    `NON_SUNNER` test account) must see zero rows from another user's
 *    notifications.
 *
 * Skips when no Supabase host is configured — Q4 in plan tracks the CI
 * Supabase strategy. When the staging stack + seed are wired up, drop the
 * [Assume] guard.
 */
class HomeRlsPolicyTest {
    private val configured: Boolean
        get() =
            BuildConfig.SUPABASE_URL.isNotBlank() &&
                BuildConfig.SUPABASE_ANON_KEY.isNotBlank()

    private val nonSunnerCredsConfigured: Boolean
        get() =
            !System.getenv("SUPABASE_TEST_NON_SUNNER_EMAIL").isNullOrBlank() &&
                !System.getenv("SUPABASE_TEST_NON_SUNNER_PASSWORD").isNullOrBlank()

    @Test
    fun anonymous_client_cannot_select_awards() =
        runBlocking {
            assumeTrue("Supabase not configured for RLS test — see plan Q4", configured)

            val rows = anonClient().from("awards").select()
            val parsed = Json.parseToJsonElement(rows.data).jsonArray

            assertEquals(
                "Anon JWT must see zero rows under awards_select_authenticated",
                0,
                parsed.size,
            )
        }

    @Test
    fun anonymous_client_cannot_select_kudos_settings() =
        runBlocking {
            assumeTrue("Supabase not configured for RLS test — see plan Q4", configured)

            val rows = anonClient().from("kudos_settings").select()
            val parsed = Json.parseToJsonElement(rows.data).jsonArray

            assertEquals(
                "Anon JWT must see zero rows under kudos_settings_select_authenticated",
                0,
                parsed.size,
            )
        }

    @Test
    fun non_self_jwt_cannot_select_other_users_notifications() =
        runBlocking {
            assumeTrue("Supabase not configured for RLS test — see plan Q4", configured)
            assumeTrue(
                "Non-Sunner test account env not configured — see plan Q4",
                nonSunnerCredsConfigured,
            )

            val email = checkNotNull(System.getenv("SUPABASE_TEST_NON_SUNNER_EMAIL"))
            val password = checkNotNull(System.getenv("SUPABASE_TEST_NON_SUNNER_PASSWORD"))

            val client = newClient()
            client.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }

            val rows = client.from("notifications").select()
            val parsed = Json.parseToJsonElement(rows.data).jsonArray

            assertEquals(
                "Non-self JWT must see zero rows under notifications_select_own",
                0,
                parsed.size,
            )

            client.auth.signOut()
        }

    private fun anonClient() =
        createSupabaseClient(
            supabaseUrl = BuildConfig.SUPABASE_URL,
            supabaseKey = BuildConfig.SUPABASE_ANON_KEY,
        ) {
            install(Auth)
            install(Postgrest)
        }

    private fun newClient() = anonClient()
}
