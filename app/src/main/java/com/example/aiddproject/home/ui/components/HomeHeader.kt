package com.example.aiddproject.home.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.aiddproject.R
import com.example.aiddproject.auth.login.ui.components.LanguageSelector
import com.example.aiddproject.core.locale.Language

/**
 * Home header (`mms_1_header` — `6885:9057`): SAA logo on the leading edge, then a
 * trailing actions row of language pill + search + bell. Sits above the keyvisual
 * background; the dark gradient overlay is applied at the screen level.
 *
 * Layout from the MoMorph instance: 375×104dp wrapping a 44dp iOS status-bar slot
 * we collapse into Android `systemBarsPadding` + a 60dp action row.
 */
@Composable
fun HomeHeader(
    selectedLanguage: Language,
    onLanguageSelected: (Language) -> Unit,
    onSearchClick: () -> Unit,
    onBellClick: () -> Unit,
    unreadCount: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .height(60.dp)
                .padding(start = 20.dp, end = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(R.drawable.ic_logo_saa),
            contentDescription = null,
            modifier =
                Modifier
                    .width(48.dp)
                    .height(44.dp),
        )
        Spacer(Modifier.weight(1f))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            LanguageSelector(
                selected = selectedLanguage,
                onSelect = onLanguageSelected,
            )
            IconButton(onClick = onSearchClick) {
                Icon(
                    painter = painterResource(R.drawable.ic_search),
                    contentDescription = stringResource(R.string.a11y_home_search),
                    tint = Color.White,
                    modifier = Modifier.size(24.dp),
                )
            }
            BellWithBadge(
                unreadCount = unreadCount,
                onClick = onBellClick,
            )
        }
    }
}
