package com.example.aiddproject.kudos.compose.domain

/**
 * Q-W-1 — Markdown-subset wrapper for the composer's message body.
 *
 * Stored on the server as the raw Markdown string (`**bold**`,
 * `*italic*`, `~~strike~~`, `1.`/`2.` numbered lists, `> quote`,
 * `[label](url)` links, `@FullName` plain-text mentions); the hub's
 * `KudosFeedCard` renderer round-trips it back to formatted text.
 *
 * [plainText] is the non-formatting projection — used by:
 *   - the 1–1000-character validator (whitespace + format markers are
 *     stripped before counting),
 *   - the live character counter under the textarea,
 *   - the empty-vs-whitespace-only check in
 *     [com.example.aiddproject.kudos.compose.domain.WriteKudoValidators.validateMessage].
 *
 * Per plan § Phase 1, the MVP slice ships with markdown == plainText
 * (no toolbar). Phase 6 / US4 swaps in the real `MessageMarkdown`
 * encode/decode and the toolbar.
 */
data class RichTextValue(
    val markdown: String,
    val plainText: String,
) {
    val length: Int
        get() = plainText.length

    val isBlank: Boolean
        get() = plainText.isBlank()

    companion object {
        val Empty: RichTextValue = RichTextValue(markdown = "", plainText = "")

        /** MVP helper — Phase 1 / US1 treats input as plain text. */
        fun ofPlainText(text: String): RichTextValue = RichTextValue(markdown = text, plainText = text)
    }
}
