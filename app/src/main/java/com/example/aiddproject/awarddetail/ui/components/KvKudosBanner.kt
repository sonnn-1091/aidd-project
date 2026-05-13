package com.example.aiddproject.awarddetail.ui.components

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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aiddproject.R
import com.example.aiddproject.ui.theme.SaaCream

/**
 * KV Kudos banner — Figma node `6885:10266` (`mms_A_KV Kudos`).
 *
 * Vertical column, **left-aligned**, anchored 20dp from the screen
 * left edge. Per Figma styles:
 * - Subtitle text "Hệ thống ghi nhận và cảm ơn" — 14sp Montserrat 500,
 *   color `#FFEA9E` (SaaCream), left-aligned.
 * - Logo lockup row — 39dp tall, 9dp gap:
 *   - 49×38dp `kudos_hero_icon` (Sun* "S" mark in red).
 *   - 163×39dp `kudos_hero_wordmark` ("KUDOS" outline wordmark in
 *     SaaCream).
 *
 * Shares the same Figma exports as the Sun*Kudos hub hero so both
 * surfaces render the brand lockup identically.
 */
@Composable
fun KvKudosBanner(modifier: Modifier = Modifier) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, top = 16.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = stringResource(R.string.award_detail_kv_subtitle),
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
