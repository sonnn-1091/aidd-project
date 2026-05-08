# Supabase setup — AIDD Project

This directory holds the local Supabase configuration for the Login feature
(`8HGlvYGJWq-iOS-Login`).

## Prerequisites

- [Supabase CLI](https://supabase.com/docs/guides/cli) installed (`supabase --version`).
- Docker running (the local stack uses Postgres + GoTrue + PostgREST in
  containers).
- A Google Cloud OAuth client with the **Android SHA-256** debug cert hash
  registered (Q7 in `plan.md`). You will need its **Web client ID** later.

## First-time local setup

```bash
# From the repo root
supabase start                 # boots local stack on http://localhost:54321
supabase db reset              # applies migrations + seed.sql
```

After `supabase start`, copy the printed `API URL` and `anon key` into
`local.properties` at the repo root:

```properties
supabase.url=http://10.0.2.2:54321        # 10.0.2.2 from the Android emulator
supabase.anonKey=<paste anon key here>
```

> Use `10.0.2.2` (not `localhost`) when running on the Android emulator —
> that is the emulator's loopback to the host machine.

## Configuring Google OAuth

1. In the Supabase dashboard (or `supabase/config.toml` for the local stack),
   enable the **Google** provider under Authentication → Providers.
2. Paste the Google Cloud **Web client ID** as the Supabase `client_id`.
   (We use the Android Credential Manager + Google ID flow, which produces
   ID tokens that Supabase verifies against the Web client ID.)
3. Register the Android app's debug **SHA-256** signing-cert hash in the same
   Google Cloud OAuth client → Android section. Without this, Credential
   Manager will refuse to issue an ID token. Find it via:
   ```bash
   ./gradlew signingReport | grep -A 1 'Variant: debug' | grep SHA-256
   ```
4. Restart the local stack: `supabase stop && supabase start`.

## Migrations

- `migrations/20260506_init_users_table.sql` — creates `public.users`
  (Sunner whitelist) with RLS enabled and `select`-own-row policy.

To author a new migration:

```bash
supabase migration new <slug>
# edit the generated file, then
supabase db reset
```

## Seed data

`seed.sql` provisions:
- `alice@sun-asterisk.com` — full Sunner (`auth.users` + `public.users`).
- `bob@external.example` — Google identity only, no `public.users` row.

Used by the Login integration tests under `app/src/androidTest/`.

## Open questions

See `.momorph/specs/8HGlvYGJWq-iOS-Login/plan.md` § Open Questions:
- **Q4**: CI Supabase strategy — `supabase start` vs hosted ephemeral.
- **Q5**: Cert pinning approach for `network_security_config.xml`.
- **Q7**: SHA-256 cert registration timing.
