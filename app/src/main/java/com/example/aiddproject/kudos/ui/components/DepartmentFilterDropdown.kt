package com.example.aiddproject.kudos.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.aiddproject.kudos.domain.Department

/**
 * Department filter ModalBottomSheet (spec § US3, T058).
 * Same shape as [HashtagFilterDropdown] — re-selecting active row
 * clears via `null` callback.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DepartmentFilterDropdown(
    departments: List<Department>,
    activeDepartmentId: String?,
    onSelect: (Department?) -> Unit,
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
            departments.forEach { department ->
                val isActive = department.id == activeDepartmentId
                FilterRow(
                    label = department.name,
                    isActive = isActive,
                    onTap = {
                        if (isActive) onSelect(null) else onSelect(department)
                        onDismiss()
                    },
                )
            }
        }
    }
}
