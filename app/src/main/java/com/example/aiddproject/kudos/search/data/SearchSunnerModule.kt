package com.example.aiddproject.kudos.search.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt wiring for the Search Sunner feature.
 *
 *  - Provides a [RecentSunnersDataStore]-qualified `DataStore<Preferences>`
 *    backed by the file `recent_sunners_preferences` (distinct from
 *    `AppModule.provideLanguageDataStore`'s `language_preferences` file).
 *  - Binds the [RecentSunnerRepository] interface to its default impl.
 *
 * Two `@Module` objects in one file: `SearchSunnerProvidesModule` (for
 * `@Provides`) and `SearchSunnerBindsModule` (for `@Binds`). Hilt does
 * not allow mixing `@Provides` and `@Binds` in the same `@Module` class.
 */
@Module
@InstallIn(SingletonComponent::class)
object SearchSunnerProvidesModule {
    @Provides
    @Singleton
    @RecentSunnersDataStore
    fun provideRecentSunnersDataStore(
        @ApplicationContext context: Context,
    ): DataStore<Preferences> =
        PreferenceDataStoreFactory.create(
            produceFile = { context.preferencesDataStoreFile("recent_sunners_preferences") },
        )
}

@Module
@InstallIn(SingletonComponent::class)
abstract class SearchSunnerBindsModule {
    @Binds
    @Singleton
    abstract fun bindRecentSunnerRepository(impl: DefaultRecentSunnerRepository): RecentSunnerRepository
}
