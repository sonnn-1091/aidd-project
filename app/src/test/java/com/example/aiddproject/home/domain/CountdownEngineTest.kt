@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.example.aiddproject.home.domain

import app.cash.turbine.test
import com.example.aiddproject.home.domain.states.CountdownState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class CountdownEngineTest {
    /** Mutable clock so individual tests can drive deterministic time. */
    private class FakeClock(
        var now: Instant,
    ) : Clock {
        override fun now(): Instant = now
    }

    // ---- Pure compute() ----

    @Test
    fun `pre-event 1d 2h 3m splits into d-h-m and isPreEvent=true`() {
        val now = SaaCountdownTarget - 1.days - 2.hours - 3.minutes
        val state = CountdownEngine.compute(SaaCountdownTarget, now)
        assertEquals(CountdownState(days = 1, hours = 2, minutes = 3, isPreEvent = true), state)
    }

    @Test
    fun `pre-event truncates the seconds component to whole minutes`() {
        val now = SaaCountdownTarget - 5.minutes - 59.seconds
        val state = CountdownEngine.compute(SaaCountdownTarget, now)
        // 5m 59s remaining → minutes = 5 (we render whole minutes, never partials)
        assertEquals(CountdownState(days = 0, hours = 0, minutes = 5, isPreEvent = true), state)
    }

    @Test
    fun `at the target moment clamps to zero and isPreEvent=false`() {
        val state = CountdownEngine.compute(SaaCountdownTarget, SaaCountdownTarget)
        assertEquals(CountdownState(days = 0, hours = 0, minutes = 0, isPreEvent = false), state)
    }

    @Test
    fun `post-event clamps to zero and isPreEvent=false`() {
        val now = SaaCountdownTarget + 5.hours
        val state = CountdownEngine.compute(SaaCountdownTarget, now)
        assertEquals(CountdownState(days = 0, hours = 0, minutes = 0, isPreEvent = false), state)
    }

    // ---- Flow ticks() ----

    @Test
    fun `ticks emits the computed state immediately on subscribe`() =
        runTest(UnconfinedTestDispatcher()) {
            val clock = FakeClock(now = SaaCountdownTarget - 2.minutes)
            val engine = CountdownEngine(clock)

            engine.ticks().test {
                assertEquals(
                    CountdownState(days = 0, hours = 0, minutes = 2, isPreEvent = true),
                    awaitItem(),
                )
                cancelAndConsumeRemainingEvents()
            }
        }

    @Test
    fun `ticks recomputes from clock on every tick - does NOT decrement an in-memory counter`() =
        runTest(UnconfinedTestDispatcher()) {
            val clock = FakeClock(now = SaaCountdownTarget - 5.minutes)
            val engine = CountdownEngine(clock)

            engine.ticks().test {
                assertEquals(5, awaitItem().minutes)

                // Jump the clock by 2 minutes (NOT 1 second). If the engine were
                // decrementing a local counter, the next emission would still show 4m.
                clock.now = SaaCountdownTarget - 3.minutes
                advanceTimeBy(1.seconds)

                assertEquals(3, awaitItem().minutes)
                cancelAndConsumeRemainingEvents()
            }
        }

    @Test
    fun `ticks transitions from pre-event to event when the clock crosses the target`() =
        runTest(UnconfinedTestDispatcher()) {
            val clock = FakeClock(now = SaaCountdownTarget - 1.seconds)
            val engine = CountdownEngine(clock)

            engine.ticks().test {
                val first = awaitItem()
                assertEquals(true, first.isPreEvent)

                clock.now = SaaCountdownTarget + 1.seconds
                advanceTimeBy(1.seconds)

                val second = awaitItem()
                assertEquals(false, second.isPreEvent)
                assertEquals(0, second.days)
                assertEquals(0, second.hours)
                assertEquals(0, second.minutes)
                cancelAndConsumeRemainingEvents()
            }
        }
}
