package com.example.aiddproject.kudos.data

import com.example.aiddproject.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import javax.inject.Singleton

/**
 * Hilt graph for the Sun*Kudos feature data layer.
 *
 * Mirrors `home/data/HomeRepositoryModule` — branches on
 * [BuildConfig.DEMO_MODE] so `:installDemoDebug` swaps in
 * [DemoKudosRepository] (no network, no Supabase project required)
 * while production runs against [SupabaseKudosRepository].
 */
@Module
@InstallIn(SingletonComponent::class)
object KudosRepositoryModule {
    @Provides
    @Singleton
    fun provideKudosRepository(supabaseClient: SupabaseClient): KudosRepository =
        if (BuildConfig.DEMO_MODE) {
            DemoKudosRepository()
        } else {
            SupabaseKudosRepository(supabaseClient)
        }
}
