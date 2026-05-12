# Sun*Kudos — UI Deviation Report

**Frame**: `fO0Kt19sZZ-iOS-Sun-Kudos`
**Date**: 2026-05-12
**Method**: Section-by-section visual diff between
`assets/frame.png` (Figma export) and emulator screenshots
`assets/verify_01_top.png` … `verify_05_bottom.png` (Pixel_10_Pro AVD).

---

## Deviation summary

| # | Section | Severity | Issue | Status |
|---|---|---|---|---|
| D1 | Global background | **High** | KV PNG bleeds through ALL scrolled content (not just the hero region) — bottom sections still show orange waves behind cards | **Fixed** in T114 |
| D2 | Hero | Medium | KUDOS wordmark text-only — Figma uses a vector wordmark with custom letter spacing | Open — Phase 14 polish |
| D3 | Carousel cards | Low | Card width may be slightly narrower than Figma (273dp vs my fillMaxWidth - 32dp peek) | Open — within tolerance |
| D4 | Spotlight search | Low | Search input is inert (search treatment is baked into image) — interactive search deferred | Open — design intent |
| D5 | Stats panel | Low | Stat labels match the Figma "Số Kudos bạn nhận được:" / "Số Kudos bạn đã gửi:" / "Số tim bạn nhận được:" / "Số Secret Box bạn đã mở:" / "Số Secret Box chưa mở:" wording vs the shorter labels we ship ("Kudos nhận:" etc.) | Open — Phase 14 polish |
| D6 | Top 10 | Low | Avatar circles are placeholder gray; Figma shows actual avatar artwork | Open — needs avatar source |
| D7 | Feed card timestamp | Low | We render ISO timestamp (`2026-05-12T10:00:00Z`); Figma renders human-readable (`10:00 - 10/30/2025`) | Open — Phase 14 polish |
| D8 | Send Kudos pill | Low | Figma has slight ▾ chevron beside the label (not visible in current render) — verify | Open — recheck |

---

## D1 — Global KV background bleed (HIGH) — ✅ FIXED

### Symptom (pre-fix)
After scrolling past the hero, the colorful KV background image was still visible behind:
- The carousel
- The Spotlight section header
- The All Kudos block (Stats, Top 10, Feed cards)

### Root cause
`KudosScreenContent` painted the `Image(kudos_kv_bg)` as a screen-wide layer at the top 360dp. LazyColumn cells stacked on top of that without their own backgrounds, so the KV bled through whenever scrolled content overlapped the top 360dp band.

### Fix applied
- Removed the global `Image` + gradient overlay from `KudosScreenContent`'s outer Box.
- `KudosHeroBanner` now renders the KV PNG as its OWN local background (drawn behind hero text/logo, clipped to the section bounds at 220dp tall).
- LazyColumn below the hero now sees only the screen's `#00070C` background.
- Verified across 5 scroll positions: clean dark navy below the hero, no bleed.

---

## Other observations (informational)

- Section header chrome ("Sun* Annual Awards 2025" caption + divider + cream title) renders consistently across HIGHLIGHT KUDOS, SPOTLIGHT BOARD, ALL KUDOS, TOP 10 — matches Figma.
- Carousel infinite-loop + prev/next chevrons working correctly.
- Spotlight image renders at correct aspect ratio with all baked content visible (388 KUDOS / Sunner cloud / activity ribbon).
- All Kudos block order is correct: header → Stats → Top 10 → Feed → View All.
- Anonymous sender row (D7 area) correctly renders "Người bí ẩn" instead of the sender's full name (Q-K-3).
