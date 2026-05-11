# Implementation Plan: Award Detail (Top Talent default)

**Frame**: `c-QM3_zjkG-iOS-Award-Top-talent`
**Date**: 2026-05-11
**Spec**: `spec.md` (ratified 2026-05-11)

---

## Summary

This plan ships the **Award Detail** screen — the authenticated read-only
page that replaces the placeholder at `Routes.AWARD_DETAIL_PATTERN`
(`route_award_detail/{awardId}`). It is the destination Home's awards
carousel already navigates to (wired in commit `14d93e5`) and also the
landing page for the bottom-nav **Awards** tab.

The screen is **parametric**: the category dropdown inside the Highlight
block lets the user switch between every award in `/awards`, the body
re-renders from the per-award payload, and the sticky chrome (header +
bottom nav) stays put. The Sun\*Kudos promo block at the bottom of the
body is a literal reuse of Home's existing `KudosSection` since both Chi
tiết CTAs funnel into the same destination (`Routes.KUDOS_OVERVIEW`,
Resolved Q3).

**Existing infrastructure to reuse** (≈70% of the screen's surface):

- Navigation: `Routes.AWARD_DETAIL_PATTERN` + `Routes.awardDetail(id)` already
  defined; `composable(...)` placeholder ready for replacement
  (`navigation/AppNavigation.kt:121-127`).
- Home's `AwardsRepository` interface + `SupabasePostgrestAwardsRepository`
  impl already query the `awards` Postgrest table; we **extend** them with
  a `detail(id)` method (Resolved Q7 + Q8 — implementer's discretion).
- `home/domain/Award` (id, name, thumbnailUrl, sortOrder) is the dropdown's
  list model **unchanged**; a new sibling `AwardDetail` model carries the
  detail-screen-only fields (description, quantity, quantity_unit,
  prize_value, image_url) so Home's "minimal Award" contract holds.
- `home/domain/states/AwardsState` is reused verbatim for the dropdown's
  category list state.
- `home/ui/components/`: `HomeHeader`, `HomeBottomBar`, `BellWithBadge`,
  `KudosSection`, `NotificationsSheet` are imported **as-is**. The
  "lift-to-`core/chrome/ui`" refactor is deferred to a follow-on (see
  § Risk Assessment "R4 — chrome reuse coupling"); not blocking this slice.
- `core/locale/ui/LanguageSelector` (moved into `core` by the Language
  Dropdown spec) is the header language pill.
- `core/auth/AuthRedirectController` + `SessionGate` handle US8 transparently;
  401s on `/awards/:id` route to Login via the existing pipeline.
- `core/ui/rememberSingleClickHandler` wraps every navigation and retry
  control (TR-004).

**Net new code is concentrated in** a new `awarddetail/` feature package
under `com.example.aiddproject` containing one ViewModel, one `LazyColumn`-
based stateless content composable, four small components (the Highlight
header with its category dropdown, the body's title row, the recipient/
prize info rows, the Coil-loaded badge image with placeholder fallback),
and one Hilt module that binds the repository extension. Plus a small
extension to the existing Home repository: a new `detail(id)` method on
`AwardsRepository` with both a Supabase implementation and a demo fake.

---

## Technical Context

**Language/Framework**: Kotlin 2.2.10 + Jetpack Compose + Material 3
**Primary Dependencies**: Hilt (DI), Supabase Kotlin SDK (`supabase-kt`),
Coil 3 (image loading), Timber (telemetry), `androidx.navigation.compose`,
DataStore Preferences (for locale via the existing `LanguagePreferenceRepository`)
**Database**: Supabase (Postgres) — `awards` table already in the schema
(used by Home carousel); RLS policies already enforce authenticated `SELECT`
**Testing**: JUnit4 + Compose UI Test + Robolectric (unit) + Espresso
(instrumented) + Hilt testing for VM/repository integration
**State Management**: per-feature `ViewModel` exposing `StateFlow<…UiState>`;
domain-specific sealed interfaces for sub-states (`AwardDetailState`,
re-uses `AwardsState` for the dropdown list); `SavedStateHandle` for the
`activeAwardId` per FR-001 + TR-005.
**API Style**: Direct Supabase Postgrest queries via the existing
repository pattern (no REST shim layer). The "`GET /awards/:id`" in spec
§ API Dependencies is the **conceptual** contract; on Android the same
query is realized as `supabaseClient.from("awards").select { filter { eq("id", awardId) } }.decodeSingle()`.

---

## Constitution Compliance Check

*GATE: Must pass before implementation can begin. Each item maps to a
principle in `.momorph/constitution.md`.*

- [x] **I. Clean Code & Source Organization** — new feature package
  `com.example.aiddproject.awarddetail/` with `ui/`, `data/`, `domain/`
  subpackages mirroring Home's layout. Composables stay <150 LOC each
  (the body splits into `AwardHeroBlock`, `AwardInfoBlock`,
  `RecipientCountRow`, `PrizeValueRow`, `HighlightBlock`). Kotlin official
  style enforced via `ktlint`.
- [x] **II. Tech Stack Best Practices** — immutable data classes
  (`AwardDetail`), `Flow`/`StateFlow` for async, repository pattern
  extending Home's `AwardsRepository`. Supabase client continues to use
  anon key only; all versions pinned via `libs.versions.toml`. The
  Postgrest detail query is a single `.decodeSingle()` call — no manual
  JSON walking.
- [x] **III. Material Design 3 (Android)** — `Scaffold` + `LazyColumn` +
  M3 `DropdownMenu` (custom-styled per Figma node `6885:10284` at
  task-execution time via `query_section`). 48dp touch targets, localized
  `contentDescription` on every control, `font scaling` respected (no
  hard-coded `dpToSp` ratios). `Role.Button` + `stateDescription` on the
  category dropdown trigger (TR-003).
- [x] **IV. OWASP Secure Coding** — TR-002 in the spec is satisfied by
  reusing Home's existing `SecureTimberTree` scrub for log writes;
  Supabase RLS on `awards` is **already enforced** by the existing
  policy (added during Home Phase 2). FR-004 surfaces only localized
  error copy — no exception text. No new secrets, no PII handling.
- [x] **V. Test-Driven Development** — every new public function in
  `data/`, `domain/`, and `ui/` ships with failing tests committed before
  the implementation. Coverage targets: contract tests (Postgrest detail
  query against test-double), VM state-machine tests (AwardDetailState
  transitions), Compose UI tests for the dropdown contract + sticky chrome
  + Chi tiết navigation.

**Violations (if any)**: None. No new dependencies, no constitution
amendments needed.

---

## Architecture Decisions

### Frontend (Jetpack Compose)

- **Component pattern**: feature-first under
  `com.example.aiddproject.awarddetail/`:
  - `ui/AwardDetailScreen.kt` — stateful entry point; owns `HiltViewModel`
    + `Scaffold` + handles auth-redirect side effects. <80 LOC.
  - `ui/AwardDetailScreenContent.kt` — stateless layout (preview-driven,
    UI test-driven). Composes the chrome (`HomeHeader`, `HomeBottomBar`
    imported from `home/ui/components/`) around a `LazyColumn` that
    renders KV banner, Highlight block, Award info block, Sun\*Kudos
    promo block.
  - `ui/AwardDetailUiState.kt` — root immutable UI state class.
  - `ui/AwardDetailViewModel.kt` — Hilt-injected; combines
    `observeAwards()` flow (for the dropdown) with a per-`activeAwardId`
    detail fetch; exposes `StateFlow<AwardDetailUiState>`.
  - `ui/components/HighlightBlock.kt` — section sub-label + title + the
    category dropdown anchored to Figma `6885:10284`. <120 LOC.
  - `ui/components/AwardCategoryDropdown.kt` — M3 `DropdownMenu` mirroring
    `LanguageSelector`'s contract (anchor pill + menu listing items
    + selected state + outside-tap dismiss + single-click guard).
    <150 LOC.
  - `ui/components/AwardHeroBlock.kt` — badge image (Coil with
    placeholder fallback per TR-007) + title row.
  - `ui/components/AwardInfoBlock.kt` — description paragraph +
    `RecipientCountRow` + `PrizeValueRow`. Each child <80 LOC.
- **State hoisting**: every component above the screen-level VM is
  stateless; the VM owns `StateFlow<AwardDetailUiState>` and emits new
  states via reducer-style methods (`onCategorySelected(id)`,
  `onRetry()`, `onAwardsScrollToTop()`).
- **Single source of truth**: per FR-001 + Resolved Q1, the
  `activeAwardId` lives in `SavedStateHandle` and is restored on
  configuration change. Cold-launch process-death does NOT restore — the
  screen falls back to FR-001's first-by-`sort_order` default (Edge
  Cases § "Process death + restore").
- **Loading / error / empty / populated**: domain-specific sealed
  interfaces in `awarddetail/domain/states/`:
  - `AwardDetailState`: `Loading | Loaded(AwardDetail) | Error(messageRes: Int)`
  - For the dropdown list, **reuse** the existing
    `home.domain.states.AwardsState` (it already has
    `Loading | Populated | Empty | Error`). No new sealed interface.
- **Out-of-order request cancellation**: per edge case "Network slow →
  fast race", the ViewModel cancels any in-flight detail fetch on
  `onCategorySelected` and re-issues a new one. Use
  `viewModelScope.launch { ... }.also { previous?.cancel() }` pattern.

### Backend (Supabase / Postgrest)

- **API surface — `awards` table** (already migrated for Home):
  - `list()` — already exists, used by Home carousel and now by the
    category dropdown. **Unchanged.**
  - `detail(id)` — **NEW**. Adds a single-row Postgrest query:
    `supabaseClient.from("awards").select(columns) { filter { eq("id", id) } }.decodeSingle<AwardDetailRow>()`.
    The `columns` projection includes `description`, `quantity`,
    `quantity_unit`, `prize_value`, `image_url` in addition to the
    `Award` columns the list query already pulls.
- **Schema**: spec § Deferred Q7 confirmed `detail(id)` is the chosen
  approach; the implementer SHOULD also confirm the SQL column names
  (`description`, `quantity`, `quantity_unit`, `prize_value`, `image_url`)
  match the live Supabase schema. The `.momorph/contexts/database-schema.sql`
  artefact is **missing** at the project level, so this confirmation
  happens against the production Postgres at task-execution time.
  Add migration entries only if columns are absent.
- **RLS**: the `awards` table's existing RLS policy
  (`authenticated.select`) already covers both `list()` and `detail(id)`
  since both go through the same `from("awards").select { ... }` API. No
  policy changes needed.
- **Caching**: per spec § State Management:
  - `observeAwards()` is already a shared `Flow` cached at the
    repository level for the session.
  - `detail(id)` SHOULD be cached in-memory per id with a short TTL
    (5 min) so dropdown round-trips don't re-hit the network. Use a
    simple `Mutex`-guarded `MutableMap<String, CachedAwardDetail>` in
    the repository, NOT a heavy library.

### Backend Localization (Deferred Q6)

The spec defers the locale mechanism to task-execution time. Two paths
are supported and the plan does NOT prescribe one:

- **Path A — per-locale columns**: `awards.name_vi`, `awards.name_en`,
  etc. The repository's `detail(id)` projects the column matching
  `LocaleViewModel.language.value.tag` (`"vi"` → `name_vi`).
- **Path B — `?locale=` query param**: pass the language tag via the
  Postgrest `headers { append("Accept-Language", lang.tag) }` mechanism
  and let the backend serve one locale per row.

Implementer picks whichever ships in production. The repository's
`detail(id)` signature `suspend fun detail(id: String, locale: Language): Result<AwardDetail>`
accommodates both — the `Language` arg is passed but interpreted by the
impl. Demo repository ignores the locale and returns hard-coded VN
strings.

### Integration Points

- **Navigation graph**: `navigation/AppNavigation.kt:121-127` already
  registers the `Routes.AWARD_DETAIL_PATTERN` route with a placeholder
  composable. We replace the placeholder body with a real
  `AwardDetailScreen(...)` invocation. The `awardId` argument extraction
  stays as-is. We add a Sun\*Kudos navigation callback that routes to
  `Routes.KUDOS_OVERVIEW` (same destination as the bottom-nav Kudos
  tab — Resolved Q3).
- **Home → Award Detail**: already wired via `HomeScreen.onAwardChiTietTap`
  (HomeScreen.kt:93) which calls `onNavigateToAwardDetail(award)` →
  `Routes.awardDetail(award.id)` in `AppNavigation.kt:103-105`. No change.
- **Bottom-nav Awards tab from non-Home screens**: not yet wired. The
  `HomeBottomBar.onTabSelect(HomeNavTab.Awards)` currently fires from
  `HomeScreen` and `HomeScreen` swallows it. For this screen, we wire
  the same callback to a no-op (US3 scenario 4 — already on Awards →
  scroll to top, see ViewModel `onAwardsTabRetap()`).
- **Sun\*Kudos block**: `home.ui.components.KudosSection` is imported
  with `onChiTietClick = { onNavigateToKudosOverview() }`. Per Resolved
  Q3 this is the same handler the Kudos bottom-nav tab fires. Both
  surfaces are unified in `AppNavigation`.
- **Header chrome**: `home.ui.components.HomeHeader` is imported with
  the same parameters Home passes (language, search, bell with badge).
  Bell badge count comes from
  `notificationsSummaryRepository.observe()` — same source as Home.
- **`AwardCard.onChiTietClick` from Home**: no change. It already
  bubbles the `Award` to `HomeScreen.onAwardChiTietTap`, which is wired
  in `AppNavigation` to `Routes.awardDetail(award.id)`.

---

## Project Structure

### Documentation (this feature)

```text
.momorph/specs/c-QM3_zjkG-iOS-Award-Top-talent/
├── spec.md       # Feature specification (ratified 2026-05-11)
├── plan.md       # This file
└── tasks.md      # Generated by /momorph.tasks (next step)
```

No `design-style.md` per project convention (visual specs fetched on
demand from MoMorph at task-execution time). No `research.md` —
codebase reconnaissance is folded into this plan (§ Integration Points
+ § Summary).

### Source Code (affected areas)

```text
app/src/main/java/com/example/aiddproject/
├── awarddetail/                                    # NEW feature package
│   ├── domain/
│   │   ├── AwardDetail.kt                          # NEW data class (id, name, description,
│   │   │                                           # quantity, quantity_unit, prize_value, image_url, sort_order)
│   │   └── states/
│   │       └── AwardDetailState.kt                 # NEW sealed interface (Loading|Loaded|Error)
│   └── ui/
│       ├── AwardDetailScreen.kt                    # NEW stateful entry point
│       ├── AwardDetailScreenContent.kt             # NEW stateless layout (full Scaffold from Phase 3)
│       ├── AwardDetailUiState.kt                   # NEW root UI state
│       ├── AwardDetailViewModel.kt                 # NEW HiltViewModel
│       └── components/
│           ├── HighlightBlock.kt                   # NEW section header + dropdown anchor
│           ├── AwardCategoryDropdown.kt            # NEW M3 dropdown (mirrors LanguageSelector contract)
│           ├── AwardHeroBlock.kt                   # NEW badge image + title row
│           ├── AwardInfoBlock.kt                   # NEW description + RecipientCountRow + PrizeValueRow
│           └── KvKudosBanner.kt                    # NEW static "KUDOS" banner (mms_A_KV Kudos)
├── home/                                           # MODIFIED — repository extension only
│   ├── data/
│   │   ├── AwardsRepository.kt                     # MODIFIED — add detail(id, locale) method
│   │   ├── SupabasePostgrestAwardsRepository.kt    # MODIFIED — implement detail(id) via
│   │   │                                           # supabaseClient.from("awards").select { eq("id", id) }.decodeSingle()
│   │   └── DemoAwardsRepository.kt                 # MODIFIED — fake detail(id) returning hard-coded VN payload
│   └── (HomeRepositoryModule.kt UNCHANGED)         # Existing Hilt module already binds AwardsRepository
└── navigation/
    └── AppNavigation.kt                            # MODIFIED — replace placeholder composable
                                                    # with AwardDetailScreen(...)

# Tests
app/src/test/java/com/example/aiddproject/
├── awarddetail/
│   ├── domain/
│   │   └── states/AwardDetailStateTest.kt          # NEW transition tests
│   └── ui/
│       └── AwardDetailViewModelTest.kt             # NEW state-machine + cancellation tests
└── home/data/
    └── SupabaseAwardsRepositoryDetailTest.kt       # NEW gateway-double tests for the new
                                                    # detail(id) method (lives alongside the
                                                    # existing SupabasePostgrestAwardsRepositoryTest)
app/src/androidTest/java/com/example/aiddproject/
└── awarddetail/
    ├── AwardDetailScreenTest.kt                    # NEW Compose UI tests (load/loading/error/retry,
    │                                               # dropdown integration, sticky chrome, Kudos Chi tiết nav)
    ├── AwardDetailTouchTargetTest.kt               # NEW 48dp contract per TR-003
    ├── AwardDetailFocusOrderTest.kt                # NEW TalkBack focus contract
    └── AwardCategoryDropdownTest.kt                # NEW dropdown contract tests
                                                    # (14 tests mirroring LanguageSelectorTest's T018–T031)
```

### Resource Files

```text
app/src/main/res/
├── values/strings.xml                # ADD new keys (see Phase 1 § Strings inventory)
├── values-en/strings.xml             # MIRROR new keys
├── values-ja/strings.xml             # MIRROR new keys (deprecation-window parity)
└── drawable/
    └── ic_award_badge_placeholder.xml  # NEW placeholder for null image_url (per FR-008 + TR-007)
```

### Dependencies

| Package | Version | Purpose |
|---------|---------|---------|
| (none — new) | — | All required libraries (Compose, Hilt, Supabase-kt, Coil, Timber, kotlinx.serialization) are already pinned in `libs.versions.toml`. **No additions.** |

---

## Implementation Strategy

### Phase Breakdown

#### Phase 0: Asset Preparation

Pull badge artwork + Kudos banner image + section icons from MoMorph using
`get_media_files` for the Node IDs in spec § Screen Components. Place
under `app/src/main/res/drawable-{mdpi,hdpi,xhdpi,xxhdpi}` (raster) or
`app/src/main/res/drawable/` (vector). Verify naming follows existing
patterns (`ic_*` for icons, `img_*` for images).

Specific assets:
- Award badge graphic for Top Talent (Figma node `6885:10313`) +
  any sibling award badges (the dropdown lists multiple categories — if
  the live `awards` table has rows pointing at external `image_url`s, we
  do NOT bundle them; only the **placeholder** for `image_url = null`
  ships in res/drawable).
- KV Kudos banner (`mms_A_KV Kudos`, `6885:10266`) — visual lockup
  (flame icon + "KUDOS" text). Should be reused from Home if already
  bundled; otherwise pull.
- Section icons: leading icon on the award title row (Figma
  `6885:10297` adjacent), recipient-count icon, prize-value icon.

#### Phase 1: Setup (Shared Infrastructure)

- Add localized strings to `values/strings.xml` (+ `values-en/` + `values-ja/`):
  - `award_detail_title` ("Hệ thống giải thưởng SAA 2025")
  - `award_detail_sub_label` ("Sun\* Annual Awards 2025")
  - `award_detail_quantity_label` ("Số lượng giải thưởng")
  - `award_detail_prize_label` ("Giá trị giải thưởng")
  - `award_detail_prize_caption` ("cho mỗi giải thưởng")
  - `award_detail_kudos_label` ("Phong trào ghi nhận")
  - `award_detail_kudos_section_title` ("Sun\* Kudos")
  - `award_detail_kudos_badge` ("ĐIỂM MỚI CỦA SAA 2025")
  - `award_detail_kudos_chi_tiet` ("Chi tiết") — may reuse `home_link_chi_tiet`
  - `award_detail_loading` ("Đang tải…") — may reuse `home_awards_loading`
  - `award_detail_error` ("Không tải được giải thưởng, vui lòng thử lại")
  - `award_detail_retry` ("Thử lại") — may reuse `home_action_retry`
  - `award_detail_placeholder_quantity` ("—")
  - `award_detail_placeholder_prize` ("—")
  - A11y labels: `a11y_award_detail_screen`,
    `a11y_award_category_dropdown` (formatted with current award name),
    `a11y_award_badge_image`.
- Confirm `Routes.AWARD_DETAIL_PATTERN` + `Routes.awardDetail(id)` exist
  (already shipped — see `navigation/Routes.kt:29-31`).
- Confirm `composable(Routes.AWARD_DETAIL_PATTERN)` registration at
  `navigation/AppNavigation.kt:121-127` — currently a placeholder.

#### Phase 2: Foundational (Blocking Prerequisites)

**Goal**: every type and contract needed by US1–US8 exists and compiles
with failing tests before any user-story slice starts.

- New domain model `AwardDetail` (data class) in
  `awarddetail/domain/AwardDetail.kt`.
- New sealed interface `AwardDetailState` in
  `awarddetail/domain/states/AwardDetailState.kt`.
- Repository extension: add `suspend fun detail(id: String, locale: Language): Result<AwardDetail>`
  to `home.data.AwardsRepository` (single shared interface for both
  list + detail — Resolved Q8). Implement in
  `SupabasePostgrestAwardsRepository` (new method) and `DemoAwardsRepository`
  (returns hard-coded VN Top Talent for any id; spec is read-only).
- Hilt binding for the new method lives in the existing
  `HomeRepositoryModule` — no new module needed.
- Failing unit tests written first (TDD per Constitution Principle V):
  - `SupabaseAwardsRepositoryDetailTest.detail_returns_full_payload_when_id_matches`
  - `SupabaseAwardsRepositoryDetailTest.detail_returns_failure_when_id_missing`
  - `SupabaseAwardsRepositoryDetailTest.detail_propagates_locale_to_query`
    (Path A or B per Resolved Q6; test asserts whichever is wired)
  - `SupabaseAwardsRepositoryDetailTest.detail_401_propagates_as_failure_for_AuthRedirectController`
  - `AwardDetailStateTest.transitions_Loading_to_Loaded_on_success`
  - `AwardDetailStateTest.transitions_Loading_to_Error_on_failure`
  - `StringResourceParityTest` (existing — at
    `app/src/test/java/com/example/aiddproject/core/locale/StringResourceParityTest.kt`):
    **runs unmodified** and must stay green once the Phase 1 strings
    land in `values/`, `values-en/`, and `values-ja/` (until the JA
    deprecation window closes — see the existing parity contract).
    No new test file; this is a regression gate on Phase 1.

**Checkpoint**: `./gradlew assembleDebug testDebugUnitTest
compileDebugAndroidTestKotlin` passes; domain + data tests are
red where implementation is still missing.

#### Phase 3: User Story 1 + User Story 8 — Body load + auth gate (Priority: P1) 🎯 MVP slice

**Goal**: navigating from Home tapping "Chi tiết" on a card opens this
screen with the **full Scaffold structure** (sticky `topBar` +
scrollable body + sticky `bottomBar`), the body loads, errors retry
cleanly, and an unauthenticated session bounces to Login through the
existing pipeline.

**Scaffold layout decision** (lands now, not deferred): Phase 3 wires
the full `Scaffold { topBar = HomeHeader, bottomBar = HomeBottomBar }`
shell from the start. Interactive header children (search / bell /
language pill) AND bottom-nav tabs are bound to **stub callbacks** that
no-op (or log) until Phases 5–6 wire them to real navigation handlers.
This ordering means:
- FR-014 (sticky chrome) is testable as soon as Phase 3 lands.
- Phases 5 + 6 add **behavior**, not **layout**.
- No mid-phase Scaffold rewrite that would invalidate the visual smoke.

US8 piggy-backs because the `AuthRedirectController` is already
collected at the NavHost level — the repository surfaces 401 as
`Result.failure` and the existing pipeline handles the bounce.

**Tests first**:
- `AwardDetailViewModelTest.load_emits_Loading_then_Loaded_on_success`
- `AwardDetailViewModelTest.load_emits_Loading_then_Error_on_failure`
- `AwardDetailViewModelTest.retry_re_issues_fetch_after_error`
- `AwardDetailViewModelTest.401_repository_result_emits_Error_state`
  (VM-level only — the navigation outcome is asserted separately at
  the integration layer per the AuthRedirectController collection
  pattern Login + Home already use)
- Compose UI: `AwardDetailScreenTest.body_renders_full_award_payload`,
  `AwardDetailScreenTest.loading_indicator_visible_while_fetching`,
  `AwardDetailScreenTest.error_state_shows_retry_button`,
  `AwardDetailScreenTest.retry_button_re_issues_fetch`,
  `AwardDetailScreenTest.null_image_url_renders_placeholder`,
  `AwardDetailScreenTest.null_quantity_and_prize_render_placeholders`,
  `AwardDetailScreenTest.sticky_header_stays_pinned_on_scroll`,
  `AwardDetailScreenTest.sticky_bottom_nav_stays_pinned_on_scroll`.

**Implementation** (full Scaffold + stateless components):
- `AwardDetailScreen` (stateful) wires the VM + the full `Scaffold` +
  replaces the placeholder at `AppNavigation.kt:121-127`. The Scaffold
  topBar imports `HomeHeader` from `home/ui/components/` with stub
  callbacks (`onSearchClick = {}`, `onBellClick = {}`,
  `onLanguageSelected = {}`); the bottomBar imports `HomeBottomBar`
  with `selected = HomeNavTab.Awards` and `onTabSelect = {}`. Stub
  callbacks become real in Phases 5–6.
- `AwardDetailScreenContent` (stateless) composes the body inside a
  `LazyColumn`:
  - `KvKudosBanner` (static — pulls from Phase 0 assets) — top of body
  - `HighlightBlock` (without the dropdown yet — dropdown lands in
    Phase 4; for Phase 3 the trigger renders the active award's name
    + chevron but tap is a no-op)
  - `AwardHeroBlock` (badge image with Coil + placeholder fallback per
    TR-007)
  - `AwardInfoBlock` (title row + description + `RecipientCountRow` +
    `PrizeValueRow`)
  - `KudosSection` (imported from `home/ui/components/`) — Sun\*Kudos
    promo block; Chi tiết callback already wires to a stub in Phase 3,
    real navigation lands in Phase 5
- `AwardDetailViewModel` exposing `StateFlow<AwardDetailUiState>` with
  `detail: AwardDetailState` + load/retry methods. `SavedStateHandle`
  receives the `awardId` nav argument automatically (Hilt wires the
  back-stack-entry args through). `init {}` launches a
  `viewModelScope` coroutine that:
  1. Reads `savedStateHandle.get<String>("awardId")`.
  2. If non-null, calls `repository.detail(awardId, locale).fold(...)`
     and emits `AwardDetailState.Loaded`/`Error`.
  3. If null (e.g. entered via bottom-nav Awards tab without a prior
     in-session selection), calls `repository.list()` to retrieve
     the catalogue, picks `first().id` by `sort_order`, writes it
     back to `SavedStateHandle["awardId"]`, then proceeds with the
     detail fetch. Per FR-001 + Resolved Q1.
  - The `list()` call is `suspend` so the fallback MUST run inside a
    coroutine — never synchronously in `init {}`. Use a
    `MutableStateFlow(AwardDetailUiState.empty)` initially and emit
    the populated state once the fallback completes.

**Checkpoint**: end-to-end smoke on the emulator — tap "Chi tiết" on
Top Talent in Home → Award Detail opens with full chrome → body
populated. Scroll the body → header + bottom nav stay pinned.
Disconnect network → tap Chi tiết → error state visible → Retry →
loading → populated. Sign out → re-launch + try Award Detail deep link
→ bounces to Login (US8 integration verified end-to-end on the device,
not via VM-level test).

#### Phase 4: User Story 2 — Category dropdown (Priority: P1)

**Goal**: the Highlight block's dropdown opens with every award from
`/awards` and the body re-renders to the selected category.

**Tests first** — 14 dropdown contract tests mirroring
`LanguageSelectorTest`'s T018–T031, plus 4 VM-level tests:

*Dropdown contract (14, matching Language Dropdown parity)*:
- `AwardCategoryDropdownTest.menu_renders_every_award_from_repository`
- `AwardCategoryDropdownTest.selecting_other_award_invokes_callback_and_updates_anchor`
- `AwardCategoryDropdownTest.content_description_recomputes_when_active_award_flips`
- `AwardCategoryDropdownTest.trigger_has_role_button_with_expanded_collapsed_state`
- `AwardCategoryDropdownTest.opening_menu_focuses_first_row_for_TalkBack`
- `AwardCategoryDropdownTest.keyboard_tab_order_is_anchor_then_rows`
- `AwardCategoryDropdownTest.trigger_meets_48dp_touch_target`
- `AwardCategoryDropdownTest.rows_meet_48dp_touch_target`
- `AwardCategoryDropdownTest.reselecting_active_award_is_idempotent_no_callback`
- `AwardCategoryDropdownTest.row_double_tap_yields_one_select_callback`
- `AwardCategoryDropdownTest.anchor_double_tap_yields_one_open_transition`
- `AwardCategoryDropdownTest.second_deliberate_tap_on_anchor_closes_menu`
- `AwardCategoryDropdownTest.outside_tap_dismisses_menu_without_changing_active`
- `AwardCategoryDropdownTest.predictive_back_dismisses_menu_without_popping_screen`

*ViewModel-level (state-machine behavior, not the dropdown widget)*:
- `AwardDetailViewModelTest.dropdown_select_cancels_in_flight_request_and_loads_new`
  (out-of-order race per Edge Cases — slow-then-fast B→C while A is
  still in flight, only C's payload reaches UI state)
- `AwardDetailViewModelTest.dropdown_select_idempotent_does_not_re_fetch`
- `AwardDetailViewModelTest.empty_awards_list_dropdown_renders_AwardsState_Empty`
  (Edge Case § "Empty awards list")
- `AwardDetailViewModelTest.single_award_dropdown_lists_one_row` (Edge
  Case § "Single award")

**Implementation**:
- `AwardCategoryDropdown` composable lives in
  `awarddetail/ui/components/AwardCategoryDropdown.kt`. Uses M3
  `DropdownMenu` with custom `containerColor` / `border` / `shape`
  per Figma `6885:10284` (values queried at task-execution time).
- ViewModel adds `onCategorySelected(id)`. On call:
  1. Cancel any previous detail fetch coroutine.
  2. If `id == activeAwardId`, return early (idempotent).
  3. Set `detail = AwardDetailState.Loading`.
  4. Update `activeAwardId` via `SavedStateHandle["activeAwardId"] = id`.
  5. Launch new fetch in `viewModelScope`.

**Checkpoint**: on the emulator — tap dropdown → all categories
listed → tap Top Project → menu closes, anchor flips to "Top Project",
body re-renders with Top Project's payload. Tap dropdown again → tap
Top Talent (active) → menu closes silently, no second fetch. Tap
dropdown → tap outside → menu closes, content unchanged.

#### Phase 5: User Story 3 + User Story 4 — Bottom nav + Sun\*Kudos Chi tiết (Priority: P1)

**Goal**: the bottom nav routes work, the Awards tab renders active, the
Sun\*Kudos Chi tiết button navigates to the same destination as the
Kudos bottom-nav tab (Resolved Q3).

**Tests first** (sticky-chrome tests already shipped in Phase 3; this
phase only adds the navigation-wiring tests):
- `AwardDetailScreenTest.tap_SAA_2025_tab_navigates_to_home`
- `AwardDetailScreenTest.tap_kudos_tab_navigates_to_kudos_overview`
- `AwardDetailScreenTest.tap_profile_tab_navigates_to_profile`
- `AwardDetailScreenTest.tap_awards_tab_while_active_scrolls_body_to_top`
- `AwardDetailScreenTest.tap_kudos_chi_tiet_navigates_to_kudos_overview`
- `AwardDetailScreenTest.kudos_chi_tiet_double_tap_yields_one_navigation`
- `AwardDetailScreenTest.kudos_banner_image_load_fail_renders_placeholder`

**Implementation** (Phase 5 wires callbacks; layout already exists from Phase 3):
- Replace the stub `onTabSelect = {}` from Phase 3 with the real
  routing lambda that maps `HomeNavTab.Awards → noop / scroll-to-top`,
  `SAA_2025 → home`, `Kudos → kudos overview`, `Profile → profile`.
- VM adds `onAwardsTabRetap()` — scrolls `LazyListState` to index 0
  (US3 scenario 4 per Resolved Q2).
- `KudosSection`'s `onChiTietClick` callback (already imported in
  Phase 3) is rewired from the Phase 3 stub to the real
  `onNavigateToKudosOverview` lambda. Per Resolved Q3 this lambda is
  the **same handler** the bottom-nav Kudos tab fires — declared
  exactly once in `AwardDetailScreen` and threaded through to both
  call sites.
- Wrap **every** navigation callback in `rememberSingleClickHandler`
  per TR-004 — including the Retry button (already wired in Phase 3
  with the single-click guard).

**Checkpoint**: emulator pass — all bottom-nav tabs route correctly,
Awards tab highlight is gold (visual fidelity verified against Figma
node `6885:10332`), Sun\*Kudos Chi tiết opens the Kudos overview.

#### Phase 6: User Story 5 + User Story 6 + User Story 7 — Header chrome (Priority: P2)

**Goal**: the language pill, bell-with-badge, and search icon all work
on this screen and behave identically to Home.

**Tests first** (integration only — the language pill and bell are
already covered by `LanguageSelectorTest` + `BellWithBadgeTest`; this
phase verifies wiring, not the component contracts):
- `AwardDetailScreenTest.tap_search_navigates_to_search_route`
- `AwardDetailScreenTest.tap_bell_opens_notifications_sheet`
- `AwardDetailScreenTest.bell_shows_badge_when_unread_count_positive`
- `AwardDetailScreenTest.bell_no_badge_when_unread_count_zero`
- `AwardDetailScreenTest.language_pill_is_present_and_tappable`
  (integration check — confirms the pill renders and clicks reach the
  `LocaleViewModel`; the dropdown's own contract lives in
  `LanguageSelectorTest`, not re-tested here)
- `AwardDetailScreenTest.selecting_EN_via_pill_re_renders_localized_chrome_strings`
- `AwardDetailScreenTest.selecting_EN_does_not_mutate_api_sourced_fields`
  (Resolved Q6 — client never translates API strings)
- `AwardDetailFocusOrderTest.tab_order_is_language_search_bell_dropdown_kudos_chitiet_navbar`

**Implementation** (Phase 6 wires callbacks; layout already exists from Phase 3):
- Replace the Phase 3 stub `onSearchClick = {}` with
  `onNavigateToSearch` (routes to `Routes.SEARCH`).
- Replace the Phase 3 stub `onBellClick = {}` with the
  `NotificationsSheet` reveal logic — mirrors Home's pattern (a
  `rememberModalBottomSheetState` + `LaunchedEffect` to show / hide).
- Replace the Phase 3 stub `onLanguageSelected = {}` with the real
  `localeViewModel.setLanguage(it)` call — the `LanguageSelector`
  itself is unchanged, only the upstream callback is rewired.
- Wire bell badge count via `notificationsSummaryRepository` (same
  source as Home — exposed through Hilt).

**Checkpoint**: every header control behaves identically to Home;
language switch re-renders every localized label on Award Detail
within one recomposition (SC-004 / TR-006).

#### Phase 7: Polish & Cross-Cutting

- **Telemetry**: emit a Timber breadcrumb on `AwardDetailViewModel`
  init (`award_detail.entered`, with `awardId` only — no PII per
  TR-002 / `SecureTimberTree`). Emit on dropdown switch
  (`award_detail.category_changed`). Emit on retry
  (`award_detail.retry`). All through the existing tag pattern
  (`Timber.tag("AwardDetailTelemetry")`).
- **Performance instrumentation (TR-001 / SC-001 / SC-002)**: wrap the
  `loadAward(id)` call in `Trace.beginSection("AwardDetail.load")` /
  `Trace.endSection()` so Android Profiler / `systrace` can verify
  the ≤ 800 ms warm + ≤ 2 s cold + ≤ 1.5 s dropdown-switch budgets.
  Manual stopwatch verification on emulator-5554 at the Phase 7 visual
  smoke; if budgets are missed, file as a follow-on rather than block.
  No automated perf test in this slice (Compose perf tests are deferred
  per the project's testing strategy).
- **A11y polish**: localized `contentDescription` on every interactive
  control; `Role.Button` + `stateDescription` on the category dropdown
  trigger (matches Language Dropdown TR-003 exactly). Manual TalkBack
  walkthrough on emulator-5554 — follow the documented focus order from
  spec § TR-003 + `AwardDetailFocusOrderTest`.
- **Visual fidelity**: compare emulator screenshot to Figma render
  using MoMorph's `get_frame_image` for `c-QM3_zjkG`; iterate on the
  custom dropdown chrome (border, radius, container color) per node
  `6885:10284` via `query_section`. Apply the same review discipline
  the Language Dropdown's Phase 6 (T045–T048) demonstrated.
- **TR-007 image resilience**: confirm Coil falls back to
  `ic_award_badge_placeholder` on null + load-failure for both the
  award badge AND the Kudos banner image.
- **Run full QA gate**: `./gradlew lint ktlintCheck assembleDebug
  testDebugUnitTest compileDebugAndroidTestKotlin`. Each must be green
  before merge. `connectedDebugAndroidTest` deferred to CI per the
  Login Phase 7 carry-over (Q4 from Login).
- **Doc updates**:
  - Add a `### Award Detail destination` note to Home's spec § Notes
    so future readers see Home's `onAwardChiTietTap` lands here.
  - Add a row to the existing project SCREENFLOW (when authored) — the
    spec's § Navigation Flow describes all edges.
  - Update `Routes.kt` KDoc to reflect that
    `Routes.AWARD_DETAIL_PATTERN` is now a real screen, not a placeholder.

### Risk Assessment

| Risk | Probability | Impact | Mitigation |
|------|-------------|--------|------------|
| **R1 — `/awards/:id` schema differs from spec assumption** (e.g., `prize_value` is actually `Decimal + currency_code` despite Resolved Q5) | Low (Resolved Q5) | High | Phase 2's failing test asserts the projected columns; if the schema differs the test fails on the very first compile-and-run. Fix the model class + projection in Phase 2 before US1 starts. |
| **R2 — Localization mechanism unknown (Q6)** | Medium | Medium | Repository signature accepts `locale: Language`; Phase 2 implementer picks whichever path ships (Path A: per-locale columns, Path B: `Accept-Language` header). Both paths satisfy US5 scenario 2 because the **client-side contract** is "render whatever the backend returns" (no client translation). |
| **R3 — Out-of-order detail fetch races (slow → fast dropdown taps)** | Medium | Medium | VM cancels in-flight jobs on `onCategorySelected`; covered by `dropdown_select_cancels_in_flight_request_and_loads_new` test (Phase 4). |
| **R4 — Cross-feature import of `home/ui/components/`** | Low | Low | Acceptable for this slice — Kotlin allows it and the constitution's "feature-first" rule is preserved at the **package** level. The lift to `core/chrome/ui/` is a documented follow-on once a third feature also needs the chrome. |
| **R5 — Bottom-nav Awards tab from non-Home screens** (e.g. user taps Awards while on Profile) | Low | Low | The route navigates to `Routes.AWARD_DETAIL_PATTERN` with `awardId` = the in-session last viewed OR the first by `sort_order` (FR-001 + Resolved Q1). VM init reads `SavedStateHandle["activeAwardId"]`; if absent, calls `repository.list().first().id`. |
| **R6 — Process death restores to Award Detail without an `awardId`** (rare cold-launch edge) | Low | Low | Edge Cases § "Process death + restore" rules apply: fall back to FR-001's default. No stale data. |
| **R7 — Predictive back gesture while dropdown is open** | Low | Low | M3 `DropdownMenu`'s `Popup` defaults to `dismissOnBackPress = true` — the gesture dismisses the menu without popping the screen. Covered by the Language Dropdown's existing `predictive_back_dismisses_menu_without_popping_parent` test (mirrored in `AwardCategoryDropdownTest`). |

### Estimated Complexity

- **Frontend**: Medium. The screen has more sections than Home but
  every section either reuses an existing component (KudosSection,
  HomeHeader, HomeBottomBar, LanguageSelector, AwardCard's Coil image
  pattern) or is a 1-purpose composable (RecipientCountRow,
  PrizeValueRow, KvKudosBanner). The category dropdown is the only
  novel component, and it mirrors the Language Dropdown contract.
- **Backend**: Low. One new repository method (`detail(id, locale)`) and
  one new Postgrest projection. RLS is already in place.
- **Testing**: Medium. ~25-30 new tests across unit + UI suites; the
  contract mirrors Language Dropdown + Home Phase 3 so authoring is
  fast (copy-and-adapt).

**Estimated effort**: 4–5 days for a single developer at the pace
demonstrated by the Home + Language Dropdown phases.

---

## Integration Testing Strategy

### Test Scope

- [x] **Component/Module interactions**: Award Detail composables ↔
  AwardDetailViewModel ↔ extended `AwardsRepository`.
- [x] **External dependencies**: Supabase Postgrest (`awards` table
  `SELECT`); Coil image loader (Kudos banner + badge image).
- [x] **Data layer**: `awards` table read-only access (no writes).
- [x] **User workflows**: Home carousel → Award Detail → dropdown switch →
  Kudos Chi tiết → Kudos overview.

### Test Categories

| Category | Applicable? | Key Scenarios |
|----------|-------------|---------------|
| UI ↔ Logic | Yes | Dropdown open/select/dismiss, Retry, sticky scroll, language switch in-place, bell badge reactivity. |
| Service ↔ Service | Yes | `AwardsRepository` ↔ `AuthRedirectController` (401 propagation), `LocaleViewModel` ↔ `AwardDetailViewModel` (locale-aware re-fetch on Path B if chosen). |
| App ↔ External API | Yes | Supabase Postgrest `awards.select { eq("id", id) }` against a test-double / local Postgrest. |
| App ↔ Data Layer | Yes | TTL-cached `detail(id)` — cache hit on idempotent reselection (covered by `AwardDetailViewModelTest.dropdown_select_idempotent_does_not_re_fetch`); cache invalidation on 401 (covered by `SupabaseAwardsRepositoryDetailTest.detail_401_propagates_as_failure_for_AuthRedirectController` + the cache is dropped when `AuthRedirectController` engages). |
| Cross-platform | No | Android-only per constitution. |

### Test Environment

- **Environment type**: Robolectric (Compose UI unit tests) + Android
  Instrumented (emulator-5554, the same target used by Home + Language
  Dropdown). Cold-launch tests use Robolectric's `RuntimeEnvironment.application`.
- **Test data strategy**: Hilt module overrides bind the existing
  `DemoAwardsRepository` (extended with the new `detail(id, locale)`
  method in Phase 2) in tests. The fake returns a small deterministic
  catalogue (3–5 award rows, each with name + description + quantity +
  prize_value + image_url) without hitting Supabase. The same fake is
  already used by Home's `DEMO_MODE` build variant — no parallel test
  double.
- **Isolation approach**: per-test Compose tree (`createComposeRule()`),
  per-test ViewModel (`SavedStateHandle()` instantiated fresh), per-test
  `LazyListState`.

### Mocking Strategy

| Dependency | Strategy | Rationale |
|------------|----------|-----------|
| `AwardsRepository.detail(id, locale)` | Existing `DemoAwardsRepository` (extended in Phase 2) for VM + UI tests | Avoids Supabase coupling in unit + UI tests; gateway-based contract test exercises the real Postgrest impl. The same fake powers the project's `DEMO_MODE` build variant — already authored, no test-only duplicate. |
| Supabase Postgrest HTTP | `MockServer`-style harness in `SupabaseAwardsRepositoryDetailTest` | Same approach as the existing `SupabasePostgrestAwardsRepositoryTest` from Home — proves the projection + `eq("id", ...)` filter wires up to the right SQL. |
| Coil image loader | Real impl + bundled drawables in instrumentation | Image-load resilience (TR-007) needs the real Coil pipeline to assert the placeholder fallback. |
| `AuthRedirectController` | Real impl | The 401 path is integration-critical; mocking would defeat the purpose. |
| `LanguagePreferenceRepository` | Real impl backed by an in-memory DataStore | Locale switch must be observed end-to-end. |

### Test Scenarios Outline

1. **Happy path**:
   - [ ] User taps Top Talent → body renders within ~1s; every field
     comes from API (FR-002, US1 scenario 1).
   - [ ] User opens dropdown → all categories listed; selecting Top
     Project re-renders body (FR-005 + FR-007, US2 scenario 2).
   - [ ] User taps Sun\*Kudos Chi tiết → Kudos overview route opens
     (FR-009, US4 scenario 1).
2. **Error handling**:
   - [ ] API 500 → error state with Retry → Retry → loading → populated
     (FR-004, US1 scenario 3).
   - [ ] 401 mid-flow → AuthRedirectController routes to Login (FR-012,
     US8 scenario 2).
   - [ ] Image load fails → placeholder renders for both badge and Kudos
     banner (FR-008 + TR-007).
3. **Edge cases**:
   - [ ] `image_url = null` → placeholder (FR-008, US1 scenario 4).
   - [ ] `quantity = null` AND `prize_value = null` → placeholder "—"
     (US1 scenario 5).
   - [ ] Rapid dropdown switch (B → C before A returns) → only the
     latest selection's payload is visible.
   - [ ] System back while dropdown is open → menu closes only (US2
     edge cases / Language Dropdown FR-008).
   - [ ] Process death + cold launch → falls back to FR-001 default,
     no stale data (Edge Cases § "Process death + restore").
   - [ ] Empty awards list (`/awards` returns `[]`) → dropdown renders
     a localized empty-state message; body shows FR-001's fallback
     branch (Edge Cases § "Empty awards list").
   - [ ] Single award (`/awards` returns one row) → dropdown opens with
     exactly one row; reselection is idempotent (Edge Cases § "Single
     award").
   - [ ] Long description (multi-screen text) → flows within the
     `LazyColumn` scroll container without truncation, no "Read more"
     CTA (Edge Cases § "Long description").
   - [ ] Image cache eviction → re-fetch is transparent (Edge Cases §
     "Image cache eviction"; verified via Coil's default disk-cache
     behaviour, no custom test).

### Tooling & Framework

- **Test framework**: JUnit 4 + Compose `ui-test-junit4` + Robolectric
  (`@RunWith(RobolectricTestRunner::class)`) + Hilt Testing
  (`@HiltAndroidTest`).
- **Supporting tools**: existing `MockSupabaseTest` harness pattern from
  Home; existing `TestLanguageRepository` for locale parity.
- **CI integration**: unit + Robolectric Compose tests run on every PR;
  `connectedDebugAndroidTest` runs nightly per project carry-over.

### Coverage Goals

| Area | Target | Priority |
|------|--------|----------|
| `AwardDetailViewModel` state transitions | ≥ 95% line, 100% branch on each `AwardDetailState` case + dropdown idempotence + cancellation + empty/single-award edge cases | High |
| `AwardCategoryDropdown` contract | **14 instrumented tests** mirroring Language Dropdown's T018–T031 (enumerated in Phase 4) | High |
| `AwardsRepository.detail(id, locale)` — both impls (`SupabasePostgrestAwardsRepository` real, `DemoAwardsRepository` fake) | 100% branch on success + failure + 401 + locale propagation (path A or B per Resolved Q6) | High |
| Compose UI integration on `AwardDetailScreen` | All P1 acceptance scenarios from US1 + US2 + US3 + US4 + US8; sticky chrome from FR-014 | High |
| Header chrome (US5/US6/US7) | Behavioral wiring tests only — the underlying `LanguageSelector`, `BellWithBadge`, and `NotificationsSheet` contracts are already covered by their own existing test suites and NOT re-tested here | Medium |
| Edge cases | Empty awards list, single award, slow→fast race, predictive-back-while-dropdown-open, image-load fail (badge + Kudos banner), null `quantity`/`prize_value`/`image_url` | High |
| Performance budgets (TR-001 / SC-001 / SC-002) | Manual `systrace` / Android Profiler check at Phase 7 — no automated test | Medium |
| Visual fidelity vs Figma | Manual screenshot diff at Phase 7 (not automated) | Medium |

---

## Threat Model (per Constitution Security Requirements)

Spec § TR-002 establishes that:
- Award payload is **read-only** and **not embargoed** (Resolved Q4).
- Supabase RLS already enforces authenticated `SELECT` on `awards`.
- Service-role key is never shipped (constitution § II).
- All log writes go through `SecureTimberTree` from Home Phase 10.

**Threats considered**:

| Threat | Likelihood | Mitigation |
|--------|------------|------------|
| Unauthenticated reads via direct deep link | Medium | RLS on `awards.select` + Phase 6's `AuthRedirectController` integration; US8 acceptance test covers the bounce. |
| Token exfiltration via log scrub bypass | Low | All Timber calls in this feature use the existing tag pattern; `SecureTimberTree` already scrubs the access token. New telemetry calls in Phase 7 emit only `awardId` (already a public identifier). |
| Sensitive payload masking bypass (would matter if quantities/prizes were embargoed) | N/A | Resolved Q4 — fields ship visible, no embargo. |
| Image-loader side channels (Coil cache fingerprinting) | Low | Coil's default disk cache uses content-hashed keys; no plaintext PII in image URLs. |
| Replay of `awardId` parameter (e.g. crafted deep link targeting non-existent id) | Low | `detail(id)` returns `Result.failure` on missing row; FR-004 surfaces a friendly error without disclosing existence/structure. |
| Network MITM | Low | App pins TLS via OkHttp (already configured for Supabase). No additional pinning needed. |

**No new threat classes** introduced by this feature beyond those Home
already mitigated.

---

## Dependencies & Prerequisites

### Required Before Start

- [x] `constitution.md` reviewed and understood
- [x] `spec.md` ratified (2026-05-11)
- [x] Navigation route already registered (`Routes.AWARD_DETAIL_PATTERN`)
- [x] Reusable components inventoried (Phase 1.4 codebase research is
  inlined in this plan's § Summary + § Integration Points)
- [ ] **API contract for `detail(id)`**: `.momorph/contexts/api-docs.yaml`
  is **missing** at the project level — confirm column projection
  against the live Supabase schema at Phase 2 task-execution time.
- [ ] **DB schema**: `.momorph/contexts/database-schema.sql` is
  **missing** — confirm `description`, `quantity`, `quantity_unit`,
  `prize_value`, `image_url` column names exist on the `awards` table
  (or add a migration in Phase 2 if absent).
- [ ] **Backend API test cases**: `.momorph/contexts/BACKEND_API_TESTCASES.md`
  is **missing** — backend contract tests are out of scope for this UI
  slice but should be authored in a follow-on plan if the project
  starts authoring server-side contract tests.

### External Dependencies

- **Supabase Postgrest** — the `awards` table must include the detail
  columns. If they're missing the implementer authors a migration
  before Phase 2 completes.
- **MoMorph asset pipeline** — `get_media_files` for the Node IDs
  enumerated in spec § Screen Components at Phase 0.

---

## Next Steps

After plan approval:

1. **Run** `/momorph.tasks c-QM3_zjkG-iOS-Award-Top-talent` to generate
   the task breakdown (~30-40 tasks across 8 phases).
2. **Review** `tasks.md` for parallelization opportunities — every
   "tests first" sub-bullet is [P] within a phase.
3. **Begin implementation** following the task order. Commit per task /
   coherent task group (per the project's "granular commits" memory).

---

## Notes

### Naming convention

The Figma frame is labelled "Top Talent" but per spec § Notes the
**screen** is parametric. All implementation symbols use `AwardDetail*`
(ViewModel, Screen, UiState, State, Repository method) — never
`TopTalent*`. Resolved in spec § Notes ("Frame name vs. screen name").

### Reuse-vs-rebuild calls

| Concern | Decision | Rationale |
|---------|----------|-----------|
| Sun\*Kudos block | **Reuse** `home.ui.components.KudosSection` directly | Same content + same destination (Resolved Q3). Net new code = 0. |
| Header chrome | **Reuse** `home.ui.components.HomeHeader` directly | Identical contract. The lift-to-`core/chrome` is a documented follow-on but not blocking. |
| Bottom navigation | **Reuse** `home.ui.components.HomeBottomBar` with `selected = HomeNavTab.Awards` | Already designed to be screen-agnostic via its enum + callback. |
| Language pill | **Reuse** `core.locale.ui.LanguageSelector` | Already in `core/` (lifted by uUvW6Qm1ve Phase 2). |
| Award badge image + placeholder | **New** but uses the existing Coil pipeline from `AwardCard` | Different bounds, different placeholder asset, but the Coil contract is the same. |
| Category dropdown | **New** but mirrors `LanguageSelector` contract | Functionally analogous; copy the structure (single-click guard, focus-on-open, idempotent reselection). |

### Why no `research.md`

The codebase reconnaissance needed for this plan was small in scope
(~5 files inspected) and is fully inlined into this plan's § Summary +
§ Integration Points + § Project Structure tables. Authoring a separate
`research.md` would duplicate the same content without adding clarity.

### Deferred items (carry-over to follow-on plans)

- **Lift `home/ui/components/{HomeHeader, HomeBottomBar, BellWithBadge, KudosSection, NotificationsSheet}` to `core/chrome/ui/`** when a third authenticated screen also needs the chrome (next likely candidate: Sun\*Kudos hub `fO0Kt19sZZ`).
- **Author `.momorph/contexts/api-docs.yaml`** with the `awards` endpoint contract via `/momorph.apidocs` once the backend OpenAPI artefact lands.
- **Author `.momorph/contexts/database-schema.sql`** by exporting the live Supabase migrations and pinning them to a versioned location.
- **Backend contract tests** for `detail(id)` go in a separate backend-side plan; out of scope here.
