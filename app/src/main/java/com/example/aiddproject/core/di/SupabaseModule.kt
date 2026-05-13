package com.example.aiddproject.core.di

import com.example.aiddproject.BuildConfig
import com.example.aiddproject.core.auth.AuthErrorInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.annotations.SupabaseInternal
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.flow.Flow
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SupabaseModule {
    // SupabaseInternal: httpConfig is the supported (if internal-flagged) seam
    // for installing custom Ktor plugins on the underlying HttpClient. We use it
    // to wire AuthErrorInterceptor — the only alternative would be forking the
    // Supabase client. Opt-in acknowledged here so the rest of the codebase stays
    // off internal APIs.
    @OptIn(SupabaseInternal::class)
    @Provides
    @Singleton
    fun provideSupabaseClient(authErrorInterceptor: AuthErrorInterceptor): SupabaseClient =
        createSupabaseClient(
            supabaseUrl = BuildConfig.SUPABASE_URL,
            supabaseKey = BuildConfig.SUPABASE_ANON_KEY,
        ) {
            install(Auth)
            install(Postgrest)
            install(Storage)
            httpConfig {
                authErrorInterceptor.installInto(this)
            }
        }

    @Provides
    fun provideSessionStatusFlow(client: SupabaseClient): Flow<SessionStatus> = client.auth.sessionStatus

    @Provides
    @Singleton
    fun provideStorage(client: SupabaseClient): Storage = client.storage
}
