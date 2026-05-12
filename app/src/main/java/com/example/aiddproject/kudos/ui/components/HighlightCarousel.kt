package com.example.aiddproject.kudos.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aiddproject.R
import com.example.aiddproject.kudos.domain.Kudos
import com.example.aiddproject.kudos.domain.states.KudosHighlightState
import com.example.aiddproject.kudos.ui.KudosTestTags

/**
 * Highlight carousel (spec § US4).
 *
 * - HorizontalPager over the top-5 cards (the repository already
 *   returns them sorted by heartCount DESC).
 * - PageIndicator below the pager renders "1/5".
 * - Filter-change reset: a `LaunchedEffect(filterResetTick)` scrolls
 *   pagerState back to page 0 (US3 scenario 3).
 * - Active/faded card styling: the centered page is opaque, sides
 *   render at alpha 0.5.
 */
@Composable
fun HighlightCarousel(
    state: KudosHighlightState,
    filterResetTick: Int,
    selectedHashtagLabel: String?,
    selectedDepartmentLabel: String?,
    onHashtagTriggerTap: () -> Unit,
    onDepartmentTriggerTap: () -> Unit,
    onHeartTap: (kudosId: String) -> Unit,
    onCopyLink: (kudosId: String) -> Unit,
    onCardTap: (Kudos) -> Unit,
    onHashtagChipTap: (hashtagId: String) -> Unit,
    onProfileTap: (userId: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .testTag(KudosTestTags.HIGHLIGHT),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        KudosSectionHeader(title = stringResource(R.string.kudos_section_highlight_title))
        HighlightFilterRow(
            selectedHashtagLabel = selectedHashtagLabel,
            selectedDepartmentLabel = selectedDepartmentLabel,
            onHashtagTriggerTap = onHashtagTriggerTap,
            onDepartmentTriggerTap = onDepartmentTriggerTap,
        )
        when (state) {
            KudosHighlightState.Loading -> SectionPlaceholder(text = stringResource(R.string.kudos_loading))
            KudosHighlightState.Empty -> SectionPlaceholder(text = stringResource(R.string.kudos_empty))
            is KudosHighlightState.Error -> SectionPlaceholder(text = stringResource(state.messageRes))
            is KudosHighlightState.Loaded -> {
                val pagerState = rememberPagerState(pageCount = { state.items.size })

                // Filter-change tick → reset to page 0.
                LaunchedEffect(filterResetTick) {
                    if (pagerState.pageCount > 0 && pagerState.currentPage != 0) {
                        pagerState.scrollToPage(0)
                    }
                }

                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxWidth().testTag(KudosTestTags.HIGHLIGHT_PAGER),
                    contentPadding = PaddingValues(horizontal = 12.dp),
                ) { page ->
                    val active = pagerState.currentPage == page
                    HighlightCard(
                        kudos = state.items[page],
                        onHeartTap = onHeartTap,
                        onCopyLink = onCopyLink,
                        onCardTap = onCardTap,
                        onHashtagChipTap = onHashtagChipTap,
                        onProfileTap = onProfileTap,
                        modifier =
                            Modifier
                                .padding(horizontal = 4.dp)
                                .alpha(if (active) 1f else 0.5f)
                                .testTag("${KudosTestTags.HIGHLIGHT_CARD}_$page"),
                    )
                }
                PageIndicator(
                    currentPage = pagerState.currentPage,
                    pageCount = state.items.size,
                )
            }
        }
    }
}

@Composable
internal fun SectionPlaceholder(text: String) {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(96.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White.copy(alpha = 0.05f)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = Color.White.copy(alpha = 0.7f),
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
        )
    }
}
