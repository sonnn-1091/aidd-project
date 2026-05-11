# Feature Specification: Award Detail — Top Project Leader default

**Frame ID**: `QQvsfK3yaK`
**Frame Name**: `[iOS] Award_Top project leader`
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

**Sibling delta-spec**: `.momorph/specs/FQoJZLkG_d-iOS-Award-Top-project/spec.md`
— the first delta-spec to follow this same pattern; reviewed four
times 2026-05-11. Read it for an example of how the delta-spec
template scales across the four remaining Award frames (Top Heart,
MVP, Best Manager, Signature 2025 — Creator, and this one).

**Read the canonical spec first.** Every functional requirement,
technical requirement, success criterion, edge case, state-management
contract, and API dependency from the canonical spec applies here
**unchanged**. This document records only:

1. What is **different** about this frame (default award data).
2. How the existing implementation already covers it (zero new code
   beyond a one-row DEMO append).
3. The frame-specific test cases.

If you're looking for the screen's behaviour, navigation flow,
auth gate, dropdown contract, sticky chrome contract, or anything
else generic — read the canonical spec.

---

## Overview

The Award Detail screen rendered with **Top Project Leader** as the
default selection. The user reaches it by:

- Selecting "Top Project Leader" from the category dropdown while
  already on Award Detail.
- Tapping **Chi tiết** on the Top Project Leader card in Home's
  awards carousel — the route is
  `Routes.awardDetail("<top-project-leader-uuid>")`. The Home carousel
  must list a Top Project Leader entry first (see § Dependencies).

**Target users**: same as canonical — authenticated Sunners.

**Implementation platform**: Android only (Kotlin + Compose + M3 +
Supabase). **Single composable**: `AwardDetailScreen` at
`com.example.aiddproject.awarddetail.ui.AwardDetailScreen` — no new
file, no new ViewModel.

---

## What's different from the canonical spec

| Field | Top Talent (`c-QM3_zjkG`) | **Top Project Leader (this frame)** |
|-------|---------------------------|-------------------------------------|
| `award.id` (UUID) | `…000000a01` | `…000000a04` (proposed — implementer's call when appending to DEMO + Supabase) |
| `award.name` (carousel) | "Top Talent Award" | "Top Project Leader Award" |
| `award.name` (detail body + dropdown) | "Top Talent" | **"Top Project Leader"** |
| Badge image (Figma) | "TOP TALENT" lockup | **"TOP PROJECT LEADER" lockup** (composite via `mm_media_Picture-Award` instance — see § Dependencies) |
| Description | "Vinh danh cá nhân xuất sắc…" | **"Giải thưởng Top Project Leader vinh danh những nhà quản lý dự án xuất sắc – những người hội tụ năng lực quản lý vững vàng; khả năng truyền cảm hứng mạnh mẽ; và tư duy 'Aim High – Be Agile' trong mọi bài toán và bối cảnh… (full Figma copy at text node `6885:10542` — pull verbatim at implementation time via `query_section`)"** |
| `award.quantity` | `10` | **`3`** (renders as `"03"` via the shipped Q-TP-2 `"%02d"` formatter — no new client work) |
| `award.quantity_unit` | "Cá nhân" | **"Cá nhân"** (same as Top Talent) |
| `award.prize_value` | "7.000.000 VNĐ" | **"7.000.000 VNĐ"** (same as Top Talent) |
| `sort_order` | `1` | `4` (proposed — implementer's call) |

**That is the entire diff.** Everything else — sticky chrome, dropdown
behaviour, Sun\*Kudos Chi tiết destination, auth gate, header chrome,
bottom-nav routing, locale switch, error/retry/null-placeholder
handling, telemetry, Q-TP-2 zero-pad — is **identical** and already
shipped per canonical's tasks T001–T103 + delta-spec FQoJZLkG_d's
Phase 1–6 (commits `e20f419` → `ff92ff6`).

**No new Q-numbers**: unlike Top Project's Q-TP-1 (DEMO drift from
Figma) and Q-TP-2 (single-digit zero-pad), Top Project Leader's data
shape matches Top Talent's exactly — same unit (`Cá nhân`), same
prize value (`7.000.000 VNĐ`), single-digit quantity already handled
by the shipped `"%02d"` formatter. Pure data append.

---

## User Scenarios

All user stories (US1–US8) from the canonical spec apply unchanged.
The only frame-specific scenarios are mirror cases that exercise the
**Top Project Leader** payload instead of Top Talent. They are listed
below for traceability against MoMorph's `TC_AWARD_TOP_PL_*` test
case IDs.

### Frame-specific acceptance scenarios

These are **parametric repeats** of the canonical US1–US3 scenarios
swapped to Top Project Leader data. No new behaviour.

1. **Mount with Top Project Leader default (TC_AWARD_TOP_PL_GUI_001)**
   - **Given** the user navigates to this frame via the Home carousel
     Chi tiết tap on Top Project Leader OR selects "Top Project
     Leader" from the category dropdown,
   - **When** the body resolves to `AwardDetailState.Loaded`,
   - **Then** the dropdown trigger reads "Top Project Leader", the
     badge image renders the "TOP PROJECT LEADER" lockup, the
     description matches Figma node `6885:10542`, the quantity row
     reads "03 Cá nhân", and the prize row reads "7.000.000 VNĐ cho
     mỗi giải thưởng".

2. **Dropdown default state (TC_AWARD_TOP_PL_GUI_002)**
   - **Given** the screen is rendered with Top Project Leader as the
     active award,
   - **When** the user observes the Highlight block,
   - **Then** the dropdown trigger pill renders "Top Project Leader"
     + the chevron — matching the design pill chrome from canonical
     US2 + FR-005.

3. **Bottom-nav Awards tab navigation (TC_AWARD_TOP_PL_ACC_003)**
   - **Given** the user is on any authenticated screen,
   - **When** the user taps **Awards** in the bottom navigation bar,
   - **Then** Award Detail mounts. The default award is determined
     per canonical FR-001 + Resolved Q1 — last-viewed in-session,
     fallback to first-by-`sort_order`. **Top Project Leader becomes
     the default only if it was last viewed**; otherwise Top Talent
     remains the default (first by sort_order). The frame's existence
     does NOT change the default-selection rules.

4. **Switch to a different award (TC_AWARD_TOP_PL_FUN_002)**
   - **Given** the screen is on Top Project Leader,
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
canonical spec's `6885:1029x` IDs become this frame's `6885:1051x`
range), but they map 1:1:

| Component | Top Talent node | **Top Project Leader node** |
|-----------|----------------|-----------------------------|
| KV Kudos banner | `6885:10266` | **`6885:10510`** |
| Highlight block | `6885:10283` | **`6885:10527`** |
| Highlight header (sub-label + title + dropdown) | `6885:10284` | **`6885:10528`** |
| Award info — title text | `6885:10297` | **`6885:10541`** |
| Award info — description text | `6885:10298` | **`6885:10542`** |
| Award info — quantity label | `6885:10303` | **`6885:10547`** |
| Award info — quantity value | `6885:10305` | **`6885:10549`** |
| Award info — quantity unit | (n/a — same row as value) | **`6885:10550`** |
| Award info — prize label | `6885:10306` | **`6885:10555`** |
| Award info — prize value | `6885:10311` | **`6885:10557`** |
| Award info — prize caption | (n/a — same row as value) | **`6885:10558`** |

The **behaviour** of every component is identical. The implementer
fetches frame-specific pixel chrome on demand via `query_section`
against these new IDs only if there's a visual divergence (none
documented as of 2026-05-11).

---

## Data Requirements

Identical to the canonical spec's § Key Entities. The `awards` table
needs **one new row appended** to carry the Top Project Leader entry
(production Supabase + DEMO `DEMO_DETAILS`).

**Proposed DEMO_DETAILS row for Top Project Leader** (implementer
finalizes UUID + sortOrder when appending):

```kotlin
AwardDetail(
    id = "00000000-0000-0000-0000-000000000a04", // proposed — confirm at impl time
    name = "Top Project Leader",
    description =
        "<full Figma copy from text node 6885:10542 — pull verbatim " +
            "at implementation time via `mcp__momorph__query_section`>",
    quantity = 3, // renders as "03" via shipped Q-TP-2 %02d formatter
    quantityUnit = "Cá nhân",
    prizeValue = "7.000.000 VNĐ",
    imageUrl = null, // until Slice A bundles the badge composite — see § Dependencies
    sortOrder = 4, // proposed — confirm at impl time
)
```

**Proposed carousel `Award` row in `DEMO_AWARDS`**:

```kotlin
Award(
    id = "00000000-0000-0000-0000-000000000a04",
    name = "Top Project Leader Award", // " Award" suffix stripped by AwardCategoryDropdown.displayName
    thumbnailUrl = null,
    sortOrder = 4,
)
```

The description text MUST be pulled verbatim from Figma at implementation
time — this spec deliberately does NOT inline the full copy because (a) it
is long, and (b) inlining a Figma string risks drift between spec and
source. Use `mcp__momorph__query_section` with `nodeId = "6885:10542"` or
`get_node` to fetch the authoritative text.

---

## API Requirements

Identical to canonical spec § API Dependencies. No new endpoints. The
same `GET /awards/:id` (Postgrest detail query) serves this award.

---

## State Management

Identical to canonical spec § State Management. No new state types.

---

## Success Criteria

Identical to canonical spec § Success Criteria.

---

## Out of Scope

- **Building a separate composable for Top Project Leader**: forbidden
  — the whole point of the parametric Award Detail design is one
  composable serves every award.
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
- [x] Sibling delta-spec `FQoJZLkG_d-iOS-Award-Top-project` ratified
  (establishes the delta-spec template + Slice A/D shipment proof)
- [x] `AwardDetailScreen` composable shipped (commit `06a9f87` +
  follow-on polish + Slice D test backfill `d69a6c8`)
- [x] Q-TP-2 `"%02d"` formatter shipped (commit `9366e39`) —
  guarantees `quantity = 3` renders as `"03"` automatically
- [ ] **Add `DEMO_AWARDS` + `DEMO_DETAILS` row for Top Project Leader**:
  one-row append to `DemoAwardsRepository.kt`. Description text MUST
  be pulled from Figma node `6885:10542` verbatim at impl time.
  Suggested commit: `feat(awarddetail): append Top Project Leader to
  DEMO fixtures`.
- [ ] **Top Project Leader badge image (Slice A equivalent)**: pull
  Picture-Award composite for this frame via
  `mcp__momorph__get_media_files` / `list_media_nodes`. Per the Top
  Project precedent, MoMorph may return null for the composite — in
  that case fall back to downloading BG + wordmark layers separately
  and compositing offline with Python + Pillow (see Top Project Slice
  A in `FQoJZLkG_d/plan.md`). Bundle as
  `app/src/main/res/drawable-mdpi/ic_award_top_project_leader.png`
  and flip `imageUrl` to
  `"android.resource://com.example.aiddproject/drawable/ic_award_top_project_leader"`.
- [ ] **Regression-test append** (Slice D equivalent — small): extend
  `DemoAwardsRepositoryTest.kt` with three assertions for the new
  row (detail-returns-payload, list-includes-Top-Project-Leader,
  list-sorted-by-sort-order updated to expect 4 entries).
  `AwardInfoBlockTest.kt` already covers `quantity = 3 → "03"` if
  the parametric set is extended; otherwise add a `quantity = 3`
  variant. `AwardDetailScreenTest`'s `body_renders_*` series can
  stay as-is — the screen is parametric.

---

## Notes

### Why a delta-spec instead of a full spec

Same reasoning as `FQoJZLkG_d`: Top Project Leader on Android is
**the same screen** as Top Talent, parameterised by award data. The
MoMorph frame system treats every default-rendered award as a separate
frame because the design tool exports one canvas per state, but **on
Android there is one screen**.

The delta-spec keeps the canonical spec as the source of truth and
uses this file purely as a **traceability anchor** for MoMorph's
per-frame test-case IDs (`TC_AWARD_TOP_PL_*`) that otherwise would
have no home.

### Cross-frame test-case mapping

| MoMorph test case | Maps to canonical spec |
|-------------------|------------------------|
| `TC_AWARD_TOP_PL_ACC_001` (authenticated access) | Canonical US1 acceptance scenario 1 + US8 |
| `TC_AWARD_TOP_PL_ACC_002` (unauthenticated → Login) | Canonical US8 |
| `TC_AWARD_TOP_PL_ACC_003` (Awards tab nav) | Canonical US3 + T101 (Phase 8) |
| `TC_AWARD_TOP_PL_ACC_004` (dropdown selects Top Project Leader) | Canonical US2 acceptance scenario 2 |
| `TC_AWARD_TOP_PL_GUI_001` (overall layout) | Canonical US1 + Frame-specific scenario 1 above |
| `TC_AWARD_TOP_PL_GUI_002` (dropdown default = Top Project Leader) | Frame-specific scenario 2 above |
| `TC_AWARD_TOP_PL_FUN_001` (dropdown opens) | Canonical US2 acceptance scenario 1 |
| `TC_AWARD_TOP_PL_FUN_002` (dropdown switches award) | Canonical US2 acceptance scenario 2 + Frame-specific scenario 4 |

### Comparison with sibling delta-specs

| Field | Top Talent | Top Project | **Top Project Leader** |
|-------|-----------|-------------|------------------------|
| `quantity` | 10 | 2 | **3** |
| renders as | "10" | "02" | **"03"** |
| `quantityUnit` | Cá nhân | Tập thể | **Cá nhân** |
| `prizeValue` | 7.000.000 VNĐ | 15.000.000 VNĐ | **7.000.000 VNĐ** |
| New Q-numbers introduced | — | Q-TP-1 + Q-TP-2 | **none** |

The "none" in the last column is the key signal: this delta-spec is
the simplest case so far — a pure data append. The plumbing
(`%02d` formatter, parametric composable, DEMO fixture pattern,
badge composition fallback) is all in place from Top Talent + Top
Project.

### Next likely frames following this pattern

- `[iOS] Award_MVP` (`b2BuS8HYIt`)
- `[iOS] Award_Best Manager` (`7y195PPTxQ`)
- `[iOS] Award_Signature 2025 - Creator` (`O98TwiHaJe`)

Plus the existing `[iOS] Home` already cards Top Heart at slot 3 in
DEMO_AWARDS but no separate frame exists yet for it — when it does,
follow the same delta-spec pattern.
