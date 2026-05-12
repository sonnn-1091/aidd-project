package com.example.aiddproject.kudos.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aiddproject.R
import com.example.aiddproject.kudos.ui.KudosTestTags

/** "1/5" page indicator for the Highlight carousel (spec § US4). */
@Composable
fun PageIndicator(
    currentPage: Int,
    pageCount: Int,
    modifier: Modifier = Modifier,
) {
    val label = stringResource(R.string.a11y_kudos_carousel_page_indicator, currentPage + 1, pageCount)
    Text(
        text = "${currentPage + 1}/$pageCount",
        color = Color.White.copy(alpha = 0.75f),
        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
        textAlign = TextAlign.Center,
        modifier =
            modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 4.dp)
                .testTag(KudosTestTags.PAGE_INDICATOR)
                .semantics { contentDescription = label },
    )
}
