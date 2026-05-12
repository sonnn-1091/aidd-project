package com.example.aiddproject.kudos.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.aiddproject.kudos.domain.Department

/**
 * Department filter — same M3 [DropdownMenu] chrome as
 * [HashtagFilterDropdown]. Re-selecting active row clears the filter.
 */
@Composable
fun DepartmentFilterDropdown(
    expanded: Boolean,
    departments: List<Department>,
    activeDepartmentId: String?,
    onSelect: (Department?) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        containerColor = MenuSurfaceColor,
        border = BorderStroke(1.dp, MenuBorderColor),
    ) {
        Column(modifier = Modifier.padding(horizontal = 6.dp)) {
            departments.forEach { department ->
                FilterMenuItem(
                    label = department.name,
                    isActive = department.id == activeDepartmentId,
                    onTap = {
                        onDismiss()
                        if (department.id == activeDepartmentId) onSelect(null) else onSelect(department)
                    },
                )
            }
        }
    }
}
