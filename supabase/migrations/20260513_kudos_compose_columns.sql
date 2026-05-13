-- ============================================================================
-- Viết Kudo composer (7fFAb-K35a) — kudos table compose columns (T011)
-- Migration: 20260513_kudos_compose_columns
--
-- Adds (or no-ops if already present) the four columns the composer needs:
--   title         text        — recognition award title (1–100 chars)
--   tags          text[]      — 1–5 hashtag IDs
--   image_ids     text[]      — 0–5 Storage paths (per Q-W-2 — submit-time
--                                upload only, no draft uploads)
--   is_anonymous  boolean     — sender-anonymous flag (default false)
--
-- The hub feed (`fO0Kt19sZZ`) already reads these fields, so the migration is
-- a defensive no-op when the hub's earlier migration shipped them.
-- ============================================================================

alter table public.kudos
    add column if not exists title text;

alter table public.kudos
    add column if not exists tags text[];

alter table public.kudos
    add column if not exists image_ids text[] not null default '{}';

alter table public.kudos
    add column if not exists is_anonymous boolean not null default false;

-- Constraints — guarded by `if not exists` via dynamic block so re-running
-- against a partially-migrated schema stays idempotent. Postgres lacks a
-- direct "add constraint if not exists" so we DROP-then-ADD with name
-- prefixes that match the table.

do $$
begin
    -- title: 1–100 chars when set; NOT NULL once data lands.
    if not exists (
        select 1 from pg_constraint where conname = 'kudos_title_length_ck'
    ) then
        alter table public.kudos
            add constraint kudos_title_length_ck
            check (title is null or char_length(title) between 1 and 100);
    end if;

    -- tags: 1–5 entries when set.
    if not exists (
        select 1 from pg_constraint where conname = 'kudos_tags_count_ck'
    ) then
        alter table public.kudos
            add constraint kudos_tags_count_ck
            check (tags is null or (array_length(tags, 1) between 1 and 5));
    end if;

    -- image_ids: 0–5 entries.
    if not exists (
        select 1 from pg_constraint where conname = 'kudos_image_ids_count_ck'
    ) then
        alter table public.kudos
            add constraint kudos_image_ids_count_ck
            check (
                array_length(image_ids, 1) is null
                or array_length(image_ids, 1) <= 5
            );
    end if;
end$$;
