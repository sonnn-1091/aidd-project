package com.example.aiddproject.awarddetail.ui.components

import androidx.compose.foundation.background
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
import com.example.aiddproject.core.ui.rememberSingleClickHandler

/**
 * Highlight block (Figma `mms_B_Highlight` `6885:10283` + header
 * `mms_B.1_header` `6885:10284`). Sub-label + screen title + the
 * category dropdown anchor.
 *
 * **Phase 3 scope**: the dropdown trigger renders the active award's
 * name + chevron, but `onClick` invokes [onDropdownTriggerClick] which
 * is a no-op stub until Phase 4 wires the real
 * `AwardCategoryDropdown` composable.
 *
 * Touch-target floor: the trigger is `heightIn(min = 48.dp)` per
 * TR-003 + TR-008, even though the visible pill is 40dp tall.
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
                .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            text = stringResource(R.string.award_detail_sub_label),
            color = Color.White.copy(alpha = 0.7f),
            style =
                MaterialTheme.typography.bodySmall.copy(
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                ),
        )
        Text(
            text = stringResource(R.string.award_detail_title),
            color = Color.White,
            style =
                MaterialTheme.typography.headlineSmall.copy(
                    fontSize = 24.sp,
                    lineHeight = 28.sp,
                    fontWeight = FontWeight.Bold,
                ),
        )
        Spacer(Modifier.height(4.dp))
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
                modifier =
                    Modifier
                        .width(160.dp)
                        .height(40.dp)
                        .background(
                            color = Color(0x33998C5F),
                            shape = RoundedCornerShape(4.dp),
                        ).padding(horizontal = 12.dp),
            ) {
                Text(
                    text = activeAwardName ?: "",
                    color = Color.White,
                    style =
                        MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 14.sp,
                            lineHeight = 20.sp,
                            fontWeight = FontWeight.Medium,
                        ),
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = "▾",
                    color = Color.White,
                    fontSize = 16.sp,
                )
            }
        }
    }
}

const val TEST_TAG_DROPDOWN_TRIGGER: String = "award_dropdown_trigger"
