package com.example.aiddproject.awarddetail.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aiddproject.R
import com.example.aiddproject.ui.theme.SaaCream

/**
 * Award info block (Figma `mms_2.3_award` `6885:10292` — description
 * + quantity row + prize-value row). Display-only; contents replace
 * when the dropdown selection changes.
 *
 * Each sub-row renders a "—" placeholder when the corresponding field
 * is `null` (FR-008 + US1 acceptance scenario 5).
 */
@Composable
fun AwardInfoBlock(
    description: String,
    quantity: Int?,
    quantityUnit: String?,
    prizeValue: String?,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = description,
            color = Color.White,
            style =
                MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 14.sp,
                    lineHeight = 21.sp,
                    fontWeight = FontWeight.Normal,
                ),
        )
        HorizontalDivider(
            thickness = 1.dp,
            color = Color.White.copy(alpha = 0.2f),
        )
        RecipientCountRow(quantity = quantity, quantityUnit = quantityUnit)
        HorizontalDivider(
            thickness = 1.dp,
            color = Color.White.copy(alpha = 0.2f),
        )
        PrizeValueRow(prizeValue = prizeValue)
    }
}

@Composable
private fun RecipientCountRow(
    quantity: Int?,
    quantityUnit: String?,
) {
    val placeholder = stringResource(R.string.award_detail_placeholder_value)
    val valueText = quantity?.toString() ?: placeholder
    val unitText = quantityUnit.orEmpty()
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = stringResource(R.string.award_detail_quantity_label),
            color = Color.White,
            style =
                MaterialTheme.typography.titleSmall.copy(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                ),
            modifier = Modifier.weight(1f),
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = valueText,
            color = Color.White,
            style =
                MaterialTheme.typography.headlineSmall.copy(
                    fontSize = 22.sp,
                    lineHeight = 28.sp,
                    fontWeight = FontWeight.Bold,
                ),
        )
        if (unitText.isNotEmpty()) {
            Spacer(Modifier.width(6.dp))
            Text(
                text = unitText,
                color = Color.White.copy(alpha = 0.7f),
                style =
                    MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 14.sp,
                    ),
            )
        }
    }
    Spacer(Modifier.height(0.dp))
}

@Composable
private fun PrizeValueRow(prizeValue: String?) {
    val placeholder = stringResource(R.string.award_detail_placeholder_value)
    val valueText = prizeValue ?: placeholder
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = stringResource(R.string.award_detail_prize_label),
            color = Color.White,
            style =
                MaterialTheme.typography.titleSmall.copy(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                ),
            modifier = Modifier.weight(1f),
        )
        Spacer(Modifier.width(8.dp))
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = valueText,
                color = SaaCream,
                style =
                    MaterialTheme.typography.headlineSmall.copy(
                        fontSize = 22.sp,
                        lineHeight = 28.sp,
                        fontWeight = FontWeight.Bold,
                    ),
            )
            Text(
                text = stringResource(R.string.award_detail_prize_caption),
                color = Color.White.copy(alpha = 0.7f),
                style =
                    MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.sp,
                    ),
            )
        }
    }
}
