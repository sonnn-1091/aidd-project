package com.example.aiddproject.home.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aiddproject.ui.theme.SaaCream

/**
 * Two-line section header matching Figma component `6885:8015` (used by
 * `mms_4.1_header` and `mms_5.1_header`):
 *
 *  1. **Caption** — small white 12sp Montserrat 400 string sitting above the
 *     divider (e.g. "Sun* Annual Awards 2025", "Phong trào ghi nhận").
 *  2. **Divider** — a 1dp horizontal line in the brand-dark colour `#2E3940`.
 *  3. **Title** — the big cream 22sp Montserrat 500 section title (e.g.
 *     "Hệ thống giải thưởng", "Sun* Kudos").
 *
 * Vertical gap between caption and divider, and between divider and title,
 * is 4dp (matches the design column gap on `6885:9031` / `6885:9040`).
 */
@Composable
fun SectionHeader(
    caption: String,
    title: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = caption,
            color = Color.White,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            fontWeight = FontWeight.Normal,
        )
        Spacer(Modifier.height(4.dp))
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(SectionHeaderDividerColor),
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = title,
            color = SaaCream,
            fontSize = 22.sp,
            lineHeight = 28.sp,
            fontWeight = FontWeight.Medium,
        )
    }
}

private val SectionHeaderDividerColor: Color = Color(0xFF2E3940)
