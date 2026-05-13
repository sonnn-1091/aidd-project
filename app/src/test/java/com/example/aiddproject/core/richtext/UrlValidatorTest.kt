package com.example.aiddproject.core.richtext

import org.junit.Assert.fail
import org.junit.Ignore
import org.junit.Test

/**
 * Failing-stub test class for `UrlValidator` (T016).
 *
 * Real bodies land in Phase 2 / T026.
 */
@Ignore("Phase 2 / T026 wires the real UrlValidator + replaces these bodies.")
class UrlValidatorTest {
    @Test
    fun https_url_isValid() {
        fail("not implemented — T026")
    }

    @Test
    fun http_url_isValid() {
        fail("not implemented — T026")
    }

    @Test
    fun plain_text_isInvalid() {
        fail("not implemented — T026")
    }

    @Test
    fun javascript_scheme_isRejected_owasp_a03() {
        fail("not implemented — T026")
    }

    @Test
    fun data_uri_isRejected_owasp_a03() {
        fail("not implemented — T026")
    }

    @Test
    fun url_with_path_and_query_isValid() {
        fail("not implemented — T026")
    }
}
