# Tasks: Award Detail (Top Talent default)

**Frame**: `c-QM3_zjkG-iOS-Award-Top-talent`
**Prerequisites**: plan.md (required, present, reviewed twice 2026-05-11),
spec.md (required, present, ratified 2026-05-11)

> **Note on `design-style.md`**: This project intentionally does NOT
> produce a `design-style.md`. Per `spec.md` § Visual Requirements +
> Out of Scope and `plan.md` § Project Structure, visual specifications
> (colours, sizes, fonts, spacing) are fetched on-demand at
> implementation time via MoMorph `query_section` / `get_node` for the
> Node IDs listed in `spec.md` § Screen Components. Tasks below
> reference those Node IDs where pixel-level detail is needed.
>
> **Note on `research.md`**: Intentionally not produced — codebase
> reconnaissance is inlined in `plan.md` § Summary + § Integration
> Points + § Reuse-vs-rebuild table.
>
> **Note on `contexts/`**: `.momorph/contexts/api-docs.yaml`,
> `database-schema.sql`, and `BACKEND_API_TESTCASES.md` are MISSING at
> the project level. Implementer confirms the Supabase `awards` table
> schema against the live database at Phase 2 task-execution time per
> `plan.md` § Dependencies & Prerequisites.

---

## Task Format

```
- [ ] T### [P?] [Story?] Description | file/path.kt
```

- **[P]**: Can run in parallel (different files, no dependencies on incomplete tasks)
- **[Story]**: Which user story this belongs to (US1, US2, US3, US4, US5, US6, US7, US8)
- **|**: File path affected by this task

> **MVP slice**: Phase 3 collapses **US1 + US8** (body load + auth gate) into
> the smallest shippable increment. Phases 4–6 then layer in the dropdown,
> bottom-nav routing, Sun\*Kudos navigation, and header chrome on top of
> that foundation. The full Scaffold (sticky header + sticky bottom nav)
> lands in Phase 3 with stub callbacks; subsequent phases only wire
> behaviour, not layout. See `plan.md` § Phase 3 "Scaffold layout
> decision".

---

## Phase 0: Asset Preparation

**Purpose**: pull the badge artwork, Kudos banner, and section icons
referenced by `spec.md` § Screen Components from MoMorph into the
project's `res/drawable*` tree. Net new code = 0 (just images +
vector drawables).

- [ ] T001 [P] Run `get_media_files` for Figma nodes `6885:10266` (KV Kudos banner — `mms_A_KV Kudos`), `6885:10313`/`6885:10314` (Award badge graphic — `mms_2.3_award`'s circular lockup), and `6885:10297` (leading icon on award title row). Place vector drawables under `app/src/main/res/drawable/` and raster assets under the density-suffixed folders. Naming: `ic_award_*` for award-specific icons, `img_kudos_banner_*` if raster. Skip any asset already present in the Home Phase 0 inventory (de-dupe by content hash). | app/src/main/res/drawable/, app/src/main/res/drawable-{mdpi,hdpi,xhdpi,xxhdpi}/
- [ ] T002 [P] Author `ic_award_badge_placeholder.xml` — a neutral vector drawable rendered when `award.image_url` is `null` or fails to load (FR-008 / TR-007). 240×240dp viewport, neutral gold-tinted stroke matching `HeroOutlinedBorder` (`Color(0xFF998C5F)`). The drawable is referenced by Coil's `placeholder()` + `error()` builders in Phase 3. | app/src/main/res/drawable/ic_award_badge_placeholder.xml

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: add the localized string resources the body + chrome will
reference, and confirm the navigation route registration that Home
already shipped is correctly placed. No new dependencies (per
`plan.md` § Dependencies — empty table).

### Localized strings (VN authoritative + EN + JA parity for deprecation window)

- [ ] T003 [P] Add the new `award_detail_*` keys to `values/strings.xml` (VN authoritative): `award_detail_title` ("Hệ thống giải thưởng SAA 2025"), `award_detail_sub_label` ("Sun\* Annual Awards 2025"), `award_detail_quantity_label` ("Số lượng giải thưởng"), `award_detail_prize_label` ("Giá trị giải thưởng"), `award_detail_prize_caption` ("cho mỗi giải thưởng"), `award_detail_kudos_label` ("Phong trào ghi nhận"), `award_detail_kudos_section_title` ("Sun\* Kudos"), `award_detail_kudos_badge` ("ĐIỂM MỚI CỦA SAA 2025"), `award_detail_error` ("Không tải được giải thưởng, vui lòng thử lại"), `award_detail_placeholder_quantity` ("—"), `award_detail_placeholder_prize` ("—"). Keys that already exist (`home_link_chi_tiet`, `home_awards_loading`, `home_action_retry`) are reused — do NOT duplicate. | app/src/main/res/values/strings.xml
- [ ] T004 [P] Add the a11y string keys to `values/strings.xml`: `a11y_award_detail_screen` ("Màn hình chi tiết giải thưởng"), `a11y_award_category_dropdown` formatted string ("Chọn hạng mục giải thưởng, %1$s, danh sách thả xuống"), `a11y_award_badge_image` ("Huy hiệu giải thưởng %1$s"). | app/src/main/res/values/strings.xml
- [ ] T005 [P] Mirror every key from T003 + T004 into `values-en/strings.xml` with the English authoritative translations. Use the same formatted-string placeholders (`%1$s`). | app/src/main/res/values-en/strings.xml
- [ ] T006 [P] Mirror every key from T003 + T004 into `values-ja/strings.xml` (deprecation-window parity — see Language Dropdown spec § Out of Scope). JA values may be machine-translated placeholders; this file is dead on disk until removed in a future cleanup. | app/src/main/res/values-ja/strings.xml

### Navigation confirmation

- [ ] T007 Verify `Routes.AWARD_DETAIL_PATTERN` + `Routes.awardDetail(id)` exist with the signature documented in `plan.md` § Integration Points (already shipped in commit `14d93e5`; should match `app/src/main/java/com/example/aiddproject/navigation/Routes.kt:29-31`). If absent, restore. No edit unless the verification fails. | app/src/main/java/com/example/aiddproject/navigation/Routes.kt (read-only verification)

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: every type and contract needed by US1–US8 exists and
compiles with failing tests **before** any user-story slice starts.
**Blocks all US1–US8 implementation tasks.**

### Tests First (TDD per Constitution Principle V — written and FAILING before implementation)

- [ ] T008 [P] Write `SupabaseAwardsRepositoryDetailTest.detail_returns_full_payload_when_id_matches` — set up a fake Supabase `MockServer` returning a single `awards` row with all 7 fields (id, name, description, quantity, quantity_unit, prize_value, image_url); assert `repository.detail(id, Language.VN).getOrNull()` matches a hand-rolled `AwardDetail` instance. Lives in `home/data/` alongside the existing `SupabasePostgrestAwardsRepositoryTest`. | app/src/test/java/com/example/aiddproject/home/data/SupabaseAwardsRepositoryDetailTest.kt
- [ ] T009 [P] Write `SupabaseAwardsRepositoryDetailTest.detail_returns_failure_when_id_missing` — `MockServer` returns 404 / empty body; assert `.isFailure` and that the exception message is **localized-error-friendly** (does NOT leak the HTTP status code per TR-002). | app/src/test/java/com/example/aiddproject/home/data/SupabaseAwardsRepositoryDetailTest.kt
- [ ] T010 [P] Write `SupabaseAwardsRepositoryDetailTest.detail_propagates_locale_to_query` — call `detail(id, Language.EN)`; assert the outgoing HTTP request carries the locale either as a query param OR as an `Accept-Language: en` header. Test asserts whichever path the impl wires (Resolved Q6 deferred to task-execution time). | app/src/test/java/com/example/aiddproject/home/data/SupabaseAwardsRepositoryDetailTest.kt
- [ ] T011 [P] Write `SupabaseAwardsRepositoryDetailTest.detail_401_propagates_as_failure_for_AuthRedirectController` — `MockServer` returns 401; assert `.isFailure` AND the exception type is something `AuthRedirectController` recognizes (e.g., `HttpRequestException` with status 401). Locks the integration path described in US8 + FR-012. | app/src/test/java/com/example/aiddproject/home/data/SupabaseAwardsRepositoryDetailTest.kt
- [ ] T012 [P] Write `AwardDetailStateTest.transitions_Loading_to_Loaded_on_success` — sealed-interface state-machine smoke test: starting from `Loading`, applying a successful payload yields `Loaded(AwardDetail)`. | app/src/test/java/com/example/aiddproject/awarddetail/domain/states/AwardDetailStateTest.kt
- [ ] T013 [P] Write `AwardDetailStateTest.transitions_Loading_to_Error_on_failure` — symmetric to T012 for the failure path. | app/src/test/java/com/example/aiddproject/awarddetail/domain/states/AwardDetailStateTest.kt

### String parity (existing test must stay green)

- [ ] T014 [P] Run `StringResourceParityTest` after T003–T006 land. The existing test at `app/src/test/java/com/example/aiddproject/core/locale/StringResourceParityTest.kt` must stay green — every new `award_detail_*` key has a sibling in `values-en/` and `values-ja/`. If the test fails, fix the missing locale; no edit to the test itself. | app/src/test/java/com/example/aiddproject/core/locale/StringResourceParityTest.kt (verification only)

### Domain models

- [ ] T015 Create the `AwardDetail` data class in `awarddetail/domain/AwardDetail.kt` with fields `id: String`, `name: String`, `description: String`, `quantity: Int?` (nullable per FR-008), `quantity_unit: String?` (nullable), `prize_value: String?` (nullable, **pre-formatted** per Resolved Q5 — no client-side `NumberFormat`), `image_url: String?` (nullable), `sort_order: Int = 0`. Kotlin official style; immutable; KDoc references spec § Key Entities. | app/src/main/java/com/example/aiddproject/awarddetail/domain/AwardDetail.kt
- [ ] T016 Create `AwardDetailState` sealed interface in `awarddetail/domain/states/AwardDetailState.kt` with cases `Loading`, `Loaded(detail: AwardDetail)`, `Error(messageRes: Int)`. KDoc cites US1 scenario 2/3 + FR-003/FR-004. (Depends on T012 + T013 — failing tests must exist first.) | app/src/main/java/com/example/aiddproject/awarddetail/domain/states/AwardDetailState.kt

### Repository extension

- [ ] T017 Extend `AwardsRepository` interface with `suspend fun detail(id: String, locale: Language): Result<AwardDetail>`. Document inline: "List + detail both go through this single repository per Resolved Q8." The list method is unchanged. | app/src/main/java/com/example/aiddproject/home/data/AwardsRepository.kt
- [ ] T018 Implement `detail(id, locale)` in `SupabasePostgrestAwardsRepository` via `supabaseClient.from("awards").select(columns) { filter { eq("id", id) } }.decodeSingle<AwardDetailRow>()`. Map the row into the `AwardDetail` domain model. Wrap in `Result.runCatching { ... }` so any throw (network, 401, parse) becomes `Result.failure`. Path A (per-locale columns) vs Path B (Accept-Language header) is the implementer's call per Resolved Q6 — document the chosen path in a KDoc comment. (Depends on T015 + T017 + the failing tests T008–T011.) | app/src/main/java/com/example/aiddproject/home/data/SupabasePostgrestAwardsRepository.kt
- [ ] T019 Implement `detail(id, locale)` in `DemoAwardsRepository` — returns hard-coded VN payloads for the existing demo award list (id-keyed). Ignores the `locale` arg per `plan.md` § Backend Localization "demo repository ignores the locale". | app/src/main/java/com/example/aiddproject/home/data/DemoAwardsRepository.kt

**Checkpoint**: `./gradlew assembleDebug testDebugUnitTest
compileDebugAndroidTestKotlin` passes. T008–T013 are GREEN (failing
tests are now passing). The domain layer + repository extension is in
final shape; user-story phases can proceed.

---

## Phase 3: User Story 1 + User Story 8 — Body load + auth gate (Priority: P1) 🎯 MVP slice

**Goal**: navigating from Home tapping "Chi tiết" on a card opens this
screen with the **full Scaffold structure** (sticky `topBar` + scrollable
body + sticky `bottomBar`). The body loads, errors retry cleanly, and
an unauthenticated session bounces to Login. Header + bottom-nav
interactive children are stubbed; subsequent phases wire them.

**Independent Test**: tap Top Talent in Home's awards carousel → Award
Detail opens with the full chrome and body populated within ~1s on
emulator-5554; scroll the body and verify header + bottom nav stay
pinned; disconnect network and verify Retry flow; sign out and verify
deep-link bounces to Login.

### Tests First — ViewModel layer

- [ ] T020 [P] [US1] Write `AwardDetailViewModelTest.load_emits_Loading_then_Loaded_on_success` — inject a `DemoAwardsRepository` with a Top Talent row; collect `state.detail` over time; assert the first two emissions are `Loading` then `Loaded`. | app/src/test/java/com/example/aiddproject/awarddetail/ui/AwardDetailViewModelTest.kt
- [ ] T021 [P] [US1] Write `AwardDetailViewModelTest.load_emits_Loading_then_Error_on_failure` — inject a stub that returns `Result.failure`; assert `Loading → Error(messageRes)`. | app/src/test/java/com/example/aiddproject/awarddetail/ui/AwardDetailViewModelTest.kt
- [ ] T022 [P] [US1] Write `AwardDetailViewModelTest.retry_re_issues_fetch_after_error` — fixture starts in `Error`; call `vm.onRetry()`; assert state transitions back to `Loading` then `Loaded`. | app/src/test/java/com/example/aiddproject/awarddetail/ui/AwardDetailViewModelTest.kt
- [ ] T023 [P] [US8] Write `AwardDetailViewModelTest.401_repository_result_emits_Error_state` — repository returns `Result.failure(HttpRequestException(status = 401))`; assert VM emits `AwardDetailState.Error`. **NOT** an integration test — the AuthRedirectController bounce is asserted manually at the Phase 3 checkpoint, NOT in this unit test. | app/src/test/java/com/example/aiddproject/awarddetail/ui/AwardDetailViewModelTest.kt
- [ ] T024 [P] [US1] Write `AwardDetailViewModelTest.init_uses_savedStateHandle_awardId_when_present` — pre-seed `SavedStateHandle("awardId" to "top-talent-id")`; assert the VM fetches that exact id, NOT the first by `sort_order`. | app/src/test/java/com/example/aiddproject/awarddetail/ui/AwardDetailViewModelTest.kt
- [ ] T025 [P] [US1] Write `AwardDetailViewModelTest.init_falls_back_to_first_award_by_sort_order_when_savedStateHandle_empty` — `SavedStateHandle` has no `awardId`; assert the VM calls `repository.list()` and picks the first row (per FR-001 + Resolved Q1). | app/src/test/java/com/example/aiddproject/awarddetail/ui/AwardDetailViewModelTest.kt

### Tests First — Compose UI layer

- [ ] T026 [P] [US1] Write `AwardDetailScreenTest.body_renders_full_award_payload` — fake repo returns Top Talent payload; assert badge image, title, description, quantity, prize all render. | app/src/androidTest/java/com/example/aiddproject/awarddetail/AwardDetailScreenTest.kt
- [ ] T027 [P] [US1] Write `AwardDetailScreenTest.loading_indicator_visible_while_fetching` — fake repo blocks on a `CompletableDeferred`; assert spinner/skeleton visible until the deferred completes. | app/src/androidTest/java/com/example/aiddproject/awarddetail/AwardDetailScreenTest.kt
- [ ] T028 [P] [US1] Write `AwardDetailScreenTest.error_state_shows_retry_button` — fake repo returns failure; assert the Retry button is displayed with the localized label. | app/src/androidTest/java/com/example/aiddproject/awarddetail/AwardDetailScreenTest.kt
- [ ] T029 [P] [US1] Write `AwardDetailScreenTest.retry_button_re_issues_fetch` — first call fails, second succeeds; assert tap on Retry triggers the second fetch and the body populates. | app/src/androidTest/java/com/example/aiddproject/awarddetail/AwardDetailScreenTest.kt
- [ ] T030 [P] [US1] Write `AwardDetailScreenTest.null_image_url_renders_placeholder` — fake repo returns `image_url = null`; assert the placeholder drawable (`ic_award_badge_placeholder`) renders in its place AND the rest of the body still renders. Covers FR-008 + TR-007. | app/src/androidTest/java/com/example/aiddproject/awarddetail/AwardDetailScreenTest.kt
- [ ] T031 [P] [US1] Write `AwardDetailScreenTest.null_quantity_and_prize_render_placeholders` — fake repo returns `quantity = null, prize_value = null`; assert "—" placeholder strings render in both sections (FR-008 + US1 scenario 5). | app/src/androidTest/java/com/example/aiddproject/awarddetail/AwardDetailScreenTest.kt
- [ ] T032 [P] [US1] Write `AwardDetailScreenTest.sticky_header_stays_pinned_on_scroll` — scroll the `LazyColumn` past several screens of content; assert `HomeHeader` bounds top-y remains 0 (or status-bar offset). FR-014 + TC_IOS_AWARD_DETAIL_FUN_010. | app/src/androidTest/java/com/example/aiddproject/awarddetail/AwardDetailScreenTest.kt
- [ ] T033 [P] [US1] Write `AwardDetailScreenTest.sticky_bottom_nav_stays_pinned_on_scroll` — symmetric to T032; assert `HomeBottomBar` bounds bottom-y remains at screen edge. FR-014 + TC_IOS_AWARD_DETAIL_FUN_011. | app/src/androidTest/java/com/example/aiddproject/awarddetail/AwardDetailScreenTest.kt

### Implementation — UI state + ViewModel

- [ ] T034 [US1] Create `AwardDetailUiState.kt` — root immutable state class wrapping `activeAwardId: String?`, `detail: AwardDetailState`, `categories: AwardsState`, `isDropdownOpen: Boolean` (transient, default `false`). KDoc cites US1 + US2 + spec § State Management. | app/src/main/java/com/example/aiddproject/awarddetail/ui/AwardDetailUiState.kt
- [ ] T035 [US1] Create `AwardDetailViewModel.kt` — `@HiltViewModel` injecting `SavedStateHandle` + `AwardsRepository` + `LocaleViewModel`. `init {}` launches a `viewModelScope` coroutine implementing the three-step routine from `plan.md` § Phase 3 ("Init coroutine"): read `awardId` from `SavedStateHandle`; if null, fetch list + pick first by `sort_order` + write back to handle; then call `detail(id, locale)` and emit state. Adds `onRetry()` method. Wires telemetry breadcrumbs (`award_detail.entered`) via `Timber.tag("AwardDetailTelemetry")`. (Depends on T015 + T016 + T017 + T020–T025.) | app/src/main/java/com/example/aiddproject/awarddetail/ui/AwardDetailViewModel.kt

### Implementation — Composables (stateless, no behavior wiring beyond callbacks)

- [ ] T036 [P] [US1] Create `KvKudosBanner.kt` — static banner reusing the assets pulled in T001. Renders "Hệ thống ghi nhận và cảm ơn" + KUDOS lockup. <80 LOC. | app/src/main/java/com/example/aiddproject/awarddetail/ui/components/KvKudosBanner.kt
- [ ] T037 [P] [US1] Create `HighlightBlock.kt` — sub-label + title + dropdown anchor. **Phase 3 version**: the dropdown trigger renders the active award's name + chevron but `onClick` is a no-op stub (full dropdown lands in Phase 4 / T058–T072). <120 LOC. References Figma `6885:10284` for visual fidelity (chrome via `query_section`). | app/src/main/java/com/example/aiddproject/awarddetail/ui/components/HighlightBlock.kt
- [ ] T038 [P] [US1] Create `AwardHeroBlock.kt` — badge image via Coil (`AsyncImage` with `placeholder` + `error` both pointing at `ic_award_badge_placeholder`), title row (icon + name) underneath. Per TR-007, Coil falls back to the placeholder on null `image_url`, on load failure, AND on cache miss. <100 LOC. | app/src/main/java/com/example/aiddproject/awarddetail/ui/components/AwardHeroBlock.kt
- [ ] T039 [P] [US1] Create `AwardInfoBlock.kt` — description paragraph + `RecipientCountRow` (label "Số lượng giải thưởng" + value + unit) + `PrizeValueRow` (label "Giá trị giải thưởng" + amount + caption "cho mỗi giải thưởng"). All three sub-rows live in the same file, each <80 LOC. Renders `"—"` placeholder strings when `quantity` / `prize_value` is `null`. | app/src/main/java/com/example/aiddproject/awarddetail/ui/components/AwardInfoBlock.kt

### Implementation — Screen + navigation wiring

- [ ] T040 [US1] Create `AwardDetailScreenContent.kt` — **stateless** layout. Receives `state: AwardDetailUiState` + every callback as a parameter. Composes a `Scaffold` with `topBar = { HomeHeader(...) }`, `bottomBar = { HomeBottomBar(selected = HomeNavTab.Awards, ...) }`, and the body inside a `LazyColumn` containing (in order): `KvKudosBanner`, `HighlightBlock`, `AwardHeroBlock`, `AwardInfoBlock`, `KudosSection` (imported from `home/ui/components/`). All header + nav callbacks accepted as parameters (the stateful screen passes stubs in Phase 3, real handlers in Phases 5–6). | app/src/main/java/com/example/aiddproject/awarddetail/ui/AwardDetailScreenContent.kt
- [ ] T041 [US1] Create `AwardDetailScreen.kt` — **stateful** entry point. Owns the `HiltViewModel` + `LocaleViewModel` + `SnackbarHostState`. Passes the VM's `state` and stubbed callbacks (`onSearchClick = {}`, `onBellClick = {}`, `onLanguageSelected = {}`, `onTabSelect = {}`, `onKudosChiTietClick = {}`, `onDropdownTriggerClick = {}`) into `AwardDetailScreenContent`. The `onRetry` callback wires to `vm.onRetry()` and is wrapped in `rememberSingleClickHandler` per TR-004. | app/src/main/java/com/example/aiddproject/awarddetail/ui/AwardDetailScreen.kt
- [ ] T042 [US1] Modify `AppNavigation.kt` — replace the placeholder composable at `Routes.AWARD_DETAIL_PATTERN` (currently `PlaceholderScreen(label = "Award detail: $awardId")` on lines 121-127) with a real `AwardDetailScreen(...)` invocation. The `awardId` is already extracted from `backStackEntry.arguments` — pass it via the existing Hilt-injected `SavedStateHandle` (no change to the navigation argument plumbing). | app/src/main/java/com/example/aiddproject/navigation/AppNavigation.kt

**Checkpoint**: end-to-end smoke on emulator-5554 — tap "Chi tiết" on
Top Talent in Home → Award Detail opens with full chrome → body
populated. Scroll the body → header + bottom nav stay pinned.
Disconnect network → tap Chi tiết → error state visible → Retry →
loading → populated. Sign out → re-launch + try Award Detail deep
link → bounces to Login. Run `./gradlew assembleDebug
testDebugUnitTest compileDebugAndroidTestKotlin` — all green.

---

## Phase 4: User Story 2 — Category dropdown (Priority: P1)

**Goal**: the Highlight block's dropdown opens with every award from
`/awards`, the body re-renders to the selected category, and the
dropdown's contract mirrors the Language Dropdown's 14-test parity.

**Independent Test**: from a populated detail screen, tap the
dropdown → all categories listed → tap Top Project → menu closes,
anchor flips to "Top Project", body re-renders. Tap dropdown → tap
Top Talent (active) → menu closes silently, no second fetch.

### Tests First — 14 dropdown contract tests (mirror Language Dropdown T018–T031)

- [ ] T043 [P] [US2] Write `AwardCategoryDropdownTest.menu_renders_every_award_from_repository` — fake repo returns 4 awards; opens the menu; asserts 4 rows render with the correct names in `sort_order` order. | app/src/androidTest/java/com/example/aiddproject/awarddetail/AwardCategoryDropdownTest.kt
- [ ] T044 [P] [US2] Write `AwardCategoryDropdownTest.selecting_other_award_invokes_callback_and_updates_anchor` — taps a non-active row; asserts `onSelect(id)` fires exactly once AND the anchor's visible text flips to the new award. | app/src/androidTest/java/com/example/aiddproject/awarddetail/AwardCategoryDropdownTest.kt
- [ ] T045 [P] [US2] Write `AwardCategoryDropdownTest.content_description_recomputes_when_active_award_flips` — drives selection A → B; asserts the anchor's `contentDescription` advances from "Chọn hạng mục giải thưởng, A, danh sách thả xuống" to "…, B, …". Mirrors Language Dropdown T020. | app/src/androidTest/java/com/example/aiddproject/awarddetail/AwardCategoryDropdownTest.kt
- [ ] T046 [P] [US2] Write `AwardCategoryDropdownTest.trigger_has_role_button_with_expanded_collapsed_state` — when closed, asserts `Role.Button` + `stateDescription` matches `a11y_dropdown_collapsed`; opens menu; asserts state flips to `a11y_dropdown_expanded`. Mirrors Language Dropdown T021. | app/src/androidTest/java/com/example/aiddproject/awarddetail/AwardCategoryDropdownTest.kt
- [ ] T047 [P] [US2] Write `AwardCategoryDropdownTest.opening_menu_focuses_first_row_for_TalkBack` — taps anchor; asserts first row reports `assertIsFocused()`. Mirrors Language Dropdown T022. | app/src/androidTest/java/com/example/aiddproject/awarddetail/AwardCategoryDropdownTest.kt
- [ ] T048 [P] [US2] Write `AwardCategoryDropdownTest.keyboard_tab_order_is_anchor_then_rows` — verifies anchor + each row are independently focusable via `requestFocus()`. Mirrors Language Dropdown T023. | app/src/androidTest/java/com/example/aiddproject/awarddetail/AwardCategoryDropdownTest.kt
- [ ] T049 [P] [US2] Write `AwardCategoryDropdownTest.trigger_meets_48dp_touch_target` — `getBoundsInRoot()` on the anchor ≥ 48×48dp. TR-003 + TR-008. | app/src/androidTest/java/com/example/aiddproject/awarddetail/AwardCategoryDropdownTest.kt
- [ ] T050 [P] [US2] Write `AwardCategoryDropdownTest.rows_meet_48dp_touch_target` — every row's bounds ≥ 48×48dp. Mirrors Language Dropdown T025. | app/src/androidTest/java/com/example/aiddproject/awarddetail/AwardCategoryDropdownTest.kt
- [ ] T051 [P] [US2] Write `AwardCategoryDropdownTest.reselecting_active_award_is_idempotent_no_callback` — selects the already-active row; asserts `onSelect` is NOT invoked. Covers FR-005 + US2 scenario 4. | app/src/androidTest/java/com/example/aiddproject/awarddetail/AwardCategoryDropdownTest.kt
- [ ] T052 [P] [US2] Write `AwardCategoryDropdownTest.row_double_tap_yields_one_select_callback` — two rapid taps on the same row produce exactly one `onSelect` call. TR-004. | app/src/androidTest/java/com/example/aiddproject/awarddetail/AwardCategoryDropdownTest.kt
- [ ] T053 [P] [US2] Write `AwardCategoryDropdownTest.anchor_double_tap_yields_one_open_transition` — two rapid taps on the anchor leave the menu open (one expanded=true transition, second suppressed by the single-click guard). Mirrors Language Dropdown T028. | app/src/androidTest/java/com/example/aiddproject/awarddetail/AwardCategoryDropdownTest.kt
- [ ] T054 [P] [US2] Write `AwardCategoryDropdownTest.second_deliberate_tap_on_anchor_closes_menu` — first tap opens; `mainClock.advanceTimeBy(500)` past the guard window; second tap closes. FR-006. | app/src/androidTest/java/com/example/aiddproject/awarddetail/AwardCategoryDropdownTest.kt
- [ ] T055 [P] [US2] Write `AwardCategoryDropdownTest.outside_tap_dismisses_menu_without_changing_active` — tap a sibling node outside the popup; assert menu closes and active is unchanged. FR-006. | app/src/androidTest/java/com/example/aiddproject/awarddetail/AwardCategoryDropdownTest.kt
- [ ] T056 [P] [US2] Write `AwardCategoryDropdownTest.predictive_back_dismisses_menu_without_popping_screen` — `Espresso.pressBack()` while menu is open; assert menu closes AND the screen's anchor is still composed. Mirrors Language Dropdown T031. | app/src/androidTest/java/com/example/aiddproject/awarddetail/AwardCategoryDropdownTest.kt

### Tests First — ViewModel-level dropdown behavior

- [ ] T057 [P] [US2] Write `AwardDetailViewModelTest.dropdown_select_cancels_in_flight_request_and_loads_new` — issue selection B while A's fetch is still in flight; assert A's result NEVER reaches UI state; only B's payload does. Out-of-order race per Edge Cases. | app/src/test/java/com/example/aiddproject/awarddetail/ui/AwardDetailViewModelTest.kt
- [ ] T058 [P] [US2] Write `AwardDetailViewModelTest.dropdown_select_idempotent_does_not_re_fetch` — call `onCategorySelected(activeAwardId)`; assert `repository.detail()` is NOT invoked a second time. | app/src/test/java/com/example/aiddproject/awarddetail/ui/AwardDetailViewModelTest.kt
- [ ] T059 [P] [US2] Write `AwardDetailViewModelTest.empty_awards_list_dropdown_renders_AwardsState_Empty` — fake `list()` returns `[]`; assert `state.categories == AwardsState.Empty`. Edge Case § "Empty awards list". | app/src/test/java/com/example/aiddproject/awarddetail/ui/AwardDetailViewModelTest.kt
- [ ] T060 [P] [US2] Write `AwardDetailViewModelTest.single_award_dropdown_lists_one_row` — fake `list()` returns one row; assert `state.categories == AwardsState.Populated(listOf(...))` and reselecting that row is idempotent. Edge Case § "Single award". | app/src/test/java/com/example/aiddproject/awarddetail/ui/AwardDetailViewModelTest.kt

### Implementation — Dropdown composable + VM wiring

- [ ] T061 [US2] Create `AwardCategoryDropdown.kt` — M3 `DropdownMenu` with custom `containerColor` / `border` / `shape` per Figma node `6885:10284` (values queried via `query_section` at task-execution time). Structure mirrors `LanguageSelector`'s contract: anchor pill, single-click-guarded open lambda, `Modifier.semantics { role; contentDescription; stateDescription }`, `FocusRequester` on first row + `LaunchedEffect(expanded)` to move TalkBack focus, `Modifier.heightIn(min = 48.dp)` on every row, `rememberSingleClickHandler` on row clicks, idempotent reselection short-circuit (`if (id != selected) onSelect(id)`). <150 LOC. (Depends on T043–T056.) | app/src/main/java/com/example/aiddproject/awarddetail/ui/components/AwardCategoryDropdown.kt
- [ ] T062 [US2] Wire `AwardCategoryDropdown` into `HighlightBlock` — replace the Phase 3 stubbed chevron with the real dropdown anchor. The block receives `categories: AwardsState`, `activeAwardId: String?`, and `onCategorySelected: (String) -> Unit` as parameters. Renders empty-state / error-state per `AwardsState` matching `home/ui/components/AwardsSection`'s pattern. | app/src/main/java/com/example/aiddproject/awarddetail/ui/components/HighlightBlock.kt
- [ ] T063 [US2] Add `onCategorySelected(id: String)` to `AwardDetailViewModel` implementing the 5-step routine from `plan.md` § Phase 4 (cancel in-flight → idempotency check → `Loading` → write `SavedStateHandle` → launch fetch). Emit telemetry breadcrumb `award_detail.category_changed` via `Timber.tag("AwardDetailTelemetry")`. (Depends on T057 + T058.) | app/src/main/java/com/example/aiddproject/awarddetail/ui/AwardDetailViewModel.kt
- [ ] T064 [US2] Update `AwardDetailScreen` to thread the real `onCategorySelected` callback through to `AwardDetailScreenContent` (replacing the Phase 3 stub). Update `AwardDetailUiState` if needed to surface `categories` from the VM (via a `combine(repository.observeAwards(), ...)` flow that the VM exposes). | app/src/main/java/com/example/aiddproject/awarddetail/ui/AwardDetailScreen.kt + app/src/main/java/com/example/aiddproject/awarddetail/ui/AwardDetailViewModel.kt

**Checkpoint**: on emulator-5554 — open dropdown → 4 categories listed
→ select Top Project → body re-renders. Reselect Top Talent (active)
→ menu closes silently. Tap outside → menu dismisses. Predictive
back → dropdown closes only, screen stays composed.

---

## Phase 5: User Story 3 + User Story 4 — Bottom nav + Sun\*Kudos Chi tiết (Priority: P1)

**Goal**: the bottom-nav routes work, the Awards tab is active, and
the Sun\*Kudos Chi tiết button navigates to the same destination as
the Kudos bottom-nav tab (Resolved Q3).

**Independent Test**: tap each of the four tabs in turn from Award
Detail and verify the navigation outcome. Scroll to the Sun\*Kudos
block, tap **Chi tiết**, verify the Kudos hub opens (same destination
as the Kudos tab).

### Tests First

- [ ] T065 [P] [US3] Write `AwardDetailScreenTest.tap_SAA_2025_tab_navigates_to_home` — tap the `Saa2025` tab; assert the navigation callback is invoked with `HomeNavTab.Saa2025`. TC_IOS_AWARD_DETAIL_FUN_013. | app/src/androidTest/java/com/example/aiddproject/awarddetail/AwardDetailScreenTest.kt
- [ ] T066 [P] [US3] Write `AwardDetailScreenTest.tap_kudos_tab_navigates_to_kudos_overview` — tap the `Kudos` tab; assert callback fires with `HomeNavTab.Kudos`. TC_IOS_AWARD_DETAIL_FUN_014. | app/src/androidTest/java/com/example/aiddproject/awarddetail/AwardDetailScreenTest.kt
- [ ] T067 [P] [US3] Write `AwardDetailScreenTest.tap_profile_tab_navigates_to_profile` — tap the `Profile` tab; assert callback fires with `HomeNavTab.Profile`. TC_IOS_AWARD_DETAIL_FUN_015. | app/src/androidTest/java/com/example/aiddproject/awarddetail/AwardDetailScreenTest.kt
- [ ] T068 [P] [US3] Write `AwardDetailScreenTest.tap_awards_tab_while_active_scrolls_body_to_top` — `LazyListState.scrollToItem(5)` first, then tap the `Awards` (active) tab; assert `firstVisibleItemIndex == 0`. Resolved Q2. | app/src/androidTest/java/com/example/aiddproject/awarddetail/AwardDetailScreenTest.kt
- [ ] T069 [P] [US4] Write `AwardDetailScreenTest.tap_kudos_chi_tiet_navigates_to_kudos_overview` — tap the Sun\*Kudos Chi tiết button; assert the same `onNavigateToKudosOverview` lambda fires (the one threaded into BOTH the bottom-nav Kudos tab AND the Sun\*Kudos block per Resolved Q3). | app/src/androidTest/java/com/example/aiddproject/awarddetail/AwardDetailScreenTest.kt
- [ ] T070 [P] [US4] Write `AwardDetailScreenTest.kudos_chi_tiet_double_tap_yields_one_navigation` — two rapid taps on Chi tiết produce exactly one navigation call. TR-004. | app/src/androidTest/java/com/example/aiddproject/awarddetail/AwardDetailScreenTest.kt
- [ ] T071 [P] [US4] Write `AwardDetailScreenTest.kudos_banner_image_load_fail_renders_placeholder` — the Kudos banner image fails to load (offline / 404); assert placeholder renders and Chi tiết remains tappable. TR-007 + TC_IOS_AWARD_DETAIL_FUN_021. | app/src/androidTest/java/com/example/aiddproject/awarddetail/AwardDetailScreenTest.kt

### Implementation

- [ ] T072 [US3] Add `onAwardsTabRetap()` to `AwardDetailViewModel` — accepts a `LazyListState` (passed from the screen) and calls `scrollToItem(0)` inside `viewModelScope`. Used by the Awards tab re-tap path. | app/src/main/java/com/example/aiddproject/awarddetail/ui/AwardDetailViewModel.kt
- [ ] T073 [US3] Update `AwardDetailScreen` — replace the Phase 3 stub `onTabSelect = {}` with the real routing lambda that maps `HomeNavTab.Saa2025 → onNavigateToHome()`, `HomeNavTab.Awards → vm.onAwardsTabRetap(lazyState)`, `HomeNavTab.Kudos → onNavigateToKudosOverview()`, `HomeNavTab.Profile → onNavigateToProfile()`. Each callback is `rememberSingleClickHandler`-wrapped per TR-004. | app/src/main/java/com/example/aiddproject/awarddetail/ui/AwardDetailScreen.kt
- [ ] T074 [US4] Update `AwardDetailScreen` — declare `onNavigateToKudosOverview` exactly once and thread it into BOTH the bottom-nav `Kudos` tab handler AND the `KudosSection.onChiTietClick` callback. Per Resolved Q3 these are the same destination; the wiring guarantees consistency. The Chi tiết callback is wrapped in `rememberSingleClickHandler` per TR-004 + T070. | app/src/main/java/com/example/aiddproject/awarddetail/ui/AwardDetailScreen.kt
- [ ] T075 [US3+US4] Update `AppNavigation.kt:121-127` — wire all five external callbacks into the `AwardDetailScreen(...)` invocation: `onNavigateToHome = { navController.popBackStack(Routes.HOME, inclusive = false) }`, `onNavigateToKudosOverview = { navController.navigate(Routes.KUDOS_OVERVIEW) }`, `onNavigateToProfile = { navController.navigate(Routes.PROFILE) }`, `onNavigateToSearch = { navController.navigate(Routes.SEARCH) }`, `onNavigateBack = { navController.popBackStack() }`. | app/src/main/java/com/example/aiddproject/navigation/AppNavigation.kt

**Checkpoint**: emulator pass — all bottom-nav tabs route correctly,
Awards tab highlight is gold (visual fidelity vs Figma `6885:10332`),
Sun\*Kudos Chi tiết opens the Kudos overview (same as the Kudos tab).
Re-tap Awards while already on the screen → body scrolls to top.

---

## Phase 6: User Story 5 + User Story 6 + User Story 7 — Header chrome (Priority: P2)

**Goal**: the language pill, bell-with-badge, and search icon work on
this screen and behave identically to Home. The existing
`LanguageSelector` + `BellWithBadge` contracts are NOT re-tested —
only integration is verified here.

**Independent Test**: on a VN-rendered Award Detail, open the language
pill, select EN — verify every localized chrome label re-renders in
English while API-sourced fields are unchanged. Tap the bell → the
NotificationsSheet opens. Tap the search icon → the Search route
opens.

### Tests First — Header chrome integration

- [ ] T076 [P] [US7] Write `AwardDetailScreenTest.tap_search_navigates_to_search_route` — tap the search icon; assert `onNavigateToSearch` callback fires exactly once. TC_IOS_AWARD_DETAIL_FUN_017. | app/src/androidTest/java/com/example/aiddproject/awarddetail/AwardDetailScreenTest.kt
- [ ] T077 [P] [US6] Write `AwardDetailScreenTest.tap_bell_opens_notifications_sheet` — tap the bell icon; assert the `ModalBottomSheet` containing the NotificationsSheet content is visible (asserted via `assertIsDisplayed` on the sheet's testTag). TC_IOS_AWARD_DETAIL_FUN_016. | app/src/androidTest/java/com/example/aiddproject/awarddetail/AwardDetailScreenTest.kt
- [ ] T078 [P] [US6] Write `AwardDetailScreenTest.bell_shows_badge_when_unread_count_positive` — fake `notificationsSummaryRepository` emits `unreadCount = 3`; assert the badge indicator is present on the bell. TC_IOS_AWARD_DETAIL_GUI_002. | app/src/androidTest/java/com/example/aiddproject/awarddetail/AwardDetailScreenTest.kt
- [ ] T079 [P] [US6] Write `AwardDetailScreenTest.bell_no_badge_when_unread_count_zero` — fake repo emits `unreadCount = 0`; assert no badge. TC_IOS_AWARD_DETAIL_GUI_003. | app/src/androidTest/java/com/example/aiddproject/awarddetail/AwardDetailScreenTest.kt
- [ ] T080 [P] [US5] Write `AwardDetailScreenTest.language_pill_is_present_and_tappable` — integration check only; asserts the pill renders at the documented header position and `LanguageSelector`'s anchor testTag is in the semantics tree. The dropdown's own contract is covered by the existing `LanguageSelectorTest` and NOT re-tested here. | app/src/androidTest/java/com/example/aiddproject/awarddetail/AwardDetailScreenTest.kt
- [ ] T081 [P] [US5] Write `AwardDetailScreenTest.selecting_EN_via_pill_re_renders_localized_chrome_strings` — driver: stateful wrapper with `LanguageProvider(language)` reading the locale flow; tap pill → tap EN row; assert at least one localized chrome string (e.g. `award_detail_quantity_label`) re-renders in English in the same composition. TR-006 + TC_IOS_AWARD_DETAIL_FUN_019. | app/src/androidTest/java/com/example/aiddproject/awarddetail/AwardDetailScreenTest.kt
- [ ] T082 [P] [US5] Write `AwardDetailScreenTest.selecting_EN_does_not_mutate_api_sourced_fields` — same setup as T081; assert the API-sourced strings (name, description, quantity_unit, prize_value) remain the values the fake repo returned. Locks Resolved Q6: client never translates API strings. | app/src/androidTest/java/com/example/aiddproject/awarddetail/AwardDetailScreenTest.kt
- [ ] T083 [P] [US5+US6+US7] Write `AwardDetailFocusOrderTest.tab_order_is_language_search_bell_dropdown_kudos_chitiet_navbar` — `requestFocus()` on each header control in the documented order; assert each is focusable independently. Mirrors `HomeFocusOrderTest`. | app/src/androidTest/java/com/example/aiddproject/awarddetail/AwardDetailFocusOrderTest.kt

### Implementation — Header chrome callbacks

- [ ] T084 [US7] Update `AwardDetailScreen` — replace the Phase 3 stub `onSearchClick = {}` with the real `onNavigateToSearch` callback (wrapped in `rememberSingleClickHandler`). | app/src/main/java/com/example/aiddproject/awarddetail/ui/AwardDetailScreen.kt
- [ ] T085 [US6] Update `AwardDetailScreen` — replace the stub `onBellClick = {}` with the NotificationsSheet reveal pattern (mirror `HomeScreen`'s `rememberModalBottomSheetState` + `LaunchedEffect(showSheet)` mechanic). Bell badge count flows from `notificationsSummaryRepository.observe()` via Hilt — same source as Home. | app/src/main/java/com/example/aiddproject/awarddetail/ui/AwardDetailScreen.kt
- [ ] T086 [US5] Update `AwardDetailScreen` — replace the stub `onLanguageSelected = {}` with `localeViewModel.setLanguage(it)`. The `LanguageSelector` itself is unchanged (lives in `core/locale/ui/`); only the upstream callback gets rewired. | app/src/main/java/com/example/aiddproject/awarddetail/ui/AwardDetailScreen.kt

**Checkpoint**: every header control behaves identically to Home;
language switch on Award Detail re-renders the chrome in a single
recomposition (no Activity recreation per SC-004 / TR-006);
notifications bell opens the sheet; search opens the Search route.

---

## Phase 7: Polish & Cross-Cutting Concerns

**Purpose**: telemetry, accessibility walkthrough, visual fidelity vs
Figma, performance instrumentation, image-resilience verification,
final QA gate, doc updates.

- [ ] T087 [P] Add telemetry breadcrumbs to `AwardDetailViewModel`: `award_detail.entered` on init (with `awardId` only — no PII per TR-002), `award_detail.category_changed` on dropdown switch (already wired in T063), `award_detail.retry` on retry. All emitted via `Timber.tag("AwardDetailTelemetry")` and scrubbed by the existing `SecureTimberTree` from Home Phase 10. | app/src/main/java/com/example/aiddproject/awarddetail/ui/AwardDetailViewModel.kt
- [ ] T088 [P] Wrap the `loadAward(id)` call in `Trace.beginSection("AwardDetail.load")` / `Trace.endSection()` so Android Profiler / `systrace` can verify the TR-001 / SC-001 / SC-002 budgets (≤ 800 ms warm, ≤ 2 s cold, ≤ 1.5 s dropdown switch). No automated perf test in this slice — manual stopwatch verification at T093 visual smoke. | app/src/main/java/com/example/aiddproject/awarddetail/ui/AwardDetailViewModel.kt
- [ ] T089 [P] Add `Modifier.testTag(...)` to every interactive control on `AwardDetailScreen` for instrumented test discoverability: `TEST_TAG_AWARD_DETAIL_RETRY`, `TEST_TAG_AWARD_DROPDOWN_TRIGGER`, `TEST_TAG_AWARD_DROPDOWN_MENU`, `TEST_TAG_AWARD_BADGE_IMAGE`. Lives in a companion object or top-level `const`s at the bottom of `AwardDetailScreenContent.kt`. | app/src/main/java/com/example/aiddproject/awarddetail/ui/AwardDetailScreenContent.kt
- [ ] T090 [P] Manual TalkBack walkthrough on emulator-5554 — follow the documented focus order from `spec.md` § TR-003 + `AwardDetailFocusOrderTest`. Capture before/after recording if a regression is found. (No file change unless an a11y bug surfaces.) | (no file)
- [ ] T091 Visual fidelity audit: capture `AwardDetailScreen` screenshot on emulator-5554 → compare to MoMorph `get_frame_image` output for `c-QM3_zjkG` → query `query_section` for any divergent node (header alignment, dropdown chrome, badge image bounds, Kudos block spacing) and iterate per Language Dropdown Phase 6 (T045–T048) discipline. File one bug-fix task per divergence found. | (no file — may file follow-on T### tasks)
- [ ] T092 [P] TR-007 image resilience verification: install a debug build; toggle airplane mode mid-fetch; verify the badge image AND the Kudos banner image both fall back to their placeholders. (Already covered by T030 + T071 in automation; this is the manual smoke pass.) | (no file)
- [ ] T093 [P] Update Home's `spec.md` § Notes — add a sentence: "Tapping **Chi tiết** on an awards-carousel card navigates to `[iOS] Award_Top talent` (`c-QM3_zjkG`); the `Award` model from `home/domain/` is the dropdown list source on that screen, and the new `detail(id, locale)` repository method ships the full payload." | .momorph/specs/OuH1BUTYT0-iOS-Home/spec.md
- [ ] T094 [P] Update `Routes.kt` KDoc — change the comment on `AWARD_DETAIL_PATTERN` from "placeholder" wording to reflect that the route now resolves to the real `AwardDetailScreen`. | app/src/main/java/com/example/aiddproject/navigation/Routes.kt
- [ ] T095 Run full Quality Gate: `./gradlew lint ktlintCheck assembleDebug testDebugUnitTest compileDebugAndroidTestKotlin` — every step must be green. `connectedDebugAndroidTest` is deferred to CI per the Login Phase 7 carry-over (Q4 from Login spec). | (no file)

---

## Dependencies & Execution Order

### Phase Dependencies

- **Phase 0 (Asset Preparation)** — no code dependencies; can run in
  parallel with Phase 1.
- **Phase 1 (Setup)** — no dependencies; can run in parallel with
  Phase 0. Localized strings + nav route confirmation.
- **Phase 2 (Foundational)** — depends on Phase 1 (strings must exist
  for `Error(messageRes)` references). **Blocks Phases 3–6.**
  - Tests T008–T013 [P] before implementation T015–T019.
  - T014 (parity test verification) runs after T003–T006 land.
  - T015 + T016 (domain types) before T017 (repository signature)
    before T018/T019 (impls).
- **Phase 3 (US1+US8 MVP)** — depends on Phase 2 completion. **Blocks
  Phases 4–6.**
  - VM tests T020–T025 [P] before T035 (VM impl).
  - UI tests T026–T033 [P] before T036–T041 (composables + screen).
  - T034 (UiState) before T035 (VM) before T040 (Content) before T041
    (Screen) before T042 (nav wire).
- **Phase 4 (US2)** — depends on Phase 3 (the dropdown wires into
  `HighlightBlock` which Phase 3 stubbed).
  - 14 dropdown tests T043–T056 [P] + 4 VM tests T057–T060 [P] before
    T061 (dropdown impl) + T062 (HighlightBlock wire) + T063 (VM
    method) + T064 (Screen + VM update).
- **Phase 5 (US3+US4)** — depends on Phase 3 (the Scaffold's bottomBar
  is already wired; Phase 5 only adds callbacks). Phase 5 can run in
  parallel with Phase 4 but they share `AwardDetailScreen.kt` so
  commits must be sequenced.
  - Tests T065–T071 [P] before impl T072–T075.
- **Phase 6 (US5+US6+US7)** — depends on Phase 3 (the Scaffold's
  topBar is already wired; Phase 6 only adds callbacks). Can run in
  parallel with Phase 5 — different `AwardDetailScreen` callback
  set, but the file is shared so sequence commits.
  - Tests T076–T083 [P] before impl T084–T086.
- **Phase 7 (Polish)** — depends on Phases 3–6 complete (visual + a11y
  smoke needs the full screen; full QA gate is the final step).

### Within Each Phase

- Test tasks marked [P] all touch one or two test files; authoring is
  parallelizable but **commits must be sequenced** when they touch
  the same file (e.g., all 14 dropdown tests in
  `AwardCategoryDropdownTest.kt` — author in parallel, commit in
  groups of 4–5).
- Composable tasks marked [P] each create separate files — fully
  parallel.
- ViewModel modifications (T035, T063, T072, T087, T088) all touch
  the same `AwardDetailViewModel.kt` — sequence sequentially even
  though they belong to different phases.
- `AwardDetailScreen.kt` modifications (T041, T064, T073, T074, T084,
  T085, T086) — same file, sequence sequentially.
- `AppNavigation.kt` modifications (T042, T075) — same file, sequence.

### Parallel Opportunities

- **Phase 0**: T001 + T002 [P]
- **Phase 1**: T003 + T004 + T005 + T006 + T007 [P]
- **Phase 2**: T008–T013 (test authoring) all [P]; T015 [P]; T019 [P]
- **Phase 3**: T020–T025 (VM tests) [P]; T026–T033 (UI tests) [P]; T036–T039 (composables) [P]
- **Phase 4**: T043–T056 (dropdown tests) + T057–T060 (VM tests) all [P]
- **Phase 5**: T065–T071 [P]
- **Phase 6**: T076–T083 [P]
- **Phase 7**: T087 + T088 + T089 + T090 + T092 + T093 + T094 [P]; T091 + T095 sequential at end

### Phase parallelism

- Phase 0 ∥ Phase 1 (no overlap)
- Phase 5 ∥ Phase 6 (different VM/Screen callback sets; sequence
  commits on shared files)
- Phase 4 generally NOT parallel with Phase 5/6 because the dropdown's
  `HighlightBlock` integration changes the same `AwardDetailScreenContent.kt`
  region that Phase 5's bottom-nav wiring touches.

---

## Implementation Strategy

### MVP First (Recommended)

1. **Phase 0 + Phase 1 + Phase 2** — foundation (~14 tasks, ~1 day).
2. **Phase 3** — US1+US8 vertical slice (~23 tasks, ~1.5 days). 🎯 **STOP and VALIDATE**: on emulator-5554, tap Top Talent from Home → screen opens with full chrome → body populated → Retry flow works → 401 deep-link bounces. This is the smallest shippable cut.
3. **Phase 4** — US2 dropdown (~22 tasks, ~1.5 days). Validate against `AwardCategoryDropdownTest` (14 contract tests + 4 VM tests).
4. **Phase 5 + Phase 6** — US3/US4/US5/US6/US7 in parallel (~22 tasks, ~1 day). Validate the full screen against `spec.md` § Acceptance Scenarios.
5. **Phase 7** — polish + final QA gate (~9 tasks, ~0.5 day).

**Total estimated effort**: 4–5 days for a single developer at the
pace demonstrated by the Home + Language Dropdown phases (matches
`plan.md` § Estimated Complexity).

### Incremental Delivery

Phase 3 is the smallest shippable cut. Phase 4 (dropdown) is the next
must-have for the screen to feel "complete". Phases 5 + 6 can ship
together as one PR (they touch the same callback set). Phase 7 is
last and **non-blocking** for merge — telemetry, perf instrumentation,
and visual fidelity audits can land in a follow-on PR if needed.

---

## Notes

- **Commit cadence**: granular commits per task or coherent task group
  (matches the project's auto-memory preference). Mark tasks complete
  as you go: `[x]`. The Quality Gate must be green before each
  commit (per the project's auto-memory `feedback_commit_per_task`
  note).
- **Test-first discipline**: every "Tests First" block lists tests
  that MUST be authored and committed before the corresponding
  implementation. Constitution Principle V.
- **Single-click suppression** (TR-004) is mandatory on every
  navigation / retry control. Use `rememberSingleClickHandler` from
  `com.example.aiddproject.core.ui`. Pattern is established in
  `HomeFab.kt`, `KudosSection.kt`, `LanguageSelector.kt`.
- **No new dependencies**: every required library (Compose, Hilt,
  Supabase-kt, Coil, Timber, kotlinx.serialization) is already
  pinned in `gradle/libs.versions.toml`.
- **Visual specs**: any task referencing pixel-level fidelity
  (`HighlightBlock`, `AwardCategoryDropdown`, `AwardHeroBlock`,
  `AwardInfoBlock`) fetches values at task-execution time via
  MoMorph `query_section` / `get_node` for the Node IDs in
  `spec.md` § Screen Components — NOT enumerated here per
  Constitution Principle II.
- **Resolved Q6 + Q7 (Deferred)**: backend confirmation happens at
  T018 (Postgrest detail query). The implementer picks Path A (per-
  locale columns) vs Path B (`Accept-Language` header) based on what
  ships in production, and updates T010's test assertions accordingly.
- **`connectedDebugAndroidTest`**: deferred to CI per the Login
  Phase 7 carry-over. Local quality gate excludes it (matches the
  Language Dropdown's T044 convention).
