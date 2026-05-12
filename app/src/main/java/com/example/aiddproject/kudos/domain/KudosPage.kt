package com.example.aiddproject.kudos.domain

/**
 * Paginated slice of the All Kudos feed (spec § US1 + § US3).
 *
 * [items] is the rows for this page; [hasMore] tells the UI whether
 * to render the "View all Kudos" link (US14) vs an end-of-feed cap;
 * [nextPage] is the cursor to pass to the next
 * [com.example.aiddproject.kudos.data.KudosRepository.listKudos] call.
 */
data class KudosPage(
    val items: List<Kudos>,
    val hasMore: Boolean,
    val nextPage: Int?,
)

/**
 * Combined hashtag + department filter applied to both feeds (US3).
 * AND-combined: a kudos is included only if its hashtags contain
 * [hashtagId] AND its sender/recipient department matches
 * [departmentId]. Null fields are treated as wildcards.
 */
data class KudosFilter(
    val hashtagId: String? = null,
    val departmentId: String? = null,
)
