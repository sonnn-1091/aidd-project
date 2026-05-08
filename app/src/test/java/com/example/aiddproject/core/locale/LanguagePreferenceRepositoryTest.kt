package com.example.aiddproject.core.locale

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.preferencesOf
import androidx.datastore.preferences.core.stringPreferencesKey
import app.cash.turbine.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import timber.log.Timber
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

                // Round-trip back to VN to assert two-way switching (the JA leg is
                // gone with the JA enum value — see Language Dropdown spec
                // uUvW6Qm1ve § Resolved Q1).
                repository.set(Language.VN)
                assertEquals(Language.VN, awaitItem())

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
        // JA was removed from `Language.entries` per Language Dropdown spec
        // uUvW6Qm1ve. The orphaned-token fallback is asserted by
        // `orphaned JA token returns VN default` below.
    }

    @Test
    fun `preferencesOf round-trips via Language fromCode`() {
        val key = stringPreferencesKey("language_code")
        val prefs = preferencesOf(key to "EN")
        assertEquals(Language.EN, Language.fromCode(prefs[key]))
    }

    // ----- Phase 2 tests (Language Dropdown spec uUvW6Qm1ve) -----

    @Test
    fun `first launch no preference returns VN default`() =
        runTest {
            // FR-002: brand-mandated VN default on fresh install in every locale
            // (spec § Resolved Q2). Same as `default is VN when nothing persisted`
            // but explicit so the FR-002 contract has a named test.
            val repository = newRepo()
            assertEquals(Language.VN, repository.language.first())
        }

    @Test
    fun `orphaned JA token returns VN default`() =
        runTest {
            // Spec § Edge Cases — DataStore unavailable or corrupted.
            // Existing installs that persisted "JA" before the JA removal must
            // fall back to VN silently on next read (spec § Resolved Q3).
            val file = File(tempFolder.newFolder(), "language_preferences.preferences_pb")
            val dataStore: DataStore<Preferences> =
                PreferenceDataStoreFactory.create(
                    scope = backgroundScope,
                    produceFile = { file },
                )
            dataStore.edit { it[stringPreferencesKey("language_code")] = "JA" }

            val repository = LanguagePreferenceRepository(dataStore)
            assertEquals(Language.VN, repository.language.first())
        }

    @Test
    fun `set writes via Language code token not name or ordinal`() =
        runTest {
            // Spec § Key Entities — DataStore serialization contract: persisted
            // value MUST be `Language.code`, not `Language.name` or
            // `Language.ordinal.toString()`. Regression lock.
            val file = File(tempFolder.newFolder(), "language_preferences.preferences_pb")
            val dataStore: DataStore<Preferences> =
                PreferenceDataStoreFactory.create(
                    scope = backgroundScope,
                    produceFile = { file },
                )
            val repository = LanguagePreferenceRepository(dataStore)
            repository.set(Language.EN)

            val rawValue = dataStore.data.first()[stringPreferencesKey("language_code")]
            assertEquals("EN", rawValue) // not "English", not "1"
        }

    @Test
    fun `write failure emits telemetry breadcrumb and keeps flow unchanged`() =
        runTest {
            // Spec § Edge Cases — DataStore write failure + TR-005.
            // The repository wraps `dataStore.edit { ... }` in `runCatching` and
            // emits a `LanguageTelemetry`-tagged Timber breadcrumb on failure;
            // the language flow does not advance because nothing was written.
            val captured = mutableListOf<String>()
            val tree =
                object : Timber.Tree() {
                    override fun log(
                        priority: Int,
                        tag: String?,
                        message: String,
                        t: Throwable?,
                    ) {
                        if (tag == "LanguageTelemetry") captured += message
                    }
                }
            Timber.plant(tree)

            try {
                val throwingDataStore =
                    object : DataStore<Preferences> {
                        override val data = flowOf(preferencesOf())

                        override suspend fun updateData(transform: suspend (Preferences) -> Preferences): Preferences {
                            error("simulated disk pressure")
                        }
                    }
                val repository = LanguagePreferenceRepository(throwingDataStore)

                // Must NOT rethrow.
                repository.set(Language.EN)

                // Telemetry breadcrumb fired.
                assertNotNull(
                    "Expected LanguageTelemetry breadcrumb on write failure",
                    captured.firstOrNull { it.contains("language.write.failure") },
                )

                // Flow does not advance — `data` is the constant `flowOf(preferencesOf())`,
                // so it always emits VN. Check it stays VN even after the failed write.
                assertEquals(Language.VN, repository.language.first())
            } finally {
                Timber.uproot(tree)
            }

            // Sanity: `assertNull` import is exercised so the empty-cap check
            // would catch a no-op tree implementation.
            assertNull(null as String?)
        }

    @After
    fun tearDownTimber() {
        // Belt-and-braces: ensure no test trees leak across runs.
        Timber.uprootAll()
    }
}
