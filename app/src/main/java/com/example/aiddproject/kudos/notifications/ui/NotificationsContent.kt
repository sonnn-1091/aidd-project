@file:OptIn(
    kotlin.time.ExperimentalTime::class,
    androidx.compose.material3.ExperimentalMaterial3Api::class,
)

package com.example.aiddproject.kudos.notifications.ui

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.PlaylistAddCheck
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aiddproject.R
import com.example.aiddproject.core.ui.rememberSingleClickHandler
import com.example.aiddproject.kudos.notifications.domain.NotificationItem
import com.example.aiddproject.kudos.notifications.domain.NotificationPayload
import com.example.aiddproject.kudos.notifications.domain.NotificationType
import com.example.aiddproject.kudos.notifications.ui.components.NotificationRow
import com.example.aiddproject.ui.theme.SaaInk
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

/**
 * Stateless content for the Notifications screen (Figma `_b68CBWKl5`).
 *
 * Layout follows the Figma frame:
 *  - SaaInk solid backdrop with the keyvisual PNG layered on top (top-
 *    aligned `ContentScale.Crop` so the rightside streamers show
 *    through the translucent list card).
 *  - 140dp header gradient overlay matches the iOS `TopNavigation`
 *    gradient `linear-gradient(180deg, #00101A 0% → transparent 100%)`
 *    at 0.9 opacity (same recipe Home uses).
 *  - TopAppBar shows only the back chevron + centered title (no
 *    actions — the mark-all-read is its own row below the title per
 *    the frame).
 *  - "Đánh dấu đọc tất cả" row sits between the AppBar and the list,
 *    20dp from the left edge (matches Figma `mms_Button_read all`
 *    position).
 *  - Notification list is wrapped in an 8dp-radius card filled with
 *    `rgba(0,7,12,0.6)` (`ListCardColor` below) and 20dp horizontal
 *    margin. Rows divide via a 1dp `#2E3940` line; final row has no
 *    bottom divider so the card's rounded corners read cleanly.
 */
@Composable
fun NotificationsContent(
    state: NotificationsUiState,
    callbacks: NotificationsCallbacks,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val resources = LocalContext.current.resources

    LaunchedEffect(state.snackbar) {
        state.snackbar?.let {
            snackbarHostState.showSnackbar(resources.getString(it.messageRes))
            callbacks.onConsumeSnackbar()
        }
    }

    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(SaaInk)
                .testTag(NotificationsTestTags.SCREEN),
    ) {
        Image(
            painter = painterResource(R.drawable.notifications_kv_bg),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            alignment = Alignment.TopCenter,
            modifier = Modifier.fillMaxSize(),
        )
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .background(HeaderGradient),
        )

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = stringResource(R.string.notifications_title),
                            color = Color.White,
                            fontSize = 17.sp,
                            lineHeight = 24.sp,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 0.5.sp,
                        )
                    },
                    navigationIcon = {
                        val backClick = rememberSingleClickHandler(onClick = callbacks.onNavigateBack)
                        IconButton(
                            onClick = { backClick() },
                            modifier = Modifier.testTag(NotificationsTestTags.BACK_BUTTON),
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_back_chevron),
                                contentDescription = stringResource(R.string.a11y_notifications_back),
                                tint = Color.White,
                            )
                        }
                    },
                    colors =
                        TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Transparent,
                            scrolledContainerColor = Color.Transparent,
                            titleContentColor = Color.White,
                            navigationIconContentColor = Color.White,
                            actionIconContentColor = Color.White,
                        ),
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) },
        ) { padding ->
            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(padding),
            ) {
                ReadAllRow(
                    enabled = state.unreadCount > 0,
                    onClick = callbacks.onReadAll,
                )
                Spacer(Modifier.height(12.dp))
                Box(modifier = Modifier.weight(1f)) {
                    when (val listState = state.listState) {
                        NotificationsListState.Loading -> LoadingPlaceholder()
                        NotificationsListState.Empty -> EmptyPlaceholder()
                        is NotificationsListState.Error -> ErrorPlaceholder(listState, callbacks.onRefresh)
                        is NotificationsListState.Loaded -> LoadedCard(listState.items, callbacks)
                    }
                }
            }
        }
    }
}

/**
 * Inline "Đánh dấu đọc tất cả" row — icon + text, no background.
 * Mirrors Figma `mms_Button_read all` (`6885:9392`): 20dp left margin,
 * 4dp gap between icon and label, Montserrat 700 14sp white. Dims to
 * 40% alpha when there's nothing to read so the visual still reserves
 * its space (Q-N-10 no-op behavior).
 */
@Composable
private fun ReadAllRow(
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val click = rememberSingleClickHandler(onClick = onClick)
    val a11y = stringResource(R.string.a11y_notifications_mark_all_read)
    val tint = if (enabled) Color.White else Color.White.copy(alpha = 0.4f)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(enabled = enabled, role = Role.Button) { click() }
                .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 16.dp)
                .testTag(NotificationsTestTags.MARK_ALL_READ_BUTTON)
                .semantics { contentDescription = a11y },
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Outlined.PlaylistAddCheck,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(24.dp),
        )
        Text(
            text = stringResource(R.string.notifications_mark_all_read),
            color = tint,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun LoadingPlaceholder() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(color = Color.White)
    }
}

@Composable
private fun EmptyPlaceholder() {
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp)
                .testTag(NotificationsTestTags.EMPTY_PLACEHOLDER),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(R.string.notifications_empty),
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 14.sp,
            lineHeight = 20.sp,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun ErrorPlaceholder(
    state: NotificationsListState.Error,
    onRetry: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(state.messageRes),
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 14.sp,
            lineHeight = 20.sp,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = onRetry,
            modifier = Modifier.testTag(NotificationsTestTags.ERROR_RETRY),
        ) {
            Text(stringResource(R.string.notifications_retry))
        }
    }
}

@Composable
private fun LoadedCard(
    items: List<NotificationItem>,
    callbacks: NotificationsCallbacks,
) {
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 0.dp),
        modifier =
            Modifier
                .fillMaxSize()
                .testTag(NotificationsTestTags.LIST),
    ) {
        item {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(ListCardColor),
            ) {
                items.forEachIndexed { index, item ->
                    NotificationRow(
                        item = item,
                        onRowTap = { callbacks.onRowTap(item) },
                        onInlineCommunityStandardsTap = callbacks.onInlineCommunityStandardsTap,
                    )
                    if (index < items.lastIndex) {
                        HorizontalDivider(
                            thickness = 1.dp,
                            color = DividerColor,
                            modifier = Modifier.padding(horizontal = 8.dp),
                        )
                    }
                }
            }
        }
        item { Spacer(Modifier.height(24.dp)) }
    }
}

/**
 * Callback bag for [NotificationsContent]. Mirrors the SearchSunner
 * pattern — the Hilt screen wraps each VM method + each nav lambda
 * into a single lambda here so the stateless content composable can
 * be mounted directly from an instrumented test.
 */
data class NotificationsCallbacks(
    val onNavigateBack: () -> Unit,
    val onRowTap: (NotificationItem) -> Unit,
    val onReadAll: () -> Unit,
    val onRefresh: () -> Unit,
    val onLoadMore: () -> Unit,
    val onConsumeSnackbar: () -> Unit,
    val onInlineCommunityStandardsTap: () -> Unit,
)

// ── Figma tokens (queried 2026-05-15 against frame _b68CBWKl5) ──────
private val ListCardColor: Color = Color(0x99000A0F) // rgba(0,7,12,0.6)
private val DividerColor: Color = Color(0xFF2E3940)
private val HeaderGradient: Brush =
    Brush.verticalGradient(
        colors =
            listOf(
                Color(0xE600101A), // #00101A at 0.9 opacity (top)
                Color(0x4D00101A),
                Color(0x0000101A), // transparent
            ),
    )

// ── Previews ────────────────────────────────────────────────────────

@Preview(name = "Notifications — loaded", showBackground = true)
@Preview(
    name = "Notifications — loaded (dark)",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun NotificationsContentLoadedPreview() {
    val now = Clock.System.now()
    NotificationsContent(
        state =
            NotificationsUiState(
                unreadCount = 3,
                listState =
                    NotificationsListState.Loaded(
                        items =
                            listOf(
                                NotificationItem(
                                    "p1",
                                    NotificationType.KUDOS_RECEIVED,
                                    false,
                                    now - 12.minutes,
                                    NotificationPayload.KudoRef("kudo-1", false),
                                    "Sunner Huỳnh Dương Xuân Nhật vừa gửi đến bạn lời ghi nhận đầy yêu thương!",
                                ),
                                NotificationItem(
                                    "p2",
                                    NotificationType.HEART_RECEIVED,
                                    true,
                                    now - 2.hours,
                                    NotificationPayload.KudoRef("kudo-2", true),
                                    "Wow! Lời nhắn gửi của bạn cho Sunner <tên Sunner> vừa nhận thêm lượt tim!",
                                ),
                                NotificationItem(
                                    "p3",
                                    NotificationType.CONTENT_HIDDEN,
                                    true,
                                    now - 1.days,
                                    NotificationPayload.KudoRef("kudo-3", false),
                                    "Tiếc quá! Bạn có một lời nhắn bị tạm ẩn vì \"vướng\" một số tiêu chuẩn! Hãy xem các tiêu chuẩn và gửi lại cho đồng đội nhé!",
                                ),
                                NotificationItem(
                                    "p4",
                                    NotificationType.SECRET_BOX_UNLOCK,
                                    true,
                                    now - 3.days,
                                    NotificationPayload.SecretBox,
                                    "Bạn vừa mở khoá một Hộp bí mật",
                                ),
                            ),
                    ),
            ),
        callbacks = previewCallbacks(),
    )
}

@Preview(name = "Notifications — empty", showBackground = true)
@Composable
private fun NotificationsContentEmptyPreview() {
    NotificationsContent(
        state = NotificationsUiState(listState = NotificationsListState.Empty),
        callbacks = previewCallbacks(),
    )
}

@Composable
private fun previewCallbacks(): NotificationsCallbacks =
    NotificationsCallbacks(
        onNavigateBack = {},
        onRowTap = {},
        onReadAll = {},
        onRefresh = {},
        onLoadMore = {},
        onConsumeSnackbar = {},
        onInlineCommunityStandardsTap = {},
    )
