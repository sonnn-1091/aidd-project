package com.example.aiddproject.kudos.compose.ui

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aiddproject.R
import com.example.aiddproject.auth.login.data.AuthRepository
import com.example.aiddproject.core.richtext.MessageMarkdown
import com.example.aiddproject.core.richtext.UrlValidator
import com.example.aiddproject.kudos.compose.data.WriteKudoErrorMapper
import com.example.aiddproject.kudos.compose.domain.RichTextValue
import com.example.aiddproject.kudos.compose.domain.UploadedImage
import com.example.aiddproject.kudos.compose.domain.WriteKudoDraft
import com.example.aiddproject.kudos.compose.domain.WriteKudoFieldErrors
import com.example.aiddproject.kudos.compose.domain.WriteKudoValidators
import com.example.aiddproject.kudos.data.KudosRepository
import com.example.aiddproject.kudos.domain.Hashtag
import com.example.aiddproject.kudos.domain.SunnerNode
import com.example.aiddproject.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

/**
 * Hilt ViewModel for the Viết Kudo composer (T035–T039 / FR-001..012,
 * TR-001..005). Owns the entire form state, the recipient + hashtag
 * picker overlays, the @mention overlay (Phase 6), and the
 * single-flight submit job slot.
 *
 * The novel "tap-on-disabled-Send reveals errors" pattern (plan §
 * Notes) is implemented by [onSendTap]: the outer Box gesture
 * intercept always fires this handler; the handler forks on
 * [WriteKudoUiState.isSubmitEnabled].
 */
@HiltViewModel
class WriteKudoViewModel
    @Inject
    constructor(
        private val savedStateHandle: SavedStateHandle,
        private val kudosRepository: KudosRepository,
        private val authRepository: AuthRepository,
    ) : ViewModel() {
        private val _state = MutableStateFlow(initialState())
        val state: StateFlow<WriteKudoUiState> = _state.asStateFlow()

        // One-shot events to the UI (navigation, snackbars).
        private val _events =
            MutableSharedFlow<WriteKudoEvent>(
                replay = 0,
                extraBufferCapacity = 4,
                onBufferOverflow = BufferOverflow.DROP_OLDEST,
            )
        val events: SharedFlow<WriteKudoEvent> = _events.asSharedFlow()

        // Single-flight submit (FR-009/012).
        private var submitJob: Job? = null

        // ── Field-update intents (T036) ──────────────────────────────

        fun onRecipientChosen(node: SunnerNode) {
            _state.update {
                it.copy(
                    recipientId = node.id,
                    recipientName = node.fullName,
                    recipientPicker = RecipientPickerState.Closed,
                    formDirty = true,
                    fieldErrors = it.fieldErrors.copy(recipient = null),
                )
            }
        }

        fun onTitleChange(value: String) {
            // Cap input length to MAX_TITLE_LENGTH to make the
            // "too long" error path purely server-side; the field
            // simply refuses additional characters at the input gate.
            val clipped =
                if (value.length > WriteKudoValidators.MAX_TITLE_LENGTH) {
                    value.take(WriteKudoValidators.MAX_TITLE_LENGTH)
                } else {
                    value
                }
            _state.update {
                it.copy(
                    title = clipped,
                    formDirty = true,
                    fieldErrors = it.fieldErrors.copy(title = null),
                )
            }
        }

        fun onMessageChange(value: RichTextValue) {
            _state.update {
                it.copy(
                    message = value,
                    formDirty = true,
                    fieldErrors = it.fieldErrors.copy(message = null),
                )
            }
        }

        fun onMessageSelectionChange(range: IntRange) {
            _state.update { it.copy(messageSelection = range) }
        }

        // ── Toolbar transforms (T082) ────────────────────────────────

        fun onToolbarAction(action: ToolbarAction) {
            val s = _state.value
            val sel = s.messageSelection
            val next =
                when (action) {
                    ToolbarAction.Bold -> MessageMarkdown.applyBold(s.message, sel)
                    ToolbarAction.Italic -> MessageMarkdown.applyItalic(s.message, sel)
                    ToolbarAction.Strikethrough -> MessageMarkdown.applyStrikethrough(s.message, sel)
                    ToolbarAction.NumberedList -> MessageMarkdown.applyNumberedList(s.message, sel)
                    ToolbarAction.Quote -> MessageMarkdown.applyQuote(s.message, sel)
                }
            onMessageChange(next)
        }

        // ── Link dialog (T083) ───────────────────────────────────────

        fun onLinkButtonTap() {
            _state.update {
                it.copy(linkDialog = LinkDialogState(capturedSelection = it.messageSelection))
            }
        }

        fun onLinkDialogUrlChange(url: String) {
            _state.update { current ->
                val dialog = current.linkDialog ?: return@update current
                current.copy(linkDialog = dialog.copy(url = url, showInvalidError = false))
            }
        }

        fun onLinkDialogSubmit() {
            val dialog = _state.value.linkDialog ?: return
            if (!UrlValidator.isValid(dialog.url)) {
                _state.update { it.copy(linkDialog = dialog.copy(showInvalidError = true)) }
                return
            }
            val s = _state.value
            val next = MessageMarkdown.applyLink(s.message, dialog.capturedSelection, dialog.url.trim())
            _state.update { it.copy(message = next, linkDialog = null, formDirty = true) }
        }

        fun onLinkDialogDismiss() {
            _state.update { it.copy(linkDialog = null) }
        }

        // ── Mention overlay (T082 / @mention) ────────────────────────

        fun onMentionTriggered(
            query: String,
            triggerOffset: Int,
        ) {
            _state.update {
                it.copy(
                    mentionOverlay =
                        MentionOverlayState.Open(
                            query = query,
                            triggerOffset = triggerOffset,
                            results = RecipientPickerState.ResultState.Loading,
                        ),
                )
            }
            viewModelScope.launch {
                val result = kudosRepository.searchSunner(query)
                val selfId = authRepository.currentUserId()
                _state.update { current ->
                    val overlay = current.mentionOverlay
                    if (overlay !is MentionOverlayState.Open) return@update current
                    val next =
                        result.fold(
                            onSuccess = { matches ->
                                val filtered = matches.map { it.node }.filter { it.id != selfId }
                                if (filtered.isEmpty()) {
                                    RecipientPickerState.ResultState.NoResults
                                } else {
                                    RecipientPickerState.ResultState.Loaded(filtered)
                                }
                            },
                            onFailure = { RecipientPickerState.ResultState.Error(R.string.write_kudo_recipient_load_error) },
                        )
                    current.copy(mentionOverlay = overlay.copy(results = next))
                }
            }
        }

        fun onMentionPick(node: SunnerNode) {
            val s = _state.value
            val overlay = s.mentionOverlay as? MentionOverlayState.Open ?: return
            val src = s.message.plainText
            val before = src.substring(0, overlay.triggerOffset)
            // Skip past the existing "@..." typed run (including the @).
            val afterAtRunStart = overlay.triggerOffset
            val tail =
                src.substring(afterAtRunStart).let { rest ->
                    // Drop the leading `@xyz` token so it gets replaced by the mention.
                    if (rest.startsWith("@")) {
                        val end = rest.indexOfFirst { it.isWhitespace() }.takeIf { it >= 0 } ?: rest.length
                        rest.substring(end)
                    } else {
                        rest
                    }
                }
            val replacement = "@${node.fullName} "
            val newPlain = before + replacement + tail
            _state.update {
                it.copy(
                    message = RichTextValue.ofPlainText(newPlain),
                    mentionOverlay = MentionOverlayState.Closed,
                    formDirty = true,
                )
            }
        }

        fun onMentionDismiss() {
            _state.update { it.copy(mentionOverlay = MentionOverlayState.Closed) }
        }

        fun onHashtagAdd(tag: Hashtag) {
            _state.update { current ->
                if (current.tags.any { it.id == tag.id }) {
                    current
                } else if (current.tags.size >= WriteKudoValidators.MAX_HASHTAGS) {
                    current.copy(
                        fieldErrors = current.fieldErrors.copy(hashtags = R.string.write_kudo_error_hashtags_max),
                    )
                } else {
                    current.copy(
                        tags = current.tags + tag,
                        formDirty = true,
                        fieldErrors = current.fieldErrors.copy(hashtags = null),
                    )
                }
            }
        }

        fun onHashtagRemove(tagId: String) {
            _state.update { current ->
                current.copy(
                    tags = current.tags.filterNot { it.id == tagId },
                    formDirty = true,
                    fieldErrors = current.fieldErrors.copy(hashtags = null),
                )
            }
        }

        fun onAnonymousToggle(checked: Boolean) {
            _state.update {
                it.copy(
                    isAnonymous = checked,
                    // Clear nickname + its inline error when toggling off so
                    // the next on-toggle starts from a blank slate.
                    anonymousNickname = if (checked) it.anonymousNickname else "",
                    formDirty = true,
                    fieldErrors = it.fieldErrors.copy(anonymousNickname = null),
                )
            }
        }

        fun onAnonymousNicknameChange(value: String) {
            _state.update {
                it.copy(
                    anonymousNickname = value,
                    formDirty = true,
                    fieldErrors = it.fieldErrors.copy(anonymousNickname = null),
                )
            }
        }

        // ── Image attachments (Q-W-2 — T092/T093) ────────────────────

        /**
         * Append a picked image to the local list AFTER validating mime
         * + size. NO Storage upload happens here — that's deferred to
         * [onSendTap] (FR-008 + Q-W-2). Rejected files surface the
         * inline error and do NOT add a thumbnail.
         */
        fun onImagePicked(
            uri: Uri,
            mime: String,
            sizeBytes: Long,
        ) {
            if (mime !in WriteKudoValidators.ALLOWED_IMAGE_MIMES) {
                _state.update { it.copy(fieldErrors = it.fieldErrors.copy(images = R.string.write_kudo_error_image_type)) }
                return
            }
            if (sizeBytes > WriteKudoValidators.MAX_IMAGE_BYTES) {
                _state.update { it.copy(fieldErrors = it.fieldErrors.copy(images = R.string.write_kudo_error_image_size)) }
                return
            }
            _state.update { current ->
                if (current.images.size >= WriteKudoValidators.MAX_IMAGES) {
                    current
                } else {
                    current.copy(
                        images =
                            current.images +
                                UploadedImage(
                                    clientId = UUID.randomUUID().toString(),
                                    localUri = uri,
                                    sizeBytes = sizeBytes,
                                    mime = mime,
                                    storagePath = null,
                                ),
                        formDirty = true,
                        fieldErrors = current.fieldErrors.copy(images = null),
                    )
                }
            }
        }

        /** Remove a picked image — pure local-list mutation per Q-W-2. */
        fun onImageRemove(clientId: String) {
            _state.update { current ->
                current.copy(
                    images = current.images.filterNot { it.clientId == clientId },
                    formDirty = true,
                    fieldErrors = current.fieldErrors.copy(images = null),
                )
            }
        }

        // ── Recipient picker (T038) ──────────────────────────────────

        fun onRecipientPickerOpen() {
            _state.update {
                if (it.recipientPicker is RecipientPickerState.Open) {
                    it
                } else {
                    it.copy(recipientPicker = RecipientPickerState.Open())
                }
            }
            loadRecipientList()
        }

        fun onRecipientPickerDismiss() {
            _state.update { it.copy(recipientPicker = RecipientPickerState.Closed) }
        }

        /**
         * Kept on the surface for the @mention overlay's debounced
         * search flow; the recipient picker itself no longer renders a
         * search input (per Figma sub-flow `5MU728Tjck`).
         */
        @Suppress("unused")
        fun onRecipientQueryChange(
            @Suppress("UNUSED_PARAMETER") query: String,
        ) = Unit

        fun onRecipientRetry() {
            loadRecipientList()
        }

        /**
         * Run the directory query once and populate the picker results
         * — no debounce since there's no search input to debounce
         * against. Filters out the current authenticated user as a UX
         * hint; server RLS is the authoritative self-send gate.
         */
        private fun loadRecipientList() {
            viewModelScope.launch {
                val callResult = kudosRepository.searchSunner(query = "")
                val selfId = authRepository.currentUserId()
                _state.update { current ->
                    val picker = current.recipientPicker
                    if (picker !is RecipientPickerState.Open) return@update current
                    val next =
                        callResult.fold(
                            onSuccess = { matches ->
                                val filtered = matches.map { it.node }.filter { it.id != selfId }
                                if (filtered.isEmpty()) {
                                    RecipientPickerState.ResultState.NoResults
                                } else {
                                    RecipientPickerState.ResultState.Loaded(filtered)
                                }
                            },
                            onFailure = { RecipientPickerState.ResultState.Error(R.string.write_kudo_recipient_load_error) },
                        )
                    current.copy(recipientPicker = picker.copy(results = next))
                }
            }
        }

        // ── Hashtag picker (T039) ────────────────────────────────────

        fun onHashtagPickerOpen() {
            _state.update { it.copy(hashtagPicker = HashtagPickerState.Open()) }
            viewModelScope.launch {
                val result = kudosRepository.listHashtags()
                _state.update { current ->
                    if (current.hashtagPicker !is HashtagPickerState.Open) return@update current
                    val next =
                        result.fold(
                            onSuccess = { list ->
                                if (list.isEmpty()) {
                                    HashtagPickerState.ResultState.Empty
                                } else {
                                    HashtagPickerState.ResultState.Loaded(list)
                                }
                            },
                            onFailure = { HashtagPickerState.ResultState.Error(R.string.write_kudo_hashtag_load_error) },
                        )
                    current.copy(hashtagPicker = HashtagPickerState.Open(results = next))
                }
            }
        }

        fun onHashtagPickerDismiss() {
            _state.update { it.copy(hashtagPicker = HashtagPickerState.Closed) }
        }

        // ── Send / Cancel (T037) ─────────────────────────────────────

        /**
         * Q-W-2 submit flow — uploads each image sequentially under
         * `kudos-attachments/{auth.uid()}/{kudoId}/...`, INSERTs the
         * kudos row with the resolved image_ids, and on any failure
         * rolls back by deleting every successfully-uploaded object.
         */
        fun onSendTap() {
            val current = _state.value
            if (!current.isSubmitEnabled) {
                revealErrors()
                return
            }
            if (current.isSending) return // single-flight (FR-009)

            submitJob =
                viewModelScope.launch {
                    _state.update { it.copy(isSending = true, fieldErrors = WriteKudoFieldErrors.None) }
                    val kudoId = UUID.randomUUID().toString()
                    val uploaded = mutableListOf<UploadedImage>()

                    // ── Step 1: upload images sequentially (Q-W-2) ──
                    var imageFailure: Throwable? = null
                    for ((index, picked) in current.images.withIndex()) {
                        val uploadResult = kudosRepository.uploadKudoImage(kudoId, index, picked.localUri)
                        if (uploadResult.isSuccess) {
                            uploaded += uploadResult.getOrNull()!!
                        } else {
                            imageFailure = uploadResult.exceptionOrNull()
                            break
                        }
                    }

                    if (imageFailure != null) {
                        // Rollback: delete every successful upload before exit.
                        uploaded.forEach { kudosRepository.deleteKudoImage(it) }
                        _state.update {
                            it.copy(
                                isSending = false,
                                fieldErrors = it.fieldErrors.copy(images = R.string.write_kudo_error_image_upload),
                            )
                        }
                        return@launch
                    }

                    // ── Step 2: INSERT the kudos row ────────────────
                    val draft =
                        WriteKudoDraft(
                            id = kudoId,
                            recipientId = current.recipientId!!,
                            title = current.title,
                            message = current.message.markdown,
                            tags = current.tags.map { it.id },
                            imageIds = uploaded.mapNotNull { it.storagePath },
                            isAnonymous = current.isAnonymous,
                            anonymousNickname =
                                current.anonymousNickname.trim().takeIf { current.isAnonymous && it.isNotEmpty() },
                        )
                    val result = kudosRepository.createKudo(draft)
                    result.fold(
                        onSuccess = {
                            _state.update { it.copy(isSending = false) }
                            _events.tryEmit(WriteKudoEvent.Submitted)
                        },
                        onFailure = { throwable ->
                            // Rollback the uploaded images too.
                            uploaded.forEach { kudosRepository.deleteKudoImage(it) }
                            val mapped = WriteKudoErrorMapper.map(throwable)
                            if (mapped.hasAny) {
                                _state.update { it.copy(isSending = false, fieldErrors = mapped) }
                            } else {
                                _state.update {
                                    it.copy(
                                        isSending = false,
                                        snackbar = SnackbarMessage(R.string.write_kudo_error_submit_generic),
                                    )
                                }
                            }
                        },
                    )
                }
        }

        fun revealErrors() {
            val s = _state.value
            val errors =
                WriteKudoFieldErrors(
                    recipient = WriteKudoValidators.validateRecipient(s.recipientId, authRepository.currentUserId()),
                    title = WriteKudoValidators.validateTitle(s.title),
                    message = WriteKudoValidators.validateMessage(s.message),
                    hashtags = WriteKudoValidators.validateHashtags(s.tags.map { it.id }),
                    images = s.fieldErrors.images,
                    anonymousNickname =
                        WriteKudoValidators.validateAnonymousNickname(s.isAnonymous, s.anonymousNickname),
                )
            _state.update { it.copy(fieldErrors = errors) }
        }

        fun onCancelTap() {
            val current = _state.value
            if (current.isSending) {
                // Cancel the in-flight submit; stay on screen.
                submitJob?.cancel()
                submitJob = null
                _state.update { it.copy(isSending = false) }
                return
            }
            if (current.formDirty) {
                _state.update { it.copy(confirmDialog = ConfirmDialogState.UnsavedChanges) }
            } else {
                _events.tryEmit(WriteKudoEvent.NavigateBack)
            }
        }

        fun onConfirmDiscard() {
            _state.update { it.copy(confirmDialog = null) }
            _events.tryEmit(WriteKudoEvent.NavigateBack)
        }

        fun onDismissConfirmDialog() {
            _state.update { it.copy(confirmDialog = null) }
        }

        fun onSnackbarShown() {
            _state.update { it.copy(snackbar = null) }
        }

        // ── Internals ────────────────────────────────────────────────

        private fun initialState(): WriteKudoUiState {
            val prefilledId: String? = savedStateHandle[Routes.WRITE_KUDO_ARG_RECIPIENT]
            return if (prefilledId.isNullOrBlank()) {
                WriteKudoUiState()
            } else {
                WriteKudoUiState(
                    recipientId = prefilledId,
                    recipientName = null, // resolved by the picker / repo when needed
                    formDirty = false, // prefill does NOT dirty (US1 Sc4)
                )
            }
        }
    }

/** One-shot events emitted by the VM to the UI layer. */
sealed interface WriteKudoEvent {
    data object Submitted : WriteKudoEvent

    data object NavigateBack : WriteKudoEvent
}

/** Formatting-toolbar action taps (T082). */
enum class ToolbarAction {
    Bold,
    Italic,
    Strikethrough,
    NumberedList,
    Quote,
}
