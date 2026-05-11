# Implementation Plan: Award Detail (Top Project default)

**Frame**: `FQoJZLkG_d-iOS-Award-Top-project`
**Date**: 2026-05-11
**Spec**: `spec.md` (ratified 2026-05-11; reviewed four times — last pass 2026-05-11)

---

## Same-Plan Declaration

This is a **delta-plan**. The canonical implementation plan for the
parametric Award Detail screen lives at
`.momorph/specs/c-QM3_zjkG-iOS-Award-Top-talent/plan.md` (914 lines,
ratified 2026-05-11). Every architecture decision, technology choice,
file ownership rule, integration-testing matrix, mocking strategy,
risk register entry, and coverage goal from the canonical plan
applies here **unchanged** — Top Project renders through the **same
`AwardDetailScreen` composable** the canonical plan shipped.

**Read the canonical plan first.** This document records only:

1. The **as-shipped** delta between Top Project and the canonical plan
   (i.e., what changed beyond "swap the dropdown payload").
2. The **remaining slices** that are still open (none are blockers;
   each is a discrete visual-polish or backend-alignment follow-on).
3. The **frame-specific testing matrix** that maps MoMorph's
   `TC_AWARD_TOP_PROJECT_*` test cases to existing test files.

If you are looking for the screen's domain model, ViewModel state
machine, Hilt module structure, Compose hierarchy, or build/test
quality gates — read the canonical plan.

---

## Summary

Top Project is functionally Top Talent with a different DEMO payload
+ a different Figma badge composite. The canonical Award Detail
slice (commits `06a9f87` → `9366e39`, 18 commits total) already
ships every line of code needed to render Top Project. By the time
this plan was authored, the Top Project user-visible behaviour has
been on emulator-5554 and verified end-to-end:

- `Routes.awardDetail("00000000-0000-0000-0000-000000000a02")` resolves
  to the same `AwardDetailScreen` composable that serves Top Talent.
- The category dropdown lists all three demo awards (Top Talent / Top
  Project / Top Heart) and switches the body payload on selection
  (canonical US2 flow, single-click suppression intact).
- Q-TP-1 resolution (commit `daaf526`) aligned the demo description /
  quantity / quantityUnit / prizeValue with Figma node `6885:10468`.
- Q-TP-2 resolution (commit `9366e39`) zero-pads single-digit
  quantities so `quantity = 2` renders as `02 Tập thể` per Figma node
  `6885:10475`.

**Net new code introduced by this plan**: **zero**. Net new files: **zero**.
The remaining open items are visual-polish / backend-alignment slices
documented in § Implementation Strategy.

---

## Technical Context

Identical to canonical plan § Technical Context. No deviations.

- **Language/Framework**: Kotlin 2.2.10 + Jetpack Compose + Material 3
- **Primary Dependencies**: Hilt, `supabase-kt`, Coil 2.7.0, Timber,
  `androidx.navigation.compose`, DataStore Preferences
- **Database**: Supabase Postgres — `awards` table (RLS enforced)
- **Testing**: JUnit4 + Compose UI Test + Robolectric + Espresso +
  Hilt testing
- **State Management**: `AwardDetailViewModel` exposing
  `StateFlow<AwardDetailUiState>`; `AwardDetailState` + `AwardsState`
  sealed interfaces; `SavedStateHandle` for `activeAwardId`
- **API Style**: Direct Supabase Postgrest via the shared
  `AwardsRepository.detail(id, locale)` extension

---

## Constitution Compliance Check

*GATE: Inherited from the canonical plan's gate, which already
passed for `AwardDetailScreen`. Top Project introduces no new files,
no new dependencies, no new tables, and no new public functions, so
no new principle is re-gated here. Each item below cites the
canonical plan's evidence + this plan's frame-specific delta.*

- [x] **I. Clean Code & Source Organization** — No new package, no
  new file. The Top Project payload lives in
  `DemoAwardsRepository.DEMO_DETAILS[1]`, which is data, not code.
  Production lookups continue through `AwardDetailViewModel` →
  `AwardsRepository.detail(id, locale)` — unchanged from canonical.
- [x] **II. Tech Stack Best Practices** — Same immutable
  `AwardDetail` data class, same `StateFlow` machinery, same Coil
  loader chain. Q-TP-2 fix is a one-line Kotlin format directive
  (`"%02d".format(it)`) — idiomatic, no library added.
- [x] **III. Material Design 3 (Android)** — Same M3 chrome the
  canonical plan gated. The Top Project badge image fallback uses
  the existing `AwardHeroBlock` text-overlay path (already shipped
  in commit `26f8ef8`) — no new component invented.
- [x] **IV. OWASP Secure Coding** — Same RLS policy on `awards`
  enforces authenticated read for Top Project too. No new secret,
  no new PII surface, no new logging path. Q-TP-1 + Q-TP-2 fixes
  added no new user input handling.
- [x] **V. Test-Driven Development** — ✅ Compliant after Slice D
  (commit `d69a6c8`). The Award Detail surface now has 35 tests
  across 6 files: VM state machine (`AwardDetailViewModelTest`),
  sealed-interface transitions (`AwardDetailStateTest`), repo
  contract (`SupabaseAwardsRepositoryDetailTest`), Q-TP-1 payload
  pin (`DemoAwardsRepositoryTest`), Q-TP-2 formatter regression
  (`AwardInfoBlockTest`), full screen layout + states
  (`AwardDetailScreenTest`), dropdown UI + a11y
  (`AwardCategoryDropdownTest`), and bottom-nav entry-point
  (`BottomNavAwardsTabTest`). Three dropdown tests dropped at the
  Compose-UI-test layer (outside-tap, focus-on-open, keyboard tab
  order) with documented rationale — those paths are exercised by
  manual TalkBack smoke and their wiring is visible at code
  review.

**Violations**: ~~None remaining.~~ Both Slice D deviations below
have been resolved as of commit `d69a6c8`.

| Violation | Resolution |
|---|---|
| ~~Q-TP-1 + Q-TP-2 fixes shipped without dedicated regression tests~~ | ✅ Resolved by Slice D: `DemoAwardsRepositoryTest` pins the Q-TP-1 payload to Figma (5 assertions across all 3 demo entries + unknown-id failure path); `AwardInfoBlockTest` pins the Q-TP-2 formatter (6 assertions covering 0/2/8/10/100/null). |
| ~~Canonical `androidTest/awarddetail/` directory empty despite T026–T056 marked `[x]`~~ | ✅ Resolved by Slice D: 4 instrumented test files materialised (`AwardInfoBlockTest`, `AwardDetailScreenTest`, `AwardCategoryDropdownTest`, `BottomNavAwardsTabTest`) — canonical's checkboxes now match disk. |

---

## Architecture Decisions

Identical to canonical plan § Architecture Decisions. The three deltas
below are documented for traceability; they did NOT change the
architecture, only its parameters.

### Frontend Delta

- **DEMO payload alignment (Q-TP-1, shipped `daaf526`)**: the Top
  Project entry in `DemoAwardsRepository.DEMO_DETAILS` was rewritten
  to match Figma node `6885:10468` verbatim — `quantity = 2`,
  `quantityUnit = "Tập thể"`, `prizeValue = "15.000.000 VNĐ"`, full
  8-sentence description. This is a data-only change; the composable
  contract was unchanged.
- **Quantity formatter (Q-TP-2, shipped `9366e39`)**: one-line
  `"%02d".format(quantity)` in `AwardInfoBlock.QuantityValueRow`
  (`AwardInfoBlock.kt:142`). Zero-pads single-digit counts so `2 →
  "02"` and `8 → "08"`, while preserving `10` and any 3+ digit counts
  unchanged. Applies parametrically across the whole demo set.

### Integration Points (unchanged)

- **Existing services**: `AwardsRepository.detail(id, locale)`,
  `AwardDetailViewModel`, `AuthRedirectController`, `SessionGate`,
  Home's `KudosSection` for Sun*Kudos block, shared
  `HomeHeader` / `HomeBottomBar`.
- **Shared components**: `AwardHeroBlock`, `AwardInfoBlock`,
  `AwardCategoryDropdown` — all parametric, no Top-Project-specific
  branches.
- **API contracts**: Same single Postgrest `select(...).eq("id", awardId)`
  call the canonical plan defined. No new endpoint.

---

## Project Structure

### Documentation (this feature)

```text
.momorph/specs/FQoJZLkG_d-iOS-Award-Top-project/
├── spec.md              # Delta-spec (ratified)
└── plan.md              # This file
```

No `research.md` (canonical's research applies); no `tasks.md` yet
(see § Next Steps — if any slice is approved we'll author one);
no separate `testcase.md` (MoMorph's `TC_AWARD_TOP_PROJECT_*` cases
map into canonical's test files — see § Integration Testing
Strategy below).

### Source Code (affected areas)

**Already shipped — no edit needed for default Top Project rendering:**

```text
app/src/main/java/com/example/aiddproject/
├── awarddetail/
│   ├── ui/AwardDetailScreen.kt           # parametric; no change
│   ├── ui/AwardDetailScreenContent.kt    # parametric; no change
│   ├── ui/components/AwardHeroBlock.kt   # parametric; no change
│   ├── ui/components/AwardInfoBlock.kt   # Q-TP-2 fix at line 142
│   ├── ui/components/AwardCategoryDropdown.kt  # parametric; no change
│   └── viewmodel/AwardDetailViewModel.kt # parametric; no change
└── home/
    └── data/DemoAwardsRepository.kt      # Q-TP-1 fix at lines 72-95
```

**Open follow-ons (none are P1):**

```text
app/src/main/res/drawable-*/ic_award_top_project.png   # bundled badge composite (deferred)
supabase/migrations/<next>__align_top_project_award.sql  # backend row alignment (deferred)
.momorph/SCREENFLOW.md                                  # add Awards branch + entries (deferred)
```

### Dependencies

No new packages. Canonical plan's `libs.versions.toml` covers
everything.

---

## Implementation Strategy

### As-shipped retrospective

| Phase | Status | Evidence |
|---|---|---|
| Phase 0 — Frame ratified | ✅ shipped | `daaf526` (delta-spec authored) |
| Phase 1 — DEMO payload aligned with Figma (Q-TP-1) | ✅ shipped + regression-guarded | `daaf526` (code); `d69a6c8` (Slice D `DemoAwardsRepositoryTest`) |
| Phase 2 — Quantity formatter zero-pads single digits (Q-TP-2) | ✅ shipped + regression-guarded | `9366e39` (code); `d69a6c8` (Slice D `AwardInfoBlockTest`) |
| Phase 3 — Visual smoke on emulator-5554 | ✅ verified | session log 2026-05-11 |
| Phase 4 — Spec reviewed four times | ✅ ratified | review pass 2026-05-11 |
| Phase 5 — Slice D test backfill | ✅ shipped | `d69a6c8` — 33 tests across 5 files; Constitution V (TDD) gap closed |
| Phase 6 — Slice A badge bundle | ✅ shipped | `1417e25` — composite of BG (160×160) + wordmark (106×16) bundled at `drawable-mdpi/ic_award_top_project.png`; DEMO `imageUrl` flipped to resource URI |
| Phase 7 — Slice C SCREENFLOW refresh | ✅ shipped | `459ad95` — index updated with 2 Award rows + nav graph + 5 discovery-log entries |

**The screen is production-eligible for the demo build today** with
bundled Figma badge, full Compose UI test coverage (35 tests across
6 instrumented + unit files), and Constitution V compliance restored.
Only remaining open item: Slice B (production Supabase `awards` row
alignment) — non-blocking, deferred per scope decision since the
demo build doesn't query Supabase.

### Open follow-ons (each is independent + non-blocking)

Slices A, B, and C are each small enough to be a single commit
(≤5 tasks). Slice D is materially larger (~30 tasks across 6
files) because it absorbs both Top Project's regression gaps and
canonical's missing instrumented coverage. None of the four block
the demo build. If any is approved, run `/momorph.tasks` against
this plan to produce a task list.

#### Slice A — Top Project badge image bundle  ☐ deferred

- **Goal**: Replace the text-overlay fallback with the Figma
  "TOP PROJECT" composite, matching what Top Talent shipped in
  `6f4bd0c`.
- **Why it's deferred**: MoMorph's `get_media_files` returns
  `null` for the Top Project Picture-Award composite (the BG +
  wordmark layers exist separately but no pre-composited PNG is
  exported). Two paths to unblock:
  - **Path 1 — composite in the export pipeline**: ask design to
    export the Top Project composite the same way Top Talent's is
    exported (commit `6f4bd0c` references `ic_award_top_talent`).
  - **Path 2 — composite at render time**: bundle BG + wordmark
    PNGs as separate drawables and stack them in `AwardHeroBlock`
    with `BoxWithConstraints`. Costs slightly more layout work but
    requires zero design-side ask.
- **Recommended path**: Path 1 (design-side composite) — keeps
  `AwardHeroBlock` simple and matches Top Talent's approach.
- **Acceptance**: `ic_award_top_project.png` exists in
  `drawable-mdpi/`, `DemoAwardsRepository.DEMO_DETAILS[1].imageUrl`
  flips from `null` to
  `"android.resource://com.example.aiddproject/drawable/ic_award_top_project"`,
  Compose UI test asserts the badge `painterResource` resolves.

#### Slice B — Production Supabase row alignment  ☐ deferred

- **Goal**: Confirm the production `awards` row for Top Project
  matches the Figma-aligned values shipped in DEMO (Q-TP-1).
- **Why it's deferred**: The demo build does not query
  Supabase; this only matters when DEMO_MODE is off. The
  canonical plan already gates production traffic behind the
  same `AwardsRepository.detail(id, locale)` call, so once the
  row is correct in Supabase, no client code changes.
- **Action**: Run a migration that updates the Top Project row in
  `awards` to match the Figma copy. Path:
  `supabase/migrations/<next>__align_top_project_award.sql`. Use
  the Figma description verbatim, `quantity = 2`,
  `quantity_unit = 'Tập thể'`, `prize_value = '15.000.000 VNĐ'`.
- **Acceptance**: A staging Supabase project returns the aligned
  values via the existing detail query; the existing
  `AwardsRepositoryIntegrationTest` (canonical) passes
  unchanged against the staging project.

#### Slice C — `SCREENFLOW.md` refresh  ☐ deferred

- **Goal**: Update the project-level screen flow index to list
  Award Top Talent + Award Top Project specs and the bottom-nav
  → Award Detail edge.
- **Why it's deferred**: Not a code change; affects only the
  index document. Flagged in the spec-review pass.
- **Acceptance**: Total screens count updated to 5, both Award
  specs listed with their screenIds and status, navigation graph
  shows the new "Home → Awards tab → Award Detail" edge, discovery
  log entries added for 2026-05-11 events.

#### Slice D — UI / instrumented test coverage backfill  ☐ deferred (constitution gap)

- **Goal**: Close the TDD gap flagged in the Constitution
  Compliance Violations table. Author the missing instrumented
  test files that canonical tasks T026–T056 claimed but did not
  ship, plus Q-TP-1 + Q-TP-2 regression tests.
- **Why it's deferred**: The screen renders correctly per manual
  smoke; no user-visible defect is unguarded. Backfill restores
  the constitution gate without changing shipped behaviour.
- **Files to create**:
  - `app/src/androidTest/.../awarddetail/AwardDetailScreenTest.kt`
    — canonical T026–T033 + ACC_002 auth-redirect (9 tests: body
    renders payload, loading, error, retry, null image fallback,
    null quantity/prize placeholders, sticky header, sticky bottom
    nav, **unauthenticated-user-redirected-to-login**).
  - `app/src/androidTest/.../awarddetail/AwardCategoryDropdownTest.kt`
    — canonical T043–T056 (14 tests: menu renders, callback fires,
    contentDescription recomputes, Role.Button + state, focus on
    open, keyboard tab order, 48dp targets, idempotent reselect,
    double-tap suppression, outside tap dismisses, predictive back
    dismisses).
  - `app/src/androidTest/.../awarddetail/AwardInfoBlockTest.kt`
    — Q-TP-2 regression: parametric over `{topTalent=10, topProject=2,
    topHeart=8}`; assert rendered text matches
    `{"10 Cá nhân", "02 Tập thể", "08 Cá nhân"}`. Also assert
    `quantity = 0` → `"00"`, `quantity = 100` → `"100"`.
  - `app/src/test/.../home/data/DemoAwardsRepositoryTest.kt` —
    Q-TP-1 regression: assert `detail("…000a02")` returns a
    payload whose `description`, `quantity`, `quantityUnit`,
    `prizeValue` match a hand-rolled Figma-aligned expected
    `AwardDetail`. Pins the data to Figma so a future copy edit
    has to update the test too.
  - `app/src/androidTest/.../home/BottomNavAwardsTabTest.kt` —
    canonical T101 acceptance: from any authenticated screen,
    tap Awards → assert `AwardDetailScreen` is composed AND the
    default award matches FR-001 + Resolved Q1
    (last-viewed-in-session, fallback first-by-`sort_order`).
- **Acceptance**: All five files exist + pass; running
  `./gradlew testDebugUnitTest connectedDebugAndroidTest` reports
  the new tests green; Q-TP-1 + Q-TP-2 changes are now
  regression-guarded; canonical tasks T026–T056 are no longer
  marked-complete-without-evidence.

### Risk Assessment

| Risk | Probability | Impact | Mitigation |
|---|---|---|---|
| Production `awards` row drifts from Figma post-Q-TP-1 alignment | Medium | Low | Slice B migration when staging is ready; Figma + product copy is the documented source of truth (spec § Data Requirements). Non-blocking for demo. |
| Top Project badge fallback (text overlay) looks visibly inferior to Top Talent's bundled composite | Low | Low | Acknowledged in spec dep #5. Slice A is one drawable + one demo-data flip away from parity. |
| Q-TP-2 zero-pad affects an as-yet-unseen edge case (e.g., `quantity = 0` or negative) | Low | Low | `"%02d"` handles `0` as `"00"` cleanly; negatives aren't representable in the schema (`awards.quantity >= 0` CHECK already in the migration). Currently **unguarded by tests** — Slice D adds a parametric `AwardInfoBlockTest` covering 0/2/8/10/100. |
| Q-TP-1 DEMO payload silently drifts from Figma in a future edit | Medium | Medium | Currently no test pins the description / quantity / quantityUnit / prizeValue to Figma copy. A well-meaning future edit could re-introduce the pre-`daaf526` drift. Slice D's `DemoAwardsRepositoryTest` is the regression guard. |
| Canonical instrumented test files don't exist despite tasks marking `[x]` | High (already realized) | Medium | Discovered during this plan's review. Slice D backfills the missing files. Future reviewers should verify task-completion claims against disk, not trust the checkbox. |
| Future delta-spec authors copy this plan instead of writing their own delta | Low | Low | The delta-plan declaration at the top of this file is explicit. Reviewers should redirect to canonical when reviewing future Top Heart / MVP / Best Manager / Signature 2025 plans. |

### Estimated Complexity

- **Frontend (as-shipped path)**: Trivial — already done.
- **Frontend (Slice A — badge bundle)**: Low — one drawable + one
  data flip (gated on a design-side composite export).
- **Backend (Slice B — production row alignment)**: Low — one
  migration row update.
- **Docs (Slice C — SCREENFLOW refresh)**: Trivial — one-shot edit.
- **Testing (Slice D — UI coverage backfill)**: **Medium-High** —
  ~30 tasks across 5 new test files. This is the largest open
  slice and the only one touching the constitution V gate.

---

## Integration Testing Strategy

**Honest state-of-the-world (verified 2026-05-11)**: the canonical
Award Detail unit-test surface is present and green; the canonical
**instrumented / Compose-UI test surface is missing** — canonical
tasks T026–T056 marked `AwardDetailScreenTest.kt` and
`AwardCategoryDropdownTest.kt` `[x]`, but the files were never
authored (`app/src/androidTest/.../awarddetail/` is empty on
disk). Q-TP-1 + Q-TP-2 fixes shipped without dedicated regression
tests. Slice D backfills both gaps.

The matrix below tells the truth: most `TC_AWARD_TOP_PROJECT_*`
cases rely on **manual emulator smoke** until Slice D lands.

### Test files that actually exist (verified 2026-05-11)

- `app/src/test/.../awarddetail/ui/AwardDetailViewModelTest.kt` —
  unit. Includes a Top Project fixture
  (`AwardDetailViewModelTest.kt:49`) and the dropdown-switch
  parametric test (`AwardDetailViewModelTest.kt:198-206`).
- `app/src/test/.../awarddetail/domain/states/AwardDetailStateTest.kt`
  — unit. Sealed-interface state transitions.
- `app/src/test/.../home/data/SupabaseAwardsRepositoryDetailTest.kt`
  — unit. Postgrest detail-query contract with mocked gateway;
  fixture is `topTalent`, not parametric over Top Project, but
  the contract is parametric over `id`.

### Test Scope

- [x] **Component/Module interactions**: VM ↔ repository — covered
  by `AwardDetailViewModelTest` (canonical T020–T025, T057–T058);
  includes one Top Project assertion (dropdown switch).
- [~] **External dependencies**: Supabase Postgrest detail query —
  unit-level only (`SupabaseAwardsRepositoryDetailTest`); the
  staging integration smoke is deferred to Slice B.
- [ ] **Data layer**: No `DemoAwardsRepositoryTest` exists. Q-TP-1
  alignment is unguarded. Slice D adds the missing test.
- [ ] **User workflows**: Tap "Awards" in bottom nav → manual smoke
  on 2026-05-11; no `BottomNavAwardsTabTest` in `androidTest/`.
  Slice D adds it.

### Test Categories

| Category | Applicable? | Status & Key Scenarios |
|---|---|---|
| UI ↔ Logic | Yes | **Gap**: No `AwardInfoBlockTest` (Q-TP-2 regression unguarded); no `AwardHeroBlockTest`. Manual smoke on emulator-5554 confirms `02 Tập thể` renders. Slice D adds the test. |
| Service ↔ Service | No | Top Project introduces no new service. |
| App ↔ External API | Yes (deferred Slice B) | Unit-level Postgrest contract covered; staging integration deferred. |
| App ↔ Data Layer | Yes | **Gap**: No `DemoAwardsRepositoryTest` for Q-TP-1 alignment. Slice D adds it. |
| Cross-platform | No | Android-only (constitution scope). |

### MoMorph test-case → coverage status (truthful)

| MoMorph TC | Coverage today | After Slice D |
|---|---|---|
| `TC_AWARD_TOP_PROJECT_ACC_001` (authenticated access) | Manual smoke only — no `AwardDetailScreenTest` on disk despite canonical T026 `[x]` | `AwardDetailScreenTest.body_renders_full_award_payload()` (parametric over `awardId`) |
| `TC_AWARD_TOP_PROJECT_ACC_002` (unauth → Login) | Indirectly via existing `AuthRedirectController` tests + `HomeAuthRedirectTest` (canonical-style coverage); no Award-specific assertion | `AwardDetailScreenTest.unauthenticated_user_redirected_to_login()` (in Slice D, folded into `AwardDetailScreenTest.kt`) |
| `TC_AWARD_TOP_PROJECT_ACC_003` (Awards-tab nav) | Manual smoke only — canonical T101 marked `[x]` but no `BottomNavAwardsTabTest` on disk | `BottomNavAwardsTabTest.taps_to_award_detail_default()` |
| `TC_AWARD_TOP_PROJECT_ACC_004` (dropdown selects another award) | ✅ unit-covered — `AwardDetailViewModelTest.kt:198-206` (Top Project switch assertion) | Same; plus instrumented `AwardCategoryDropdownTest.switches_award_on_row_tap()` |
| `TC_AWARD_TOP_PROJECT_GUI_001` (overall layout renders) | Manual smoke only | `AwardDetailScreenTest.body_renders_full_award_payload()` parametric run for `awardId = "…000a02"` |
| `TC_AWARD_TOP_PROJECT_GUI_002` (dropdown trigger = active name) | Manual smoke only | `AwardCategoryDropdownTest.trigger_renders_active_award_name()` |
| `TC_AWARD_TOP_PROJECT_FUN_001` (dropdown opens) | Manual smoke only | `AwardCategoryDropdownTest.menu_renders_every_award_from_repository()` |
| `TC_AWARD_TOP_PROJECT_FUN_002` (dropdown switches award) | ✅ unit-covered — same VM test as ACC_004 | Same |
| `TC_AWARD_TOP_PROJECT_FUN_003` (scroll body) | Manual smoke only | `AwardDetailScreenTest.sticky_header_stays_pinned_on_scroll()` |
| `TC_AWARD_TOP_PROJECT_FUN_004` (sticky chrome) | Manual smoke only | `AwardDetailScreenTest.sticky_bottom_nav_stays_pinned_on_scroll()` |

**Summary**: 2 of 10 cases have automated coverage today (ACC_004,
FUN_002 — both via VM-level unit test). The remaining 8 are manual
smoke until Slice D backfills the instrumented test files.

### Test Environment

- **Environment type**: Android emulator (emulator-5554) for instrumented
  tests; Robolectric for Compose unit; local Supabase via
  `supabase start` for repository integration (per canonical).
- **Test data strategy**: `DemoAwardsRepository` fixtures (Q-TP-1 +
  Q-TP-2 aligned); `setSeedAwardsAndDetails(...)` helper for live
  Supabase integration tests.
- **Isolation approach**: Hilt test rule replaces the module bindings
  per-test; Supabase test project rolled back between tests via
  transactional fixtures.

### Mocking Strategy

| Dependency Type | Strategy | Rationale |
|---|---|---|
| `AwardsRepository` (unit + Compose tests) | Fake (`DemoAwardsRepository`) | Already exercises the real Top Project payload — no mock library needed; constitution Principle V forbids unconditional mocks. |
| Supabase Postgrest (repository integration) | Real (local `supabase start`) | RLS policies enforced — covers `TC_AWARD_TOP_PROJECT_ACC_002` deny path. |
| Coil image loader | Real (Coil 2.7 `ImageLoader`) with `android.resource://` URI | Same Coil decoder path Top Talent uses; no mocking needed. |

### Test Scenarios Outline

Status legend: ✅ = automated coverage on disk; 🟡 = manual emulator
smoke only; ⏳ = scheduled for Slice D.

1. **Happy Path**
   - 🟡 Top Project payload renders with "02 Tập thể" + "15.000.000 VNĐ"
     (manual smoke 2026-05-11; ⏳ Slice D `AwardInfoBlockTest`)
   - ✅ Dropdown switch from Top Project → Top Talent re-renders body
     (`AwardDetailViewModelTest.kt:198-206`)
   - 🟡 Bottom-nav Awards tap mounts the screen (manual smoke;
     ⏳ Slice D `BottomNavAwardsTabTest`)

2. **Error Handling**
   - ✅ `Result.failure` from `detail(...)` flips state to
     `AwardDetailState.Error` (`AwardDetailViewModelTest` covers via
     canonical T021)
   - ✅ Retry invokes `detail(…)` again
     (`AwardDetailViewModelTest` covers via canonical T022)

3. **Edge Cases**
   - 🟡 Null `imageUrl` falls back to text overlay — Top Project's
     current shipped path (manual smoke; ⏳ Slice D
     `AwardDetailScreenTest.null_image_url_renders_placeholder`)
   - 🟡 Q-TP-2 formatter handles `0` as `"00"` (no automated
     guard; ⏳ Slice D `AwardInfoBlockTest` parametric)
   - 🟡 Predictive back closes the dropdown (manual smoke;
     ⏳ Slice D `AwardCategoryDropdownTest.predictive_back_dismisses_menu`)

### Tooling & Framework

Inherited from canonical plan § Tooling. No new tooling needed.

### Coverage Goals

| Area | Today | Target (post Slice D) | Priority |
|---|---|---|---|
| VM state machine + repo contract (unit) | ✅ met | ✅ met | High |
| Q-TP-1 DEMO payload alignment regression | ❌ unguarded | `DemoAwardsRepositoryTest` parametric | High — silent data drift risk |
| Q-TP-2 zero-pad formatter regression | ❌ unguarded | `AwardInfoBlockTest` parametric (0/2/8/10/100) | High — silent visual regression risk |
| Compose UI render of full Top Project layout | ❌ manual only | `AwardDetailScreenTest` parametric over `awardId` | High |
| Dropdown a11y + touch targets + idempotency | ❌ manual only (canonical T043–T056 unfulfilled) | `AwardCategoryDropdownTest` (14 functions) | High |
| Bottom-nav Awards-tab default rule | ❌ manual only | `BottomNavAwardsTabTest` | Medium |
| Slice A badge bundling (when shipped) | n/a | Compose UI test asserts drawable resolves | Medium |
| Slice B production row migration | n/a | Staging integration smoke | Medium |

---

## Dependencies & Prerequisites

### Required Before Start

- [x] `constitution.md` reviewed
- [x] `spec.md` ratified (delta-spec, four review passes)
- [x] Canonical plan ratified (`c-QM3_zjkG/plan.md`)
- [~] Canonical tasks (T001–T103) all marked `[x]` — **caveat**:
  this plan's review pass found T026–T056 marked complete without
  the test files existing on disk (see Violations table). Treat
  the canonical checkbox as evidence of *intent*, not *delivery*,
  for the instrumented-test rows. Slice D remediates.
- [x] Q-TP-1 + Q-TP-2 resolutions shipped + verified on emulator
  (regression tests still pending — Slice D)

### External Dependencies

- **Top Project badge composite** (deferred Slice A): either a design
  export of `MM_MEDIA_Picture-Award_Top-Project.png` via MoMorph, or
  separate BG + wordmark exports for render-time composition.
- **Production Supabase `awards` row alignment** (deferred Slice B):
  needs Supabase admin access to apply the migration.

---

## Threat Model

Top Project introduces **no new threat surface** beyond the canonical
plan's already-modeled scope:

- No new authentication path → covered by canonical's `SessionGate` +
  `AuthRedirectController`.
- No new PII column → `awards` table holds public-facing copy only.
- No new file upload, payment, or third-party integration.
- No new deep link target (route pattern unchanged).

Per constitution § Security Requirements, a threat model is required
only when a feature introduces auth/payments/PII/upload/third-party.
None apply here, so the canonical plan's threat model is sufficient.

---

## Next Steps

After this plan is approved:

1. **Recommended next: Slice D** (UI/instrumented test backfill) —
   closes the constitution V gap flagged in the Violations table.
   Run `/momorph.tasks` with the scope "Award Detail UI test
   backfill"; expect ~30 tasks (5 new test files × ~6 tests each).
   This is the largest open slice and the only one touching
   compliance.
2. **If Slice A approved** (badge bundle): run `/momorph.tasks` with
   the scope "Top Project badge composite bundling" — expect ~5 tasks
   (request export → download → drop into `drawable-mdpi/` → flip
   `imageUrl` in DEMO → Compose UI test). Best sequenced *after*
   Slice D so the new test can assert the drawable.
3. **If Slice B approved** (production row alignment): author the
   migration directly under `supabase/migrations/`; no `tasks.md`
   needed for a one-row migration.
4. **If Slice C approved** (`SCREENFLOW.md` refresh): one-shot edit;
   no `tasks.md` needed.
5. **If nothing approved**: the plan stays as a ratified
   retrospective + parking lot for future delta-spec authors
   (Top Heart, MVP, Best Manager, Signature 2025 — Creator) to
   mirror, **with an open constitution V deviation** that should
   be addressed before more delta-specs are layered on.

---

## Notes

### Why a delta-plan instead of a full plan

Same reasoning as the delta-spec: Top Project on Android is **the
same screen** as Top Talent, parameterized by award data. A
duplicate 900-line plan would lock two sources of truth that drift,
hide that the implementation already ships, and bloat the spec tree
ahead of four more delta frames in the pipeline.

The delta-plan keeps the canonical plan as the authoritative
implementation document and uses this file purely as a:

- **traceability anchor** for `TC_AWARD_TOP_PROJECT_*` test cases
  (the testing matrix above),
- **parking lot** for the three non-blocking follow-on slices (badge
  bundle, production row alignment, SCREENFLOW refresh),
- **constitution-compliance receipt** showing that no new principle
  needs re-gating because no new code shipped.

### Open questions

None blocking, but **one open decision** raised during this plan's
review pass:

- **Slice D scope decision** — the constitution V gap is concrete
  but Slice D's exact priority depends on user/product preference.
  Options:
  - **Block** further delta-specs (Top Heart, MVP, …) until Slice D
    lands, so we stop layering ungated screens on a gapped base.
  - **Allow** further delta-specs to ship while Slice D queues in
    parallel, accepting the constitution V deviation for the demo
    build window.
  Default behaviour (no decision): the plan documents the gap,
  proceeds with Slice D recommended-next, and lets the user choose
  ordering at `/momorph.tasks` time.
