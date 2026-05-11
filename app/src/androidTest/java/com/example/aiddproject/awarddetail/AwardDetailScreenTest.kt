package com.example.aiddproject.awarddetail

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import com.example.aiddproject.R
import com.example.aiddproject.awarddetail.domain.AwardDetail
import com.example.aiddproject.awarddetail.domain.states.AwardDetailState
import com.example.aiddproject.awarddetail.ui.AwardDetailScreenContent
import com.example.aiddproject.awarddetail.ui.AwardDetailUiState
import com.example.aiddproject.awarddetail.ui.TEST_TAG_AWARD_DETAIL_ERROR
import com.example.aiddproject.awarddetail.ui.TEST_TAG_AWARD_DETAIL_LOADING
import com.example.aiddproject.awarddetail.ui.TEST_TAG_AWARD_DETAIL_RETRY
import com.example.aiddproject.awarddetail.ui.TEST_TAG_AWARD_DETAIL_SCREEN
import com.example.aiddproject.core.locale.Language
import com.example.aiddproject.home.domain.Award
import com.example.aiddproject.home.domain.states.AwardsState
import com.example.aiddproject.home.ui.components.HomeNavTab
import com.example.aiddproject.home.ui.components.TEST_TAG_HOME_BOTTOM_BAR
import com.example.aiddproject.ui.theme.AIDDProjectTheme
import org.junit.Rule
import org.junit.Test

/**
 * Compose UI tests for the stateless [AwardDetailScreenContent].
 * Drives the composable directly with a fixed [AwardDetailUiState] so
 * the test never touches DI or the live `AwardDetailViewModel` —
 * `AwardDetailViewModelTest` covers the state machine. Follows the
 * `HomeScreenTest` pattern (no Hilt in androidTest; stateless target).
 *
 * Backfills canonical Top Talent tasks T026–T033 that were marked
 * `[x]` without authoring the test file (gap surfaced in delta-spec
 * `FQoJZLkG_d` plan review). Tests parametrize over the three demo
 * awards (Top Talent / Top Project / Top Heart) so any future delta
 * (Top Heart, MVP, Best Manager, Signature 2025) doesn't need a
 * separate copy.
 *
 * T028 (unauthenticated → Login redirect) intentionally omitted:
 * stateless content has no session check; that redirect path is
 * already covered by `HomeAuthRedirectTest` + `AuthRedirectController`
 * unit tests.
 */
class AwardDetailScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val ctx = InstrumentationRegistry.getInstrumentation().targetContext

    private val topTalent =
        AwardDetail(
            id = "a01",
            name = "Top Talent",
            description = "Giải thưởng Top Talent vinh danh những cá nhân xuất sắc toàn diện.",
            quantity = 10,
            quantityUnit = "Cá nhân",
            prizeValue = "7.000.000 VNĐ",
            imageUrl = null,
            sortOrder = 1,
        )

    private val topProject =
        AwardDetail(
            id = "a02",
            name = "Top Project",
            description = "Giải thưởng Top Project vinh danh các tập thể dự án xuất sắc.",
            quantity = 2,
            quantityUnit = "Tập thể",
            prizeValue = "15.000.000 VNĐ",
            imageUrl = null,
            sortOrder = 2,
        )

    private val topHeart =
        AwardDetail(
            id = "a03",
            name = "Top Heart",
            description = "Top Heart tôn vinh những Sunner luôn đặt trái tim vào mỗi hành động.",
            quantity = 8,
            quantityUnit = "Cá nhân",
            prizeValue = "5.000.000 VNĐ",
            imageUrl = null,
            sortOrder = 3,
        )

    private val populatedCategories =
        AwardsState.Populated(
            items =
                listOf(
                    Award(id = "a01", name = "Top Talent Award", thumbnailUrl = null, sortOrder = 1),
                    Award(id = "a02", name = "Top Project Award", thumbnailUrl = null, sortOrder = 2),
                    Award(id = "a03", name = "Top Heart Award", thumbnailUrl = null, sortOrder = 3),
                ),
        )

    private fun loadedState(detail: AwardDetail): AwardDetailUiState =
        AwardDetailUiState(
            activeAwardId = detail.id,
            detail = AwardDetailState.Loaded(detail),
            categories = populatedCategories,
            unreadCount = 0,
            language = Language.VN,
        )

    private fun setContent(
        state: AwardDetailUiState,
        onRetry: () -> Unit = {},
        onTabSelect: (HomeNavTab) -> Unit = {},
        lazyListState: LazyListState = LazyListState(),
    ) {
        composeRule.setContent {
            AIDDProjectTheme {
                AwardDetailScreenContent(
                    state = state,
                    onRetry = onRetry,
                    onLanguageSelected = {},
                    onSearchClick = {},
                    onBellClick = {},
                    onTabSelect = onTabSelect,
                    onCategorySelected = {},
                    onKudosChiTietClick = {},
                    lazyListState = lazyListState,
                )
            }
        }
    }

    @Test
    fun body_renders_top_talent_payload() {
        setContent(loadedState(topTalent))

        composeRule.onNodeWithTag(TEST_TAG_AWARD_DETAIL_SCREEN).assertIsDisplayed()
        composeRule.onNodeWithText("10").assertIsDisplayed()
        composeRule.onNodeWithText("Cá nhân").assertIsDisplayed()
        composeRule.onNodeWithText("7.000.000 VNĐ").assertIsDisplayed()
    }

    @Test
    fun body_renders_top_project_payload_with_zero_padded_quantity() {
        setContent(loadedState(topProject))

        composeRule.onNodeWithText("02").assertIsDisplayed()
        composeRule.onNodeWithText("Tập thể").assertIsDisplayed()
        composeRule.onNodeWithText("15.000.000 VNĐ").assertIsDisplayed()
    }

    @Test
    fun body_renders_top_heart_payload_with_zero_padded_quantity() {
        setContent(loadedState(topHeart))

        composeRule.onNodeWithText("08").assertIsDisplayed()
        composeRule.onNodeWithText("5.000.000 VNĐ").assertIsDisplayed()
    }

    @Test
    fun loading_indicator_visible_while_fetching() {
        setContent(
            AwardDetailUiState(
                activeAwardId = null,
                detail = AwardDetailState.Loading,
                categories = AwardsState.Loading,
                unreadCount = 0,
                language = Language.VN,
            ),
        )

        composeRule.onNodeWithTag(TEST_TAG_AWARD_DETAIL_LOADING).assertIsDisplayed()
    }

    @Test
    fun error_state_shows_retry_button() {
        setContent(
            AwardDetailUiState(
                activeAwardId = "a02",
                detail = AwardDetailState.Error(messageRes = R.string.award_detail_error),
                categories = populatedCategories,
                unreadCount = 0,
                language = Language.VN,
            ),
        )

        composeRule.onNodeWithTag(TEST_TAG_AWARD_DETAIL_ERROR).assertIsDisplayed()
        composeRule.onNodeWithTag(TEST_TAG_AWARD_DETAIL_RETRY).assertIsDisplayed()
    }

    @Test
    fun retry_button_invokes_callback() {
        var retryCount = 0
        setContent(
            state =
                AwardDetailUiState(
                    activeAwardId = "a02",
                    detail = AwardDetailState.Error(messageRes = R.string.award_detail_error),
                    categories = populatedCategories,
                    unreadCount = 0,
                    language = Language.VN,
                ),
            onRetry = { retryCount++ },
        )

        composeRule.onNodeWithTag(TEST_TAG_AWARD_DETAIL_RETRY).performClick()
        composeRule.waitForIdle()

        assert(retryCount == 1) { "Expected onRetry invoked once, got $retryCount" }
    }

    @Test
    fun null_image_url_renders_fallback_overlay() {
        // Top Project DEMO ships with imageUrl=null → AwardHeroBlock renders
        // the uppercase award name text overlay on the placeholder badge.
        setContent(loadedState(topProject.copy(imageUrl = null)))

        composeRule.onNodeWithText("TOP PROJECT").assertIsDisplayed()
    }

    @Test
    fun null_quantity_and_prize_render_em_dash_placeholders() {
        setContent(
            loadedState(
                topProject.copy(quantity = null, prizeValue = null),
            ),
        )

        val placeholder = ctx.getString(R.string.award_detail_placeholder_value)
        // The placeholder appears twice: once in the quantity row and
        // once in the prize row.
        composeRule.onAllNodesWithText(placeholder).assertCountEquals(2)
    }

    @Test
    fun sticky_bottom_nav_visible_when_body_is_scrolled() {
        val lazyListState = LazyListState(firstVisibleItemIndex = 3, firstVisibleItemScrollOffset = 0)
        setContent(state = loadedState(topProject), lazyListState = lazyListState)

        composeRule.onNodeWithTag(TEST_TAG_HOME_BOTTOM_BAR).assertIsDisplayed()
    }
}
