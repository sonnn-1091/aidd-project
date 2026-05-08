package com.example.aiddproject.home.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

/**
 * Material 3 [ModalBottomSheet] hosting the Notifications panel on Home (US6).
 *
 * The actual notifications list + read/unread interactions are owned by the
 * Notifications panel spec, not Home — this sheet ships a "No notifications"
 * placeholder body for now. Home only fires the badge-summary endpoint and
 * re-fetches on dismissal.
 *
 * Wraps the body in [Modifier.systemBarsPadding] so the sheet content doesn't
 * collide with the status-bar inset (Q-Plan / risk register: edge-to-edge +
 * `ModalBottomSheet` can otherwise leave a visible gap).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        modifier = modifier.testTag(TEST_TAG_NOTIFICATIONS_SHEET),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .systemBarsPadding()
                    .padding(24.dp),
            contentAlignment = Alignment.Center,
        ) {
            // Placeholder copy while the Notifications panel spec is unbuilt.
            Text(text = "No notifications yet")
        }
    }
}

const val TEST_TAG_NOTIFICATIONS_SHEET: String = "notifications_sheet"
