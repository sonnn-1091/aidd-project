package com.example.aiddproject.kudos.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
 * Banner owns its local KV background (paints the colorful artwork
 * at the back of this section only). Sections below the hero see
 * the screen's solid `#00070C` fill — fixes D1 from the deviation
 * report where the KV bled through the entire scroll.
 */
@Composable
fun KudosHeroBanner(modifier: Modifier = Modifier) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .height(HERO_HEIGHT.dp)
                .testTag(KudosTestTags.HERO),
    ) {
        Image(
            painter = painterResource(R.drawable.kudos_kv_bg),
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.fillMaxSize(),
        )
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.Top),
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
                    painter = painterResource(R.drawable.ic_logo_saa),
                    contentDescription = null,
                    modifier = Modifier.height(38.dp),
                )
                Text(
                    text = stringResource(R.string.kudos_hero_brand),
                    color = SaaCream,
                    style =
                        MaterialTheme.typography.displaySmall.copy(
                            fontSize = 36.sp,
                            lineHeight = 40.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 4.sp,
                        ),
                )
            }
        }
    }
}

private const val HERO_HEIGHT: Int = 220
