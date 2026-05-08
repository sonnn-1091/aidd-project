# Implementation Plan: Language Dropdown

**Frame**: `uUvW6Qm1ve-iOS-Language-dropdown`
**Date**: 2026-05-08
**Spec**: `specs/uUvW6Qm1ve-iOS-Language-dropdown/spec.md`

---

## Summary

Tighten the existing shared `LanguageSelector` Compose component (already
shipped from Login Phase 3 + Home Phase 11) so it matches the ratified
`[iOS] Language dropdown` spec end-to-end. The work is **mostly subtractive
+ contractual**: drop Japanese (JA) from `Language.entries`, harden the
DataStore decoder with the new "unknown code → VN" fallback contract,
recompute the anchor's `contentDescription` when the language changes so
TalkBack re-announces, lock down the per-row TalkBack focus order, and
pull `LanguageSelector` out of the auth/login package now that a second
host (Home) consumes it. No new screens, no new APIs, no new database
state — DataStore Preferences is the only persistence surface and it
already exists.

The plan is structured to land in a single MVP slice (US1 + US2 + US3 +
US4 ratified together) because the underlying composable already exists
and the spec's requirements compose into one coherent change set.

---

## Technical Context

**Language/Framework**: Kotlin 2.2.10 / Jetpack Compose (Material 3)
**Primary Dependencies**: shipped — Hilt + KSP, Jetpack DataStore
Preferences, Navigation-Compose, `androidx.lifecycle.runtime.compose`,
M3 `DropdownMenu` + `DropdownMenuItem`. **No new dependencies.**
**Platform**: Android only — min SDK 31, target SDK 36, Java 11 (per
`app/build.gradle.kts`).
**Database**: N/A — client-only feature; Jetpack DataStore Preferences
is the persistence surface.
**Testing**: JUnit + kotlinx-coroutines-test + Turbine (unit);
`androidx.compose.ui.test` (instrumented). No Supabase / RLS testing
required.
**State Management**: ViewModel (`LocaleViewModel` — already shipped)
exposing `language: StateFlow<Language>`; persistence via the singleton
`LanguagePreferenceRepository`. Per-screen `expanded: Boolean` for menu
visibility (`remember { mutableStateOf(false) }`).
**API Style**: N/A — client-only. The dropdown invokes no backend
endpoints (per spec § API Dependencies).

---

## Constitution Compliance Check

*GATE: Must pass before implementation can begin. Each item maps to a principle in `.momorph/constitution.md`.*

- [x] **I. Clean Code & Source Organization** — `LanguageSelector`
      currently lives at `auth/login/ui/components/LanguageSelector.kt` for
      historical reasons. This plan promotes it to `core/locale/ui/` so
      every chrome surface (Login + Home + future) consumes it from a
      neutral location. Composable stays under the 150-LOC ceiling
      (current is ≈ 137). No new package layout invented — same
      `core/locale/` slice that hosts `LanguageProvider` and the
      repository.
- [x] **II. Tech Stack Best Practices** — `Language.entries` becomes
      `[VN, EN]` (immutable enum); `LanguagePreferenceRepository` stays a
      `@Singleton` with a `Flow<Language>` and a `suspend set(...)`;
      `LocaleViewModel.language` is `StateFlow` via `stateIn(Eagerly)`;
      DataStore key/value contract is unchanged (key
      `"language_code"`, value = `Language.code`). No new dependency
      versions to pin.
- [x] **III. Material Design 3 (Android)** — anchor uses the existing
      `DropdownMenu` API; `selected = true` for the active row uses M3
      defaults (no custom check glyph per spec § Out of Scope); 48dp
      touch-target enforced via `Modifier.heightIn(min = 48.dp)` on each
      `DropdownMenuItem` and on the anchor pill (anchor visual is 32dp
      tall but the click region is extended); dynamic colour + light/dark
      already inherited from `AIDDProjectTheme`.
- [x] **IV. OWASP Secure Coding** — no PII / token / network surface; no
      new logging; the existing `SecureTimberTree` continues to scrub
      anything that *would* leak. RLS is N/A (no backend table).
- [x] **V. Test-Driven Development** — failing
      `LanguagePreferenceRepositoryTest` cases for the orphaned-JA-token
      fallback land **before** the decoder change; failing
      `LanguageSelectorTest` cases for the contentDescription recompute,
      the JA absence from the runtime row list, and the TalkBack-focus
      contract land **before** the composable touches.

**Violations**: none.

| Violation | Justification | Alternative Rejected |
|-----------|---------------|---------------------|
| (none) | — | — |

---

## Architecture Decisions

### Frontend (Jetpack Compose)

- **Component move**: `LanguageSelector.kt` lifts from
  `app/src/main/java/com/example/aiddproject/auth/login/ui/components/`
  to `app/src/main/java/com/example/aiddproject/core/locale/ui/`.
  Justification: Login owns the auth flow; the dropdown is now consumed
  by Home and any future chrome surface, and the spec § Notes called
  this out explicitly. Move uses Kotlin's package-rename refactor — no
  behaviour change. Login + Home call sites adopt the new import path
  in the same commit.

- **`Language.entries` shape change**: drop `JA`. Result:
  `entries = [VN, EN]` with the same `code`/`tag`/`nativeName`/`flagEmoji`
  fields. `Language.Default` stays `VN`. `Language.fromCode(...)` already
  returns `Default` for unknown codes — that contract is **already met**
  by the existing implementation, so the orphaned-JA fallback (Edge Cases —
  DataStore unavailable or corrupted) needs no change to that function;
  what changes is that JA is no longer one of the recognised codes.
  *Behaviour gap to verify in tests*: the existing
  `LanguagePreferenceRepositoryTest` doesn't assert the unknown-code
  branch — we add cases covering both a JA token and a generic unknown
  token (e.g. `"FR"`) returning VN.

- **Anchor contentDescription recompute (TR-003)**: the anchor's a11y
  label is built via `stringResource(R.string.a11y_language_selector,
  selected.nativeName)`. `stringResource` is Compose-keyed so it
  recomposes whenever `selected` changes — the existing implementation
  is correct; the plan adds a **regression test** that asserts the
  contentDescription text changes when the language flips, so a future
  regression to a non-keyed sibling is caught.

- **TalkBack initial focus on open (TR-003)**: M3 `DropdownMenu` does
  not auto-move focus to the first item when expanded. The plan adds
  a `LaunchedEffect(expanded)` keyed on the menu visibility that, when
  `expanded == true`, calls `focusRequester.requestFocus()` on the first
  row's `Modifier.focusRequester(...)`. Wrapping the requester in
  `remember { FocusRequester() }` keeps it stable across recomposition.
  Tests use `assertIsFocused()` against the VN row test tag.

- **Keyboard Tab order (TR-003)**: M3 `DropdownMenu` already handles
  visual order (top-to-bottom). The plan does not need a custom
  `Modifier.focusOrder` — instead the test asserts the order matches
  visual order (anchor → VN → EN) via `requestFocus()` chains.

- **Single-click guard on rows (TR-004)**: each `DropdownMenuItem`'s
  `onClick` is wrapped in `rememberSingleClickHandler { onSelect(lang) }`.
  Anchor click already has a guard from Phase 11 (or rather, the
  current code uses `clickableWithoutRipple` — we add the guard wrapper).

- **Idempotent reselection (TR-005)**: the existing `LanguageSelector`
  already short-circuits via `if (lang != selected) onSelect(lang)` —
  the parent's `onSelect` (and therefore `LocaleViewModel.setLanguage`)
  is never invoked when the user re-taps the active language. The
  reselection contract is verified by the
  `reselecting_active_language_is_idempotent` instrumented test below;
  no separate `LocaleViewModel` test is needed because (a) the VM lives
  inside `LanguageProvider.kt` with no dedicated test file today, and
  (b) the UI guard is the actual contract — the VM call simply never
  happens. As defence in depth, DataStore's `edit { ... }` semantics
  de-duplicate value writes for identical preferences automatically, so
  even if the UI guard regressed the worst case is a redundant write,
  not a recomposition storm.

- **Predictive back gesture (Edge Cases)**: M3 `DropdownMenu` already
  collaborates with `onDismissRequest` — we don't need to add a custom
  `BackHandler`. The plan asserts via instrumented test that the menu
  closes on `pressBack()` when expanded.

### Backend (N/A)

The Language dropdown is a client-only feature. There is no API contract
to define and no Supabase migration to author.

### Integration Points

- **Existing services to reuse (no changes other than the JA removal)**:
  - `LanguageProvider` (in `core/locale/`) — paired
    `LocalConfiguration` + `LocalContext` overrides drive the
    single-recomposition locale switch (TR-001). Untouched by this plan.
  - `LocaleViewModel` (in `core/locale/`) — Hilt VM, exposes
    `language: StateFlow<Language>` and `setLanguage(Language)`. Plan
    adds a UI-side equality short-circuit but the VM is otherwise
    unchanged.
  - `LanguagePreferenceRepository` — DataStore Preferences-backed,
    already implements the unknown-code-returns-VN contract via
    `Language.fromCode(...)`. Plan adds tests, no behaviour change.
  - `rememberSingleClickHandler` (in `core/ui/`) — TR-005 click guard,
    already shipped from Home Phase 5.

- **String resources (existing + new)**:
   - `a11y_language_selector` (already shipped in all three locales) is
     the anchor's contentDescription template.
   - Row contentDescriptions read from `Language.nativeName` (no new
     string resource for that).
   - The 2-letter codes "VN", "EN" stay `translatable="false"` in
     `values/strings.xml`.
   - **NEW**: `a11y_dropdown_expanded` and `a11y_dropdown_collapsed` —
     two short strings used as the anchor's `stateDescription` for
     TalkBack (TR-003). **Localized** (NOT brand-fixed) — TalkBack
     reads `stateDescription` literally without translation, so a
     Vietnamese user must hear a Vietnamese state announcement. Values:
      - `values/strings.xml` (VN authoritative): `a11y_dropdown_expanded
        = "đã mở"`, `a11y_dropdown_collapsed = "đã đóng"`.
      - `values-en/strings.xml`: `"expanded"`, `"collapsed"`.
      - `values-ja/strings.xml`: not added (JA is out of scope per
        spec § Out of Scope; `values-ja` stays as a dead resource only
        for keys that already exist there).

- **`values-ja/strings.xml`**: stays on disk as a dead resource for
  one release cycle (per spec § Out of Scope); this plan removes it
  from `StringResourceParityTest`'s active locale set so the test
  doesn't flag it as missing translations relative to a JA runtime that
  no longer exists.

- **Telemetry tag**: `LanguageTelemetry` (matches the existing
  `HomeTelemetry` pattern in `HomeViewModel`). Used by
  `LanguagePreferenceRepository` on a `dataStore.edit { ... }` failure
  to fire a `Timber.tag(LanguageTelemetry).w(error,
  "language.write.failure")` breadcrumb. No new logging library — the
  shipped `SecureTimberTree` already scrubs token-shaped values; this
  surface produces none.

---

## Project Structure

### Documentation (this feature)

```text
.momorph/specs/uUvW6Qm1ve-iOS-Language-dropdown/
├── spec.md              # Feature specification (existing, ratified 2026-05-08)
├── plan.md              # This file
└── tasks.md             # To be generated by /momorph.tasks
```

`research.md` is intentionally NOT produced — research findings are
inlined in this plan's Architecture Decisions section, mirroring the
Home plan's "research findings inlined" pattern.

### Source Code (affected areas)

```text
app/src/main/java/com/example/aiddproject/
├── core/
│   └── locale/
│       ├── Language.kt                                  # MODIFIED — drop JA from entries
│       ├── LanguagePreferenceRepository.kt              # MODIFIED — wrap dataStore.edit in runCatching
│       │                                                  + emit `LanguageTelemetry` Timber breadcrumb on
│       │                                                  write failure (spec TR-005). Decoder side is
│       │                                                  already correct via Language.fromCode(...).
│       ├── LanguageProvider.kt                          # NO CHANGE
│       └── ui/
│           └── LanguageSelector.kt                      # MOVED + MODIFIED — see "LanguageSelector changes" below
├── auth/login/ui/
│   ├── LoginScreen.kt                                   # MODIFIED — import path update only
│   └── components/
│       └── LanguageSelector.kt                          # DELETED — moved to core/locale/ui/
└── home/ui/components/
    └── HomeHeader.kt                                    # MODIFIED — import path update only

app/src/main/res/values-ja/                              # NO CHANGE — file stays as dead resource
                                                          for one release cycle (spec § Out of Scope)

app/src/test/java/com/example/aiddproject/
├── core/locale/
│   └── LanguagePreferenceRepositoryTest.kt              # MODIFIED — see "Existing tests broken by JA removal"
└── home/ui/
    └── HomeViewModelTest.kt                             # MODIFIED — see "Existing tests broken by JA removal"

app/src/androidTest/java/com/example/aiddproject/
├── auth/login/ui/components/
│   └── LanguageSelectorTest.kt                          # DELETED — moved to core/locale/ui/
├── core/locale/ui/
│   └── LanguageSelectorTest.kt                          # NEW (moved + rewritten) — see "Phase 1 test list"
└── home/
    └── HomeLocaleSwitchTest.kt                          # NO CHANGE — only references Language.EN
```

### Existing tests broken by JA removal

A grep audit (`grep -rn "Language\.JA\|menuItemTag(Language\.JA)" app/src/`) at
the start of Phase 2 surfaces three test files that hard-code
`Language.JA` and will fail to compile after the enum value is removed.
Each is updated in lockstep with the production change so the build
never regresses:

| File | Lines | Required edits |
|------|-------|----------------|
| `app/src/androidTest/.../auth/login/ui/components/LanguageSelectorTest.kt` | 58, 62 | Remove `assertIsDisplayed()` for the JA row + native name; the file moves to `core/locale/ui/` so the edits land alongside the move. |
| `app/src/test/.../home/ui/HomeViewModelTest.kt` | 228, 230, 317, 319 | The two test methods (`language flow propagates persisted preference into uiState` and `language StateFlow is collected eagerly`) currently exercise JA. Replace with `Language.EN` so the propagation contract is still asserted with a non-default value. |
| `app/src/test/.../core/locale/LanguagePreferenceRepositoryTest.kt` | 60–61, 94 | Existing `set(Language.JA)` round-trip + `Language.fromCode("JA")` mapping become invalid. Replace with `Language.EN` round-trip; add new `fromCode("JA") = Default` (= VN) case to lock the orphaned-token contract from spec § Edge Cases. |

### LanguageSelector changes (file move + edits)

The component MOVES from `auth/login/ui/components/LanguageSelector.kt`
to `core/locale/ui/LanguageSelector.kt`. While in transit, it gets four
contract additions:

1. **JA row removal** — `Language.entries.forEach { lang -> ... }` already
   iterates whatever `entries` contains, so dropping `JA` from the enum
   automatically removes the row. No code change inside the loop.
2. **Anchor 48dp click region** — the visual pill stays 32dp tall (Figma
   anchor height). Wrap the existing 32dp `Row` inside a parent `Box`
   that applies `Modifier.heightIn(min = 48.dp)` and absorbs the click
   (move `clickableWithoutRipple` + `singleClickGuard` to the Box). The
   anchor's visual height is unchanged; the touch region grows by ≈ 8dp
   on top and bottom. Tested via `HomeTouchTargetTest`-style assertion
   that anchor `getBoundsInRoot().height >= 48.dp`.
3. **`Role.Button` + expand/collapse semantics** — anchor's `semantics`
   block adds `role = Role.Button` and `stateDescription =
   stringResource(if (expanded) R.string.a11y_dropdown_expanded else
   R.string.a11y_dropdown_collapsed)`. The two new strings are
   brand-fixed (translatable=false) and live in `values/strings.xml`.
   Each `DropdownMenuItem` already carries `Role.Button` via M3 default.
4. **TalkBack focus-on-open** — `remember { FocusRequester() }` attached
   to the first row's `Modifier.focusRequester(...)`; a
   `LaunchedEffect(expanded)` calls `focusRequester.requestFocus()` when
   `expanded` flips from false to true. The effect intentionally does
   NOT request focus on close so keyboard/TalkBack focus returns to the
   anchor naturally per M3 default.

All existing test tags (`TEST_TAG_ANCHOR`, `TEST_TAG_MENU`,
`menuItemTag(lang)`) carry forward unchanged so call-site updates are
import-path-only.

### Dependencies

| Package | Catalog alias | Version | Purpose |
|---------|---------------|---------|---------|
| (none) | — | — | No new dependencies. |

All required libraries are already pinned in `gradle/libs.versions.toml`
from prior phases.

---

## Implementation Strategy

### Phase Breakdown

#### Phase 1: Tests First (Constitution Principle V)

Failing tests committed before any production code change. The list below
covers every FR / TR / Edge Case from the spec that needs UI-level
verification — anything not asserted here is locked by a unit-level
contract test elsewhere.

##### `LanguagePreferenceRepositoryTest` (unit)

- **`first_launch_no_preference_returns_VN_default`** — fresh DataStore
  with no key written, assert `language.first() == Language.VN`. Covers
  FR-002 (the brand-mandated VN default for fresh installs in every
  locale, per spec § Resolved Questions Q2).
- **`orphaned_JA_token_returns_VN_default`** — write `"JA"` directly
  into the underlying preferences (bypassing the typed `set()`), assert
  `language.first() == Language.VN`. Covers spec § Edge Cases —
  DataStore unavailable or corrupted.
- **`unknown_code_returns_VN_default`** — write `"FR"` directly, assert
  the same VN fallback. Locks `Language.fromCode(...)`'s default branch.
- **`set_writes_via_Language_code_token_not_name_or_ordinal`** — set
  `Language.EN`, read the underlying `Preferences[KEY]` raw, assert it
  equals `"EN"` (NOT `"English"` and NOT `"1"`). Locks the
  serialization contract from spec § Key Entities. **Regression test —
  the repository already serializes via `code`; this case prevents a
  future enum reshuffle from silently breaking the persisted format.**
- **`write_failure_emits_telemetry_breadcrumb_and_keeps_flow_unchanged`**
  — inject a DataStore stub whose `edit { ... }` throws; assert
  `repository.set(Language.EN)` returns without rethrowing, the
  `language` flow does not advance off VN, and a `LanguageTelemetry`
  Timber breadcrumb fired (verified via a test `Timber.Tree` planted
  in the test rule). Covers spec § Edge Cases — DataStore write failure
  + TR-005.

##### `LanguageSelectorTest` (instrumented, in the new `core/locale/ui/` path)

- **`menu_renders_exactly_VN_then_EN_no_JA`** — opens the menu,
  asserts only two `DropdownMenuItem`s render in the documented order,
  no JA tag exists. Covers FR-004.
- **`selecting_EN_invokes_onSelect_and_updates_anchor_label`** — FR-005
  happy path. Drives a stateful wrapper that owns the `selected`
  variable; opens the menu, taps the EN row, asserts (a) `onSelect(EN)`
  fired exactly once, (b) the menu closed, (c) the anchor's visible
  label flipped from "VN" to "EN" within the same composition. Carries
  forward the existing `selecting_EN_emits_callback_and_updates_visible_label`
  test from the pre-move location into the new `core/locale/ui/` test
  path.
- **`contentDescription_recomputes_when_language_flips`** —
  drives `selected = VN → EN` via the wrapper's mutable state, asserts
  the anchor's `contentDescription` advances from "Ngôn ngữ, Tiếng
  Việt, danh sách thả xuống" → "Ngôn ngữ, English, danh sách thả
  xuống". Covers TR-003 anchor recompute.
- **`anchor_exposes_role_button_with_expanded_state`** — when the menu
  is closed, asserts anchor's `Role.Button` + `stateDescription`
  matches `a11y_dropdown_collapsed`; opens the menu, asserts state
  flips to `a11y_dropdown_expanded`. Covers TR-003 expand/collapse
  contract.
- **`opening_menu_focuses_first_row_for_TalkBack`** — taps the anchor,
  asserts the VN row reports `assertIsFocused()`. Covers TR-003 initial
  focus.
- **`keyboard_tab_order_is_anchor_then_VN_then_EN`** — `requestFocus()`
  on anchor, `pressKey(Tab)` advances focus through VN then EN in
  visual order. Covers TR-003 keyboard nav.
- **`anchor_meets_48dp_touch_target`** — `getBoundsInRoot().height`
  on the anchor node ≥ 48dp despite the 32dp visual pill. Covers TR-008.
- **`each_row_meets_48dp_touch_target`** — `getBoundsInRoot()` on each
  `DropdownMenuItem` ≥ 48dp on both axes. Covers TR-008.
- **`reselecting_active_language_is_idempotent`** — selects VN while
  VN is already active, asserts `onSelect` callback is not invoked.
  Covers FR-006 + TR-005 UI-side.
- **`menu_double_tap_yields_one_select_callback`** — two rapid taps on
  EN row produce exactly one `onSelect(EN)` call. Covers TR-004.
- **`anchor_double_tap_yields_one_open`** — two rapid taps on the
  anchor leave the menu in the *open* state with exactly one
  `expanded = true` transition (asserted via a callback counter
  wrapping the open lambda). Covers TR-004 anchor side.
- **`second_tap_on_anchor_closes_menu`** — open the menu, tap the
  anchor again, assert the menu is no longer displayed AND the active
  language is unchanged. Covers FR-008 first dismiss path.
- **`outside_tap_closes_menu_without_changing_language`** — open the
  menu, tap a sibling node outside the menu bounds, assert the menu
  closes and `onSelect` was not invoked. Covers FR-008 second dismiss
  path.
- **`predictive_back_dismisses_menu_without_popping_parent`** —
  expanded menu + `pressBack()` → menu closes, parent composable still
  composed. Covers FR-008 third dismiss path + spec § Edge Cases —
  predictive back gesture.

##### Deliberately NOT tested in Phase 1

The following spec edge cases are **acceptable-as-is** and don't get
their own Phase-1 test; they are captured here so the omission is
intentional rather than overlooked:

- **Dark Mode toggle while menu is open** — handled entirely by M3
  theme infrastructure; toggling the system theme propagates through
  `MaterialTheme` without re-creating the menu. Visual smoke in Phase 4
  (open menu in light mode → toggle to dark → menu remains open with
  the new colours) is sufficient.
- **Rotation while menu is open** — spec § Edge Cases explicitly
  permits the menu to close on rotation; the persisted preference is
  unaffected because the rotation does not invoke
  `LanguagePreferenceRepository.set(...)`. Asserting "menu collapses"
  on rotation is a Compose-default behaviour (the menu's `expanded`
  state is `remember` not `rememberSaveable`), not a contract the
  feature itself owns.
- **Tap during dismiss animation** — M3 `DropdownMenu` resolves race
  conditions internally; the spec-acknowledged outcome is "menu
  dismisses, no select fires", which the framework already guarantees.

#### Phase 2: Foundational

- **Move `LanguageSelector.kt`**: relocate from
  `auth/login/ui/components/` to `core/locale/ui/`. Update both Login
  and Home import paths. Delete the old file.
- **Drop `JA` from `Language.entries`**: edit `Language.kt`. Remove the
  `JA(...)` enum value. `Language.fromCode("JA")` will fall through to
  `Default = VN` (already implemented).
- **Update `LanguagePreferenceRepository` doc comment**: replace the
  example unsupported code (`"FR"`) with `"JA"` so the comment matches
  the new reality, but no behaviour change.

#### Phase 3: Core Features (US1 + US2 + US3 + US4)

These four user stories collapse into a single phase because the
underlying work overlaps almost entirely (the same composable, the same
repository, the same view model). Implementation is one vertical slice:

- **`LanguageSelector` rewrite (in the new path)**:
   - Keep the existing anchor + `DropdownMenu` shape.
   - Add a `FocusRequester` and a `LaunchedEffect(expanded)` that calls
     `requestFocus()` on the first row when the menu opens.
   - Wrap each row's `onClick` in `rememberSingleClickHandler`.
   - Wrap the anchor's `clickableWithoutRipple` in
     `rememberSingleClickHandler` (currently unguarded — bug fix).
   - Add `Modifier.heightIn(min = 48.dp)` to the anchor and to each
     row. Anchor visual stays 32dp; the click region extends.
   - Keep the `selected = (lang == selected)` parameter on
     `DropdownMenuItem` (M3 default selected-state visual).
   - Test tags stay the same (`TEST_TAG_ANCHOR`, `TEST_TAG_MENU`,
     `menuItemTag(lang)`) so existing tests carry forward with import-path
     updates only.

- **String resource cleanup**: drop `values-ja` from
  `StringResourceParityTest`'s active locale set. The Java file stays
  on disk per spec § Out of Scope but is no longer asserted against.

#### Phase 4: Polish

- **Login + Home screenshot smoke**: instrumented run on a connected
  device — open the dropdown on each host, verify VN + EN render in
  the expected order with no JA row visible.
- **Quality Gates**: `./gradlew lint ktlintCheck assembleDebug
  testDebugUnitTest compileDebugAndroidTestKotlin` — all green.

### Risk Assessment

| Risk | Probability | Impact | Mitigation |
|------|-------------|--------|------------|
| Orphaned JA token in production DataStore breaks first cold start | Medium | Medium | `Language.fromCode(...)` already falls back to VN. Tests added in Phase 1 lock this contract. |
| `LanguageSelector` file move breaks Login + Home builds | Low | Low | Same-commit import-path updates plus assembleDebug gate before any test runs. |
| **Existing tests hard-code `Language.JA`** — `LanguageSelectorTest`, `LanguagePreferenceRepositoryTest`, `HomeViewModelTest` will fail to compile when the enum value disappears | High | Medium | Update all three test files in the same commit as the enum change. The "Existing tests broken by JA removal" subsection enumerates each file + line so no edit is missed. CI catches anything overlooked because compile-time symbol resolution fails. |
| Predictive back interferes with the menu's own dismiss handler | Low | Low | Instrumented test in Phase 1 asserts back-pop is consumed by the menu and the parent stays. |
| Removing JA from `Language.entries` breaks an unrelated production code path that hard-coded `Language.JA` | Low | High | grep audit at the start of Phase 2 covers `app/src/main/` AND `app/src/test/` AND `app/src/androidTest/`. Production grep returned **zero hits** at plan-write time (only the selector itself iterates `Language.entries`). |
| Anchor 48dp click region regresses the visual 32dp pill | Low | Low | Wrapping `Box(Modifier.heightIn(min=48.dp))` keeps the inner `Row` at 32dp; the `clip(RoundedCornerShape(4.dp))` stays inside the visual Row, not the outer Box. Visual screenshot smoke in Phase 4 verifies. |
| DataStore write failure swallows silently with no audit trail | Medium | Low | The repository wraps `dataStore.edit { ... }` in `runCatching`. On failure, a `LanguageTelemetry`-tagged Timber breadcrumb fires (matching the `HomeTelemetry` pattern). Telemetry SDK swap is out of scope per Login Phase 7 carry-over. |
| `values-ja/strings.xml` still loaded by the system if device locale is `ja` (rare but possible) | Low | Low | The runtime never selects JA via the dropdown after this lands; the only way to land on JA strings is a system-locale match. Acceptable per spec § Edge Cases (resource-resolver fallback to default). One release cycle later, delete the file outright. |

### Estimated Complexity

- **Frontend**: Low — one composable, one enum, one repository test.
- **Backend**: N/A.
- **Testing**: Medium — six new instrumented test cases + two new unit
  test cases, plus the file moves. Most are mirrors of patterns Login
  and Home already use.

---

## Integration Testing Strategy

### Test Scope

- [x] **Component / Module interactions**: `LanguageSelector` ↔
      `LocaleViewModel` ↔ `LanguagePreferenceRepository`;
      `LanguageProvider` re-render triggered by language flow emit.
- [x] **External dependencies**: Jetpack DataStore Preferences (in-process,
      file-backed). Tested with the in-memory test rule.
- [x] **Data layer**: DataStore unknown-token decoding, idempotent
      writes.
- [x] **User workflows**: open dropdown on Login → choose EN → verify
      Login strings re-render → force-stop → relaunch → verify Login
      first frame is EN.

### Test Categories

| Category | Applicable? | Key Scenarios |
|----------|-------------|---------------|
| UI ↔ Logic | Yes | Anchor open/close; row select → repository write; reselection no-op. |
| Service ↔ Service | Yes | `LocaleViewModel` → `LanguagePreferenceRepository`. |
| App ↔ External API | No | Client-only feature. |
| App ↔ Data Layer | Yes | DataStore unknown-token fallback; orphaned JA migration. |
| Cross-platform | No | Android-only. |

### Test Environment

- **Environment type**: JVM unit (DataStore + repository) + Android
  emulator (Compose UI).
- **Test data strategy**: in-memory DataStore for unit tests; fresh
  composable per instrumented test.
- **Isolation approach**: each test constructs its own DataStore /
  composable; no shared state across tests.

### Mocking Strategy

| Dependency Type | Strategy | Rationale |
|-----------------|----------|-----------|
| DataStore Preferences | Real (in-memory) | The decoder under test IS DataStore + the repository; mocking would defeat the purpose. |
| `LocaleViewModel` | Test fake (stateless `MutableStateFlow<Language>` exposing the same API) | `hiltViewModel()` requires Activity context that the test rule doesn't expose; the fake is trivial and matches Login's existing pattern. |
| Compose host | Real `createComposeRule()` | Standard Compose UI test harness. |

### Test Scenarios Outline

1. **Happy Path**
   - [ ] Cold start with no preference → Login renders in VN.
   - [ ] Open dropdown → tap EN → menu closes, anchor shows EN, all
         localizable Login strings re-render in EN.
   - [ ] Force-stop and relaunch → Login first frame is EN.

2. **Error Handling**
   - [ ] DataStore contains orphaned `"JA"` value → first read returns
         VN, no crash.
   - [ ] DataStore contains an unknown code (e.g. `"FR"`) → first read
         returns VN.
   - [ ] DataStore write fails → menu closes, locale flow does not
         advance, no user-visible error toast.

3. **Edge Cases**
   - [ ] Tap anchor again while menu is open → menu closes, no
         selection change.
   - [ ] Reselect already-active language → menu closes, no callback
         fires, no DataStore write.
   - [ ] Predictive back gesture commit on Android 14+ → menu
         dismisses, parent stays.
   - [ ] Rotate while menu is open → menu closes (acceptable per
         spec); persisted preference unchanged.

### Tooling & Framework

- **Test framework**: JUnit 4 (unit), AndroidX Compose Test
  (instrumented), Turbine for Flow assertions, MockK for the
  `LocaleViewModel` fake when needed.
- **Supporting tools**: in-memory DataStore (`PreferenceDataStoreFactory`
  with a temp file in `@TempDir`).
- **CI integration**: same Quality Gates pipeline as Home (already
  pending from Login Q4 carry-over).

### Coverage Goals

| Area | Target | Priority |
|------|--------|----------|
| `LanguageSelector` composable + state machine | ≥ 95% | High |
| `LanguagePreferenceRepository` decoder branches | 100% (every `fromCode` branch covered) | High |
| `LocaleViewModel.setLanguage(...)` short-circuit | 100% | High |
| Cross-host integration (anchor on Login + on Home) | Spot test on each host | Medium |

---

## Dependencies & Prerequisites

### Required Before Start

- [x] `constitution.md` reviewed — v1.0.0.
- [x] `spec.md` ratified (review pass 2026-05-08; 4 questions resolved).
- [x] Login + Home shipped — provide the only two existing host
      surfaces that consume `LanguageSelector` today.
- [x] grep audit completed at plan-write time. Findings:
   - **Production code** — only `LanguageSelector.kt`'s
     `Language.entries.forEach { ... }` references `Language` — that
     loop adapts automatically when `JA` is removed.
   - **Test code** — three files reference `Language.JA` literally and
     are itemised in "Existing tests broken by JA removal" above.
   - **`menuItemTag(Language.JA)`** appears only inside
     `LanguageSelectorTest` (one location, two assertions). No other
     callers.
   No production-side breakage expected from the enum shrink.

### External Dependencies

- None. The Language dropdown is a client-only feature; no Supabase, no
  Google OAuth, no Coil, no third-party services touched.

---

## Next Steps

After plan approval:

1. **Run** `/momorph.tasks` to generate the task breakdown.
2. **Verify** the JA-removal grep audit returns clean (no out-of-scope
   `Language.JA` references) before any code edit.
3. **Begin** Phase 1 — write the failing tests first per Constitution
   Principle V.

---

## Notes

- This is the **fourth** phase of locale work in the project. Login
  Phase 3 introduced the original `LanguageSelector`; Home Phase 11
  reused it; this plan brings both call sites onto a single,
  spec-aligned, JA-free implementation in the canonical
  `core/locale/ui/` package.
- The plan deliberately collapses US1, US2, US3, and US4 into one
  implementation phase because the underlying work is a single vertical
  slice — splitting them buys nothing and risks intermediate broken
  states (e.g. anchor moved but rows not yet on the new focus
  contract).
- `research.md` is intentionally not produced — research findings are
  inlined in this plan's Architecture Decisions section, mirroring the
  Home plan's pattern (per `tasks.md` Note: research.md is "recommended,
  intentionally not produced — research findings inlined in plan").
- Visual specs for the anchor pill chrome and the dropdown surface are
  fetched at task-execution time via MoMorph `query_section` /
  `get_node` for the Node IDs in spec.md — not enumerated here per
  Constitution Principle II's "no guessed visual values" rule.
