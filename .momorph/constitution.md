<!--
SYNC IMPACT REPORT
==================
Version change: (none) → 1.0.0
Action: Initial ratification (file did not exist; created from constitution-template.md)

Principles defined:
  I.   Clean Code & Source Organization
  II.  Tech Stack Best Practices (Android + Supabase)
  III. Platform-Native UI/UX — Material Design 3 (Android)
  IV.  OWASP Secure Coding (NON-NEGOTIABLE)
  V.   Test-Driven Development (NON-NEGOTIABLE)

Added sections:
  - Security Requirements
  - Development Workflow & Quality Gates
  - Governance

Removed sections: (none — initial draft)

Templates checked / updated:
  ✅ .momorph/templates/plan-template.md         — Constitution Check items align (coding conventions, security, testing)
  ✅ .momorph/templates/spec-template.md         — TR-002 (security), accessibility, dependency on constitution.md preserved
  ✅ .momorph/templates/tasks-template.md        — Polish phase already covers security hardening + a11y; TDD ordering enforced
  ⚠ .momorph/guidelines/backend.md               — Document targets Next.js, not Supabase/Android. Flagged for replacement when backend guidelines for Supabase + Kotlin client are authored.
  ⚠ .momorph/guidelines/db_guidelines/*          — All three files are Node ORM guides (Mikro/Mongoose/Prisma). Not applicable to Supabase/Postgres. Flagged for follow-up.

Deferred TODOs: (none)

Notes:
  - Project scope is intentionally Android-only at ratification. Expansion to iOS / Web requires
    a constitution amendment (MINOR bump) to add the corresponding platform UI principle.
-->

# AIDD Project Constitution

## Core Principles

### I. Clean Code & Source Organization

Source code MUST be readable, concise, and organized by feature/domain — not by technical layer
alone. Apply the following non-negotiable rules:

- **Naming**: Identifiers express intent. Classes/composables in PascalCase; functions and
  properties in camelCase; package names in lowercase. No abbreviations except well-known ones
  (e.g., `id`, `url`).
- **Single Responsibility**: One file = one cohesive concern. Composables MUST be small (<150
  LOC); functions MUST do one thing.
- **Package layout**: Feature-first under `com.example.aiddproject.<feature>` with subpackages
  `ui/` (composables, state), `data/` (repository, Supabase client), `domain/` (models, use
  cases). Shared cross-feature code lives under `core/`.
- **No dead code**: Unused parameters, imports, and commented-out blocks MUST be removed before
  merge. Comments explain *why*, never *what*.
- **Formatting**: Kotlin official code style (see `gradle.properties: kotlin.code.style=official`)
  enforced via `ktlint` / Android Studio formatter. No mixed tabs/spaces.

**Rationale**: A predictable structure lets any contributor locate, modify, and test code without
tribal knowledge, and keeps cognitive load proportional to feature complexity rather than
accidental layering.

### II. Tech Stack Best Practices (Android + Supabase)

The chosen stack is fixed at ratification: **Kotlin + Jetpack Compose + Material 3 + Supabase**.
All implementation MUST follow the idiomatic patterns of these technologies:

- **Kotlin**: Prefer immutability (`val`, `data class`, `List` over `MutableList`). Use coroutines
  + `Flow` for async work; no blocking calls on the main dispatcher. Use sealed classes/interfaces
  for finite state.
- **Jetpack Compose**: Stateless composables by default; hoist state. Use `remember` /
  `rememberSaveable` correctly. UI state lives in `ViewModel` exposed as `StateFlow`. No business
  logic inside composables.
- **Android platform**: Respect lifecycle (`viewModelScope`, `lifecycleScope`,
  `repeatOnLifecycle`). Single source of truth for data via repositories. Configuration changes
  must not lose user state.
- **Supabase**: Use the official `supabase-kt` client. **Never** ship the service role key in the
  app — only the anon/publishable key. All authorization MUST be enforced by Postgres
  Row-Level Security (RLS); RLS policies MUST be enabled on every table accessed from the client.
  Auth flows use Supabase Auth (email/OAuth/OTP) — no custom credential storage.
- **Build/dependencies**: All versions managed via `gradle/libs.versions.toml`. No transitive
  pinning in module `build.gradle.kts`. Compose dependencies come from the BOM.

**Rationale**: Sticking to vendor-recommended patterns means upgrades, bug fixes, and security
patches flow in predictably. Custom abstractions over Compose/Supabase tend to drift and become
a liability.

### III. Platform-Native UI/UX — Material Design 3 (Android)

UI MUST conform to **Material Design 3** specifications as published by Google:

- Use Material 3 components (`androidx.compose.material3`) — do not hand-roll equivalents.
- Theming via `MaterialTheme` color/typography/shape tokens. Support **dynamic color (Material
  You)** on Android 12+ and provide a static fallback palette below.
- Support **light and dark themes**; no hard-coded colors in composables.
- Typography uses the M3 type scale; spacing/elevation use M3 tokens.
- **Accessibility (MUST)**: minimum touch target 48×48dp; meaningful `contentDescription` on
  every interactive non-text element; respect system font scaling; meet WCAG 2.1 AA contrast.
  Screens MUST be navigable with TalkBack.
- **Responsiveness**: layouts adapt to phone, foldable, and tablet form factors using
  `WindowSizeClass`. No fixed pixel widths for primary content.

**Rationale**: Following the platform's published guidelines gives users the consistency they
expect and makes accessibility, internationalization, and form-factor support emergent rather
than retrofitted.

### IV. OWASP Secure Coding (NON-NEGOTIABLE)

All code — client and database — MUST adhere to **OWASP Secure Coding Practices** and the
**OWASP Mobile Top 10**:

- **Secrets management**: No API keys, tokens, or credentials in source, version control, or
  shared preferences in plaintext. Local secrets stored only in EncryptedSharedPreferences or
  Android Keystore. CI secrets injected at build time.
- **Network**: TLS-only (`usesCleartextTraffic="false"`); enforce certificate pinning for
  Supabase endpoints in production builds. Validate hostnames.
- **Input validation**: Validate and sanitize all external input (user, network, deep links,
  intents) at trust boundaries. Reject unexpected schemas.
- **AuthN/AuthZ**: Authentication via Supabase Auth only. Authorization enforced server-side via
  RLS — the client MUST NOT be the source of truth for permissions.
- **Data at rest**: Sensitive user data encrypted (Keystore-backed). Disable cloud auto-backup
  for caches/credentials (`android:allowBackup` configured intentionally; sensitive paths
  excluded via `data_extraction_rules.xml` / `backup_rules.xml`).
- **Logging**: Never log PII, tokens, or full request/response bodies. Logs go to Logcat in debug
  builds only; release builds strip verbose logs.
- **Dependencies**: Run dependency vulnerability scans (e.g., `gradle dependencyCheck` or
  Dependabot/Renovate) on every PR. No dependencies with known critical CVEs may merge.
- **Permissions**: Declare the minimum Android runtime permissions required. Request at point of
  use, not at startup.
- **WebView/Deep links**: If used, disable JavaScript by default; verified app links only; no
  unrestricted intent forwarding.

**Rationale**: Mobile clients are an untrusted environment. Treating the device as hostile and
the server as the authority is the only defensible posture; OWASP MASVS encodes this discipline.

### V. Test-Driven Development (NON-NEGOTIABLE)

Tests are written **before** implementation, on the Red → Green → Refactor cycle:

- **Unit tests** (`app/src/test/`): Pure logic, ViewModels, mappers, use cases. JUnit + coroutines
  test dispatcher. Required for every non-trivial function.
- **Instrumented / UI tests** (`app/src/androidTest/`): Compose UI tests
  (`createAndroidComposeRule`) for critical user flows; Espresso only where Compose tests are
  insufficient.
- **Integration tests**: Repository layer tested against a real Supabase test project (or
  `supabase start` local stack) — never against unconditional mocks. RLS policies MUST have
  policy tests proving denied access for unauthenticated/unauthorized roles.
- **Order**: For every user story task, the failing test MUST be committed (or staged) before the
  implementation that makes it pass. Plans and task lists enforce this ordering.
- **Coverage**: Critical user flows (auth, data mutation, payments if added) MUST have both unit
  and instrumented test coverage. No PR merges with newly added untested public functions in
  `domain/` or `data/`.

**Rationale**: TDD prevents regression, surfaces unclear requirements early, and produces a test
suite that is a byproduct of development rather than an afterthought. RLS policy tests are the
only way to verify authorization without shipping it to production users.

## Security Requirements

These augment Principle IV with project-wide constraints:

- Supabase **service role key** is restricted to server-side functions / admin tooling only —
  never bundled, never in CI artifacts shipped to the device.
- Auth sessions persisted via Supabase's built-in storage (Keystore-backed). Token refresh
  handled by the SDK; manual refresh logic is forbidden.
- Database migrations checked into the repo (`supabase/migrations/`); RLS policies are part of
  the migration that creates the table — a table merging without a policy is a release blocker.
- Threat modeling required for any feature handling: authentication, payments, PII, file upload,
  or third-party integrations. Recorded in the feature's `plan.md` under a "Threat Model" section.

## Development Workflow & Quality Gates

The following gates MUST pass before code merges to `main`:

1. **Lint & format**: `./gradlew lint ktlintCheck` (or equivalent) — zero new warnings.
2. **Build**: `./gradlew assembleDebug` succeeds.
3. **Tests**: `./gradlew testDebugUnitTest connectedDebugAndroidTest` — all green. New code MUST
   add tests per Principle V.
4. **Security scan**: Dependency vulnerability scan — no new critical/high CVEs.
5. **Constitution Check**: PR description includes the Constitution Compliance checklist from
   `plan-template.md`. Violations MUST be justified with rejected alternatives.
6. **Code review**: At least one reviewer approval. Reviewer checks: principle compliance,
   accessibility, RLS coverage for new tables, no secrets in diffs.
7. **No `--no-verify` / hook bypass** without an issue link explaining why.

Branching: feature branches off `main`. Squash-merge with conventional commit messages
(`feat:`, `fix:`, `chore:`, `docs:`, `test:`, `refactor:`).

## Governance

This constitution supersedes ad-hoc team conventions. In any conflict between this document and
informal practice, this document wins until amended.

**Amendment procedure**:

1. Open a PR modifying `.momorph/constitution.md` with the proposed change and a Sync Impact
   Report header (see template at top of this file).
2. Document rationale, identify which principle(s) are added/changed/removed, and propagate
   updates into `.momorph/templates/*` and `.momorph/guidelines/*` in the same PR.
3. Bump `Version` per semantic versioning:
   - **MAJOR**: Removing/redefining a principle in a backward-incompatible way.
   - **MINOR**: Adding a principle or materially expanding guidance.
   - **PATCH**: Wording, typo, clarifying refinements with no semantic change.
4. Require at least one approval from a project maintainer to merge.

**Compliance review**: Every feature `plan.md` MUST include a Constitution Compliance Check.
Quarterly, maintainers audit a sample of merged PRs against this constitution and file
follow-ups for drift.

**Runtime guidance**: Day-to-day development guidance lives in `AGENTS.md` (command catalog) and
`.momorph/guidelines/` (per-domain conventions). Those documents MUST stay consistent with this
constitution; on conflict, this document is authoritative.

**Version**: 1.0.0 | **Ratified**: 2026-05-06 | **Last Amended**: 2026-05-06
