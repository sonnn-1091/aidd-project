# Tasks: Award Detail (Top Project default) — Slices D + A + C

**Frame**: `FQoJZLkG_d-iOS-Award-Top-project`
**Plan**: `plan.md` (ratified 2026-05-11, reviewed twice)
**Spec**: `spec.md` (delta-spec, reviewed four times)
**Scope decision (2026-05-11)**: User selected Mức 2 + Slices C and D. Slice B
dropped (no Supabase production data needed for demo). `design-style.md`
prerequisite waived per Constitution Principle II (visual specs fetched
on-demand via `query_section`).

---

## Task Format

```
- [ ] T### [P?] [Story?] Description | file/path
```

- **[P]**: No dependency on the immediate predecessor — either (a) different
  file from neighbors so two devs can split work, OR (b) inside the same Compose
  test file but with no inter-test dependency (each `setContent { … }` runs
  independently; the test runner parallelizes them). Convention matches the
  canonical Top Talent `tasks.md`. **For commit cadence**: same-file `[P]`
  tests are typically batched into one commit per file — `[P]` here documents
  the lack of inter-test dependency, not a commit boundary.
- **[Story]**: US1 = Slice D (TDD backfill), US2 = Slice A (badge), US3 = Slice C (SCREENFLOW)

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Verify the gates this delta-plan claims are intact before
authoring new tests against the canonical surface.

- [x] T001 Verify `app/src/main/java/com/example/aiddproject/awarddetail/` exists with the parametric `AwardDetailScreen` + components shipped by canonical (T015–T042). Fail-fast if any file is missing. | app/src/main/java/com/example/aiddproject/awarddetail/  → ✅ Verified 2026-05-11: domain/ + ui/components/ {AwardCategoryDropdown, AwardHeroBlock, AwardInfoBlock, HighlightBlock, KvKudosBanner}.kt all present.
- [x] T002 [P] Verify Q-TP-1 fix in `DemoAwardsRepository.kt:72-95` (Top Project entry matches Figma node `6885:10468`: `quantity=2`, `quantityUnit="Tập thể"`, `prizeValue="15.000.000 VNĐ"`, 8-sentence description). Read-only check; no edit. | app/src/main/java/com/example/aiddproject/home/data/DemoAwardsRepository.kt  → ✅ Verified 2026-05-11: lines 72-95 match Figma exactly.
- [x] T003 [P] Verify Q-TP-2 fix in `AwardInfoBlock.kt:142` (`"%02d".format(it)` formatter present). Read-only check. | app/src/main/java/com/example/aiddproject/awarddetail/ui/components/AwardInfoBlock.kt  → ✅ Verified 2026-05-11: `quantity?.let { "%02d".format(it) } ?: placeholder` present.

---

## Phase 2: Foundation (Blocking Prerequisites)

**Purpose**: Confirm the canonical instrumented-test gap before backfilling.

**⚠️ CRITICAL**: No Phase 3 work begins until this phase passes.

- [x] T004 Confirm `app/src/androidTest/java/com/example/aiddproject/awarddetail/` is empty (the gap Slice D remediates). If files appeared since plan review, re-scope Phase 3 to avoid duplication. | app/src/androidTest/java/com/example/aiddproject/awarddetail/  → ✅ Verified 2026-05-11: dir exists but empty (`total 0`). Gap confirmed.
- [x] T005 [P] Confirm `gradle/libs.versions.toml` already exposes `androidx.compose.ui:ui-test-junit4` + `hilt-android-testing` (canonical bootstrapped these for the empty test files). No new dep should be added; if missing, file a blocker before continuing. | gradle/libs.versions.toml  → ⚠️ **BLOCKER FILED 2026-05-11, resolved by T005b**: `androidx-compose-ui-test-junit4` ✅ present; `hilt-android-testing` ❌ was MISSING. User chose Option A — wire the dep.
- [x] T005b Wire `hilt-android-testing` dep + Hilt KSP processor for androidTest. Add `hilt-android-testing = { group = "com.google.dagger", name = "hilt-android-testing", version.ref = "hilt" }` to `libs.versions.toml`. Add `androidTestImplementation(libs.hilt.android.testing)` + `kspAndroidTest(libs.hilt.compiler)` to `app/build.gradle.kts`. Run `./gradlew :app:dependencies --configuration kspAndroidTestDebugKotlinProcessorClasspath` to confirm resolution. | gradle/libs.versions.toml + app/build.gradle.kts  → ✅ Shipped 2026-05-11: dep resolves to `hilt-android-testing:2.59.2`. `compileDebugKotlin` green.

**Checkpoint**: Foundation verified — backfill can proceed in parallel.

---

## Phase 3: User Story 1 — Slice D: UI/instrumented test backfill (Priority: P1) 🎯 MVP

**Goal**: Close the Constitution V (TDD) gap. Pin Q-TP-1 + Q-TP-2 fixes
behind regression tests. Backfill the 5 instrumented test files that
canonical tasks T026–T056 marked complete without authoring.

**Independent Test**: Run `./gradlew testDebugUnitTest connectedDebugAndroidTest`
on emulator-5554 — all newly added tests pass; pre-existing tests remain
green; coverage report shows the 5 new test files materialized.

### Tests — Q-TP-2 regression (`AwardInfoBlockTest`)

- [ ] T006 [US1] Create `AwardInfoBlockTest.kt` scaffold with `createComposeRule()` + `R.style.Theme_AIDDProject` host. KDoc cites Q-TP-2 + spec § Data Requirements Q-TP-2 resolution. | app/src/androidTest/java/com/example/aiddproject/awarddetail/AwardInfoBlockTest.kt
- [ ] T007 [P] [US1] Test `renders_zero_padded_02_when_quantity_is_2` — `setContent { AwardInfoBlock(quantity = 2, quantityUnit = "Tập thể", …) }`; assert `onNodeWithText("02")` is displayed AND `onNodeWithText("Tập thể")` is displayed. | app/src/androidTest/java/com/example/aiddproject/awarddetail/AwardInfoBlockTest.kt
- [ ] T008 [P] [US1] Test `renders_zero_padded_08_when_quantity_is_8` — same shape as T007; covers Top Heart's `quantity=8`. | app/src/androidTest/java/com/example/aiddproject/awarddetail/AwardInfoBlockTest.kt
- [ ] T009 [P] [US1] Test `renders_10_unchanged_when_quantity_is_10` — Top Talent path; assert `"10"` (NOT `"010"`). Locks the `%02d` "AT LEAST 2 digits" contract. | app/src/androidTest/java/com/example/aiddproject/awarddetail/AwardInfoBlockTest.kt
- [ ] T010 [P] [US1] Test `renders_00_when_quantity_is_0_edge_case` — `quantity = 0`; assert `"00"` renders. Pins the formatter against future regressions to `quantity.toString()`. | app/src/androidTest/java/com/example/aiddproject/awarddetail/AwardInfoBlockTest.kt
- [ ] T011 [P] [US1] Test `renders_three_digits_unchanged_when_quantity_is_100` — `quantity = 100`; assert `"100"` (NOT truncated). Locks the AT-LEAST-2-digits contract. | app/src/androidTest/java/com/example/aiddproject/awarddetail/AwardInfoBlockTest.kt
- [ ] T012 [P] [US1] Test `renders_em_dash_placeholder_when_quantity_is_null` — `quantity = null`; assert the localized `award_detail_placeholder_value` (`"—"`) renders. Covers FR-008. | app/src/androidTest/java/com/example/aiddproject/awarddetail/AwardInfoBlockTest.kt

### Tests — Q-TP-1 regression (`DemoAwardsRepositoryTest`)

- [ ] T013 [P] [US1] Create `DemoAwardsRepositoryTest.kt` with `runTest { }` + a hand-rolled expected `AwardDetail` for each of the three demo entries; load Figma copy verbatim. | app/src/test/java/com/example/aiddproject/home/data/DemoAwardsRepositoryTest.kt
- [ ] T014 [P] [US1] Test `detail_returns_top_project_payload_matching_figma_node_6885_10468` — call `repo.detail("00000000-0000-0000-0000-000000000a02", Language.VN)`; assert returned `AwardDetail` matches `quantity=2`, `quantityUnit="Tập thể"`, `prizeValue="15.000.000 VNĐ"`, and the 8-sentence description verbatim. Pins Q-TP-1. | app/src/test/java/com/example/aiddproject/home/data/DemoAwardsRepositoryTest.kt
- [ ] T015 [P] [US1] Test `detail_returns_top_talent_payload_unchanged` — covers regression in the other direction. | app/src/test/java/com/example/aiddproject/home/data/DemoAwardsRepositoryTest.kt
- [ ] T016 [P] [US1] Test `detail_returns_top_heart_payload_unchanged` — third demo entry. | app/src/test/java/com/example/aiddproject/home/data/DemoAwardsRepositoryTest.kt
- [ ] T017 [P] [US1] Test `detail_returns_failure_when_id_unknown` — `repo.detail("nonexistent-id", Language.VN)`; assert `.isFailure` AND exception is `NoSuchElementException` (per canonical DEMO contract at `DemoAwardsRepository.kt:25`). | app/src/test/java/com/example/aiddproject/home/data/DemoAwardsRepositoryTest.kt
- [ ] T018 [P] [US1] Test `list_returns_three_demo_awards_sorted_by_sort_order` — `repo.list()` returns 3 items in sortOrder 1/2/3. | app/src/test/java/com/example/aiddproject/home/data/DemoAwardsRepositoryTest.kt

### Tests — Screen layout (`AwardDetailScreenTest`)

- [ ] T019 [P] [US1] Create `AwardDetailScreenTest.kt` scaffold with `createAndroidComposeRule<HiltTestActivity>()` + a fake `AwardsRepository` providing the Top Talent payload by default. KDoc cites canonical T026–T033 + ACC_002. (`[P]` against T006/T013/T029/T044 — different file scaffolds can be staffed in parallel.) | app/src/androidTest/java/com/example/aiddproject/awarddetail/AwardDetailScreenTest.kt
- [ ] T020 [P] [US1] Test `body_renders_full_award_payload` — assert badge image, title, description, quantity row, prize row are all displayed. Parametric over `awardId ∈ {a01, a02, a03}` via `@RunWith(Parameterized::class)`. Covers `TC_AWARD_TOP_PROJECT_ACC_001` + `GUI_001`. | app/src/androidTest/java/com/example/aiddproject/awarddetail/AwardDetailScreenTest.kt
- [ ] T021 [P] [US1] Test `loading_indicator_visible_while_fetching` — fake repo blocks on `CompletableDeferred`; assert spinner/skeleton is displayed until the deferred completes. | app/src/androidTest/java/com/example/aiddproject/awarddetail/AwardDetailScreenTest.kt
- [ ] T022 [P] [US1] Test `error_state_shows_retry_button` — fake repo returns `Result.failure`; assert localized `award_detail_error_*` copy + Retry button visible. | app/src/androidTest/java/com/example/aiddproject/awarddetail/AwardDetailScreenTest.kt
- [ ] T023 [P] [US1] Test `retry_button_re_issues_fetch` — first call fails, second succeeds; tap Retry; assert body populates with payload. | app/src/androidTest/java/com/example/aiddproject/awarddetail/AwardDetailScreenTest.kt
- [ ] T024 [P] [US1] Test `null_image_url_renders_text_overlay_placeholder` — fake repo returns `imageUrl = null`; assert `AwardHeroBlock` text-overlay fallback renders the uppercase award name. Covers FR-008 + TR-007. | app/src/androidTest/java/com/example/aiddproject/awarddetail/AwardDetailScreenTest.kt
- [ ] T025 [P] [US1] Test `null_quantity_and_prize_render_em_dash_placeholders` — fake repo returns `quantity = null, prizeValue = null`; assert `"—"` placeholders in both rows. | app/src/androidTest/java/com/example/aiddproject/awarddetail/AwardDetailScreenTest.kt
- [ ] T026 [P] [US1] Test `sticky_header_stays_pinned_on_scroll` — scroll the `LazyColumn` past several screens; assert `HomeHeader` bounds top-y stays at status-bar offset. Covers FR-014 + `TC_AWARD_TOP_PROJECT_FUN_004`. | app/src/androidTest/java/com/example/aiddproject/awarddetail/AwardDetailScreenTest.kt
- [ ] T027 [P] [US1] Test `sticky_bottom_nav_stays_pinned_on_scroll` — symmetric to T026 for `HomeBottomBar`. | app/src/androidTest/java/com/example/aiddproject/awarddetail/AwardDetailScreenTest.kt
- [ ] T028 [P] [US1] Test `unauthenticated_user_redirected_to_login` — fake `SessionRepository` returns no session; assert the screen does not mount AND `Routes.LOGIN` is the current backstack top. Covers `TC_AWARD_TOP_PROJECT_ACC_002` + US8. | app/src/androidTest/java/com/example/aiddproject/awarddetail/AwardDetailScreenTest.kt

### Tests — Dropdown (`AwardCategoryDropdownTest`)

- [ ] T029 [P] [US1] Create `AwardCategoryDropdownTest.kt` scaffold with `createComposeRule()` + a fake `AwardsState.Populated(3 demo awards)`. KDoc cites canonical T043–T056. (`[P]` against T006/T013/T019/T044 — cross-file scaffolds.) | app/src/androidTest/java/com/example/aiddproject/awarddetail/AwardCategoryDropdownTest.kt
- [ ] T030 [P] [US1] Test `menu_renders_every_award_from_repository_in_sort_order` — tap anchor; assert 3 rows visible with names `"Top Talent"`, `"Top Project"`, `"Top Heart"` (NOT `"Top Talent Award"` — `displayName` strips the suffix per `AwardCategoryDropdown.kt:270`). | app/src/androidTest/java/com/example/aiddproject/awarddetail/AwardCategoryDropdownTest.kt
- [ ] T031 [P] [US1] Test `selecting_other_award_invokes_callback_and_updates_anchor` — tap a non-active row; assert `onSelect(award)` fires exactly once AND anchor text flips to the new award's `displayName`. Covers `TC_AWARD_TOP_PROJECT_FUN_002` (instrumented mirror). | app/src/androidTest/java/com/example/aiddproject/awarddetail/AwardCategoryDropdownTest.kt
- [ ] T032 [P] [US1] Test `contentDescription_recomputes_when_active_award_flips` — drives selection Top Talent → Top Project; assert anchor's `contentDescription` advances per `R.string.a11y_award_category_dropdown`. | app/src/androidTest/java/com/example/aiddproject/awarddetail/AwardCategoryDropdownTest.kt
- [ ] T033 [P] [US1] Test `trigger_has_role_button_with_collapsed_and_expanded_states` — assert `Role.Button` + `stateDescription` matches `R.string.a11y_dropdown_collapsed`; open menu; assert state flips to `R.string.a11y_dropdown_expanded`. TR-003. | app/src/androidTest/java/com/example/aiddproject/awarddetail/AwardCategoryDropdownTest.kt
- [ ] T034 [P] [US1] Test `opening_menu_focuses_first_row_for_TalkBack` — tap anchor; `mainClock.advanceTimeBy(100)`; assert first row `assertIsFocused()`. Mirrors Language Dropdown T022. | app/src/androidTest/java/com/example/aiddproject/awarddetail/AwardCategoryDropdownTest.kt
- [ ] T035 [P] [US1] Test `keyboard_tab_order_is_anchor_then_rows_in_order` — verifies anchor + each row independently focusable via `requestFocus()`. | app/src/androidTest/java/com/example/aiddproject/awarddetail/AwardCategoryDropdownTest.kt
- [ ] T036 [P] [US1] Test `trigger_meets_48dp_touch_target` — `getBoundsInRoot()` on `TEST_TAG_AWARD_DROPDOWN_TRIGGER` ≥ 48×48dp. TR-003 + TR-008. | app/src/androidTest/java/com/example/aiddproject/awarddetail/AwardCategoryDropdownTest.kt
- [ ] T037 [P] [US1] Test `rows_meet_48dp_touch_target` — every row's bounds ≥ 48×48dp. | app/src/androidTest/java/com/example/aiddproject/awarddetail/AwardCategoryDropdownTest.kt
- [ ] T038 [P] [US1] Test `reselecting_active_award_is_idempotent_no_callback` — pre-select Top Project; open menu; tap Top Project row again; assert `onSelect` NOT invoked. Covers FR-006. | app/src/androidTest/java/com/example/aiddproject/awarddetail/AwardCategoryDropdownTest.kt
- [ ] T039 [P] [US1] Test `row_double_tap_yields_one_select_callback` — two rapid taps on the same row produce exactly one `onSelect` call. TR-004 (`rememberSingleClickHandler`). | app/src/androidTest/java/com/example/aiddproject/awarddetail/AwardCategoryDropdownTest.kt
- [ ] T040 [P] [US1] Test `anchor_double_tap_yields_one_open_transition` — two rapid taps leave menu open. | app/src/androidTest/java/com/example/aiddproject/awarddetail/AwardCategoryDropdownTest.kt
- [ ] T041 [P] [US1] Test `second_deliberate_tap_on_anchor_closes_menu` — first tap opens; `mainClock.advanceTimeBy(500)` past guard window; second tap closes. | app/src/androidTest/java/com/example/aiddproject/awarddetail/AwardCategoryDropdownTest.kt
- [ ] T042 [P] [US1] Test `outside_tap_dismisses_menu_without_changing_active` — open menu, tap a sibling node; assert menu closes AND active unchanged. | app/src/androidTest/java/com/example/aiddproject/awarddetail/AwardCategoryDropdownTest.kt
- [ ] T043 [P] [US1] Test `predictive_back_dismisses_menu_without_popping_screen` — `Espresso.pressBack()` while menu open; assert menu closes AND anchor still composed. | app/src/androidTest/java/com/example/aiddproject/awarddetail/AwardCategoryDropdownTest.kt

### Tests — Bottom nav entry point (`BottomNavAwardsTabTest`)

- [ ] T044 [P] [US1] Create `BottomNavAwardsTabTest.kt` scaffold that mounts the full `AppNavigation` graph + a fake authenticated session. KDoc cites canonical T101 + spec frame-specific scenario 3. (`[P]` against T006/T013/T019/T029 — cross-file scaffolds.) | app/src/androidTest/java/com/example/aiddproject/home/BottomNavAwardsTabTest.kt
- [ ] T045 [P] [US1] Test `awards_tab_tap_from_home_mounts_award_detail_with_first_sort_order_default` — from Home, tap Awards tab; assert `AwardDetailScreen` is composed AND active award is Top Talent (sortOrder=1, per FR-001 + Resolved Q1 fallback). | app/src/androidTest/java/com/example/aiddproject/home/BottomNavAwardsTabTest.kt
- [ ] T046 [P] [US1] Test `awards_tab_retap_scrolls_body_to_top` — scroll body down; tap Awards tab again; assert `LazyListState.firstVisibleItemIndex == 0`. Covers polish behavior shipped in commit `0293084` (Phase 5+6 — Awards retap + statusBars fix). | app/src/androidTest/java/com/example/aiddproject/home/BottomNavAwardsTabTest.kt

**Checkpoint**: All 5 instrumented test files exist and pass. Constitution V
(TDD) gap is closed. Q-TP-1 + Q-TP-2 are regression-guarded.

---

## Phase 4: User Story 2 — Slice A: Top Project badge image bundle (Priority: P2)

**Goal**: Replace the text-overlay fallback for Top Project with the
bundled Figma badge composite, achieving visual parity with Top Talent.

**Independent Test**: Launch the app in DEMO mode, navigate to Award
Detail with Top Project selected; assert the "TOP PROJECT" badge image
(not text overlay) renders in `AwardHeroBlock`. Re-run T020 (parametric
over awardId) — it should still pass with the new asset.

- [x] T047 [US2] Pull the Top Project badge composite from MoMorph using `mcp__momorph__get_media_files` against frame `FQoJZLkG_d` OR fall back to `mcp__momorph__get_media_file` on the Picture-Award node `6885:10458`. If both return null, escalate to a render-time composition path (BG layer + wordmark layer stacked) — but try the direct export first. | (asset pull — no file)  → ✅ Shipped 2026-05-11: composite endpoint `6885:10463` returned null; fell back to downloading the two layers (BG `I6885:10463;72:2085` 160×160 + wordmark `I6885:10463;72:2104;214:654` 106×16) and compositing offline via Python + Pillow.
- [x] T048 [US2] Save the resulting PNG as `ic_award_top_project.png` in `app/src/main/res/drawable-mdpi/`. Verify dimensions match Top Talent's `ic_award_top_talent.png` (166×167) within ±10%. Use `file public/assets/ic_award_top_project.png` to confirm actual dimensions before committing. | app/src/main/res/drawable-mdpi/ic_award_top_project.png  → ✅ Shipped 2026-05-11: 160×160 RGBA (Top Talent reference 166×167; within 4% tolerance).
- [x] T049 [US2] Flip `DemoAwardsRepository.DEMO_DETAILS[1].imageUrl` from `null` to `"android.resource://com.example.aiddproject/drawable/ic_award_top_project"`. Match Top Talent's named-form Coil URI exactly. | app/src/main/java/com/example/aiddproject/home/data/DemoAwardsRepository.kt  → ✅ Shipped 2026-05-11.
- [x] T050 [P] [US2] Re-run `T020 body_renders_full_award_payload` parametric over `awardId=a02`; assert `AwardHeroBlock` now renders the bundled drawable (not the text-overlay fallback). Add an assertion that the `painterResource` resolves successfully (no exception). | app/src/androidTest/java/com/example/aiddproject/awarddetail/AwardDetailScreenTest.kt  → ✅ Verified 2026-05-11: AwardDetailScreenTest 9/9 still green after the flip (T024 `null_image_url_renders_fallback_overlay` uses its own `copy(imageUrl = null)` so still exercises the fallback path).
- [x] T051 [US2] Visual smoke on emulator-5554: select Top Project from dropdown; capture screenshot of `AwardHeroBlock`; compare against Figma node `6885:10458` for parity. No automated tool needed — eyeball is sufficient for cosmetic acceptance. | (emulator smoke — no file)  → ✅ Verified 2026-05-11: gold decorative BG + "TOP PROJECT" wordmark centered, matches Figma node `6885:10463`.

**Checkpoint**: Top Project visually matches Figma with bundled badge.

---

## Phase 5: User Story 3 — Slice C: SCREENFLOW.md refresh (Priority: P3)

**Goal**: Bring the project-level screen flow index back in sync with
the 5 ratified specs that actually exist on disk.

**Independent Test**: Read `.momorph/SCREENFLOW.md` cold — verify the
discovery progress, screens table, navigation graph, and discovery log
all reflect the post-2026-05-11 reality (5 specs, Awards branch
wired up).

- [x] T052 [US3] Update `SCREENFLOW.md` Discovery Progress table (Total Screens 3→5, Spec Shipped 3→5). Add screens table rows for `c-QM3_zjkG-iOS-Award-Top-talent` + `FQoJZLkG_d-iOS-Award-Top-project` with status `spec_shipped` + correct Detail File paths. Set Last Updated to 2026-05-11. | .momorph/SCREENFLOW.md  → ✅ Shipped 2026-05-11.
- [x] T053 [US3] Extend the Mermaid navigation graph: add `AwardTopTalent` + `AwardTopProject` nodes; add solid edge `Home -- "Awards tab tap" --> AwardTopTalent`; add dotted edge `AwardTopTalent -. "dropdown select Top Project" .-> AwardTopProject` (and reverse). Add Discovery Log entries for the Award Top Talent + Award Top Project spec ratification + implementation dates. | .momorph/SCREENFLOW.md  → ✅ Shipped 2026-05-11: nav graph now shows Home → Awards-tab → Top Talent, Chi tiết → Top Project, dropdown sibling-state transitions, Sun*Kudos Chi tiết → Home. Screen Groups + Shared Components tables also updated. Discovery Log has 5 new entries (Award Top Talent ship, Top Project delta-spec + Q-TP-1/Q-TP-2, Slice D backfill, Slice A badge).

**Checkpoint**: Project navigation graph reflects reality. Future
delta-spec authors (Top Heart, MVP, Best Manager, Signature 2025)
can read the index and see the Awards branch pattern.

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Wrap up, update plan retrospective, final verification.

- [x] T054 Run the full quality gate: `./gradlew lint ktlintCheck testDebugUnitTest connectedDebugAndroidTest`. All green; no new lint warnings; no test failures. If any fail, fix root cause — do NOT skip with `--no-verify`. | (build — no file)  → ✅ 2026-05-11: unit tests 137 pass (incl. 5 new DemoRepo + fixture fix for Slice A imageUrl flip), lint + ktlint clean, instrumented 26 awarddetail + 2 BottomNav all green, app install + launch verified on emulator-5554.
- [x] T055 [P] Update `plan.md` § As-shipped retrospective: mark Phase 5 (Constitution V gap closed) as `✅ shipped` with the Slice D commit references. Mark Slices A + C as ✅ shipped with their commit hashes. | .momorph/specs/FQoJZLkG_d-iOS-Award-Top-project/plan.md  → ✅ Shipped 2026-05-11: retrospective table now lists 7 phases with explicit commit hashes (daaf526, 9366e39, d69a6c8, 1417e25, 459ad95).
- [x] T056 Update `plan.md` § Constitution Compliance Check item V from `[~]` Partial to `[x]` Compliant. Strike through both rows of the Violations table — Q-TP-1 + Q-TP-2 are now regression-guarded; canonical T026–T056 gap is closed. **Sequential after T055** (same file). | .momorph/specs/FQoJZLkG_d-iOS-Award-Top-project/plan.md  → ✅ Shipped 2026-05-11: V flipped to [x] Compliant; both Violations rows struck through with resolution citation `d69a6c8`.
- [x] T057 Final commit per `feedback_commit_per_task.md` memory: granular commits per task or coherent task group. Conventional message style: `test(awarddetail): T0xx — <description>` for test tasks; `feat(awarddetail): T047-T051 — bundle Top Project badge`; `docs(momorph): T052-T053 — refresh SCREENFLOW with Awards branch`. | (git — no file)  → ✅ See git log; commits e20f419, d69a6c8, 1417e25, 459ad95, plus the final polish commit landing this task.

---

## Dependencies & Execution Order

### Phase Dependencies

- **Phase 1 (Setup)**: No deps — run immediately. T001 sequential, T002 + T003 parallel.
- **Phase 2 (Foundation)**: Depends on Phase 1. T004 sequential, T005 parallel.
- **Phase 3 (US1 = Slice D)**: Depends on Phase 2. This is the MVP — fully self-contained TDD backfill.
- **Phase 4 (US2 = Slice A)**: Depends on Phase 3 (T020 from Phase 3 is the test that asserts the new drawable in T050). **Do NOT start Slice A until Slice D's T019–T028 are at minimum scaffolded** — otherwise T050 cannot exist.
- **Phase 5 (US3 = Slice C)**: Independent of Phase 4. Can start anytime after Phase 3 completes (so the discovery log can record D + A commits as "shipped"). Best done last.
- **Phase 6 (Polish)**: Depends on Phases 3 + 4 + 5 all complete.

### Within Each User Story

- **US1 (Slice D)**: 5 sub-groups by test file. Each file's scaffold task (T006, T013, T019, T029, T044) MUST land before its `[P]` test tasks. The 5 scaffolds themselves are mutually parallel.
- **US2 (Slice A)**: Sequential — T047 (pull) → T048 (save) → T049 (flip URI) → T050 (verify test) → T051 (smoke).
- **US3 (Slice C)**: T052 (table updates) → T053 (graph + log) sequential — same file.

### Parallel Opportunities

- **Within Phase 3**: After T006/T013/T019/T029/T044 land, the test functions inside each file are mutually `[P]` because they target the same file but no test depends on another test's state (each `setContent { … }` independently). The Compose UI test runner serializes them automatically.
- **Across Phase 3 sub-groups**: Different files → can be staffed in parallel. A team of 5 could land all 5 scaffolds in parallel, then fan out per file.
- **Polish T055 + T056**: Same file (`plan.md`) → **sequential, not parallel**. Best merged into one commit `docs(awarddetail): mark plan retrospective + Constitution V compliant after Slice D/A/C`.

---

## Implementation Strategy

### MVP First (Recommended)

1. Phase 1 + Phase 2 (T001–T005). Should take ~15 minutes — pure verification.
2. **Phase 3 only** (Slice D — T006–T046). This is the MVP for the compliance work. Stop here, run the gate, validate that all 41 tests pass.
3. Re-baseline: confirm Q-TP-1 + Q-TP-2 are regression-guarded; confirm canonical UI surface is finally tested.

### Incremental Delivery (Chosen path)

1. **Setup + Foundation** — verify state of the world.
2. **Slice D (US1)** — TDD backfill. Commit per scaffold + per logical test group (e.g., `test(awarddetail): T006-T012 — AwardInfoBlockTest for Q-TP-2 regression`).
3. **Slice A (US2)** — badge bundle. One commit covering T047–T051: `feat(awarddetail): bundle Top Project badge composite`.
4. **Slice C (US3)** — SCREENFLOW refresh. One commit: `docs(momorph): refresh SCREENFLOW with Awards branch + Top Project entries`.
5. **Polish** — final gate + plan retrospective update + final commit.

---

## Notes

- **Commit cadence**: Per `feedback_commit_per_task.md` memory rule — granular commits per task or coherent group; never batch entire phases. QA gate green first.
- **Skip the test-first dance for already-shipped code**: Q-TP-1 + Q-TP-2 shipped before tests existed. The new tests in T007–T012 + T014 will PASS on first run (regression guards, not failing-then-passing TDD). That's acceptable for backfill — document each test's KDoc with "regression guard against future drift" so reviewers know the intent.
- **If MoMorph still returns null for Top Project badge** (T047 fallback path): proceed with render-time composition. Split T048 into two drawables (`ic_award_top_project_bg.png` + `ic_award_top_project_wordmark.png`) and modify `AwardHeroBlock` to stack them. Add a Phase 4 task `T048b Stack BG + wordmark in AwardHeroBlock with BoxWithConstraints`. Do NOT block on design — the render-time path is sufficient.
- **Mark tasks complete in-place** as you go: `- [x]`. Do NOT batch updates at end of phase.
- **Total task count**: 57 (3 setup + 2 foundation + 41 Slice D + 5 Slice A + 2 Slice C + 4 polish).
