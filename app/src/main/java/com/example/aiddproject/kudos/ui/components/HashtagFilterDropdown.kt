package com.example.aiddproject.kudos.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aiddproject.core.ui.rememberSingleClickHandler
import com.example.aiddproject.kudos.domain.Hashtag
import com.example.aiddproject.ui.theme.SaaCream

/**
 * Hashtag filter ModalBottomSheet (spec § US3, T057).
 *
 * Re-tapping the active hashtag clears the selection — the caller
 * passes `null` through [onSelect] in that case (no special arg).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HashtagFilterDropdown(
    hashtags: List<Hashtag>,
    activeHashtagId: String?,
    onSelect: (Hashtag?) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState()
    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF00070C),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            hashtags.forEach { hashtag ->
                val isActive = hashtag.id == activeHashtagId
                FilterRow(
                    label = "#${hashtag.tagName}",
                    isActive = isActive,
                    onTap = {
                        if (isActive) onSelect(null) else onSelect(hashtag)
                        onDismiss()
                    },
                )
            }
        }
    }
}

@Composable
internal fun FilterRow(
    label: String,
    isActive: Boolean,
    onTap: () -> Unit,
) {
    val click = rememberSingleClickHandler { onTap() }
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .clickable(onClick = click)
                .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            color = if (isActive) SaaCream else Color.White,
            style =
                MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 14.sp,
                    fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
                ),
        )
    }
}
