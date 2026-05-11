# Feature Specification: Award Detail — Top Project default

**Frame ID**: `FQoJZLkG_d`
**Frame Name**: `[iOS] Award_Top project`
**File Key**: `9ypp4enmFmdK3YAFJLIu6C`
**Created**: 2026-05-11
**Status**: Ratified (delta-only spec — see § Same-Screen Declaration)

---

## Same-Screen Declaration

This frame renders the **same parametric Award Detail screen** as
`c-QM3_zjkG` (`[iOS] Award_Top talent`). MoMorph publishes one frame
per default-rendered award, but on Android they are a single
`AwardDetailScreen` composable that switches between awards via the
category dropdown (US2 in the canonical spec).

**Canonical spec**: `.momorph/specs/c-QM3_zjkG-iOS-Award-Top-talent/spec.md`
(ratified 2026-05-08, reviewed twice 2026-05-11).

**Read that first.** Every functional requirement, technical
requirement, success criterion, edge case, state-management
contract, and API dependency from the canonical spec applies here
**unchanged**. This document records only:

1. What is **different** about this frame (default award data).
2. How the existing implementation already covers it (no new code).
3. The frame-specific test cases.

If you're looking for the screen's behaviour, navigation flow,
auth gate, dropdown contract, sticky chrome contract, or anything
else generic — read the canonical spec.

---

## Overview

The Award Detail screen rendered with **Top Project** as the default
selection (vs Top Talent in `c-QM3_zjkG`). The user reaches it by:

- Tapping **Chi tiết** on the Top Project card in Home's awards
  carousel — the route is `Routes.awardDetail("00000000-0000-0000-0000-000000000a02")`.
- Selecting "Top Project" from the category dropdown while already on
  Award Detail.

**Target users**: same as canonical — authenticated Sunners.

**Implementation platform**: Android only (Kotlin + Compose + M3 +
Supabase). **Single composable**: `AwardDetailScreen` at
`com.example.aiddproject.awarddetail.ui.AwardDetailScreen` — no new
file, no new ViewModel.

---

## What's different from the canonical spec

| Field | Top Talent (`c-QM3_zjkG`) | **Top Project (this frame)** |
|-------|---------------------------|-------------------------------|
| `award.id` (UUID) | `…000000a01` | `…000000a02` |
| `award.name` (carousel) | "Top Talent Award" | "Top Project Award" |
| `award.name` (detail body + dropdown) | "Top Talent" | **"Top Project"** |
| Badge image (Figma) | "TOP TALENT" lockup | **"TOP PROJECT" lockup** |
| Description | "Vinh danh cá nhân xuất sắc…" | **"Vinh danh các tập thể dự án xuất sắc với kết quả kinh doanh vượt kỳ vọng…"** |
| `award.quantity` | `10` | **`2`** |
| `award.quantity_unit` | "Cá nhân" | **"Tập thể"** |
| `award.prize_value` | "7.000.000 VNĐ" | **"15.000.000 VNĐ"** |
| `sort_order` | `1` | `2` |

**That is the entire diff.** Everything else — sticky chrome, dropdown
behaviour, Sun\*Kudos Chi tiết destination, auth gate, header chrome,
bottom-nav routing, locale switch, error/retry/null-placeholder
handling, telemetry — is **identical** and already shipped per
`c-QM3_zjkG`'s tasks T001–T103.

---

## User Scenarios

All user stories (US1–US8) from the canonical spec apply unchanged.
The only frame-specific scenarios are mirror cases that exercise the
**Top Project** payload instead of Top Talent. They are listed below
for traceability against MoMorph's `TC_AWARD_TOP_PROJECT_*` test
case IDs.

### Frame-specific acceptance scenarios

These are **parametric repeats** of the canonical US1–US3 scenarios
swapped to Top Project data. No new behaviour.

1. **Mount with Top Project default (TC_AWARD_TOP_PROJECT_GUI_001)**
   - **Given** the user navigates to this frame via the
     `Routes.awardDetail("…000a02")` route OR selects "Top Project"
     from the category dropdown,
   - **When** the body resolves to `AwardDetailState.Loaded`,
   - **Then** the dropdown trigger reads "Top Project", the badge
     image renders the "TOP PROJECT" lockup, the description matches
     the table above, the quantity row reads "02 Tập thể", and the
     prize row reads "15.000.000 VNĐ cho mỗi giải thưởng".

2. **Dropdown default state (TC_AWARD_TOP_PROJECT_GUI_002)**
   - **Given** the screen is rendered with Top Project as the active
     award,
   - **When** the user observes the Highlight block,
   - **Then** the dropdown trigger pill renders "Top Project" + the
     chevron — matching the design pill chrome from canonical
     US2 + FR-005.

3. **Bottom-nav Awards tab navigation
   (TC_AWARD_TOP_PROJECT_ACC_003)**
   - **Given** the user is on any authenticated screen,
   - **When** the user taps **Awards** in the bottom navigation bar,
   - **Then** Award Detail mounts. The default award is determined
     per canonical FR-001 + Resolved Q1 — last-viewed in-session,
     fallback to first-by-`sort_order`. **Top Project becomes the
     default only if it was last viewed; otherwise Top Talent
     remains the default** (first by sort_order). The frame's
     existence does NOT change the default-selection rules.

4. **Switch to a different award (TC_AWARD_TOP_PROJECT_FUN_002)**
   - **Given** the screen is on Top Project,
   - **When** the user selects "Top Talent" from the dropdown,
   - **Then** the body re-renders with Top Talent's payload — this
     is the same flow as canonical US2 acceptance scenario 2.

All other scenarios (loading, error, retry, null-image fallback,
predictive-back, sticky chrome on scroll, etc.) inherit from the
canonical spec unchanged.

---

## Component Behavior

Identical to the canonical spec's § Screen Components table.
Frame-specific Node IDs (Figma reassigns IDs per frame, so the
canonical spec's `6885:1029x` IDs become this frame's `6885:1045x`
range), but they map 1:1:

| Component | Top Talent node | **Top Project node** |
|-----------|----------------|----------------------|
| Header (`mms_1_header`) | `6885:10264` | (rendered from the same `HomeHeader` shared composable; per-frame ID `6885:10435` for the Figma frame instance) |
| KV Kudos banner | `6885:10266` | **`6885:10436`** |
| Highlight block | `6885:10283` | **`6885:10453`** |
| Highlight header (sub-label + title + dropdown) | `6885:10284` | **`6885:10454`** |
| Award info — title row text | `6885:10297` | **`6885:10467`** |
| Award info — description text | `6885:10298` | **`6885:10468`** |
| Award info — quantity label | `6885:10303` | **`6885:10473`** |
| Award info — quantity value | `6885:10305` | **`6885:10475`** |
| Award info — prize label | `6885:10306` | **`6885:10476`** |
| Award info — prize value | `6885:10311` | **`6885:10481`** |
| Award badge image | `6885:10313` | **`6885:10483`** |
| Sun\*Kudos block | `6885:10315` | (same `mms_2.4_kudos` instance; per-frame ID for this frame is the parent's child) |
| Bottom nav | `6885:10332` | (same `HomeBottomBar` shared composable; per-frame ID for this frame is the parent's child) |

The **behaviour** of every component is identical. The implementer
fetches frame-specific pixel chrome on demand via `query_section`
against these new IDs only if there's a visual divergence (none
documented as of 2026-05-11).

---

## Data Requirements

Identical to the canonical spec's § Key Entities. The `awards` table
already carries the Top Project row (seeded in DEMO; production
backend is the source of truth for column values).

**DEMO_DETAILS row for Top Project** (already in
`DemoAwardsRepository`):

```kotlin
AwardDetail(
    id = "00000000-0000-0000-0000-000000000a02",
    name = "Top Project",
    description = "Top Project ghi nhận những dự án đem lại tác động lớn nhất tới khách hàng, đội nhóm, và cộng đồng Sun* trong năm 2025.",
    quantity = 5,           // ⚠ MISMATCH WITH FIGMA — see Open Question Q-TP-1
    quantityUnit = "Dự án", // ⚠ MISMATCH WITH FIGMA ("Tập thể") — see Q-TP-1
    prizeValue = "15.000.000 VNĐ",
    imageUrl = null,
    sortOrder = 2,
)
```

**Resolved Q-TP-1 (data alignment with Figma — 2026-05-11)**:
DEMO_DETAILS for Top Project was updated to match Figma node
`6885:10468` verbatim:

- `quantity`: `5` → **`2`**
- `quantityUnit`: `"Dự án"` → **`"Tập thể"`**
- `description`: short summary → **full Figma copy** ("Giải thưởng
  Top Project vinh danh các tập thể dự án xuất sắc với kết quả kinh
  doanh vượt kỳ vọng…")

**Still open** (deferred to backend confirmation): production Supabase
`awards` table row for Top Project should mirror these Figma values.
If the production row differs, Figma + product copy is the source of
truth — file a follow-on migration to align.

---

## API Requirements

Identical to canonical spec § API Dependencies. No new endpoints.

---

## State Management

Identical to canonical spec § State Management. No new state types.

---

## Success Criteria

Identical to canonical spec § Success Criteria.

---

## Out of Scope

- **Building a separate composable for Top Project**: forbidden — the
  whole point of the parametric Award Detail design is one composable
  serves every award. Any feature that needs per-award-only behaviour
  (e.g. a "Top Project gallery" hypothetical) would be a NEW spec
  with a NEW screen file.
- **Visual specs**: per Constitution Principle II + canonical spec
  § Out of Scope. The implementer fetches values at task-execution
  time via `query_section` against the per-frame Node IDs above —
  NOT enumerated here. As of 2026-05-11 review the visual chrome is
  identical to Top Talent's, so no extra `query_section` calls are
  needed unless a regression surfaces.
- **A new `[iOS] Award_*` spec for Top Heart, Best Manager, MVP,
  Signature 2025 — Creator**: these will follow the same delta-spec
  pattern. **Do not** author a 750-line duplicate per frame.

---

## Dependencies

- [x] Canonical spec `c-QM3_zjkG-iOS-Award-Top-talent` ratified
- [x] `AwardDetailScreen` composable shipped (commit `06a9f87` +
  follow-on polish)
- [x] `DemoAwardsRepository.DEMO_DETAILS` already carries a Top
  Project entry (commit `203e196`)
- [ ] **Q-TP-1 resolution**: update DEMO_DETAILS to match Figma
  values (quantity / unit / description) — see § Data Requirements
  open question. Block ratification of any visual regression test
  for this frame until resolved.
- [ ] **Top Project badge image**: bundle `ic_award_top_project.png`
  (download Figma node `6885:10458` or whatever the Top Project
  composite resolves to via `get_media_files` on this frame) and
  wire it into `DemoAwardsRepository`'s Top Project `imageUrl`
  field. Mirrors the Top Talent badge wiring (T100 from canonical
  tasks).

---

## Notes

### Why a delta-spec instead of a full spec

The Top Project frame is **functionally identical** to Top Talent —
same layout, same components, same Node-ID structure, same Figma
template, same behaviour, same data shape. The MoMorph frame system
treats every default-rendered award as a separate frame because the
design tool exports one canvas per state, but **on Android there is
one screen**.

Writing a 750-line duplicate spec would:

- Lock in two sources of truth that drift apart over time.
- Hide that the implementation already covers this frame (zero new
  code needed).
- Bloat the spec tree and slow down future Top Heart / MVP / Best
  Manager / Signature 2025 frames that will follow the same pattern.

The delta-spec pattern keeps the canonical spec as the source of
truth and uses this file purely as a **traceability anchor** for
MoMorph's per-frame test-case IDs (`TC_AWARD_TOP_PROJECT_*`) that
otherwise would have no home.

### Cross-frame test-case mapping

| MoMorph test case | Maps to canonical spec |
|-------------------|------------------------|
| `TC_AWARD_TOP_PROJECT_ACC_001` (authenticated access) | Canonical US1 acceptance scenario 1 + US8 |
| `TC_AWARD_TOP_PROJECT_ACC_002` (unauthenticated → Login) | Canonical US8 |
| `TC_AWARD_TOP_PROJECT_ACC_003` (Awards tab nav) | Canonical US3 + T101 (Phase 8) |
| `TC_AWARD_TOP_PROJECT_ACC_004` (dropdown selects Top Project) | Canonical US2 acceptance scenario 2 |
| `TC_AWARD_TOP_PROJECT_GUI_001` (overall layout) | Canonical US1 + Frame-specific scenario 1 above |
| `TC_AWARD_TOP_PROJECT_GUI_002` (dropdown default = Top Project) | Frame-specific scenario 2 above |
| `TC_AWARD_TOP_PROJECT_FUN_001` (dropdown opens) | Canonical US2 acceptance scenario 1 |
| `TC_AWARD_TOP_PROJECT_FUN_002` (dropdown switches award) | Canonical US2 acceptance scenario 2 + Frame-specific scenario 4 |
| `TC_AWARD_TOP_PROJECT_FUN_003` (scroll body) | Canonical US3 acceptance scenario 5 + FR-014 |
| `TC_AWARD_TOP_PROJECT_FUN_004` (sticky header + nav on scroll) | Canonical FR-014 |

### Next likely frames following this pattern

- `[iOS] Award_Top project leader` (`QQvsfK3yaK`)
- `[iOS] Award_MVP` (`b2BuS8HYIt`)
- `[iOS] Award_Best Manager` (`7y195PPTxQ`)
- `[iOS] Award_Signature 2025 - Creator` (`O98TwiHaJe`)

Each will be a 1-page delta-spec referencing this same canonical
spec. The implementer's job for each is: pull the badge image,
update DEMO_DETAILS, add the row to the `awards` Supabase table —
**no new screen file**.
