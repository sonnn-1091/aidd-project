package com.example.aiddproject.core.locale

import java.util.Locale

/**
 * Supported display languages on the Login screen and across the app.
 *
 * - [code]: short two-letter form shown in the language selector ("VN", "EN", "JA").
 * - [tag]: BCP-47 tag used to build a [Locale] for resource lookup.
 * - [nativeName]: full localized name shown in the dropdown items and read by
 *   assistive tech.
 * - [flagEmoji]: country flag rendered in the header pill — MoMorph node
 *   `MM_MEDIA_IC VN Flag` (and EN/JA peers) export no asset, so we use the system
 *   emoji per plan.md asset prep T009.
 */
enum class Language(
    val code: String,
    val tag: String,
    val nativeName: String,
    val flagEmoji: String,
) {
    VN(code = "VN", tag = "vi", nativeName = "Tiếng Việt", flagEmoji = "🇻🇳"),
    EN(code = "EN", tag = "en", nativeName = "English", flagEmoji = "🇺🇸"),
    JA(code = "JA", tag = "ja", nativeName = "日本語", flagEmoji = "🇯🇵"),
    ;

    fun toLocale(): Locale = Locale.forLanguageTag(tag)

    companion object {
        val Default: Language = VN

        /** Returns the matching [Language] for the persisted code, falling back to [Default]. */
        fun fromCode(code: String?): Language = entries.firstOrNull { it.code == code } ?: Default
    }
}
