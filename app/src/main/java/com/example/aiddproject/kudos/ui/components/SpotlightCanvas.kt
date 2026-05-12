package com.example.aiddproject.kudos.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.aiddproject.kudos.domain.SpotlightGraph
import com.example.aiddproject.kudos.domain.SpotlightSearchResult
import com.example.aiddproject.kudos.ui.KudosTestTags
import com.example.aiddproject.ui.theme.SaaCream
import kotlin.math.cos
import kotlin.math.sin

/**
 * Spotlight pan/zoom canvas (spec § US9).
 *
 * - Nodes are arranged on a deterministic circle so the layout is
 *   stable across recompositions without a layout solver.
 * - `rememberTransformableState` handles both pinch-zoom (scale) and
 *   two-finger pan (offset) within sensible bounds.
 * - When [searchResult] is a Match, the matched node renders as a
 *   filled SaaCream halo; other nodes stay dim.
 */
@Composable
fun SpotlightCanvas(
    graph: SpotlightGraph,
    searchResult: SpotlightSearchResult,
    modifier: Modifier = Modifier,
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    val transformableState =
        rememberTransformableState { zoomChange, panChange, _ ->
            scale = (scale * zoomChange).coerceIn(0.5f, 2.5f)
            offset += panChange
        }
    val matchId =
        when (searchResult) {
            is SpotlightSearchResult.Match -> searchResult.node.id
            else -> null
        }
    Canvas(
        modifier =
            modifier
                .fillMaxWidth()
                .height(280.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.Black.copy(alpha = 0.4f))
                .transformable(state = transformableState)
                .testTag(KudosTestTags.SPOTLIGHT_CANVAS),
    ) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val centerX = canvasWidth / 2f
        val centerY = canvasHeight / 2f
        val radius = (minOf(canvasWidth, canvasHeight) / 2f) * 0.7f
        translate(left = offset.x, top = offset.y) {
            // Edges first (rendered behind nodes).
            graph.edges.forEach { edge ->
                val senderIdx = graph.nodes.indexOfFirst { it.id == edge.senderId }
                val recipientIdx = graph.nodes.indexOfFirst { it.id == edge.recipientId }
                if (senderIdx >= 0 && recipientIdx >= 0) {
                    val from = nodePosition(senderIdx, graph.nodes.size, centerX, centerY, radius, scale)
                    val to = nodePosition(recipientIdx, graph.nodes.size, centerX, centerY, radius, scale)
                    drawLine(
                        color = SaaCream.copy(alpha = 0.3f),
                        start = from,
                        end = to,
                        strokeWidth = (1.5f + edge.weight) * scale,
                    )
                }
            }
            // Nodes on top.
            graph.nodes.forEachIndexed { idx, node ->
                val pos = nodePosition(idx, graph.nodes.size, centerX, centerY, radius, scale)
                val isMatch = node.id == matchId
                drawCircle(
                    color = if (isMatch) SaaCream else SaaCream.copy(alpha = 0.4f),
                    radius = (if (isMatch) 14f else 10f) * scale,
                    center = pos,
                )
            }
        }
    }
}

private fun nodePosition(
    index: Int,
    total: Int,
    centerX: Float,
    centerY: Float,
    radius: Float,
    scale: Float,
): Offset {
    val angle = (2.0 * Math.PI * index / total).toFloat()
    val r = radius * scale
    return Offset(
        x = centerX + r * cos(angle),
        y = centerY + r * sin(angle),
    )
}
