@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.example.aiddproject.home.data

import com.example.aiddproject.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import javax.inject.Singleton
import kotlin.time.Clock

/**
 * Hilt graph for the Home feature data layer.
 *
 * Each repository is provided through a gateway → repository pair, with the gateway
 * branching on [BuildConfig.DEMO_MODE] so `:installDemoDebug` swaps in fakes that need
 * neither network nor a Supabase project. Mirrors Login's `AuthRepositoryModule`.
 *
 * The countdown clock is also bound here so [com.example.aiddproject.home.domain.CountdownEngine]
 * can be `@Inject`-constructed; tests pass a fake [Clock] directly.
 */
@Module
@InstallIn(SingletonComponent::class)
object HomeRepositoryModule {
    @Provides
    @Singleton
    fun provideAwardsRepository(supabaseClient: SupabaseClient): AwardsRepository =
        if (BuildConfig.DEMO_MODE) {
            DemoAwardsRepository()
        } else {
            SupabasePostgrestAwardsRepository(DefaultSupabasePostgrestAwardsGateway(supabaseClient))
        }

    @Provides
    @Singleton
    fun provideKudosSummaryRepository(supabaseClient: SupabaseClient): KudosSummaryRepository =
        if (BuildConfig.DEMO_MODE) {
            DemoKudosSummaryRepository()
        } else {
            SupabaseKudosSummaryRepository(DefaultSupabaseKudosSummaryGateway(supabaseClient))
        }

    @Provides
    @Singleton
    fun provideNotificationsSummaryRepository(supabaseClient: SupabaseClient): NotificationsSummaryRepository =
        if (BuildConfig.DEMO_MODE) {
            DemoNotificationsSummaryRepository()
        } else {
            SupabaseNotificationsSummaryRepository(DefaultSupabaseNotificationsSummaryGateway(supabaseClient))
        }

    @Provides
    fun provideClock(): Clock = Clock.System
}
