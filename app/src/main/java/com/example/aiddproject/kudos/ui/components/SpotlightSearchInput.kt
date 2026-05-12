package com.example.aiddproject.kudos.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.aiddproject.R
import com.example.aiddproject.kudos.ui.KudosTestTags
import com.example.aiddproject.ui.theme.SaaCream

/**
 * Spotlight search input (spec § US9).
 *
 * Enforces `maxLength = 100` per spec — extra characters are dropped
 * silently before the callback fires.
 */
@Composable
fun SpotlightSearchInput(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = query,
        onValueChange = { incoming ->
            if (incoming.length <= MAX_LENGTH) onQueryChange(incoming)
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.6f),
            )
        },
        placeholder = {
            Text(
                text = stringResource(R.string.kudos_spotlight_search_placeholder),
                color = Color.White.copy(alpha = 0.5f),
            )
        },
        singleLine = true,
        modifier =
            modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .testTag(KudosTestTags.SPOTLIGHT_SEARCH_INPUT),
        colors =
            OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = SaaCream,
                unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                cursorColor = SaaCream,
            ),
    )
}

private const val MAX_LENGTH: Int = 100
