package com.example.aiddproject.kudos.compose.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aiddproject.R
import com.example.aiddproject.kudos.compose.domain.RichTextValue
import com.example.aiddproject.kudos.compose.domain.WriteKudoValidators
import com.example.aiddproject.home.ui.components.HomeBottomBar
import com.example.aiddproject.home.ui.components.HomeNavTab
import com.example.aiddproject.kudos.compose.ui.components.AnonymousToggle
import com.example.aiddproject.kudos.compose.ui.components.BottomActionBar
import com.example.aiddproject.kudos.compose.ui.components.HashtagSection
import com.example.aiddproject.kudos.compose.ui.components.ImageSection
import com.example.aiddproject.kudos.compose.ui.components.LinkInsertDialog
import com.example.aiddproject.kudos.compose.ui.components.MentionSuggestionOverlay
import com.example.aiddproject.kudos.compose.ui.components.MessageEditor
import com.example.aiddproject.kudos.compose.ui.components.RecipientPickerField
import com.example.aiddproject.kudos.compose.ui.components.TitleField
import com.example.aiddproject.kudos.domain.Hashtag
import com.example.aiddproject.kudos.domain.SunnerNode

/**
 * Stateless content composable for the Viết Kudo composer (T052) +
 * `momorph.implement-ui` polish pass against Figma `7fFAb-K35a`.
 *
 * Layout (Figma top-to-bottom):
 *   Box (full-bleed `bg_home` + 140dp top gradient — matches hub chrome)
 *     ├── TopAppBar: ← + "New Kudo" title (transparent, over the
 *     │              keyvisual background)
 *     ├── Form card (cream #FFF8E1, 11dp radius, 18/12dp padding) —
 *     │   the scrollable Column of A → recipient row → title row →
 *     │   helper text → message editor → hashtag → image → anonymous.
 *     └── BottomActionBar (sticky H + I).
 *   + overlays: RecipientPickerOverlay / HashtagPickerOverlay /
 *               UnsavedChangesDialog / LinkInsertDialog /
 *               MentionSuggestionOverlay.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WriteKudoScreenContent(
    state: WriteKudoUiState,
    callbacks: WriteKudoCallbacks,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(KvBaseBackground)
                .testTag(WriteKudoTestTags.SCREEN),
    ) {
        // Keyvisual artwork behind everything — same as Sun*Kudos hub.
        Image(
            painter = painterResource(R.drawable.bg_home),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
        // 140dp top gradient so the TopAppBar title reads against the
        // dark band even with the busy keyvisual underneath.
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
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = stringResource(R.string.write_kudo_top_bar_title),
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = callbacks.onCancelTap) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.a11y_write_kudo_cancel),
                                tint = Color.White,
                            )
                        }
                    },
                    colors =
                        TopAppBarDefaults.centerAlignedTopAppBarColors(
                            containerColor = Color.Transparent,
                            navigationIconContentColor = Color.White,
                            titleContentColor = Color.White,
                        ),
                    modifier = Modifier.statusBarsPadding(),
                )
            },
            bottomBar = {
                Column {
                    BottomActionBar(
                        isSubmitEnabled = state.isSubmitEnabled,
                        isSending = state.isSending,
                        onCancel = callbacks.onCancelTap,
                        onSend = callbacks.onSendTap,
                    )
                    HomeBottomBar(
                        selected = HomeNavTab.Kudos,
                        onTabSelect = callbacks.onSelectBottomTab,
                    )
                }
            },
        ) { padding ->
            FormCard(padding = padding, state = state, callbacks = callbacks)
        }
    }

    // ── Top-level overlays (both recipient and hashtag pickers now
    // anchor INSIDE their trigger via DropdownMenu, so neither is
    // rendered here). ──
    if (state.confirmDialog is ConfirmDialogState.UnsavedChanges) {
        UnsavedChangesDialog(
            onConfirm = callbacks.onConfirmDiscard,
            onDismiss = callbacks.onDismissConfirmDialog,
        )
    }

    state.linkDialog?.let { linkDialog ->
        LinkInsertDialog(
            state = linkDialog,
            onUrlChange = callbacks.onLinkDialogUrlChange,
            onSubmit = callbacks.onLinkDialogSubmit,
            onDismiss = callbacks.onLinkDialogDismiss,
        )
    }
}

/**
 * Cream form card per Figma `Viết KUDO` (6885:9291). Hosts the
 * scrollable Column of form fields + the bottom mention overlay.
 */
@Composable
private fun FormCard(
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
                .padding(horizontal = 20.dp, vertical = 16.dp),
    ) {
        Surface(
            color = FormCardBackground,
            shape = RoundedCornerShape(11.dp),
            tonalElevation = 0.dp,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // A — instructional header
                Text(
                    text = stringResource(R.string.write_kudo_header),
                    color = FormCardTextPrimary,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .testTag(WriteKudoTestTags.HEADER),
                )

                RecipientPickerField(
                    recipientName = state.recipientName ?: state.recipientId,
                    pickerState = state.recipientPicker,
                    onOpenPicker = callbacks.onRecipientPickerOpen,
                    onDismissPicker = callbacks.onRecipientPickerDismiss,
                    onPick = callbacks.onRecipientChosen,
                    onRetry = callbacks.onRecipientRetry,
                    errorRes = state.fieldErrors.recipient,
                    enabled = !state.isSending,
                )
                TitleField(
                    value = state.title,
                    onValueChange = callbacks.onTitleChange,
                    errorRes = state.fieldErrors.title,
                )

                // B.5 helper text — descriptive (NOT the Community
                // Standards link, which lives inside the toolbar row).
                Text(
                    text = stringResource(R.string.write_kudo_title_helper),
                    color = FormCardTextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    fontWeight = FontWeight.Normal,
                    modifier = Modifier.fillMaxWidth(),
                )

                MessageEditor(
                    value = state.message,
                    onValueChange = callbacks.onMessageChange,
                    onSelectionChange = callbacks.onMessageSelectionChange,
                    onMentionQueryChange = callbacks.onMentionTriggered,
                    onMentionDismiss = callbacks.onMentionDismiss,
                    onToolbarAction = callbacks.onToolbarAction,
                    onLinkTap = callbacks.onLinkButtonTap,
                    onCommunityStandardsTap = callbacks.onCommunityStandardsTap,
                    errorRes = state.fieldErrors.message,
                    enabled = !state.isSending,
                )

                val mention = state.mentionOverlay
                if (mention is MentionOverlayState.Open) {
                    MentionSuggestionOverlay(state = mention, onPick = callbacks.onMentionPick)
                }
                @Suppress("UNUSED_EXPRESSION") WriteKudoValidators.MAX_MESSAGE_LENGTH

                HashtagSection(
                    tags = state.tags,
                    pickerState = state.hashtagPicker,
                    onAddTap = callbacks.onHashtagPickerOpen,
                    onPickerDismiss = callbacks.onHashtagPickerDismiss,
                    onHashtagAdd = callbacks.onHashtagAdd,
                    onRemoveTag = callbacks.onHashtagRemove,
                    errorRes = state.fieldErrors.hashtags,
                )
                ImageSection(
                    images = state.images,
                    onAddTap = callbacks.onImageAddTap,
                    onRemoveImage = callbacks.onImageRemove,
                    errorRes = state.fieldErrors.images,
                )
                AnonymousToggle(
                    checked = state.isAnonymous,
                    onCheckedChange = callbacks.onAnonymousToggle,
                    nickname = state.anonymousNickname,
                    onNicknameChange = callbacks.onAnonymousNicknameChange,
                    nicknameErrorRes = state.fieldErrors.anonymousNickname,
                )
            }
        }
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

data class WriteKudoCallbacks(
    val onRecipientPickerOpen: () -> Unit,
    val onRecipientPickerDismiss: () -> Unit,
    val onRecipientQueryChange: (String) -> Unit,
    val onRecipientChosen: (SunnerNode) -> Unit,
    val onRecipientRetry: () -> Unit,
    val onTitleChange: (String) -> Unit,
    val onMessageChange: (RichTextValue) -> Unit,
    val onMessageSelectionChange: (IntRange) -> Unit,
    val onHashtagPickerOpen: () -> Unit,
    val onHashtagPickerDismiss: () -> Unit,
    val onHashtagAdd: (Hashtag) -> Unit,
    val onHashtagRemove: (String) -> Unit,
    val onAnonymousToggle: (Boolean) -> Unit,
    val onAnonymousNicknameChange: (String) -> Unit,
    val onCommunityStandardsTap: () -> Unit,
    val onCancelTap: () -> Unit,
    val onSendTap: () -> Unit,
    val onConfirmDiscard: () -> Unit,
    val onDismissConfirmDialog: () -> Unit,
    val onToolbarAction: (ToolbarAction) -> Unit,
    val onLinkButtonTap: () -> Unit,
    val onLinkDialogUrlChange: (String) -> Unit,
    val onLinkDialogSubmit: () -> Unit,
    val onLinkDialogDismiss: () -> Unit,
    val onMentionTriggered: (query: String, triggerOffset: Int) -> Unit,
    val onMentionDismiss: () -> Unit,
    val onMentionPick: (SunnerNode) -> Unit,
    val onImageAddTap: () -> Unit,
    val onImageRemove: (clientId: String) -> Unit,
    val onSelectBottomTab: (HomeNavTab) -> Unit,
)

// ── Figma tokens (queried 2026-05-13 from frame 7fFAb-K35a) ─────────

private val KvBaseBackground: Color = Color(0xFF00070C)
private val FormCardBackground: Color = Color(0xFFFFF8E1)
private val FormCardTextPrimary: Color = Color(0xFF00101A)
private val FormCardTextSecondary: Color = Color(0xFF999999)

private val HeaderGradient: Brush =
    Brush.verticalGradient(
        colors =
            listOf(
                Color(0xE6001019),
                Color(0x4D00101A),
                Color(0x00001019),
            ),
    )
