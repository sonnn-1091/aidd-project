# Feature Specification: Login

**Frame ID**: `8HGlvYGJWq`
**Frame Name**: `[iOS] Login`
**File Key**: `9ypp4enmFmdK3YAFJLIu6C`
**Frame Revision**: `dbf4a0b8e338a97a500fd60ebbf3dbb1`
**Created**: 2026-05-06
**Status**: Draft

---

## Overview

The Login screen is the unauthenticated entry point to the SAA 2025 (Sun* Annual Awards 2025)
application. Authentication is **Google OAuth-only** — there is no email/password,
biometric, or anonymous-access path. Sun* employees ("Sunners") tap a single CTA to start the
Google consent flow; on success they land on the Home screen. The screen also exposes a
language switcher (VN default; VN/EN supported — JA was removed by Language Dropdown spec
`uUvW6Qm1ve` § Resolved Q1) so users can change UI language before authenticating, and shows
static branding (logo, tagline, description, copyright). The language switcher composable
now lives in the screen-neutral package `com.example.aiddproject.core.locale.ui.LanguageSelector`
(moved during Language Dropdown spec `uUvW6Qm1ve` Phase 2; previously
`auth.login.ui.components.LanguageSelector`).

**Target users**: Sun* employees with a registered Google work account.
**Implementation platform**: **Android only** (Kotlin + Jetpack Compose + Material 3) per
constitution v1.0.0. The MoMorph frame is labeled "[iOS]" because that is the source design
language; it is translated to Material 3 on Android — see Constitution Alignment below.
**Business context**: Single sign-on for the annual awards platform; restricting auth to the
corporate Google identity is the access-control mechanism — no in-app account creation.

---

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Sign in with Google (Priority: P1)

A first-time or logged-out Sunner taps the "LOGIN With Google" CTA, completes Google's OAuth
flow, and is taken to the Home dashboard.

**Why this priority**: This is the *only* way into the app. Without it, no other feature is
reachable. MVP-blocking.

**Independent Test**: Launch the app while signed out, tap the Google CTA, complete the OAuth
consent with a valid Sun* Google account, and verify navigation to Home.

**Acceptance Scenarios**:

1. **Given** the user is not authenticated and on the Login screen,
   **When** they tap "LOGIN With Google" and complete OAuth with a valid Sun* Google account,
   **Then** the system creates an authenticated session and navigates to the Home screen.
2. **Given** OAuth is in progress after a tap on the CTA,
   **When** the user taps the CTA again before the first request resolves,
   **Then** only one authentication request is sent (double-tap is suppressed).
3. **Given** the user is on the Login screen,
   **When** they tap the CTA,
   **Then** the button enters a loading/disabled state until the OAuth flow resolves.
4. **Given** the user cancels the Google consent screen *or* a network error occurs,
   **When** the OAuth flow returns failure,
   **Then** an error message is displayed and the user remains on the Login screen.
5. **Given** the Google account is disabled / deleted / locked at Google,
   **When** the user attempts OAuth with that account,
   **Then** sign-in is rejected with an account-status error message and the user remains on
   the Login screen.
6. **Given** the Google account exists and authenticates successfully but is NOT a registered
   Sunner (no matching `users` row),
   **When** the post-OAuth profile fetch returns empty / forbidden,
   **Then** the system signs the user out and navigates to `[iOS] Access denied`
   (`k-7zJk2B7s`).

### User Story 2 - Skip login for authenticated users (Priority: P1)

A user who previously signed in and still holds a valid session token is taken straight to
Home without seeing the Login screen.

**Why this priority**: Pairs with US1 to deliver the expected "stay signed in" UX. Without it,
every cold start would force re-auth, violating user expectations.

**Independent Test**: Sign in successfully, fully close the app, reopen it, and verify the app
opens on Home (not Login).

**Acceptance Scenarios**:

1. **Given** the user signed in previously and the access token is still valid,
   **When** the app launches,
   **Then** the user is routed directly to Home without rendering the Login screen.
2. **Given** the user signed in previously but the access token has expired,
   **When** the app launches,
   **Then** the Login screen is shown; on successful re-auth the user reaches Home.
3. **Given** the user has explicitly logged out,
   **When** the app launches,
   **Then** the Login screen is shown regardless of any remaining cached identity.

### User Story 3 - Switch display language before signing in (Priority: P2)

A user who prefers EN over the default VN can change language from the Login screen,
and all localizable text on the screen updates immediately. (JA was removed by Language
Dropdown spec `uUvW6Qm1ve` § Resolved Q1; see that spec for the current contract.)

**Why this priority**: Important for international employees but not blocking — the app still
works in VN. Should-have, not must-have.

**Independent Test**: On the Login screen, open the language dropdown, select EN, and verify
the description text and copyright text re-render in English without a screen reload.

**Acceptance Scenarios**:

1. **Given** the user is on the Login screen on first launch,
   **When** the screen renders,
   **Then** the language selector shows "VN" with the Vietnam flag pre-selected.
2. **Given** the language dropdown is open,
   **When** the user selects "EN",
   **Then** the language label updates to "EN", the flag updates accordingly, and the
   localizable text on the screen (description and copyright) re-renders in English. The
   "LOGIN With Google" CTA label is English-only on all locales (FR-015) and does not
   change.
3. **Given** a language has been changed from VN to EN,
   **When** the app is closed and reopened (still unauthenticated),
   **Then** the previously selected language is restored.
4. **Given** the dropdown is open,
   **When** the user views the options,
   **Then** only the supported languages (VN, EN) are listed. JA was removed by Language
   Dropdown spec `uUvW6Qm1ve` § Resolved Q1.

### User Story 4 - Understand product context before signing in (Priority: P3)

The screen displays brand identity (logo + "ROOT FURTHER" tagline), a one-line invitation
description, and a copyright notice so the user knows what app they are about to sign into.

**Why this priority**: Informational only; does not gate functionality.

**Independent Test**: Open the app while signed out and verify all four static elements
(logo, "ROOT FURTHER", description, copyright) are visible.

**Acceptance Scenarios**:

1. **Given** the Login screen is rendered,
   **When** the user looks at the screen,
   **Then** the Sun* Annual Awards logo, "ROOT FURTHER" tagline, the localized description
   ("Bắt đầu hành trình của bạn cùng SAA 2025. Đăng nhập để khám phá!" / "Start your journey
   with SAA 2025. Log in to explore!"), and a copyright line ("Bản quyền thuộc về Sun* © 2025")
   are all visible.

### Edge Cases

- **No internet at OAuth start** → display network-error message; remain on Login.
- **OAuth canceled** by user (close consent sheet) → silent return to Login, no error toast.
- **Token revoked server-side** while app is in foreground → next protected request triggers
  re-auth; user is bounced back to Login with a session-expired message.
- **Multiple Google accounts on device** → user picks one in the system chooser; the chosen
  account's identity is what gets authenticated.
- **Clock skew on device** → if device time is significantly off, OAuth may fail signature
  validation; surface a generic error.
- **Account exists in Google but is not a registered Sun* user** → API rejects post-OAuth
  identity exchange; navigate to `[iOS] Access denied` (`k-7zJk2B7s`).
- **App backgrounded mid-OAuth, then resumed** → the deep-link / universal-link callback
  completes the flow on resume; UI does NOT lose the in-flight `loading` state.
- **OAuth authorization code expires** (e.g., user takes the consent screen too long, or
  device sleeps mid-flow) → callback returns an expired-code error; show a recoverable error
  ("Please try again") and return CTA to `idle`.
- **Offline at app launch with cached session** → proceed to Home; the first authenticated
  request after Home renders will trigger refresh, and a refresh failure surfaces a
  reconnect/retry banner (handled by the global network layer, not this screen).
- **Language code persisted but no longer supported** → fall back to VN.

---

## UI/UX Requirements *(from Figma)*

### Screen Components

| Node ID       | Name                              | Type      | Interactions                                                                                          |
|---------------|-----------------------------------|-----------|-------------------------------------------------------------------------------------------------------|
| `6885:8977`   | Sun* Annual Awards logo           | Image     | None (static)                                                                                         |
| `6885:8967`   | "ROOT FURTHER" tagline            | Label     | None (static, brand text)                                                                             |
| `6885:8968`   | Description text                  | Label     | None (static, localizable; updates on language change)                                                |
| `6885:8976`   | Language selector                 | Dropdown  | Tap → opens VN/EN/JA list; tap option → updates label + flag, re-renders localizable text            |
| `6885:8969`   | "LOGIN With Google" CTA           | Button    | Tap → starts Google OAuth; success → navigate to Home; failure → error message; loading state shown |
| `6885:8971`   | Copyright text                    | Label     | None (static, localizable)                                                                            |

### Component Behavior — detailed

#### `6885:8969` — Login with Google button (CTA)

- **Interaction**: `on_click` → initiates Google OAuth consent flow.
- **Navigation**: On success → Home screen (`OuH1BUTYT0` — `[iOS] Home`). On account-not-
  authorized → `[iOS] Access denied` (`k-7zJk2B7s`).
- **Validation**: Always enabled on render; no pre-condition validation. Disabled only while a
  request is in-flight.
- **State transitions**:
  - `idle` → `loading` (on tap; double-tap suppressed)
  - `loading` → `idle` + navigate (on success)
  - `loading` → `error` (on failure; error message visible; button returns to `idle`)
  - `loading` → `idle` (on user-canceled OAuth; no error toast)

#### `6885:8976` — Language selector

- **Interaction**: `on_click` → opens dropdown (`[iOS] Language dropdown` (`uUvW6Qm1ve`));
  selecting an option closes dropdown and emits language-change event.
- **Navigation**: None (in-place UI update).
- **Validation**:
  - `required`: false
  - `defaultValue`: `VN`
  - `format`: language code, one of {`VN`, `EN`, `JA`}.
  - On invalid value (e.g., persisted code no longer supported) fall back to `VN`.
- **State transitions**: `closed` ↔ `open`; selection mutates the global `language` state.

(Static elements `6885:8977`, `6885:8967`, `6885:8968`, `6885:8971` have no interaction states.)

### Navigation Flow

- **Entry**: App cold start while unauthenticated, or after explicit logout, or after token
  expiry.
- **Exit (success)**: → Home screen (`OuH1BUTYT0` — `[iOS] Home`).
- **Exit (account not authorized)**: → `[iOS] Access denied` (`k-7zJk2B7s`).
- **Exit (other failures)**: Stay on Login; surface error message.
- **Bypass**: When valid session exists at app launch → navigate directly to Home, skipping
  Login render.

### Localized Copy

The complete set of localizable strings used on the Login screen and its error states.
String keys are the names that will appear in `res/values/strings.xml` and per-locale
overrides.

**Brand-fixed (NOT localized)** — same on every locale:

| Key | Value |
|---|---|
| `brand_root_further` | `ROOT FURTHER` |
| `brand_copyright` | `Sun*` (referenced inside the localized copyright line below) |
| `login_cta_label` | `LOGIN With Google` (FR-015 — English-only on all locales) |

**Localized — Description and copyright**:

| Key | VN (default) | EN | JA |
|---|---|---|---|
| `login_description` | `Bắt đầu hành trình của bạn cùng SAA 2025. Đăng nhập để khám phá!` | `Start your journey with SAA 2025. Log in to explore!` | `SAA 2025で旅を始めよう。サインインして探検しましょう。` |
| `login_copyright` | `Bản quyền thuộc về Sun* © 2025` | `Copyright belongs to Sun* © 2025` | `Copyright belongs to Sun* © 2025` |

**Localized — Error messages** (mapped to FR-005 failure modes):

| Key | Trigger | VN | EN | JA |
|---|---|---|---|---|
| `error_oauth_network` | No internet at OAuth start; network error | `Không thể kết nối. Vui lòng kiểm tra Internet và thử lại.` | `Couldn't connect. Please check your internet and try again.` | `接続できませんでした。インターネット接続をご確認のうえ、もう一度お試しください。` |
| `error_oauth_account_disabled` | Google account disabled / locked / deleted | `Tài khoản Google này không thể đăng nhập. Vui lòng liên hệ quản trị viên.` | `This Google account can't be used to sign in. Please contact your admin.` | `このGoogleアカウントではサインインできません。管理者にお問い合わせください。` |
| `error_oauth_code_expired` | OAuth authorization code expired (long delay between consent and callback) | `Đăng nhập mất quá nhiều thời gian. Vui lòng thử lại.` | `Sign-in took too long. Please try again.` | `サインインに時間がかかりすぎました。もう一度お試しください。` |
| `error_oauth_play_services` | Google Play Services unavailable / outdated | `Hãy cập nhật Google Play Services để đăng nhập.` | `Update Google Play Services to sign in.` | `サインインするにはGoogle Playサービスを更新してください。` |
| `error_oauth_session_expired` | Token revoked / expired during foreground use, bouncing user back to Login | `Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.` | `Your session has expired. Please sign in again.` | `セッションの有効期限が切れました。もう一度サインインしてください。` |
| `error_oauth_generic` | Any unclassified failure | `Đã có lỗi xảy ra. Vui lòng thử lại.` | `Something went wrong. Please try again.` | `問題が発生しました。もう一度お試しください。` |

**No copy required**:
- **OAuth canceled by user** (closed consent sheet) — silent return to Login per spec
  edge cases; no message is shown.
- **Account-not-authorized (not a Sunner)** — handled by navigation to `[iOS] Access
  denied` (`k-7zJk2B7s`); copy lives on that screen, not Login.

**Implementation note**: error messages are surfaced via Material 3 `Snackbar` at the
bottom of the screen, dismissible by tap or after 6 seconds. Only one snackbar visible at
a time; new errors replace old ones.

### Behavioral Accessibility

The visual side of the design (colors, spacing, typography, asset variants) is **out of scope
for this spec** and is fetched at implementation time via MoMorph `query_section` / `get_node`
for the listed Node IDs. Behavioral accessibility, however, IS in scope:

- **Focus order**: language selector → description text → "LOGIN With Google" CTA. Logo and
  copyright are decorative for assistive tech (skip in focus traversal).
- **CTA accessibility label**: announce as "Sign in with Google, button" (localized);
  state changes (loading) MUST be announced via an accessibility live region — e.g.,
  "Signing in" while the request is in flight.
- **Language selector accessibility**: announce as "Language, [full localized language name],
  dropdown" — using the full name (e.g., "Tiếng Việt", "English", "日本語"), not the two-letter
  code shown in the visible label; the dropdown's open/closed state must be exposed to
  assistive tech; selection changes MUST be announced.
- **Description and copyright**: readable as static text; no role overrides.
- **Keyboard / external-control navigation**: every interactive control must be reachable
  without a touch gesture; tapping/activating via keyboard equivalent (Enter/Space) MUST
  trigger the same action as tap.
- **Internationalization**: VN, EN, JA. Localized text MUST come from a single resource file
  per language; no hard-coded UI strings except brand-fixed text ("ROOT FURTHER", "Sun*").
  Right-to-left layout is not in scope for the supported languages.

---

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST authenticate users exclusively via Google OAuth; no other auth method
  may be exposed on this screen.
- **FR-002**: System MUST navigate authenticated users from Login to Home immediately on
  successful OAuth completion.
- **FR-003**: System MUST suppress duplicate authentication requests when the CTA is
  double-tapped while a request is in flight.
- **FR-004**: System MUST display a loading state on the CTA while OAuth is in progress —
  specifically a circular progress indicator inside the button. The button label "LOGIN
  With Google" MUST remain unchanged during the loading state. The button MUST be visually
  and interactively disabled while loading.
- **FR-005**: System MUST display a user-visible error and keep the user on Login when OAuth
  fails (network error, canceled consent, account disabled). On account-not-authorized, system
  MUST navigate to `[iOS] Access denied`.
- **FR-006**: On app launch, system MUST detect a valid existing session and route directly to
  Home, bypassing the Login screen.
- **FR-007**: On app launch with an expired or revoked session, system MUST render the Login
  screen.
- **FR-008**: After explicit logout, system MUST render the Login screen on next launch.
- **FR-009**: Users MUST be able to switch the display language to VN, EN, or JA from the
  Login screen.
- **FR-010**: System MUST default the language to VN on first launch.
- **FR-011**: System MUST persist the chosen language across app restarts (pre- and
  post-authentication).
- **FR-012**: System MUST re-render all localizable on-screen text immediately after a language
  change (no full reload).
- **FR-013**: System MUST restrict the language dropdown to the supported set {VN, EN, JA}.
- **FR-014**: System MUST display the static branding elements (logo, "ROOT FURTHER", localized
  description, copyright) on every render of the Login screen.
- **FR-015**: The Login CTA label MUST be the literal string "LOGIN With Google" on all
  supported locales. It MUST NOT be localized into VN or JA. (Brand-fixed string, treated
  the same as "ROOT FURTHER" and "Sun*".)

### Technical Requirements

- **TR-001 (Auth, Constitution Principle II)**: Authentication MUST go through Supabase Auth
  using Google as the OAuth provider. The app uses the anon/publishable Supabase key only —
  service role key MUST NOT ship in the client.
- **TR-002 (Security, Constitution Principle IV)**: OAuth tokens MUST be persisted via the
  Supabase SDK's secure storage backed by Android Keystore (and/or
  EncryptedSharedPreferences). No tokens, ID tokens, refresh tokens, or PII may be written to
  logs or to plain SharedPreferences, file caches, or any non-encrypted on-device location.
- **TR-003 (Security, Constitution Principle IV)**: Authorization for any post-login data
  access MUST be enforced by Postgres Row-Level Security on the Supabase backend. The client
  is not the source of truth for who can see what.
- **TR-004 (Security)**: TLS-only network calls; certificate pinning enabled in release builds
  for Supabase endpoints.
- **TR-005 (Performance)**: Auto-redirect decision (valid session → Home) MUST resolve before
  Login UI is rendered to avoid a flash of the Login screen for already-signed-in users.
- **TR-006 (Reliability)**: CTA tap-to-OAuth-start latency target: < 300 ms p95 on warm start.
- **TR-007 (i18n)**: All user-visible text on this screen MUST come from a localizable resource;
  no inline string literals in UI code except brand-fixed strings.

### Key Entities

- **Session**: opaque record managed by Supabase Auth — fields used by the client are
  `access_token`, `refresh_token`, `expires_at`, `user.id`. Persisted by the SDK; never
  inspected/modified manually.
- **User identity** (from Google via Supabase): `email`, `id`, optional `name`, `avatar_url`.
  The Sun*-side authorization (which emails count as Sunners) is enforced server-side, not
  derived in the client.
- **Language preference**: stored locally as one of `VN | EN | JA`. Default `VN`.

---

## API Dependencies

> All endpoints below are *predicted* based on observed component behavior. Concrete contracts
> are deferred to `/momorph.apispecs`. Because this project standardizes on Supabase, most of
> these are SDK calls rather than hand-rolled REST endpoints.

| Endpoint / SDK call                                  | Method | Purpose                                       | Triggered by                  | Status    |
|------------------------------------------------------|--------|-----------------------------------------------|-------------------------------|-----------|
| `auth.signInWithOAuth({provider: 'google'})`         | n/a    | Start Google OAuth flow via Supabase Auth     | Login CTA tap (US1)           | Predicted |
| `auth.exchangeCodeForSession(code)`                  | n/a    | Exchange OAuth callback code for session      | OAuth redirect resolution     | Predicted |
| `auth.getSession()`                                  | n/a    | Read persisted session at app launch          | App cold start (US2)          | Predicted |
| `auth.refreshSession()`                              | n/a    | Refresh access token before expiry            | Token near-expiry (US2)       | Predicted |
| `auth.signOut()`                                     | n/a    | Clear session                                 | Logout flow (out of this spec)| Predicted |
| `GET /rest/v1/users?select=...&id=eq.{id}` (RLS)     | GET    | Load Sunner profile post-login                | After successful auth (US1)   | Predicted |

The "Sunner whitelist" / membership check (i.e., is this Google email a registered Sun*
employee?) is expected to be enforced on the database side via RLS plus a `users` table
keyed on `auth.users.id`. If an unregistered Google account authenticates, the post-login
profile fetch returns empty / forbidden, which the client treats as account-not-authorized
(FR-005) and navigates to `[iOS] Access denied`.

---

## State Management

- **Global / app-scoped**:
  - `AuthState`: sealed type — `Loading | Unauthenticated | Authenticated(user) | Error(reason)`.
    Single source of truth for navigation gating.
  - `LanguagePreference`: persisted in Jetpack DataStore (Preferences DataStore); read at
    launch, written on change. Drives the locale provider for the whole app.
- **Screen-local**:
  - `cta.loading`: boolean — true while OAuth is in flight; gates the disabled state and
    double-tap suppression.
  - `cta.error`: optional error reason — populated on OAuth failure; surfaced as a snackbar /
    inline message; cleared on next CTA tap.
  - `language.dropdownOpen`: boolean — local UI state for the dropdown.
- **Cache / invalidation**: session is owned by the Supabase SDK; do not maintain a parallel
  cache. Language preference is a pure local setting and does not need server sync.
- **Optimistic updates**: not applicable on this screen.

---

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: ≥ 99% of OAuth attempts on healthy networks complete (success or
  user-canceled) within 5 seconds end-to-end.
- **SC-002**: ≥ 99% of authenticated cold starts route to Home without rendering the Login
  screen (no Login flash).
- **SC-003**: 0 instances of duplicate auth requests in production telemetry from
  double-tap on CTA.
- **SC-004**: Language change reflects in all on-screen localizable text within 1 frame
  (visually instant) after selection.
- **SC-005**: 0 instances of access tokens, refresh tokens, or full OAuth payloads appearing
  in production logs (validated by log-scrubbing tests).

---

## Out of Scope

- Logout flow and account-management UI (separate screens).
- Email/password, magic-link, biometric, or anonymous authentication paths.
- Onboarding or first-time-user guidance after login.
- "Forgot account" / "Switch account" UX.
- Server-side provisioning of new Sunner records (assumed pre-existing).
- Visual specs (colors, sizes, fonts, asset variants, animations) — fetched at implementation
  time via MoMorph `query_section` / `get_node` / `list_media_nodes`.

---

## Dependencies

- [x] Constitution document exists (`.momorph/constitution.md`) — v1.0.0
- [ ] API specifications available (`.momorph/API.yml`) — to be produced via `/momorph.apispecs`
- [ ] Database design completed (`.momorph/database.sql`) — to be produced via `/momorph.database`
- [ ] Screen flow documented (`.momorph/SCREENFLOW.md`) — to be produced via
      `/momorph.screenflow` (run blocked on prior write-permission denial; rerun pending)
- [ ] Home screen spec (`OuH1BUTYT0` — `[iOS] Home`) — required to wire the success
      navigation target
- [ ] Access denied screen spec (`k-7zJk2B7s` — `[iOS] Access denied`) — required for
      authorization-failure navigation
- [ ] Language dropdown spec (`uUvW6Qm1ve` — `[iOS] Language dropdown`) — referenced from
      `6885:8976`
- [ ] Supabase project provisioned with Google OAuth provider configured

---

## Constitution Alignment

Verified against `.momorph/constitution.md` v1.0.0:

- **I. Clean Code & Source Organization** — feature folder `auth/login/` planned with
  `ui/` (composables/views, state) + `data/` (auth repo) + `domain/` (auth use cases).
- **II. Tech Stack Best Practices** — uses Supabase Auth idiomatically (no custom credential
  handling), anon key only.
- **III. Material Design 3 (Android)** — the implementation target is **Android only**
  (decision recorded 2026-05-06). The MoMorph design frame is labeled "[iOS]" because that
  is the source design language, but it will be translated to Material 3 components on
  Android: the Login CTA → Material `Button`; the language selector →
  `ExposedDropdownMenuBox`; layout uses Compose with M3 theme tokens; no iOS controls or
  human-interface idioms (e.g., bottom sheets shaped as iOS sheets) are imported. Where this
  spec previously hedged "platform-neutral", read it as Android-specific.
- **IV. OWASP Secure Coding** — TR-001..TR-004 cover token storage, key management, TLS, and
  RLS-as-authoritative-authorization.
- **V. Test-Driven Development** — every FR/SC above is independently testable; failing
  tests for `AuthState` transitions, language persistence, and double-tap suppression must be
  written before implementation per the constitution gate.

---

## Notes

- Frame revision pinned: `dbf4a0b8e338a97a500fd60ebbf3dbb1`. If the design revision changes,
  re-run `/momorph.specify` and diff this spec.
- Test coverage: the 20 test cases under `frame.id=49530` in MoMorph
  (`TC_LOGIN_ACC_001..003`, `TC_LOGIN_GUI_001..002`, `TC_LOGIN_FUN_001..015`) map onto the
  acceptance scenarios above; mapping table to be produced by `/momorph.createtestcases`.
- Resolved (2026-05-06): error message copy locked in under § Localized Copy.
- Resolved (2026-05-06): language preference persists in Jetpack DataStore (Preferences
  DataStore); no other storage target is in scope.
- Resolved (2026-05-06): Login CTA label is English-only on all locales — see FR-015.
- Resolved (2026-05-06): loading state shows a spinner indicator only; label unchanged —
  see FR-004.
