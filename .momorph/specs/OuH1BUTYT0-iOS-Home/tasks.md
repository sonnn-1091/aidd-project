# Tasks: Home

**Frame**: `OuH1BUTYT0-iOS-Home`
**Prerequisites**: plan.md (required, present), spec.md (required, present),
research.md (recommended, intentionally not produced — research findings
inlined in plan)

> **Note on `design-style.md`**: This project intentionally does NOT produce a
> `design-style.md`. Per `spec.md` § Visual Requirements and `plan.md` §
> Notes, visual specifications (colors, sizes, fonts, asset variants) are
> fetched on-demand at implementation time via MoMorph
> `query_section` / `get_node` for the Node IDs listed in `spec.md`. Tasks
> below reference those Node IDs where pixel-level detail is needed.

---

## Task Format

```
- [ ] T### [P?] [Story?] Description | file/path.kt
```

- **[P]**: Can run in parallel (different files, no dependencies on incomplete tasks)
- **[Story]**: Which user story this belongs to (US1, US2, US3, US4, US5, US6, US7)
- **|**: File path affected by this task

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Asset preparation, dependency wiring, database migration, auth
interceptor scaffolding, and string resources.

- [x] T001 Add Coil + kotlinx-datetime + lifecycle-runtime-compose entries to version catalog | gradle/libs.versions.toml
- [x] T002 Wire new deps in app build (`implementation(libs.coil.compose)`, `implementation(libs.kotlinx.datetime)`, `implementation(libs.androidx.lifecycle.runtime.compose)`) | app/build.gradle.kts
- [ ] T003 [P] Phase-0 asset prep: download Search icon for Node `I6885:9057;88:1869` via MoMorph (`mm_media_search`) and place vector at `app/src/main/res/drawable/ic_search.xml` | app/src/main/res/drawable/ic_search.xml
- [ ] T004 [P] Phase-0 asset prep: download Notification bell icon for Node `I6885:9057;88:1830` (`mm_media_notification`) | app/src/main/res/drawable/ic_bell.xml
- [ ] T005 [P] Phase-0 asset prep: download FAB pencil icon (child of `6885:9058`) | app/src/main/res/drawable/ic_fab_pencil.xml
- [ ] T006 [P] Phase-0 asset prep: download FAB S/Kudos icon (child of `6885:9058` → `MM_MEDIA_IC_Kudos Logo` `6885:7657`) | app/src/main/res/drawable/ic_fab_skudos.xml
- [ ] T007 [P] Phase-0 asset prep: download four NavBar tab icons (children of `I6885:9056;75:2008`) | app/src/main/res/drawable/ic_navbar_{saa2025,awards,kudos,profile}.xml
- [ ] T008 [P] Phase-0 asset prep: download Award card placeholder/thumbnail assets (children of `Top Talent Award` instance `6885:8051`) | app/src/main/res/drawable/ic_award_*.xml + ic_award_card_placeholder.png
- [ ] T009 [P] Phase-0 asset prep: download Kudos banner fallback drawable (decide based on `6885:9041` design) | app/src/main/res/drawable/ic_kudos_banner_placeholder.xml
- [ ] T010 [P] Phase-0 asset prep: export the two `mm_media_bg` shadow rectangles (`6885:8981` Shadow Left, `6885:8982` Shadow Bottom) as overlay drawables to layer over `bg_keyvisual.png` | app/src/main/res/drawable/bg_home_shadow_left.xml + bg_home_shadow_bottom.xml
- [x] T011 Author Supabase migration: `public.awards` (id uuid PK, name text not null, thumbnail_url text, sort_order int default 0, created_at timestamptz default now()), `public.kudos_settings` (single-row with `is_kudos_available bool not null default false`, `banner_url text`, `badge_text text`, `description_text text`), `public.notifications` (id uuid PK, user_id uuid references auth.users(id) on delete cascade, title text, body text, read_at timestamptz null, created_at timestamptz default now() + index on `(user_id, read_at)`), RPC `notifications_summary()` returning `{ unread_count int }` and RPC `kudos_summary()` returning the kudos_settings projection — all `security invoker`. RLS enabled on every table in the same migration with policies `awards_select_authenticated`, `kudos_settings_select_authenticated`, `notifications_select_own` (`user_id = auth.uid()`), plus matching `no_client_writes` (insert/update/delete with `false`) on each | supabase/migrations/20260507_init_home_tables.sql
- [x] T012 [P] Extend Supabase seed with 3 awards, 1 kudos_settings row (`is_kudos_available = true`, public-CDN banner_url placeholder per Q-Plan-1), 4 notifications (2 read + 2 unread for the demo Sunner) | supabase/seed.sql
- [x] T013 [P] Create `AuthError` sealed type (Unauthenticated 401, Forbidden 403) | app/src/main/java/com/example/aiddproject/core/auth/AuthError.kt
- [x] T014 [P] Create `AuthRedirectEvent` sealed type (`SessionExpired`, `Forbidden`) | app/src/main/java/com/example/aiddproject/core/auth/AuthRedirectEvent.kt
- [x] T015 [P] Create `AuthErrorInterceptor` Ktor plugin: observes every response; on 401/403 emits to a shared `MutableSharedFlow<AuthError>`. MUST whitelist requests whose path matches `^/auth/v1/.*` so an in-flight `signOut()` doesn't re-trigger the redirect (risk register) | app/src/main/java/com/example/aiddproject/core/auth/AuthErrorInterceptor.kt
- [x] T016 Create `AuthRedirectController` (Hilt `@Singleton`): collects from `AuthErrorInterceptor.flow`, exposes `events: SharedFlow<AuthRedirectEvent>` (depends on T013, T014, T015) | app/src/main/java/com/example/aiddproject/core/auth/AuthRedirectController.kt
- [x] T017 Modify `SupabaseModule.provideSupabaseClient` to install `AuthErrorInterceptor` on the underlying Ktor HttpClient (depends on T015) | app/src/main/java/com/example/aiddproject/core/di/SupabaseModule.kt
- [ ] T018 [P] Add Home brand-fixed strings (translatable="false") to `values/strings.xml`: `home_btn_about_award`, `home_btn_about_kudos`, `home_countdown_days_label`, `home_countdown_hours_label`, `home_countdown_min_label`, `home_coming_soon` | app/src/main/res/values/strings.xml
- [ ] T019 [P] Add Home localized display strings (VN authoritative) to `values/strings.xml`: `home_section_awards_title`, `home_section_kudos_title`, `home_kudos_note_heading`, `home_kudos_note_body`, `home_link_chi_tiet`, `home_awards_loading`, `home_awards_empty`, `home_awards_error`, `home_action_retry`, `home_navbar_saa_2025`, `home_navbar_awards`, `home_navbar_kudos`, `home_navbar_profile`, `home_theme_paragraph` | app/src/main/res/values/strings.xml
- [ ] T020 [P] Mirror localized strings into `values-en/strings.xml` (use spec § Localized Copy table; `home_theme_paragraph` and `home_kudos_note_body` left as TODO placeholders for translator pickup) | app/src/main/res/values-en/strings.xml
- [ ] T021 [P] Mirror localized strings into `values-ja/strings.xml` (same TODO placeholders) | app/src/main/res/values-ja/strings.xml
- [ ] T022 [P] Add Home accessibility labels to all three locales (`a11y_home_bell_badge`, `a11y_home_bell_no_badge`, `a11y_home_search`, `a11y_home_fab_compose_kudo`, `a11y_home_fab_kudos_feed`, `a11y_home_navbar_tab_active`, `a11y_home_navbar_tab_inactive`) | app/src/main/res/values{,-en,-ja}/strings.xml

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Domain models, sealed states, repositories (real + demo +
Hilt module), route placeholders, and the AuthRedirectController wired
into AppNavigation. **No user-story work can begin until this phase is
complete.**

### Tests First (TDD per Constitution Principle V — written and FAILING before implementation)

- [x] T023 [P] Write `CountdownEngineTest` — pre-event tick produces decreasing values; at-event clamps to 0; `Clock` injectable for deterministic time | app/src/test/java/com/example/aiddproject/home/domain/CountdownEngineTest.kt
- [x] T024 [P] Write `AuthRedirectControllerTest` — synthesized 401 emits `SessionExpired`; 403 emits `Forbidden`; 5xx and other 4xx do NOT emit; `auth/v1/*` whitelisted requests do not re-trigger | app/src/test/java/com/example/aiddproject/core/auth/AuthRedirectControllerTest.kt
- [x] T025 [P] Write `SupabasePostgrestAwardsRepositoryTest` — gateway-based, like Login's `SupabaseAuthRepositoryTest`; success → list mapped, empty array → empty list, network error → Result.failure | app/src/test/java/com/example/aiddproject/home/data/SupabasePostgrestAwardsRepositoryTest.kt
- [x] T026 [P] Write `SupabaseKudosSummaryRepositoryTest` — RPC happy path, 404 returns Result.failure | app/src/test/java/com/example/aiddproject/home/data/SupabaseKudosSummaryRepositoryTest.kt
- [x] T027 [P] Write `SupabaseNotificationsSummaryRepositoryTest` — RPC returns `{ unreadCount }`, missing field defaults to 0 | app/src/test/java/com/example/aiddproject/home/data/SupabaseNotificationsSummaryRepositoryTest.kt

### Domain (entities + states + countdown)

- [x] T028 [P] Create `CountdownTarget` constant — `LocalDateTime(2025,12,26,0,0).toInstant(TimeZone.of("Asia/Ho_Chi_Minh"))` (Q-Home-1) | app/src/main/java/com/example/aiddproject/home/domain/CountdownTarget.kt
- [x] T029 [P] Create `CountdownEngine` — pure Kotlin class with injectable `Clock`; exposes `Flow<CountdownState>` ticking every 1s; computes `(target - Clock.System.now())` each tick (don't decrement an in-memory counter) (depends on T028) | app/src/main/java/com/example/aiddproject/home/domain/CountdownEngine.kt
- [x] T030 [P] Create `AwardsState` sealed (`Loading | Empty | Error(reason) | Populated(list)`) | app/src/main/java/com/example/aiddproject/home/domain/states/AwardsState.kt
- [x] T031 [P] Create `KudosState` sealed (`Hidden | Loading | Loaded(KudosSummary) | Error`) | app/src/main/java/com/example/aiddproject/home/domain/states/KudosState.kt
- [x] T032 [P] Create `NotificationsState` sealed (`Loading | Loaded(unreadCount: Int) | Error`) | app/src/main/java/com/example/aiddproject/home/domain/states/NotificationsState.kt
- [x] T033 [P] Create `CountdownState` data class (`days, hours, minutes, isPreEvent: Boolean`) | app/src/main/java/com/example/aiddproject/home/domain/states/CountdownState.kt
- [x] T034 [P] Create `Award` entity (`id: String, name: String, thumbnailUrl: String?, sortOrder: Int`) — fields strictly limited to what the design slot consumes (Q-Home-8) | app/src/main/java/com/example/aiddproject/home/domain/Award.kt
- [x] T035 [P] Create `KudosSummary` entity (`isKudosAvailable: Boolean, bannerImageUrl: String?, badgeText: String?, descriptionText: String`) | app/src/main/java/com/example/aiddproject/home/domain/KudosSummary.kt
- [x] T036 [P] Create `NotificationsSummary` entity (`unreadCount: Int`) | app/src/main/java/com/example/aiddproject/home/domain/NotificationsSummary.kt

### Data layer (interfaces + impls + demo fakes + Hilt)

- [x] T037 [P] Create `AwardsRepository` interface — `suspend fun list(): Result<List<Award>>` | app/src/main/java/com/example/aiddproject/home/data/AwardsRepository.kt
- [x] T038 [P] Create `KudosSummaryRepository` interface — `suspend fun get(): Result<KudosSummary>` | app/src/main/java/com/example/aiddproject/home/data/KudosSummaryRepository.kt
- [x] T039 [P] Create `NotificationsSummaryRepository` interface — `suspend fun get(): Result<NotificationsSummary>` | app/src/main/java/com/example/aiddproject/home/data/NotificationsSummaryRepository.kt
- [x] T040 Implement `SupabasePostgrestAwardsRepository` — `from("awards").select { order = "sort_order" }`; raw JSON parsing via `kotlinx.serialization.json.Json` (no @Serializable plugin reliance, mirroring Login's `SupabasePostgrestUsersRepository`) (depends on T034, T037) | app/src/main/java/com/example/aiddproject/home/data/SupabasePostgrestAwardsRepository.kt
- [x] T041 Implement `SupabaseKudosSummaryRepository` — calls RPC `kudos_summary()` (depends on T035, T038) | app/src/main/java/com/example/aiddproject/home/data/SupabaseKudosSummaryRepository.kt
- [x] T042 Implement `SupabaseNotificationsSummaryRepository` — calls RPC `notifications_summary()` (depends on T036, T039) | app/src/main/java/com/example/aiddproject/home/data/SupabaseNotificationsSummaryRepository.kt
- [x] T043 [P] Create `DemoAwardsRepository` — returns 3 hard-coded awards mirroring the seed data | app/src/main/java/com/example/aiddproject/home/data/DemoAwardsRepository.kt
- [x] T044 [P] Create `DemoKudosSummaryRepository` — returns `isKudosAvailable=true` with placeholder URL/copy | app/src/main/java/com/example/aiddproject/home/data/DemoKudosSummaryRepository.kt
- [x] T045 [P] Create `DemoNotificationsSummaryRepository` — returns `unreadCount = 2` | app/src/main/java/com/example/aiddproject/home/data/DemoNotificationsSummaryRepository.kt
- [x] T046 Create `HomeRepositoryModule` — Hilt `@Provides` branches each repo on `BuildConfig.DEMO_MODE` (mirrors Login's `AuthRepositoryModule`) (depends on T040–T045) | app/src/main/java/com/example/aiddproject/home/data/HomeRepositoryModule.kt

### Navigation (route constants + placeholder destinations + interceptor wiring)

- [x] T047 Extend `Routes` with `AWARDS_OVERVIEW`, `KUDOS_OVERVIEW`, `KUDOS_FEED`, `KUDOS_DETAIL`, `WRITE_KUDO`, `AWARD_DETAIL` (with `awardId` route arg pattern `award_detail/{awardId}`), `SEARCH`, `PROFILE` | app/src/main/java/com/example/aiddproject/navigation/Routes.kt
- [x] T048 Register placeholder destinations in `AppNavigation` for every new route from T047 — each rendering a labeled `PlaceholderScreen("Awards overview")` etc. so phase 3+ instrumented tests can drive navigation end-to-end (depends on T047) | app/src/main/java/com/example/aiddproject/navigation/AppNavigation.kt
- [x] T049 Wire `AuthRedirectController.events` collection inside `AppNavigation` as a no-op stub for now (Phase 6 will promote it to actually trigger navigation + signOut) (depends on T016) | app/src/main/java/com/example/aiddproject/navigation/AppNavigation.kt

**Checkpoint**: All foundation unit tests green; Hilt graph compiles with the
new modules; placeholder routes navigable; `AuthRedirectController` emits
correct events for synthesized 401/403 responses; awards repo round-trips
against the local Supabase stack with seeded data.

---

## Phase 3: User Story 1 - View the SAA 2025 hub on Home (Priority: P1) 🎯 MVP

**Goal**: Authenticated user lands on Home and sees the brand logo,
ROOT FURTHER tagline, real-time countdown to 2025-12-26 (UTC+7), the theme
paragraph, the awards section header, FAB, and bottom NavBar with the SAA
2025 tab marked active.

**Independent Test**: Authenticate with a demo Sunner → land on Home →
verify the header (logo + language + search + bell), hero (ROOT FURTHER
logo + countdown + ABOUT buttons), theme paragraph, awards section header,
FAB, and bottom NavBar with the SAA 2025 tab active.

### Tests First

- [x] T050 [P] [US1] Write `HomeViewModelTest` — countdown state ticks; section state combines correctly; lifecycle pause stops the ticker; verify the `awardsState.value` is `AwardsState.Loading` synchronously at construction (TR-003 first-paint guarantee) | app/src/test/java/com/example/aiddproject/home/ui/HomeViewModelTest.kt

### Models / state aggregate

- [x] T051 [P] [US1] Create `HomeUiState` aggregate — `(countdown: CountdownState, awards: AwardsState, kudos: KudosState, notifications: NotificationsState, language: Language)` | app/src/main/java/com/example/aiddproject/home/ui/HomeUiState.kt

### ViewModel

- [x] T052 [US1] Create `HomeViewModel` — Hilt `@HiltViewModel`; combines per-section flows from injected repositories + `CountdownEngine` + `LanguagePreferenceRepository`; uses `viewModelScope.launch { repeatOnLifecycle(STARTED) { … } }` for each section's fetch (parallel per Q-Home-7); exposes `events: SharedFlow<HomeEvent>` for one-shot navigations (depends on T029, T046, T051) | app/src/main/java/com/example/aiddproject/home/ui/HomeViewModel.kt

### UI subcomposables

- [x] T053 [P] [US1] Create `HomeHeader` subcomposable — Row layout containing SAA logo (`ic_logo_saa`, decorative), `LanguageSelector` (reused from Login), search icon button stub, bell icon stub. Reuses gradient overlay pattern from Login's `LoginScreen` (depends on T003, T004) | app/src/main/java/com/example/aiddproject/home/ui/components/HomeHeader.kt
- [x] T054 [P] [US1] Create `HomeHero` subcomposable — Column with `ic_logo_root_further` (reused from Login), countdown display, ABOUT AWARD + ABOUT KUDOS Material `Button`s. Countdown uses `derivedStateOf` keyed on `(days, hours, minutes)` so TalkBack live region only re-announces on minute changes (spec § Behavioral Accessibility) (depends on T033) | app/src/main/java/com/example/aiddproject/home/ui/components/HomeHero.kt
- [x] T055 [P] [US1] Create `ThemeParagraph` subcomposable — single localized `Text(stringResource(R.string.home_theme_paragraph))` (Q-Home-4 — localized) | app/src/main/java/com/example/aiddproject/home/ui/components/ThemeParagraph.kt
- [x] T056 [US1] Create `HomeScreen` (stateful) + `HomeScreenContent` (stateless) — `Scaffold` + `SnackbarHost`; full-bleed `bg_keyvisual` + Home shadow overlays (from T010); composes Header, Hero, Theme, Awards header (placeholder until US2), FAB stub (placeholder until US5), BottomBar stub (placeholder until US3); uses `LazyColumn` so `AwardsSection`'s `LazyRow` nests cleanly (depends on T052, T053, T054, T055) | app/src/main/java/com/example/aiddproject/home/ui/HomeScreen.kt

### Navigation wire-up

- [x] T057 [US1] Wire `HomeScreen` behind `Routes.HOME` in `AppNavigation`, replacing the Home placeholder shipped during Login Phase 3. Login's success path (`onNavigateToHome`) now lands on this screen (depends on T056) | app/src/main/java/com/example/aiddproject/navigation/AppNavigation.kt

### Instrumented tests

- [x] T058 [P] [US1] Write `HomeScreenTest` — Compose UI test asserts all four hub elements visible (logo, ROOT FURTHER tagline via contentDescription, countdown values, theme paragraph, ABOUT AWARD button, ABOUT KUDOS button). Drives `HomeScreenContent` directly with a known `HomeUiState` so no DI/VM | app/src/androidTest/java/com/example/aiddproject/home/HomeScreenTest.kt
- [x] T059 [P] [US1] Write `CountdownDisplayTest` — pre-event clock: DAYS/HOURS/MINUTES non-zero, "Coming soon" visible; at/post-event clock: values clamp to 0, "Coming soon" hidden; TalkBack live region announces only on minute boundaries | app/src/androidTest/java/com/example/aiddproject/home/CountdownDisplayTest.kt

**Checkpoint**: launching the app authenticated lands on the hub view; the
countdown ticks against UTC+7; theme paragraph + ABOUT buttons render in
the active locale; TalkBack re-announces the countdown only on minute
boundaries.

---

## Phase 4: User Story 2 - Browse and open awards from the carousel (Priority: P1)

**Goal**: With awards data, the carousel scrolls horizontally; tapping a
card's "Chi tiết" link navigates to that award's Detail screen with the
right `awardId`. Loading / empty / error / populated states each render.

**Independent Test**: Toggle awards repo seed to ≥1 award → carousel
scrolls, tap Chi tiết on first → Award Detail placeholder opens with the
right `awardId`. Force `Result.failure` → error state with Retry; tap
Retry → re-fetch succeeds → populated.

### Tests First

- [x] T060 [P] [US2] Extend `HomeViewModelTest` — `AwardsState` transition test: Loading → Populated, Loading → Error, Retry → Loading → Populated; assert `awardsState.value === AwardsState.Loading` synchronously at VM construction BEFORE any coroutine launches (TR-003 first-paint guarantee, no `Initial` predecessor) | app/src/test/java/com/example/aiddproject/home/ui/HomeViewModelTest.kt
- [x] T061 [P] [US2] Write `AwardsCarouselTest` — populated state: drag scroll left/right; tap "Chi tiết" on first card fires the navigation callback with that awardId; loading state shows spinner; empty state shows empty message; error state shows Retry button | app/src/androidTest/java/com/example/aiddproject/home/AwardsCarouselTest.kt

### UI

- [x] T062 [P] [US2] Create `AwardCard` subcomposable — uses the design's `Top Talent Award` instance component (`6885:8051`); thumbnail (`AsyncImage` with placeholder from T008) + name + Chi tiết link; double-tap suppression on Chi tiết (TR-005) | app/src/main/java/com/example/aiddproject/home/ui/components/AwardCard.kt
- [x] T063 [US2] Create `AwardsSection` subcomposable — section title + `LazyRow` of `AwardCard`s; renders one of `AwardsState`'s 4 branches (loading spinner / empty message / error+Retry / populated cards) (depends on T030, T062) | app/src/main/java/com/example/aiddproject/home/ui/components/AwardsSection.kt

### ViewModel intent + screen wiring

- [x] T064 [US2] Add `HomeViewModel.onRetryAwards()` intent that re-fires the awards fetch | app/src/main/java/com/example/aiddproject/home/ui/HomeViewModel.kt
- [x] T065 [US2] Wire `AwardsSection` into `HomeScreenContent` between `ThemeParagraph` and the Kudos slot; pass `onChiTietTap = { awardId -> navController.navigate("award_detail/$awardId") }` (depends on T056, T063) | app/src/main/java/com/example/aiddproject/home/ui/HomeScreen.kt

**Checkpoint**: tap "Chi tiết" → Award Detail placeholder opens with the
right `awardId`; carousel scrolls; loading / empty / error states each
render and Retry recovers.

---

## Phase 5: User Story 3 - Reach other major sections from Home (Priority: P1)

**Goal**: NavBar tabs and hero buttons navigate to every other primary
screen (Awards, Kudos, Profile). SAA 2025 tab is active on Home; re-tap
scrolls Home content to top.

**Independent Test**: From Home, tap each NavBar tab → corresponding
placeholder displayed with that tab marked active. Tap ABOUT AWARD →
Awards overview placeholder. Tap ABOUT KUDOS → Kudos overview
placeholder. Re-tap SAA 2025 tab → Home `LazyColumn` scrolls to top.

### Tests First

- [x] T066 [P] [US3] Write `HomeBottomBarTest` — NavBar renders 4 tabs (SAA 2025 active by default); tap each tab → corresponding placeholder + that tab marks active; re-tap of active SAA 2025 fires the scroll-to-top callback; scroll-to-top is suppressed when `notificationsSheetVisible == true` (risk register) | app/src/androidTest/java/com/example/aiddproject/home/HomeBottomBarTest.kt

### UI

- [x] T067 [P] [US3] Create `HomeBottomBar` subcomposable — Material 3 `NavigationBar` with 4 `NavigationBarItem`s using the icons from T007; `selected = (currentRoute == this tab's route)`; `Role.Tab` semantics + `a11y_home_navbar_tab_active`/`_inactive` content descriptions (TR-009); minimum touch target 48dp per tab | app/src/main/java/com/example/aiddproject/home/ui/components/HomeBottomBar.kt

### Wiring

- [x] T068 [US3] Wire `HomeBottomBar` into `HomeScreenContent`'s `Scaffold.bottomBar` slot; expose the Home root `LazyListState` via `rememberLazyListState()`; on re-tap of the active SAA 2025 tab call `lazyListState.animateScrollToItem(0)` (suppress when `notificationsSheetVisible`) (depends on T056, T067) | app/src/main/java/com/example/aiddproject/home/ui/HomeScreen.kt
- [x] T069 [US3] Wire ABOUT AWARD button → `navController.navigate(Routes.AWARDS_OVERVIEW)` and ABOUT KUDOS button → `Routes.KUDOS_OVERVIEW`. Both visible regardless of `isKudosAvailable` (Q-Home-9). Add double-tap suppression (TR-005) | app/src/main/java/com/example/aiddproject/home/ui/components/HomeHero.kt
- [x] T070 [US3] Apply double-tap suppression to all NavBar tabs + hero buttons via a shared `Modifier.singleClickGuard(scope)` helper that flips a local boolean for one frame after click (TR-005) | app/src/main/java/com/example/aiddproject/home/ui/components/{HomeHero, HomeBottomBar}.kt + new app/src/main/java/com/example/aiddproject/core/ui/SingleClickGuard.kt

**Checkpoint**: NavBar tabs + hero buttons all navigate to their
placeholder destinations; SAA 2025 re-tap scrolls Home to top; back from
any tab restores Home.

---

## Phase 6: User Story 4 - Stay protected by session state (Priority: P1)

**Goal**: 401 from any Home data API → sign out + Login (with
`error_oauth_session_expired` snackbar). 403 → Access Denied. RLS denial
verified for all three new tables.

**Independent Test**: Force any of the three Home APIs to return 401 →
land on Login with the expired-session snackbar visible. Force 403 →
land on Access Denied. Run `HomeRlsPolicyTest` against local Supabase
seeded with two Sunners → assert each can only read their own
notifications and the public awards/kudos_settings rows.

### Tests First

- [x] T071 [P] [US4] Write `HomeAuthRedirectTest` — synthesize a 401 from awards/kudos/notifications APIs (one test per API surface) → assert `signOut()` called + navigation arrived at Login + `error_oauth_session_expired` snackbar shown. Synthesize a 403 → assert navigation lands on Access Denied | app/src/androidTest/java/com/example/aiddproject/home/HomeAuthRedirectTest.kt
- [x] T072 [P] [US4] Write `HomeRlsPolicyTest` — under a non-self JWT: `awards` SELECT returns 0 rows (auth-only policy still scopes properly), `kudos_settings` SELECT returns 0 rows for unauthenticated, `notifications` SELECT for another user's `user_id` returns 0 rows. Mirrors Login's `RlsPolicyTest` pattern; gated on `BuildConfig.SUPABASE_URL` non-empty + the second-Sunner test creds (Q4 from Login plan) | app/src/androidTest/java/com/example/aiddproject/home/HomeRlsPolicyTest.kt

### Implementation

- [x] T073 [US4] Promote `AuthRedirectController` collection in `AppNavigation` from no-op stub (T049) to: on `SessionExpired` → call `Supabase.auth.signOut()` and `navController.navigate(LOGIN) { popUpTo(GATE) { inclusive = true } }`; on `Forbidden` → `navController.navigate(ACCESS_DENIED) { popUpTo(GATE) { inclusive = true } }`. The stack-replace removes any modal sheets implicitly (Q-Plan-3) (depends on T016, T049) | app/src/main/java/com/example/aiddproject/navigation/AppNavigation.kt
- [x] T074 [US4] Add a session-expired hint mechanism to Login — extend `AuthRedirectController` with a one-shot `sessionExpiredHint: SharedFlow<Unit>` that `LoginScreen`'s `LaunchedEffect` collects to surface `snackbarHostState.showSnackbar(R.string.error_oauth_session_expired)` (FR-014; spec edit US4-1) | app/src/main/java/com/example/aiddproject/core/auth/AuthRedirectController.kt + app/src/main/java/com/example/aiddproject/auth/login/ui/LoginScreen.kt

**Checkpoint**: 401/403 from any of the three Home APIs route correctly;
Login's session-expired snackbar fires; RLS denial test green.

---

## Phase 7: User Story 5 - Engage with the Kudos community (Priority: P2)

**Goal**: Kudos section visible only when `isKudosAvailable=true`; banner
loads with fallback drawable on URL failure; section Chi tiết → Kudos
detail; FAB pencil hidden when flag false; FAB S/Kudos always visible.

**Independent Test**: Demo flag `true` → Kudos section renders + FAB
pencil visible; banner image stub URL → fallback drawable; tap section
Chi tiết → KudosDetail placeholder; tap FAB pencil → WriteKudo
placeholder (with double-tap suppression); tap FAB S/Kudos → Kudos feed
placeholder. Demo flag `false` → section hidden, FAB pencil hidden,
S/Kudos remains.

### Tests First

- [x] T075 [P] [US5] Extend `HomeViewModelTest` — `KudosState` transitions: Hidden when `isKudosAvailable=false`, Loaded when `true`, Error on RPC failure | app/src/test/java/com/example/aiddproject/home/ui/HomeViewModelTest.kt
- [x] T076 [P] [US5] Write `HomeFabTest` — pencil hidden when `kudos.isAvailable=false`, visible when `true`, double-tap on pencil yields exactly one `WriteKudo` on back stack; S/Kudos always visible regardless of flag | app/src/androidTest/java/com/example/aiddproject/home/HomeFabTest.kt
- [x] T077 [P] [US5] Write `KudosSectionTest` — visible only when flag true; banner with stub failing URL renders fallback drawable; Chi tiết tap fires nav callback | app/src/androidTest/java/com/example/aiddproject/home/KudosSectionTest.kt

### Implementation

- [x] T078 [P] [US5] Configure Coil `ImageLoader` in `AIDDApplication.onCreate` per Q-Plan-1: public CDN, NO custom interceptor, default disk + memory cache enabled. Forward-compat note in code: if backend later moves to Supabase Storage, swap in OkHttp auth interceptor + `diskCachePolicy(DISABLED)` here only | app/src/main/java/com/example/aiddproject/AIDDApplication.kt
- [x] T079 [P] [US5] Create `KudosSection` subcomposable — `Card` containing Coil `AsyncImage(model = bannerImageUrl, placeholder = painterResource(R.drawable.ic_kudos_banner_placeholder), error = painterResource(R.drawable.ic_kudos_banner_placeholder))` + badge text + description + Chi tiết button. Renders nothing when `state == KudosState.Hidden` (Q-Home-5 / Q-Home-9 only the lower section is gated) (depends on T031, T035, T009) | app/src/main/java/com/example/aiddproject/home/ui/components/KudosSection.kt
- [x] T080 [P] [US5] Create `HomeFab` subcomposable — Material 3 `FloatingActionButton` with two stacked icons. Pencil icon rendered ONLY when `kudosState is Loaded && kudosState.summary.isKudosAvailable` (Q-Home-2). S/Kudos icon always rendered (Q-Home-9). Double-tap suppression on pencil via `pencilInFlight: Boolean` state (depends on T005, T006) | app/src/main/java/com/example/aiddproject/home/ui/components/HomeFab.kt
- [x] T081 [US5] Wire `KudosSection` into `HomeScreenContent` between `AwardsSection` and `HomeFab` slot; wire `HomeFab` as `Scaffold.floatingActionButton` slot positioned above NavBar (depends on T056, T065, T079, T080) | app/src/main/java/com/example/aiddproject/home/ui/HomeScreen.kt
- [x] T082 [US5] Add `HomeViewModel.onWriteKudoTap()` (with double-tap guard `pencilInFlight`) navigating to `Routes.WRITE_KUDO` and `onKudosFeedTap()` navigating to `Routes.KUDOS_FEED`; section Chi tiết callback navigates to `Routes.KUDOS_DETAIL` | app/src/main/java/com/example/aiddproject/home/ui/HomeViewModel.kt

**Checkpoint**: with `isKudosAvailable=true` → full section + FAB pencil
visible + Chi tiết → KudosDetail; with `false` → section + pencil
disappear, S/Kudos / NavBar Kudos / ABOUT KUDOS still present.

---

## Phase 8: User Story 6 - View notifications (Priority: P2)

**Goal**: Bell shows red badge dot when `unreadCount > 0`. Tapping the
bell opens a Material 3 `ModalBottomSheet` over Home; back press
dismisses; on dismiss, the badge re-fetches.

**Independent Test**: Demo `unreadCount = 2` → badge visible. Tap bell →
sheet appears over Home; back press → sheet dismisses, Home back stack
unchanged; badge re-fetches (in demo, value stays 2). Demo
`unreadCount = 0` → no badge. Force notifications API into `Error` state
→ no badge but bell still tappable.

### Tests First

- [x] T083 [P] [US6] Extend `HomeViewModelTest` — `NotificationsState` transitions: Loaded(>0) → badge visible; Loaded(0) → no badge; Error → no badge AND bell still tappable; on `onNotificationsSheetDismissed()` triggers a new fetch | app/src/test/java/com/example/aiddproject/home/ui/HomeViewModelTest.kt
- [x] T084 [P] [US6] Write `NotificationsSheetTest` — bell tap opens `ModalBottomSheet`; back-press dismisses without leaving Home; on dismiss `notifications_summary` re-fetch fires; bell tap is enabled in every notifications state | app/src/androidTest/java/com/example/aiddproject/home/NotificationsSheetTest.kt

### Implementation

- [x] T085 [P] [US6] Create `BellWithBadge` subcomposable — `BadgedBox` wrapping the `ic_bell` IconButton; renders red badge `Badge()` only when `state is Loaded && state.unreadCount > 0`; content description = `a11y_home_bell_badge` (formatted with count) when badged, `a11y_home_bell_no_badge` otherwise (depends on T004) | app/src/main/java/com/example/aiddproject/home/ui/components/BellWithBadge.kt
- [x] T086 [P] [US6] Create `NotificationsSheet` subcomposable — Material 3 `ModalBottomSheet` with a stub list ("No notifications" placeholder + a TODO note that the real panel is owned by the Notifications spec). Sheet content padded with `Modifier.systemBarsPadding()` (risk register: status-bar gap) | app/src/main/java/com/example/aiddproject/home/ui/components/NotificationsSheet.kt
- [x] T087 [US6] Wire bell tap in `HomeHeader` → flips `notificationsSheetVisible` state in `HomeScreen`; render `NotificationsSheet` conditionally on that state with `onDismissRequest = { notificationsSheetVisible = false; viewModel.onNotificationsSheetDismissed() }` (depends on T053, T085, T086) | app/src/main/java/com/example/aiddproject/home/ui/HomeScreen.kt + HomeHeader.kt
- [x] T088 [US6] Add `HomeViewModel.onNotificationsSheetDismissed()` that re-invokes the notifications-summary fetch; this is also added to the API-trigger note in spec § API Dependencies | app/src/main/java/com/example/aiddproject/home/ui/HomeViewModel.kt

**Checkpoint**: bell tap → sheet over Home; back-press dismisses without
leaving Home; badge updates after dismissal.

---

## Phase 9: User Story 7 - Search + Language switcher (Priority: P3)

**Goal**: Search icon navigates to Search placeholder; language switcher
on Home reuses Login's `LanguageSelector` and re-renders Home text within
one frame on selection (carries Login SC-004 via shared
`LanguageProvider`).

**Independent Test**: Tap search icon → Search placeholder. Tap language
pill → dropdown opens with VN/EN/JA. Select EN → header / theme / hero
button labels / awards section title / NavBar tab labels all re-render in
EN within one recomposition.

### Tests First

- [x] T089 [P] [US7] Extend `HomeScreenTest` — search icon tap fires nav callback to `Routes.SEARCH` | app/src/androidTest/java/com/example/aiddproject/home/HomeScreenTest.kt
- [x] T090 [P] [US7] Write `HomeLocaleSwitchTest` — drives the locale state from VN → EN inside `LanguageProvider`; asserts `home_theme_paragraph`, `home_section_awards_title`, and `home_navbar_awards` text all re-render to their EN variants in the same composition tree (no Activity recreation) | app/src/androidTest/java/com/example/aiddproject/home/HomeLocaleSwitchTest.kt

### Implementation

- [x] T091 [US7] Add Search `IconButton` (icon `ic_search`) to `HomeHeader`; `onClick = { navController.navigate(Routes.SEARCH) }`; `contentDescription = stringResource(R.string.a11y_home_search)`; double-tap suppression (depends on T053, T003) | app/src/main/java/com/example/aiddproject/home/ui/components/HomeHeader.kt
- [x] T092 [US7] Drop in Login's `LanguageSelector` composable into `HomeHeader` between the logo and search icon; binds to `LocaleViewModel` (already shipped from Login) so language state is shared globally (depends on T053) | app/src/main/java/com/example/aiddproject/home/ui/components/HomeHeader.kt

**Checkpoint**: search icon navigates; language dropdown opens and
selection re-renders all Home text within one frame.

---

## Phase 10: Polish & Cross-Cutting Concerns

**Purpose**: Accessibility hardening, log scrubbing extension, telemetry
stubs, and final quality gate run.

- [x] T093 [P] Wire countdown live region with minute-only re-announce (`liveRegion = LiveRegionMode.Polite` + `derivedStateOf` keyed on minute), bell live region for badge change, FAB pencil/S/Kudos `contentDescription` from `a11y_home_fab_*` keys | app/src/main/java/com/example/aiddproject/home/ui/components/{HomeHero, BellWithBadge, HomeFab}.kt
- [x] T094 [P] Write `HomeFocusOrderTest` — verify focus order via TalkBack-equivalent traversal: language switcher → search → bell → countdown → ABOUT AWARD → ABOUT KUDOS → theme paragraph → first award Chi tiết → … → Kudos Chi tiết → FAB pencil → FAB S/Kudos → NavBar tabs | app/src/androidTest/java/com/example/aiddproject/home/HomeFocusOrderTest.kt
- [x] T095 [P] Write `HomeKeyboardTest` — Tab to each interactive control; press Enter; assert the same callback fires as a tap (mirrors Login's `LoginKeyboardTest`) | app/src/androidTest/java/com/example/aiddproject/home/HomeKeyboardTest.kt
- [x] T096 [P] Write `HomeTouchTargetTest` — assert search, bell, language pill, FAB pencil, FAB S/Kudos, ABOUT AWARD, ABOUT KUDOS, each NavBar tab, and each Chi tiết link have width AND height ≥ 48dp (Constitution Principle III; mirrors Login's `TouchTargetTest`) | app/src/androidTest/java/com/example/aiddproject/home/HomeTouchTargetTest.kt
- [x] T097 [P] Extend `SecureTimberTree`'s scrub regex set to redact values associated with `award.name`, `award.description`, `notification.title`, `notification.body` keys (TR-007) | app/src/main/java/com/example/aiddproject/core/logging/SecureTimberTree.kt
- [x] T098 [P] Extend `SecureTimberTreeTest` with new redaction cases for `award.name`, `award.description`, `notification.title`, `notification.body` | app/src/test/java/com/example/aiddproject/core/logging/SecureTimberTreeTest.kt
- [x] T099 [P] Add Timber breadcrumbs at the four state transitions per section in `HomeViewModel` (loading / success / error / retry) — telemetry SDK stub pending Login Phase 7 carry-over (telemetry SDK choice). Document the pending real-SDK swap in code comments | app/src/main/java/com/example/aiddproject/home/ui/HomeViewModel.kt
- [x] T100 Run full Quality Gates locally before opening PR: `./gradlew lint ktlintCheck assembleDebug testDebugUnitTest connectedDebugAndroidTest` — all green | (no file)

---

## Phase 11: UI Fidelity Fixes (Design Audit)

**Purpose**: Close visual gaps surfaced by an audit of the current Compose
implementation against the Figma frame `OuH1BUTYT0` ([iOS] Home, file
`9ypp4enmFmdK3YAFJLIu6C`). Each task references the Figma node ID for
pixel-level fidelity.

> **Audit summary** (2026-05-08): the structural coverage is correct
> (every section is present and routes work) but several visuals deviate
> from the design — the countdown digit boxes, the missing event-info
> block, the section-header pattern, the kudos Chi tiết button, and the
> single-pill FAB are the largest gaps.

### Tests First

- [ ] T101 [P] [UI] Extend `HomeScreenTest` — assert event-info block
  renders the three lines (date, location, livestream tagline) when
  `state.countdown.isPreEvent == true`; assert the section-header pattern
  renders both the caption ("Sun* Annual Awards 2025") AND the big title
  ("Hệ thống giải thưởng") for the awards section, and ("Phong trào ghi
  nhận", "Sun* Kudos") for the kudos section | app/src/androidTest/java/com/example/aiddproject/home/HomeScreenTest.kt
- [ ] T102 [P] [UI] Extend `CountdownDisplayTest` — assert a 2-digit
  zero-padded value renders as TWO separate digit nodes (one per cell);
  assert the "DAYS"/"HOURS"/"MINUTES" label is 18sp Montserrat 400 white
  (mirrors Figma node `6885:8997`) | app/src/androidTest/java/com/example/aiddproject/home/CountdownDisplayTest.kt
- [ ] T103 [P] [UI] Extend `HomeFabTest` — assert the FAB renders as a
  SINGLE pill (cream bg, 100dp corner radius) containing pen + "/" divider
  + Kudos icon when `isKudosAvailable=true`; when false the pen + "/"
  collapse and only the Kudos icon remains within the same pill | app/src/androidTest/java/com/example/aiddproject/home/HomeFabTest.kt
- [ ] T104 [P] [UI] Extend `KudosSectionTest` — assert Chi tiết is a
  proper Button (cream bg, 4dp radius, 160dp wide) not a clickable text
  row; assert the section header renders BOTH the caption and the big
  cream title | app/src/androidTest/java/com/example/aiddproject/home/KudosSectionTest.kt

### String resources

- [ ] T105 [P] [UI] Add new string resources for the audit gap (`values/strings.xml`
  + `values-en/strings.xml` + `values-ja/strings.xml`):
   - **Brand-fixed (`translatable="false"`)**: `home_event_date_value`
     ("26/12/2025"), `home_event_location_value` ("Âu Cơ Art Center"),
     `home_section_kudos_brand_title` ("Sun* Kudos"), `home_fab_divider`
     ("/").
   - **Localized (VN authoritative)**: rename `home_section_awards_title`
     → `home_section_awards_caption` ("Sun* Annual Awards 2025"); add
     `home_section_awards_title` ("Hệ thống giải thưởng"); add
     `home_section_kudos_caption` ("Phong trào ghi nhận"); add
     `home_event_time_label` ("Thời gian:"), `home_event_location_label`
     ("Địa điểm:"), `home_event_livestream` ("Tường thuật trực tiếp tại
     Group Facebook Sun* Family"). EN/JA `home_event_livestream` and
     `home_section_awards_title` left as TODO placeholders for translator
     pickup | app/src/main/res/values{,-en,-ja}/strings.xml

### Hero / countdown

- [ ] T106 [UI] Refactor `HomeHero.CountdownRow` (`6885:8988`) so each
  unit (DAYS / HOURS / MINUTES) renders as TWO 32×56dp digit cells
  side-by-side with 8dp gap. Cell styling: 0.5dp `SaaCream` border, 8dp
  corner radius, gradient bg `linear-gradient(180deg, white 0%, white
  10%)` at 50% opacity, 16.64dp backdrop blur (or solid translucent
  white if `RenderEffect.createBlurEffect` is unavailable on min SDK).
  Digit text: 32sp white, monospaced — use `FontFamily.Monospace` as a
  proxy for "Digital Numbers" (the Figma font is a custom display family
  not bundled in the app; document the substitution). Label below each
  unit: 18sp Montserrat 400 white, 24dp height, `gap = 4dp` between
  digits row and label. Value is 2-digit zero-padded; split via
  `value.toString().padStart(2, '0').forEachIndexed { ... }` (depends
  on T102) | app/src/main/java/com/example/aiddproject/home/ui/components/HomeHero.kt
- [ ] T107 [UI] Add the `EventInfoBlock` composable below the countdown
  inside `HomeHero` (`6885:9016`): three rows, gap 8dp:
   1. "Thời gian: 26/12/2025" — label 14sp R300 white + date 18sp R400
      cream (`SaaCream`).
   2. "Địa điểm: Âu Cơ Art Center" — same pattern.
   3. "Tường thuật trực tiếp tại Group Facebook Sun* Family" — 14sp R400
      white, lineHeight 20sp, full width.
  Keep this block below the hero ABOUT buttons OR above per the design
  (Figma puts it inside Frame 553 alongside countdown — see node
  `6885:8985`). Reads localized strings from T105 | app/src/main/java/com/example/aiddproject/home/ui/components/HomeHero.kt

### Section headers

- [ ] T108 [P] [UI] Create `SectionHeader` subcomposable matching Figma
  component `6885:8015` — column with: caption Text (12sp Montserrat
  400 white, 16dp tall), a 1dp horizontal divider (color `#2E3940`,
  full width, gap 4dp above and below), and a big title Text (22sp
  Montserrat 500 `SaaCream`, 28dp tall) inside a Row that may host a
  trailing slot (Frame 488 in design — currently empty for Awards and
  Kudos but the slot is reserved for future filter chips) | app/src/main/java/com/example/aiddproject/home/ui/components/SectionHeader.kt
- [ ] T109 [UI] Replace the bold `Text` headers in `AwardsSection` and
  `KudosSection` with the new `SectionHeader`. Awards passes `caption =
  home_section_awards_caption` + `title = home_section_awards_title`;
  Kudos passes `caption = home_section_kudos_caption` + `title =
  home_section_kudos_brand_title` (depends on T108) | app/src/main/java/com/example/aiddproject/home/ui/components/{AwardsSection, KudosSection}.kt

### Kudos Chi tiết

- [ ] T110 [UI] Replace the clickable `Row` Chi tiết link in
  `KudosSection` with a proper `Button` matching the design's
  `mms_5.3_Button` (`6885:9055`): 160×40dp, padding 12dp, 4dp radius,
  bg `SaaCream`, label "Chi tiết" 14sp Montserrat 500 `SaaInk`, plus
  `ic_chevron_right` 24dp icon (depends on T104) | app/src/main/java/com/example/aiddproject/home/ui/components/KudosSection.kt

### FAB

- [ ] T111 [UI] Refactor `HomeFab` to a SINGLE pill button (`6885:9058`):
  width = wrap (89dp when both icons present), height = 48dp, padding
  8dp, 100dp corner radius, bg `SaaCream`, glow shadow `0 4dp 4dp 0
  rgba(0,0,0,0.25)` + `0 0 6dp 0 #FAE287`. Children:
   1. Pen icon `ic_fab_pencil` (24dp) — rendered ONLY when
      `isKudosAvailable=true` (Q-Home-2).
   2. "/" divider Text (24sp Montserrat 400 `SaaInk`) — rendered ONLY
      when the pen is rendered (collapses with it).
   3. Kudos logo icon `ic_navbar_kudos` (24dp) — always rendered
      (Q-Home-9).
  Keep both `rememberSingleClickHandler` guards on the pen and Kudos
  taps. Update `HomeFabTest` (T103) to assert the pill semantics:
  one button node, two click sub-targets when both icons present, one
  when pen is hidden | app/src/main/java/com/example/aiddproject/home/ui/components/HomeFab.kt

### Header polish

- [ ] T112 [P] [UI] Tweak `HomeHeader` actions row spacing to match the
  Figma `actions` frame (`I6885:9057;88:1828`): `Arrangement.spacedBy(10.dp)`
  between language pill / search / bell (currently 4dp). Ensure trailing
  edge padding ends 20dp from the screen edge | app/src/main/java/com/example/aiddproject/home/ui/components/HomeHeader.kt
- [ ] T113 [P] [UI] Override M3 `Badge` color in `BellWithBadge` to the
  Figma red `#D4271D` (`I6885:9057;88:1830;72:1628`); fix size to 8dp
  (M3 default is 6dp dot). Use `Badge(containerColor = Color(0xFFD4271D),
  modifier = Modifier.size(8.dp).testTag(TEST_TAG_HOME_BELL_BADGE))` | app/src/main/java/com/example/aiddproject/home/ui/components/BellWithBadge.kt

### Awards card

- [ ] T114 [P] [UI] Update `AwardCard` to match `Top Talent Award`
  (`6885:8051`): 160×298dp column with three blocks:
   1. **Thumbnail** — 160×160dp `mm_media_Picture-Award` style: cream
      border 0.5dp, 11.4dp corner radius, ornate gradient placeholder.
      Reuses `ic_award_card_top_talent.png` for the placeholder until
      remote `thumbnail_url` is supplied.
   2. **Title + description** — 14sp Montserrat 500 cream title (single
      line) + 14sp Montserrat 300 white description (3-line, lineHeight
      20sp).
   3. **Chi tiết button** — 84×32dp, transparent background, 4dp
      radius, label "Chi tiết" 14sp Montserrat 500 white, chevron 24dp
      after it. Click is `rememberSingleClickHandler(onClick =
      { onChiTietTap(award) })` | app/src/main/java/com/example/aiddproject/home/ui/components/AwardCard.kt

### Validation

- [ ] T115 Run full Quality Gates after Phase 11 lands: `./gradlew lint
  ktlintCheck assembleDebug testDebugUnitTest compileDebugAndroidTestKotlin`
  — all green. Capture an emulator screenshot and compare side-by-side
  against `assets/frame.png` to confirm the gaps are closed | (no file)

**Checkpoint**: emulator screenshot of Home matches the Figma frame
within the constitution's visual-regression threshold (or the
intentional Material 3 substitutions — e.g. NavigationBar visual,
monospaced digit font — are documented in `plan.md` § Notes).

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies — start immediately. T001+T002 are
  sequential (catalog before app build). T003–T010 are all `[P]` once
  T001+T002 land. T011 (migration) is independent. T013–T017
  (auth interceptor stack) chain through Hilt wiring. T018–T022 (strings)
  are all `[P]`.
- **Foundational (Phase 2)**: Depends on Phase 1. **BLOCKS all user stories.**
- **US1 (Phase 3)**: Depends on Phase 2. MVP.
- **US2 (Phase 4)**: Depends on Phase 2 + Phase 3 (modifies `HomeScreen`).
- **US3 (Phase 5)**: Depends on Phase 2 + Phase 3. Can run in parallel
  with US2 once US1 ships `HomeScreenContent` skeleton.
- **US4 (Phase 6)**: Depends on Phase 2 (`AuthRedirectController` exists)
  + Phase 3 (Home actually rendered). Promotes the no-op stub from T049
  to real navigation.
- **US5 (Phase 7)**: Depends on Phase 2 + Phase 3 (modifies `HomeScreen`
  to insert KudosSection + FAB). Can run in parallel with US2/US3/US4
  once Home shell exists.
- **US6 (Phase 8)**: Depends on Phase 2 + Phase 3. Modifies `HomeHeader`
  (bell tap) and `HomeScreen` (sheet container). Can run in parallel with
  US2/US3/US4/US5.
- **US7 (Phase 9)**: Depends on Phase 2 + Phase 3. Trivially parallel —
  modifies `HomeHeader` only (search icon + language pill drop-in).
- **Polish (Phase 10)**: Depends on all desired user stories being
  complete.

### Within Each User Story

- Tests written and FAILING before implementation (Constitution Principle V).
- Models / sealed types / entities → Repositories → ViewModel → UI →
  Navigation wiring.
- Story complete and independently testable before moving to next priority.

### Parallel Opportunities

- **Phase 1**: T003–T010 (asset downloads) all `[P]`. T013–T015 (auth
  types) `[P]`. T018–T022 (strings) `[P]`. Sequential chains: T001 → T002,
  T015 → T016 → T017.
- **Phase 2**: All Tests-First (T023–T027) `[P]`. All Domain entities
  (T028, T030–T036) `[P]`. T029 depends on T028. Repository interfaces
  T037–T039 `[P]`; impls T040 ← T034+T037, T041 ← T035+T038, T042 ←
  T036+T039; demo fakes T043–T045 `[P]`; T046 ← all impl + demo. T047 →
  T048 → T049 sequential.
- **Phase 3 (US1)**: T050 (test) `[P]`. T051 `[P]`. T053–T055 (UI
  subcomposables) `[P]` once T051 lands. T052 (VM) ← T046, T029, T051.
  T056 ← T052+T053+T054+T055. T057 ← T056. T058+T059 `[P]` after T057.
- **Phase 4 (US2)**: T060+T061 (tests) `[P]`. T062 `[P]`. T063 ← T030+T062.
  T064 ← T052. T065 ← T056+T063.
- **Phase 5 (US3)**: T066 (test) `[P]`. T067 `[P]`. T068 ← T056+T067.
  T069 ← T054. T070 modifies multiple files; can run after T067+T069.
- **Phase 6 (US4)**: T071+T072 (tests) `[P]`. T073 ← T016+T049. T074 ← T073.
- **Phase 7 (US5)**: T075–T077 (tests) `[P]`. T078 `[P]`. T079 ← T031+T035.
  T080 ← T031+T035. T081 ← T056+T065+T079+T080. T082 ← T052.
- **Phase 8 (US6)**: T083+T084 (tests) `[P]`. T085 `[P]`. T086 `[P]`.
  T087 ← T053+T085+T086. T088 ← T052.
- **Phase 9 (US7)**: T089+T090 (tests) `[P]`. T091+T092 `[P]` after T053.
- **Phase 10**: All tasks `[P]` except T100 (final gate run, depends on
  all others).

---

## Implementation Strategy

### MVP First (Recommended)

1. Complete Phase 1 + Phase 2 (Setup + Foundational).
2. Complete Phase 3 (US1 hub view) only.
3. **STOP and VALIDATE**: end-to-end against staging Supabase with seed
   data. Hub renders, countdown ticks, theme paragraph in active locale.
4. Deploy to internal channel for stakeholder verification.

### Incremental Delivery

1. MVP cut (above) → Test → Internal release.
2. Add US2 (awards carousel) → Test → Release.
3. Add US3 (NavBar + hero shortcuts) → Test → Release.
4. Add US4 (auth gate hardening) → Test → Release.
5. Add US5 (Kudos community) → Test → Release.
6. Add US6 (notifications) → Test → Release.
7. Add US7 (search + language) → Test → Release.
8. Phase 10 polish across all releases — accessibility, log scrubbing,
   telemetry stubs, final quality gates.

---

## Notes

- **Independent test criteria for each story** are stated above; honor
  them in PR descriptions so reviewers can verify story-by-story.
- **Open infra/security questions** carried over from Login plan (Q4 CI
  Supabase strategy, Q5 cert pinning, Q7 OAuth SHA-256, telemetry SDK
  choice) are not hard-blocks for Home implementation; the Home cert pin
  set inherits whatever Login lands on.
- **Visual specs** for any task that needs pixel-level fidelity (countdown
  number style, NavBar icon weight, FAB elevation, banner aspect ratio,
  award card design) are fetched at task-execution time via MoMorph
  `query_section` / `get_node` for the Node IDs in `spec.md` — not
  enumerated here.
- **Asset audit**: every downloaded asset (T003–T010) MUST have its
  actual pixel dimensions verified via `file <path>` BEFORE any
  `Image(...)` call (the `momorph.implement-ui` audit pattern).
- **Demo mode**: every new repository must follow Login's
  `BuildConfig.DEMO_MODE` branch pattern; `DemoFakes`-style classes are
  T043–T045, branched in T046's Hilt module.
- **Commit cadence**: commit after each task or logical group. Mark tasks
  complete as you go: `[x]`. Run unit tests before moving to the next
  task; run instrumented tests before completing a phase.
