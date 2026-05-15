# Tasks: Viết Kudo (Write Kudo composer)

**Frame**: `7fFAb-K35a-iOS-Sun-Kudos-Viet-Kudo`
**Plan**: `plan.md` (reviewed twice 2026-05-13; Q-W-1/2/3/5 resolved)
**Spec**: `spec.md` (reviewed twice 2026-05-13)
**Scope decision**: Full 10-phase task list. MVP slice = Phases 1–3 inclusive (Setup → Foundation → US1). `design-style.md` prerequisite waived per Constitution Principle II — canonical project pattern (visuals fetched via `query_section` at task-execution time).

---

## Task Format

```
- [ ] T### [P?] [Story?] Description | file/path
```

- **[P]**: No dependency on the immediate predecessor — either (a) different file from neighbors, OR (b) inside the same Compose test file with no inter-test dependency. Matches the canonical convention from Sun*Kudos hub `tasks.md`.
- **[Story]**: `[USn]` for tasks inside a user-story phase. NO label on Setup / Foundation / Polish.

---

## Phase mapping (plan → tasks.md)

| Tasks Phase     | Plan Phase | Scope                                                       |
| --------------- | ---------- | ----------------------------------------------------------- |
| Phase 1 (Setup) | plan Phase 0 | Deps, routes, scaffold, migrations, failing tests           |
| Phase 2 (Foundation) | plan Phase 1 (subset) | Domain models, validators, repo extension, error mapper |
| Phase 3 [US1]   | plan Phase 1 | 🎯 MVP — compose plain-text Kudo end-to-end                |
| Phase 4 [US2]   | plan Phase 2 | Cancel + unsaved-changes confirmation                       |
| Phase 5 [US3]   | plan Phase 3 | Reveal-errors-on-disabled-tap                              |
| Phase 6 [US4]   | plan Phase 4 | Rich-text toolbar + @mentions                              |
| Phase 7 [US5]   | plan Phase 5 | Image attachments (Q-W-2 submit-time upload)               |
| Phase 8 [US6]   | plan Phase 6 | Anonymous toggle semantic                                  |
| Phase 9 [US7]   | plan Phase 7 | Community Standards link                                   |
| Phase 10 (Polish) | plan Phase 8 | A11y + visuals + final QA gate                            |

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Add the one new dep (`supabase-storage-kt`), refactor the existing `WRITE_KUDO` route to accept a prefill arg, scaffold the `kudos/compose/` package, ship the 3 migrations, and commit the first round of **failing** tests per Constitution V.

- [ ] T001 Verify the canonical Viết Kudo Figma frames are reachable via `mcp__momorph__list_design_items` for `7fFAb-K35a` (default), `0le8xKnFE_` (validation error state), `5MU728Tjck` (recipient dropdown), `aKWA2klsnt` (hashtag dropdown) — confirm the component map matches spec § Component Behavior. Read-only check; no edit. | (verification — no file)
- [ ] T002 [P] Add `supabase-storage = { group = "io.github.jan-tennert.supabase", name = "storage-kt" }` to the `[libraries]` block in `gradle/libs.versions.toml`. No version — inherits from the existing `supabase-bom = "3.6.0"`. | gradle/libs.versions.toml
- [ ] T003 Wire `implementation(libs.supabase.storage)` into `app/build.gradle.kts` `dependencies { ... }` block, immediately after the existing `implementation(libs.supabase.postgrest)` line. Run `./gradlew :app:compileDebugKotlin` to confirm the artifact resolves. | app/build.gradle.kts
- [ ] T004 [P] Provide a `Storage` client binding in `core/supabase/` Hilt module — `@Provides @Singleton fun provideStorage(client: SupabaseClient): Storage = client.storage`. (Check if `core/supabase/SupabaseModule.kt` exists; create if needed alongside the existing `Auth` + `Postgrest` providers.) | app/src/main/java/com/example/aiddproject/core/supabase/SupabaseModule.kt
- [ ] T005 [P] In `navigation/Routes.kt`, replace the existing `const val WRITE_KUDO: String = "route_write_kudo"` line with `const val WRITE_KUDO_PATTERN: String = "route_write_kudo?recipientUserId={recipientUserId}"` AND add a `fun writeKudo(recipientUserId: String? = null): String = ...` helper. Add KDoc explaining the optional prefill argument for the Search Sunner entry point. | app/src/main/java/com/example/aiddproject/navigation/Routes.kt
- [ ] T006 In `navigation/AppNavigation.kt`, update both existing callers of `Routes.WRITE_KUDO` (line 104 — Home FAB navigation, line 139 — hub Send pill) to call `Routes.writeKudo()` (with no prefill). Run `./gradlew :app:compileDebugKotlin` to confirm. | app/src/main/java/com/example/aiddproject/navigation/AppNavigation.kt
- [ ] T007 [P] Create the new feature sub-package `com.example.aiddproject.kudos.compose/` with empty sub-directories `ui/`, `ui/components/`, `data/`, `domain/`. Empty `package-info.kt` anchors OK. | app/src/main/java/com/example/aiddproject/kudos/compose/ (and subdirs)
- [ ] T008 [P] Create the new `core/richtext/` package with empty `package-info.kt`. This houses `MessageMarkdown.kt` (encode/decode) + `UrlValidator.kt`. | app/src/main/java/com/example/aiddproject/core/richtext/
- [ ] T009 [P] Add the first ~25 form-level + a11y string resources to `app/src/main/res/values/strings.xml` per plan § "New string resources" — labels, placeholders, Cancel/Send copy, hashtag/image limit hints. Defer rich-text + image-error keys to later phases. The `StringResourceParityTest` will fail at unit-test time until T010 ships. | app/src/main/res/values/strings.xml
- [ ] T010 [P] Mirror the new keys into `app/src/main/res/values-en/strings.xml`. Brand-fixed strings inherit from `values/`. | app/src/main/res/values-en/strings.xml
- [ ] T011 [P] Author `supabase/migrations/20260513_kudos_compose_columns.sql` — `ALTER TABLE kudos` with `ADD COLUMN IF NOT EXISTS title TEXT NOT NULL CHECK (char_length(title) BETWEEN 1 AND 100)`, `ADD COLUMN IF NOT EXISTS tags TEXT[] NOT NULL CHECK (array_length(tags, 1) BETWEEN 1 AND 5)`, `ADD COLUMN IF NOT EXISTS image_ids TEXT[] NOT NULL DEFAULT '{}' CHECK (array_length(image_ids, 1) IS NULL OR array_length(image_ids, 1) <= 5)`, `ADD COLUMN IF NOT EXISTS is_anonymous BOOLEAN NOT NULL DEFAULT FALSE`. The `IF NOT EXISTS` clauses make the migration a no-op when the hub migration already shipped these columns. | supabase/migrations/20260513_kudos_compose_columns.sql
- [ ] T012 [P] Author `supabase/migrations/20260513_kudos_insert_rls.sql` — RLS policy on `kudos` INSERT: `sender_id DEFAULT auth.uid()`, `CREATE POLICY "kudos_insert_self_only" ON kudos FOR INSERT WITH CHECK (sender_id = auth.uid() AND recipient_id <> auth.uid() AND tags <@ (SELECT array_agg(id) FROM hashtags))`. | supabase/migrations/20260513_kudos_insert_rls.sql
- [ ] T013 [P] Author `supabase/migrations/20260513_kudos_attachments_storage.sql` — `INSERT INTO storage.buckets (id, name, public) VALUES ('kudos-attachments', 'kudos-attachments', false)`; bucket-level INSERT/DELETE policy `CREATE POLICY "owner_can_write" ON storage.objects FOR INSERT WITH CHECK (bucket_id = 'kudos-attachments' AND (storage.foldername(name))[1] = auth.uid()::text)` (and the mirror DELETE policy); SELECT policy `CREATE POLICY "authenticated_can_read" ON storage.objects FOR SELECT USING (bucket_id = 'kudos-attachments' AND auth.role() = 'authenticated')`; mime-type allow-list `image/jpeg`, `image/png`, `image/webp`; 10 MB size limit. | supabase/migrations/20260513_kudos_attachments_storage.sql
- [ ] T014 [P] Write **failing** unit-test stub `WriteKudoValidatorsTest.kt` in `app/src/test/.../kudos/compose/domain/` — empty `@Test` methods for all required validators (title 1–100, message 1–1000 trimmed, hashtags 1–5, recipient required, no-self-send placeholder). Tests must compile but fail (`fail("not implemented")`). | app/src/test/java/com/example/aiddproject/kudos/compose/domain/WriteKudoValidatorsTest.kt
- [ ] T015 [P] Write **failing** unit-test stub `MessageMarkdownTest.kt` — encode/decode round-trip + per-toolbar-action transforms (bold, italic, strikethrough, numbered list, quote, link). Markdown subset per Q-W-1. | app/src/test/java/com/example/aiddproject/core/richtext/MessageMarkdownTest.kt
- [ ] T016 [P] Write **failing** unit-test stub `UrlValidatorTest.kt` — accept `https://example.com`, reject `not-a-valid-url`, reject `javascript:alert(1)`, accept `http://internal/path?q=1`. | app/src/test/java/com/example/aiddproject/core/richtext/UrlValidatorTest.kt
- [ ] T017 Write **failing** instrumented test stub `WriteKudoScreenComposeTest.kt` in `androidTest/.../kudos/compose/` — top-level class with `@RunWith(AndroidJUnit4::class)` + Hilt + Compose rule. Add ONE failing `@Test fun us1_happyPath_renders_and_submits()` and ONE failing `@Test fun us3_tappingDisabledSend_revealsAllFieldErrors()` (the latter guards the novel composable pattern documented in plan § Notes). | app/src/androidTest/java/com/example/aiddproject/kudos/compose/WriteKudoScreenComposeTest.kt
- [ ] T018 Write **failing** instrumented test stub `WriteKudoRlsPolicyTest.kt` — `@HiltAndroidTest` class binding the real `SupabaseKudosRepository`. Four failing tests: (a) self-send rejection, (b) tag-whitelist rejection, (c) sender_id auto-derivation, (d) Storage bucket cross-user INSERT/DELETE rejection (per the flat-bucket policy from T013). | app/src/androidTest/java/com/example/aiddproject/kudos/compose/WriteKudoRlsPolicyTest.kt

**Checkpoint**: Setup complete. Build green. Targeted unit + instrumented tests committed and FAILING per Constitution V. No production code yet.

---

## Phase 2: Foundation (Blocking Prerequisites)

**Purpose**: Domain models, validators, rich-text encoding skeleton, repository extension, error mapper, Hilt wiring. All Phase 3+ tasks depend on this phase.

**⚠️ CRITICAL**: No user-story work begins until this phase passes.

### Domain models (shared)

- [ ] T019 [P] Create `WriteKudoDraft` data class in `kudos/compose/domain/WriteKudoDraft.kt` — `{id: String, recipientId: String, title: String, message: String (markdown-encoded), tags: List<String>, imageIds: List<String>, isAnonymous: Boolean}`. `id` is the client-generated UUID per Q-W-2. | app/src/main/java/com/example/aiddproject/kudos/compose/domain/WriteKudoDraft.kt
- [ ] T020 [P] Create `WriteKudoFieldErrors` data class — `{recipient: Int? @StringRes, title: Int?, message: Int?, hashtags: Int?, images: Int?}` with a `None` static instance. Equality-friendly so Compose recomposition skips when unrelated fields change. | app/src/main/java/com/example/aiddproject/kudos/compose/domain/WriteKudoFieldErrors.kt
- [ ] T021 [P] Create `UploadedImage` data class — `{clientId: String, storagePath: String, sizeBytes: Long, mime: String, localUri: Uri?}`. `storagePath` is `null` (or sentinel) while the file is only local (Q-W-2 — pre-submit). | app/src/main/java/com/example/aiddproject/kudos/compose/domain/UploadedImage.kt
- [ ] T022 [P] Create `MentionSuggestion` data class — `{userId: String, fullName: String, deptCode: String?, avatarUrl: String?}`. Reused for both the recipient picker and the @mention overlay. | app/src/main/java/com/example/aiddproject/kudos/compose/domain/MentionSuggestion.kt
- [ ] T023 [P] Create `RichTextValue` data class wrapping the markdown string + plain-text projection in `kudos/compose/domain/RichTextValue.kt`. Provides `plainText: String` (the non-formatting characters only, used for length validation). Static `Empty` constant. | app/src/main/java/com/example/aiddproject/kudos/compose/domain/RichTextValue.kt

### Validators (shared)

- [ ] T024 Implement `WriteKudoValidators.kt` — pure functions `validateTitle(String): Int?`, `validateMessage(RichTextValue): Int?`, `validateHashtags(List<String>): Int?`, `validateRecipient(String?, currentUserId: String): Int?`. Each returns a `@StringRes Int?` (null = valid). Run `./gradlew :app:testDebugUnitTest --tests "*WriteKudoValidatorsTest"` — the failing tests from T014 now turn green. | app/src/main/java/com/example/aiddproject/kudos/compose/domain/WriteKudoValidators.kt

### Rich-text core (used by US1 plain-text + US4 rich-text)

- [ ] T025 [P] Implement `core/richtext/MessageMarkdown.kt` — `encode(value: RichTextValue): String` and `decode(markdown: String): RichTextValue`, plus per-toolbar transforms `applyBold(value, selection)`, `applyItalic`, `applyStrikethrough`, `applyNumberedList`, `applyQuote`, `applyLink(value, selection, url)`. Plain-text round-trip MUST be lossless for US1 MVP (subsequent rich-text work is in Phase 6). Failing tests from T015 turn green. | app/src/main/java/com/example/aiddproject/core/richtext/MessageMarkdown.kt
- [ ] T026 [P] Implement `core/richtext/UrlValidator.kt` — `isValid(url: String): Boolean` rejecting `javascript:` / `data:` schemes per OWASP A03, accepting `http://` and `https://`, validating RFC 3986 host shape. Failing tests from T016 turn green. | app/src/main/java/com/example/aiddproject/core/richtext/UrlValidator.kt

### Repository extension (shared)

- [ ] T027 Extend `kudos/data/KudosRepository.kt` with three new methods per plan § Repository surface: `suspend fun createKudo(draft: WriteKudoDraft): Result<Kudos>`, `suspend fun uploadKudoImage(kudoId: String, index: Int, uri: Uri): Result<UploadedImage>`, `suspend fun deleteKudoImage(ref: UploadedImage): Result<Unit>`. KDoc each method with its FR / US reference and Q-W-2 semantics. | app/src/main/java/com/example/aiddproject/kudos/data/KudosRepository.kt
- [ ] T028 Implement `createKudo` in `kudos/data/SupabaseKudosRepository.kt` — Postgrest INSERT into `kudos` with client-supplied `id`; decode response into `Kudos`; map exceptions through the to-be-created `WriteKudoErrorMapper` (T031). Inject `Storage` client into the constructor (provided by T004). | app/src/main/java/com/example/aiddproject/kudos/data/SupabaseKudosRepository.kt
- [ ] T029 [P] Implement `uploadKudoImage` + `deleteKudoImage` in `SupabaseKudosRepository.kt` — Storage `from("kudos-attachments").upload(...)` / `.delete(...)` with the flat bucket path `{user_id}/{kudo_id}/{index}_{filename}`. Honour the mime/size limits per bucket policy (T013). | app/src/main/java/com/example/aiddproject/kudos/data/SupabaseKudosRepository.kt
- [ ] T030 Implement the same 3 methods in `kudos/data/DemoKudosRepository.kt` — `createKudo` honours `draft.id`, appends to in-memory list, returns synthetic `Kudos`; `uploadKudoImage` returns a fake path `kudos-attachments/demo/{kudoId}/{index}_...` immediately; `deleteKudoImage` is a no-op. Expose an injectable `failOnNthUpload: Int? = null` field on the demo class so Phase 7's partial-failure test can simulate a failure. | app/src/main/java/com/example/aiddproject/kudos/data/DemoKudosRepository.kt

### Error mapping (shared)

- [ ] T031 Create `kudos/compose/data/WriteKudoErrorMapper.kt` — maps Supabase exceptions to `WriteKudoFieldErrors` slots. Specifically: `PostgrestException(code = "23514", message contains "recipient_id")` → `recipient = R.string.write_kudo_error_recipient_self`; tag-whitelist violation → `hashtags = R.string.write_kudo_error_hashtags_*`; everything else → null (caller surfaces as snackbar). | app/src/main/java/com/example/aiddproject/kudos/compose/data/WriteKudoErrorMapper.kt
- [ ] T032 [P] Write `WriteKudoErrorMapperTest.kt` unit tests — at least one test per mapped server-error code, plus a "default → null" catch-all. | app/src/test/java/com/example/aiddproject/kudos/compose/data/WriteKudoErrorMapperTest.kt

**Checkpoint**: Foundation complete. Domain layer compiles, validators + markdown + url tests green, repository surface available. Ready for parallel user-story work.

---

## Phase 3: User Story 1 — Compose and submit a Kudo (Priority: P1) 🎯 MVP

**Goal**: User can pick a recipient via overlay, type a title, type plain-text message, pick at least one hashtag, tap Send, see the new Kudo appear at the top of the Sun*Kudos hub.

**Independent Test**: Open Viết Kudo from the hub Send pill → fill recipient + title + plain-text message + 1 hashtag → tap Send → assert (a) the screen pops back to the hub and (b) the hub's All-Kudos list contains the new Kudo at the top.

### ViewModel + UiState (US1)

- [ ] T033 [US1] Create `kudos/compose/ui/WriteKudoUiState.kt` — full data class per plan § "WriteKudoUiState shape (concrete)". Include the derived `isSubmitEnabled: Boolean` getter (not a stored field). Sealed sub-states `RecipientPickerState`, `HashtagPickerState`, `MentionOverlayState`, `LinkDialogState`, `ConfirmDialogState`, `SnackbarMessage`. | app/src/main/java/com/example/aiddproject/kudos/compose/ui/WriteKudoUiState.kt
- [ ] T034 [US1] Create `kudos/compose/ui/WriteKudoTestTags.kt` — `const val` test tags for every interactive surface: HEADER, RECIPIENT_FIELD, RECIPIENT_OVERLAY, TITLE_INPUT, COMMUNITY_STANDARDS_LINK, FORMATTING_TOOLBAR, BOLD/ITALIC/STRIKE/LIST/LINK/QUOTE_BUTTON, MESSAGE_TEXTAREA, MENTION_OVERLAY, HASHTAG_SECTION, HASHTAG_ADD, HASHTAG_CHIP_PREFIX, HASHTAG_OVERLAY, IMAGE_SECTION, IMAGE_ADD, IMAGE_THUMBNAIL_PREFIX, ANONYMOUS_TOGGLE, CANCEL_BUTTON, SEND_BUTTON, CONFIRM_DIALOG, LINK_DIALOG. | app/src/main/java/com/example/aiddproject/kudos/compose/ui/WriteKudoTestTags.kt
- [ ] T035 [US1] Create `kudos/compose/ui/WriteKudoViewModel.kt` (Hilt) — read `recipientUserId` nav arg from `SavedStateHandle`, initialize `WriteKudoUiState` with prefill if present (and `formDirty = false`), expose `state: StateFlow<WriteKudoUiState>`. Track a `private var submitJob: Job? = null` slot. Inject `KudosRepository` + `WriteKudoErrorMapper` + an `AuthUserIdProvider` (use the existing `SessionGate`/auth source). | app/src/main/java/com/example/aiddproject/kudos/compose/ui/WriteKudoViewModel.kt
- [ ] T036 [US1] In `WriteKudoViewModel`, implement field-update intents — `onRecipientChosen(MentionSuggestion)`, `onTitleChange(String)`, `onMessageChange(RichTextValue)`, `onHashtagAdd(String)`, `onHashtagRemove(String)`, `onAnonymousToggle(Boolean)`. Each (a) updates the relevant field, (b) flips `formDirty = true` (unless this is the initial prefill of the recipient), and (c) clears the field's own error in `fieldErrors`. | app/src/main/java/com/example/aiddproject/kudos/compose/ui/WriteKudoViewModel.kt
- [ ] T037 [US1] In `WriteKudoViewModel`, implement `onSendTap()` — happy path only: generate `kudoId = UUID.randomUUID().toString()` → call `createKudo(draft.copy(id = kudoId, imageIds = state.images.mapNotNull { it.storagePath }))` → on success emit `Submitted` one-shot event → on Supabase exception map via `WriteKudoErrorMapper` and surface in `fieldErrors`. Wrap in `submitJob = viewModelScope.launch { ... }` for FR-009/FR-012 single-flight. Phase 7 will replace this with the full image-upload-then-insert flow. | app/src/main/java/com/example/aiddproject/kudos/compose/ui/WriteKudoViewModel.kt
- [ ] T038 [US1] In `WriteKudoViewModel`, implement recipient-picker logic — `onRecipientPickerOpen()` triggers `KudosRepository.searchSunner("")` (seed 20 colleagues per Q-W-5), exposes a debounced `Flow<String>` for the search input via `MutableStateFlow<String>().debounce(200).distinctUntilChanged().flatMapLatest { searchSunner(it) }`. Pre-filter out `auth.uid()` (TR-002 client-side hint). | app/src/main/java/com/example/aiddproject/kudos/compose/ui/WriteKudoViewModel.kt
- [ ] T039 [US1] In `WriteKudoViewModel`, implement hashtag-picker logic — `onHashtagPickerOpen()` triggers `KudosRepository.listHashtags()` once per session and caches the result. Removing a chip is `tags.filter { it != id }`. The "+ Hashtag" button disables when `tags.size >= 5` (also enforce in the picker overlay). | app/src/main/java/com/example/aiddproject/kudos/compose/ui/WriteKudoViewModel.kt
- [ ] T040 [P] [US1] Write `WriteKudoViewModelTest.kt` — `kotlinx-coroutines-test` + `StandardTestDispatcher`. Nested test classes per intent: `OnRecipientChosen` (prefill doesn't dirty, manual choice does), `OnTitleChange` (dirty + clears title error), `OnSendTap_HappyPath` (calls createKudo, emits Submitted), `OnSendTap_RecipientSelfRejection` (server 23514 → recipient error). Use a `FakeKudosRepository` (separate from DEMO — minimal, controllable). | app/src/test/java/com/example/aiddproject/kudos/compose/ui/WriteKudoViewModelTest.kt

### Sub-composables (US1)

- [ ] T041 [P] [US1] Create `HeaderText.kt` composable — static centered `Text(R.string.write_kudo_header)`. <30 LOC. Visual chrome (color/size/spacing) fetched on-demand by the implementer via `mcp__momorph__query_section nodeId="6885:9292"`. | app/src/main/java/com/example/aiddproject/kudos/compose/ui/components/HeaderText.kt
- [ ] T042 [P] [US1] Create `RecipientPickerField.kt` — label "Người nhận *" + dropdown trigger button that opens the picker overlay. Shows `state.recipientName ?: placeholder` + chevron icon. testTag `RECIPIENT_FIELD`. Disabled visual when `state.recipientPicker` is in an unrecoverable error state. | app/src/main/java/com/example/aiddproject/kudos/compose/ui/components/RecipientPickerField.kt
- [ ] T043 [P] [US1] Create `RecipientPickerOverlay.kt` — M3 `DropdownMenu`-anchored overlay with an `OutlinedTextField` search input + `LazyColumn` of results. Renders four sealed-state branches: `Loading` (CircularProgressIndicator), `Empty/NoResults` (localized empty-state text), `Error` (error text + Retry button), `Loaded` (list of `MentionSuggestion` rows — each shows avatar + name + dept code). testTag `RECIPIENT_OVERLAY`. Source the search query from `state.recipientPicker.query` and bubble updates via `onQueryChange`. | app/src/main/java/com/example/aiddproject/kudos/compose/ui/components/RecipientPickerOverlay.kt
- [ ] T044 [P] [US1] Create `TitleField.kt` — label "Danh hiệu *" + M3 `OutlinedTextField` bound to `state.title`. Placeholder = "Danh tặng một danh hiệu cho…". Pass `state.fieldErrors.title` to the `supportingText` slot. testTag `TITLE_INPUT`. | app/src/main/java/com/example/aiddproject/kudos/compose/ui/components/TitleField.kt
- [ ] T045 [P] [US1] Create `CommunityStandardsLink.kt` — `TextButton` rendering "Tiêu chuẩn cộng đồng" (R.string.write_kudo_community_standards_link) using `core/ui/rememberSingleClickHandler`. testTag `COMMUNITY_STANDARDS_LINK`. onClick callback only — Phase 9 wires the placeholder destination. | app/src/main/java/com/example/aiddproject/kudos/compose/ui/components/CommunityStandardsLink.kt
- [ ] T046 [P] [US1] Create `MessageEditor.kt` — Phase 3 MVP variant: `BasicTextField` (or M3 `OutlinedTextField` multi-line) bound to `state.message.plainText`, hint text "Hãy gửi gắm…" below, character counter `state.message.plainText.length`/1000 below the textarea, hint label "Bạn có thể …" below the counter. Phase 6 swaps in the toolbar + @mentions. testTag `MESSAGE_TEXTAREA`. | app/src/main/java/com/example/aiddproject/kudos/compose/ui/components/MessageEditor.kt
- [ ] T047 [P] [US1] Create `HashtagSection.kt` — label "Hashtag *" + `FlowRow` of `FilterChip`s for `state.tags` (each chip has an "x" remove button) + "+ Hashtag (Tối đa 5)" trigger button. Trigger calls `onHashtagPickerOpen`; chips call `onHashtagRemove`. testTag `HASHTAG_SECTION`. Show `state.fieldErrors.hashtags` below if non-null. | app/src/main/java/com/example/aiddproject/kudos/compose/ui/components/HashtagSection.kt
- [ ] T048 [P] [US1] Create `HashtagPickerOverlay.kt` — M3 `DropdownMenu`-anchored overlay with a checklist of hashtags. Four sealed states (loading/empty/error/loaded). Selecting an unchecked tag calls `onHashtagAdd`; selecting a checked one calls `onHashtagRemove`. Dismiss without selection leaves state unchanged. testTag `HASHTAG_OVERLAY`. | app/src/main/java/com/example/aiddproject/kudos/compose/ui/components/HashtagPickerOverlay.kt
- [ ] T049 [P] [US1] Create `AnonymousToggle.kt` — Row with M3 `Checkbox` + label `R.string.write_kudo_anonymous_label`. Whole-row clickable per Material guidance. testTag `ANONYMOUS_TOGGLE`. Bound to `state.isAnonymous`. | app/src/main/java/com/example/aiddproject/kudos/compose/ui/components/AnonymousToggle.kt
- [ ] T050 [P] [US1] Create `BottomActionBar.kt` — fixed-height `Row` containing `TextButton` "Hủy" (H — testTag `CANCEL_BUTTON`) + filled `Button` "Gửi đi" (I — testTag `SEND_BUTTON`). Render the I button with the "tap-reveals-errors-on-disabled" pattern from plan § Notes: outer `Box(Modifier.pointerInput { detectTapGestures(onTap = onSend) })` intercepts the tap; inner `Button(enabled = state.isSubmitEnabled)` keeps the M3 disabled visual. KDoc the pattern explicitly. | app/src/main/java/com/example/aiddproject/kudos/compose/ui/components/BottomActionBar.kt
- [ ] T051 [P] [US1] Create the optional reusable `SelectionDropdown.kt` — generic M3-`DropdownMenu`-based overlay parameterized by item type, used by recipient picker + hashtag picker + (Phase 6) mention overlay. If the abstraction expands the diff too much during T043/T048, leave each picker self-contained and skip this task. | app/src/main/java/com/example/aiddproject/kudos/compose/ui/components/SelectionDropdown.kt

### Stateless screen + Hilt entry (US1)

- [ ] T052 [US1] Create `WriteKudoScreenContent.kt` — stateless composable taking `state: WriteKudoUiState` + the full callback set per plan § Stateless content pattern (`onRecipientPickerOpen`, `onQueryChange`, `onRecipientChosen`, `onTitleChange`, `onMessageChange`, `onHashtagPickerOpen`, `onHashtagAdd`, `onHashtagRemove`, `onAnonymousToggle`, `onCommunityStandards`, `onSend`, `onCancel`). Layout: `Scaffold` with `bottomBar = BottomActionBar` and `content = LazyColumn` (HeaderText → RecipientPickerField → TitleField → CommunityStandardsLink → MessageEditor → HashtagSection → AnonymousToggle). Overlays render conditionally on top. <150 LOC. | app/src/main/java/com/example/aiddproject/kudos/compose/ui/WriteKudoScreenContent.kt
- [ ] T053 [US1] Create `WriteKudoScreen.kt` (Hilt entry) — collects `viewModel.state` and `viewModel.events`, exposes a single `onSubmitted: () -> Unit` callback to the navigation layer (which sets the savedStateHandle flag + pops). Wraps content in `core/session/SessionGate` for the 401 auth gate consistent with the hub. | app/src/main/java/com/example/aiddproject/kudos/compose/ui/WriteKudoScreen.kt
- [ ] T054 [US1] In `navigation/AppNavigation.kt`, replace `composable(Routes.WRITE_KUDO) { PlaceholderScreen(label = "Write a Kudo") }` at line 147 with `composable(Routes.WRITE_KUDO_PATTERN, arguments = listOf(navArgument("recipientUserId") { type = NavType.StringType; nullable = true; defaultValue = null })) { WriteKudoScreen(onSubmitted = { navController.previousBackStackEntry?.savedStateHandle?.set("kudoSubmitted", true); navController.popBackStack() }, onNavigateUp = { navController.popBackStack() }, onNavigateToCommunityStandards = { /* Phase 9 wires */ }) }`. | app/src/main/java/com/example/aiddproject/navigation/AppNavigation.kt
- [ ] T055 [US1] In `kudos/ui/KudosScreen.kt`, add a `LaunchedEffect(currentBackStackEntry?.savedStateHandle)` block that collects `getStateFlow("kudoSubmitted", false)` and calls `kudosViewModel.refresh()` + resets the flag to false per plan § Notes "Cross-screen submit signal". KDoc the pattern for future cross-screen signals. | app/src/main/java/com/example/aiddproject/kudos/ui/KudosScreen.kt

### Tests turn green (US1)

- [ ] T056 [US1] Flesh out `WriteKudoScreenComposeTest.us1_happyPath_renders_and_submits()` (from T017) — `setContent { WriteKudoScreenContent(state = mvpHappyPathState, callbacks = ...) }`, assert all primary fields render via testTags, type into title + message, pick a hashtag, tap Send, assert `onSend` was invoked. Use a `Snapshot` flow + Compose test rule. | app/src/androidTest/java/com/example/aiddproject/kudos/compose/WriteKudoScreenComposeTest.kt
- [ ] T057 [US1] Add `us1_prefill_recipientId_doesNotDirtyForm()` to `WriteKudoScreenComposeTest` — start with prefilled `recipientId`, assert `state.formDirty == false`, assert Cancel does NOT show the dialog (covers spec US1 Scenario 4). | app/src/androidTest/java/com/example/aiddproject/kudos/compose/WriteKudoScreenComposeTest.kt
- [ ] T058 [P] [US1] Add `us1_recipientPicker_empty_loading_error_loaded()` Compose test exercising all four sealed-state branches of `RecipientPickerOverlay` — covers spec edge cases "No recipient list available" + "Recipient search no results". | app/src/androidTest/java/com/example/aiddproject/kudos/compose/WriteKudoScreenComposeTest.kt
- [ ] T059 [P] [US1] Add `us1_hashtagPicker_empty_and_dismiss_without_selection()` Compose test — covers spec edge case "No hashtag suggestions" + "Close picker without selecting". | app/src/androidTest/java/com/example/aiddproject/kudos/compose/WriteKudoScreenComposeTest.kt
- [ ] T060 [US1] Make the `WriteKudoRlsPolicyTest` tests green (from T018) — implement the actual Postgrest calls against the dev Supabase instance with two real test users; assert the 23514 / tag-whitelist / sender_id rejections fire correctly. | app/src/androidTest/java/com/example/aiddproject/kudos/compose/WriteKudoRlsPolicyTest.kt
- [ ] T061 [US1] Run the full unit + instrumented suite (`./gradlew :app:testDebugUnitTest :app:connectedAndroidTest`). All US1 tests green. Commit: `feat(write-kudo): Phase 3 — US1 compose & submit MVP`.

**Checkpoint**: 🎯 **MVP gate** — a user can compose a plain-text Kudo end-to-end, submit it, see it appear at the top of the hub. Phases 4–9 are P1/P2/P3 refinements layered on top.

---

## Phase 4: User Story 2 — Cancel with unsaved-content protection (Priority: P1)

**Goal**: A confirmation dialog appears before discarding a dirty form; clean forms cancel immediately. System back gesture mirrors Cancel. Cancel mid-submit cancels the in-flight request (FR-012).

**Independent Test**: Type into the message, tap Cancel → dialog appears. Confirm → form clears + navigates back. Dismiss → still on composer with content intact.

- [ ] T062 [US2] Create `UnsavedChangesDialog.kt` composable — M3 `AlertDialog` with title `R.string.write_kudo_confirm_discard_title`, body `R.string.write_kudo_confirm_discard_body`, confirm button `R.string.write_kudo_confirm_discard_confirm`, dismiss button `R.string.write_kudo_confirm_discard_dismiss`. testTag `CONFIRM_DIALOG`. | app/src/main/java/com/example/aiddproject/kudos/compose/ui/components/UnsavedChangesDialog.kt
- [ ] T063 [US2] In `WriteKudoViewModel`, implement `onCancelTap()` — if `state.formDirty == false`, emit `NavigateBack` event; if true, emit `state.copy(confirmDialog = ConfirmDialogState.UnsavedChanges)`. Add `onConfirmDiscard()` (emit `NavigateBack`) and `onDismissConfirmDialog()` (clear confirmDialog). | app/src/main/java/com/example/aiddproject/kudos/compose/ui/WriteKudoViewModel.kt
- [ ] T064 [US2] In `WriteKudoViewModel`, add submit-cancel branch to `onCancelTap()` — if `state.isSending == true`, `submitJob?.cancel()`, set `isSending = false`, do NOT navigate (per FR-012). Subsequent Cancel taps follow the normal flow. | app/src/main/java/com/example/aiddproject/kudos/compose/ui/WriteKudoViewModel.kt
- [ ] T065 [US2] In `WriteKudoScreen.kt`, wire `BackHandler(enabled = state.formDirty || state.isSending) { viewModel.onCancelTap() }` so system back gesture mirrors Cancel for both dirty-form and in-flight cases. | app/src/main/java/com/example/aiddproject/kudos/compose/ui/WriteKudoScreen.kt
- [ ] T066 [US2] In `WriteKudoScreenContent.kt`, render `UnsavedChangesDialog` conditionally when `state.confirmDialog is ConfirmDialogState.UnsavedChanges`. Wire confirm/dismiss callbacks. | app/src/main/java/com/example/aiddproject/kudos/compose/ui/WriteKudoScreenContent.kt
- [ ] T067 [P] [US2] Add Compose UI tests in `WriteKudoScreenComposeTest`: `us2_cancelOnCleanForm_navigatesBackNoDialog`, `us2_cancelOnDirtyForm_showsDialog_confirmDiscards_dismissKeepsForm`, `us2_systemBack_mirrorsCancel`, `us2_cancelDuringSubmit_cancelsJobAndPreservesForm`. Cover TC_VIETKUDO_FUN_042 / FUN_051 / FUN_052 / FUN_053 / FUN_054 / FUN_060. | app/src/androidTest/java/com/example/aiddproject/kudos/compose/WriteKudoScreenComposeTest.kt
- [ ] T068 [US2] Add ViewModel unit tests `OnCancelTap_*` in `WriteKudoViewModelTest`: clean form → NavigateBack event, dirty form → dialog state, in-flight → submitJob cancelled. | app/src/test/java/com/example/aiddproject/kudos/compose/ui/WriteKudoViewModelTest.kt
- [ ] T069 [US2] Run unit + instrumented tests. Commit: `feat(write-kudo): Phase 4 — US2 cancel with unsaved-changes protection`.

**Checkpoint**: User Story 2 complete. Test data is never silently lost.

---

## Phase 5: User Story 3 — Reveal validation errors on disabled Send tap (Priority: P1)

**Goal**: Tapping the visually-disabled Send button reveals every empty/invalid required field's error at once (the `0le8xKnFE_` Figma state). Errors clear per-field on edit.

**Independent Test**: Open composer (Send is disabled). Tap Send → assert inline errors render under recipient, title, message, hashtags. Edit one field → that field's error clears.

- [ ] T070 [US3] In `WriteKudoViewModel`, implement `revealErrors()` — compute every field's error via the validators (T024) + populate `state.fieldErrors`. Called by `onSendTap()` when `state.isSubmitEnabled == false`. | app/src/main/java/com/example/aiddproject/kudos/compose/ui/WriteKudoViewModel.kt
- [ ] T071 [US3] In `WriteKudoViewModel`, update `onSendTap()` to fork on `state.isSubmitEnabled`: false → `revealErrors()` + return (no submission); true → existing happy path. Server-error mapping path (T037) unchanged. | app/src/main/java/com/example/aiddproject/kudos/compose/ui/WriteKudoViewModel.kt
- [ ] T072 [P] [US3] In `RecipientPickerField.kt`, render `state.fieldErrors.recipient` (as `@StringRes`) below the dropdown trigger. Use M3 error treatment (error-color text + below the field). | app/src/main/java/com/example/aiddproject/kudos/compose/ui/components/RecipientPickerField.kt
- [ ] T073 [P] [US3] In `TitleField.kt`, pass `state.fieldErrors.title` to the `OutlinedTextField`'s `supportingText` slot + set `isError = (state.fieldErrors.title != null)`. | app/src/main/java/com/example/aiddproject/kudos/compose/ui/components/TitleField.kt
- [ ] T074 [P] [US3] In `MessageEditor.kt`, render `state.fieldErrors.message` below the character counter. Apply error treatment to the counter when message > 1000 chars. | app/src/main/java/com/example/aiddproject/kudos/compose/ui/components/MessageEditor.kt
- [ ] T075 [P] [US3] In `HashtagSection.kt`, the section already renders `state.fieldErrors.hashtags` (T047) — verify the error string for the 1000-char case displays the message-error correctly. | app/src/main/java/com/example/aiddproject/kudos/compose/ui/components/HashtagSection.kt
- [ ] T076 [US3] Add Compose UI tests in `WriteKudoScreenComposeTest`: `us3_tapDisabledSend_revealsAllEmptyFieldErrors`, `us3_editingField_clearsThatFieldsError`, `us3_serverSelfSendRejection_showsInlineErrorUnderRecipient`, `us3_messageOver1000_showsCharacterLimitError`. Covers spec US3 Sc1–Sc8 + TC_VIETKUDO_FUN_001/003/006/010. | app/src/androidTest/java/com/example/aiddproject/kudos/compose/WriteKudoScreenComposeTest.kt
- [ ] T077 [US3] Run tests. Commit: `feat(write-kudo): Phase 5 — US3 inline validation error reveal`.

**Checkpoint**: User Story 3 complete. The `0le8xKnFE_` state is fully implemented.

---

## Phase 6: User Story 4 — Rich-text formatting + @mentions (Priority: P2)

**Goal**: Bold / italic / strikethrough / numbered list / quote / link formatting via the toolbar; @mention overlay anchored to the message caret.

**Independent Test**: Select text, tap Bold → text becomes bold in the rendered preview. Type "@ng" → suggestion overlay shows matching colleagues; select one → mention inserted. Tap Link, enter URL → link wraps the selection.

- [ ] T078 [P] [US4] Create `FormattingToolbar.kt` — `Row` of 6 toggle `IconButton`s (Bold, Italic, Strikethrough, Numbered list, Link, Quote). Each toggle reflects the active formatting at the caret. Bold/Italic/Strike/List/Quote call `MessageMarkdown.applyXxx(state.message, currentSelection)`. Link button opens `LinkInsertDialog`. testTags `BOLD_BUTTON`, etc. | app/src/main/java/com/example/aiddproject/kudos/compose/ui/components/FormattingToolbar.kt
- [ ] T079 [P] [US4] Create `LinkInsertDialog.kt` — M3 `AlertDialog` with `OutlinedTextField` for the URL input. On Confirm, validate via `UrlValidator.isValid(url)` — invalid → inline error in the dialog; valid → callback `onSubmitLink(url)`. testTag `LINK_DIALOG`. | app/src/main/java/com/example/aiddproject/kudos/compose/ui/components/LinkInsertDialog.kt
- [ ] T080 [P] [US4] Create `MentionSuggestionOverlay.kt` — `LazyColumn` anchored to the caret position via `Popup` with a calculated offset. Renders the same 4 sealed states as RecipientPickerOverlay (loading/empty/error/loaded). testTag `MENTION_OVERLAY`. Each row is a `MentionSuggestion`; tap inserts `@FullName` at the caret. | app/src/main/java/com/example/aiddproject/kudos/compose/ui/components/MentionSuggestionOverlay.kt
- [ ] T081 [US4] Upgrade `MessageEditor.kt` — host the `FormattingToolbar` ABOVE the textarea. Track the current selection (`TextRange`) in local Compose state and forward it to the toolbar. Detect "@" + ≥1 char in the input stream and emit an `onMentionQueryChange(String)` event for the VM. | app/src/main/java/com/example/aiddproject/kudos/compose/ui/components/MessageEditor.kt
- [ ] T082 [US4] In `WriteKudoViewModel`, add toolbar intent handlers `onToolbarToggle(action: ToolbarAction)` that delegate to `MessageMarkdown.applyXxx` + update `state.message`. Add `onMentionQueryChange(String)` debounced through the existing recipient-search flow (200 ms debounce + `flatMapLatest`). `onMentionPick(MentionSuggestion)` inserts the mention text at the caret + closes the overlay. | app/src/main/java/com/example/aiddproject/kudos/compose/ui/WriteKudoViewModel.kt
- [ ] T083 [US4] In `WriteKudoViewModel`, add link-dialog intents — `onLinkButtonTap()` sets `state.linkDialog = LinkDialogState.Idle`; `onSubmitLink(url)` validates and inserts the link via `MessageMarkdown.applyLink` then clears `linkDialog`; `onDismissLinkDialog()` clears. | app/src/main/java/com/example/aiddproject/kudos/compose/ui/WriteKudoViewModel.kt
- [ ] T084 [US4] In `WriteKudoScreenContent.kt`, render `LinkInsertDialog` and `MentionSuggestionOverlay` conditionally on their state slots. | app/src/main/java/com/example/aiddproject/kudos/compose/ui/WriteKudoScreenContent.kt
- [ ] T085 [P] [US4] Add Compose UI tests: `us4_boldToggle_appliesBold`, `us4_italicToggle_appliesItalic`, `us4_strikethroughToggle_appliesStrike`, `us4_numberedList_appliesList`, `us4_quote_appliesQuote`, `us4_link_validUrl_inserts`, `us4_link_invalidUrl_showsError`, `us4_mentionTyping_showsOverlay`, `us4_mentionPick_insertsMention`, `us4_mentionNoMatch_emptyOverlay`. Covers spec US4 Sc1–Sc4 + TC_VIETKUDO_FUN_018/019/020/021/022/023/025/055/056. | app/src/androidTest/java/com/example/aiddproject/kudos/compose/WriteKudoScreenComposeTest.kt
- [ ] T086 [P] [US4] Add ViewModel unit tests `OnToolbarToggle_*` covering each of the 5 toggle actions + link insertion + mention pick. | app/src/test/java/com/example/aiddproject/kudos/compose/ui/WriteKudoViewModelTest.kt
- [ ] T087 [P] [US4] Add the corresponding rich-text strings to both `values/` and `values-en/` strings.xml — `write_kudo_link_dialog_*`, `a11y_write_kudo_mention_overlay`. | app/src/main/res/values/strings.xml, app/src/main/res/values-en/strings.xml
- [ ] T088 [US4] (DEFERRED — out of scope for this slice) Upgrade the hub's `KudosFeedCard` rich-text renderer to display the new Markdown subset. Until this lands, the feed renders plain-text by stripping markdown. Tracked as a separate Phase 11 polish ticket. | (deferred)
- [ ] T089 [US4] Run tests. Commit: `feat(write-kudo): Phase 6 — US4 rich-text toolbar + @mentions`.

**Checkpoint**: User Story 4 complete. Authoring richer recognition messages is supported.

---

## Phase 7: User Story 5 — Image attachments (Q-W-2 submit-time upload) (Priority: P2)

**Goal**: Pick up to 5 JPG/PNG/WEBP images ≤ 10 MB each. Local preview shown via `Uri` immediately. Storage upload happens only at submit time; partial failures roll back so no orphans accumulate.

**Independent Test**: Pick a JPG → thumbnail appears (no upload yet). Submit → image uploaded → kudos row inserted with `image_ids = [path]` → hub refreshes. Force a mid-submit upload failure → assert rollback DELETE called for the already-uploaded objects + form remains editable.

- [ ] T090 [P] [US5] Create `ImageSection.kt` — label "Image" + `FlowRow` of thumbnails (each rendered via Coil `AsyncImage` from `UploadedImage.localUri`) + per-thumbnail "x" delete button + "+ Image (Tối đa 5)" add button. Hide the add button when `state.images.size >= 5`. testTags `IMAGE_SECTION`, `IMAGE_ADD`, `IMAGE_THUMBNAIL_PREFIX + index`. | app/src/main/java/com/example/aiddproject/kudos/compose/ui/components/ImageSection.kt
- [ ] T091 [US5] In `WriteKudoScreen.kt`, integrate `rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri -> viewModel.onImagePicked(uri) }` for the system photo picker. Wire the launcher to the `ImageSection`'s add button onClick. | app/src/main/java/com/example/aiddproject/kudos/compose/ui/WriteKudoScreen.kt
- [ ] T092 [US5] In `WriteKudoViewModel`, implement `onImagePicked(uri: Uri)` — read the file's mime via `ContentResolver.getType` + size via `openAssetFileDescriptor`. Validate: mime in `{image/jpeg, image/png, image/webp}` AND size ≤ 10 MB. On reject, set `state.fieldErrors.images` to the appropriate error (`R.string.write_kudo_error_image_type` or `_size`). On accept, append a new `UploadedImage(clientId = UUID, storagePath = null, sizeBytes, mime, localUri = uri)` to `state.images` (no Storage call). | app/src/main/java/com/example/aiddproject/kudos/compose/ui/WriteKudoViewModel.kt
- [ ] T093 [US5] In `WriteKudoViewModel`, implement `onImageRemove(clientId: String)` — pure local-list mutation. `state.copy(images = state.images.filter { it.clientId != clientId })`. NO Storage call (Q-W-2 makes FR-010 trivial). | app/src/main/java/com/example/aiddproject/kudos/compose/ui/WriteKudoViewModel.kt
- [ ] T094 [US5] In `WriteKudoViewModel`, REPLACE `onSendTap`'s happy-path block with the Q-W-2 full flow per plan § Image upload model: (1) generate `kudoId`, (2) sequentially `repo.uploadKudoImage(kudoId, index, image.localUri!!)` for each image; on failure → DELETE all successful uploads + surface inline image error + clear `isSending` + return; (3) build `WriteKudoDraft` with `id = kudoId` and `imageIds = uploaded paths`; (4) `repo.createKudo(draft)` — on failure → DELETE all uploaded images + surface error + clear `isSending`; (5) on full success → emit `Submitted`. Wrap each phase in `try { ... } finally { cleanup }` so cancellation during partial-upload still rolls back. | app/src/main/java/com/example/aiddproject/kudos/compose/ui/WriteKudoViewModel.kt
- [ ] T095 [P] [US5] Add the image-error strings to `values/` and `values-en/` strings.xml — `write_kudo_error_image_type`, `write_kudo_error_image_size`, `write_kudo_error_image_upload`. | app/src/main/res/values/strings.xml, app/src/main/res/values-en/strings.xml
- [ ] T096 [P] [US5] Add Compose UI tests: `us5_pickValidJpg_addsThumbnail`, `us5_pickInvalidMime_showsTypeError_noThumbnail`, `us5_pickOversizeFile_showsSizeError_noThumbnail`, `us5_atFiveImages_addButtonHidden`, `us5_removeThumbnail_isLocalOnly_noStorageCall`. Covers spec US5 Sc1–Sc3 + TC_VIETKUDO_FUN_028/029/035/049/050. | app/src/androidTest/java/com/example/aiddproject/kudos/compose/WriteKudoScreenComposeTest.kt
- [ ] T097 [US5] Add the partial-failure rollback test `us5_uploadFailsMidSubmit_rollsBackSuccessfulUploads` — use the `DemoKudosRepository.failOnNthUpload` hook from T030 to fail the 3rd upload; assert `deleteKudoImage` was called for uploads #1 and #2 + `isSending = false` + form data preserved + inline error visible. | app/src/androidTest/java/com/example/aiddproject/kudos/compose/WriteKudoScreenComposeTest.kt
- [ ] T098 [P] [US5] Add the createKudo-failure-post-uploads rollback test `us5_createKudoFailsAfterAllUploadsSucceed_deletesAllUploads` — `failOnNthUpload = null` (all uploads succeed) + force `createKudo` to throw; assert `deleteKudoImage` called for all images + error surfaced. | app/src/androidTest/java/com/example/aiddproject/kudos/compose/WriteKudoScreenComposeTest.kt
- [ ] T099 [P] [US5] Add ViewModel unit test `OnImagePicked_*` covering mime allow-list, size limit, max-5 enforcement, and the no-storage-call invariant. | app/src/test/java/com/example/aiddproject/kudos/compose/ui/WriteKudoViewModelTest.kt
- [ ] T100 [US5] Extend `WriteKudoRlsPolicyTest` to include a real Storage bucket round-trip — upload a 1 KB image as user A, attempt to DELETE it as user B (should fail), attempt to upload to `kudos-attachments/{userA_id}/...` as user B (should fail). | app/src/androidTest/java/com/example/aiddproject/kudos/compose/WriteKudoRlsPolicyTest.kt
- [ ] T101 [US5] Run unit + instrumented tests. Commit: `feat(write-kudo): Phase 7 — US5 image attachments with Q-W-2 submit-time upload + rollback`.

**Checkpoint**: User Story 5 complete. Attachments + partial-failure rollback both verified.

---

## Phase 8: User Story 6 — Anonymous toggle semantic (Priority: P2)

**Goal**: Toggling Anonymous persists `is_anonymous = true` on the new kudos row; the hub renders the anonymous fallback label for that Kudo.

**Independent Test**: Check Anonymous, submit a Kudo → assert the resulting row in `kudos` has `is_anonymous = true` AND the hub's feed card renders the localized "Anonymous" fallback in place of the sender name.

- [ ] T102 [US6] Anonymous Toggle composable already shipped in Phase 3 (T049). Verify by visual inspection that toggling flips `state.isAnonymous` and the resulting `WriteKudoDraft.isAnonymous` is passed to `createKudo`. No new code unless a defect is found. | (verification — no file change expected)
- [ ] T103 [P] [US6] Add Compose UI test `us6_toggleAnonymous_persistsFlagInDraft` — toggle the checkbox + submit + assert the captured `WriteKudoDraft.isAnonymous == true`. Use a fake `KudosRepository` to capture the call. | app/src/androidTest/java/com/example/aiddproject/kudos/compose/WriteKudoScreenComposeTest.kt
- [ ] T104 [P] [US6] Add Compose UI test `us6_submitWithoutToggle_defaultsToFalse` — assert default `isAnonymous == false`. Covers TC_VIETKUDO_FUN_030 / 044 / 045. | app/src/androidTest/java/com/example/aiddproject/kudos/compose/WriteKudoScreenComposeTest.kt
- [ ] T105 [US6] Verify the hub's `KudosFeedCard` renders `is_anonymous = true` correctly via the existing `kudos_anonymous_nickname_fallback` string. Manual check on emulator with a freshly-submitted anonymous Kudo. No code change. | (verification — no file change expected)
- [ ] T106 [US6] Run tests. Commit: `feat(write-kudo): Phase 8 — US6 anonymous toggle test coverage`.

**Checkpoint**: User Story 6 complete.

---

## Phase 9: User Story 7 — Community Standards link (Priority: P3)

**Goal**: Tapping "Tiêu chuẩn cộng đồng" navigates to a Community Standards screen (placeholder for this slice — full screen ships in a separate spec).

**Independent Test**: Tap B.5 → navigates to `PlaceholderScreen("Community Standards")`. Back returns to the composer with the draft intact.

- [ ] T107 [US7] Add `const val COMMUNITY_STANDARDS: String = "route_community_standards"` to `navigation/Routes.kt`. KDoc cites US7 + notes that the destination is a placeholder until a dedicated spec ships. | app/src/main/java/com/example/aiddproject/navigation/Routes.kt
- [ ] T108 [US7] Register `composable(Routes.COMMUNITY_STANDARDS) { PlaceholderScreen(label = "Community Standards") }` in `navigation/AppNavigation.kt`. | app/src/main/java/com/example/aiddproject/navigation/AppNavigation.kt
- [ ] T109 [US7] In `navigation/AppNavigation.kt`, update the `WriteKudoScreen` composable invocation (line 147 area) to wire `onNavigateToCommunityStandards = { navController.navigate(Routes.COMMUNITY_STANDARDS) }`. | app/src/main/java/com/example/aiddproject/navigation/AppNavigation.kt
- [ ] T110 [P] [US7] Add Compose UI test `us7_tapCommunityStandardsLink_invokesCallback` — assert the `onCommunityStandards` callback fires when B.5 is tapped. Covers TC_VIETKUDO_FUN_017 / 046. | app/src/androidTest/java/com/example/aiddproject/kudos/compose/WriteKudoScreenComposeTest.kt
- [ ] T111 [US7] Run tests. Commit: `feat(write-kudo): Phase 9 — US7 community standards link (placeholder destination)`.

**Checkpoint**: All user stories complete. Functional gate passed.

---

## Phase 10: Polish & Cross-Cutting Concerns

**Purpose**: Accessibility audit, Figma visual fidelity, dark theme + dynamic color, font scaling, asset export. Final QA gate before merge.

- [ ] T112 [P] TalkBack audit on a real device — every interactive element exposes a meaningful `contentDescription`, required fields announce their required state, validation errors are announced via Compose `Modifier.semantics { liveRegion = LiveRegionMode.Polite }`, character counter announces "X of 1000" politely + assertively above 1000. Apply Compose semantics modifiers as needed throughout the composables. | (multiple files — kudos/compose/ui/components/)
- [ ] T113 [P] Apply `Modifier.semantics { contentDescription = stringResource(R.string.a11y_write_kudo_*) }` to every component lacking one. Use the a11y strings from T009/T010 + add any missing ones. | (multiple files — kudos/compose/ui/components/ + strings.xml)
- [ ] T114 [P] `mcp__momorph__get_media_files` Figma asset pull for icons (toolbar C.1–C.6, F.5 add-image, B.2 chevron, H/I action-bar icons) → save to `app/src/main/res/drawable-nodpi/`. Replace any `Icons.Default.*` placeholders in the composables with the Figma exports. | app/src/main/res/drawable-nodpi/ic_write_kudo_*.png
- [ ] T115 `mcp__momorph__query_section` pass per Figma frame (`7fFAb-K35a` default, `0le8xKnFE_` error state, `5MU728Tjck` recipient dropdown, `aKWA2klsnt` hashtag dropdown) → align spacing / typography / colors against the design via the existing M3 theme tokens. No hard-coded hex / sp values (Constitution III). | (multiple files — kudos/compose/ui/components/)
- [ ] T116 [P] Dark-theme + dynamic-color sanity check — switch system theme + dynamic color toggle on a Pixel 10 emulator, confirm every composable reads from `MaterialTheme.colorScheme` correctly. Fix any hard-coded colors. | (multiple files)
- [ ] T117 [P] Font-scaling test at 200% — confirm no clipping / overlap / overflow in form fields, error messages, dialog body text, sticky bottom action bar. Adjust `Modifier.padding` / line-height where necessary. | (multiple files)
- [ ] T118 [P] Run `./gradlew :app:ktlintCheck` and fix any style violations. | (multiple files)
- [ ] T119 Run `./gradlew :app:lintDebug` + fix flagged issues (esp. missing `contentDescription`, hard-coded strings, unused resources). | (multiple files)
- [ ] T120 Full instrumented run on Pixel_10_Pro emulator (`./gradlew :app:connectedAndroidTest`). All US1–US7 tests + RLS policy tests green. Manual smoke test of the happy-path flow + error reveal + cancel-with-dialog + image attachment + anonymous toggle. | (verification — emulator run)
- [ ] T121 Run `./gradlew :app:testDebugUnitTest`. All unit tests green. | (verification — local run)
- [ ] T122 Author or extend the screen's QA gate document (mirror the hub's `DEVIATIONS.md` pattern in `.momorph/specs/7fFAb-K35a-iOS-Sun-Kudos-Viet-Kudo/`) — list every Figma visual deviation that remains, with severity + ticket-ref for follow-up. | .momorph/specs/7fFAb-K35a-iOS-Sun-Kudos-Viet-Kudo/DEVIATIONS.md
- [ ] T123 Commit: `feat(write-kudo): Phase 10 — polish + a11y + visual fidelity + QA gate`. Open PR for review.

**Checkpoint**: Feature complete. Ready for code review + ship.

---

## Dependencies & Execution Order

### Phase dependencies

- **Setup (Phase 1)**: No dependencies — can start immediately.
- **Foundation (Phase 2)**: Depends on Phase 1 completion. **BLOCKS all user stories.**
- **User Stories (Phase 3+)**: All depend on Phase 2 completion.
  - **Phase 3 (US1 MVP)** is the canonical first slice; later phases layer onto it.
  - **Phase 4 (US2 Cancel)** depends on Phase 3 (uses `state.formDirty` + `submitJob` slot).
  - **Phase 5 (US3 Errors)** depends on Phase 3 (uses `state.fieldErrors` shape + Send-button wrap).
  - **Phase 6 (US4 Rich-text)** depends on Phase 3 (extends `MessageEditor`). Independent of Phases 4–5.
  - **Phase 7 (US5 Images)** depends on Phase 3 (extends `onSendTap`). Independent of Phases 4–6.
  - **Phase 8 (US6 Anonymous)** is mostly verification — depends on Phase 3 (toggle composable shipped in T049). Effectively free.
  - **Phase 9 (US7 Standards link)** depends on Phase 3 (B.5 composable shipped in T045). Independent of Phases 4–8.
- **Polish (Phase 10)**: Depends on all user-story phases (3 through 9).

### Within each user story

- Failing tests are committed FIRST (Phase 1 covers US1 failing tests; subsequent phases ship their failing tests at the START of the phase per Constitution V).
- Domain models before services before composables (foundation layering).
- Stateless content before Hilt entry before navigation wiring.
- Tests turn green at the END of each phase before the commit.

### Parallel opportunities

- All Phase 1 tasks marked [P] can run in parallel (T002 + T004 + T005 + T007 + T008 + T009 + T010 + T011 + T012 + T013 + T014 + T015 + T016).
- Phase 2 domain models (T019–T023) all parallel.
- Phase 3 sub-composables (T041–T051) all parallel after the UiState + TestTags + VM core ship.
- Phase 4–9 user-story phases can be worked on by different developers in parallel ONCE Phase 3 ships.
- All Compose UI tests marked [P] within the same phase have no inter-test dependencies and can run in parallel.

---

## Implementation Strategy

### MVP first (recommended)

1. Complete Phase 1 + Phase 2 (~32 tasks).
2. Complete Phase 3 (~29 tasks) → 🎯 MVP gate.
3. **STOP and VALIDATE**: open Viết Kudo from the hub Send pill on the emulator, submit a plain-text Kudo, see it land at the top of the All-Kudos list. Commit + push for staging review.
4. Once stakeholders sign off on MVP, proceed with Phases 4–9 in priority order (P1 → P2 → P3): Phase 4 (US2 Cancel) → Phase 5 (US3 Errors) → Phase 6 (US4 Rich-text) → Phase 7 (US5 Images) → Phase 8 (US6 Anonymous) → Phase 9 (US7 Standards link).
5. Polish (Phase 10) closes the loop.

### Incremental delivery

1. Setup + Foundation (Phase 1 + 2). Commit.
2. Phase 3 US1 → test → commit (MVP shippable here).
3. Phase 4 US2 → test → commit.
4. Phase 5 US3 → test → commit.
5. Phase 6 US4 → test → commit.
6. Phase 7 US5 → test → commit.
7. Phase 8 US6 → test → commit.
8. Phase 9 US7 → test → commit.
9. Phase 10 polish → test → final commit + PR.

Per `memory/feedback_commit_per_task.md`: granular commits per task / coherent task group; never batch phases. QA gate must be green first.

---

## Notes

- Commit after each task or coherent task group; never batch phases.
- Run tests before moving to next phase.
- Update spec.md / plan.md if requirements change during implementation; flag deviations in `DEVIATIONS.md` (T122).
- Mark tasks complete as you go: `[x]`.
- Q-W-4 (Community Standards target screen) remains a placeholder per recommendation; revisit when the standards spec ships.
- The `WriteKudoScreenComposeTest` file grows across phases — keep each phase's tests in a nested `@Nested` class for clarity once it exceeds 10 tests.
