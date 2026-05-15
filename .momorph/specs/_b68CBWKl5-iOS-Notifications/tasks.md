# Tasks: Sun*Kudos — Notifications

**Frame**: `_b68CBWKl5-iOS-Notifications`
**Prerequisites**: `plan.md` (required), `spec.md` (required) — both shipped, all 11 Q-Ns resolved
**Generated**: 2026-05-15

---

## Task Format

`- [ ] T### [P?] [Story?] Description with file path`

- **[P]**: Parallelizable (different files, no incomplete dependencies)
- **[USx]**: User-story tag (required for Phase 3+ tasks; absent on Setup / Foundational / Polish)

---

## User Story Map (from spec.md)

| Tag | Title | Priority |
|---|---|---|
| **US1** | Catch up on unread notifications (list + tap → destination + mark-read) | P1 |
| **US2** | Mark everything as read in one tap | P1 |
| **US3** | Follow inline "Tiêu chuẩn cộng đồng" link from a content-hidden notification | P2 |
| **US4** | Type-specific routing on row tap (7 destinations) | P1 |

Independent-testability: US1, US2, US4 are P1 and largely intertwined (the row composable + VM are shared). US3 plugs into the existing row composable as a CONTENT_HIDDEN-only conditional. Recommended MVP = US1 + US4 (the load + routing core), with US2 + US3 layered immediately after.

---

## Phase 1: Setup (Shared Infrastructure)

- [x] T001 Create new feature package directories: `app/src/main/java/com/example/aiddproject/kudos/notifications/{domain,data,ui}` and `app/src/main/java/com/example/aiddproject/kudos/notifications/ui/components`
- [x] T002 Create new test package directories: `app/src/test/java/com/example/aiddproject/kudos/notifications/{data,ui}` and `app/src/androidTest/java/com/example/aiddproject/kudos/notifications/ui`
- [x] T003 Append ~10 string resources to `app/src/main/res/values/strings.xml` under a `<!-- Notifications (Figma _b68CBWKl5) -->` block: `notifications_title` ("Thông báo"), `notifications_empty` ("Bạn chưa có thông báo nào"), `notifications_loading`, `notifications_error`, `notifications_retry`, `notifications_mark_all_read` ("Đánh dấu đọc tất cả"), `notifications_inline_community_standards` ("Tiêu chuẩn cộng đồng"), `a11y_notifications_back` ("Quay lại"), `a11y_notifications_row` (formatted `%1$s, %2$s, %3$s`), `a11y_notifications_mark_all_read`, plus relative-time plurals `relative_time_minutes_ago` / `_hours_ago` / `_days_ago` / `_months_ago` / `_years_ago` (single `%1$d` formatter each per Q-N-5 ladder)

---

## Phase 2: Foundational (Blocking prerequisites for all user stories)

- [x] T004 [P] Create `NotificationType` enum with 7 values (`KUDOS_RECEIVED`, `HEART_RECEIVED`, `SECRET_BOX_UNLOCK`, `LEVEL_UP`, `CONTENT_HIDDEN`, `BADGE_COLLECTED`, `REVIEW_REQUEST`) at `app/src/main/java/com/example/aiddproject/kudos/notifications/domain/NotificationType.kt`
- [x] T005 [P] Create `NotificationPayload` sealed interface with 4 variants (`KudoRef(kudoId, isAnonymous)`, `SecretBox`, `Profile`, `Review(reviewCount)`) at `app/src/main/java/com/example/aiddproject/kudos/notifications/domain/NotificationPayload.kt`
- [x] T006 [P] Create `NotificationItem` data class (id, type, isRead, createdAt: Instant, payload, displayBody) at `app/src/main/java/com/example/aiddproject/kudos/notifications/domain/NotificationItem.kt`
- [x] T007 [P] Create pure `RelativeTimeFormatter` function implementing the FR-010 ladder (`<60min → phút trước; <24h → giờ trước; <30 ngày → ngày trước; <12 tháng → tháng trước; else → năm trước`) at `app/src/main/java/com/example/aiddproject/kudos/notifications/ui/components/RelativeTimeFormatter.kt`. Pure function — takes `(createdAt: Instant, now: Instant, resources: Resources)`, returns `String`.
- [x] T008 [P] Create `RelativeTimeFormatterTest` covering all 5 ladder thresholds + edge cases at the 60-min, 24-hr, 30-day, 12-month boundaries at `app/src/test/java/com/example/aiddproject/kudos/notifications/ui/RelativeTimeFormatterTest.kt`
- [x] T009 Create `NotificationRepository` interface with `observeRecent(): Flow<List<NotificationItem>>`, `loadMore(cursor: String?): Result<Page>`, `markRead(id: String): Result<Unit>`, `markAllRead(): Result<Int>` at `app/src/main/java/com/example/aiddproject/kudos/notifications/data/NotificationRepository.kt`
- [x] T010 Create `DemoNotificationRepository` implementing the interface with an in-memory `MutableStateFlow<List<NotificationItem>>` seeded with one of each `NotificationType` (mix of read/unread, varied `createdAt` so relative-time ladder is exercised) at `app/src/main/java/com/example/aiddproject/kudos/notifications/data/DemoNotificationRepository.kt`
- [x] T011 Create `DefaultSupabaseNotificationRepository` skeleton that throws `NotImplementedError()` from each method (backend not yet authored; the `BuildConfig.DEMO_MODE` branch keeps this from running in demo builds) at `app/src/main/java/com/example/aiddproject/kudos/notifications/data/DefaultSupabaseNotificationRepository.kt`
- [x] T012 Create `NotificationsCountFlow` `@Singleton` wrapping `private val _count = MutableStateFlow<Int>(0)`, exposing `val count: StateFlow<Int>` + `suspend fun refreshFromServer()` (calls the existing `NotificationsSummaryRepository.get()`) + `fun setTo(value: Int)` + `fun decrement(by: Int = 1)` at `app/src/main/java/com/example/aiddproject/kudos/notifications/data/NotificationsCountFlow.kt`
- [x] T013 Create `NotificationRepositoryModule` Hilt module with `@Provides @Singleton` for `NotificationRepository` (branches on `BuildConfig.DEMO_MODE` like `HomeRepositoryModule`) at `app/src/main/java/com/example/aiddproject/kudos/notifications/data/NotificationRepositoryModule.kt`
- [x] T014 [P] Create `DemoNotificationRepositoryTest` covering observeRecent emits the seed, markRead flips isRead + decrements unread count, markAllRead returns the cleared count + sets all isRead=true, loadMore returns paginated chunks at `app/src/test/java/com/example/aiddproject/kudos/notifications/data/DemoNotificationRepositoryTest.kt`
- [x] T015 [P] Create `NotificationsCountFlowTest` covering setTo updates emission, decrement subtracts, refreshFromServer calls the stub summary repo at `app/src/test/java/com/example/aiddproject/kudos/notifications/data/NotificationsCountFlowTest.kt`
- [x] T016 Add `const val NOTIFICATIONS: String = "route_notifications"` and `const val ADMIN_REVIEW: String = "route_admin_review"` to `app/src/main/java/com/example/aiddproject/navigation/Routes.kt`
- [x] T017 Create `NotificationsTestTags` object with `SCREEN`, `BACK_BUTTON`, `MARK_ALL_READ_BUTTON`, `LIST`, `EMPTY_PLACEHOLDER`, `ERROR_RETRY`, `rowTag(notificationId)`, `unreadDotTag(notificationId)`, `inlineCommunityStandardsTag(notificationId)` at `app/src/main/java/com/example/aiddproject/kudos/notifications/ui/NotificationsTestTags.kt`

---

## Phase 3: User Story 1 (P1) — Catch up on unread notifications

**Goal**: a user can open the Notifications screen, see their list of notifications newest-first with unread rows visibly marked, tap a row to navigate to the type-specific destination, and have the tapped row mark-read on return.

**Independent test**: with the demo repo seed (one of each type, some unread), open `Routes.NOTIFICATIONS` directly via `navController.navigate(...)`, verify the list renders with unread rows showing the red-dot indicator. Tap an unread row → captures the navigation callback → return to Notifications → red-dot is gone.

- [x] T018 [P] [US1] Create `NotificationsUiState` data class + `NotificationsListState` sealed interface (`Loading` / `Loaded(items, hasMore)` / `Empty` / `Error(messageRes)`) at `app/src/main/java/com/example/aiddproject/kudos/notifications/ui/NotificationsUiState.kt`
- [x] T019 [US1] Create `NotificationsViewModel` `@HiltViewModel` injecting `NotificationRepository` + `NotificationsCountFlow`. `init {}` launches `repo.observeRecent()` collector + calls `countFlow.refreshFromServer()`. Intents: `onRowTap(item, onNavigate)` (optimistic local mutation `isRead=true`, fire `repo.markRead`, decrement countFlow, invoke `onNavigate`); `onRefresh()` (re-issue load); `onLoadMore()` (cursor pagination). Implement at `app/src/main/java/com/example/aiddproject/kudos/notifications/ui/NotificationsViewModel.kt`
- [x] T020 [US1] Create `NotificationRow` composable taking `NotificationItem` + `onRowTap` + `onInlineCommunityStandardsTap` (no-op for non-CONTENT_HIDDEN types). Renders type-keyed icon (placeholder Material icon for now per Phase-0 Note 0), `displayBody`, relative-time, and the red-dot indicator when `!isRead`. Inline link rendered only when `type == CONTENT_HIDDEN`. At `app/src/main/java/com/example/aiddproject/kudos/notifications/ui/components/NotificationRow.kt`
- [x] T021 [US1] Create `NotificationsContent` stateless composable hosting `Scaffold(topBar = TopAppBar(navigationIcon=back, title="Thông báo", actions=ReadAllButton)) { padding -> PullToRefreshBox { ... } }`. Branches on `state.listState`: Loading → centered spinner, Empty → centered `notifications_empty` text, Error → centered text + retry CTA, Loaded → `LazyColumn` of `NotificationRow`s. Accepts a `callbacks: NotificationsCallbacks` data class with 8 lambdas. At `app/src/main/java/com/example/aiddproject/kudos/notifications/ui/NotificationsContent.kt`
- [x] T022 [US1] Create `NotificationsScreen` thin Hilt entry: takes nav lambdas (`onNavigateBack`, `onNavigateToKudoDetail(kudoId, isAnonymous)`, `onNavigateToSecretBoxOpen`, `onNavigateToProfile`, `onNavigateToCommunityStandards`, `onNavigateToAdminReview`); wires `BackHandler`; collects `viewModel.state`; constructs the `NotificationsCallbacks` bag and delegates to `NotificationsContent`. At `app/src/main/java/com/example/aiddproject/kudos/notifications/ui/NotificationsScreen.kt`
- [x] T023 [US1] Wire `composable(Routes.NOTIFICATIONS) { NotificationsScreen(...) }` and `composable(Routes.ADMIN_REVIEW) { PlaceholderScreen(label = "Admin Review Content") }` in `app/src/main/java/com/example/aiddproject/navigation/AppNavigation.kt`. Nav-to-Profile/KudoDetail/SecretBox uses the `savedStateHandle` handshake from commit `3e7f91d`.
- [x] T024 [US1] Migrate Home: in `app/src/main/java/com/example/aiddproject/home/ui/HomeScreen.kt` remove `var notificationsSheetVisible by rememberSaveable` (line 75), the `if (notificationsSheetVisible) { NotificationsSheet(...) }` block (lines 116-122), the `notificationsSheetVisible` parameter on `HomeScreenContent`, and the SAA-tab scroll-suppress guard (line 150). Add `onNavigateToNotifications: () -> Unit` parameter; change `onBellClick = { notificationsSheetVisible = true }` to `onBellClick = onNavigateToNotifications`. Update the AppNavigation `composable(Routes.HOME) { HomeScreen(... onNavigateToNotifications = { navController.navigate(Routes.NOTIFICATIONS) } ...) }` call site.
- [x] T025 [US1] Refactor `app/src/main/java/com/example/aiddproject/home/ui/HomeViewModel.kt`: drop `onNotificationsSheetDismissed()` method (line 118), drop the `notificationsState = MutableStateFlow<NotificationsState>` field (line 53), drop the direct `notificationsRepository.get().fold {...}` (line 183-192). Inject `NotificationsCountFlow`; on init call `viewModelScope.launch { countFlow.refreshFromServer() }`. Expose `unreadCount` via the existing UiState (now mirroring `countFlow.count.value`).
- [x] T026 [US1] Refactor `app/src/main/java/com/example/aiddproject/home/ui/HomeUiState.kt`: drop `val notifications: NotificationsState` field (line 22); `val unreadCount: Int` (line 25) now derived from a constructor argument fed by the `NotificationsCountFlow` collector in `HomeViewModel.combine(...)`.
- [x] T027 [US1] Delete `app/src/main/java/com/example/aiddproject/home/domain/states/NotificationsState.kt` — only consumed by HomeUiState/HomeViewModel; both refactored above.
- [x] T028 [US1] Update `app/src/test/java/com/example/aiddproject/home/ui/HomeViewModelTest.kt`: delete the test at line 203 (`onNotificationsSheetDismissed re-fires only the notifications fetch (US6)`); update any other test that asserts on `notificationsState` (replace with `countFlow.count.value` assertions).
- [x] T029 [US1] Create `NotificationsViewModelTest` covering: initial state is Loading; observeRecent emits → state transitions to Loaded; onRowTap fires `markRead` + decrements countFlow + invokes onNavigate exactly once; onRefresh re-issues load. Use Turbine + StandardTestDispatcher. At `app/src/test/java/com/example/aiddproject/kudos/notifications/ui/NotificationsViewModelTest.kt`
- [x] T030 [US1] Add a `@Preview` decorator for `NotificationsContent` showing the Loaded state with the demo seed in `NotificationsContent.kt` so the populated state can be reviewed in Android Studio Preview.

---

## Phase 4: User Story 2 (P1) — Mark everything as read

**Goal**: tapping "Đánh dấu đọc tất cả" clears every red-dot at once + decrements the bell badge to 0 globally. Optimistic update with rollback on failure.

**Independent test**: with ≥ 2 unread notifications in state, tap the read-all button → all rows lose the red-dot in a single recomposition → countFlow value goes to 0. Inject a fake repo that fails `markAllRead()` → state rolls back to pre-tap.

- [x] T031 [P] [US2] Extend `NotificationsViewModel` with `onReadAll()` intent: early-return if `unreadCount == 0` (per Q-N-10); else snapshot current state; optimistically set all `isRead = true`; set `countFlow.setTo(0)`; launch `repo.markAllRead()`; on failure → emit Snackbar message + restore snapshot + restore countFlow value. Implement in the existing `NotificationsViewModel.kt` file (no new file).
- [x] T032 [US2] Add `ReadAllButton` icon-text composable invoked from `NotificationsContent`'s TopAppBar `actions` slot. Uses `stringResource(R.string.notifications_mark_all_read)` + an icon (Material `Done` or custom — Phase-0 audit). Tag: `MARK_ALL_READ_BUTTON`. Implement at `app/src/main/java/com/example/aiddproject/kudos/notifications/ui/components/ReadAllButton.kt`
- [x] T033 [US2] Add Snackbar host wiring in `NotificationsContent` for the read-all-failure rollback message. The `NotificationsUiState` gains a `snackbar: SnackbarMessage?` slot; VM exposes `onConsumeSnackbar()` to clear it after display.
- [x] T034 [US2] Extend `NotificationsViewModelTest` with: `onReadAll happy path clears unread + decrements countFlow`; `onReadAll on empty unread is a no-op + no API call fired`; `onReadAll rollback restores state + countFlow when markAllRead returns failure`. At the existing test file.

---

## Phase 5: User Story 4 (P1) — Type-specific routing on row tap

**Goal**: each of the 7 notification types navigates to the correct destination on row tap.

**Independent test**: with one notification of each type in state, tap each row in turn, verify the corresponding nav lambda fires with the correct args (e.g. `onNavigateToKudoDetail("kudo-xyz", isAnonymous=false)` for KUDOS_RECEIVED).

- [x] T035 [US4] Author the routing branch in `NotificationsViewModel.onRowTap`: switch on `item.type` + `item.payload` to invoke the right nav callback. Routes per Q-N-2/Q-N-3/spec US4: KUDOS_RECEIVED & HEART_RECEIVED & CONTENT_HIDDEN → `onNavigateToKudoDetail(kudoId, isAnonymous)`; SECRET_BOX_UNLOCK → `onNavigateToSecretBoxOpen`; LEVEL_UP & BADGE_COLLECTED → `onNavigateToProfile`; REVIEW_REQUEST → `onNavigateToAdminReview`.
- [ ] T036 [US4] Conduct Phase-0 icon audit: query Figma for each of the 7 icon component instances (per the plan's table — `componentId: 6885:8273` through `6885:8313`); map each to either a Material default with tint OR a downloaded Figma export under `app/src/main/res/drawable-xxhdpi/ic_notification_*.png`. Update `NotificationType.iconResId` extension function to return the chosen drawable. Implement at `app/src/main/java/com/example/aiddproject/kudos/notifications/domain/NotificationType.kt` (extends the enum file from T004).
- [x] T037 [US4] Migrate Kudos hub: in `app/src/main/java/com/example/aiddproject/kudos/ui/KudosScreen.kt` add `onNavigateToNotifications: () -> Unit` parameter; replace the no-op `onBellClick` lambda (line 68) with `onBellClick = onNavigateToNotifications`. In `KudosScreenContent.kt` line 146 change `unreadCount = 0` to `unreadCount = state.unreadCount`. In `KudosViewModel.kt` inject `NotificationsCountFlow` + expose `unreadCount` via the UiState. Update the AppNavigation call site to pass `onNavigateToNotifications = { navController.navigate(Routes.NOTIFICATIONS) }`.
- [x] T038 [US4] Migrate Award Detail: same sheet→route pattern as Home (T024). In `app/src/main/java/com/example/aiddproject/awarddetail/ui/AwardDetailScreen.kt` remove the `notificationsSheetVisible` state (line 40), the `NotificationsSheet(...)` render block (line 64-72), and the import (line 12). Add `onNavigateToNotifications` parameter; rewire `onBellClick`. Update the AppNavigation call site.
- [ ] T039 [US4] Create `NotificationsScreenTest` instrumented Compose UI test covering: (a) for each of the 7 NotificationTypes, tap the row → assert the corresponding nav lambda fires exactly once with the correct args; (b) back arrow + system back both fire `onNavigateBack` exactly once; (c) Loading/Empty/Error states each render the correct testTag. At `app/src/androidTest/java/com/example/aiddproject/kudos/notifications/ui/NotificationsScreenTest.kt`

---

## Phase 6: User Story 3 (P2) — Inline Community Standards link

**Goal**: tapping the inline "Tiêu chuẩn cộng đồng" link inside a CONTENT_HIDDEN row navigates to the Community Standards screen WITHOUT also firing the parent row's tap handler.

**Independent test**: with a CONTENT_HIDDEN notification in state, tap the inline link → `onNavigateToCommunityStandards` fires exactly once; `onRowTap` callback does NOT fire (verify via captured counters in the instrumented test).

- [x] T040 [US3] Add the `onInlineCommunityStandardsTap` lambda parameter to `NotificationRow` (already declared on the composable per T020). Render an IconButton + text label inside the CONTENT_HIDDEN row's content frame — conditional on `type == NotificationType.CONTENT_HIDDEN`. The IconButton's pointer-input naturally consumes the gesture before bubbling to the parent row's `clickable`. Implement in `NotificationRow.kt`.
- [x] T041 [US3] Add `onInlineCommunityStandardsTap` to the `NotificationsCallbacks` data class (in `NotificationsContent.kt`) and wire `callbacks.onInlineCommunityStandardsTap = onNavigateToCommunityStandards` in `NotificationsScreen.kt`.
- [ ] T042 [US3] Extend `NotificationsScreenTest` with: (a) tap on inline link → `onNavigateToCommunityStandards` fires exactly once; (b) tap on inline link → `onRowTap` does NOT fire (zero invocations). Update `NotificationsScreenTest.kt`.

---

## Phase 7: Polish & Cross-Cutting

- [x] T043 Delete the obsolete bottom-sheet composable: `app/src/main/java/com/example/aiddproject/home/ui/components/NotificationsSheet.kt` (57 LOC, replaced by the dedicated screen across all 3 entry points).
- [x] T044 Delete the obsolete instrumented test: `app/src/androidTest/java/com/example/aiddproject/home/NotificationsSheetTest.kt` (tests the deleted sheet; equivalent contract moved to `NotificationsScreenTest.kt` in T039 + T042).
- [ ] T045 Run `./gradlew :app:testDebugUnitTest` — verify all unit tests pass (including the modified HomeViewModelTest from T028). Fix any breakages introduced by the migration.
- [ ] T046 Run `./gradlew :app:assembleDebug` — verify the build is green; address any unresolved references from the deletions.
- [ ] T047 TalkBack walk-through on a physical device (or emulator): bell tap from Home → Notifications screen; verify each row reads its merged contentDescription correctly; verify the inline Community Standards link reads as its own focusable button; verify the read-all button reads its label.
- [ ] T048 System font-scale 200% check on a physical device: verify rows wrap multi-line without truncation; verify the red-dot stays right-aligned; verify the inline link doesn't crowd the relative-time text.
- [ ] T049 Locale toggle test: switch device language between VN/EN/JA → verify chrome strings (title, button, placeholder, error) re-render; verify the relative-time formatter respects the locale (the `Resources` parameter handles plural pickers via `getQuantityString`).
- [ ] T050 Tag the Figma frame `_b68CBWKl5` with "Spec Created" via `mcp__momorph__upload_specs` (out-of-band).

---

## Dependencies

```
Phase 1 (Setup)
  ↓
Phase 2 (Foundational) ─ All P parallelizable internally (T004-T008 are independent; T009 must precede T010-T013; T014-T015 [P] after their respective subjects exist)
  ↓
Phase 3 (US1) ─ Largest phase; T018-T022 build the UI shell; T023 wires nav; T024-T028 migrate Home (sequential — same file groups); T029 [P] adds tests
  ↓
Phase 4 (US2) ─ All tasks layer onto the VM/Content from Phase 3; T031-T033 sequential (same files); T034 [P] adds tests
  ↓
Phase 5 (US4) ─ T035 layers onto VM; T036 is independent (icon audit); T037-T038 migrate other host VMs; T039 caps with instrumented test
  ↓
Phase 6 (US3) ─ Trivial extension on top of US4's row composable
  ↓
Phase 7 (Polish) ─ Deletions, full-build verification, manual checks
```

**Critical-path constraint**: Phase 3's migration steps (T024-T027) all touch related files in the Home feature and must be sequential — running them in parallel risks merge conflicts. Run them in order, with a build between each.

---

## Parallel Execution Examples

### Phase 2 (Foundational) — 5 [P] tasks runnable concurrently
The domain types (T004, T005, T006), the formatter (T007), and its test (T008) all touch independent new files with no cross-imports.

### Phase 3 (US1) — VM, Content, Tests in parallel
T018 (UiState), T029 (VM tests, after VM exists) are independent of the host-migration tasks (T024-T028). The migration must be sequential within Home; tests + the new Notifications screen can be written in parallel.

### Phase 5 (US4) — Icon audit + Kudos migration + Awards migration
T036 (icon audit — touches only `NotificationType.kt`), T037 (Kudos migration — Kudos files), T038 (Awards migration — AwardDetailScreen) all touch disjoint files. [P] across all three.

### Phase 7 (Polish) — Sequential
The polish phase is mostly verification + cleanup; T043 + T044 deletions are independent but their effects on the build are not, so verify with T045/T046 between deletions.

---

## Implementation Strategy

**MVP** (suggested ship target): **US1 + US4** — load the list, render newest-first with read-state indicators, and route each tap to the correct destination. Without these, the screen is functionally broken. US2 (read-all) and US3 (inline link) layer cleanly afterwards.

**Recommended commit cadence** (per the user's `feedback_commit_per_task` memory):
- 1 commit per Phase 1 / Phase 2 task group
- 1 commit per migration sub-step in Phase 3 (Home migration, Kudos migration, Awards migration are independent enough to land separately)
- 1 commit per Phase 4 / Phase 5 / Phase 6
- 1 commit per Phase 7 deletion

**Backend dependency**: backend team owns the 4 Postgrest endpoints (`GET /notifications`, `PATCH /notifications/{id}/read`, `PATCH /notifications/read-all`, `GET /notifications/unread-count`) AND the `notifications` table schema + RLS policies. At MVP, ship against `DemoNotificationRepository` only (`BuildConfig.DEMO_MODE`). When the backend lands, swap `T011`'s `NotImplementedError()` stubs for real Postgrest calls — no changes elsewhere needed.

**Risk hotspots** (from plan.md):
- The migration (T024-T028 + T037 + T038) touches existing code. Run all existing instrumented tests before merging.
- T036 icon audit is gated by Figma access — start it as soon as Phase 2 finishes so it's not on the critical path.
- The `NotificationsCountFlow` singleton must be injected into all 3 host VMs (Home, Kudos, Awards). Verify with a manual smoke test: tap bell from each, verify the badge clears after a read-all.

---

## Total task count

**50 tasks**, distributed:

| Phase | Tasks | Tags |
|---|---|---|
| 1 — Setup | T001 – T003 | 3 tasks, no story tag |
| 2 — Foundational | T004 – T017 | 14 tasks, no story tag (8 of which are [P]) |
| 3 — US1 | T018 – T030 | 13 tasks |
| 4 — US2 | T031 – T034 | 4 tasks |
| 5 — US4 | T035 – T039 | 5 tasks |
| 6 — US3 | T040 – T042 | 3 tasks |
| 7 — Polish | T043 – T050 | 8 tasks, no story tag |

Note: Tasks within US1 are the most numerous because that's where the full migration (4 sub-steps) lives. US4 tasks are smaller per-item but more strategically important (the icon audit and the 2 remaining host migrations).
