package com.example.aiddproject.core.richtext

import org.junit.Assert.fail
import org.junit.Ignore
import org.junit.Test

/**
 * Failing-stub test class for `MessageMarkdown` (T015).
 *
 * Pins down the Q-W-1 Markdown-subset round-trip contract. Replaced with
 * real bodies in Phase 2 / T025.
 */
@Ignore("Phase 2 / T025 wires the real MessageMarkdown + replaces these bodies.")
class MessageMarkdownTest {
    @Test
    fun roundTrip_plainText_isLossless() {
        fail("not implemented — T025")
    }

    @Test
    fun roundTrip_bold_isLossless() {
        fail("not implemented — T025")
    }

    @Test
    fun roundTrip_italic_isLossless() {
        fail("not implemented — T025")
    }

    @Test
    fun roundTrip_strikethrough_isLossless() {
        fail("not implemented — T025")
    }

    @Test
    fun roundTrip_numberedList_isLossless() {
        fail("not implemented — T025")
    }

    @Test
    fun roundTrip_quote_isLossless() {
        fail("not implemented — T025")
    }

    @Test
    fun roundTrip_link_isLossless() {
        fail("not implemented — T025")
    }

    @Test
    fun applyBold_wrapsSelectionWithDoubleStar() {
        fail("not implemented — T025")
    }

    @Test
    fun applyItalic_wrapsSelectionWithSingleStar() {
        fail("not implemented — T025")
    }

    @Test
    fun applyStrikethrough_wrapsSelectionWithTilde() {
        fail("not implemented — T025")
    }

    @Test
    fun applyNumberedList_toggles_listMode() {
        fail("not implemented — T025")
    }

    @Test
    fun applyQuote_toggles_blockquote() {
        fail("not implemented — T025")
    }

    @Test
    fun applyLink_validUrl_wrapsSelection() {
        fail("not implemented — T025")
    }
}
