# Feature Specification: Sun*Kudos — Search Sunner (default state)

**Frame ID**: `3jgwke3E8O`
**Frame Name**: `[iOS] Sun*Kudos_Search Sunner`
**File Key**: `9ypp4enmFmdK3YAFJLIu6C`
**Created**: 2026-05-14
**Status**: Draft

---

## Overview

People-search surface for the Sun*Kudos product. Lets a Sunner locate a colleague by name (or email) and open that colleague's profile.

This spec covers the **default / empty-query state** of the screen: an inactive search bar plus a "Recent" list of up to 5 previously-searched Sunners. One sibling sub-flow is referenced but out of scope:

- **`hldqjHoSRH` ([iOS] Sun*Kudos_Searching)** — the active typing state once the search bar is focused. Real-time query → results swap the "Recent" list. Specced separately.

**Entry**: tapping the search box in the app header (Home, Kudos hub, etc.) navigates here via the existing `onNavigateToSearch` callbacks. There is NO Viết Kudo recipient-prefill flow from this screen — the parent Viết Kudo spec's earlier reference to "Tap a user in results — prefill `recipientUserId`" is **superseded** by the PM decision (Q-S-5): Viết Kudo's inline recipient dropdown (`5MU728Tjck`) is the sole recipient-pick affordance.

**Out of scope for this screen**: editing the directory, server-side ranking, push notifications about searches, follow/connect actions, advanced filters (department, role, tier), Viết Kudo recipient prefill.

---

## User Scenarios & Testing *(mandatory)*

### User Story 1 — Open a recently-searched colleague's profile (Priority: P1)

A Sunner returns to the Search Sunner screen and sees their most recently searched colleagues at the top. They tap one to jump straight to that colleague's profile without re-typing.

**Why this priority**: The empty-state list IS the primary affordance on this screen when entering from a generic "search" surface (Home / Kudos hub). Without it, every search starts from zero, which is hostile to repeat lookups (managers checking the same reports, peers acknowledging the same teammates). P1 because the screen is otherwise just a placeholder.

**Independent Test**: Open Search Sunner from any non-Viết-Kudo entry (Home search button, Kudos hub search bar). With a populated recent-search store, verify recent items render newest-first, tapping any item navigates to the Profile screen for that user, and back-navigation returns to Search Sunner with the recent list intact.

**Acceptance Scenarios**:

1. **Given** I have searched for ≥ 1 Sunner in the past, **When** the Search Sunner screen mounts, **Then** the screen renders a "Recent" section with my recent searches (most-recent at the top, capped at the initial visible count), each row showing avatar + full name + department code.
2. **Given** I am viewing the recent list, **When** I tap any row, **Then** the app navigates to that Sunner's Profile screen (`bEpdheM0yU` for someone else, `hSH7L8doXB` for myself in the edge case) AND the tapped Sunner's record is promoted to the top of the recent list (or refreshed if already there). Whether the bottom-nav "Profile" tab visually becomes active during this navigation is determined by the Profile screen's own nav-graph integration — not by this screen.
3. **Given** I am on Profile after tapping a recent item, **When** I tap back, **Then** I return to Search Sunner with my recent list ordering preserved (i.e. the promotion from step 2 persists).
4. **Given** I have NO recent searches stored (fresh install or after clearing every entry), **When** the screen mounts, **Then** the "Recent" label + "View all" button do NOT render at all (no empty-state placeholder per Q-S-1), and the screen shows the search bar alone — encouraging the user to type.

---

### User Story 2 — Remove a stale colleague from the recent list (Priority: P2)

A Sunner notices an outdated entry in their recent searches (someone they no longer work with, a typo'd duplicate, or just clutter). They tap the X on that row to remove it instantly, without confirmation.

**Why this priority**: P2 because users can live with a noisy list temporarily — but a clean recent list is the difference between this screen being useful and being a junk drawer. The Figma component spec for `mms_5.1_mm_media_close` (Node `6891:22103`) explicitly says "không cần xác nhận" (no confirmation needed), so we implement direct-tap removal.

**Independent Test**: Open Search Sunner with ≥ 1 recent item. Tap the X icon on a row. Verify that row disappears immediately, the recent list shrinks by 1, and the change persists across app restarts (DataStore / SharedPrefs write).

**Acceptance Scenarios**:

1. **Given** the recent list contains a Sunner I want to forget, **When** I tap the X icon on that row, **Then** the row is removed from the list immediately (no dialog), the list rebalances vertically, AND the change persists locally (verified by killing + restarting the app).
2. **Given** I just removed the LAST recent item, **When** the list shrinks to 0, **Then** the "Recent" label + "View all" button disappear and the empty state from US1 Scenario 4 takes over.
3. **Given** I removed an item by mistake, **When** I look for an "Undo" affordance, **Then** there is NONE per Q-S-2 (PM confirmed: matches Figma literally — no Snackbar, no confirmation, no undo. The recent list is local; the user can re-add by searching again).

---

### User Story 3 — Expand / collapse the recent list (Priority: P2)

A Sunner suspects the row they want is among their recent searches but isn't in the currently visible window (the default state shows the 2 most-recent rows — Q-S-8). They tap "View all" to expand the full recent history; tap "Collapse" to fold it back.

**Why this priority**: P2 because the typical recent-list use-case (top 2 results) is already covered by US1. The expand/collapse toggle handles the corner case of "I searched this person 4 entries ago and want them again without typing".

**Independent Test**: With ≥ 3 recent items stored, verify that the default screen renders the 2 most-recent rows + "View all" button, that tapping "View all" expands the list to show all (up to 5) recent items and the button relabels to "Collapse" (Q-S-3), and that tapping "Collapse" folds the list back to 2 rows and the button re-labels to "View all".

**Acceptance Scenarios**:

1. **Given** I have 5 recent searches stored but only 2 are visible by default (Q-S-8), **When** I tap "View all", **Then** the list expands in place to show all 5 entries AND the button label changes to "Collapse" (Q-S-3 resolution: option B).
2. **Given** the list is expanded and the button reads "Collapse", **When** I tap "Collapse", **Then** the list shrinks back to the 2 most-recent rows AND the button label reverts to "View all".
3. **Given** I have ≤ 2 recent searches stored (the default-visible count of 2 per Q-S-8), **When** the screen mounts, **Then** the "View all" button does NOT render at all (Q-S-4 resolution: hide, not disable).
4. **Given** the list is expanded, **When** I tap any row, **Then** standard US1 navigation applies — no special behavior because the list is expanded.

---

### User Story 4 — Start a live search by typing (Priority: P1)

A Sunner taps the search bar and starts typing a name or email. The screen transitions to the active "Searching" state (frame `hldqjHoSRH`) which lives outside this spec but is reached from here.

**Why this priority**: P1 because this is the primary find-someone-new path; the recent list (US1) only helps for repeat lookups. Without active typing, the directory is unreachable for first-time targets.

**Independent Test**: Tap the search bar. Verify the screen transitions to the Searching state (recent list disappears or moves out of view, soft keyboard appears, the search bar shows the active/focused state).

**Acceptance Scenarios**:

1. **Given** the search bar is inactive on screen mount, **When** I tap it, **Then** focus moves to the input field, the soft keyboard slides up, and the screen transitions to the Searching state (`hldqjHoSRH`) which owns the real-time results rendering.
2. **Given** I am on the active Searching state, **When** I tap the back arrow OR clear the query AND blur, **Then** I return to this default-state screen with my recent list unchanged.

---

### Edge Cases

- **Cold start, no recent searches**: Empty state (US1.4). Screen shows only the search bar.
- **Network down**: Recent list still renders (it's local). Tapping a row to open Profile may surface a Profile-side error; not this screen's concern.
- **Stale avatar URLs**: Recent list stores `avatarUrl` at capture time. If the colleague has since changed their avatar, the recent list shows the old image until next search. Avatar component should already gracefully fall back to a placeholder on 404 (cross-feature concern — see Avatar conventions in `kudos/ui/components/`).
- **Stale `userId` in recent list** (deleted account, revoked permissions, role change): the row still renders from local data. Tapping it routes to Profile (or pops back to Viết Kudo). The destination screen — not this screen — owns the "user not found / inaccessible" error UI. This screen MUST NOT pre-validate `userId` existence on row tap (would defeat offline-functional FR-011). The recent-list garbage collection (silently remove rows whose `userId` is unreachable) is a deferred polish item; out of MVP scope.
- **TalkBack flow**: Each recent row reads as "Avatar of [Name], [Department], button" + the X is a sibling button "Xoá [Name] khỏi danh sách gần đây".
- **System font scale 200%**: List rows reflow vertically; X stays right-aligned. No truncation.
- **Concurrent removal**: User taps X on row A and row B in quick succession. Both should remove independently; no race condition because removal is a synchronous local-list mutation.
- **Locale switch while on screen**: "Recent" / "View all" / "Search Sunner" placeholder strings re-render in the active locale on next composition.
- **Bottom-nav tap while on screen**: Standard nav-graph behavior — switches to the tapped destination (SAA / Awards / Kudos / Profile). Search Sunner is a destination off the Kudos tab, so re-tapping Kudos pops back to the hub; tapping other tabs leaves Search Sunner.

---

## UI/UX Requirements *(from Figma)*

### Screen Components

| # | Component | Node ID | Type | Behavior |
|---|-----------|---------|------|----------|
| 1 | Status bar | `6891:21278` | iOS system status bar | No app-side behavior (system chrome). Android renders the platform status bar with `statusBarsPadding`. |
| 2 | Back arrow | `6891:21281` (component instance `6891:16823`) | Icon button. Lives inside `✏️ Left Accessory` frame (`6891:21280`), which is a **sibling** of the search bar within `_TopNavigation-content` (`6891:21279`) — NOT a child of the search bar despite how the Figma design-item description groups them. | Tap → pop back-stack to entry screen. Reuses the project-wide chevron-left vector drawable (`R.drawable.ic_back_chevron`). 48dp touch target via M3 `IconButton` default. |
| 3 | Search bar | `6891:22074` | Input field, INACTIVE state — this screen owns only the placeholder render and the on-tap transition. | Tap → transition to active state (Searching frame `hldqjHoSRH`); soft keyboard slides up. The `1–100 chars` validation per Figma's design-item spec (`required=false, minLength=1, maxLength=100`) is enforced once the user STARTS typing — that responsibility belongs to the sibling Searching-state spec, not this default-state screen. This screen merely surfaces the placeholder copy "Search Sunner". |
| 4 | "Recent" label | `6891:22079` | Static TEXT | Read-only. Hidden entirely when the recent list is empty (Q-S-1: hide, no placeholder). |
| 5 | "View all" / "Collapse" button | `6891:22081` | Text link / button | Tap → toggles between the collapsed (2 rows visible — Q-S-8) and expanded (all up to 5 rows visible) states. Label flips: "View all" ↔ "Collapse" (Q-S-3 resolution: option B). Hidden when `recentSunners.size ≤ 2` (Q-S-4: hide, do not disable). |
| 6 | Recent item row (×N) | `6891:22087`, `6891:22109` (and any further sibling instances) | Composite list item: avatar + name + dept code + close icon | Tap on the row body → navigate to Profile screen for that user (`bEpdheM0yU` for others, `hSH7L8doXB` for self) per Q-S-5. NO entry-mode dual-behavior. Promotes the row to position 0 of the recent list on tap. |
| 7 | Recent-item remove (X) (×N) | `6891:22103`, `6891:22110` | Icon button per row | Tap → remove that Sunner from the recent list immediately (no confirmation, no Snackbar — per Figma). Local-list mutation only, persisted to DataStore. Stops propagation so the parent row's tap handler does NOT fire. |
| 8 | Bottom nav bar | `6891:21297` (component `6885:8076`) | 4-tab system component (SAA / Awards / Kudos / Profile) | Reused chrome. Active tab is Kudos when arriving from the Kudos hub (verify per entry context). |

### Navigation Flow

- **From** (entry points):
  - **Header search box** on Home (`OuH1BUTYT0`), Awards (`c-QM3_zjkG` etc.), Kudos hub (`fO0Kt19sZZ`) — i.e. wherever the app header surfaces the magnifying-glass search trigger. All these screens already call `navController.navigate(Routes.SEARCH)` via existing `onNavigateToSearch` callbacks (4 sites verified in `AppNavigation.kt`). Per Q-S-6: this is the SOLE entry-point affordance for the screen.
  - The Viết Kudo composer does NOT navigate here (Q-S-5). Its recipient picker is the inline dropdown `5MU728Tjck` only.

- **To** (exit destinations):
  - **Profile** (`bEpdheM0yU` other, `hSH7L8doXB` self) when a recent row is tapped (FR-010).
  - **Searching state** (`hldqjHoSRH`) when the search bar is tapped (FR-009).
  - **Previous screen** (back-arrow tap or system back, FR-013).

### Behavioral Accessibility

- Back-arrow `contentDescription = "Quay lại"` (reuses the shared `a11y_community_standards_back` string, OR a new dedicated key — implementer's call).
- Recent rows expose `Role.Button` semantics with the merged accessibility text "Avatar [Name], [Department], button".
- X buttons expose `Role.Button` with `contentDescription = "Xoá [Name] khỏi danh sách gần đây"`.
- Search bar exposes `Role.SearchBox`-like semantics (the M3 `SearchBar` component meets this by default).
- 48×48 dp minimum touch target on back arrow, X buttons, and row body — Material default.

---

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST render the screen at navigation route `Routes.SEARCH` (existing, at `Routes.kt:34`, currently bound to a `PlaceholderScreen(label = "Search")` at `AppNavigation.kt:193`). The implementation swaps the placeholder for `SearchSunnerScreen(...)`. Per Q-S-7 resolution: reuse, do NOT rename — the 4 existing `onNavigateToSearch` call sites (Home, Awards, Kudos hub, …) keep working unchanged.
- **FR-002**: Recent list MUST persist locally (DataStore preferred; SharedPrefs acceptable) keyed on the authenticated user's ID. Survives app restart. Cleared on logout.
- **FR-003**: Recent list MUST be capped at 5 entries. Adding a 6th entry evicts the oldest. Order: most-recent-first.
- **FR-004**: Each recent entry stores: `userId: String` (required), `fullName: String` (required), `departmentName: String?` (nullable for Sunners without a department), `avatarUrl: String?` (nullable; UI falls back to placeholder).
- **FR-005**: Tapping a recent row MUST promote that entry to position 0 (most-recent) on commit. If the row is already at position 0, no-op (still update the timestamp for last-accessed sort).
- **FR-006**: Tapping the X on a recent row MUST remove that entry immediately, with no confirmation, no undo Snackbar, no dialog (per Q-S-2). Removal MUST persist to DataStore synchronously.
- **FR-007**: The default-visible count for the recent list when collapsed is **2** (per Q-S-8). When `recentSunners.size > 2`, the screen renders the 2 most-recent rows plus a "View all" button. When `recentSunners.size <= 2`, the "View all" button MUST NOT render (per Q-S-4 — hide, do not disable).
- **FR-008**: The "View all" / "Collapse" button MUST toggle the list visibility (per Q-S-3): tapping "View all" expands to show all (≤ 5) rows AND relabels the button to "Collapse"; tapping "Collapse" folds back to 2 rows AND relabels to "View all".
- **FR-009**: Tapping the search bar MUST transition to the Searching state (`hldqjHoSRH`). The transition implementation (separate composable / sealed UI state / nav destination) is the implementer's call.
- **FR-010**: Tap on a recent row MUST navigate to the Profile destination (currently `Routes.PROFILE` = `"route_profile"`, bound to a `PlaceholderScreen`). Per Q-S-5: this is the ONLY tap-row behavior — there is NO entry-mode dual-behavior, NO Viết Kudo recipient prefill. The Profile screen has no parameterized route helper yet, so `userId` is passed via the destination's `savedStateHandle` (calling code sets `navController.getBackStackEntry(Routes.PROFILE).savedStateHandle["userId"] = tappedUserId` immediately before `navigate(...)`). When the Profile screen ships its own parameterization scheme, this spec's implementer should align — minor refactor expected.
- **FR-011**: Screen MUST be functional offline. Recent list reads from local storage; only the live-search state (`hldqjHoSRH`) requires the network.
- **FR-012**: All user-facing copy (placeholder "Search Sunner", "Recent", "View all", "Collapse", a11y labels) MUST be sourced from `strings.xml` and be localizable. VN canonical.
- **FR-013**: Back-arrow + system-back gesture MUST behave identically — pop back-stack to entry screen.

### Technical Requirements

- **TR-001**: Performance — list render MUST be backed by a `LazyColumn` (cheap for ≤ 5 items, future-proof for "View all history" expansion). First paint MUST feel perceptually instant on a mid-tier device, with NO network or Supabase I/O during composition; the only allowed initialization-time read is the per-user DataStore prefetch via the VM's `init {}` block.
- **TR-002**: Security — recent list is per-user. On logout, the DataStore key MUST be cleared. No cross-user leakage. No PII in logs (FR-005 fields are display data, not credentials, but treat as PII for the OWASP Mobile Top-10 hygiene rule).
- **TR-003**: Integration — REUSE the existing `Routes.SEARCH` constant (at `Routes.kt:34`, value `"route_search"`). In `AppNavigation.kt`, replace the line-193 `PlaceholderScreen(label = "Search")` body with `SearchSunnerScreen(...)`, identical pattern to the Community Standards swap shipped in commit `504a73d`. No nav-graph schema change, no new route constant, no `savedStateHandle` parameterization needed at the route level (Q-S-5 collapses the dual-behavior; the screen has a single deterministic on-tap path). The 4 existing `onNavigateToSearch` call sites continue to work unchanged.
- **TR-004**: Accessibility — Constitution III. Listed under "Behavioral Accessibility" above.
- **TR-005**: Constitution alignment — feature-first package: `com.example.aiddproject.kudos.search.{ui, data, domain}`. ViewModel needed (the recent list is observed state). Repository owns DataStore I/O; UI consumes a `StateFlow<SearchSunnerUiState>`.

### Key Entities

```
SearchSunnerUiState {
    val recentSunners: List<RecentSunner>     // 0..5
    val isViewingAll: Boolean                  // toggled by "View all" / "Collapse" button (FR-008)
}

RecentSunner {
    val userId: String
    val fullName: String
    val departmentName: String?
    val avatarUrl: String?
    val lastSearchedAt: Instant                // for ordering
}
```

The `RecentSunner` projection is intentionally smaller than the full `SunnerNode` domain model already used elsewhere in the kudos package — it stores only what the row needs to render, decoupling from any future `SunnerNode` schema changes.

### State Management

- **Local component state**: scroll position (`rememberScrollState` / `LazyListState`); the "View all" expand boolean if owned at composable level instead of VM.
- **Hoisted ViewModel state**: `SearchSunnerUiState` (above). Hilt-injected `SearchSunnerViewModel` with `StateFlow<SearchSunnerUiState>`. Owns the DataStore I/O via a `RecentSunnerRepository`.
- **Loading state**: NONE in the default state (DataStore reads are synchronous-fast; show the cached list immediately). The Searching state (`hldqjHoSRH`) owns its own loading.
- **Error state**: NONE in the default state (no network). DataStore read failure should surface a snackbar but is rare enough to be a polish task, not MVP.
- **Empty state**: when `recentSunners.isEmpty()` → hide the "Recent" label + "View all" button (per Q-S-1). Screen shows search bar alone.
- **Cache invalidation**: on logout the repository CLEARS the DataStore key. No other invalidation needed — every mutation (add, remove, promote) writes through synchronously.
- **Optimistic updates**: NOT NEEDED. The list IS the local source of truth; "remove" is a direct mutation, not a server call.

---

## API Dependencies

| Endpoint | Method | Purpose | Triggered by | Status |
|----------|--------|---------|--------------|--------|
| *(none for this screen)* | — | Default-state recent list is local-only. | — | N/A |
| (See sibling spec for `hldqjHoSRH` for the live-search endpoint.) | — | Sunner directory search. | Active typing on the Searching state. | Out of scope here |

The recent list is **local**. The only mutation that crosses the network is the eventual live-search query on the Searching state, which is a separate frame.

---

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 100% of taps on a recent row successfully navigate to the Profile destination — zero nav crashes, zero wrong-destination bugs in QA and the first 7-day production telemetry window.
- **SC-002**: Recent-list mutations (add via tap, remove via X) persist across app restart in every QA test case (kill + relaunch + verify list state).
- **SC-003**: TalkBack walk-through (sighted facilitator + screen-reader user pair) reads each row's name + department + X-button affordance correctly, in source order, during the QA gate.
- **SC-004**: Tap-on-search-bar transition to the Searching state feels seamless (no flicker, no losing the typed character on the next composition).

---

## Out of Scope

- **The active typing / live-search state** (`hldqjHoSRH`) — separate spec.
- **Advanced filters** (department, tier, role) on the recent list — future enhancement.
- **A server-side "people I've searched" sync** across devices — local-only at MVP.
- **Snackbar "Undo" after removing a recent row** — explicitly excluded per Q-S-2.
- **Viết Kudo recipient prefill from Search Sunner** — explicitly excluded per Q-S-5. Use Viết Kudo's inline recipient dropdown (`5MU728Tjck`) instead.
- **Sticky/pinned colleagues** (favorites) — separate feature.
- **Group search** (search multiple Sunners at once) — not in scope.
- **Recent-search analytics** (telemetry on which rows users tap most) — separable concern.

---

## Resolved Decisions (all answered 2026-05-14)

| ID | Question | Decision | Codified in |
|----|----------|----------|-------------|
| Q-S-1 | Empty-state UI when `recentSunners.isEmpty()`. | **Hide** the "Recent" label + "View all" button entirely. No placeholder copy. The screen renders the search bar alone. | US1.4, Component table row 4 |
| Q-S-2 | Undo Snackbar after removing a recent item. | **No undo, no confirmation, no Snackbar.** MVP follows Figma literally. The user can re-add by searching again. | US2.3, FR-006 |
| Q-S-3 | "View all" button behavior after expanding. | **Option B — relabel to "Collapse".** Tapping "Collapse" folds the list back to 2 rows and relabels to "View all". | US3.1, US3.2, FR-008, Component table row 5 |
| Q-S-4 | Behavior when `recentSunners.size <= defaultVisibleCount`. | **Hide** the "View all" button entirely. Do NOT disable. | US3.3, FR-007, Component table row 5 |
| Q-S-5 | Entry-mode dual behavior (Profile vs. Viết Kudo recipient prefill). | **Tap row → open Profile, always.** There is NO Viết Kudo entry mode. US5 removed. The parent Viết Kudo spec's reference to "Search Sunner ... prefill recipient" is **superseded** and should be cleaned up in a follow-up edit of `7fFAb-K35a/spec.md`. | Overview, US1, FR-010, Out of Scope |
| Q-S-6 | Where the user enters Search Sunner. | **Tap the search box in the app header** (Home / Awards / Kudos hub / etc., wherever the magnifying-glass header search trigger appears). The 4 existing `onNavigateToSearch` call sites already wire this. No new entry point needed. | Navigation Flow → From |
| Q-S-7 | Route reuse vs. rename. | **Reuse `Routes.SEARCH`** (existing at `Routes.kt:34`, currently bound to `PlaceholderScreen` at `AppNavigation.kt:193`). Swap the placeholder body for `SearchSunnerScreen(...)` — same pattern as commit `504a73d` for Community Standards. No new route constant. | FR-001, TR-003 |
| Q-S-8 | Default-visible count when collapsed. | **2 rows** (matches Figma's rendered state). When `recentSunners.size > 2`, the screen shows the 2 most-recent + "View all"; when `≤ 2`, the screen shows all rows and hides "View all". | US3, FR-007 |

All 8 questions are answered. The spec is fully self-contained for planning.

---

## Dependencies

- [x] Constitution document exists (`.momorph/constitution.md`) — Principles I (feature-first packaging), II (Hilt + StateFlow + DataStore), III (M3 + 48dp + TalkBack), and V (TDD on the VM) apply.
- [ ] API specifications — N/A for this screen (live-search lives in sibling spec).
- [ ] Database design — N/A (no Postgres reads). DataStore is per-device local storage.
- [x] Screen flow documented (`.momorph/SCREENFLOW.md`) — row #16 appended by the screenflow agent on 2026-05-14, status `spec_draft`, pointing at this spec file.
- [x] Parent Viết Kudo spec (`specs/7fFAb-K35a-iOS-Sun-Kudos-Viet-Kudo/spec.md`) — line 720 contains a stale "Search Sunner ... prefill recipient" reference that is **superseded** by Q-S-5 in this spec. Follow-up edit needed on the parent to remove that stale line.
- [ ] Sibling Searching-state spec (`hldqjHoSRH`) — not yet authored. Required for full live-search behavior; this spec is implementable without it (default state can ship first).
- [x] `Routes.SEARCH = "route_search"` exists in `Routes.kt:34`. Currently bound to `PlaceholderScreen(label = "Search")` at `AppNavigation.kt:193`. Recommended resolution: reuse, don't rename (Q-S-7).
- [ ] `Routes.PROFILE = "route_profile"` exists in `Routes.kt:35` but has NO parameterized helper (e.g. `Routes.profile(userId)`) and is currently bound to a `PlaceholderScreen`. The parameterization scheme — query arg vs. nav-pattern segment — is owned by the Profile screen's own spec/implementation. This screen will pass `userId` via whatever scheme that screen ratifies; until then, the row-tap `DEFAULT` flow degrades to `navigate(Routes.PROFILE)` with userId on `savedStateHandle`.

---

## Notes

- **Two visible Recent rows in the Figma**: nodes `6891:22087` (inside Frame 557) and `6891:22109` (inside Frame 558). Both render the same `kết quả search 3` component (`490:5562`) with paired close-button siblings (`6891:22103` and `6891:22110`). Implementation MUST treat them as instances of one component rendered in a loop, NOT as two distinct hardcoded rows.
- **The "iOS Searching" sibling frame `hldqjHoSRH`** is the active-typing state. It is OUT OF SCOPE for this spec but the transition trigger (US4) is defined here. A separate spec should cover that frame's behavior (debounced query, no-results state, error state, search-hit row variant, etc.).
- **Figma vs. parent-spec conflict — RESOLVED by Q-S-5**: Figma's component spec said "Click item → Profile", parent Viết Kudo spec said "Click item → prefill recipient". PM ruled in favor of Figma — tap row always opens Profile. The parent Viết Kudo spec (`7fFAb-K35a/spec.md`, line 720) still contains the now-stale "Search Sunner ... prefill recipient" claim and should be amended in a follow-up edit. Until then, anyone reading that spec should disregard the Search-Sunner reference in favor of this spec's FR-010 + Q-S-5 resolution.
- **No MoMorph test cases on this frame** (`get_frame_test_cases` returned `[]` at 2026-05-14). The Acceptance Scenarios in US1–US4 are the authoritative test contract.
- **`Frame 540` inside the row** (`I6891:22087;490:5558` / `I6891:22109;490:5558`) holds the two text rows (name + dept). Both are inside the `Tên` frame — the implementer just needs to render the 2-line text stack; no special handling.
- **Bottom nav reuse**: the bottom-nav component `6885:8076` is the same one used by Home / Awards / Kudos hub. Existing Compose implementation should be reused unchanged.
