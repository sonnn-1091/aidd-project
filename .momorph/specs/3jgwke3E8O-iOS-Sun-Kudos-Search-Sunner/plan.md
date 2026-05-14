# Implementation Plan: Sun*Kudos — Search Sunner (default state)

**Frame**: `3jgwke3E8O-iOS-Sun-Kudos-Search-Sunner`
**Date**: 2026-05-14
**Spec**: `specs/3jgwke3E8O-iOS-Sun-Kudos-Search-Sunner/spec.md`

---

## Summary

Replace the existing `PlaceholderScreen(label = "Search")` at `Routes.SEARCH` (line 193 of `AppNavigation.kt`) with the Search Sunner screen specified by Figma frame `3jgwke3E8O`. All 4 existing `onNavigateToSearch` entry points (Home, Awards, Kudos hub, AwardDetail) start landing on real UI with NO caller-side changes.

Primary requirement (US1 / P1, per spec): a Sunner opens Search Sunner from the header search box, sees their 2 most-recent searched colleagues, taps one, and lands on that colleague's Profile screen. Tap on the X removes a row (US2). Tap "View all" toggles to "Collapse" and shows all 5 (US3). Tapping the search bar transitions to the Searching state (US4) — that sibling frame `hldqjHoSRH` is out of scope here.

**Technical approach**: feature-first under `com.example.aiddproject.kudos.search.{ui, data, domain}`. Hilt-injected `SearchSunnerViewModel` exposes a `StateFlow<SearchSunnerUiState>` populated from a `RecentSunnerRepository` that wraps a per-user DataStore. UI is two composables (thin entry + stateless content) following the established Write-Kudo / Community-Standards split. Reuses the new `ic_back_chevron` drawable from commit `e0a2f5d`, the existing `kudos_kv_bg` background, and `rememberSingleClickHandler` for tap debouncing.

**No backend**: this screen's default state is fully local. The only network operation lives in the sibling Searching-state spec (`hldqjHoSRH`), not yet authored — but this screen ships first and ships cleanly.

Estimated effort: **~8 hours** for one engineer including tests (Phase 0 0.25h + Phase 1 3h + Phase 2 3h + Phase 3 1h + Phase 4 1h ≈ 8.25h). Matches the per-phase breakdown in the Implementation Strategy section below.

---

## Technical Context

**Language/Framework**: Kotlin / Jetpack Compose + Material 3
**Primary Dependencies**: `androidx.compose.material3`, `androidx.compose.foundation`, `androidx.activity.compose` (`BackHandler`), `androidx.lifecycle:lifecycle-viewmodel-compose`, `androidx.hilt:hilt-navigation-compose`, `androidx.datastore:datastore-preferences`, `kotlinx-serialization-json` — all already on classpath via `libs.versions.toml`
**Database**: N/A for backend. DataStore (Preferences) for per-user recent-list persistence.
**Testing**: JUnit 4 + Turbine (StateFlow assertions, already used by `KudosViewModelTest`) + `androidx.compose.ui.test:ui-test-junit4` (instrumented UI test). Hilt test rule for VM tests.
**State Management**: Hilt-injected `SearchSunnerViewModel` exposing `StateFlow<SearchSunnerUiState>`. Repository owns DataStore I/O.
**API Style**: N/A — zero Postgrest/Storage operations on this screen.

---

## Constitution Compliance Check

*GATE: Must pass before implementation can begin. Each item maps to a principle in `.momorph/constitution.md`.*

- [x] **I. Clean Code & Source Organization** — feature-first package `com.example.aiddproject.kudos.search/{ui, data, domain}`. Composables stay well under 150 LOC each (estimated entry ~25 LOC, content ~180 LOC with private sub-composables but each function <60 LOC). Kotlin official style enforced via existing project ktlint. No new cross-feature abstractions; an internal `RecentSunnerRepository` and an internal `RecentSunner` domain model.
- [x] **II. Tech Stack Best Practices** — immutable `data class` for state + the recent-row projection; `MutableStateFlow` + `asStateFlow()` for VM-side exposure; `coroutines + Flow` for repo reads; Hilt for DI; no main-thread blocking. No Supabase client touched on this screen. Versions inherited from `libs.versions.toml` (no new deps).
- [x] **III. Material Design 3 (Android)** — `Scaffold` + `CenterAlignedTopAppBar` + `LazyColumn` from `androidx.compose.material3`. `MaterialTheme` tokens only; light/dark via existing app theme. Touch targets ≥ 48dp via M3 `IconButton` defaults. Back arrow uses the project's `ic_back_chevron` vector. Locale-aware strings via `strings.xml`.
- [x] **IV. OWASP Secure Coding** — recent list is local-only, per-user-keyed. On logout the DataStore key MUST be cleared (spec TR-002 + this plan's Phase 2). No secrets in source. No PII in logs (avatar URL + fullName + dept code treated as PII per OWASP Mobile Top-10 hygiene; the `Avatar` Coil image loader is the only outbound network call from this screen, and it goes through the existing TLS chain). No new auth surface.
- [x] **V. Test-Driven Development** — VM unit tests authored FIRST (recent-list mutation semantics: add, remove, promote, cap at 5; view-all toggle). Repository tests for DataStore round-trips. Then one instrumented Compose UI test for the row-tap → Profile-navigation invocation and X-removal. RLS test N/A (no Postgres reads).

**Violations (if any)**: NONE.

| Violation | Justification | Alternative Rejected |
|-----------|---------------|---------------------|
| *(none)* | — | — |

---

## Architecture Decisions

### Frontend Approach

- **Component Structure**: Two composables under `kudos/search/ui/`, mirroring the established Write-Kudo + Community-Standards split:
  - `SearchSunnerScreen.kt` — thin Hilt entry. Takes `onNavigateBack: () -> Unit` + `onNavigateToProfile: (userId: String) -> Unit`. Collects `viewModel.state` as `StateFlow`. Wires `BackHandler { onNavigateBack() }`. Delegates rendering to `SearchSunnerContent(...)`.
  - `SearchSunnerContent.kt` — stateless content composable. Hosts the `Scaffold` with top-app-bar (back arrow + inactive search-bar pill), the scrollable body (Recent header + LazyColumn of rows + "View all"/"Collapse" toggle), and the bottom-nav. Takes `state: SearchSunnerUiState`, `callbacks: SearchSunnerCallbacks`, `modifier: Modifier`. Easy to render in `@Preview` and in instrumented tests with captured callback counters.

  Why split: matches the established pattern (`WriteKudoScreen` + `WriteKudoScreenContent`, `CommunityStandardsScreen` + `CommunityStandardsContent`). Tests render the content composable directly with a fake state + captured callback lambdas — no Hilt or nav-graph plumbing needed.

  **`SearchSunnerCallbacks` shape** — define as a `data class` to keep the Content parameter list flat (Write Kudo precedent):
  ```kotlin
  data class SearchSunnerCallbacks(
      val onNavigateBack: () -> Unit,
      val onSearchBarTap: () -> Unit,                  // Phase 4 stub Toast → eventually navigate(Routes for hldqjHoSRH)
      val onToggleViewAll: () -> Unit,
      val onRemove: (userId: String) -> Unit,
      val onRowTap: (userId: String) -> Unit,           // triggers VM's addOrPromote + nav event
      val onSelectBottomTab: (HomeNavTab) -> Unit,
  )
  ```
  Six callbacks total. `SearchSunnerScreen` constructs the instance and injects the right lambdas — most go straight to VM methods; `onNavigateBack` + `onSelectBottomTab` route through the caller's `navController`.

- **Styling Strategy**: M3 theme tokens only. The `SaaCream` brand token already exists in `ui/theme/Color.kt` for accents. Pixel-level Figma values (colors of the search-pill, gradient overlay, row spacing) are fetched at implementation time via `query_section` on the listed Node IDs — NOT pre-encoded in this plan.

- **Search-bar implementation**: NOT the M3 `SearchBar` composable. The default state is *inactive* (placeholder + leading magnifying glass), tap-to-transition to the Searching frame. A simpler `Modifier.clickable { onSearchBarTap() }` over a styled `Row` (`Icon` + `Text` placeholder) is sufficient and matches the iOS visual without dragging in M3 `SearchBar`'s full expand/collapse animation. The Searching state's spec will choose whether to upgrade to M3 `SearchBar` at that point.

- **List rendering**: `LazyColumn` for the recent list. Each row is a private `RecentSunnerRow` composable that takes `RecentSunner` + `onRowTap` + `onRemoveTap`. The row's clickable area excludes the X (the X gets its own `IconButton`-handled gesture, no need for explicit stop-propagation in Compose — the `IconButton`'s pointer-input naturally swallows the tap).

- **Avatar rendering**: inline within the row. **No new shared `Avatar` component** introduced — the existing kudos features (`KudosFeedCard`, `TopTenRecipients`, `HighlightCard`, `RecipientPickerOverlay`) each render avatars inline, and refactoring them into a shared component is a separable concern. For the URL load path, use **Coil** (`io.coil-kt:coil-compose`) which is already on the classpath — falls back to `R.drawable.kudos_avatar_recipient` (or equivalent placeholder) on 404. If Coil is NOT yet wired, fall back to `painterResource` on the placeholder drawable + flag a TODO for Coil integration as a follow-up.

- **Data Fetching**: NONE for this screen. The `RecentSunnerRepository` reads from DataStore (local). The eventual live-search query lives in the sibling Searching-state spec.

- **Localization**: copy goes into `app/src/main/res/values/strings.xml` (VN canonical) with the `search_sunner_*` prefix. EN/JA translations deferred to a separate task; Android resource resolution falls back to `values/` for missing keys.

### Backend Approach

N/A — zero backend work for this screen. The repository writes only to DataStore.

### Integration Points

- **Existing services**: `LanguagePreferenceRepository` (at `core/locale/LanguagePreferenceRepository.kt`) is the canonical DataStore example to model `RecentSunnerRepository` after — same `Flow<Preferences>` → mapped state pattern.

  ⚠️ **DataStore Hilt binding gotcha**: `AppModule.kt:52` currently exposes ONE `DataStore<Preferences>` via `provideLanguageDataStore(...)`, backed by the file `language_preferences`. Reusing that instance for recent-Sunner storage would mix unrelated keys into the same preferences file. The correct approach is a **dedicated DataStore** backed by `recent_sunners_preferences` with a `@Qualifier` annotation to disambiguate the Hilt binding. Concretely: add a `@Qualifier @Retention(BINARY) annotation class RecentSunnersDataStore` somewhere under `kudos/search/data/` AND a new `@Provides @RecentSunnersDataStore fun provideRecentSunnersDataStore(@ApplicationContext ctx): DataStore<Preferences>` in either `AppModule.kt` (preferred — co-locates with the existing language provider) OR a new module `kudos/search/data/RecentSunnersDataStoreModule.kt`. The repository then injects via `@RecentSunnersDataStore dataStore: DataStore<Preferences>`. Same pattern Hilt docs recommend for multi-DataStore apps.
- **Shared components**:
  - `ic_back_chevron.xml` (`drawable/`, added in commit `e0a2f5d`) — back arrow.
  - `kudos_kv_bg.png` (`drawable-mdpi/`) — dark KV background; reuse if the Figma frame uses it (verify via `query_section` against frame bg nodes).
  - `HomeBottomBar` (`home/ui/components/HomeBottomBar.kt`) — 4-tab system bottom nav, already used by Home / Awards / Kudos hub. Reuse unchanged.
  - `rememberSingleClickHandler` (`core/ui/`) — double-tap suppression for back-arrow + each recent row + X button.
- **API contracts**: NONE.
- **Navigation graph**: existing `composable(Routes.SEARCH) { PlaceholderScreen(label = "Search") }` at `AppNavigation.kt:193` REPLACED in-place by `composable(Routes.SEARCH) { SearchSunnerScreen(onNavigateBack = { navController.popBackStack() }, onNavigateToProfile = { id -> ... }) }`. No `Routes.kt` change. No call-site changes — all 4 `onNavigateToSearch` callers (Home `HomeScreen.kt:69`, AwardDetail `AwardDetailScreen.kt:35`, KudosScreen `KudosScreen.kt:30`, ... ) continue to fire `navController.navigate(Routes.SEARCH)` exactly as before.

### Profile destination handshake

`Routes.PROFILE = "route_profile"` is currently bound to a `PlaceholderScreen` and has no parameterized helper. Per spec FR-010, the row-tap handler passes `userId` via the destination's `savedStateHandle`:

```kotlin
onNavigateToProfile = { userId ->
    navController.currentBackStackEntry?.savedStateHandle?.set("userId", userId)
    navController.navigate(Routes.PROFILE)
}
```

When the Profile screen ships its own parameterization (likely a `route_profile?userId={userId}` pattern segment), the implementer should align both the Routes constant and this Search-Sunner call site in a single follow-up commit. Flagged as a risk row.

---

## Project Structure

### Documentation (this feature)

```text
.momorph/specs/3jgwke3E8O-iOS-Sun-Kudos-Search-Sunner/
├── spec.md              # Feature specification (exists, reviewed twice, all Q-S-* answered)
├── plan.md              # This file
├── tasks.md             # Generated next by /momorph.tasks
└── (research.md)        # NOT NEEDED — codebase patterns are well-understood (DataStore via Language repo, two-composable split via Write-Kudo, drawable reuse via Community-Standards)
```

### Source Code (affected areas)

```text
app/src/main/java/com/example/aiddproject/kudos/search/                     NEW PACKAGE
├── domain/
│   └── RecentSunner.kt                              NEW — projection data class
├── data/
│   ├── RecentSunnerRepository.kt                    NEW — DataStore-backed CRUD (interface + impl)
│   └── RecentSunnerRepositoryModule.kt              NEW — Hilt binding (@Module @InstallIn(SingletonComponent::class))
└── ui/
    ├── SearchSunnerScreen.kt                        NEW — thin Hilt entry composable; BackHandler + delegates to Content
    ├── SearchSunnerContent.kt                       NEW — stateless content; Scaffold + top-app-bar + LazyColumn body + bottom-nav
    ├── SearchSunnerUiState.kt                       NEW — data class for the screen's state (recentSunners, isViewingAll)
    ├── SearchSunnerViewModel.kt                     NEW — @HiltViewModel; collects repo Flow, exposes StateFlow, handles intents
    └── SearchSunnerTestTags.kt                      NEW — testTag constants

app/src/main/java/com/example/aiddproject/navigation/
└── AppNavigation.kt                                 MODIFIED — replace 1 PlaceholderScreen line with real Screen call

app/src/main/res/values/
└── strings.xml                                      MODIFIED — append search_sunner_* keys (~10 entries)

app/src/test/java/com/example/aiddproject/kudos/search/                     NEW UNIT TESTS
├── data/
│   └── RecentSunnerRepositoryTest.kt                NEW — DataStore round-trips (add, remove, promote, cap, clear-on-logout)
└── ui/
    └── SearchSunnerViewModelTest.kt                 NEW — state-flow assertions for all intents

app/src/androidTest/java/com/example/aiddproject/kudos/search/              NEW INSTRUMENTED TEST
└── ui/
    └── SearchSunnerScreenTest.kt                    NEW — Compose UI test (5-7 assertions)
```

### New Files

| File | Purpose | Estimated LOC |
|------|---------|---------------|
| `kudos/search/domain/RecentSunner.kt` | `data class RecentSunner(userId, fullName, departmentName?, avatarUrl?, lastSearchedAt)`. | ~15 |
| `kudos/search/data/RecentSunnerRepository.kt` | `interface RecentSunnerRepository` + `DefaultRecentSunnerRepository` impl. Constructor injects `@RecentSunnersDataStore dataStore: DataStore<Preferences>` + `authRepository: AuthRepository`. Methods: `observeAll(): Flow<List<RecentSunner>>`, `addOrPromote(node: SunnerNode)` (or a simpler `RecentSunner` payload — implementer's call), `remove(userId: String)`, `clear()` (called on logout). Persists the list as a single JSON string via `kotlinx-serialization` into a Preferences key namespaced by `currentUserId`. | ~120 |
| `kudos/search/data/RecentSunnerRepositoryModule.kt` | Hilt `@Module @InstallIn(SingletonComponent::class)`. Binds `RecentSunnerRepository` → `DefaultRecentSunnerRepository`. **Also defines** the `@Qualifier annotation class RecentSunnersDataStore` and the `@Provides @RecentSunnersDataStore @Singleton fun provideRecentSunnersDataStore(@ApplicationContext context: Context): DataStore<Preferences>` provider, backed by the file `recent_sunners_preferences` (distinct from the existing `language_preferences` file). | ~50 |
| `kudos/search/ui/SearchSunnerUiState.kt` | `data class SearchSunnerUiState(recentSunners: List<RecentSunner>, isViewingAll: Boolean)`. Computed `visibleSunners: List<RecentSunner>` getter that returns the first 2 when `!isViewingAll`, else the full list. | ~25 |
| `kudos/search/ui/SearchSunnerViewModel.kt` | `@HiltViewModel class SearchSunnerViewModel @Inject constructor(private val repo, private val authRepo): ViewModel()`. `init {}` block launches `repo.observeAll().collect { update _state }`. Intents: `onToggleViewAll()`, `onRemove(userId)`, `onRowTap(userId)` (callbacks invoke `onNavigateToProfile`; row promotion to position 0 happens on commit via `repo.addOrPromote()` BEFORE the navigation). | ~80 |
| `kudos/search/ui/SearchSunnerScreen.kt` | Thin Hilt entry: `@Composable fun SearchSunnerScreen(onNavigateBack, onNavigateToProfile, viewModel = hiltViewModel())`. Wires `BackHandler`, delegates to Content. | ~25 |
| `kudos/search/ui/SearchSunnerContent.kt` | Stateless content composable. Scaffold + top-app-bar (back + inactive search-bar pill) + scrollable body (Recent label + LazyColumn + "View all"/"Collapse" toggle) + `HomeBottomBar`. Includes 3 private sub-composables (`InactiveSearchBar`, `RecentSection`, `RecentSunnerRow`) + 2 `@Preview` wrappers (light/dark). | ~180 |
| `kudos/search/ui/SearchSunnerTestTags.kt` | TestTag constants (`SCREEN`, `BACK_BUTTON`, `SEARCH_BAR`, `RECENT_LABEL`, `VIEW_ALL_BUTTON`, `recentRowTag(userId)`, `removeButtonTag(userId)`). | ~20 |
| `test/.../RecentSunnerRepositoryTest.kt` | Unit tests: add new item, add-existing promotes, remove drops + persists, cap-at-5 evicts oldest, clear() empties the store. Uses an in-memory `TestDataStore`. | ~120 |
| `test/.../SearchSunnerViewModelTest.kt` | Unit tests: state emits initial empty, state updates on repo change, `onToggleViewAll` flips boolean, `onRemove` calls repo + state updates, `onRowTap` promotes + emits one-shot navigation event (via SharedFlow). Uses Turbine. | ~120 |
| `androidTest/.../SearchSunnerScreenTest.kt` | Instrumented UI: title renders, back button has correct contentDescription, 2 rows render by default with `isViewingAll = false` + size = 3, "View all" button visible and tap flips to "Collapse" + shows all 3 rows, tap row → captures onNavigateToProfile, tap X → captures onRemove, empty list hides Recent label + View all button. | ~150 |

### Modified Files

| File | Change |
|------|--------|
| `navigation/AppNavigation.kt:193` | Swap `PlaceholderScreen(label = "Search")` body for `SearchSunnerScreen(onNavigateBack = { navController.popBackStack() }, onNavigateToProfile = { id -> navController.currentBackStackEntry?.savedStateHandle?.set("userId", id); navController.navigate(Routes.PROFILE) }, onSelectBottomTab = { tab -> /* same `when (tab) { Saa2025 → popBackStack(HOME); Awards → navigate(AWARDS_OVERVIEW); Kudos → navigate(KUDOS_OVERVIEW); Profile → navigate(PROFILE) }` block already used by Write Kudo in commit d82553e */ })`. Add `import com.example.aiddproject.kudos.search.ui.SearchSunnerScreen` and `import com.example.aiddproject.home.ui.components.HomeNavTab`. |
| `res/values/strings.xml` | Append ~10 string resources under a single comment-delimited block: `search_sunner_placeholder`, `search_sunner_section_recent`, `search_sunner_view_all`, `search_sunner_collapse`, `search_sunner_coming_soon` (Toast for the deferred Searching-state transition — see Phase 4 step 1), `a11y_search_sunner_back`, `a11y_search_sunner_row` (formatted with name + dept), `a11y_search_sunner_remove` (formatted with name), `a11y_search_sunner_search_bar`. |

### Dependencies

| Package | Version | Purpose |
|---------|---------|---------|
| *(none new)* | — | DataStore, Hilt, Compose M3, navigation-compose, coroutines + Flow, kotlinx-serialization all already on classpath. Coil-compose is already on classpath for avatar loading. |

---

## Implementation Strategy

### Phase Breakdown

#### Phase 0 — Asset Audit (~15 min)

- Query Figma via `query_section` against frame bg nodes (`6891:21273` + children) to confirm whether the dark KV background of this screen IS the existing `kudos_kv_bg.png` (visual hash compare) or a different asset. If same → reuse; if different → download via the `get_media_files` presigned URL flow used for the Root Further asset in commit `e0a2f5d`.
- Confirm whether the back arrow uses the project's `ic_back_chevron` drawable — it does per FR-013 and the screen's Node `6891:21281` is a placeholder of the same convention as Community Standards (`6885:10825`).
- Confirm whether a magnifying-glass search icon needs a new drawable, or whether `Icons.Filled.Search` from Material icons is acceptable. The Figma node `6891:22074` is the search-bar instance with placeholder text; the icon detail is decided at implementation time via `query_section`. Default to `Icons.Filled.Search` unless Figma calls for a custom vector.
- Check whether a placeholder avatar drawable exists. `kudos_avatar_recipient.png` (used by Viết Kudo's `RecipientPickerOverlay`) is the default fallback for the row avatars when the loaded URL fails. No new asset required.

#### Phase 1 — Foundation: domain + data + strings + failing tests (~3 h)

TDD order per Constitution V — write failing tests FIRST.

1. Create the new package directories (main + test + androidTest).
2. Author the `RecentSunner` domain model. Include a `kotlinx-serialization` `@Serializable` annotation so the repository can JSON-encode it for DataStore. Add a `lastSearchedAt: Instant` field for ordering — serialize via `kotlinx-datetime` if already on classpath, else a `Long` epoch-millis.
3. Append the ~10 string resources to `strings.xml` under a `<!-- Search Sunner (Figma frame 3jgwke3E8O) -->` block. Sources:
   - Node `I6891:22074;28:2014` ("Search Sunner") → `search_sunner_placeholder`
   - Node `6891:22079` ("Recent") → `search_sunner_section_recent`
   - Node `I6891:22081;72:2029` ("View all ") → `search_sunner_view_all` (trim trailing space)
   - NEW key not in Figma: `search_sunner_collapse` = "Collapse" (VN: "Thu gọn")
   - NEW key not in Figma: `search_sunner_coming_soon` = "Tính năng đang được phát triển" (Toast text for the deferred Searching-state transition — see Phase 4 step 1)
   - NEW a11y keys: `a11y_search_sunner_back` = "Quay lại", `a11y_search_sunner_search_bar` = "Tìm kiếm Sunner", `a11y_search_sunner_row` (formatted), `a11y_search_sunner_remove` (formatted)
4. Write `RecentSunnerRepositoryTest.kt` — failing tests covering: empty initial state, add-new-then-observe emits, add-existing promotes to head, remove + observe emits without that row, cap-at-5 evicts oldest on 6th add, `clear()` empties the store. Use an in-memory DataStore harness (the project's `LanguagePreferenceRepository` tests demonstrate the pattern; reuse `PreferenceDataStoreFactory.create()` against a tmp file).
5. Write `SearchSunnerViewModelTest.kt` — failing tests covering: initial state has `recentSunners = []` + `isViewingAll = false`; collecting repo's flow updates state; `onToggleViewAll()` flips the boolean; `onRowTap(id)` calls `repo.addOrPromote()` THEN emits a one-shot `NavigateToProfile(id)` event via `SharedFlow`; `onRemove(id)` calls `repo.remove()`. Use Turbine for assertions.
6. Verify tests FAIL with `./gradlew :app:testDebugUnitTest --tests "com.example.aiddproject.kudos.search.*"`.

#### Phase 2 — User Story 1 (P1) — Open a recently-searched colleague's profile (~3 h)

1. Author the Hilt module `RecentSunnerRepositoryModule.kt`:
   - Define the qualifier: `@Qualifier @Retention(BINARY) annotation class RecentSunnersDataStore`.
   - `@Provides @Singleton @RecentSunnersDataStore fun provideRecentSunnersDataStore(@ApplicationContext context: Context): DataStore<Preferences> = PreferenceDataStoreFactory.create(produceFile = { context.preferencesDataStoreFile("recent_sunners_preferences") })` — mirror the existing `provideLanguageDataStore` pattern at `AppModule.kt:52` but with the qualifier + distinct file name.
   - Bind `RecentSunnerRepository` → `DefaultRecentSunnerRepository`.
2. Implement `RecentSunnerRepository.kt` until the data-layer tests go green:
   - Inject `@RecentSunnersDataStore dataStore: DataStore<Preferences>` (NOT the unqualified version, which is bound to language preferences and would mix unrelated keys).
   - Inject `AuthRepository` to read the current user's ID — used as part of the DataStore key (`recent_sunners_${currentUserId}`).
   - Encode the list as a single JSON string via `kotlinx-serialization` and write it to a `stringPreferencesKey("recent_sunners_$userId")`. On `clear()` (called by an auth-observer when logout fires), remove the key.
   - `addOrPromote(node)` updates `lastSearchedAt = Clock.System.now()` (via `kotlinx-datetime`) and writes the full list back, capped at 5 entries (drop the OLDEST per `lastSearchedAt`).
3. Implement `SearchSunnerUiState.kt` with the computed `visibleSunners` getter.
4. Implement `SearchSunnerViewModel.kt`:
   - In `init {}`: launch a coroutine in `viewModelScope` that collects `repo.observeAll()` and mirrors into the `_state` MutableStateFlow.
   - `onToggleViewAll()`: flips `isViewingAll`.
   - `onRemove(userId)`: launches `repo.remove(userId)`.
   - `onRowTap(userId)`: launches `repo.addOrPromote(...)` BEFORE the navigation event fires, so the recent list's promotion is persisted before Profile mounts. Then emit `NavigateToProfile(userId)` on a `SharedFlow` (or use a callback parameter — simpler).
5. Implement `SearchSunnerContent.kt`:
   - `Scaffold` with `topBar` = a `CenterAlignedTopAppBar` containing: navigationIcon = back IconButton (`ic_back_chevron`), title = an `InactiveSearchBar` private composable that renders `Icons.Filled.Search` + "Search Sunner" placeholder text inside a clickable styled `Row`. Tap → `callbacks.onSearchBarTap()` (which navigates to the Searching frame; for MVP, no-op + TODO since the sibling spec is not yet shipped — see Risk row).
   - Body: `Column(verticalScroll(rememberScrollState()))` with: an optional `RecentSection` composable (rendered only when `state.recentSunners.isNotEmpty()`) containing a `Row { Text("Recent"); Spacer(weight 1f); TextButton(onClick = onToggleViewAll) { Text(if (isViewingAll) "Collapse" else "View all") } }`, then a `LazyColumn` of `state.visibleSunners`, each rendered as a private `RecentSunnerRow`.
   - Bottom bar: `HomeBottomBar(selected = HomeNavTab.Kudos, onTabSelect = callbacks.onSelectBottomTab)` — see Risk: which tab is "active" is ambiguous; default to `Kudos` since the screen lives under the kudos package.
   - `@Preview` wrappers (light/dark) at the bottom.
6. Implement `SearchSunnerScreen.kt` — thin entry: collects `viewModel.state`, wires `BackHandler`, observes the one-shot nav events, and delegates to `SearchSunnerContent`. Hilt-injected.
7. Implement `SearchSunnerTestTags.kt`.
8. Wire `AppNavigation.kt:193` — swap the placeholder. Wire `onNavigateToProfile` per the `savedStateHandle` pattern.
9. Run unit tests + build green.
10. **Manual smoke test**: install debug APK, navigate Home → tap header search → land on Search Sunner with empty state. (No recent items will exist until US1 row taps populate the store; the live-search state isn't shipped yet.)

#### Phase 3 — User Story 2 (P2) — Remove a stale colleague + US3 — Expand/collapse (~1 h)

1. Verify the `onRemove` + `onToggleViewAll` paths from Phase 2 are wired through to the row's X-icon `IconButton` and the "View all"/"Collapse" `TextButton`.
2. Add the instrumented UI test `SearchSunnerScreenTest.kt`:
   - Top-bar title (the inactive search bar's "Search Sunner" placeholder is visible).
   - Back button is the first focusable element + has `contentDescription = "Quay lại"`.
   - With `state.recentSunners.size = 3` and `isViewingAll = false`: only 2 rows render; "View all" button is visible.
   - Tap "View all" → `onToggleViewAll` callback fires exactly once (captured counter); the test re-renders with `isViewingAll = true`; assert all 3 rows now appear AND the button label changes to "Collapse" (string-resource lookup).
   - Tap an X icon on any row → captures `onRemove(userId)` callback exactly once.
   - Tap a row body (anywhere except the X) → captures `onNavigateToProfile(userId)` callback exactly once.
   - With `state.recentSunners.isEmpty()`: Recent label + View all button are NOT in the composition tree.
3. Run instrumented test on emulator: `./gradlew :app:connectedDebugAndroidTest --tests "com.example.aiddproject.kudos.search.ui.SearchSunnerScreenTest"`.

#### Phase 4 — User Story 4 (P1) — Search-bar tap transition + Polish (~1 h)

1. Wire the search-bar tap to a callback. For MVP (sibling Searching-state spec not yet authored): the callback fires a non-blocking `Toast.makeText(context, R.string.search_sunner_coming_soon, Toast.LENGTH_SHORT).show()` (adds the string key to `strings.xml`; suggested VN copy: "Tính năng đang được phát triển"). Document the deferred wiring in a `// TODO(searching-state, spec hldqjHoSRH)` comment in the Screen entry composable.
   Alternative: defer the entire US4 acceptance until the sibling spec lands. **Recommended path** for shipping THIS screen alone: ship with the Toast stub, then replace the stub with `navController.navigate(Routes.SEARCH_ACTIVE)` (or whatever the sibling spec names) once `hldqjHoSRH` is implemented. The instrumented test for US4 asserts only that the callback fires (verifies wiring), not the destination behavior — that contract belongs to the sibling spec's tests.
2. TalkBack walk-through on a physical device covering all 4 acceptance scenarios in US1 + the remove + view-all flows.
3. 200% system font scale check — confirm no truncation, scroll handles overflow, X stays right-aligned.
4. Locale toggle while on screen — confirm strings re-render in the active locale.
5. Visually review the light + dark `@Preview` decorators against the Figma frame; flag any deltas as fixup tasks.
6. **Logout-clearing**: add a hook in the existing logout flow (probably `AuthRepository.signOut()` or its caller) that calls `recentSunnerRepository.clear()`. This is a small change in the auth feature, not in this package — needs cross-feature coordination, flagged as a risk row.

### Risk Assessment

| Risk | Probability | Impact | Mitigation |
|------|-------------|--------|------------|
| `Routes.PROFILE` evolves (gains parameterization) and breaks the `savedStateHandle` workaround | High | Medium | Document the workaround in code with a `// TODO(profile-spec): when Profile screen ratifies its parameterization scheme, refactor this call site.` Open a tracking issue. |
| Sibling Searching-state spec (`hldqjHoSRH`) not yet authored — US4 transition has nowhere to navigate at MVP | High | Low | Ship the search-bar tap as a no-op + Toast for MVP. US4's acceptance scenarios test the *trigger*, not the destination. Backfill the transition when `hldqjHoSRH` ships. |
| `RecentSunnerRepository.clear()` on logout — wiring it into the auth flow is a cross-feature change | Medium | Medium | Phase 4 step 6 covers this. The auth feature has an existing logout entry point; add a single line + DI binding. If the auth surface is touchy, gate the change behind a follow-up commit and document as a Phase 4 deliverable rather than a Phase 1 prerequisite. |
| Bottom-nav "active tab" ambiguity — which tab should light up when on Search Sunner? | Medium | Low | Default to `HomeNavTab.Kudos` (the screen lives under the kudos package). PM hasn't ruled on this; if they want "no active tab", a follow-up tweak passes `null` or an unselected sentinel. |
| Avatar URLs return 404 / no Coil setup | Low | Low | Fall back to `R.drawable.kudos_avatar_recipient` via `placeholder`/`error` params of Coil's `AsyncImage`. If Coil isn't wired, swap to `painterResource` on the fallback drawable and TODO the URL load. |
| Q-S-5's parent-spec staleness — the Viết Kudo spec line 720 still claims "Search Sunner ... prefill recipient" | Medium | Low | This plan does NOT touch the parent spec. Cleanup is a separate small edit (1 line) to `7fFAb-K35a/spec.md`. Risk is documentation drift, not code correctness. |
| DataStore key namespacing across users — accidental cross-user leak | Low | High | Key includes `currentUserId`; on logout we call `clear()` AND on next login we re-read from the new key. Unit test: simulate user A populating, then user B login, then assert user B's recent list is empty. |

### Estimated Complexity

- **Frontend**: **Medium** — 2 composables + 4 sub-composables + LazyColumn + bottom nav. ~205 LOC of Compose. Straightforward.
- **Backend**: **N/A** — no backend work.
- **Data layer**: **Low-Medium** — DataStore wiring + JSON serialization + per-user key namespacing. ~120 LOC + Hilt module. Established pattern via `LanguagePreferenceRepository`.
- **Testing**: **Medium** — 2 unit test files + 1 instrumented UI test. ~390 LOC total.

**Total estimated effort**: **~8 hours** for one engineer including all phases + tests + PR.

---

## Integration Testing Strategy

### Test Scope

- [x] **Component/Module interactions**: `SearchSunnerScreen` ↔ `SearchSunnerContent` (callbacks propagation), VM ↔ Repository (state mirroring + intent dispatch).
- [x] **External dependencies**: DataStore (round-trip via in-memory test harness).
- [ ] **Backend services**: NONE — this screen has none.
- [x] **User workflows**: Header search → Search Sunner → tap recent row → Profile. Verifies the integration of the Header search trigger (existing call sites) with the new screen.

### Test Categories

| Category | Applicable? | Key Scenarios |
|----------|-------------|---------------|
| UI ↔ Logic | **Yes** | Tap row body → `onNavigateToProfile`; tap X → `onRemove`; tap "View all" → toggle state. |
| Service ↔ Service | No | No service-to-service calls. |
| App ↔ External API | No | No external APIs. |
| App ↔ Data Layer | **Yes** | Repo writes + reads from DataStore; cap-at-5 + promote semantics; clear-on-logout. |
| Cross-platform | No | Android-only. |
| **A11y** | **Yes** | TalkBack reads row as "Avatar [Name], [Department], button"; X exposes "Xoá [Name] khỏi danh sách gần đây"; back arrow is first focusable. |

### Test Environment

- **Environment type**: Local emulator (Pixel 5a API 33+) for instrumented tests; JVM JUnit for VM + repo unit tests.
- **Test data strategy**: Repo unit tests use a fresh tmp DataStore per test (`@TempDir`). VM tests inject a fake repository (in-memory list). UI tests render `SearchSunnerContent` directly with hand-crafted state — no Hilt rule needed for the content tests; only the full-screen integration test needs `HiltAndroidRule`.
- **Isolation approach**: Per-test fresh state. No shared in-memory store across tests.

### Mocking Strategy

| Dependency Type | Strategy | Rationale |
|-----------------|----------|-----------|
| `RecentSunnerRepository` (in VM tests) | Fake (in-memory `MutableStateFlow<List<RecentSunner>>`) | Verify intent dispatch without DataStore plumbing in VM tests. |
| `AuthRepository.currentUserId` (in repo tests) | Stub returning a fixed `"user-A"` | Validate per-user keying without an auth dependency. |
| Compose `onNavigateToProfile` / `onRemove` | Captured lambdas counted by `mutableStateOf<Int>` | Verify exact invocation count + arguments. |
| Coil avatar load | Real if Coil is wired; otherwise placeholder painter | Real loading isn't asserted by tests; URL fallback is visual. |
| Navigation graph | Direct content composable rendering | Full `TestNavHostController` only needed if we test the AppNavigation swap end-to-end (optional, deferred). |

### Test Scenarios Outline

1. **Happy Path (UI test)**
   - [ ] Top-app-bar inactive search-bar shows "Search Sunner" placeholder.
   - [ ] With `recentSunners.size = 3` and `isViewingAll = false`: exactly 2 rows render; "View all" button visible.
   - [ ] Tap "View all" → state.isViewingAll flips to true; all 3 rows render; button reads "Collapse".
   - [ ] Tap "Collapse" → state flips to false; back to 2 rows; button reads "View all".
   - [ ] Tap row body → `onNavigateToProfile(userId)` invoked exactly once with the correct userId.
   - [ ] Tap X icon → `onRemove(userId)` invoked exactly once; row body's tap NOT fired.
   - [ ] With `recentSunners.isEmpty()` → Recent label + View all button are absent from the composition.

2. **Data Layer (repo test)**
   - [ ] Empty initial observe emits `[]`.
   - [ ] Add new → observe emits a list of 1; entry's `lastSearchedAt` is set.
   - [ ] Add existing → observe emits same size, entry's `lastSearchedAt` updated, entry moved to head.
   - [ ] Add 6 distinct → observe emits 5 (oldest evicted).
   - [ ] Remove → observe emits without the removed row, persists across new repo instance.
   - [ ] Clear → observe emits `[]`, persists.
   - [ ] User-A populates, switch `currentUserId` to user-B, observe emits `[]` (cross-user isolation).

3. **A11y**
   - [ ] Back IconButton's contentDescription matches `R.string.a11y_search_sunner_back`.
   - [ ] First focusable element is the back arrow.
   - [ ] Each row's content description merges name + department (formatted via `R.string.a11y_search_sunner_row`).
   - [ ] X button's contentDescription is the formatted remove-string with the user's name.

4. **Edge Cases**
   - [ ] Concurrent removal: tap X on row A and row B in rapid succession — both rows disappear, no race.
   - [ ] 200% font scale: rows wrap vertically, X stays right-aligned, scroll handles overflow. (Manual / Android Studio Layout Inspector.)
   - [ ] Dark mode rendering: visual `@Preview` review.
   - [ ] Locale toggle: re-render in EN/JA → falls back to VN copy per Android resource resolution.

### Tooling & Framework

- **Test framework**: JUnit 4 + Turbine (already on classpath for `KudosViewModelTest`) + `androidx.compose.ui.test:ui-test-junit4`.
- **Supporting tools**: `androidx.test.ext:junit`, `androidx.test:runner`. `HiltAndroidRule` only for the optional full-screen integration test.
- **CI integration**: existing Gradle `:app:testDebugUnitTest` + `:app:connectedDebugAndroidTest` tasks pick up the new test files automatically.

### Coverage Goals

| Area | Target | Priority |
|------|--------|----------|
| Repo CRUD + cap-at-5 + clear semantics | 100% | High |
| VM intent dispatch + state emissions | 100% | High |
| UI: tap-row + tap-X + view-all toggle | 100% | High |
| UI: empty state | 100% | High |
| Visual regression | Manual `@Preview` review | Medium |

---

## Dependencies & Prerequisites

### Required Before Start

- [x] `constitution.md` reviewed — Principles I-V all addressed in the compliance section above.
- [x] `spec.md` approved (reviewed twice, all 8 Q-S-* answered by PM/Design on 2026-05-14).
- [ ] `research.md` — **NOT REQUIRED**. Codebase patterns are well-understood: DataStore via `LanguagePreferenceRepository`, two-composable split via Write-Kudo, drawable reuse via Community-Standards, Hilt VM pattern via every existing screen.
- [ ] API contracts — N/A (no APIs).
- [ ] Database migrations — N/A.

### External Dependencies

- **None at MVP**. The future Searching-state spec (`hldqjHoSRH`) will introduce a Sunner-search endpoint, but ships separately.

---

## Next Steps

After plan approval:

1. **Run** `/momorph.tasks` to generate the granular task breakdown. Expected ~12-14 atomic tasks: package scaffolding → strings → failing repo tests → repo impl + Hilt module → failing VM tests → VM impl → Content composable + sub-composables → Screen entry + nav wire → UI tests → manual smoke test → polish (logout-clear hook, TalkBack walk, dark/light previews) → ship.
2. **Review** `tasks.md` for ordering. Repo + VM tests can run in parallel during Phase 1 once the domain model + strings are in place (the [P] marker on tasks.md captures this). UI implementation is serial after repo+VM are green.
3. **Begin** implementation in the order tasks.md prescribes. Commit per task per the user's `feedback_commit_per_task` memory.

---

## Notes

- **No `research.md`** — the codebase patterns are already well-rehearsed across the recent Viết Kudo + Community-Standards work. If the implementer hits an unexpected codebase quirk during Phase 1, they may pause and author research.md then; not anticipated.
- **No `testcase.md`** — the spec's Acceptance Scenarios in US1–US4 + the Integration Testing Strategy in this plan already define the test contract. Duplication would not help.
- **No `contract.md`** — feature has zero API contracts.
- **Asset-download deferred** — Phase 0 only checks whether the existing `kudos_kv_bg.png` matches Figma's background; if not, downloads via the same `get_media_files`-presigned-URL flow used for the Root Further banner in commit `e0a2f5d`. The back arrow + magnifying-glass icon + avatar placeholder are all expected to be reused from existing drawables.
- **Parent Viết Kudo spec cleanup** — line 720 of `7fFAb-K35a/spec.md` still contains the now-stale "Search Sunner ... prefill recipient" reference per Q-S-5 resolution. Tracked as a documentation follow-up in the Risk Assessment row; NOT a blocker for this plan to proceed.
- **`Routes.PROFILE` is also a placeholder** — `AppNavigation.kt:194` binds it to `PlaceholderScreen(label = "Profile")`. The tap-row → Profile navigation will land on the placeholder until the Profile screen ships. This is acceptable for MVP because the trigger is testable (callback invocation) even if the destination is a placeholder. When the Profile screen ships, no changes are needed here — the `savedStateHandle["userId"]` write will already be consumed by whatever Profile's eventual implementation expects.
- **The four `onNavigateToSearch` call sites already wire the entry**: `HomeScreen.kt:69`, `AwardDetailScreen.kt:35`, `KudosScreen.kt:30`, plus the AwardDetailScreenContent indirect at `:127`. After this work, all four start landing on the real Search Sunner screen with zero call-site changes.
