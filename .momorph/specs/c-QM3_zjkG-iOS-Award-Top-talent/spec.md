# Feature Specification: Award Detail (Top Talent default)

**Frame ID**: `c-QM3_zjkG`
**Frame Name**: `[iOS] Award_Top talent`
**File Key**: `9ypp4enmFmdK3YAFJLIu6C`
**Created**: 2026-05-11
**Reviewed**: 2026-05-11
**Status**: Ratified — review questions resolved 2026-05-11 (see § Resolved Questions)

---

## Overview

The Award Detail screen is the authenticated read-only page that surfaces the
full information for a single award category in the **Sun\* Annual Awards 2025
(SAA 2025)** system. The Figma frame is labelled "Top Talent" because that is
the default selection rendered in the design, but the screen is **parametric**:
a category dropdown inside the **Highlight block** (`mms_B.1_header`, NOT the
page header — the page header hosts the language pill, search, and bell) lets
the user switch between every award returned by the awards API, and the
entire body re-renders from the API payload of the newly selected category.

Each award detail surfaces:

- Badge image, award name, multi-paragraph description
- Number of recipients (e.g. "10 Cá nhân") and prize value (e.g.
  "7.000.000 VNĐ cho mỗi giải thưởng")
- A Kudos promo block (the "Sun\* Kudos" community feature) with a Chi tiết CTA
  that bounces the user to the Sun\* Kudos hub

The screen sits inside the standard SAA 2025 chrome (sticky header with logo +
language pill + search + bell, sticky bottom navigation bar with four tabs)
and is reached primarily from the **Home awards carousel** ("Chi tiết" on any
award card → this screen, scoped to that award).

**Target users**: authenticated Sunners with a valid Supabase session — the
screen is **gated by authentication** (TC_IOS_AWARD_DETAIL_ACC_002).

**Implementation platform**: **Android only** (Kotlin + Jetpack Compose +
Material 3 + Supabase) per constitution v1.0.0 § Tech Stack. The MoMorph frame
is labelled "[iOS]" because that is the source design language; on Android it
renders as the platform-native Material 3 surfaces (`Scaffold`, `LazyColumn`,
`ExposedDropdownMenuBox`, `NavigationBar`).

**Business context**: The awards catalogue is the **content hook** of SAA
2025 — Home's awards carousel is the discovery surface (per the Home spec US2,
`OuH1BUTYT0`), and this screen is the destination for every "Chi tiết" tap on
that carousel. Without this screen the carousel funnels nowhere.

---

## User Scenarios & Testing *(mandatory)*

### User Story 1 - View an award's full details (Priority: P1)

A signed-in Sunner who tapped "Chi tiết" on an award card from Home lands on
this screen with the full information for that award fully populated from the
backend: badge image, name, description, recipient count + unit, and prize
value.

**Why this priority**: This is the **reason the screen exists**. Without it the
Home carousel "Chi tiết" buttons funnel into a blank shell. P1 because every
US below assumes the body has loaded.

**Independent Test**: Sign in, tap "Chi tiết" on a Top Talent card on Home,
verify the Award Detail screen mounts and shows Top Talent's full data
(name, description, quantity=10 Cá nhân, prize=7.000.000 VNĐ) within ~1s on
a normal connection (TC_IOS_AWARD_DETAIL_ACC_001 + TC_IOS_AWARD_DETAIL_FUN_001).

**Acceptance Scenarios**:

1. **Given** the user has tapped "Chi tiết" on a Top Talent card,
   **When** the screen renders for the first time,
   **Then** within a single round-trip to `/awards/:id` the body shows the
   award's badge image, title row (icon + name), description paragraph,
   recipient count block (icon + label "Số lượng giải thưởng" + value + unit),
   and prize block (icon + label "Giá trị giải thưởng" + value + caption
   "cho mỗi giải thưởng") — every field is sourced from the API payload, no
   hard-coded copy (TC_IOS_AWARD_DETAIL_FUN_001).
2. **Given** the awards API is still in flight,
   **When** the user is on the screen,
   **Then** a loading indicator (spinner or skeleton) renders in the body
   slot and the header + bottom nav remain interactive
   (TC_IOS_AWARD_DETAIL_FUN_002).
3. **Given** the awards API call has failed (offline, 5xx, or timeout),
   **When** the body renders,
   **Then** a friendly error message + a **Retry** button appears in place of
   the body; tapping Retry re-issues the request and surfaces a fresh loading
   state. The exception text is **never** shown directly per TR-002 (security)
   and constitution § OWASP — only the localized error copy
   (TC_IOS_AWARD_DETAIL_FUN_003).
4. **Given** the API returns an award whose `image_url` is `null`,
   **When** the body renders,
   **Then** the badge image slot shows a placeholder of the same dimensions
   and the rest of the body still renders normally (no crash, no layout
   collapse) — see FR-008 + TC_IOS_AWARD_DETAIL_FUN_004 + GUI_007.
5. **Given** the API returns an award whose `quantity` and/or `prize_value` is
   `null`,
   **When** the body renders,
   **Then** the corresponding section shows a placeholder ("—" or similar)
   instead of `null` text and the rest of the body still renders normally
   (TC_IOS_AWARD_DETAIL_FUN_020).

---

### User Story 2 - Switch between award categories via the dropdown (Priority: P1)

A user viewing one award (e.g. Top Talent) wants to compare it to another
category (e.g. Top Project) without leaving the screen. The dropdown in the
Highlight block lists every award category available in the backend; selecting
one re-fetches that award's payload and the body re-renders in place.

**Why this priority**: The dropdown is the design's only **on-screen** way to
navigate across categories — without it the user has to go back to Home,
scroll the carousel, and tap a different card. P1 because the test suite
documents 5 functional cases for it (FUN_005–FUN_009).

**Independent Test**: From a populated detail screen, tap the dropdown, verify
the list contains every award returned by `/awards`, tap a different category,
and verify the body fully re-renders to that category's data while the header
and bottom nav stay unchanged (TC_IOS_AWARD_DETAIL_FUN_006).

**Acceptance Scenarios**:

1. **Given** the dropdown is closed and shows the active award's name,
   **When** the user taps the dropdown trigger,
   **Then** a menu opens listing **every** award returned by `/awards` (no
   client-side hardcoding); the active award's row is rendered in a
   visually distinct selected state (TC_IOS_AWARD_DETAIL_FUN_005).
2. **Given** the dropdown is open with ≥ 2 categories,
   **When** the user selects a category other than the currently active one,
   **Then** the dropdown closes, the trigger updates to the selected name,
   the body shows a loading state, and once the new payload arrives the
   badge image, title, description, quantity, and prize all update; the
   sticky header + sticky bottom nav do **not** re-render
   (TC_IOS_AWARD_DETAIL_FUN_006).
3. **Given** the dropdown is open,
   **When** the user taps outside the menu bounds,
   **Then** the menu dismisses and the active category is unchanged
   (TC_IOS_AWARD_DETAIL_FUN_007).
4. **Given** the dropdown is open and the user re-selects the **already active**
   category,
   **When** the row is tapped,
   **Then** the menu closes silently and no second API call is fired (idempotent
   reselection — mirrors the Language Dropdown FR-006 contract for
   consistency).
5. **Given** the awards list is long enough to overflow the visible menu
   surface,
   **When** the user opens the menu,
   **Then** the list is internally scrollable and both the **first** item
   and the **last** item are reachable. There is no pagination — the
   dropdown lists every award (TC_IOS_AWARD_DETAIL_FUN_008 +
   TC_IOS_AWARD_DETAIL_FUN_009; see § Out of Scope on pagination).

---

### User Story 3 - Bottom navigation between top-level destinations (Priority: P1)

Tapping a tab in the bottom navigation bar moves the user to the corresponding
top-level destination. The **Awards** tab is rendered in its **active** state
on this screen.

**Why this priority**: P1 because it is the user's only escape hatch back to
the rest of the app from this leaf screen (system back returns to Home but
does not jump to Profile, Kudos, etc.).

**Independent Test**: On Award Detail, tap each of the four tabs in turn and
verify the navigation outcome matches the contract (TC_IOS_AWARD_DETAIL_FUN_013
–FUN_015 + GUI_010).

**Acceptance Scenarios**:

1. **Given** the user is on Award Detail and the Awards tab is active,
   **When** the user taps **SAA 2025**,
   **Then** the app navigates to the Home screen (`OuH1BUTYT0`)
   (TC_IOS_AWARD_DETAIL_FUN_013).
2. **Given** the user is on Award Detail,
   **When** the user taps **Kudos**,
   **Then** the app navigates to the Sun\* Kudos hub
   (TC_IOS_AWARD_DETAIL_FUN_014).
3. **Given** the user is on Award Detail,
   **When** the user taps **Profile**,
   **Then** the app navigates to "Profile bản thân" (`hSH7L8doXB`)
   (TC_IOS_AWARD_DETAIL_FUN_015).
4. **Given** the user is on Award Detail with the Awards tab already active,
   **When** the user taps **Awards** again,
   **Then** the body scrolls back to top (mirrors Home's `SAA 2025` re-tap
   behaviour) and the active award is unchanged
   (Resolved Q2, 2026-05-11).
5. **Given** the user scrolls the body,
   **When** content moves vertically,
   **Then** the bottom navigation bar stays pinned at the bottom and the
   header stays pinned at the top — FR-014 + TC_IOS_AWARD_DETAIL_FUN_010
   + FUN_011.

---

### User Story 4 - Open the Sun*Kudos hub from the promo block (Priority: P1)

At the bottom of the scrollable body sits the Sun\* Kudos promo block
("ĐIỂM MỚI CỦA SAA 2025"). Tapping its **Chi tiết** button navigates to the
Sun\* Kudos hub screen.

**Why this priority**: This block is the **primary cross-promotion** between
the Awards system and the Kudos community feature — both are the two pillars
of SAA 2025. P1 because the Figma frame includes it as a first-class CTA.

**Independent Test**: Scroll to the bottom of Award Detail, tap **Chi tiết**
on the Sun\* Kudos block, verify the Sun\* Kudos hub opens
(TC_IOS_AWARD_DETAIL_FUN_012).

**Acceptance Scenarios**:

1. **Given** the user has scrolled to the Sun\* Kudos block,
   **When** the user taps the **Chi tiết** button,
   **Then** the app navigates to the **same destination** the bottom-nav
   **Kudos** tab opens — the Sun\* Kudos hub (`fO0Kt19sZZ` — `[iOS]
   Sun\*Kudos`). Both surfaces therefore funnel into one consistent landing
   page (Resolved Q3, 2026-05-11 — TC_IOS_AWARD_DETAIL_FUN_012 +
   FUN_014).
2. **Given** the Kudos banner image fails to load (offline, 404, slow),
   **When** the block renders,
   **Then** a placeholder image of the same dimensions renders in its place
   and the Chi tiết button remains functional
   (TC_IOS_AWARD_DETAIL_FUN_021).
3. **Given** the user double-taps Chi tiết within ~400 ms,
   **When** both taps are processed,
   **Then** exactly **one** navigation occurs (mirror the Home `Chi tiết` +
   FAB single-click suppression contract — TR-004 from Home spec).

---

### User Story 5 - Language switcher in the header (Priority: P2)

The header includes the shared language pill (VN / EN — JA was removed by the
Language Dropdown spec `uUvW6Qm1ve` § Resolved Q1). Tapping it opens an inline
dropdown anchored to the pill; selecting a language re-renders every
localized string on the screen within a single recomposition (no Activity
recreation).

**Why this priority**: P2 because it is **not the primary task** on this
screen (data display is) but it is the user's only way to switch locale while
on Award Detail without going back through Home or Login. Strictly necessary
for international users.

**Independent Test**: On a VN-rendered Award Detail, open the language pill,
select EN — verify every localized label (header search a11y, error states,
section labels, button copy) re-renders in English while API-sourced fields
(award name, description, quantity, prize) **do not change**
(TC_IOS_AWARD_DETAIL_FUN_018 + FUN_019).

**Acceptance Scenarios**:

1. **Given** the user is on Award Detail,
   **When** the user taps the language pill,
   **Then** the inline language dropdown opens anchored to the pill (no new
   route is pushed), listing exactly VN + EN per Language Dropdown FR-004
   (TC_IOS_AWARD_DETAIL_FUN_018).
2. **Given** the language is VN and the user selects EN,
   **When** the selection commits,
   **Then** every `stringResource()` caller on the screen re-renders in
   English in the same composition tree, and the persisted preference is
   updated through `LanguagePreferenceRepository`. API-sourced fields
   (name, description, quantity unit, prize value) follow whatever locale
   the backend returns for the active session — the **client MUST NOT**
   translate API strings on its own (TC_IOS_AWARD_DETAIL_FUN_019). The
   exact mechanism by which the backend decides which locale to serve is
   tracked in § Deferred Questions Q6 and is not in scope for this spec.
3. **Given** the language dropdown is open,
   **When** the user taps outside, system-back, or re-selects the active
   language,
   **Then** the menu dismisses without firing the locale-change side effect
   (mirrors Language Dropdown FR-008 + FR-006).

---

### User Story 6 - Open the Notifications surface from the bell (Priority: P2)

The bell icon in the header surfaces an unread-count badge indicator when the
user has unread notifications. Tapping it opens the Notifications screen / sheet.

**Why this priority**: P2 because Notifications is a peripheral concern on
Award Detail (the user is here to read an award, not check pings) but the
shared header contract demands it.

**Independent Test**: With `unreadNotificationCount > 0`, the bell shows a
badge; tap the bell and verify the Notifications surface opens
(TC_IOS_AWARD_DETAIL_GUI_002 + FUN_016).

**Acceptance Scenarios**:

1. **Given** the user has ≥ 1 unread notification,
   **When** the screen renders the header,
   **Then** the bell icon surfaces an unread badge indicator
   (TC_IOS_AWARD_DETAIL_GUI_002).
2. **Given** the user has zero unread notifications,
   **When** the screen renders the header,
   **Then** the bell icon shows **no badge** (TC_IOS_AWARD_DETAIL_GUI_003).
3. **Given** the bell is tapped,
   **When** the navigation handler runs,
   **Then** the Notifications screen (`_b68CBWKl5`) opens
   (TC_IOS_AWARD_DETAIL_FUN_016).

---

### User Story 7 - Open the Search surface from the magnifier (Priority: P2)

Tapping the magnifier icon in the header opens the Search overlay / screen.

**Why this priority**: P2 — same rationale as US6; secondary affordance the
shared header demands.

**Independent Test**: Tap the magnifier icon and verify the Search surface
opens (TC_IOS_AWARD_DETAIL_FUN_017).

**Acceptance Scenarios**:

1. **Given** the user is on Award Detail,
   **When** the magnifier icon is tapped,
   **Then** the Search surface opens.

---

### User Story 8 - Auth gate redirects unauthenticated users to Login (Priority: P1)

Award Detail is part of the **authenticated zone** of the app. A user without
a valid Supabase session who lands on this route (deep link, lost session
mid-flow, expired token) MUST be redirected to Login.

**Why this priority**: P1 — security boundary. Unauthenticated access would
leak the awards catalogue (which may contain unpublished categories) and
break the data-model invariant that the screen runs against a user identity.

**Independent Test**: Sign out, attempt to deep-link to Award Detail, verify
the user lands on Login instead (TC_IOS_AWARD_DETAIL_ACC_002).

**Acceptance Scenarios**:

1. **Given** there is no valid Supabase session,
   **When** the user navigates to Award Detail,
   **Then** the route immediately redirects to Login (`8HGlvYGJWq`); no
   network call is fired against `/awards/:id`.
2. **Given** the user's session expires while on Award Detail (401 returned
   from `/awards/:id`),
   **When** the response is received,
   **Then** the existing `AuthRedirectController` (introduced in Login Phase 4)
   navigates the user to Login and surfaces the localized "session-expired"
   snackbar — mirrors Home spec US4 and the Login spec's FR-014 contract
   (the spec is **8HGlvYGJWq**; not to be confused with FR-014 in **this**
   spec which is the sticky-chrome rule).

---

### Edge Cases

- **Empty awards list**: if `/awards` returns `[]` while the user opens the
  dropdown, the dropdown body MUST render a localized empty-state message
  (not a blank list).
- **Single award**: if `/awards` returns exactly 1 item, the dropdown still
  opens but the list has one row; selecting it is idempotent.
- **Network slow → fast race**: if the user selects category B before
  category A's request completes, the in-flight A request MUST be
  discarded (no late writes to UI state) so the body always reflects the
  most recent selection.
- **System back from this screen**: returns to Home (no in-screen back state).
  This is a **leaf screen** in the back stack.
- **Predictive back gesture while dropdown is open**: dismisses the
  dropdown without popping the screen (mirror Language Dropdown FR-008).
- **Process death + restore**: per Resolved Q1, the in-session
  last-viewed `activeAwardId` is **not** persisted across cold launches.
  If the OS restores a saved navigation state pointing at Award Detail
  after process death (e.g. via the system back stack), the screen MUST
  fall back to FR-001's default landing (first award by `sort_order`) and
  re-fetch from `/awards/:id` — no stale data from the in-memory store.
- **Image cache eviction**: badge image and Kudos banner image MUST be
  fetched through the project's existing Coil pipeline; an evicted image
  re-fetches transparently.
- **Long description**: descriptions of arbitrary length MUST flow naturally
  within the body's scroll container; no truncation, no "Read more" CTA per
  the design.

---

## UI/UX Requirements *(from Figma)*

### Screen Components

Pixel-level chrome (colors, sizes, fonts, spacing, badges, dividers) is
intentionally **not** enumerated here per Constitution Principle II + the
MoMorph contract: implementer fetches those values at task-execution time via
`query_section` / `get_node` for the Node IDs below. This spec captures
**behavior** only.

| Component | Node ID | Description | Interactions |
|-----------|---------|-------------|--------------|
| **Header — root** (`mms_1_header`) | `6885:10264` | Sticky top chrome containing the logo (left), and the action cluster (right). Reused from Home + Login. | Stays pinned to the top throughout vertical scroll (FR-014). |
| **Logo** (`mms_1.1_mm_media_logo`) | `I6885:10264;88:1827` | "Sun\* Annual Awards 2025" branded mark. | Static; not interactive (TC_IOS_AWARD_DETAIL_GUI_002). |
| **Header actions** (`mms_1.2_actions`) | `I6885:10264;88:1828` | Cluster: language pill + search + bell-with-badge. | See US5, US6, US7. |
| **KV Kudos banner** (`mms_A_KV Kudos`) | `6885:10266` | Static banner: "Hệ thống ghi nhận và cảm ơn" + KUDOS lockup. | Static; no behavior. |
| **Highlight block** (`mms_B_Highlight`) | `6885:10283` | Container for the section sub-label, title, and category dropdown. | Container only. |
| **Highlight header** (`mms_B.1_header`) | `6885:10284` | Sub-label "Sun\* Annual Awards 2025" + title "Hệ thống giải thưởng SAA 2025" + the dropdown trigger. | The dropdown trigger is the **only** interactive element in this block — see US2. |
| **Award info block** (`mms_2.3_award`) | `6885:10292` | Body of the currently-selected award: badge image, title row, description, recipient count block, prize value block. | Display-only (US1); contents replace on dropdown selection (US2). |
| **Award title** (`mms_C2.1.2_Top Talent`) | `6885:10297` | Award name with leading icon. | Static text; sourced from API `award.name`. |
| **Award description** | `6885:10298` | Multi-paragraph descriptive copy. | Static text; sourced from API `award.description`. |
| **Recipient count label** | `6885:10303` | Label "Số lượng giải thưởng". | Static localized label. |
| **Recipient count value** | `6885:10305` | Numeric count + unit (e.g. "10 Cá nhân"). | Sourced from `award.quantity` + `award.quantity_unit`. |
| **Prize value label** | `6885:10306` | Label "Giá trị giải thưởng". | Static localized label. |
| **Prize value amount** | `6885:10311` | "7.000.000 VNĐ" + caption "cho mỗi giải thưởng". | Sourced from `award.prize_value`. |
| **Award badge image** | `6885:10313` / `6885:10314` | Badge graphic for the active award (e.g., "TOP TALENT" lockup). | Display-only; null `image_url` falls back to a placeholder (FR-008 / TC_IOS_AWARD_DETAIL_FUN_004). |
| **Sun\*Kudos promo block** (`mms_2.4_kudos`) | `6885:10315` | Label "Phong trào ghi nhận" + title "Sun\* Kudos" + Kudos banner image + "ĐIỂM MỚI CỦA SAA 2025" badge + descriptive copy + **Chi tiết** button. | Tap **Chi tiết** → Sun\* Kudos hub (US4). |
| **Bottom navigation bar** (`mms_3_nav bar`) | `6885:10332` | Four tabs: SAA 2025 (icon: home), Awards (icon: medal — **active** on this screen), Kudos (icon: badge), Profile (icon: user). | Stays pinned to the bottom throughout vertical scroll (FR-014); see US3. |

### Navigation Flow

- **From**:
  - Primary: **Home** (`OuH1BUTYT0`) → tap **Chi tiết** on any award card in
    the carousel (Home spec US2). The tapped card's `awardId` is passed as a
    route argument.
  - Secondary: bottom nav **Awards** tab from any authenticated screen —
    opens Award Detail scoped to the **last-viewed** award (in-session
    restore) or the **first award by `sort_order`** on the first session
    visit. See FR-001 + Resolved Q1.
  - Deep link from notifications / external (TBD if applicable).
- **To**:
  - **Home** (`OuH1BUTYT0`) — system back, or bottom-nav SAA 2025 tab.
  - **Sun\* Kudos hub** (`fO0Kt19sZZ` — `[iOS] Sun*Kudos`) — bottom-nav
    Kudos tab, OR the Chi tiết CTA in the Sun\* Kudos block (both routes
    funnel into the same destination, per Resolved Q3).
  - **Profile bản thân** (`hSH7L8doXB`) — bottom-nav Profile tab.
  - **Notifications** (`_b68CBWKl5`) — header bell icon.
  - **Search** (screenId TBD; reuses Home's search route) — header search
    icon.
  - **Login** (`8HGlvYGJWq`) — auth gate redirect (US8) or session-expired
    bounce.
- **Triggers**: all interactive elements above; see component table.

### Visual Requirements

- **Sticky chrome**: header pinned to top, bottom nav pinned to bottom
  throughout vertical scroll (FUN_010 + FUN_011).
- **Active tab indicator**: the **Awards** tab in the bottom nav renders in
  its active visual state on this screen (TC_IOS_AWARD_DETAIL_GUI_010).
- **Animations / transitions**: defer to Material 3 defaults for the
  category dropdown open/close, badge image fade-in, and tab transitions —
  no custom motion is specified by Figma.
- **Accessibility**: WCAG AA — see TR-003 below. Pixel values are out of
  scope here.

---

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST navigate to this screen scoped to a specific
  `awardId` (carried in the route arguments). When the user enters from the
  bottom-nav **Awards** tab AFTER having previously viewed an award in the
  current session, the system MUST restore the **last-viewed `activeAwardId`**
  (Resolved Q1, 2026-05-11). On the user's **first** session visit (no prior
  selection in-memory), the system MUST default to the first award returned
  by `/awards` ordered by the same `sort_order` field used by the Home
  carousel. The last-viewed selection lives in the `AwardDetailViewModel`'s
  `SavedStateHandle` and is **session-scoped** — it does NOT need to survive
  process death.
- **FR-002**: System MUST fetch the award payload via `GET /awards/:id` on
  screen mount and on every dropdown selection that differs from the active
  award (US1 + US2). Identical reselections MUST NOT fire a network call
  (US2 idempotent reselection).
- **FR-003**: While the fetch is in flight, the body region MUST render a
  loading indicator and the header + bottom nav MUST remain interactive
  (TC_IOS_AWARD_DETAIL_FUN_002).
- **FR-004**: On API failure the body region MUST render a localized error
  message + **Retry** button; tapping Retry re-issues the request. The raw
  exception/stack trace MUST NOT be surfaced (TR-002 + constitution § OWASP).
- **FR-005**: System MUST list every award returned by `GET /awards` in the
  category dropdown (no client-side hardcoding), with the active award
  marked in a visually distinct selected state
  (TC_IOS_AWARD_DETAIL_FUN_005). Re-selecting the active row MUST close the
  menu without re-fetching.
- **FR-006**: The category dropdown MUST dismiss on outside tap, on system
  back, AND on a second tap on its trigger (mirror Language Dropdown FR-008).
- **FR-007**: Switching the category MUST replace the full body (badge image,
  title, description, quantity, prize) without re-rendering the header or
  bottom nav (TC_IOS_AWARD_DETAIL_FUN_006).
- **FR-008**: System MUST render placeholders without crashing for
  `award.image_url`, `award.quantity`, and `award.prize_value` `null`
  (TC_IOS_AWARD_DETAIL_FUN_004 + FUN_020).
- **FR-009**: Tapping the Sun\*Kudos block's Chi tiết button MUST navigate to
  the Sun\*Kudos hub (US4). Double-tap MUST yield exactly one navigation.
- **FR-010**: Bottom navigation tab taps MUST route to the destinations in
  US3 acceptance scenarios; the **Awards** tab MUST render in its active
  state while this screen is composed.
- **FR-011**: Header chrome — language pill, search icon, bell icon — MUST
  reuse the shared components from Login + Home so behavior is consistent
  (US5, US6, US7). The bell badge MUST reflect the live unread count from the
  same Notifications repository used on Home.
- **FR-012**: Unauthenticated access (no session OR 401 mid-flow) MUST route
  to Login through the existing `AuthRedirectController` (US8 + Home FR-014
  / Login Phase 4 carry-over).
- **FR-013**: System back from this screen returns to the previous screen in
  the back stack (typically Home). While the category dropdown is open,
  system back MUST close the dropdown only — it MUST NOT pop the screen.
- **FR-014**: The Header (`mms_1_header`) MUST remain pinned to the top of the
  viewport and the bottom navigation bar (`mms_3_nav bar`) MUST remain pinned
  to the bottom throughout the body's vertical scroll
  (TC_IOS_AWARD_DETAIL_FUN_010 + FUN_011). Sticky chrome MUST stay accessible
  via TalkBack and keyboard at any scroll offset.

### Technical Requirements

- **TR-001 (Performance)**: First content paint MUST happen within
  **≤ 800 ms** of screen mount on a warm cache and **≤ 2 s** on cold cache
  over a normal Sun\* office Wi-Fi connection. Dropdown selection → body
  refresh MUST complete within **≤ 1.5 s** on a normal connection.
- **TR-002 (Security)**: Award payload is read-only, **not embargoed**, and
  not personally identifying (`quantity` + `prize_value` ship visible from
  day one — Resolved Q4, 2026-05-11). Supabase RLS on the `awards` table
  MUST enforce that only authenticated users can `SELECT` (admin / writer
  policies are out of scope of this screen). **Never** ship the service-role
  key in the app (constitution § II). All log writes MUST be passed
  through the existing `SecureTimberTree` scrub from Home Phase 10 —
  payload fields MUST NOT leak into logs.
- **TR-003 (Accessibility)**: Every interactive control MUST be reachable via
  TalkBack with a localized `contentDescription` and have a touch target of
  ≥ 48 × 48 dp (Constitution Principle III). The category dropdown's
  trigger MUST surface `Role.Button` + `stateDescription` ("expanded" /
  "collapsed"), and opening the menu MUST move TalkBack focus to the first
  row — mirror Language Dropdown TR-003. Predictive-back MUST dismiss the
  dropdown without popping the screen.
- **TR-004 (Single-click suppression)**: Award dropdown row tap, dropdown
  trigger, error-state **Retry** button, Sun\*Kudos block **Chi tiết**
  button, header bell, header search, header language pill, and each
  bottom-nav tab MUST all be wrapped in `rememberSingleClickHandler`
  (from `com.example.aiddproject.core.ui`) so a finger-bounce double-tap
  can never push two navigations / locale changes / retries through the
  system. Inherited from Home TR-005 / Login TR-004.
- **TR-005 (Configuration changes)**: Rotating the device or recreating the
  activity MUST NOT lose the current `awardId`, scroll position, or
  dropdown-open state. Use `rememberSaveable` + `ViewModel.SavedStateHandle`
  per Constitution Principle II.
- **TR-006 (Locale change without recreation)**: Switching language MUST
  re-render every `stringResource()` caller within the same composition tree
  — no Activity recreation, no white flash. Mirror Login SC-004 + Language
  Dropdown TR-001.
- **TR-007 (Image-load resilience)**: Badge image (`award.image_url`) and
  the Kudos banner image MUST fall back to a placeholder on null / load
  failure and MUST NOT crash the screen (FR-008 + TC_IOS_AWARD_DETAIL_FUN_021).
  Reuse the project's existing Coil pipeline with the placeholder drawable
  defined in `core/ui/placeholders`.

### Key Entities

- **Award**: a single award category in the SAA 2025 catalogue.
  Attributes (all sourced from the `awards` table — see § Deferred Questions
  for the schema confirmation path):
  - `id` (UUID or similar, PK) — required, primary lookup key.
  - `name` (String, localized: VN authoritative, EN optional) — required,
    e.g. "Top Talent".
  - `description` (String, localized) — required, multi-paragraph.
  - `quantity` (Int, nullable) — number of recipients, e.g. `10`.
  - `quantity_unit` (String, localized, nullable) — e.g. "Cá nhân".
  - `prize_value` (**String, nullable, pre-formatted** for display — e.g.
    `"7.000.000 VNĐ"`). The client renders this verbatim with no
    locale-aware number formatting (Resolved Q5, 2026-05-11). Backend
    owns localized formatting; the implementer MUST NOT apply
    `NumberFormat`/`DecimalFormat` to this field.
  - `image_url` (String URL, nullable) — badge graphic for the award.
  - `sort_order` (Int) — same ordering used by Home carousel; drives both
    list order and the dropdown order.

- **AwardListItem** (for the dropdown): minimal projection containing
  `{ id, name, sort_order }` — used by the dropdown to list categories
  without loading the full description / image for each row.

---

## API Dependencies

| Endpoint | Method | Purpose | Status | Triggered by |
|----------|--------|---------|--------|--------------|
| `/awards` | `GET` | List all award categories for the dropdown (returns `AwardListItem[]` ordered by `sort_order`). Reused from Home's awards carousel. | **Exists** (per Home spec; pinned screen ID `OuH1BUTYT0`). | US2 — dropdown open. |
| `/awards/:id` | `GET` | Fetch the full payload for a single award (full `Award` model). | **Predicted new** — Home only uses the list endpoint; this screen needs the detail. **The implementer MUST confirm with backend whether `/awards/:id` exists or whether the detail data is embedded in `/awards` list items.** | US1 mount + US2 selection. |
| `/notifications/unread-count` | `GET` | Unread badge count for the header bell. Reused from Home. | **Exists** (per Home spec US6). | US6 — header bell badge. |

**Open API question**: it may be that the `/awards` list endpoint already
returns the full `Award` payload (name + description + quantity + prize +
image_url) for every row, in which case the screen does NOT need a separate
`/awards/:id` call — it just picks the right row from the cached list by
`id`. The test cases (FUN_001 referencing `/awards/:id`) suggest a dedicated
endpoint exists; implementer to confirm before wiring.

---

## State Management

- **Screen-level UI state** (ViewModel `StateFlow<AwardDetailUiState>`).
  Naming follows the project pattern established by Home (see
  `AwardsState`, `KudosState` in `home/domain/states/`) — domain-specific
  sealed interfaces with `Loading | Populated | Empty | Error` cases, not a
  generic `Resource<T>`:
  - `activeAwardId: String` — current selection, restored via
    `SavedStateHandle`.
  - `detail: AwardDetailState` — sealed interface
    `Loading | Loaded(Award) | Error(messageRes: Int)` for the body region.
  - `categories: AwardListState` — sealed interface
    `Loading | Populated(List<AwardListItem>) | Empty | Error(messageRes: Int)`
    for the dropdown. MAY reuse the existing `AwardsState` from Home if
    its shape already covers `List<AwardListItem>`.
  - `isDropdownOpen: Boolean` — local UI flag, NOT persisted to
    `SavedStateHandle` (transient).
- **Repository contract** (`AwardsRepository` — extends Home's):
  - `fun observeAwards(): Flow<AwardListState>` — long-lived flow used by
    both Home carousel and this dropdown (already exists in Home spec).
  - `suspend fun loadAward(id: String): AwardDetailState` — single-shot
    fetch for this screen. **NEW** — Home only consumes the list; this
    screen needs detail. Implementer to add to the existing repository.
- **Cache / invalidation**:
  - `observeAwards()` SHOULD be cached at the repository level for the
    duration of the session — categories rarely change during SAA 2025.
  - `loadAward(id)` SHOULD be cached per-`id` with a short TTL (e.g. 5 min)
    so repeated round-trips through the dropdown don't re-hit the network.
  - On a 401 from either endpoint, the cache MUST be invalidated and the
    `AuthRedirectController` engaged (FR-012).
- **Optimistic updates**: none — this screen is read-only.
- **Out-of-order responses**: dropdown selection cancels any in-flight
  detail request before issuing the new one (edge case "Network slow → fast
  race").

---

## Success Criteria *(mandatory)*

- **SC-001**: 95% of authenticated users tapping "Chi tiết" on a Home
  awards carousel card see the body fully populated within 2 s on
  cellular 4G (TR-001 budget).
- **SC-002**: Dropdown category switches complete in < 1.5 s on a normal
  connection in 95% of sessions, with **zero** observed UI tear (header /
  bottom nav re-render).
- **SC-003**: No crash report tagged to this screen for `image_url`,
  `quantity`, or `prize_value` `null` payloads in production (FR-008).
- **SC-004**: Locale switch on this screen completes in a single
  recomposition with no Activity recreation (TR-006).
- **SC-005**: Zero raw exception strings surface to the user in error
  states (TR-002 + FR-004).

---

## Out of Scope

- **Award creation / editing**: this is a read-only viewer. Admin authoring
  surfaces (if any) live in a separate admin frame (see "Admin - Overview",
  `9ja9g9iJLW`).
- **Sharing / deep-linking outwards**: no "share this award" CTA.
- **Award favourites / bookmarks**: not in Figma.
- **Past-edition awards**: this screen surfaces SAA **2025** awards only.
  An "edition picker" is not in scope.
- **Pagination of the dropdown**: with only ~5 award categories the
  dropdown lists every row; if the catalogue grows the dropdown can scroll
  but virtualization is not required.
- **Visual specs**: colours, sizes, fonts, spacing, dividers, badges,
  shadows are intentionally out of this spec per Constitution Principle II
  and the MoMorph contract. Implementer fetches those values at
  task-execution time via `query_section` / `get_node` for the Node IDs
  enumerated above.

---

## Dependencies

- [x] Constitution document exists (`.momorph/constitution.md`)
- [ ] **API specifications**: `.momorph/contexts/api-docs.yaml` is
  **MISSING** at the project level. The `/awards` + `/awards/:id` contracts
  documented here are predictions; implementer MUST confirm the OpenAPI
  schema with the backend team before wiring (or author it as a follow-on
  via `/momorph.apidocs`).
- [ ] **Database design**: `.momorph/contexts/database-schema.sql` is
  **MISSING** at the project level. The `awards` table schema is inferred
  from the test cases — implementer MUST cross-reference with the actual
  Supabase migration before relying on column names.
- [ ] **Backend API test cases**: `.momorph/contexts/BACKEND_API_TESTCASES.md`
  is **MISSING** at the project level. The frontend test cases here cover
  the UI contract; backend contract tests are a follow-on.
- [ ] **Screen flow**: `.momorph/SCREENFLOW.md` is **MISSING** at the
  project level. The "From" / "To" routes documented in § Navigation Flow
  describe this screen's edges and can be folded into a SCREENFLOW graph
  when produced.
- [x] **Existing component library**: `LanguageSelector` (shared from Login
  + Home, lives in `com.example.aiddproject.core.locale.ui` since
  uUvW6Qm1ve Phase 2), `AwardCard` + `AwardsRepository` (from Home spec
  US2), `NotificationsBell` (from Home spec US6),
  `AuthRedirectController` (from Login Phase 4 / Home Phase 6),
  `rememberSingleClickHandler` (from `core/ui`).
- [x] **Home spec** (`.momorph/specs/OuH1BUTYT0-iOS-Home/spec.md`) — the
  entry point for US1 and the source of the awards repository contract.
- [x] **Language Dropdown spec**
  (`.momorph/specs/uUvW6Qm1ve-iOS-Language-dropdown/spec.md`) — the source
  of the inline language switcher used by US5.

---

## Notes

### Frame revision pin

The MoMorph frame revision was `7bf7991370f37cf86b5b5f94283587df` at spec
time. Re-run `/momorph.specify` and diff this spec if the design revision
changes.

### Frame name vs. screen name

The Figma frame is labelled "[iOS] Award\_Top talent" because the design
canvas shows the Top Talent payload. The **screen** rendered by this spec is
parametric (US2) — it is the generic **Award Detail** screen that defaults to
Top Talent at first mount when navigated to from the bottom nav without a
selection (FR-001). When naming the implementation route / ViewModel, prefer
`AwardDetail` over `TopTalent` so the abstraction is not tied to one
category.

### Mapping to existing test cases (page_name `[iOS] Award_Top talent`)

| Test case area | Range | Mapped user story / requirement |
|----------------|-------|-------------------------------|
| `ACC_001..002` | Access control | US1 entry path; US8 auth gate. |
| `GUI_001..011` | UI layout & display | All US — implementer uses for visual fidelity per Constitution Principle II (pixel values via `query_section`). |
| `FUN_001..004` | Data load + states | US1 (FR-002, FR-003, FR-004, FR-008). |
| `FUN_005..009` | Category dropdown | US2 (FR-005, FR-006, FR-007). |
| `FUN_010..011` | Sticky chrome | US3 acceptance scenario 5 + FR-014. |
| `FUN_012` | Sun\*Kudos Chi tiết | US4 (FR-009). |
| `FUN_013..015` | Bottom nav | US3 (FR-010). |
| `FUN_016..017` | Header bell / search | US6, US7 (FR-011). |
| `FUN_018..019` | Header language pill | US5 (FR-011 + TR-006). |
| `FUN_020` | Null `quantity` / `prize_value` | US1 acceptance scenario 5 (FR-008). |
| `FUN_021` | Kudos banner image fail | US4 acceptance scenario 2 (TR-007). |

### Resolved Questions (2026-05-11 review)

| # | Question | Resolution |
|---|----------|------------|
| Q1 | Bottom-nav **Awards** tab entry — what default award? | **Last-viewed in-session** restoration (session-scoped, not across cold launches); fallback to **first by `sort_order`** on first-ever entry. Codified in FR-001. |
| Q2 | Re-tap **Awards** while already on this screen? | **Scroll body to top**, active award unchanged. Codified in US3 acceptance scenario 4. |
| Q3 | Sun\*Kudos block **Chi tiết** destination? | The **same screen** the bottom-nav **Kudos** tab opens — Sun\* Kudos hub (`fO0Kt19sZZ` — `[iOS] Sun*Kudos`). Codified in US4 acceptance scenario 1 + § Navigation Flow. |
| Q4 | Are `quantity` / `prize_value` **embargoed** pre-ceremony? | **No** — visible from day one. TR-002 no longer prescribes server-side masking. |
| Q5 | `prize_value` storage shape? | **Pre-formatted String** (e.g. `"7.000.000 VNĐ"`), rendered verbatim by the client. No client-side currency formatting. Codified in § Key Entities. |

### Deferred Questions (resolved at implementation time, NOT blockers for ratification)

| # | Question | Where it's flagged in this spec | Resolved by |
|---|----------|--------------------------------|-------------|
| Q6 | Localization model — per-locale columns vs. `?locale=` query param? | US5 acceptance scenario 2; § Key Entities note on `name` / `description` / `quantity_unit`. | Backend / DB confirmation at task-execution time. The spec describes both paths so the implementer can wire whichever exists without re-opening the spec. |
| Q7 | `/awards/:id` dedicated endpoint vs. payload embedded in `/awards` list rows? | § API Dependencies "Open API question"; FR-002. | Backend confirmation at task-execution time. State Management § Repository contract names a `loadAward(id)` method; if the list already carries the full payload the implementer SHOULD implement that method as a local lookup against the cached `observeAwards()` flow instead of a second network call. |
| Q8 | Should the existing `AwardsRepository` be extended with `loadAward(id)`, or should a new repository be added? | § State Management Repository contract notes "Implementer to add to the existing repository" with a fallback to whichever shape ships. | Implementer's discretion (Resolved Q8, 2026-05-11). |

### Implementation hints (informational, NOT requirements)

- Reuse `HomeScaffold` / `HomeHeader` if the chrome contract is already
  generalized in `home/ui/components`; if not, lift it into
  `core/chrome/ui` so this screen + Home + future authenticated chrome
  screens share one implementation. Avoid copy-paste.
- The Kudos block's "Chi tiết" button MAY be a literal reuse of the Home
  Kudos section's Chi tiết button (`KudosSection.kt`,
  `TEST_TAG_KUDOS_CHI_TIET`) given they navigate to the same destination.
- Single-click suppression is **mandatory** (TR-004); follow the pattern
  already established in `HomeFab.kt`, `KudosSection.kt`, and
  `LanguageSelector.kt`.
