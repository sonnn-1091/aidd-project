package com.example.aiddproject.home

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import com.example.aiddproject.R
import com.example.aiddproject.core.locale.Language
import com.example.aiddproject.core.locale.LanguageProvider
import com.example.aiddproject.home.domain.KudosSummary
import com.example.aiddproject.home.domain.states.KudosState
import com.example.aiddproject.home.ui.components.KudosSection
import com.example.aiddproject.home.ui.components.TEST_TAG_KUDOS_BANNER
import com.example.aiddproject.home.ui.components.TEST_TAG_KUDOS_CHI_TIET
import com.example.aiddproject.home.ui.components.TEST_TAG_KUDOS_SECTION
import com.example.aiddproject.ui.theme.AIDDProjectTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/**
 * Compose UI coverage for the Kudos section (US5, T077). Asserts:
 *  - Section is hidden when `KudosState.Hidden` (Q-Home-9 — only the lower
 *    section gates on `isKudosAvailable`, not FAB S/Kudos / NavBar Kudos /
 *    ABOUT KUDOS).
 *  - Section renders banner + heading + body + Chi tiết link when the flag
 *    is true.
 *  - The banner falls back to the local `ic_kudos_banner` drawable when no
 *    remote URL is configured (FR-006). Coil-driven URL fallback (404 path)
 *    is exercised by the production AsyncImage's `error =` parameter — there's
 *    no Compose assertion that surfaces "the placeholder painter is showing"
 *    without inspecting Coil internals, so this test just guarantees the
 *    banner node is present.
 *  - Chi tiết tap fires the navigation callback exactly once (single-click
 *    guard from TR-005).
 */
class KudosSectionTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val ctx = InstrumentationRegistry.getInstrumentation().targetContext

    private val sampleKudosWithUrl =
        KudosSummary(
            isKudosAvailable = true,
            bannerImageUrl = "http://127.0.0.1:1/never-resolves.png",
            badgeText = "FUN",
            descriptionText = "Recognise teammates who carried you.",
        )
    private val sampleKudosNoUrl =
        sampleKudosWithUrl.copy(bannerImageUrl = null)

    @Test
    fun hidden_state_renders_nothing() {
        setContent(state = KudosState.Hidden)
        composeRule.onNodeWithTag(TEST_TAG_KUDOS_SECTION).assertDoesNotExist()
    }

    @Test
    fun loaded_state_renders_section_caption_brand_title_heading_and_chi_tiet() {
        setContent(state = KudosState.Loaded(sampleKudosNoUrl))

        composeRule.onNodeWithTag(TEST_TAG_KUDOS_SECTION).assertIsDisplayed()
        // Phase 11: caption above the divider + cream brand title below.
        composeRule
            .onNodeWithText(ctx.getString(R.string.home_section_kudos_caption))
            .assertIsDisplayed()
        composeRule
            .onNodeWithText(ctx.getString(R.string.home_section_kudos_brand_title))
            .assertIsDisplayed()
        composeRule
            .onNodeWithText(ctx.getString(R.string.home_kudos_note_heading))
            .assertIsDisplayed()
        composeRule
            .onNodeWithText(ctx.getString(R.string.home_link_chi_tiet))
            .assertIsDisplayed()
    }

    @Test
    fun banner_renders_local_drawable_when_no_remote_url_configured() {
        setContent(state = KudosState.Loaded(sampleKudosNoUrl))
        // Banner node is always present; the drawable behind it is the local
        // fallback when no remote URL is supplied.
        composeRule.onNodeWithTag(TEST_TAG_KUDOS_BANNER).assertIsDisplayed()
    }

    @Test
    fun banner_node_is_present_when_remote_url_supplied() {
        setContent(state = KudosState.Loaded(sampleKudosWithUrl))
        // AsyncImage falls back to the `error =` painter when the URL fails;
        // we only assert the banner node is mounted — Coil internals are
        // tested by the library itself.
        composeRule.onNodeWithTag(TEST_TAG_KUDOS_BANNER).assertIsDisplayed()
    }

    @Test
    fun chi_tiet_tap_fires_callback_once() {
        var taps = 0
        setContent(
            state = KudosState.Loaded(sampleKudosNoUrl),
            onChiTietClick = { taps++ },
        )

        composeRule.onNodeWithTag(TEST_TAG_KUDOS_CHI_TIET).performClick()
        assertEquals(1, taps)
    }

    @Test
    fun chi_tiet_double_tap_is_suppressed() {
        var taps = 0
        setContent(
            state = KudosState.Loaded(sampleKudosNoUrl),
            onChiTietClick = { taps++ },
        )

        // Two rapid taps in the same recomposition window — the single-click
        // guard must drop the second.
        composeRule.onNodeWithTag(TEST_TAG_KUDOS_CHI_TIET).performClick()
        composeRule.onNodeWithTag(TEST_TAG_KUDOS_CHI_TIET).performClick()
        assertEquals(1, taps)
    }

    private fun setContent(
        state: KudosState,
        onChiTietClick: () -> Unit = {},
    ) {
        composeRule.setContent {
            AIDDProjectTheme {
                LanguageProvider(language = Language.VN) {
                    KudosSection(
                        state = state,
                        onChiTietClick = onChiTietClick,
                    )
                }
            }
        }
    }
}
