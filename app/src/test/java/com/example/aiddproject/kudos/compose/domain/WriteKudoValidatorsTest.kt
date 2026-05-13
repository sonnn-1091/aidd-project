package com.example.aiddproject.kudos.compose.domain

import org.junit.Assert.fail
import org.junit.Ignore
import org.junit.Test

/**
 * Failing-stub test class for `WriteKudoValidators` (T014).
 *
 * Each test placeholder pins down a validator contract from spec § Data
 * Requirements. They are [@Ignore]-annotated until Phase 2 lands the real
 * `WriteKudoValidators` implementation (T024), at which point each
 * `@Ignore` is removed AND the `fail("not implemented")` body is replaced
 * with real assertions. The Constitution V intent is honoured: the tests
 * exist BEFORE the implementation and are the contract the impl is
 * written against.
 */
@Ignore("Phase 2 / T024 wires the real validators + replaces these bodies.")
class WriteKudoValidatorsTest {
    @Test
    fun validateTitle_emptyReturnsRequiredError() {
        fail("not implemented — T024")
    }

    @Test
    fun validateTitle_exactly100CharsAccepted() {
        fail("not implemented — T024")
    }

    @Test
    fun validateTitle_over100CharsReturnsTooLongError() {
        fail("not implemented — T024")
    }

    @Test
    fun validateMessage_emptyReturnsRequiredError() {
        fail("not implemented — T024")
    }

    @Test
    fun validateMessage_onlyWhitespaceReturnsBlankError() {
        fail("not implemented — T024")
    }

    @Test
    fun validateMessage_exactly1000CharsAccepted() {
        fail("not implemented — T024")
    }

    @Test
    fun validateMessage_over1000CharsReturnsTooLongError() {
        fail("not implemented — T024")
    }

    @Test
    fun validateHashtags_emptyReturnsRequiredError() {
        fail("not implemented — T024")
    }

    @Test
    fun validateHashtags_oneAccepted() {
        fail("not implemented — T024")
    }

    @Test
    fun validateHashtags_fiveAccepted() {
        fail("not implemented — T024")
    }

    @Test
    fun validateRecipient_nullReturnsRequiredError() {
        fail("not implemented — T024")
    }

    @Test
    fun validateRecipient_currentUserReturnsSelfSendError() {
        fail("not implemented — T024")
    }
}
