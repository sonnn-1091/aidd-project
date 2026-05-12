package com.example.aiddproject.kudos.domain

/** Top 10 recent gift recipients list row (spec § US12). */
data class GiftRecipient(
    val userId: String,
    val fullName: String,
    val avatarUrl: String? = null,
    val rewardName: String,
)
