package com.example.aiddproject.kudos.standards.ui

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Entry composable for the Community Standards screen (Figma frame
 * `xms7csmDhD`).
 *
 * Reusable from any caller — takes only an `onNavigateBack` callback
 * and reads nothing from caller context. Wires `BackHandler` so the
 * system-back gesture routes to the same callback as the top-app-bar's
 * back arrow (spec FR-003). All rendering is delegated to
 * [CommunityStandardsContent] for harness-isolation in tests.
 *
 * No ViewModel: the screen is fully static, content lives in
 * `strings.xml`, and there is no async work to coordinate (spec § State
 * Management).
 */
@Composable
fun CommunityStandardsScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BackHandler(onBack = onNavigateBack)
    CommunityStandardsContent(
        onBack = onNavigateBack,
        modifier = modifier,
    )
}
