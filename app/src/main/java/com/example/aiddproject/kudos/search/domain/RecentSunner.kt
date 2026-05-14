package com.example.aiddproject.kudos.search.domain

import kotlinx.serialization.Serializable

/**
 * Local-only projection of a Sunner that has been opened from the
 * Search Sunner screen recently. Intentionally smaller than the full
 * `SunnerNode` domain model — stores only the fields the recent-row
 * needs to render, so the recent list survives any future schema
 * change in the directory model.
 *
 * Persisted as a JSON array under the per-user DataStore key
 * `recent_sunners_$userId` (see [RecentSunnerRepository]).
 */
@Serializable
data class RecentSunner(
    val userId: String,
    val fullName: String,
    val departmentName: String? = null,
    val avatarUrl: String? = null,
    /** Epoch millis at the moment of the most recent search / row tap. Used for ordering. */
    val lastSearchedAtMillis: Long,
)
