package com.example.aiddproject.kudos.compose.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.aiddproject.R
import com.example.aiddproject.kudos.compose.ui.WriteKudoTestTags

/**
 * A — Static instructional header text (Figma `6885:9292`).
 *
 * Visual chrome (color / size / spacing) is fetched on-demand by
 * `momorph.implement-ui` at task-execution time — this composable
 * carries only the behavior contract (testTag + the localised
 * string).
 */
@Composable
fun HeaderText(modifier: Modifier = Modifier) {
    Text(
        text = stringResource(R.string.write_kudo_header),
        style = MaterialTheme.typography.titleMedium,
        textAlign = TextAlign.Center,
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .testTag(WriteKudoTestTags.HEADER),
    )
}
