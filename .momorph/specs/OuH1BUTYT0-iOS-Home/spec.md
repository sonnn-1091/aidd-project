# Feature Specification: Home

**Frame ID**: `OuH1BUTYT0`
**Frame Name**: `[iOS] Home`
**File Key**: `9ypp4enmFmdK3YAFJLIu6C`
**Frame Revision**: `3c9059f3da88539320cb62e39aefcf38`
**Created**: 2026-05-07
**Status**: Draft

---

## Overview

The Home screen is the authenticated hub of the SAA 2025 (Sun* Annual Awards 2025)
application — the destination after a successful login (US1 of `[iOS] Login`,
`8HGlvYGJWq`) and the start tab of the bottom navigation bar (`SAA 2025`). It sits
on the same keyvisual background as Login and surfaces every other major area of
the product: the awards catalogue, the Kudos community feature, the user's
notifications, and a branded countdown to the awards ceremony on **26 December
2025**.

**Target users**: authenticated Sunners with a valid Supabase session.
**Implementation platform**: **Android only** (Kotlin + Jetpack Compose +
Material 3) per constitution v1.0.0. The MoMorph frame is labeled "[iOS]" because
that is the source design language; it is translated to Material 3 on Android — see
Constitution Alignment below.
**Business context**: Home is the engagement surface of SAA 2025. It anchors brand
identity ("ROOT FURTHER" theme), communicates urgency via the countdown, drives
content discovery (awards carousel) and community participation (Kudos), and is
the recurring entry point users return to between flows.

---

## User Scenarios & Testing *(mandatory)*

### User Story 1 - View the SAA 2025 hub on Home (Priority: P1) 🎯 MVP

A signed-in Sunner opens the app and sees the SAA 2025 hub: brand logo, the
"ROOT FURTHER" tagline, a real-time countdown to the awards date, the theme
paragraph, and the awards section. This is the **entry point** to every other
authenticated screen.

**Why this priority**: This is what the user lands on right after sign-in
(directly from Login per US1 of `8HGlvYGJWq`) and on every cold start with a
valid session (US2 of `8HGlvYGJWq`). Without it the rest of the product is
unreachable — MVP-blocking.

**Independent Test**: Authenticate with a valid Sunner account → land on Home →
verify the header (logo + language + search + bell), hero (ROOT FURTHER logo +
countdown + ABOUT buttons), theme paragraph, awards section header, FAB, and
bottom NavBar with the SAA 2025 tab marked active.

**Acceptance Scenarios**:

1. **Given** the user has just completed Login successfully,
   **When** the navigation transition resolves,
   **Then** the Home screen renders and Login is removed from the back stack
   (TC_HOME_ACC_005).
2. **Given** the user is on Home and the device clock is before 26 Dec 2025,
   **When** the screen has rendered for at least one second,
   **Then** the countdown DAYS / HOURS / MINUTES values update every second and
   the "Coming soon" label is visible (TC_HOME_FUN_001, TC_HOME_FUN_002).
3. **Given** the device clock is on or after 26 Dec 2025,
   **When** Home renders,
   **Then** the "Coming soon" label is hidden and the countdown displays a
   completed/zeroed state (TC_HOME_FUN_002).
4. **Given** Home is rendered,
   **When** the user looks at the screen,
   **Then** the header (logo + language + search + bell), hero block, theme
   paragraph, awards section header, FAB, and bottom NavBar (SAA 2025 active)
   are all visible (TC_HOME_GUI_001).

---

### User Story 2 - Browse and open awards from the carousel (Priority: P1)

A Sunner browses the list of award categories (e.g., "Top Talent Award") on Home
and taps "Chi tiết" on a card to open that award's detail page.

**Why this priority**: Awards discovery is the primary content hook of SAA 2025;
without it Home is a static brand page. P1 because every award detail screen
funnels through this carousel.

**Independent Test**: With at least one award returned by the awards API, scroll
the carousel horizontally and tap "Chi tiết" on a card — Award Detail for that
award opens.

**Acceptance Scenarios**:

1. **Given** the awards API has returned ≥ 1 card,
   **When** the user drags the carousel left or right,
   **Then** the cards scroll horizontally and previously off-screen cards become
   fully visible (TC_HOME_FUN_005).
2. **Given** the awards API is in flight,
   **When** the user is on Home,
   **Then** the awards section displays a loading indicator (TC_HOME_GUI_002).
3. **Given** the awards API has returned an empty array,
   **When** Home renders the awards section,
   **Then** an empty-state message is shown and no card items render
   (TC_HOME_GUI_003).
4. **Given** the awards API call has failed,
   **When** the awards section renders,
   **Then** an error-state UI with a Retry button is shown (TC_HOME_GUI_004).
5. **Given** the awards API returned an error and the user taps Retry,
   **When** the retry call succeeds,
   **Then** the error UI is replaced with the populated card list
   (TC_HOME_FUN_003).
6. **Given** the awards section has populated cards,
   **When** the user taps "Chi tiết" on a specific card,
   **Then** the system navigates to that award's Detail screen
   (TC_HOME_FUN_004).

---

### User Story 3 - Reach other major sections from Home (Priority: P1)

A Sunner uses the hero CTAs (ABOUT AWARD / ABOUT KUDOS) and the bottom NavBar
(SAA 2025 / Awards / Kudos / Profile) to navigate to every other primary screen
in the product.

**Why this priority**: Without navigation the app is single-screen. NavBar
specifically must be reachable from every authenticated screen — Home is the
canonical host. P1.

**Independent Test**: From Home, tap each of the four NavBar tabs in turn and
verify the destination screen renders with the correct tab marked active. Also
tap the two hero buttons.

**Acceptance Scenarios**:

1. **Given** the user is on Home,
   **When** they tap "ABOUT AWARD",
   **Then** the Awards overview screen is displayed (TC_HOME_FUN_007).
2. **Given** the user is on Home,
   **When** they tap "ABOUT KUDOS",
   **Then** the Kudos overview screen is displayed (TC_HOME_FUN_008).
3. **Given** the user is on Home,
   **When** they tap the Awards / Kudos / Profile NavBar tab,
   **Then** the corresponding screen is displayed and the tapped tab is rendered
   in the active state (TC_HOME_FUN_015, TC_HOME_FUN_016, TC_HOME_FUN_017).
4. **Given** the user has navigated to Awards / Kudos / Profile,
   **When** they tap the SAA 2025 tab,
   **Then** the Home screen is restored and the SAA 2025 tab is marked active
   (TC_HOME_ACC_006, TC_HOME_FUN_018).

---

### User Story 4 - Stay protected by session state (Priority: P1)

The Home screen MUST never render for an unauthenticated user, and it MUST
redirect away on a 401 / 403 from any data API. This is the cross-cutting
authorization guarantee for every authenticated screen, anchored at Home because
it is the most-visited.

**Why this priority**: Constitution Principle IV (OWASP) — the client cannot be
the source of truth for authorization. Any data leak via Home is a release
blocker. P1, NON-NEGOTIABLE.

**Independent Test**: With no session, navigate to Home → redirected to Login.
With a valid session, force the awards API to return 401 → redirected to Login;
force 403 → redirected to Access Denied (`k-7zJk2B7s`).

**Acceptance Scenarios**:

1. **Given** the user has a valid Supabase session,
   **When** the awards API or notifications API returns 401,
   **Then** the system signs the user out and routes to Login
   (`8HGlvYGJWq`); a session-expired snackbar (`error_oauth_session_expired`
   from Login spec) is shown on the Login screen so the user understands why
   they were bounced (TC_HOME_ACC_003).
2. **Given** the user has a valid session,
   **When** any data API on Home returns 403,
   **Then** the system routes to Access Denied (`k-7zJk2B7s`) without showing
   any cached protected content (TC_HOME_ACC_004).
3. **Given** the user has no session and tries to deep-link to Home,
   **When** the gate evaluates,
   **Then** the user is redirected to Login (TC_HOME_ACC_002).

---

### User Story 5 - Engage with the Kudos community (Priority: P2)

When the Kudos feature is enabled (`isKudosAvailable = true`), Sunners see a
Kudos section on Home, can read the highlight, can open the Kudos feed via the
section Chi tiết button or via the FAB S/Kudos icon, and can compose a new
"Kudo" via the FAB pencil icon.

**Why this priority**: Kudos is the new community feature for SAA 2025
(non-negotiable spec note in section copy: "ĐIỂM MỚI CỦA SAA 2025"). It is
feature-flagged so it can ship dark — P2 because Awards (US2) is the more
foundational engagement surface, but the Kudos slice is launch-month critical.

**Independent Test**: Toggle `isKudosAvailable=true` → Kudos section visible
with banner, badge, description, and Chi tiết button → tap Chi tiết →
KudosDetail. Tap FAB pencil → WriteKudo form. Tap FAB S/Kudos icon → Kudos feed.
Toggle flag false → Kudos section disappears entirely.

**Acceptance Scenarios**:

1. **Given** `isKudosAvailable = true`,
   **When** Home renders,
   **Then** the Kudos section is visible with banner, badge, description, and
   Chi tiết button (TC_HOME_FUN_009).
2. **Given** `isKudosAvailable = false`,
   **When** Home renders,
   **Then** the Kudos section is not displayed at all (no disabled placeholder,
   no empty container) (TC_HOME_GUI_005).
3. **Given** the Kudos banner image URL fails to load,
   **When** the section renders,
   **Then** a fallback placeholder fills the banner slot (TC_HOME_FUN_010).
4. **Given** Kudos is available and the user taps the section's "Chi tiết"
   button,
   **When** the tap resolves,
   **Then** the Kudos detail / feed screen is displayed (TC_HOME_FUN_011).
5. **Given** the user taps the FAB pencil icon,
   **When** the action resolves,
   **Then** the WriteKudo form screen is displayed (TC_HOME_FUN_012).
6. **Given** the FAB pencil action is in flight,
   **When** the user taps the FAB pencil a second time within the same gesture
   window,
   **Then** the second tap is suppressed and only one WriteKudo screen lands on
   the back stack (TC_HOME_FUN_013).
7. **Given** the user taps the FAB S/Kudos icon,
   **When** the action resolves,
   **Then** the Kudos feed screen is displayed (TC_HOME_FUN_014).

---

### User Story 6 - View notifications (Priority: P2)

A Sunner sees an unread-indicator dot on the bell icon when `unreadCount > 0`,
and tapping the bell opens the Notifications panel.

**Why this priority**: Engagement loop for nudging users back to the app
(announcements, awards updates). P2 because the panel itself can ship with a
read-only stub; surfacing the unread state is what matters first.

**Independent Test**: Seed `unreadCount=3` → red badge dot visible. Tap bell →
Notifications panel opens. Mark all as read (or set `unreadCount=0`) → badge
disappears.

**Acceptance Scenarios**:

1. **Given** the notifications API reports `unreadCount > 0`,
   **When** Home renders,
   **Then** a red badge dot is overlaid on the bell icon (TC_HOME_GUI_006).
2. **Given** `unreadCount = 0`,
   **When** Home renders,
   **Then** the bell icon shows no badge dot (TC_HOME_GUI_006).
3. **Given** the user taps the bell icon,
   **When** the action resolves,
   **Then** the Notifications panel is displayed (TC_HOME_FUN_006).

---

### User Story 7 - Find content and switch language from Home (Priority: P3)

A Sunner taps the search icon to open Search, or taps the language switcher
(reused from Login) to switch between VN / EN / JA. The CTA "ABOUT AWARD" /
"ABOUT KUDOS" labels and "Chi tiết" links are localized; static brand text is
not.

**Why this priority**: Discoverability and accessibility nice-to-haves. The
search screen itself is out of scope here; this story validates the entry path
only. P3.

**Independent Test**: Tap the search icon → Search screen opens. Tap the
language pill → dropdown opens; choose EN → header / theme / award captions
re-render in EN within one frame.

**Acceptance Scenarios**:

1. **Given** the user is on Home,
   **When** they tap the search icon,
   **Then** the Search screen is displayed (TC_HOME_FUN_020).
2. **Given** the user taps the language switcher pill,
   **When** the dropdown opens,
   **Then** options for VN / EN / JA are shown (TC_HOME_FUN_019, FR-013).
3. **Given** the dropdown is open,
   **When** the user selects EN,
   **Then** every localizable on-screen text re-renders in English without an
   Activity recreation (carries SC-004 from Login spec).

---

### Edge Cases

- **Awards API timeout** → render the awards error state with Retry; rest of
  Home (hero, kudos, navbar) remains interactive.
- **Notifications API timeout** → bell icon renders without a badge; tapping
  still opens the panel which can render its own error state.
- **Kudos banner image returns 404** → fallback placeholder per TC_HOME_FUN_010.
- **Countdown crosses 26 Dec 2025 while screen is open** → "Coming soon" label
  hides; values clamp to zero. No re-fetch needed (countdown is client-derived).
- **Device clock manipulated** → countdown is computed against
  `Clock.System.now()` projected into Asia/Ho_Chi_Minh (UTC+7), not against
  the device's local wall clock — manipulating the local clock does not
  fast-forward the countdown. (Server-side authorization unaffected.)
- **App backgrounded then resumed after > 1 minute** → the countdown timer is
  cancelled on `STOPPED` and restarted on `STARTED` (per TR-004); on resume,
  the displayed values immediately recompute against the current wall clock
  so no stale value is shown.
- **Network offline at first render** → awards section shows error/Retry; rest
  of Home renders from local resources. Notifications badge shows last cached
  state.
- **Token revoked mid-session** → the next protected request returns 401, which
  triggers global redirect to Login (TC_HOME_ACC_003).
- **Account restricted (e.g., disabled at server)** → 403 routes to Access
  Denied (TC_HOME_ACC_004).
- **Rapid double-tap on FAB pencil** → second tap suppressed
  (TC_HOME_FUN_013); the same protection MUST apply to every other
  navigation-firing control on Home — FAB S/Kudos, ABOUT AWARD / ABOUT
  KUDOS, all Chi tiết links (award + kudos), bell, search, NavBar tabs —
  per TR-005, to avoid duplicate destinations on the back stack.
- **Kudos section banner image very tall / aspect mismatch** → image is
  letterboxed to the slot; layout below remains stable.

---

## UI/UX Requirements *(from Figma)*

### Screen Components

| Node ID                    | Name                                  | Type     | Interactions                                                                                                                                  |
|----------------------------|---------------------------------------|----------|-----------------------------------------------------------------------------------------------------------------------------------------------|
| `6885:9057`                | Header                                | Frame    | None (container)                                                                                                                              |
| `I6885:9057;88:1827`       | SAA logo                              | Image    | None (decorative; no contentDescription)                                                                                                      |
| `I6885:9057;88:1829`       | Language switcher                     | Dropdown | Tap → opens VN/EN/JA list; selection re-renders localizable text in place                                                                     |
| `I6885:9057;88:1869`       | Search icon                           | Button   | Tap → navigate to Search screen                                                                                                               |
| `I6885:9057;88:1830`       | Notification bell                     | Button   | Tap → open Notifications panel as a modal sheet on top of Home (Q-Home-6); renders red badge dot when `unreadCount > 0`; badge re-fetches on sheet dismissal |
| `6885:8979`                | Keyvisual background                  | Group    | Decorative                                                                                                                                    |
| `6885:8984`                | ROOT FURTHER logo                     | Image    | Decorative (TalkBack reads brand string `ROOT FURTHER`)                                                                                       |
| `6885:8986`                | Countdown timer                       | Frame    | Recomputes once per second toward `2025-12-26T00:00:00+07:00` (Asia/Ho_Chi_Minh); displays DAYS/HOURS/MINUTES; "Coming soon" label hides past target |
| `6885:9026`                | "ABOUT AWARD" button                  | Button   | Tap → navigate to Awards overview (`mms_2.2_Button`)                                                                                          |
| `6885:9027`                | "ABOUT KUDOS" button                  | Button   | Tap → navigate to Kudos overview (`mms_2.3_Button`)                                                                                           |
| `6885:9028` / `6885:9029`  | Theme paragraph                       | Label    | Static text, localized into VN / EN / JA per § Localized Copy (Q-Home-4)                                                                      |
| `6885:9030`                | Awards section                        | Frame    | Container — renders header + scrollable card list                                                                                             |
| `6885:9032`                | Awards card list                      | Frame    | Horizontal scroll; loading / empty / error states with Retry                                                                                  |
| `6885:9033`–`6885:9035`    | Award card                            | Instance | Renders the sub-elements defined by the `Top Talent Award` instance component (`6885:8051`); no extra API-driven fields beyond what the design slot consumes (Q-Home-8). Tap "Chi tiết" → Award Detail screen for that award id |
| `6885:9039`                | Kudos section (lower)                 | Frame    | This LOWER section is visible only when `isKudosAvailable = true`; entirely hidden otherwise (Q-Home-9). Hero/FAB/NavBar Kudos surfaces are not gated here |
| `6885:9041`                | Kudos banner                          | Frame    | Image with fallback placeholder on load failure                                                                                               |
| `6885:9053`                | Kudos description                     | Label    | Static localized copy                                                                                                                         |
| `6885:9055`                | Kudos "Chi tiết" button               | Button   | Tap → Kudos detail / feed screen                                                                                                              |
| `6885:9058`                | Floating Action Button (FAB)          | Instance | Pencil icon (→ WriteKudo, hidden when `isKudosAvailable=false`, double-tap suppressed) + S/Kudos icon (→ Kudos feed, always shown) — see § Component Behavior |
| `6885:9056`                | Bottom NavBar                         | Instance | 4 tabs: SAA 2025 (active on Home), Awards, Kudos, Profile. Tap any tab → corresponding root. Re-tap of active SAA 2025 tab while on Home → smooth-scroll to top (Q-Home-3) |

### Component Behavior — detailed

#### `6885:8986` — Countdown timer
- **Interaction**: none (display-only).
- **State transitions**:
  - `pre-event` → recompute `(target - now)` every 1 second, render DAYS / HOURS
    / MINUTES; "Coming soon" label visible.
  - `at-or-post-event` → values clamp to 0; "Coming soon" label hidden.
- **Lifecycle**: timer starts on Home `STARTED`, cancels on `STOPPED` (via
  `repeatOnLifecycle`).
- **Target**: `2025-12-26T00:00:00+07:00` (Sun* HQ time zone, Asia/Ho_Chi_Minh,
  UTC+7). Compared against `Clock.System.now()` adjusted to that zone — the
  device's local clock is NOT the ground truth.

#### `6885:9026` — "ABOUT AWARD" button (`mms_2.2_Button`)
- **Interaction**: `on_click` → navigate to Awards overview screen.
- **Validation**: enabled while session is valid; double-tap suppressed.
- **State transitions**: idle ↔ pressed (visual only) → navigate.

#### `6885:9027` — "ABOUT KUDOS" button (`mms_2.3_Button`)
- **Interaction**: `on_click` → navigate to Kudos overview screen.
- **Visibility (Q-Home-9)**: always rendered alongside ABOUT AWARD,
  regardless of `isKudosAvailable`. The destination Kudos overview screen
  handles its own empty/disabled state.

#### `6885:9032` / `6885:9033`–`6885:9035` — Awards card list + cards
- **Interaction**: card item is informational; tapping the "Chi tiết" link on a
  card navigates to Award Detail. Horizontal swipe scrolls the row.
- **State transitions**:
  - `loading` → spinner / skeleton in the section body.
  - `empty` → empty-state message; no cards.
  - `error` → error UI with Retry button; tapping Retry re-fires the awards
    fetch.
  - `populated` → cards rendered using the design's `Top Talent Award`
    instance component (`6885:8051`); the API populates only the slots the
    component exposes, no additional fields (Q-Home-8).
- **Validation**: card content sourced entirely from server response; no client
  authorization filtering.

#### `6885:9039` — Kudos section
- **Visibility**: `isKudosAvailable = true` (server-provided feature flag).
- **State transitions**:
  - `unavailable` → not in composition.
  - `available + banner-loading` → placeholder where banner image will land.
  - `available + banner-loaded` → banner image rendered.
  - `available + banner-error` → fallback placeholder image.

#### `6885:9055` — Kudos "Chi tiết" button
- **Interaction**: `on_click` → navigate to Kudos detail / feed screen.
- **Validation**: only rendered when Kudos section is visible.

#### `6885:9058` — Floating Action Button (FAB)
- **Layout**: fixed bottom-right above the NavBar.
- **Children**: pencil icon and S/Kudos icon.
- **Interactions**:
  - Pencil → navigate to WriteKudo form. Double-tap suppression: ignore the
    second tap if the navigation transaction has not yet completed
    (TC_HOME_FUN_013).
  - S/Kudos → navigate to Kudos feed.
- **Visibility (Q-Home-2 / Q-Home-9)**:
  - **Pencil** — rendered ONLY when `isKudosAvailable = true`. Hidden
    completely when the flag is false (no point writing into a disabled
    Kudos surface).
  - **S/Kudos** — rendered always for authenticated users, regardless of
    `isKudosAvailable`. The Kudos feed destination handles its own
    empty/disabled state.

#### `6885:9056` — Bottom NavBar
- **Tabs**: `SAA 2025` | `Awards` | `Kudos` | `Profile`.
- **Active state**: at most one tab active at a time; the SAA 2025 tab is
  active while the user is on Home, mirrored to whichever NavBar root the user
  is currently viewing. (Visual treatment of the active vs inactive state is
  driven by design tokens fetched at implementation time.)
- **Interactions**: tap any tab → navigate to that root; re-tapping the
  active SAA 2025 tab while on Home **scrolls Home to top** (Q-Home-3).
  Re-tap behavior on other tabs' roots is owned by those tabs' specs.

#### `I6885:9057;88:1830` — Notification bell
- **Interaction**: `on_click` → open Notifications panel as a **modal sheet
  on top of Home** (Q-Home-6). System back press dismisses the sheet without
  leaving Home; on dismissal Home re-fetches `notifications_summary` so the
  badge mirrors any newly-read items.
- **Validation**: `unreadCount: Int >= 0` from notifications API; badge dot
  shown when `> 0`.

#### `I6885:9057;88:1829` — Language switcher
- **Behavior identical** to the Login screen language selector
  (`uUvW6Qm1ve` / `mms_2.1_language` from `8HGlvYGJWq`); reuses the same
  component and persisted preference (`LanguagePreferenceRepository`).

(Static elements `6885:8979`, `6885:8984`, `6885:9028`, `6885:9053` have no
interaction states.)

### Navigation Flow

- **Entry**: success path of `[iOS] Login` (US1) OR cold start with valid
  session (US2 of Login). Direct deep-link with no session → bounced to Login
  by the auth gate.
- **Outbound (from Home, push onto back stack)**:
  - Award card "Chi tiết" → Award Detail (per-award id; screen TBD).
  - "ABOUT AWARD" → Awards overview (root for Awards tab).
  - "ABOUT KUDOS" → Kudos overview.
  - Kudos section "Chi tiết" → Kudos detail / feed screen.
  - FAB pencil → WriteKudo form (only when `isKudosAvailable = true`).
  - FAB S/Kudos icon → Kudos feed.
  - Search → Search screen.
  - NavBar tabs → Awards / Kudos / Profile roots.
- **Outbound (modal overlay on top of Home, not back-stack push)**:
  - Bell → Notifications panel as a modal sheet (Q-Home-6); back press
    dismisses without leaving Home.
- **Auth-failure paths**:
  - 401 anywhere on Home → sign out + Login (`8HGlvYGJWq`); Login surfaces
    `error_oauth_session_expired` snackbar so the user understands the
    bounce.
  - 403 anywhere on Home → Access Denied (`k-7zJk2B7s`).

### Localized Copy

Where labels appear on Home, the following keys are referenced:

**Brand-fixed (NOT localized)** — same on every locale:

| Key                          | Value                                                                                                                                                         |
|------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `brand_root_further`         | `ROOT FURTHER` (image-rendered tagline)                                                                                                                       |
| `home_btn_about_award`       | `ABOUT AWARD`                                                                                                                                                 |
| `home_btn_about_kudos`       | `ABOUT KUDOS`                                                                                                                                                 |
| `home_countdown_days_label`  | `DAYS` (matches the all-caps brand styling on the hero; same on every locale)                                                                                 |
| `home_countdown_hours_label` | `HOURS`                                                                                                                                                        |
| `home_countdown_min_label`   | `MINUTES`                                                                                                                                                      |
| `home_coming_soon`           | `Coming soon` (English brand label across all locales)                                                                                                         |

**Localized — display strings**:

| Key                          | VN (default)                                                                                                                                                                                                                                                                                                                                                                                                | EN                                | JA                                  |
|------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-----------------------------------|-------------------------------------|
| `home_theme_paragraph`       | `Không đơn thuần là một cái tên, "Root Further" chính là tinh thần mà mỗi người Sun* đang hướng tới: luôn nhìn nhận sâu sắc trong mọi bối cảnh và không ngừng sáng tạo, mở rộng bản thân để vượt qua những giới hạn mà chính mình đã từng đặt ra. Mượn hình ảnh ẩn dụ của lý thuyết phối màu, chỉ từ ba màu cơ bản: đỏ, vàng và lam, sức sáng tạo vô tận của mỗi cá nhân có thể tạo ra số lượng màu sắc gần như vô hạn, với mỗi gam màu đều đại diện cho sự bứt phá và sáng tạo không giới hạn.` | TBD — flag for translation        | TBD — flag for translation          |
| `home_section_awards_title`  | `Sun* Annual Awards 2025`                                                                                                                                                                                                                                                                                                                                                                                   | `Sun* Annual Awards 2025`          | `Sun* Annual Awards 2025`            |
| `home_section_kudos_title`   | `Phong trào ghi nhận`                                                                                                                                                                                                                                                                                                                                                                                       | `Recognition movement` [DRAFT]    | `表彰活動` [DRAFT]                    |
| `home_kudos_note_heading`    | `ĐIỂM MỚI CỦA SAA 2025`                                                                                                                                                                                                                                                                                                                                                                                     | `WHAT'S NEW IN SAA 2025` [DRAFT]  | `SAA 2025の新機能` [DRAFT]            |
| `home_kudos_note_body`       | `Hoạt động ghi nhận và cảm ơn đồng nghiệp - lần đầu tiên được diễn ra dành cho tất cả Sunner. Hoạt động sẽ được triển khai vào tháng 11/2025, khuyến khích người Sun* chia sẻ những lời ghi nhận, cảm ơn đồng nghiệp trên hệ thống do BTC công bố. Đây sẽ là chất liệu để Hội đồng Heads tham khảo trong quá trình lựa chọn người đạt giải.`                                                                  | TBD — flag for translation        | TBD — flag for translation          |
| `home_link_chi_tiet`         | `Chi tiết`                                                                                                                                                                                                                                                                                                                                                                                                  | `Details`                         | `詳細`                                |
| `home_awards_loading`        | `Đang tải…`                                                                                                                                                                                                                                                                                                                                                                                                  | `Loading…`                        | `読み込み中…`                            |
| `home_awards_empty`          | `Chưa có giải thưởng nào`                                                                                                                                                                                                                                                                                                                                                                                    | `No awards yet`                   | `まだアワードはありません`                  |
| `home_awards_error`          | `Không thể tải danh sách giải thưởng. Vui lòng thử lại.`                                                                                                                                                                                                                                                                                                                                                     | `Couldn't load awards. Try again.`| `アワードを読み込めませんでした。再試行してください。` |
| `home_action_retry`          | `Thử lại`                                                                                                                                                                                                                                                                                                                                                                                                    | `Retry`                           | `再試行`                                 |
| `home_navbar_saa_2025`       | `SAA 2025`                                                                                                                                                                                                                                                                                                                                                                                                  | `SAA 2025`                        | `SAA 2025`                            |
| `home_navbar_awards`         | `Giải thưởng`                                                                                                                                                                                                                                                                                                                                                                                                | `Awards`                          | `アワード`                              |
| `home_navbar_kudos`          | `Kudos`                                                                                                                                                                                                                                                                                                                                                                                                      | `Kudos`                           | `Kudos`                              |
| `home_navbar_profile`        | `Hồ sơ`                                                                                                                                                                                                                                                                                                                                                                                                      | `Profile`                         | `プロフィール`                            |

**Accessibility labels**:

| Key                              | Trigger                                                                                                                              |
|----------------------------------|--------------------------------------------------------------------------------------------------------------------------------------|
| `a11y_home_bell_badge`           | "Notifications, %d unread" / locale equivalents — content description for the bell when `unreadCount > 0`                            |
| `a11y_home_bell_no_badge`        | "Notifications" / locale equivalents — when `unreadCount = 0`                                                                        |
| `a11y_home_search`               | "Search" / locale equivalents                                                                                                        |
| `a11y_home_fab_compose_kudo`     | "Write a Kudo" / locale equivalents                                                                                                  |
| `a11y_home_fab_kudos_feed`       | "Open Kudos feed" / locale equivalents                                                                                               |
| `a11y_home_navbar_tab_active`    | "%s, selected, tab" — announces the currently active tab                                                                              |
| `a11y_home_navbar_tab_inactive`  | "%s, tab" — announces an inactive tab                                                                                                 |

### Behavioral Accessibility

- **Focus order** (top-to-bottom, then left-to-right): language switcher →
  search → bell → ROOT FURTHER (skipped if decorative) → countdown values
  (read as "X days, Y hours, Z minutes remaining") → ABOUT AWARD → ABOUT KUDOS
  → theme paragraph → awards section header → first award "Chi tiết" link → …
  → Kudos "Chi tiết" → FAB pencil → FAB S/Kudos → NavBar tabs.
- **Live region**: countdown values use `liveRegion = Polite` so TalkBack
  re-announces only on minute changes (not every second — verbosity guard).
- **Tab semantics**: NavBar tabs MUST expose `Role.Tab` with `selected = true`
  for the active tab so TalkBack reads "Tab N of 4, selected".
- **Keyboard / external-control navigation**: every interactive control
  reachable via Tab; Enter/Space activates.
- **Touch targets**: every interactive control ≥ 48dp × 48dp (constitution
  Principle III). The four NavBar tabs MUST split the bottom bar evenly while
  preserving 48dp tap regions.
- **Internationalization**: as Login (VN default; VN/EN/JA supported via the
  shared `LanguagePreferenceRepository`); right-to-left out of scope.

---

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST render the Home screen as the start destination of
  the authenticated app shell (post-Login OR cold start with valid session).
- **FR-002**: System MUST recompute the remaining time to the countdown target
  once per second; the displayed values are DAYS / HOURS / MINUTES (no SECONDS
  field is shown). When the target is reached, the values clamp to zero and the
  "Coming soon" label hides. Recomputation continues only while the screen is
  in the `STARTED` lifecycle state (TR-004).
- **FR-003**: System MUST render the awards section in one of four states:
  loading, empty, error (with Retry), or populated cards — driven by the
  awards API response.
- **FR-004**: Tapping "Chi tiết" on an award card MUST navigate to that
  specific award's Detail screen.
- **FR-005**: System MUST render the Kudos section IF AND ONLY IF
  `isKudosAvailable = true`; otherwise the section MUST NOT occupy any
  layout space.
- **FR-006**: When the Kudos banner image fails to load, the system MUST
  render a fallback placeholder of the same dimensions.
- **FR-007**: Tapping the FAB pencil icon MUST navigate to WriteKudo. A
  second tap during the same in-flight navigation MUST be suppressed
  (no duplicate WriteKudo on the back stack).
- **FR-008**: Tapping the FAB S/Kudos icon MUST navigate to the Kudos feed.
- **FR-009**: Tapping the bell icon MUST open the Notifications panel as a
  modal sheet over Home (Q-Home-6). System back press dismisses the sheet
  without leaving Home; on dismissal the badge MUST be re-fetched.
- **FR-010**: System MUST display a red badge dot on the bell icon when
  `unreadCount > 0` and MUST hide it when `unreadCount = 0`.
- **FR-011**: Tapping the search icon MUST navigate to the Search screen.
- **FR-012**: Tapping the language switcher MUST open a VN/EN/JA dropdown;
  selecting a language MUST persist via `LanguagePreferenceRepository` and
  re-render localizable text within one frame (carries SC-004 from Login).
- **FR-013**: Tapping a NavBar tab MUST navigate to its destination root and
  mark itself active. Re-tapping the active SAA 2025 tab while on Home MUST
  smooth-scroll Home's content to the top (Q-Home-3).
- **FR-014**: 401 from any Home data API MUST trigger sign-out and
  navigation to Login. Login MUST surface the `error_oauth_session_expired`
  snackbar (defined in `8HGlvYGJWq` Login spec) so the user understands the
  redirect.
- **FR-015**: 403 from any Home data API MUST trigger navigation to Access
  Denied.
- **FR-016**: Tapping "ABOUT AWARD" MUST navigate to the Awards overview
  root; tapping "ABOUT KUDOS" MUST navigate to the Kudos overview root.

### Technical Requirements

- **TR-001 (Auth, Constitution Principle II)**: All Home APIs MUST be called
  with the Supabase access token; the token comes from the SDK-managed
  session (no client-side parallel cache).
- **TR-002 (Authorization, Constitution Principle II / IV)**: Authorization
  is enforced by Postgres RLS on the Supabase backend. The client MUST NOT
  filter rows for visibility — it renders whatever the server returns.
- **TR-003 (Performance)**: Awards section first paint (loading state)
  MUST appear within 100 ms of Home composing; populated cards MUST render
  within 2000 ms p95 on a healthy network.
- **TR-004 (Reliability)**: Countdown timer MUST be lifecycle-aware
  (`repeatOnLifecycle(STARTED)`) so it does not run while the screen is
  off-screen.
- **TR-005 (Reliability)**: All double-tap-prone navigation actions on Home
  MUST suppress duplicate navigation transactions: FAB pencil, FAB S/Kudos
  icon, ABOUT AWARD / ABOUT KUDOS hero buttons, Chi tiết links (award cards
  and Kudos section), bell icon, search icon, NavBar tabs. (Mirrors FR-003 of
  Login spec; one-shot navigation transaction per gesture window.)
- **TR-006 (Security, Constitution Principle IV)**: TLS-only network calls;
  certificate pinning enabled in release builds for Supabase endpoints
  (shared with Login).
- **TR-007 (Security)**: No PII / award content / notification body leaks
  to logs (shared `SecureTimberTree` from Login).
- **TR-008 (i18n)**: All user-visible non-brand text on this screen MUST come
  from a localizable resource, identical pattern to Login.
- **TR-009 (a11y)**: NavBar tabs MUST expose `Role.Tab` semantics; bell,
  FAB icons, search, and award Chi tiết links MUST have non-empty
  `contentDescription`.

### Key Entities

- **Award**: `{ id: uuid, name: string, thumbnailUrl: string, sortOrder: int }`
  — surfaced by the awards API. The card renders ONLY the sub-elements defined
  by the `Top Talent Award` instance component (`6885:8051`); no additional
  fields beyond those the design slot consumes (Q-Home-8). Rendered in display
  order by `sortOrder`.
- **NotificationSummary**: `{ unreadCount: int }` — minimum payload for the
  badge. Detailed notifications fetched by the panel itself.
- **KudosSummary**: `{ isKudosAvailable: bool, bannerImageUrl: string?,
  badgeText: string?, descriptionText: string }` — drives the entire Kudos
  section's visibility + content. Server-side feature flag.
- **LanguagePreference**: shared with Login; persisted in DataStore
  (`VN | EN | JA`, default `VN`).
- **CountdownTarget**: `2025-12-26T00:00:00+07:00` (Asia/Ho_Chi_Minh, UTC+7)
  — static client constant. Server-side validation is unaffected; this is
  display-only.

---

## API Dependencies

> Endpoints below are **predicted** based on observed component behavior.
> Concrete contracts are deferred to `/momorph.apispecs`.

| Endpoint / SDK call                                        | Method | Purpose                                                                            | Triggered by                  | Status    |
|------------------------------------------------------------|--------|------------------------------------------------------------------------------------|-------------------------------|-----------|
| `GET /rest/v1/awards?select=*&order=sort_order` (RLS)      | GET    | Load award cards for the carousel                                                  | Home `STARTED`, Retry tap     | Predicted |
| `GET /rest/v1/notifications_summary` (RLS or RPC)          | GET    | Get `unreadCount` for the bell badge                                               | Home `STARTED`; on Notifications sheet dismissal (Q-Home-6) | Predicted |
| `GET /rest/v1/kudos_summary` (RLS) — or feature-flag RPC   | GET    | Get `{ isKudosAvailable, banner, description }`                                    | Home `STARTED`                | Predicted |
| `auth.signOut()` (Supabase SDK)                            | n/a    | Side-effect when any Home API returns 401 (TR-001 / FR-014)                        | 401 from Home APIs            | Predicted |

> The notifications-list endpoint (`GET /rest/v1/notifications`) is owned by
> the Notifications panel spec, not Home. Home only fires the badge-summary
> endpoint above; tapping the bell hands off to that panel, which fetches
> its own data.

The 401 / 403 routing logic is project-wide concern (Constitution Principle IV)
and can be implemented as a single Supabase HTTP interceptor invoked by every
Postgrest call, not duplicated per API.

---

## State Management

- **Global / app-scoped**:
  - `AuthState` — already global from Login; Home reads-only.
  - `LanguagePreference` — already global from Login.
  - `KudosFeatureFlag` (`isKudosAvailable: Boolean`) — derived from
    `kudos_summary` API response; refreshed on every Home `STARTED`
    (Q-Home-5). No longer-lived in-memory cache; the flag IS the response.
- **Screen-local**:
  - `awards.state`: sealed `Loading | Empty | Error(reason) | Populated(list)`.
  - `notifications.state`: sealed `Loading | Loaded(unreadCount) | Error`.
  - `kudos.state`: sealed `Hidden | Loading | Loaded(KudosSummary) | Error`.
  - `countdown.state`: derived ticking value `(days, hours, minutes,
    isPreEvent)`.
  - `fab.pencilInFlight`: boolean — guards double-tap suppression
    (TR-005).
- **Cache / invalidation (Q-Home-5)**:
  - Awards / Kudos summary / Notifications summary: refreshed on every Home
    `STARTED` lifecycle event. No longer-lived in-memory cache.
  - Awards section: explicit refresh also on Retry tap (TC_HOME_FUN_003).
  - Notifications summary: also refreshed on dismissal of the Notifications
    sheet (Q-Home-6) so the badge reflects newly-read notifications.
- **Fetch concurrency (Q-Home-7)**: awards / kudos_summary /
  notifications_summary are fired in **parallel** on Home `STARTED`. Each
  has an independent state machine; failure in one section does not block
  the others.
- **Optimistic updates**: not applicable on this screen (read-only).

---

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: ≥ 99% of Home renders show the populated awards list within
  2000 ms on healthy networks (TR-003).
- **SC-002**: 0 instances of duplicate destinations on the back stack from
  double-tap on FAB pencil / NavBar tabs / Chi tiết links in production
  telemetry (TR-005).
- **SC-003**: Countdown reflects the correct minute-precision remaining time
  to 26 Dec 2025 within ± 1 second of wall-clock at any sample point.
- **SC-004**: Language change from Home re-renders all localizable text
  within 1 frame (carries SC-004 from Login).
- **SC-005**: 0 instances of access tokens / award content / notification
  bodies appearing in production logs (validated by log-scrub tests; shared
  with Login SC-005).
- **SC-006**: ≥ 99% of 401 responses on Home result in a Login redirect
  within 2 seconds (server-side observable in API audit logs).

---

## Out of Scope

- Award Detail, Awards overview, Kudos feed, Kudos detail, WriteKudo form,
  Notifications panel, Search, Profile screens — separate specs.
- Pull-to-refresh on Home — defer to a later iteration if needed.
- Push notifications — only the in-app unread badge is in scope here.
- Awards search / filter / sort UI — out of scope.
- Visual specs (colors, sizes, fonts, asset variants, animations) — fetched
  at implementation time via MoMorph `query_section` / `get_node` /
  `list_media_nodes` for the Node IDs in this spec.

---

## Dependencies

- [x] Constitution document exists (`.momorph/constitution.md`) — v1.0.0
- [x] Login spec exists (`.momorph/specs/8HGlvYGJWq-iOS-Login/spec.md`) —
      Home is the success destination for US1 of Login.
- [ ] API specifications available (`.momorph/contexts/api-docs.yaml`) — to be
      produced via `/momorph.apispecs`
- [ ] Database design completed (`.momorph/contexts/database-schema.sql`) —
      to be produced via `/momorph.database`. Entities `awards`,
      `notifications_summary`, `kudos_summary` to be designed.
- [ ] Screen flow documented (`.momorph/SCREENFLOW.md`) — to be produced via
      `/momorph.screenflow` (still pending from Login spec dependency).
- [ ] Award Detail screen spec — required to wire `Chi tiết` navigation.
- [ ] Awards overview screen spec — required to wire `ABOUT AWARD` and the
      NavBar `Awards` tab.
- [ ] Kudos overview screen spec — required to wire `ABOUT KUDOS`.
- [ ] Kudos feed screen spec — required to wire FAB S/Kudos + Kudos tab.
- [ ] Kudos detail screen spec — required to wire section `Chi tiết`.
- [ ] WriteKudo form spec — required to wire FAB pencil.
- [ ] Notifications panel spec — required to wire bell tap.
- [ ] Search screen spec — required to wire search icon.
- [ ] Profile screen spec — required to wire NavBar `Profile` tab.
- [ ] Access Denied spec (`k-7zJk2B7s`) — already referenced from Login.

---

## Constitution Alignment

Verified against `.momorph/constitution.md` v1.0.0:

- **I. Clean Code & Source Organization** — feature folder `home/` planned
  with `ui/` (composables, state) + `data/` (repos for awards, notifications,
  kudos summary) + `domain/` (use cases for retry / dismissal). Existing
  shared `core/` for session, locale, navigation.
- **II. Tech Stack Best Practices** — sealed `awards.state`, `kudos.state`,
  `notifications.state`; `StateFlow`/coroutines; lifecycle-aware countdown
  via `repeatOnLifecycle`; reuses Supabase Auth + Postgrest from Login.
- **III. Material Design 3 (Android)** — implementation target is **Android
  only**. Hero buttons → Material `Button`; bottom NavBar → Material
  `NavigationBar` + `NavigationBarItem`; FAB → Material `FloatingActionButton`
  (or `ExtendedFloatingActionButton` if we expand both icons) layered above
  the NavBar; awards card list → Compose `LazyRow`; Kudos section → standard
  Material card / column. Theme tokens via existing `ui/theme/`; light + dark;
  dynamic color on Android 12+; `WindowSizeClass` for phone/foldable/tablet;
  min touch target 48dp.
- **IV. OWASP Secure Coding** — TR-001..TR-006 cover token storage, TLS,
  cert pinning, RLS-as-authoritative-authorization, and 401/403 redirects.
  No client-side authorization filtering.
- **V. Test-Driven Development** — every FR/SC is testable: ViewModel state
  transitions for awards / notifications / kudos / countdown; instrumented
  Compose tests for FAB double-tap, NavBar tab activation, badge visibility;
  RLS denial test for awards as a non-Sunner JWT (mirrors Login's
  `RlsPolicyTest`).

---

## Resolved Decisions

All review-time questions resolved 2026-05-07; carried into the body of this
spec.

- [x] **Q-Home-1 (Behavior)** — **Resolved**: countdown target is
      `2025-12-26T00:00:00+07:00` (Asia/Ho_Chi_Minh, UTC+7). Computed against
      `Clock.System.now()` adjusted to that zone — device clock is irrelevant
      for the ground truth.
- [x] **Q-Home-2 (Behavior)** — **Resolved**: FAB pencil (WriteKudo entry) is
      **hidden** when `isKudosAvailable = false`. Only renders when the Kudos
      surface is available. The FAB S/Kudos icon (Kudos feed) is shown
      regardless (see Q-Home-9).
- [x] **Q-Home-3 (UX)** — **Resolved**: re-tapping the active SAA 2025 tab
      while on Home **scrolls Home to top** (iOS standard). Re-tap on any
      other active tab is owned by that tab's spec.
- [x] **Q-Home-4 (Copy)** — **Resolved**: the theme paragraph (`mms_3_note`)
      IS localized into EN / JA. Moved out of brand-fixed into the localized
      table; EN / JA copy left TBD pending translation.
- [x] **Q-Home-5 (Caching)** — **Resolved**: refresh on every Home `STARTED`;
      no longer-lived in-memory cache.
- [x] **Q-Home-6 (Notifications)** — **Resolved**: Notifications panel is a
      **modal sheet on top of Home**, NOT a separate route on the back stack.
      System back press dismisses the sheet without leaving Home.
- [x] **Q-Home-7 (Performance)** — **Resolved**: awards / kudos_summary /
      notifications_summary fire in **parallel** on Home `STARTED`, with
      independent state machines per section.
- [x] **Q-Home-8 (Data)** — **Resolved**: award card renders ONLY the
      sub-elements shown in the Figma instance — no additional API-driven
      fields. The `Award` entity exposes only what the card slot consumes.
      Implementation queries the per-card sub-elements at impl time via
      `query_section`/`get_node` for the `Top Talent Award` instance
      component (`6885:8051`).
- [x] **Q-Home-9 (Surface)** — **Resolved**: ONLY the lower Kudos section
      itself is gated on `isKudosAvailable`. The ABOUT KUDOS hero button,
      FAB S/Kudos icon, and NavBar `Kudos` tab remain visible regardless of
      the flag. (Tap into them while flag is false navigates to the
      respective destination, which renders its own empty/disabled state.)
      Note this differs from Q-Home-2: the FAB **pencil** (WriteKudo entry)
      IS hidden when the flag is false because there is nothing to write
      into; the FAB **S/Kudos** icon (feed entry) is always shown.

---

## Notes

- Frame revision pinned: `3c9059f3da88539320cb62e39aefcf38`. Re-run
  `/momorph.specify` and diff this spec if the design revision changes.
- Test coverage: the 32 test cases under `frame.id=49532` in MoMorph
  (`TC_HOME_ACC_001..006`, `TC_HOME_GUI_001..006`,
  `TC_HOME_FUN_001..020`) map onto the acceptance scenarios above. A mapping
  table will be produced by `/momorph.createtestcases`.
- The language switcher reuses the same component and persisted preference
  as Login (`LanguageSelector` + `LanguagePreferenceRepository`); implementation
  should NOT re-implement.
- The Home keyvisual is similar to Login's but the frame adds two extra
  rectangles in `mm_media_bg` (`Shadow Left` 6885:8981, `Shadow Bottom`
  6885:8982) layered around the same `MM_MEDIA_Keyvisual BG`. Implementation
  may either reuse `bg_keyvisual` and add overlay shadow drawables, or export
  a new pre-shadowed keyvisual — defer to implementation-time visual
  fidelity check.
- `SCREENFLOW.md` is still missing (carried over from Login spec); this spec
  describes Home's outbound edges so it can be folded into the SCREENFLOW
  graph when produced.
- Visual specs and assets (countdown number style, NavBar icons, FAB icons,
  Kudos banner placeholder, award card design) are intentionally NOT
  enumerated here — implementation queries them on demand via MoMorph
  `query_section` / `get_node` / `get_media_files`.
