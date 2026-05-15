# Feature Specification: Sun*Kudos — Notifications

**Frame ID**: `_b68CBWKl5`
**Frame Name**: `[iOS] Notifications`
**File Key**: `9ypp4enmFmdK3YAFJLIu6C`
**Created**: 2026-05-15
**Status**: Draft

---

## Overview

The Notifications screen is the destination of the bell-icon trigger in the app header. It lists every system-generated notification for the authenticated Sunner — kudos received, hearts received on sent kudos, Secret Box unlocks, level-ups, content-hidden warnings, badge milestones, and (for admin/reviewer roles) review-flag requests — and lets the user tap into the referenced screen OR batch-mark everything as read.

**Target users**: every authenticated Sunner. The "Review request" type renders only when the current user has admin/reviewer permissions; all other types are user-personal.

**Business context**: notifications are the asynchronous-engagement glue of the Sun*Kudos product. Without them, users miss kudos they've received, hearts on kudos they've sent, and timely Secret Box unlocks — all of which depend on the user opening the app at the right moment. Centralising every event into a single list (instead of pushing the user back to the hub via toast/snackbar) supports the "lan tỏa năng lượng tích cực" recurring-engagement goal.

**Entry points**:
- Header bell icon on Home (`OuH1BUTYT0`), Award Detail (`c-QM3_zjkG` and variants), Kudos hub (`fO0Kt19sZZ`), and any other screen that mounts `HomeHeader` with `onBellClick` wired. All these screens already pass `unreadCount` to the bell badge.

**Important migration note**: the current codebase has a partial implementation that opens a **bottom sheet** on bell tap from Home (`HomeScreen.kt:90`: `onBellClick = { notificationsSheetVisible = true }`), wired as a "Phase 13 polish" stub. The Kudos hub currently has the bell wired to a no-op (`KudosScreen.kt:68`). This spec replaces both with a **dedicated full-screen route** (`Routes.NOTIFICATIONS`) — Figma frame `_b68CBWKl5` is authored as a full screen with its own TopNavigation, not a sheet. Implementation MUST remove the bottom-sheet stub from Home + wire the Kudos hub bell + AppNavigation entry; see Q-N-11.

**Out of scope for this spec**: push-notification delivery (FCM / APNs setup, foreground service, notification channels) — these are a separate platform concern. This screen is the in-app inbox view.

---

## User Scenarios & Testing *(mandatory)*

### User Story 1 — Catch up on unread notifications (Priority: P1)

A Sunner opens the app, sees the bell-icon badge showing N unread, taps the bell, and sees a chronological list of every notification with the unread ones visibly marked. They tap an item to jump to the referenced screen (a kudo they received, a Secret Box to open, their own profile after a level-up, etc.) — the tapped item is marked read and the badge count decrements.

**Why this priority**: this is the primary purpose of the screen. Without read-state semantics and tap-through, the bell badge can't be cleared and the user can't act on what they were notified about. P1 is non-negotiable for shipping.

**Independent Test**: with at least 1 unread notification in the user's notification feed, open the Notifications screen from any entry point, verify (a) the unread item shows the red-dot indicator + bold text styling, (b) tapping it routes to the type-specific destination, and (c) returning to Notifications shows the item with the indicator removed and the badge count decremented by 1.

**Acceptance Scenarios**:

1. **Given** I have 3 unread + 5 read notifications, **When** the screen mounts, **Then** all 8 render in newest-first order; each unread row shows the red-dot indicator (`mms_B.1.3_Group 425`); each read row hides it.
2. **Given** I tap an unread "Kudos received" row, **When** the route resolves, **Then** I land on the Kudo Detail screen for that kudo (`T0TR16k0vH` for normal, `5C2BL6GYXL` for anonymous), AND the system marks that single notification as read (red-dot removed when I return).
3. **Given** I tap a read row, **When** the route resolves, **Then** I land on the same destination but no read-state mutation happens (the row was already read).
4. **Given** I tap the back arrow OR press the system-back gesture, **When** the screen pops, **Then** I return to the entry screen (Home / Awards / Kudos hub) with the bell-icon badge reflecting any unread count I just decremented.
5. **Given** I have ZERO notifications (fresh account or after every read), **When** the screen mounts, **Then** the list renders as empty — a placeholder "Bạn chưa có thông báo nào" (per Q-N-1) sits in the body so the screen isn't visually blank.

---

### User Story 2 — Mark everything as read in one tap (Priority: P1)

A Sunner has a long backlog of unread notifications (10+) and doesn't want to tap into each one. They tap "Đánh dấu đọc tất cả" — every unread row immediately loses its red dot, the bell-icon badge clears to 0 globally.

**Why this priority**: P1 because a long unread list otherwise becomes a chore. The Figma component spec (`mms_Button_read all`, node `6885:9392`) explicitly says "đánh dấu tất cả thông báo chưa đọc thành đã đọc; tất cả chấm đỏ biến mất" — this is a designed-in primary action.

**Independent Test**: with ≥ 2 unread notifications visible, tap the "Đánh dấu đọc tất cả" button. Verify (a) every red-dot indicator disappears in a single recomposition, (b) the bell-icon badge in the app header clears to 0, AND (c) the change persists across screen-pop + remount (no row re-renders as unread after returning).

**Acceptance Scenarios**:

1. **Given** I have 5 unread + 2 read notifications, **When** I tap the "Đánh dấu đọc tất cả" button, **Then** all 5 unread rows lose their red-dot indicator in a single state update, my bell-badge count clears to 0, AND a backend `PATCH /notifications/read-all` (or equivalent) call succeeds.
2. **Given** I have ZERO unread notifications, **When** I tap the button, **Then** nothing observable happens — no badge change, no row update, no error, no API call. Per Q-N-10 the handler MUST early-return without an API call (the button stays visible and enabled per Figma's "vẫn hiển thị nhưng không có tác dụng khi click").
3. **Given** the backend call fails (network down / 500), **When** the user tapped the button, **Then** the UI MUST surface a recoverable error (Snackbar with retry CTA) AND restore the red-dot indicators (optimistic-update rollback) so the local state matches the server state.

---

### User Story 3 — Follow an inline "Community Standards" link from a hidden-content warning (Priority: P2)

A Sunner receives notification type 5 (their kudo was auto-hidden for violating the anti-spam standards). The notification row carries an inline link to the Community Standards screen so they can read the rules before retrying.

**Why this priority**: P2 because the user can still tap the notification row body to reach the same warning destination, but the inline link is the discoverable path to the *rules themselves*. Without it, the user has no way to figure out why their content was hidden short of trial-and-error.

**Independent Test**: with a "Content hidden" notification (type 5) in the list, tap the inline "Tiêu chuẩn cộng đồng" affordance. Verify navigation to `xms7csmDhD` (Community Standards screen) with the back-stack preserving the Notifications screen underneath.

**Acceptance Scenarios**:

1. **Given** I see a type-5 (Content hidden) notification, **When** I tap the inline "Tiêu chuẩn cộng đồng" link/icon button, **Then** I navigate to `Routes.COMMUNITY_STANDARDS` (existing route, shipped in commit `504a73d`). Tapping back returns to Notifications.
2. **Given** I tap the row body (NOT the inline link) of the same notification, **When** the route resolves, **Then** I land on the Kudo Detail of the hidden kudo (`T0TR16k0vH`) — NOT Community Standards. The two destinations are distinct.

---

### User Story 4 — Type-specific routing on row tap (Priority: P1)

Per the Figma B.1 spec, each of the 7 notification types routes to a different destination on row tap. The routing table is the load-bearing contract for the screen.

**Why this priority**: P1 because the wrong destination is functionally a broken notification. P1 lumped with US1; called out separately because the routing table is the spec's most testable behavioral contract.

**Independent Test**: for each of the 7 types, plant a seed notification, tap it, verify the destination route is exactly the one specified below.

**Acceptance Scenarios** (one per type):

1. **Given** type 1 (Kudos received), **When** tapped, **Then** routes to the Kudo Detail screen for the referenced kudo (`T0TR16k0vH` or `5C2BL6GYXL` if the kudo is anonymous).
2. **Given** type 2 (Heart received), **When** tapped, **Then** routes to the Kudo Detail screen for the OWN kudo that received the heart.
3. **Given** type 3 (Secret Box unlock), **When** tapped, **Then** routes to the Open Secret Box screen (`kQk65hSYF2`).
4. **Given** type 4 (Level up), **When** tapped, **Then** routes to the Profile-self screen (`hSH7L8doXB`).
5. **Given** type 5 (Content hidden), **When** the ROW body is tapped, **Then** routes to the Kudo Detail screen for the hidden kudo. (Inline "Tiêu chuẩn cộng đồng" link covered by US3.)
6. **Given** type 6 (Badge collected), **When** tapped, **Then** routes to the Profile-self screen (`Routes.PROFILE`, Figma `hSH7L8doXB`). Per Q-N-2: a dedicated badge-section deep-link is **deferred** — for MVP, both type-4 (Level up) and type-6 (Badge collected) route to the same Profile-self entry. The badge sub-section deep-link can be added later when the Profile spec ratifies its anchor scheme.
7. **Given** type 7 (Review request, admin-only), **When** tapped, **Then** routes to a placeholder Admin Review destination (`PlaceholderScreen(label = "Admin Review Content")` per Q-N-3, option a). The admin spec is not yet authored; this MVP stub keeps the notification routing testable without blocking on the admin feature.

---

### Edge Cases

- **Empty inbox**: no notifications at all → empty-state placeholder per US1 Scenario 5; copy "Bạn chưa có thông báo nào" per Q-N-1.
- **Mixed read/unread + visual ordering**: notifications are listed strictly newest-first regardless of read state — read items are NOT pushed to the bottom; no separate "Earlier" section (per Q-N-4).
- **Relative time edge cases**: a notification created exactly 60 minutes ago renders as "1 giờ trước" (not "60 phút trước"). The threshold ladder is `<60 phút → x phút trước; <24 giờ → x giờ trước; <30 ngày → x ngày trước; <12 tháng → x tháng trước; else → x năm trước` (per Q-N-5).
- **Inline-link tap-vs-row-tap gesture conflict (type 5)**: tapping the inline "Tiêu chuẩn cộng đồng" link MUST NOT also fire the parent row's tap handler. Compose's child-IconButton consumes the gesture by default; verify with an instrumented test.
- **Reviewer/admin role gating**: a regular Sunner MUST NEVER receive a type-7 (Review request) notification. The backend filters by role; the client trusts the server. If the client receives one (server bug / RLS hole), render it normally — there's nothing user-harmful about seeing a stray review row.
- **Notification deleted/archived server-side while open**: if the notifications feed has changed since the screen mounted (e.g. the user dismissed a notification in another tab), the next pull-to-refresh OR navigation away-and-back syncs. This screen does NOT push real-time updates from the server (no WebSocket / no Supabase Realtime per Q-N-6); **pull-to-refresh is the consistency mechanism**.
- **Network failure on load**: render an error state with a retry CTA — same pattern as the kudos hub feeds.
- **Read-all rollback** (US2 Scenario 3): if the batch-write fails, restore local state to pre-tap.
- **TalkBack flow**: each row's contentDescription = `{notification_text}, {relative_time}, {unread/read}, button`. The mark-all-as-read button reads "Đánh dấu đọc tất cả, button".
- **Locale switch while open**: relative-time strings re-render in the active locale.
- **Long content overflow**: notification text is multi-line; rows grow vertically to fit. No truncation.

---

## UI/UX Requirements *(from Figma)*

### Screen Components

| # | Component | Node ID | Type | Behavior |
|---|-----------|---------|------|----------|
| 1 | Status bar | `6885:9375` | System chrome | No app-side behavior — Android renders the platform status bar with `statusBarsPadding`. |
| 2 | Back arrow | `6885:9389` (in left accessory `6885:9388`) | Icon button | Tap → pop back-stack to entry screen. Reuses `R.drawable.ic_back_chevron`. M3 IconButton default 48dp touch target. |
| 3 | Title text | `6885:9380` | Static TEXT | Read-only. Localized: VN canonical = **"Thông báo"** (per Q-N-7); EN fallback = "Notifications" (Figma source). Sourced from `strings.xml` key `notifications_title`. |
| 4 | "Đánh dấu đọc tất cả" button (A.1) | `6885:9392` | Icon + text button | Tap → batch-mark every unread notification as read. Behaviour per US2 + FR-005. **Always visible, always enabled** (per Q-N-10). The handler MUST early-return without an API call when there are zero unread items, so a tap on an "empty" state is a free no-op. |
| 5 | Notification list container | `6885:9393` | Scrollable list (`LazyColumn`) | Renders one row per notification (`mms_B.1_Noti` and `Noti` siblings — the Figma uses two component variants but functionally they're the same row template). Newest-first ordering. |
| 6 | Notification row body (B.1) | `6885:9394` (and sibling `Noti` instances `6885:9395`–`6885:9400`) | Composite tappable row: Icon + Content + Unread indicator | Tap on the row body → navigate to the type-specific destination (US4 routing table). Marks the single notification as read on tap. |
| 6.1 | Notification icon (B.1.1) | `I6885:9394;128:2909` | Static icon — 7 variants keyed on `NotificationType` | No tap. Visual differentiator only. |
| 6.2 | Notification content (B.1.2) | `I6885:9394;128:2911` | Text block: body + relative timestamp + optional inline link (type 5 only) | Body and timestamp are read-only. The inline "Tiêu chuẩn cộng đồng" link/icon button (type-5 only) IS a separate interactive element — see row 6.3. |
| 6.3 | Inline "Tiêu chuẩn cộng đồng" link button (type 5 only) | Inside content frame for type-5 rows — Figma node `I6885:9398;128:3467` (the type-5 sample uses component instance `6885:8386`) | Inline text+icon link | Tap → navigate to `Routes.COMMUNITY_STANDARDS` (`xms7csmDhD`). Gesture MUST be consumed before the parent row's tap handler fires (US3 acceptance). |
| 6.4 | Unread indicator (B.1.3) | `I6885:9394;128:2914` | Red dot | Rendered when the notification's `isRead == false`. Hidden when read. |

### Navigation Flow

- **From** (entry points):
  - **Header bell icon** on Home / Award Detail / Kudos hub / any screen that mounts `HomeHeader` with `onBellClick` wired. All these surfaces already exist; the Notifications screen is currently a placeholder per `Routes.kt`.
- **To** (exit destinations, per US4) — Figma frame ID + the current route binding in `AppNavigation.kt`:
  - **Kudo Detail** (Figma `T0TR16k0vH` / `5C2BL6GYXL` anonymous) → `Routes.KUDOS_DETAIL` — types 1, 2, 5. Currently bound to `PlaceholderScreen` at `AppNavigation.kt:150`; `kudoId` passed via destination's `savedStateHandle` until Kudo Detail screen ratifies its parameterization. Same handshake pattern as Search Sunner → Profile (commit `3e7f91d`).
  - **Open Secret Box** (Figma `kQk65hSYF2`) → `Routes.SECRET_BOX_OPEN` — type 3. Currently a placeholder (`AppNavigation.kt:193`).
  - **Profile-self** (Figma `hSH7L8doXB`) → `Routes.PROFILE` — types 4, 6. Currently a placeholder (`AppNavigation.kt:220`).
  - **Community Standards** (Figma `xms7csmDhD`) → `Routes.COMMUNITY_STANDARDS` — type 5 inline link only. **Shipped** (commit `504a73d`).
  - **Admin Review Content** → type 7. Per Q-N-3 (option a), MVP stubs this to a placeholder route — implementer adds `const val ADMIN_REVIEW: String = "route_admin_review"` to `Routes.kt` and binds `PlaceholderScreen(label = "Admin Review Content")` until the admin spec lands.
  - **Previous screen** — back arrow + system back via `popBackStack()`.

### Behavioral Accessibility

- Back arrow `contentDescription = "Quay lại"` (reuses the shared key).
- "Đánh dấu đọc tất cả" button exposes `Role.Button` + the localized button label as `contentDescription`.
- Notification rows expose `Role.Button` with a merged contentDescription: `{notification_body}, {relative_time}, {read|unread}`.
- Inline "Tiêu chuẩn cộng đồng" link exposes its own `Role.Button` with `contentDescription = "Tiêu chuẩn cộng đồng, button"` so TalkBack reads it as a separate affordance from the row.
- 48×48dp minimum touch target on back arrow + read-all button + inline link button.

---

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST render the screen at navigation route `Routes.NOTIFICATIONS` (NEW constant; add to `Routes.kt`). Wired from each `HomeHeader`'s existing `onBellClick` callback. Bell badge on `HomeHeader` reads the unread-count source-of-truth from a **Hilt-singleton `NotificationsCountFlow`** (per Q-N-8, option b) injected into BOTH `HomeHeader`'s host VM and `NotificationsViewModel`. This avoids a second round-trip API call from the header just to know the badge count.
- **FR-002**: On screen mount, the VM MUST issue a `GET /notifications` call to load the authenticated user's notifications, newest-first, **cursor-paginated at 30 items per page** with the cursor anchored on `(createdAt desc, id desc)` for stable tie-breaking (per Q-N-9). Subsequent pages load via infinite-scroll trigger when the user reaches the LazyColumn's last item.
- **FR-003**: Each notification's read/unread state MUST be reflected by the presence/absence of the red-dot indicator (`mms_B.1.3_Group 425`).
- **FR-004**: Tap on a notification row body MUST: (a) navigate to the type-specific destination per US4's routing table, AND (b) mark that single notification as read via a `PATCH /notifications/{id}/read` (or batched) endpoint. Navigation and the read-write fire concurrently (no await); the read-write is fire-and-forget under the VM's scope.
- **FR-005**: Tap on the "Đánh dấu đọc tất cả" button MUST: (a) **early-return without an API call if there are zero unread notifications** (per Q-N-10 — the button stays visible and enabled but a tap on the empty state is a free no-op), (b) otherwise optimistically clear every unread row's red-dot in local state, (c) issue a `PATCH /notifications/read-all` call, (d) on success — no-op (state already correct); on failure — Snackbar with retry + rollback the local state.
- **FR-006**: Tap on the inline "Tiêu chuẩn cộng đồng" link (type-5 rows only) MUST navigate to `Routes.COMMUNITY_STANDARDS` AND consume the gesture so the parent row's tap handler does NOT also fire.
- **FR-007**: Empty state: when `notifications.isEmpty()`, render the placeholder copy **"Bạn chưa có thông báo nào"** (per Q-N-1) centered in the body. The read-all button **stays visible** (per Q-N-10) — tapping it is the no-op covered in FR-005.
- **FR-008**: Error state: on `GET /notifications` failure, render an error placeholder with a retry CTA. Same pattern as the kudos hub feeds.
- **FR-009**: Pull-to-refresh: pulling the list MUST re-issue the load call. Mirrors the kudos hub's `PullToRefreshBox` pattern.
- **FR-010**: Relative-time rendering MUST follow the ladder (per Q-N-5): `<60 phút → "{N} phút trước"`, `<24 giờ → "{N} giờ trước"`, `<30 ngày → "{N} ngày trước"`, `<12 tháng → "{N} tháng trước"`, else → `"{N} năm trước"`. Implemented as a pure formatter in the VM/domain layer, NOT inside the Composable (so the value is stable across recomposition).
- **FR-011**: Bell-badge unread count MUST stay in sync with the server-side count after every state mutation. Per Q-N-8 the source of truth is a **Hilt-singleton `NotificationsCountFlow`** (lives in `kudos/notifications/data/`); both `HomeHeader`'s host VMs (Home / Awards / Kudos hub) and `NotificationsViewModel` inject it. Mutations on this screen (mark-single-read, read-all) decrement the flow's emitted value; the GET on this screen's mount refreshes it from the server. Periodically (Q-Future) the flow MAY poll the server to catch stale counts.
- **FR-012**: All user-facing copy (title, button, placeholder, 7 notification templates, inline link, error/empty states, a11y labels) MUST be sourced from `strings.xml` and be localizable. VN canonical.
- **FR-013**: Back-arrow + system-back gesture MUST behave identically — pop back-stack to entry screen.

### Technical Requirements

- **TR-001**: Performance — initial paint after navigation MUST feel perceptually instant. The list is loaded asynchronously; render the skeleton/spinner state while the network call is in flight.
- **TR-002**: Security — every notification's content MAY include the sender's full name (PII). DO NOT log notification bodies. RLS on the Supabase notifications table MUST scope reads to the authenticated `auth.uid()`. Role-gated notifications (type 7) MUST be filtered server-side by role membership, NOT client-side.
- **TR-003**: Integration — **VERIFIED 2026-05-15**: `Routes.NOTIFICATIONS` does NOT exist in `Routes.kt`. Today the bell-icon callback (`onBellClick`) on Home opens a `BottomSheet` (`HomeScreen.kt:90`: `notificationsSheetVisible = true`); on the Kudos hub it is a no-op (`KudosScreen.kt:68` — "wires in Phase 13 polish"). Award Detail's bell click also routes to the same Home sheet pattern. Implementation steps to integrate this new screen:
   1. Add `const val NOTIFICATIONS: String = "route_notifications"` to `Routes.kt`.
   2. Register `composable(Routes.NOTIFICATIONS) { NotificationsScreen(...) }` in `AppNavigation.kt`.
   3. **Remove** the `notificationsSheetVisible` bottom-sheet state from `HomeScreen.kt` (and any associated `ModalBottomSheet` composable). Swap `onBellClick = { notificationsSheetVisible = true }` for `onBellClick = onNavigateToNotifications` where the caller passes a `navController.navigate(Routes.NOTIFICATIONS)` lambda from `AppNavigation.kt`.
   4. Same swap for Kudos hub (`KudosScreen.kt:68`) and Award Detail.
- **TR-004**: Accessibility — Constitution III. Listed under "Behavioral Accessibility" above.
- **TR-005**: Constitution alignment — feature-first package: `com.example.aiddproject.kudos.notifications.{ui, data, domain}`. Hilt VM exposes `StateFlow<NotificationsUiState>`. Repository (interface + Supabase impl + Demo impl) follows the established kudos-package pattern. Demo impl seeds the 7 type variants for testing.

### Key Entities

```
NotificationsUiState {
    val notifications: List<NotificationItem>      // newest-first
    val isLoading: Boolean
    val errorRes: Int?                             // null when no error
}

NotificationItem {
    val id: String                                  // UUID server-issued
    val type: NotificationType
    val isRead: Boolean
    val createdAt: Instant                          // for sorting + relative-time formatting
    val payload: NotificationPayload                // type-specific destination args
    val displayBody: String                         // pre-rendered server-side
}

enum NotificationType {
    KUDOS_RECEIVED,        // → Kudo Detail
    HEART_RECEIVED,        // → Kudo Detail (own kudo)
    SECRET_BOX_UNLOCK,     // → Open Secret Box
    LEVEL_UP,              // → Profile-self
    CONTENT_HIDDEN,        // → Kudo Detail (hidden); inline link → Community Standards
    BADGE_COLLECTED,       // → Profile-self (badge section)
    REVIEW_REQUEST,        // → Admin Review (admin/reviewer only)
}

sealed interface NotificationPayload {
    data class KudoRef(val kudoId: String, val isAnonymous: Boolean) : NotificationPayload
    data object SecretBox : NotificationPayload
    data object Profile : NotificationPayload
    data class Review(val reviewCount: Int) : NotificationPayload
}
```

The `displayBody` is pre-rendered server-side from the type template + interpolated values (sender name, level name, reward name, etc.) so the client doesn't have to re-template. This decouples the client from any future template-copy tweak.

### State Management

- **Local component state**: `LazyListState` for scroll position (`rememberLazyListState`).
- **Hoisted ViewModel state**: `NotificationsUiState` (above). Hilt-injected `NotificationsViewModel` exposes `StateFlow`. Repository owns network I/O.
- **Loading state**: top-level boolean. Renders a centered spinner OR the list with a footer spinner when paginating.
- **Error state**: top-level `errorRes`. Renders an error placeholder with a retry CTA when `isLoading == false && errorRes != null && notifications.isEmpty()`. When errors happen on optimistic updates (US2 rollback), surface via a Snackbar — don't blow away the list.
- **Empty state**: `notifications.isEmpty() && !isLoading && errorRes == null` → placeholder per Q-N-1.
- **Cache invalidation**: pull-to-refresh + screen re-mount both re-issue the load. Mutation endpoints (mark-read, read-all) update the local list optimistically; server confirmation just sets the source-of-truth.
- **Optimistic updates**: YES for both single-mark-read (FR-004) and read-all (FR-005). Rollback on failure.

---

## API Dependencies

| Endpoint | Method | Purpose | Triggered by | Status |
|----------|--------|---------|--------------|--------|
| `/notifications` | GET | Load the authenticated user's notifications (newest-first, paginated) | Screen mount + pull-to-refresh | **Predicted** |
| `/notifications/{id}/read` | PATCH | Mark a single notification as read | Row tap (FR-004) | **Predicted** |
| `/notifications/read-all` | PATCH | Mark every unread notification as read | "Đánh dấu đọc tất cả" button (FR-005) | **Predicted** |
| `/notifications/unread-count` | GET | Bell-badge count — light query (returns just an integer, no notification bodies) used to seed the Hilt-singleton `NotificationsCountFlow` at app cold start before the user opens the Notifications screen. After the first `/notifications` GET, the flow derives the count from the loaded list + tracks mutations locally; this endpoint is the cold-start cheaper-than-`/notifications` fallback. | App start `LaunchedEffect` on the AuthGate / Home (Q-N-8 architecture) | **Predicted** |

Server-side concerns out of scope here:
- Push delivery (FCM/APNs)
- Notification generation (cron / DB triggers / etc)
- Role-based filtering (auth.uid() + role check on `select` policies)

---

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 100% of taps on a notification row route to the correct destination per US4's routing table — zero nav crashes, zero wrong-destination bugs in QA + first 7-day production telemetry window.
- **SC-002**: After tapping "Đánh dấu đọc tất cả", every red-dot indicator is removed in a single recomposition (no visible flicker, no staggered animation gaps). Verified with an instrumented test.
- **SC-003**: The bell-badge count on the entry screen is consistent with the notifications-list state within 1 second of any mutation. Verified by a manual round-trip test (open Notifications → mark one read → tap back → assert badge decremented).
- **SC-004**: TalkBack walk-through (sighted + screen-reader pair) reads each row as `{body}, {time}, {read|unread}, button` in source order. Inline "Tiêu chuẩn cộng đồng" link reads as its own button.
- **SC-005**: Localized VN copy matches the Figma source word-for-word at GA.

---

## Out of Scope

- **Push notifications** (FCM / APNs / foreground service / channels). Separate platform-integration spec.
- **Notification generation** (the server-side rules that EMIT a notification when a kudo is sent, a heart received, etc). Backend concern.
- **Per-type filtering / tabs** ("All / Mentions / Hearts" tabs). Future enhancement.
- **Swipe-to-dismiss / archive** on individual rows. Future enhancement.
- **Admin Review screen itself**. Linked from type-7 row tap but specced separately; stub to placeholder until that screen ships.
- **Notification settings** (per-type mute, "do not disturb" hours). Separate Settings spec.
- **Real-time push from the server while the screen is open** (Supabase Realtime / WebSocket). Pull-to-refresh is the consistency mechanism at MVP.

---

## Resolved Decisions (all answered 2026-05-15)

| ID | Question | Decision | Codified in |
|----|----------|----------|-------------|
| Q-N-1 | Empty-state copy when `notifications.isEmpty()`. | **"Bạn chưa có thông báo nào"**, centered in the body. | US1.5, FR-007, Edge Cases |
| Q-N-2 | Type-6 (Badge collected) routing — dedicated badge deep-link or just Profile-self? | **Defer.** For MVP both type-4 (Level up) and type-6 (Badge collected) route to the same `Routes.PROFILE` entry. The badge-section anchor will be added later when the Profile spec ratifies its sub-section deep-link scheme. | US4 Scenario 6 |
| Q-N-3 | Type-7 (Review request) routes to Admin Review, but that screen isn't specced. | **Option (a) — stub to a placeholder.** Type-7 row tap navigates to `PlaceholderScreen(label = "Admin Review Content")` until the admin spec lands. Notification routing stays testable; type-7 isn't dropped. | US4 Scenario 7, FR-004 |
| Q-N-4 | Visual ordering — mix read + unread, push read down, or "Earlier" section? | **Strict newest-first mix.** No separator, no per-state sub-grouping. | US1.1, Edge Cases |
| Q-N-5 | Relative-time cap — extend "tháng" indefinitely, or add a "năm trước" tier? | **Add "{N} năm trước"** for entries ≥ 12 months. Full ladder: `<60 phút → x phút trước; <24 giờ → x giờ trước; <30 ngày → x ngày trước; <12 tháng → x tháng trước; else → x năm trước`. | FR-010, Edge Cases |
| Q-N-6 | Real-time updates via WebSocket/Supabase Realtime, or just pull-to-refresh? | **Pull-to-refresh only.** No WebSocket subscription at MVP. Pull-to-refresh is the consistency mechanism. | FR-009, Edge Cases, State Management |
| Q-N-7 | Title-bar copy — keep Figma's "Notifications" or localize to "Thông báo"? | **"Thông báo"** (VN canonical). Figma's English is the EN fallback. Sourced from `strings.xml`. | Component table row 3 |
| Q-N-8 | Bell-badge unread-count source: separate API vs. Hilt-singleton shared flow? | **Option (b) — Hilt-singleton `NotificationsCountFlow`.** Lives in `kudos/notifications/data/`; both Home/Awards/Kudos-hub VMs and `NotificationsViewModel` inject it. Mutations on this screen decrement the emitted value; GET-on-mount refreshes from server. Tighter coupling but no second round-trip per header render. | FR-001, FR-011, TR-005 |
| Q-N-9 | Pagination — confirm 30/page cursor on `createdAt + id`? | **Confirmed.** Cursor anchored on `(createdAt desc, id desc)` for stable tie-breaking. Subsequent pages load via infinite-scroll on the LazyColumn's last item. | FR-002 |
| Q-N-10 | Read-all button when `notifications.isEmpty()` — hide, disabled, or always-visible? | **Always visible, always enabled.** Handler early-returns without an API call when there are zero unread items — a free no-op. | Component table row 4, FR-005 |
| Q-N-11 | Sheet vs. dedicated screen — current code uses a `BottomSheet` on Home's bell tap. | **Confirmed: replace the sheet with the dedicated route.** Same migration shipped recently for `Routes.COMMUNITY_STANDARDS` (commit `504a73d`) and `Routes.SEARCH` / Search Sunner (commit `3e7f91d`). Remove `notificationsSheetVisible` state from `HomeScreen.kt`, wire `onBellClick` to `navController.navigate(Routes.NOTIFICATIONS)` in `AppNavigation.kt`, same swap for Kudos hub + Award Detail. | Overview migration note, TR-003 |

All 11 questions are answered. The spec is fully self-contained for planning.

---

## Dependencies

- [x] Constitution document exists (`.momorph/constitution.md`) — Principles I (feature-first), II (Hilt + StateFlow + Coroutines + Supabase), III (M3 + a11y + 48dp), IV (RLS-scoped reads), V (TDD on the VM) all apply.
- [ ] API specifications — `/notifications`, `/notifications/{id}/read`, `/notifications/read-all`, `/notifications/unread-count` — NEW endpoints; **predicted** in this spec. Backend team to author.
- [ ] Database design — NEW `notifications` table required (server-side concern). Out of scope here but block.
- [x] Screen flow documented (`.momorph/SCREENFLOW.md`) — row appended by the screenflow agent in the same change-set.
- [x] Related specs exist for destinations: Community Standards (`xms7csmDhD`, shipped), Kudo Detail (`T0TR16k0vH` / `5C2BL6GYXL`, awaiting), Open Secret Box (`kQk65hSYF2`), Profile-self (`hSH7L8doXB`).
- [ ] Admin Review Content screen spec — does not exist; required to fully implement type-7 routing.

---

## Notes

- **Two Figma component variants for the row** (`mms_B.1_Noti` `6885:9394` and `Noti` `6885:9395`+). They are functionally identical — different Figma component IDs because the first instance uses `componentId: 6885:8820` while the rest use `6885:8828` / `6885:8836`. The implementation MUST render them via ONE Compose composable parameterized on `NotificationType`, NOT as two separate composables.
- **The 7 types are AUTHORITATIVE** — adding an 8th type requires a spec amendment, a `NotificationType` enum value, an icon drawable, a string template, and a routing-table entry. Don't sneak types in.
- **`displayBody` server-side templating** — keeps client/server template strings in sync without re-deploying the client. The Figma template strings are the source of truth for the VN copy; the server interpolates the variables (sender name, level name, reward name) before sending.
- **The icon set** — Figma uses 7 distinct icons (envelope/heart/gift/star/warning/badge/pen). These need to be downloaded from Figma (or matched against existing Material icons where appropriate) at implementation time. Material has equivalents for most: `Email`, `Favorite`, `CardGiftcard`, `Star`, `Warning`, `Shield`, `Edit` — but the Figma-exported colored versions are the design source of truth.
- **No MoMorph test cases on this frame** (`get_frame_test_cases` returned `[]`). The Acceptance Scenarios in US1–US4 are the authoritative test contract.
