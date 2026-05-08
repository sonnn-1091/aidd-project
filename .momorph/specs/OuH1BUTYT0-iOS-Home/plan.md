# Implementation Plan: Home

**Frame**: `OuH1BUTYT0-iOS-Home`
**Date**: 2026-05-07
**Spec**: `specs/OuH1BUTYT0-iOS-Home/spec.md`

---

## Summary

Build the authenticated hub of the SAA 2025 Android app: a Compose Material 3
screen rendered on top of the same Supabase-session-gated shell as Login. Home
fans out to every other primary surface (Awards, Kudos, Profile, Search,
WriteKudo, Notifications) and is the default destination for both the Login
success path (US1 of `8HGlvYGJWq`) and the auto-login path (US2 of
`8HGlvYGJWq`). The screen runs three independent, lifecycle-aware fetches in
parallel on `STARTED` (awards / kudos_summary / notifications_summary), drives
a 1-second-tick countdown to `2025-12-26T00:00:00+07:00`, and feeds a global
401/403 interceptor that bounces to Login or Access Denied. Branding,
keyvisual, language switcher, and locale plumbing are reused verbatim from the
shipped Login feature; the only net-new shared infrastructure is an
auth-error interceptor that any subsequent authenticated screen will reuse.

---

## Technical Context

**Language/Framework**: Kotlin 2.2.10 / Jetpack Compose (Material 3)
**Primary Dependencies**: `supabase-kt` Auth + Postgrest (already shipped),
Hilt + KSP (shipped), Jetpack DataStore (shipped), Navigation-Compose
(shipped), `androidx.lifecycle.runtime.compose` for `repeatOnLifecycle`,
`androidx.compose.material3` `NavigationBar` / `ModalBottomSheet` /
`FloatingActionButton`, Coil (NEW — Kudos banner image with fallback per
FR-006), Timber (shipped — secure logger), `kotlinx.datetime` (NEW — UTC+7
timezone math for the countdown).
**Platform**: Android-only — min SDK 31, target SDK 36, Java 11 (per
`app/build.gradle.kts`)
**Database**: Supabase (Postgres) — RLS enforced
**Testing**: JUnit + kotlinx-coroutines-test + Turbine + MockK (unit);
`androidx.compose.ui.test` (instrumented); `supabase start` local stack for
RLS + integration tests
**State Management**: ViewModel + per-section `StateFlow<SectionState>`
(awards / kudos / notifications / countdown each independent), plus
read-only access to the existing app-scoped `SessionRepository` /
`LanguagePreferenceRepository`.
**API Style**: Supabase SDK calls (Postgrest + RPC). No hand-rolled REST.

---

## Constitution Compliance Check

*GATE: Must pass before implementation can begin. Each item maps to a principle in
`.momorph/constitution.md`.*

- [x] **I. Clean Code & Source Organization** — feature-first package layout
      under `com.example.aiddproject.home.{ui,data,domain}` with a shared
      `core/auth/` for the new 401/403 interceptor (reused by every
      subsequent authenticated screen). HomeScreen kept under the 150-LOC
      ceiling by extracting `HomeHeader`, `HomeHero`, `AwardsSection`,
      `KudosSection`, `HomeFab`, `HomeBottomBar` subcomposables.
- [x] **II. Tech Stack Best Practices** — sealed `AwardsState`, `KudosState`,
      `NotificationsState`, `CountdownState`; `StateFlow` + coroutines, no
      blocking calls; lifecycle-aware ticker (`repeatOnLifecycle(STARTED)`);
      Repository pattern over Supabase SDK; anon key only (already wired
      from Login); all versions pinned in `gradle/libs.versions.toml`.
- [x] **III. Material Design 3 (Android)** — `NavigationBar` /
      `NavigationBarItem` for bottom nav; `FloatingActionButton` for FAB
      with conditional pencil child; `ModalBottomSheet` for the
      Notifications panel; `LazyRow` for the awards carousel; `Card` /
      `Column` for the Kudos block. Theme tokens via the existing
      `ui/theme/`. Light + dark; dynamic color on Android 12+;
      `WindowSizeClass` for phone/foldable/tablet. 48dp touch targets on
      every interactive control (NavBar tabs split the bar evenly while
      meeting touch-target minimum).
- [x] **IV. OWASP Secure Coding** — no client-side authorization filtering
      (FR-002, TR-002); 401/403 interceptor at the Postgrest layer; no PII
      / award names / notification bodies in logs (existing
      `SecureTimberTree`); TLS-only; cert-pin slot inherited from Login;
      RLS enabled on every new table accessed; threat model below.
- [x] **V. Test-Driven Development** — every FR/SC committed as a failing
      test before implementation. Order: ViewModel state transitions →
      repository/use-case contracts → Compose UI tests → instrumented
      integration against local Supabase. RLS denial tests for
      `public.awards`, `public.kudos_settings`, AND `public.notifications`
      mirror Login's `RlsPolicyTest` (collected in `HomeRlsPolicyTest`).

**Violations**: none anticipated.

| Violation | Justification | Alternative Rejected |
|-----------|---------------|---------------------|
| (none) | — | — |

---

## Architecture Decisions

### Frontend (Jetpack Compose)

- **Component Structure**: Feature-first MVVM. `HomeScreen.kt` is a
  stateful composable that hoists the four section state flows from
  `HomeViewModel`; the layout itself is a stateless `HomeScreenContent`
  that takes a `HomeUiState` aggregate so previews + UI tests can drive
  every state combination without DI. Subcomposables:
  - `HomeHeader` — logo + language + search + bell (with
    `BadgedBox`-backed unread dot)
  - `HomeHero` — ROOT FURTHER tagline + countdown + ABOUT AWARD / ABOUT KUDOS
  - `ThemeParagraph` — single localized `Text`
  - `AwardsSection` — header + state-machine-driven `LazyRow`
  - `KudosSection` — flag-gated card with banner + Chi tiết
  - `HomeFab` — `FloatingActionButton` whose pencil child is conditional
  - `HomeBottomBar` — `NavigationBar` with 4 tabs + tab-active mirror
- **State**:
  - Screen-level: `HomeUiState(
      countdown: CountdownState,
      awards: AwardsState,
      kudos: KudosState,
      notifications: NotificationsState,
      language: Language)` — composed by `HomeViewModel` from per-section
      `StateFlow`s. Per-section sealed states, independent error/loading
      surfaces, per Q-Home-7 (parallel fetches).
  - Read-only references: `SessionRepository.state` (already global),
      `LanguagePreferenceRepository` (already global).
  - Local UI state: notifications-sheet visibility, scroll-to-top trigger
      from NavBar re-tap.
- **Countdown engine**: `CountdownEngine` is a pure Kotlin class taking a
  `Clock` + `targetInstant` and exposing
  `Flow<CountdownState>` that ticks every 1 second via
  `flow { while (true) { emit(now); delay(1000) } }`. The HomeViewModel
  collects it inside `viewModelScope.launch { repeatOnLifecycle(STARTED) {
  ... } }` so it pauses on background per TR-004. Target =
  `LocalDateTime(2025,12,26,0,0).toInstant(TimeZone.of("Asia/Ho_Chi_Minh"))`
  (Q-Home-1).
- **Navigation**:
  - Existing `Routes` extends with `HOME` (already declared),
    `AWARDS_OVERVIEW`, `KUDOS_OVERVIEW`, `KUDOS_FEED`, `KUDOS_DETAIL`,
    `WRITE_KUDO`, `AWARD_DETAIL` (with `awardId` arg), `SEARCH`,
    `PROFILE`, `NOTIFICATIONS_SHEET`. The not-yet-implemented destinations
    each render a placeholder composable so the wiring is end-to-end
    testable but the screens themselves are out of scope.
  - The Notifications panel is a `ModalBottomSheet` rendered inside
    `HomeScreen` (Q-Home-6) — NOT a separate route. Open/close is local
    boolean state. If a 401 fires while the sheet is open, the auth
    redirect uses `navigate(LOGIN) { popUpTo(GATE) { inclusive = true } }`
    — the stack-replace removes the entire Home destination, which
    implicitly closes the sheet (Q-Plan-3); no explicit dismissal
    sequencing is needed.
  - Re-tap of active SAA 2025 tab triggers a `LazyListState.animateScrollToItem(0)`
    on the Home root scroll container (Q-Home-3).
- **Image loading (Coil)**: NEW dependency for the Kudos banner.
  - **Source (Q-Plan-1)**: public CDN — no auth header, disk cache
    enabled. Banner is non-sensitive brand asset.
  - Configured globally in `AIDDApplication` via Coil's
    `ImageLoader.Builder()` so all `AsyncImage` calls inherit the policy.
    No custom interceptor; default disk + memory cache.
  - Fallback drawable wired via `placeholder()` / `error()` on the
    `AsyncImage` composable (FR-006).
  - **Forward-compat note**: if backend later moves the banner to
    Supabase Storage, only the `ImageLoader.Builder` config changes
    (add an `OkHttpClient` interceptor that injects
    `Authorization: Bearer <accessToken>` from `SessionRepository` AND
    sets `diskCachePolicy(CachePolicy.DISABLED)`). No Home composable
    or callsite changes required.

### Backend (Supabase)

- **Tables / RPCs to introduce**:

  | Object | Purpose | RLS / access policy |
  |---|---|---|
  | `public.awards` | Award catalog rendered in the carousel | Read-only for any authenticated role; no client writes |
  | `public.kudos_settings` | Single-row server-side feature flag + Kudos block content (banner_url, badge_text, description_text) | Read-only for any authenticated role; admin-only writes (separate tool) |
  | `public.notifications` | Per-user notifications | RLS: `user_id = auth.uid()` for select |
  | RPC `notifications_summary()` | Returns `{ unread_count: int }` for caller — wraps `count(*) where user_id = auth.uid() and read_at is null` | `security invoker`; relies on `notifications` RLS |
  | RPC `kudos_summary()` (alternative to direct table) | Returns the single-row `kudos_settings` projection. Lets us add later a per-user override without changing the client | `security invoker` |

- **Auth interceptor**: A new `AuthErrorInterceptor` plugin attached to the
  Ktor client backing `supabase-kt`. Observes every response; emits to a
  shared `MutableSharedFlow<AuthError>` when status is 401 / 403. The new
  `AuthRedirectController` (under `core/auth/`) collects that flow and
  exposes `events: SharedFlow<AuthRedirectEvent>` (`SessionExpired` |
  `Forbidden`). `AppNavigation` collects those events at the root and
  triggers `signOut()` + `navigate(LOGIN)` or `navigate(ACCESS_DENIED)`.
  This is project-wide infrastructure — every authenticated screen built
  after Home gets it for free.

- **Migrations**: `supabase/migrations/20260507_init_home_tables.sql`
  creates the three new tables + two RPCs + RLS policies in one file so
  no table merges without policies (Constitution Security Requirements).
  Seeded fixture: 3 awards + 1 kudos_settings row + a couple of read /
  unread notifications for the demo Sunner.

### Integration Points

- **Existing services to reuse (no changes)**:
  - `LanguageProvider` + `LanguagePreferenceRepository` — header language
    switcher reuses verbatim.
  - `SessionRepository` + `SessionGate` + `AuthState` — Home renders only
    when `Authenticated`; gate already routes Unauthenticated → Login.
  - `ui/theme/{Color, Theme, Type}` + `SaaInk` / `SaaCream` brand tokens
    — for the hero buttons (CTA-style match) and any brand-fixed surfaces.
  - `bg_keyvisual.png` — reused as the Home hero background. The two
    extra Shadow rectangles in Home's `mm_media_bg` (`6885:8981`,
    `6885:8982`) are layered as overlay drawables in Compose
    (Phase 0 asset decision below).
  - `LanguageSelector` composable — drop-in for Home header.
  - `SecureTimberTree` — already planted in `AIDDApplication.onCreate`.
- **Shared components introduced by Home (will be reused later)**:
  - `core/auth/AuthErrorInterceptor` + `AuthRedirectController` —
    cross-screen 401/403 routing. Used by every authenticated screen
    built after Home.
  - `home/ui/components/HomeBottomBar` — stays Home-local for now per
    Q-Plan-4. Lift to `core/ui/AppBottomBar` happens in the Awards
    screen plan when there's a second real call site.
- **API contracts**: Supabase Postgrest SELECTs against `awards` /
  `kudos_settings` and RPC calls against `notifications_summary`. Concrete
  contracts will be produced by `/momorph.apispecs` once the spec is
  ratified.
- **Secrets / config**: no new secrets. Reuses `BuildConfig.SUPABASE_URL`
  + `BuildConfig.SUPABASE_ANON_KEY`. `BuildConfig.DEMO_MODE` short-circuits
  the auth chain — Home's data layer needs the same demo-mode treatment
  (see Phase 1 below).

---

## Project Structure

### Documentation (this feature)

```text
.momorph/specs/OuH1BUTYT0-iOS-Home/
├── spec.md              # Feature specification (existing, ratified)
├── plan.md              # This file
├── tasks.md             # To be generated by /momorph.tasks
├── research.md          # (optional) Codebase research findings
└── testcase.md          # To be generated by /momorph.createtestcases
```

### Source Code (affected areas)

```text
app/src/main/java/com/example/aiddproject/
├── home/                                                    # NEW feature folder
│   ├── ui/
│   │   ├── HomeScreen.kt                                    # NEW — stateful entry
│   │   ├── HomeViewModel.kt                                 # NEW — Hilt VM
│   │   ├── HomeUiState.kt                                   # NEW — aggregate
│   │   ├── components/
│   │   │   ├── HomeHeader.kt                                # NEW — header row
│   │   │   ├── HomeHero.kt                                  # NEW — tagline + countdown + ABOUT
│   │   │   ├── ThemeParagraph.kt                            # NEW — localized prose
│   │   │   ├── AwardsSection.kt                             # NEW — header + LazyRow + states
│   │   │   ├── AwardCard.kt                                 # NEW — single card composable
│   │   │   ├── KudosSection.kt                              # NEW — flag-gated card
│   │   │   ├── HomeFab.kt                                   # NEW — pencil + S/Kudos
│   │   │   ├── HomeBottomBar.kt                             # NEW — M3 NavigationBar wrapper
│   │   │   ├── NotificationsSheet.kt                        # NEW — ModalBottomSheet wrapper
│   │   │   └── BellWithBadge.kt                             # NEW — BadgedBox icon
│   ├── data/
│   │   ├── AwardsRepository.kt                              # NEW — interface
│   │   ├── SupabasePostgrestAwardsRepository.kt             # NEW — impl
│   │   ├── DemoAwardsRepository.kt                          # NEW — fake for DEMO_MODE
│   │   ├── KudosSummaryRepository.kt                        # NEW — interface
│   │   ├── SupabaseKudosSummaryRepository.kt                # NEW — impl
│   │   ├── DemoKudosSummaryRepository.kt                    # NEW — fake
│   │   ├── NotificationsSummaryRepository.kt                # NEW — interface
│   │   ├── SupabaseNotificationsSummaryRepository.kt        # NEW — impl (RPC)
│   │   ├── DemoNotificationsSummaryRepository.kt            # NEW — fake
│   │   └── HomeRepositoryModule.kt                          # NEW — Hilt @Provides w/ DEMO_MODE branches
│   └── domain/
│       ├── Award.kt                                         # NEW — entity
│       ├── KudosSummary.kt                                  # NEW — entity
│       ├── NotificationsSummary.kt                          # NEW — entity
│       ├── CountdownEngine.kt                               # NEW — pure Kotlin ticker
│       ├── CountdownTarget.kt                               # NEW — constant @ UTC+7
│       └── states/
│           ├── AwardsState.kt                               # NEW — sealed
│           ├── KudosState.kt                                # NEW — sealed
│           ├── NotificationsState.kt                        # NEW — sealed
│           └── CountdownState.kt                            # NEW — data
├── core/
│   └── auth/                                                # NEW core slice
│       ├── AuthError.kt                                     # NEW — sealed type
│       ├── AuthErrorInterceptor.kt                          # NEW — Ktor plugin
│       ├── AuthRedirectController.kt                        # NEW — observes interceptor flow
│       └── AuthRedirectEvent.kt                             # NEW — SessionExpired | Forbidden
├── navigation/
│   ├── Routes.kt                                            # MODIFIED — add HOME, AWARDS_OVERVIEW, KUDOS_*, WRITE_KUDO, AWARD_DETAIL, SEARCH, PROFILE
│   └── AppNavigation.kt                                     # MODIFIED — wire Home + placeholders + AuthRedirectController collection
└── core/di/SupabaseModule.kt                                # MODIFIED — install AuthErrorInterceptor on the Ktor client

app/src/main/res/
├── values/strings.xml                                       # MODIFIED — add Home brand-fixed + a11y keys
├── values-en/strings.xml                                    # MODIFIED — Home localized strings
├── values-ja/strings.xml                                    # MODIFIED — Home localized strings
└── drawable/                                                # MODIFIED — bell, search, fab_pencil, fab_skudos, navbar icons (vector drawables from MoMorph)

supabase/migrations/
└── 20260507_init_home_tables.sql                            # NEW — awards + kudos_settings + notifications + RPCs + RLS
supabase/seed.sql                                            # MODIFIED — add awards / kudos_settings / notifications fixtures

app/src/test/java/com/example/aiddproject/home/
├── ui/HomeViewModelTest.kt                                  # NEW
├── domain/CountdownEngineTest.kt                            # NEW
├── data/SupabasePostgrestAwardsRepositoryTest.kt            # NEW (gateway-based, like Login)
├── data/SupabaseNotificationsSummaryRepositoryTest.kt       # NEW
└── data/SupabaseKudosSummaryRepositoryTest.kt               # NEW

app/src/test/java/com/example/aiddproject/core/auth/
└── AuthRedirectControllerTest.kt                            # NEW — 401/403 → events

app/src/androidTest/java/com/example/aiddproject/home/
├── HomeScreenTest.kt                                        # NEW — Compose UI per state
├── HomeIntegrationTest.kt                                   # NEW — end-to-end against local Supabase
├── AwardsCarouselTest.kt                                    # NEW — scroll + Chi tiết tap
├── HomeFabTest.kt                                           # NEW — pencil flag-gating + double-tap
├── HomeBottomBarTest.kt                                     # NEW — tab activation + scroll-to-top
├── NotificationsSheetTest.kt                                # NEW — open/dismiss + badge re-fetch
├── CountdownDisplayTest.kt                                  # NEW — tick + post-event clamp
├── HomeAuthRedirectTest.kt                                  # NEW — 401 / 403 routing via interceptor
└── HomeRlsPolicyTest.kt                                     # NEW — non-Sunner JWT cannot read awards / kudos_settings / another user's notifications
```

### Dependencies

| Package | Catalog alias | Version | Purpose |
|---------|---------------|---------|---------|
| `io.coil-kt:coil-compose` | `coil-compose` | 2.7.x | Async image loading + fallback for the Kudos banner (FR-006) |
| `org.jetbrains.kotlinx:kotlinx-datetime` | `kotlinx-datetime` | 0.6.x | UTC+7 timezone math for `CountdownTarget` (Q-Home-1) |
| `androidx.lifecycle:lifecycle-runtime-compose` | `androidx-lifecycle-runtime-compose` | matches existing `lifecycleRuntimeKtx` | `collectAsStateWithLifecycle` + `repeatOnLifecycle(STARTED)` for the countdown ticker |

All versions pinned in `gradle/libs.versions.toml`. No new plugins. The
existing Compose BOM, supabase-kt, Hilt, KSP, ktlint, Timber,
play-services-base, navigation-compose, datastore, credentials are reused
without bumps.

**Compose BOM note**: `coil-compose` and `kotlinx-datetime` are NOT BOM-managed
— pinned independently per Constitution Principle II.

---

## Implementation Strategy

### Phase Breakdown

#### Phase 0: Asset Preparation

- Download Home-specific assets via MoMorph `get_media_files` /
  `list_media_nodes` for the new icon nodes:
  - Search icon (`I6885:9057;88:1869` → `mm_media_search`)
  - Notification bell (`I6885:9057;88:1830` → `mm_media_notification`)
  - FAB pencil + FAB S/Kudos icons (children of `6885:9058`)
  - NavBar tab icons (4 inside `I6885:9056;75:2007`)
  - Award card thumbnail placeholder (children of `6885:8051` instance)
  - Kudos banner image fallback drawable
  - The two header/keyvisual shadow rectangles (`6885:8981` Shadow Left,
    `6885:8982` Shadow Bottom) — likely SVG; export as vector drawable
- Place vectors in `app/src/main/res/drawable/`; raster fallbacks in
  `drawable-mdpi/` mirroring Login's pattern.
- Verify each asset's actual pixel dimensions via `file` (per the
  `momorph.implement-ui` audit pattern) before any `Image(...)` call.
- Reuse `bg_keyvisual.png` for the keyvisual background.

#### Phase 1: Setup (Shared Infrastructure)

- Add Coil + kotlinx-datetime + lifecycle-runtime-compose entries to
  `libs.versions.toml`; wire the `implementation(libs.*)` lines in
  `app/build.gradle.kts`.
- Author the migration `20260507_init_home_tables.sql` with:
  - `public.awards` (`id uuid pk`, `name text not null`,
    `thumbnail_url text`, `sort_order int not null default 0`,
    `created_at timestamptz not null default now()`).
  - `public.kudos_settings` (single-row table — id pk constant; key
    columns: `is_kudos_available bool not null default false`,
    `banner_url text`, `badge_text text`, `description_text text`).
  - `public.notifications` (`id uuid pk`, `user_id uuid references
    auth.users(id) on delete cascade`, `title text`, `body text`,
    `read_at timestamptz null`, `created_at timestamptz default now()`)
    + index on `(user_id, read_at)` for the unread-count RPC.
  - RPCs `notifications_summary()` and `kudos_summary()` as
    `security invoker`.
  - RLS policies on every new table in the same migration (no merge
    without policy per Constitution Security Requirements):
    `awards_select_authenticated` on `awards`,
    `kudos_settings_select_authenticated` on `kudos_settings`,
    `notifications_select_own` on `notifications` (`user_id = auth.uid()`),
    plus matching `no_client_writes` policies (insert/update/delete with
    `false`) on each.
- Extend `supabase/seed.sql` with 3 awards, 1 kudos_settings row
  (`is_kudos_available = true`), 4 notifications (2 read + 2 unread for
  the demo Sunner).
- Create `core/auth/` package: `AuthError`, `AuthRedirectEvent`,
  `AuthErrorInterceptor` (Ktor plugin), `AuthRedirectController` (Hilt
  singleton observing the interceptor's flow). The interceptor MUST
  whitelist requests whose path matches `^/auth/v1/.*` so an in-flight
  `signOut()` (which itself can return 401 if the token already expired)
  doesn't re-trigger the redirect and produce a loop.
- Modify `core/di/SupabaseModule.kt` to install
  `AuthErrorInterceptor` when constructing the Supabase client.
- Add Home's strings to `values/strings.xml` +
  `values-en/strings.xml` + `values-ja/strings.xml`. The full set is
  defined in spec § Localized Copy and explicitly includes:
  - **Brand-fixed** (default-locale only, `translatable="false"`):
    `home_btn_about_award`, `home_btn_about_kudos`,
    `home_countdown_days_label`, `home_countdown_hours_label`,
    `home_countdown_min_label`, `home_coming_soon`.
  - **Localized display strings** (all three locales): every row of the
    spec's "Localized — display strings" table.
  - **Accessibility labels** (all three locales): `a11y_home_bell_badge`,
    `a11y_home_bell_no_badge`, `a11y_home_search`,
    `a11y_home_fab_compose_kudo`, `a11y_home_fab_kudos_feed`,
    `a11y_home_navbar_tab_active`, `a11y_home_navbar_tab_inactive`.
  - `home_theme_paragraph` and `home_kudos_note_body` EN/JA stay as
    TODO placeholders flagged for translator pickup; the existing
    `StringResourceParityTest` from Login Phase 7 will fail if any key
    goes missing in EN or JA.

**Checkpoint**: `./gradlew assembleDebug` succeeds; the new core/auth
plugin is wired into Supabase client construction; new strings present in
all three locales (verified by the Phase-7 `StringResourceParityTest` from
Login).

#### Phase 2: Foundational (Blocking Prerequisites)

**No user-story work can begin until this phase is complete.**

- **Tests-first** (Constitution Principle V):
  - `CountdownEngineTest` — pre-event tick produces decreasing values;
    at-event clamps to 0; lifecycle pause/resume.
  - `AuthRedirectControllerTest` — 401 emits `SessionExpired`; 403 emits
    `Forbidden`; non-2xx 4xx (other) does NOT emit.
  - `Supabase*RepositoryTest` (gateway-based, like Login's
    `SupabaseAuthRepositoryTest`) — Result wrapping; null/empty payload
    handling.
- **Implementation**:
  - `domain/CountdownTarget.kt` constant; `domain/CountdownEngine.kt`
    with injectable `Clock` for testability.
  - `domain/states/{AwardsState, KudosState, NotificationsState, CountdownState}`
    sealed types.
  - `domain/{Award, KudosSummary, NotificationsSummary}` data classes.
  - Repository interfaces + Supabase impls + Demo impls + Hilt module
    (mirrors the Login pattern of branching on `BuildConfig.DEMO_MODE`).
  - **Route constants + placeholder destinations**: extend `Routes.kt` with
    `HOME`, `AWARDS_OVERVIEW`, `KUDOS_OVERVIEW`, `KUDOS_FEED`,
    `KUDOS_DETAIL`, `WRITE_KUDO`, `AWARD_DETAIL` (with `awardId` arg),
    `SEARCH`, `PROFILE`. Register a no-op placeholder composable for each
    in `AppNavigation` so any phase-3+ instrumented test can drive
    navigation end-to-end before those screens are built. Phase 5 only
    wires the NavBar UI, not the routes themselves.
  - Wire `AuthRedirectController.events` collection into `AppNavigation`
    so 401 → `signOut()` + `navigate(LOGIN)` and 403 →
    `navigate(ACCESS_DENIED)`.

**Checkpoint**: All foundation unit tests green; `AuthRedirectController`
emits the correct events for synthesized 401/403 responses; awards
repository round-trips against the local Supabase stack with seeded data.

#### Phase 3: User Story 1 — View the SAA 2025 hub (P1, MVP)

- **Tests first**:
  - `HomeViewModelTest`: countdown state ticks; section state combines
    correctly; lifecycle pause stops the ticker.
  - Compose UI: hero block renders all four sub-elements (logo, countdown,
    ABOUT AWARD, ABOUT KUDOS); theme paragraph visible.
- **Implementation**:
  - `HomeViewModel` exposes `HomeUiState` composed from per-section flows.
  - `HomeScreen` + `HomeScreenContent` scaffolding; `HomeHeader`,
    `HomeHero`, `ThemeParagraph` subcomposables.
  - **Countdown a11y** (spec § Behavioral Accessibility): the countdown
    composable's accessibility node uses
    `Modifier.semantics { liveRegion = LiveRegionMode.Polite }` AND
    derives its `contentDescription` from a state that ONLY changes when
    the displayed minute value changes (not every 1-second recompute) —
    otherwise TalkBack would re-announce on every tick. Implementation:
    `derivedStateOf { "X days, Y hours, Z minutes remaining" }` keyed on
    `(days, hours, minutes)`, NOT on the raw `Instant`.
  - Wire the screen behind `Routes.HOME` in `AppNavigation`; remove the
    placeholder Home composable shipped during Login Phase 3.
  - Login's success path (`onNavigateToHome`) lands on this screen as-is.

**Checkpoint**: launching the app authenticated lands on the hub view;
the countdown ticks against UTC+7; theme paragraph + ABOUT buttons render
in the active locale; TalkBack re-announces the countdown only on minute
boundaries.

#### Phase 4: User Story 2 — Browse and open awards from the carousel (P1)

- **Tests first**:
  - `AwardsState` transition test (Loading → Populated, Loading → Error,
    Retry → Loading → Populated).
  - **TR-003 first-paint test**: assert `awardsState.value` is
    `AwardsState.Loading` synchronously at HomeViewModel construction —
    BEFORE any coroutine launches. The fetch is dispatched on `STARTED`
    via `repeatOnLifecycle`, but the initial state MUST be `Loading` so
    the section paints within 100ms of composition (TR-003). No
    `Initial`/`Idle` value preceding `Loading`.
  - Compose UI: empty state, error state with Retry, populated state with
    horizontal scroll, Chi tiết tap fires the navigation callback.
- **Implementation**:
  - `AwardsSection` composable + `AwardCard` composable.
  - HomeViewModel `onRetryAwards()` intent.
  - Award Detail destination — the placeholder route is registered in
    Phase 2 (Foundational); this phase only wires the Chi tiết callback
    to navigate with the `awardId` argument.
  - Award card double-tap suppression (TR-005).

**Checkpoint**: tap "Chi tiết" → Award Detail placeholder opens with the
right `awardId`; carousel scrolls; loading / empty / error states each
render and Retry recovers.

#### Phase 5: User Story 3 — Reach other major sections (P1)

- **Tests first**:
  - Compose UI: NavBar renders 4 tabs with the right active state; tap
    each tab → corresponding placeholder screen; re-tap of active SAA 2025
    triggers `LazyListState.animateScrollToItem(0)`.
  - Hero buttons fire navigation; double-tap suppressed.
- **Implementation**:
  - `HomeBottomBar` composable using M3 `NavigationBar` +
    `NavigationBarItem`. Wires each tab to navigate to the existing
    placeholder route (registered in Phase 2). The `currentBackStackEntry`
    drives the `selected = true` state per tab.
  - ABOUT AWARD / ABOUT KUDOS hero button wiring (always visible per
    Q-Home-9).
  - Scroll-to-top on active-tab re-tap (Q-Home-3): expose the Home root
    `LazyListState` via `rememberLazyListState()`; on re-tap of the
    active SAA 2025 tab, call `lazyListState.animateScrollToItem(0)`.
    Suppress this when `notificationsSheetVisible == true` (risk
    register).

**Checkpoint**: NavBar tabs + hero buttons all navigate to their
placeholder destinations; SAA 2025 re-tap scrolls Home to top; back from
any tab restores Home.

#### Phase 6: User Story 4 — Stay protected by session state (P1)

- **Tests first**:
  - `HomeAuthRedirectTest`: synthesize a 401 from any Home API → assert
    `signOut()` called + navigation arrived at Login + Login's
    `error_oauth_session_expired` snackbar shown.
  - Synthesize a 403 → assert navigation lands on Access Denied.
  - `HomeRlsPolicyTest`: under a non-Sunner / non-self JWT,
    `awards` SELECT returns 0 rows (RLS gate), `kudos_settings` SELECT
    returns 0 rows, and `notifications` SELECT for another user's
    `user_id` returns 0 rows — three policies, one test class, mirroring
    Login's `RlsPolicyTest`.
- **Implementation**:
  - Promote the AuthRedirectController collection from "in
    AppNavigation as a no-op" to "in AppNavigation triggering navigation
    + Login snackbar emit".
  - Login screen accepts an optional `sessionExpiredHint: Boolean` route
    arg (or reads from a SharedFlow exposed by AuthRedirectController) to
    surface the snackbar — pick whichever is least invasive at impl time.
  - Verify RLS denial paths in instrumented tests against local Supabase.

**Checkpoint**: 401/403 from any of the three Home APIs route correctly;
Login's session-expired snackbar fires; RLS denial test green.

#### Phase 7: User Story 5 — Engage with the Kudos community (P2)

- **Tests first**:
  - `KudosState` transitions: Hidden when flag false, Loaded(banner) when
    true, Failed when network error.
  - Compose UI: Kudos section renders only when `isKudosAvailable=true`;
    banner image fallback fires when URL fails.
  - `HomeFabTest`: pencil hidden when flag false, visible when flag true,
    double-tap suppressed; S/Kudos always visible.
  - `KudosSection` Chi tiết button → KudosDetail.
- **Implementation**:
  - `KudosSection` composable with Coil `AsyncImage` + fallback.
  - `HomeFab` with conditional pencil child.
  - WriteKudo + Kudos feed + Kudos detail placeholder destinations
    (already created in Phase 5; just exercise here).
  - The Kudos overview destination handles its own empty state when flag
    is false (own spec — out of scope).

**Checkpoint**: with `isKudosAvailable=true` → full section + FAB pencil
visible + Chi tiết → KudosDetail; with `false` → section + pencil
disappear, S/Kudos / NavBar Kudos / ABOUT KUDOS still present.

#### Phase 8: User Story 6 — View notifications (P2)

- **Tests first**:
  - `NotificationsState` transitions: Loaded(unreadCount > 0) → badge
    visible; Loaded(0) → no badge; `Error` → no badge AND bell still
    tappable (per spec edge case "Notifications API timeout → bell
    icon renders without a badge; tapping still opens the panel").
  - Compose UI: bell tap opens `ModalBottomSheet`; system back press
    dismisses; on dismiss, `notifications_summary` re-fetch fires.
- **Implementation**:
  - `BellWithBadge` composable wrapping `BadgedBox`. Badge dot rendered
    only when `state is Loaded && state.unreadCount > 0`.
  - `NotificationsSheet` composable (Material 3 `ModalBottomSheet`) with
    a stub list — the actual notifications panel content is the
    Notifications panel spec's concern (out of scope here).
  - `HomeViewModel.onNotificationsSheetDismissed()` triggers
    `refreshNotifications()`.
  - Bell tap is enabled in every notifications state (Loading, Loaded,
    Error) so a transient API failure doesn't block the user from opening
    the panel.

**Checkpoint**: bell tap → sheet over Home; back-press dismisses without
leaving Home; badge updates after dismissal.

#### Phase 9: User Story 7 — Search + Language switcher (P3)

- **Tests first**:
  - Compose UI: search icon tap → Search placeholder; language switcher
    dropdown lists VN/EN/JA; selecting EN re-renders Home strings without
    Activity recreation (carries Login SC-004 via shared
    `LanguageProvider`).
- **Implementation**:
  - `SearchIconButton` wires to `Routes.SEARCH` placeholder.
  - Reuses the existing `LanguageSelector` composable verbatim — no new
    code other than placement in `HomeHeader`.

**Checkpoint**: search icon navigates; language dropdown opens and
selection re-renders all Home text within one frame.

#### Phase 10: Polish & Cross-Cutting

- **Accessibility**:
  - Verify focus order matches spec § Behavioral Accessibility (language
    → search → bell → countdown → ABOUT AWARD → ABOUT KUDOS → theme
    paragraph → first award Chi tiết → … → FAB pencil → FAB S/Kudos →
    NavBar tabs).
  - Bell `BadgedBox` exposes `a11y_home_bell_badge` /
    `a11y_home_bell_no_badge` content descriptions.
  - NavBar tabs expose `Role.Tab` with `selected = true` for the active
    one (TR-009).
  - Touch-target test (`TouchTargetTest`-style instrumented test) for the
    7 interactive controls.
  - Keyboard parity (re-uses Login's pattern: Tab + Enter on each
    interactive control).
- **Telemetry hooks** — same SDK-decision blocker as Phase 7 Login work.
  Stub Timber breadcrumbs at the four state transitions per section
  (loading, success, error, retry). Real SDK wiring deferred until the
  team picks one (already on the carry-over from Login).
- **Log-scrub extension (TR-007)**: extend `SecureTimberTree`'s scrub
  regex set so it also redacts values associated with `award.name`,
  `award.description`, `notification.title`, `notification.body` keys
  before they reach Logcat. Add corresponding test cases in
  `SecureTimberTreeTest`.
- **Cert pinning** — same Q-Home / Q-Login carry-over (Q5 from Login).
  Inherited from the shared `network_security_config.xml`.
- **String parity test** — re-runs Login's `StringResourceParityTest`
  which now also covers the Home keys.
- **Run Quality Gates locally** before opening PR:
  `./gradlew lint ktlintCheck assembleDebug testDebugUnitTest connectedDebugAndroidTest`.

### Risk Assessment

| Risk | Probability | Impact | Mitigation |
|------|-------------|--------|------------|
| Awards / kudos / notifications RLS policies bug allows cross-user data leak | Low | Critical | Policy tests in instrumented suite (`AwardsRlsPolicyTest`); reviewer checklist requires every new table → RLS in same migration (constitution Security Requirements). |
| Countdown drifts due to coroutine scheduling jitter | Medium | Low | Recompute `(target - Clock.System.now())` every tick (don't decrement an in-memory counter); display granularity is minute, so up to ±1s scheduling jitter is invisible (SC-003 within ±1s). |
| `AuthErrorInterceptor` loops on its own 401 (e.g., signOut hits 401) | Medium | Medium | Interceptor whitelists `auth/v1/*` paths so the signOut request itself doesn't re-trigger redirect; unit-tested via `AuthRedirectControllerTest`. |
| `ModalBottomSheet` + edge-to-edge produce a visible status-bar gap | Medium | Low | Wrap sheet content in `Modifier.systemBarsPadding()`; instrumented test asserts the sheet covers the configured insets. |
| Coil OkHttp client picks up an auth header it shouldn't | Low | Medium | Q-Plan-1 resolved to public CDN — Coil `ImageLoader` is configured with NO auth interceptor, so the banner request never carries a token. If backend later flips to Supabase Storage, the swap is isolated to `ImageLoader.Builder` (Architecture § Image loading forward-compat note). |
| Notifications RPC under high-cardinality user accounts is slow | Low | Medium | RPC implemented as `count(*) where user_id = auth.uid() and read_at is null`; index on `(user_id, read_at)` in the migration. |
| Three parallel APIs on a slow network all error simultaneously | Medium | Medium | Each section has independent state machines (Q-Home-7); section-level Retry only re-fires that section's call, not the entire screen. |
| NavBar re-tap scroll-to-top fires when sheet is open | Low | Low | Suppress scroll-to-top when `notificationsSheetVisible`; instrumented test covers this concurrency. |
| Demo-mode forgets to short-circuit the new Home repos → real network calls fail in dev | Medium | Medium | Mirror Login's `DemoFakes.kt` pattern; provide `DemoAwards / DemoKudosSummary / DemoNotificationsSummary` and branch in the Hilt module. |
| `kotlinx-datetime` 0.6 + Kotlin 2.2.10 ABI mismatch | Low | Medium | Pin to a 0.6.x release verified against Kotlin 2.2.x on Maven Central before adding to `libs.versions.toml`. |

### Estimated Complexity

- **Frontend**: High — 8 new composables + 4 sealed states + countdown
  engine + ModalBottomSheet + scroll-to-top + double-tap suppression on 7
  controls.
- **Backend**: Medium — 3 tables, 2 RPCs, RLS policies, seed data. One
  migration file.
- **Testing**: High — every section has its own Compose test + RLS test
  + state-machine test + auth-redirect test. Total ~9 instrumented + ~5
  unit suites.

---

## Integration Testing Strategy

### Test Scope

- [x] **Component / Module interactions**: HomeViewModel ↔ 3 repositories
      ↔ Supabase SDK; `AuthRedirectController` ↔ AppNavigation;
      `LanguageSelector` ↔ Home text re-render.
- [x] **External dependencies**: Supabase Auth + Postgrest + RPCs (real,
      against local stack); Coil image loader (real, against a stub
      banner URL).
- [x] **Data layer**: Postgrest SELECT against `awards`; RPC calls;
      `notifications` row read filtered by `auth.uid()`.
- [x] **User workflows**: cold-start authenticated → land on Home →
      browse awards → tap Chi tiết → return → tap NavBar Awards →
      return; switch language → re-render; bell → sheet → dismiss → badge
      refresh; 401 from awards → bounce to Login; non-Sunner → 403 →
      Access Denied.

### Test Categories

| Category | Applicable? | Key Scenarios |
|----------|-------------|---------------|
| UI ↔ Logic | Yes | Per-section state → composable; FAB pencil flag-gating; NavBar tab activation |
| Service ↔ Service | Yes | `HomeViewModel` ↔ all three repos in parallel; `AuthRedirectController` ↔ `SessionRepository` |
| App ↔ External API | Yes | Real local Supabase stack (no Supabase mocks); Coil with stubbed image URLs |
| App ↔ Data Layer | Yes | RLS denial for non-Sunner JWT; index correctness on `notifications(user_id, read_at)` |
| Cross-platform | No | Android-only |

### Test Environment

- **Environment type**: Local emulator (Pixel 6 / API 34) for instrumented
  tests; local `supabase start` Docker stack for backend.
- **Test data strategy**: SQL fixtures applied via `supabase/seed.sql`
  (3 awards, 1 kudos_settings, 2-read + 2-unread notifications for the
  seeded Sunner).
- **Isolation approach**: each integration test resets the four tables
  via `truncate ... cascade` in a `@Before` hook; auth sessions cleared
  via `signOut()` in `@After`.

### Mocking Strategy

| Dependency Type | Strategy | Rationale |
|-----------------|----------|-----------|
| Supabase Auth + Postgrest + RPCs | Real (local stack) | Spec mandates RLS testing; mock-only would let policy bugs through (Constitution Principle V) |
| Coil image loader | Real with stubbed URL | Banner-fallback path needs a real failed URL; in CI we serve a `127.0.0.1` URL that always 404s |
| `SessionRepository` (in UI tests) | Test double exposing `MutableStateFlow<AuthState>` | Same pattern as Login's UI tests |
| `Clock` (in CountdownEngine tests) | `kotlinx.datetime.Clock` test fake | Lets us advance time without real delays |

### Test Scenarios Outline

1. **Happy Path**
   - [ ] Cold start authenticated → Home renders all four sections.
   - [ ] Tap Chi tiết on first award → Award Detail placeholder.
   - [ ] Tap each NavBar tab → corresponding placeholder, tab marks active.
   - [ ] Re-tap SAA 2025 tab → Home scroll-state.firstVisibleItemIndex = 0.
   - [ ] Switch language to EN → all localizable Home text re-renders.

2. **Error Handling**
   - [ ] Awards API returns 500 → error UI with Retry; tap Retry → success.
   - [ ] Kudos banner image URL 404 → fallback drawable rendered.
   - [ ] Notifications API offline → bell renders no badge, taps still
         open sheet.
   - [ ] 401 from any API → sign-out + Login + session-expired snackbar.
   - [ ] 403 from any API → Access Denied screen.
   - [ ] FAB pencil double-tap → exactly one WriteKudo on back stack.

3. **Edge Cases**
   - [ ] Countdown crosses target while screen is open → "Coming soon"
         hides, values clamp to 0.
   - [ ] App backgrounded for 90s, resumed → countdown recomputes
         immediately, no stale value.
   - [ ] `isKudosAvailable=false` → lower Kudos section + FAB pencil
         disappear; ABOUT KUDOS / FAB S/Kudos / NavBar Kudos remain.
   - [ ] NavBar re-tap with Notifications sheet open → no scroll (sheet
         takes precedence).
   - [ ] RLS policy: non-Sunner JWT cannot read another user's
         notifications.

### Tooling & Framework

- **Test framework**: JUnit 4 (unit), AndroidX Compose Test (UI),
  Espresso where Compose test is insufficient.
- **Supporting tools**: Turbine (Flow assertions), MockK (mocks), Hilt
  test fixtures, kotlinx-datetime test clock.
- **CI integration**: GitHub Actions (when CI lands per Login Phase 7
  T069) — `supabase start` step before `connectedDebugAndroidTest`.

### Coverage Goals

| Area | Target | Priority |
|------|--------|----------|
| Hub view + countdown (US1) | ≥ 90% line + 100% branch on the state machine | High |
| Awards carousel (US2) | ≥ 90% line + 100% branch on `AwardsState` | High |
| Auth-redirect interceptor (US4) | 100% (security-critical) | High (Constitution) |
| Kudos flag-gating (US5) | ≥ 85% | Medium |
| Notifications + sheet (US6) | ≥ 85% | Medium |
| Telemetry / log-scrub | Spot tests for log scrubbing on Home APIs | High |

---

## Threat Model (per Constitution Security Requirements)

This feature handles authenticated data, so a threat model is mandatory.

| Threat | Surface | Mitigation |
|--------|---------|------------|
| Cross-user notification leak | `notifications` table read | RLS policy `user_id = auth.uid()`; instrumented test enumerates a second JWT and confirms 0 rows visible |
| Award catalog leaked to unauthenticated callers | `awards` table read | RLS policy `auth.role() = 'authenticated'`; SessionGate prevents Home render without a session anyway |
| Kudos feature flag flipped client-side to bypass server gate | `kudos_settings` table | `KudosSummary` is server-derived; client never writes; even if a user spoofs `isKudosAvailable=true`, the WriteKudo screen's own server-side validation rejects the post |
| 401-loop DoS via the auth interceptor itself signing out and triggering its own 401 | `AuthErrorInterceptor` | Whitelist `auth/v1/*` paths so signOut doesn't re-route through the interceptor |
| Coil image cache leaks the Kudos banner URL with embedded auth | Disk cache | Not applicable — Q-Plan-1 resolved to public CDN, no auth in URL or headers. Disk cache enabled is safe. (Note retained for the day backend moves the banner to Supabase Storage; see forward-compat note in Architecture § Image loading.) |
| `kotlinx-datetime` clock manipulated client-side to fast-forward countdown | Display | Display-only; server-side authorization is unaffected (already documented in spec edge case) |
| Notification body / Award name leaked to Logcat in debug | Logs | Existing `SecureTimberTree` already scrubs token-like patterns; Phase 10 extends the regex set to also redact `award.name`, `award.description`, `notification.title`, `notification.body` keys (TR-007), with matching `SecureTimberTreeTest` cases. |
| `NotificationsSheet` long-tap or screenshot exposes content of unread items | UI | Accepted risk per Q-Plan-2 — `FLAG_SECURE` will NOT be applied to the sheet on Home. If the Notifications panel spec later identifies sensitive notification content, it can opt in there. |

---

## Dependencies & Prerequisites

### Required Before Start

- [x] `constitution.md` reviewed and understood (v1.0.0).
- [x] `spec.md` approved by stakeholders — all 9 Q-Home questions resolved
      2026-05-07.
- [x] Plan reviewed — all 4 Q-Plan questions resolved 2026-05-07.
- [x] Login feature shipped — Home reuses its `LanguageSelector`,
      `LanguageProvider`, `SessionRepository`, `bg_keyvisual`, brand color
      tokens, `SecureTimberTree`, and `Routes` foundation.
- [ ] Supabase migration for `awards` + `kudos_settings` + `notifications`
      tables + RPCs reviewed before merge (see Phase 1).
- [ ] CI runner has Docker available for `supabase start` (carried over
      from Login Q4 — still pending).
- [ ] Public-CDN URL for the Kudos banner provisioned by backend so
      `kudos_settings.banner_url` can be seeded for integration tests.

### External Dependencies

- Supabase project (URL + anon key) — already provisioned for Login.
- Public CDN host for the Kudos banner image (Q-Plan-1) — backend to
  publish a static URL; Home's Coil ImageLoader has no auth-header
  requirement.
- Translator pickup for `home_theme_paragraph` and `home_kudos_note_body`
  EN/JA copy.

---

## Next Steps

After plan approval:

1. **Run** `/momorph.tasks` to generate the task breakdown from this plan.
2. **Verify** Login Phase 7 carry-over items (CI Docker, cert pinning,
   telemetry SDK choice) — none are hard-blocks here, but the Home cert
   pin set will inherit whatever Login lands on.
3. **Confirm** backend has provisioned the public-CDN URL for the Kudos
   banner so seed data + integration tests have a real image to fetch.
4. **Begin** implementation TDD-first per Phase 0 → Phase 10 ordering.

---

## Open Questions / Resolved Decisions

- [x] **Q-Plan-1 (Image loading)** — **Resolved**: Kudos banner is
      served from a **public CDN**. Coil is configured with NO auth
      interceptor and disk cache **enabled** (banner is non-sensitive
      brand asset). If backend later moves the asset to Supabase Storage,
      the Coil `ImageLoader` swaps in an auth interceptor + disables
      disk cache — only Coil config changes, no Home composable touched.
- [x] **Q-Plan-2 (Privacy)** — **Resolved**: do NOT set `FLAG_SECURE` on
      the Notifications `ModalBottomSheet`. Screenshots / system blur of
      notification preview are allowed. If the Notifications panel spec
      later identifies sensitive content, that screen can opt in.
- [x] **Q-Plan-3 (Routing)** — **Resolved**: when 401 fires while the
      Notifications sheet is open, the redirect uses
      `navigate(LOGIN) { popUpTo(GATE) { inclusive = true } }`. The
      stack-replace removes the entire Home destination, which
      implicitly closes the sheet. No explicit dismiss-then-redirect
      sequencing needed.
- [x] **Q-Plan-4 (NavBar lift)** — **Resolved**: keep
      `HomeBottomBar` in `home/ui/components/` for now. The lift to
      `core/ui/AppBottomBar` will happen in the Awards screen plan when
      the second tab's root needs the same NavBar — at which point the
      shared component's API will be informed by two real call sites,
      avoiding premature abstraction.

---

## Notes

- This is the second screen in the project. The Login plan introduced
  every shared piece Home reuses (Hilt graph, Supabase client, locale
  plumbing, session state, secure-storage rules, brand tokens). Home's
  net contribution to shared code is `core/auth/AuthErrorInterceptor` +
  `AuthRedirectController` — every authenticated screen built after Home
  inherits this 401/403 routing for free.
- The Notifications panel, WriteKudo form, Search screen, Profile
  screen, Kudos feed / detail / overview, Awards overview, and Award
  Detail are out of scope for Home. Home wires placeholders for each so
  the navigation graph is complete and instrumented tests can run
  end-to-end without those screens being implemented yet.
- `SCREENFLOW.md` is still missing (carried over from Login). This
  plan's § Architecture Decisions / Integration Points + § Project
  Structure encode every outbound edge from Home; the screenflow graph
  can be reconstructed from spec § Navigation Flow + this plan when
  `/momorph.screenflow` is rerun.
- All eleven phases (Phase 0–10) follow the same discipline as the Login
  plan: Phase 0 asset prep, Phase 1 setup, Phase 2 foundation (blocking),
  Phase 3–9 user-story slices in priority order, Phase 10 polish +
  cross-cutting.
