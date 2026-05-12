package com.example.aiddproject.kudos.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aiddproject.R
import com.example.aiddproject.core.ui.rememberSingleClickHandler
import com.example.aiddproject.kudos.ui.KudosTestTags
import com.example.aiddproject.ui.theme.SaaCream

/**
 * Highlight filter row stub (Figma `B.1.1` + `B.1.2`).
 *
 * Phase 3 MVP renders two passive pill triggers — Hashtag + Phòng
 * ban — wired to callbacks so the hub renders. Bottom-sheet pickers
 * + active selection rendering land in Phase 5 (US3).
 */
@Composable
fun HighlightFilterRow(
    selectedHashtagLabel: String?,
    selectedDepartmentLabel: String?,
    onHashtagTriggerTap: () -> Unit,
    onDepartmentTriggerTap: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .testTag(KudosTestTags.FILTER_ROW),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        FilterTrigger(
            label = selectedHashtagLabel ?: stringResource(R.string.kudos_filter_hashtag_label),
            onTap = onHashtagTriggerTap,
            modifier = Modifier.testTag(KudosTestTags.FILTER_HASHTAG_TRIGGER),
        )
        FilterTrigger(
            label = selectedDepartmentLabel ?: stringResource(R.string.kudos_filter_department_label),
            onTap = onDepartmentTriggerTap,
            modifier = Modifier.testTag(KudosTestTags.FILTER_DEPARTMENT_TRIGGER),
        )
    }
}

@Composable
private fun FilterTrigger(
    label: String,
    onTap: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val click = rememberSingleClickHandler { onTap() }
    Row(
        modifier =
            modifier
                .heightIn(min = 40.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(SaaCream.copy(alpha = 0.10f))
                .border(width = 1.dp, color = FilterBorderColor, shape = RoundedCornerShape(4.dp))
                .clickable(onClick = click)
                .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = label,
            color = Color.White,
            style =
                MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    letterSpacing = 0.25.sp,
                ),
        )
        Icon(
            imageVector = Icons.Filled.KeyboardArrowDown,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(24.dp),
        )
    }
}

private val FilterBorderColor: Color = Color(0xFF998C5F)
