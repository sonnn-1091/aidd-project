package com.example.aiddproject.kudos.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
 * Layout:
 *  - Top: 14sp Montserrat Medium subtitle "Hệ thống ghi nhận và
 *    cảm ơn" in SaaCream (#FFEA9E).
 *  - Bottom: 39dp tall row with the SAA logo + 9dp gap + "KUDOS"
 *    brand wordmark rendered as bold 32sp text (the actual Figma
 *    "KUDOS" is a vector wordmark; we approximate with bold
 *    Montserrat).
 *
 * The KV PNG background is painted by [KudosScreenContent] behind
 * the section so the banner copy sits on top of the colorful art.
 */
@Composable
fun KudosHeroBanner(modifier: Modifier = Modifier) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 12.dp)
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
            // 49×38dp SAA logo, reused from Home — keeps the brand
            // mark aligned with Header/footer logo treatments.
            SaaLogo()
            Text(
                text = stringResource(R.string.kudos_hero_brand),
                color = SaaCream,
                style =
                    MaterialTheme.typography.displaySmall.copy(
                        fontSize = 32.sp,
                        lineHeight = 36.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 3.sp,
                    ),
            )
        }
    }
}

/**
 * Local fallback for the SAA logo when the project doesn't expose a
 * public composable. Falls back to the existing `ic_logo_saa` asset.
 */
@Composable
private fun SaaLogo() {
    androidx.compose.foundation.Image(
        painter = painterResource(R.drawable.ic_logo_saa),
        contentDescription = null,
        modifier =
            Modifier
                .height(38.dp),
    )
}
