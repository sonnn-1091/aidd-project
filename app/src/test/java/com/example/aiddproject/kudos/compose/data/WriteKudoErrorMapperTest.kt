package com.example.aiddproject.kudos.compose.data

import com.example.aiddproject.R
import com.example.aiddproject.kudos.compose.domain.WriteKudoFieldErrors
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests for [WriteKudoErrorMapper] (T032). Pure JVM — no Supabase
 * types imported, just synthetic `Throwable`s whose messages mimic
 * the Postgrest exceptions thrown by the RLS layer.
 */
class WriteKudoErrorMapperTest {
    @Test
    fun namedSelfSendConstraint_mapsToRecipientSelfError() {
        val e = RuntimeException("""new row for relation "kudos" violates check constraint "kudos_insert_self_only"""")
        assertEquals(
            WriteKudoFieldErrors(recipient = R.string.write_kudo_error_recipient_self),
            WriteKudoErrorMapper.map(e),
        )
    }

    @Test
    fun checkRecipientIdMessage_mapsToRecipientSelfError() {
        val e = RuntimeException("check violation on recipient_id constraint")
        assertEquals(
            R.string.write_kudo_error_recipient_self,
            WriteKudoErrorMapper.map(e).recipient,
        )
    }

    @Test
    fun tagsSubsetClause_mapsToHashtagsError() {
        val e = RuntimeException("violation: tags <@ select array_agg(id) from hashtags")
        assertEquals(
            R.string.write_kudo_error_hashtags_required,
            WriteKudoErrorMapper.map(e).hashtags,
        )
    }

    @Test
    fun tagNotInCatalog_mapsToHashtagsError() {
        val e = RuntimeException("tag not in known catalog")
        assertEquals(
            R.string.write_kudo_error_hashtags_required,
            WriteKudoErrorMapper.map(e).hashtags,
        )
    }

    @Test
    fun genericException_returnsNone() {
        val e = RuntimeException("internal server error 500")
        assertEquals(WriteKudoFieldErrors.None, WriteKudoErrorMapper.map(e))
    }

    @Test
    fun networkException_returnsNone() {
        val e = java.io.IOException("connection refused")
        assertEquals(WriteKudoFieldErrors.None, WriteKudoErrorMapper.map(e))
    }

    @Test
    fun nullMessage_returnsNone() {
        val e =
            object : RuntimeException() {
                override val message: String? = null
            }
        assertEquals(WriteKudoFieldErrors.None, WriteKudoErrorMapper.map(e))
    }

    @Test
    fun caseInsensitive_constraintName_isStillMatched() {
        val e = RuntimeException("VIOLATES CHECK CONSTRAINT KUDOS_INSERT_SELF_ONLY")
        assertEquals(
            R.string.write_kudo_error_recipient_self,
            WriteKudoErrorMapper.map(e).recipient,
        )
    }
}
