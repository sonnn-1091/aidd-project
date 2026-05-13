package com.example.aiddproject.kudos.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aiddproject.R
import com.example.aiddproject.core.ui.rememberSingleClickHandler
import com.example.aiddproject.kudos.ui.KudosTestTags

/**
 * "View all Kudos" link — Figma `Button` (`6891:15987`) at the
 * tail of `Danh sách Kudo` (`6891:15986`). Centered horizontally
 * inside the feed column, 14sp Montserrat Medium WHITE label +
 * 24dp arrow icon.
 */
@Composable
fun ViewAllKudosLink(
    onViewAllKudos: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val click = rememberSingleClickHandler { onViewAllKudos() }
    val a11y = stringResource(R.string.a11y_kudos_view_all)
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            modifier =
                Modifier
                    .clickable(onClick = click)
                    .padding(horizontal = 16.dp, vertical = 10.dp)
                    .semantics {
                        role = Role.Button
                        contentDescription = a11y
                    }.testTag(KudosTestTags.VIEW_ALL_KUDOS_LINK),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = stringResource(R.string.kudos_view_all_label),
                color = Color.White,
                style =
                    MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        fontWeight = FontWeight.Medium,
                    ),
            )
            Image(
                painter = painterResource(R.drawable.ic_kudos_view_detail_arrow),
                contentDescription = null,
                colorFilter = ColorFilter.tint(Color.White),
                modifier = Modifier.size(24.dp),
            )
        }
    }
}
