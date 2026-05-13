package com.example.aiddproject.core.richtext

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for [UrlValidator] (T026). Pure JVM — no Robolectric.
 */
class UrlValidatorTest {
    @Test
    fun https_url_isValid() {
        assertTrue(UrlValidator.isValid("https://example.com"))
    }

    @Test
    fun http_url_isValid() {
        assertTrue(UrlValidator.isValid("http://example.com"))
    }

    @Test
    fun url_with_path_and_query_isValid() {
        assertTrue(UrlValidator.isValid("https://example.com/path?q=1#frag"))
    }

    @Test
    fun http_localhost_with_port_isValid() {
        assertTrue(UrlValidator.isValid("http://localhost:8080/api"))
    }

    @Test
    fun plain_text_isInvalid() {
        assertFalse(UrlValidator.isValid("not-a-valid-url"))
    }

    @Test
    fun empty_isInvalid() {
        assertFalse(UrlValidator.isValid(""))
    }

    @Test
    fun whitespace_isInvalid() {
        assertFalse(UrlValidator.isValid("   "))
    }

    @Test
    fun javascript_scheme_isRejected_owasp_a03() {
        assertFalse(UrlValidator.isValid("javascript:alert(1)"))
    }

    @Test
    fun data_uri_isRejected_owasp_a03() {
        assertFalse(UrlValidator.isValid("data:text/html,<script>alert(1)</script>"))
    }

    @Test
    fun vbscript_scheme_isRejected() {
        assertFalse(UrlValidator.isValid("vbscript:msgbox(1)"))
    }

    @Test
    fun file_scheme_isRejected() {
        assertFalse(UrlValidator.isValid("file:///etc/passwd"))
    }

    @Test
    fun ftp_scheme_isRejected() {
        assertFalse(UrlValidator.isValid("ftp://example.com/file"))
    }

    @Test
    fun https_with_no_host_isInvalid() {
        assertFalse(UrlValidator.isValid("https://"))
    }

    @Test
    fun scheme_case_insensitive_HTTPS_isValid() {
        assertTrue(UrlValidator.isValid("HTTPS://Example.com"))
    }

    @Test
    fun leading_trailing_whitespace_trimmed_andValidated() {
        assertTrue(UrlValidator.isValid("  https://example.com  "))
    }
}
