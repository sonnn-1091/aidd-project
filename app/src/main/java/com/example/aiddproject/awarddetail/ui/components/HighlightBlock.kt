package com.example.aiddproject.awarddetail.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aiddproject.R
import com.example.aiddproject.core.ui.rememberSingleClickHandler
import com.example.aiddproject.ui.theme.SaaCream

/**
 * Highlight block — Figma node `6885:10283` (`mms_B_Highlight`).
 *
 * Vertical column at x=20dp from screen edge, 335dp wide, 137dp tall,
 * 24dp gap between the header sub-block and the dropdown filter row.
 *
 * Header sub-block (`6885:10285`): sub-label "Sun* Annual Awards 2025"
 * (12sp Montserrat 400 white) → 1px divider `#2E3940` → title
 * "Hệ thống giải thưởng SAA 2025" (22sp Montserrat 500 SaaCream, two
 * lines).
 *
 * Filter dropdown (`6885:10287`): 160×40dp pill with `1px #998C5F`
 * outline + 10% SaaCream fill, 4dp radius. Content: "Top Talent" label
 * (14sp Montserrat 400 white, center-aligned in its 120dp text slot)
 * + 24dp chevron icon, space-between layout, 8dp padding.
 *
 * **Phase 3 scope** — the dropdown trigger is a Box wrapped in
 * `rememberSingleClickHandler { onDropdownTriggerClick() }` which is a
 * no-op stub until Phase 4 lands the popup. Touch target is 48dp tall
 * (TR-008) even though the visible pill is 40dp.
 */
@Composable
fun HighlightBlock(
    activeAwardName: String?,
    onDropdownTriggerClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val triggerClick = rememberSingleClickHandler(onClick = onDropdownTriggerClick)
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
        Box(
            modifier =
                Modifier
                    .testTag(TEST_TAG_DROPDOWN_TRIGGER)
                    .heightIn(min = 48.dp)
                    .clickable(onClick = triggerClick),
            contentAlignment = Alignment.CenterStart,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier =
                    Modifier
                        .width(160.dp)
                        .height(40.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(SaaCream.copy(alpha = 0.10f))
                        .border(
                            width = 1.dp,
                            color = HighlightDropdownBorderColor,
                            shape = RoundedCornerShape(4.dp),
                        ).padding(horizontal = 8.dp),
            ) {
                Text(
                    text = activeAwardName.orEmpty(),
                    color = Color.White,
                    style =
                        MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 14.sp,
                            lineHeight = 20.sp,
                            fontWeight = FontWeight.Normal,
                            letterSpacing = 0.25.sp,
                        ),
                    modifier = Modifier.weight(1f),
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "▾",
                    color = Color.White,
                    fontSize = 16.sp,
                )
            }
        }
    }
}

private val HighlightDividerColor: Color = Color(0xFF2E3940)
private val HighlightDropdownBorderColor: Color = Color(0xFF998C5F)

const val TEST_TAG_DROPDOWN_TRIGGER: String = "award_dropdown_trigger"
