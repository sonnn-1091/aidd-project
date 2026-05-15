# Implementation Plan: Viết Kudo (Write Kudo composer)

**Frame**: `7fFAb-K35a-iOS-Sun-Kudos-Viet-Kudo`
**Date**: 2026-05-13
**Spec**: `spec.md` (reviewed twice 2026-05-13)
**Status**: Reviewed twice (plan review passes 2026-05-13); Q-W-1, Q-W-2, Q-W-3, Q-W-5 resolved 2026-05-13; only Q-W-4 (Community Standards target screen) remains and is recommended out of scope.

---

## Summary

Viết Kudo is the **write-path** of the Sun*Kudos feature: a single
form screen owned by a `WriteKudoViewModel` that captures
`recipient_id`, `title`, rich-text `message`, `tags` (1–5),
optional `image_ids` (0–5), and `is_anonymous`, then POSTs a new
`kudos` row. On success it returns to the Sun*Kudos hub which
refreshes Highlight + All-Kudos so the new card surfaces at the
top.

The hub (`fO0Kt19sZZ`) and its routing scaffolding are already
shipped — `Routes.WRITE_KUDO` exists at `navigation/Routes.kt:24`
and the hub's Send pill already wires `onNavigateToSendKudos →
navController.navigate(Routes.WRITE_KUDO)` (`navigation/AppNavigation.kt:139`).
**The Home FAB pencil → `Routes.WRITE_KUDO` is also already wired**
end-to-end (`navigation/AppNavigation.kt:104` → `HomeScreen.kt:96`
→ `HomeFab.kt:60` `onPencilClick`). The destination is currently a
`PlaceholderScreen("Write a Kudo")` at `navigation/AppNavigation.kt:147`.
This plan replaces that placeholder with a real `WriteKudoScreen`
and adds an optional nav argument for the recipient prefill path
(used by the future Search Sunner entry).

**Existing infrastructure to reuse** (≈50% of the screen's surface):

- **Routing**: `Routes.WRITE_KUDO` + hub Send pill + Home FAB
  pencil are ALL pre-wired. Only the placeholder destination at
  `AppNavigation.kt:147` needs to flip to the real composable.
  No changes to `HomeFab.kt`, `HomeScreen.kt`, or the Home
  composable block.
- **Auth gate**: `core/auth/AuthRedirectController` +
  `core/session/SessionGate` handle 401 globally — composer just
  surfaces the failure to the global handler.
- **Repository pattern**: `kudos/data/KudosRepository` interface +
  `SupabaseKudosRepository` impl + `DemoKudosRepository` fake +
  `KudosRepositoryModule` Hilt binding already live for the hub.
  This plan EXTENDS the same interface with three new methods
  (`createKudo`, `uploadKudoImage`, `deleteKudoImage`) instead of
  introducing a second interface — composer ops are semantically
  cohesive with the rest of the kudos data surface.
- **Reusable composer pieces from the hub**:
  - `kudos/ui/components/HashtagFilterDropdown` — same M3
    `DropdownMenu` chrome the hashtag picker (E.2) and recipient
    picker (B.2) can reuse via a generic `SelectionDropdown`.
  - `kudos/domain/Hashtag` + `Department` already model the
    catalog rows.
  - `kudos/domain/SpotlightSearch` already implements debounced
    sunner search — the recipient dropdown reuses the same
    `KudosRepository.searchSunner` + debounce pattern.
- **Chrome shared with Home / hub**: `HomeHeader` (logo + language
  pill + search + bell) is NOT used here per Figma — the composer
  is full-bleed form with only a sticky bottom action bar (H + I).
  No top app bar.
- **Single-click suppression**: `core/ui/rememberSingleClickHandler`
  used on B.5 (Community Standards), H (Cancel), I (Send), F.5
  (Add Image), C.1–C.6, E.2 "+ Hashtag" trigger.

**Net new code** is concentrated in a sibling `kudos/compose/`
package mirroring the hub's `ui/data/domain` split, plus a new
`core/richtext/` helper if `kudos.message` is encoded as a
custom format. The single routing change is flipping
`navigation/AppNavigation.kt:147` from the placeholder to the
real `WriteKudoScreen` (with an optional recipient nav arg).

---

## Technical Context

**Language / Framework**: Kotlin 2.2.10 + Jetpack Compose + Material 3
**Primary Dependencies**: Hilt (DI), Supabase Kotlin SDK
(`supabase-kt`) — `from(...).insert(...)` for kudos rows +
Storage client for image upload, Coil 2.7 (image thumbnails),
Timber (telemetry), `androidx.navigation.compose`.

**New Compose APIs** (already in the BOM, no new deps):

- `androidx.compose.foundation.text.input.TextField` (M3) for
  Title (B.4) and the rich-text editor host for Message (D).
- `androidx.compose.material3.AlertDialog` for the unsaved-changes
  confirmation dialog (US2 Sc2) and the C.5 link-insert dialog.
- `androidx.compose.material3.SnackbarHost` reusing the hub's
  `CopyLinkSnackbarHost` pattern for generic submit / upload
  errors.
- `androidx.activity.compose.rememberLauncherForActivityResult`
  + `ActivityResultContracts.PickVisualMedia` for the system
  image picker (F.5) — modern photo picker, no runtime permission
  required.
- `androidx.activity.compose.BackHandler` for system-back with
  `formDirty` confirmation (FR-007, TR-004).

**Rich-text encoding for `kudos.message`** — see Open Question
Q-W-1 below. Default plan: store as a Markdown subset
(`**bold**`, `*italic*`, `~~strike~~`, `1.`/`2.` numbered lists,
`> quote`, `[label](url)` for links, `@FullName` mentions as
plain text) round-trippable with the hub's
`KudosFeedCard` renderer. A `core/richtext/MessageMarkdown.kt`
module owns the encode/decode + per-toolbar-action transforms.
This choice is deferred to plan-execution if Q-W-1 resolves
differently.

**Database (Supabase / Postgres)**: New/extended objects required:

- `kudos` table — add columns if not present: `title TEXT NOT NULL
  CHECK (char_length(title) BETWEEN 1 AND 100)`, `tags TEXT[] NOT
  NULL CHECK (array_length(tags, 1) BETWEEN 1 AND 5)`, `image_ids
  TEXT[] NOT NULL DEFAULT '{}' CHECK (array_length(image_ids, 1)
  IS NULL OR array_length(image_ids, 1) <= 5)`, `is_anonymous
  BOOLEAN NOT NULL DEFAULT FALSE`. **Verify** before migration —
  the hub feed cards already render these fields so they are
  likely shipped.
- **RLS policy** on `kudos` for INSERT:
  - `sender_id = auth.uid()` (auto-set via `DEFAULT auth.uid()`).
  - `recipient_id != auth.uid()` (self-send rejection — surfaces as
    a structured error mapped to FR-005 "You cannot send a kudo
    to yourself.").
  - `recipient_id` must exist in `users` (FK).
  - Sender must be in an authenticated session.
- **Hashtag validation** — server-side `tags <@ (SELECT array_agg(id)
  FROM hashtags)` to reject unknown tag values, OR a trigger that
  rewrites `tags` to known IDs.
- **Storage**: a new bucket `kudos-attachments/{user_id}/{kudo_id}/`
  with RLS allowing INSERT/DELETE only when the row owner equals
  `auth.uid()`. Bucket-level mime-type allow-list `image/jpeg`,
  `image/png`, `image/webp`; 10 MB size limit at the bucket
  policy.
- Existing `users` view (Supabase Auth + profile) — used for
  recipient picker + @mention overlay. RLS: every authenticated
  user can SELECT every other user's id + display_name +
  avatar_url + department. No new columns needed.

**Testing**: JUnit4 + Compose UI Test + Robolectric (unit) +
Espresso (instrumented) + Hilt testing rule + mockk.

**State Management**: One `WriteKudoViewModel` exposing
`StateFlow<WriteKudoUiState>` (per spec § State Management).
`SavedStateHandle` for the optional prefill arg + cross-rotation
preservation of form fields. A single `viewModelScope` job slot
named `submitJob` handles the active POST so the Cancel-mid-submit
contract (FR-012) can `submitJob.cancel()` cleanly.

**API Style**: Direct Supabase Postgrest + Storage via the existing
repository pattern. The 5 conceptual endpoints in spec § API
Requirements are realized as `supabaseClient.from("kudos")
.insert(...)` / `storage.from(...).upload(...)` / `.delete(...)` /
`.from("users").select(...)` / `.from("hashtags").select(...)`.
The auth-token + RLS pipeline matches the hub.

---

## Constitution Compliance Check

*GATE: Must pass before implementation can begin.*

- [x] **I. Clean Code & Source Organization** — new feature sub-
      package `com.example.aiddproject.kudos.compose/` with `ui/`,
      `data/`, `domain/` mirroring the hub. `WriteKudoScreenContent`
      splits into ~12 sub-composables, each <150 LOC (header A,
      recipient picker B.2, title input B.4, community-standards
      link B.5, formatting toolbar C, message textarea D, hashtag
      section E, image section F, anonymous toggle G, bottom action
      bar H+I, unsaved-changes dialog, link-insert dialog). Kotlin
      official style via `ktlint`.
- [x] **II. Tech Stack Best Practices** — immutable data classes
      (`WriteKudoUiState`, `WriteKudoFieldErrors`,
      `UploadedImage`, `MentionSuggestion`), `Flow`/`StateFlow` for
      async, repository pattern extending the existing
      `KudosRepository`. Supabase anon key only; all versions
      pinned via `libs.versions.toml`. NEW Compose APIs come from
      the existing `androidx-compose-bom`; no new top-level deps.
- [x] **III. Material Design 3 (Android)** — M3 `TextField`,
      `OutlinedTextField`, `AlertDialog`, `DropdownMenu`,
      `FilterChip` for hashtag chips, `IconButton` for toolbar
      toggles + thumbnail delete, `Checkbox` + Text for the
      anonymous toggle (Row click affordance), `Button` (filled)
      for Send, `TextButton` for Cancel. 48dp touch targets on
      every interactive element; localized `contentDescription`
      per spec § A11y; live region announcements via Compose
      `LiveRegionMode.Polite/Assertive` semantics.
- [x] **IV. OWASP Secure Coding** — Supabase RLS on `kudos`
      INSERT enforces self-send rejection (TR-002); Storage RLS
      scopes uploads to `auth.uid()` (TR-003); client validates
      file mime + size before upload (FR-008, defence in depth);
      no kudos message body or recipient names logged via
      `SecureTimberTree`; no new secrets, no new PII surface
      beyond what the hub already exposes.
- [x] **V. Test-Driven Development** — every new public function
      in `compose/data/`, `compose/domain/`, and the
      `WriteKudoViewModel` ships with failing tests committed
      before the implementation. Each user story has a
      corresponding Compose UI test class (US-level) + ViewModel
      unit test (state-machine). RLS policy tests for the
      self-send rejection ship with the migration.

**Violations**: None. The plan introduces no new top-level deps
and no constitution amendments.

---

## Architecture Decisions

### Frontend Approach

- **Component Structure**: Feature-first sub-package
  `com.example.aiddproject.kudos.compose/` with three folders:
  - `ui/` — `WriteKudoScreen.kt` (Hilt entry),
    `WriteKudoScreenContent.kt` (stateless),
    `WriteKudoUiState.kt`, `WriteKudoViewModel.kt`,
    `WriteKudoTestTags.kt`, plus `ui/components/` for every
    section sub-composable + dialogs.
  - `data/` — extends `kudos/data/KudosRepository` with three new
    methods. New file `WriteKudoErrors.kt` maps server error
    payloads → `WriteKudoFieldError` sealed class.
  - `domain/` — `WriteKudoDraft` (immutable form value),
    `MentionSuggestion`, `UploadedImage`, `WriteKudoFieldError`.

- **Stateless content pattern**: `WriteKudoScreenContent` takes
  `state: WriteKudoUiState` + callbacks (`onRecipientPick`,
  `onTitleChange`, `onMessageChange`, `onToolbarToggle`,
  `onMentionPick`, `onHashtagAdd`, `onHashtagRemove`, `onAddImage`,
  `onRemoveImage`, `onAnonymousToggle`, `onCommunityStandards`,
  `onCancel`, `onSend`, `onDismissConfirmDialog`,
  `onConfirmDiscard`, `onDismissLinkDialog`, `onSubmitLink`).
  Compose UI tests drive this directly (never touch the live
  `WriteKudoViewModel` — Constitution V).

- **Send-disabled vs. tap-reveals-errors implementation** —
  per spec FR-002, the `I` button is rendered as `Button(enabled =
  state.isSubmitEnabled, ...)` for visuals BUT wrapped in an outer
  `Box(Modifier.clickable(onClick = onSend))` so taps on the
  disabled-looking button still fire `onSend`. The VM's `onSend`
  handler routes to `revealErrors()` if `isSubmitEnabled` is false
  (the `0le8xKnFE_` contract), otherwise submits. (See "Reveal-
  errors-on-disabled-tap composable" pattern note in `Notes`.)

- **Styling Strategy**: Same approach as the hub — visual chrome
  fetched on-demand via `query_section` at task-execution time
  (Constitution Principle II); no `design-style.md` artifact per
  the project convention. The implementer will pull frame
  `7fFAb-K35a` (default), `PV7jBVZU1N` (with-content variant),
  `0le8xKnFE_` (error state), `5MU728Tjck` (recipient dropdown),
  and `aKWA2klsnt` (hashtag dropdown) at the relevant tasks.

- **Data Fetching**:
  - Recipient picker (B.2): lazy on first open;
    `KudosRepository.searchSunner(query)` with 200 ms debounce
    flowing through `Flow<String>.debounce(200).distinctUntilChanged()`
    in the VM. Cached in `WriteKudoUiState.recipientSearch.results`.
  - Hashtag picker (E.2): lazy on first open;
    `KudosRepository.listHashtags()` once per session, cached in
    the VM.
  - @mention overlay (D): same recipient search debounce, gated
    by `mentionQuery != null`.
  - Submit (I): `KudosRepository.createKudo(draft)` called inside
    a tracked `submitJob` Job slot so Cancel-mid-submit can
    cancel it (FR-012). On success, set a `kudoSubmitted = true`
    flag on the back-stack entry's `savedStateHandle` and pop;
    the hub composable observes this in a `LaunchedEffect` on
    its `currentBackStackEntry` and triggers a refresh.

### WriteKudoUiState shape (concrete)

```kotlin
data class WriteKudoUiState(
    // Form values
    val recipientId: String? = null,
    val recipientName: String? = null,
    val title: String = "",
    val message: RichTextValue = RichTextValue.Empty,
    val tags: List<HashtagId> = emptyList(),
    val images: List<UploadedImage> = emptyList(),
    val isAnonymous: Boolean = false,

    // Field-level errors (cleared per-field on edit)
    val fieldErrors: WriteKudoFieldErrors = WriteKudoFieldErrors.None,

    // Form lifecycle
    val isSending: Boolean = false,
    val formDirty: Boolean = false,

    // Overlays
    val recipientPicker: RecipientPickerState = RecipientPickerState.Idle,
    val hashtagPicker: HashtagPickerState = HashtagPickerState.Idle,
    val mentionOverlay: MentionOverlayState = MentionOverlayState.Idle,
    val linkDialog: LinkDialogState? = null,
    val confirmDialog: ConfirmDialogState? = null,

    // Submit-time feedback
    val snackbar: SnackbarMessage? = null,
) {
    val isSubmitEnabled: Boolean
        get() = recipientId != null &&
                title.isNotBlank() &&
                message.plainText.trim().isNotEmpty() &&
                tags.isNotEmpty() &&
                !isSending
}
```

`WriteKudoFieldErrors` is a `data class` with one nullable
`@StringRes Int` slot per required field (recipient, title,
message, hashtags). Encoded as a data class rather than a `Map` so
Compose recomposition skips when an unrelated field changes.

### Backend Approach

- **API Design**: Direct Supabase Postgrest INSERT for `kudos`
  rows + Supabase Storage upload for attachments. Five
  conceptual endpoints (spec § API Requirements) realized as
  `KudosRepository` methods (see "Repository surface" below).
- **Validation**: Two layers per Constitution IV:
  1. Client validators in `compose/domain/WriteKudoValidators.kt`
     for instant feedback (US3 tap-reveals-errors).
  2. Server-side constraints + RLS on `kudos` (self-send + tag
     whitelist + length checks). Server errors map to the same
     `WriteKudoFieldErrors` slots so the UI is path-agnostic.
- **Image upload model (Q-W-2 — no drafts)**: picked files are
  held client-side as `Uri` + validated mime/size only; NO
  Storage upload happens at pick time. The submit flow is:
  1. Client-side validation passes; `isSending = true`.
  2. Client generates `kudoId: UUID` (this will be the new row's
     primary key + the Storage subfolder name).
  3. For each `images[i]`, sequentially upload to
     `kudos-attachments/{user_id}/{kudoId}/{i}_{filename}`. If
     any upload fails:
     - DELETE every successfully-uploaded object for this
       submission (rollback so no orphans).
     - Surface a localized inline error in F; `isSending = false`;
       form remains editable (FR-011).
  4. INSERT the `kudos` row with `id = kudoId` + `image_ids =
     [the uploaded paths]`. If INSERT fails:
     - DELETE every uploaded image for this kudoId.
     - Surface error per the I-component error matrix.
  5. On full success: navigate back, set `kudoSubmitted = true`
     on `previousBackStackEntry.savedStateHandle`.

  Removing an image before submit is purely a local-list mutation
  — no Storage call needed because nothing was uploaded yet.
  FR-010 ("MUST delete the storage object") is trivially
  satisfied — there is no storage object to delete prior to
  submit. The 24-h `_drafts/*` cron job is no longer needed.

### Integration Points

- **Existing services**:
  - `kudos/data/KudosRepository` (interface + Supabase + Demo
    impls + Hilt module) — extended with `createKudo`,
    `uploadKudoImage`, `deleteKudoImage`. `searchSunner` and
    `listHashtags` reused unchanged.
  - `core/session/SessionGate` — wraps the composable so 401
    handling is consistent with the rest of the authenticated
    tab tree.
  - `core/auth/AuthRedirectController` — global 401 →
    `Routes.LOGIN`; the composer just lets Supabase exceptions
    bubble.
  - `core/locale/LanguagePreferenceRepository` —
    `LocalLanguage.current` injection for VN/EN strings.
  - `core/ui/rememberSingleClickHandler` — wraps every primary
    tap target.
- **Shared components reused** (no copy-paste):
  - The hub's `HashtagFilterDropdown` is M3-styled; extracted into
    a generic `compose/ui/components/SelectionDropdown.kt`
    parameterized by item type and selected-state highlight, then
    consumed by both the recipient picker (B.2), hashtag picker
    (E.2) — and refactored into the hub's existing
    `HashtagFilterDropdown` + `DepartmentFilterDropdown` callers
    if scope allows (defer if it expands the diff).
  - The hub's `KudosFeedCard` rich-text renderer (currently
    plain-text) gains a `RichText(value: RichTextValue)`
    primitive shared with the composer's message preview
    (deferred to a Phase 7 polish task — not blocking MVP).
- **API contracts** (full signatures live in the "Repository
  surface" block below):
  - `createKudo(draft)` — INSERT a `kudos` row with a client-
    supplied UUID id; returns the server-derived row.
  - `uploadKudoImage(kudoId, index, uri)` — submit-time upload
    to `kudos-attachments/{auth.uid()}/{kudoId}/{index}_{filename}`.
  - `deleteKudoImage(ref)` — rollback delete for partial-failure
    submits.

### Repository surface (extended interface)

```kotlin
interface KudosRepository {
    // ... existing methods unchanged ...

    /** US1 / FR-001 — insert a new `kudos` row with a client-
     * supplied primary-key `id` (UUID). Returns the server-
     * derived row (including `sender_id`, `created_at`).
     * Server enforces RLS (self-send rejection, tag whitelist,
     * lengths). */
    suspend fun createKudo(draft: WriteKudoDraft): Result<Kudos>

    /** US5 / Q-W-2 submit-time upload — POSTs the file to
     * `kudos-attachments/{auth.uid()}/{kudoId}/{index}_{filename}`
     * and returns the storage path. Client validates mime + size
     * (FR-008) before calling. */
    suspend fun uploadKudoImage(
        kudoId: String,
        index: Int,
        uri: Uri,
    ): Result<UploadedImage>

    /** US5 / Q-W-2 rollback — delete a previously-uploaded image
     * when a partial-failure submit needs to clean up. */
    suspend fun deleteKudoImage(ref: UploadedImage): Result<Unit>
}
```

`WriteKudoDraft` MUST carry the client-generated `id: String`
(UUID), the resolved `image_ids: List<String>` (Storage paths
returned by `uploadKudoImage`), and all the form fields. The VM
constructs the draft only after every image upload succeeds.

### Routing changes

| Route                  | Where                                            | Change                                                                                                                                                              |
| ---------------------- | ------------------------------------------------ | ------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `Routes.WRITE_KUDO`    | `navigation/Routes.kt:24`                        | **No constant change.** Add a `writeKudo(recipientUserId: String? = null)` helper that returns the route + optional nav-arg.                                        |
| `Routes.WRITE_KUDO_PATTERN` | `navigation/Routes.kt` (new const)          | New pattern constant `"route_write_kudo?recipientUserId={recipientUserId}"` so the destination accepts an optional argument.                                       |
| Compose destination    | `navigation/AppNavigation.kt:147`                | Replace `PlaceholderScreen("Write a Kudo")` with `WriteKudoScreen(...)` Hilt entry. Wire `onSubmitSuccess` to set `savedStateHandle["kudoSubmitted"] = true` + pop. |
| Home FAB pencil click  | `home/ui/components/HomeFab.kt` + `HomeScreen`   | Wire the pencil icon's `onClick` to `navController.navigate(Routes.writeKudo())`. The a11y label `a11y_home_fab_compose_kudo` is already in place.                  |
| Hub Send pill          | `navigation/AppNavigation.kt:139`                | Unchanged. Already navigates to `Routes.WRITE_KUDO` (no recipient arg).                                                                                              |
| Hub-side post-submit refresh | `kudos/ui/KudosScreen.kt`                  | Observe `savedStateHandle.getStateFlow("kudoSubmitted", false)` in a `LaunchedEffect` — when true, call `kudosViewModel.refresh()` then reset to false.              |
| Search Sunner → composer entry | `Routes.kt` + Search Sunner spec (future)  | Search Sunner spec ships separately; this plan exposes `recipientUserId` as the nav arg so the future flow can pass it.                                              |

---

## Project Structure

### Documentation (this feature)

```text
.momorph/specs/7fFAb-K35a-iOS-Sun-Kudos-Viet-Kudo/
├── spec.md       # Feature specification (reviewed twice)
├── plan.md       # This file
└── tasks.md      # Task breakdown (next step — run /momorph.tasks)
```

No `design-style.md`, no `research.md`, no `contract.md` — per
project convention (visuals fetched at implementation time via
`query_section`; API contracts inline in this plan).

### Source code (affected areas)

#### New files

| File                                                                                              | Purpose                                                              |
| ------------------------------------------------------------------------------------------------- | -------------------------------------------------------------------- |
| `kudos/compose/ui/WriteKudoScreen.kt`                                                             | Hilt entry composable; observes VM state and renders Content.        |
| `kudos/compose/ui/WriteKudoScreenContent.kt`                                                      | Stateless content composable + callbacks (Compose UI test target).   |
| `kudos/compose/ui/WriteKudoUiState.kt`                                                            | `WriteKudoUiState` + sub-state sealed types.                         |
| `kudos/compose/ui/WriteKudoViewModel.kt`                                                          | Hilt VM; orchestrates repo + validators + `submitJob`.               |
| `kudos/compose/ui/WriteKudoTestTags.kt`                                                           | Test tag constants for instrumented tests.                           |
| `kudos/compose/ui/components/HeaderText.kt`                                                       | A — static instructional header.                                     |
| `kudos/compose/ui/components/RecipientPickerField.kt`                                             | B.1 + B.2 — label + dropdown trigger + overlay state plumbing.       |
| `kudos/compose/ui/components/RecipientPickerOverlay.kt`                                           | Sub-flow `5MU728Tjck` — search field + result list.                  |
| `kudos/compose/ui/components/TitleField.kt`                                                       | B.3 + B.4 — label + input.                                           |
| `kudos/compose/ui/components/CommunityStandardsLink.kt`                                           | B.5 — text-link button.                                              |
| `kudos/compose/ui/components/MessageEditor.kt`                                                    | C + D + D.1 — formatting toolbar + rich-text textarea + hint + counter. |
| `kudos/compose/ui/components/FormattingToolbar.kt`                                                | C.1–C.6 toolbar widget (delegates to `MessageMarkdown` transforms).   |
| `kudos/compose/ui/components/MentionSuggestionOverlay.kt`                                         | @mention dropdown (anchored to caret in D).                          |
| `kudos/compose/ui/components/LinkInsertDialog.kt`                                                 | C.5 — URL-input dialog.                                              |
| `kudos/compose/ui/components/HashtagSection.kt`                                                   | E + E.1 + E.2 — label + tag chips + add button.                      |
| `kudos/compose/ui/components/HashtagPickerOverlay.kt`                                             | Sub-flow `aKWA2klsnt` — hashtag catalog list with selection.         |
| `kudos/compose/ui/components/ImageSection.kt`                                                     | F + F.1 + F.2…F.2b + F.5 — thumbnails + Add Image button.            |
| `kudos/compose/ui/components/AnonymousToggle.kt`                                                  | G — labeled checkbox row.                                            |
| `kudos/compose/ui/components/BottomActionBar.kt`                                                  | Sticky H + I container.                                              |
| `kudos/compose/ui/components/UnsavedChangesDialog.kt`                                             | US2 Sc2 confirmation dialog.                                         |
| `kudos/compose/ui/components/SelectionDropdown.kt`                                                | Generic dropdown chrome reused by B.2 picker, E.2 picker, mentions.  |
| `kudos/compose/domain/WriteKudoDraft.kt`                                                          | Immutable form payload.                                              |
| `kudos/compose/domain/WriteKudoFieldErrors.kt`                                                    | Sealed errors, one slot per field.                                   |
| `kudos/compose/domain/UploadedImage.kt`                                                           | `{clientId, storagePath, sizeBytes, mime}`.                          |
| `kudos/compose/domain/MentionSuggestion.kt`                                                       | `{userId, fullName, deptCode}`.                                      |
| `kudos/compose/domain/WriteKudoValidators.kt`                                                     | Pure validators (title 1–100, message 1–1000 trimmed, hashtags 1–5). |
| `kudos/compose/domain/RichTextValue.kt`                                                           | Rich-text model (delegates to `core/richtext/MessageMarkdown`).      |
| `core/richtext/MessageMarkdown.kt`                                                                | Encode / decode Markdown subset; per-toolbar transforms.             |
| `core/richtext/UrlValidator.kt`                                                                   | RFC 3986 sanity check for C.5.                                       |
| `kudos/compose/data/WriteKudoErrorMapper.kt`                                                      | Maps Supabase exceptions → `WriteKudoFieldErrors`.                   |
| **Tests**                                                                                         |                                                                      |
| `kudos/compose/ui/WriteKudoViewModelTest.kt`                                                      | State-machine unit tests (one nested class per US).                  |
| `kudos/compose/domain/WriteKudoValidatorsTest.kt`                                                 | Validator boundary tests.                                            |
| `core/richtext/MessageMarkdownTest.kt`                                                            | Round-trip encode/decode + per-action transforms.                    |
| `core/richtext/UrlValidatorTest.kt`                                                               | RFC sanity tests.                                                    |
| `kudos/compose/data/WriteKudoErrorMapperTest.kt`                                                  | Server error → field-error mapping.                                  |
| `androidTest/.../kudos/compose/WriteKudoScreenComposeTest.kt`                                     | Compose UI tests — one nested class per US (US1, US2, US3, US4, US5, US6, US7). |
| `androidTest/.../kudos/compose/WriteKudoRlsPolicyTest.kt`                                         | Live RLS test — self-send rejection + tag whitelist.                 |

#### Modified files

| File                                                                                  | Changes                                                                                                                                                                |
| ------------------------------------------------------------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `navigation/Routes.kt`                                                                | Replace `const val WRITE_KUDO` with `const val WRITE_KUDO_PATTERN = "route_write_kudo?recipientUserId={recipientUserId}"` and add `fun writeKudo(recipientUserId: String? = null): String`. Update both existing callers (hub Send pill at `AppNavigation.kt:139` and Home FAB at `AppNavigation.kt:104`) to call the helper instead of the bare constant. |
| `navigation/AppNavigation.kt`                                                         | (a) Replace `composable(Routes.WRITE_KUDO) { PlaceholderScreen(...) }` at line 147 with `composable(Routes.WRITE_KUDO_PATTERN, arguments = listOf(navArgument("recipientUserId") { type = NavType.StringType; nullable = true; defaultValue = null })) { WriteKudoScreen(...) }`. (b) Update callers at lines 104 + 139 to `navController.navigate(Routes.writeKudo())`. |
| `kudos/data/KudosRepository.kt`                                                       | Add 3 new methods: `createKudo` (id supplied by client), `uploadKudoImage`, `deleteKudoImage` (see Repository surface above).                                          |
| `kudos/data/SupabaseKudosRepository.kt`                                               | Implement the 3 new methods (Postgrest INSERT with client-supplied `id` + Storage POST/DELETE). Inject `Storage` client alongside the existing `Postgrest` client.      |
| `kudos/data/DemoKudosRepository.kt`                                                   | Implement the 3 new methods as in-memory fakes for instrumented tests + DEMO build flavour. The fake uploader supports an injectable "fail on Nth call" hook for the partial-failure rollback test. |
| `kudos/data/KudosRepositoryModule.kt`                                                 | Unchanged binding — same interface, more methods. Verify Storage client is bound at module level (likely already provided by `core/supabase/`).                         |
| `kudos/ui/KudosScreen.kt`                                                             | Add `LaunchedEffect(currentBackStackEntry?.savedStateHandle)` block that observes the `kudoSubmitted` flag and calls `viewModel.refresh()` + resets the flag. **First cross-screen `savedStateHandle` use in the codebase** — see "Cross-screen submit signal" note below. |
| `app/src/main/res/values/strings.xml` + `values-en/strings.xml`                       | Add ~40 strings (labels, errors, a11y, placeholders) — listed below.                                                                                                    |
| `app/src/main/res/drawable-nodpi/` (deferred to implement-ui)                         | Pull C.1–C.6 toolbar icons, F.5 add-image icon, B.2 chevron icon, H/I action-bar icons from Figma at task-execution time via `mcp__momorph__get_media_files`.           |
| `gradle/libs.versions.toml`                                                           | Add `supabase-storage = { group = "io.github.jan-tennert.supabase", name = "storage-kt" }` (no version — inherits from `supabase-bom`).                                |
| `app/build.gradle.kts`                                                                | Add `implementation(libs.supabase.storage)` to the `dependencies { ... }` block.                                                                                       |
| **Migrations** (`supabase/migrations/`)                                               |                                                                                                                                                                        |
| `supabase/migrations/20260513_kudos_compose_columns.sql`                              | (Conditional) `ALTER TABLE kudos ADD COLUMN IF NOT EXISTS title TEXT NOT NULL CHECK (...)`, `tags TEXT[] ...`, `image_ids TEXT[] ...`, `is_anonymous BOOLEAN ...`. Verify against existing `kudos` schema before authoring; this migration is a no-op if the hub already created the columns. |
| `supabase/migrations/20260513_kudos_insert_rls.sql`                                   | RLS policy on `kudos` INSERT: enforce `sender_id = auth.uid()` (DEFAULT), reject `recipient_id = auth.uid()`, validate `tags <@ (SELECT array_agg(id) FROM hashtags)`. |
| `supabase/migrations/20260513_kudos_attachments_storage.sql`                          | Create `kudos-attachments` storage bucket with **flat structure** `{user_id}/{kudo_id}/{index}_{filename}` (per Q-W-2, no `_drafts/` subfolder). Bucket policies: INSERT allowed when `(storage.foldername(name))[1] = auth.uid()::text`; DELETE same; SELECT allowed for any authenticated user (Kudo feed surfaces these images publicly within the org). Allowed mime types `image/jpeg`, `image/png`, `image/webp`; size limit 10 MB. |

#### New string resources

```xml
<!-- VN (values/) -->
<string name="write_kudo_header">Gửi lời cám ơn và ghi nhận đến đồng đội</string>
<string name="write_kudo_recipient_label">Người nhận</string>
<string name="write_kudo_recipient_placeholder">Tìm kiếm</string>
<string name="write_kudo_title_label">Danh hiệu</string>
<string name="write_kudo_title_placeholder">Danh tặng một danh hiệu cho…</string>
<string name="write_kudo_community_standards_link">Tiêu chuẩn cộng đồng</string>
<string name="write_kudo_message_placeholder">Hãy gửi gắm lời cám ơn và ghi nhận đến đồng đội tại đây nhé!</string>
<string name="write_kudo_message_hint">Bạn có thể "@ + tên" để nhắc tới đồng nghiệp khác</string>
<string name="write_kudo_character_counter">%1$d/%2$d</string>
<string name="write_kudo_hashtag_label">Hashtag</string>
<string name="write_kudo_hashtag_add">+ Hashtag</string>
<string name="write_kudo_hashtag_limit_note">Tối đa 5</string>
<string name="write_kudo_image_label">Image</string>
<string name="write_kudo_image_add">+ Image</string>
<string name="write_kudo_image_limit_note">Tối đa 5</string>
<string name="write_kudo_anonymous_label">Gửi lời cám ơn và ghi nhận ẩn danh</string>
<string name="write_kudo_cancel">Hủy</string>
<string name="write_kudo_send">Gửi đi</string>
<string name="write_kudo_required_marker">*</string>
<string name="write_kudo_confirm_discard_title">Bạn có chắc muốn hủy không?</string>
<string name="write_kudo_confirm_discard_body">Dữ liệu đã nhập sẽ bị mất.</string>
<string name="write_kudo_confirm_discard_confirm">Hủy</string>
<string name="write_kudo_confirm_discard_dismiss">Tiếp tục soạn</string>
<string name="write_kudo_link_dialog_title">Chèn liên kết</string>
<string name="write_kudo_link_dialog_url">URL</string>
<string name="write_kudo_link_dialog_invalid">URL không hợp lệ</string>

<!-- Field errors -->
<string name="write_kudo_error_recipient_required">Please select a recipient.</string>
<string name="write_kudo_error_recipient_self">You cannot send a kudo to yourself.</string>
<string name="write_kudo_error_title_required">Please enter a title for this recognition.</string>
<string name="write_kudo_error_title_too_long">Title is too long (max 100 characters).</string>
<string name="write_kudo_error_message_required">Please write your recognition message.</string>
<string name="write_kudo_error_message_blank">Message cannot be empty.</string>
<string name="write_kudo_error_message_too_long">Character limit reached. Please shorten your message.</string>
<string name="write_kudo_error_hashtags_required">Please add at least one hashtag.</string>
<string name="write_kudo_error_hashtags_max">Maximum 5 hashtags allowed.</string>
<string name="write_kudo_error_image_type">Unsupported file type. JPG, PNG, or WEBP only.</string>
<string name="write_kudo_error_image_size">File too large (max 10 MB).</string>
<string name="write_kudo_error_image_upload">Couldn\'t upload image. Please try again.</string>
<string name="write_kudo_error_submit_generic">Couldn\'t send Kudo. Please try again.</string>

<!-- A11y -->
<string name="a11y_write_kudo_screen">Sun Kudos compose screen</string>
<string name="a11y_write_kudo_required_field">required</string>
<string name="a11y_write_kudo_remove_image">Remove image %1$d</string>
<string name="a11y_write_kudo_remove_hashtag">Remove hashtag %1$s</string>
<string name="a11y_write_kudo_anonymous_checked">Send anonymously, on</string>
<string name="a11y_write_kudo_anonymous_unchecked">Send anonymously, off</string>
<string name="a11y_write_kudo_send_disabled_hint">Send disabled — fill required fields</string>
<string name="a11y_write_kudo_send_enabled">Send Kudo, button</string>
<string name="a11y_write_kudo_send_sending">Sending Kudo</string>
<string name="a11y_write_kudo_cancel">Cancel</string>
<string name="a11y_write_kudo_character_counter">%1$d of %2$d characters</string>
<string name="a11y_write_kudo_mention_overlay">Mention suggestions, %1$d results</string>
```

EN translations mirror the same keys; brand-fixed names
(`KUDOS`, `SAA`) inherit from `values/` per the existing
convention.

### Dependencies

| Package                                       | Version           | Purpose                                                                                                    | New?       |
| --------------------------------------------- | ----------------- | ---------------------------------------------------------------------------------------------------------- | ---------- |
| `androidx.compose.material3`                  | (BOM)             | `TextField`, `AlertDialog`, `DropdownMenu`, `FilterChip`, `Checkbox`, `Button`, `TextButton`, `SnackbarHost` | Existing   |
| `androidx.compose.foundation`                 | (BOM)             | `BasicTextField`, `LazyColumn`, `Row`, `Column`, etc.                                                       | Existing   |
| `androidx.activity:activity-compose`          | (existing)        | `BackHandler`, `PickVisualMedia` launcher                                                                  | Existing   |
| `androidx.navigation.compose`                 | (existing)        | `composable` with nav-args                                                                                 | Existing   |
| `io.github.jan-tennert.supabase:storage-kt`   | 3.6.0 (Supabase BOM) | Storage upload/delete for F.5 — **first feature to need this artifact**                                | **NEW**    |
| `io.coil-kt:coil-compose`                     | 2.7               | Thumbnail rendering                                                                                        | Existing   |
| `com.google.dagger:hilt-android`              | (existing)        | VM injection                                                                                               | Existing   |

**One new top-level dep**: `supabase-storage-kt` aligned with the
existing `supabase-bom = "3.6.0"`. Pinned via `libs.versions.toml`
following the project's BOM-managed convention. Aligned with
Constitution II ("Build/dependencies: All versions managed via
`gradle/libs.versions.toml`"). Add in Phase 0 as a single-line
addition to `libs.versions.toml` + `app/build.gradle.kts`.

---

## Implementation Strategy

### Phase Breakdown (vertical slices, P1 first)

1. **Phase 0 — Setup & failing tests**
   - 0.1 Add `supabase-storage-kt` artifact to
     `libs.versions.toml` + `app/build.gradle.kts`. Verify
     `core/supabase/` provides a `Storage` client instance (add
     if missing).
   - 0.2 Replace `Routes.WRITE_KUDO` with `WRITE_KUDO_PATTERN` +
     `writeKudo(recipientUserId: String? = null)` helper. Update
     the two existing callers (Home FAB line 104, hub Send pill
     line 139) to use the helper.
   - 0.3 Scaffold `kudos/compose/{ui,data,domain}` package layout
     + `core/richtext/` with empty Kotlin files matching the
     table above.
   - 0.4 Write **failing** unit tests for `WriteKudoValidators` +
     `MessageMarkdown` round-trip + `UrlValidator` (Constitution V).
   - 0.5 Write **failing** Compose UI tests for US1 (happy path)
     AND US3 (tap-reveals-errors on the disabled Send button)
     against an empty `WriteKudoScreenContent` stub. The latter
     guards the novel composable pattern documented in Notes.
   - 0.6 Author the 3 SQL migrations
     (`20260513_kudos_compose_columns.sql`,
     `20260513_kudos_insert_rls.sql`,
     `20260513_kudos_attachments_storage.sql`). Verify the
     `kudos` schema before authoring the columns migration —
     skip columns the hub migration already shipped.
   - 0.7 Write **failing** `WriteKudoRlsPolicyTest` covering:
     self-send rejection, tag-whitelist rejection, sender_id
     auto-derivation, and Storage bucket-policy enforcement (a
     second user's token cannot INSERT into another user's
     `kudos-attachments/{otherUser}/{anyKudoId}/...` path, and
     cannot DELETE another user's objects).

2. **Phase 1 — US1: Compose & submit a Kudo** (P1, MVP)
   - 1.1 Domain models — `WriteKudoDraft`, `UploadedImage`,
     `WriteKudoFieldErrors`, `RichTextValue` (plain-text only at
     this stage; rich-text in Phase 4).
   - 1.2 Validators (`WriteKudoValidators`).
   - 1.3 Repository extensions — `createKudo` impl (Demo + Supabase).
   - 1.4 `WriteKudoViewModel` core — recipient/title/message/tags
     form state, derived `isSubmitEnabled`, submit happy path,
     401 handing-off, `recipientUserId` nav-arg prefill (no
     `formDirty` flip).
   - 1.5 Sub-composables — `HeaderText`, `RecipientPickerField`,
     `RecipientPickerOverlay` (includes empty / loading / error /
     "no results" states — covers spec edge cases "No recipient
     list available" and "Recipient search no results"),
     `TitleField`, `MessageEditor` (plain-text only),
     `HashtagSection`, `HashtagPickerOverlay` (includes
     "no hashtag suggestions" + "close without selecting" empty
     states), `BottomActionBar`, `AnonymousToggle` (already on
     by Phase 1 because it shares state shape — keep checkbox
     grey-styled).
   - 1.6 Replace the placeholder at `AppNavigation.kt:147` with
     `WriteKudoScreen(...)` Hilt entry; add the cross-screen
     `savedStateHandle` observer in `kudos/ui/KudosScreen.kt`
     per the Notes pattern.
   - 1.7 Strings — first ~25 (form-level + edge-case copy);
     defer rich-text + image-error keys to later phases.
   - 1.8 Tests turn green: US1 Compose UI test, US1 Scenario 4
     prefill test, ViewModel state machine, RLS policy. Each
     overlay component gets its own Compose UI test for
     loading/empty/error/loaded states (no shortcut — the
     spec's edge cases all live in these overlays).
   - **MVP gate**: at this point a user can pick a recipient,
     type a title, type plain-text message, pick a hashtag, hit
     Send, see the new Kudo at the top of the hub.

3. **Phase 2 — US2: Cancel with unsaved-content protection** (P1)
   - 2.1 `formDirty` tracking in the VM.
   - 2.2 `UnsavedChangesDialog` composable.
   - 2.3 `BackHandler` wired when `formDirty = true`.
   - 2.4 Cancel-mid-submit path (FR-012): track `submitJob`,
     cancel via `viewModelScope` cancellation.
   - 2.5 Compose UI tests: US2 Sc1 (clean cancel), Sc2 (dirty
     cancel), TC_FUN_051/052/053/054 (system-back parity),
     TC_FUN_060 (cancel during submit).

4. **Phase 3 — US3: Reveal errors on disabled-Send tap** (P1)
   - 3.1 `revealErrors()` in the VM — computes
     `WriteKudoFieldErrors` from current state.
   - 3.2 Per-field error rendering under each component (using
     existing M3 `supportingText` slot on `TextField`, custom
     inline `Text` below dropdown / hashtag section).
   - 3.3 Edit-clears-error wiring per field.
   - 3.4 Server error mapping for self-send rejection
     (`WriteKudoErrorMapper`).
   - 3.5 Compose UI tests: US3 Sc1–Sc8 + `0le8xKnFE_`-style "all
     errors visible at once" assertion.

5. **Phase 4 — US4: Rich-text + @mentions** (P2)
   - 4.1 `MessageMarkdown` module + transforms per toolbar action.
   - 4.2 `FormattingToolbar` composable (C.1–C.6) — toggle
     active-state per selection.
   - 4.3 `LinkInsertDialog` + `UrlValidator`.
   - 4.4 `MentionSuggestionOverlay` anchored to D's caret;
     debounced `searchSunner` query.
   - 4.5 Hub `KudosFeedCard` upgrade to render Markdown (deferred
     here if it expands diff — fallback: feed renders plain-text
     view of rich-text until separate ticket).
   - 4.6 Compose UI tests: US4 Sc1–Sc4, TC_FUN_018/019/020/021/022/023/025/055/056.

6. **Phase 5 — US5: Image attachments** (P2, Q-W-2 model — no drafts)
   - 5.1 `ImageSection` + `Add Image` button + per-image
     thumbnail with "x". Thumbnails render from the local `Uri`
     via Coil; no Storage round-trip at pick time.
   - 5.2 `PickVisualMedia` launcher integration; client-side
     mime + size validation (FR-008) BEFORE the file enters the
     local list — rejects do NOT add a thumbnail.
   - 5.3 `uploadKudoImage` + `deleteKudoImage` repo methods
     (Supabase Storage + Demo fakes). Demo fake returns a fake
     path instantly; tests can simulate failure by failing the
     N-th call.
   - 5.4 Extend `WriteKudoViewModel.submit()` to the
     Q-W-2 flow:
     (a) Generate `kudoId = UUID.randomUUID().toString()`.
     (b) Sequentially upload each picked image; on failure,
         rollback the successful uploads via `deleteKudoImage`,
         surface inline error in F, clear `isSending`.
     (c) Only after all uploads succeed, call `createKudo(draft
         with image_ids = uploaded paths)`.
     (d) On `createKudo` failure, rollback uploads then surface
         error per the I-component error matrix.
   - 5.5 Removing a thumbnail before submit = local-list
     mutation only. No Storage call (Q-W-2 satisfies FR-010
     trivially).
   - 5.6 Compose UI tests: US5 Sc1–Sc3, TC_FUN_028/029/035/049/050;
     PLUS a new test for "image upload fails partway through
     submit → rollback delete called for already-uploaded images,
     form remains editable" (the partial-failure rollback path).

7. **Phase 6 — US6: Anonymous toggle (semantic)** (P2)
   - 6.1 Already wired in Phase 1; this phase only validates the
     server persists `is_anonymous` correctly and the hub renders
     the anonymous label.
   - 6.2 Compose UI tests: US6 Sc1–Sc2, TC_FUN_030/044/045.

8. **Phase 7 — US7: Community Standards link** (P3)
   - 7.1 `CommunityStandardsLink` composable.
   - 7.2 Route wiring — landing screen is **out of scope** for
     this slice. Tap → `PlaceholderScreen("Community Standards")`
     until the standards screen ships separately.
   - 7.3 Compose UI tests: US7 Sc1, TC_FUN_017/046.

9. **Phase 8 — Polish & accessibility**
   - 8.1 A11y audit (TalkBack walkthrough on a device).
   - 8.2 Live-region announcements for character counter +
     validation errors.
   - 8.3 `momorph.implement-ui` pass to align visuals against
     Figma frames `7fFAb-K35a`, `0le8xKnFE_`, `5MU728Tjck`,
     `aKWA2klsnt`.
   - 8.4 Dark theme + dynamic color sanity check.
   - 8.5 Font scaling test (200%).
   - 8.6 Image / icon assets export from Figma via
     `mcp__momorph__get_media_files` to `drawable-nodpi/`.

### Risk Assessment

| Risk                                                                                       | Probability | Impact | Mitigation                                                                                                                                                  |
| ------------------------------------------------------------------------------------------ | ----------- | ------ | ----------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Rich-text encoding mismatch with hub's `KudosFeedCard` renderer                            | Med         | High   | Q-W-1 resolved up-front; integration test in Phase 4 renders submitted message via the hub's card and asserts visual parity (snapshot or text-content match). |
| Storage RLS misconfiguration leaks attachments across users                                | Low         | High   | RLS policy test (`WriteKudoRlsPolicyTest`) committed in Phase 0 — fails until policy is correct. Pen-test the bucket with a second user's token before merge. |
| Partial-failure rollback misses an uploaded image (orphan in Storage)                      | Med         | Med    | Submit's rollback step deletes uploads in a `try { ... } finally { cleanup }` block. Compose UI test "Image upload fails partway → rollback delete called for already-uploaded images" + a manual probe of the bucket after a forced failure. |
| Cancel-mid-submit double-submits if a user re-taps Send before `isSending` flips           | Low         | Med    | Single `submitJob` slot in the VM; cancel sets it null. Compose UI test TC_FUN_060 covers.                                                                  |
| @mention debounce starvation if user types fast (request storm)                            | Med         | Low    | 200 ms debounce + `distinctUntilChanged` + cancel-previous via `flatMapLatest`. Tested at the VM layer.                                                       |
| Hub doesn't refresh after submit (back-stack signal missed)                                | Low         | Med    | `savedStateHandle` flag pattern is well-tested in Award Detail; instrumented test asserts hub `KudosFeedCard` count increases by 1 after submit.              |
| Storage bucket policy / size limit blocks a valid upload                                   | Med         | Low    | Client-side validation (FR-008) is the first gate; server-side limits are a defence-in-depth. Inline error guides the user (FR-011).                          |
| Hub's existing `KudosRepository` interface bloat                                             | Low         | Low    | If the 3 new methods feel out-of-place during review, extract to a `WriteKudoRepository` interface that wraps `KudosRepository.searchSunner + listHashtags`. |

### Estimated Complexity

- **Frontend**: **High** — ~12 sub-composables, custom rich-text
  editor, two overlay sub-flows, validation reveal pattern.
- **Backend**: **Medium** — three new repository methods, one
  Storage bucket + RLS migration, modest SQL.
- **Testing**: **High** — 8 user stories × ~3 scenarios each + 65
  TC_VIETKUDO test cases mapped + RLS live tests.

---

## Integration Testing Strategy

### Test scope

- [x] **Component / module interactions**: `WriteKudoViewModel` ↔
      `KudosRepository` ↔ Supabase / Demo fakes.
- [x] **External dependencies**: Supabase Postgrest INSERT
      (kudos), Supabase Storage POST/DELETE (attachments),
      Supabase Auth (sender id derivation).
- [x] **Data layer**: `kudos` row created with correct shape; RLS
      enforced; tags whitelist enforced; orphaned-attachment
      cleanup on remove.
- [x] **User workflows**: Compose → submit → hub refresh →
      new card visible at top of All-Kudos.

### Test categories

| Category               | Applicable? | Key scenarios                                                                                                          |
| ---------------------- | ----------- | ---------------------------------------------------------------------------------------------------------------------- |
| UI ↔ Logic             | Yes         | All US1–US7 Compose UI test classes against `WriteKudoScreenContent`.                                                  |
| Service ↔ Service      | Yes         | `WriteKudoViewModel` ↔ `KudosRepository` ↔ `WriteKudoErrorMapper`.                                                     |
| App ↔ External API     | Yes (live)  | `WriteKudoRlsPolicyTest` — real Supabase, real RLS, self-send rejection + tag-whitelist.                              |
| App ↔ Data Layer       | Yes         | `kudos` row shape post-submit (id + sender_id + created_at populated server-side).                                    |
| Cross-platform         | N/A         | Android-only per `memory/project_platform_target.md`.                                                                  |

### Test environment

- **Local Compose tests**: Robolectric + Hilt test rule + `DemoKudosRepository`.
- **Instrumented (`androidTest/`)**: emulator (Pixel_10_Pro AVD)
  + Hilt test rule binding `DemoKudosRepository` by default; a
  dedicated `@HiltAndroidTest` class binds `SupabaseKudosRepository`
  for the RLS policy test.
- **Test data**: Demo repository seeded with 20 colleagues + 15
  hashtags; instrumented tests rely on this fixture.

### Mocking strategy

| Dependency type                     | Strategy | Rationale                                                                                                                    |
| ----------------------------------- | -------- | ---------------------------------------------------------------------------------------------------------------------------- |
| `KudosRepository`                   | Fake (DemoKudosRepository) | Constitution V — TDD favours fast deterministic tests; the SupabaseKudosRepository has its own live RLS test. |
| Supabase client                     | Real (for RLS test only) | Live RLS verification needed for self-send + tags whitelist.                                                                   |
| `core/locale/LanguagePreferenceRepository` | Real / DI override | Locale-sensitive strings asserted in EN + VN.                                                                                  |
| File picker (`PickVisualMedia`)     | Test contract that returns canned `Uri`s | UI-test only.                                                                                                                  |
| Image upload                        | DemoKudosRepository returns fake `UploadedImage` instantly | Avoid real Storage round-trip in UI tests.                                                                                     |

### Test scenarios outline

1. **Happy path**
   - [ ] Open composer from hub Send pill → submit → hub refreshes → new card at top.
   - [ ] Open composer from Home FAB → submit → return to Home (or hub on `popBackStack` policy).
   - [ ] Open composer pre-filled (future Search Sunner) → submit → hub refreshes.

2. **Error handling**
   - [ ] Tap disabled Send → all empty fields show errors at once.
   - [ ] Self-send blocked at picker (cannot see self in list) AND at submit (server rejection if list-filter bypassed).
   - [ ] 5xx on submit → snackbar + form preserved.
   - [ ] 401 on submit → global redirect.
   - [ ] Image upload mid-submit failure → rollback deletes the already-uploaded objects for this kudoId, inline image-section error shows, `isSending` clears, form remains editable, other fields intact (Q-W-2 partial-failure path).
   - [ ] `createKudo` failure after all uploads succeed → rollback deletes every uploaded image, inline error shows, form preserved.

3. **Edge cases**
   - [ ] Boundary inputs (title=1/100, message=1/1000, hashtags=1/5, images=0/5).
   - [ ] Cancel during isSending → submit cancelled, form editable.
   - [ ] System back during isSending → same as Cancel.
   - [ ] @ with no matches → empty overlay + literal "@" preserved.
   - [ ] Hashtag picker dismissed without selection → list unchanged.
   - [ ] Rotation mid-compose → all state preserved via `SavedStateHandle`.

### Tooling & framework

- **Test framework**: JUnit4 + Compose UI Test + Robolectric (unit) + Espresso (instrumented) + Hilt testing rule + mockk.
- **Supporting tools**: `androidx.compose.ui.test.junit4.createComposeRule`, `Hilt` test fixtures, `kotlinx-coroutines-test` (`StandardTestDispatcher`).
- **CI**: existing Gradle workflow — `:app:testDebugUnitTest` + `:app:connectedAndroidTest` against the configured emulator.

### Coverage goals

| Area                                | Target            | Priority |
| ----------------------------------- | ----------------- | -------- |
| `WriteKudoViewModel` state machine  | 90%+              | High     |
| `WriteKudoValidators`               | 100%              | High     |
| `MessageMarkdown` round-trip        | 100%              | High     |
| Compose UI tests per US             | 100% scenarios    | High     |
| RLS policy live test — `kudos` insert | 100% scenarios   | High     |
| RLS policy live test — Storage bucket | 100% scenarios   | High     |
| `WriteKudoErrorMapper`              | 90%+              | Med      |
| `core/richtext/UrlValidator`        | 100%              | Med      |
| Overlay sub-components (recipient, hashtag, mention) state coverage | 100% sealed-state branches | Med |

---

## Dependencies & Prerequisites

### Required before start

- [x] `constitution.md` reviewed.
- [x] `spec.md` approved (reviewed twice 2026-05-13).
- [x] `SCREENFLOW.md` updated to include `7fFAb-K35a` + its 3
      sub-flow rows.
- [ ] Open Question Q-W-1 (rich-text encoding) resolved by user.
- [ ] Supabase Storage bucket `kudos-attachments` provisioned +
      RLS policy authored (Phase 0 migration task).
- [ ] Verify `kudos` table has `title`, `tags`, `image_ids`,
      `is_anonymous` columns — add migration if missing (Phase 0).

### External dependencies

- Supabase Storage — first feature in the app to use it. Verify
  `storage-kt` is on the classpath.
- Future Search Sunner spec (`3jgwke3E8O`) — composer must accept
  a `recipientUserId` prefill arg now; Search Sunner spec wires
  it later.

---

## Open Questions

- [x] **Q-W-1 — `kudos.message` rich-text encoding (RESOLVED — Markdown subset)**:
      Store the message as a Markdown subset (`**bold**`, `*italic*`,
      `~~strike~~`, `1.`/`2.` numbered lists, `> quote`, `[label](url)`
      links, plain-text `@FullName` mentions). Round-trippable with
      the hub's `KudosFeedCard` renderer. `core/richtext/MessageMarkdown.kt`
      owns the encode/decode + per-toolbar-action transforms.
- [x] **Q-W-2 — Image upload timing (RESOLVED — no drafts, upload on submit)**:
      Picked images are held client-side as `Uri` + validated mime/size
      ONLY; no Storage upload happens at pick time. On Send, the VM
      uploads all images to `kudos-attachments/{user_id}/{kudo_id}/`
      (kudo_id is a client-generated UUID also used as the new `kudos`
      row's primary key), then INSERTs the `kudos` row with the
      resulting `image_ids` array. On any partial failure (some
      uploads succeed, then one fails, OR uploads succeed but the
      kudos INSERT fails), the VM rolls back by deleting all
      successfully-uploaded objects so no orphans accumulate. This
      eliminates the `_drafts/` subfolder concept and the 24-hour
      cron job. **Storage bucket structure is therefore flat**:
      `kudos-attachments/{user_id}/{kudo_id}/{filename}`.
- [x] **Q-W-3 — Supabase Storage SDK availability (RESOLVED)**:
      Verified — `storage-kt` is NOT yet on the classpath
      (`libs.versions.toml` only declares `auth-kt` and
      `postgrest-kt`). Add `supabase-storage-kt` artifact (inherits
      version from existing `supabase-bom = "3.6.0"`) in Phase 0.
      No constitution amendment — same vendor, same auth pipeline.
- [ ] **Q-W-4 — Community Standards target screen**: tap on B.5
      currently routes to a placeholder. Should this be a
      `Routes.COMMUNITY_STANDARDS` real screen, an in-app
      webview, or out of scope for this slice? Recommendation:
      out of scope — placeholder until a dedicated spec ships.
- [x] **Q-W-5 — Recipient list size (RESOLVED — typed-query pattern)**:
      Recipient picker opens with the first 20 colleagues
      (alphabetically by `display_name`) and surfaces a "Type to
      search" hint at the bottom. Typing any character triggers
      the existing `KudosRepository.searchSunner(query)` flow
      (200 ms debounce + `flatMapLatest`) and replaces the
      seed list with up to 20 matches. Same pattern as the hub's
      Spotlight live search. Same overlay used for the @mention
      suggestion list (different anchor + reduced max-rows).

---

## Notes

- **Reveal-errors-on-disabled-tap composable pattern**: M3
  `Button(enabled = false)` ignores the inner `onClick`, so the
  composer wraps the Send button in an outer `Box(Modifier
  .pointerInput { detectTapGestures(onTap = { onSendOrReveal() }) })`
  that intercepts the tap regardless of M3's disabled state. The
  inner `Button(enabled = state.isSubmitEnabled)` keeps the M3
  visual treatment. This pattern is novel for this codebase —
  document it in the component's KDoc.
- **Cross-screen submit signal via `savedStateHandle`**: this is the
  FIRST cross-screen use of `savedStateHandle` in this codebase
  (Award Detail / hub `savedStateHandle` use is intra-VM for NAV
  args only). The pattern:
  ```kotlin
  // In WriteKudoScreen on submit success:
  navController.previousBackStackEntry
      ?.savedStateHandle
      ?.set("kudoSubmitted", true)
  navController.popBackStack()

  // In KudosScreen (hub):
  val currentEntry = navController.currentBackStackEntry
  LaunchedEffect(currentEntry) {
      val flow = currentEntry
          ?.savedStateHandle
          ?.getStateFlow("kudoSubmitted", false)
          ?: return@LaunchedEffect
      flow.collect { submitted ->
          if (submitted) {
              kudosViewModel.refresh()
              currentEntry.savedStateHandle["kudoSubmitted"] = false
          }
      }
  }
  ```
  Document in `KudosScreen.kt` KDoc — future cross-screen signals
  follow this template.
- **No new `design-style.md`**: per project convention shared
  with the hub plan, visual chrome is fetched on-demand at
  task-execution time via `query_section`. The Figma frame IDs
  are listed in spec § overview.
- **DEMO build flavour**: the existing `DemoKudosRepository`
  pattern is reused. `createKudo` honours the client-supplied
  `id` and appends to its in-memory list; `uploadKudoImage`
  returns a fake `kudos-attachments/demo/{kudoId}/{index}_...`
  path immediately so emulator-only DEMO builds work offline;
  `deleteKudoImage` is a no-op (in-memory bookkeeping only).
  The demo uploader supports an injectable `failOnNthCall: Int?`
  hook so the partial-failure rollback test (Phase 5.6) can
  simulate a mid-submit failure.
- **Strings localization parity**: every key in `values/` gets a
  parallel key in `values-en/`. Brand-fixed strings (KUDOS, SAA)
  inherit from `values/` only — same convention as Login + Home
  + hub.
- **Memory parity**: per `memory/feedback_commit_per_task.md`,
  commits are granular per task / coherent task group; never
  batch phases. QA gate must be green first.
