package com.example.aiddproject.kudos.ui

/**
 * Stable [androidx.compose.ui.platform.testTag] values for the Sun*Kudos
 * hub composables. Centralised so production composables + Compose UI
 * tests share one source — phases 3, 5, 6, 7, 9, 10, 11, 12 all
 * reference these constants.
 */
object KudosTestTags {
    const val SCREEN: String = "kudos_screen"
    const val LAZY_COLUMN: String = "kudos_lazy_column"
    const val HERO: String = "kudos_hero"
    const val SEND_KUDOS_CTA: String = "kudos_send_cta"
    const val FILTER_ROW: String = "kudos_filter_row"
    const val FILTER_HASHTAG_TRIGGER: String = "kudos_filter_hashtag_trigger"
    const val FILTER_DEPARTMENT_TRIGGER: String = "kudos_filter_department_trigger"
    const val HIGHLIGHT: String = "kudos_highlight"
    const val HIGHLIGHT_PAGER: String = "kudos_highlight_pager"
    const val HIGHLIGHT_CARD: String = "kudos_highlight_card"
    const val PAGE_INDICATOR: String = "kudos_page_indicator"
    const val HEART_ICON: String = "kudos_heart_icon"
    const val STAR_TIER_BADGE: String = "kudos_star_tier_badge"
    const val SPOTLIGHT: String = "kudos_spotlight"
    const val SPOTLIGHT_CANVAS: String = "kudos_spotlight_canvas"
    const val SPOTLIGHT_SEARCH_INPUT: String = "kudos_spotlight_search_input"
    const val FEED: String = "kudos_feed"
    const val FEED_CARD: String = "kudos_feed_card"
    const val VIEW_ALL_KUDOS_LINK: String = "kudos_view_all_link"
    const val STATS: String = "kudos_stats"
    const val TOP_TEN: String = "kudos_top_ten"
    const val OPEN_SECRET_BOX_CTA: String = "kudos_open_secret_box_cta"
    const val SNACKBAR_HOST: String = "kudos_snackbar_host"
}
