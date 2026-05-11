package com.example.aiddproject.awarddetail.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aiddproject.R
import com.example.aiddproject.ui.theme.SaaCream

/**
 * Award info block — Figma node `6885:10292` (`mms_2.3_award`) sans
 * the hero badge (which lives in [AwardHeroBlock]).
 *
 * Layout: a single column with three "info sections" stacked with
 * 1px `#2E3940` dividers between them. Per Figma:
 *
 *  - **Section 1 — Title + description** (`6885:10294`, gap 12dp):
 *    title row [24dp icon + 8dp + "Top Talent" 14sp Montserrat 700
 *    SaaCream] above the description (14sp Montserrat 300 Light white,
 *    lineHeight 20dp, letterSpacing 0.25sp).
 *  - **Section 2 — Quantity** (`6885:10300`, gap 12dp): title row
 *    [24dp icon + 8dp + "Số lượng giải thưởng" 14sp/700 SaaCream]
 *    above a value row (gap 4dp, items center): "10" 18sp Montserrat
 *    700 white (letterSpacing 0.5sp, lineHeight 24dp) + "Cá nhân"
 *    14sp Montserrat 300 Light white.
 *  - **Section 3 — Prize** (`6885:10308`, gap 12dp): title row
 *    [24dp icon + 8dp + "Giá trị giải thưởng" 14sp/700 SaaCream]
 *    above a value row (gap 8dp): "7.000.000 VNĐ" 18sp/700 white +
 *    "cho mỗi giải thưởng" 14sp/300 Light white.
 *
 * Null `quantity` / `prize_value` render the localized "—" placeholder
 * (`award_detail_placeholder_value`) per FR-008 + US1 scenario 5.
 */
@Composable
fun AwardInfoBlock(
    awardName: String,
    description: String,
    quantity: Int?,
    quantityUnit: String?,
    prizeValue: String?,
    modifier: Modifier = Modifier,
    // Optional caption override. Null → fall back to the localized
    // default `R.string.award_detail_prize_caption` ("cho mỗi giải thưởng").
    // Per delta-spec b2BuS8HYIt Q-MVP-1 (MVP renders "cho giải cá nhân").
    prizeCaption: String? = null,
    // Optional second prize-value row for dual-prize awards. Both
    // fields must be non-null for the second row to render.
    // Per delta-spec O98TwiHaJe Q-SIG-1 (Signature 2025 — Creator).
    prizeValueTeam: String? = null,
    prizeCaptionTeam: String? = null,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Section 1 — title + description
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            InfoTitleRow(title = awardName)
            Text(
                text = description,
                color = Color.White,
                style =
                    MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        fontWeight = FontWeight.Light,
                        letterSpacing = 0.25.sp,
                    ),
            )
        }
        InfoDivider()
        // Section 2 — quantity
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            InfoTitleRow(title = stringResource(R.string.award_detail_quantity_label))
            QuantityValueRow(quantity = quantity, unit = quantityUnit)
        }
        InfoDivider()
        // Section 3 — prize (first, always present).
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            InfoTitleRow(title = stringResource(R.string.award_detail_prize_label))
            PrizeValueRow(prizeValue = prizeValue, prizeCaption = prizeCaption)
        }
        // Section 4 — second prize section (Q-SIG-1, optional). Per
        // Figma frame `O98TwiHaJe` Picture-Award INSTANCE — the team
        // prize lives in its OWN section with its OWN title row +
        // divider above, mirroring the cá nhân section's chrome.
        // Renders only when both `prizeValueTeam` and `prizeCaptionTeam`
        // are non-null.
        if (prizeValueTeam != null && prizeCaptionTeam != null) {
            InfoDivider()
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                InfoTitleRow(title = stringResource(R.string.award_detail_prize_label))
                PrizeValueRow(prizeValue = prizeValueTeam, prizeCaption = prizeCaptionTeam)
            }
        }
    }
}

@Composable
private fun InfoTitleRow(title: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_navbar_awards),
            contentDescription = null,
            tint = SaaCream,
            modifier = Modifier.size(24.dp),
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = title,
            color = SaaCream,
            style =
                MaterialTheme.typography.titleSmall.copy(
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    fontWeight = FontWeight.Bold,
                ),
        )
    }
}

@Composable
private fun InfoDivider() {
    HorizontalDivider(
        thickness = 1.dp,
        color = InfoDividerColor,
    )
}

@Composable
private fun QuantityValueRow(
    quantity: Int?,
    unit: String?,
) {
    val placeholder = stringResource(R.string.award_detail_placeholder_value)
    // Zero-pad single-digit counts to two digits so Top Project's
    // `2` renders as `02 Tập thể` matching Figma node `6885:10475`
    // (delta-spec FQoJZLkG_d § Q-TP-2 Option A). Counts ≥ 10 pass
    // through unchanged — `%02d` formats AT LEAST 2 digits, never
    // truncates.
    val valueText = quantity?.let { "%02d".format(it) } ?: placeholder
    val unitText = unit.orEmpty()
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = valueText,
            color = Color.White,
            style =
                MaterialTheme.typography.titleMedium.copy(
                    fontSize = 18.sp,
                    lineHeight = 24.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp,
                ),
        )
        if (unitText.isNotEmpty()) {
            Spacer(Modifier.width(4.dp))
            Text(
                text = unitText,
                color = Color.White,
                style =
                    MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        fontWeight = FontWeight.Light,
                        letterSpacing = 0.25.sp,
                    ),
            )
        }
    }
}

@Composable
private fun PrizeValueRow(
    prizeValue: String?,
    prizeCaption: String? = null,
) {
    val placeholder = stringResource(R.string.award_detail_placeholder_value)
    val valueText = prizeValue ?: placeholder
    val captionText = prizeCaption ?: stringResource(R.string.award_detail_prize_caption)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = valueText,
            color = Color.White,
            style =
                MaterialTheme.typography.titleMedium.copy(
                    fontSize = 18.sp,
                    lineHeight = 24.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp,
                ),
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = captionText,
            color = Color.White,
            style =
                MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    fontWeight = FontWeight.Light,
                    letterSpacing = 0.25.sp,
                ),
        )
    }
}

private val InfoDividerColor: Color = Color(0xFF2E3940)
