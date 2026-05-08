# Tasks: Login

**Frame**: `8HGlvYGJWq-iOS-Login`
**Prerequisites**: plan.md (required, present), spec.md (required, present), research.md (recommended, intentionally not produced — research findings inlined in plan)

> **Note on `design-style.md`**: This project intentionally does NOT produce a
> `design-style.md`. Per `spec.md` § Visual Requirements and `plan.md` § Notes, visual
> specifications (colors, sizes, fonts, asset variants) are fetched on-demand at
> implementation time via MoMorph `query_section` / `get_node` for the Node IDs listed
> in `spec.md`. Tasks below reference those Node IDs where pixel-level detail is needed.

---

## Task Format

```
- [ ] T### [P?] [Story?] Description | file/path.ts
```

- **[P]**: Can run in parallel (different files, no dependencies on incomplete tasks)
- **[Story]**: Which user story this belongs to (US1, US2, US3, US4)
- **|**: File path affected by this task

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Project scaffolding, dependencies, build configuration, and asset preparation.

- [x] T001 Add version catalog entries for supabase-kt, ktor-android, androidx.credentials, googleid, hilt, ksp plugin, hilt-navigation-compose, datastore-preferences, navigation-compose, turbine, mockk, kotlinx-coroutines-test | gradle/libs.versions.toml
- [x] T002 Update top-level build to alias hilt + ksp plugins | build.gradle.kts
- [x] T003 Update app build: enable buildConfig, add ksp + hilt plugins, add new dependencies, expose SUPABASE_URL/SUPABASE_ANON_KEY via buildConfigField (read from local.properties) | app/build.gradle.kts
- [x] T004 [P] Add Supabase keys to local.properties (gitignored) — `supabase.url=...`, `supabase.anonKey=...` | local.properties
- [x] T005 [P] Add Hilt + coroutines + supabase-kt + ktor keep rules for release | app/proguard-rules.pro
- [x] T006 [P] Configure ktlintCheck in build to enforce Kotlin official style on every PR (Constitution Principle I) | app/build.gradle.kts
- [x] T007 [P] SAA logo + ROOT FURTHER tagline + keyvisual background downloaded via MoMorph during the UI implement pass. Delivered as PNGs in `drawable-mdpi/` (the Figma export was raster, not vector — `ic_logo_saa.png` 48×44, `ic_logo_root_further.png` 247×109, `bg_keyvisual.png` 375×812) | app/src/main/res/drawable-mdpi/ic_logo_saa.png + ic_logo_root_further.png + bg_keyvisual.png
- [x] T008 [P] Google "G" icon — SVG export from MoMorph converted to vector drawable | app/src/main/res/drawable/ic_google_g.xml
- [x] T009 [P] Country flag — MoMorph node `MM_MEDIA_IC VN Flag` returned no asset, so each `Language` enum value carries a `flagEmoji` field (🇻🇳 / 🇺🇸 / 🇯🇵) rendered via `Text` per the plan's documented fallback | (no asset; emoji-rendered in code)

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core infrastructure required by ALL user stories: Hilt graph, Supabase client,
locale plumbing, session state, secure-storage rules, database schema + RLS.

**⚠️ CRITICAL**: No user story work can begin until this phase is complete.

- [x] T010 Create Application class with `@HiltAndroidApp` | app/src/main/java/com/example/aiddproject/AIDDApplication.kt
- [x] T011 Update Manifest: declare `android:name=".AIDDApplication"`, add `INTERNET` + `ACCESS_NETWORK_STATE` permissions, set `android:networkSecurityConfig="@xml/network_security_config"` | app/src/main/AndroidManifest.xml
- [x] T012 [P] Create network security config — TLS-only base; cert-pin slot left empty pending Q5 (release-only pin set will be filled in Phase 7 Polish) | app/src/main/res/xml/network_security_config.xml
- [x] T013 [P] Update backup rules to exclude Supabase auth token storage paths (Constitution Principle IV) | app/src/main/res/xml/backup_rules.xml
- [x] T014 [P] Update data extraction rules to exclude Supabase auth token storage paths | app/src/main/res/xml/data_extraction_rules.xml
- [x] T015 [P] Create Hilt application module (singletons, IO dispatcher) | app/src/main/java/com/example/aiddproject/core/di/AppModule.kt
- [x] T016 Create Hilt Supabase module — provides `SupabaseClient` (with Auth + Postgrest plugins) configured from `BuildConfig.SUPABASE_URL` + `BuildConfig.SUPABASE_ANON_KEY` | app/src/main/java/com/example/aiddproject/core/di/SupabaseModule.kt
- [x] T017 [P] Create Language enum (VN, EN, JA) with `tag`, `localizedName`, `toLocale()` | app/src/main/java/com/example/aiddproject/core/locale/Language.kt
- [x] T018 [P] Create LanguagePreferenceRepository — Preferences DataStore-backed; default VN; falls back to VN on unsupported persisted value | app/src/main/java/com/example/aiddproject/core/locale/LanguagePreferenceRepository.kt
- [x] T019 Create LanguageProvider composable — overrides BOTH `LocalConfiguration` and `LocalContext` (via `createConfigurationContext`) so `stringResource()` switches without Activity recreation; observes `LanguagePreferenceRepository.flow` | app/src/main/java/com/example/aiddproject/core/locale/LanguageProvider.kt
- [x] T020 [P] Create AuthState sealed class — `Loading | Unauthenticated | Authenticated(user) | Error(reason)` | app/src/main/java/com/example/aiddproject/core/session/AuthState.kt
- [x] T021 Create SessionRepository — wraps `supabase.auth.sessionStatus: Flow<SessionStatus>` and maps to `StateFlow<AuthState>` | app/src/main/java/com/example/aiddproject/core/session/SessionRepository.kt
- [x] T022 [P] Author all 11 string keys + 3 a11y label keys in VN (default `values/`) per spec § Localized Copy; mark `brand_root_further`, `brand_company`, `login_cta_label` as `translatable="false"` | app/src/main/res/values/strings.xml
- [x] T023 [P] Author EN strings — 8 localizable keys + 3 a11y label keys; brand-fixed strings NOT duplicated here | app/src/main/res/values-en/strings.xml
- [x] T024 [P] Author JA strings — 8 localizable keys + 3 a11y label keys; brand-fixed strings NOT duplicated here | app/src/main/res/values-ja/strings.xml
- [x] T025 Create Supabase migration: `public.users` table (id uuid PK references `auth.users(id)`, email text not null unique, full_name text, created_at timestamptz default now()), enable RLS, policy "users_select_own" with `using (auth.uid() = id)` | supabase/migrations/20260506_init_users_table.sql
- [x] T026 [P] Create Supabase seed fixtures — one Sunner row, one non-Sunner reference for negative tests | supabase/seed.sql
- [x] T027 [P] Create Supabase setup README (Google OAuth provider config steps, local stack `supabase start` command, Q5/Q7 prerequisites) | supabase/README.md
- [x] T028 [P] Write LanguagePreferenceRepository unit test — default VN; persist round-trip; invalid stored value falls back VN | app/src/test/java/com/example/aiddproject/core/locale/LanguagePreferenceRepositoryTest.kt
- [x] T029 [P] Write SessionRepository unit test — emits `Loading` initially, `Authenticated` on session set, `Unauthenticated` on signOut | app/src/test/java/com/example/aiddproject/core/session/SessionRepositoryTest.kt

**Checkpoint**: App launches with `AIDDApplication` + Hilt graph valid. DataStore round-trips
a language. Local Supabase stack reachable from instrumented host. All 11 string keys
present in all three locales. RLS policy migration applied successfully on local stack.

---

## Phase 3: User Story 1 - Sign in with Google (Priority: P1) 🎯 MVP

**Goal**: A first-time or logged-out Sunner taps "LOGIN With Google", completes the OAuth
flow, and lands on Home. Non-Sunners are routed to Access denied.

**Independent Test**: Launch app signed out → tap CTA → complete OAuth with seeded Sunner
Google account → land on Home placeholder. Repeat with non-Sunner → land on Access denied
placeholder.

### Tests First (TDD per Constitution Principle V — written and FAILING before implementation)

- [x] T030 [P] [US1] Write LoginViewModel test — state transitions for tap → loading → success/error; double-tap suppression (FR-003) | app/src/test/java/com/example/aiddproject/auth/login/ui/LoginViewModelTest.kt
- [x] T031 [P] [US1] Write SignInWithGoogleUseCase test — happy path; Google failure; Supabase failure; not-a-Sunner | app/src/test/java/com/example/aiddproject/auth/login/domain/SignInWithGoogleUseCaseTest.kt
- [x] T032 [P] [US1] Write SupabaseAuthRepository test — `signInWithIdToken` delegation; session emitted | app/src/test/java/com/example/aiddproject/auth/login/data/SupabaseAuthRepositoryTest.kt
- [x] T033 [P] [US1] Write VerifySunnerMembershipUseCase test — Sunner row found returns true; empty/forbidden returns false and triggers signOut | app/src/test/java/com/example/aiddproject/auth/login/domain/VerifySunnerMembershipUseCaseTest.kt

### Models / state

- [x] T034 [P] [US1] Create LoginUiState data class — `isLoading: Boolean`, `error: LoginError?`, `playServicesAvailable: Boolean` | app/src/main/java/com/example/aiddproject/auth/login/ui/LoginUiState.kt
- [x] T035 [P] [US1] Create LoginError sealed class — one variant per error string key in spec § Localized Copy | app/src/main/java/com/example/aiddproject/auth/login/ui/LoginError.kt

### Data layer

- [x] T036 [P] [US1] Create AuthRepository interface — `suspend signInWithIdToken(token: String): Result<Unit>`, `suspend signOut()` | app/src/main/java/com/example/aiddproject/auth/login/data/AuthRepository.kt
- [x] T037 [US1] Implement SupabaseAuthRepository — delegates to `supabase.auth.signInWith(IDToken) { idToken; provider = Google }` and `auth.signOut()` (depends on T036) | app/src/main/java/com/example/aiddproject/auth/login/data/SupabaseAuthRepository.kt
- [x] T038 [P] [US1] Create GoogleCredentialProvider — wraps `CredentialManager.getCredential` with `GetGoogleIdOption`; returns `Result<GoogleIdToken>` | app/src/main/java/com/example/aiddproject/auth/login/data/GoogleCredentialProvider.kt

### Domain layer

- [x] T039 [US1] Implement SignInWithGoogleUseCase — orchestrates GoogleCredentialProvider → AuthRepository.signInWithIdToken → VerifySunnerMembershipUseCase (depends on T036, T038, T040) | app/src/main/java/com/example/aiddproject/auth/login/domain/SignInWithGoogleUseCase.kt
- [x] T040 [US1] Implement VerifySunnerMembershipUseCase — `supabase.from("users").select().eq("id", uid).single()`; on empty/forbidden → call `AuthRepository.signOut()` and return `NotASunner` | app/src/main/java/com/example/aiddproject/auth/login/domain/VerifySunnerMembershipUseCase.kt

### ViewModel

- [x] T041 [US1] Implement LoginViewModel — exposes `StateFlow<LoginUiState>` and `SharedFlow<LoginError>` (replay=0); intent: `onSignInTap()` enforces double-tap suppression via `if (state.value.isLoading) return` (depends on T039, T040) | app/src/main/java/com/example/aiddproject/auth/login/ui/LoginViewModel.kt

### UI

- [x] T042 [P] [US1] Create GoogleSignInButton composable — Material 3 `Button`; shows `CircularProgressIndicator` inside when `isLoading=true`; label "LOGIN With Google" always visible (FR-004, FR-015); `enabled = !isLoading && playServicesAvailable`; semantics live region announces `a11y_cta_loading` during loading | app/src/main/java/com/example/aiddproject/auth/login/ui/components/GoogleSignInButton.kt
- [x] T043 [US1] Create LoginScreen composable — `Scaffold` with `SnackbarHost(snackbarHostState)`; `LaunchedEffect` collects `LoginViewModel.errors` and dispatches via `snackbarHostState.showSnackbar`; renders only the CTA in this phase (branding added in Phase 6) (depends on T041, T042) | app/src/main/java/com/example/aiddproject/auth/login/ui/LoginScreen.kt

### Navigation

- [x] T044 [P] [US1] Create Routes constants — `route_gate`, `route_login`, `route_home`, `route_access_denied` | app/src/main/java/com/example/aiddproject/navigation/Routes.kt
- [x] T045 [US1] Create AppNavigation NavHost — wires Login → Home on success, Login → Access denied on `NotASunner`; placeholder Home and Access-denied composables (full screens out of scope) (depends on T043, T044) | app/src/main/java/com/example/aiddproject/navigation/AppNavigation.kt
- [x] T046 [US1] Update MainActivity to host AppNavigation inside `AIDDProjectTheme` and `LanguageProvider` (depends on T045) | app/src/main/java/com/example/aiddproject/MainActivity.kt

### Instrumented tests

- [x] T047 [P] [US1] Write LoginScreen Compose UI test — CTA renders; tap drives loading; error snackbar displayed for each `LoginError` variant; FR-003 double-tap suppression verified | app/src/androidTest/java/com/example/aiddproject/auth/login/LoginScreenTest.kt
- [x] T048 [US1] Write LoginIntegrationTest — against local Supabase + faked GoogleCredentialProvider that returns a known ID token; happy path Sunner → reaches Home route; non-Sunner → reaches Access denied route; signOut called on non-Sunner detection | app/src/androidTest/java/com/example/aiddproject/auth/login/LoginIntegrationTest.kt
- [x] T049 [US1] Write RLS policy test — non-Sunner JWT cannot select another user's row (Constitution Security Requirements; risk row #3) | app/src/androidTest/java/com/example/aiddproject/auth/login/RlsPolicyTest.kt

**Checkpoint**: Tap CTA with valid Sunner Google account → reach Home placeholder.
Non-Sunner → Access denied placeholder. All US1 ViewModel/repository/use-case unit tests
green; LoginIntegrationTest green; RLS denial test green.

---

## Phase 4: User Story 2 - Skip login for authenticated users (Priority: P1)

**Goal**: A user with a valid persisted session is taken straight to Home on cold start
without seeing Login. Expired/revoked session → Login. Logged-out → Login.

**Independent Test**: Sign in successfully (US1), close the app, reopen → app opens on
Home with no Login flash. Then `signOut()`, close, reopen → Login appears.

### Tests First

- [x] T050 [P] [US2] Write SessionGate Compose test — renders Home composable when state=`Authenticated`; renders Login when `Unauthenticated`; renders splash/loading when `Loading` | app/src/androidTest/java/com/example/aiddproject/core/session/SessionGateTest.kt
- [x] T051 [P] [US2] Write SessionRepository extended test — cold start with valid token → `Authenticated`; expired → `Unauthenticated`; logout → `Unauthenticated` (extends T029) | app/src/test/java/com/example/aiddproject/core/session/SessionRepositoryColdStartTest.kt

### Implementation

- [x] T052 [US2] Create SessionGate composable — observes `SessionRepository.state`; routes to Login/Home; renders splash while `Loading` to satisfy TR-005 (no Login flash) | app/src/main/java/com/example/aiddproject/core/session/SessionGate.kt
- [x] T053 [US2] Update AppNavigation — make `route_gate` the start destination; SessionGate decides initial routing (depends on T052) | app/src/main/java/com/example/aiddproject/navigation/AppNavigation.kt

**Checkpoint**: Cold start with valid session → Home directly, no Login flash measurable.
Cold start with expired session → Login. After signOut → Login on next launch.

---

## Phase 5: User Story 3 - Switch display language before signing in (Priority: P2)

**Goal**: User can switch language between VN/EN/JA from the Login screen; description and
copyright re-render immediately; CTA label "LOGIN With Google" stays English (FR-015);
choice persists across restarts.

**Independent Test**: On Login screen, open language dropdown, select EN. Description and
copyright re-render in English. CTA label unchanged. Close and reopen app — language is
still EN.

### Tests First

- [x] T054 [P] [US3] Write LanguageSelector Compose UI test — opens dropdown; only VN/EN/JA listed; selecting EN updates label + flag and emits language change | app/src/androidTest/java/com/example/aiddproject/auth/login/ui/components/LanguageSelectorTest.kt
- [x] T055 [P] [US3] Write Locale-switching instrumented test — verifies switching from VN to EN updates `login_description` text in the rendered UI within one recomposition without Activity recreation (validates dual-context override technique from plan § Locale) | app/src/androidTest/java/com/example/aiddproject/core/locale/LocaleSwitchingTest.kt

### Implementation

- [x] T056 [P] [US3] Create LanguageSelector composable — Material 3 `ExposedDropdownMenuBox`; current selection shows flag + 2-letter code; dropdown items show flag + full localized name; semantics announce `a11y_language_selector` | app/src/main/java/com/example/aiddproject/auth/login/ui/components/LanguageSelector.kt
- [x] T057 [US3] Wire LanguageSelector into LoginScreen top-right; binds to `LanguagePreferenceRepository.flow` and writes selection back via repository (depends on T043, T056) | app/src/main/java/com/example/aiddproject/auth/login/ui/LoginScreen.kt

**Checkpoint**: Switching language re-renders all localizable text within 1 frame
(SC-004). Persists across restart. CTA label unchanged on language switch.

---

## Phase 6: User Story 4 - Understand product context before signing in (Priority: P3)

**Goal**: Logo, "ROOT FURTHER" tagline, localized invitation description, and copyright are
all visible on Login.

**Independent Test**: Open the app while signed out — verify logo, "ROOT FURTHER",
description, copyright are all visible on the rendered screen.

### Implementation

- [x] T058 [US4] Add Sun* Annual Awards logo to LoginScreen — `Image(painterResource(R.drawable.ic_logo_saa), contentDescription = null)` (decorative, skipped by screen reader per spec § Behavioral Accessibility) | app/src/main/java/com/example/aiddproject/auth/login/ui/LoginScreen.kt
- [x] T059 [US4] Add ROOT FURTHER tagline to LoginScreen — rendered as `Image(R.drawable.ic_logo_root_further)` (raster export from Figma) with brand-string `contentDescription` rather than the `Text` originally planned | app/src/main/java/com/example/aiddproject/auth/login/ui/LoginScreen.kt
- [x] T060 [US4] Add localized description to LoginScreen — `Text(stringResource(R.string.login_description))` | app/src/main/java/com/example/aiddproject/auth/login/ui/LoginScreen.kt
- [x] T061 [US4] Add localized copyright to LoginScreen — `Text(stringResource(R.string.login_copyright))` | app/src/main/java/com/example/aiddproject/auth/login/ui/LoginScreen.kt
- [x] T062 [P] [US4] Smoke instrumented test — all four static elements visible on render | app/src/androidTest/java/com/example/aiddproject/auth/login/LoginBrandingTest.kt

**Checkpoint**: All four static elements visible on every render of the Login screen
(FR-014). Description and copyright re-render on language change (verified via T055).

---

## Phase 7: Polish & Cross-Cutting Concerns

**Purpose**: Accessibility hardening, security hardening, telemetry, quality gates.

- [x] T063 [P] CTA polite live region for loading announcements (already wired in T042); focus order is the Compose default — language selector → description → CTA — verified by the natural composition order in `LoginScreenContent` | app/src/main/java/com/example/aiddproject/auth/login/ui/LoginScreen.kt
- [x] T064 [P] Keyboard parity instrumented test — `Key.Enter` on focused CTA fires `onSignInTap` | app/src/androidTest/java/com/example/aiddproject/auth/login/LoginKeyboardTest.kt
- [x] T065 [P] Google Play Services availability check on `LoginScreen` enter — `GoogleApiAvailability.isGooglePlayServicesAvailable()` flips `playServicesAvailable=false` and surfaces an indefinite `error_oauth_play_services` snackbar | app/src/main/java/com/example/aiddproject/auth/login/ui/LoginScreen.kt
- [x] T066 [P] Localized accessibility labels for language dropdown — `a11y_language_selector` template + `Language.nativeName` interpolation already shipped in Phase 5 | app/src/main/res/values/strings.xml + LanguageSelector.kt
- [x] T067 [P] `SecureTimberTree` token scrubber (regexes: access_token / refresh_token / Bearer / id_token / anon_key / api_key / password / secret / bare JWT prefix) | app/src/main/java/com/example/aiddproject/core/logging/SecureTimberTree.kt
- [x] T068 [P] Wired `SecureTimberTree` in `AIDDApplication.onCreate` under `BuildConfig.DEBUG` | app/src/main/java/com/example/aiddproject/AIDDApplication.kt
- [ ] T069 [P] **Deferred** — CI workflow doesn't exist yet for this repo. Action: when CI is provisioned, add a step that runs `aapt2 dump badging` + `strings` over the release APK and greps for `eyJ`, `access_token`, `Bearer ` patterns; fail on any hit | .github/workflows/android.yml (TBD)
- [ ] T070 [P] **Deferred** — needs telemetry SDK choice (Firebase Analytics, Datadog, OpenTelemetry?). Tracked under plan Q-followup. Stub today: emit Timber breadcrumbs at the four LoginViewModel transition points; replace with real SDK once selected | app/src/main/java/com/example/aiddproject/auth/login/ui/LoginViewModel.kt
- [ ] T071 [P] **Deferred** — same blocker as T070; needs telemetry SDK | app/src/main/java/com/example/aiddproject/core/session/SessionGate.kt
- [ ] T072 **Deferred** — Q5 (cert-pinning strategy) unresolved; SHA-256 pin hashes for Supabase project not yet captured. Network security config has TLS-only baseline already (Phase 2 T012); pin set fills in pre-release | app/src/main/res/xml/network_security_config.xml
- [ ] T073 [P] **Deferred** — depends on T072 pin set being non-empty | app/src/androidTest/java/com/example/aiddproject/core/network/CertPinningTest.kt
- [x] T074 [P] String-resource parity test — every `translatable="true"` key in `values/strings.xml` exists in EN + JA; brand-fixed keys are NOT duplicated into locale folders | app/src/test/java/com/example/aiddproject/core/locale/StringResourceParityTest.kt
- [x] T075 [P] Touch-target instrumented test — GoogleSignInButton ≥ 48dp on both axes; LanguageSelector anchor width ≥ 48dp | app/src/androidTest/java/com/example/aiddproject/auth/login/TouchTargetTest.kt
- [x] T076 Local Quality Gates — `./gradlew :app:assembleDebug :app:ktlintCheck :app:testDebugUnitTest` green; `connectedDebugAndroidTest` requires an emulator/device + Q4/Q7 prereqs (deferred) | (no file)

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies — start immediately.
- **Foundational (Phase 2)**: Depends on Phase 1. **BLOCKS all user stories.**
- **US1 (Phase 3)**: Depends on Phase 2. MVP.
- **US2 (Phase 4)**: Depends on Phase 2 + Phase 3 navigation in place (uses Login route). Could also start in parallel with US1's later sub-tasks if SessionRepository is complete (T021 done in Phase 2).
- **US3 (Phase 5)**: Depends on Phase 2 + Phase 3 (modifies LoginScreen). Can run in parallel with US2 once US1 ships LoginScreen v1.
- **US4 (Phase 6)**: Depends on Phase 2 + Phase 3 (modifies LoginScreen). Can run in parallel with US2 and US3 once US1 ships LoginScreen v1.
- **Polish (Phase 7)**: Depends on all desired user stories being complete.

### Within Each User Story

- Tests written and FAILING before implementation (Constitution Principle V).
- Models / sealed types → Repositories → Use cases → ViewModel → UI → Navigation wiring.
- Story complete and independently testable before moving to next priority.

### Parallel Opportunities

- **Phase 1**: T004–T009 all `[P]` — parallel after T001–T003 finish.
- **Phase 2**: T012–T014, T015 + T017 + T018 + T020, T022–T024, T026–T029 marked `[P]` — many can run concurrently. Single non-`[P]` chain is T010 → T011 (Application class + Manifest), T016 (Supabase module — wait for T015 AppModule), T019 (LanguageProvider — wait for T017 + T018), T021 (SessionRepository — wait for T020 + T016), T025 (migration — independent of code).
- **Phase 3 (US1)**: T030–T033 (all tests) `[P]`. T034 + T035 + T036 + T038 + T042 + T044 `[P]`. Sequential chain: T037 ← T036, T039 ← T036+T038, T040 ← inherits, T041 ← T039+T040, T043 ← T041+T042, T045 ← T043+T044, T046 ← T045, T047 + T048 + T049 `[P]` after T046.
- **Phases 4, 5, 6**: All independent of each other (modify different files or different sections of LoginScreen) — can run in parallel after Phase 3 completes.
- **Phase 7**: Most tasks `[P]` — only T076 (the final Quality Gates run) depends on all others.

---

## Implementation Strategy

### MVP First (Recommended)

1. Complete Phase 1 + Phase 2 (Setup + Foundational).
2. Complete Phase 3 (US1) only.
3. **STOP and VALIDATE**: end-to-end Sign-In with Google works against staging Supabase
   with a real Sunner account. Non-Sunner → Access denied works. RLS denial test green.
4. Deploy to internal channel for stakeholder verification.

### Incremental Delivery

1. MVP cut (above) → Test → Internal release.
2. Add US2 (auto-login) → Test → Release.
3. Add US3 (language switcher) → Test → Release.
4. Add US4 (branding completion) → Test → Release.
5. Phase 7 polish across all five releases — accessibility, telemetry, cert pinning,
   log scrubbing.

---

## Notes

- **Independent test criteria for each story** are stated above; honor them in PR
  descriptions so reviewers can verify story-by-story.
- **Open infra/security questions** Q4 (CI Supabase strategy), Q5 (cert pinning), Q6
  (JVM target), Q7 (OAuth SHA-256 registration), Q8 (per-app-language UI) are tracked in
  `plan.md`. Q5 must resolve before T072. Q7 must resolve before T048. Q6 may force a
  one-task addition in Phase 1 (mechanical bump of `compileOptions` + `kotlinOptions`).
- **Visual specs** for any task that needs pixel-level fidelity (button styling, dropdown
  layout, branding placement) are fetched at task-execution time via MoMorph
  `query_section` / `get_node` for the Node IDs in `spec.md` — not in this tasks file.
- **Commit cadence**: commit after each task or logical group. Mark tasks complete as
  you go: `[x]`. Run unit tests before moving to the next task; run instrumented tests
  before completing a phase.
