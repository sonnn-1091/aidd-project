package com.example.aiddproject.home.domain

/**
 * Award catalog entity rendered in the carousel on Home (`mms_4.2_award list`).
 *
 * Per spec Q-Home-8 the card slot consumes ONLY what the design instance
 * (`Top Talent Award` `6885:8051`) exposes — no extra API-driven fields.
 */
data class Award(
    val id: String,
    val name: String,
    val thumbnailUrl: String? = null,
    val sortOrder: Int = 0,
)
