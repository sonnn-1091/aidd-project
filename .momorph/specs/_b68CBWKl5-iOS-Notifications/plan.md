# Implementation Plan: Sun*Kudos — Notifications

**Frame**: `_b68CBWKl5-iOS-Notifications`
**Date**: 2026-05-15
**Spec**: `specs/_b68CBWKl5-iOS-Notifications/spec.md`

---

## Summary

Replace the existing `NotificationsSheet` bottom-sheet (`home/ui/components/NotificationsSheet.kt`, currently used by `HomeScreen` and `AwardDetailScreen` on bell tap) with a **dedicated full-screen `Routes.NOTIFICATIONS`** per Figma frame `_b68CBWKl5`. Wire the same route from the Kudos hub bell (currently a no-op stub). Introduce a **Hilt-singleton `NotificationsCountFlow`** as the cross-feature source-of-truth for the bell-badge count (Q-N-8). Add full notification list/CRUD on top of the existing `NotificationsSummaryRepository` count-only plumbing.

Primary contract (per spec):
- Render a paginated newest-first list of 7 notification types (KUDOS_RECEIVED, HEART_RECEIVED, SECRET_BOX_UNLOCK, LEVEL_UP, CONTENT_HIDDEN, BADGE_COLLECTED, REVIEW_REQUEST).
- Row tap → type-specific destination + mark single read (optimistic).
- "Đánh dấu đọc tất cả" button → mark all read (optimistic + rollback on failure).
- Type-5 inline "Tiêu chuẩn cộng đồng" link → Community Standards screen (already shipped).
- Pull-to-refresh, no real-time (per Q-N-6).
- Relative-time ladder extends through "năm trước" (per Q-N-5).

**Existing infrastructure to integrate with** (verified at recon-time):
- `home/data/NotificationsSummaryRepository.kt` — 8-line interface: `suspend fun get(): Result<NotificationsSummary>`. Implementations: `DemoNotificationsSummaryRepository` + `SupabaseNotificationsSummaryRepository`. The Supabase impl calls a server-side **RPC** named `notifications_summary()` (NOT a REST endpoint — Postgrest RPC). Returns `NotificationsSummary(unreadCount: Int)` for the bell badge. Reused as the cold-start count source.
- `home/domain/states/NotificationsState.kt` — sealed interface (`Loading` / `Loaded(count)` / `Error`) wrapping the count for the HomeUiState. Only consumed by `HomeUiState` + `HomeViewModel`. **Deleted post-migration** once the count moves out of HomeUiState into the cross-feature `NotificationsCountFlow`.
- `home/ui/components/NotificationsSheet.kt` (57 LOC) — the bottom-sheet composable being replaced. Dead code post-migration; deleted at the end of Phase 4.
- `HomeViewModel` already wires `unreadCount` via `notificationsRepository.get().fold { summary -> notificationsState.value = NotificationsState.Loaded(summary.unreadCount) }` at line 185. Refactor to collect the new `NotificationsCountFlow`.
- **Existing test files** (modified / deleted as part of the migration):
  - `app/src/androidTest/.../home/NotificationsSheetTest.kt` — instrumented test of the sheet's dismiss + tap-to-route behavior. **DELETED** post-migration; equivalent contract moves to `NotificationsScreenTest.kt`.
  - `app/src/test/.../home/ui/HomeViewModelTest.kt` — contains a test `onNotificationsSheetDismissed re-fires only the notifications fetch (US6)` at line 203. **MODIFIED** to remove that test and update any other tests that rely on the now-deleted `notificationsState` flow.

**Backend convention note** — the existing summary plumbing calls a Supabase RPC (`notifications_summary()`), not a REST endpoint. The 4 new endpoints predicted by the spec will likely be a mix of Postgrest table reads (for `GET /notifications` and `PATCH /notifications/{id}/read` against the `notifications` table) + RPC (for `PATCH /notifications/read-all`, which atomically updates many rows). The backend team's call. The plan uses spec endpoint names as conceptual references; the implementer wires whatever the backend ships.

Estimated effort: **~12–14 hours** for one engineer including tests, migration, and dead-code removal.

---

## Technical Context

**Language/Framework**: Kotlin / Jetpack Compose + Material 3
**Primary Dependencies**: `androidx.compose.material3`, `androidx.compose.foundation`, `androidx.activity.compose` (`BackHandler`), `androidx.lifecycle:lifecycle-viewmodel-compose`, `androidx.hilt:hilt-navigation-compose`, `coil-compose`, `kotlinx-datetime` (for `Instant` + relative-time math) — all already on classpath via `libs.versions.toml`
**Database**: Supabase (new `notifications` table — backend concern; this plan assumes the schema exists)
**Testing**: JUnit 4 + Turbine + `androidx.compose.ui.test:ui-test-junit4`
**State Management**: Hilt VM exposing `StateFlow<NotificationsUiState>` + Hilt-singleton `NotificationsCountFlow`
**API Style**: Supabase Postgrest reads (RLS-scoped to `auth.uid()`) + Postgrest PATCH-equivalent updates

---

## Constitution Compliance Check

*GATE: Must pass before implementation can begin. Each item maps to a principle in `.momorph/constitution.md`.*

- [x] **I. Clean Code & Source Organization** — feature-first package `com.example.aiddproject.kudos.notifications.{ui, data, domain}`. Composables under 150 LOC each. Kotlin official style via existing ktlint. Migration removes the existing sheet implementation cleanly (no co-existence).
- [x] **II. Tech Stack Best Practices** — immutable `data class` + `sealed` types; `StateFlow` + `MutableStateFlow`; coroutines + Flow; Hilt for DI including the cross-feature singleton; supabase-kt client uses anon/publishable key; no Service Role anywhere.
- [x] **III. Material Design 3 (Android)** — `Scaffold` + `TopAppBar` + `LazyColumn` from `androidx.compose.material3`. Pull-to-refresh via `PullToRefreshBox` (same pattern as `KudosScreenContent`). `MaterialTheme` tokens for color/typography; light + dark via existing theme. Touch targets ≥ 48dp via M3 `IconButton` defaults. Back arrow reuses `ic_back_chevron`.
- [x] **IV. OWASP Secure Coding** — RLS-scoped Supabase reads (server-side filtering by `auth.uid()`); role-gated type-7 filtered server-side, NOT client-side; no notification body in Timber/logcat; no Service Role; per Constitution IV.
- [x] **V. Test-Driven Development** — VM unit tests authored alongside the VM (state mirroring, intent dispatch, optimistic updates + rollback). Repository tests for both Demo + Supabase impls (Demo seeded with all 7 type variants per TR-005). Instrumented Compose UI test for per-row routing + the read-all flow.

**Violations (if any)**: NONE.

| Violation | Justification | Alternative Rejected |
|-----------|---------------|---------------------|
| *(none)* | — | — |

---

## Architecture Decisions

### Frontend Approach

- **Component Structure** — two composables under `kudos/notifications/ui/`, mirroring the Write-Kudo / Community-Standards / Search-Sunner split:
  - `NotificationsScreen.kt` — thin Hilt entry. Takes `onNavigateBack`, `onNavigateToKudoDetail(kudoId)`, `onNavigateToSecretBoxOpen`, `onNavigateToProfile`, `onNavigateToCommunityStandards`, `onNavigateToAdminReview`. Wires `BackHandler`. Collects `viewModel.state` + delegates to Content.
  - `NotificationsContent.kt` — stateless content. Hosts `Scaffold` with `TopAppBar(navigationIcon = back, title = "Thông báo", actions = readAllButton)`. Body is `PullToRefreshBox` wrapping `LazyColumn` of `NotificationRow` composables. Branches on UI state: Loading → centered spinner, Error → centered retry CTA, Empty → centered placeholder, Loaded → list.

  **`NotificationsCallbacks` data class** (8 lambdas — same shape pattern as Search Sunner):
  ```kotlin
  data class NotificationsCallbacks(
      val onNavigateBack: () -> Unit,
      val onRefresh: () -> Unit,
      val onReadAll: () -> Unit,
      val onRowTap: (NotificationItem) -> Unit,
      val onInlineCommunityStandardsTap: () -> Unit,
      val onRetry: () -> Unit,
      val onLoadMore: () -> Unit,
      val onConsumeSnackbar: () -> Unit,
  )
  ```

- **Styling Strategy** — M3 theme tokens only. Per-section visual values fetched at implementation time via `query_section` against the listed Node IDs. The 7 notification icons MAY be downloaded as colored Figma exports OR matched against Material defaults (`Email` / `Favorite` / `CardGiftcard` / `Star` / `Warning` / `Shield` / `Edit`) — decide per-icon at Phase 0 after a `query_section` on each icon node.

- **State Sealing** — `SearchResultsState`-style sealed interface for the list slot:
  ```kotlin
  sealed interface NotificationsListState {
      data object Loading : NotificationsListState
      data class Loaded(val items: List<NotificationItem>, val hasMore: Boolean) : NotificationsListState
      data object Empty : NotificationsListState
      data class Error(val messageRes: Int) : NotificationsListState
  }
  ```
  The `UiState` wraps this + a `snackbar` slot for the read-all-failure rollback message.

- **Data Fetching** — `NotificationRepository` (NEW, interface + `DefaultSupabaseNotificationRepository` + `DemoNotificationRepository`). Methods: `observeRecent(): Flow<Page<NotificationItem>>`, `loadMore(cursor): Result<Page>`, `markRead(id): Result<Unit>`, `markAllRead(): Result<Int>` (returns the count cleared for badge-decrement). Demo seeds one of each type for testing.

- **Hilt-singleton `NotificationsCountFlow`** (Q-N-8 lynchpin):
  - Lives in `kudos/notifications/data/NotificationsCountFlow.kt`.
  - Wraps a `private val _count = MutableStateFlow<Int>(0)` exposed as `val count: StateFlow<Int>`.
  - Methods: `suspend fun refreshFromServer()` (calls `NotificationsSummaryRepository.get()` and `_count.value = summary.unreadCount`); `fun setTo(value: Int)` (called by NotificationsViewModel after a full list load); `fun decrementBy(n: Int)`; `fun decrement()`.
  - Both `HomeViewModel` (already injects `NotificationsSummaryRepository`) and `NotificationsViewModel` inject this. Mutations on the Notifications screen are mirrored into the flow → bell badge updates everywhere in real time.

- **Localization** — copy goes into `app/src/main/res/values/strings.xml` (VN canonical) with the `notifications_*` prefix. The 7 notification body templates are AUTHORED server-side (per spec — `displayBody` is pre-rendered with interpolated names/counts), so the client only ships the title + button + placeholder + a11y + error strings — about ~10 keys total.

### Backend Approach

Backend work is **out of scope for this plan** but the spec predicts 4 endpoints (`GET /notifications`, `PATCH /notifications/{id}/read`, `PATCH /notifications/read-all`, `GET /notifications/unread-count`). The plan ASSUMES the Supabase schema + Postgrest endpoints + RLS policies are authored separately. The `DefaultSupabaseNotificationRepository` impl wires against them.

If the backend isn't ready at implementation time, the implementer can ship the screen against the **DemoNotificationRepository only** (gated by `BuildConfig.DEMO_MODE` per the existing kudos pattern) — same approach `DemoKudosRepository` enables for the kudos hub today.

### Integration Points

- **Existing services**:
  - `NotificationsSummaryRepository` (`home/data/`) — cheap count-only endpoint. The `NotificationsCountFlow.refreshFromServer()` calls into this. Repository stays put; only its caller changes.
  - `HomeViewModel.notificationsRepository` field + the `onNotificationsSheetDismissed` method — both REFACTORED post-migration to use the new flow.
  - `NotificationsSheet` composable (`home/ui/components/NotificationsSheet.kt`) — DELETED at end of Phase 4 after all 3 hosts migrate.

- **Shared components**:
  - `ic_back_chevron.xml` (from commit `e0a2f5d`) — back arrow.
  - `PullToRefreshBox` pattern from `KudosScreenContent`.
  - Material icons (`Email` / `Favorite` / `CardGiftcard` / `Star` / `Warning` / `Shield` / `Edit`) — Phase-0 decide vs Figma exports.

- **API contracts**: 4 NEW Postgrest endpoints per spec § API Dependencies (backend team).

- **Navigation graph**: NEW `Routes.NOTIFICATIONS` and NEW `Routes.ADMIN_REVIEW` (for the type-7 placeholder per Q-N-3). Both added to `Routes.kt`. `AppNavigation.kt` registers both composables. 3 entry-point host screens (Home, Awards, Kudos) swap their bell callbacks to navigate to the new route.

### Profile + Kudo Detail handshake (placeholder destinations)

`Routes.PROFILE`, `Routes.KUDOS_DETAIL`, `Routes.SECRET_BOX_OPEN`, and the new `Routes.ADMIN_REVIEW` are all currently bound to `PlaceholderScreen`. The Notifications screen passes type-specific arguments via the destination's `savedStateHandle` — same pattern Search Sunner → Profile uses (commit `3e7f91d`):

```kotlin
onNavigateToKudoDetail = { kudoId ->
    navController.currentBackStackEntry?.savedStateHandle?.set("kudoId", kudoId)
    navController.navigate(Routes.KUDOS_DETAIL)
}
```

When each destination ratifies its own parameterization, the call sites here align in a follow-up.

---

## Project Structure

### Documentation (this feature)

```text
.momorph/specs/_b68CBWKl5-iOS-Notifications/
├── spec.md              # Feature specification (exists, reviewed twice, all 11 Q-Ns resolved)
├── plan.md              # This file
├── tasks.md             # Generated next by /momorph.tasks
└── (research.md)        # NOT NEEDED — codebase patterns are well-understood from prior session work
```

### Source Code (affected areas)

```text
app/src/main/java/com/example/aiddproject/kudos/notifications/         NEW PACKAGE
├── domain/
│   ├── NotificationItem.kt                    NEW — projection (id, type, isRead, createdAt, payload, displayBody)
│   ├── NotificationType.kt                    NEW — enum (7 values)
│   └── NotificationPayload.kt                 NEW — sealed interface (KudoRef, SecretBox, Profile, Review)
├── data/
│   ├── NotificationRepository.kt              NEW — interface (observeRecent, loadMore, markRead, markAllRead)
│   ├── DefaultSupabaseNotificationRepository.kt NEW — supabase-kt impl
│   ├── DemoNotificationRepository.kt          NEW — seeds one of each type
│   ├── NotificationsCountFlow.kt              NEW — Hilt-singleton MutableStateFlow<Int> wrapper
│   └── NotificationRepositoryModule.kt        NEW — Hilt bindings (interface→impl branch on DEMO_MODE)
└── ui/
    ├── NotificationsScreen.kt                 NEW — thin Hilt entry; BackHandler + delegates to Content
    ├── NotificationsContent.kt                NEW — stateless content; Scaffold + TopAppBar + PullToRefreshBox + LazyColumn
    ├── NotificationsUiState.kt                NEW — `data class` + sealed list-state
    ├── NotificationsViewModel.kt              NEW — Hilt VM; load on init, mutations, decrements count-flow
    ├── NotificationsTestTags.kt               NEW — testTag constants
    └── components/
        ├── NotificationRow.kt                 NEW — per-row composable; branches icon + body + optional inline link
        └── RelativeTimeFormatter.kt           NEW — pure function for FR-010 ladder

app/src/main/java/com/example/aiddproject/navigation/
└── AppNavigation.kt                           MODIFIED — register NotificationsScreen + ADMIN_REVIEW placeholder
└── Routes.kt                                  MODIFIED — add NOTIFICATIONS + ADMIN_REVIEW constants

app/src/main/java/com/example/aiddproject/home/ui/
├── HomeScreen.kt                              MODIFIED — remove notificationsSheetVisible state + sheet render
├── HomeViewModel.kt                           MODIFIED — drop onNotificationsSheetDismissed; drop notificationsState MutableStateFlow; collect NotificationsCountFlow instead of polling summaryRepository directly
├── HomeUiState.kt                             MODIFIED — drop `notifications: NotificationsState` field; `unreadCount` now mirrors `NotificationsCountFlow.count.value` instead of being derived from `NotificationsState.Loaded`
└── components/
    └── NotificationsSheet.kt                  DELETED — replaced by the dedicated screen

app/src/main/java/com/example/aiddproject/home/domain/states/
└── NotificationsState.kt                      DELETED — only consumed by HomeUiState + HomeViewModel; both refactored above

app/src/test/java/com/example/aiddproject/home/ui/
└── HomeViewModelTest.kt                       MODIFIED — remove the `onNotificationsSheetDismissed re-fires only the notifications fetch (US6)` test (line 203); update any other tests that observe `notificationsState`

app/src/androidTest/java/com/example/aiddproject/home/
└── NotificationsSheetTest.kt                  DELETED — sheet behavior is replaced by NotificationsScreenTest (under kudos/notifications/ui/)

app/src/main/java/com/example/aiddproject/kudos/ui/
├── KudosScreen.kt                             MODIFIED — wire onBellClick to navigate
├── KudosScreenContent.kt                      MODIFIED — pass real unreadCount from VM (was hardcoded 0)
└── KudosViewModel.kt                          MODIFIED — inject NotificationsCountFlow + expose unreadCount

app/src/main/java/com/example/aiddproject/awarddetail/ui/
└── AwardDetailScreen.kt                       MODIFIED — same sheet→route migration as HomeScreen

app/src/main/res/values/
└── strings.xml                                MODIFIED — append notifications_* keys (~10 entries)

app/src/test/java/com/example/aiddproject/kudos/notifications/         NEW TEST PACKAGE
├── data/
│   └── DemoNotificationRepositoryTest.kt      NEW — round-trips for the 7 demo seed types
└── ui/
    └── NotificationsViewModelTest.kt          NEW — state-flow assertions for all intents + optimistic-update + rollback

app/src/androidTest/java/com/example/aiddproject/kudos/notifications/  NEW ANDROID TEST PACKAGE
└── ui/
    └── NotificationsScreenTest.kt             NEW — Compose UI test (~8-10 assertions, one per US scenario)
```

### New Files

| File | Purpose | Estimated LOC |
|------|---------|---------------|
| `notifications/domain/NotificationItem.kt` | Data class + helper `isReadable` getter | ~30 |
| `notifications/domain/NotificationType.kt` | Enum + `iconResId` extension | ~20 |
| `notifications/domain/NotificationPayload.kt` | Sealed interface (4 variants) | ~25 |
| `notifications/data/NotificationRepository.kt` | Interface | ~30 |
| `notifications/data/DefaultSupabaseNotificationRepository.kt` | supabase-kt Postgrest reads + writes against the `notifications` table | ~150 |
| `notifications/data/DemoNotificationRepository.kt` | In-memory seed of 7 notifications (one of each type), mutation methods | ~120 |
| `notifications/data/NotificationsCountFlow.kt` | `@Singleton` wrapper around `MutableStateFlow<Int>` + `refreshFromServer / setTo / decrementBy` | ~60 |
| `notifications/data/NotificationRepositoryModule.kt` | Hilt module: interface→impl binding + flow @Singleton provider | ~40 |
| `notifications/ui/NotificationsUiState.kt` | `data class NotificationsUiState` + sealed `NotificationsListState` | ~45 |
| `notifications/ui/NotificationsViewModel.kt` | `@HiltViewModel` — init load, pagination, mutations, snackbar emission, count-flow updates | ~180 |
| `notifications/ui/NotificationsScreen.kt` | Thin entry; collects state, wires `BackHandler`, builds `NotificationsCallbacks` from VM methods + caller-provided nav lambdas | ~50 |
| `notifications/ui/NotificationsContent.kt` | Stateless content; Scaffold + TopAppBar + PullToRefreshBox + LazyColumn + Loading/Error/Empty branches | ~200 |
| `notifications/ui/components/NotificationRow.kt` | Per-row composable; type-keyed icon + body Text + relative-time + optional inline link + unread red-dot | ~150 |
| `notifications/ui/components/RelativeTimeFormatter.kt` | Pure function: `Instant.toRelativeVi(now: Instant): String` — implements the FR-010 ladder | ~50 |
| `notifications/ui/NotificationsTestTags.kt` | testTag constants | ~25 |
| `test/.../DemoNotificationRepositoryTest.kt` | Round-trip tests for all 7 types + mutations | ~100 |
| `test/.../NotificationsViewModelTest.kt` | Intent dispatch + state mirroring + optimistic update + rollback | ~250 |
| `androidTest/.../NotificationsScreenTest.kt` | UI tests covering US1–US4 + a11y | ~200 |

### Modified Files

| File | Change |
|------|--------|
| `navigation/Routes.kt` | Add `const val NOTIFICATIONS: String = "route_notifications"` and `const val ADMIN_REVIEW: String = "route_admin_review"`. |
| `navigation/AppNavigation.kt` | Register `composable(Routes.NOTIFICATIONS) { NotificationsScreen(...) }` + `composable(Routes.ADMIN_REVIEW) { PlaceholderScreen(label = "Admin Review Content") }`. Pass nav lambdas (back, kudo-detail with savedStateHandle, secret-box, profile, community-standards, admin-review). |
| `home/ui/HomeScreen.kt` | (1) Remove `var notificationsSheetVisible by rememberSaveable` + the `if (notificationsSheetVisible) { NotificationsSheet(...) }` block + the `notificationsSheetVisible` param threading through `HomeScreenContent`. (2) Change `onBellClick = { notificationsSheetVisible = true }` to `onBellClick = onNavigateToNotifications` where the caller (AppNavigation.kt) passes `{ navController.navigate(Routes.NOTIFICATIONS) }`. (3) Drop the `notificationsSheetVisible &&` guard on the SAA-tab scroll-suppress. |
| `home/ui/HomeViewModel.kt` | (1) Remove `onNotificationsSheetDismissed()` method. (2) Replace the direct `notificationsRepository.get().fold {...}` with `viewModelScope.launch { notificationsCountFlow.refreshFromServer() }` on init AND `state.unreadCount` getter mirrors `notificationsCountFlow.count.value`. (3) Inject `NotificationsCountFlow`. |
| `home/ui/HomeUiState.kt` | Replace `notifications: NotificationsState` field with a simple `unreadCount: Int` mirrored from the flow. The `NotificationsState` sealed class MAY be deleted if nothing else uses it. |
| `kudos/ui/KudosScreen.kt` | Add `onNavigateToNotifications: () -> Unit` parameter. Replace `onBellClick = { /* Notifications sheet wires in Phase 13 polish. */ }` with `onBellClick = onNavigateToNotifications`. |
| `kudos/ui/KudosScreenContent.kt` | (1) Change `unreadCount = 0` hardcode to `unreadCount = state.unreadCount` (the VM exposes it via flow collection). (2) Plumb `onNavigateToNotifications` through. |
| `kudos/ui/KudosViewModel.kt` | Inject `NotificationsCountFlow`. Expose `unreadCount: StateFlow<Int>` (or merge into existing UI state). |
| `awarddetail/ui/AwardDetailScreen.kt` | Same sheet→route migration as HomeScreen (remove the sheet state + render; swap the bell callback). |
| `home/ui/components/NotificationsSheet.kt` | **DELETED** at the end of Phase 4 (dead code post-migration). |
| `home/data/NotificationsSummaryRepository.kt` | (no change needed — `NotificationsCountFlow.refreshFromServer()` calls into it). |
| `res/values/strings.xml` | Append ~10 strings: `notifications_title` ("Thông báo"), `notifications_empty` ("Bạn chưa có thông báo nào"), `notifications_loading` / `_error` / `_retry`, `notifications_mark_all_read` ("Đánh dấu đọc tất cả"), `notifications_inline_community_standards` ("Tiêu chuẩn cộng đồng"), `a11y_notifications_back`, `a11y_notifications_row` (formatted), `a11y_notifications_mark_all_read`, `notifications_relative_time_*` (formatted plurals — N min, N giờ, N ngày, N tháng, N năm). |

### Dependencies

| Package | Version | Purpose |
|---------|---------|---------|
| *(none new)* | — | Hilt, Compose M3, navigation-compose, supabase-kt, coil-compose, kotlinx-datetime all already on classpath. Material icons already imported. |

---

## Implementation Strategy

### Phase Breakdown

#### Phase 0 — Asset audit (~30 min)

- For each of the 7 `NotificationType` values, decide icon strategy. Each row's icon is a Figma `MM_MEDIA_IC` instance keyed by a distinct `componentId`:
  | Type | Row instance | Icon component instance | Component ID | Figma colored variant |
  |------|---|---|---|---|
  | `KUDOS_RECEIVED` | `6885:9394` | `I6885:9394;128:2910` | `6885:8273` | Envelope blue |
  | `HEART_RECEIVED` | `6885:9395` | `I6885:9395;128:2922` | `6885:8281` | Heart pink |
  | `SECRET_BOX_UNLOCK` | `6885:9396` | `I6885:9396;128:2922` | `6885:8275` | Gift green |
  | `LEVEL_UP` | `6885:9397` | `I6885:9397;128:2922` | `6885:8277` | Star yellow |
  | `CONTENT_HIDDEN` | `6885:9398` | `I6885:9398;128:3453` | `6885:8279` | Warning yellow |
  | `BADGE_COLLECTED` | `6885:9399` | `I6885:9399;128:2922` | `6885:8311` | Badge/Shield blue |
  | `REVIEW_REQUEST` | `6885:9400` | `I6885:9400;128:2922` | `6885:8313` | Pen/Review purple |
  1. For each, query the icon component instance via `query_section` to inspect color + silhouette.
  2. If Material icons match the silhouette (`Email` / `Favorite` / `CardGiftcard` / `Star` / `Warning` / `Shield` / `Edit`), use the Material default with a tint matching Figma's colored variant.
  3. If Figma's icon is custom (e.g. art-direction treatment), download via `get_media_files` presigned URLs (same flow used for the Root Further banner in commit `e0a2f5d`).
- Store any downloaded icons under `drawable-xxhdpi/ic_notification_*.{png,xml}`.

#### Phase 1 — Foundation: domain + data + Hilt + strings (~3 h)

TDD order per Constitution V — failing tests FIRST.

1. Create new package directories (main + test + androidTest).
2. Author `NotificationType.kt` (enum), `NotificationPayload.kt` (sealed), `NotificationItem.kt` (data class).
3. Add the ~10 strings to `strings.xml` under a `<!-- Notifications (Figma frame _b68CBWKl5) -->` block.
4. Author `RelativeTimeFormatter.kt` — pure function for FR-010 ladder; trivial unit-testable.
5. Author `NotificationRepository.kt` (interface) with the 4 methods.
6. Author `NotificationsCountFlow.kt` as a Hilt-singleton wrapper. Inject `NotificationsSummaryRepository` into it (so its `refreshFromServer()` can fetch the cold-start count).
7. Author `NotificationRepositoryModule.kt` — Hilt module binding interface → impl (branches on `BuildConfig.DEMO_MODE` per the kudos package pattern).
8. Write **failing tests** for: `DemoNotificationRepository` (round-trip the 7 seed types, mark-read decrements unread count, mark-all-read clears all), `RelativeTimeFormatter` (5 ladder thresholds + edge cases at 60 min / 24 hr / 30 day / 12 month boundaries), `NotificationsCountFlow` (refresh-from-server, decrement, setTo).
9. Verify tests fail.

#### Phase 2 — User Story 1 + 2 (P1): list + read-state (~4 h)

1. Implement `DemoNotificationRepository` — in-memory `MutableStateFlow<List<NotificationItem>>` seeded with one of each type. Mutations update the flow.
2. Implement `DefaultSupabaseNotificationRepository` — Postgrest reads against the `notifications` table (RLS-scoped to `auth.uid()`); PATCH-equivalent updates via supabase-kt's `from("notifications").update {...}.eq("id", id)` pattern. **NOTE**: backend schema not yet authored; ship this against the Demo impl only at MVP if the schema isn't ready (gated by `BuildConfig.DEMO_MODE`).
3. Implement `NotificationsUiState.kt`.
4. Implement `NotificationsViewModel`:
   - `init`: launch `repo.observeRecent()` collector + call `notificationsCountFlow.refreshFromServer()`. Mirror loaded list into state.
   - `onRowTap(item)`: optimistic local mutation `isRead = true`; fire `repo.markRead(item.id)` in `viewModelScope`; decrement `countFlow`; invoke navigation callback synchronously (don't wait on the network).
   - `onReadAll()`: early-return if `unreadCount == 0` (Q-N-10). Otherwise — snapshot current unread list, optimistic clear, fire `repo.markAllRead()`. On failure → emit snackbar + restore the snapshot to state AND restore the count flow.
   - `onRefresh()`: re-issue load.
   - `onLoadMore()`: cursor-paginated fetch when LazyColumn reaches the bottom.
5. Implement `NotificationsContent.kt`:
   - `Scaffold(topBar = { TopAppBar(navigationIcon, title, actions = { ReadAllButton(...) }) })`.
   - Body: `PullToRefreshBox(state, isRefreshing, onRefresh)`. Inside, branch on `state.listState`:
     - `Loading` → centered spinner
     - `Empty` → centered "Bạn chưa có thông báo nào" placeholder
     - `Error` → centered error + retry CTA
     - `Loaded` → `LazyColumn` of `NotificationRow`s + footer spinner when `hasMore && isLoadingMore`
   - Snackbar host for the read-all-failure message.
6. Implement `NotificationRow.kt` — branch on `NotificationType` for icon; render `displayBody` + `relativeTime`; inline "Tiêu chuẩn cộng đồng" `IconButton` for `CONTENT_HIDDEN` type; trailing red-dot for `isRead == false`.
7. Implement `NotificationsScreen.kt` — thin Hilt entry; build callbacks bag from VM + nav lambdas.
8. Run unit tests + build green.

#### Phase 3 — Wire navigation + migrate existing sheet (~3 h)

1. Add `Routes.NOTIFICATIONS` and `Routes.ADMIN_REVIEW` constants to `Routes.kt`.
2. Register both composables in `AppNavigation.kt`:
   - `composable(Routes.NOTIFICATIONS) { NotificationsScreen(...) }` with all 6 nav lambdas wired (back via `popBackStack()`; kudo-detail / secret-box / profile / community-standards / admin-review via `navigate()` + `savedStateHandle` for kudoId).
   - `composable(Routes.ADMIN_REVIEW) { PlaceholderScreen(label = "Admin Review Content") }`.
3. **Migrate Home**:
   - Remove `notificationsSheetVisible` state + the `NotificationsSheet(...)` render block in `HomeScreen.kt`.
   - Drop `notificationsSheetVisible` param from `HomeScreenContent`. Drop the SAA-tab scroll-suppress guard.
   - Change `onBellClick = { notificationsSheetVisible = true }` → `onBellClick = onNavigateToNotifications` (new parameter).
   - In `HomeViewModel`: remove `onNotificationsSheetDismissed()`; inject `NotificationsCountFlow`; replace the direct `notificationsRepository.get()` call with `notificationsCountFlow.refreshFromServer()` on init; rewire `state.unreadCount` to collect from the flow.
   - Update HomeViewModelTest to match the refactored API.
4. **Migrate Kudos hub**:
   - In `KudosScreen.kt`: replace the no-op `onBellClick` lambda with `onBellClick = onNavigateToNotifications` (new parameter).
   - In `KudosScreenContent.kt`: change `unreadCount = 0` to `unreadCount = state.unreadCount`.
   - In `KudosViewModel.kt`: inject `NotificationsCountFlow`; expose `unreadCount` via state.
5. **Migrate Award Detail**:
   - Same pattern as Home migration in `AwardDetailScreen.kt`.
6. **Delete `home/ui/components/NotificationsSheet.kt`** — dead code now that 3 hosts all navigate.
7. Build green; manual smoke test of each entry point's bell tap.

#### Phase 4 — Polish (~2 h)

1. **Instrumented UI test** (`NotificationsScreenTest.kt`) — covers all 4 US's acceptance scenarios:
   - Type-specific routing for each of the 7 types (parameterized).
   - Read-all click clears all dots + fires read-all callback exactly once.
   - Empty state renders placeholder.
   - Inline "Tiêu chuẩn cộng đồng" link fires its own callback, NOT the row's.
   - Back arrow + system-back both invoke `onNavigateBack` exactly once.
2. TalkBack walk-through on a physical device covering the row content-description, the read-all button, the inline link.
3. 200% font scale check — confirm row text wraps without truncation; red-dot stays right-aligned.
4. Locale toggle — verify relative-time + chrome strings re-render correctly.
5. Tag the Figma frame `_b68CBWKl5` "Spec Created" via `mcp__momorph__upload_specs` (out-of-band).
6. **Parent Viết Kudo spec line-720 cleanup** is unrelated — leave for its own commit.

### Risk Assessment

| Risk | Probability | Impact | Mitigation |
|------|-------------|--------|------------|
| Backend `notifications` table + Postgrest endpoints not yet authored | High | High | Ship against `DemoNotificationRepository` only at MVP (gated by `BuildConfig.DEMO_MODE` per kudos pattern). Document the predicted endpoint contract in the spec for the backend team. |
| Migration breaks Home / Kudos / Awards in ways the existing tests don't catch | Medium | High | Run all existing instrumented tests for Home/Awards/Kudos screens after migration. Specifically: `HomeViewModelTest.kt` has a test `onNotificationsSheetDismissed re-fires only the notifications fetch (US6)` at line 203 — DELETE that test as part of Phase 3. `NotificationsSheetTest.kt` (androidTest) becomes obsolete — DELETE entire file. The `NotificationsCountFlow` is `@Singleton` — same instance across the app — so a stale collector is the main risk; verify with manual smoke. |
| `NotificationsSheet.kt` may still be referenced by tests or other indirect callers | LOW (verified) | Medium | Pre-deletion verified: 2 test files reference it (`NotificationsSheetTest` androidTest + `HomeViewModelTest` unit test). Both are addressed in the Modified Files table above. No other callers; the deletion is safe. |
| 7 icon set — Material defaults don't match Figma's colored variants visually | Medium | Low | Phase 0 audit lets the implementer choose per-icon: Material default + tint OR Figma export. Color-stylized icons are visual polish; functional behavior is unaffected. |
| `Routes.ADMIN_REVIEW` placeholder may bewilder users who tap a type-7 row | Low | Low | Acceptable since type-7 is admin-only AND the placeholder text is "Admin Review Content" — clear "not built yet". When admin spec lands, swap the placeholder for the real screen. |
| Relative-time edge cases — millisecond precision, timezone math, "vừa xong" if < 1 min | Low | Low | Q-N-5 caps the ladder at 5 tiers. Implementer can add a "<1 phút" tier as a polish detail (e.g. "vừa xong"); not in the spec but harmless. |
| Optimistic-update rollback timing — if user navigates away mid-rollback | Low | Low | `viewModelScope` survives Profile → back navigation. Rollback completes; user sees the restored list when they return. No data corruption. |
| Cross-feature `NotificationsCountFlow` injection — 3 VMs need the same Hilt singleton | Low | Medium | Hilt's `@Singleton` semantics guarantee one instance. Risk is forgetting to inject in one of the 3 VMs → that screen's bell badge stays stale. Mitigated by adding a checklist item to the AppNavigation regression test. |

### Estimated Complexity

- **Frontend**: **Medium** — 2 top-level composables + 2 sub-composables + LazyColumn + sealed state + 7-type rendering. ~400 LOC of Compose.
- **Backend**: **N/A** (separate plan) — backend team owns the Postgrest endpoints + RLS policies.
- **Data layer**: **Medium** — Repository interface + Demo + Supabase impls + Hilt singleton flow + count-flow lifecycle. ~400 LOC of Kotlin data layer.
- **Migration**: **Medium** — touches 3 host VMs + 3 host screens + 1 dead-code deletion. Risk if existing tests aren't run.
- **Testing**: **Medium** — 2 unit-test files + 1 instrumented test file. ~550 LOC total.

**Total estimated effort**: **~12–14 hours** for one engineer including all phases + tests + migration + PR.

---

## Integration Testing Strategy

### Test Scope

- [x] **Component/Module interactions**: `NotificationsScreen` ↔ `NotificationsContent` (callback bag), VM ↔ Repository (state mirroring + intent dispatch), VM ↔ `NotificationsCountFlow` (decrement / setTo on mutations).
- [x] **External dependencies**: Supabase Postgrest (mocked via `DemoNotificationRepository` for unit/UI tests; real instance for integration).
- [x] **User workflows**: bell tap from each host → Notifications screen → row tap → destination screen.

### Test Categories

| Category | Applicable? | Key Scenarios |
|----------|-------------|---------------|
| UI ↔ Logic | **Yes** | Row tap → `onRowTap` callback fires with correct payload; X icon would-be-tap is not applicable (no X here). Inline link gesture isolated from row. |
| Service ↔ Service | **Yes** | `NotificationsCountFlow.refreshFromServer()` → `NotificationsSummaryRepository.get()` returns count. |
| App ↔ External API | No | Postgrest mocked in tests via Demo impl. |
| App ↔ Data Layer | **Yes** | Repo CRUD + count-flow sync. |
| Cross-platform | No | Android-only. |
| A11y | **Yes** | Row contentDescription merges body + time + read state. Inline link is a separate focusable element. |
| **Cross-feature** | **Yes** | After mutations on the Notifications screen, the bell-badge on the entry screen reflects the new count (verified by collecting `NotificationsCountFlow.count` from the host VM). |

### Test Environment

- Local emulator (Pixel 5a API 33+) for instrumented; JVM JUnit for VM + repo unit tests.
- DemoNotificationRepository fixture data: one of each `NotificationType`, mixed read/unread (3 unread + 4 read).

### Mocking Strategy

| Dependency Type | Strategy | Rationale |
|-----------------|----------|-----------|
| `NotificationRepository` (in VM tests) | Fake (in-memory `MutableStateFlow<List<NotificationItem>>`) | Verify VM intent dispatch without Supabase plumbing. |
| `NotificationsSummaryRepository` (in `NotificationsCountFlow` tests) | Stub returning fixed `NotificationsSummary(unreadCount = N)` | Validate flow updates without auth dependency. |
| `NotificationsCountFlow` (in VM tests) | Real instance with stubbed summary repo | Verify the cross-feature count contract end-to-end. |
| Navigation callbacks | Captured lambdas counted by `mutableStateOf<Int>` | Verify per-type routing fires exactly once with correct args. |
| Supabase client | Not touched in unit tests; only `DefaultSupabaseNotificationRepository` integration tests (if backend ready) | Production wiring exercised manually. |

### Coverage Goals

| Area | Target | Priority |
|------|--------|----------|
| VM intent dispatch (rowTap, readAll, refresh, loadMore) | 100% | High |
| Optimistic update + rollback on read-all failure | 100% | High |
| 7-type routing in the UI test | 100% | High |
| RelativeTimeFormatter (5 ladder tiers) | 100% | High |
| `NotificationsCountFlow` (refresh, setTo, decrement) | 100% | High |
| Cross-feature flow sync (mutation on screen → flow value updates → host VM sees new count) | 100% | High |

---

## Dependencies & Prerequisites

### Required Before Start

- [x] `constitution.md` reviewed — all 5 principles addressed above.
- [x] `spec.md` approved — 11/11 Q-Ns resolved (2026-05-15).
- [ ] `research.md` — NOT REQUIRED. Codebase patterns are well-rehearsed from prior session work (Search Sunner / Community Standards / Write Kudo all follow the same shape).
- [ ] **Backend `notifications` table + 4 Postgrest endpoints** — blocked on backend team. MVP can ship against `DemoNotificationRepository` only.
- [ ] **Profile / Kudo Detail / Secret Box / Admin Review screens** — currently all placeholders. The Notifications screen's row-tap navigation lands on placeholders for these destinations until each ships its own spec.

### External Dependencies

- None at MVP — DemoNotificationRepository is fully local.

---

## Next Steps

After plan approval:

1. **Run** `/momorph.tasks` to generate the granular task breakdown. Expected: ~18-20 atomic tasks given the migration scope.
2. **Review** `tasks.md` for ordering — Phase 1 + Phase 2 can run partially in parallel (domain types + relative-time formatter + Demo repo are independent). Migration in Phase 3 should be one VM at a time (Home → Kudos → Awards) with build-and-run between each.
3. **Begin** implementation. Commit per task per the user's `feedback_commit_per_task` memory.

---

## Notes

- **No `research.md`** — codebase patterns understood from the recent Search Sunner / Community Standards work.
- **No `testcase.md`** — spec's Acceptance Scenarios + this plan's Integration Testing Strategy together define the test contract.
- **No `contract.md`** — backend team owns the API contracts. Spec lists predicted endpoints.
- **Migration scope** — this plan touches 3 existing host VMs (Home / Kudos / Awards) and deletes 1 existing composable (`NotificationsSheet.kt`). Care must be taken to update existing tests during the migration so the build stays green.
- **Backend not blocking MVP** — the Demo impl is the MVP. When the backend lands, swap the `@Provides` to return `DefaultSupabaseNotificationRepository` (already structured via the `BuildConfig.DEMO_MODE` branch).
- **`NotificationsCountFlow` is the architectural lynchpin** — gets its initial value from the existing `NotificationsSummaryRepository`; tracks mutations from the Notifications screen; gets read by Home/Kudos/Awards host VMs for the bell badge. One singleton, three readers, two writers.
- **Type-7 ADMIN_REVIEW placeholder** — when the admin spec lands, swap `PlaceholderScreen(label = "Admin Review Content")` for the real composable. The Notifications screen's call site already passes the right route constant; no change needed there.
