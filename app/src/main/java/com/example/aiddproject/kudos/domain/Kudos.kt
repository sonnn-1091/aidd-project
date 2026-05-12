package com.example.aiddproject.kudos.domain

/**
 * Single Kudos message rendered in the Highlight carousel + the All
 * Kudos feed (spec fO0Kt19sZZ § US1, US4, US5).
 *
 * Per-viewer derived fields ([senderVisibleToMe], [likeDisabledForMe],
 * [likedByCurrentUser]) are computed server-side against the requesting
 * user's identity so the client never has to know its own user id to
 * decide whether to show the sender, gate the heart, or render the
 * filled-heart state. The client trusts these flags as-is.
 *
 * Q-K-1 (special-day x2 heart math) is read off [com.example.aiddproject.kudos.domain.SystemFlags];
 * Q-K-3 (anonymous visibility) is encoded as [senderVisibleToMe];
 * Q-K-5 (self-like blocking) is encoded as [likeDisabledForMe].
 */
data class Kudos(
    val id: String,
    val sender: SunnerNode,
    val recipient: SunnerNode,
    val message: String,
    val title: String? = null,
    val hashtags: List<Hashtag> = emptyList(),
    val photos: List<String> = emptyList(),
    val createdAt: String,
    val heartCount: Int = 0,
    val likedByCurrentUser: Boolean = false,
    val senderVisibleToMe: Boolean = true,
    val likeDisabledForMe: Boolean = false,
    val anonymousNickname: String? = null,
    val isAnonymous: Boolean = false,
)
