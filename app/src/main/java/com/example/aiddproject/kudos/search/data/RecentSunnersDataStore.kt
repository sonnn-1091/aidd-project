package com.example.aiddproject.kudos.search.data

import javax.inject.Qualifier

/**
 * Disambiguates the `DataStore<Preferences>` injection for the Search
 * Sunner feature so it doesn't collide with the existing
 * `provideLanguageDataStore` binding in `AppModule`. The recent-Sunner
 * list lives in a dedicated `recent_sunners_preferences` file.
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class RecentSunnersDataStore
