package com.example.aiddproject.kudos.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aiddproject.R
import com.example.aiddproject.kudos.ui.KudosTestTags
import com.example.aiddproject.ui.theme.SaaCream

/**
 * Hub hero banner — Figma frame `mms_A_KV Kudos` (`6885:9066`).
 *
 * Layout per Figma:
 *  - 14sp Montserrat Medium SaaCream subtitle "Hệ thống ghi nhận và
 *    cảm ơn".
 *  - 8dp gap.
 *  - 39dp-tall row with 9dp gap between:
 *    - 49×38dp `kudos_hero_icon` (Figma node `6885:9071`, the Sun*
 *      "S" mark in red).
 *    - 163×39dp `kudos_hero_wordmark` (Figma node `6885:9077`, the
 *      "KUDOS" outline wordmark in SaaCream).
 *
 * The colorful keyvisual artwork is painted at the screen level
 * (`KudosScreenContent` paints `bg_home`), so this hero is
 * transparent and lets the wave artwork show through.
 */
@Composable
fun KudosHeroBanner(modifier: Modifier = Modifier) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 12.dp)
                .testTag(KudosTestTags.HERO),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = stringResource(R.string.kudos_hero_subtitle),
            color = SaaCream,
            style =
                MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    fontWeight = FontWeight.Medium,
                ),
        )
        Row(
            modifier = Modifier.height(39.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(9.dp),
        ) {
            Image(
                painter = painterResource(R.drawable.kudos_hero_icon),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier =
                    Modifier
                        .width(49.dp)
                        .height(38.dp),
            )
            Image(
                painter = painterResource(R.drawable.kudos_hero_wordmark),
                contentDescription = stringResource(R.string.kudos_hero_brand),
                contentScale = ContentScale.Fit,
                modifier =
                    Modifier
                        .width(163.dp)
                        .height(39.dp),
            )
        }
    }
}
