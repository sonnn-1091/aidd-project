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

    // ─────────────────────── Toolbar transforms (T078) ──────────────────────
    //
    // Each transform takes a [value] + a [selection] range (inferred from
    // the user's current cursor / highlight in the textarea) and returns
    // a new [RichTextValue] with the formatting applied around the
    // selected substring. If [selection] is empty (a caret), the transform
    // inserts the formatting markers around an empty span so the user can
    // type inside.

    fun applyBold(
        value: RichTextValue,
        selection: IntRange,
    ): RichTextValue = wrap(value, selection, "**", "**")

    fun applyItalic(
        value: RichTextValue,
        selection: IntRange,
    ): RichTextValue = wrap(value, selection, "*", "*")

    fun applyStrikethrough(
        value: RichTextValue,
        selection: IntRange,
    ): RichTextValue = wrap(value, selection, "~~", "~~")

    /** Number every non-blank line in the selection (or the line containing the caret). */
    fun applyNumberedList(
        value: RichTextValue,
        selection: IntRange,
    ): RichTextValue = applyLinePrefix(value, selection, numbered = true)

    /** Prefix every non-blank line in the selection with `> ` (toggle). */
    fun applyQuote(
        value: RichTextValue,
        selection: IntRange,
    ): RichTextValue = applyLinePrefix(value, selection, numbered = false, prefix = "> ")

    /**
     * Wrap the selected text in a markdown hyperlink. If [selection] is
     * empty, inserts `[url](url)` at the caret.
     */
    fun applyLink(
        value: RichTextValue,
        selection: IntRange,
        url: String,
    ): RichTextValue {
        val src = value.markdown
        val safeRange = selection.coerceIntoBounds(src.length)
        val before = src.substring(0, safeRange.first)
        val mid = src.substring(safeRange.first, safeRange.last + 1)
        val after = src.substring(safeRange.last + 1)
        val label = if (mid.isEmpty()) url else mid
        val replacement = "[$label]($url)"
        return decode(before + replacement + after)
    }

    private fun wrap(
        value: RichTextValue,
        selection: IntRange,
        open: String,
        close: String,
    ): RichTextValue {
        val src = value.markdown
        val safeRange = selection.coerceIntoBounds(src.length)
        val before = src.substring(0, safeRange.first)
        val mid = src.substring(safeRange.first, safeRange.last + 1)
        val after = src.substring(safeRange.last + 1)
        return decode(before + open + mid + close + after)
    }

    private fun applyLinePrefix(
        value: RichTextValue,
        selection: IntRange,
        numbered: Boolean,
        prefix: String = "",
    ): RichTextValue {
        val src = value.markdown
        if (src.isEmpty()) {
            return decode(if (numbered) "1. " else prefix)
        }
        val safeRange = selection.coerceIntoBounds(src.length)
        val before = src.substring(0, safeRange.first)
        val mid = src.substring(safeRange.first, safeRange.last + 1).ifEmpty { src.substring(safeRange.first) }
        val after = if (mid == src.substring(safeRange.first)) "" else src.substring(safeRange.last + 1)
        val transformed =
            mid.lines().mapIndexed { index, line ->
                if (line.isBlank()) {
                    line
                } else if (numbered) {
                    "${index + 1}. $line"
                } else {
                    "$prefix$line"
                }
            }.joinToString("\n")
        return decode(before + transformed + after)
    }

    private fun IntRange.coerceIntoBounds(length: Int): IntRange {
        val start = first.coerceIn(0, length)
        val end = last.coerceIn(start - 1, length - 1).coerceAtLeast(start - 1)
        return start..end
    }

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
