package com.example.aiddproject.awarddetail.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.aiddproject.R
import com.example.aiddproject.ui.theme.SaaCream

/**
 * Hero badge block (Figma `mms_2.3_award` `6885:10292` — badge image
 * area + title row). Coil loads the badge from [imageUrl]; on null /
 * load failure it falls back to `ic_award_badge_placeholder` per
 * TR-007 + FR-008.
 */
@Composable
fun AwardHeroBlock(
    awardName: String,
    imageUrl: String?,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val badgeContentDescription = stringResource(R.string.a11y_award_badge_image, awardName)
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Box(
            modifier =
                Modifier
                    .testTag(TEST_TAG_AWARD_BADGE_IMAGE)
                    .size(240.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .border(
                        width = 1.dp,
                        color = SaaCream.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(16.dp),
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
                modifier = Modifier.size(240.dp),
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Spacer(Modifier.width(0.dp))
            Text(
                text = awardName,
                color = SaaCream,
                style =
                    MaterialTheme.typography.titleLarge.copy(
                        fontSize = 20.sp,
                        lineHeight = 24.sp,
                        fontWeight = FontWeight.Bold,
                    ),
            )
        }
    }
}

const val TEST_TAG_AWARD_BADGE_IMAGE: String = "award_badge_image"
