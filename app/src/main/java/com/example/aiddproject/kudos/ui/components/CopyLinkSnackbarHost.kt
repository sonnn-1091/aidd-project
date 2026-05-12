package com.example.aiddproject.kudos.ui.components

import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.example.aiddproject.kudos.domain.SnackbarMessage
import com.example.aiddproject.kudos.ui.KudosTestTags

/**
 * Copy Link Snackbar host (Figma `B.4` toast over Scaffold body).
 *
 * Phase 3 MVP plumbs the SnackbarHostState into the Scaffold's
 * snackbar slot + observes [message] so when Phase 9 (US13) sets
 * the slot, the existing host fires the M3 Snackbar without any
 * further wiring.
 */
@Composable
fun CopyLinkSnackbarHost(
    hostState: SnackbarHostState,
    message: SnackbarMessage?,
    onDismissed: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val text = message?.let { stringResource(it.messageRes) }
    LaunchedEffect(message) {
        if (text != null) {
            hostState.showSnackbar(text)
            onDismissed()
        }
    }
    SnackbarHost(
        hostState = hostState,
        modifier = modifier.testTag(KudosTestTags.SNACKBAR_HOST),
    )
}
