package com.example.aiddproject.kudos.compose.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import com.example.aiddproject.R
import com.example.aiddproject.kudos.compose.ui.WriteKudoTestTags

/**
 * G — "Gửi lời cám ơn và ghi nhận ẩn danh" checkbox (Figma `6885:9363`).
 *
 * Whole-row click target per Material guidance + 48dp minimum height
 * (Constitution III). The Checkbox onCheckedChange is null so M3
 * doesn't route the tap; the Row's clickable wrapper is the single
 * tap surface.
 */
@Composable
fun AnonymousToggle(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val stateDesc =
        stringResource(
            if (checked) R.string.a11y_write_kudo_anonymous_checked else R.string.a11y_write_kudo_anonymous_unchecked,
        )
    val label = stringResource(R.string.write_kudo_anonymous_label)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier =
            modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .clickable(role = Role.Checkbox) { onCheckedChange(!checked) }
                .padding(horizontal = 8.dp)
                .testTag(WriteKudoTestTags.ANONYMOUS_TOGGLE)
                .semantics {
                    role = Role.Checkbox
                    contentDescription = label
                    stateDescription = stateDesc
                },
    ) {
        Checkbox(checked = checked, onCheckedChange = null)
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(start = 8.dp),
        )
    }
}
