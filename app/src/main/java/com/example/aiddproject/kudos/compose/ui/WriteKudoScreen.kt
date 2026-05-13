package com.example.aiddproject.kudos.compose.ui

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import android.widget.Toast
import kotlinx.coroutines.flow.collectLatest

/**
 * Hilt entry composable for the Viết Kudo composer (T053).
 *
 * 401s are handled globally by the `AuthErrorInterceptor` installed
 * on the Supabase Ktor client (same gate Home + the hub rely on) —
 * no per-screen SessionGate wrap is needed.
 *
 * Wires:
 *   - `BackHandler` that mirrors Cancel when the form is dirty OR
 *     a submit is in flight (FR-007 / FR-012 / TR-004).
 *   - One-shot event collection: `Submitted` → [onSubmitted];
 *     `NavigateBack` → [onNavigateBack].
 *   - Snackbar surface for generic submit failures (Toast MVP;
 *     Phase 10 polish swaps in M3 SnackbarHost).
 */
@Composable
fun WriteKudoScreen(
    onSubmitted: () -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToCommunityStandards: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: WriteKudoViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    BackHandler(enabled = state.formDirty || state.isSending) {
        viewModel.onCancelTap()
    }

    LaunchedEffect(viewModel) {
        viewModel.events.collectLatest { event ->
            when (event) {
                WriteKudoEvent.Submitted -> onSubmitted()
                WriteKudoEvent.NavigateBack -> onNavigateBack()
            }
        }
    }

    state.snackbar?.let { msg ->
        val text = stringResource(msg.messageRes)
        LaunchedEffect(msg) {
            Toast.makeText(context, text, Toast.LENGTH_LONG).show()
            viewModel.onSnackbarShown()
        }
    }

    WriteKudoScreenContent(
        state = state,
        callbacks =
            WriteKudoCallbacks(
                onRecipientPickerOpen = viewModel::onRecipientPickerOpen,
                onRecipientPickerDismiss = viewModel::onRecipientPickerDismiss,
                onRecipientQueryChange = viewModel::onRecipientQueryChange,
                onRecipientChosen = viewModel::onRecipientChosen,
                onRecipientRetry = viewModel::onRecipientRetry,
                onTitleChange = viewModel::onTitleChange,
                onMessageChange = viewModel::onMessageChange,
                onHashtagPickerOpen = viewModel::onHashtagPickerOpen,
                onHashtagPickerDismiss = viewModel::onHashtagPickerDismiss,
                onHashtagAdd = viewModel::onHashtagAdd,
                onHashtagRemove = viewModel::onHashtagRemove,
                onAnonymousToggle = viewModel::onAnonymousToggle,
                onCommunityStandardsTap = onNavigateToCommunityStandards,
                onCancelTap = viewModel::onCancelTap,
                onSendTap = viewModel::onSendTap,
                onConfirmDiscard = viewModel::onConfirmDiscard,
                onDismissConfirmDialog = viewModel::onDismissConfirmDialog,
            ),
        modifier = modifier,
    )
}
