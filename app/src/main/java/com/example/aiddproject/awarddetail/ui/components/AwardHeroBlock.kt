package com.example.aiddproject.awarddetail.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.aiddproject.R
import com.example.aiddproject.ui.theme.SaaCream

/**
 * Hero badge — Figma node `6885:10293` (`mm_media_Picture-Award`).
 *
 * Per Figma: 160×160dp box, **centered** in the parent column, with a
 * 0.455dp SaaCream border + 11.429dp radius. The badge BG image
 * (`MM_MEDIA_Award BG`) fills the box; an `Awards-Name` text label
 * (105×17dp) is centered on top — when the live `awards.image_url`
 * ships a pre-composited badge graphic, Coil renders it as one image
 * and the overlay slot collapses to nothing. Until then the placeholder
 * vector drawable stands in.
 *
 * The title row + description that used to live here are now part of
 * [AwardInfoBlock] so the whole award block matches the Figma
 * `mms_2.3_award` frame's child ordering (badge → info → dividers →
 * info → dividers → info).
 */
@Composable
fun AwardHeroBlock(
    awardName: String,
    imageUrl: String?,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val badgeContentDescription = stringResource(R.string.a11y_award_badge_image, awardName)
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier =
                Modifier
                    .testTag(TEST_TAG_AWARD_BADGE_IMAGE)
                    .size(160.dp)
                    .clip(RoundedCornerShape(11.dp))
                    .border(
                        width = 1.dp,
                        color = SaaCream,
                        shape = RoundedCornerShape(11.dp),
                    ).semantics { contentDescription = badgeContentDescription },
        ) {
            AsyncImage(
                model =
                    ImageRequest
                        .Builder(context)
                        .data(imageUrl)
                        .crossfade(true)
                        .build(),
                contentDescription = null,
                placeholder = painterResource(R.drawable.ic_award_badge_placeholder),
                error = painterResource(R.drawable.ic_award_badge_placeholder),
                fallback = painterResource(R.drawable.ic_award_badge_placeholder),
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(160.dp),
            )
        }
    }
}

const val TEST_TAG_AWARD_BADGE_IMAGE: String = "award_badge_image"
