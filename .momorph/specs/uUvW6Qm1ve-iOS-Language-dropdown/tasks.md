# Tasks: Language Dropdown

**Frame**: `uUvW6Qm1ve-iOS-Language-dropdown`
**Prerequisites**: plan.md (required, present), spec.md (required,
present, ratified 2026-05-08)

> **Note on `design-style.md`**: This project intentionally does NOT
> produce a `design-style.md`. Per `spec.md` § Visual Requirements and
> `plan.md` § Notes, visual specifications (colours, sizes, fonts) are
> fetched on-demand at implementation time via MoMorph
> `query_section` / `get_node` for the Node IDs listed in `spec.md`.
> Tasks below reference those Node IDs where pixel-level detail is
> needed.
>
> **Note on `research.md`**: Intentionally not produced — research
> findings are inlined in `plan.md` § Architecture Decisions.

---

## Task Format

```
- [ ] T### [P?] [Story?] Description | file/path.kt
```

- **[P]**: Can run in parallel (different files, no dependencies on incomplete tasks)
- **[Story]**: Which user story this belongs to (US1, US2, US3, US4)
- **|**: File path affected by this task

> **MVP slice**: this feature collapses US1 + US2 + US3 + US4 into a
> single Phase 3 because the underlying work is one vertical slice
> (one composable, one enum, one repository — see plan.md § Implementation
> Strategy). Splitting them risks intermediate broken states. Tasks
> within Phase 3 carry the `[US1]` label as the primary story; mirror
> stories (US2/US3/US4) ride along on the same diff and are tagged in
> the description where coverage is asserted.

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: pre-flight grep audit, brand-fixed + localized string
resource additions, telemetry tag wiring. Plan's "no new dependencies"
holds.

- [x] T001 [P] Pre-flight grep audit: run `grep -rn "Language\.JA\|menuItemTag(Language\.JA)" app/src/` and confirm production code has zero hits (only tests reference `Language.JA`). Findings documented in plan.md § Dependencies & Prerequisites; this task verifies the audit still holds at implementation time. | (no file)
- [x] T002 [P] Add new TalkBack `stateDescription` strings to `values/strings.xml`: `a11y_dropdown_expanded` ("đã mở") and `a11y_dropdown_collapsed` ("đã đóng"). VN authoritative; localized (NOT brand-fixed). | app/src/main/res/values/strings.xml
- [x] T003 [P] Mirror the new strings into `values-en/strings.xml`: `a11y_dropdown_expanded` ("expanded"), `a11y_dropdown_collapsed` ("collapsed"). | app/src/main/res/values-en/strings.xml
- [x] T004 [P] Drop the `values-ja` locale from `StringResourceParityTest`'s active set (per plan.md § Out of Scope). The Java file stays on disk as a dead resource for one release cycle but the parity test no longer asserts JA coverage. If `StringResourceParityTest` does not yet exist, document this as a follow-on for the Login Phase 7 carry-over. | app/src/test/java/.../StringResourceParityTest.kt (or follow-on note)

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: file move, enum shrink, repository hardening. **Blocks all
US1–US4 implementation tasks** because the core types they depend on
must be in their final shape first.

### Tests First (TDD per Constitution Principle V — written and FAILING before implementation)

- [x] T005 [P] Write `LanguagePreferenceRepositoryTest.first_launch_no_preference_returns_VN_default` — fresh in-memory DataStore with no key written; assert `language.first() == Language.VN` (FR-002). | app/src/test/java/com/example/aiddproject/core/locale/LanguagePreferenceRepositoryTest.kt
- [x] T006 [P] Write `LanguagePreferenceRepositoryTest.orphaned_JA_token_returns_VN_default` — write `"JA"` directly into preferences (bypassing typed `set()`); assert `language.first() == Language.VN` (spec § Edge Cases — DataStore unavailable or corrupted; § Resolved Q3). | app/src/test/java/com/example/aiddproject/core/locale/LanguagePreferenceRepositoryTest.kt
- [x] T007 [P] Write `LanguagePreferenceRepositoryTest.unknown_code_returns_VN_default` — write `"FR"` directly; assert VN fallback. Locks the `Language.fromCode(...)` default branch. | app/src/test/java/com/example/aiddproject/core/locale/LanguagePreferenceRepositoryTest.kt
- [x] T008 [P] Write `LanguagePreferenceRepositoryTest.set_writes_via_Language_code_token_not_name_or_ordinal` — set `Language.EN`; read raw `Preferences[KEY]`; assert it equals `"EN"` (NOT `"English"` and NOT `"1"`). Regression lock per spec § Key Entities. | app/src/test/java/com/example/aiddproject/core/locale/LanguagePreferenceRepositoryTest.kt
- [x] T009 [P] Write `LanguagePreferenceRepositoryTest.write_failure_emits_telemetry_breadcrumb_and_keeps_flow_unchanged` — inject a DataStore stub whose `edit { ... }` throws; assert `repository.set(Language.EN)` returns without rethrowing, the language flow does not advance off VN, and a `LanguageTelemetry` Timber breadcrumb fires (verified via a test `Timber.Tree`). Covers spec § Edge Cases — DataStore write failure + TR-005. | app/src/test/java/com/example/aiddproject/core/locale/LanguagePreferenceRepositoryTest.kt

### Existing-test cleanup (compile-blocking after T011 lands)

- [x] T010 Update existing `LanguagePreferenceRepositoryTest` cases that reference `Language.JA` (lines 60–61: `set(Language.JA)` round-trip; line 94: `Language.fromCode("JA")` mapping). Replace with `Language.EN` round-trip; the new T006 case already covers the orphaned-`"JA"` token contract. (Depends on T005–T009 being committed first so the test file is in its final shape before lines are changed.) | app/src/test/java/com/example/aiddproject/core/locale/LanguagePreferenceRepositoryTest.kt
- [x] T010a [P] Update `HomeViewModelTest` cases that use `Language.JA` (lines 228, 230, 317, 319 in the `language flow propagates persisted preference into uiState` and `language StateFlow is collected eagerly` methods). Replace with `Language.EN` so the propagation contract is still asserted with a non-default value. | app/src/test/java/com/example/aiddproject/home/ui/HomeViewModelTest.kt

### Domain (enum shrink + repository hardening)

- [x] T011 Drop `JA` from `Language.entries`. Remove the `JA(...)` enum value and update the KDoc comment to reflect the new `[VN, EN]` set. `Language.fromCode("JA")` already falls back to `Default = VN` via the existing `firstOrNull` branch — no change needed inside `fromCode`. (Depends on T010 + T010a so existing JA-using tests don't break compile.) | app/src/main/java/com/example/aiddproject/core/locale/Language.kt
- [x] T012 Wrap `LanguagePreferenceRepository.set(...)`'s `dataStore.edit { ... }` in `runCatching { ... }`. On failure, fire `Timber.tag("LanguageTelemetry").w(error, "language.write.failure")` and return normally so the caller does not see an exception. Document inline that DataStore's edit semantics already de-duplicate identical writes. (Depends on T009 to satisfy the failing test.) | app/src/main/java/com/example/aiddproject/core/locale/LanguagePreferenceRepository.kt

### File move

- [x] T013 Move `LanguageSelector.kt` from `app/src/main/java/com/example/aiddproject/auth/login/ui/components/LanguageSelector.kt` to `app/src/main/java/com/example/aiddproject/core/locale/ui/LanguageSelector.kt`. Update the package declaration in the moved file. Do NOT modify the composable body in this task — keep it identical so the diff is purely the move. (Depends on T011 so the move and the JA-row removal land together; the row disappears automatically because `Language.entries.forEach` adapts.) | app/src/main/java/com/example/aiddproject/core/locale/ui/LanguageSelector.kt + DELETE app/src/main/java/com/example/aiddproject/auth/login/ui/components/LanguageSelector.kt
- [x] T014 [P] Update import path in `LoginScreen.kt` from `com.example.aiddproject.auth.login.ui.components.LanguageSelector` to `com.example.aiddproject.core.locale.ui.LanguageSelector`. | app/src/main/java/com/example/aiddproject/auth/login/ui/LoginScreen.kt
- [x] T015 [P] Update import path in `HomeHeader.kt` from `com.example.aiddproject.auth.login.ui.components.LanguageSelector` to `com.example.aiddproject.core.locale.ui.LanguageSelector`. | app/src/main/java/com/example/aiddproject/home/ui/components/HomeHeader.kt
- [x] T016 [P] Move existing `LanguageSelectorTest.kt` from `app/src/androidTest/java/com/example/aiddproject/auth/login/ui/components/LanguageSelectorTest.kt` to `app/src/androidTest/java/com/example/aiddproject/core/locale/ui/LanguageSelectorTest.kt`. Update the package declaration. While in transit, remove lines 58 + 62 (the JA row assertions) — the JA row no longer exists. Other assertions stay; new contract tests are added in Phase 3. | app/src/androidTest/java/com/example/aiddproject/core/locale/ui/LanguageSelectorTest.kt + DELETE app/src/androidTest/java/com/example/aiddproject/auth/login/ui/components/LanguageSelectorTest.kt
- [x] T017 Update import paths in `HomeLocaleSwitchTest.kt` (uses `TEST_TAG_ANCHOR` and `menuItemTag` from the moved file). | app/src/androidTest/java/com/example/aiddproject/home/HomeLocaleSwitchTest.kt

**Checkpoint**: foundation green — `./gradlew assembleDebug
testDebugUnitTest compileDebugAndroidTestKotlin` passes. JA is gone
from production + tests; the moved selector compiles in its new path;
the repository emits a telemetry breadcrumb on write failure.

---

## Phase 3: User Story 1 + 2 + 3 + 4 — Language Dropdown Behaviour (Priority: P1) 🎯 MVP

**Goal**: tighten `LanguageSelector` to match every spec contract —
TalkBack focus on open, content-description recompute on language flip,
single-click guards on anchor + rows, 48dp click region on the 32dp
anchor pill, M3-default selected state, idempotent reselection,
predictive-back dismissal. The four spec user stories collapse into one
phase because they share one composable.

**Independent Test**: open Login → tap language pill → observe two rows
(VN, EN) with VN selected → tap EN → menu closes, anchor flips to "🇬🇧
EN", every Login string re-renders in English. Force-stop → relaunch →
Login first frame is in EN. Tap pill → observe EN row selected → tap
EN again → menu closes silently (no callback, no DataStore write).

### Tests First (instrumented `LanguageSelectorTest` in the new path)

- [x] T018 [P] [US1] Write `menu_renders_exactly_VN_then_EN_no_JA` — opens the menu; asserts only two `DropdownMenuItem`s render in the documented order; no JA tag exists. Covers FR-004 + spec § Resolved Q1. | app/src/androidTest/java/com/example/aiddproject/core/locale/ui/LanguageSelectorTest.kt
- [x] T019 [P] [US1] Write `selecting_EN_invokes_onSelect_and_updates_anchor_label` — drives a stateful wrapper owning `selected`; opens menu; taps EN; asserts (a) `onSelect(EN)` fired exactly once, (b) the menu closed, (c) the anchor's visible label flipped from "VN" to "EN" within the same composition. Covers FR-005 happy path. | app/src/androidTest/java/com/example/aiddproject/core/locale/ui/LanguageSelectorTest.kt
- [x] T020 [P] [US1] Write `contentDescription_recomputes_when_language_flips` — drives `selected = VN → EN`; asserts the anchor's `contentDescription` advances from "Ngôn ngữ, Tiếng Việt, danh sách thả xuống" → "Ngôn ngữ, English, danh sách thả xuống". Covers TR-003 anchor recompute. | app/src/androidTest/java/com/example/aiddproject/core/locale/ui/LanguageSelectorTest.kt
- [x] T021 [P] [US1] Write `anchor_exposes_role_button_with_expanded_state` — when menu is closed, asserts anchor's `Role.Button` + `stateDescription` matches `a11y_dropdown_collapsed`; opens menu; asserts state flips to `a11y_dropdown_expanded`. Covers TR-003 expand/collapse contract. | app/src/androidTest/java/com/example/aiddproject/core/locale/ui/LanguageSelectorTest.kt
- [x] T022 [P] [US1] Write `opening_menu_focuses_first_row_for_TalkBack` — taps the anchor; asserts the VN row reports `assertIsFocused()`. Covers TR-003 initial focus. | app/src/androidTest/java/com/example/aiddproject/core/locale/ui/LanguageSelectorTest.kt
- [x] T023 [P] [US1] Write `keyboard_tab_order_is_anchor_then_VN_then_EN` — `requestFocus()` on anchor; `pressKey(Tab)` advances focus through VN then EN in visual order. Covers TR-003 keyboard nav. | app/src/androidTest/java/com/example/aiddproject/core/locale/ui/LanguageSelectorTest.kt
- [x] T024 [P] [US1] Write `anchor_meets_48dp_touch_target` — `getBoundsInRoot().height` on the anchor node ≥ 48dp despite the 32dp visual pill. Covers TR-008. | app/src/androidTest/java/com/example/aiddproject/core/locale/ui/LanguageSelectorTest.kt
- [x] T025 [P] [US1] Write `each_row_meets_48dp_touch_target` — `getBoundsInRoot()` on each `DropdownMenuItem` ≥ 48dp on both axes. Covers TR-008. | app/src/androidTest/java/com/example/aiddproject/core/locale/ui/LanguageSelectorTest.kt
- [x] T026 [P] [US1] Write `reselecting_active_language_is_idempotent` — selects VN while VN is already active; asserts `onSelect` callback is not invoked. Covers FR-006 + TR-005 UI-side. | app/src/androidTest/java/com/example/aiddproject/core/locale/ui/LanguageSelectorTest.kt
- [x] T027 [P] [US1] Write `menu_double_tap_yields_one_select_callback` — two rapid taps on EN row produce exactly one `onSelect(EN)` call. Covers TR-004 row side. | app/src/androidTest/java/com/example/aiddproject/core/locale/ui/LanguageSelectorTest.kt
- [x] T028 [P] [US1] Write `anchor_double_tap_yields_one_open` — two rapid taps on the anchor leave the menu in the *open* state with exactly one `expanded = true` transition (asserted via a callback counter wrapping the open lambda). Covers TR-004 anchor side. | app/src/androidTest/java/com/example/aiddproject/core/locale/ui/LanguageSelectorTest.kt
- [x] T029 [P] [US1] Write `second_tap_on_anchor_closes_menu` — open the menu; tap the anchor again; assert the menu is no longer displayed AND the active language is unchanged. Covers FR-008 first dismiss path. | app/src/androidTest/java/com/example/aiddproject/core/locale/ui/LanguageSelectorTest.kt
- [x] T030 [P] [US1] Write `outside_tap_closes_menu_without_changing_language` — open the menu; tap a sibling node outside the menu bounds; assert the menu closes and `onSelect` was not invoked. Covers FR-008 second dismiss path. | app/src/androidTest/java/com/example/aiddproject/core/locale/ui/LanguageSelectorTest.kt
- [x] T031 [P] [US1] Write `predictive_back_dismisses_menu_without_popping_parent` — expanded menu + `Espresso.pressBack()` → menu closes, parent composable still composed. Covers FR-008 third dismiss path + spec § Edge Cases — predictive back gesture. | app/src/androidTest/java/com/example/aiddproject/core/locale/ui/LanguageSelectorTest.kt

### LanguageSelector composable changes (in `core/locale/ui/`)

- [x] T032 [US1] Wrap the existing 32dp anchor `Row` inside a parent `Box` that applies `Modifier.heightIn(min = 48.dp)`. Move `clickableWithoutRipple` + the new `rememberSingleClickHandler` wrapper to the outer Box. The inner Row keeps its 32dp `height`, 4dp `clip` shape, and visual padding so the rendered pill is unchanged. Tested by T024. | app/src/main/java/com/example/aiddproject/core/locale/ui/LanguageSelector.kt
- [x] T033 [US1] Wrap the anchor's open lambda in `rememberSingleClickHandler { expanded = !expanded }` so a finger-bounce double-tap can never push two `expanded = true` transitions through the menu. Tested by T028. | app/src/main/java/com/example/aiddproject/core/locale/ui/LanguageSelector.kt
- [x] T034 [US1] Add `Role.Button` + `stateDescription = stringResource(if (expanded) R.string.a11y_dropdown_expanded else R.string.a11y_dropdown_collapsed)` to the anchor's `Modifier.semantics { ... }` block, alongside the existing `contentDescription`. Tested by T021. | app/src/main/java/com/example/aiddproject/core/locale/ui/LanguageSelector.kt
- [x] T035 [US1] Add a `remember { FocusRequester() }` named `firstRowFocusRequester` and attach it to the **first** `DropdownMenuItem`'s `Modifier.focusRequester(...)`. Add a `LaunchedEffect(expanded)` that calls `firstRowFocusRequester.requestFocus()` when `expanded` flips true. Tested by T022. | app/src/main/java/com/example/aiddproject/core/locale/ui/LanguageSelector.kt
- [x] T036 [US1] Wrap each `DropdownMenuItem`'s `onClick` body in `rememberSingleClickHandler` so a row double-tap yields exactly one `onSelect(lang)` call. The existing `if (lang != selected) onSelect(lang)` short-circuit stays inside the wrapped lambda. Tested by T026 + T027. | app/src/main/java/com/example/aiddproject/core/locale/ui/LanguageSelector.kt
- [x] T037 [US1] Add `Modifier.heightIn(min = 48.dp)` to each `DropdownMenuItem` so TR-008's row touch-target floor is met. Tested by T025. | app/src/main/java/com/example/aiddproject/core/locale/ui/LanguageSelector.kt
- [x] T038 [US1] Pass `selected = (lang == selected)` to each `DropdownMenuItem` so M3 renders the active row in its default selected-state colour treatment (no custom check glyph per spec § Out of Scope). The existing implementation already does this — task is a verification + KDoc note, no behaviour change. | app/src/main/java/com/example/aiddproject/core/locale/ui/LanguageSelector.kt

### LocaleViewModel + repository wiring (defence-in-depth, no new public surface)

- [x] T039 [US1] Verify `LocaleViewModel.setLanguage(...)` (in `LanguageProvider.kt`) does NOT swallow the repository's failure — by Phase 2 the repository swallows it internally and returns normally, so the VM's `viewModelScope.launch { repository.set(language) }` naturally does the right thing. Task is a code-read + KDoc cross-reference; no edit unless the read finds a regression. | app/src/main/java/com/example/aiddproject/core/locale/LanguageProvider.kt

**Checkpoint**: every Phase-3 instrumented test green; the dropdown
behaves identically on Login + Home host screens; the persisted choice
survives a force-stop + relaunch.

---

## Phase 4: Polish & Cross-Cutting Concerns

**Purpose**: visual smoke + parity-test cleanup + doc update on the
shared `LanguageSelector` contract.

- [x] T040 [P] Visual smoke: install a debug build on the connected emulator; open the language pill on Login → verify VN + EN render in the documented order with no JA row; flip to EN → verify Login strings re-render; navigate to Home → verify the same dropdown opens from Home's header with the same two rows. Capture before/after screenshots and attach to the PR. | (no file)
- [x] T041 [P] Update Login's spec.md ChangeLog (or `Notes` section) and Home's spec.md to reference the moved `LanguageSelector` path, so future readers find the canonical location at `core/locale/ui/`. | .momorph/specs/8HGlvYGJWq-iOS-Login/spec.md + .momorph/specs/OuH1BUTYT0-iOS-Home/spec.md
- [x] T042 [P] Add a short KDoc to `LanguageSelector.kt`'s file header listing the two host call sites (Login, Home) and noting that adding a third host needs no code change — the component is now in a screen-neutral package. | app/src/main/java/com/example/aiddproject/core/locale/ui/LanguageSelector.kt
- [x] T043 [P] If `StringResourceParityTest` exists, drop `values-ja` from its active locale set (per Phase 1 T004 — this task closes the loop after the new strings land in `values/` and `values-en/`). If the test does not yet exist, append a follow-on note to the Login Phase 7 carry-over list. | app/src/test/java/.../StringResourceParityTest.kt OR a follow-on note

---

## Phase 5: Final Validation

- [x] T044 Run full Quality Gates: `./gradlew lint ktlintCheck assembleDebug testDebugUnitTest compileDebugAndroidTestKotlin` — all green. The `connectedDebugAndroidTest` step is left for CI per Login Phase 7 carry-over (Q4) — emulator is local-only. | (no file)

---

## Phase 6: Post-ship bug fixes (visual fidelity vs Figma)

**Purpose**: cluster of design-vs-implementation gaps surfaced during
post-ship visual review against Figma node `6891:15595`. Each task is a
self-contained fix; commit per task or per coherent group.

- [x] T045 Bug — dropdown surface chrome + row labels — apply Figma node `6891:15595` design tokens (Details-Container-2 #00070C, Details-Border #998C5F, 8dp radius); switch row label from `lang.nativeName` to `lang.code` per FR-004 + spec § Screen Components. Add `rows_render_two_letter_code_not_native_name` regression test. (Shipped in commit `ad86cd2`.) | app/src/main/java/com/example/aiddproject/core/locale/ui/LanguageSelector.kt + app/src/androidTest/.../LanguageSelectorTest.kt
- [x] T046 Bug — row contentPadding + flag-slot width — set DropdownMenuItem `contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)` to match Figma `6891:15596` Button `padding: 16px`; wrap each flag emoji in a 24dp fixed-width `Box` (Figma `Frame 485` icon slot) so 🇻🇳 (~21dp intrinsic) and 🇺🇸 (~26dp intrinsic) leave the same x-offset for the code label. (Shipped in commit `e11457f`.) | app/src/main/java/com/example/aiddproject/core/locale/ui/LanguageSelector.kt
- [x] T047 Bug — menu surface horizontal inset — Figma node `6891:15595` has `padding: 6px` on all sides; the M3 `DropdownMenu` adds only `MenuListContentPadding = PaddingValues(vertical = 8.dp)` and zero horizontal padding, so rows currently touch the gold border on left/right. Wrap the menu content in an inner `Column(modifier = Modifier.padding(horizontal = 6.dp))` so each row sits 6dp inside the surface chrome and the active-row cream tint inherits the same inset. (Shipped in commit `441aca0`.) | app/src/main/java/com/example/aiddproject/core/locale/ui/LanguageSelector.kt
- [x] T048 Bug — row width + center-aligned content — Figma node `6891:15596` rows are 108dp wide and the flag+code group is centered horizontally inside the row's 76dp content area (Frame 485 sits at +13dp from the 16dp left padding, which is `(76 − 50) / 2`). Currently each row auto-sizes to the natural width of the content (≈82dp) AND the inner Row uses `Arrangement.Start`. Fix by (a) setting `Modifier.width(108.dp)` on each `DropdownMenuItem` so M3 sizes the menu to 122dp overall (108 + 6×2 inset + 1×2 border) matching Figma, and (b) making the inner Row `fillMaxWidth()` + `horizontalArrangement = Arrangement.Center` so the 24dp flag-slot + 4dp gap + code label centers as one group within the 76dp content area. | app/src/main/java/com/example/aiddproject/core/locale/ui/LanguageSelector.kt — Figma node `6891:15595` has `padding: 6px` on all sides; the M3 `DropdownMenu` adds only `MenuListContentPadding = PaddingValues(vertical = 8.dp)` and zero horizontal padding, so rows currently touch the gold border on left/right. Wrap the menu content in an inner `Column(modifier = Modifier.padding(horizontal = 6.dp))` so each row sits 6dp inside the surface chrome and the active-row cream tint inherits the same inset (matches the Figma render). | app/src/main/java/com/example/aiddproject/core/locale/ui/LanguageSelector.kt

---

## Dependencies & Execution Order

### Phase Dependencies

- **Phase 1 (Setup)** — no dependencies; can start immediately. T001
  is a verification step (grep audit), T002–T003 are string adds, T004
  is a parity-test cleanup follow-on.
- **Phase 2 (Foundational)** — depends on Phase 1 (string keys must
  exist before the composable references them). **Blocks Phase 3.**
  Within Phase 2:
  - T005–T009 (failing tests) [P] — must land before T011/T012
    (production change).
  - T010 + T010a (existing-test cleanup) — must land before T011
    (otherwise compilation fails).
  - T011 (drop JA) — depends on T010 + T010a.
  - T012 (repository runCatching + telemetry) — depends on T009.
  - T013 (file move) — depends on T011 (the move and the row-disappear
    must land in the same diff).
  - T014, T015, T016, T017 (import-path updates) — all [P] after T013.
- **Phase 3 (User Stories)** — depends on Phase 2 completion.
  Within Phase 3:
  - All test tasks T018–T031 [P] — must land FAILING before T032–T038
    (Constitution Principle V).
  - Composable changes T032–T038 — sequential within the same file
    (`LanguageSelector.kt`); not parallelizable but quick.
  - T039 (VM cross-reference) [P] — independent of T032–T038.
- **Phase 4 (Polish)** — all tasks [P]; depends on Phase 3 green.
- **Phase 5 (Final Validation)** — depends on Phase 4 done.

### Within Phase 3

- Tests T018–T031 [P] all touch the same file
  (`LanguageSelectorTest.kt`); they're parallelizable in *authoring*
  but the file edits must be sequenced when committed. Convention:
  one task per test method, one commit per group of related tests.
- Composable edits T032–T038 affect one file (`LanguageSelector.kt`);
  treat as sequential.

### Parallel Opportunities

- **Phase 1**: T001, T002, T003, T004 all [P].
- **Phase 2**: T005–T009 (test authoring) [P]; T010a [P]; T014/T015/T016
  (import updates) [P].
- **Phase 3**: every test task T018–T031 marked [P]; T039 [P] alongside
  the composable edits.
- **Phase 4**: T040, T041, T042, T043 all [P].

---

## Implementation Strategy

### MVP First (Recommended)

Phase 3 is the MVP — collapsing US1+US2+US3+US4 into a single slice. There
is no "MVP cut" smaller than the full Phase 3 because the four user
stories share the same composable, the same enum, and the same
repository.

1. Phase 1 + Phase 2 (Setup + Foundational).
2. Phase 3 (US1+US2+US3+US4 vertical slice).
3. **STOP and VALIDATE** end-to-end on the emulator: Login + Home both
   show the new dropdown with VN + EN, persistence works, TalkBack focus
   lands on VN, predictive back dismisses cleanly.
4. Phase 4 polish + Phase 5 final gates.

### Incremental Delivery

Not applicable — there is no smaller cut. The work IS the slice.

---

## Notes

- **Single MVP slice rationale**: see plan.md § Implementation
  Strategy and § Notes. Splitting the user stories into separate phases
  would force intermediate broken states (e.g. anchor moved but rows
  not yet on the new focus contract), which is worse than landing the
  coherent change set at once.
- **No new dependencies**: this feature ships entirely on libraries
  already pinned in `gradle/libs.versions.toml`.
- **No backend, no API, no Supabase**: the dropdown is a client-only
  feature per spec.md § API Dependencies. No RLS test, no migration,
  no network call.
- **Visual specs**: any task requiring pixel-level fidelity (anchor
  pill chrome, dropdown surface chrome, row chrome) fetches values at
  task-execution time via MoMorph `query_section` / `get_node` for the
  Node IDs in `spec.md` § Screen Components — not enumerated here per
  Constitution Principle II.
- **Commit cadence**: commit after each task or coherent task group
  (e.g. all string-resource adds, all import-path updates, all test
  authoring). Mark tasks complete as you go: `[x]`. Run unit tests
  before moving to the next task; run the instrumented suite before
  closing a phase.
- **Existing tests carried forward**: the existing
  `LanguageSelectorTest`'s `anchor_renders_with_current_language_two_letter_code`
  and `selecting_already_active_language_does_not_emit_callback` cases
  are preserved through the move (T016) — only the JA-row assertions
  on lines 58 + 62 are removed. Net new test count is +14 (T005–T009,
  T018–T031, minus T016's existing-case carry-forward).
