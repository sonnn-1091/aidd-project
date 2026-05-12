# Implementation Plan: Sun*Kudos hub

**Frame**: `fO0Kt19sZZ-iOS-Sun-Kudos`
**Date**: 2026-05-12
**Spec**: `spec.md` (ratified 2026-05-12, reviewed three times)

---

## Summary

Sun*Kudos is the **main recognition hub** of SAA 2025 — the most
complex screen authored so far (14 user stories vs Award Detail's
8, ~30 components vs ~12). The placeholder route `Routes.KUDOS_OVERVIEW`
already exists in `AppNavigation.kt` (currently renders
`PlaceholderScreen(label = "Kudos overview")`); this plan replaces
that placeholder with a real `KudosScreen` composable backed by a
single `KudosViewModel` exposing `StateFlow<KudosUiState>`.

The architecture mirrors Award Detail's parametric stateless
composable + ViewModel + sealed-state-per-section pattern, scaled
up: 5 independent sealed states (one per body section) live inside
one aggregate `KudosUiState`, refreshed in parallel on mount + on
pull-to-refresh (Q-K-2). Reactions (like / unlike) are optimistic
with server-enforced uniqueness via Postgres `unique(user_id,
kudos_id)`. Anonymous Kudos visibility follows the server-derived
`sender_visible_to_me` per-viewer flag (Q-K-3). Self-like blocking
follows the server-derived `like_disabled_for_me` flag (Q-K-5).

**Existing infrastructure to reuse** (≈30% of the screen's surface):
- Auth gate: `core/auth/AuthRedirectController` + `core/session/SessionGate` already gate every authenticated route.
- Chrome: `HomeHeader` (logo + language pill + search + bell) + `HomeBottomBar` already shared by Home + Award Detail; Sun*Kudos's bottom-nav Kudos tab is already wired.
- Locale: `core/locale/LanguagePreferenceRepository` + `LanguageSelector` ship the language pill / dropdown.
- Single-click suppression: `core/ui/rememberSingleClickHandler` is the TR-004 contract — reused for Send Kudos pill, Open Secret Box, hashtag/department dropdown triggers, heart taps.
- Coil 2.7 image loading: badge / attachment / avatar rendering already wired in Award Detail.
- Supabase Postgrest read pattern: established by Award Detail's `AwardsRepository.detail(id, locale)` — Kudos repository follows the same shape.

**Net new code** is concentrated in a new `kudos/` feature package
mirroring `awarddetail/`'s `ui/data/domain` layout, plus a Spotlight
pan/zoom canvas + Spotlight live search debouncer that have no
prior analogue. The placeholder at `AppNavigation.kt:130`
(`composable(Routes.KUDOS_OVERVIEW) { PlaceholderScreen(...) }`)
flips to the real composable.

---

## Technical Context

**Language/Framework**: Kotlin 2.2.10 + Jetpack Compose + Material 3
**Primary Dependencies**: Hilt (DI), Supabase Kotlin SDK (`supabase-kt`),
Coil 2.7 (image loading), Timber (telemetry),
`androidx.navigation.compose`, DataStore Preferences (locale).
**New compose APIs** (already in the BOM, no new dep needed):
- `androidx.compose.material3.pulltorefresh.PullToRefreshBox`
  for the pull-to-refresh contract (Q-K-2)
- `androidx.compose.foundation.pager.HorizontalPager` +
  `rememberPagerState()` for the Highlight carousel
- `Modifier.transformable` + `rememberTransformableState` for the
  Spotlight pan/zoom canvas
- `kotlinx.coroutines.flow.debounce` for the Spotlight live search

**Database**: Supabase (Postgres) — new tables/views required:
`kudos`, `kudos_hashtags`, `hashtags`, `departments`, `reactions`,
`user_stats` (view or table), `secret_boxes`, `reward_recipients`,
`system_flags`, `spotlight_graph` (view or RPC). RLS policies on
every read; reactions `unique(user_id, kudos_id)` constraint
enforces 1-like-per-user (Q-K-5 augments at the API level so
sender + recipient both can't like).

**Testing**: JUnit4 + Compose UI Test + Robolectric (unit) +
Espresso (instrumented) + Hilt testing rule + mockk.

**State Management**: One `KudosViewModel` exposing
`StateFlow<KudosUiState>`; `KudosUiState` aggregates 5 sealed
sub-states (`KudosHighlightState`, `AllKudosState`,
`SpotlightState`, `PersonalStatsState`, `TopTenState`) plus filter
fields (`selectedHashtagId`, `selectedDepartmentId`) plus search
field (`spotlightSearchQuery`). `SavedStateHandle` for filter
persistence across rotation.

**API Style**: Direct Supabase Postgrest queries via the existing
repository pattern (no REST shim layer). The 15 conceptual
endpoints in spec § API Requirements are realized as
`supabaseClient.from("…").select(...).filter(...).decodeList<…>()`
calls. The auth-token + RLS pipeline matches Award Detail.

---

## Constitution Compliance Check

*GATE: Must pass before implementation can begin. Each item maps
to a principle in `.momorph/constitution.md`.*

- [x] **I. Clean Code & Source Organization** — new feature package
  `com.example.aiddproject.kudos/` with `ui/`, `data/`, `domain/`
  subpackages mirroring `awarddetail/`'s layout. The hub stateless
  composable splits into 8+ section sub-composables, each <150 LOC
  (KudosHeroBanner, SendKudosCta, FilterRow, HighlightCarousel,
  HighlightCard, SpotlightBoard, AllKudosFeed, KudosFeedCard,
  PersonalStatsPanel, OpenSecretBoxCta, TopTenRecipients). Kotlin
  official style enforced via `ktlint`.
- [x] **II. Tech Stack Best Practices** — immutable data classes
  (`Kudos`, `Hashtag`, `Department`, `SpotlightGraph`,
  `PersonalStats`, `GiftRecipient`), `Flow`/`StateFlow` for async,
  repository pattern extending the existing `home/data/KudosSummaryRepository`
  pattern. Supabase client continues to use the anon key only; all
  versions pinned via `libs.versions.toml`. NEW Compose APIs come
  from the existing `androidx-compose-bom`; no new top-level dep
  added (pull-to-refresh, HorizontalPager, transformable all ship
  with the BOM).
- [x] **III. Material Design 3 (Android)** — M3 `PullToRefreshBox`,
  `LazyColumn` for the feed, `HorizontalPager` for the carousel,
  `ModalBottomSheet` for hashtag/department dropdowns,
  `OutlinedTextField` for Spotlight search. 48dp touch targets on
  every interactive element (spec § Accessibility); localized
  `contentDescription` per the spec's A11y contract; font scaling
  respected (no hard-coded sp→dp ratios); responsive via the
  existing `WindowSizeClass` already wired for Home.
- [x] **IV. OWASP Secure Coding** — Supabase RLS on every new
  table; `reactions.unique(user_id, kudos_id)` enforces
  1-like-per-user at the DB level; server-side `like_disabled_for_me`
  + RLS on the `reactions` INSERT/DELETE handles + RPC enforce
  Q-K-5 (sender + recipient block) so the client-side disable is
  a UX hint only. Logging via the existing `SecureTimberTree`
  scrub (no kudos message body, no recipient names ever logged).
  No new secrets, no new PII surface.
- [x] **V. Test-Driven Development** — every new public function
  in `data/`, `domain/`, and the `KudosViewModel` ships with
  failing tests committed before the implementation. Each user
  story has a corresponding Compose UI test class (US-level) +
  ViewModel unit test (state-machine). RLS policy tests for the
  new tables follow the same pattern as Home's RLS policy tests.

**Violations (if any)**: None. The plan introduces no new
dependencies, no constitution amendments needed.

---

## Architecture Decisions

### Frontend approach

- **Component Structure**: Feature-first package
  `com.example.aiddproject.kudos/` with three subpackages:
  - `ui/` — `KudosScreen.kt` (Hilt entry), `KudosScreenContent.kt`
    (stateless), `KudosUiState.kt`, `KudosViewModel.kt`, plus
    `ui/components/` for every section sub-composable.
  - `data/` — `KudosRepository.kt` (interface) +
    `SupabaseKudosRepository.kt` (real impl) +
    `DemoKudosRepository.kt` (DEMO fixture).
  - `domain/` — immutable models (`Kudos`, `Hashtag`, etc.) +
    `domain/states/` for the 5 sealed-interface section states.
- **Stateless content pattern**: `KudosScreenContent` takes
  `state: KudosUiState` + 10+ callbacks (`onPullToRefresh`,
  `onSelectHashtag`, `onSelectDepartment`, `onCardTap`,
  `onHashtagChipTap`, `onHeartTap`, `onCopyLink`, `onSendKudos`,
  `onOpenSecretBox`, `onProfileTap`, `onViewAllKudos`,
  `onSpotlightSearchChange`) — mirrors `AwardDetailScreenContent`.
  Compose UI tests drive this directly; never touch the live
  `KudosViewModel` (constitution V).
- **Styling Strategy**: Same approach as Award Detail — visual
  chrome fetched on-demand via `query_section` at task-execution
  time (Constitution Principle II); no `design-style.md` artifact
  per the project's established convention.
- **Data Fetching**: `KudosViewModel` orchestrates 5 parallel
  `flowOn(Dispatchers.IO)` fetches on mount + on pull-to-refresh.
  Each fetch goes through `KudosRepository` → Supabase Postgrest
  query. Repository pattern matches `home/data/KudosSummaryRepository`
  style.

### KudosUiState shape (concrete)

```kotlin
data class KudosUiState(
    // Section-level sealed states (Loading / Empty / Loaded / Error)
    val highlight: KudosHighlightState,
    val allKudos: AllKudosState,
    val spotlight: SpotlightState,
    val stats: PersonalStatsState,
    val topTen: TopTenState,

    // Filter selections (also persisted via SavedStateHandle)
    val selectedHashtagId: String? = null,
    val selectedDepartmentId: String? = null,

    // Spotlight search input (debounced into a Flow inside the VM)
    val spotlightSearchQuery: String = "",
    val spotlightSearchResult: SpotlightSearchResult = SpotlightSearchResult.Idle,

    // Pull-to-refresh state
    val isRefreshing: Boolean = false,

    // Snackbar / toast text for Copy Link feedback
    val snackbar: SnackbarMessage? = null,

    // System flag — drives heart math (+1 vs +2) and x2 fire badge
    val specialDayActive: Boolean = false,

    // Locale (read from LanguagePreferenceRepository)
    val language: Language = Language.Default,
) {
    companion object {
        val Empty: KudosUiState = KudosUiState(
            highlight = KudosHighlightState.Loading,
            allKudos = AllKudosState.Loading,
            spotlight = SpotlightState.Loading,
            stats = PersonalStatsState.Loading,
            topTen = TopTenState.Loading,
        )
    }
}
```

### KudosRepository interface (concrete)

The 15 conceptual endpoints from spec § API Requirements collapse
into this interface — each method returns `Result<T>` so
optimistic / rollback flows can branch on success vs failure
without exception handling at the call site (matches
`AwardsRepository.detail(id, locale)` shape):

```kotlin
interface KudosRepository {
    // Highlight + feed (US3, US4)
    suspend fun listHighlight(filter: KudosFilter): Result<List<Kudos>>
    suspend fun listKudos(filter: KudosFilter, page: Int, limit: Int): Result<KudosPage>
    suspend fun detail(kudosId: String): Result<Kudos>

    // Reactions (US5, Q-K-5)
    suspend fun addReaction(kudosId: String): Result<Unit>
    suspend fun removeReaction(kudosId: String): Result<Unit>

    // Filter dropdowns
    suspend fun listHashtags(): Result<List<Hashtag>>
    suspend fun listDepartments(): Result<List<Department>>

    // Spotlight (US9, Q-K-2)
    suspend fun loadSpotlightGraph(): Result<SpotlightGraph>  // returns total_kudos_count + graph
    suspend fun searchSunner(query: String, limit: Int = 20): Result<List<SunnerMatch>>

    // Personal stats (US10) + system flags
    suspend fun personalStats(): Result<PersonalStats>
    suspend fun systemFlags(): Result<SystemFlags>  // specialDayActive, x2BonusActive (Q-K-1)

    // Secret box (US11)
    suspend fun nextUnopenedBox(): Result<SecretBoxRef?>
    suspend fun openSecretBox(boxId: String): Result<SecretBoxReward>

    // Top 10 (US12)
    suspend fun listRecentGiftRecipients(limit: Int = 10): Result<List<GiftRecipient>>
}

data class KudosFilter(
    val hashtagId: String? = null,
    val departmentId: String? = null,
)
```

### Pull-to-refresh contract (Q-K-2 mechanic)

```kotlin
// Inside KudosViewModel:
fun onPullToRefresh() = viewModelScope.launch {
    if (_uiState.value.isRefreshing) return@launch  // gate per spec Concurrency rule
    _uiState.update { it.copy(isRefreshing = true) }
    try {
        coroutineScope {
            // Five parallel fetches; section failures isolate via Result.
            val h = async { fetchHighlight() }
            val a = async { fetchAllKudos(resetPaging = true) }
            val s = async { fetchSpotlight() }
            val p = async { fetchPersonalStats() }
            val t = async { fetchTopTen() }
            awaitAll(h, a, s, p, t)
        }
    } finally {
        _uiState.update { it.copy(isRefreshing = false) }
    }
}
```

### Optimistic reaction rollback (US5 mechanic)

```kotlin
fun onHeartTap(kudosId: String) = viewModelScope.launch {
    val current = findKudos(kudosId) ?: return@launch
    if (current.like_disabled_for_me) return@launch
    val wasLiked = current.liked_by_current_user
    val delta = if (specialDayActive) 2 else 1
    val optimistic = current.copy(
        liked_by_current_user = !wasLiked,
        heart_count = current.heart_count + (if (wasLiked) -delta else +delta),
    )
    applyKudosLocally(optimistic)
    val result = if (wasLiked) repo.removeReaction(kudosId)
                 else repo.addReaction(kudosId)
    if (result.isFailure) {
        // Rollback to the pre-tap snapshot; surface an inline error toast.
        applyKudosLocally(current)
        _uiState.update { it.copy(snackbar = SnackbarMessage.ReactionFailed) }
    }
}
```

`applyKudosLocally(kudos)` patches the kudos in every section's
state that contains it (Highlight + All Kudos) — single point of
mutation so the two feeds stay in sync.

### Backend approach

- **API Design**: Direct Supabase Postgrest reads;
  reactions use Postgrest insert/delete on the `reactions` table.
  No custom REST endpoints. The 15 conceptual endpoints from
  spec § API Requirements map to:
  - 12 `from("table").select().filter()` Postgrest reads
  - 2 reactions writes (insert / delete)
  - 1 RPC for Secret Box open (returns reward payload atomically)
- **Data Access**: Repository pattern with one
  `KudosRepository` interface; production binding =
  `SupabaseKudosRepository`; DEMO binding =
  `DemoKudosRepository`. Hilt module follows the same
  shape as `AwardsRepository`'s binding.
- **Validation**: Spotlight search input `maxLength = 100` enforced
  client-side via `TextFieldValue` + `filter { it.length <= 100 }`.
  Server-side validation: kudos hashtag/department filters are
  ID-based (RLS catches anything malformed); reactions enforce
  business rules via `like_disabled_for_me` + RLS.

### Integration points

- **Existing services to reuse**:
  - `core/auth/AuthRedirectController` — 401 redirect.
  - `core/session/SessionGate` — auth gate at the navigation level.
  - `core/locale/LanguagePreferenceRepository` — locale-aware
    `stringResource`.
  - `home/ui/components/HomeHeader`, `HomeBottomBar`,
    `KudosSection` (for AwardDetail's KudosSection card — Sun*Kudos
    DOES NOT reuse `KudosSection` for the hub body, that component
    is just the preview card on Home/AwardDetail).
  - `core/ui/rememberSingleClickHandler` — TR-004 single-click
    suppression on every navigation-triggering control.
- **Shared composables**: Reuse `HomeHeader` + `HomeBottomBar`
  inside `KudosScreenContent`'s Scaffold. Same chrome contract as
  Award Detail.
- **API contracts**: This plan references but does NOT ship
  backend API specs. The conceptual endpoints in
  `spec.md § API Requirements` are the contract; backend
  implementation lives in a separate work stream.

---

## Project Structure

### Documentation (this feature)

```text
.momorph/specs/fO0Kt19sZZ-iOS-Sun-Kudos/
├── spec.md              # Ratified feature spec (3 review passes)
├── plan.md              # This file
└── tasks.md             # Next step (~150 tasks expected)
```

### Source code (affected areas)

#### New files (≈40)

**`app/src/main/java/com/example/aiddproject/kudos/`**:

| File | Purpose |
|---|---|
| `ui/KudosScreen.kt` | Hilt-injected entry composable; observes ViewModel + delegates to stateless content |
| `ui/KudosScreenContent.kt` | Stateless content composable; receives `KudosUiState` + callbacks; assembles all sections in a LazyColumn wrapped in PullToRefreshBox |
| `ui/KudosUiState.kt` | Aggregate state data class |
| `ui/KudosViewModel.kt` | State orchestration, 5 parallel fetches, optimistic reaction handling |
| `ui/components/KudosHeroBanner.kt` | A — hero banner (US1) |
| `ui/components/SendKudosCta.kt` | A.1 — Send Kudos pill (US6) |
| `ui/components/HighlightFilterRow.kt` | B.1.1 + B.1.2 — Hashtag + Phòng ban filters with bottom sheets (US3) |
| `ui/components/HighlightCarousel.kt` | B.2 / B.5 — HorizontalPager with page indicator + active/faded card states (US4) |
| `ui/components/HighlightCard.kt` | B.3 — single carousel card with sender/recipient info + content + action row (US5, US7, US8, US13) |
| `ui/components/SpotlightBoard.kt` | B.6 / B.7 — Spotlight container + total counter + pan/zoom canvas + search (US9) |
| `ui/components/AllKudosFeed.kt` | C — paginated feed using a LazyColumn (US1 + state-driven empty/loaded/error) |
| `ui/components/KudosFeedCard.kt` | C.3 — single feed card (similar to HighlightCard but with 5-line truncation + photos) |
| `ui/components/PersonalStatsPanel.kt` | D.1 — 5 stat tiles + conditional x2 fire badge (US10) |
| `ui/components/OpenSecretBoxCta.kt` | D.2 — Open Secret Box button with disabled state + single-click suppression (US11) |
| `ui/components/TopTenRecipients.kt` | D.3 — Top 10 latest gift recipients list (US12) |
| `ui/components/CopyLinkSnackbarHost.kt` | M3 `SnackbarHost` driving the Copy Link feedback message "Link copied — ready to share!" (US13). Compose `SnackbarHostState` is held in `KudosViewModel`; `showSnackbar(...)` fires on successful `Clipboard.setText(url)`. NOT an Android `Toast` — Snackbar matches the project's existing M3 chrome elsewhere. |
| `data/KudosRepository.kt` | Interface — 15 conceptual endpoints |
| `data/SupabaseKudosRepository.kt` | Production Supabase Postgrest impl |
| `data/DemoKudosRepository.kt` | DEMO fixture impl (seed data for ~10 kudos + hashtags + departments + stats + top 10) |
| `data/KudosRepositoryModule.kt` | Hilt module binding (mirrors AwardsRepository binding) |
| `data/dto/KudosDto.kt` | Postgrest row → domain mapper |
| `data/dto/HashtagDto.kt` | |
| `data/dto/DepartmentDto.kt` | |
| `data/dto/SpotlightGraphDto.kt` | |
| `data/dto/PersonalStatsDto.kt` | |
| `data/dto/GiftRecipientDto.kt` | |
| `domain/Kudos.kt` | Immutable model with all server-derived per-viewer fields (`sender_visible_to_me`, `like_disabled_for_me`, `liked_by_current_user`) |
| `domain/Hashtag.kt` | |
| `domain/Department.kt` | |
| `domain/SpotlightGraph.kt` | + `total_kudos_count` |
| `domain/PersonalStats.kt` | |
| `domain/GiftRecipient.kt` | |
| `domain/Reaction.kt` | + helpers for optimistic local apply |
| `domain/states/KudosHighlightState.kt` | sealed: `Loading / Empty / Loaded(items) / Error(messageRes)` |
| `domain/states/AllKudosState.kt` | sealed + pagination metadata |
| `domain/states/SpotlightState.kt` | sealed |
| `domain/states/PersonalStatsState.kt` | sealed |
| `domain/states/TopTenState.kt` | sealed |

**Resources** (`app/src/main/res/`):

| File | Status | Purpose |
|---|---|---|
| `values/strings.xml` | Modified | Add ~25 `kudos_*` strings (see Modified Files table for the Q-K-4 URL template). |
| `values-en/strings.xml` | Modified | EN locale parity for the new keys (Constitution's `StringResourceParityTest` validates parity at unit-test time). |
| `drawable-mdpi/ic_kudos_*.png` | New | Section icons (heart, send, secret-box, fire-x2) — pulled from Figma via `get_media_files` in Phase 0 |

#### Modified files

| File | Changes |
|---|---|
| `app/src/main/java/com/example/aiddproject/navigation/AppNavigation.kt` | Line 130: replace `composable(Routes.KUDOS_OVERVIEW) { PlaceholderScreen(label = "Kudos overview") }` with real `KudosScreen()` Hilt-injected wiring + outbound nav callbacks (send-kudos, kudo-detail, all-kudos, open-secret-box, profile-self/other). Also wire `KudosScreen`'s callbacks to existing routes: `Routes.WRITE_KUDO` (US6), `Routes.KUDOS_DETAIL` (US7), `Routes.KUDOS_FEED` (US14), `Routes.PROFILE` (US8 + US12), `Routes.SECRET_BOX_OPEN` (US11 — NEW route). |
| `app/src/main/java/com/example/aiddproject/navigation/Routes.kt` | Add one new constant: `const val SECRET_BOX_OPEN: String = "route_secret_box_open"` (US11). Existing `KUDOS_OVERVIEW`, `KUDOS_FEED`, `KUDOS_DETAIL`, `WRITE_KUDO`, `PROFILE` constants are reused as-is. |
| `app/src/main/java/com/example/aiddproject/MainActivity.kt` | (none expected — Hilt module registers automatically) |
| `app/src/main/res/values/strings.xml` | Add `kudos_copy_link_url_template` = `https://saa.sun-asterisk.com/kudos/%1$s` (Q-K-4) plus ≈25 other `kudos_*` strings — section headers, empty/error copy, A11y contentDescription templates, Snackbar text. |

#### Tests

| File | Test scope |
|---|---|
| `test/.../kudos/ui/KudosViewModelTest.kt` | State machine: 5 parallel fetches, filter race, optimistic reaction + rollback, pull-to-refresh, double-tap suppression, secret-box concurrency |
| `test/.../kudos/data/SupabaseKudosRepositoryTest.kt` | Postgrest contract tests for each conceptual endpoint via mock gateway |
| `test/.../kudos/data/DemoKudosRepositoryTest.kt` | DEMO fixture pin (~10 assertions) |
| `androidTest/.../kudos/KudosScreenTest.kt` | Layout + section rendering (parametric over loaded/empty/error per section) + pull-to-refresh integration |
| `androidTest/.../kudos/HighlightCarouselTest.kt` | Swipe + page indicator + active/faded card state + filter reset |
| `androidTest/.../kudos/KudosFeedCardTest.kt` | Heart toggle (incl. disabled state) + copy link + hashtag chip tap + image tap + truncation |
| `androidTest/.../kudos/HighlightFilterRowTest.kt` | Hashtag + Department bottom-sheet open/select; AND-combined filtering |
| `androidTest/.../kudos/SpotlightBoardTest.kt` | Total counter render; search input maxLength 100; no-results message; pan/zoom limited coverage |
| `androidTest/.../kudos/PersonalStatsPanelTest.kt` | x2 badge conditional render |
| `androidTest/.../kudos/OpenSecretBoxCtaTest.kt` | Disabled when count=0; single-click suppression on double-tap |
| `androidTest/.../kudos/TopTenRecipientsTest.kt` | List render + empty state + tap row → profile callback |

### Dependencies

No new top-level dependency required. The Compose BOM already
includes:

- `androidx.compose.material3:material3` ≥ 1.3.0 (PullToRefreshBox)
- `androidx.compose.foundation:foundation` ≥ 1.6.0
  (HorizontalPager + transformable + rememberTransformableState)
- `androidx.compose.material:material-icons-extended` — **NOT
  currently in `libs.versions.toml`**. Verified 2026-05-12 via
  grep. Phase 0 MUST add it as a new entry in
  `gradle/libs.versions.toml` (library coordinate
  `androidx.compose.material:material-icons-extended`; no version
  ref needed since it's BOM-managed) plus a `implementation(
  libs.androidx.compose.material.icons.extended)` line in
  `app/build.gradle.kts`. Used for: fire (x2 badge), gift box
  (Secret Box), heart filled/outline, copy link, paper plane
  (Send Kudos). If product wants custom-drawn icons later, swap
  per-icon without changing the rest of the screen.

---

## Implementation Strategy

### Phase breakdown

The 14 user stories naturally cluster into 13 implementation
phases (Phase 0 – Phase 12), sequenced by dependency. Phase 0 +
Phase 1 are infrastructure / setup; Phases 2–11 deliver the user
stories in priority order; Phase 12 is final polish.

### Phase dependency graph

```text
Phase 0 (Setup)
   ↓
Phase 1 (Foundational: domain + repo + Hilt)
   ↓
Phase 2 (US1 MVP — render hub with all section states; 🎯 STOP & VALIDATE)
   ↓
Phase 3 (US2 Auth gate) — depends on Phase 2's screen mount
   ↓
Phase 4 (US3 Filters) ─┐
                        ├─ Both depend on Phase 2's section render
Phase 5 (US4 Carousel) ─┘  but can be staffed in parallel by separate devs
   ↓
Phase 6 (US5 Like) — depends on Phase 4 (filter feed has cards to like)
   ↓
Phase 7 (US6 Send + US7 Detail nav) ─┐
                                       ├─ Pure callback-wiring; can run after Phase 2
Phase 8 (US13 Copy + US14 View all     │
        + US8 Profile nav)            ─┘
   ↓
Phase 9 (US9 Spotlight) — largest single phase; mostly independent of Phases 4-8
Phase 10 (US10 Stats + US11 Secret Box) — independent of Phases 4-9
Phase 11 (US12 Top 10) — independent of Phases 4-10
   ↓
Phase 12 (Polish + final QA gate) — depends on EVERY prior phase
```

**MVP path**: Phases 0 → 1 → 2 → 3 → 4 → 5 → 6 → 7. This gives a
fully P1 working hub (auth gated, filtered, swipeable carousel,
likeable, send-Kudos shortcut, detail nav). Phases 8–11 layer
P2+P3 features. Phase 12 gates merge.

**Parallel staffing opportunities**: Phases 4 + 5 (filter and
carousel are disjoint code paths), Phases 9 + 10 + 11 (all
independent body sections). A 3-dev team could cut calendar time
in half on the P2 phases.

**Phase 0 — Setup**: Asset pull, dependency verification, package scaffold.

**Phase 1 — Foundational (Blocking)**: Domain models, sealed
states, repository interface + DEMO impl, Hilt module. Sets up the
data layer that every section depends on.

**Phase 2 — US1 (P1) MVP**: Render the hub end-to-end with
stubbed/empty section content. Establishes the Scaffold + section
ordering + auth gate + pull-to-refresh + Error/Loading/Empty
state-machine pattern. **MVP stop-and-validate point**.

**Phase 3 — US2 (P1) Auth gate**: Wire `AuthRedirectController`
401 handler to redirect to Login; verify session expiry contract.

**Phase 4 — US3 (P1) Filter Highlight + All Kudos**: Hashtag +
Phòng ban bottom-sheet filters; AND-combined query; carousel reset
on filter change; hashtag chip tap re-applies filter; empty
filter-result state.

**Phase 5 — US4 (P1) Highlight Carousel**: HorizontalPager with
5 top cards, page indicator, swipe nav, active/faded state.

**Phase 6 — US5 (P1) Like / unlike**: Heart icon state machine;
optimistic update + rollback on failure; `like_disabled_for_me`
gate (Q-K-5); special-day x2 math; star tier badge render from
server-derived `star_tier`.

**Phase 7 — US6 (P1) Send Kudos shortcut + US7 (P1) View detail**:
Wire `Routes.WRITE_KUDO` callback for the Send Kudos pill; wire
`Routes.KUDOS_DETAIL` for both Highlight + Feed card body taps;
route by `is_anonymous` to anonymous detail variant when needed.

**Phase 8 — US13 (P3) Copy Link + US14 (P2) View all + US8 (P2) Profile nav**:
Clipboard write + toast for both card variants; "View all Kudos"
link → `Routes.KUDOS_FEED`; sender/recipient/Top10 row taps →
`Routes.PROFILE` (self or other variant).

**Phase 9 — US9 (P2) Spotlight Board**: Container + total counter
(reads from `/spotlight/graph` response); pan/zoom canvas using
`Modifier.transformable`; debounced live search with `maxLength = 100`;
empty / loading / interactive states.

**Phase 10 — US10 (P2) Personal stats + US11 (P2) Open Secret Box**:
Stats panel with 5 tiles + conditional x2 fire badge; Open Secret
Box CTA with `disabled` state + single-click suppression + nav to
secret-box flow.

**Phase 11 — US12 (P2) Top 10 latest gift recipients**: List
render + empty state + profile-row tap nav.

**Phase 12 — Polish**: A11y stress tests (every component meets
the spec's contentDescription contract); pull-to-refresh debounce;
final QA gate (lint + ktlint + unit + instrumented + emulator
smoke).

### Risk assessment

| Risk | Probability | Impact | Mitigation |
|---|---|---|---|
| Spotlight pan/zoom is complex; no prior canvas-style impl in the codebase | High | Medium | Phase 9 is sequenced LAST among P2 stories so the team has the parametric / stateless pattern down by then. Compose `Modifier.transformable` + `Canvas` API has well-documented examples — start with a static graph fixture then add gestures. If too costly, MVP-trim to a static SVG image + the total counter (degrade gracefully; Spotlight remains in plan but rendered as a placeholder until a follow-on). |
| 15 conceptual API endpoints require backend work that hasn't shipped | High | High | Build entirely against `DemoKudosRepository` fixtures first. Phase 1 ships the demo impl with ~10 kudos + 5 hashtags + 5 departments + a stub spotlight graph + canonical Top-10 + canonical stats. Production Supabase reads land in a follow-on once backend ships — until then DEMO_MODE is the canonical path. |
| Optimistic reaction with rollback is subtle (race with refresh, server says "already liked") | Medium | Medium | Test coverage: 4 unit test cases for `KudosViewModelTest` — happy path, failure rollback, server-says-already-liked dedupe, refresh-during-flight wins. |
| 5 parallel state machines + filter changes + pull-to-refresh + reactions = high concurrency surface | Medium | Medium | Concurrency rules section in spec is exhaustive; ViewModel uses one `Job` per section, `Job.cancel()` on filter change, `PullRefreshState.isRefreshing` gating. Tests at unit level cover the cancellation contract. |
| `material-icons-extended` may not be in `libs.versions.toml` | Low | Low | Phase 0 T002 verifies + adds if missing — small change. |
| A11y contract is long (12+ contentDescription entries); easy to miss one | Low | Medium | Each section's instrumented test has a `contentDescription` assertion (per the spec's A11y table). Failing tests catch missing labels at Phase-end gate. |

### Estimated complexity

- **Frontend**: **High** — biggest single screen in the project so
  far. 14 USes, ~30 components, 5 parallel state machines, pan/zoom
  canvas + carousel + bottom sheets + pull-to-refresh.
- **Backend**: **Medium-High** — 15 conceptual endpoints across 8+
  tables. Out of scope of this plan (separate work stream); demo
  fixtures unblock client work.
- **Testing**: **High** — 11 test classes planned; ~80-120 tests
  total. Pan/zoom + bottom-sheet + pull-to-refresh are non-trivial
  in Compose UI test (some may need to drop to manual smoke +
  callback assertions like AwardCategoryDropdown's
  outside-tap/focus drops).

---

## Integration Testing Strategy

### Test scope

- **UI ↔ Logic**: `KudosScreenTest` parametric over section state
  (Loading / Empty / Loaded / Error per section). Filter changes
  trigger correct refetch via callback assertions.
- **Service ↔ Service**: `SupabaseKudosRepositoryTest` covers each
  conceptual endpoint via mocked Postgrest gateway. RLS policy
  tests (instrumented, live Supabase test project) prove deny
  paths for unauthenticated / unauthorized roles.
- **Data layer**: `DemoKudosRepositoryTest` pins the DEMO fixture
  shape (~10 assertions) so future copy edits update the test too.
- **User workflows**: One end-to-end test per P1 story spanning
  ViewModel + Repository + Compose stateless content.

### Test categories

| Category | Applicable? | Key scenarios |
|---|---|---|
| UI ↔ Logic | Yes | All US1–US14 acceptance scenarios become Compose UI tests against `KudosScreenContent` |
| Service ↔ Service | Yes | Postgrest contract per endpoint; RLS policy deny |
| App ↔ External API | Yes (deferred) | Live Supabase staging once backend ships |
| App ↔ Data Layer | Yes | DEMO + Supabase repo tests |
| Cross-platform | No | Android-only |

### MoMorph test-case → planned test mapping

The spec references 39 MoMorph test cases (TC_IOS_KUDOS_*).
Each maps 1:1 to a planned test:

| MoMorph cluster | Planned test class |
|---|---|
| `ACC_001..003` (auth gate) | `KudosScreenTest` + existing `AuthRedirectController` unit tests |
| `ACC_004..009` (navigation) | `KudosScreenTest` (callback assertions) + `KudosViewModelTest` (filter+state) |
| `GUI_001..009` (layout + empty / placeholder) | `KudosScreenTest` (parametric over state) + `PersonalStatsPanelTest` + `HighlightFilterRowTest` + `OpenSecretBoxCtaTest` |
| `FUN_001..005` (carousel rules + filters) | `HighlightCarouselTest` + `HighlightFilterRowTest` + `KudosViewModelTest` |
| `FUN_006..010` (heart business rules) | `KudosFeedCardTest` + `HighlightCarouselTest` + `KudosViewModelTest` |
| `FUN_011..013` (Spotlight states + x2 badge) | `SpotlightBoardTest` + `PersonalStatsPanelTest` |
| `FUN_014..018` (Copy Link + heart + hashtag chip) | `KudosFeedCardTest` + `HighlightCarouselTest` |
| `FUN_019..023` (Carousel + filters interaction) | `HighlightCarouselTest` + `HighlightFilterRowTest` |
| `FUN_024..025` (Open Secret Box) | `OpenSecretBoxCtaTest` + `KudosViewModelTest` |
| `FUN_026..029` (Send Kudos nav + Spotlight gestures + image tap) | `KudosScreenTest` + `SpotlightBoardTest` + `KudosFeedCardTest` |
| `FUN_030..033` (cross-component filter / heart side-effects) | `KudosViewModelTest` |
| `FUN_034..036` (Spotlight search validation) | `SpotlightBoardTest` |
| `FUN_037..039` (heart icon + carousel card + secret box state transitions) | `KudosFeedCardTest` + `HighlightCarouselTest` + `OpenSecretBoxCtaTest` |

### Test environment

- **Environment**: Android emulator (emulator-5554) for instrumented
  tests; Robolectric for Compose unit; local Supabase via
  `supabase start` for repository integration.
- **Test data**: `DemoKudosRepository` fixtures (≈10 kudos + 5
  hashtags + 5 departments + 8 spotlight graph nodes + canonical
  stats + 8 top-10 recipients). `setSeedKudos(...)` helper for
  live Supabase integration tests.
- **Isolation**: Hilt test rule replaces module bindings per-test;
  Supabase test project rolled back between tests via
  transactional fixtures.

### Mocking strategy

| Dependency | Strategy | Rationale |
|---|---|---|
| `KudosRepository` (unit + Compose tests) | Fake (`DemoKudosRepository` or hand-rolled per test) | Already exercises realistic payloads; constitution forbids unconditional mocks. |
| Supabase Postgrest (repository integration) | Real (local `supabase start`) | RLS enforced — covers `ACC_001/003` deny paths. |
| Coil image loader | Real (Coil 2.7 `ImageLoader`) with `android.resource://` URIs for DEMO avatars / images | Same decoder path as Award Detail. |
| `SystemClock` for special-day flag | Injectable `Clock` interface | Test sets a fixed instant; production binds to `Clock.systemDefaultZone()`. |

### Coverage goals

| Area | Target | Priority |
|---|---|---|
| VM state machine + repo contract (unit) | 90% lines | High |
| Like/unlike optimistic + rollback | 100% paths | High |
| `like_disabled_for_me` + Q-K-5 enforcement | 100% paths (sender + recipient + others) | High |
| Compose UI render per section per state | All Loaded/Empty/Error paths | High |
| A11y `contentDescription` per component | All entries from spec's A11y table | High |
| Pan/zoom canvas | Smoke-only (gesture testing in Compose is limited) | Medium |
| Pull-to-refresh contract | One end-to-end test (full chain) | High |
| Spotlight live search debounce | Unit test on the Flow + 1 instrumented for input cap | Medium |

---

## Dependencies & Prerequisites

### Required before start

- [x] `constitution.md` reviewed and understood
- [x] `spec.md` ratified (3 review passes 2026-05-12)
- [x] `AwardDetailScreen` pattern shipped — sets the
  stateless-content + ViewModel template this screen follows
- [x] `Routes.KUDOS_OVERVIEW` placeholder route already exists in
  `AppNavigation.kt`; just replace the placeholder
- [x] Auth gate (`AuthRedirectController` + `SessionGate`) already
  shipped — US2 plumbing is reuse
- [ ] **`design-style.md` gate waived** per project convention
  (Constitution Principle II: visual specs fetched via
  `query_section` on-demand; canonical Top Talent and all Award
  delta-plans also operated without `design-style.md`)
- [ ] **`.momorph/contexts/BACKEND_API_TESTCASES.md` does NOT
  exist** in this repo. The spec's § API Requirements section is
  the canonical client-side contract; backend specs live in a
  separate work stream. Plan proceeds without it; treat the spec's
  endpoint list as the source of truth for client work.

### External dependencies

- **Backend API endpoints** (the 15 conceptual endpoints in spec):
  these MUST ship (or be mocked in DEMO mode) for the screen to
  render real data. Plan ships the DEMO path first; Supabase
  production binding is a follow-on once backend is ready.
- **DB migrations**: new tables (`kudos`, `kudos_hashtags`,
  `hashtags`, `departments`, `reactions`, `user_stats`,
  `secret_boxes`, `reward_recipients`, `system_flags`,
  `spotlight_graph` view) need migration files under
  `supabase/migrations/`. Out of scope of this plan.
- **Outbound navigation targets** (US6/7/8/11/12/14):
  - `PV7jBVZU1N` Send Kudos compose — not yet spec'd; US6
    navigates to existing `Routes.WRITE_KUDO` placeholder until it
    ships.
  - `T0TR16k0vH` View Kudo + `5C2BL6GYXL` anonymous variant —
    not yet spec'd; US7 routes to `Routes.KUDOS_DETAIL` placeholder.
  - `j_a2GQWKDJ` All Kudos paginated — not yet spec'd; US14 routes
    to `Routes.KUDOS_FEED` placeholder.
  - `kQk65hSYF2` Open Secret Box — discovered, not implemented;
    US11 routes to a TBD placeholder route (`Routes.SECRET_BOX_OPEN`
    to be added in Phase 0).
  - `hSH7L8doXB` / `bEpdheM0yU` Profile self/other — not yet
    spec'd; US8 + US12 route to existing `Routes.PROFILE`
    placeholder. The single-target-vs-self-other distinction can
    land in a follow-on once Profile specs ship.

---

## Threat Model

Sun*Kudos introduces these new threat surfaces beyond what Home
and Award Detail already cover:

- **New PII columns**: `kudos.message`, `kudos.title`,
  `users.full_name`, `users.avatar_url` — all surfaced in the
  client. Logging contract: NEVER log message body or PII;
  reuse `SecureTimberTree` scrubber (constitution IV).
- **Reactions table**: `unique(user_id, kudos_id)` constraint
  enforces 1-per-user. RLS INSERT policy enforces
  `current_user.id ≠ kudos.sender_id AND current_user.id ≠ kudos.recipient_id`
  (Q-K-5).
- **Anonymous Kudos visibility**: server-side `sender_visible_to_me`
  derivation MUST NOT leak the real `sender_id` to non-recipient
  viewers. The RLS read policy for `kudos` MUST scrub `sender_id`
  to `null` (or to the `anonymous_nickname` placeholder) when
  `current_user.id != recipient_id AND is_anonymous = true`.
  Without this, client-side logic alone is NOT a security
  boundary.
- **Secret Box open**: The `users.me.secret-boxes.open` RPC must
  be idempotent — double-tap protection on the client is a UX
  hint; server MUST atomically transition the box from
  `unopened → opened` with the reward returned in one transaction
  (no double-open even if the request is retried).
- **No new file upload surface** in scope. Image attachments on
  Kudos cards (C.3.6) are READ-ONLY display from server URLs.
  Upload is owned by the Send Kudos compose flow (`PV7jBVZU1N`,
  out of scope here).

Per constitution § Security Requirements, threat model required
because this feature touches PII + new tables. Updated above.

---

## Next Steps

After this plan is approved:

1. **Run `/momorph.tasks`** to generate the task breakdown.
   Expected: ~150-180 tasks across 13 implementation phases
   (Phase 0 – Phase 12). The first task list is the largest in
   the project to date — bigger than canonical Award Detail
   (103 tasks).
2. **Decide on the production backend path**: build entirely
   against `DemoKudosRepository` first (recommended — unblocks
   client work immediately), then layer Supabase production
   binding in a follow-on once backend ships.
3. **MVP scope decision**: Phase 2 (US1) is the MVP — auth gate +
   pull-to-refresh + all sections rendering Empty/Loaded/Error
   state with DEMO data. Stop and validate before sequencing the
   remaining phases.

---

## Notes

### Open questions

- **Q-K-1** (only open question carried from spec): Special-day
  flag source — endpoint vs JWT. Plan punts to the implementer per
  the spec's note. Default proposal: hit `GET /api/v1/system/flags`
  on mount, cache in `KudosUiState.specialDayActive: Boolean`. If
  backend wants to encode in JWT later, this is a one-line change
  in the repository.

### Sequencing rationale

Phases 2–6 cover all P1 stories — these MUST ship together to
make the screen usable. Phases 7–11 are P2 (each independently
shippable). Phase 12 is final polish gating merge to main.

P3 stories (US13 Copy Link) bundled into Phase 8 because the
implementation is a 5-line clipboard write + toast — not worth
its own phase.

### Why no Sun*Kudos delta-spec pattern

Award Detail's delta-spec pattern works because all 6 awards
share the same composable + state machine. Sun*Kudos has 11
sub-screens in MoMorph (compose, detail, all-kudos paginated, etc.)
but those are functionally DIFFERENT screens with their own
state machines and composables — they need their own full specs
when authored. The delta-spec pattern is not applicable here.

### Estimated effort

Based on Award Detail's actual measurements (8 USes, 103 tasks,
1 full session for the canonical spec → impl):

- 14 USes ≈ 1.75x Award Detail's surface
- Each task ≈ similar size to Award Detail tasks
- Total estimate: **150-180 tasks**
- Estimated calendar time (single developer): **2-3 weeks** for
  full impl with tests, assuming DEMO_MODE only (no live backend
  integration). Add **1 week** for Supabase production binding +
  RLS policy tests.

### Cross-document references

- `c-QM3_zjkG-iOS-Award-Top-talent/plan.md` — the canonical
  parametric-screen plan; Sun*Kudos's plan borrows its
  architecture template
- `OuH1BUTYT0-iOS-Home/spec.md` — Home's spec; documents the
  shared chrome (`HomeHeader`, `HomeBottomBar`, `KudosSection`
  card)
- `SCREENFLOW.md` — already updated with Sun*Kudos node + edges
  (commits 82e3af3 + c98af7d)
