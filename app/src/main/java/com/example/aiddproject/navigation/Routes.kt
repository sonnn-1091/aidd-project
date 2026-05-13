package com.example.aiddproject.navigation

/**
 * Single source of truth for navigation route names.
 *
 * - Phase-3/4 routes (already shipped from Login): GATE, LOGIN, HOME, ACCESS_DENIED.
 * - Home-feature outbound routes (this UI pass): AWARDS_OVERVIEW, KUDOS_OVERVIEW,
 *   KUDOS_FEED, KUDOS_DETAIL, WRITE_KUDO, AWARD_DETAIL, SEARCH, PROFILE.
 *
 * The Home-feature routes are registered as PLACEHOLDER destinations in
 * `AppNavigation` so the navigation graph is end-to-end navigable before the
 * destination screens themselves ship.
 */
object Routes {
    const val GATE: String = "route_gate"
    const val LOGIN: String = "route_login"
    const val HOME: String = "route_home"
    const val ACCESS_DENIED: String = "route_access_denied"

    const val AWARDS_OVERVIEW: String = "route_awards_overview"
    const val KUDOS_OVERVIEW: String = "route_kudos_overview"
    const val KUDOS_FEED: String = "route_kudos_feed"
    const val KUDOS_DETAIL: String = "route_kudos_detail"

    /**
     * Viết Kudo composer (`7fFAb-K35a` spec) — accepts an optional
     * `recipientUserId` query argument so the future Search Sunner
     * entry can prefill the recipient field without dirtying the form.
     * Use [writeKudo] to construct a concrete route value.
     */
    const val WRITE_KUDO_PATTERN: String = "route_write_kudo?recipientUserId={recipientUserId}"
    const val WRITE_KUDO_ARG_RECIPIENT: String = "recipientUserId"

    const val SEARCH: String = "route_search"
    const val PROFILE: String = "route_profile"

    // Sun*Kudos US11 — Open Secret Box flow. Delta-spec `kQk65hSYF2` (open
    // animation) is out of scope for the hub work; the placeholder destination
    // lands here and the real screen lands when that spec ships.
    const val SECRET_BOX_OPEN: String = "route_secret_box_open"

    /**
     * Viết Kudo US7 — Community Standards page. Currently a placeholder
     * destination; the real screen ships in a separate spec.
     */
    const val COMMUNITY_STANDARDS: String = "route_community_standards"

    /**
     * Award Detail destination is parameterized by `awardId`. Wired
     * to the real `AwardDetailScreen` since the Award_Top talent spec
     * (`c-QM3_zjkG`) shipped — see `awarddetail/ui/AwardDetailScreen.kt`.
     */
    const val AWARD_DETAIL_PATTERN: String = "route_award_detail/{awardId}"

    fun awardDetail(awardId: String): String = "route_award_detail/$awardId"

    /**
     * Constructs a concrete `WRITE_KUDO` route value. Pass [recipientUserId]
     * from the Search Sunner flow to prefill the composer's recipient field;
     * leave `null` for the canonical hub Send-pill / Home FAB entries.
     */
    fun writeKudo(recipientUserId: String? = null): String =
        if (recipientUserId == null) {
            "route_write_kudo"
        } else {
            "route_write_kudo?recipientUserId=$recipientUserId"
        }
}
