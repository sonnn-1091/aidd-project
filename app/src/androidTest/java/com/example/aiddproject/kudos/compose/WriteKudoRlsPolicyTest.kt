package com.example.aiddproject.kudos.compose

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.fail
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Failing-stub live-Supabase RLS policy test (T018).
 *
 * Verifies the three migrations that ship in Phase 1:
 *   - 20260513_kudos_compose_columns.sql
 *   - 20260513_kudos_insert_rls.sql
 *   - 20260513_kudos_attachments_storage.sql
 *
 * Phase 3 / T060 swaps the [@Ignore] + `fail()` bodies for real Postgrest
 * calls against the dev Supabase instance with two real test users.
 * Phase 7 / T100 extends with a live Storage round-trip cross-user test.
 */
@RunWith(AndroidJUnit4::class)
@Ignore("Phase 3 / T060 + Phase 7 / T100 wire real Supabase calls.")
class WriteKudoRlsPolicyTest {
    @Test
    fun selfSend_isRejectedByRls() {
        fail("not implemented — T060")
    }

    @Test
    fun tagsOutsideHashtagCatalog_areRejected() {
        fail("not implemented — T060")
    }

    @Test
    fun senderId_isAutoDerivedFromAuthUid() {
        fail("not implemented — T060")
    }

    @Test
    fun storage_otherUsersFolder_uploadIsRejected() {
        fail("not implemented — T100")
    }
}
