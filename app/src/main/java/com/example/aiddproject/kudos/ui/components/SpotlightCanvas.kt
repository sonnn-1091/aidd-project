package com.example.aiddproject.kudos.ui.components

import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.aiddproject.R
import com.example.aiddproject.kudos.ui.KudosTestTags

/**
 * Spotlight pan/zoom canvas (spec § US9).
 *
 * Renders the baked Figma asset `kudos_spotlight` (node `6885:9101`)
 * and applies the user's pan + zoom transform via [graphicsLayer].
 * The image already contains the network of Sunner names + edges
 * + watermark — the Compose layer only handles gesture transforms.
 *
 * Match highlighting is no longer drawn on top of the image (the
 * baked layout doesn't expose per-node positions). The
 * [com.example.aiddproject.kudos.domain.SpotlightSearchResult] is
 * still surfaced as inline "no results" copy by [SpotlightBoard].
 */
@Composable
fun SpotlightCanvas(modifier: Modifier = Modifier) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    val transformableState =
        rememberTransformableState { zoomChange, panChange, _ ->
            scale = (scale * zoomChange).coerceIn(0.8f, 2.5f)
            offset += panChange
        }
    Image(
        painter = painterResource(R.drawable.kudos_spotlight),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier =
            modifier
                .fillMaxWidth()
                .height(280.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.Black.copy(alpha = 0.4f))
                .transformable(state = transformableState)
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offset.x,
                    translationY = offset.y,
                ).testTag(KudosTestTags.SPOTLIGHT_CANVAS),
    )
}
