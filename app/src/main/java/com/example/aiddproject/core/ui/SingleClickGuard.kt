package com.example.aiddproject.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Default guard window — long enough to drop a finger-bounce double-tap,
 * short enough not to interfere with intentional re-taps.
 */
private const val DEFAULT_GUARD_WINDOW_MS: Long = 400L

/**
 * Wraps [onClick] in a guard that drops any second invocation fired within
 * [windowMillis] of the first. TR-005 calls for double-tap suppression on every
 * navigation-triggering control on Home (FAB pencil / S-Kudos, NavBar tabs,
 * hero ABOUT buttons, Chi tiết links, search, bell). The boolean is local to
 * each call site, so two unrelated controls never block each other.
 */
@Composable
fun rememberSingleClickHandler(
    windowMillis: Long = DEFAULT_GUARD_WINDOW_MS,
    onClick: () -> Unit,
): () -> Unit {
    val scope = rememberCoroutineScope()
    val inFlight = remember { mutableStateOf(false) }
    return {
        if (!inFlight.value) {
            inFlight.value = true
            onClick()
            scope.launch {
                delay(windowMillis)
                inFlight.value = false
            }
        }
    }
}
