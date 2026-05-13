package com.example.aiddproject.kudos.compose

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.fail
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Failing-stub instrumented test class for the Viết Kudo composer (T017).
 *
 * Phase 3 (US1) replaces the [@Ignore] + `fail()` bodies with real Compose
 * UI assertions against `WriteKudoScreenContent`. Phases 4–9 extend this
 * class with their own user-story tests in nested classes once it grows
 * past ~10 tests.
 *
 * The novel "tap-reveals-errors-on-disabled-Send" pattern documented in
 * plan § Notes is guarded by [us3_tappingDisabledSend_revealsAllFieldErrors]
 * — the test ensures the outer `Box(...pointerInput...)` wrapper around
 * the M3 disabled `Button` still intercepts taps.
 */
@RunWith(AndroidJUnit4::class)
@Ignore("Phase 3 / T056+ lands real Compose UI assertions.")
class WriteKudoScreenComposeTest {
    @Test
    fun us1_happyPath_renders_and_submits() {
        fail("not implemented — T056")
    }

    @Test
    fun us3_tappingDisabledSend_revealsAllFieldErrors() {
        fail("not implemented — T076 (novel composable pattern)")
    }
}
