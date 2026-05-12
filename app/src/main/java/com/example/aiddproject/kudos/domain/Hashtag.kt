package com.example.aiddproject.kudos.domain

/** Filter chip + tag rendered inside Kudos cards (spec § US3). */
data class Hashtag(
    val id: String,
    val tagName: String,
)
