package com.example.aiddproject.home.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aiddproject.R
import com.example.aiddproject.core.ui.rememberSingleClickHandler
import com.example.aiddproject.ui.theme.SaaCream
import com.example.aiddproject.ui.theme.SaaInk

/**
 * `mms_6_float button` (`6885:9058`) — a SINGLE pill-shaped button with
 * cream background, 100dp corner radius, and 8dp padding. Children:
 *  - Pen icon (`ic_fab_pencil`) → WriteKudo. Rendered ONLY when
 *    `isKudosAvailable=true` (Q-Home-2).
 *  - "/" divider (24sp Montserrat, dark) — rendered alongside the pen and
 *    collapses with it.
 *  - Kudos logo icon (`ic_navbar_kudos`) → Kudos feed. Always rendered
 *    (Q-Home-9).
 *
 * Each child has its own click region so the pill behaves like two
 * sub-targets (pen → WriteKudo, Kudos → feed). Both clicks are wrapped in
 * [rememberSingleClickHandler] (TR-005) so a finger-bounce double-tap
 * cannot push two destinations on the back stack.
 *
 * Localized accessibility content descriptions are surfaced on each
 * sub-target (`a11y_home_fab_compose_kudo`, `a11y_home_fab_kudos_feed`).
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
    val divider = stringResource(R.string.home_fab_divider)
    Row(
        modifier =
            modifier
                .height(48.dp)
                .clip(RoundedCornerShape(100.dp))
                .background(SaaCream)
                .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (isKudosAvailable) {
            Icon(
                painter = painterResource(R.drawable.ic_fab_pencil),
                contentDescription = null,
                tint = SaaInk,
                modifier =
                    Modifier
                        .size(24.dp)
                        .clip(RoundedCornerShape(100.dp))
                        .clickable(onClick = pencilClick)
                        .testTag(TEST_TAG_HOME_FAB_PENCIL)
                        .semantics {
                            role = Role.Button
                            contentDescription = pencilCd
                        },
            )
            Text(
                text = divider,
                color = SaaInk,
                fontSize = 24.sp,
                lineHeight = 32.sp,
                fontWeight = FontWeight.Normal,
            )
        } else {
            // Maintain visual centering when the pen + divider collapse.
            Spacer(Modifier.width(0.dp))
        }
        // The brand Kudos glyph (`MM_MEDIA_IC_Kudos Logo` `6885:7657`) is a
        // stylized "S" with a red sweep — distinct from the navbar Kudos
        // icon (speech bubble + heart). Vector lives at `ic_fab_skudos.xml`.
        // tint=null so the multi-colour glyph (dark + red) renders verbatim.
        Icon(
            painter = painterResource(R.drawable.ic_fab_skudos),
            contentDescription = null,
            tint = androidx.compose.ui.graphics.Color.Unspecified,
            modifier =
                Modifier
                    .size(24.dp)
                    .clip(RoundedCornerShape(100.dp))
                    .clickable(onClick = sKudosClick)
                    .testTag(TEST_TAG_HOME_FAB_SKUDOS)
                    .semantics {
                        role = Role.Button
                        contentDescription = sKudosCd
                    },
        )
    }
}

const val TEST_TAG_HOME_FAB_PENCIL: String = "home_fab_pencil"
const val TEST_TAG_HOME_FAB_SKUDOS: String = "home_fab_skudos"
