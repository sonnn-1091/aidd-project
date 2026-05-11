package com.example.aiddproject.awarddetail.ui.components

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aiddproject.R
import com.example.aiddproject.ui.theme.SaaCream

/**
 * Static banner at the top of the body — Figma node `6885:10266`
 * (`mms_A_KV Kudos`). Subtitle "Hệ thống ghi nhận và cảm ơn" stacked
 * above the "🔥 KUDOS" lockup. Display-only; no behavior.
 *
 * Pixel-level chrome (exact font weights, kerning) is fetched on
 * demand via MoMorph `query_section` at task-execution time per
 * Constitution Principle II — this composable carries the minimal
 * spec-faithful structure.
 */
@Composable
fun KvKudosBanner(modifier: Modifier = Modifier) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = stringResource(R.string.award_detail_kv_subtitle),
            color = Color.White.copy(alpha = 0.7f),
            style =
                MaterialTheme.typography.bodySmall.copy(
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                ),
            textAlign = TextAlign.Center,
        )
        Text(
            text = "🔥 KUDOS",
            color = SaaCream,
            style =
                MaterialTheme.typography.headlineMedium.copy(
                    fontSize = 28.sp,
                    lineHeight = 32.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                ),
        )
    }
}
