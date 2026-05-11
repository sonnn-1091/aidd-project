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

**DEMO_DETAILS row for Top Project** (resolved per Q-TP-1, commit
`daaf526` — values now match Figma node `6885:10468` verbatim):

```kotlin
AwardDetail(
    id = "00000000-0000-0000-0000-000000000a02",
    name = "Top Project",
    description = "Giải thưởng Top Project vinh danh các tập thể dự án xuất sắc " +
        "với kết quả kinh doanh vượt kỳ vọng, hiệu quả vận hành tối ưu " +
        "và tinh thần làm việc tận tâm. …",  // full 8-sentence Figma copy
    quantity = 2,
    quantityUnit = "Tập thể",
    prizeValue = "15.000.000 VNĐ",
    imageUrl = null, // composite badge unavailable from MoMorph; text overlay used
    sortOrder = 2,
)
```

**Resolved Q-TP-1 (data alignment with Figma — 2026-05-11)**:
Previous demo values had drifted from Figma — `quantity = 5`,
`quantityUnit = "Dự án"`, and a short summary description. All three
were updated to match Figma node `6885:10468` verbatim in commit
`daaf526`. Visual smoke on emulator-5554 confirmed the body now
renders the full Figma copy + `2 Tập thể` + `15.000.000 VNĐ`.

**Still open — backend confirmation**: production Supabase `awards`
table row for Top Project should mirror these Figma values. If the
production row differs, Figma + product copy is the source of truth
— file a follow-on migration to align. Non-blocking for the demo
build.

**Resolved Q-TP-2 (quantity display formatting — 2026-05-11)**:
Figma node `6885:10475` displays `02` (zero-padded) for `quantity = 2`,
but the impl previously rendered `quantity.toString()` which yielded
`2`. Top Talent's `quantity = 10` masked the discrepancy.

**Chosen Option A**: zero-pad single-digit counts client-side via
`"%02d".format(quantity)` in `AwardInfoBlock.QuantityValueRow`. The
Java/Kotlin `%02d` directive formats AT LEAST 2 digits — 3+ digit
counts pass through unchanged. Effects across the demo set:

- Top Talent (`10`) → `10 Cá nhân` (unchanged)
- Top Project (`2`) → `02 Tập thể` ✓ matches Figma
- Top Heart (`8`) → `08 Cá nhân` (consistent with the pattern)

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
- [x] **Q-TP-1 resolution**: DEMO_DETAILS values aligned with Figma
  (`quantity = 2`, `quantityUnit = "Tập thể"`, full description) —
  shipped in commit `daaf526`.
- [x] **Q-TP-2 resolution**: zero-padded single-digit quantity
  formatting (`"%02d".format(quantity)`) wired into
  `AwardInfoBlock.QuantityValueRow`. See § Data Requirements
  Q-TP-2.
- [ ] **Top Project badge image**: MoMorph's `get_media_files`
  returns `null` for the Top Project Picture-Award composite node
  (the BG + wordmark layers exist separately but no pre-composited
  PNG is exported). Current path: the existing
  `AwardHeroBlock` text-overlay fallback renders the uppercased
  award name (`TOP PROJECT`) on the placeholder. To match Figma's
  graphic exactly, either (a) bundle the BG + wordmark layers and
  composite at render time, or (b) wait for Supabase Storage to
  ship a pre-composited URL via `awards.image_url`. Tracked as a
  visual-polish follow-on, not blocking ratification.

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
