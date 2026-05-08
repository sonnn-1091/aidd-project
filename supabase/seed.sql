-- ============================================================================
-- Login feature — Local Supabase seed (T026)
--
-- Two fixtures used by the integration test suite:
--   - alice@sun-asterisk.com — registered Sunner (has a public.users row).
--   - bob@external.example  — Google account that authenticates but is NOT a
--                             Sunner (no public.users row); used to verify the
--                             Access denied navigation path.
--
-- The auth.users rows must be inserted with the service_role JWT (this seed
-- runs as such on `supabase db reset`). Production never sees these rows.
-- ============================================================================

-- --- Sunner fixture --------------------------------------------------------
insert into auth.users (
    id,
    email,
    instance_id,
    aud,
    role,
    encrypted_password,
    email_confirmed_at,
    created_at,
    updated_at
) values (
    '00000000-0000-0000-0000-000000000001',
    'alice@sun-asterisk.com',
    '00000000-0000-0000-0000-000000000000',
    'authenticated',
    'authenticated',
    crypt('test-password-not-used', gen_salt('bf')),
    now(),
    now(),
    now()
) on conflict (id) do nothing;

insert into public.users (id, email, full_name)
values (
    '00000000-0000-0000-0000-000000000001',
    'alice@sun-asterisk.com',
    'Alice Sunner'
) on conflict (id) do nothing;

-- --- Non-Sunner fixture ----------------------------------------------------
-- auth.users row only — NO matching public.users row. Tests this account ends
-- up on the [iOS] Access denied screen.
insert into auth.users (
    id,
    email,
    instance_id,
    aud,
    role,
    encrypted_password,
    email_confirmed_at,
    created_at,
    updated_at
) values (
    '00000000-0000-0000-0000-000000000002',
    'bob@external.example',
    '00000000-0000-0000-0000-000000000000',
    'authenticated',
    'authenticated',
    crypt('test-password-not-used', gen_salt('bf')),
    now(),
    now(),
    now()
) on conflict (id) do nothing;

-- ============================================================================
-- Home feature seed (T012) — 3 awards + 1 kudos_settings row + 4 notifications
-- ============================================================================

insert into public.awards (id, name, thumbnail_url, sort_order) values
    ('00000000-0000-0000-0000-000000000a01', 'Top Talent Award',  null, 0),
    ('00000000-0000-0000-0000-000000000a02', 'Top Project Award', null, 1),
    ('00000000-0000-0000-0000-000000000a03', 'Top Heart Award',   null, 2)
on conflict (id) do nothing;

insert into public.kudos_settings (
    id,
    is_kudos_available,
    banner_url,
    badge_text,
    description_text
) values (
    1,
    true,
    -- Public-CDN URL placeholder per Q-Plan-1; replaced by ops once the asset
    -- is published. Coil's `error()` painter falls back to ic_kudos_banner.
    'https://example.invalid/saa2025/kudos-banner.png',
    'ĐIỂM MỚI CỦA SAA 2025',
    'Hoạt động ghi nhận và cảm ơn đồng nghiệp - lần đầu tiên được diễn ra dành cho tất cả Sunner.'
) on conflict (id) do update set
    is_kudos_available = excluded.is_kudos_available,
    banner_url         = excluded.banner_url,
    badge_text         = excluded.badge_text,
    description_text   = excluded.description_text,
    updated_at         = now();

-- 4 notifications for Alice — 2 unread (read_at null) + 2 read.
insert into public.notifications (id, user_id, title, body, read_at) values
    ('00000000-0000-0000-0000-000000000n01', '00000000-0000-0000-0000-000000000001',
     'Submissions are open', 'Submit your nominations for the SAA 2025 awards.', null),
    ('00000000-0000-0000-0000-000000000n02', '00000000-0000-0000-0000-000000000001',
     'New Kudos received',   'You received a new Kudo from a colleague.',         null),
    ('00000000-0000-0000-0000-000000000n03', '00000000-0000-0000-0000-000000000001',
     'Awards ceremony date', 'Mark your calendar for 26 December 2025.',          now() - interval '2 days'),
    ('00000000-0000-0000-0000-000000000n04', '00000000-0000-0000-0000-000000000001',
     'Welcome to SAA 2025',  'Thanks for joining the platform.',                  now() - interval '7 days')
on conflict (id) do nothing;
