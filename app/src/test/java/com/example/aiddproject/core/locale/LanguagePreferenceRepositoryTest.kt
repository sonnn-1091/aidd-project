package com.example.aiddproject.core.locale

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.preferencesOf
import androidx.datastore.preferences.core.stringPreferencesKey
import app.cash.turbine.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class LanguagePreferenceRepositoryTest {
    @get:Rule
    val tempFolder = TemporaryFolder()

    /**
     * Create a fresh DataStore + repository scoped to the *current* `runTest` scope so
     * Preferences DataStore I/O is dispatched on the test scheduler. Sharing a top-level
     * `TestScope` field would deadlock here — the dataStore's writes get scheduled on a
     * dispatcher that the runTest scheduler never advances.
     */
    private fun TestScope.newRepo(): LanguagePreferenceRepository {
        val file = File(tempFolder.newFolder(), "language_preferences.preferences_pb")
        val dataStore: DataStore<Preferences> =
            PreferenceDataStoreFactory.create(
                scope = backgroundScope,
                produceFile = { file },
            )
        return LanguagePreferenceRepository(dataStore)
    }

    @Test
    fun `default is VN when nothing persisted`() =
        runTest {
            val repository = newRepo()
            repository.language.test {
                assertEquals(Language.VN, awaitItem())
                cancelAndConsumeRemainingEvents()
            }
        }

    @Test
    fun `set persists and updates flow`() =
        runTest {
            val repository = newRepo()
            repository.language.test {
                assertEquals(Language.VN, awaitItem())

                repository.set(Language.EN)
                assertEquals(Language.EN, awaitItem())

                repository.set(Language.JA)
                assertEquals(Language.JA, awaitItem())

                cancelAndConsumeRemainingEvents()
            }
        }

    @Test
    fun `unsupported persisted code falls back to VN`() =
        runTest {
            val file = File(tempFolder.newFolder(), "language_preferences.preferences_pb")
            val dataStore: DataStore<Preferences> =
                PreferenceDataStoreFactory.create(
                    scope = backgroundScope,
                    produceFile = { file },
                )
            val repository = LanguagePreferenceRepository(dataStore)

            // Pre-write an unsupported code directly into the underlying DataStore.
            val key = stringPreferencesKey("language_code")
            dataStore.edit { it[key] = "FR" }

            repository.language.test {
                assertEquals(Language.VN, awaitItem())
                cancelAndConsumeRemainingEvents()
            }
        }

    @Test
    fun `language enum fromCode is null-safe`() {
        assertEquals(Language.VN, Language.fromCode(null))
        assertEquals(Language.VN, Language.fromCode(""))
        assertEquals(Language.VN, Language.fromCode("XX"))
        assertEquals(Language.EN, Language.fromCode("EN"))
        assertEquals(Language.JA, Language.fromCode("JA"))
    }

    @Test
    fun `preferencesOf round-trips via Language fromCode`() {
        val key = stringPreferencesKey("language_code")
        val prefs = preferencesOf(key to "EN")
        assertEquals(Language.EN, Language.fromCode(prefs[key]))
    }
}
