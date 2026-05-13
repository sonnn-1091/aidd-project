package com.example.aiddproject.kudos.compose.domain

import com.example.aiddproject.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Unit tests for [WriteKudoValidators] (T024 / spec § Data Requirements).
 *
 * Boundary cases per spec edge case "Exactly-boundary inputs": title=1
 * char, title=100 chars, message=1 char, message=1000 chars, hashtags=1,
 * hashtags=5.
 */
class WriteKudoValidatorsTest {
    // ─────────────────────────── validateTitle ──────────────────────────

    @Test
    fun validateTitle_emptyReturnsRequiredError() {
        assertEquals(R.string.write_kudo_error_title_required, WriteKudoValidators.validateTitle(""))
    }

    @Test
    fun validateTitle_whitespaceOnlyReturnsRequiredError() {
        assertEquals(R.string.write_kudo_error_title_required, WriteKudoValidators.validateTitle("   "))
    }

    @Test
    fun validateTitle_oneCharAccepted() {
        assertNull(WriteKudoValidators.validateTitle("A"))
    }

    @Test
    fun validateTitle_exactly100CharsAccepted() {
        assertNull(WriteKudoValidators.validateTitle("A".repeat(100)))
    }

    @Test
    fun validateTitle_over100CharsReturnsTooLongError() {
        assertEquals(
            R.string.write_kudo_error_title_too_long,
            WriteKudoValidators.validateTitle("A".repeat(101)),
        )
    }

    // ────────────────────────── validateMessage ─────────────────────────

    @Test
    fun validateMessage_emptyReturnsRequiredError() {
        assertEquals(
            R.string.write_kudo_error_message_required,
            WriteKudoValidators.validateMessage(RichTextValue.Empty),
        )
    }

    @Test
    fun validateMessage_onlyWhitespaceReturnsBlankError() {
        assertEquals(
            R.string.write_kudo_error_message_blank,
            WriteKudoValidators.validateMessage(RichTextValue.ofPlainText("   \n  \t")),
        )
    }

    @Test
    fun validateMessage_oneCharAccepted() {
        assertNull(WriteKudoValidators.validateMessage(RichTextValue.ofPlainText("A")))
    }

    @Test
    fun validateMessage_exactly1000CharsAccepted() {
        assertNull(WriteKudoValidators.validateMessage(RichTextValue.ofPlainText("A".repeat(1000))))
    }

    @Test
    fun validateMessage_over1000CharsReturnsTooLongError() {
        assertEquals(
            R.string.write_kudo_error_message_too_long,
            WriteKudoValidators.validateMessage(RichTextValue.ofPlainText("A".repeat(1001))),
        )
    }

    // ────────────────────────── validateHashtags ────────────────────────

    @Test
    fun validateHashtags_emptyReturnsRequiredError() {
        assertEquals(
            R.string.write_kudo_error_hashtags_required,
            WriteKudoValidators.validateHashtags(emptyList()),
        )
    }

    @Test
    fun validateHashtags_oneAccepted() {
        assertNull(WriteKudoValidators.validateHashtags(listOf("teamwork")))
    }

    @Test
    fun validateHashtags_fiveAccepted() {
        assertNull(WriteKudoValidators.validateHashtags(listOf("a", "b", "c", "d", "e")))
    }

    @Test
    fun validateHashtags_sixReturnsMaxError() {
        assertEquals(
            R.string.write_kudo_error_hashtags_max,
            WriteKudoValidators.validateHashtags(listOf("a", "b", "c", "d", "e", "f")),
        )
    }

    // ────────────────────────── validateRecipient ───────────────────────

    @Test
    fun validateRecipient_nullReturnsRequiredError() {
        assertEquals(
            R.string.write_kudo_error_recipient_required,
            WriteKudoValidators.validateRecipient(null, currentUserId = "u-self"),
        )
    }

    @Test
    fun validateRecipient_emptyReturnsRequiredError() {
        assertEquals(
            R.string.write_kudo_error_recipient_required,
            WriteKudoValidators.validateRecipient("", currentUserId = "u-self"),
        )
    }

    @Test
    fun validateRecipient_currentUserReturnsSelfSendError() {
        assertEquals(
            R.string.write_kudo_error_recipient_self,
            WriteKudoValidators.validateRecipient("u-self", currentUserId = "u-self"),
        )
    }

    @Test
    fun validateRecipient_otherUserAccepted() {
        assertNull(WriteKudoValidators.validateRecipient("u-other", currentUserId = "u-self"))
    }

    @Test
    fun validateRecipient_nullCurrentUser_skipsSelfSendCheck() {
        assertNull(WriteKudoValidators.validateRecipient("u-anyone", currentUserId = null))
    }
}
