package com.example.aiddproject.kudos.standards.ui

/**
 * Test tag constants for the Community Standards screen (Figma frame
 * `xms7csmDhD`). Co-located with the screen so refactors stay safe.
 */
object CommunityStandardsTestTags {
    const val SCREEN: String = "community_standards_screen"
    const val BACK_BUTTON: String = "community_standards_back_button"
    const val KV_BANNER: String = "community_standards_kv_banner"
    const val CRITERIA_LIST: String = "community_standards_criteria_list"
    const val SUPPORT_CONTACT: String = "community_standards_support_contact"

    /** Test tag for criterion row N (1-indexed). */
    fun criteriaRowTag(n: Int): String = "community_standards_criteria_row_$n"
}
