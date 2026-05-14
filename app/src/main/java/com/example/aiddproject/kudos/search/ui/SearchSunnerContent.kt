package com.example.aiddproject.kudos.search.ui

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aiddproject.R
import com.example.aiddproject.core.ui.rememberSingleClickHandler
import com.example.aiddproject.home.ui.components.HomeBottomBar
import com.example.aiddproject.home.ui.components.HomeNavTab
import com.example.aiddproject.kudos.search.domain.RecentSunner
import com.example.aiddproject.ui.theme.SaaCream
import com.example.aiddproject.ui.theme.SaaInk

/**
 * Stateless content composable for the Search Sunner screen (Figma
 * frame `3jgwke3E8O`).
 *
 * Hosts the Scaffold + custom top bar (back arrow + inactive search
 * bar pill) + Recent section (label, "View all"/"Collapse" toggle,
 * row list) + bottom nav. Owns no state — every callback comes from
 * the [callbacks] bag, every render input from [state].
 *
 * Mirrors the WriteKudo / CommunityStandards split so an instrumented
 * test can render this directly with a captured callback set.
 */
@Composable
fun SearchSunnerContent(
    state: SearchSunnerUiState,
    callbacks: SearchSunnerCallbacks,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(SaaInk)
                .testTag(SearchSunnerTestTags.SCREEN),
    ) {
        Image(
            painter = painterResource(R.drawable.kudos_kv_bg),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            topBar = {
                SearchSunnerTopBar(
                    query = state.searchQuery,
                    onQueryChange = callbacks.onSearchQueryChange,
                    onBack = callbacks.onNavigateBack,
                )
            },
            bottomBar = {
                HomeBottomBar(
                    selected = HomeNavTab.Kudos,
                    onTabSelect = callbacks.onSelectBottomTab,
                )
            },
        ) { padding ->
            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(padding)
                        // Figma: 24dp gap from TopNav bottom (y=87) to Title
                        // top (y=111). Outer 20dp horizontal matches each
                        // section's per-frame padding (Title + Frame 556
                        // both `0 20 0 20` in Figma).
                        .padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (state.isSearching) {
                    SearchResultsSection(
                        results = state.searchResults,
                        onRowTap = callbacks.onRowTap,
                    )
                } else if (state.recentSunners.isNotEmpty()) {
                    RecentSectionHeader(
                        isViewingAll = state.isViewingAll,
                        showViewAllButton = state.showViewAllButton,
                        onToggleViewAll = callbacks.onToggleViewAll,
                    )
                    RecentSunnerList(
                        visibleSunners = state.visibleSunners,
                        onRowTap = callbacks.onRowTap,
                        onRemove = callbacks.onRemove,
                    )
                }
            }
        }
    }
}

/**
 * Top bar: back arrow on the left, an inactive search-bar pill that
 * fills the remaining width. Tap on the pill transitions to the
 * Searching state (deferred to a sibling spec; MVP fires the
 * "coming soon" Toast via [callbacks.onSearchBarTap]).
 */
@Composable
private fun SearchSunnerTopBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onBack: () -> Unit,
) {
    // Figma node 6891:21279 — width 375, height 40, padding 0/20/0/0
    // (right only), gap 12 between Left Accessory and search bar.
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier =
            Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(end = 20.dp),
    ) {
        IconButton(
            onClick = rememberSingleClickHandler(onClick = onBack),
            modifier = Modifier.testTag(SearchSunnerTestTags.BACK_BUTTON),
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_back_chevron),
                contentDescription = stringResource(R.string.a11y_search_sunner_back),
                tint = Color.White,
            )
        }
        SearchBarTextField(
            query = query,
            onQueryChange = onQueryChange,
            modifier = Modifier.weight(1f),
        )
    }
}

/**
 * Editable search field — `BasicTextField` styled as the Figma pill.
 * Text and placeholder are LEFT-aligned (override of Figma's
 * `textAlign: center` per UX feedback: centered text in a typing-in-
 * progress field jumps as characters are added, which is jarring).
 *
 * Visual per Figma `mms_2_Search bar` (`6891:22074`):
 *  - background: SaaCream @ 10% alpha
 *  - border: 1dp #998C5F
 *  - radius: 4dp; padding: 10dp
 *  - text + placeholder: Montserrat 500 14sp, white @ 80%
 *  - **NO leading magnifying-glass icon** — Figma's instance has no
 *    icon child (only the Label TEXT).
 *
 * Keyboard: single-line, IME action = Search. The action is a no-op
 * — search happens automatically 500ms after the user stops typing,
 * driven by the VM's debounce pipeline.
 */
@Composable
private fun SearchBarTextField(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val placeholder = stringResource(R.string.search_sunner_placeholder)
    val a11yLabel = stringResource(R.string.a11y_search_sunner_search_bar)
    val textStyle =
        TextStyle(
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 14.sp,
            lineHeight = 20.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Start,
        )
    BasicTextField(
        value = query,
        onValueChange = onQueryChange,
        singleLine = true,
        textStyle = textStyle,
        cursorBrush = SolidColor(Color.White),
        keyboardOptions =
            KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions =
            KeyboardActions(
                // Search fires automatically on debounce (500ms after
                // the last keystroke) — see SearchSunnerViewModel.
                onSearch = { /* no-op */ },
            ),
        modifier =
            modifier
                .clip(RoundedCornerShape(4.dp))
                .background(SaaCream.copy(alpha = 0.10f))
                .border(BorderStroke(1.dp, SearchBarBorderColor), RoundedCornerShape(4.dp))
                .padding(10.dp)
                .testTag(SearchSunnerTestTags.SEARCH_BAR)
                .semantics { contentDescription = a11yLabel },
        decorationBox = { innerTextField ->
            Box(contentAlignment = Alignment.CenterStart) {
                if (query.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = textStyle,
                    )
                }
                innerTextField()
            }
        },
    )
}

/**
 * Section header for the recent-search list: "Recent" label on the
 * left, "View all" / "Collapse" toggle on the right when more than
 * the collapsed-visible-count of rows exist (FR-007 / FR-008).
 */
@Composable
private fun RecentSectionHeader(
    isViewingAll: Boolean,
    showViewAllButton: Boolean,
    onToggleViewAll: () -> Unit,
) {
    // Figma node 6891:22086 — height 32dp, space-between layout.
    // Label + button both WHITE per Figma (NOT SaaCream).
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier =
            Modifier
                .fillMaxWidth()
                .height(32.dp)
                .testTag(SearchSunnerTestTags.RECENT_LABEL),
    ) {
        Text(
            text = stringResource(R.string.search_sunner_section_recent),
            color = Color.White,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.1.sp,
            modifier = Modifier.weight(1f),
        )
        if (showViewAllButton) {
            val toggleClick = rememberSingleClickHandler(onClick = onToggleViewAll)
            Text(
                text =
                    stringResource(
                        if (isViewingAll) R.string.search_sunner_collapse else R.string.search_sunner_view_all,
                    ),
                color = Color.White,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                modifier =
                    Modifier
                        .clickable(role = Role.Button) { toggleClick() }
                        .padding(vertical = 6.dp)
                        .testTag(SearchSunnerTestTags.VIEW_ALL_BUTTON),
            )
        }
    }
}

/**
 * Active-search results body. Renders one of:
 *  - Loading spinner (centered) while waiting for the repo call.
 *  - LazyColumn of result rows (avatar + name + dept, NO X icon).
 *  - "Không tìm thấy Sunner phù hợp" centered text for an empty result.
 *  - Generic error text for a failed call.
 *
 * Tapping a result row promotes the Sunner to the head of the recent
 * list and navigates to Profile — same as a recent-row tap.
 */
@Composable
private fun SearchResultsSection(
    results: SearchResultsState,
    onRowTap: (userId: String) -> Unit,
) {
    when (results) {
        SearchResultsState.Idle -> Unit
        SearchResultsState.Loading -> CenteredMessage(stringResource(R.string.search_sunner_loading))
        SearchResultsState.Empty -> CenteredMessage(stringResource(R.string.search_sunner_no_results))
        SearchResultsState.Error -> CenteredMessage(stringResource(R.string.search_sunner_error))
        is SearchResultsState.Loaded ->
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(0.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                items(items = results.matches, key = { it.id }) { node ->
                    SunnerRow(
                        userId = node.id,
                        fullName = node.fullName,
                        departmentName = node.department?.name,
                        onRowTap = { onRowTap(node.id) },
                        onRemove = null, // search results have no X
                    )
                }
            }
    }
}

@Composable
private fun CenteredMessage(text: String) {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 14.sp,
            lineHeight = 20.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun RecentSunnerList(
    visibleSunners: List<RecentSunner>,
    onRowTap: (userId: String) -> Unit,
    onRemove: (userId: String) -> Unit,
) {
    val listState = rememberLazyListState()
    // Figma Frame 556 stacks rows end-to-end with no gap (each row is
    // a self-contained 60dp tall card with internal padding).
    LazyColumn(
        state = listState,
        verticalArrangement = Arrangement.spacedBy(0.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        items(items = visibleSunners, key = { it.userId }) { sunner ->
            SunnerRow(
                userId = sunner.userId,
                fullName = sunner.fullName,
                departmentName = sunner.departmentName,
                onRowTap = { onRowTap(sunner.userId) },
                onRemove = { onRemove(sunner.userId) },
            )
        }
    }
}

/**
 * Shared row composable used by BOTH the Recent list and the live-
 * search results. Avatar (40dp circle, white border) + name
 * (Montserrat 500 14sp, white) + department code (Montserrat 500
 * 14sp, #999999). The trailing X icon is rendered only when
 * [onRemove] is non-null — search-result rows don't have one.
 *
 * Figma `kết quả search 3` / `mms_B.3_*`.
 */
@Composable
private fun SunnerRow(
    userId: String,
    fullName: String,
    departmentName: String?,
    onRowTap: () -> Unit,
    onRemove: (() -> Unit)?,
) {
    val rowClick = rememberSingleClickHandler(onClick = onRowTap)
    val rowA11y =
        stringResource(
            R.string.a11y_search_sunner_row,
            fullName,
            departmentName.orEmpty(),
        )
    val removeA11y = stringResource(R.string.a11y_search_sunner_remove, fullName)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier =
            Modifier
                .fillMaxWidth()
                .height(60.dp)
                .clickable(role = Role.Button) { rowClick() }
                .testTag(SearchSunnerTestTags.recentRowTag(userId))
                .semantics { contentDescription = rowA11y },
    ) {
        Avatar(
            avatarUrl = null,
            modifier = Modifier.padding(10.dp),
        )
        // Figma `kết quả search 3` has `gap: 2px` between the Avatar
        // frame (60dp wide incl. 10dp padding) and the Tên frame.
        Spacer(Modifier.width(2.dp))
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = fullName,
                color = Color.White,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
            )
            if (!departmentName.isNullOrBlank()) {
                Text(
                    text = departmentName,
                    color = RowDepartmentColor,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                )
            }
        }
        if (onRemove != null) {
            IconButton(
                onClick = onRemove,
                modifier =
                    Modifier
                        .testTag(SearchSunnerTestTags.removeButtonTag(userId))
                        .semantics { contentDescription = removeA11y },
            ) {
                // Figma 6891:22103 — X is 16dp, not the M3 default 24dp.
                // M3 IconButton keeps a 48dp tap target around it for a11y.
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}

/**
 * 40dp circular avatar — placeholder for now. Production should load
 * `avatarUrl` via Coil's `AsyncImage`; this MVP renders the existing
 * `kudos_avatar_recipient` placeholder until the live-search state
 * populates real avatar URLs (sibling spec).
 */
@Composable
private fun Avatar(
    @Suppress("UNUSED_PARAMETER") avatarUrl: String?,
    modifier: Modifier = Modifier,
) {
    Image(
        painter = painterResource(R.drawable.kudos_avatar_recipient),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier =
            modifier
                .size(40.dp)
                .clip(CircleShape)
                // Figma border is 1.869dp; round to 1.5dp for closer fidelity
                // than 2dp (the half-px difference is visible on xxhdpi).
                .border(1.5.dp, Color.White, CircleShape),
    )
}

/**
 * Callback bag for the stateless [SearchSunnerContent]. Six lambdas
 * cover: back nav, search-bar tap, view-all toggle, X removal, row
 * tap, bottom-nav tab select. The Hilt-aware [SearchSunnerScreen]
 * constructs the bag from VM methods + caller-provided nav lambdas.
 */
data class SearchSunnerCallbacks(
    val onNavigateBack: () -> Unit,
    val onSearchQueryChange: (String) -> Unit,
    val onToggleViewAll: () -> Unit,
    val onRemove: (userId: String) -> Unit,
    val onRowTap: (userId: String) -> Unit,
    val onSelectBottomTab: (HomeNavTab) -> Unit,
)

// ── Figma tokens (queried 2026-05-14 against frame 3jgwke3E8O) ──────
private val SearchBarBorderColor: Color = Color(0xFF998C5F)
private val RowDepartmentColor: Color = Color(0xFF999999)

@Preview(name = "Search Sunner — empty (light)", showBackground = true)
@Preview(
    name = "Search Sunner — empty (dark)",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun SearchSunnerContentEmptyPreview() {
    SearchSunnerContent(
        state = SearchSunnerUiState(),
        callbacks = previewCallbacks(),
    )
}

@Preview(name = "Search Sunner — populated (collapsed)", showBackground = true)
@Composable
private fun SearchSunnerContentPopulatedPreview() {
    SearchSunnerContent(
        state =
            SearchSunnerUiState(
                recentSunners =
                    listOf(
                        RecentSunner("u1", "Dương Huỳnh Xuân Nhật", "CECV1", null, 3),
                        RecentSunner("u2", "Trần Bình", "CECU01", null, 2),
                        RecentSunner("u3", "Lê Châu", "CESC1", null, 1),
                    ),
                isViewingAll = false,
            ),
        callbacks = previewCallbacks(),
    )
}

@Composable
private fun previewCallbacks(): SearchSunnerCallbacks =
    SearchSunnerCallbacks(
        onNavigateBack = {},
        onSearchQueryChange = {},
        onToggleViewAll = {},
        onRemove = {},
        onRowTap = {},
        onSelectBottomTab = {},
    )
