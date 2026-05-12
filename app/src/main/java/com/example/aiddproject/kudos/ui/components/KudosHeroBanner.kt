package com.example.aiddproject.kudos.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aiddproject.R
import com.example.aiddproject.kudos.ui.KudosTestTags

/**
 * Hub hero banner (Figma `6885:9060` group — KV background + brand
 * text). Phase 3 MVP renders text-only ("Hệ thống ghi nhận và cảm ơn"
 * subtitle + "KUDOS" brand mark) over the parent screen's gradient.
 * Phase 13 polish swaps in the KV PNG (T009).
 */
@Composable
fun KudosHeroBanner(modifier: Modifier = Modifier) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(top = 16.dp, bottom = 12.dp)
                .testTag(KudosTestTags.HERO),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = stringResource(R.string.kudos_hero_subtitle),
            color = Color.White.copy(alpha = 0.85f),
            style =
                MaterialTheme.typography.titleMedium.copy(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                ),
        )
        Text(
            text = stringResource(R.string.kudos_hero_brand),
            color = Color.White,
            style =
                MaterialTheme.typography.displayMedium.copy(
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 4.sp,
                ),
        )
    }
}
