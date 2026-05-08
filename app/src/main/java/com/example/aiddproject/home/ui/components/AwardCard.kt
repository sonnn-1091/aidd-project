package com.example.aiddproject.home.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aiddproject.R
import com.example.aiddproject.core.ui.rememberSingleClickHandler
import com.example.aiddproject.home.domain.Award
import com.example.aiddproject.ui.theme.SaaCream

/**
 * Award carousel card — `Top Talent Award` instance (`6885:8051`).
 *
 * Layout (160×298dp, gap=12dp):
 *  - **Thumbnail** — 160×160dp cream-bordered (0.5dp), 11.4dp corner-radius
 *    image. Currently uses the local `ic_award_card_top_talent.png` because
 *    `Award.thumbnailUrl` is unset for the seeded fixtures; once the
 *    Postgrest payload supplies real URLs we'll pivot to Coil's `AsyncImage`
 *    here without changing the API.
 *  - **Title + description** — Frame 490 in design (`I6885:9033;72:2049`):
 *    14sp Montserrat 500 cream title + 14sp Montserrat 300 white description
 *    (3-line cap, lineHeight 20sp).
 *  - **Chi tiết button** — Figma `I6885:9033;72:2052`: 84×32dp transparent
 *    button (no background fill), 4dp corner radius, label "Chi tiết" 14sp
 *    Montserrat 500 white + chevron 24dp. Click is wrapped in
 *    [rememberSingleClickHandler] so a double-tap can't push two AwardDetail
 *    destinations on the back stack (TR-005).
 */
@Composable
fun AwardCard(
    award: Award,
    onChiTietClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val chiTietClick = rememberSingleClickHandler(onClick = onChiTietClick)
    Column(
        modifier = modifier.width(160.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Image(
            painter = painterResource(R.drawable.ic_award_card_top_talent),
            contentDescription = award.name,
            contentScale = ContentScale.Crop,
            modifier =
                Modifier
                    .size(160.dp)
                    .clip(RoundedCornerShape(11.4.dp))
                    .border(
                        width = 0.5.dp,
                        color = SaaCream,
                        shape = RoundedCornerShape(11.4.dp),
                    ),
        )
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = award.name,
                color = SaaCream,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
            )
            Text(
                // The seeded `Award` model intentionally only carries the slot
                // values the design needs (Q-Home-8) — there is no separate
                // description field, so we render the placeholder copy from
                // the Figma instance until backend exposes one.
                text = stringResource(R.string.home_award_description_placeholder),
                color = Color.White,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                fontWeight = FontWeight.Light,
                maxLines = 3,
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier =
                Modifier
                    .height(32.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .clickable(onClick = chiTietClick)
                    .padding(vertical = 6.dp),
        ) {
            Text(
                text = stringResource(R.string.home_link_chi_tiet),
                color = Color.White,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                fontWeight = FontWeight.Medium,
            )
            Spacer(Modifier.width(0.dp))
            Icon(
                painter = painterResource(R.drawable.ic_chevron_right),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(24.dp),
            )
        }
    }
}
