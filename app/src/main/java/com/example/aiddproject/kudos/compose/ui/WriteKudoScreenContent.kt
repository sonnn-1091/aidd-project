package com.example.aiddproject.kudos.compose.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.aiddproject.R
import com.example.aiddproject.kudos.compose.domain.RichTextValue
import com.example.aiddproject.kudos.compose.domain.WriteKudoValidators
import com.example.aiddproject.kudos.compose.ui.components.AnonymousToggle
import com.example.aiddproject.kudos.compose.ui.components.BottomActionBar
import com.example.aiddproject.kudos.compose.ui.components.CommunityStandardsLink
import com.example.aiddproject.kudos.compose.ui.components.HashtagPickerOverlay
import com.example.aiddproject.kudos.compose.ui.components.HashtagSection
import com.example.aiddproject.kudos.compose.ui.components.HeaderText
import com.example.aiddproject.kudos.compose.ui.components.MessageEditor
import com.example.aiddproject.kudos.compose.ui.components.RecipientPickerField
import com.example.aiddproject.kudos.compose.ui.components.RecipientPickerOverlay
import com.example.aiddproject.kudos.compose.ui.components.TitleField
import com.example.aiddproject.kudos.domain.Hashtag
import com.example.aiddproject.kudos.domain.SunnerNode

/**
 * Stateless content composable for the Viết Kudo composer (T052).
 *
 * Drives the entire screen from a single [state] + callback bag —
 * Compose UI tests target this directly without instantiating the
 * Hilt VM (Constitution V).
 *
 * Layout (Figma top-to-bottom):
 *   Scaffold
 *     ├── content (scrollable column)
 *     │     ├── HeaderText (A)
 *     │     ├── RecipientPickerField (B.1 + B.2)
 *     │     ├── TitleField (B.3 + B.4)
 *     │     ├── CommunityStandardsLink (B.5)
 *     │     ├── MessageEditor (D — plain-text MVP)
 *     │     ├── HashtagSection (E)
 *     │     └── AnonymousToggle (G)
 *     └── bottomBar = BottomActionBar (H + I) — sticky
 *   + overlays (RecipientPickerOverlay, HashtagPickerOverlay,
 *               UnsavedChangesDialog)
 */
@Composable
fun WriteKudoScreenContent(
    state: WriteKudoUiState,
    callbacks: WriteKudoCallbacks,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier =
            modifier
                .fillMaxSize()
                .testTag(WriteKudoTestTags.SCREEN),
        bottomBar = {
            BottomActionBar(
                isSubmitEnabled = state.isSubmitEnabled,
                isSending = state.isSending,
                onCancel = callbacks.onCancelTap,
                onSend = callbacks.onSendTap,
            )
        },
    ) { padding ->
        FormColumn(padding = padding, state = state, callbacks = callbacks)
    }

    // ── Overlays ────────────────────────────────────────────────────
    val picker = state.recipientPicker
    if (picker is RecipientPickerState.Open) {
        RecipientPickerOverlay(
            state = picker,
            onQueryChange = callbacks.onRecipientQueryChange,
            onPick = callbacks.onRecipientChosen,
            onDismiss = callbacks.onRecipientPickerDismiss,
            onRetry = callbacks.onRecipientRetry,
        )
    }

    val hashtagPicker = state.hashtagPicker
    if (hashtagPicker is HashtagPickerState.Open) {
        HashtagPickerOverlay(
            state = hashtagPicker,
            selected = state.tags.map { it.id },
            onAdd = callbacks.onHashtagAdd,
            onRemove = callbacks.onHashtagRemove,
            onDismiss = callbacks.onHashtagPickerDismiss,
        )
    }

    if (state.confirmDialog is ConfirmDialogState.UnsavedChanges) {
        UnsavedChangesDialog(
            onConfirm = callbacks.onConfirmDiscard,
            onDismiss = callbacks.onDismissConfirmDialog,
        )
    }
}

@Composable
private fun FormColumn(
    padding: PaddingValues,
    state: WriteKudoUiState,
    callbacks: WriteKudoCallbacks,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        HeaderText()
        RecipientPickerField(
            recipientName = state.recipientName ?: state.recipientId, // fall back to id until resolved
            onOpenPicker = callbacks.onRecipientPickerOpen,
            errorRes = state.fieldErrors.recipient,
            enabled = !state.isSending,
        )
        TitleField(
            value = state.title,
            onValueChange = callbacks.onTitleChange,
            errorRes = state.fieldErrors.title,
        )
        CommunityStandardsLink(onTap = callbacks.onCommunityStandardsTap)
        MessageEditor(
            value = state.message,
            onValueChange = callbacks.onMessageChange,
            errorRes = state.fieldErrors.message,
            enabled = !state.isSending,
        )
        // Annotate the message error path to include the explicit
        // over-1000 case (the validator reports it on Send; the
        // counter visualises it live regardless).
        // No additional UI — handled inside MessageEditor.
        // The MAX_MESSAGE_LENGTH constant is re-exported for tests.
        @Suppress("UNUSED_EXPRESSION") WriteKudoValidators.MAX_MESSAGE_LENGTH

        HashtagSection(
            tags = state.tags,
            onAddTap = callbacks.onHashtagPickerOpen,
            onRemoveTag = { callbacks.onHashtagRemove(it.id) },
            errorRes = state.fieldErrors.hashtags,
        )
        AnonymousToggle(
            checked = state.isAnonymous,
            onCheckedChange = callbacks.onAnonymousToggle,
        )
    }
}

@Composable
private fun UnsavedChangesDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.write_kudo_confirm_discard_title)) },
        text = { Text(stringResource(R.string.write_kudo_confirm_discard_body)) },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                modifier = Modifier.testTag(WriteKudoTestTags.CONFIRM_DIALOG_CONFIRM),
            ) { Text(stringResource(R.string.write_kudo_confirm_discard_confirm)) }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag(WriteKudoTestTags.CONFIRM_DIALOG_DISMISS),
            ) { Text(stringResource(R.string.write_kudo_confirm_discard_dismiss)) }
        },
        modifier = Modifier.testTag(WriteKudoTestTags.CONFIRM_DIALOG),
    )
}

/**
 * Callback bag for [WriteKudoScreenContent]. Grouping the ~15 handlers
 * into one class keeps the stateless API readable AND makes Compose UI
 * tests easier to drive (pass a single `WriteKudoCallbacks(mock(),
 * mock(), ...)` instance with the captures you care about).
 */
data class WriteKudoCallbacks(
    val onRecipientPickerOpen: () -> Unit,
    val onRecipientPickerDismiss: () -> Unit,
    val onRecipientQueryChange: (String) -> Unit,
    val onRecipientChosen: (SunnerNode) -> Unit,
    val onRecipientRetry: () -> Unit,
    val onTitleChange: (String) -> Unit,
    val onMessageChange: (RichTextValue) -> Unit,
    val onHashtagPickerOpen: () -> Unit,
    val onHashtagPickerDismiss: () -> Unit,
    val onHashtagAdd: (Hashtag) -> Unit,
    val onHashtagRemove: (String) -> Unit,
    val onAnonymousToggle: (Boolean) -> Unit,
    val onCommunityStandardsTap: () -> Unit,
    val onCancelTap: () -> Unit,
    val onSendTap: () -> Unit,
    val onConfirmDiscard: () -> Unit,
    val onDismissConfirmDialog: () -> Unit,
)
