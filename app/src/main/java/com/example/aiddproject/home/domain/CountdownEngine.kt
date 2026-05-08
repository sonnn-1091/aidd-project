@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.example.aiddproject.home.domain

import com.example.aiddproject.home.domain.states.CountdownState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

/**
 * Pure ticking engine that converts `(target - clock.now())` into a [CountdownState] every
 * second. The Clock is injectable so tests can drive deterministic time; the production
 * Hilt module binds [Clock.System].
 *
 * Intentionally re-reads the clock on every tick instead of decrementing an in-memory
 * counter — that way the displayed value stays consistent even if the device sleeps,
 * the process is paused, or the user changes the system time.
 */
@Singleton
class CountdownEngine
    @Inject
    constructor(
        private val clock: Clock,
    ) {
        fun ticks(): Flow<CountdownState> =
            flow {
                while (true) {
                    emit(compute(SaaCountdownTarget, clock.now()))
                    delay(1.seconds)
                }
            }

        fun snapshot(): CountdownState = compute(SaaCountdownTarget, clock.now())

        companion object {
            fun compute(
                target: Instant,
                now: Instant,
            ): CountdownState {
                val remaining = target - now
                if (remaining <= Duration.ZERO) {
                    return CountdownState(0, 0, 0, isPreEvent = false)
                }
                val totalSeconds = remaining.inWholeSeconds
                return CountdownState(
                    days = (totalSeconds / SECONDS_PER_DAY).toInt(),
                    hours = ((totalSeconds % SECONDS_PER_DAY) / SECONDS_PER_HOUR).toInt(),
                    minutes = ((totalSeconds % SECONDS_PER_HOUR) / SECONDS_PER_MINUTE).toInt(),
                    isPreEvent = true,
                )
            }

            private const val SECONDS_PER_MINUTE = 60L
            private const val SECONDS_PER_HOUR = 3_600L
            private const val SECONDS_PER_DAY = 86_400L
        }
    }
