package com.example.aiddproject.kudos.search.ui

/**
 * Test-tag constants for the Search Sunner screen (Figma frame
 * `3jgwke3E8O`). Centralised so refactors stay safe.
 */
object SearchSunnerTestTags {
    const val SCREEN: String = "search_sunner_screen"
    const val BACK_BUTTON: String = "search_sunner_back_button"
    const val SEARCH_BAR: String = "search_sunner_search_bar"
    const val RECENT_LABEL: String = "search_sunner_recent_label"
    const val VIEW_ALL_BUTTON: String = "search_sunner_view_all_button"

    fun recentRowTag(userId: String): String = "search_sunner_recent_row_$userId"
    fun removeButtonTag(userId: String): String = "search_sunner_remove_button_$userId"
}
