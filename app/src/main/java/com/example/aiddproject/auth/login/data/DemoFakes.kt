package com.example.aiddproject.auth.login.data

import android.content.Context
import kotlinx.coroutines.delay

/**
 * Demo-mode fakes used when `BuildConfig.DEMO_MODE = true`. Every call resolves as
 * the seeded Sunner `alice@sun-asterisk.com` so the entire Login → Home flow runs
 * end-to-end on a developer machine without a real Supabase project or Google OAuth
 * registration. Disable by setting `app.demoMode=false` in `local.properties`.
 *
 * Loading is briefly delayed so the spinner state is observable.
 */

const val DEMO_USER_ID: String = "00000000-0000-0000-0000-000000000001"
const val DEMO_USER_EMAIL: String = "alice@sun-asterisk.com"

class DemoGoogleCredentialProvider : GoogleCredentialProvider {
    override suspend fun getIdToken(activityContext: Context): Result<String> {
        delay(400)
        return Result.success("demo.id.token.$DEMO_USER_ID")
    }
}

class DemoSupabaseAuthGateway : SupabaseAuthGateway {
    @Volatile private var signedIn: Boolean = false

    override suspend fun signInWithGoogleIdToken(token: String) {
        delay(300)
        signedIn = true
    }

    override suspend fun signOut() {
        signedIn = false
    }

    override fun currentUserId(): String? = if (signedIn) DEMO_USER_ID else null
}

class DemoUsersRepository : UsersRepository {
    override suspend fun isRegisteredSunner(userId: String): Result<Boolean> {
        // The demo Sunner is whitelisted; any other user id is rejected so the
        // not-a-Sunner branch is exercisable from the demo too if you wire a
        // different DEMO_USER_ID for testing.
        return Result.success(userId == DEMO_USER_ID)
    }
}
