package com.example.aiddproject.kudos.domain

/**
 * Lightweight pointer to a not-yet-opened Secret Box (spec § US11).
 *
 * The CTA only needs to know "is there one to open" + which id to
 * pass to [com.example.aiddproject.kudos.data.KudosRepository.openSecretBox].
 * The reveal animation + reward payload live in delta-spec
 * `kQk65hSYF2` and are out of scope for the hub.
 */
data class SecretBoxRef(
    val id: String,
)

/**
 * Reward revealed when a box is opened. Returned by the
 * `open_secret_box` RPC atomically — the row in `secret_boxes` is
 * flipped to opened in the same transaction.
 */
data class SecretBoxReward(
    val boxId: String,
    val name: String,
    val description: String,
)
