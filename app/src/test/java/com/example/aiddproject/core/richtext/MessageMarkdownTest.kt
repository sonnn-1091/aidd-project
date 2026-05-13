package com.example.aiddproject.core.richtext

import com.example.aiddproject.kudos.compose.domain.RichTextValue
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Ignore
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
    @Ignore("Phase 6 / T078 wires `applyBold` + replaces this body.")
    fun applyBold_wrapsSelectionWithDoubleStar() {
        fail("not implemented — T078")
    }

    @Test
    @Ignore("Phase 6 / T078 wires `applyItalic` + replaces this body.")
    fun applyItalic_wrapsSelectionWithSingleStar() {
        fail("not implemented — T078")
    }

    @Test
    @Ignore("Phase 6 / T078 wires `applyStrikethrough` + replaces this body.")
    fun applyStrikethrough_wrapsSelectionWithTilde() {
        fail("not implemented — T078")
    }

    @Test
    @Ignore("Phase 6 / T078 wires `applyNumberedList` + replaces this body.")
    fun applyNumberedList_toggles_listMode() {
        fail("not implemented — T078")
    }

    @Test
    @Ignore("Phase 6 / T078 wires `applyQuote` + replaces this body.")
    fun applyQuote_toggles_blockquote() {
        fail("not implemented — T078")
    }

    @Test
    @Ignore("Phase 6 / T079 wires `applyLink` + replaces this body.")
    fun applyLink_validUrl_wrapsSelection() {
        fail("not implemented — T079")
    }
}
