package com.example.aiddproject.kudos.compose.domain

/**
 * Immutable payload sent to [com.example.aiddproject.kudos.data.KudosRepository.createKudo]
 * after every client validation passes and (per Q-W-2) every image upload
 * has successfully landed in Storage.
 *
 * The [id] is a **client-generated** UUID — it doubles as the new `kudos`
 * row's primary key AND the Storage subfolder name, so the upload phase
 * can run before the INSERT phase under the same kudoId path without
 * needing a server round-trip to learn the id (see plan § Image upload
 * model).
 *
 * The [imageIds] list contains Storage paths returned by
 * [com.example.aiddproject.kudos.data.KudosRepository.uploadKudoImage];
 * if the kudos INSERT fails, the VM rolls back by deleting every
 * uploaded path so no orphans accumulate.
 */
data class WriteKudoDraft(
    val id: String,
    val recipientId: String,
    val title: String,
    val message: String,
    val tags: List<String>,
    val imageIds: List<String> = emptyList(),
    val isAnonymous: Boolean = false,
    val anonymousNickname: String? = null,
)
