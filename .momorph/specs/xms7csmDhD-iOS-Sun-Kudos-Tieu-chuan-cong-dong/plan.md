# Implementation Plan: Sun*Kudos — Tiêu chuẩn cộng đồng (Community Standards)

**Frame**: `xms7csmDhD-iOS-Sun-Kudos-Tieu-chuan-cong-dong`
**Date**: 2026-05-14
**Spec**: `specs/xms7csmDhD-iOS-Sun-Kudos-Tieu-chuan-cong-dong/spec.md`

---

## Summary

Replace the existing `PlaceholderScreen(label = "Community Standards")` at the `Routes.COMMUNITY_STANDARDS` destination with the real, fully-static Community Standards screen specified by Figma frame `xms7csmDhD`.

Primary requirement (P1, from spec): a Sunner taps "Tiêu chuẩn cộng đồng" in the Viết Kudo formatting toolbar (B.5, Node `6885:9303`), reads the 10 anti-spam criteria + the Security Standards block, and taps back — recovering their composer draft, focus, and scroll position bit-for-bit.

**Technical approach**: a single stateless content composable + a thin screen entry composable that wires the system back-handler. **No ViewModel, no repository, no Supabase reads, no asynchronous work, no new dependencies.** All copy lives in `strings.xml` (VN canonical) so localization is mechanical. One new drawable (the KV banner) is downloaded from Figma at implementation time via `get_media_files` against Node `6885:10830`.

This is the smallest meaningful unit of work that exists in the kudos package — call it ~1 day of work for one engineer including tests.

---

## Technical Context

**Language/Framework**: Kotlin / Jetpack Compose
**Primary Dependencies**: `androidx.compose.material3`, `androidx.compose.foundation`, `androidx.activity.compose` (`BackHandler`), `androidx.navigation.compose` — all already on the classpath via `libs.versions.toml` + the Compose BOM
**Database**: N/A (no DB reads)
**Testing**: JUnit 4 (unit) + `androidx.compose.ui.test` semantics harness (instrumented UI) — both already used by the kudos package
**State Management**: NONE beyond intrinsic `rememberScrollState()` (per spec State Management subsection)
**API Style**: N/A — zero network/Postgrest/Storage operations

---

## Constitution Compliance Check

*GATE: Must pass before implementation can begin. Each item maps to a principle in `.momorph/constitution.md`.*

- [x] **I. Clean Code & Source Organization** — new files placed feature-first under `com.example.aiddproject.kudos.standards.ui.*`. Composables stay well under the 150 LOC ceiling: estimated `CommunityStandardsScreen` ~20 LOC, `CommunityStandardsContent` ~170 LOC (the Content composable is just above the threshold because of the 3 private section sub-composables + light/dark `@Preview` wrappers; each individual composable function is small — the public `CommunityStandardsContent` body itself is <60 LOC). Kotlin official style enforced via existing project ktlint config. No new abstractions: the screen is a leaf in the navigation graph.
- [x] **II. Tech Stack Best Practices** — no mutability, no coroutines (nothing to await), no `Flow` plumbing (no state to observe). M3 Compose top-app-bar + Surface + Column primitives used directly. No new dependencies (no `libs.versions.toml` edit needed). No Supabase client touched.
- [x] **III. Material Design 3 (Android)** — `CenterAlignedTopAppBar` (or `TopAppBar`) from `androidx.compose.material3`. `MaterialTheme` tokens for color/typography (light + dark via existing `KudosTheme` / app theme). Back arrow uses `Icons.AutoMirrored.Filled.ArrowBack` (already standard in `KudosScreen`). 48dp touch target on the back IconButton (Material default already meets this; verified). Body text respects system font scaling. `WindowSizeClass` not bound here per spec relaxation — body is a single flowing Column that reflows naturally.
- [x] **IV. OWASP Secure Coding** — no secrets, no PII, no auth, no network. The Slack handle (`duong.thi.thuy.an`) is publicly-published BTC SAA contact. No deep-links to external apps in MVP. No threat model needed — the only attack surface is the back-handler, which goes through standard nav-graph.
- [x] **V. Test-Driven Development** — instrumented Compose UI test authored FIRST (asserts top-bar title, 10 list items by semantics, Security block visibility, back-arrow navigates back). Unit tests not applicable (no domain/data code).

**Violations (if any)**: NONE.

| Violation | Justification | Alternative Rejected |
|-----------|---------------|---------------------|
| *(none)* | — | — |

---

## Architecture Decisions

### Frontend Approach

- **Component Structure**: Two composables, both in the new `kudos/standards/ui/` package — matching the established Write-Kudo pattern (`WriteKudoScreen` + `WriteKudoScreenContent`) verified at `app/src/main/java/com/example/aiddproject/kudos/compose/ui/`:
  - `CommunityStandardsScreen.kt` — thin entry composable. Takes `onNavigateBack: () -> Unit`. Wires `androidx.activity.compose.BackHandler { onNavigateBack() }` so the system-back gesture routes to the same callback (spec FR-003). Delegates all rendering to `CommunityStandardsContent(onBack = onNavigateBack, modifier = modifier)`. Hilt-free (no ViewModel).
  - `CommunityStandardsContent.kt` — stateless content composable. **Hosts the `Scaffold`** (per the established pattern — `WriteKudoScreenContent` does the same) with: `topBar = CenterAlignedTopAppBar(title = "Tiêu chuẩn chung", navigationIcon = IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.a11y_community_standards_back)) })`. The Scaffold's body is a `Column` with `verticalScroll(rememberScrollState())` containing 3 private section composables: `KvBanner`, `CommunityStandardsSection`, `SecurityStandardsSection`. Takes only `onBack: () -> Unit` and `Modifier` — no state. Two `@Preview` decorators (light + dark) for Android Studio visual regression.

  Why split: matches the established Write-Kudo pattern so the test harness can render `CommunityStandardsContent` directly with a captured `onBack` lambda, no Hilt or nav-graph plumbing in unit-level UI tests.

- **Styling Strategy**: M3 theme tokens only (`MaterialTheme.colorScheme`, `MaterialTheme.typography`). The cream form-card / gold-accent palette already exists in the kudos theme (see `WriteKudoScreenContent` for precedent). Pixel-level Figma styles (colors, spacing, the gradient overlay at Node `6885:10807`) are fetched at implementation time via `query_section` on the listed Node IDs — NOT pre-encoded in this plan.

- **Data Fetching**: NONE.

- **Localization**: copy goes into `app/src/main/res/values/strings.xml` (VN canonical) with the `community_standards_*` prefix proposed by the spec. EN translations deferred to a separate task; Android's resource-resolution algorithm transparently falls back from `values-en/` to `values/` for any missing key (spec FR-005).

### Backend Approach

N/A. Zero backend work for this feature.

### Integration Points

- **Existing services**: NONE — no repositories, no use cases, no Supabase calls.
- **Shared components**: `Icons.AutoMirrored.Filled.ArrowBack` (Material icons, already used in `KudosScreen` and `WriteKudoScreenContent`). The KV background pattern (`bg_home` PNG + 140dp top gradient) — optional reuse if the design calls for the same chrome behind the cream content card; check the Figma frame's background nodes (`6885:10807`, `6885:10808`, `6885:10831`) at implementation time.
- **API contracts**: NONE.
- **Navigation graph**: existing `composable(Routes.COMMUNITY_STANDARDS) { PlaceholderScreen(label = "Community Standards") }` at `AppNavigation.kt:188` is REPLACED in-place by `composable(Routes.COMMUNITY_STANDARDS) { CommunityStandardsScreen(onNavigateBack = { navController.popBackStack() }) }`. No `Routes.kt` change needed; route key already exists.

---

## Project Structure

### Documentation (this feature)

```text
.momorph/specs/xms7csmDhD-iOS-Sun-Kudos-Tieu-chuan-cong-dong/
├── spec.md              # Feature specification (exists, reviewed twice)
├── plan.md              # This file
├── tasks.md             # Generated next by /momorph.tasks
└── (research.md)        # NOT NEEDED — feature is trivial, no codebase research required
```

### Source Code (affected areas)

```text
app/src/main/java/com/example/aiddproject/kudos/standards/                NEW PACKAGE
└── ui/
    ├── CommunityStandardsScreen.kt        NEW — thin entry composable: BackHandler + delegates to Content (no Scaffold)
    └── CommunityStandardsContent.kt       NEW — stateless content: Scaffold + top-app-bar + 3 section composables

app/src/main/java/com/example/aiddproject/navigation/
└── AppNavigation.kt                       MODIFIED — replace 1 PlaceholderScreen line with real screen

app/src/main/res/values/
└── strings.xml                            MODIFIED — append community_standards_* keys (~20 entries; full breakdown in the Modified Files table below)

app/src/main/res/drawable-xxhdpi/                                          NEW ASSET (PNG per existing project convention — bg_home.png, bg_keyvisual.png live here)
└── img_community_standards_kv.png         NEW — downloaded from Figma via get_media_files

app/src/androidTest/java/com/example/aiddproject/kudos/standards/         NEW PACKAGE
└── ui/
    └── CommunityStandardsScreenTest.kt    NEW — Compose UI test (5–6 assertions)
```

No unit-test directory needed — there is no domain or data layer to test.

### New Files

| File | Purpose | Estimated LOC |
|------|---------|---------------|
| `kudos/standards/ui/CommunityStandardsScreen.kt` | Thin entry composable; `BackHandler` + delegates to `CommunityStandardsContent`. No Scaffold here. | ~20 |
| `kudos/standards/ui/CommunityStandardsContent.kt` | Stateless content composable; hosts the `Scaffold` + M3 top-app-bar + scrollable Column with `KvBanner` + `CommunityStandardsSection` + `SecurityStandardsSection` private section composables + light/dark `@Preview` wrappers. | ~170 |
| `res/drawable-xxhdpi/img_community_standards_kv.png` | KV banner asset (Figma Node `6885:10830`). PNG to match the existing `bg_home.png` / `bg_keyvisual.png` convention. | binary |
| `androidTest/.../CommunityStandardsScreenTest.kt` | UI + a11y + nav tests | ~80 |

### Modified Files

| File | Change |
|------|--------|
| `navigation/AppNavigation.kt:188-190` | Swap `PlaceholderScreen(label = "Community Standards")` for `CommunityStandardsScreen(onNavigateBack = { navController.popBackStack() })`. Add import. |
| `res/values/strings.xml` | Append ~20 string resources under a single comment-delimited block: app-bar title + 2 section titles + community intro + community warning + 10 numbered criteria + 4 security paragraphs + back-arrow a11y label. See spec § Key Entities for the canonical key names. Locale folders `values-en/` and `values-ja/` already exist on disk — leave them untouched in this PR; the Android resource-resolution algorithm falls back from missing keys in `values-en/` or `values-ja/` to `values/` automatically. EN/JA translation authoring is a separate downstream task. |

### Dependencies

| Package | Version | Purpose |
|---------|---------|---------|
| *(none — zero new dependencies)* | — | All required APIs (M3 Scaffold, BackHandler, Icons.AutoMirrored, navigation-compose) are already on the classpath. |

---

## Implementation Strategy

### Phase Breakdown

#### Phase 0 — Asset Preparation (~30 min)

- Pull the KV banner from Figma via `get_media_files` against frame `xms7csmDhD`, specifically Node `6885:10830` ("mm_media_Artboard 4@2x 1"). Save as `app/src/main/res/drawable-xxhdpi/img_community_standards_kv.png` — PNG format and `xxhdpi` density to match the existing project convention (`bg_home.png`, `bg_keyvisual.png` both live in `drawable-xxhdpi/`). If file size is concerning, generate `-mdpi` + `-xhdpi` variants too; for a one-shot KV image, `xxhdpi` alone is sufficient.
- While in Figma, also note the background nodes (`6885:10807` bg group, `6885:10808` MM_MEDIA_Keyvisual BG, `6885:10809` Shadow Left, `6885:10831` Cover) — these may be reproducible in Compose with `Brush.verticalGradient` + `MaterialTheme.colorScheme` tokens (see `WriteKudoScreenContent`'s `HeaderGradient` for precedent) instead of downloaded image assets. Resolve at implementation time via `query_section` against the surrounding parent frame.
- If the KV BG (`6885:10808`) is the same `bg_home.png` already in the project, REUSE that drawable rather than downloading a duplicate. To verify: download the Figma asset to a tmp path and run `diff` (or `cmp`) against `app/src/main/res/drawable-xxhdpi/bg_home.png`; if identical, delete the tmp file and reference `R.drawable.bg_home` in the new screen.
- Verify asset names match `img_community_standards_*` snake_case convention already used elsewhere in the project (`bg_home`, `bg_keyvisual`, `kudos_avatar_*`).

#### Phase 1 — Foundation (~1 h)

- Create new package directories: `kudos/standards/ui/` (main + androidTest).
- Append the ~20 string resources to `strings.xml` with VN copy lifted verbatim from Figma text nodes. Source-node → string-key mapping:
  - Node `6885:10816` ("Tiêu chuẩn chung") → `community_standards_title_appbar`
  - Node `6885:10849` ("Tiêu chuẩn cộng đồng") → `community_standards_section_community_title`
  - Node `6885:10851` (intro paragraph) → `community_standards_section_community_intro`
  - Node `6885:10852` (warning + 10 criteria, ONE text block in Figma) → split into 11 keys: `community_standards_section_community_warning` + `community_standards_criteria_1` … `_10`
  - Node `6885:10855` ("Tiêu chuẩn bảo mật") → `community_standards_section_security_title`
  - Node `6885:10857` (commitment + Information Security + Sharing Scope, ONE text block) → split into 3 keys: `community_standards_section_security_commitment`, `_info`, `_scope`
  - Node `6885:10859` (support contact) → `community_standards_section_security_support`
  - **NEW key not from Figma**: `a11y_community_standards_back` = "Quay lại" (back-arrow contentDescription per FR-008)
  - Total: 1 + 1 + 1 + 11 + 1 + 3 + 1 + 1 = **20 keys**.
  - **Critical**: criterion 7 in Node `6885:10852` embeds inline example fragments (`"Cảm ơn nhiều"`, `"Thanks nhé"`, `"Good job!"`) — these MUST be preserved verbatim. Without them, "quá ngắn, không có ngữ cảnh" is meaningless.
  - The single multi-line text block at Node `6885:10852` MUST be split into 11 separate string resources: 1 for the warning paragraph + 10 for the numbered criteria. This is FR-006 (list semantics).
- Write the failing Compose UI test FIRST (Constitution V). The test asserts:
  - Top-app-bar title text is "Tiêu chuẩn chung".
  - Back-arrow IconButton is present with `contentDescription` "Quay lại" and is the first focusable element.
  - The 10 criteria are rendered as a list (each as a separate testTag-able row or via `semantics { collectionInfo = ... }`).
  - The Security Standards block renders all 4 paragraphs.
  - Tapping the back arrow invokes the `onNavigateBack` callback exactly once.
  - System back press also invokes `onNavigateBack`.

#### Phase 2 — User Story 1 (P1) — Core functionality (~2 h)

- Implement `CommunityStandardsContent.kt` (Scaffold lives here, NOT in the Screen entry):
  - `Scaffold(topBar = { CenterAlignedTopAppBar(...) }) { padding -> ... }` with `containerColor = MaterialTheme.colorScheme.background` (or `Color.Transparent` if the spec implementation surfaces the KV background behind the Scaffold per Figma).
  - `topBar` exposes a `CenterAlignedTopAppBar` with `title = { Text(stringResource(R.string.community_standards_title_appbar)) }` and `navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.a11y_community_standards_back)) } }`. (The M3 `IconButton` already provides a 48dp touch target — verified in M3 source — so no manual sizing needed for TR-004.)
  - Body: `Column(modifier = Modifier.padding(padding).verticalScroll(rememberScrollState()))` containing 3 private section composables (`KvBanner`, `CommunityStandardsSection`, `SecurityStandardsSection`) in source order.
  - The 10-criterion list goes inside `CommunityStandardsSection`. Render it as a `Column` of 10 `Row`s; wrap the whole list in `Modifier.semantics(mergeDescendants = false) { collectionInfo = androidx.compose.ui.semantics.CollectionInfo(rowCount = 10, columnCount = 1) }` and each item in `Modifier.semantics { collectionItemInfo = CollectionItemInfo(rowIndex = i, rowSpan = 1, columnIndex = 0, columnSpan = 1) }`. This satisfies FR-006 (TalkBack announces "List, 10 items"). Verified API surface: `androidx.compose.ui.semantics.{collectionInfo, collectionItemInfo, CollectionInfo, CollectionItemInfo}`.
  - Two `@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_NO)` + `@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES)` decorators on a `CommunityStandardsContentPreview()` wrapper so light + dark visual regressions are catchable in Android Studio without launching the app.
- Implement `CommunityStandardsScreen.kt` (the THIN entry, no Scaffold):
  - Single composable `fun CommunityStandardsScreen(onNavigateBack: () -> Unit, modifier: Modifier = Modifier)`.
  - `BackHandler { onNavigateBack() }` so system-back routes to the same callback (FR-003).
  - Delegates rendering: `CommunityStandardsContent(onBack = onNavigateBack, modifier = modifier)`.
  - That's it — no Hilt, no ViewModel, no state hoisting.
- Wire the screen into `AppNavigation.kt`: replace the `PlaceholderScreen(label = "Community Standards")` body at line 188-190 with `CommunityStandardsScreen(onNavigateBack = { navController.popBackStack() })`. Add the `import com.example.aiddproject.kudos.standards.ui.CommunityStandardsScreen`.
- Run the failing test — it should now pass.

#### Phase 3 — User Story 2 (P3) — Reusable entry (~30 min)

- Verify (don't add new code): the screen takes only `onNavigateBack: () -> Unit` and reads nothing from caller context, so it is already reusable from any future entry point. No changes required.
- Add a brief doc-comment on `CommunityStandardsScreen` noting this contract.

#### Phase 4 — Polish (~1 h)

- TalkBack walk-through on a physical device (or emulator with TalkBack enabled) covering all 4 acceptance scenarios in spec US1.
- 200% system font scale check — confirm no truncation, scroll handles overflow.
- Locale toggle while on screen — confirm strings refresh on next composition (Compose handles this automatically when the system Configuration changes; no app-code change needed).
- Visually review the light + dark `@Preview` decorators added in Phase 2 (rendered in Android Studio's Preview pane) against the Figma frame; flag any deltas as Phase 4 fixups, not new tasks.
- Tag the Figma frame `xms7csmDhD` with "Spec Created" via `mcp__momorph__upload_specs` once the spec is ratified (out-of-band, not blocking).

### Risk Assessment

| Risk | Probability | Impact | Mitigation |
|------|-------------|--------|------------|
| KV banner asset is huge or wrong density, bloating APK | Medium | Low | Default to a single `-xxhdpi` PNG (matches existing `bg_home.png` / `bg_keyvisual.png` convention). If the PNG export from Figma exceeds ~200 KB, run it through `pngquant` / `optipng` before committing. Add lower-density variants only if visual fidelity at lower DPIs degrades. |
| Single multi-line text node (`6885:10852`) gets copied verbatim as ONE string resource, defeating list semantics | Medium | Medium | Phase 1 step explicitly calls out the split into 11 keys (warning + 10 criteria). UI test asserts `collectionInfo` → if implementer copies the block verbatim, the test fails. |
| Inline example fragments in criterion 7 get dropped during copy-paste | Medium | Medium | Documented in spec § Notes + this plan's Phase 1 callout. Manual diff against Figma during PR review. |
| Implementer adds an unnecessary ViewModel "for consistency" | Low | Low | Spec § State Management explicitly enumerates NONE for each facet. Plan's Architecture Decisions reiterate "no ViewModel". |
| EN locale needed at GA, blocking release | Low | Medium | Spec FR-005 + Android resource-resolution fallback handles missing keys silently. Add EN translation as a follow-up task, not a blocker. |
| Back-handler double-invokes (toolbar back + system back fire twice) | Low | Low | M3 `Scaffold` + `BackHandler` are independent — pressing one doesn't bubble to the other. Verified by the "tap back" + "press system back" tests being separate cases. |

### Estimated Complexity

- **Frontend**: **Low** — static content, no state, no async work, 2 composables totaling ~190 LOC.
- **Backend**: **N/A** — no backend work.
- **Testing**: **Low** — one instrumented UI test file (~80 LOC), 5–6 assertions, no flaky timing.

**Total estimated effort**: ~4–5 hours including tests and PR.

---

## Integration Testing Strategy

### Test Scope

- [x] **Component/Module interactions**: `CommunityStandardsScreen` ↔ `CommunityStandardsContent` (back callback propagation, scroll state preservation across rotation).
- [ ] **External dependencies**: NONE — feature has none.
- [ ] **Data layer**: NONE — no persistence or cache.
- [x] **User workflows**: Viết Kudo → Community Standards → back → Viết Kudo (composer draft preserved).

### Test Categories

| Category | Applicable? | Key Scenarios |
|----------|-------------|---------------|
| UI ↔ Logic | **Yes** | Back-arrow tap + system-back both invoke `onNavigateBack` exactly once. |
| Service ↔ Service | No | No services involved. |
| App ↔ External API | No | No external APIs. |
| App ↔ Data Layer | No | No persistence. |
| Cross-platform | No | Android-only project (per CLAUDE.md). |
| **Navigation flow** | **Yes** | Round-trip from Viết Kudo composer → Community Standards → back; composer draft, focus, scroll position bit-for-bit identical. Implemented as an instrumented test on the parent feature, NOT this feature, since the parent owns the draft state. Cross-link to that test from this feature's PR description. |
| **A11y** | **Yes** | TalkBack announces "List, 10 items" before reading criteria. Back arrow is the first focusable element. All section titles are readable as headings. |

### Test Environment

- **Environment type**: Local emulator (Pixel 5a API 33+) and physical device sanity check.
- **Test data strategy**: NONE — no data needed; strings are read from resources.
- **Isolation approach**: Each test composes the screen fresh via `setContent { ... }` so state never leaks across cases.

### Mocking Strategy

| Dependency Type | Strategy | Rationale |
|-----------------|----------|-----------|
| `onNavigateBack` callback | Real lambda captured by `mutableStateOf` counter | Verify exact invocation count (single-fire on each gesture). |
| Resource strings | Real | Tests read the actual `strings.xml` to catch missing-key regressions early. |
| Navigation graph | Real (`TestNavHostController` is overkill here) | The feature test renders `CommunityStandardsScreen` directly with a captured callback. The cross-screen round-trip is verified by the parent Viết Kudo test. |

### Test Scenarios Outline

1. **Happy Path**
   - [ ] Top-app-bar title reads "Tiêu chuẩn chung".
   - [ ] KV banner image is composed.
   - [ ] All 10 criteria render in order; semantics expose `collectionInfo.itemCount = 10`.
   - [ ] Security Standards block renders all 4 paragraphs including Slack handle.
   - [ ] Tapping the back arrow invokes `onNavigateBack` exactly once.
   - [ ] System back press invokes `onNavigateBack` exactly once.

2. **A11y**
   - [ ] Back IconButton exposes `contentDescription = "Quay lại"` (read from `strings.xml`).
   - [ ] First focusable element on screen is the back arrow.
   - [ ] 10 criteria each expose a `collectionItemInfo` with the correct `rowIndex`, and the parent list exposes `collectionInfo.rowCount = 10`. (Compose's `Role` enum has no `Row` value — collection semantics are the right contract here, not `Role`.)

3. **Edge Cases**
   - [ ] Configuration change (rotation, dark-mode toggle) preserves scroll position.
   - [ ] 200% font scale: content scrollable, no truncation. (Manual / Android Studio Layout Inspector.)

### Tooling & Framework

- **Test framework**: `androidx.compose.ui.test:ui-test-junit4` (already on classpath via the kudos package's `androidTest` setup).
- **Supporting tools**: `androidx.test.ext:junit`, `androidx.test:runner`. No Hilt rule needed (no DI in this screen).
- **CI integration**: existing Gradle `connectedDebugAndroidTest` task picks up the new test file automatically.

### Coverage Goals

| Area | Target | Priority |
|------|--------|----------|
| Back-navigation paths (both arrow + system) | 100% | High |
| List-semantics correctness | 100% | High |
| String-resource resolution | 100% | High (catches missing-key bugs) |
| Visual regression | Manual `@Preview` review | Medium |

---

## Dependencies & Prerequisites

### Required Before Start

- [x] `constitution.md` reviewed — Principles I-V all addressed in the compliance section above.
- [x] `spec.md` approved (reviewed twice via `/momorph.reviewspecify`).
- [ ] `research.md` — **NOT REQUIRED** for this feature. The codebase patterns are already understood from the parent Viết Kudo work; the change is too small to warrant a research doc.
- [ ] API contracts — N/A (no APIs).
- [ ] Database migrations — N/A.

### External Dependencies

- **Figma access** to file `9ypp4enmFmdK3YAFJLIu6C` for the KV banner asset download (Node `6885:10830`) and the gradient/background tokens at implementation time. The implementer already has authenticated MoMorph MCP access per the project setup.

---

## Next Steps

After plan approval:

1. **Run** `/momorph.tasks` to generate the granular task breakdown from this plan. The expected output is ~8–10 atomic tasks: download banner → add strings → write failing test → implement content composable → implement screen entry → wire AppNavigation → run test → manual a11y check → preview review → ship.
2. **Review** `tasks.md` for ordering. No parallelization is meaningful here — the feature is small and serial.
3. **Begin** implementation in the order tasks.md prescribes. Commit per task per the user's `feedback_commit_per_task` memory.

---

## Notes

- **No `research.md`** is being authored. The plan explicitly skips it because (a) the spec is unambiguous, (b) the codebase patterns for static M3 screens already exist (the `PlaceholderScreen` itself is a working M3 example to crib from, and `KudosScreen`'s top-app-bar / back-handler pattern transfers directly), and (c) the feature is trivial enough that a research detour would burn more time than it saves. If the implementer hits an unexpected codebase quirk during Phase 1, they may pause and author `research.md` then — but I do not anticipate this.
- **No `testcase.md`** either — the spec's Acceptance Scenarios in US1 + US2 are already framed as testable G/W/T statements, and the Integration Testing Strategy in this plan refines them into specific instrumented-test assertions. A separate `testcase.md` would duplicate.
- **No `contract.md`** — feature has zero API contracts.
- **Asset-download note**: the spec deliberately avoids prescribing pixel values, so the Phase 0 step is the implementer's gateway to fetch them. Use `query_section` against the parent frame `xms7csmDhD` to retrieve colors, the gradient definition, the typography scale, padding/spacing, and the KV banner's intrinsic size at the moment of implementation. Do NOT hardcode any of those values in this plan or in the spec — they belong in Compose theme tokens at implementation time only.
- **Localization note for the implementer**: VN copy is canonical. Do NOT machine-translate to EN at implementation time — leave the `values-en/` file alone and let Android fall back to `values/`. EN authoring is a separate, downstream task tracked outside this plan.
