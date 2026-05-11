package com.example.aiddproject.awarddetail.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
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
import com.example.aiddproject.home.domain.Award
import com.example.aiddproject.home.domain.states.AwardsState
import com.example.aiddproject.ui.theme.SaaCream

/**
 * Highlight block — Figma node `6885:10283` (`mms_B_Highlight`).
 *
 * See `KvKudosBanner.kt` doc + this file's commit history for the
 * detailed Figma-vs-impl mapping. Phase 4 swaps the static dropdown
 * pill for the real [AwardCategoryDropdown] composable so the
 * category menu opens when the trigger is tapped.
 */
@Composable
fun HighlightBlock(
    categories: AwardsState,
    activeAwardId: String?,
    onCategorySelected: (Award) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = stringResource(R.string.award_detail_sub_label),
                color = Color.White,
                style =
                    MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        fontWeight = FontWeight.Normal,
                    ),
            )
            HorizontalDivider(
                thickness = 1.dp,
                color = HighlightDividerColor,
            )
            Text(
                text = stringResource(R.string.award_detail_title),
                color = SaaCream,
                style =
                    MaterialTheme.typography.headlineSmall.copy(
                        fontSize = 22.sp,
                        lineHeight = 28.sp,
                        fontWeight = FontWeight.Medium,
                    ),
            )
        }
        AwardCategoryDropdown(
            categories = categories,
            activeAwardId = activeAwardId,
            onSelect = onCategorySelected,
        )
    }
}

private val HighlightDividerColor: Color = Color(0xFF2E3940)
