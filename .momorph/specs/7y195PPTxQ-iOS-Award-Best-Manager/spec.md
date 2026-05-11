# Feature Specification: Award Detail — Best Manager default

**Frame ID**: `7y195PPTxQ`
**Frame Name**: `[iOS] Award_Best Manager`
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

**Sibling delta-specs**:
- `.momorph/specs/FQoJZLkG_d-iOS-Award-Top-project/spec.md` —
  first delta-spec, established the pattern with Q-TP-1 / Q-TP-2 fixes.
- `.momorph/specs/QQvsfK3yaK-iOS-Award-Top-project-leader/spec.md`
  — second delta-spec, validated pure-data-append flow (2 commits,
  ~30 min spec → impl).

**Read the canonical spec first.** Every functional requirement,
technical requirement, success criterion, edge case, state-management
contract, and API dependency from the canonical spec applies here
**unchanged**. This document records only:

1. What is **different** about this frame (default award data).
2. How the existing implementation already covers it (zero new code
   beyond a one-row DEMO append + badge bundle).
3. The frame-specific test cases.

---

## Overview

The Award Detail screen rendered with **Best Manager** as the
default selection. The user reaches it by:

- Selecting "Best Manager" from the category dropdown while already
  on Award Detail.
- Tapping **Chi tiết** on the Best Manager card in Home's awards
  carousel — the route is
  `Routes.awardDetail("<best-manager-uuid>")`. The Home carousel
  must list a Best Manager entry first (see § Dependencies).

**Target users**: same as canonical — authenticated Sunners.

**Implementation platform**: Android only (Kotlin + Compose + M3 +
Supabase). **Single composable**: `AwardDetailScreen` at
`com.example.aiddproject.awarddetail.ui.AwardDetailScreen` — no new
file, no new ViewModel.

---

## What's different from the canonical spec

| Field | Top Talent (`c-QM3_zjkG`) | **Best Manager (this frame)** |
|-------|---------------------------|-------------------------------|
| `award.id` (UUID) | `…000000a01` | `…000000a05` (proposed — implementer's call when appending to DEMO + Supabase) |
| `award.name` (carousel) | "Top Talent Award" | "Best Manager Award" |
| `award.name` (detail body + dropdown) | "Top Talent" | **"Best Manager"** |
| Badge image (Figma) | "TOP TALENT" lockup | **"BEST MANAGER" lockup** (composite via `mm_media_Picture-Award` instance — see § Dependencies) |
| Description | "Vinh danh cá nhân xuất sắc…" | **"Giải thưởng Best Manager vinh danh những nhà lãnh đạo tiêu biểu – người đã dẫn dắt đội ngũ của mình tạo ra kết quả vượt kỳ vọng, tác động nổi bật đến hiệu quả kinh doanh và sự phát triển bền vững của tổ chức. Dưới sự lãnh đạo của họ, đội ngũ luôn chinh phục và làm chủ mọi mục tiêu bằng năng lực đa nhiệm, khả năng phối hợp hiệu quả, và tư duy ứng dụng công nghệ linh hoạt trong kỷ nguyên số. Họ truyền cảm hứng để tập thể trở nên tự tin tràn đầy năng lượng, sẵn sàng đón nhận, thậm chí dẫn dắt tạo ra những thay đổi có tính cách mạng."** (full Figma copy at text node `6885:10616`) |
| `award.quantity` | `10` | **`1`** (renders as `"01"` via the shipped Q-TP-2 `"%02d"` formatter — smallest-non-zero case, exercised by the `quantity = 0` edge test in `AwardInfoBlockTest` already) |
| `award.quantity_unit` | "Cá nhân" | **"Cá nhân"** (same as Top Talent) |
| `award.prize_value` | "7.000.000 VNĐ" | **"10.000.000 VNĐ"** (new value — neither 7M nor 15M; still a pre-formatted string per Resolved Q5) |
| `sort_order` | `1` | `5` (proposed — implementer's call) |

**That is the entire diff.** Everything else — sticky chrome, dropdown
behaviour, Sun\*Kudos Chi tiết destination, auth gate, header chrome,
bottom-nav routing, locale switch, error/retry/null-placeholder
handling, telemetry, Q-TP-2 zero-pad — is **identical** and already
shipped per canonical's tasks T001–T103 + delta-spec FQoJZLkG_d's
Phase 1–6.

**No new Q-numbers**: Best Manager is the second pure-data-append
delta after Top Project Leader. The `quantity = 1` case is the
smallest non-zero single-digit value the dropdown will encounter;
the shipped `"%02d"` formatter handles it identically to
`quantity = 3` (Top Project Leader) and `quantity = 0` (edge test
already in `AwardInfoBlockTest`). No new client work.

---

## User Scenarios

All user stories (US1–US8) from the canonical spec apply unchanged.
The only frame-specific scenarios are mirror cases that exercise the
**Best Manager** payload instead of Top Talent. They are listed
below for traceability against MoMorph's `TC_AWARD_BM_*` test
case IDs (matching the pattern established by `TC_AWARD_TOP_TALENT_*`
and `TC_AWARD_TOP_PROJECT_*` / `TC_AWARD_TOP_PL_*`).

### Frame-specific acceptance scenarios

These are **parametric repeats** of the canonical US1–US3 scenarios
swapped to Best Manager data. No new behaviour.

1. **Mount with Best Manager default (TC_AWARD_BM_GUI_001)**
   - **Given** the user navigates to this frame via the Home carousel
     Chi tiết tap on Best Manager OR selects "Best Manager" from the
     category dropdown,
   - **When** the body resolves to `AwardDetailState.Loaded`,
   - **Then** the dropdown trigger reads "Best Manager", the badge
     image renders the "BEST MANAGER" lockup, the description matches
     Figma node `6885:10616`, the quantity row reads "01 Cá nhân",
     and the prize row reads "10.000.000 VNĐ cho mỗi giải thưởng".

2. **Dropdown default state (TC_AWARD_BM_GUI_002)**
   - **Given** the screen is rendered with Best Manager as the active
     award,
   - **When** the user observes the Highlight block,
   - **Then** the dropdown trigger pill renders "Best Manager" + the
     chevron — matching the design pill chrome from canonical
     US2 + FR-005.

3. **Bottom-nav Awards tab navigation (TC_AWARD_BM_ACC_003)**
   - **Given** the user is on any authenticated screen,
   - **When** the user taps **Awards** in the bottom navigation bar,
   - **Then** Award Detail mounts. The default award is determined
     per canonical FR-001 + Resolved Q1 — last-viewed in-session,
     fallback to first-by-`sort_order`. **Best Manager becomes the
     default only if it was last viewed**; otherwise Top Talent
     remains the default (first by sort_order). The frame's existence
     does NOT change the default-selection rules.

4. **Switch to a different award (TC_AWARD_BM_FUN_002)**
   - **Given** the screen is on Best Manager,
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
canonical spec's `6885:1029x` IDs become this frame's `6885:1058x`–
`6885:1063x` range), but they map 1:1:

| Component | Top Talent node | **Best Manager node** |
|-----------|----------------|-----------------------|
| KV Kudos banner | `6885:10266` | **`6885:10584`** |
| Highlight block | `6885:10283` | **`6885:10601`** |
| Highlight header (sub-label + title + dropdown) | `6885:10284` | **`6885:10602`** |
| Award info — title text | `6885:10297` | **`6885:10615`** |
| Award info — description text | `6885:10298` | **`6885:10616`** |
| Award info — quantity label | `6885:10303` | **`6885:10621`** |
| Award info — quantity value | `6885:10305` | **`6885:10623`** |
| Award info — quantity unit | (n/a — same row as value) | **`6885:10624`** |
| Award info — prize label | `6885:10306` | **`6885:10629`** |
| Award info — prize value | `6885:10311` | **`6885:10631`** |
| Award info — prize caption | (n/a — same row as value) | **`6885:10632`** |

The **behaviour** of every component is identical. The implementer
fetches frame-specific pixel chrome on demand via `query_section`
against these new IDs only if there's a visual divergence (none
documented as of 2026-05-11).

---

## Data Requirements

Identical to the canonical spec's § Key Entities. The `awards` table
needs **one new row appended** to carry the Best Manager entry
(production Supabase + DEMO `DEMO_DETAILS`).

**Proposed DEMO_DETAILS row for Best Manager** (implementer
finalizes UUID + sortOrder when appending):

```kotlin
AwardDetail(
    id = "00000000-0000-0000-0000-000000000a05", // proposed — confirm at impl time
    name = "Best Manager",
    description =
        "<full Figma copy from text node 6885:10616 — pull verbatim " +
            "at implementation time via `mcp__momorph__query_section`>",
    quantity = 1, // renders as "01" via shipped Q-TP-2 %02d formatter
    quantityUnit = "Cá nhân",
    prizeValue = "10.000.000 VNĐ",
    imageUrl = null, // until Slice A bundles the badge composite — see § Dependencies
    sortOrder = 5, // proposed — confirm at impl time
)
```

**Proposed carousel `Award` row in `DEMO_AWARDS`**:

```kotlin
Award(
    id = "00000000-0000-0000-0000-000000000a05",
    name = "Best Manager Award", // " Award" suffix stripped by AwardCategoryDropdown.displayName
    thumbnailUrl = null,
    sortOrder = 5,
)
```

The description text MUST be pulled verbatim from Figma at implementation
time — this spec deliberately does NOT inline the full copy in the DEMO
block above because (a) it is long, and (b) inlining a Figma string risks
drift between spec and source. Use `mcp__momorph__query_section` with
`nodeId = "6885:10616"` to fetch the authoritative text. **The full copy
IS inlined in the diff table above** for spec-reading convenience but
the implementer should treat the Figma node as the source of truth.

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

- **Building a separate composable for Best Manager**: forbidden —
  the whole point of the parametric Award Detail design is one
  composable serves every award.
- **Visual specs**: per Constitution Principle II + canonical spec
  § Out of Scope. The implementer fetches values at task-execution
  time via `query_section` against the per-frame Node IDs above —
  NOT enumerated here. As of 2026-05-11 review the visual chrome is
  identical to Top Talent's, so no extra `query_section` calls are
  needed unless a regression surfaces.
- **A new `[iOS] Award_*` spec for Top Heart, MVP, Signature 2025 —
  Creator**: these will follow the same delta-spec pattern. **Do
  not** author a 750-line duplicate per frame.

---

## Dependencies

- [x] Canonical spec `c-QM3_zjkG-iOS-Award-Top-talent` ratified
- [x] Sibling delta-specs `FQoJZLkG_d-iOS-Award-Top-project` +
  `QQvsfK3yaK-iOS-Award-Top-project-leader` ratified (validate the
  delta-spec template at 2-commit / 30-min pace)
- [x] `AwardDetailScreen` composable shipped (commit `06a9f87` +
  follow-on polish + Slice D test backfill `d69a6c8`)
- [x] Q-TP-2 `"%02d"` formatter shipped (commit `9366e39`) —
  guarantees `quantity = 1` renders as `"01"` automatically. Note:
  `quantity = 0` edge case is already test-covered in
  `AwardInfoBlockTest.renders_00_when_quantity_is_0_edge_case`; this
  spec's `quantity = 1` case is bracketed between 0 (covered) and 2
  (covered by Top Project test), so no new boundary test is needed.
- [ ] **Add `DEMO_AWARDS` + `DEMO_DETAILS` row for Best Manager**:
  one-row append to `DemoAwardsRepository.kt`. Description text MUST
  be pulled from Figma node `6885:10616` verbatim at impl time.
- [ ] **Best Manager badge image (Slice A equivalent)**: pull
  Picture-Award composite for this frame via
  `mcp__momorph__get_media_files` / `list_media_nodes`. Per the Top
  Project precedent, MoMorph may return null for the composite — in
  that case fall back to downloading BG + wordmark layers separately
  and compositing offline with Python + Pillow. Bundle as
  `app/src/main/res/drawable-mdpi/ic_award_best_manager.png` and
  flip `imageUrl` to
  `"android.resource://com.example.aiddproject/drawable/ic_award_best_manager"`.
- [ ] **Regression-test append** (Slice D equivalent — small): extend
  `DemoAwardsRepositoryTest.kt` with assertions for the new row
  (`detail returns best manager payload matching figma node 6885 10616`
  + update `list returns N demo awards` to expect 5 entries).

---

## Notes

### Why a delta-spec instead of a full spec

Same reasoning as the two prior delta-specs: Best Manager on Android
is **the same screen** as Top Talent, parameterised by award data.
The delta-spec keeps the canonical spec as the source of truth and
uses this file purely as a **traceability anchor** for MoMorph's
per-frame test-case IDs (`TC_AWARD_BM_*`).

### Cross-frame test-case mapping

| MoMorph test case | Maps to canonical spec |
|-------------------|------------------------|
| `TC_AWARD_BM_ACC_001` (authenticated access) | Canonical US1 acceptance scenario 1 + US8 |
| `TC_AWARD_BM_ACC_002` (unauthenticated → Login) | Canonical US8 |
| `TC_AWARD_BM_ACC_003` (Awards tab nav) | Canonical US3 + T101 (Phase 8) |
| `TC_AWARD_BM_ACC_004` (dropdown selects Best Manager) | Canonical US2 acceptance scenario 2 |
| `TC_AWARD_BM_GUI_001` (overall layout) | Canonical US1 + Frame-specific scenario 1 above |
| `TC_AWARD_BM_GUI_002` (dropdown default = Best Manager) | Frame-specific scenario 2 above |
| `TC_AWARD_BM_FUN_001` (dropdown opens) | Canonical US2 acceptance scenario 1 |
| `TC_AWARD_BM_FUN_002` (dropdown switches award) | Canonical US2 acceptance scenario 2 + Frame-specific scenario 4 |

### Comparison across all four delta-specs to date

| Field | Top Talent | Top Project | Top Project Leader | **Best Manager** |
|-------|-----------|-------------|--------------------|------------------|
| `quantity` | 10 | 2 | 3 | **1** |
| renders as | "10" | "02" | "03" | **"01"** |
| `quantityUnit` | Cá nhân | Tập thể | Cá nhân | **Cá nhân** |
| `prizeValue` | 7.000.000 VNĐ | 15.000.000 VNĐ | 7.000.000 VNĐ | **10.000.000 VNĐ** |
| New Q-numbers | — | Q-TP-1 + Q-TP-2 | none | **none** |

Best Manager introduces a new `prizeValue` (10M VNĐ) but the field
is still a pre-formatted string per Resolved Q5 — no new behavior.
The `quantity = 1` is the smallest non-zero single-digit count
observed across the demo set; the shipped Q-TP-2 formatter handles
it identically to other single-digit cases.

### Next likely frames following this pattern

- `[iOS] Award_MVP` (`b2BuS8HYIt`)
- `[iOS] Award_Signature 2025 - Creator` (`O98TwiHaJe`)
- A future `[iOS] Award_Top heart` frame if/when MoMorph publishes
  one (today Top Heart only exists in the carousel via `DEMO_AWARDS`,
  no dedicated detail frame).
