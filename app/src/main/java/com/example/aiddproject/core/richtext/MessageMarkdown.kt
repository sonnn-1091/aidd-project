package com.example.aiddproject.core.richtext

import com.example.aiddproject.kudos.compose.domain.RichTextValue

/**
 * Q-W-1 — Markdown-subset codec for the Viết Kudo composer message body
 * (T025). Round-trip-lossless with the hub's `KudosFeedCard` renderer.
 *
 * **Phase 1 / US1 MVP scope**: the only transform shipped here is the
 * plain-text round-trip. The 6 toolbar transforms (bold / italic /
 * strikethrough / numbered list / quote / link) ship in Phase 6 / US4
 * alongside the `FormattingToolbar` composable — the test class already
 * pins their contracts so they can be added without touching the
 * existing surface.
 *
 * Supported markdown:
 *   - `**bold**`
 *   - `*italic*`
 *   - `~~strike~~`
 *   - `1.`/`2.` numbered list lines
 *   - `> quote` blockquote lines
 *   - `[label](url)` hyperlinks
 *   - `@FullName` plain-text mentions (no special syntax)
 *
 * [plainTextOf] strips the formatting markers from the markdown string
 * so the 1–1000-character validator (and the live counter) count only
 * the visible characters.
 */
object MessageMarkdown {
    /** Encode a [RichTextValue] back to its raw markdown string. */
    fun encode(value: RichTextValue): String = value.markdown

    /**
     * Decode a markdown string into a [RichTextValue] with a synthesised
     * [RichTextValue.plainText] projection.
     */
    fun decode(markdown: String): RichTextValue =
        RichTextValue(
            markdown = markdown,
            plainText = plainTextOf(markdown),
        )

    /**
     * Strip the markdown formatting markers to compute the visible-text
     * projection. Applied in order: links → bold → strikethrough →
     * italic → numbered-list prefix → quote prefix.
     */
    fun plainTextOf(markdown: String): String {
        var s = markdown
        s = LINK_RE.replace(s) { it.groupValues[1] }
        s = BOLD_RE.replace(s) { it.groupValues[1] }
        s = STRIKE_RE.replace(s) { it.groupValues[1] }
        s = ITALIC_RE.replace(s) { it.groupValues[1] }
        s = NUMBERED_LIST_PREFIX_RE.replace(s, "")
        s = QUOTE_PREFIX_RE.replace(s, "")
        return s
    }

    private val LINK_RE = Regex("""\[([^\]]+)]\(([^)]+)\)""")
    private val BOLD_RE = Regex("""\*\*([^*]+)\*\*""")
    private val STRIKE_RE = Regex("""~~([^~]+)~~""")

    // Italic is single-star; the bold pass runs first so its `**` never
    // matches here.
    private val ITALIC_RE = Regex("""\*([^*]+)\*""")

    // "1. ", "12. ", etc. at the start of a line.
    private val NUMBERED_LIST_PREFIX_RE = Regex("""(?m)^\d+\.\s""")

    // "> " at the start of a line.
    private val QUOTE_PREFIX_RE = Regex("""(?m)^>\s""")
}
