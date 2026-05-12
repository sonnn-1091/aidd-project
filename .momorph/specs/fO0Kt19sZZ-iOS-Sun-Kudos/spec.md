# Feature Specification: Sun*Kudos hub

**Frame ID**: `fO0Kt19sZZ`
**Frame Name**: `[iOS] Sun*Kudos`
**File Key**: `9ypp4enmFmdK3YAFJLIu6C`
**Created**: 2026-05-12
**Status**: Ratified (review pass 2026-05-12)

---

## Overview

**Feature**: Sun*Kudos hub — the main recognition feed for the SAA
2025 mobile experience.

**Purpose**: Surface the company's recognition activity in one
scrollable hub where any authenticated Sunner can:

1. See the most-liked recent Kudos (Highlight carousel),
2. Browse the full Kudos feed (All Kudos),
3. Compose a new Kudos via the prominent Send-Kudos CTA,
4. Track their own giving/receiving stats + unopened Secret Boxes,
5. Explore the recognition graph (Spotlight Board),
6. See who recently received SAA rewards (Top 10 gift recipients).

**Target users**: Authenticated Sunners (Sun* employees) on the
mobile app — same audience as Home and Award Detail. Anonymous /
expired-session users are redirected to Login (same gate as the rest
of the authenticated tab tree).

**Business context**: Kudos drives the bottom-up recognition culture
that the Awards system (`c-QM3_zjkG`) celebrates at year-end. The
hub is the most-trafficked screen during SAA 2025 because every
heart given counts toward award eligibility. Performance + correct
"hearts on special day" doubling are P1 because they directly
influence award-recipient selection.

---

## User Scenarios

### US1: Render Sun*Kudos hub for authenticated Sunner [P1]

**As a** authenticated Sunner
**I want to** open Sun*Kudos and see the full hub layout
**So that** I can browse recognition activity end-to-end without
hunting for sections

#### Acceptance Scenarios

**Scenario 1 — Happy path with data**
- **Given** the user is signed in AND the database has Kudos +
  user-stats + spotlight + gift-recipients data,
- **When** the user navigates to Sun*Kudos from any entry point,
- **Then** the screen renders top-to-bottom:
  - KV Kudos hero banner with "Hệ thống ghi nhận và cảm ơn" + logo
  - **Send Kudos** CTA pill with placeholder "Hôm nay, bạn muốn gửi
    kudos đến ai?"
  - **Highlight** section header "HIGHLIGHT KUDOS" + Hashtag filter
    + Phòng ban filter + 5-card carousel + page-indicator
  - **Spotlight Board** with total-kudos count + pan/zoom canvas +
    Sunner search input
  - **All Kudos** section header + personal statistics tiles + Open
    Secret Box CTA + Top 10 latest gift recipients + Kudos feed +
    "View all Kudos →" link
- **And** the screen scrolls smoothly as one LazyColumn (matching
  TC_IOS_KUDOS_GUI_002).

**Scenario 2 — Empty system (no Kudos yet)**
- **Given** the system is fresh / pre-launch,
- **When** the user lands on the hub,
- **Then** Highlight Kudos shows "Hiện tại chưa có Kudos nào.";
  All Kudos shows the same string; Top 10 Gift Recipients shows
  "Chưa có dữ liệu"; Spotlight shows its empty-state message.
  (TC_IOS_KUDOS_GUI_001 + TC_IOS_KUDOS_FUN_002 + TC_IOS_KUDOS_FUN_003.)

**Scenario 3 — Loading**
- **Given** the user opens the hub while data is still in flight,
- **When** Highlight + All Kudos + Spotlight are still resolving,
- **Then** each section renders its own loading indicator
  independently so chrome (header, CTA) stays interactive.

**Scenario 4 — Error on any section**
- **Given** one section's API call fails,
- **When** the data state resolves to error,
- **Then** that section shows a localized inline error message
  + a Retry control. Other sections render normally.

**Scenario 5 — Pull-to-refresh (Q-K-2 contract)**
- **Given** the hub has loaded any combination of sections,
- **When** the user performs a pull-down gesture from the top of
  the body,
- **Then** every section refetches in parallel — Highlight + All
  Kudos + Spotlight (graph + total counter) + personal stats + Top
  10. The refresh indicator dismisses when all sections resolve;
  individual section failures still show their own inline error
  states without blocking the others.

### US2: Auth gate (redirect when unauthenticated/expired) [P1]

**As a** unauthenticated user
**I want to** be redirected to Login when I try to open Sun*Kudos
**So that** server-side RLS isn't the only barrier — the client also
short-circuits cleanly

#### Acceptance Scenarios

**Scenario 1 — Unauthenticated user (TC_IOS_KUDOS_ACC_001)**
- **Given** the user has no auth session,
- **When** they navigate to Sun*Kudos,
- **Then** the navigation is intercepted and the Login screen
  (`8HGlvYGJWq`) is shown.

**Scenario 2 — Expired session mid-session (TC_IOS_KUDOS_ACC_003)**
- **Given** the user's Supabase session token is no longer valid,
- **When** any API call from this screen returns 401,
- **Then** the existing `AuthRedirectController` pipeline routes to
  Login (same contract as Home + Award Detail).

### US3: Filter Highlight + All Kudos by Hashtag and/or Department [P1]

**As a** Sunner
**I want to** narrow the feeds by hashtag or department or both
**So that** I can find recognition relevant to a specific team /
theme

#### Acceptance Scenarios

**Scenario 1 — Single Hashtag (TC_IOS_KUDOS_FUN_020/021)**
- **Given** the screen is loaded with no filters applied,
- **When** the user taps the Hashtag dropdown and selects
  `#teamwork`,
- **Then** the bottom sheet closes; the Hashtag button shows
  `#teamwork`; both Highlight carousel and All Kudos feed re-fetch
  filtered to that hashtag.

**Scenario 2 — Combined Hashtag + Department (TC_IOS_KUDOS_FUN_004)**
- **Given** the user has selected `#teamwork`,
- **When** the user taps Phòng ban and selects `Division A`,
- **Then** the feeds re-render with **AND** logic — only Kudos that
  have `#teamwork` **and** belong to Division A.

**Scenario 3 — Filter resets carousel position (TC_IOS_KUDOS_FUN_005)**
- **Given** the user has swiped the carousel to card 3,
- **When** any filter changes,
- **Then** the carousel re-fetches and resets to card 1 with the
  page indicator at "1/5".

**Scenario 4 — Hashtag chip tap (TC_IOS_KUDOS_FUN_016/031)**
- **Given** a Kudos card displays a hashtag chip,
- **When** the user taps the chip,
- **Then** the Hashtag filter is set to that chip's value AND both
  Highlight + All Kudos refresh; the page is NOT navigated.

**Scenario 5 — Empty result after filter (TC_IOS_KUDOS_FUN_002)**
- **Given** the chosen filter combination yields zero Kudos,
- **When** the feeds re-render,
- **Then** both sections show "Hiện tại chưa có Kudos nào."

### US4: Highlight carousel — top 5 by heart count [P1]

**As a** Sunner
**I want to** swipe through the most-loved recent Kudos
**So that** I see what's being celebrated right now

#### Acceptance Scenarios

**Scenario 1 — Order (TC_IOS_KUDOS_FUN_001)**
- **Given** the system has more than 5 Kudos with varying heart
  counts,
- **When** the carousel renders,
- **Then** it shows exactly 5 cards sorted by heartCount DESC.

**Scenario 2 — Swipe (TC_IOS_KUDOS_FUN_019)**
- **Given** the carousel has at least 2 cards,
- **When** the user swipes left/right,
- **Then** the carousel advances/retreats one card and the page
  indicator updates ("1/5" → "2/5", etc.).

**Scenario 3 — Active vs faded (TC_IOS_KUDOS_FUN_038)**
- **Given** the carousel renders 3+ cards,
- **When** the user observes any non-center card,
- **Then** side cards are visually de-emphasized (faded). After
  swiping a card to center, its emphasized state becomes the
  active one.

### US5: Like / unlike a Kudos with correct heart accounting [P1]

**As a** Sunner
**I want to** show appreciation for a Kudos with a heart
**So that** the sender knows their recognition resonated

#### Acceptance Scenarios

**Scenario 1 — Toggle like (TC_IOS_KUDOS_FUN_015/018/037)**
- **Given** the user has not liked a Kudos card (heart icon grey),
- **When** they tap the heart,
- **Then** the icon flips to red (active), the displayed heart count
  increments by 1 (or 2 on a special day — see Scenario 4), and the
  reaction is persisted via `POST /api/v1/kudos/{kudosId}/reactions`.

**Scenario 2 — Unlike (TC_IOS_KUDOS_FUN_009/015)**
- **Given** the user has previously liked a Kudos,
- **When** they tap the heart again,
- **Then** the icon flips back to grey, the count decrements by the
  same amount that was added (1 or 2), and the reaction is removed
  via `DELETE /api/v1/kudos/{kudosId}/reactions`.

**Scenario 3 — Sender OR recipient cannot like own Kudos
(TC_IOS_KUDOS_FUN_008 extended per Q-K-5)**
- **Given** the displayed Kudos was sent by OR received by the
  current user (`current_user.id == kudos.sender_id` OR
  `current_user.id == kudos.recipient_id`),
- **When** they observe the card,
- **Then** the heart icon is disabled (not tappable) and no
  reaction request is fired on tap. Server MUST enforce the same
  rule at the API level (client-side disable is a UX hint only).

**Scenario 4 — Special-day x2 (TC_IOS_KUDOS_FUN_010)**
- **Given** the server marks today as a special day,
- **When** a user likes a Kudos,
- **Then** the heart count increments by 2 (and unlike decrements
  by 2 — TC_IOS_KUDOS_FUN_009 covers symmetric withdrawal).

**Scenario 5 — One like per user per kudos (TC_IOS_KUDOS_FUN_007)**
- **Given** the user has already liked a Kudos earlier in the
  session AND navigated away and back,
- **When** they observe the card,
- **Then** the heart remains red and the persisted state hydrates
  from `GET /api/v1/kudos` (the response includes
  `liked_by_current_user: true`).

**Scenario 6 — Star tier badge (TC_IOS_KUDOS_FUN_006)**
- **Given** the recipient of a Kudos has received N total Kudos,
- **When** the card renders the recipient info,
- **Then** their star-tier badge follows a **threshold-tier** rule:
  - **N < 10** → no badge
  - **10 ≤ N < 20** → 1 star
  - **20 ≤ N < 50** → 2 stars
  - **N ≥ 50** → 3 stars
  The thresholds are server-side properties of the recipient's
  Kudos count; the client renders the badge tier verbatim from a
  `recipient.star_tier: 0 | 1 | 2 | 3` field on the kudos payload
  (no client-side bucket math — prevents drift if the thresholds
  change product-side).

### US6: Open Send Kudos compose flow [P1]

**As a** Sunner
**I want to** tap the Send-Kudos CTA and start composing
**So that** the entry point is one tap away on the hub

#### Acceptance Scenarios

**Scenario 1 — Tap CTA (TC_IOS_KUDOS_FUN_026)**
- **Given** the screen is loaded,
- **When** the user taps the Send Kudos pill at the top of the body,
- **Then** navigation pushes to `[iOS] Sun*Kudos_Gửi lời chúc Kudos`
  (compose flow). The hub state is preserved on back.

### US7: View Kudos detail [P1]

**As a** Sunner
**I want to** open a Kudos card to read the full message
**So that** truncated cards (3 or 5 lines) don't hide context

#### Acceptance Scenarios

**Scenario 1 — Tap Highlight card "Xem chi tiết"
(TC_IOS_KUDOS_ACC_004)**
- **Given** at least one Highlight card is shown,
- **When** the user taps the "Xem chi tiết" CTA on the center card,
- **Then** navigation pushes to the Kudos detail screen
  (`[iOS] Sun*Kudos_View kudo` or `_View kudo ẩn danh` depending on
  `kudos.is_anonymous`).

**Scenario 2 — Tap All Kudos card body (TC_IOS_KUDOS_ACC_005)**
- **Given** at least one All Kudos card is visible,
- **When** the user taps anywhere on the card's content area,
- **Then** the same detail screen opens.

### US8: Open Sender / Recipient profile from any card [P2]

**As a** Sunner
**I want to** tap an avatar or name to open that Sunner's profile
**So that** I can see their full Kudos history

#### Acceptance Scenarios

**Scenario 1 — Highlight card avatars (TC_IOS_KUDOS_ACC_006)**
- **Given** a Highlight center card shows sender + receiver,
- **When** the user taps either avatar/name,
- **Then** navigation pushes to `[iOS] Profile bản thân`
  (`hSH7L8doXB`) if the tapped user is the current user, otherwise
  `[iOS] Profile người khác` (`bEpdheM0yU`) with the tapped
  Sunner's `user_id` as a route argument.

**Scenario 2 — All Kudos avatars (TC_IOS_KUDOS_ACC_007)**
- Same flow from the feed cards.

**Scenario 3 — Top 10 gift recipient row (TC_IOS_KUDOS_ACC_008)**
- Tap a Sunner row in the Top 10 → profile screen.

**Scenario 4 — Anonymous sender (Q-K-3 resolution)**
- **Given** a Kudos was sent with `is_anonymous = true`,
- **When** the user observes the card,
- **Then** the rendering depends on the per-viewer
  `sender_visible_to_me` flag served by the API:
  - **When the current user is the recipient** (or the real
    sender): `sender_visible_to_me = true` — real sender avatar +
    name render; tap → sender profile. Anonymity is only against
    other viewers.
  - **When the current user is any other viewer**:
    `sender_visible_to_me = false` — the sender section renders
    `anonymous_nickname` (e.g., "Một người Sun*") with NO tappable
    navigation; only the recipient avatar is interactive.

### US9: Spotlight Board — explore the recognition network [P2]

**As a** Sunner
**I want to** pan/zoom the Spotlight chart and search for a Sunner
**So that** I can visualize the recognition graph for the org

#### Acceptance Scenarios

**Scenario 1 — Pan + zoom (TC_IOS_KUDOS_FUN_027)**
- **Given** the Spotlight has loaded with data,
- **When** the user pans (drag) or pinches (zoom),
- **Then** the canvas updates view position / scale accordingly.

**Scenario 2 — Live search (TC_IOS_KUDOS_FUN_028)**
- **Given** the user taps the search input and types a Sunner name,
- **When** the input debounces (≈300 ms),
- **Then** the matching node on the canvas is highlighted/focused.

**Scenario 3 — No-match (TC_IOS_KUDOS_FUN_036)**
- **Given** the typed name doesn't match any Sunner,
- **When** the search resolves,
- **Then** an inline "no results" message is shown beneath the
  input.

**Scenario 4 — Max length (TC_IOS_KUDOS_FUN_034/035)**
- The search input caps at 100 characters. Typing the 101st
  character is suppressed.

**Scenario 5 — Total counter refresh (TC_IOS_KUDOS_FUN_012 +
Q-K-2 resolution)**
- **Given** the Spotlight is interactive AND the displayed total
  is stale (another user sent a Kudos elsewhere since this client
  last fetched),
- **When** the user performs a pull-to-refresh on the screen,
- **Then** the total-Kudos label (e.g., "388 KUDOS") refreshes to
  reflect the latest DB state. NO realtime channel, NO polling —
  the label is only as fresh as the most recent pull-to-refresh
  or initial mount.

**Scenario 6 — Empty state (TC_IOS_KUDOS_FUN_011)**
- **Given** the Spotlight has no data,
- **When** the section loads,
- **Then** the canvas shows its empty-state copy; pan/zoom and
  search are disabled.

### US10: Personal stats + x2 bonus badge [P2]

**As a** Sunner
**I want to** see my own giving/receiving stats prominently
**So that** I can gauge how I'm doing in the SAA cycle

#### Acceptance Scenarios

**Scenario 1 — Render stats (TC_IOS_KUDOS_GUI_002)**
- **Given** the user has session data,
- **When** the All Kudos block renders,
- **Then** the personal-stats panel shows: "Số kudos nhận được",
  "Số kudos đã gửi", "Số tim bạn nhận được", "Số secret box đã mở",
  "Số secret box chưa mở".

**Scenario 2 — x2 fire badge (TC_IOS_KUDOS_FUN_013)**
- **Given** admin has activated the "x2 fire bonus",
- **When** the personal-stats panel renders,
- **Then** a "x2" fire badge is shown next to "Số tim bạn nhận
  được". When the bonus is OFF, the badge is hidden.

### US11: Open Secret Box [P2]

**As a** Sunner
**I want to** tap "Mở Secret Box" to open my next unopened reward
**So that** I can claim accumulated rewards in one tap

#### Acceptance Scenarios

**Scenario 1 — Tap when boxes available (TC_IOS_KUDOS_FUN_024)**
- **Given** the user has ≥1 unopened Secret Box,
- **When** they tap "Mở Secret Box",
- **Then** navigation pushes to the open-secret-box animation flow
  (`[iOS] Open secret box`); on success the personal-stats panel
  updates (`secret_boxes_unopened` -1, `secret_boxes_opened` +1).

**Scenario 2 — Disabled when zero (TC_IOS_KUDOS_FUN_039)**
- **Given** `secret_boxes_unopened == 0`,
- **When** the user observes the button,
- **Then** it is disabled (not tappable).

**Scenario 3 — Double-tap prevention (TC_IOS_KUDOS_FUN_025)**
- **Given** the user has ≥1 unopened box,
- **When** they tap "Mở Secret Box" twice in rapid succession,
- **Then** only one open is triggered. Mirrors Home's
  `rememberSingleClickHandler` contract (TR-004 carried across).

### US12: Top 10 latest gift recipients [P2]

**As a** Sunner
**I want to** see who recently received SAA rewards
**So that** the recognition feels public and aspirational

#### Acceptance Scenarios

**Scenario 1 — Populated list (TC_IOS_KUDOS_GUI_002)**
- **Given** the system has gift recipients,
- **When** the section renders,
- **Then** up to 10 rows show: avatar + Sunner full name + reward
  description (e.g., "Nhận được 1 áo phông SAA").

**Scenario 2 — Empty state (TC_IOS_KUDOS_FUN_003)**
- **Given** no gift recipients yet,
- **When** the section renders,
- **Then** the row area shows "Chưa có dữ liệu".

**Scenario 3 — Profile navigation (TC_IOS_KUDOS_ACC_008)**
- Tap a row → profile screen for that Sunner.

### US13: Copy Link on a Kudos [P3]

**As a** Sunner
**I want to** copy the public link to a Kudos
**So that** I can share it externally

#### Acceptance Scenarios

**Scenario 1 — Highlight card (TC_IOS_KUDOS_FUN_017)**
- Tap "Copy Link" on the active center card → URL is copied to the
  system clipboard AND a toast appears: "Link copied — ready to
  share!".

**Scenario 2 — All Kudos card (TC_IOS_KUDOS_FUN_014)**
- Same behavior on any feed card.

### US14: View All Kudos navigation [P2]

**As a** Sunner
**I want to** tap "View all Kudos →" to see the full list with
pagination
**So that** the hub stays scannable while the full archive is one
tap away

#### Acceptance Scenarios

**Scenario 1 — Tap link (TC_IOS_KUDOS_ACC_009)**
- **Given** the All Kudos section is rendered,
- **When** the user taps "View all Kudos →",
- **Then** navigation pushes to `[iOS] Sun*Kudos_All Kudos`
  (`j_a2GQWKDJ`) which loads `GET /api/v1/kudos` with full
  pagination.

---

## Component Behavior

Every interactive component below carries a Figma Node ID (some are
empty in MoMorph's design-items export — flagged as `id = null`;
the implementer resolves them via `query_section` against the
adjacent labeled Node IDs).

### A — KV Kudos hero band

| Component | Node ID | Type | Role |
|---|---|---|---|
| A — hero container | `6885:9066` | FRAME | Layout; non-interactive |
| A.1 — Send Kudos pill | `6885:9083` / `6891:21267` | INSTANCE | Tap → opens Send Kudos compose (US6) |

### B — Highlight Kudos block

| Component | Node ID | Type | Role |
|---|---|---|---|
| B — section container | `6885:9084` | FRAME | Layout |
| B.1 — filter header row | `6885:9085` | FRAME | Layout |
| B.1.1 — Hashtag filter | `null` (resolve via `query_section`) | button | Opens hashtag bottom sheet (US3) |
| B.1.2 — Phòng ban filter | `null` | button | Opens department bottom sheet (US3) |
| B.2 — section title band | `6885:9090` | FRAME | Display |
| B.2.1 / B.2.2 — carousel arrows | `null` | buttons | Advance/retreat carousel (also via swipe) |
| B.3 — highlight card (5 instances) | `6885:9091`, `9092`, `9093`, `9263`, `9264`, `9265` | INSTANCE | Tap card body / "Xem chi tiết" → detail (US7); tap avatar → profile (US8); heart → like (US5); copy link → US13 |
| B.3.1 — sender avatar | `I…;89:2951;89:2598` per instance | ELLIPSE | Tap → sender profile when `sender_visible_to_me = true`; no-op otherwise (Q-K-3) |
| B.3.2 — sender info (name + emp code + tier badge) | `I…;89:2951;89:2599` per instance | FRAME | Tap → sender profile (same rule as B.3.1). When anonymous-to-viewer, render `anonymous_nickname` without tap target |
| B.3.4 — direction arrow (→) | `I…;89:2952` per instance | FRAME | Display only |
| B.3.5 — receiver avatar | `I…;89:2951;89:2717` per instance | ELLIPSE | Tap → receiver profile |
| B.3.6 — receiver info (name + emp code + star-tier badge) | `null` | FRAME | Tap → receiver profile; star-tier badge follows US5 Scenario 6 thresholds |
| B.4 — content area | `I…;89:2956` per instance | FRAME | Layout |
| B.4.1 — post time | `I…;89:2957` per instance | TEXT | Display only |
| B.4.2 — message body | `I…;89:2959` per instance | FRAME | Truncated to 3 lines; "..." overflow |
| B.4.3 — hashtag chips | `I…;89:2969` per instance | FRAME | Tap chip → set Hashtag filter (US3 scenario 4) |
| B.4.4 — action row | `I…;89:2972` per instance | FRAME | Layout container |
| B.4.4.1 — heart icon + count | `null` (child of B.4.4) | button | Toggle like (US5). Disabled when `kudos.like_disabled_for_me = true` (Q-K-5) |
| B.4.4.2 — Copy Link | `null` (child of B.4.4) | button | Copy share URL to clipboard + show toast (US13) |
| B.4.4.3 — Xem chi tiết | `null` (child of B.4.4) | button | Navigate to Kudo detail (US7) |
| B.5 — pagination | `6885:9098` | INSTANCE | Prev / current/total / Next |
| B.5.2 — page indicator | `I6885:9098;93:2086` | TEXT | "1/5" format |
| B.6 — Spotlight header | `null` | section header | Display |
| B.7 — Spotlight container | `6885:9101` | FRAME | Layout |
| B.7.1 — Total Kudos label | `6885:9219` | TEXT | Reactive count from DB |
| B.7.2 — pan/zoom canvas | `6885:9217` | FRAME | Gestures (US9) |
| B.7.3 — Sunner search input | `6885:9216` | INSTANCE | Max 100 chars; debounced live search |

### C — All Kudos block

| Component | Node ID | Type | Role |
|---|---|---|---|
| C — feed container | `6885:9220` | FRAME | Layout |
| C.1 — section header | `null` | section header | Display |
| C.2 — "View all Kudos" link | `null` | button | Navigate to `[iOS] Sun*Kudos_All Kudos` (US14) |
| C.3 — Kudos post card | `null` (one instance per kudos) | INSTANCE | Same interactions as B.3 (detail, profile, like, copy link, hashtag) |
| C.3.1 — sender info | `null` | FRAME | Same tap-to-profile rule as B.3.2 (per-viewer `sender_visible_to_me`) |
| C.3.2 — direction icon (→) | `null` | FRAME | Display only |
| C.3.3 — receiver info (+ star-tier badge) | `null` | FRAME | Tap → receiver profile; star-tier rules per US5 Scenario 6 |
| C.3.4 — post time label | `null` | TEXT | Same format as B.4.1 |
| C.3.5 — content card | `null` | FRAME | Layout: title + message body (5-line truncate) + hashtag chips (1-line max) + photos + action row |
| C.3.6 — image attachment | `null` | INSTANCE | Tap → full-screen image viewer |
| C.3.7 — hashtag chips (feed) | `null` | FRAME | Tap chip → set Hashtag filter (US3 scenario 4) — same as B.4.3 |
| C.3.8 — action row (heart + Copy Link + Xem chi tiết) | `null` | FRAME | Same children + behaviors as B.4.4 (heart, copy link, xem chi tiết) |

### D — Personal stats + rewards

| Component | Node ID | Type | Role |
|---|---|---|---|
| D.1 — stats container | `6885:9223` | FRAME | Layout |
| D.1.2 — Kudos received | `6885:9225` | FRAME | Display |
| D.1.3 — Kudos sent | `6885:9230` | FRAME | Display |
| D.1.4 — Hearts received (+ x2 badge) | `6885:9235` | FRAME | x2 badge conditional on admin flag (US10 scenario 2) |
| D.1.5 — visual divider | `6885:9243` | RECTANGLE | Display |
| D.1.6 — boxes opened | `6885:9244` | FRAME | Display |
| D.1.7 — boxes unopened | `6885:9249` | FRAME | Display |
| D.2 — Open Secret Box CTA | `null` | button | Tap → open flow (US11). Disabled when D.1.7 == 0 |
| D.3 — Top 10 container | `6885:9255` | FRAME | Layout |
| D.3.1 — section title | `6885:9258` | TEXT | Display |
| D.3.2 — recipient row (3 instances visible) | `6885:9259`, `9260`, `9261` | INSTANCE | Tap row → profile (US12) |

---

## Data Requirements

### Display fields

| Field | Source | Format / rules |
|---|---|---|
| KV Kudos banner text | static i18n | "Hệ thống ghi nhận và cảm ơn" |
| Send Kudos placeholder | static i18n | "Hôm nay, bạn muốn gửi kudos đến ai?" |
| Highlight cards | `GET /api/v1/kudos/highlight` | Top-5 by `heartCount DESC`; reset on filter change |
| All Kudos feed | `GET /api/v1/kudos?page&limit&hashtag_id&department_id` | Only `status='active' AND deleted_at IS NULL` |
| `kudos.created_at` | server | Rendered "HH:mm — DD/MM/YYYY" in local timezone |
| `kudos.message` | server | Highlight: 3-line truncate; Feed: 5-line truncate |
| `kudos.title` | server | UPPERCASE category title (e.g., "TEAMWORK") |
| `kudos_hashtags[].tag_name` | server (relation) | Max 5 chips per line; overflow "..." |
| `kudos.is_anonymous` | server | Drives detail-screen route (`T0TR16k0vH` vs `5C2BL6GYXL`); does NOT directly hide sender on the hub (see `sender_visible_to_me`) |
| `kudos.sender_visible_to_me` (derived, per-viewer) | server (Q-K-3) | When `true`, render real sender avatar + name + tap-to-profile; when `false`, render `anonymous_nickname` non-interactively |
| `kudos.anonymous_nickname` | server | Display fallback when `sender_visible_to_me = false` (e.g., "Một người Sun*") |
| `kudos.liked_by_current_user` | server (derived) | Hydrates heart state on load |
| `kudos.like_disabled_for_me` | server (derived, Q-K-5) | `true` when current user is sender OR recipient — drives heart icon disabled state |
| `kudos.heart_count` | server | Display verbatim |
| Recipient star tier | server (`recipient.star_tier: 0|1|2|3`) | Threshold-tier per US5 Scenario 6: 0 below 10 Kudos, 1 at [10,20), 2 at [20,50), 3 at ≥50. Client renders the field verbatim — no client-side bucket math |
| Spotlight total | Returned in the `/api/v1/spotlight/graph` response payload as `total_kudos_count: number` | Refreshed by mount + pull-to-refresh only (Q-K-2) |
| Spotlight graph | `GET /api/v1/spotlight/graph` | Pan/zoom/search payload |
| Personal stats | `GET /api/v1/users/me/stats` | `kudos_received_count`, `kudos_sent_count`, `hearts_received`, `secret_boxes_opened`, `secret_boxes_unopened` |
| x2 fire bonus flag | `GET /api/v1/system/flags` (or session) | Boolean — toggled by admin |
| Top 10 gift recipients | `GET /api/v1/rewards/recent?limit=10` | Latest by `awarded_at DESC` |

### Input fields & validation

| Field | Component | Validation |
|---|---|---|
| Hashtag filter selection | B.1.1 | Single-select; nullable (null → no filter); AND-combined with Department |
| Department filter selection | B.1.2 | Single-select; nullable; AND-combined with Hashtag |
| Spotlight search input | B.7.3 | `maxLength = 100`; free-text; debounced ~300 ms |

### Data relationships

- `kudos.sender_id → users.id` — **always populated** server-side
  (NOT nullable). Anonymity is enforced at the **read** layer via
  the per-viewer `sender_visible_to_me` derivation (Q-K-3), not by
  scrubbing `sender_id`.
- `kudos.recipient_id → users.id`
- `kudos.hashtag_id → hashtags.id` (many-to-many via
  `kudos_hashtags`)
- `kudos.department_id → departments.id` (recipient's department at
  send time; denormalized for filter performance)
- `reactions.user_id + reactions.kudos_id` unique
  (constraint enforces "1 like per user per kudos")
- `reward_recipients.user_id → users.id` for Top 10 list

---

## API Requirements (Predicted)

| Endpoint | Method | Purpose | Triggered by |
|---|---|---|---|
| `/api/v1/auth/me` | GET | Confirm session for auth gate | Screen mount (US2) |
| `/api/v1/kudos/highlight` | GET | Top-5 by heart count | Mount + filter change (US3, US4) |
| `/api/v1/kudos?page&limit&hashtag_id&department_id` | GET | Paginated feed | Mount + filter change + scroll-to-bottom |
| `/api/v1/kudos/{kudosId}` | GET | Single Kudos for detail | "Xem chi tiết" / card tap (US7) |
| `/api/v1/kudos/{kudosId}/reactions` | POST | Add heart (server checks 1-per-user) | Heart tap (US5 scenario 1) |
| `/api/v1/kudos/{kudosId}/reactions` | DELETE | Remove heart | Heart re-tap (US5 scenario 2) |
| `/api/v1/hashtags` | GET | Hashtag bottom-sheet list | B.1.1 tap |
| `/api/v1/departments` | GET | Department bottom-sheet list | B.1.2 tap |
| `/api/v1/spotlight/graph` | GET | Spotlight Sunner-graph payload **and** total kudos count (`total_kudos_count` field on the response — B.7.1 reads this) | Spotlight section mount + pull-to-refresh |
| `/api/v1/spotlight/search?q&limit` | GET | Live Sunner search | B.7.3 input change (debounced) |
| `/api/v1/users/me/stats` | GET | Personal-stats tiles | Mount |
| `/api/v1/system/flags` | GET | x2 fire bonus flag | Mount (or hydrated from session) |
| `/api/v1/users/me/secret-boxes/next` | GET | Resolve next unopened box id | D.2 tap |
| `/api/v1/users/me/secret-boxes/{boxId}/open` | POST | Open box + return reward | D.2 confirm |
| `/api/v1/rewards/recent?limit=10` | GET | Top 10 latest gift recipients | Mount |

Authentication: all endpoints require a valid Supabase JWT; RLS
policies enforce read scope at the row level. The auth gate +
401-redirect pipeline already established by Home applies verbatim.

### Server-derived per-viewer fields on every kudos payload

The `GET /api/v1/kudos`, `GET /api/v1/kudos/highlight`, and
`GET /api/v1/kudos/{kudosId}` responses MUST include the following
derived booleans on each `kudos` object — computed from the auth
JWT's `user_id` at request time:

- `liked_by_current_user: boolean` — hydrates heart icon state
- `sender_visible_to_me: boolean` (Q-K-3) — `true` when current
  user is the recipient OR the real sender; `false` for any other
  viewer of an anonymous kudos
- `like_disabled_for_me: boolean` (Q-K-5) — `true` when current
  user is the sender OR the recipient; disables the heart icon
  client-side (server still enforces the rule at write time)

These derived fields keep the client renderer simple — no
client-side equality checks against `current_user.id` are needed.

---

## Accessibility Behavior

Per Constitution Principle III, the screen MUST be navigable with
TalkBack and conform to WCAG 2.1 AA. The following accessibility
behaviors are part of the spec contract (they affect what gets
implemented, distinct from visual accessibility like contrast which
the implementer handles via `design-style` tokens at task-execution
time):

### Touch targets

- Every tappable element ≥48×48 dp (Constitution III + TR-008 from
  Award Detail spec): Send Kudos pill, Hashtag filter, Phòng ban
  filter, carousel arrows, each Highlight card body + Xem chi
  tiết + Copy Link + heart, hashtag chips, each All Kudos card
  + its action row, Open Secret Box CTA, Top 10 recipient rows,
  "View all Kudos" link, Spotlight search input.

### Screen reader (TalkBack) contentDescription contract

| Component | `contentDescription` |
|---|---|
| A.1 Send Kudos pill | "Gửi Kudos, nhập tên người nhận" |
| B.1.1 Hashtag filter trigger | "Lọc theo hashtag, {hashtag tên đang chọn / 'chưa chọn'}, danh sách thả xuống" |
| B.1.2 Phòng ban filter trigger | "Lọc theo phòng ban, {phòng ban tên đang chọn / 'chưa chọn'}, danh sách thả xuống" |
| B.3 Highlight card | "Kudos từ {sender hoặc 'Một người Sun*'} gửi {recipient}, {N} tim, hashtag {tags}" |
| B.4.4 heart icon | `Role.Button` + `stateDescription` toggling between "đã thích" / "chưa thích"; `enabled = false` (TalkBack reads "đã tắt") when `like_disabled_for_me = true` |
| B.4.4 "Xem chi tiết" | `Role.Button` + "Xem chi tiết Kudos" |
| B.4.4 Copy Link | `Role.Button` + "Sao chép liên kết Kudos" |
| B.5 page indicator | "Trang {N} trên {Total}" |
| B.7.3 Sunner search | "Tìm kiếm Sunner trong bảng Spotlight" |
| C.2 "View all Kudos" | `Role.Button` + "Xem tất cả Kudos" |
| D.2 Open Secret Box | `Role.Button` + "Mở Secret Box, còn {N} chiếc" (or "Mở Secret Box, đã hết hộp" when disabled) |
| D.3.2 recipient row | "Sunner {full_name}, {reward description}" |

### Focus order

Top-to-bottom matches the visual reading order:
1. Send Kudos pill (A.1)
2. Hashtag filter (B.1.1) → Phòng ban filter (B.1.2)
3. Highlight carousel cards (one focus group per card; swipe
   gesture also moves focus)
4. Spotlight header → Spotlight search → pan/zoom canvas
   (canvas focusable as a single group)
5. Personal stats tiles (D.1.2 → D.1.3 → D.1.4 → D.1.6 → D.1.7)
6. Open Secret Box CTA
7. Top 10 recipient rows
8. All Kudos feed cards
9. "View all Kudos" link

### Font scaling

All text scales with the system font setting. Truncation lines
(3 lines for Highlight, 5 lines for Feed) MUST respect the scaled
line height — the implementer uses `androidx.compose.material3`
typography tokens without hard-coded heights.

### Predictive back

Bottom sheets (hashtag, department) and any modal popups (e.g.,
Open Secret Box flow before navigating) MUST close on predictive
back without popping the screen itself — same contract as
Language Dropdown + Award Detail's category dropdown.

---

## State Management

### Local component state

- `highlightState: KudosHighlightState` — `Loading / Empty /
  Loaded(items: List<Kudos>) / Error(messageRes: Int)` (sealed
  interface mirroring `AwardsState`)
- `allKudosState: AllKudosState` — same shape with pagination
  metadata (`hasMore`, `nextPage`)
- `spotlightState: SpotlightState` — `Loading / Empty /
  Loaded(graph) / Error`
- `statsState: PersonalStatsState` — `Loading / Loaded(stats) /
  Error`
- `topTenState: TopTenState` — same shape
- `selectedHashtagId: String?` (null when unfiltered)
- `selectedDepartmentId: String?` (null when unfiltered)
- `spotlightSearchQuery: String` (held in `TextFieldValue`,
  debounced to a `Flow`)
- `secretBoxBusy: Boolean` (prevents double-tap per TC_FUN_025;
  reuses `core/ui/rememberSingleClickHandler`)

### Global / session state (reused from existing infra)

- Session JWT from `core/auth` — same pipeline as Home + Award
  Detail.
- Locale from `core/locale/LanguagePreferenceRepository` — section
  headers + empty-state copy localize.
- Special-day flag from `GET /api/v1/system/flags` — drives the +1
  vs +2 heart math AND the x2 fire badge visibility.

### Cache / invalidation

- Highlight + All Kudos refetch on **any filter change**
  (in-memory only; no persistent cache).
- Reactions are optimistic: tapping a heart flips the local state
  immediately + fires the POST/DELETE; on failure the icon reverts
  and an inline toast surfaces.
- Personal stats refetch on `secretBoxOpened` success (US11) and on
  screen-resume (`Lifecycle.Event.ON_RESUME`) to capture
  cross-session changes.
- **Pull-to-refresh contract (Q-K-2 resolution)**: the screen wraps
  the body in a `PullRefresh`/`SwipeRefresh` container. Pulling
  refetches all sections together — Highlight + All Kudos +
  Spotlight (graph + total) + personal stats + Top 10. No
  realtime channel; no auto-poll timer.

### Concurrency / race rules

- If the user changes a filter while a previous fetch is in flight,
  the in-flight result MUST be discarded (use `kotlinx.coroutines`
  `Job.cancel()` per the canonical AwardDetailViewModel pattern).
- Heart tap is single-flight per `kudosId`; rapid double-tap on the
  same card is debounced via `rememberSingleClickHandler`.
- Pull-to-refresh while a refresh is already in flight is a no-op
  — the indicator stays visible and the second pull does NOT
  trigger a fresh fetch chain. The `PullRefreshState` `isRefreshing`
  flag gates new requests.
- Open Secret Box tap while the open-flow modal is mounting is a
  no-op (TC_IOS_KUDOS_FUN_025) — guarded by the local
  `secretBoxBusy` flag + `rememberSingleClickHandler`.
- Personal stats refresh + Secret Box success: when both arrive
  concurrently, the Secret Box success payload wins
  (`secret_boxes_opened += 1`, `secret_boxes_unopened -= 1`
  applied to the latest stats response).

---

## Out of Scope

- The **Send Kudos compose flow** itself — owned by
  `PV7jBVZU1N-iOS-Sun-Kudos-Gửi-lời-chúc-Kudos` spec (separate frame).
- The **View Kudo detail** screen — owned by
  `T0TR16k0vH-iOS-Sun-Kudos-View-kudo` (and the anonymous variant
  `5C2BL6GYXL`).
- The **All Kudos full-list** screen with infinite scroll — owned
  by `j_a2GQWKDJ-iOS-Sun-Kudos-All-Kudos`.
- The **Open Secret Box animation** — owned by
  `kQk65hSYF2-iOS-Open-secret-box` (already discovered).
- The **Hashtag bottom sheet** internals — own frame `V5GRjAdJyb`
  (`[iOS] Sun*Kudos_dropdown hashtag`). This hub spec covers the
  trigger (B.1.1 tap) + the filter-application result (US3); the
  bottom-sheet's own list rendering, search, scroll, dismiss
  semantics ship in `V5GRjAdJyb`'s spec.
- The **Phòng ban bottom sheet** internals — own frame
  `76k69LQPfj` (`[iOS] Sun*Kudos_dropdown phòng ban`). Same
  scope-boundary as the hashtag sheet above.
- Visual specs (colors / typography / spacing / motion) — per
  Constitution Principle II + canonical Out of Scope. The
  implementer fetches per-section CSS via `query_section` at
  task-execution time.
- Backend implementation of any predicted API endpoint — those
  spec docs live elsewhere; this screen consumes the contracts as
  documented above.

---

## Dependencies

- [x] `[iOS] Login` (8HGlvYGJWq) + `AuthRedirectController` ship
  the auth gate
- [x] `[iOS] Home` (OuH1BUTYT0) ships `HomeHeader`, `HomeBottomBar`,
  `KudosSection`, `rememberSingleClickHandler` — most are reused
  verbatim
- [x] `[iOS] Language dropdown` (uUvW6Qm1ve) ships `LanguageSelector`
- [x] `AwardDetailScreen` (across all 7 Award frames) establishes
  the parametric stateless-content + ViewModel pattern this screen
  follows
- [ ] Send Kudos compose spec — required for US6 to navigate to a
  real destination instead of a placeholder
- [ ] View Kudo detail spec — required for US7
- [ ] All Kudos paginated list spec — required for US14
- [ ] Open Secret Box animation spec — required for US11
- [ ] Profile spec (`hSH7L8doXB` self / `bEpdheM0yU` other) —
  required for US8 + US12 navigation targets
- [ ] Backend endpoints listed in § API Requirements need to ship
  (or be mocked in DEMO mode) for the screen to render real data

---

## Notes

### Why the spec lists 14 user stories

Sun*Kudos is a hub — many disjoint sub-features (filter, carousel,
spotlight, stats, secret box, top 10, copy link, send kudos
shortcut). Each maps to one MoMorph test-area cluster and one
sealed UI state, so listing them separately matches both the
implementation's state-machine structure and the test plan (39
test cases organized by component/section).

### Constitution alignment

- **Principle I (Source organization)**: New feature package
  `com.example.aiddproject.kudos/` mirroring `awarddetail/`'s
  `ui/data/domain` layout. Composables stay <150 LOC each — the
  hub splits into one stateless content composable plus dedicated
  sub-composables per section (mirrors `AwardDetailScreenContent`).
- **Principle II (Tech stack)**: Single `KudosViewModel` exposing
  `StateFlow<KudosUiState>`; sealed state interfaces per section;
  Supabase Postgrest reads + RLS writes for reactions.
- **Principle III (Material Design 3)**: M3 components (Scaffold,
  BottomSheet for hashtag/department filters, LazyColumn for the
  feed, Image for attachments). Visual chrome fetched on-demand via
  `query_section` at impl time.
- **Principle IV (OWASP)**: Reactions enforce RLS at server side
  (1-like-per-user via `unique(user_id, kudos_id)`); the client
  MUST optimistic-update but never claim authority. No PII in
  client logs (existing `SecureTimberTree` scrub applies).
- **Principle V (TDD)**: Each user story above translates 1:1 to a
  test class (Compose UI test for UI-heavy stories;
  ViewModel unit test for state-machine stories). Plan + tasks
  must enforce failing-test-first ordering when implementation
  starts.

### Cross-document references

- Canonical Award Detail spec: `.momorph/specs/c-QM3_zjkG-iOS-Award-Top-talent/spec.md`
  — borrowed patterns: auth gate, parametric ViewModel, sealed
  state, retry-on-error, RLS-backed write.
- Home spec: `.momorph/specs/OuH1BUTYT0-iOS-Home/spec.md` — shared
  chrome (`HomeHeader`, `HomeBottomBar`, `KudosSection`).
- SCREENFLOW: this spec adds one more authenticated-main-app screen
  on top of Home + Award Detail.

### Open questions

- **Q-K-1**: Does the "special day" flag come from a system-flags
  endpoint or is it encoded in the JWT? — implementer call, no
  product input required.

### Resolved questions (2026-05-12)

- **Q-K-2** — **Resolved: pull-to-refresh only**. The Spotlight total
  counter (B.7.1) refreshes ONLY when the user performs a
  pull-to-refresh gesture on the screen. NO realtime channel, NO
  polling timer. State management section updated accordingly —
  drop the 60 s polling note; rely on the screen-level
  `SwipeRefresh`/`PullToRefresh` to trigger every section's refetch
  together (Highlight + All Kudos + Spotlight total + personal
  stats + Top 10). Spotlight graph data only re-fetches if the user
  explicitly pulls.

- **Q-K-3** — **Resolved: anonymous sender is visible to recipient**.
  When `kudos.is_anonymous = true`:
  - **Other viewers** (anyone on the hub feed who is NOT the
    recipient) see the sender as the `anonymous_nickname` (e.g.,
    "Một người Sun*"); tapping the sender row is a no-op.
  - **The recipient** of the Kudos sees the real sender's avatar +
    name; tapping navigates to the sender's profile.
  - The server determines viewer identity from the auth JWT and
    serves a per-viewer payload (the kudos response includes a
    derived `sender_visible_to_me` flag — true for recipient + the
    real sender, false otherwise).
  - The client renders accordingly: when `sender_visible_to_me`
    is true → real sender info + tap-to-profile; when false →
    anonymous nickname + no tap.

- **Q-K-4** — **Resolved: web URL format `https://saa.sun-asterisk.com/kudos/{kudosId}`**
  (confirmed 2026-05-12). Copy Link copies this exact URL verbatim
  to the system clipboard; toast text remains
  `"Link copied — ready to share!"`. Android App Links / iOS
  Universal Links MAY be layered on the same URL in a follow-on —
  same shareable link opens the app if installed, web fallback
  otherwise.

- **Q-K-5** — **Resolved: neither sender nor recipient can like a Kudos
  they participated in**. Extends TC_IOS_KUDOS_FUN_008's
  sender-only constraint to cover recipients too. The heart icon
  is disabled when:
  - `current_user.id == kudos.sender_id`, OR
  - `current_user.id == kudos.recipient_id`.
  Rationale: liking a Kudos you received yourself would inflate
  your own heart count via your own action — undermines the
  recognition-from-peers semantics. Server MUST enforce both rules
  at the RLS / API level too (client-side disable is a UX hint,
  not the source of truth).

### Spec updates from resolved questions

- **US5 Scenario 3** is updated: "Sender OR recipient cannot like
  own Kudos. The heart icon is disabled when the current user is
  the sender OR the recipient of the displayed Kudos."
- **US7 / US8 anonymous handling** is updated: the recipient sees
  the real sender; other viewers see the anonymous nickname.
  Detail screen (`T0TR16k0vH` vs `5C2BL6GYXL`) routing remains
  driven by `is_anonymous`, but the in-detail rendering will
  similarly check the per-viewer `sender_visible_to_me` flag.
- **State management section** drops the "Spotlight total: poll
  every 60 s" line — refresh is pull-to-refresh-driven only.
- **API Requirements**: the kudos GET responses (highlight + feed +
  detail) MUST include the derived `sender_visible_to_me` boolean.
- **Out of Scope**: realtime updates remain out of scope (Q-K-2
  resolution forces pull-to-refresh contract).
