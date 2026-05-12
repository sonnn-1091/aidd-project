package com.example.aiddproject.kudos.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aiddproject.R
import com.example.aiddproject.kudos.domain.states.SpotlightState
import com.example.aiddproject.kudos.ui.KudosTestTags
import com.example.aiddproject.ui.theme.SaaCream

/**
 * Spotlight Board stub (Figma `B.6` + `B.7`).
 *
 * Phase 3 MVP renders the section title + total kudos counter + a
 * placeholder canvas box. Real pan/zoom canvas + live Sunner search
 * land in Phase 10 (US9).
 */
@Composable
fun SpotlightBoard(
    state: SpotlightState,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .testTag(KudosTestTags.SPOTLIGHT),
    ) {
        Text(
            text = stringResource(R.string.kudos_section_spotlight_title),
            color = SaaCream,
            style = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
            modifier = Modifier.padding(bottom = 8.dp),
        )
        when (state) {
            SpotlightState.Loading -> SectionPlaceholder(text = stringResource(R.string.kudos_loading))
            SpotlightState.Empty -> SectionPlaceholder(text = stringResource(R.string.kudos_section_empty_generic))
            is SpotlightState.Error -> SectionPlaceholder(text = stringResource(state.messageRes))
            is SpotlightState.Loaded -> {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.kudos_spotlight_total_label),
                        color = Color.White.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                        modifier = Modifier.padding(end = 8.dp),
                    )
                    Text(
                        text = state.graph.totalKudosCount.toString(),
                        color = SaaCream,
                        style = MaterialTheme.typography.titleLarge.copy(fontSize = 24.sp, fontWeight = FontWeight.Bold),
                    )
                }
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.Black.copy(alpha = 0.4f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "${state.graph.nodes.size} Sunners · ${state.graph.edges.size} edges",
                        color = Color.White.copy(alpha = 0.6f),
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                    )
                }
            }
        }
    }
}
