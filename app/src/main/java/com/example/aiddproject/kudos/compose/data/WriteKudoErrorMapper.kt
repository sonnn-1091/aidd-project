package com.example.aiddproject.kudos.compose.data

import com.example.aiddproject.R
import com.example.aiddproject.kudos.compose.domain.WriteKudoFieldErrors

/**
 * Maps a Supabase Postgrest exception from
 * [com.example.aiddproject.kudos.data.KudosRepository.createKudo] to a
 * field-targeted [WriteKudoFieldErrors] (T031 / FR-005).
 *
 * The composer's RLS policy (migration `20260513_kudos_insert_rls.sql`)
 * gates three server-side conditions; this mapper recognises each by
 * inspecting the thrown exception's message + code:
 *
 *   - Self-send (`recipient_id <> auth.uid()` clause) → recipient field
 *     gets `R.string.write_kudo_error_recipient_self`.
 *   - Tag whitelist (`tags <@ ...` clause) → hashtags field gets
 *     `R.string.write_kudo_error_hashtags_required` (re-used; the
 *     spec doesn't distinguish "unknown tag" from "no tag" inline).
 *   - Anything else → returns [WriteKudoFieldErrors.None]; the caller
 *     surfaces a generic snackbar via
 *     `R.string.write_kudo_error_submit_generic`.
 *
 * The mapper is pure (no Supabase types imported) so it can be unit-
 * tested with synthetic `Throwable`s — the production code lives in
 * the SupabaseKudosRepository layer where exception types are caught.
 */
object WriteKudoErrorMapper {
    /**
     * Inspect [throwable] and return a [WriteKudoFieldErrors] populated
     * for any field whose server-side rule was violated, or
     * [WriteKudoFieldErrors.None] if the exception is generic / unknown.
     */
    fun map(throwable: Throwable): WriteKudoFieldErrors {
        val message = (throwable.message ?: "").lowercase()

        if (matchesSelfSendViolation(message)) {
            return WriteKudoFieldErrors(recipient = R.string.write_kudo_error_recipient_self)
        }
        if (matchesTagWhitelistViolation(message)) {
            return WriteKudoFieldErrors(hashtags = R.string.write_kudo_error_hashtags_required)
        }
        return WriteKudoFieldErrors.None
    }

    private fun matchesSelfSendViolation(messageLower: String): Boolean =
        messageLower.contains("kudos_insert_self_only") ||
            (messageLower.contains("check") && messageLower.contains("recipient_id"))

    private fun matchesTagWhitelistViolation(messageLower: String): Boolean =
        messageLower.contains("kudos_insert_self_only") && messageLower.contains("tags") ||
            messageLower.contains("tags") && messageLower.contains("<@") ||
            messageLower.contains("tag") && messageLower.contains("not in")
}
