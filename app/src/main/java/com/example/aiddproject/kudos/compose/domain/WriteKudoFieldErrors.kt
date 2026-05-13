package com.example.aiddproject.kudos.compose.domain

import androidx.annotation.StringRes

/**
 * Field-level error bag for the Viết Kudo composer (US3 / `0le8xKnFE_`).
 *
 * One nullable string-resource slot per required field plus an optional
 * `images` slot for the Q-W-2 submit-time upload failure path (FR-011).
 *
 * Encoded as a `data class` rather than a `Map<FieldKey, Int>` so Compose
 * recomposition skips when an unrelated field changes — equality is
 * structural, the empty case (no errors) is shared via [None].
 *
 * Per spec US3 Scenario 8 + plan § Phase 5, the VM clears the field's
 * own slot when the user edits that field; submitting (or tapping the
 * disabled Send) repopulates them all at once.
 */
data class WriteKudoFieldErrors(
    @StringRes val recipient: Int? = null,
    @StringRes val title: Int? = null,
    @StringRes val message: Int? = null,
    @StringRes val hashtags: Int? = null,
    @StringRes val images: Int? = null,
) {
    val hasAny: Boolean
        get() = recipient != null || title != null || message != null || hashtags != null || images != null

    companion object {
        val None: WriteKudoFieldErrors = WriteKudoFieldErrors()
    }
}
