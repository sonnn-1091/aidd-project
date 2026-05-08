package com.example.aiddproject.home.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aiddproject.R
import com.example.aiddproject.home.domain.Award
import com.example.aiddproject.home.domain.states.AwardsState

/**
 * Awards section — `mms_4_awards` (`6885:9030`). Header + horizontal scrollable
 * card list with four states: loading / empty / error+Retry / populated
 * (FR-003).
 */
@Composable
fun AwardsSection(
    state: AwardsState,
    onChiTietTap: (Award) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.home_section_awards_title),
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 20.dp),
        )
        Spacer(Modifier.height(12.dp))
        when (state) {
            AwardsState.Loading -> AwardsLoading()
            AwardsState.Empty -> AwardsEmpty()
            is AwardsState.Error -> AwardsError(onRetry = onRetry)
            is AwardsState.Populated ->
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(state.items, key = { it.id }) { award ->
                        AwardCard(
                            award = award,
                            onChiTietClick = { onChiTietTap(award) },
                        )
                    }
                }
        }
    }
}

@Composable
private fun AwardsLoading() {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(180.dp),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(32.dp),
            color = MaterialTheme.colorScheme.onPrimary,
        )
    }
}

@Composable
private fun AwardsEmpty() {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(180.dp)
                .padding(horizontal = 20.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(R.string.home_awards_empty),
            color = Color.White,
            fontSize = 14.sp,
        )
    }
}

@Composable
private fun AwardsError(onRetry: () -> Unit) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(180.dp)
                .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.home_awards_error),
            color = Color.White,
            fontSize = 14.sp,
        )
        Spacer(Modifier.height(8.dp))
        Button(onClick = onRetry) {
            Text(text = stringResource(R.string.home_action_retry))
        }
    }
}
