# Feature Specification: Language Dropdown

**Frame ID**: `uUvW6Qm1ve`
**Frame Name**: `[iOS] Language dropdown`
**File Key**: `9ypp4enmFmdK3YAFJLIu6C`
**Created**: 2026-05-08
**Reviewed**: 2026-05-08
**Status**: Ratified — review questions resolved 2026-05-08

---

## Overview

The Language dropdown is the in-screen sub-flow that lets a user switch the
app's display language between **Vietnamese (VN)** and **English (EN)** (per
Figma `[iOS] Language dropdown`). It is anchored from a "language pill" control
in the header of every screen that shows brand chrome — at the time of writing
those screens are `[iOS] Login` (`8HGlvYGJWq`) and `[iOS] Home` (`OuH1BUTYT0`).

Tapping the pill opens a menu listing the supported languages with the
country flag and 2-letter code; tapping a row writes the choice to a
persisted preference and the entire current screen re-renders in the new
locale **without an Activity recreation**. The dropdown is not a route — it
overlays the parent screen and dismisses on outside tap, system back, or
selection.

**Target users**: every authenticated and unauthenticated user (the dropdown
is always reachable from any chrome that hosts the language pill, including
the unauthenticated Login screen — TC_LANGDD_ACC_001).

**Implementation platform**: Android only (Kotlin + Jetpack Compose +
Material 3) per the project constitution. The MoMorph frame is labelled
"[iOS]" because that is the source design language; on Android the dropdown
is rendered as a Material 3 `DropdownMenu` anchored to the language pill.

**Business context**: SAA 2025 ships in Vietnamese as the authoritative locale
with English as a secondary marketing locale for international Sun*-affiliated
audiences. The dropdown is the single user-facing surface that switches
between them. It MUST persist the choice across app restarts so the user is
not surprised by the locale resetting after a kill.

> **Design vs. existing code (Login + Home Phase 11)**: the shipped
> `LanguageSelector` component currently lists three languages — VN / EN /
> JA — backed by `Language.entries`. The Figma frame for this screen only
> shows VN + EN, and Q1 of the review pass resolved to **remove JA
> outright** (no feature flag). See § Out of Scope and § Resolved
> Questions for the migration contract.

---

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Switch the app to Vietnamese (Priority: P1) 🎯 MVP

A user reading English content taps the language pill, picks 🇻🇳 VN, and
the entire current screen re-renders in Vietnamese immediately.

**Why this priority**: VN is the authoritative locale for SAA 2025 and the
default brand voice; the ability to return to VN from any other locale is
the primary success path.

**Independent Test**: From a screen rendered in EN, open the dropdown,
choose VN. Verify every localizable string on the screen re-renders in
Vietnamese in the same composition tree (no Activity recreation, no white
flash).

**Acceptance Scenarios**:

1. **Given** the user is on Login with the app rendered in English,
   **When** the user taps the language pill and chooses 🇻🇳 VN,
   **Then** the dropdown closes, the pill anchor updates to "🇻🇳 VN", and
   every Login string (`login_description`, `login_copyright`, error
   snackbars, CTA `a11y_cta_idle`) re-renders in Vietnamese
   (TC_LANGDD_FUN_003 + TC_LANGDD_FUN_007).
2. **Given** VN is already the active language,
   **When** the user opens the dropdown,
   **Then** the VN row renders in its **active / selected** visual state and
   the EN row in its inactive state (TC_LANGDD_FUN_005).

---

### User Story 2 - Switch the app to English (Priority: P1)

A user reading Vietnamese content taps the language pill, picks 🇬🇧 EN, and
the entire current screen re-renders in English immediately.

**Why this priority**: international audiences who need English content
cannot be served otherwise. Same priority as US1 — they are mirror
operations.

**Independent Test**: From a VN-rendered screen, open the dropdown, choose
EN. Verify every localizable string re-renders in English in-place.

**Acceptance Scenarios**:

1. **Given** the user is on Login with the app rendered in Vietnamese
   (default for first launch — TC_LANGDD_GUI_002),
   **When** the user taps the language pill and chooses 🇬🇧 EN,
   **Then** the dropdown closes, the pill anchor updates to "🇬🇧 EN", and
   every Login string re-renders in English (TC_LANGDD_FUN_004 +
   TC_LANGDD_FUN_008).
2. **Given** EN is already the active language,
   **When** the user opens the dropdown,
   **Then** the EN row renders in its **active / selected** visual state
   and the VN row in its inactive state (TC_LANGDD_FUN_006). Mirrors
   US1 acceptance scenario 2.

---

### User Story 3 - Open and dismiss the dropdown (Priority: P1)

A user can deterministically open the dropdown, see the available languages,
and dismiss it without changing their selection.

**Why this priority**: the open/close affordance is the predicate for both
US1 and US2 — they are inert if the dropdown can't be opened or dismissed
cleanly. Also the dropdown must close on every reasonable dismiss gesture
so the parent screen stays usable.

**Independent Test**: Tap the language pill → dropdown opens. Tap the pill
again → dropdown closes. Open the dropdown, tap outside its bounds →
dropdown closes without changing the selection.

**Acceptance Scenarios**:

1. **Given** the user is on Login with the dropdown closed,
   **When** the user taps the language pill,
   **Then** the dropdown opens listing exactly two rows in the order
   "🇻🇳 VN" first then "🇬🇧 EN" (TC_LANGDD_FUN_001 + TC_LANGDD_FUN_010).
2. **Given** the dropdown is open,
   **When** the user taps the language pill again OR taps outside the
   dropdown bounds OR presses system back,
   **Then** the dropdown closes and the pill anchor still shows the
   previously-active language (TC_LANGDD_FUN_002).
3. **Given** the dropdown is open,
   **When** the user taps the row matching the *currently active* language,
   **Then** the dropdown closes without firing the locale-change side
   effect — the pill anchor and the screen content remain unchanged.

---

### User Story 4 - The choice persists across restarts (Priority: P2)

A user picks a language; the next time they open the app, the previously
selected language is the active one without any prompt.

**Why this priority**: persistence is critical for trust ("I told the app I
read English, why is it Vietnamese again?") but is not part of the dropdown
component itself — it sits in the shared `LanguagePreferenceRepository`. P2
because the dropdown still functionally works on first launch even if
persistence regresses.

**Independent Test**: From a fresh install, set language to EN, force-stop
the app, relaunch → Login renders in English from first frame.

**Acceptance Scenarios**:

1. **Given** the user has never opened the app before,
   **When** Login first renders,
   **Then** the language pill displays "🇻🇳 VN" and Login strings render
   in Vietnamese (TC_LANGDD_GUI_002 — VN is the default).
2. **Given** the user has previously selected EN and force-stopped the app,
   **When** the user re-launches,
   **Then** Login renders in English from first frame and the language
   pill shows "🇬🇧 EN".

---

### Edge Cases

- **Dropdown remains open during a parent navigation** (e.g. a 401 redirect
  while EN is selected and the menu is showing): the menu MUST be torn down
  with the host screen so the new screen never inherits a stranded
  DropdownMenu. The shared `popUpTo(GATE) { inclusive = true }` redirect
  pattern from Login → Home already handles this since the menu is
  composition-scoped.
- **System Dark Mode toggled while the menu is open**: re-applying the M3
  colour scheme must not close the menu, lose the selection, or recompute
  the persisted preference.
- **Configuration change (rotation, dynamic font scale)**: the menu state
  (open/closed) is `remember`-scoped to the parent screen and may collapse
  on rotation; the persisted language preference itself must NOT change.
  Acceptable for the menu to close on rotation.
- **No locale resource for a string in the chosen language**: the resource
  resolver falls back to the default (`values/strings.xml`) so Vietnamese
  copy shows through instead of the EN translation. This is a translation
  gap, not a dropdown bug — see EN placeholder strings in Login Phase 7 +
  Home Phase 11.
- **Locale change racing with an in-flight network call**: the network
  layer reads the locale at request build time. Switching locale mid-flight
  does not cancel the request; the response is still parsed against the
  schema, only the displayed copy uses the new locale.
- **DataStore unavailable or corrupted on first read**: if the DataStore
  Preferences file is missing, unreadable, or contains an unexpected token
  (e.g. a previously-persisted JA value after JA is removed from the
  enum), the repository MUST resolve to the **VN** default instead of
  throwing. The user sees the parent screen render in Vietnamese on first
  frame and the next successful write replaces the bad value.
- **DataStore write failure on selection** (rare — disk pressure, IO
  permissions): the repository's `set(language)` call fails. The dropdown
  MUST close on selection regardless, AND the in-memory locale flow MUST
  NOT advance — the parent screen stays on the previous locale so what
  the user sees matches what is persisted. Telemetry breadcrumb fires per
  the existing `HomeTelemetry` pattern. No user-visible error toast (the
  failure mode is internal-only and the user can re-tap).
- **Predictive back gesture on Android 14+**: while the predictive-back
  preview is active and the menu is open, the menu MUST treat the back
  commit as a dismiss (close the menu, do not pop the parent). If the
  user cancels the gesture, the menu remains open. This carries over the
  M3 `DropdownMenu`'s default predictive-back integration; no custom
  back-handler logic is required at the spec level.
- **Tap during dismiss animation**: if the user taps a row at the exact
  instant an outside-tap dismiss is in flight, M3 `DropdownMenu` resolves
  by completing the dismiss only — the row click is treated as outside
  the menu and the locale does not change. The single-click guard (TR-004)
  is composition-scoped to the row; once the row leaves composition the
  guard cannot re-fire. No extra spec contract is required.

---

## UI/UX Requirements *(from Figma)*

### Screen Components

| Component | Node ID | Description | Interactions |
|-----------|---------|-------------|--------------|
| **Dropdown anchor** (`mms_A_Dropdown-List`) | `6891:15595` | The language pill: country flag + 2-letter code (`'VN'` default). Sits inside the parent screen's header chrome (Login / Home). | Tap → toggles the menu open/closed. |
| **Vietnamese row** (`mms_A.1_tiếng Việt`) | `6891:15596` | One row in the dropdown body — Vietnam flag (🇻🇳) on the leading edge, "VN" label trailing. | Tap → set language=VN, close menu, fire locale-change side effect. |
| **English row** (`mms_A.2_tiếng Anh`) | `6891:15597` | One row in the dropdown body — English/UK flag (🇬🇧) on the leading edge, "EN" label trailing. | Tap → set language=EN, close menu, fire locale-change side effect. |

### Navigation Flow

- **From**: any screen that hosts the language pill — currently Login
  (`8HGlvYGJWq`) and Home (`OuH1BUTYT0`). The dropdown is anchored to the
  pill and overlays the parent screen.
- **To**: nowhere — selecting a language stays on the parent screen and
  re-renders its content in the new locale. There is no destination route.
- **Triggers**:
  - Open: tap on the pill anchor.
  - Close: tap on the pill anchor again, tap outside the menu, press
    system back, or tap a language row.
  - Persist: the locale-change side effect writes the choice through
    `LanguagePreferenceRepository` (shared from Login).

### Visual Requirements

- The menu uses the M3 `DropdownMenu` surface — pixel values, gaps, and
  motion tokens are fetched on demand at implementation time via MoMorph
  `query_section` for the node IDs above. This spec does not enumerate
  visual values.

> Touch-target sizing for the anchor + each row is captured as a
> behavioural requirement under TR-008 (Constitution Principle III) and
> intentionally does not appear here.

---

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST display the dropdown anchor in every screen
  header that ships the language pill. At minimum this covers Login
  (TC_LANGDD_ACC_001) and Home; future screens that host chrome MUST reuse
  the same shared component.
- **FR-002**: System MUST default the active language to **VN** on first
  launch when no preference has been persisted (TC_LANGDD_GUI_002).
- **FR-003**: Tapping the anchor MUST toggle the menu's open/closed state
  (TC_LANGDD_FUN_001 + TC_LANGDD_FUN_002).
- **FR-004**: The menu MUST list **exactly two** rows — "🇻🇳 VN" first then
  "🇬🇧 EN" — with no other languages, separators, or section headers
  (TC_LANGDD_FUN_009 + TC_LANGDD_FUN_010). See **Out of Scope** for
  Japanese.
- **FR-005**: Selecting a row MUST (a) close the menu, (b) update the
  anchor to display that language's flag + code, (c) write the choice
  through `LanguagePreferenceRepository`, and (d) cause every Compose
  caller of `stringResource()` on the parent screen to recompute against
  the new locale within a single recomposition — no Activity recreation,
  no white flash (TC_LANGDD_FUN_007 + TC_LANGDD_FUN_008).
- **FR-006**: The currently active language's row MUST render in a
  visually distinct selected state when the menu is open (TC_LANGDD_FUN_005
  + TC_LANGDD_FUN_006). Tapping the already-selected row MUST close the
  menu without firing the locale-change side effect (no-op).
- **FR-007**: The persisted preference MUST survive process death — on the
  next launch the app MUST read the stored language from disk and render
  the first frame in that locale (User Story 4).
- **FR-008**: The menu MUST dismiss on system back, tap outside its
  bounds, AND a second tap on the anchor (User Story 3 acceptance scenario
  2). System back MUST NOT pop the parent screen while the menu is open.

### Technical Requirements

- **TR-001 (Performance)**: A locale change MUST take effect within a
  single recomposition pass — no full Activity recreation. The shipped
  `LanguageProvider` already enforces this via paired
  `LocalConfiguration` + `LocalContext` overrides (Login Phase 3, T056);
  this spec does not regress that constraint.
- **TR-002 (Persistence)**: The choice MUST be persisted via the shared
  `LanguagePreferenceRepository` (Jetpack DataStore Preferences) so
  every screen reads from one source. No per-screen language state.
- **TR-003 (Accessibility)**: Anchor and each row MUST surface a
  localized `contentDescription`:
   - Anchor: `a11y_language_selector` formatted with the current language's
     `nativeName` (e.g. "Ngôn ngữ, Tiếng Việt, danh sách thả xuống"). The
     description MUST recompute when the active language changes so
     TalkBack re-announces the new selection on the next focus.
   - Rows: the row's own native name + flag — TalkBack reads "Tiếng Việt"
     not "VN" because the 2-letter code is non-semantic.
   - Each row exposes `Role.Button` semantics; anchor exposes
     `Role.Button` with `expanded`/`collapsed` state where the platform
     supports it.
   - **Initial focus on open**: when the menu opens via tap, TalkBack
     focus MUST land on the **first** row (VN). Keyboard focus MUST be
     reachable through `Tab` from the anchor and follow visual order
     (VN → EN). System back / Escape MUST close the menu without
     advancing focus into the parent screen.
- **TR-004 (Double-tap suppression)**: Anchor and row clicks MUST be
  guarded by the shared `rememberSingleClickHandler` (Phase 5, T070) so a
  finger-bounce double-tap can never push two locale changes through the
  preference store within the same gesture window.
- **TR-005 (Reliability)**: Selecting the *already-active* language MUST
  be idempotent — the locale-change side effect MUST detect equality and
  short-circuit so DataStore is not written redundantly (avoids unnecessary
  re-compositions of every observer in the app). Selecting a *new*
  language but having the DataStore write fail MUST leave the in-memory
  locale flow unchanged (see Edge Cases — DataStore write failure).
- **TR-006 (i18n)**: All user-visible strings MUST come from a localizable
  resource. The 2-letter codes "VN" and "EN" are brand-fixed and stay in
  `values/strings.xml` (`translatable="false"`). The native names ("Tiếng
  Việt", "English") used in row a11y descriptions live on the `Language`
  enum's `nativeName`, alongside the existing flag emoji.
- **TR-007 (Security)**: No PII or token-shaped values are produced or
  consumed by this surface — TR-007 from Home (SecureTimberTree scrub) is
  inherited only by virtue of running inside the same app process; no new
  scrub keys are required.
- **TR-008 (Touch target)**: Anchor and each row MUST satisfy the
  platform-native minimum touch target mandated by Constitution Principle
  III (the same value Login + Home assert in their `TouchTargetTest`
  suites). This is a behavioural — not visual — constraint and is
  enforced via instrumented assertions, not pixel inspection.

### Key Entities

- **`Language` (enum)**: existing shared enum (Login Phase 3) with fields
  `code: String`, `nativeName: String`, `flagEmoji: String`,
  `toLocale(): java.util.Locale`. After this spec lands the enum's
  `entries` MUST be **exactly** `[VN, EN]` (in that order) and the
  `code` value set MUST be `["VN", "EN"]`. JA is removed (see § Out of
  Scope). Field examples:
   - `VN`: `code = "VN"`, `nativeName = "Tiếng Việt"`,
     `flagEmoji = "🇻🇳"`, `toLocale() = Locale("vi")`.
   - `EN`: `code = "EN"`, `nativeName = "English"`,
     `flagEmoji = "🇬🇧"`, `toLocale() = Locale("en")`.
- **`LanguagePreferenceRepository` (existing)**: Jetpack DataStore-backed
  store with `language: Flow<Language>` and `suspend set(Language)`. This
  spec does NOT add new methods; `set` already de-duplicates value writes
  via DataStore's edit semantics, but TR-005 also asks UI to short-circuit
  when the new value equals the current.
- **DataStore serialization contract**: the persisted token is
  `Language.code` (e.g. `"VN"`, `"EN"`) — NOT `Language.name` and NOT
  `Language.ordinal`. The decoder MUST treat any unknown token (including
  the orphaned `"JA"` value from existing installs) as a missing
  preference and fall back to `Language.Default` = VN (Edge Cases —
  DataStore unavailable or corrupted). Validation rule: token ∈ `{ "VN",
  "EN" }` after this spec lands.

---

## API Dependencies

The Language dropdown is a **client-only** feature — it persists exclusively
through Jetpack DataStore on the device and does not call any backend
endpoint. The choice is NOT round-tripped to Supabase or any other server,
nor used in request headers — the API contracts are locale-agnostic and
return values that the client renders against the locally-selected
locale's resource file.

| Endpoint / SDK call | Method | Purpose | Status |
|---------------------|--------|---------|--------|
| _(none)_ | — | The dropdown does not invoke any API. | _Not applicable_ |

---

## State Management

- **Global / app-scoped**:
  - `Language` (enum, single value) — exposed by `LanguagePreferenceRepository.language: Flow<Language>`
    and lifted into a `LocaleViewModel` (existing) for Compose
    consumption. **Default: VN** (set by the shared default in the
    repository when the DataStore key is absent).
- **Screen-local**:
  - `expanded: Boolean` — whether the M3 `DropdownMenu` is visible.
    `remember { mutableStateOf(false) }` inside the `LanguageSelector`
    composable. Reset on configuration change (acceptable per Edge Cases).
- **Cache / invalidation**:
  - The persisted preference is the single source of truth — no longer-lived
    in-memory cache. Every Compose caller of `stringResource()` resolves
    against `LocalContext.current` which `LanguageProvider` re-derives
    whenever the language flow emits.
- **Loading / transient state**:
  - DataStore reads are asynchronous, so the language `StateFlow` exposes
    `Language.Default` (VN) as its `initialValue` until the disk read
    resolves. The first frame on cold launch therefore renders in VN; if
    the persisted choice is EN, the locale flips silently within the
    same composition once the read completes. This is intentional — the
    spec does not require a loading skeleton because the worst-case
    user-visible flicker is a single recomposition with the same VN→VN
    or VN→EN string set the user already accepts at runtime.
- **Optimistic updates**: not applicable — the write is local and
  synchronous from the user's perspective. The dropdown closes on
  selection regardless of write outcome (see Edge Cases — DataStore write
  failure).

---

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: ≥ 99% of language switches re-render the parent screen's
  visible strings within **one frame** of the row tap on a Pixel 6 (≈ 16
  ms at 60 Hz) — no flash, no Activity recreation. This carries over
  from Login SC-004.
- **SC-002**: 0 instances of duplicate locale writes in production
  telemetry — TR-005's short-circuit on already-active language plus the
  TR-004 single-click guard combine to make this enforceable.
- **SC-003**: ≥ 99% of cold launches render the first frame in the
  language stored on disk (FR-007). On a fresh install this is VN by
  default (FR-002).

---

## Out of Scope

- **Japanese (JA) language**: the Figma frame `[iOS] Language dropdown`
  enumerates only VN + EN. The shipped Login + Home implementation
  currently includes a JA option in the `Language` enum and selector;
  this spec REMOVES JA from the supported set. The JA translation file
  (`values-ja/strings.xml`) MAY stay on disk as a dead resource for one
  release cycle so a re-enable is a one-line change, but the runtime
  must not surface the JA row, and `StringResourceParityTest` MUST drop
  `values-ja` from its active locale set.
- **Custom dropdown chrome**: the Figma rows show flag + 2-letter code
  only. No country names, no "selected" checkmark glyph, no separators.
  The selected-state visual is whatever colour treatment the M3
  `DropdownMenuItem` uses for `selected = true`.
- **Per-flow language overrides**: e.g. "set Login to EN but keep Home in
  VN". The preference is a single global value.
- **Server-side locale negotiation**: the choice is never sent to Supabase
  or any other backend. Server APIs return locale-agnostic data; the
  client renders it against the resource files matching the local pref.
- **Right-to-left languages** (Arabic, Hebrew): not in scope. Both VN and
  EN are LTR.
- **Programmatic locale change from outside the dropdown** (e.g. a debug
  menu or a deep link with `?lang=en`): the dropdown is the only sanctioned
  surface for changing the language.

---

## Dependencies

- [x] Constitution document exists (`.momorph/constitution.md`) — v1.0.0.
- [x] Login spec shipped (`8HGlvYGJWq`) — defines the original
  `LanguageSelector` placement.
- [x] Home spec shipped (`OuH1BUTYT0`) — confirms the same selector is
  reused on the Home header.
- [x] Screen flow updated (`.momorph/SCREENFLOW.md`) — this dropdown sits
  as a sub-flow of Login + Home; no standalone route.
- [ ] String parity test (`StringResourceParityTest`) extended to drop the
  JA locale-file key set if JA is not used at runtime.
- [ ] DataStore key migration: existing installs with JA persisted MUST
  fall back to VN on first launch after the JA removal lands. This is a
  one-line change in `LanguagePreferenceRepository`'s `from(string?)`
  decoder.

---

## Resolved Questions (review pass — 2026-05-08)

- **Q1 (JA scope)** — **Resolved: REMOVE**. The Figma frame enumerates
  only VN + EN, and the brand decision is to drop the JA option from
  `Language.entries`. Implementation must remove JA from the runtime
  enum; the existing `values-ja/strings.xml` resources may stay on disk
  as dead resources for one release cycle in case the decision is
  reversed (see § Out of Scope).
- **Q2 (Default language)** — **Resolved: VN globally**. VN is the
  brand-mandated default for fresh installs in **all** locales and
  regions (no auto-selection of EN based on the device's primary
  locale). This locks FR-002 + TC_LANGDD_GUI_002.
- **Q3 (Silent fallback for orphaned JA preference)** — **Resolved: silent
  fallback is acceptable**. Installs that previously persisted JA fall
  back to VN on next read with **no user-facing notification**. The
  fallback is documented in § Edge Cases and the Dependencies checklist.
- **Q4 (Telemetry on DataStore write failure)** — **Resolved: silent
  failure is acceptable**. No snackbar / toast / retry prompt — the user
  can re-tap. The repository writes a `HomeTelemetry`-equivalent
  breadcrumb but does not surface anything user-visible.

---

## Notes

- This is the third spec'd surface in the project (Login → Home → Language
  dropdown). It is the only one that is **not** a route — it overlays an
  existing screen.
- The shared `LanguageSelector` composable already lives in
  `app/src/main/java/com/example/aiddproject/auth/login/ui/components/`.
  The Language-dropdown feature plan (next mode) should propose lifting it
  to a more screen-neutral path now that a second host (Home) consumes it
  and the spec explicitly contemplates more screens hosting the pill.
- Acceptance scenarios on this spec all use Login as the example host
  because the Figma frame is anchored on the Login header. The behavior is
  identical when the same component is anchored on Home — every scenario's
  Given/When/Then survives substitution of "Login" with "Home".
- Visual specs (anchor pill chrome, dropdown surface, row chrome) for any
  task that needs pixel-level fidelity are fetched at task-execution time
  via MoMorph `query_section` / `get_node` for the Node IDs in the
  components table — not enumerated here.
