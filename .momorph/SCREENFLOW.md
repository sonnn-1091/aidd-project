# Screen Flow Overview

## Project Info
- **Project Name**: SAA 2025 (Sun* Annual Awards 2025)
- **Platform Target**: Android (Kotlin + Jetpack Compose + Material 3) — iOS-labeled MoMorph frames are reused as the visual reference and compiled to Android.
- **Figma File Key**: 9ypp4enmFmdK3YAFJLIu6C
- **Figma URL**: https://www.figma.com/design/9ypp4enmFmdK3YAFJLIu6C
- **Created**: 2026-05-08
- **Last Updated**: 2026-05-12

---

## Discovery Progress

| Metric | Count |
|--------|-------|
| Total Screens | 10 |
| Discovered | 10 |
| Spec Shipped | 10 |
| Spec In Progress | 0 |
| Completion | 100% |

---

## Screens

| # | Screen Name | screenId | Figma Link | Status | Detail File | Parent Flow(s) | Outbound Edges |
|---|-------------|----------|------------|--------|-------------|----------------|----------------|
| 1 | [iOS] Login | 8HGlvYGJWq | https://www.figma.com/design/9ypp4enmFmdK3YAFJLIu6C?node-id=8HGlvYGJWq | spec_shipped | specs/8HGlvYGJWq-iOS-Login/spec.md | (entry) | Home (on success), Language dropdown (sub-flow) |
| 2 | [iOS] Home | OuH1BUTYT0 | https://www.figma.com/design/9ypp4enmFmdK3YAFJLIu6C?node-id=OuH1BUTYT0 | spec_shipped | specs/OuH1BUTYT0-iOS-Home/spec.md | Login (post-auth) | Login (on logout / 401), Language dropdown (sub-flow) |
| 3 | [iOS] Language dropdown | uUvW6Qm1ve | https://www.figma.com/design/9ypp4enmFmdK3YAFJLIu6C?node-id=uUvW6Qm1ve | spec_shipped | specs/uUvW6Qm1ve-iOS-Language-dropdown/spec.md | Login (8HGlvYGJWq), Home (OuH1BUTYT0) | none — selection re-renders strings on parent |
| 4 | [iOS] Award_Top talent | c-QM3_zjkG | https://www.figma.com/design/9ypp4enmFmdK3YAFJLIu6C?node-id=c-QM3_zjkG | spec_shipped | specs/c-QM3_zjkG-iOS-Award-Top-talent/spec.md | Home (Awards-tab nav, Chi tiết carousel tap) | Login (on 401), Sun*Kudos (Chi tiết tap) |
| 5 | [iOS] Award_Top project | FQoJZLkG_d | https://www.figma.com/design/9ypp4enmFmdK3YAFJLIu6C?node-id=FQoJZLkG_d | spec_shipped (delta-spec) | specs/FQoJZLkG_d-iOS-Award-Top-project/spec.md | Award_Top talent (dropdown select), Home (Chi tiết carousel tap on Top Project card) | Same as canonical Award Detail — renders through the same parametric AwardDetailScreen composable |
| 6 | [iOS] Award_Top project leader | QQvsfK3yaK | https://www.figma.com/design/9ypp4enmFmdK3YAFJLIu6C?node-id=QQvsfK3yaK | spec_shipped (delta-spec) | specs/QQvsfK3yaK-iOS-Award-Top-project-leader/spec.md | Award_Top talent (dropdown select), Home (Chi tiết carousel tap on Top Project Leader card — requires DEMO append) | Same as canonical Award Detail — pure data swap, no new Q-numbers (quantity = 3 renders as "03" via shipped Q-TP-2 formatter) |
| 7 | [iOS] Award_Best Manager | 7y195PPTxQ | https://www.figma.com/design/9ypp4enmFmdK3YAFJLIu6C?node-id=7y195PPTxQ | spec_shipped (delta-spec) | specs/7y195PPTxQ-iOS-Award-Best-Manager/spec.md | Award_Top talent (dropdown select), Home (Chi tiết carousel tap on Best Manager card — requires DEMO append) | Same as canonical Award Detail — pure data swap, no new Q-numbers (quantity = 1 renders as "01", prizeValue 10.000.000 VNĐ is new value but still pre-formatted per Q5) |
| 8 | [iOS] Award_MVP | b2BuS8HYIt | https://www.figma.com/design/9ypp4enmFmdK3YAFJLIu6C?node-id=b2BuS8HYIt | spec_shipped (delta-spec) | specs/b2BuS8HYIt-iOS-Award-MVP/spec.md | Award_Top talent (dropdown select), Home (Chi tiết carousel tap on MVP card — requires DEMO append) | Same parametric Award Detail with **Q-MVP-1** (custom prize caption "cho giải cá nhân" — extends AwardDetail model + AwardInfoBlock composable, backward-compatible) |
| 9 | [iOS] Award_Signature 2025 - Creator | O98TwiHaJe | https://www.figma.com/design/9ypp4enmFmdK3YAFJLIu6C?node-id=O98TwiHaJe | spec_shipped (delta-spec) | specs/O98TwiHaJe-iOS-Award-Signature-2025-Creator/spec.md | Award_Top talent (dropdown select), Home (Chi tiết carousel tap on Signature 2025 card — requires DEMO append) | Same parametric Award Detail with **Q-SIG-1** (dual prize-value rows — cá nhân + tập thể — and **Q-MVP-1** custom caption. Extends AwardDetail with `prizeValueTeam` + `prizeCaptionTeam`; AwardInfoBlock conditionally renders second PrizeValueRow) |
| 10 | [iOS] Sun*Kudos | fO0Kt19sZZ | https://www.figma.com/design/9ypp4enmFmdK3YAFJLIu6C?node-id=fO0Kt19sZZ | spec_shipped (full spec, ratified 2026-05-12) | specs/fO0Kt19sZZ-iOS-Sun-Kudos/spec.md | Home (Sun*Kudos Chi tiết / bottom-nav Kudos tab), Award Detail (Sun*Kudos Chi tiết) | Send Kudos compose (PV7jBVZU1N), Kudo Detail (T0TR16k0vH / 5C2BL6GYXL anonymous), All Kudos paginated (j_a2GQWKDJ), Open Secret Box (kQk65hSYF2), Profile bản thân (hSH7L8doXB) / Profile người khác (bEpdheM0yU), Login (8HGlvYGJWq on 401). Hashtag dropdown (V5GRjAdJyb) + Phòng ban dropdown (76k69LQPfj) as overlays. 4 Q-K resolved + Q-K-1 left as implementer-call. |

---

## Navigation Graph

```mermaid
flowchart TD
    subgraph Auth["Authentication"]
        Login["[iOS] Login\n(8HGlvYGJWq)"]
    end

    subgraph Main["Main Application"]
        Home["[iOS] Home\n(OuH1BUTYT0)"]
        AwardTopTalent["[iOS] Award_Top talent\n(c-QM3_zjkG)"]
        AwardTopProject["[iOS] Award_Top project\n(FQoJZLkG_d)"]
        SunKudos["[iOS] Sun*Kudos\n(fO0Kt19sZZ)"]
    end

    subgraph SubFlows["Sub-flows / Overlays"]
        LangDropdown["[iOS] Language dropdown\n(uUvW6Qm1ve)"]
        HashtagDropdown["Hashtag dropdown\n(V5GRjAdJyb)"]
        DeptDropdown["Phòng ban dropdown\n(76k69LQPfj)"]
    end

    subgraph Discovered["Discovered, spec pending"]
        SendKudos["[iOS] Send Kudos compose\n(PV7jBVZU1N)"]
        KudoDetail["[iOS] View Kudo\n(T0TR16k0vH / 5C2BL6GYXL)"]
        AllKudos["[iOS] All Kudos paginated\n(j_a2GQWKDJ)"]
        OpenSecretBox["[iOS] Open Secret Box\n(kQk65hSYF2)"]
        ProfileSelf["[iOS] Profile bản thân\n(hSH7L8doXB)"]
        ProfileOther["[iOS] Profile người khác\n(bEpdheM0yU)"]
    end

    Login -- "auth success" --> Home
    Home -- "logout / 401" --> Login
    AwardTopTalent -- "logout / 401" --> Login
    SunKudos -- "logout / 401" --> Login

    Home -- "Awards tab tap (default = first by sort_order = Top Talent)" --> AwardTopTalent
    Home -- "Chi tiết on Top Talent card" --> AwardTopTalent
    Home -- "Chi tiết on Top Project card" --> AwardTopProject
    AwardTopTalent -. "dropdown select Top Project" .-> AwardTopProject
    AwardTopProject -. "dropdown select Top Talent" .-> AwardTopTalent

    Home -- "Kudos bottom-nav tab" --> SunKudos
    AwardTopTalent -- "Sun*Kudos Chi tiết (KudosSection)" --> SunKudos
    AwardTopProject -- "Sun*Kudos Chi tiết (KudosSection)" --> SunKudos

    SunKudos -- "Send Kudos pill (A.1) tap" --> SendKudos
    SunKudos -- "Xem chi tiết / card body tap" --> KudoDetail
    SunKudos -- "View all Kudos link tap" --> AllKudos
    SunKudos -- "Open Secret Box CTA tap" --> OpenSecretBox
    SunKudos -- "tap own avatar / name" --> ProfileSelf
    SunKudos -- "tap other Sunner avatar / name" --> ProfileOther
    SunKudos -. "tap Hashtag filter" .-> HashtagDropdown
    SunKudos -. "tap Phòng ban filter" .-> DeptDropdown
    HashtagDropdown -. "select hashtag → filter both feeds" .-> SunKudos
    DeptDropdown -. "select department → filter both feeds" .-> SunKudos

    Login -. "tap language pill" .-> LangDropdown
    Home  -. "tap language pill" .-> LangDropdown
    AwardTopTalent -. "tap language pill" .-> LangDropdown
    SunKudos -. "tap language pill" .-> LangDropdown
    LangDropdown -. "select VN/EN → persist + re-render" .-> Login
    LangDropdown -. "select VN/EN → persist + re-render" .-> Home
    LangDropdown -. "select VN/EN → persist + re-render" .-> AwardTopTalent
    LangDropdown -. "select VN/EN → persist + re-render" .-> SunKudos
```

> Dotted edges denote a sub-flow / overlay relationship: the dropdown does not push a new route — it anchors to a control on its parent and dismisses back to the same parent screen. The Award_Top talent ↔ Award_Top project dotted edges are sibling-state transitions — both render through the same parametric `AwardDetailScreen` composable; the dropdown swap is an in-place re-render, not a navigation transition. The `Discovered, spec pending` subgraph contains Sun*Kudos's outbound destinations whose own specs are still pending — Sun*Kudos's spec (`fO0Kt19sZZ`) marks them as Out of Scope. Awards-related language-pill edges are shown only on Top Talent (Top Project inherits the same chrome behaviour as the canonical parametric screen).

---

## Screen Groups

### Group: Authentication
| Screen | Purpose | Entry Points |
|--------|---------|--------------|
| [iOS] Login (8HGlvYGJWq) | Supabase email/password auth | App launch, Logout from Home, 401 from Home |

### Group: Main Application
| Screen | Purpose | Entry Points |
|--------|---------|--------------|
| [iOS] Home (OuH1BUTYT0) | Awards hub: US1 hub view + US2 awards carousel + detail | Login success |
| [iOS] Award_Top talent (c-QM3_zjkG) | Parametric Award Detail screen — body re-renders per dropdown selection across all award categories. Top Talent is the default render (first by `sort_order`). | Home Awards-tab bottom-nav + Chi tiết on Top Talent carousel card |
| [iOS] Award_Top project (FQoJZLkG_d) | Same parametric Award Detail screen rendered with Top Project as the default selection. Delta-spec — see `c-QM3_zjkG` for the canonical behaviour contract. | Home Chi tiết on Top Project carousel card + Award_Top talent dropdown select "Top Project" |

### Group: Sub-flows / Overlays
| Screen | Purpose | Entry Points |
|--------|---------|--------------|
| [iOS] Language dropdown (uUvW6Qm1ve) | Surfaces VN / EN options and persists selection via `LanguagePreferenceRepository`. Same `LanguageSelector` Compose component shared by Login and Home. (Figma frame enumerates only VN + EN — see spec § Out of Scope for the JA removal.) | Language pill in Login header, Language pill in Home header |

---

## Shared Components

| Component | Used By | Notes |
|-----------|---------|-------|
| `LanguageSelector` | Login header, Home header, Award Detail header | Anchors the [iOS] Language dropdown sub-flow. Selection writes to `LanguagePreferenceRepository`; consumers observe and recompose strings. No navigation occurs on select. |
| `HomeHeader` | Home, Award Detail | Same chrome on every authenticated screen (logo + language pill + search + bell). The "lift-to-`core/chrome/ui`" refactor is deferred — currently imported as-is from `home/ui/components`. |
| `HomeBottomBar` | Home, Award Detail | Same bottom nav (SAA 2025 / Awards / Kudos / Profile). Re-tap of the active tab on Award Detail scrolls the body to the top (canonical Q2 + commit `0293084`). |
| `KudosSection` | Home, Award Detail | The Sun\*Kudos promo block at the bottom of the body — both screens' Chi tiết CTAs funnel to `Routes.KUDOS_OVERVIEW`. |
| `AwardDetailScreen` (parametric) | Award_Top talent + Award_Top project frames (and future Top Heart, MVP, Best Manager, Signature 2025 — Creator) | Single composable parameterised by `awardId`. Dropdown swap is an in-place re-render via `AwardDetailViewModel.onCategorySelected`, NOT a screen-level navigation transition. |

---

## Discovery Log

| Date | Action | Screens | Notes |
|------|--------|---------|-------|
| (prior) | Initial spec | [iOS] Login (8HGlvYGJWq) | Supabase auth integration, Sunner verification |
| (prior) | Spec + impl | [iOS] Home (OuH1BUTYT0) | Phases 1–4 shipped (UI scaffold, domain, US1 hub, US2 carousel) |
| 2026-05-08 | Spec started | [iOS] Language dropdown (uUvW6Qm1ve) | Sub-flow anchored from language-pill on Login + Home headers; status: spec_in_progress |
| 2026-05-08 | Spec ratified | [iOS] Language dropdown (uUvW6Qm1ve) | Review pass + 4 Q&A resolved (drop JA, VN-default global, silent JA fallback, silent write-failure). Status flipped to `spec_shipped`. |
| 2026-05-11 | Spec + plan + tasks + impl shipped | [iOS] Award_Top talent (c-QM3_zjkG) | Parametric Award Detail screen. 103 tasks across Phases 0–8 (commits `4e830b9` → `26f8ef8`). Resolved Q1 (default = last-viewed in-session, fallback first by sort_order), Q2 (Awards-tab retap scroll-to-top), Q3 (both Chi tiết → KUDOS_OVERVIEW), Q5 (prize_value pre-formatted), Q6/Q7/Q8 (implementer discretion). |
| 2026-05-11 | Delta-spec authored | [iOS] Award_Top project (FQoJZLkG_d) | Lightweight delta-spec referencing canonical c-QM3_zjkG (commit `daaf526`). Same parametric AwardDetailScreen — only DEMO payload differs. Resolved Q-TP-1 (DEMO description / quantity / unit / prizeValue aligned with Figma node `6885:10468`) + Q-TP-2 (zero-pad single-digit quantities — `2 → "02 Tập thể"`, commit `9366e39`). |
| 2026-05-11 | Slice D test backfill shipped | Award Detail (cross-frame) | 33 instrumented + unit tests across 5 files (commit `d69a6c8`): AwardInfoBlockTest (Q-TP-2 regression), DemoAwardsRepositoryTest (Q-TP-1 regression), AwardDetailScreenTest, AwardCategoryDropdownTest, BottomNavAwardsTabTest. Closes canonical T026–T056 evidence gap (tasks marked [x] without test files existing). Also bumps mockk 1.13.13 → 1.14.3 for 16KB-page alignment. |
| 2026-05-11 | Slice A badge bundle shipped | [iOS] Award_Top project (FQoJZLkG_d) | Top Project Figma badge composite bundled (commit `1417e25`). MoMorph composite endpoint returned null → fell back to downloading BG (160×160) + wordmark (106×16) layers separately and compositing offline with Python + Pillow. DemoAwardsRepository.DEMO_DETAILS[1].imageUrl flipped null → resource URI. |
| 2026-05-11 | Delta-spec authored | [iOS] Award_Top project leader (QQvsfK3yaK) | Lightweight delta-spec referencing canonical c-QM3_zjkG (no commit yet — spec only). Data shape identical to Top Talent (quantity=3 "Cá nhân", prize 7.000.000 VNĐ); no new Q-numbers introduced since shipped Q-TP-2 `"%02d"` formatter renders "03" automatically. Description text deliberately not inlined — pulled from Figma node `6885:10542` at impl time. Pending DEMO_AWARDS + DEMO_DETAILS append + badge composite bundle (Slice A equivalent). |
| 2026-05-11 | Delta-spec authored | [iOS] Award_Best Manager (7y195PPTxQ) | Third delta-spec following the now-validated pattern. Pure data append — `quantity=1` (renders "01"), `unit="Cá nhân"`, `prizeValue="10.000.000 VNĐ"` (new value, still pre-formatted). No new Q-numbers. Description full Figma copy at node `6885:10616`. Pending DEMO append + badge bundle. |
| 2026-05-11 | Delta-specs + impl shipped | [iOS] Award_MVP (b2BuS8HYIt) + [iOS] Award_Signature 2025 - Creator (O98TwiHaJe) | Two delta-specs break the pure-data-append pattern by introducing **Q-MVP-1** (custom prize caption per award) and **Q-SIG-1** (dual prize-value rows — cá nhân + tập thể). AwardDetail model extended with 3 new optional fields: `prizeCaption`, `prizeValueTeam`, `prizeCaptionTeam` (all backward-compatible defaults). AwardInfoBlock composable extended to accept the caption override and conditionally render a second PrizeValueRow. After this lands, all five future Award fields are absorbable by data-only deltas. |
| 2026-05-12 | Spec authored | [iOS] Sun*Kudos (fO0Kt19sZZ) | New main hub spec — 14 user stories, 30+ components mapped with Node IDs. Establishes Kudos recognition flow: Highlight carousel (top-5 by hearts) + Hashtag/Department filter (AND) + All Kudos feed + Spotlight Board (pan/zoom + live Sunner search) + personal stats with x2 fire bonus + Open Secret Box CTA + Top 10 gift recipients + Copy Link + Send Kudos shortcut + auth gate. 5 open product questions (Q-K-1 special day source, Q-K-2 realtime vs poll, Q-K-3 anonymous-to-recipient flag, Q-K-4 copy link URL, Q-K-5 like-own-received). |

---

## Next Steps

- [x] Author `specs/uUvW6Qm1ve-iOS-Language-dropdown/spec.md` covering: dropdown anchoring, VN/EN option rows, selected-state, dismiss behaviour, persistence via `LanguagePreferenceRepository`, re-render contract on parent. — **Drafted 2026-05-08; reviewed + ratified 2026-05-08.**
- [x] Review pass — 4 Q&A resolved 2026-05-08 (Q1 drop JA, Q2 VN-default global, Q3 silent JA fallback, Q4 silent DataStore-write-failure).
- [ ] Run `momorph.plan` for the Language dropdown spec to produce a feature plan + tasks. The plan must include:
   - Removal of JA from `Language.entries` + the `LanguageSelector` row list.
   - DataStore decoder fallback to VN for unknown / orphaned values.
   - Localised `contentDescription` updates so TalkBack re-announces the new selection on language change.
   - Touch-target tests on anchor + each row, mirroring Login's `TouchTargetTest`.
- [ ] Drop `values-ja/strings.xml` from the active `StringResourceParityTest` set when the JA removal lands; keep the file on disk for one release cycle.
- [ ] Future Award delta-specs (Top Heart, MVP, Best Manager, Signature 2025 — Creator) follow the same delta-spec pattern as `FQoJZLkG_d-iOS-Award-Top-project`. Each is a 1-page spec referencing canonical `c-QM3_zjkG-iOS-Award-Top-talent/spec.md`; the implementer's job is (a) update DEMO row + production Supabase `awards` row, (b) bundle the Figma badge composite, (c) author a delta-plan + tasks if either of the prior shifts surfaces a frame-specific Q-number. **Do not author a duplicate 750-line spec per frame.**
