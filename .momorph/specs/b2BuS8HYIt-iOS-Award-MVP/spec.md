# Feature Specification: Award Detail — MVP default

**Frame ID**: `b2BuS8HYIt`
**Frame Name**: `[iOS] Award_MVP`
**File Key**: `9ypp4enmFmdK3YAFJLIu6C`
**Created**: 2026-05-11
**Status**: Ratified (delta-only spec — see § Same-Screen Declaration)

---

## Same-Screen Declaration

This frame renders the **same parametric Award Detail screen** as
`c-QM3_zjkG` (`[iOS] Award_Top talent`).

**Canonical spec**: `.momorph/specs/c-QM3_zjkG-iOS-Award-Top-talent/spec.md`

**Sibling delta-specs**: `FQoJZLkG_d` (Top Project), `QQvsfK3yaK`
(Top Project Leader), `7y195PPTxQ` (Best Manager). Earlier deltas
were pure data swaps; **this delta is NOT** — it introduces
**Q-MVP-1** (custom prize caption) which extends the `AwardDetail`
data model + `AwardInfoBlock` composable.

---

## Overview

Award Detail rendered with **MVP (Most Valuable Person)** as the
default selection. Reached via the carousel Chi tiết tap on MVP or
selecting "MVP (Most Valuable Person)" from the category dropdown.

**Target users**: same as canonical — authenticated Sunners.

**Implementation platform**: Android only. **Single composable**:
`AwardDetailScreen` — no new file. The composable contract is
extended (additive, backward-compatible) to support Q-MVP-1.

---

## What's different from the canonical spec

| Field | Top Talent (`c-QM3_zjkG`) | **MVP (this frame)** |
|-------|---------------------------|----------------------|
| `award.id` (UUID) | `…000000a01` | `…000000a06` (proposed) |
| `award.name` (carousel) | "Top Talent Award" | "MVP (Most Valuable Person) Award" |
| `award.name` (detail body + dropdown) | "Top Talent" | **"MVP (Most Valuable Person)"** — long, wraps to 2 lines in the dropdown anchor |
| Badge image (Figma) | "TOP TALENT" lockup | **"MVP" lockup** (large central glyph, 56×25 wordmark per Figma) |
| Description | "Vinh danh cá nhân xuất sắc…" | Full Figma copy from text node `6885:10771` (≈600 chars across multiple sentences about năng lực vượt trội + cảm hứng lan tỏa + trọng trách dẫn dắt tập thể) |
| `award.quantity` | `10` | **`1`** (renders as `"01"` via Q-TP-2 formatter) |
| `award.quantity_unit` | "Cá nhân" | "Cá nhân" (same) |
| `award.prize_value` | "7.000.000 VNĐ" | **"15.000.000 VNĐ"** |
| `award.prize_caption` | (default "cho mỗi giải thưởng") | **"cho giải cá nhân"** — **Q-MVP-1**, NEW BEHAVIOR |
| `sort_order` | `1` | `6` (proposed) |

---

## Q-MVP-1 — Custom prize caption

**Problem**: The canonical `AwardInfoBlock` hardcodes the prize
caption to `R.string.award_detail_prize_caption` ("cho mỗi giải thưởng").
MVP per Figma uses **"cho giải cá nhân"** — a per-award-specific
caption.

**Resolution (2026-05-11)**: Extend `AwardDetail` with an optional
`prizeCaption: String?` field (null = use default). Extend
`AwardInfoBlock` to accept the override; the internal
`PrizeValueRow` falls back to `R.string.award_detail_prize_caption`
when the parameter is null. Backward-compatible — all four existing
delta-specs continue rendering the default caption.

**Why custom caption (not new string resource)**:
- The caption is **per-award** data, not per-locale UI chrome. It
  comes from `awards.prize_caption` (proposed Supabase column) /
  `AwardDetail.prizeCaption`, NOT from a translation file.
- Future awards might introduce more caption variants (e.g.,
  Signature 2025 - Creator's dual caption per Q-SIG-1) — a
  Figma-string-per-resource approach would explode.
- The implementer MUST localize the caption value upstream (backend
  serves per-locale text); the client only renders verbatim per
  Resolved Q5 of the canonical spec.

**Impact summary**:
- `AwardDetail.kt`: +1 optional field (`prizeCaption: String? = null`)
- `AwardInfoBlock.kt`: +1 optional param, fallback to string resource
- `AwardInfoBlockTest.kt`: +2 regression tests
  (`renders_default_prize_caption_when_prize_caption_is_null`,
  `renders_custom_prize_caption_when_provided`)

---

## User Scenarios

Inherits US1–US8 from canonical. Frame-specific:

1. **Mount with MVP default (TC_AWARD_MVP_GUI_001)**
   - **Given** the user navigates here via carousel Chi tiết on MVP
     OR selects "MVP (Most Valuable Person)" from the dropdown,
   - **When** body resolves to `Loaded`,
   - **Then** dropdown trigger reads "MVP (Most Valuable Person)"
     (text wraps to 2 lines if needed), badge shows the "MVP" lockup,
     description matches Figma node `6885:10771`, quantity row reads
     "01 Cá nhân", prize row reads **"15.000.000 VNĐ cho giải cá nhân"**
     (NOT the default caption "cho mỗi giải thưởng").

2. **Switch from MVP to a default-caption award** (regression for
   Q-MVP-1)
   - **Given** the screen is on MVP showing "cho giải cá nhân",
   - **When** the user selects "Top Talent" from the dropdown,
   - **Then** the prize caption switches back to the default
     "cho mỗi giải thưởng". No stale caption text bleeds across
     award switches.

All other scenarios inherit from canonical unchanged.

---

## Component Behavior

Same § Screen Components table as canonical, with per-frame Node IDs
in the `6885:107x` range. The only material change is the prize-row
caption source (Q-MVP-1).

---

## Data Requirements

**Proposed DEMO_DETAILS row for MVP**:

```kotlin
AwardDetail(
    id = "00000000-0000-0000-0000-000000000a06", // proposed
    name = "MVP (Most Valuable Person)",
    description = "<full Figma copy from text node 6885:10771>",
    quantity = 1, // renders as "01" via Q-TP-2 formatter
    quantityUnit = "Cá nhân",
    prizeValue = "15.000.000 VNĐ",
    prizeCaption = "cho giải cá nhân", // Q-MVP-1 override
    imageUrl = null, // until Slice A bundles the badge — see § Dependencies
    sortOrder = 6, // proposed
)
```

---

## API Requirements / State Management / Success Criteria

Identical to canonical. The proposed Supabase column
`awards.prize_caption: text | null` MUST exist for production
non-DEMO mode; otherwise an existing `awards` row mapping to MVP
would render with the default caption (visual drift).

---

## Out of Scope

- A separate composable for MVP — forbidden.
- Per-locale captions via string resources — handled upstream (see
  Q-MVP-1 resolution).
- A spec for Signature 2025 — Creator's dual-prize behavior — that
  is `O98TwiHaJe`'s delta-spec with its own Q-SIG-1.

---

## Dependencies

- [x] Canonical spec ratified
- [x] Sibling delta-specs `FQoJZLkG_d`, `QQvsfK3yaK`, `7y195PPTxQ`
  ratified
- [x] Q-TP-2 `%02d` formatter shipped — handles `quantity = 1` as `"01"`
- [ ] **Q-MVP-1 architecture change**: extend `AwardDetail` +
  `AwardInfoBlock` to support optional `prizeCaption`. Ships in the
  same commit batch as this delta-spec's implementation.
- [ ] **DEMO append**: 1 row in `DEMO_AWARDS` + 1 row in
  `DEMO_DETAILS`.
- [ ] **MVP badge bundle**: BG (160×160) + "MVP" wordmark (56×25)
  composite offline via Python + Pillow; bundle as
  `drawable-mdpi/ic_award_mvp.png`.
- [ ] **Regression tests**: extend `DemoAwardsRepositoryTest`
  (asserts `prizeCaption = "cho giải cá nhân"`) + `AwardInfoBlockTest`
  (caption override + default-fallback cases).

---

## Notes

### Comparison across all 5 delta-specs

| Field | Top Project | Top Project Leader | Best Manager | **MVP** | Signature 2025 — Creator |
|-------|-------------|--------------------|--------------| -------|--------------------------|
| `quantity` | 2 | 3 | 1 | **1** | 1 |
| `quantityUnit` | Tập thể | Cá nhân | Cá nhân | **Cá nhân** | Cá nhân hoặc tập thể |
| `prizeValue` | 15M | 7M | 10M | **15M** | 5M (cá nhân) |
| `prizeCaption` | (default) | (default) | (default) | **"cho giải cá nhân"** | "cho giải cá nhân" |
| `prizeValueTeam` | — | — | — | — | 8M (tập thể) |
| New Q-number | Q-TP-1, Q-TP-2 | none | none | **Q-MVP-1** | Q-SIG-1 |

### Cross-frame test-case mapping

| MoMorph test case | Maps to canonical spec |
|-------------------|------------------------|
| `TC_AWARD_MVP_ACC_001` (authenticated access) | Canonical US1 acceptance scenario 1 + US8 |
| `TC_AWARD_MVP_ACC_002` (unauthenticated → Login) | Canonical US8 |
| `TC_AWARD_MVP_ACC_003` (Awards tab nav) | Canonical US3 + T101 |
| `TC_AWARD_MVP_ACC_004` (dropdown selects MVP) | Canonical US2 acceptance scenario 2 |
| `TC_AWARD_MVP_GUI_001` (overall layout) | Canonical US1 + Frame-specific scenario 1 above |
| `TC_AWARD_MVP_GUI_002` (dropdown default = MVP) | Frame-specific scenario above |
| `TC_AWARD_MVP_FUN_001` (dropdown opens) | Canonical US2 acceptance scenario 1 |
| `TC_AWARD_MVP_FUN_002` (dropdown switches award) | Canonical US2 + Q-MVP-1 caption regression (frame scenario 2) |
