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
}
