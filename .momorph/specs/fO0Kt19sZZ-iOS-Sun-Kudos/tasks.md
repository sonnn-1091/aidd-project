# Tasks: Sun*Kudos hub

**Frame**: `fO0Kt19sZZ-iOS-Sun-Kudos`
**Plan**: `plan.md` (ratified, reviewed twice 2026-05-12)
**Spec**: `spec.md` (ratified, reviewed three times 2026-05-12)
**Scope decision (2026-05-12)**: User selected Mức A — full 13-phase
task list (~180 tasks). MVP slice = Phases 1–8 inclusive (Phase 1
Setup → Phase 8 US6+US7 nav). `design-style.md` prerequisite
waived per Constitution Principle II (canonical project pattern).

---

## Task Format

```
- [ ] T### [P?] [Story?] Description | file/path
```

- **[P]**: No dependency on the immediate predecessor — either
  (a) different file from neighbors, OR (b) inside the same Compose
  test file but with no inter-test dependency (each `setContent { … }`
  runs independently). Matches the canonical convention established
  by Top Talent tasks.md.
- **[Story]**: `[USn]` for tasks inside a user-story phase. NO label
  on Setup / Foundational / Polish.

---

## Phase mapping (plan → tasks.md)

| Tasks Phase | Plan Phase | Scope |
|---|---|---|
| Phase 1 (Setup) | plan Phase 0 | Assets + deps + scaffolding |
| Phase 2 (Foundational) | plan Phase 1 | Domain + states + repo + Hilt |
| Phase 3 [US1] | plan Phase 2 | 🎯 MVP — render hub end-to-end |
| Phase 4 [US2] | plan Phase 3 | Auth gate |
| Phase 5 [US3] | plan Phase 4 | Hashtag + Department filter |
| Phase 6 [US4] | plan Phase 5 | Highlight carousel |
| Phase 7 [US5] | plan Phase 6 | Like / unlike + star tier |
| Phase 8 [US6+US7] | plan Phase 7 | Send Kudos + Detail nav |
| Phase 9 [US13+US14+US8] | plan Phase 8 | Copy link + View all + Profile nav |
| Phase 10 [US9] | plan Phase 9 | Spotlight Board |
| Phase 11 [US10+US11] | plan Phase 10 | Stats + Secret Box |
| Phase 12 [US12] | plan Phase 11 | Top 10 |
| Phase 13 (Polish) | plan Phase 12 | A11y + final QA gate |

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Pull Figma assets, add the one new dependency
(`material-icons-extended`), add the one new route
(`SECRET_BOX_OPEN`), and create the `kudos/` package scaffold.

- [ ] T001 Verify the canonical Sun*Kudos Figma frame `fO0Kt19sZZ` is reachable via `mcp__momorph__list_design_items` + confirm the hub component map matches the spec § Component Behavior table. Read-only check; no edit. | (verification — no file)
- [ ] T002 [P] Pull Figma media files for the Sun*Kudos frame via `mcp__momorph__get_media_files` (`screenId=fO0Kt19sZZ`). Identify the KV Kudos hero artwork + Spotlight background + any per-section icons not in `material-icons-extended`. | (asset pull — no file)
- [ ] T003 [P] Add `material-icons-extended` to `gradle/libs.versions.toml` — new library entry `androidx-compose-material-icons-extended = { group = "androidx.compose.material", name = "material-icons-extended" }` (no version ref — BOM-managed). | gradle/libs.versions.toml
- [ ] T004 Wire `implementation(libs.androidx.compose.material.icons.extended)` into `app/build.gradle.kts` dependency block, near the existing M3 lines. | app/build.gradle.kts
- [ ] T005 [P] Add the new route constant `const val SECRET_BOX_OPEN: String = "route_secret_box_open"` to `Routes.kt`. KDoc cites US11 + delta-spec dependency on `kQk65hSYF2` (Open Secret Box animation, out of scope). | app/src/main/java/com/example/aiddproject/navigation/Routes.kt
- [ ] T006 [P] Add the 25 new `kudos_*` string resources to `app/src/main/res/values/strings.xml`: section headers ("ALL KUDOS", "HIGHLIGHT KUDOS", "SPOTLIGHT BOARD"), filter labels ("Hashtag", "Phòng ban"), empty/error copy ("Hiện tại chưa có Kudos nào.", "Chưa có dữ liệu"), Send Kudos placeholder ("Hôm nay, bạn muốn gửi kudos đến ai?"), Copy Link Snackbar text ("Link copied — ready to share!"), `kudos_copy_link_url_template = https://saa.sun-asterisk.com/kudos/%1$s` (Q-K-4), 12 A11y `contentDescription` templates from spec § Accessibility. | app/src/main/res/values/strings.xml
- [ ] T007 [P] Mirror the new keys into `app/src/main/res/values-en/strings.xml` (English copies; Vietnamese copy stays in `values/`). The existing `StringResourceParityTest` will fail-fast at unit-test time if any new key is missing from this file. | app/src/main/res/values-en/strings.xml
- [ ] T008 Create the new feature package `com.example.aiddproject.kudos/` with empty subpackages `ui/`, `ui/components/`, `data/`, `data/dto/`, `domain/`, `domain/states/`. Empty `package-info.kt` files OK to anchor each. | app/src/main/java/com/example/aiddproject/kudos/ (and subdirs)
- [ ] T009 [P] Drop the bundled KV Kudos hero artwork (from T002) into `app/src/main/res/drawable-mdpi/kudos_hero_artwork.png` if MoMorph returns a composite. If null, defer to per-frame text overlay fallback (mirrors the AwardHeroBlock pattern). | app/src/main/res/drawable-mdpi/kudos_hero_artwork.png
- [ ] T010 [P] Drop any per-section icons not satisfied by `material-icons-extended` into `app/src/main/res/drawable-mdpi/ic_kudos_*.png`. Common need: x2 fire badge if no extended icon matches. | app/src/main/res/drawable-mdpi/ic_kudos_*.png

---

## Phase 2: Foundation (Blocking Prerequisites)

**Purpose**: Domain models, sealed state interfaces, repository
interface + DEMO impl + Hilt module. NO user-story work begins
until this phase passes.

**⚠️ CRITICAL**: All Phase 3+ tasks depend on this phase.

### Domain models (US1+ basis)

- [ ] T011 [P] Create `Kudos` data class in `domain/Kudos.kt` with: id, sender, recipient, message, title, hashtags, photos, created_at, heart_count, liked_by_current_user, sender_visible_to_me (Q-K-3), like_disabled_for_me (Q-K-5), anonymous_nickname, is_anonymous. | app/src/main/java/com/example/aiddproject/kudos/domain/Kudos.kt
- [ ] T012 [P] Create `Hashtag` data class (id, tag_name) in `domain/Hashtag.kt`. | app/src/main/java/com/example/aiddproject/kudos/domain/Hashtag.kt
- [ ] T013 [P] Create `Department` data class (id, name) in `domain/Department.kt`. | app/src/main/java/com/example/aiddproject/kudos/domain/Department.kt
- [ ] T014 [P] Create `SpotlightGraph` + `SunnerNode` + `KudosEdge` data classes in `domain/SpotlightGraph.kt`. Include `total_kudos_count` field per Q-K-2 resolution (count lives on the graph response). | app/src/main/java/com/example/aiddproject/kudos/domain/SpotlightGraph.kt
- [ ] T015 [P] Create `PersonalStats` data class (kudos_received, kudos_sent, hearts_received, secret_boxes_opened, secret_boxes_unopened) in `domain/PersonalStats.kt`. | app/src/main/java/com/example/aiddproject/kudos/domain/PersonalStats.kt
- [ ] T016 [P] Create `GiftRecipient` data class (user_id, full_name, avatar_url, reward_name) in `domain/GiftRecipient.kt`. | app/src/main/java/com/example/aiddproject/kudos/domain/GiftRecipient.kt
- [ ] T017 [P] Create `Reaction` + `SystemFlags` (specialDayActive, x2BonusActive — Q-K-1) + `SecretBoxRef` + `SecretBoxReward` + `KudosPage` + `KudosFilter` + `SunnerMatch` + `SnackbarMessage` + `SpotlightSearchResult` (sealed: Idle/Loading/Match(node)/NoMatch) sealed/data classes in `domain/` files of matching names. | app/src/main/java/com/example/aiddproject/kudos/domain/ (multiple files)

### Sealed section states

- [ ] T018 [P] Create `KudosHighlightState` sealed interface in `domain/states/KudosHighlightState.kt` with cases `Loading / Empty / Loaded(items: List<Kudos>) / Error(messageRes: Int)`. Mirror Home's `AwardsState` shape. | app/src/main/java/com/example/aiddproject/kudos/domain/states/KudosHighlightState.kt
- [ ] T019 [P] Create `AllKudosState` sealed interface with `Loading / Empty / Loaded(items, hasMore, nextPage) / Error`. | app/src/main/java/com/example/aiddproject/kudos/domain/states/AllKudosState.kt
- [ ] T020 [P] Create `SpotlightState` sealed interface with `Loading / Empty / Loaded(graph) / Error`. | app/src/main/java/com/example/aiddproject/kudos/domain/states/SpotlightState.kt
- [ ] T021 [P] Create `PersonalStatsState` sealed interface with `Loading / Loaded(stats) / Error`. | app/src/main/java/com/example/aiddproject/kudos/domain/states/PersonalStatsState.kt
- [ ] T022 [P] Create `TopTenState` sealed interface with `Loading / Empty / Loaded(items) / Error`. | app/src/main/java/com/example/aiddproject/kudos/domain/states/TopTenState.kt

### Repository contract

- [ ] T023 Create `KudosRepository` interface in `data/KudosRepository.kt` with the 14 suspend methods from plan § Architecture Decisions § KudosRepository interface (listHighlight, listKudos, detail, addReaction, removeReaction, listHashtags, listDepartments, loadSpotlightGraph, searchSunner, personalStats, systemFlags, nextUnopenedBox, openSecretBox, listRecentGiftRecipients). Each returns `Result<T>`. | app/src/main/java/com/example/aiddproject/kudos/data/KudosRepository.kt
- [ ] T024 [P] Create `DemoKudosRepository.kt` implementing every method with seed fixtures: 10 kudos (varying heart counts for carousel ordering test, mix of `is_anonymous`, mix of `sender_id`/`recipient_id` to exercise Q-K-3 + Q-K-5), 5 hashtags, 5 departments, 8 spotlight graph nodes with edges + a `total_kudos_count = 388` (matches Figma example), canonical PersonalStats (Kudos received=42, sent=18, hearts=156, boxes_opened=3, boxes_unopened=2), SystemFlags(specialDayActive=false, x2BonusActive=false), 1 unopened secret box, 8 GiftRecipient rows. | app/src/main/java/com/example/aiddproject/kudos/data/DemoKudosRepository.kt
- [ ] T025 [P] Create `SupabaseKudosRepository.kt` scaffold — each method `TODO("supabase impl in follow-on")`. Production impl deferred per plan's DEMO-first strategy. | app/src/main/java/com/example/aiddproject/kudos/data/SupabaseKudosRepository.kt
- [ ] T026 Create `KudosRepositoryModule.kt` (Hilt module) binding `KudosRepository` to `DemoKudosRepository` when `BuildConfig.DEMO_MODE = true`, else `SupabaseKudosRepository`. Mirrors `home/data/AwardsRepositoryModule.kt`. | app/src/main/java/com/example/aiddproject/kudos/data/KudosRepositoryModule.kt
- [ ] T027 [P] Write `DemoKudosRepositoryTest.kt` (~12 unit assertions) pinning the DEMO fixture: list-highlight returns top-5 sorted; list-kudos paginates; reactions throw NoSuchElement for unknown id; spotlight returns total_count + graph; personalStats values match canonical; nextUnopenedBox returns a non-null first call then null after openSecretBox. | app/src/test/java/com/example/aiddproject/kudos/data/DemoKudosRepositoryTest.kt

**Checkpoint**: Domain layer compiles + DEMO repo tests green. Phase
3 can proceed.

---

## Phase 3: User Story 1 — Render Sun*Kudos hub (Priority: P1) 🎯 MVP

**Goal**: Replace `AppNavigation.kt:130` placeholder with a real
`KudosScreen` that renders every section's state (Loading / Empty /
Loaded / Error) using DEMO data, wrapped in `PullToRefreshBox`.

**Independent Test**: Launch DEMO build, tap Kudos bottom-nav tab,
observe all five section blocks render with seed data; pull down to
trigger refresh — all sections re-fetch; verify Empty/Error states
by toggling DEMO repo flags.

### State + ViewModel scaffold (US1)

- [ ] T028 [US1] Create `KudosUiState.kt` matching the plan's
concrete data class shape (11 fields + Empty companion). | app/src/main/java/com/example/aiddproject/kudos/ui/KudosUiState.kt
- [ ] T029 [US1] Create `KudosViewModel.kt` (Hilt `@HiltViewModel`) injecting `KudosRepository` + `LanguagePreferenceRepository` + `SavedStateHandle`. Expose `uiState: StateFlow<KudosUiState>` initialized to `KudosUiState.Empty`. | app/src/main/java/com/example/aiddproject/kudos/ui/KudosViewModel.kt
- [ ] T030 [P] [US1] Write `KudosViewModelTest.init_emits_Empty_then_section_Loaded_states` — fixture seeds DEMO repo; assert init() flips all 5 sections from Loading → Loaded within `runTest`. | app/src/test/java/com/example/aiddproject/kudos/ui/KudosViewModelTest.kt
- [ ] T031 [US1] Implement `KudosViewModel.init { … }` block that kicks off 5 parallel fetches via `coroutineScope { async ... awaitAll }` (per plan's pull-to-refresh snippet). Each fetch updates only its section's slice of `KudosUiState` via `_uiState.update`. | app/src/main/java/com/example/aiddproject/kudos/ui/KudosViewModel.kt
- [ ] T032 [P] [US1] Write `KudosViewModelTest.init_section_failure_isolates_to_one_state_Error` — one repo method returns Result.failure; assert only that section flips to Error while others Loaded. | app/src/test/java/com/example/aiddproject/kudos/ui/KudosViewModelTest.kt
- [ ] T033 [P] [US1] Write `KudosViewModelTest.onPullToRefresh_gates_when_already_refreshing` — call onPullToRefresh twice rapidly; assert only one fetch cycle fires. Per plan's `isRefreshing` gate. | app/src/test/java/com/example/aiddproject/kudos/ui/KudosViewModelTest.kt
- [ ] T034 [US1] Implement `KudosViewModel.onPullToRefresh()` per plan's Kotlin snippet — `isRefreshing` gate + parallel awaitAll. | app/src/main/java/com/example/aiddproject/kudos/ui/KudosViewModel.kt

### Stateless content composable + section scaffolds (US1)

- [ ] T035 [US1] Create `KudosScreenContent.kt` — stateless `@Composable` taking `state: KudosUiState` + 12 callbacks (`onPullToRefresh`, `onSelectHashtag`, `onSelectDepartment`, `onHashtagChipTap`, `onCardTap`, `onHeartTap`, `onCopyLink`, `onSendKudos`, `onOpenSecretBox`, `onProfileTap`, `onViewAllKudos`, `onSpotlightSearchChange`). Body is a Scaffold with HomeHeader topBar + HomeBottomBar bottomBar, body is `PullToRefreshBox` wrapping a LazyColumn assembling the 5 sections in order. | app/src/main/java/com/example/aiddproject/kudos/ui/KudosScreenContent.kt
- [ ] T036 [P] [US1] Create `KudosHeroBanner.kt` rendering the static banner (logo + "Hệ thống ghi nhận và cảm ơn" + "KUDOS"). | app/src/main/java/com/example/aiddproject/kudos/ui/components/KudosHeroBanner.kt
- [ ] T037 [P] [US1] Create `SendKudosCta.kt` rendering the pill button with placeholder text; invokes `onSendKudos`. Uses `rememberSingleClickHandler` (TR-004). Min 48dp touch target. | app/src/main/java/com/example/aiddproject/kudos/ui/components/SendKudosCta.kt
- [ ] T038 [P] [US1] Create stub `HighlightFilterRow.kt` rendering two pill triggers with "Hashtag" + "Phòng ban" labels. Tap is a no-op stub (full impl in Phase 5). | app/src/main/java/com/example/aiddproject/kudos/ui/components/HighlightFilterRow.kt
- [ ] T039 [P] [US1] Create stub `HighlightCarousel.kt` that switches on `KudosHighlightState` → Loading / Empty / Error placeholders or a non-functional list of cards (full carousel in Phase 6). | app/src/main/java/com/example/aiddproject/kudos/ui/components/HighlightCarousel.kt
- [ ] T040 [P] [US1] Create stub `SpotlightBoard.kt` rendering section title + a Loading/Empty/Error placeholder switch on `SpotlightState`. Full pan/zoom + search in Phase 10. | app/src/main/java/com/example/aiddproject/kudos/ui/components/SpotlightBoard.kt
- [ ] T041 [P] [US1] Create stub `AllKudosFeed.kt` switching on `AllKudosState` → Loading / Empty / Error or a non-functional list of KudosFeedCard placeholders (full impl in Phase 7). | app/src/main/java/com/example/aiddproject/kudos/ui/components/AllKudosFeed.kt
- [ ] T042 [P] [US1] Create stub `PersonalStatsPanel.kt` rendering 5 stat tiles using `state.stats` (Loaded case; placeholder Loading/Error otherwise). x2 fire badge full impl in Phase 11. | app/src/main/java/com/example/aiddproject/kudos/ui/components/PersonalStatsPanel.kt
- [ ] T043 [P] [US1] Create stub `OpenSecretBoxCta.kt` rendering the button (full disabled-state + nav in Phase 11). | app/src/main/java/com/example/aiddproject/kudos/ui/components/OpenSecretBoxCta.kt
- [ ] T044 [P] [US1] Create stub `TopTenRecipients.kt` switching on `TopTenState` → empty placeholder or non-functional rows (full impl Phase 12). | app/src/main/java/com/example/aiddproject/kudos/ui/components/TopTenRecipients.kt
- [ ] T045 [P] [US1] Create stub `CopyLinkSnackbarHost.kt` with `SnackbarHostState`; render in the Scaffold's snackbarHost slot. Full Copy Link in Phase 9. | app/src/main/java/com/example/aiddproject/kudos/ui/components/CopyLinkSnackbarHost.kt

### Hilt-injected entry + nav wiring (US1)

- [ ] T046 [US1] Create `KudosScreen.kt` Hilt-injected entry composable that collects `KudosViewModel.uiState` as state, delegates to `KudosScreenContent` with the 12 callbacks wired to VM functions + nav callbacks. | app/src/main/java/com/example/aiddproject/kudos/ui/KudosScreen.kt
- [ ] T047 [US1] Modify `AppNavigation.kt` line 130: replace `composable(Routes.KUDOS_OVERVIEW) { PlaceholderScreen(label = "Kudos overview") }` with `composable(Routes.KUDOS_OVERVIEW) { KudosScreen(onNavigateToSendKudos = …, onNavigateToKudoDetail = …, onNavigateToAllKudos = …, onNavigateToProfile = …, onNavigateToSecretBoxOpen = …) }`. Wire each callback to `navController.navigate(Routes.X)`. | app/src/main/java/com/example/aiddproject/navigation/AppNavigation.kt
- [ ] T048 [P] [US1] Write `KudosScreenTest.scaffold_renders_all_five_sections_when_all_states_Loaded` — drives KudosScreenContent with a fixed `KudosUiState` having every section in Loaded state; asserts every section block is visible via test tags. | app/src/androidTest/java/com/example/aiddproject/kudos/KudosScreenTest.kt
- [ ] T049 [P] [US1] Write `KudosScreenTest.shows_empty_strings_when_sections_Empty` — Empty state per section renders the localized "Hiện tại chưa có Kudos nào." / "Chưa có dữ liệu" strings (parametric over section). | app/src/androidTest/java/com/example/aiddproject/kudos/KudosScreenTest.kt
- [ ] T050 [P] [US1] Write `KudosScreenTest.shows_error_inline_with_retry_when_sections_Error` — Error state per section renders the inline error text + Retry control. | app/src/androidTest/java/com/example/aiddproject/kudos/KudosScreenTest.kt
- [ ] T051 [P] [US1] Write `KudosScreenTest.pullToRefresh_invokes_callback` — using `performTouchInput { swipeDown() }` on the PullToRefreshBox; assert `onPullToRefresh` callback fired exactly once. | app/src/androidTest/java/com/example/aiddproject/kudos/KudosScreenTest.kt
- [ ] T052 [US1] Add test tags (`TEST_TAG_KUDOS_SCREEN`, `TEST_TAG_KUDOS_HERO`, `TEST_TAG_KUDOS_HIGHLIGHT`, `TEST_TAG_KUDOS_SPOTLIGHT`, `TEST_TAG_KUDOS_FEED`, `TEST_TAG_KUDOS_STATS`, `TEST_TAG_KUDOS_TOP_TEN`) as `const val` exports in `KudosScreenContent.kt`. | app/src/main/java/com/example/aiddproject/kudos/ui/KudosScreenContent.kt

**Checkpoint** 🎯 **MVP** — the hub renders end-to-end with DEMO
data. Stop and validate before Phase 4.

---

## Phase 4: User Story 2 — Auth gate (Priority: P1)

**Goal**: Unauthenticated/expired-session users are redirected to
Login when navigating to or while on Sun*Kudos.

**Independent Test**: Launch app with no session → tap Kudos tab →
expect Login. With valid session → expire it (set token to expired)
→ trigger any repo call → expect redirect.

- [ ] T053 [US2] Confirm `core/session/SessionGate` already wraps the entire authenticated route tree (existing infra). Read-only verification — no edit. | (verification — no file)
- [ ] T054 [US2] Wire `KudosViewModel` to invoke `AuthRedirectController.onAuthExpired()` when any repo Result.failure carries a 401 / `HttpRequestException(status = 401)` (same pattern as `AwardDetailViewModel`). | app/src/main/java/com/example/aiddproject/kudos/ui/KudosViewModel.kt
- [ ] T055 [P] [US2] Write `KudosViewModelTest.401_repository_failure_emits_AuthExpired_event` — stub repo to return 401; assert the event is forwarded. | app/src/test/java/com/example/aiddproject/kudos/ui/KudosViewModelTest.kt
- [ ] T056 [P] [US2] Write `KudosAuthRedirectTest.unauthenticated_user_redirected_to_login` — mount KudosScreen with no session; assert the screen does not mount AND `Routes.LOGIN` is the current backstack top. Mirrors `HomeAuthRedirectTest`. | app/src/androidTest/java/com/example/aiddproject/kudos/KudosAuthRedirectTest.kt

**Checkpoint**: Auth gate parity with Home + Award Detail.

---

## Phase 5: User Story 3 — Filter Highlight + All Kudos (Priority: P1)

**Goal**: Hashtag + Phòng ban bottom-sheet filters; AND-combined
query; carousel resets to card 1 on filter change; tapping a chip
in any feed card re-applies the Hashtag filter.

**Independent Test**: Apply Hashtag = "#teamwork" → expect both
Highlight + All Kudos filtered. Apply Department = "Division A" →
expect AND filter. Swipe carousel to card 3, change filter → expect
reset to card 1. Tap a hashtag chip in All Kudos → Hashtag filter
applied + both feeds refresh.

- [ ] T057 [US3] Create `HashtagFilterDropdown.kt` — `ModalBottomSheet` listing hashtags from `state.hashtags` (or loaded on tap via callback); on row tap fires `onSelectHashtag(hashtag.id)` and dismisses. Re-tapping the same hashtag clears the selection. | app/src/main/java/com/example/aiddproject/kudos/ui/components/HashtagFilterDropdown.kt
- [ ] T058 [P] [US3] Create `DepartmentFilterDropdown.kt` — same shape as HashtagFilterDropdown for departments. | app/src/main/java/com/example/aiddproject/kudos/ui/components/DepartmentFilterDropdown.kt
- [ ] T059 [US3] Replace `HighlightFilterRow.kt` stub with full impl: two pill triggers showing the active hashtag/department name (or "Hashtag" / "Phòng ban" placeholder). Tap opens the corresponding bottom sheet. Localized contentDescription per spec § A11y. | app/src/main/java/com/example/aiddproject/kudos/ui/components/HighlightFilterRow.kt
- [ ] T060 [US3] Implement `KudosViewModel.onSelectHashtag(hashtagId: String?)` — update `_uiState.selectedHashtagId`, kick off parallel re-fetch of Highlight + All Kudos with the new filter, and reset the highlight pager to page 0 (US3 scenario 3). | app/src/main/java/com/example/aiddproject/kudos/ui/KudosViewModel.kt
- [ ] T061 [US3] Implement `KudosViewModel.onSelectDepartment(deptId: String?)` — symmetric to T060. Both filters AND-combine via the `KudosFilter` data class. | app/src/main/java/com/example/aiddproject/kudos/ui/KudosViewModel.kt
- [ ] T062 [US3] Implement `KudosViewModel.onHashtagChipTap(hashtagId: String)` — sets the Hashtag filter (same path as the dropdown selection), does NOT navigate. | app/src/main/java/com/example/aiddproject/kudos/ui/KudosViewModel.kt
- [ ] T063 [P] [US3] Write `KudosViewModelTest.onSelectHashtag_AND_combined_with_department_refetches_both_feeds` — fixture selects both filters; assert repo called with `KudosFilter(hashtagId=…, departmentId=…)`. | app/src/test/java/com/example/aiddproject/kudos/ui/KudosViewModelTest.kt
- [ ] T064 [P] [US3] Write `KudosViewModelTest.filter_change_resets_carousel_page_to_zero` — pager state at page=2; trigger filter change; assert pager is reset. | app/src/test/java/com/example/aiddproject/kudos/ui/KudosViewModelTest.kt
- [ ] T065 [P] [US3] Write `KudosViewModelTest.filter_change_cancels_in_flight_fetch` — issue filter A then filter B before A completes; assert A's result NEVER reaches state. | app/src/test/java/com/example/aiddproject/kudos/ui/KudosViewModelTest.kt
- [ ] T066 [P] [US3] Write `HighlightFilterRowTest.opens_hashtag_bottom_sheet_on_trigger_tap` — instrumented Compose UI test. | app/src/androidTest/java/com/example/aiddproject/kudos/HighlightFilterRowTest.kt
- [ ] T067 [P] [US3] Write `HighlightFilterRowTest.selecting_hashtag_in_sheet_updates_trigger_label` — | app/src/androidTest/java/com/example/aiddproject/kudos/HighlightFilterRowTest.kt
- [ ] T068 [P] [US3] Write `HighlightFilterRowTest.selecting_department_emits_callback` — | app/src/androidTest/java/com/example/aiddproject/kudos/HighlightFilterRowTest.kt
- [ ] T069 [P] [US3] Write `KudosScreenTest.empty_string_renders_when_filter_yields_no_results` — fixture sets a filter that the DEMO repo returns empty for; assert both Highlight + All Kudos show "Hiện tại chưa có Kudos nào." | app/src/androidTest/java/com/example/aiddproject/kudos/KudosScreenTest.kt

**Checkpoint**: AND-combined filter works end-to-end; carousel resets
on change; chip tap re-applies.

---

## Phase 6: User Story 4 — Highlight carousel (Priority: P1)

**Goal**: `HorizontalPager` with top-5 cards (sorted by heart count
DESC), page indicator, swipe nav, active/faded card state.

**Independent Test**: Carousel shows 5 cards in heart-count-DESC
order. Swipe left/right advances/retreats and updates "1/5" indicator.
Side cards are visually de-emphasized.

- [ ] T070 [US4] Replace `HighlightCarousel.kt` stub with `HorizontalPager(state = rememberPagerState(pageCount = { items.size }))`. Wrap each page in a HighlightCard. Track active page index via pagerState.currentPage. | app/src/main/java/com/example/aiddproject/kudos/ui/components/HighlightCarousel.kt
- [ ] T071 [P] [US4] Create `HighlightCard.kt` rendering one kudos card: sender row (avatar + name + tier badge with Q-K-3 visibility branch) → direction arrow → recipient row (avatar + name + star tier per US5 scenario 6) → post time → title → message body (3-line truncate) → hashtag chips → action row (heart + Copy Link + Xem chi tiết). | app/src/main/java/com/example/aiddproject/kudos/ui/components/HighlightCard.kt
- [ ] T072 [P] [US4] Add `PageIndicator.kt` showing "1/5" format reading from pagerState.currentPage + items.size. | app/src/main/java/com/example/aiddproject/kudos/ui/components/PageIndicator.kt
- [ ] T073 [P] [US4] Add active/faded styling: when `pagerState.currentPage != pageIndex` apply `Modifier.alpha(0.5f)` to the card. Animated via Compose's built-in animation if performance allows. | app/src/main/java/com/example/aiddproject/kudos/ui/components/HighlightCarousel.kt
- [ ] T074 [P] [US4] Wire `KudosFilter` change to `pagerState.scrollToPage(0)` via a `LaunchedEffect(filter)` inside HighlightCarousel (or via the VM setting a one-shot reset event). | app/src/main/java/com/example/aiddproject/kudos/ui/components/HighlightCarousel.kt
- [ ] T075 [P] [US4] Write `HighlightCarouselTest.renders_top_five_cards_sorted_by_heart_count` — fixture has 8 cards; pager shows first 5 in heart_count DESC order. | app/src/androidTest/java/com/example/aiddproject/kudos/HighlightCarouselTest.kt
- [ ] T076 [P] [US4] Write `HighlightCarouselTest.swipe_left_advances_page_indicator` — `performTouchInput { swipeLeft() }`; assert page indicator shows "2/5". | app/src/androidTest/java/com/example/aiddproject/kudos/HighlightCarouselTest.kt
- [ ] T077 [P] [US4] Write `HighlightCarouselTest.center_card_is_visible_side_cards_faded` — via alpha semantic assertion or visual snapshot. | app/src/androidTest/java/com/example/aiddproject/kudos/HighlightCarouselTest.kt
- [ ] T078 [P] [US4] Write `HighlightCarouselTest.filter_change_resets_to_page_zero` — change filter via callback; assert pagerState.currentPage == 0. | app/src/androidTest/java/com/example/aiddproject/kudos/HighlightCarouselTest.kt

**Checkpoint**: Carousel functional; tests green.

---

## Phase 7: User Story 5 — Like / unlike + star tier (Priority: P1)

**Goal**: Heart icon toggle with optimistic update + rollback;
`like_disabled_for_me` gate (Q-K-5) for sender + recipient; +1 or
+2 hearts on special day (Q-K-1); star tier badge from server-side
`recipient.star_tier`.

**Independent Test**: Tap heart → red + count+1; tap again → grey
+ count-1. Sender's own kudos card heart disabled. Recipient's own
kudos card heart disabled. With specialDayActive=true, +1 becomes +2.

- [ ] T079 [US5] Create `HeartIcon.kt` Composable: state `liked: Boolean`, `count: Int`, `disabled: Boolean`. Renders filled red heart when liked, outlined grey otherwise. Disabled state grays out + intercepts tap. | app/src/main/java/com/example/aiddproject/kudos/ui/components/HeartIcon.kt
- [ ] T080 [P] [US5] Create `StarTierBadge.kt` — `tier: 0|1|2|3` → renders 0/1/2/3 star glyphs (uses material-icons-extended star icon). 0 → no render. | app/src/main/java/com/example/aiddproject/kudos/ui/components/StarTierBadge.kt
- [ ] T081 [US5] Implement `KudosViewModel.onHeartTap(kudosId: String)` per plan's optimistic snippet — pre-compute optimistic patch with +1 or +2 from `specialDayActive`; `applyKudosLocally(kudos)` single mutation point; on Result.failure restore + Snackbar.ReactionFailed. | app/src/main/java/com/example/aiddproject/kudos/ui/KudosViewModel.kt
- [ ] T082 [US5] Implement `applyKudosLocally(kudos)` helper on KudosViewModel — finds the kudos in highlight + allKudos slices of state and patches both. Single mutation point keeps the two feeds in sync. | app/src/main/java/com/example/aiddproject/kudos/ui/KudosViewModel.kt
- [ ] T083 [P] [US5] Write `KudosViewModelTest.onHeartTap_optimistic_increments_then_persists` — happy path; assert state mutates immediately + repo.addReaction called. | app/src/test/java/com/example/aiddproject/kudos/ui/KudosViewModelTest.kt
- [ ] T084 [P] [US5] Write `KudosViewModelTest.onHeartTap_failure_rolls_back_and_emits_snackbar` — repo returns Result.failure; assert state restores + snackbar set. | app/src/test/java/com/example/aiddproject/kudos/ui/KudosViewModelTest.kt
- [ ] T085 [P] [US5] Write `KudosViewModelTest.onHeartTap_disabled_for_sender` — kudos.like_disabled_for_me = true (sender); assert no state change + no repo call. | app/src/test/java/com/example/aiddproject/kudos/ui/KudosViewModelTest.kt
- [ ] T086 [P] [US5] Write `KudosViewModelTest.onHeartTap_disabled_for_recipient` — same gate applies (Q-K-5). | app/src/test/java/com/example/aiddproject/kudos/ui/KudosViewModelTest.kt
- [ ] T087 [P] [US5] Write `KudosViewModelTest.onHeartTap_uses_plus_two_on_special_day` — specialDayActive=true; assert heart_count delta = 2 in optimistic patch. | app/src/test/java/com/example/aiddproject/kudos/ui/KudosViewModelTest.kt
- [ ] T088 [P] [US5] Write `KudosViewModelTest.onHeartTap_synchronizes_highlight_and_feed_states` — same kudos in both sections; tap heart; assert both slices mutated. | app/src/test/java/com/example/aiddproject/kudos/ui/KudosViewModelTest.kt
- [ ] T089 [P] [US5] Write `HeartIconTest.disabled_icon_does_not_fire_callback` — instrumented Compose UI test; tap disabled icon; callback NOT fired. | app/src/androidTest/java/com/example/aiddproject/kudos/HeartIconTest.kt
- [ ] T090 [P] [US5] Write `HeartIconTest.toggle_renders_filled_red_when_liked` — assert color semantic when liked=true. | app/src/androidTest/java/com/example/aiddproject/kudos/HeartIconTest.kt
- [ ] T091 [P] [US5] Write `StarTierBadgeTest.renders_correct_star_count_for_each_tier` — parametric over tier 0/1/2/3. | app/src/androidTest/java/com/example/aiddproject/kudos/StarTierBadgeTest.kt
- [ ] T092 [P] [US5] Wire HighlightCard + KudosFeedCard to invoke `onHeartTap(kudosId)` via the heart icon. | app/src/main/java/com/example/aiddproject/kudos/ui/components/HighlightCard.kt + KudosFeedCard.kt

**Checkpoint**: Reactions + star tier working end-to-end; rollback path tested.

---

## Phase 8: User Story 6 — Send Kudos shortcut + User Story 7 — View detail (Priority: P1)

**Goal**: Tap Send Kudos pill → navigate to Routes.WRITE_KUDO.
Tap any card body / "Xem chi tiết" → navigate to Routes.KUDOS_DETAIL
(passing kudosId + is_anonymous for routing variant selection).

**Independent Test**: Tap CTA → expect Write Kudo placeholder
mounts. Tap a Highlight card body → expect Kudos Detail mounts.

- [ ] T093 [US6] Wire `KudosScreenContent.onSendKudos` callback through `KudosScreen` to `navController.navigate(Routes.WRITE_KUDO)`. SendKudosCta already invokes onSendKudos from T037. | app/src/main/java/com/example/aiddproject/kudos/ui/KudosScreen.kt + AppNavigation.kt
- [ ] T094 [P] [US6] Write `KudosScreenTest.send_kudos_pill_tap_fires_callback` — find SendKudosCta via test tag, performClick, assert onSendKudos fired exactly once. | app/src/androidTest/java/com/example/aiddproject/kudos/KudosScreenTest.kt
- [ ] T095 [US7] Wire HighlightCard "Xem chi tiết" + body tap to `onCardTap(kudos)` callback through to `navController.navigate(Routes.KUDOS_DETAIL)`. (Detail screen itself is `Routes.KUDOS_DETAIL` placeholder — Sun*Kudos hub spec doesn't ship it.) | app/src/main/java/com/example/aiddproject/kudos/ui/components/HighlightCard.kt + AppNavigation.kt
- [ ] T096 [P] [US7] Same wiring for KudosFeedCard body tap. | app/src/main/java/com/example/aiddproject/kudos/ui/components/KudosFeedCard.kt
- [ ] T097 [P] [US7] Write `HighlightCarouselTest.xem_chi_tiet_tap_fires_card_callback_with_kudos_id` — find Xem chi tiết button, performClick, assert callback received the right kudos.id. | app/src/androidTest/java/com/example/aiddproject/kudos/HighlightCarouselTest.kt
- [ ] T098 [P] [US7] Write `KudosFeedCardTest.body_tap_fires_card_callback_with_kudos_id` — symmetric for feed card. | app/src/androidTest/java/com/example/aiddproject/kudos/KudosFeedCardTest.kt

**Checkpoint**: Navigation wired; placeholders open. Real
destinations land when those specs ship.

---

## Phase 9: User Story 13 — Copy Link + User Story 14 — View all Kudos + User Story 8 — Profile nav (Priority: P3+P2+P2)

**Goal**: Copy Link copies the Q-K-4 URL to clipboard + shows
Snackbar. View All Kudos link → `Routes.KUDOS_FEED`. Sender /
recipient / Top 10 row taps → `Routes.PROFILE`. Anonymous sender
row is non-tappable for non-recipients (Q-K-3).

**Independent Test**: Tap Copy Link on a Highlight card →
clipboard has the URL + Snackbar shows. Tap "View all Kudos" → see
KUDOS_FEED placeholder. Tap sender avatar on a non-anonymous kudos
→ PROFILE placeholder. On an anonymous kudos as non-recipient, tap
sender row → no-op.

- [ ] T099 [US13] Implement `KudosViewModel.onCopyLink(kudosId: String)` — read `R.string.kudos_copy_link_url_template`, format with `kudosId`, write to `ClipboardManager` (provided via composition local), set `state.snackbar = SnackbarMessage.LinkCopied`. | app/src/main/java/com/example/aiddproject/kudos/ui/KudosViewModel.kt
- [ ] T100 [P] [US13] Replace `CopyLinkSnackbarHost.kt` stub with full impl: observe `state.snackbar`, push to `SnackbarHostState.showSnackbar(...)`, then `state.snackbar = null` to dismiss. | app/src/main/java/com/example/aiddproject/kudos/ui/components/CopyLinkSnackbarHost.kt
- [ ] T101 [P] [US13] Wire HighlightCard's Copy Link button to `onCopyLink(kudos.id)`. | app/src/main/java/com/example/aiddproject/kudos/ui/components/HighlightCard.kt
- [ ] T102 [P] [US13] Wire KudosFeedCard's Copy Link button to `onCopyLink(kudos.id)`. | app/src/main/java/com/example/aiddproject/kudos/ui/components/KudosFeedCard.kt
- [ ] T103 [P] [US13] Write `KudosViewModelTest.onCopyLink_writes_url_to_clipboard_and_sets_snackbar` — fake ClipboardManager; assert URL formatted with kudosId + snackbar set. | app/src/test/java/com/example/aiddproject/kudos/ui/KudosViewModelTest.kt
- [ ] T104 [P] [US13] Write `KudosScreenTest.copy_link_shows_snackbar_text` — tap Copy Link via test tag; assert "Link copied — ready to share!" snackbar visible. | app/src/androidTest/java/com/example/aiddproject/kudos/KudosScreenTest.kt
- [ ] T105 [US14] Create `ViewAllKudosLink.kt` — text link with arrow icon; invokes `onViewAllKudos` callback. Min 48dp touch target. | app/src/main/java/com/example/aiddproject/kudos/ui/components/ViewAllKudosLink.kt
- [ ] T106 [US14] Wire `onViewAllKudos` through `KudosScreen` → `navController.navigate(Routes.KUDOS_FEED)`. | app/src/main/java/com/example/aiddproject/kudos/ui/KudosScreen.kt + AppNavigation.kt
- [ ] T107 [P] [US14] Add ViewAllKudosLink to bottom of `AllKudosFeed.kt` body. | app/src/main/java/com/example/aiddproject/kudos/ui/components/AllKudosFeed.kt
- [ ] T108 [P] [US14] Write `KudosScreenTest.view_all_kudos_link_fires_callback` — | app/src/androidTest/java/com/example/aiddproject/kudos/KudosScreenTest.kt
- [ ] T109 [US8] Implement `KudosViewModel.onProfileTap(userId: String)` — emits a one-shot nav event to `Routes.PROFILE` (passing userId). Anonymous-with-no-tap is enforced at the component level (HighlightCard / KudosFeedCard read `sender_visible_to_me` before wiring the tap). | app/src/main/java/com/example/aiddproject/kudos/ui/KudosViewModel.kt
- [ ] T110 [US8] In HighlightCard + KudosFeedCard, conditionally wire the sender row's tap to `onProfileTap(kudos.sender.id)` only when `kudos.sender_visible_to_me = true` (Q-K-3). Recipient row always tappable. | app/src/main/java/com/example/aiddproject/kudos/ui/components/HighlightCard.kt + KudosFeedCard.kt
- [ ] T111 [P] [US8] Write `KudosFeedCardTest.anonymous_sender_row_no_tap_for_other_viewer` — fixture has is_anonymous=true + sender_visible_to_me=false; assert tap on sender row does NOT fire onProfileTap. | app/src/androidTest/java/com/example/aiddproject/kudos/KudosFeedCardTest.kt
- [ ] T112 [P] [US8] Write `KudosFeedCardTest.anonymous_sender_visible_to_recipient` — sender_visible_to_me=true; tap fires callback. | app/src/androidTest/java/com/example/aiddproject/kudos/KudosFeedCardTest.kt
- [ ] T113 [P] [US8] Write `KudosFeedCardTest.recipient_row_always_tappable` — | app/src/androidTest/java/com/example/aiddproject/kudos/KudosFeedCardTest.kt
- [ ] T114 [P] [US8] Wire Top 10 row tap (T044 component) to `onProfileTap`. (Full Top 10 impl in Phase 12; this task wires the placeholder.) | app/src/main/java/com/example/aiddproject/kudos/ui/components/TopTenRecipients.kt

**Checkpoint**: Copy Link + View All + Profile nav working.

---

## Phase 10: User Story 9 — Spotlight Board (Priority: P2)

**Goal**: Spotlight section renders total counter, pan/zoom canvas,
live Sunner search (maxLength 100, debounced ~300ms, "no results"
inline message).

**Independent Test**: Spotlight loaded → see total + canvas with
nodes. Type into search → see matched node highlighted. Type
nonexistent name → "no results". Type 101 chars → suppressed at
100.

- [ ] T115 [US9] Replace `SpotlightBoard.kt` stub with full impl rendering section header + total counter (B.7.1, reads `state.spotlight.graph.total_kudos_count`) + the pan/zoom canvas + Sunner search input. | app/src/main/java/com/example/aiddproject/kudos/ui/components/SpotlightBoard.kt
- [ ] T116 [US9] Create `SpotlightCanvas.kt` — `Canvas { … }` rendering nodes + edges from `SpotlightGraph`. Wraps in `Modifier.transformable(state = rememberTransformableState { zoomChange, panChange, _ -> … })` so user can pan + pinch-zoom. Apply current scale + offset to the canvas transform. | app/src/main/java/com/example/aiddproject/kudos/ui/components/SpotlightCanvas.kt
- [ ] T117 [P] [US9] Create `SpotlightSearchInput.kt` — `OutlinedTextField` with `maxLength = 100` enforced via `filter { it.text.length <= 100 }`. Placeholder "Tìm kiếm". | app/src/main/java/com/example/aiddproject/kudos/ui/components/SpotlightSearchInput.kt
- [ ] T118 [US9] Implement `KudosViewModel.onSpotlightSearchChange(query: String)` — update `_uiState.spotlightSearchQuery`. Debounce via a `MutableStateFlow<String>` + `.debounce(300.milliseconds)` + collect inside `init` block. On each emission call `repo.searchSunner(query, limit=20)`, set `state.spotlightSearchResult` to Match(node) / NoMatch / Idle accordingly. | app/src/main/java/com/example/aiddproject/kudos/ui/KudosViewModel.kt
- [ ] T119 [P] [US9] Wire matched node highlight: when `state.spotlightSearchResult is Match`, pass the matched node id to SpotlightCanvas; canvas renders that node with an emphasis (filled vs outlined). | app/src/main/java/com/example/aiddproject/kudos/ui/components/SpotlightBoard.kt + SpotlightCanvas.kt
- [ ] T120 [P] [US9] Wire NoMatch inline message: when `state.spotlightSearchResult is NoMatch`, render a Text below the input with localized "no results" copy. | app/src/main/java/com/example/aiddproject/kudos/ui/components/SpotlightBoard.kt
- [ ] T121 [P] [US9] Empty state: when `state.spotlight is SpotlightState.Empty`, render section header + empty-state copy; canvas and search disabled. | app/src/main/java/com/example/aiddproject/kudos/ui/components/SpotlightBoard.kt
- [ ] T122 [P] [US9] Write `KudosViewModelTest.spotlight_search_debounces_then_calls_repo` — emit 5 rapid queries; assert repo.searchSunner called once with the last query. | app/src/test/java/com/example/aiddproject/kudos/ui/KudosViewModelTest.kt
- [ ] T123 [P] [US9] Write `KudosViewModelTest.spotlight_search_no_match_sets_NoMatch_result` — repo returns empty list; assert state flips to NoMatch. | app/src/test/java/com/example/aiddproject/kudos/ui/KudosViewModelTest.kt
- [ ] T124 [P] [US9] Write `SpotlightSearchInputTest.maxLength_100_suppresses_101st_char` — type 100 chars, then 1 more; assert text stays at 100. | app/src/androidTest/java/com/example/aiddproject/kudos/SpotlightSearchInputTest.kt
- [ ] T125 [P] [US9] Write `SpotlightBoardTest.renders_total_counter_from_graph_response` — assert "388 KUDOS" text visible when graph.total_kudos_count=388. | app/src/androidTest/java/com/example/aiddproject/kudos/SpotlightBoardTest.kt
- [ ] T126 [P] [US9] Write `SpotlightBoardTest.empty_state_disables_search_input` — assert search input is `disabled` when state=Empty. | app/src/androidTest/java/com/example/aiddproject/kudos/SpotlightBoardTest.kt
- [ ] T127 [P] [US9] Write `SpotlightBoardTest.no_match_message_renders_when_search_returns_no_results` — | app/src/androidTest/java/com/example/aiddproject/kudos/SpotlightBoardTest.kt
- [ ] T128 [P] [US9] Pan/zoom interactive test (limited Compose UI coverage): assert SpotlightCanvas is composed within the transformable scope. Full gesture coverage deferred to manual smoke. | app/src/androidTest/java/com/example/aiddproject/kudos/SpotlightBoardTest.kt

**Checkpoint**: Spotlight Board rendered + searchable. Pan/zoom
working on emulator (manual smoke).

---

## Phase 11: User Story 10 — Personal stats + User Story 11 — Open Secret Box (Priority: P2)

**Goal**: Personal stats panel with 5 tiles + conditional x2 fire
badge (when `systemFlags.x2BonusActive`). Open Secret Box CTA:
disabled when `secret_boxes_unopened = 0`; single-click suppression;
on success stats refresh.

**Independent Test**: Stats panel shows correct values. With x2
bonus active, fire badge appears next to "Số tim bạn nhận được".
With unopened boxes=0, button disabled. Tap when >0 → nav to
SECRET_BOX_OPEN. Stats decrement unopened + increment opened.

- [ ] T129 [US10] Replace `PersonalStatsPanel.kt` stub with full impl: 5 tiles rendering `state.stats` values (Loaded case). Loading shows skeleton; Error shows inline error. | app/src/main/java/com/example/aiddproject/kudos/ui/components/PersonalStatsPanel.kt
- [ ] T130 [US10] Add x2 fire badge next to "Số tim bạn nhận được" tile, conditional on `state.specialDayActive == true` AND `state.systemFlags?.x2BonusActive == true` (combined check — admin can enable bonus without special day or vice versa per spec). Use material-icons-extended fire icon. | app/src/main/java/com/example/aiddproject/kudos/ui/components/PersonalStatsPanel.kt
- [ ] T131 [P] [US10] Write `PersonalStatsPanelTest.renders_five_tile_values_from_state` — fixture with PersonalStats values; assert each tile renders. | app/src/androidTest/java/com/example/aiddproject/kudos/PersonalStatsPanelTest.kt
- [ ] T132 [P] [US10] Write `PersonalStatsPanelTest.x2_badge_visible_when_active` — | app/src/androidTest/java/com/example/aiddproject/kudos/PersonalStatsPanelTest.kt
- [ ] T133 [P] [US10] Write `PersonalStatsPanelTest.x2_badge_hidden_when_inactive` — | app/src/androidTest/java/com/example/aiddproject/kudos/PersonalStatsPanelTest.kt
- [ ] T134 [US11] Replace `OpenSecretBoxCta.kt` stub with full impl: button with localized "Mở Secret Box" + box count badge; `enabled = stats.secret_boxes_unopened > 0`. Wraps onClick in `rememberSingleClickHandler` (TR-004). | app/src/main/java/com/example/aiddproject/kudos/ui/components/OpenSecretBoxCta.kt
- [ ] T135 [US11] Implement `KudosViewModel.onOpenSecretBox()` — guard with local `secretBoxBusy = true`; call `repo.nextUnopenedBox()` then `repo.openSecretBox(boxId)`; on success update PersonalStats slice (+1 opened, -1 unopened) AND emit a one-shot nav event to `Routes.SECRET_BOX_OPEN`. On failure surface a Snackbar. | app/src/main/java/com/example/aiddproject/kudos/ui/KudosViewModel.kt
- [ ] T136 [US11] Wire `Routes.SECRET_BOX_OPEN` nav callback from `KudosScreen` to `navController.navigate(Routes.SECRET_BOX_OPEN)`. Add placeholder `composable(Routes.SECRET_BOX_OPEN) { PlaceholderScreen(label = "Secret Box open") }` to `AppNavigation.kt`. | app/src/main/java/com/example/aiddproject/kudos/ui/KudosScreen.kt + AppNavigation.kt
- [ ] T137 [P] [US11] Write `KudosViewModelTest.onOpenSecretBox_decrements_unopened_increments_opened_on_success` — | app/src/test/java/com/example/aiddproject/kudos/ui/KudosViewModelTest.kt
- [ ] T138 [P] [US11] Write `KudosViewModelTest.onOpenSecretBox_double_tap_only_one_open_triggered` — call twice rapidly; assert repo.openSecretBox called once. | app/src/test/java/com/example/aiddproject/kudos/ui/KudosViewModelTest.kt
- [ ] T139 [P] [US11] Write `KudosViewModelTest.onOpenSecretBox_failure_surfaces_snackbar` — repo failure; assert snackbar set. | app/src/test/java/com/example/aiddproject/kudos/ui/KudosViewModelTest.kt
- [ ] T140 [P] [US11] Write `OpenSecretBoxCtaTest.disabled_when_unopened_is_zero` — | app/src/androidTest/java/com/example/aiddproject/kudos/OpenSecretBoxCtaTest.kt
- [ ] T141 [P] [US11] Write `OpenSecretBoxCtaTest.enabled_when_unopened_gt_zero_fires_callback` — | app/src/androidTest/java/com/example/aiddproject/kudos/OpenSecretBoxCtaTest.kt

**Checkpoint**: Stats + Secret Box flows working with DEMO data.

---

## Phase 12: User Story 12 — Top 10 latest gift recipients (Priority: P2)

**Goal**: List 10 latest gift recipients (avatar + full name +
reward description). Empty state. Row tap → profile.

**Independent Test**: With DEMO 8 recipients → all 8 rows render.
With 0 → "Chưa có dữ liệu". Tap a row → profile nav.

- [ ] T142 [US12] Replace `TopTenRecipients.kt` stub with full impl: section title + LazyColumn of up to 10 rows (avatar via Coil + full_name + reward_name); on row tap fire `onProfileTap(user_id)`. | app/src/main/java/com/example/aiddproject/kudos/ui/components/TopTenRecipients.kt
- [ ] T143 [US12] Empty state: when `state.topTen is TopTenState.Empty`, render single placeholder row with "Chưa có dữ liệu". | app/src/main/java/com/example/aiddproject/kudos/ui/components/TopTenRecipients.kt
- [ ] T144 [P] [US12] Write `TopTenRecipientsTest.renders_up_to_ten_rows_from_state` — fixture with 8 recipients; assert all 8 visible. | app/src/androidTest/java/com/example/aiddproject/kudos/TopTenRecipientsTest.kt
- [ ] T145 [P] [US12] Write `TopTenRecipientsTest.empty_state_shows_localized_no_data_string` — | app/src/androidTest/java/com/example/aiddproject/kudos/TopTenRecipientsTest.kt
- [ ] T146 [P] [US12] Write `TopTenRecipientsTest.row_tap_fires_callback_with_user_id` — | app/src/androidTest/java/com/example/aiddproject/kudos/TopTenRecipientsTest.kt

**Checkpoint**: Top 10 rendering + tap nav working.

---

## Phase 13: Polish & Cross-Cutting Concerns

**Purpose**: A11y stress test (every component meets the spec's
contentDescription contract), pull-to-refresh edge cases, full QA
gate, plan retrospective update, final commit.

- [ ] T147 A11y audit pass — verify every interactive component carries the localized contentDescription from spec § Accessibility table. Cross-check each row against the implemented Composable. | (audit — no file)
- [ ] T148 [P] Write per-section contentDescription assertion tests across the existing test suite (extend each `*Test.kt` with one assertion comparing the rendered semantic to `ctx.getString(R.string.…)`). | app/src/androidTest/java/com/example/aiddproject/kudos/ (multiple files)
- [ ] T149 [P] Write `KudosScreenA11yTest.focus_order_is_top_to_bottom_visual_reading_order` — assert TalkBack focus order matches spec § A11y focus order (Send Kudos pill → filters → carousel → Spotlight → stats → Secret Box → Top 10 → feed → View all). | app/src/androidTest/java/com/example/aiddproject/kudos/KudosScreenA11yTest.kt
- [ ] T150 [P] Write `KudosScreenA11yTest.every_interactive_meets_48dp_touch_target` — parametric over a list of test tags; assert each `getBoundsInRoot()` ≥ 48×48dp. | app/src/androidTest/java/com/example/aiddproject/kudos/KudosScreenA11yTest.kt
- [ ] T151 [P] Write `KudosViewModelTest.pullToRefresh_during_in_flight_refresh_is_noop` — concurrency rule from spec. | app/src/test/java/com/example/aiddproject/kudos/ui/KudosViewModelTest.kt
- [ ] T152 [P] Write `KudosViewModelTest.openSecretBox_during_animation_modal_mount_is_noop` — concurrency rule. | app/src/test/java/com/example/aiddproject/kudos/ui/KudosViewModelTest.kt
- [ ] T153 [P] Write `KudosViewModelTest.stats_refresh_and_secret_box_success_concurrent_secretbox_payload_wins` — concurrency rule. | app/src/test/java/com/example/aiddproject/kudos/ui/KudosViewModelTest.kt
- [ ] T154 Run the full quality gate: `./gradlew lint ktlintCheck testDebugUnitTest connectedDebugAndroidTest`. All green; no new lint warnings; no test failures. Fix root causes — do NOT skip with `--no-verify`. | (build — no file)
- [ ] T155 [P] Update `plan.md` § Implementation Strategy retrospective: mark Phases 0–12 as `✅ shipped` with commit hashes. | .momorph/specs/fO0Kt19sZZ-iOS-Sun-Kudos/plan.md
- [ ] T156 [P] Update `SCREENFLOW.md` discovery log with the Sun*Kudos ship date + commit hashes. | .momorph/SCREENFLOW.md
- [ ] T157 Final commit per memory `feedback_commit_per_task.md`: granular commits per phase or coherent task group. Conventional message styles: `test(kudos): …` for test tasks; `feat(kudos): …` for impl; `chore(kudos): …` for setup; `docs(kudos): …` for plan/SCREENFLOW updates. | (git — no file)

---

## Dependencies & Execution Order

### Phase dependencies

- **Phase 1 (Setup)**: No deps — run first. T001 sequential; T002/T003/T005/T006/T007/T009/T010 [P]; T004 after T003; T008 anytime.
- **Phase 2 (Foundational)**: Depends on Phase 1. T011–T022 mutually [P] (different files). T023 after domain models. T024 after T023 (impl uses interface). T025 [P] with T024 (different file). T026 after T024+T025 (binds both). T027 after T024.
- **Phase 3 [US1 MVP]**: Depends on Phase 2. T028→T029→T031 sequential on same file (KudosViewModel.kt) within VM core. T030, T032, T033 [P] (different test methods in same file). T034 after T031. T035 after T028. T036-T045 [P] (different stub files). T046 after T029+T035. T047 after T046. T048-T051 [P]. T052 sequential after T035.
- **Phase 4 [US2]**: Depends on Phase 3. T053 anytime. T054 sequential same file. T055+T056 [P].
- **Phase 5 [US3]**: Depends on Phase 3 + Phase 4. T057+T058 [P]. T059 after T057+T058. T060/T061/T062 sequential same file (KudosViewModel.kt). T063-T069 [P].
- **Phase 6 [US4]**: Depends on Phase 5 (filter reset hook). T070 after stubs. T071+T072+T073+T074 sequential same file or per file. T075-T078 [P].
- **Phase 7 [US5]**: Depends on Phase 6 (heart icon lives on cards). T079 (HeartIcon.kt) [P] with T080 (StarTierBadge.kt). T081+T082 sequential same file. T083-T091 [P]. T092 same file edits.
- **Phase 8 [US6+US7]**: Depends on Phase 3 (Send Kudos pill exists in MVP). T093 after T046. T094 [P]. T095 same file (HighlightCard); T096 same file (KudosFeedCard). T097+T098 [P].
- **Phase 9 [US13+US14+US8]**: Depends on Phase 7 (cards have action rows). T099+T100 sequential same files. T101+T102 [P]. T103+T104 [P]. T105 [P]. T106 same file as T047. T107+T108 [P]. T109+T110 sequential same files. T111-T114 [P].
- **Phase 10 [US9]**: Depends on Phase 3 (SpotlightBoard stub). T115 after T040. T116+T117 [P]. T118 same file (KudosViewModel.kt). T119+T120+T121 [P]. T122-T128 [P].
- **Phase 11 [US10+US11]**: Depends on Phase 3 (PersonalStatsPanel + OpenSecretBoxCta stubs). T129+T130 sequential same file. T131-T133 [P]. T134 after T043. T135 sequential same file as VM core. T136 same file as T047. T137-T141 [P].
- **Phase 12 [US12]**: Depends on Phase 3 (TopTenRecipients stub). T142+T143 sequential same file. T144-T146 [P].
- **Phase 13 (Polish)**: Depends on ALL prior phases. T147 → T148-T153 [P] → T154 (build gate) → T155-T156 [P] → T157 (commit).

### Within-phase notes

- **MVP scope (Phases 1–8)**: ~100 tasks. Delivers a fully-rendered auth-gated filterable Kudos hub with carousel + likes + send/detail nav.
- **P2 scope (Phases 9–11)**: ~50 tasks. Copy link, View all, Profile nav, Spotlight, Stats, Secret Box.
- **P3 wrap-up (Phase 12 + 13)**: ~10 tasks. Top 10 + final QA gate.

### Parallel staffing opportunities

- **Phase 2**: Domain models (T011–T022) all `[P]` — 12 files, no inter-dependency. A team of 3 could land Phase 2 in one sitting.
- **Phase 5 + 6**: Disjoint code paths (filter ≠ carousel) — staffable in parallel by two devs.
- **Phase 9 + 10 + 11**: All P2 stories operate on independent body sections. Three devs could ship them concurrently.

---

## Implementation Strategy

### MVP First (Recommended)

1. **Complete Phases 1 + 2** — infrastructure ready (~25 tasks).
2. **Complete Phase 3 only (US1 MVP)** — hub renders end-to-end with DEMO data, every section in every state (~25 tasks).
3. **STOP and VALIDATE**: manual emulator smoke + run instrumented tests. Verify pull-to-refresh + section ordering + Empty/Error states.
4. **Continue with Phases 4–8** if MVP looks right (~50 tasks). After Phase 8, all P1 stories ship.

### Incremental Delivery

1. **Setup + Foundation + US1 MVP** — ship as first commit batch (~50 tasks, granular commits per task group).
2. **US2 + US3** — auth gate + filter. Independent commit per phase.
3. **US4 + US5** — carousel + likes. Independent commits.
4. **US6 + US7** — Send Kudos + Detail nav (one phase, one commit).
5. **US13 + US14 + US8** — Copy link + View all + Profile nav.
6. **US9 (Spotlight)** — largest single phase.
7. **US10 + US11** — Stats + Secret Box.
8. **US12** — Top 10.
9. **Polish** — A11y + QA gate + retrospective.

---

## Notes

- **Commit cadence**: Per memory `feedback_commit_per_task.md` — granular per logical task group; never batch phases. QA gate green first.
- **Test cadence**: Per Constitution V — failing test before impl for every new public function. The tasks above interleave test tasks immediately before their corresponding implementation tasks.
- **DEMO-only path**: Plan deliberately ships against `DemoKudosRepository` first. `SupabaseKudosRepository` scaffolds in T025 with `TODO`s — production binding lands in a follow-on once backend ships (separate work stream).
- **Open question Q-K-1** (special-day flag source): T118 calls `repo.systemFlags()` on mount; if backend later encodes the flag in JWT, only the repository impl changes — no client-state shape change.
- **Total task count**: **157** (Phase 1: 10, Phase 2: 17, Phase 3: 25, Phase 4: 4, Phase 5: 13, Phase 6: 9, Phase 7: 14, Phase 8: 6, Phase 9: 16, Phase 10: 14, Phase 11: 13, Phase 12: 5, Phase 13: 11). Below the plan's 150-180 estimate because some tasks bundle multiple test methods into one file (matches Top Talent canonical convention).
