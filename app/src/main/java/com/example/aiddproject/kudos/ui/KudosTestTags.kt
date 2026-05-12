package com.example.aiddproject.kudos.ui

/**
 * Stable [androidx.compose.ui.platform.testTag] values for the Sun*Kudos
 * hub composables. Centralised so production composables + Compose UI
 * tests share one source — phases 3, 5, 6, 7, 9, 10, 11, 12 all
 * reference these constants.
 */
object KudosTestTags {
    const val SCREEN: String = "kudos_screen"
    const val HERO: String = "kudos_hero"
    const val SEND_KUDOS_CTA: String = "kudos_send_cta"
    const val FILTER_ROW: String = "kudos_filter_row"
    const val FILTER_HASHTAG_TRIGGER: String = "kudos_filter_hashtag_trigger"
    const val FILTER_DEPARTMENT_TRIGGER: String = "kudos_filter_department_trigger"
    const val HIGHLIGHT: String = "kudos_highlight"
    const val SPOTLIGHT: String = "kudos_spotlight"
    const val FEED: String = "kudos_feed"
    const val STATS: String = "kudos_stats"
    const val TOP_TEN: String = "kudos_top_ten"
    const val OPEN_SECRET_BOX_CTA: String = "kudos_open_secret_box_cta"
    const val SNACKBAR_HOST: String = "kudos_snackbar_host"
}
