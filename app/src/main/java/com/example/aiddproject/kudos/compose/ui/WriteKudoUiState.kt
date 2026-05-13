package com.example.aiddproject.kudos.compose.ui

import com.example.aiddproject.kudos.compose.domain.RichTextValue
import com.example.aiddproject.kudos.compose.domain.UploadedImage
import com.example.aiddproject.kudos.compose.domain.WriteKudoFieldErrors
import com.example.aiddproject.kudos.domain.Hashtag
import com.example.aiddproject.kudos.domain.SunnerNode

/**
 * Single read model for the Viết Kudo composer — observed as a
 * `StateFlow<WriteKudoUiState>` by `WriteKudoScreen` (plan § Stateless
 * content pattern).
 *
 * Submit-enablement is the derived [isSubmitEnabled] getter; it
 * deliberately is NOT a stored field so the truth source stays the
 * combination of recipient + title + message + tags + isSending.
 *
 * The four overlay sub-states are sealed interfaces so each renders
 * its own loading / empty / error / loaded branches without
 * boolean-flag soup at the call site.
 */
data class WriteKudoUiState(
    // ── Form values ─────────────────────────────────────────────────
    val recipientId: String? = null,
    val recipientName: String? = null,
    val title: String = "",
    val message: RichTextValue = RichTextValue.Empty,
    val tags: List<Hashtag> = emptyList(),
    val images: List<UploadedImage> = emptyList(),
    val isAnonymous: Boolean = false,
    // ── Validation + lifecycle ──────────────────────────────────────
    val fieldErrors: WriteKudoFieldErrors = WriteKudoFieldErrors.None,
    val isSending: Boolean = false,
    val formDirty: Boolean = false,
    // ── Overlay state ───────────────────────────────────────────────
    val recipientPicker: RecipientPickerState = RecipientPickerState.Closed,
    val hashtagPicker: HashtagPickerState = HashtagPickerState.Closed,
    val mentionOverlay: MentionOverlayState = MentionOverlayState.Closed,
    val linkDialog: LinkDialogState? = null,
    val confirmDialog: ConfirmDialogState? = null,
    // ── Caret / selection — used by toolbar transforms ──────────────
    val messageSelection: IntRange = IntRange.EMPTY,
    // ── Submit-time feedback ────────────────────────────────────────
    val snackbar: SnackbarMessage? = null,
) {
    /**
     * Derived: the Send button shows as M3-enabled iff every required
     * field has a valid value AND we are not currently submitting.
     *
     * Note: tapping the M3-disabled button still fires `onSend` via the
     * outer-Box gesture-intercept pattern (plan § Notes) — that path
     * routes to `revealErrors()` instead of submitting.
     */
    val isSubmitEnabled: Boolean
        get() =
            recipientId != null &&
                title.isNotBlank() &&
                message.plainText.trim().isNotEmpty() &&
                message.plainText.length <= 1000 &&
                tags.isNotEmpty() &&
                !isSending
}

/** Recipient picker overlay state — sub-flow `5MU728Tjck`. */
sealed interface RecipientPickerState {
    /** Picker hasn't been opened yet. */
    data object Closed : RecipientPickerState

    /** Picker is open; reusable [query] feeds the debounced search flow. */
    data class Open(
        val query: String = "",
        val results: ResultState = ResultState.Loading,
    ) : RecipientPickerState

    sealed interface ResultState {
        data object Loading : ResultState

        data object NoResults : ResultState

        data class Error(val messageRes: Int) : ResultState

        data class Loaded(val items: List<SunnerNode>) : ResultState
    }
}

/** Hashtag picker overlay state — sub-flow `aKWA2klsnt`. */
sealed interface HashtagPickerState {
    data object Closed : HashtagPickerState

    data class Open(val results: ResultState = ResultState.Loading) : HashtagPickerState

    sealed interface ResultState {
        data object Loading : ResultState

        data object Empty : ResultState

        data class Error(val messageRes: Int) : ResultState

        data class Loaded(val items: List<Hashtag>) : ResultState
    }
}

/** Confirmation dialog state — currently only the unsaved-changes flow. */
sealed interface ConfirmDialogState {
    data object UnsavedChanges : ConfirmDialogState
}

/** @mention suggestion overlay state (US4). */
sealed interface MentionOverlayState {
    data object Closed : MentionOverlayState

    /**
     * Overlay open with the typed query (text after the `@` trigger).
     * The result list reuses [RecipientPickerState.ResultState] so the
     * overlay composable can share rendering code.
     */
    data class Open(
        val query: String,
        val triggerOffset: Int,
        val results: RecipientPickerState.ResultState = RecipientPickerState.ResultState.Loading,
    ) : MentionOverlayState
}

/**
 * C.5 link-insert dialog state. Holds the user-typed URL,
 * the optional "invalid URL" inline error flag, AND the textarea
 * selection captured at the moment the dialog opened so the
 * `applyLink` transform can wrap the right range.
 */
data class LinkDialogState(
    val url: String = "",
    val capturedSelection: IntRange = IntRange.EMPTY,
    val showInvalidError: Boolean = false,
)

/** One-shot snackbar feedback (generic submit / upload errors). */
data class SnackbarMessage(val messageRes: Int)
