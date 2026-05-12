package com.example.aiddproject.kudos.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aiddproject.R
import com.example.aiddproject.ui.theme.SaaCream

/**
 * Shared section header — Figma `header` instance (`6885:8015`).
 *
 * Layout: small caption ("Sun* Annual Awards 2025", 12sp Montserrat
 * Regular white) → 4dp gap → 1px divider (#2E3940) → 4dp gap →
 * title (22sp Montserrat Medium SaaCream).
 *
 * Reused by HIGHLIGHT KUDOS, SPOTLIGHT BOARD, ALL KUDOS sections.
 */
@Composable
fun KudosSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = stringResource(R.string.home_section_awards_caption),
            color = Color.White,
            style =
                MaterialTheme.typography.bodySmall.copy(
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    fontWeight = FontWeight.Normal,
                ),
        )
        // 1px divider — Details-Border-2 #2E3940
        androidx.compose.foundation.layout
            .Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(SectionDividerColor),
            )
        Text(
            text = title,
            color = SaaCream,
            style =
                MaterialTheme.typography.titleLarge.copy(
                    fontSize = 22.sp,
                    lineHeight = 28.sp,
                    fontWeight = FontWeight.Medium,
                ),
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}

private val SectionDividerColor: Color = Color(0xFF2E3940)
