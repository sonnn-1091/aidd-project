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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.aiddproject.R
import com.example.aiddproject.core.ui.rememberSingleClickHandler
import com.example.aiddproject.ui.theme.SaaCream
import com.example.aiddproject.ui.theme.SaaInk

/**
 * `mms_6_float button` (`6885:9058`) — combined FAB with two trailing icons:
 *  - Pencil → WriteKudo (rendered ONLY when `isKudosAvailable=true`, Q-Home-2).
 *    The click is wrapped in [rememberSingleClickHandler] (TR-005,
 *    `pencilInFlight`) so a finger-bounce double-tap can never push two
 *    `WriteKudo` destinations on the back stack.
 *  - S/Kudos → Kudos feed (always rendered, Q-Home-9). The click is also
 *    guarded so a rapid double-tap doesn't double-launch the feed.
 *
 * Both icons surface localized accessibility content descriptions
 * (`a11y_home_fab_compose_kudo` / `a11y_home_fab_kudos_feed`, TR-009).
 */
@Composable
fun HomeFab(
    isKudosAvailable: Boolean,
    onPencilClick: () -> Unit,
    onSKudosClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val pencilClick = rememberSingleClickHandler(onClick = onPencilClick)
    val sKudosClick = rememberSingleClickHandler(onClick = onSKudosClick)
    val pencilCd = stringResource(R.string.a11y_home_fab_compose_kudo)
    val sKudosCd = stringResource(R.string.a11y_home_fab_kudos_feed)
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (isKudosAvailable) {
            FloatingActionButton(
                onClick = pencilClick,
                containerColor = SaaCream,
                contentColor = SaaInk,
                modifier =
                    Modifier
                        .size(48.dp)
                        .testTag(TEST_TAG_HOME_FAB_PENCIL)
                        .semantics { contentDescription = pencilCd },
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_fab_pencil),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                )
            }
        }
        FloatingActionButton(
            onClick = sKudosClick,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier =
                Modifier
                    .size(48.dp)
                    .testTag(TEST_TAG_HOME_FAB_SKUDOS)
                    .semantics { contentDescription = sKudosCd },
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

const val TEST_TAG_HOME_FAB_PENCIL: String = "home_fab_pencil"
const val TEST_TAG_HOME_FAB_SKUDOS: String = "home_fab_skudos"
