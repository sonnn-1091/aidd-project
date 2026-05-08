package com.example.aiddproject.auth.login.data

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.example.aiddproject.BuildConfig
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Obtains a Google ID token via Credential Manager + Google ID provider, ready to be
 * exchanged for a Supabase session via [AuthRepository.signInWithIdToken].
 *
 * The interface lets unit tests substitute a fake provider; tests for the [SignInWithGoogleUseCase]
 * do not touch real Play Services.
 */
interface GoogleCredentialProvider {
    suspend fun getIdToken(activityContext: Context): Result<String>
}

/** Sentinel: user dismissed the Google account chooser — surface a silent return, not a toast. */
class GoogleSignInCancelledException : Exception()

/** Sentinel: device has no Google account / Play Services or the chooser returned no credential. */
class NoGoogleAccountException(
    cause: Throwable? = null,
) : Exception(cause)

class CredentialManagerGoogleCredentialProvider(
    appContext: Context,
) : GoogleCredentialProvider {
    private val credentialManager: CredentialManager = CredentialManager.create(appContext)

    override suspend fun getIdToken(activityContext: Context): Result<String> {
        val webClientId = BuildConfig.GOOGLE_OAUTH_WEB_CLIENT_ID
        if (webClientId.isBlank()) {
            return Result.failure(
                IllegalStateException(
                    "GOOGLE_OAUTH_WEB_CLIENT_ID is not configured — set google.oauthWebClientId in local.properties",
                ),
            )
        }
        val request =
            GetCredentialRequest
                .Builder()
                .addCredentialOption(
                    GetGoogleIdOption
                        .Builder()
                        .setServerClientId(webClientId)
                        .setFilterByAuthorizedAccounts(false)
                        .setAutoSelectEnabled(false)
                        .build(),
                ).build()
        return runCatching {
            val response = credentialManager.getCredential(activityContext, request)
            val credential = response.credential
            require(credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                "Unexpected credential type: ${credential.type}"
            }
            GoogleIdTokenCredential.createFrom(credential.data).idToken
        }.recoverCatching { error ->
            throw when (error) {
                is GetCredentialCancellationException -> GoogleSignInCancelledException()
                is NoCredentialException -> NoGoogleAccountException(error)
                is GetCredentialException -> error
                else -> error
            }
        }
    }
}

@Module
@InstallIn(SingletonComponent::class)
object GoogleCredentialProviderModule {
    @Provides
    @Singleton
    fun provideGoogleCredentialProvider(
        @ApplicationContext context: Context,
    ): GoogleCredentialProvider =
        if (BuildConfig.DEMO_MODE) {
            DemoGoogleCredentialProvider()
        } else {
            CredentialManagerGoogleCredentialProvider(context)
        }
}
