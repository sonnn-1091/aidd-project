package com.example.aiddproject.kudos.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.example.aiddproject.core.ui.rememberSingleClickHandler
import com.example.aiddproject.kudos.domain.Department
import com.example.aiddproject.kudos.domain.Hashtag
import com.example.aiddproject.kudos.domain.Kudos
import com.example.aiddproject.kudos.domain.states.KudosHighlightState
import com.example.aiddproject.kudos.ui.KudosTestTags
import kotlinx.coroutines.launch

/**
 * Highlight carousel — Figma `mms_B_Highlight` (`6885:9084`).
 *
 * - Infinite-loop carousel via HorizontalPager with pageCount =
 *   Int.MAX_VALUE; the page index is mapped to the actual item via
 *   modulo so swiping past the last card wraps back to the first
 *   and vice versa.
 * - PageIndicator shows logical position "n/N" using the modulo
 *   index.
 * - Prev/Next chevron buttons flanking the indicator animate the
 *   pager to the adjacent page, wrapping freely thanks to the
 *   infinite-pageCount trick.
 * - filterResetTick rewinds the pager to a starting index that
 *   represents the logical first card (scrollToPage with the
 *   pre-computed initial page).
 */
@Composable
fun HighlightCarousel(
    state: KudosHighlightState,
    filterResetTick: Int,
    selectedHashtagLabel: String?,
    selectedDepartmentLabel: String?,
    hashtags: List<Hashtag>,
    departments: List<Department>,
    activeHashtagId: String?,
    activeDepartmentId: String?,
    onSelectHashtag: (Hashtag?) -> Unit,
    onSelectDepartment: (Department?) -> Unit,
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
            hashtags = hashtags,
            departments = departments,
            activeHashtagId = activeHashtagId,
            activeDepartmentId = activeDepartmentId,
            onSelectHashtag = onSelectHashtag,
            onSelectDepartment = onSelectDepartment,
        )
        when (state) {
            KudosHighlightState.Loading -> SectionPlaceholder(text = stringResource(R.string.kudos_loading))
            KudosHighlightState.Empty -> SectionPlaceholder(text = stringResource(R.string.kudos_empty))
            is KudosHighlightState.Error -> SectionPlaceholder(text = stringResource(state.messageRes))
            is KudosHighlightState.Loaded -> {
                val items = state.items
                if (items.isEmpty()) {
                    SectionPlaceholder(text = stringResource(R.string.kudos_empty))
                    return@Column
                }
                LoopingPager(
                    items = items,
                    filterResetTick = filterResetTick,
                    onHeartTap = onHeartTap,
                    onCopyLink = onCopyLink,
                    onCardTap = onCardTap,
                    onHashtagChipTap = onHashtagChipTap,
                    onProfileTap = onProfileTap,
                )
            }
        }
    }
}

@Composable
private fun LoopingPager(
    items: List<Kudos>,
    filterResetTick: Int,
    onHeartTap: (kudosId: String) -> Unit,
    onCopyLink: (kudosId: String) -> Unit,
    onCardTap: (Kudos) -> Unit,
    onHashtagChipTap: (hashtagId: String) -> Unit,
    onProfileTap: (userId: String) -> Unit,
) {
    val size = items.size
    // Pick a high-enough starting index aligned to a multiple of
    // `size` so currentPage % size == 0 → logical card 0.
    val startPage = remember(size) { (Int.MAX_VALUE / 2).let { it - (it % size) } }
    val pagerState =
        rememberPagerState(
            initialPage = startPage,
            pageCount = { Int.MAX_VALUE },
        )
    val scope = rememberCoroutineScope()
    val currentLogicalPage = pagerState.currentPage.mod(size)

    // Filter-change rewind to the logical card 0.
    LaunchedEffect(filterResetTick) {
        if (filterResetTick > 0) {
            pagerState.scrollToPage(startPage)
        }
    }

    HorizontalPager(
        state = pagerState,
        modifier = Modifier.fillMaxWidth().testTag(KudosTestTags.HIGHLIGHT_PAGER),
        contentPadding = PaddingValues(horizontal = 32.dp),
        pageSpacing = 8.dp,
    ) { page ->
        val active = pagerState.currentPage == page
        HighlightCard(
            kudos = items[page.mod(size)],
            onHeartTap = onHeartTap,
            onCopyLink = onCopyLink,
            onCardTap = onCardTap,
            onHashtagChipTap = onHashtagChipTap,
            onProfileTap = onProfileTap,
            modifier =
                Modifier
                    .alpha(if (active) 1f else 0.5f)
                    .testTag("${KudosTestTags.HIGHLIGHT_CARD}_${page.mod(size)}"),
        )
    }
    CarouselNavRow(
        currentLogicalPage = currentLogicalPage,
        pageCount = size,
        onPrev = {
            scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) }
        },
        onNext = {
            scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
        },
    )
}

@Composable
private fun CarouselNavRow(
    currentLogicalPage: Int,
    pageCount: Int,
    onPrev: () -> Unit,
    onNext: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        NavChevron(direction = NavDirection.Prev, onTap = onPrev)
        Box(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text(
                text = "${currentLogicalPage + 1}/$pageCount",
                color = Color.White.copy(alpha = 0.85f),
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                modifier = Modifier.testTag(KudosTestTags.PAGE_INDICATOR),
            )
        }
        NavChevron(direction = NavDirection.Next, onTap = onNext)
    }
}

private enum class NavDirection { Prev, Next }

@Composable
private fun NavChevron(
    direction: NavDirection,
    onTap: () -> Unit,
) {
    val click = rememberSingleClickHandler { onTap() }
    Box(
        modifier =
            Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White.copy(alpha = 0.08f))
                .clickable(onClick = click),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector =
                if (direction == NavDirection.Prev) {
                    Icons.AutoMirrored.Filled.KeyboardArrowLeft
                } else {
                    Icons.AutoMirrored.Filled.KeyboardArrowRight
                },
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(20.dp),
        )
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
