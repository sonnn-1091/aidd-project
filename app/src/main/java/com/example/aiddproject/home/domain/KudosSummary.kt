package com.example.aiddproject.home.domain

/**
 * Server-side feature flag + Kudos block content (drives section visibility on Home
 * per FR-005). Backed by `public.kudos_settings` (RPC `kudos_summary()`).
 */
data class KudosSummary(
    val isKudosAvailable: Boolean,
    val bannerImageUrl: String? = null,
    val badgeText: String? = null,
    val descriptionText: String = "",
)
