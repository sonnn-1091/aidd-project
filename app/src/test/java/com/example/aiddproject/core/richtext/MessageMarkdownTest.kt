package com.example.aiddproject.core.richtext

import com.example.aiddproject.kudos.compose.domain.RichTextValue
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for [MessageMarkdown] (T025).
 *
 * Phase 1 / US1 MVP scope: only the round-trip codec + plain-text
 * projection are wired here. The 6 per-toolbar transforms (T078–T084)
 * are still `@Ignore`d at the test-method level — Phase 6 / US4 lifts
 * them when `FormattingToolbar` lands.
 */
class MessageMarkdownTest {
    // ───────────────────── round-trip / plainTextOf ─────────────────────

    @Test
    fun roundTrip_plainText_isLossless() {
        val src = "Cảm ơn bạn rất nhiều!"
        val decoded = MessageMarkdown.decode(src)
        assertEquals(src, decoded.markdown)
        assertEquals(src, decoded.plainText)
        assertEquals(src, MessageMarkdown.encode(decoded))
    }

    @Test
    fun roundTrip_bold_isLossless() {
        val src = "Hello **world** here"
        val decoded = MessageMarkdown.decode(src)
        assertEquals(src, decoded.markdown)
        assertEquals("Hello world here", decoded.plainText)
    }

    @Test
    fun roundTrip_italic_isLossless() {
        val src = "Hello *world* here"
        val decoded = MessageMarkdown.decode(src)
        assertEquals(src, decoded.markdown)
        assertEquals("Hello world here", decoded.plainText)
    }

    @Test
    fun roundTrip_strikethrough_isLossless() {
        val src = "Hello ~~world~~ here"
        val decoded = MessageMarkdown.decode(src)
        assertEquals(src, decoded.markdown)
        assertEquals("Hello world here", decoded.plainText)
    }

    @Test
    fun roundTrip_numberedList_isLossless() {
        val src = "1. first\n2. second"
        val decoded = MessageMarkdown.decode(src)
        assertEquals(src, decoded.markdown)
        assertEquals("first\nsecond", decoded.plainText)
    }

    @Test
    fun roundTrip_quote_isLossless() {
        val src = "> Quoted line"
        val decoded = MessageMarkdown.decode(src)
        assertEquals(src, decoded.markdown)
        assertEquals("Quoted line", decoded.plainText)
    }

    @Test
    fun roundTrip_link_isLossless() {
        val src = "See [the docs](https://example.com) for more."
        val decoded = MessageMarkdown.decode(src)
        assertEquals(src, decoded.markdown)
        assertEquals("See the docs for more.", decoded.plainText)
    }

    @Test
    fun mentions_areTreatedAsPlainText() {
        val src = "Cảm ơn @Nguyễn Văn A rất nhiều!"
        val decoded = MessageMarkdown.decode(src)
        assertEquals(src, decoded.markdown)
        assertEquals(src, decoded.plainText)
    }

    @Test
    fun plainTextOf_returnsEmptyForEmptyInput() {
        assertEquals("", MessageMarkdown.plainTextOf(""))
    }

    @Test
    fun decode_empty_returnsEmpty() {
        val v = MessageMarkdown.decode("")
        assertEquals(RichTextValue.Empty, v)
    }

    // ────────── Toolbar transforms — Phase 6 / US4 (T078 → T086) ─────────

    @Test
    fun applyBold_wrapsSelectionWithDoubleStar() {
        val src = RichTextValue.ofPlainText("hello world")
        val out = MessageMarkdown.applyBold(src, selection = 6..10) // "world"
        assertEquals("hello **world**", out.markdown)
        assertEquals("hello world", out.plainText)
    }

    @Test
    fun applyItalic_wrapsSelectionWithSingleStar() {
        val src = RichTextValue.ofPlainText("hello world")
        val out = MessageMarkdown.applyItalic(src, selection = 6..10)
        assertEquals("hello *world*", out.markdown)
        assertEquals("hello world", out.plainText)
    }

    @Test
    fun applyStrikethrough_wrapsSelectionWithTilde() {
        val src = RichTextValue.ofPlainText("hello world")
        val out = MessageMarkdown.applyStrikethrough(src, selection = 6..10)
        assertEquals("hello ~~world~~", out.markdown)
        assertEquals("hello world", out.plainText)
    }

    @Test
    fun applyNumberedList_prefixesLines() {
        val src = RichTextValue.ofPlainText("first\nsecond")
        val out = MessageMarkdown.applyNumberedList(src, selection = 0..11)
        assertEquals("1. first\n2. second", out.markdown)
        assertEquals("first\nsecond", out.plainText)
    }

    @Test
    fun applyQuote_prefixesLineWithChevron() {
        val src = RichTextValue.ofPlainText("hello")
        val out = MessageMarkdown.applyQuote(src, selection = 0..4)
        assertEquals("> hello", out.markdown)
        assertEquals("hello", out.plainText)
    }

    @Test
    fun applyLink_validUrl_wrapsSelection() {
        val src = RichTextValue.ofPlainText("see docs for more")
        val out = MessageMarkdown.applyLink(src, selection = 4..7, url = "https://example.com")
        assertEquals("see [docs](https://example.com) for more", out.markdown)
        assertEquals("see docs for more", out.plainText)
    }

    @Test
    fun applyLink_emptySelection_insertsUrlAsLabel() {
        val src = RichTextValue.ofPlainText("see  for more")
        val out = MessageMarkdown.applyLink(src, selection = 4..3, url = "https://example.com")
        assertTrue(out.markdown.contains("[https://example.com](https://example.com)"))
    }

    @Test
    fun applyBold_emptySelection_insertsEmptyBoldMarkers() {
        val src = RichTextValue.Empty
        val out = MessageMarkdown.applyBold(src, selection = 0..-1)
        assertEquals("****", out.markdown)
    }
}
