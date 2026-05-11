# Feature Specification: Award Detail — Signature 2025 - Creator default

**Frame ID**: `O98TwiHaJe`
**Frame Name**: `[iOS] Award_Signature 2025 - Creator`
**File Key**: `9ypp4enmFmdK3YAFJLIu6C`
**Created**: 2026-05-11
**Status**: Ratified (delta-only spec — see § Same-Screen Declaration)

---

## Same-Screen Declaration

This frame renders the **same parametric Award Detail screen** as
`c-QM3_zjkG` (`[iOS] Award_Top talent`).

**Canonical spec**: `.momorph/specs/c-QM3_zjkG-iOS-Award-Top-talent/spec.md`

**Sibling delta-specs**: `FQoJZLkG_d`, `QQvsfK3yaK`, `7y195PPTxQ`,
`b2BuS8HYIt`. Most prior deltas were pure data swaps;
`b2BuS8HYIt` (MVP) introduced **Q-MVP-1** (custom caption); **this
delta** introduces **Q-SIG-1** (dual prize-value rows) which extends
the `AwardDetail` model further.

---

## Overview

Award Detail rendered with **Signature 2025 - Creator** as the
default selection. The award is open to both individuals and teams —
each with its own prize value — so the prize section renders **two
rows** instead of the canonical one.

**Target users**: same as canonical — authenticated Sunners.

**Implementation platform**: Android only. **Single composable**:
`AwardDetailScreen` — no new file. The composable contract is
extended (additive, backward-compatible) to support Q-SIG-1 in
addition to Q-MVP-1.

---

## What's different from the canonical spec

| Field | Top Talent (`c-QM3_zjkG`) | **Signature 2025 - Creator (this frame)** |
|-------|---------------------------|-------------------------------------------|
| `award.id` (UUID) | `…000000a01` | `…000000a07` (proposed) |
| `award.name` (carousel) | "Top Talent Award" | "Signature 2025 - Creator Award" |
| `award.name` (detail body + dropdown) | "Top Talent" | **"Signature 2025 - Creator"** |
| Badge image (Figma) | "TOP TALENT" lockup | **"Signature 2025 Creator" lockup** (111×26 wordmark per Figma) |
| Description | "Vinh danh cá nhân xuất sắc…" | Full Figma copy from text node `6885:10690` (≈600 chars about tinh thần "Creator", tư duy chủ động, nhạy bén với vấn đề, định hình chuẩn mực mới) |
| `award.quantity` | `10` | **`1`** (renders as `"01"` via Q-TP-2 formatter) |
| `award.quantity_unit` | "Cá nhân" | **"Cá nhân hoặc tập thể"** — longest unit string in the demo set |
| `award.prize_value` (cá nhân) | "7.000.000 VNĐ" | **"5.000.000 VNĐ"** |
| `award.prize_caption` (cá nhân) | (default "cho mỗi giải thưởng") | **"cho giải cá nhân"** — same override as MVP per Q-MVP-1 |
| **`award.prize_value_team`** | (n/a — canonical has one row) | **"8.000.000 VNĐ"** — **Q-SIG-1**, NEW BEHAVIOR |
| **`award.prize_caption_team`** | (n/a) | **"cho giải tập thể"** — **Q-SIG-1** |
| `sort_order` | `1` | `7` (proposed) |

---

## Q-SIG-1 — Dual prize-value rows

**Problem**: Canonical `AwardInfoBlock` renders a **single**
prize-value row under the "Giá trị giải thưởng" label. Signature 2025
— Creator splits the prize between two payouts (individual + team),
each with its own caption — requiring **two value rows** under the
same label.

**Resolution (2026-05-11)**: Extend `AwardDetail` with optional
`prizeValueTeam: String?` and `prizeCaptionTeam: String?` fields.
Extend `AwardInfoBlock`: when **both** team fields are non-null,
render a second `PrizeValueRow` after the first within the same
prize section column (which already uses `verticalArrangement =
Arrangement.spacedBy(12.dp)` — the gap propagates between the two
value rows). Defensive: if only one of the two team fields is set,
the second row is skipped (verified by
`AwardInfoBlockTest.second_row_does_not_render_when_only_team_value_provided`).
Backward-compatible — all five prior delta-specs continue rendering
a single row.

**Why not a unit-level dual-prize**:
- The two prize rows aren't "individual unit prizes" — they're
  separate award outcomes. Modeling as data preserves semantics.
- A future award could plausibly have a third tier (e.g., "best of
  fundraising"); the model could later grow to a list, but for
  now 1-or-2 is enough.

**Impact summary**:
- `AwardDetail.kt`: +2 optional fields (`prizeValueTeam`,
  `prizeCaptionTeam`).
- `AwardInfoBlock.kt`: +2 optional params, conditional render of
  second `PrizeValueRow`.
- `AwardInfoBlockTest.kt`: +3 regression tests
  (`renders_only_first_prize_row_when_team_fields_are_null`,
  `renders_both_prize_rows_when_team_fields_provided`,
  `second_row_does_not_render_when_only_team_value_provided`).

---

## User Scenarios

Inherits US1–US8 from canonical. Frame-specific:

1. **Mount with Signature 2025 - Creator default (TC_AWARD_SIG_GUI_001)**
   - **Given** the user navigates here via carousel Chi tiết OR
     selects "Signature 2025 - Creator" from the dropdown,
   - **When** body resolves to `Loaded`,
   - **Then** dropdown trigger reads "Signature 2025 - Creator",
     badge shows the Signature wordmark, description matches Figma
     node `6885:10690`, quantity row reads "01 Cá nhân hoặc tập thể",
     and the prize section renders **TWO rows**:
     - Row 1: "5.000.000 VNĐ cho giải cá nhân"
     - Row 2: "8.000.000 VNĐ cho giải tập thể"

2. **Switch from Signature 2025 to a single-prize award** (regression
   for Q-SIG-1)
   - **Given** the screen is on Signature 2025 showing 2 prize rows,
   - **When** the user selects "Top Talent" from the dropdown,
   - **Then** the prize section collapses back to a single row. No
     stale second row bleeds across award switches.

All other scenarios inherit from canonical unchanged.

---

## Component Behavior

Same § Screen Components table as canonical, with per-frame Node IDs
in the `6885:106x`–`6885:107x` range. The material change is the
prize-section's optional second row (Q-SIG-1).

The Figma frame ships **separate FRAME nodes** for the two prize
sub-sections (`6885:10708` for cá nhân + `6885:10714` for tập thể).
Our Android implementation collapses both into a single
"Giá trị giải thưởng" section column to avoid duplicating the
section label — the visual hierarchy stays close to Figma because
the two rows sit immediately under one label with the same 12dp
vertical rhythm.

---

## Data Requirements

**Proposed DEMO_DETAILS row for Signature 2025 - Creator**:

```kotlin
AwardDetail(
    id = "00000000-0000-0000-0000-000000000a07", // proposed
    name = "Signature 2025 - Creator",
    description = "<full Figma copy from text node 6885:10690>",
    quantity = 1, // renders as "01"
    quantityUnit = "Cá nhân hoặc tập thể",
    prizeValue = "5.000.000 VNĐ",
    prizeCaption = "cho giải cá nhân", // Q-MVP-1 override
    prizeValueTeam = "8.000.000 VNĐ",   // Q-SIG-1 second row
    prizeCaptionTeam = "cho giải tập thể",
    imageUrl = null, // until Slice A bundles the badge
    sortOrder = 7, // proposed
)
```

---

## API Requirements / State Management / Success Criteria

Identical to canonical. The proposed Supabase columns
`awards.prize_value_team: text | null` +
`awards.prize_caption_team: text | null` MUST exist for production
non-DEMO mode; otherwise an existing `awards` row mapping to
Signature 2025 would render with a single prize row (visual drift,
data loss).

---

## Out of Scope

- A separate composable for Signature 2025 — forbidden.
- Modeling three-or-more prize tiers (`prizeValueX` for X ≥ 3) —
  no current award needs it; if a future award does, evolve the
  model then.
- Per-locale captions via string resources — handled upstream per
  Q-MVP-1 resolution.

---

## Dependencies

- [x] Canonical spec ratified
- [x] Sibling delta-specs `FQoJZLkG_d`, `QQvsfK3yaK`, `7y195PPTxQ`,
  `b2BuS8HYIt` ratified
- [x] Q-TP-2 `%02d` formatter shipped
- [ ] **Q-MVP-1 architecture change**: extend `AwardDetail` +
  `AwardInfoBlock` to support optional `prizeCaption`. Shared with
  this delta — both ship in the same commit batch.
- [ ] **Q-SIG-1 architecture change**: extend `AwardDetail` with
  optional `prizeValueTeam` + `prizeCaptionTeam`. Extend
  `AwardInfoBlock` to conditionally render a second `PrizeValueRow`.
- [ ] **DEMO append**: 1 row in `DEMO_AWARDS` + 1 row in
  `DEMO_DETAILS` with both prize tiers populated.
- [ ] **Signature 2025 badge bundle**: BG (160×160) + wordmark
  (111×26) composite via Python + Pillow; bundle as
  `drawable-mdpi/ic_award_signature_2025_creator.png`.
- [ ] **Regression tests**: extend `DemoAwardsRepositoryTest` +
  `AwardInfoBlockTest` (caption override + dual-prize render +
  defensive partial-team-fields case).

---

## Notes

### Cross-frame test-case mapping

| MoMorph test case | Maps to canonical spec |
|-------------------|------------------------|
| `TC_AWARD_SIG_ACC_001` (authenticated access) | Canonical US1 + US8 |
| `TC_AWARD_SIG_ACC_002` (unauthenticated → Login) | Canonical US8 |
| `TC_AWARD_SIG_ACC_003` (Awards tab nav) | Canonical US3 + T101 |
| `TC_AWARD_SIG_ACC_004` (dropdown selects Signature 2025) | Canonical US2 acceptance scenario 2 |
| `TC_AWARD_SIG_GUI_001` (overall layout incl. dual prize rows) | Canonical US1 + Frame-specific scenario 1 above |
| `TC_AWARD_SIG_GUI_002` (dropdown default = Signature 2025) | Frame-specific scenario above |
| `TC_AWARD_SIG_FUN_001` (dropdown opens) | Canonical US2 acceptance scenario 1 |
| `TC_AWARD_SIG_FUN_002` (dropdown switches; dual rows collapse on switch) | Canonical US2 + Q-SIG-1 regression (frame scenario 2) |

### Why both Q-MVP-1 + Q-SIG-1 lacked carbon-copy precedent

The first three delta-specs (`FQoJZLkG_d`, `QQvsfK3yaK`, `7y195PPTxQ`)
validated the pure-data-append pattern. MVP and Signature 2025 break
that pattern because Figma's design language has stretched beyond
the canonical Award Detail template — both signal that the canonical
data model needs to grow to keep delta-specs lightweight.

Better to absorb that growth via two additive Q-numbers (each one
backward-compatible) than to leave Figma drift in the demo build OR
write yet-more-bespoke composables. After Q-MVP-1 + Q-SIG-1 land,
any future delta-spec gets to choose from a richer toolkit:

| Behavior | Available via |
|----------|---------------|
| Different award name | (data) |
| Different quantity (single digit OK) | (data — Q-TP-2 formatter handles it) |
| Different unit / prize / description | (data) |
| Different prize caption | (data — Q-MVP-1) |
| Two prize-value rows (individual + team) | (data — Q-SIG-1) |

This pushes future deltas back into pure-data-append territory,
restoring the ~20-minute spec→impl pace.
