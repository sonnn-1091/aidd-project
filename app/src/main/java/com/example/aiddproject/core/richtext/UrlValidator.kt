package com.example.aiddproject.core.richtext

import java.net.URI
import java.net.URISyntaxException

/**
 * RFC 3986 sanity check for the C.5 Link insert dialog (T026).
 *
 * Accepts only `http://` and `https://` URLs that parse cleanly with a
 * non-empty host. Rejects `javascript:`, `data:`, `vbscript:`, `file:`,
 * `ftp:`, etc. per OWASP A03 — the hub's `KudosFeedCard` renderer opens
 * links via `Intent.ACTION_VIEW` and we don't want a malicious sender
 * embedding a script-scheme link in a Kudo.
 *
 * Pure JVM — no Android framework imports — so it's exercised directly
 * by `UrlValidatorTest` without Robolectric.
 */
object UrlValidator {
    private val ALLOWED_SCHEMES = setOf("http", "https")

    fun isValid(url: String): Boolean {
        val trimmed = url.trim()
        if (trimmed.isEmpty()) return false

        val parsed =
            try {
                URI(trimmed)
            } catch (_: URISyntaxException) {
                return false
            } catch (_: IllegalArgumentException) {
                return false
            }

        val scheme = parsed.scheme?.lowercase() ?: return false
        if (scheme !in ALLOWED_SCHEMES) return false

        val host = parsed.host ?: return false
        return host.isNotBlank()
    }
}
