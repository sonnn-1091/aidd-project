package com.example.aiddproject.home.ui.components

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

/**
 * `mms_3_note` (`6885:9028`) — Vietnamese brand prose explaining the
 * "Root Further" theme. Localized into EN / JA per Q-Home-4.
 */
@Composable
fun ThemeParagraph(modifier: Modifier = Modifier) {
    Text(
        text = stringResource(R.string.home_theme_paragraph),
        color = Color.White,
        style =
            MaterialTheme.typography.bodySmall.copy(
                fontSize = 12.sp,
                lineHeight = 18.sp,
                fontWeight = FontWeight.Light,
            ),
        modifier = modifier.padding(horizontal = 20.dp, vertical = 16.dp),
    )
}
