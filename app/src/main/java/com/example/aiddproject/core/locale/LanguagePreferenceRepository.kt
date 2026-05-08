package com.example.aiddproject.core.locale

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Persists the user-selected display [Language] across launches via Preferences DataStore.
 *
 * - First launch (no value stored) emits [Language.Default] (VN).
 * - Unsupported persisted code (e.g., a legacy "FR") also falls back to VN.
 */
@Singleton
class LanguagePreferenceRepository
    @Inject
    constructor(
        private val dataStore: DataStore<Preferences>,
    ) {
        val language: Flow<Language> =
            dataStore.data.map { prefs ->
                Language.fromCode(prefs[KEY])
            }

        suspend fun set(language: Language) {
            dataStore.edit { prefs ->
                prefs[KEY] = language.code
            }
        }

        private companion object {
            val KEY = stringPreferencesKey("language_code")
        }
    }
