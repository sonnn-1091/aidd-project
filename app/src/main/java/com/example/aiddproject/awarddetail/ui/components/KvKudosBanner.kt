package com.example.aiddproject.awarddetail.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
 *   color `#FFEA9E` (SaaCream), left-aligned. Width 212dp.
 * - Logo lockup row — 221×39dp; SAA flame logo (49×38dp) +
 *   "KUDOS" wordmark side-by-side, vertically centered.
 *
 * Per Resolved Q5 + design fidelity, the "KUDOS" wordmark renders as
 * a Text composable in the same SaaCream colour; the flame uses the
 * existing `ic_logo_saa` drawable shared with Login + Home.
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
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.height(39.dp),
        ) {
            Image(
                painter = painterResource(R.drawable.ic_logo_saa),
                contentDescription = null,
                modifier =
                    Modifier
                        .width(49.dp)
                        .height(38.dp),
            )
            Spacer(Modifier.width(9.dp))
            Text(
                text = "KUDOS",
                color = SaaCream,
                style =
                    MaterialTheme.typography.headlineLarge.copy(
                        fontSize = 32.sp,
                        lineHeight = 39.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                    ),
            )
        }
    }
}
