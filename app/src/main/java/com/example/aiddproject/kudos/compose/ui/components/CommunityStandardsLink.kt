package com.example.aiddproject.kudos.compose.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.aiddproject.R
import com.example.aiddproject.core.ui.rememberSingleClickHandler
import com.example.aiddproject.kudos.compose.ui.WriteKudoTestTags

/**
 * B.5 — "Tiêu chuẩn cộng đồng" text-link (Figma `6885:9303`).
 *
 * Wired via [rememberSingleClickHandler] so a fast double-tap can't
 * navigate twice. Visual chrome is fetched on-demand by
 * `momorph.implement-ui`.
 */
@Composable
fun CommunityStandardsLink(
    onTap: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val click = rememberSingleClickHandler(onClick = onTap)
    TextButton(
        onClick = click,
        modifier = modifier.testTag(WriteKudoTestTags.COMMUNITY_STANDARDS_LINK),
    ) {
        Text(
            text = stringResource(R.string.write_kudo_community_standards_link),
            style =
                MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.primary,
                    textDecoration = TextDecoration.Underline,
                ),
            modifier = Modifier.padding(vertical = 4.dp),
        )
    }
}
