@file:OptIn(
    kotlin.time.ExperimentalTime::class,
    androidx.compose.material3.ExperimentalMaterial3Api::class,
)

package com.example.aiddproject.kudos.notifications.ui

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
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
import com.example.aiddproject.kudos.notifications.ui.components.ReadAllButton
import com.example.aiddproject.ui.theme.SaaInk
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

/**
 * Stateless content for the Notifications screen.
 *
 * Renders the TopAppBar (back + title + read-all action) and branches
 * on `state.listState` to show Loading / Empty / Error / Loaded.
 * Owns no state — every callback comes from [callbacks].
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
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = stringResource(R.string.notifications_title),
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
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
                    actions = {
                        ReadAllButton(
                            onClick = callbacks.onReadAll,
                            enabled = state.unreadCount > 0,
                        )
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
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(padding),
            ) {
                when (val listState = state.listState) {
                    NotificationsListState.Loading -> LoadingPlaceholder()
                    NotificationsListState.Empty -> EmptyPlaceholder()
                    is NotificationsListState.Error -> ErrorPlaceholder(listState, callbacks.onRefresh)
                    is NotificationsListState.Loaded -> LoadedList(listState.items, callbacks)
                }
            }
        }
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
        androidx.compose.foundation.layout.Spacer(Modifier.padding(8.dp))
        Button(
            onClick = onRetry,
            modifier = Modifier.testTag(NotificationsTestTags.ERROR_RETRY),
        ) {
            Text(stringResource(R.string.notifications_retry))
        }
    }
}

@Composable
private fun LoadedList(
    items: List<NotificationItem>,
    callbacks: NotificationsCallbacks,
) {
    LazyColumn(
        modifier =
            Modifier
                .fillMaxWidth()
                .testTag(NotificationsTestTags.LIST),
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        items(items = items, key = { it.id }) { item ->
            NotificationRow(
                item = item,
                onRowTap = { callbacks.onRowTap(item) },
                onInlineCommunityStandardsTap = callbacks.onInlineCommunityStandardsTap,
            )
        }
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
                                NotificationItem("p1", NotificationType.KUDOS_RECEIVED, false, now - 12.minutes,
                                    NotificationPayload.KudoRef("kudo-1", false), "Bạn vừa nhận Kudo từ Hồng Nhung"),
                                NotificationItem("p2", NotificationType.HEART_RECEIVED, false, now - 2.hours,
                                    NotificationPayload.KudoRef("kudo-2", true), "Một Sunner ẩn danh đã thả tim cho Kudo của bạn"),
                                NotificationItem("p3", NotificationType.CONTENT_HIDDEN, false, now - 1.days,
                                    NotificationPayload.KudoRef("kudo-3", false), "Một bài viết của bạn đã bị ẩn vì vi phạm"),
                                NotificationItem("p4", NotificationType.SECRET_BOX_UNLOCK, true, now - 3.days,
                                    NotificationPayload.SecretBox, "Bạn vừa mở khoá một Hộp bí mật"),
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
