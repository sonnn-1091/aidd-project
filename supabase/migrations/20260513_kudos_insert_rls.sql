-- ============================================================================
-- Viết Kudo composer (7fFAb-K35a) — kudos INSERT RLS policy (T012)
-- Migration: 20260513_kudos_insert_rls
--
-- Enforces three server-side rules per spec § TR-002 + FR-004:
--   1. sender_id MUST equal auth.uid()  — anti-spoofing (defaults handle this
--      when the client omits sender_id; the WITH CHECK clause is the gate).
--   2. recipient_id MUST NOT equal auth.uid() — self-send rejection
--      (Postgres throws 23514 check_violation which maps to
--      `write_kudo_error_recipient_self`).
--   3. tags MUST be a subset of the curated hashtags catalog — rejects
--      arbitrary client-supplied tag strings (maps to
--      `write_kudo_error_hashtags_*` via the error mapper).
-- ============================================================================

-- Default sender_id to auth.uid() so the client never sends it. Keep a
-- separate policy guard so even a manually-supplied value is rejected.
alter table public.kudos
    alter column sender_id set default auth.uid();

-- Drop any earlier permissive INSERT policy to keep the gate authoritative.
drop policy if exists "kudos_insert_self_only" on public.kudos;

create policy "kudos_insert_self_only"
    on public.kudos
    for insert
    with check (
        sender_id = auth.uid()
        and recipient_id <> auth.uid()
        and (
            tags is null
            or tags <@ (select array_agg(id::text) from public.hashtags)
        )
    );
