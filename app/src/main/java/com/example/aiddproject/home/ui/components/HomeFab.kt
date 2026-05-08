package com.example.aiddproject.home.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.aiddproject.R
import com.example.aiddproject.ui.theme.SaaCream
import com.example.aiddproject.ui.theme.SaaInk

/**
 * `mms_6_float button` (`6885:9058`) — combined FAB with two trailing icons:
 *  - Pencil → WriteKudo (rendered ONLY when `isKudosAvailable=true`, Q-Home-2)
 *  - S/Kudos → Kudos feed (always rendered, Q-Home-9)
 *
 * Double-tap suppression on Pencil happens at the caller level via the
 * `onPencilClick` lambda guard.
 */
@Composable
fun HomeFab(
    isKudosAvailable: Boolean,
    onPencilClick: () -> Unit,
    onSKudosClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (isKudosAvailable) {
            FloatingActionButton(
                onClick = onPencilClick,
                containerColor = SaaCream,
                contentColor = SaaInk,
                modifier =
                    Modifier
                        .size(48.dp)
                        .semantics {
                            contentDescription = "Write a Kudo"
                        },
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_fab_pencil),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                )
            }
        }
        FloatingActionButton(
            onClick = onSKudosClick,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier =
                Modifier
                    .size(48.dp)
                    .semantics {
                        contentDescription = "Open Kudos feed"
                    },
        ) {
            // Reuses the NavBar Kudos glyph (speech-bubble + heart) at FAB scale —
            // the design's wide `ic_kudos_logo` SVG is a banner-style logo, not a
            // 24dp icon, so we substitute the navbar variant here.
            Icon(
                painter = painterResource(R.drawable.ic_navbar_kudos),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
            )
        }
    }
}
