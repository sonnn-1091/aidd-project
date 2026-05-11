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
    const val WRITE_KUDO: String = "route_write_kudo"
    const val SEARCH: String = "route_search"
    const val PROFILE: String = "route_profile"

    /**
     * Award Detail destination is parameterized by `awardId`. Wired
     * to the real `AwardDetailScreen` since the Award_Top talent spec
     * (`c-QM3_zjkG`) shipped — see `awarddetail/ui/AwardDetailScreen.kt`.
     */
    const val AWARD_DETAIL_PATTERN: String = "route_award_detail/{awardId}"

    fun awardDetail(awardId: String): String = "route_award_detail/$awardId"
}
