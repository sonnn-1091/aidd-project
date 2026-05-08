package com.example.aiddproject.core.logging

import timber.log.Timber

/**
 * Debug-only Timber tree that scrubs token-like substrings before any log line reaches
 * Logcat (Constitution Principle IV; SC-005). Catches:
 *   - Bearer / access tokens (`access_token=…`, `Bearer …`, `eyJ…` JWT prefixes)
 *   - Refresh tokens (`refresh_token=…`)
 *   - Google ID tokens (long Base64URL strings beginning with `eyJ`)
 *   - Any value associated with the keys `password`, `secret`, `apiKey`, `anonKey`.
 *   - Home PII keys (TR-007): `award.name`, `award.description`,
 *     `notification.title`, `notification.body` — values may contain spaces, so
 *     quoted strings are supported alongside unquoted single tokens.
 *
 * Release builds DO NOT plant any tree (the `Timber.plant` call in `AIDDApplication` is
 * guarded by `BuildConfig.DEBUG`), so verbose logs are stripped entirely. CI also greps
 * the release artifact for token-like patterns as a belt-and-braces check (T069).
 */
class SecureTimberTree : Timber.DebugTree() {
    override fun log(
        priority: Int,
        tag: String?,
        message: String,
        t: Throwable?,
    ) {
        super.log(priority, tag, scrubLogMessage(message), t?.let(::ScrubbedThrowable))
    }
}

internal const val SCRUB_REDACTED: String = "[REDACTED]"

/**
 * Group 1 captures the leading literal (key + delimiter or `Bearer `) so it can be
 * preserved while the value is replaced. Order matters: more-specific patterns run
 * first to avoid the generic JWT regex eating already-scrubbed prefixes.
 */
private val SCRUB_PATTERNS: List<Regex> =
    listOf(
        Regex(
            """(\b(?:access_token|refresh_token|id_token|anon_?key|api_?key|password|secret)\b\s*[=:]\s*)["']?[A-Za-z0-9._\-+/=]+["']?""",
            RegexOption.IGNORE_CASE,
        ),
        // Home PII keys (TR-007). Values may contain spaces (e.g. a notification
        // title "Welcome to SAA 2025"), so we match a quoted string OR an
        // unquoted single token. Using `\Q…\E` literal segments inside the
        // alternation keeps the dots literal without escaping each one.
        Regex(
            """(\b(?:award\.name|award\.description|notification\.title|notification\.body)\b\s*[=:]\s*)(?:"[^"]*"|'[^']*'|\S+)""",
            RegexOption.IGNORE_CASE,
        ),
        Regex("""(\bBearer\s+)[A-Za-z0-9._\-+/=]+"""),
        Regex("""()eyJ[A-Za-z0-9._\-+/=]{20,}"""),
    )

internal fun scrubLogMessage(input: String): String {
    var out = input
    for (regex in SCRUB_PATTERNS) out = regex.replace(out, "$1$SCRUB_REDACTED")
    return out
}

/** Throwable wrapper that scrubs message/causedBy chain so stack-trace renderers
 *  don't leak the original message verbatim. */
private class ScrubbedThrowable(
    original: Throwable,
) : Throwable(scrubLogMessage(original.message.orEmpty()), original.cause?.let(::ScrubbedThrowable)) {
    init {
        stackTrace = original.stackTrace
    }
}
