package com.example.aiddproject.kudos.search.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.aiddproject.auth.login.data.AuthRepository
import com.example.aiddproject.kudos.search.domain.RecentSunner
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Local-only repository for the Search Sunner "Recent" list.
 *
 * Persists a small projection of recently-tapped colleagues to a per-
 * user DataStore key so the empty state on next launch is rare for an
 * active user. Capped at [MAX_ENTRIES] entries (oldest evicted on the
 * 6th add) per spec FR-003.
 *
 * Per-user keying: every write/read namespaces by the currently-
 * authenticated user's id (`recent_sunners_$userId`). On logout the
 * `clear()` method drops the calling user's key — wire it from the
 * existing auth signOut path as a follow-up (plan Phase 4 step 6).
 *
 * No network. No Supabase. The recent list is local to the device.
 */
interface RecentSunnerRepository {
    /** Emits the current user's recent list, sorted most-recent-first. */
    fun observeAll(): Flow<List<RecentSunner>>

    /**
     * Inserts or promotes [entry] to position 0 (most-recent). Updates
     * `lastSearchedAtMillis` to `System.currentTimeMillis()` so the
     * ordering reflects the tap. Evicts the oldest entry if the list
     * would exceed [MAX_ENTRIES] after insertion.
     */
    suspend fun addOrPromote(entry: RecentSunner)

    /** Removes the entry with [userId]. No-op if not present. */
    suspend fun remove(userId: String)

    /** Drops the current user's entire recent list. Called on logout. */
    suspend fun clear()

    companion object {
        const val MAX_ENTRIES: Int = 5
    }
}

@Singleton
class DefaultRecentSunnerRepository
    @Inject
    constructor(
        @RecentSunnersDataStore private val dataStore: DataStore<Preferences>,
        private val authRepository: AuthRepository,
    ) : RecentSunnerRepository {
        private val json = Json { ignoreUnknownKeys = true }

        override fun observeAll(): Flow<List<RecentSunner>> =
            dataStore.data.map { prefs ->
                val raw = prefs[currentUserKey()]
                val persisted =
                    if (raw == null) {
                        emptyList()
                    } else {
                        runCatching {
                            json.decodeFromString<List<RecentSunner>>(raw)
                        }.onFailure { error ->
                            Timber.tag(TELEMETRY_TAG).w(error, "recent.decode.failure")
                        }.getOrDefault(emptyList())
                    }
                // When the user has nothing persisted yet (fresh install
                // OR after clearing every entry), surface a small demo
                // seed so the empty-box state isn't visually empty in
                // dev/demo builds. User-triggered adds/removes write
                // through normally — once the user touches the list,
                // their persisted entries take over.
                val list = if (persisted.isEmpty()) DEMO_SEED else persisted
                list.sortedByDescending { it.lastSearchedAtMillis }
            }

        override suspend fun addOrPromote(entry: RecentSunner) {
            mutate { current ->
                val withoutDup = current.filterNot { it.userId == entry.userId }
                val updated =
                    (listOf(entry.copy(lastSearchedAtMillis = System.currentTimeMillis())) + withoutDup)
                        .take(RecentSunnerRepository.MAX_ENTRIES)
                updated
            }
        }

        override suspend fun remove(userId: String) {
            mutate { current -> current.filterNot { it.userId == userId } }
        }

        override suspend fun clear() {
            runCatching {
                dataStore.edit { prefs ->
                    prefs.remove(currentUserKey())
                }
            }.onFailure { error ->
                Timber.tag(TELEMETRY_TAG).w(error, "recent.clear.failure")
            }
        }

        private suspend inline fun mutate(crossinline block: (List<RecentSunner>) -> List<RecentSunner>) {
            runCatching {
                dataStore.edit { prefs ->
                    val key = currentUserKey()
                    val raw = prefs[key]
                    val current =
                        if (raw == null) {
                            emptyList()
                        } else {
                            runCatching { json.decodeFromString<List<RecentSunner>>(raw) }
                                .getOrDefault(emptyList())
                        }
                    val next = block(current)
                    prefs[key] = json.encodeToString(next)
                }
            }.onFailure { error ->
                Timber.tag(TELEMETRY_TAG).w(error, "recent.write.failure")
            }
        }

        private fun currentUserKey() =
            stringPreferencesKey("recent_sunners_${authRepository.currentUserId() ?: ANON_USER_ID}")

        private companion object {
            const val TELEMETRY_TAG = "SearchSunnerTelemetry"
            const val ANON_USER_ID = "anon"

            /**
             * Demo seed — 3 colleagues drawn from `DemoKudosRepository.DEMO_SUNNERS`
             * so the empty-DataStore state has something to render. Their
             * `userId`s match the demo Sunner directory so tapping any
             * seed row navigates to the correct Profile (once that screen
             * ships with parameterization). Timestamps are arbitrary
             * positive longs — only the relative ordering matters.
             *
             * Removed when the live-search state spec (`hldqjHoSRH`) ships
             * and starts populating the recent list naturally.
             */
            val DEMO_SEED: List<RecentSunner> =
                listOf(
                    RecentSunner(
                        userId = "u11",
                        fullName = "Nguyễn Ngọc Sơn",
                        departmentName = "CECV1",
                        avatarUrl = null,
                        lastSearchedAtMillis = 300L,
                    ),
                    RecentSunner(
                        userId = "u09",
                        fullName = "Dương Huỳnh Xuân Nhật",
                        departmentName = "CECU01",
                        avatarUrl = null,
                        lastSearchedAtMillis = 200L,
                    ),
                    RecentSunner(
                        userId = "u02",
                        fullName = "Trần Bình",
                        departmentName = "CECV1",
                        avatarUrl = null,
                        lastSearchedAtMillis = 100L,
                    ),
                )
        }
    }
