@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.example.aiddproject.kudos.notifications.data

import com.example.aiddproject.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import javax.inject.Singleton

/**
 * Hilt graph for the Notifications feature data layer. Mirrors
 * `HomeRepositoryModule` — `BuildConfig.DEMO_MODE` swaps in the demo
 * repository so `:installDemoDebug` runs without a Supabase project.
 */
@Module
@InstallIn(SingletonComponent::class)
object NotificationRepositoryModule {
    @Provides
    @Singleton
    fun provideNotificationRepository(supabaseClient: SupabaseClient): NotificationRepository =
        if (BuildConfig.DEMO_MODE) {
            DemoNotificationRepository()
        } else {
            DefaultSupabaseNotificationRepository(supabaseClient)
        }
}
