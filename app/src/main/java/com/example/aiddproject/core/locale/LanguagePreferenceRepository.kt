package com.example.aiddproject.core.locale

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Persists the user-selected display [Language] across launches via Preferences DataStore.
 *
 * - First launch (no value stored) emits [Language.Default] (VN).
 * - Unsupported persisted code (e.g. an orphaned "JA" from a pre-removal
 *   install, or a hand-rolled "FR") falls back to VN via [Language.fromCode].
 * - Write failures are swallowed locally — the repository emits a
 *   `LanguageTelemetry`-tagged Timber breadcrumb and returns normally so the
 *   caller never sees an exception (Language Dropdown spec uUvW6Qm1ve § Edge
 *   Cases — DataStore write failure + TR-005). DataStore's `edit` semantics
 *   already de-duplicate identical writes, so the repository does not need to
 *   short-circuit equal-value updates explicitly.
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
            runCatching {
                dataStore.edit { prefs ->
                    prefs[KEY] = language.code
                }
            }.onFailure { error ->
                Timber.tag(TELEMETRY_TAG).w(error, "language.write.failure")
            }
        }

        private companion object {
            val KEY = stringPreferencesKey("language_code")
            const val TELEMETRY_TAG = "LanguageTelemetry"
        }
    }
