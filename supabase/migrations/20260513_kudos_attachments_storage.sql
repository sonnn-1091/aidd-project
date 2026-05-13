-- ============================================================================
-- Viết Kudo composer (7fFAb-K35a) — kudos-attachments Storage bucket (T013)
-- Migration: 20260513_kudos_attachments_storage
--
-- Provisions the Storage bucket the composer uploads images to at submit time
-- (Q-W-2 — no drafts). Layout is flat:
--
--     kudos-attachments / {user_id} / {kudo_id} / {index}_{filename}
--
-- Policies:
--   - Authenticated users can INSERT under their own folder (foldername[1] =
--     auth.uid()).
--   - Authenticated users can DELETE under their own folder (used by the
--     submit-rollback path when an upload succeeds but a later one or the
--     kudos INSERT fails).
--   - Any authenticated user can SELECT — feed cards across the org need to
--     render the attachments. Per Constitution IV the bucket is not public.
--
-- Mime / size limits live on the bucket row.
-- ============================================================================

insert into storage.buckets (id, name, public, file_size_limit, allowed_mime_types)
values (
    'kudos-attachments',
    'kudos-attachments',
    false,
    10485760, -- 10 MiB
    array['image/jpeg', 'image/png', 'image/webp']
)
on conflict (id) do update set
    public = excluded.public,
    file_size_limit = excluded.file_size_limit,
    allowed_mime_types = excluded.allowed_mime_types;

-- Drop any pre-existing policies on this bucket so re-running the migration
-- is idempotent.
drop policy if exists "kudos_attachments_owner_insert" on storage.objects;
drop policy if exists "kudos_attachments_owner_delete" on storage.objects;
drop policy if exists "kudos_attachments_authenticated_select" on storage.objects;

create policy "kudos_attachments_owner_insert"
    on storage.objects
    for insert
    with check (
        bucket_id = 'kudos-attachments'
        and (storage.foldername(name))[1] = auth.uid()::text
    );

create policy "kudos_attachments_owner_delete"
    on storage.objects
    for delete
    using (
        bucket_id = 'kudos-attachments'
        and (storage.foldername(name))[1] = auth.uid()::text
    );

create policy "kudos_attachments_authenticated_select"
    on storage.objects
    for select
    using (
        bucket_id = 'kudos-attachments'
        and auth.role() = 'authenticated'
    );
