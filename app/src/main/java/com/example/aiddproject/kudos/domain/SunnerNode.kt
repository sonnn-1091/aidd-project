package com.example.aiddproject.kudos.domain

/**
 * Sender/recipient identity rendered in the Highlight + All Kudos
 * cards. Also doubles as a Spotlight Board node (the graph payload
 * embeds the same shape — see [SpotlightGraph]).
 *
 * [starTier] is read from `users.star_tier` (server-derived; 0..3) and
 * drives the [com.example.aiddproject.kudos.ui.components.StarTierBadge]
 * render. [department] is null when the user is unaffiliated or when
 * the field is hidden by privacy settings.
 */
data class SunnerNode(
    val id: String,
    val fullName: String,
    val avatarUrl: String? = null,
    val starTier: Int = 0,
    val department: Department? = null,
)
