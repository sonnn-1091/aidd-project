package com.example.aiddproject.core.locale

import java.util.Locale

/**
 * Supported display languages on every app surface that hosts the language pill
 * (Login, Home, and any future chrome screen — see Language Dropdown spec
 * `uUvW6Qm1ve`).
 *
 * - [code]: short two-letter form shown in the language selector ("VN", "EN").
 *   Also the persisted DataStore token — see [LanguagePreferenceRepository].
 * - [tag]: BCP-47 tag used to build a [Locale] for resource lookup.
 * - [nativeName]: full localized name shown in the dropdown items and read by
 *   assistive tech (TalkBack reads `nativeName`, not `code`).
 * - [flagEmoji]: country flag rendered in the header pill (system emoji — no
 *   asset file).
 *
 * **2026-05-08**: Japanese (JA) removed per Language Dropdown spec § Out of
 * Scope (Q1 resolution). The Figma frame enumerates only VN + EN. Existing
 * installs that persisted "JA" fall back to VN silently via [fromCode].
 * `values-ja/strings.xml` stays on disk as a dead resource for one release
 * cycle but is no longer surfaced at runtime.
 */
enum class Language(
    val code: String,
    val tag: String,
    val nativeName: String,
    val flagEmoji: String,
) {
    VN(code = "VN", tag = "vi", nativeName = "Tiếng Việt", flagEmoji = "🇻🇳"),
    EN(code = "EN", tag = "en", nativeName = "English", flagEmoji = "🇺🇸"),
    ;

    fun toLocale(): Locale = Locale.forLanguageTag(tag)

    companion object {
        val Default: Language = VN

        /**
         * Returns the matching [Language] for the persisted code, falling back to
         * [Default] for null, empty, or unknown codes (including the orphaned
         * "JA" token from existing installs that pre-date the JA removal).
         */
        fun fromCode(code: String?): Language = entries.firstOrNull { it.code == code } ?: Default
    }
}
