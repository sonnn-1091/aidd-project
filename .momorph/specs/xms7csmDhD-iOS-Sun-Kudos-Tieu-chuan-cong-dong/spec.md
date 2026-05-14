# Feature Specification: Sun*Kudos — Tiêu chuẩn cộng đồng (Community Standards)

**Frame ID**: `xms7csmDhD`
**Frame Name**: `[iOS] Sun*Kudos_Tiêu chuẩn cộng đồng`
**File Key**: `9ypp4enmFmdK3YAFJLIu6C`
**Created**: 2026-05-14
**Status**: Draft

---

## Overview

Static, read-only informational screen that publishes Sun*Kudos' Community Standards (10 anti-spam rules) and Security Standards (data-handling commitments + support contact) to Sunners. The screen is reached primarily from the **Viết Kudo composer's** "Tiêu chuẩn cộng đồng" inline link inside the formatting toolbar (per parent spec `7fFAb-K35a`, component B.5), and is the canonical source of truth Sunners can consult before submitting a Kudo to understand what content the system auto-flags as Spam.

**Out-of-scope for this screen**: editing/translating standards (admin tooling lives elsewhere), reporting/flagging UI, the Spam-label moderation pipeline itself — those are separate features.

---

## User Scenarios & Testing *(mandatory)*

### User Story 1 — Read the rules before sending a Kudo (Priority: P1)

A Sunner is composing a Kudo and is unsure whether the message would be auto-flagged. They tap the "Tiêu chuẩn cộng đồng" link in the composer toolbar to read the 10 anti-spam criteria, then return to the composer to revise their draft.

**Why this priority**: This is the single user journey the screen exists to serve. Without this flow, the spam rules are invisible and Sunners have no way to self-correct, which raises support volume and erodes trust in the auto-hide system. The screen is also referenced by acceptance criteria of the parent Viết Kudo spec, so it is required for that feature to ship completely.

**Independent Test**: Open the Viết Kudo screen, tap the toolbar's "Tiêu chuẩn cộng đồng" link, and verify the Community Standards screen renders with both sections (B Community + C Security) and the user can return to the composer with the prior draft intact.

**Acceptance Scenarios**:

1. **Given** I am on the Viết Kudo screen with a partially-filled draft, **When** I tap "Tiêu chuẩn cộng đồng" in the formatting toolbar, **Then** the Community Standards screen opens and shows the title "Tiêu chuẩn chung", the SAA 2025 logo banner, the 10-criterion violation list, and the Security Standards block — without losing my Viết Kudo draft.
2. **Given** I am viewing the Community Standards screen, **When** I tap the back arrow in the top navigation, **Then** I return to the previous screen (Viết Kudo) with my draft and field focus preserved.
3. **Given** I am viewing the Community Standards screen, **When** I press the device system-back gesture/button, **Then** the same back-navigation behavior as the toolbar back arrow occurs.
4. **Given** the Sunner reads the violation list, **When** they scroll down, **Then** they can read all 10 criteria followed by the Security Standards section (Information Security, Sharing Scope, Support Contact via Slack `duong.thi.thuy.an`) without truncation on any supported screen size.

---

### User Story 2 — Discover the rules from any future entry point (Priority: P3)

Future product surfaces (e.g., Settings → About, in-app help, a notification deep-link when content was auto-hidden) link to the Community Standards. The Sunner taps any such entry point and lands on the same screen.

**Why this priority**: P3 because the Viết Kudo entry is the only confirmed entry in the current scope. Designing the screen as a reusable destination from day one means future entry points need no rework on this screen — only an additional `navigate(Routes.COMMUNITY_STANDARDS)` call from the new caller.

**Independent Test**: Trigger navigation to `Routes.COMMUNITY_STANDARDS` from any non-Viết-Kudo entry; verify the screen renders identically and back-navigation returns to the new entry point (not Viết Kudo).

**Acceptance Scenarios**:

1. **Given** I land on the Community Standards screen from any source, **When** the screen renders, **Then** the content is identical regardless of entry point — no per-caller variations.
2. **Given** I arrived from a non-Viết-Kudo entry, **When** I tap back, **Then** I return to that entry, not to Viết Kudo.

---

### Edge Cases

- **Locale switch while open**: User toggles app language while viewing the screen. Strings should refresh to the active locale on next composition; no stale-text mix.
- **System font-scale = 200%**: All text remains readable, no truncation, scroll handles overflow.
- **TalkBack flow**: Screen reader announces the title, then each of the 10 list items as a list, then the Security Standards block — in source order.
- **Very narrow screens / split-screen**: Logo banner and text columns reflow without horizontal scroll.
- **No network**: The screen is fully static (no API), so airplane-mode/offline use MUST work.
- **Deep link from a hidden Kudos notification** (future): A push notification announcing "Kudo của bạn đã bị ẩn vì vi phạm tiêu chuẩn" should be able to deep-link straight here.

---

## UI/UX Requirements *(from Figma)*

### Screen Components

| Component | Node ID | Description | Interactions |
|-----------|---------|-------------|--------------|
| Top navigation — back icon | `6885:10824` / `6885:10825` | "✏️ Left Accessory" frame containing the back-arrow icon. | **Tap**: pop back-stack to the entry screen. Minimum 48×48dp touch target. |
| Top navigation — title | `6885:10815` / `6885:10816` | Static text "Tiêu chuẩn chung" (Note: the screen's heading inside section B is "Tiêu chuẩn cộng đồng" — the **app-bar title** is the more generic "Tiêu chuẩn chung" because the screen carries both Community + Security blocks). | None — read-only. |
| A — Logo banner | `6885:10829` | "ROOT FURTHER" SAA 2025 branding image (`mm_media_Artboard 4@2x 1`). | None — decorative static image. |
| B — Community Standards section | `6885:10848` | Title "Tiêu chuẩn cộng đồng" + intro paragraph + warning paragraph + ordered 10-item list of violations. | None — read-only. |
| B.intro | `6885:10851` | Intro paragraph explaining the purpose of standards. | None. |
| B.body | `6885:10852` | Warning + 10 criteria. Renders as a single text block in Figma; implementation MUST render the 10 criteria as a numbered list for a11y semantics + visual scannability. | None. |
| C — Security Standards section | `6885:10854` | Title "Tiêu chuẩn bảo mật" + 4 paragraphs (commitment, Information Security, Sharing Scope, Support Contact). | None — read-only. **NOTE**: the "Slack duong.thi.thuy.an" mention is text-only; the in-scope MVP does NOT linkify it (Slack deep-link is a P3 future enhancement). |

### Navigation Flow

- **From**:
  - **Primary (P1)**: Viết Kudo composer (`7fFAb-K35a`) → tap inline link "Tiêu chuẩn cộng đồng" in component B.5 of the formatting toolbar row.
  - **Future (P3)**: Settings / About / notification deep-link → tap "Tiêu chuẩn cộng đồng".
- **To**:
  - The previous screen, via the top-nav back arrow OR the device system-back gesture.
- **Triggers**: Back-arrow tap; system back.
- **No other navigation destinations.** The screen is a leaf in the navigation graph.

### Visual Requirements

- Layout MUST reflow without horizontal scroll across phone widths (and degrade gracefully on foldable / tablet form factors). No hard-coded pixel widths for the text columns. (Constitution III asks for `WindowSizeClass`-driven layouts; this is non-binding for the static text body here, where simple intrinsic widths suffice.)
- The full body MUST be vertically scrollable on devices where content exceeds the viewport (most phones).
- Pixel-level visuals (colors, spacing, typography tokens, image asset, gradient overlay) are NOT specified here — the implementer fetches them via `query_section` / `get_media_files` against the listed Node IDs at implementation time. This is per the spec-template convention: behavior here, CSS at implementation.
- Accessibility: minimum 48×48dp touch target on the back arrow; meaningful `contentDescription` on the back icon (localized "Quay lại" / "Back"); list items announced as a list to TalkBack; respect system font scaling; meet WCAG 2.1 AA contrast (Constitution III).
- Light + dark theme support via `MaterialTheme` tokens.

---

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST render the screen at navigation route `Routes.COMMUNITY_STANDARDS` (already referenced from `AppNavigation.kt` and currently bound to a placeholder; this spec replaces the placeholder).
- **FR-002**: System MUST display, in source order: (a) the SAA 2025 logo banner image, (b) the Community Standards block (title + intro + warning + 10 numbered criteria), (c) the Security Standards block (title + commitment + Information Security + Sharing Scope + Support Contact).
- **FR-003**: Users MUST be able to dismiss the screen via either the top-nav back arrow OR the system back gesture/button — both MUST pop the back-stack to the calling screen.
- **FR-004**: The screen MUST NOT mutate any application state. Navigating to it from Viết Kudo and back MUST preserve the composer's draft, field focus, scroll position, and validation state.
- **FR-005**: All text content (title, intro, warning, 10 criteria, all Security paragraphs, back-arrow a11y label) MUST be sourced from string resources (`strings.xml`) and be localizable. The current shipped locale set is VN + EN — VN is the canonical source per parent Viết Kudo spec; EN translations to be authored before EN GA. Until EN is authored, the Android resource-resolution algorithm transparently falls back from `values-en/strings.xml` to `values/strings.xml` (VN canonical) for any missing key — no app-code change required.
- **FR-006**: The 10-criterion list MUST be rendered with list semantics (`<ol>` equivalent / Compose `Modifier.semantics { liveRegion = ... }` is NOT needed; standard list semantics suffice) so TalkBack announces "List, 10 items" before reading items.
- **FR-007**: The screen MUST be functional offline. No network calls, no loading states, no error states required.
- **FR-008**: The back-arrow content description MUST identify the destination implicitly (label "Quay lại" / "Back" is sufficient; no need to name the destination screen).

### Technical Requirements

- **TR-001**: Performance — first frame paint after navigation MUST feel perceptually instant on a mid-tier device, with NO async work (network / database / disk I/O beyond resource lookup) during composition. The screen is pure-static so this is trivial to meet; flagging it explicitly to deter accidental Supabase reads or initializer-blockers being added to the route.
- **TR-002**: Security — No PII, no auth tokens, no Supabase reads. The Slack handle in section C (`duong.thi.thuy.an`) is publicly-published BTC SAA contact and is safe to display in plaintext. No deep-linking to Slack from this MVP.
- **TR-003**: Integration — Route key `Routes.COMMUNITY_STANDARDS` already exists in `navigation/Routes.kt` and is wired in `AppNavigation.kt` to a `PlaceholderScreen`. Implementation replaces the placeholder with the real composable; no nav-graph schema change required.
- **TR-004**: Accessibility — Constitution III. All checks above must be testable via the `androidx.compose.ui.test` semantics harness; back-arrow must be reachable via TalkBack as the first focusable element.
- **TR-005**: Constitution alignment — feature-first package layout: `com.example.aiddproject.kudos.standards.ui.CommunityStandardsScreen` + `...standards.ui.CommunityStandardsContent` (stateless content composable). No ViewModel needed since the screen is read-only static content driven by string resources.

### State Management

This subsection is intentionally exhaustive so a planner reading only this spec knows there is nothing to design.

- **Local component state**: NONE beyond Compose's intrinsic scroll position (`rememberScrollState()` for the body). No `remember { mutableStateOf(...) }` for content — all content is read from string resources at composition time.
- **Global / hoisted state**: NONE. No ViewModel is required; the screen is a stateless content composable + a thin entry composable that wires the system back-handler.
- **Loading state**: NONE. The screen has no asynchronous work to perform; nothing to "load".
- **Error state**: NONE. With no network calls, there is no failure mode to surface. (Render-time exceptions are caught by Compose's top-level crash handler, not by this screen.)
- **Empty state**: NONE applicable. The content is fixed and always non-empty.
- **Cache invalidation**: N/A. Nothing is cached.
- **Configuration-change resilience**: Scroll position MUST be preserved across rotation / dark-mode toggle / locale change (achieved automatically via `rememberSaveable` if needed; phone-only MVP can rely on `rememberScrollState` since rotation is rare here).
- **Inter-screen state**: Navigating here from Viết Kudo MUST NOT clear the composer's draft, field focus, scroll position, or validation state on return — see FR-004.

### Key Entities *(if feature involves data)*

The screen has **no domain entities**. The 10 criteria are a constant content blob expressed entirely through localized string resources:

- `community_standards_title_appbar` → "Tiêu chuẩn chung"
- `community_standards_section_community_title` → "Tiêu chuẩn cộng đồng"
- `community_standards_section_community_intro` → 1 paragraph
- `community_standards_section_community_warning` → 1 paragraph
- `community_standards_criteria_1` … `community_standards_criteria_10` → individual list items
- `community_standards_section_security_title` → "Tiêu chuẩn bảo mật"
- `community_standards_section_security_commitment`
- `community_standards_section_security_info`
- `community_standards_section_security_scope`
- `community_standards_section_security_support`
- `a11y_community_standards_back` → "Quay lại"

(Implementer may consolidate or split these names to match the existing `strings.xml` conventions; the names above are illustrative.)

---

## API Dependencies

| Endpoint | Method | Purpose | Status |
|----------|--------|---------|--------|
| *(none)* | — | The screen renders entirely from local string resources and a bundled drawable for the logo banner. No backend reads, writes, or auth checks. | N/A |

If a future iteration moves the standards to a remote CMS (so legal/BTC can update them without a release), this section will gain a single `GET /community_standards/latest` predicted endpoint — but that is explicitly **out of scope** for the MVP.

---

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 100% of taps on the Viết Kudo "Tiêu chuẩn cộng đồng" link (B.5, Node `6885:9303`) successfully render this screen — zero nav crashes, zero placeholder fallbacks — in the QA build and in production telemetry once shipped.
- **SC-002**: Back-navigation from Community Standards → Viết Kudo feels perceptually instant (no visible spinner, no skeleton) and the composer's draft, field focus, and scroll position are bit-for-bit identical to pre-navigation state. Verified by an instrumented UI test.
- **SC-003**: TalkBack walk-through (sighted facilitator + screen-reader user pair) completes in a single attempt — 0 navigation errors, 0 misannounced list items — during the QA gate.
- **SC-004**: Localized VN text matches the Figma source word-for-word at GA (manual review checklist signed off by the spec author).

---

## Out of Scope

- **Editing / authoring of standards content** — handled by future admin tooling, not by this client screen.
- **Slack deep-link** on the support contact line. The Slack handle is plaintext for MVP.
- **Linkifying example fragments** (e.g., turning "30 ký tự" into a clickable definition). MVP renders all body text as plain typography.
- **Tracking which Kudo content tripped which criterion** — that telemetry belongs to the Spam-label pipeline, not this read-only screen.
- **A per-user "I acknowledge" gate** before a Sunner can send their first Kudo. MVP does not gate sending; reading is voluntary.
- **Remote / CMS-driven content** — see API Dependencies note above.

---

## Dependencies

- [x] Constitution document exists (`.momorph/constitution.md`) — feature-first packaging, M3 components, a11y rules apply.
- [ ] API specifications available (`.momorph/API.yml`) — N/A (no APIs).
- [ ] Database design completed (`.momorph/database.sql`) — N/A (no DB reads).
- [x] Screen flow documented (`.momorph/SCREENFLOW.md`) — row for this screen added in the same change-set as this spec.
- [x] Parent Viết Kudo spec exists (`specs/7fFAb-K35a-iOS-Sun-Kudos-Viet-Kudo/spec.md`) — defines the primary entry-point link in component B.5.
- [x] Route key `Routes.COMMUNITY_STANDARDS` exists in `navigation/Routes.kt`.

---

## Notes

- **Title mismatch (intentional)**: The Figma top-nav title text is "**Tiêu chuẩn chung**" (general standards) while section B's heading is "**Tiêu chuẩn cộng đồng**" (community standards) and section C is "**Tiêu chuẩn bảo mật**" (security standards). The app-bar title is correctly the umbrella label because both sections live on the same screen — implementer SHOULD NOT "fix" this to match the Viết Kudo composer link wording.
- **List rendering of criterion 7**: Figma's text node embeds an inline example fragment (`"Cảm ơn nhiều"`, `"Thanks nhé"`, `"Good job!"`) inside criterion 7. Per the `list_design_items` payload these examples are not visible (they appear in the raw text-node export but not in the design-item description). Implementation MUST preserve them — they're load-bearing because they tell Sunners what "quá ngắn, không có ngữ cảnh" concretely means.
- **Static screen, no ViewModel**: Because the screen has zero dynamic state, a ViewModel would be ceremony with no payoff. The stateless content composable + a thin entry composable that wires the back-handler is the right shape.
- **Localization handoff**: VN copy is canonical at GA. EN translations are NOT required to ship the screen; the app already gracefully falls back to VN via `Language.fromCode` when an EN string is missing. EN authoring is tracked separately.
- **No tag in Figma**: This frame is not flagged "Spec Created" in MoMorph yet; uploading this spec via `mcp__momorph__upload_specs` (a follow-up step outside this generation) should flip that tag.
- **No MoMorph test cases on this frame** (`get_frame_test_cases` returned `[]` at 2026-05-14). The Acceptance Scenarios in User Story 1 and User Story 2 are therefore the **authoritative behavioral test contract** for this screen — they should be the source the QA agent reads when generating Compose UI tests / Espresso checks. If MoMorph test cases are authored later, they MUST be cross-checked against these scenarios to avoid drift.
