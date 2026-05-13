package com.example.aiddproject.kudos.compose.domain

import android.net.Uri

/**
 * One image attachment in the Viết Kudo composer (US5).
 *
 * Q-W-2 — **no draft uploads**. While the user is composing, [storagePath]
 * is `null` and the thumbnail renders from [localUri] via Coil. The
 * submit flow ([com.example.aiddproject.kudos.compose.ui.WriteKudoViewModel.submit])
 * uploads each image sequentially to
 * `kudos-attachments/{auth.uid()}/{kudoId}/{index}_{filename}` and sets
 * [storagePath] before the INSERT.
 *
 * [clientId] is a stable local key (UUID) used by the UI to identify a
 * thumbnail across removal / reordering. It does NOT survive the round-
 * trip; only [storagePath] lands in `kudos.image_ids`.
 */
data class UploadedImage(
    val clientId: String,
    val localUri: Uri,
    val sizeBytes: Long,
    val mime: String,
    val storagePath: String? = null,
) {
    val isUploaded: Boolean
        get() = storagePath != null
}
