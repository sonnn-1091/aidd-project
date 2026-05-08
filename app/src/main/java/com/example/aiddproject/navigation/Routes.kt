package com.example.aiddproject.navigation

/**
 * Single source of truth for navigation route names. The Login feature owns the four
 * core destinations: GATE (splash + session resolution), LOGIN, HOME, and ACCESS_DENIED
 * (non-Sunner fallback per spec FR-007).
 */
object Routes {
    const val GATE: String = "route_gate"
    const val LOGIN: String = "route_login"
    const val HOME: String = "route_home"
    const val ACCESS_DENIED: String = "route_access_denied"
}
