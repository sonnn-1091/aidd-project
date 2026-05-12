package com.example.aiddproject.kudos.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
 * Spotlight pan/zoom canvas (spec § US9). Renders the baked Figma
 * asset and applies the user's pan + zoom transform via
 * [graphicsLayer] on the inner Image so the outer 12dp-rounded
 * container stays clipped during transforms.
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
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .aspectRatio(SPOTLIGHT_ASPECT)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.Black.copy(alpha = 0.4f))
                .transformable(state = transformableState)
                .testTag(KudosTestTags.SPOTLIGHT_CANVAS),
    ) {
        Image(
            painter = painterResource(R.drawable.kudos_spotlight),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier =
                Modifier
                    .fillMaxSize()
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offset.x,
                        translationY = offset.y,
                    ),
        )
    }
}

// Spotlight image native aspect ratio: 672 / 318 ≈ 2.11.
private const val SPOTLIGHT_ASPECT: Float = 2.11f
