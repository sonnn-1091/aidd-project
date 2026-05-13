package com.example.aiddproject.kudos.compose.domain

import androidx.annotation.StringRes
import com.example.aiddproject.R

/**
 * Pure validators for the Viết Kudo composer (T024 / spec § Data
 * Requirements). Each function returns `null` for valid input or a
 * `@StringRes` for the localized inline error message.
 *
 * The error mapping mirrors the spec's component-behavior contract:
 * recipient = required + not-self, title = 1–100 chars, message =
 * 1–1000 non-whitespace chars after trim, hashtags = 1–5 entries.
 *
 * These validators run BOTH on every field edit (to clear errors as
 * the user types) AND on Send (to populate the `WriteKudoFieldErrors`
 * for the disabled-tap reveal path). The server's RLS layer is the
 * authoritative gate (TR-002, TR-004); these are UX hints only.
 */
object WriteKudoValidators {
    /** US1 — recipient required + not current user. */
    @StringRes
    fun validateRecipient(
        recipientId: String?,
        currentUserId: String?,
    ): Int? =
        when {
            recipientId.isNullOrBlank() -> R.string.write_kudo_error_recipient_required
            currentUserId != null && recipientId == currentUserId -> R.string.write_kudo_error_recipient_self
            else -> null
        }

    /** US1 — title 1–100 chars. */
    @StringRes
    fun validateTitle(title: String): Int? =
        when {
            title.isBlank() -> R.string.write_kudo_error_title_required
            title.length > MAX_TITLE_LENGTH -> R.string.write_kudo_error_title_too_long
            else -> null
        }

    /** US1 — message 1–1000 non-whitespace chars after trim. */
    @StringRes
    fun validateMessage(message: RichTextValue): Int? =
        when {
            message.plainText.isEmpty() -> R.string.write_kudo_error_message_required
            message.plainText.isBlank() -> R.string.write_kudo_error_message_blank
            message.plainText.length > MAX_MESSAGE_LENGTH -> R.string.write_kudo_error_message_too_long
            else -> null
        }

    /** US1 — 1–5 hashtags required. */
    @StringRes
    fun validateHashtags(tags: List<String>): Int? =
        when {
            tags.isEmpty() -> R.string.write_kudo_error_hashtags_required
            tags.size > MAX_HASHTAGS -> R.string.write_kudo_error_hashtags_max
            else -> null
        }

    const val MAX_TITLE_LENGTH: Int = 100
    const val MAX_MESSAGE_LENGTH: Int = 1000
    const val MAX_HASHTAGS: Int = 5
}
