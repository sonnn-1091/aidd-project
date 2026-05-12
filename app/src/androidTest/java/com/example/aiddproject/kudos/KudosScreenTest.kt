package com.example.aiddproject.kudos

import android.content.res.Configuration
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import androidx.test.platform.app.InstrumentationRegistry
import com.example.aiddproject.R
import com.example.aiddproject.core.locale.Language
import com.example.aiddproject.core.locale.LanguageProvider
import com.example.aiddproject.kudos.domain.GiftRecipient
import com.example.aiddproject.kudos.domain.Hashtag
import com.example.aiddproject.kudos.domain.Kudos
import com.example.aiddproject.kudos.domain.KudosEdge
import com.example.aiddproject.kudos.domain.PersonalStats
import com.example.aiddproject.kudos.domain.SpotlightGraph
import com.example.aiddproject.kudos.domain.SunnerNode
import com.example.aiddproject.kudos.domain.states.AllKudosState
import com.example.aiddproject.kudos.domain.states.KudosHighlightState
import com.example.aiddproject.kudos.domain.states.PersonalStatsState
import com.example.aiddproject.kudos.domain.states.SpotlightState
import com.example.aiddproject.kudos.domain.states.TopTenState
import com.example.aiddproject.kudos.ui.KudosScreenContent
import com.example.aiddproject.kudos.ui.KudosTestTags
import com.example.aiddproject.kudos.ui.KudosUiState
import com.example.aiddproject.ui.theme.AIDDProjectTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/**
 * Compose UI tests for the stateless [KudosScreenContent] (US1).
 *
 * Drives the content directly with a fixed [KudosUiState] so the
 * test never touches the live VM or DI graph — that's covered by
 * [KudosViewModelTest] in `test/`.
 */
class KudosScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    // `LanguageProvider` overlays a VN configuration on the Compose
    // tree so `stringResource()` returns Vietnamese copy regardless of
    // device locale. We mirror that overlay here so `ctx.getString()`
    // returns the same VN string that the UI rendered — otherwise the
    // assertion compares VN-rendered text against EN expectations on
    // an English emulator.
    private val ctx =
        run {
            val base = InstrumentationRegistry.getInstrumentation().targetContext
            val overlay = Configuration(base.resources.configuration).apply { setLocale(Language.VN.toLocale()) }
            base.createConfigurationContext(overlay)
        }

    private val seedSunner =
        SunnerNode(id = "u01", fullName = "Nguyễn An", starTier = 1)

    private val seedKudos =
        Kudos(
            id = "k01",
            sender = seedSunner,
            recipient = SunnerNode(id = "u02", fullName = "Trần Bình"),
            message = "Cảm ơn vì đã help team.",
            title = "Hỗ trợ sprint",
            hashtags = listOf(Hashtag(id = "h01", tagName = "teamwork")),
            createdAt = "2026-05-12T10:00:00Z",
            heartCount = 7,
        )

    private fun loadedState(): KudosUiState =
        KudosUiState(
            highlight = KudosHighlightState.Loaded(items = listOf(seedKudos)),
            allKudos = AllKudosState.Loaded(items = listOf(seedKudos), hasMore = false, nextPage = null),
            spotlight =
                SpotlightState.Loaded(
                    graph =
                        SpotlightGraph(
                            nodes = listOf(seedSunner),
                            edges = listOf(KudosEdge(senderId = "u01", recipientId = "u02")),
                            totalKudosCount = 42,
                        ),
                ),
            stats =
                PersonalStatsState.Loaded(
                    stats =
                        PersonalStats(
                            kudosReceived = 1,
                            kudosSent = 2,
                            heartsReceived = 3,
                            secretBoxesOpened = 0,
                            secretBoxesUnopened = 1,
                        ),
                ),
            topTen =
                TopTenState.Loaded(
                    items = listOf(GiftRecipient(userId = "u01", fullName = "Nguyễn An", rewardName = "Voucher 100K")),
                ),
            language = Language.VN,
        )

    private fun setContent(
        state: KudosUiState,
        onPullToRefresh: () -> Unit = {},
        onSendKudos: () -> Unit = {},
        onCardTap: (com.example.aiddproject.kudos.domain.Kudos) -> Unit = {},
    ) {
        composeRule.setContent {
            AIDDProjectTheme {
                LanguageProvider(language = state.language) {
                    KudosScreenContent(
                        state = state,
                        filterResetTick = 0,
                        onPullToRefresh = onPullToRefresh,
                        onLanguageSelected = {},
                        onSearchClick = {},
                        onBellClick = {},
                        onTabSelect = {},
                        onSendKudos = onSendKudos,
                        onSelectHashtagId = {},
                        onSelectDepartmentId = {},
                        onCardTap = onCardTap,
                        onHeartTap = {},
                        onCopyLink = {},
                        onHashtagChipTap = {},
                        onProfileTap = {},
                        onViewAllKudos = {},
                        onOpenSecretBox = {},
                        onSpotlightSearchChange = {},
                        onSnackbarDismissed = {},
                    )
                }
            }
        }
    }

    @Test
    fun scaffold_renders_all_five_sections_when_states_loaded() {
        setContent(loadedState())

        // SCREEN + LAZY_COLUMN + the always-visible top sections exist
        // without scrolling.
        composeRule.onNodeWithTag(KudosTestTags.SCREEN).assertIsDisplayed()
        composeRule.onNodeWithTag(KudosTestTags.LAZY_COLUMN).assertIsDisplayed()
        composeRule.onNodeWithTag(KudosTestTags.HERO).assertIsDisplayed()
        composeRule.onNodeWithTag(KudosTestTags.HIGHLIGHT).assertIsDisplayed()

        // Off-screen sections — scroll the LazyColumn to each in turn
        // (LazyColumn doesn't compose items below the viewport without
        // explicit scrolling).
        listOf(
            KudosTestTags.SPOTLIGHT,
            KudosTestTags.FEED,
            KudosTestTags.STATS,
            KudosTestTags.TOP_TEN,
        ).forEach { tag ->
            composeRule
                .onNodeWithTag(KudosTestTags.LAZY_COLUMN)
                .performScrollToNode(hasTestTag(tag))
            composeRule.onNodeWithTag(tag).assertIsDisplayed()
        }
    }

    @Test
    fun renders_empty_copy_when_sections_empty() {
        val emptyState =
            loadedState().copy(
                highlight = KudosHighlightState.Empty,
                allKudos = AllKudosState.Empty,
            )
        setContent(emptyState)

        // Both Highlight + AllKudos render the same `kudos_empty`
        // copy. LazyColumn doesn't compose off-screen items, so only
        // the top Highlight Empty is in the semantics tree without
        // scrolling — assert at least one is visible.
        val emptyCopy = ctx.getString(R.string.kudos_empty)
        composeRule.onAllNodesWithText(emptyCopy).onFirst().assertIsDisplayed()
    }

    @Test
    fun renders_error_copy_when_section_errors() {
        val errorState =
            loadedState().copy(
                highlight = KudosHighlightState.Error(R.string.kudos_error),
            )
        setContent(errorState)

        val errorCopy = ctx.getString(R.string.kudos_error)
        composeRule.onAllNodesWithText(errorCopy).onFirst().assertIsDisplayed()
    }

    @Test
    fun send_kudos_pill_tap_fires_callback() {
        var sendCount = 0
        setContent(loadedState(), onSendKudos = { sendCount++ })

        composeRule.onNodeWithTag(KudosTestTags.SEND_KUDOS_CTA).performClick()
        composeRule.waitForIdle()
        assertEquals(1, sendCount)
    }

    @Test
    fun highlight_card_tap_fires_card_callback_with_kudos() {
        var tappedKudos: com.example.aiddproject.kudos.domain.Kudos? = null
        setContent(loadedState(), onCardTap = { tappedKudos = it })

        // First card is at index 0.
        composeRule.onNodeWithTag("${KudosTestTags.HIGHLIGHT_CARD}_0").performClick()
        composeRule.waitForIdle()
        assertEquals(seedKudos.id, tappedKudos?.id)
    }

    @Test
    fun pull_to_refresh_swipe_invokes_callback() {
        var refreshCount = 0
        setContent(loadedState(), onPullToRefresh = { refreshCount++ })

        composeRule.onNodeWithTag(KudosTestTags.LAZY_COLUMN).performTouchInput { swipeDown() }
        composeRule.waitForIdle()
        assertEquals(1, refreshCount)
    }
}
