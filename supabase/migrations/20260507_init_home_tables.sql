-- ============================================================================
-- Home feature — Awards / Kudos settings / Notifications tables + RPCs (T011)
-- Migration: 20260507_init_home_tables
--
-- Creates the three tables Home reads from on STARTED:
--   public.awards                  — carousel content (FR-003, FR-004)
--   public.kudos_settings          — single-row server-side feature flag + Kudos
--                                    block content (FR-005, FR-006)
--   public.notifications           — per-user notifications (FR-009, FR-010)
--
-- Plus two RPCs the client calls instead of touching the tables directly:
--   notifications_summary()        — returns { unread_count }
--   kudos_summary()                — returns the kudos_settings row
--
-- Per Constitution Security Requirements, RLS is enabled in the SAME migration
-- that creates each table — a table merging without a policy is a release
-- blocker. All client writes are blocked; provisioning happens server-side
-- through a separate admin tool.
-- ============================================================================

-- ----------------------------------------------------------------------------
-- public.awards — carousel content
-- ----------------------------------------------------------------------------
create table if not exists public.awards (
    id            uuid        primary key default gen_random_uuid(),
    name          text        not null,
    thumbnail_url text,
    sort_order    int         not null default 0,
    created_at    timestamptz not null default now()
);

alter table public.awards enable row level security;

-- Any authenticated Sunner can read the awards catalogue. SessionGate already
-- prevents Home from rendering for unauthenticated callers, but RLS is the
-- authoritative gate (Constitution Principle IV).
create policy "awards_select_authenticated"
    on public.awards
    for select
    using (auth.role() = 'authenticated');

create policy "awards_no_client_insert"
    on public.awards
    for insert
    with check (false);

create policy "awards_no_client_update"
    on public.awards
    for update
    using (false);

create policy "awards_no_client_delete"
    on public.awards
    for delete
    using (false);

-- ----------------------------------------------------------------------------
-- public.kudos_settings — single-row server-side feature flag + content
-- ----------------------------------------------------------------------------
create table if not exists public.kudos_settings (
    id                   smallint    primary key default 1,
    is_kudos_available   boolean     not null default false,
    banner_url           text,
    badge_text           text,
    description_text     text        not null default '',
    updated_at           timestamptz not null default now(),
    -- Lock the table to a single row so client reads are deterministic.
    constraint kudos_settings_singleton check (id = 1)
);

alter table public.kudos_settings enable row level security;

create policy "kudos_settings_select_authenticated"
    on public.kudos_settings
    for select
    using (auth.role() = 'authenticated');

create policy "kudos_settings_no_client_insert"
    on public.kudos_settings
    for insert
    with check (false);

create policy "kudos_settings_no_client_update"
    on public.kudos_settings
    for update
    using (false);

create policy "kudos_settings_no_client_delete"
    on public.kudos_settings
    for delete
    using (false);

-- ----------------------------------------------------------------------------
-- public.notifications — per-user notifications + unread tracking
-- ----------------------------------------------------------------------------
create table if not exists public.notifications (
    id          uuid        primary key default gen_random_uuid(),
    user_id     uuid        not null references auth.users (id) on delete cascade,
    title       text        not null,
    body        text,
    read_at     timestamptz null,
    created_at  timestamptz not null default now()
);

-- Index supports the hot path: count unread per user.
create index if not exists notifications_user_unread_idx
    on public.notifications (user_id, read_at)
    where read_at is null;

alter table public.notifications enable row level security;

-- A user can read ONLY their own notifications. Cross-user leakage is the
-- highest-impact threat for this feature (plan § Threat Model row #1) — this
-- policy + the corresponding RLS denial test are mandatory.
create policy "notifications_select_own"
    on public.notifications
    for select
    using (auth.uid() = user_id);

create policy "notifications_no_client_insert"
    on public.notifications
    for insert
    with check (false);

-- Marking notifications read happens server-side (e.g. from the Notifications
-- panel's RPC); the client cannot directly UPDATE the row.
create policy "notifications_no_client_update"
    on public.notifications
    for update
    using (false);

create policy "notifications_no_client_delete"
    on public.notifications
    for delete
    using (false);

-- ----------------------------------------------------------------------------
-- RPC notifications_summary() — { unread_count }
-- security invoker so it relies on the notifications RLS policy (cannot be
-- abused to read other users' counts).
-- ----------------------------------------------------------------------------
create or replace function public.notifications_summary()
returns table (unread_count int)
language sql
security invoker
set search_path = public
as $$
    select count(*)::int as unread_count
    from public.notifications
    where user_id = auth.uid() and read_at is null;
$$;

-- ----------------------------------------------------------------------------
-- RPC kudos_summary() — projection of the singleton kudos_settings row
-- ----------------------------------------------------------------------------
create or replace function public.kudos_summary()
returns table (
    is_kudos_available boolean,
    banner_url         text,
    badge_text         text,
    description_text   text
)
language sql
security invoker
set search_path = public
as $$
    select is_kudos_available, banner_url, badge_text, description_text
    from public.kudos_settings
    where id = 1;
$$;

grant execute on function public.notifications_summary() to authenticated;
grant execute on function public.kudos_summary() to authenticated;
