package com.example.aiddproject.home.ui.components

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.aiddproject.R
import com.example.aiddproject.ui.theme.SaaCream

enum class HomeNavTab(
    @DrawableRes val icon: Int,
    @StringRes val label: Int,
) {
    Saa2025(R.drawable.ic_navbar_saa, R.string.home_navbar_saa_2025),
    Awards(R.drawable.ic_navbar_awards, R.string.home_navbar_awards),
    Kudos(R.drawable.ic_navbar_kudos, R.string.home_navbar_kudos),
    Profile(R.drawable.ic_navbar_profile, R.string.home_navbar_profile),
}

/**
 * `mms_7_nav bar` (`6885:9056`) — Material 3 bottom NavigationBar with 4 tabs.
 * SAA 2025 is active on Home; the visual highlight uses the brand cream
 * (`SaaCream`).
 */
@Composable
fun HomeBottomBar(
    selected: HomeNavTab,
    onTabSelect: (HomeNavTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    NavigationBar(modifier = modifier) {
        HomeNavTab.entries.forEach { tab ->
            val isActive = tab == selected
            NavigationBarItem(
                selected = isActive,
                onClick = { onTabSelect(tab) },
                modifier =
                    Modifier.semantics { role = Role.Tab },
                icon = {
                    Icon(
                        painter = painterResource(tab.icon),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                    )
                },
                label = {
                    Text(
                        text = stringResource(tab.label),
                        maxLines = 1,
                    )
                },
                colors =
                    NavigationBarItemDefaults.colors(
                        selectedIconColor = SaaCream,
                        selectedTextColor = SaaCream,
                        indicatorColor = SaaCream.copy(alpha = 0.16f),
                    ),
            )
        }
    }
}
