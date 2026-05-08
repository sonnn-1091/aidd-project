# Implementation Plan: Login

**Frame**: `8HGlvYGJWq-iOS-Login`
**Date**: 2026-05-06
**Spec**: `specs/8HGlvYGJWq-iOS-Login/spec.md`

---

## Summary

Implement the Login screen for the SAA 2025 Android app: a single Google-OAuth CTA, a
language selector (VN default, EN/JA), and static branding. Authentication goes through
**Supabase Auth** using Android's **Credential Manager + Google ID provider** to obtain a
Google ID token, then `signInWithIdToken` against Supabase. Authorization (is this Google
user a registered Sunner?) is enforced post-login via a Supabase `users` table fetch under
RLS — non-Sunners are signed out and routed to `[iOS] Access denied`. Auto-login (US2) is
handled by a `SessionGate` composable that resolves the cached Supabase session before
rendering the Login UI.

---

## Technical Context

**Language/Framework**: Kotlin 2.2.10 / Jetpack Compose (Material 3)
**Primary Dependencies**: `supabase-kt` (Auth + Postgrest), `androidx.credentials` +
`credentials-play-services-auth`, `googleid`, Hilt (DI), Jetpack DataStore (Preferences),
Navigation-Compose
**Platform**: Android-only — min SDK 31, target SDK 36, Java 11 (per
`app/build.gradle.kts`)
**Database**: Supabase (Postgres) — RLS-enforced
**Testing**: JUnit + kotlinx-coroutines-test + Turbine + MockK (unit);
`androidx.compose.ui.test` (instrumented); local Supabase via `supabase start` for repo
integration tests
**State Management**: ViewModel + `StateFlow<LoginUiState>`; sealed `AuthState` for global
session state; Hilt-scoped repositories
**API Style**: Supabase SDK calls (no hand-rolled REST). Network only outbound to
`<project>.supabase.co` over TLS.

---

## Constitution Compliance Check

*GATE: Must pass before implementation can begin. Each item maps to a principle in
`.momorph/constitution.md`.*

- [x] **I. Clean Code & Source Organization** — feature-first package layout under
      `com.example.aiddproject.auth.login.{ui,data,domain}` with a sibling
      `settings.language` package and a top-level `core/` for shared infrastructure.
      Composables kept <150 LOC by extracting `GoogleSignInButton` and `LanguageSelector`.
- [x] **II. Tech Stack Best Practices** — idiomatic Kotlin (sealed class for `AuthState`,
      `data class` UI state, `StateFlow`/coroutines, no blocking calls); Compose state
      hoisted from screen → VM; repository pattern over Supabase SDK; Supabase **anon key
      only** (loaded from `local.properties` → `BuildConfig`); versions pinned in
      `gradle/libs.versions.toml`.
- [x] **III. Material Design 3 (Android)** — Material 3 `Button` for the CTA;
      `ExposedDropdownMenuBox` for the language selector; theme tokens via existing
      `ui/theme/`; light + dark; dynamic color on Android 12+; `WindowSizeClass` for
      phone/foldable/tablet; min touch target 48dp on every interactive element.
- [x] **IV. OWASP Secure Coding** — no secrets in source (Supabase URL + anon key in
      gitignored `local.properties`, surfaced via `BuildConfig`); Supabase SDK persists
      tokens via Android Keystore-backed storage; TLS-only with cert pinning in release;
      no token / PII logging (lint rule + log scrubber); RLS-enforced authorization on the
      `users` table; threat model recorded below; declared minimum runtime permissions
      (`INTERNET` only — no `READ_CONTACTS`, no broad permissions).
- [x] **V. Test-Driven Development** — every FR/SC in spec has a failing test committed
      before implementation. Order: ViewModel state-machine → repository contract → UI
      Compose tests → instrumented integration against local Supabase. RLS policy test
      proves a non-Sunner Google account is denied.

**Violations**: none anticipated.

| Violation | Justification | Alternative Rejected |
|-----------|---------------|---------------------|
| (none) | — | — |

---

## Architecture Decisions

### Frontend (Jetpack Compose)

- **Component Structure**: Feature-based, MVVM. `LoginScreen.kt` is a stateless composable
  taking `LoginUiState` + callbacks; `LoginViewModel` owns state and exposes
  `StateFlow<LoginUiState>`. Subcomponents `GoogleSignInButton.kt` and
  `LanguageSelector.kt` are also stateless.
- **State**:
  - Screen-local: `LoginUiState(isLoading, error)` in VM.
  - Global: `AuthState` (sealed) provided via Hilt-scoped `SessionRepository`; observed by
    `SessionGate` composable that gates `LoginScreen` vs `HomeScreen`.
  - Persisted: `LanguagePreference` via Preferences DataStore; exposed as a `Flow<Language>`.
- **Navigation**: Navigation-Compose. Routes: `gate → login → home`, plus `access_denied`.
  Auth flow uses Activity-based Credential Manager (no fragment back-stack interference).
- **Locale (in-memory, no Activity recreation)**: `LanguageProvider` wraps the nav host
  and observes `LanguagePreference: Flow<Language>`. On each change it builds a
  `Configuration` overlay (`Configuration(LocalConfiguration.current).apply
  { setLocale(language.toLocale()) }`), creates a locale-overridden context with
  `LocalContext.current.createConfigurationContext(overlay)`, and provides BOTH overrides
  via `CompositionLocalProvider`:
  ```kotlin
  CompositionLocalProvider(
      LocalConfiguration provides overlay,
      LocalContext provides localizedContext,
  ) { content() }
  ```
  Both overrides are required: `stringResource()` reads from `LocalContext.current.resources`,
  so overriding only `LocalConfiguration` would not switch text. With both overrides,
  recomposition propagates the new locale within a single frame, satisfying SC-004 with
  no Activity recreation and full Compose state preservation.

### Backend (Supabase)

- **Auth Provider**: Google OAuth configured in the Supabase project dashboard. Client
  flow: Credential Manager → Google ID token → `supabase.auth.signInWith(IDToken) {
  idToken = token; provider = Google; }`. No PKCE redirect / browser handoff needed —
  ID-token flow is the recommended Android path.
- **Spec API mapping** (resolves divergence with spec § API Dependencies):
  | Spec row (predicted) | Plan implementation | Notes |
  |---|---|---|
  | `auth.signInWithOAuth({provider: 'google'})` | `auth.signInWith(IDToken) { idToken; provider = Google }` | Replaced — Android uses ID-token flow, not browser redirect |
  | `auth.exchangeCodeForSession(code)` | **N/A** | Not needed under ID-token flow; deleted from API surface |
  | `auth.getSession()` | `SessionRepository` exposes `supabase.auth.sessionStatus: Flow<SessionStatus>` | SDK-managed |
  | `auth.refreshSession()` | Handled transparently by SDK | No explicit call site |
  | `auth.signOut()` | Called by `VerifySunnerMembershipUseCase` on non-Sunner detection | Used in error path only on this screen |
  | `GET /rest/v1/users?id=eq.{id}` | `supabase.from("users").select().eq("id", uid).single()` | Postgrest, RLS-gated |
- **Authorization**: A `public.users` table keyed on `auth.users.id` stores the Sunner
  whitelist. RLS policy: `select` allowed only when `auth.uid() = id`. Post-login the
  client calls `from("users").select("*").eq("id", auth.uid()).single()`. Empty/forbidden
  → not a Sunner → sign out + navigate to `[iOS] Access denied`.
- **Migrations**: Live in `supabase/migrations/`. The `users` table + RLS policy are part
  of the same migration that creates the table (per constitution's Security Requirements).
  Seeded fixture data for integration tests.

### Integration Points

- **Existing services**: None — first feature in the codebase.
- **Shared components**: A new `SessionGate` and `LanguageProvider` will be created in
  this slice and reused by every subsequent screen.
- **API contracts**: Supabase Auth + a single Postgrest read on `users`. No custom
  endpoints. Concrete contracts are deferred to `/momorph.apispecs`.
- **Secrets / config**: `local.properties` declares `supabase.url` and `supabase.anonKey`;
  `app/build.gradle.kts` exposes them via `buildConfigField`. The release-build CI
  injects them from a vault.

---

## Project Structure

### Documentation (this feature)

```text
.momorph/specs/8HGlvYGJWq-iOS-Login/
├── spec.md              # Feature specification (existing)
├── plan.md              # This file
├── tasks.md             # To be generated by /momorph.tasks
└── testcase.md          # To be generated by /momorph.createtestcases
```

### Source Code (affected areas)

```text
app/src/main/java/com/example/aiddproject/
├── AIDDApplication.kt                                  # NEW — @HiltAndroidApp entry
├── MainActivity.kt                                     # MODIFIED — host nav graph + theme
├── core/
│   ├── di/
│   │   ├── AppModule.kt                                # NEW — Hilt singletons
│   │   └── SupabaseModule.kt                           # NEW — provides SupabaseClient
│   ├── locale/
│   │   ├── Language.kt                                 # NEW — enum VN, EN, JA
│   │   ├── LanguagePreferenceRepository.kt             # NEW — DataStore-backed
│   │   └── LanguageProvider.kt                         # NEW — Compose wrapper
│   ├── session/
│   │   ├── AuthState.kt                                # NEW — sealed state
│   │   ├── SessionRepository.kt                        # NEW — wraps Supabase auth.sessionStatus
│   │   └── SessionGate.kt                              # NEW — composable router
│   └── ui/theme/                                       # EXISTING — extend tokens if needed
├── auth/
│   └── login/
│       ├── ui/
│       │   ├── LoginScreen.kt                          # NEW — stateless screen
│       │   ├── LoginViewModel.kt                       # NEW — Hilt VM
│       │   ├── LoginUiState.kt                         # NEW — data class
│       │   └── components/
│       │       ├── GoogleSignInButton.kt               # NEW
│       │       └── LanguageSelector.kt                 # NEW
│       ├── data/
│       │   ├── AuthRepository.kt                       # NEW — interface
│       │   ├── SupabaseAuthRepository.kt               # NEW — impl
│       │   └── GoogleCredentialProvider.kt             # NEW — wraps Credential Manager
│       └── domain/
│           ├── SignInWithGoogleUseCase.kt              # NEW
│           └── VerifySunnerMembershipUseCase.kt        # NEW
└── navigation/
    ├── AppNavigation.kt                                # NEW — NavHost
    └── Routes.kt                                       # NEW — route constants

app/src/main/res/
├── values/strings.xml                                  # MODIFIED — add VN strings (default)
├── values-en/strings.xml                               # NEW — EN strings
├── values-ja/strings.xml                               # NEW — JA strings
├── drawable/                                           # NEW assets — see Phase 0
└── xml/
    ├── backup_rules.xml                                # MODIFIED — exclude auth tokens
    └── data_extraction_rules.xml                       # MODIFIED — exclude auth tokens

app/src/main/AndroidManifest.xml                        # MODIFIED — INTERNET, ACCESS_NETWORK_STATE,
                                                        #            android:name=.AIDDApplication,
                                                        #            android:networkSecurityConfig
app/src/main/res/xml/network_security_config.xml        # NEW — TLS-only + cert-pinning config
# NOTE: NOT adding locales_config.xml. The in-memory locale switcher (see
# Architecture → Locale) handles language changes without Activity recreation.
# Adding locales_config.xml would expose a system per-app-language Settings UI,
# which uses AppCompatDelegate.setApplicationLocales and DOES recreate the
# Activity — conflicting with the in-memory approach. See open question Q8 if
# system per-app-language support becomes a requirement later.
app/build.gradle.kts                                    # MODIFIED — deps + buildConfig fields,
                                                        #            buildFeatures.buildConfig=true,
                                                        #            ksp + hilt plugins
app/proguard-rules.pro                                  # MODIFIED — Hilt + coroutines + supabase-kt
                                                        #            keep rules for release
gradle/libs.versions.toml                               # MODIFIED — versions + plugin entries
build.gradle.kts                                        # MODIFIED — top-level: alias hilt + ksp
local.properties                                        # MODIFIED — supabase.url, supabase.anonKey
                                                        #            (gitignored)

supabase/                                                # NEW — local Supabase config
└── migrations/
    └── 20260506_init_users_table.sql                  # NEW — users table + RLS

app/src/test/java/com/example/aiddproject/
├── auth/login/ui/LoginViewModelTest.kt                 # NEW
├── auth/login/data/SupabaseAuthRepositoryTest.kt       # NEW
├── auth/login/domain/SignInWithGoogleUseCaseTest.kt    # NEW
└── core/locale/LanguagePreferenceRepositoryTest.kt     # NEW

app/src/androidTest/java/com/example/aiddproject/
├── auth/login/LoginScreenTest.kt                       # NEW — Compose UI test
├── auth/login/LoginIntegrationTest.kt                  # NEW — vs local Supabase
└── core/session/SessionGateTest.kt                     # NEW
```

### Dependencies

| Package | Catalog alias | Version | Purpose |
|---------|---------------|---------|---------|
| `io.github.jan-tennert.supabase:auth-kt` | `supabase-auth` | 3.x (latest) | Supabase Auth client |
| `io.github.jan-tennert.supabase:postgrest-kt` | `supabase-postgrest` | 3.x (latest) | Read `users` table |
| `io.ktor:ktor-client-android` | `ktor-android` | matched to supabase-kt | HTTP engine for supabase-kt |
| `androidx.credentials:credentials` | `androidx-credentials` | 1.3.x | Credential Manager API |
| `androidx.credentials:credentials-play-services-auth` | `androidx-credentials-play-services-auth` | 1.3.x | Google credential provider |
| `com.google.android.libraries.identity.googleid:googleid` | `googleid` | 1.1.x | `GetGoogleIdOption` |
| `com.google.dagger:hilt-android` | `hilt-android` | 2.52+ | DI |
| `com.google.dagger:hilt-compiler` | `hilt-compiler` | 2.52+ | Hilt processor (via **KSP**, not KAPT — project uses Kotlin 2.2.10) |
| `com.google.devtools.ksp` (plugin) | `ksp` | matched to Kotlin 2.2.10 | Annotation processor for Hilt |
| `androidx.hilt:hilt-navigation-compose` | `hilt-navigation-compose` | 1.2.x | `hiltViewModel()` |
| `androidx.datastore:datastore-preferences` | `datastore-preferences` | 1.1.x | `LanguagePreference` |
| `androidx.navigation:navigation-compose` | `navigation-compose` | 2.8.x | NavHost |
| `app.cash.turbine:turbine` | `turbine` | 1.x | Flow testing | `testImplementation` |
| `io.mockk:mockk` | `mockk` | 1.13.x | Mocking | `testImplementation` |
| `org.jetbrains.kotlinx:kotlinx-coroutines-test` | `coroutines-test` | matched | Test dispatchers | `testImplementation` |

All versions pinned in `gradle/libs.versions.toml`. Dependabot/Renovate configured to scan
weekly (per constitution Principle IV).

**Compose BOM note (Constitution Principle II)**: existing Compose UI/Material3/tooling
deps continue to come from `androidx.compose:compose-bom` (already pinned in
`libs.versions.toml`). `navigation-compose` and `hilt-navigation-compose` are NOT covered
by the Compose BOM — they ship their own version coordinates and must be pinned
independently (which is why they have explicit version columns above).

**Java/JVM target check**: existing `app/build.gradle.kts` declares Java 11. Verify in
Phase 1 whether the chosen `supabase-kt` version requires source/target 17 — if so,
upgrade `compileOptions` and add `kotlinOptions { jvmTarget = "17" }`. See open question Q7.

---

## Implementation Strategy

### Phase Breakdown

#### Phase 0: Asset Preparation

- Download from MoMorph using `mcp__momorph__get_media_files` / `list_media_nodes` for
  Node IDs `6885:8977` (Sun* logo) and `6885:8969` (Google "G" icon if not vector). Place
  vector assets in `app/src/main/res/drawable/` (mdpi, hdpi, xhdpi, xxhdpi, xxxhdpi
  variants where rasterized).
- Country flag for the language selector: prefer Compose-rendered emoji or a vector flag
  shipped with the app — confirm the design source provides the flag asset; otherwise
  use the system emoji 🇻🇳 / 🇺🇸 / 🇯🇵 with Compose `Text`.
- Naming: `ic_logo_saa.xml`, `ic_google_g.xml`, `ic_flag_vn.xml`, etc.

#### Phase 1: Foundation (blocking)

- Add dependencies + `libs.versions.toml` entries.
- Configure `local.properties` reading in `build.gradle.kts` →
  `BuildConfig.SUPABASE_URL`, `BuildConfig.SUPABASE_ANON_KEY`.
- Create `AIDDApplication` with `@HiltAndroidApp`; register in `AndroidManifest.xml`.
- Wire Hilt modules: `AppModule`, `SupabaseModule` (provides `SupabaseClient`,
  `SessionRepository`).
- Create `core/locale/` (Language, repository, provider) with Preferences DataStore.
- Create `core/session/AuthState` sealed class + `SessionRepository`.
- Set up Supabase local stack (`supabase/migrations/20260506_init_users_table.sql`) with:
  - `public.users` (id uuid PK references `auth.users(id)`, email text, full_name text,
    created_at timestamptz default now()).
  - RLS enabled; policy "users can read own row" with `using (auth.uid() = id)`.
  - Test fixture for one Sunner and one non-Sunner.
- Configure `backup_rules.xml` and `data_extraction_rules.xml` to exclude auth token
  storage paths (per constitution IV).

**Checkpoint**: App launches; Hilt graph valid; DataStore round-trips a language; local
Supabase is reachable from instrumented tests.

#### Phase 2: User Story 1 — Sign in with Google (P1, MVP)

- **Tests first** (TDD per Principle V):
  - `LoginViewModelTest`: state transitions for tap → loading → success/error;
    double-tap suppression.
  - `SignInWithGoogleUseCaseTest`: happy path + Google failure + Supabase failure +
    not-a-Sunner.
  - `SupabaseAuthRepositoryTest`: signInWithIdToken delegation; session persistence.
  - Compose UI: tap CTA emits intent; loading state visible; error displayed.
- **Implementation**:
  - `GoogleCredentialProvider`: wraps `CredentialManager.getCredential` with
    `GetGoogleIdOption`. Returns `Result<GoogleIdToken>`.
  - `SupabaseAuthRepository`: `signIn(idToken)` → `signInWith(IDToken)`.
  - `VerifySunnerMembershipUseCase`: post-auth `users` fetch; on empty → call
    `signOut()` and emit `AuthState.Error(NotASunner)`.
  - `LoginViewModel`: orchestrates the use case; exposes `LoginUiState`.
  - `LoginScreen` + `GoogleSignInButton`: stateless; consumes VM.
  - Navigation: success → `home`; not-a-Sunner → `access_denied`; failure → stay.
- **Integration test (instrumented)**: end-to-end against local Supabase with a seeded
  `users` row; mocked Credential Manager (use a test fake that returns a known ID token
  signed by a test Google service account, OR stub the Google call entirely and exercise
  the Supabase path with a service-role-issued JWT in test only).

**Checkpoint**: Tap CTA with valid Sunner Google account → reach Home. Non-Sunner →
Access denied. All ViewModel tests + at least one instrumented happy-path test green.

#### Phase 3: User Story 2 — Auto-login (P1)

- **Tests first**:
  - `SessionGateTest`: renders Home when `AuthState.Authenticated`; renders Login when
    `Unauthenticated`; renders splash/loading when `Loading`.
  - `SessionRepositoryTest`: cold start with valid token → `Authenticated`; expired token
    → `Unauthenticated`; signed out → `Unauthenticated`.
- **Implementation**: `SessionGate` composable as the root of the nav graph; subscribes
  to `SessionRepository.state: StateFlow<AuthState>`. The Supabase SDK's `sessionStatus`
  flow is the source of truth.

**Checkpoint**: Cold start with valid session → Home, no Login flash (TR-005).

#### Phase 4: User Story 3 — Language switcher (P2)

- **Tests first**:
  - `LanguagePreferenceRepositoryTest`: default = VN; persistence; invalid stored value →
    fallback VN.
  - Compose UI: open dropdown → select EN → description/copyright re-render in EN.
- **Implementation**: `LanguageSelector` composable using `ExposedDropdownMenuBox`; on
  selection, write to DataStore; `LanguageProvider` reacts and updates locale.

**Checkpoint**: Switching language re-renders all localizable text within 1 frame
(SC-004).

#### Phase 5: User Story 4 — Static branding (P3) + string-resource scaffolding

- Logo, "ROOT FURTHER", description, copyright rendered.
- Author **all 11 string keys** from spec § Localized Copy:

  Brand-fixed (`values/strings.xml` only, with `translatable="false"`; NOT duplicated
  into `values-en/` or `values-ja/`):
  - `brand_root_further` = "ROOT FURTHER"
  - `brand_company` = "Sun*"  (used to compose `login_copyright` and similar)
  - `login_cta_label` = "LOGIN With Google"  (FR-015)

  Localized display (present in `values/strings.xml` for VN, with overrides in
  `values-en/strings.xml` and `values-ja/strings.xml`):
  - `login_description`
  - `login_copyright`

  Localized errors (same three-file pattern):
  - `error_oauth_network`
  - `error_oauth_account_disabled`
  - `error_oauth_code_expired`
  - `error_oauth_play_services`
  - `error_oauth_session_expired`
  - `error_oauth_generic`

  Localized accessibility labels (used by the screen-reader announcements specified in
  Phase 6):
  - `a11y_cta_idle` = "Sign in with Google, button"
  - `a11y_cta_loading` = "Signing in"
  - `a11y_language_selector` (formatted with current language full-name, e.g.
    "Language, Tiếng Việt, dropdown")

  Verbatim values for VN/EN/JA come from spec § Localized Copy. Add a unit test that
  enumerates `values-en` and `values-ja` and fails if any non-`translatable="false"` key
  is missing in either locale.
- Smoke test: all four static elements present and visible on render.

#### Phase 6: Polish & Cross-Cutting

- **Accessibility (per spec § Behavioral Accessibility)**:
  - Focus order: language selector → description → CTA. Logo + copyright marked
    `Modifier.semantics { invisibleToUser() }` (or `contentDescription = null` for
    images).
  - CTA: `Modifier.semantics { liveRegion = LiveRegionMode.Polite }` so loading-state
    transitions ("Signing in") are announced.
  - Language dropdown: `contentDescription` uses the full localized language name
    ("Tiếng Việt"/"English"/"日本語"), not the two-letter code displayed visibly.
  - **Keyboard parity** (spec requires Enter/Space MUST equal tap): rely on Compose's
    default key-event handling for `Button` + `ExposedDropdownMenuBox`; add
    instrumented test that drives interaction via `KeyEvent` and asserts the same
    state transition occurs.
  - Touch targets ≥ 48dp on every interactive control (constitution Principle III);
    enforced by Compose lint warnings + a smoke test that measures size in pixels.
- **Google Play Services availability check** (Credential Manager prerequisite):
  on `LoginScreen` enter, call `GoogleApiAvailability.getInstance()
  .isGooglePlayServicesAvailable(context)`. If unavailable, show a non-recoverable
  error state directing the user to update Play Services. Keep the CTA disabled in
  this state.
- Error surfacing wiring (string keys themselves are added in Phase 5):
  - `LoginScreen` is wrapped in a Material 3 `Scaffold` with a `SnackbarHost(snackbarHostState)`
    at the bottom slot. `snackbarHostState = remember { SnackbarHostState() }`.
  - VM exposes a `SharedFlow<LoginError>` (one-shot events, replay = 0). `LoginScreen`
    consumes via `LaunchedEffect(Unit) { vm.errors.collect { ... snackbarHostState.showSnackbar(...) } }`.
  - `Snackbar` config: bottom; `SnackbarDuration.Short` (6s); single-line; latest
    replaces previous (`SnackbarHostState.showSnackbar` cancels any in-flight snackbar).
  - Error → string key mapping: `LoginError` is a sealed type whose variants 1:1 with
    the 6 error keys defined in Phase 5; the screen resolves via `stringResource(error.key)`.
- Loading state visual: locked — Material 3 `CircularProgressIndicator` inside the
  `Button`; label "LOGIN With Google" remains visible and unchanged; button
  `enabled = false` while loading. CTA accessibility live region announces "Signing in".
- Telemetry hooks for SC-001 (OAuth success rate within 5s), SC-002 (Login-flash
  detector — instrument `SessionGate` to measure resolution time), SC-003
  (duplicate-tap counter), SC-005 (log-scrub validation in CI).
- Cert pinning approach: declare TLS-only + pin sets in
  `network_security_config.xml`. **Pin SPKI hashes**, not full cert hashes (LetsEncrypt
  rotates leaf certs frequently); pin the current intermediate plus 1–2 backup pins.
  See open question Q5 for the strategy decision. Release-only instrumented test
  validates the pin set is non-empty and matches the Supabase project domain.
- Run `./gradlew lint ktlintCheck assembleDebug testDebugUnitTest connectedDebugAndroidTest`
  per constitution Quality Gates.

### Risk Assessment

| Risk | Probability | Impact | Mitigation |
|------|-------------|--------|------------|
| Supabase Google provider not configured before Phase 2 implementation | Medium | High | Treat provider config as a Phase 1 gate; document setup in `supabase/README.md`. Block Phase 2 until verified end-to-end against staging Supabase. |
| Credential Manager Google ID flow differences across Play Services versions | Medium | Medium | Test on min SDK 31 device + latest device; use stable `googleid` 1.1.x. Fallback path: surface an actionable error directing users to update Play Services. |
| RLS policy bug allows non-Sunners to read other rows | Low | Critical | Policy test in instrumented suite (per spec FR-005 and constitution Security Requirements). Reviewer checklist item: every new table → RLS policy in same migration. |
| Token leakage via Logcat in debug builds | Medium | Critical | Custom `Timber` tree that scrubs known-secret patterns; CI grep step on `assembleRelease` output for token-like strings; SC-005 enforces zero leakage. |
| `local.properties` keys committed accidentally | Low | High | `local.properties` already gitignored (verified); `.gitignore` audited; pre-commit hook to reject diffs touching `local.properties` content. |
| In-memory locale switcher mis-implemented (overrides only `LocalConfiguration`, not `LocalContext`) → strings don't switch | Medium | Medium | Plan codifies the dual-override technique in Architecture → Locale; instrumented test asserts `description` text changes from VN to EN within one recomposition without Activity recreation. |
| Local Supabase stack flakes in CI | Medium | Medium | Use `supabase start` in CI with cached image; fall back to a hosted ephemeral Supabase project for CI runs if local proves unstable. |
| Credential Manager test doubles complex to set up for instrumented tests | Medium | Medium | Stub `GoogleCredentialProvider` behind a Hilt test module that returns a fixed ID token; integration tests then exercise the Supabase path only. |
| supabase-kt 3.x requires JVM 17, project on Java 11 | Medium | Medium | Verify in Phase 1 prereqs. Mechanical fix: update `compileOptions` + `kotlinOptions.jvmTarget`. CI bump too. (Q6) |
| Cert-pinning approach picked too late, release blocked | Medium | High | Resolve Q5 before Phase 6. If skipping pinning, document the threat-model rationale (constitution Principle IV requires explicit decision). |
| Google Cloud OAuth client SHA-256 cert hash not registered before Phase 2 integration tests | High | High | Prereq item: register **debug** SHA-256 in Google Cloud OAuth client config before Phase 2 starts; release SHA-256 before release build. (Q7) |

### Estimated Complexity

- **Frontend**: Medium (Compose + Material 3 dropdown + state machine + i18n)
- **Backend**: Low (one table, one RLS policy, Supabase config)
- **Testing**: Medium-High (Credential Manager fakes + local Supabase + Compose UI tests
  + RLS policy tests are all individually straightforward but stack up)

---

## Integration Testing Strategy

### Test Scope

- [x] **Component/Module interactions**: `LoginViewModel` ↔ use cases ↔ repositories;
      `SessionGate` ↔ `SessionRepository` ↔ Supabase SDK.
- [x] **External dependencies**: Supabase Auth + Postgrest (real, against local stack);
      Credential Manager (faked).
- [x] **Data layer**: Preferences DataStore (`LanguagePreference`); Supabase
      `auth.users` + `public.users`.
- [x] **User workflows**: cold-start → Login → Google tap → Home; cold-start with valid
      session → Home directly; logged-out → Login; non-Sunner → Access denied.

### Test Categories

| Category | Applicable? | Key Scenarios |
|----------|-------------|---------------|
| UI ↔ Logic | Yes | CTA tap → VM intent → state update → UI recomposition; dropdown selection → locale change |
| Service ↔ Service | Yes | `SessionRepository` ↔ Supabase SDK; `VerifySunnerMembershipUseCase` ↔ Postgrest |
| App ↔ External API | Yes | Real local Supabase stack (no Supabase mocks); Credential Manager faked at the boundary only |
| App ↔ Data Layer | Yes | DataStore round-trip; `users` row read; RLS denial for non-self rows |
| Cross-platform | No | Android-only |

### Test Environment

- **Environment type**: Local emulator (Pixel 6 / API 34) for instrumented tests; local
  `supabase start` Docker stack for backend.
- **Test data strategy**: SQL fixtures applied via migration in `supabase/seed.sql` —
  one Sunner (`alice@sun-asterisk.com`) and one non-Sunner.
- **Isolation approach**: Each integration test resets the `users` table in a `@Before`
  hook; auth sessions cleared via `signOut()` in `@After`.

### Mocking Strategy

| Dependency Type | Strategy | Rationale |
|-----------------|----------|-----------|
| Supabase Auth + Postgrest | Real (local stack) | Spec mandates RLS testing; mock-only would let policy bugs through |
| Credential Manager / Google | Fake at the boundary | Real Google sign-in in CI is impractical; fake returns a known ID token signed by a test JWK |
| Preferences DataStore | Real | Lightweight; no value in mocking |
| `SessionRepository` (in UI tests) | Test double exposing a `MutableStateFlow<AuthState>` | Lets UI tests drive state transitions deterministically |

### Test Scenarios Outline

1. **Happy Path**
   - [ ] Cold start unauthenticated → Login renders → tap CTA with Sunner account → Home.
   - [ ] Cold start with valid session → Home directly (no Login flash).
   - [ ] Switch language to EN → description + copyright re-render in EN.

2. **Error Handling**
   - [ ] OAuth canceled → silent return to Login.
   - [ ] Network unavailable at OAuth start → error message; remain on Login.
   - [ ] Non-Sunner Google account → sign out + Access denied screen.
   - [ ] Disabled/locked Google account → error message; remain on Login.
   - [ ] Token expired between cold starts → Login renders (spec FR-007).
   - [ ] After explicit logout, next cold start → Login renders (spec FR-008,
         US2 scenario 3).
   - [ ] Google Play Services unavailable / outdated → CTA disabled with actionable
         error.
   - [ ] Keyboard activation: Tab to CTA, press Enter → identical OAuth flow as a
         tap (spec § Behavioral Accessibility).

3. **Edge Cases**
   - [ ] Double-tap CTA → exactly one auth request issued.
   - [ ] App backgrounded mid-OAuth → resume completes via deep-link.
   - [ ] Persisted language code no longer supported (e.g., legacy "FR") → fall back VN.
   - [ ] Offline at cold start with cached session → render Home; first protected
         request fails gracefully.
   - [ ] RLS policy test: non-Sunner JWT cannot select another user's row.

### Tooling & Framework

- **Test framework**: JUnit 4 (unit), AndroidX Compose Test (UI), Espresso (where Compose
  test is insufficient).
- **Supporting tools**: Turbine (Flow assertions), MockK (mocks), Hilt test fixtures.
- **CI integration**: GitHub Actions (or similar) — `supabase start` step before
  `connectedDebugAndroidTest`; cached emulator image; failure surfaces in PR check.

### Coverage Goals

| Area | Target | Priority |
|------|--------|----------|
| Auth flow (US1, US2) | ≥ 90% line + 100% branch on the state machine | High |
| Language switcher (US3) | ≥ 85% | Medium |
| RLS policy denial paths | 100% (every new table-level policy has a denial test) | High (Constitution) |
| Telemetry / logging | Spot tests for log scrubbing (SC-005) | High |

---

## Threat Model (per Constitution Security Requirements)

This feature handles authentication, so a threat model is mandatory.

| Threat | Surface | Mitigation |
|--------|---------|------------|
| Token theft via rooted device | On-device storage | Supabase SDK uses Android Keystore; `allowBackup` excludes auth files (`backup_rules.xml`); release build ProGuard-stripped of debug logging |
| MITM at Supabase endpoint | Network | TLS-only; certificate pinning in release `network_security_config.xml` |
| Replay of stolen ID token | Auth | Supabase verifies Google JWT signature + `aud` + `exp`; ID tokens are short-lived; refresh handled by SDK |
| Privilege escalation via missing RLS | Database | RLS enabled on every table accessed from client; policy tests in CI; reviewer checklist |
| Phishing app intercepts OAuth | App registration | Use Credential Manager + Google ID provider (no browser redirect); SHA-256 of release signing cert registered with Google Cloud OAuth client |
| Logging-based PII leakage | App logs | Custom log tree scrubs token-like patterns; release builds strip verbose logs; CI grep on artifacts (SC-005) |
| Account takeover via session fixation | Auth | Each successful sign-in issues a fresh Supabase session; refresh tokens rotated by SDK |
| Supabase service-role key exposed | Build artifact | `service_role` key NEVER in app or CI artifact destined for the device; only in server-side admin tools |

---

## Dependencies & Prerequisites

### Required Before Start

- [x] `constitution.md` reviewed and understood (v1.0.0).
- [x] `spec.md` approved by stakeholders — Q1–Q3 resolved 2026-05-06 and codified into
      spec § Localized Copy + FR-004 + FR-015.
- [ ] Supabase project provisioned with Google OAuth provider enabled.
- [ ] Google Cloud OAuth client configured with Android SHA-256 cert hash.
- [ ] Local Supabase migration for `users` table + RLS reviewed (see Phase 1).
- [ ] CI runner has Docker available for `supabase start`.

### External Dependencies

- Supabase project (URL + anon key).
- Google Cloud project (OAuth 2.0 Web client ID for ID-token flow on Android).
- Sunner whitelist source-of-truth (whoever provisions `public.users` rows — out of scope
  for this feature; assumed handled separately).

---

## Next Steps

After plan approval:

1. **Run** `/momorph.tasks` to generate the task breakdown from this plan.
2. **Resolve infra/security questions** Q4 (CI Supabase strategy), Q5 (cert-pinning),
   Q6 (JVM target), Q7 (OAuth SHA-256 registration). Q5/Q7 must be resolved before
   the relevant phase; Q4/Q6 are mechanical and can be deferred to Phase 1 prereqs.
3. **Verify** CI has Docker / Supabase access before Phase 2 starts.
4. **Begin** implementation TDD-first per Phase 1 → Phase 6 ordering.

---

## Open Questions

- [x] ~~**Q1 (Copy)**: Error message text for the failure modes.~~ **Resolved 2026-05-06**
      — locked in spec § Localized Copy; six string keys defined in VN/EN/JA.
- [x] ~~**Q2 (Copy)**: CTA label localization.~~ **Resolved 2026-05-06** — English-only
      on all locales (spec FR-015).
- [x] ~~**Q3 (UX)**: Loading-state label change vs indicator-only.~~ **Resolved
      2026-05-06** — indicator only; label "LOGIN With Google" unchanged (spec FR-004).
- [ ] **Q4 (Infra)**: Confirm whether CI runs `supabase start` (preferred) or hits a
      hosted ephemeral Supabase project. Affects integration-test setup time.
- [ ] **Q5 (Security)**: Cert-pinning strategy. Options: (a) SPKI-pin the current
      LetsEncrypt intermediate(s) with backup pins (resilient but requires periodic
      review); (b) skip pinning, rely on system trust + HSTS (simpler, fits most
      threat models); (c) pin a custom CA if Sun* operates one. Decision needed before
      release-build cutoff. Plan currently writes (a) as the placeholder approach.
- [ ] **Q6 (Compat)**: Verify supabase-kt 3.x runs on Java 11 source/target as currently
      configured in `app/build.gradle.kts`. If JVM 17 is required, bump
      `compileOptions` and `kotlinOptions.jvmTarget` in Phase 1 (mechanical change but
      may affect AGP / lint config).
- [ ] **Q7 (Compat)**: Confirm Credential Manager `googleid` library works against the
      project's `compileSdk = 36` and Play Services version policy. The library is
      stable on API 31+ but SHA-256 signing-cert registration with Google Cloud must
      happen before Phase 2 integration tests can run end-to-end.
- [ ] **Q8 (Scope)**: Should the app expose system per-app-language support (Android 13+
      Settings → Apps → SAA → Language)? Adding `locales_config.xml` enables this and
      uses `AppCompatDelegate.setApplicationLocales`, which recreates the Activity on
      change. Plan currently says NO (in-memory switcher only, no Settings UI
      integration) to honor SC-004 ("within 1 frame"). Decision can be deferred — adding
      locales_config later is non-breaking.

---

## Notes

- Plan deliberately picks **Credential Manager + Google ID provider + Supabase
  `signInWithIdToken`** over the older browser-redirect / `signInWithOAuth(provider =
  Google)` path. The ID-token flow is the modern Android pattern and avoids the
  custom-tab handoff entirely, simplifying state preservation across the auth flow
  (Edge Case: app-backgrounded-mid-OAuth becomes trivial).
- `SCREENFLOW.md` is still missing; this plan assumes the navigation targets named in
  `spec.md` (`OuH1BUTYT0` Home, `k-7zJk2B7s` Access denied, `uUvW6Qm1ve` Language
  dropdown). When `/momorph.screenflow` is rerun and produces `SCREENFLOW.md`, update
  the Routes constants if any IDs differ.
- Language dropdown frame `uUvW6Qm1ve` is referenced as a separate MoMorph screen but
  the plan implements the dropdown inline as a Material 3 `ExposedDropdownMenuBox` —
  no separate route. The standalone frame is treated as design-asset reference only.
- No design-style.md is produced upstream by intent (per spec scope). Visual specs are
  pulled at implementation time via `mcp__momorph__query_section` / `get_node` for the
  six Node IDs listed in `spec.md`.
