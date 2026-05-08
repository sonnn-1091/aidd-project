-- ============================================================================
-- Login feature — Initial users table + RLS (T025)
-- Migration: 20260506_init_users_table
--
-- Creates the Sunner whitelist table that gates Home access. Per Constitution
-- Security Requirements, RLS is enabled in the SAME migration that creates the
-- table. The table is keyed on auth.users(id) so it tracks Supabase's auth
-- identities one-to-one.
-- ============================================================================

create table if not exists public.users (
    id          uuid        primary key references auth.users (id) on delete cascade,
    email       text        not null unique,
    full_name   text,
    created_at  timestamptz not null default now()
);

alter table public.users enable row level security;

-- A signed-in user can read ONLY their own row. This is the mechanism that
-- distinguishes a registered Sunner from a "Google account that authenticated
-- but isn't a Sunner": the latter's row simply doesn't exist, so the SELECT
-- returns empty and the client routes to Access denied.
create policy "users_select_own"
    on public.users
    for select
    using (auth.uid() = id);

-- Sunner provisioning happens server-side (separate admin tool), NOT from the
-- client. Reject all client-originated writes.
create policy "users_no_client_insert"
    on public.users
    for insert
    with check (false);

create policy "users_no_client_update"
    on public.users
    for update
    using (false);

create policy "users_no_client_delete"
    on public.users
    for delete
    using (false);
