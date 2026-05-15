package com.example.aiddproject.kudos.search.domain

/**
 * Local-only projection of a Sunner that has been opened from the
 * Search Sunner screen recently. Intentionally smaller than the full
 * `SunnerNode` domain model — stores only the fields the recent-row
 * needs to render, so the recent list survives any future schema
 * change in the directory model.
 *
 * Persisted as a JSON array (encoded by hand via `org.json.JSONArray`
 * in `DefaultRecentSunnerRepository`) under the per-user DataStore
 * key `recent_sunners_$userId`. Hand-rolled because the project does
 * not apply the `kotlinx-serialization` Gradle plugin yet — adding a
 * `@Serializable` annotation alone (without the plugin) throws at
 * runtime.
 */
data class RecentSunner(
    val userId: String,
    val fullName: String,
    val departmentName: String? = null,
    val avatarUrl: String? = null,
    /** Epoch millis at the moment of the most recent search / row tap. Used for ordering. */
    val lastSearchedAtMillis: Long,
)
