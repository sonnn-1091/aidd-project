package com.example.aiddproject.home.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.aiddproject.R
import com.example.aiddproject.core.ui.rememberSingleClickHandler
import com.example.aiddproject.home.domain.states.KudosState

/**
 * `mms_5_kudos` (`6885:9039`) — flag-gated section with banner + heading + body
 * + Chi tiết button. Renders nothing when [KudosState.Hidden] (Q-Home-9 — only
 * the lower section is gated, not FAB S/Kudos / NavBar Kudos / ABOUT KUDOS).
 *
 * The banner uses Coil's [AsyncImage] with the local `ic_kudos_banner` drawable
 * as both placeholder and error fallback, so a 404 / network failure on the
 * remote URL keeps the layout intact (FR-006). When no remote URL is set we
 * skip Coil and draw the local asset directly.
 */
@Composable
fun KudosSection(
    state: KudosState,
    onChiTietClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (state == KudosState.Hidden) return
    val chiTietClick = rememberSingleClickHandler(onClick = onChiTietClick)
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .testTag(TEST_TAG_KUDOS_SECTION),
    ) {
        Text(
            text = stringResource(R.string.home_section_kudos_title),
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(12.dp))
        KudosBanner(state = state)
        Spacer(Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.home_kudos_note_heading),
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.home_kudos_note_body),
            color = Color.White,
            fontSize = 12.sp,
            lineHeight = 18.sp,
            fontWeight = FontWeight.Light,
        )
        Spacer(Modifier.height(16.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier =
                Modifier
                    .clickable(onClick = chiTietClick)
                    .testTag(TEST_TAG_KUDOS_CHI_TIET),
        ) {
            Text(
                text = stringResource(R.string.home_link_chi_tiet),
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
            )
            Spacer(Modifier.width(4.dp))
            Icon(
                painter = painterResource(R.drawable.ic_chevron_right),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
private fun KudosBanner(state: KudosState) {
    val bannerUrl = (state as? KudosState.Loaded)?.summary?.bannerImageUrl
    val bannerModifier =
        Modifier
            .fillMaxWidth()
            .height(146.dp)
            .clip(RoundedCornerShape(12.dp))
            .testTag(TEST_TAG_KUDOS_BANNER)
    if (bannerUrl != null) {
        AsyncImage(
            model = bannerUrl,
            contentDescription = null,
            placeholder = painterResource(R.drawable.ic_kudos_banner),
            error = painterResource(R.drawable.ic_kudos_banner),
            contentScale = ContentScale.Crop,
            modifier = bannerModifier,
        )
    } else {
        Image(
            painter = painterResource(R.drawable.ic_kudos_banner),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = bannerModifier,
        )
    }
}

const val TEST_TAG_KUDOS_SECTION: String = "kudos_section"
const val TEST_TAG_KUDOS_BANNER: String = "kudos_banner"
const val TEST_TAG_KUDOS_CHI_TIET: String = "kudos_chi_tiet"
