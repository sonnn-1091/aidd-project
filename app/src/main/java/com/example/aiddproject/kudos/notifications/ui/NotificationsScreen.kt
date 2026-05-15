package com.example.aiddproject.kudos.notifications.ui

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * Hilt entry composable for the Notifications screen.
 *
 * Wires:
 *  - `BackHandler` so system-back routes through [onNavigateBack] (the
 *    bell-tap host re-mounts when we pop, so its bell badge picks up
 *    the new count via the shared singleton).
 *  - A [NotificationRouter] built from the nav lambdas, so the VM's
 *    row-tap logic can dispatch without leaking NavController.
 */
@Composable
fun NotificationsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToKudoDetail: (kudoId: String, isAnonymous: Boolean) -> Unit,
    onNavigateToSecretBoxOpen: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToCommunityStandards: () -> Unit,
    onNavigateToAdminReview: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NotificationsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    BackHandler(onBack = onNavigateBack)

    val router =
        remember(onNavigateToKudoDetail, onNavigateToSecretBoxOpen, onNavigateToProfile, onNavigateToAdminReview) {
            NotificationRouter(
                onNavigateToKudoDetail = onNavigateToKudoDetail,
                onNavigateToSecretBoxOpen = onNavigateToSecretBoxOpen,
                onNavigateToProfile = onNavigateToProfile,
                onNavigateToAdminReview = onNavigateToAdminReview,
            )
        }

    NotificationsContent(
        state = state,
        callbacks =
            NotificationsCallbacks(
                onNavigateBack = onNavigateBack,
                onRowTap = { item -> viewModel.onRowTap(item, router) },
                onReadAll = viewModel::onReadAll,
                onRefresh = viewModel::onRefresh,
                onLoadMore = viewModel::onLoadMore,
                onConsumeSnackbar = viewModel::onConsumeSnackbar,
                onInlineCommunityStandardsTap = onNavigateToCommunityStandards,
            ),
        modifier = modifier,
    )
}
