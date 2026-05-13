package com.example.aiddproject.kudos.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aiddproject.R
import com.example.aiddproject.core.ui.rememberSingleClickHandler
import com.example.aiddproject.kudos.domain.Department
import com.example.aiddproject.kudos.domain.Hashtag
import com.example.aiddproject.kudos.ui.KudosTestTags
import com.example.aiddproject.ui.theme.SaaCream

/**
 * Highlight filter row — two pill triggers, each acts as the anchor
 * for its own [HashtagFilterDropdown] / [DepartmentFilterDropdown]
 * M3 menu (mirrors Home's `LanguageSelector` pattern).
 */
@Composable
fun HighlightFilterRow(
    selectedHashtagLabel: String?,
    selectedDepartmentLabel: String?,
    hashtags: List<Hashtag>,
    departments: List<Department>,
    activeHashtagId: String?,
    activeDepartmentId: String?,
    onSelectHashtag: (Hashtag?) -> Unit,
    onSelectDepartment: (Department?) -> Unit,
    modifier: Modifier = Modifier,
) {
    var hashtagOpen by remember { mutableStateOf(false) }
    var departmentOpen by remember { mutableStateOf(false) }

    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .testTag(KudosTestTags.FILTER_ROW),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(modifier = Modifier.testTag(KudosTestTags.FILTER_HASHTAG_TRIGGER)) {
            FilterTriggerPill(
                label = selectedHashtagLabel ?: stringResource(R.string.kudos_filter_hashtag_label),
                onTap = { hashtagOpen = !hashtagOpen },
            )
            HashtagFilterDropdown(
                expanded = hashtagOpen,
                hashtags = hashtags,
                activeHashtagId = activeHashtagId,
                onSelect = onSelectHashtag,
                onDismiss = { hashtagOpen = false },
            )
        }
        Box(modifier = Modifier.testTag(KudosTestTags.FILTER_DEPARTMENT_TRIGGER)) {
            FilterTriggerPill(
                label = selectedDepartmentLabel ?: stringResource(R.string.kudos_filter_department_label),
                onTap = { departmentOpen = !departmentOpen },
            )
            DepartmentFilterDropdown(
                expanded = departmentOpen,
                departments = departments,
                activeDepartmentId = activeDepartmentId,
                onSelect = onSelectDepartment,
                onDismiss = { departmentOpen = false },
            )
        }
    }
}

@Composable
private fun FilterTriggerPill(
    label: String,
    onTap: () -> Unit,
) {
    val click = rememberSingleClickHandler { onTap() }
    Row(
        modifier =
            Modifier
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
        Image(
            painter = painterResource(R.drawable.ic_kudos_filter_chevron),
            contentDescription = null,
            modifier = Modifier.size(24.dp),
        )
    }
}

private val FilterBorderColor: Color = Color(0xFF998C5F)
