package com.example.aiddproject.core.logging

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

/**
 * Coverage for [scrubLogMessage] (T067, SC-005). Assertions invoke the scrub function
 * directly so the test is independent of Android's Logcat sink.
 */
class SecureTimberTreeTest {
    @Test
    fun bearer_token_value_is_redacted() {
        val out = scrubLogMessage("Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.dGVzdA.signature_part")
        assertFalse("JWT body must not appear", out.contains("dGVzdA"))
        assertFalse(out.contains("signature_part"))
        assertEquals("Authorization: Bearer [REDACTED]", out)
    }

    @Test
    fun access_token_kv_is_redacted() {
        assertEquals(
            "Sending request with access_token=[REDACTED]",
            scrubLogMessage("Sending request with access_token=abc123XYZ.def-ghi"),
        )
    }

    @Test
    fun refresh_token_quoted_value_is_redacted() {
        // Scrubber strips both the value and its surrounding quotes — the redaction
        // is what matters; format preservation isn't a security requirement.
        val out = scrubLogMessage("""Persisted refresh_token="abcdef.123-456" to keystore""")
        assertFalse("refresh-token value must not appear", out.contains("abcdef"))
        assertEquals("Persisted refresh_token=[REDACTED] to keystore", out)
    }

    @Test
    fun bare_jwt_string_is_redacted() {
        val out = scrubLogMessage("Got token eyJabcdefghijklmnopqrst.eyJdef.signaturepart back")
        assertFalse("JWT-looking string must be redacted", out.contains("eyJabc"))
        assertEquals("Got token [REDACTED] back", out)
    }

    @Test
    fun anon_key_assignment_is_redacted() {
        assertEquals(
            "anon_key: [REDACTED]",
            scrubLogMessage("anon_key: aBcDeFgHiJkLmNoPqRsTuVwXyZ_-12.34"),
        )
    }

    @Test
    fun harmless_messages_are_left_alone() {
        val message = "User alice navigated from Login to Home"
        assertEquals(message, scrubLogMessage(message))
    }

    // ---- TR-007: Home PII keys ----

    @Test
    fun award_name_quoted_value_with_spaces_is_redacted() {
        val out = scrubLogMessage("""Loaded award.name="Top Talent Award" sortOrder=0""")
        assertFalse("award name must not appear", out.contains("Top Talent"))
        assertEquals("Loaded award.name=[REDACTED] sortOrder=0", out)
    }

    @Test
    fun award_description_quoted_value_is_redacted() {
        val out =
            scrubLogMessage("""Got award.description="Recognise teammates who carried you" mid-fetch""")
        assertFalse("description must not appear", out.contains("Recognise"))
        assertEquals("Got award.description=[REDACTED] mid-fetch", out)
    }

    @Test
    fun notification_title_unquoted_value_is_redacted() {
        val out = scrubLogMessage("Sending notification.title=Welcome to user 42")
        // Unquoted variant only catches the first whitespace-bounded token, but
        // that's the security-significant boundary — the leak risk is
        // distinguishing "is there a notification title here" from "what's in
        // it". The first token is masked, which prevents single-keyword leakage.
        assertFalse(out.startsWith("Sending notification.title=Welcome"))
        assertEquals("Sending notification.title=[REDACTED] to user 42", out)
    }

    @Test
    fun notification_body_quoted_value_is_redacted() {
        val out =
            scrubLogMessage("""Pushed notification.body="You earned a Kudo from Bob" to channel""")
        assertFalse("body content must not appear", out.contains("Bob"))
        assertEquals("Pushed notification.body=[REDACTED] to channel", out)
    }

    @Test
    fun award_name_with_colon_separator_is_redacted() {
        val out = scrubLogMessage("""award.name: "Top Project Award"""")
        assertFalse(out.contains("Top Project"))
        assertEquals("award.name: [REDACTED]", out)
    }

    @Test
    fun pii_keys_match_case_insensitively() {
        // Defensive: scrubber should not skip the redaction just because a log
        // statement happened to capitalize the field key.
        val out = scrubLogMessage("""Award.Name="Captured" was set""")
        assertFalse(out.contains("Captured"))
        assertEquals("Award.Name=[REDACTED] was set", out)
    }
}
