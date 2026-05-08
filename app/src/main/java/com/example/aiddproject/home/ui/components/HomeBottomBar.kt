package com.example.aiddproject.home.ui.components

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.aiddproject.R
import com.example.aiddproject.core.ui.rememberSingleClickHandler
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
 * SAA 2025 is active on Home; visual highlight uses the brand cream
 * (`SaaCream`). Each item:
 *  - Exposes `Role.Tab` semantics (TR-009).
 *  - Surfaces a localized active/inactive content description merged with the
 *    tab label so TalkBack reads e.g. "SAA 2025, đang chọn, tab".
 *  - Enforces a minimum 48dp touch target (Constitution Principle III).
 *  - Wraps onClick in [rememberSingleClickHandler] so a finger-bounce double-tap
 *    can never push two destinations on the back stack (TR-005).
 */
@Composable
fun HomeBottomBar(
    selected: HomeNavTab,
    onTabSelect: (HomeNavTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    NavigationBar(modifier = modifier.testTag(TEST_TAG_HOME_BOTTOM_BAR)) {
        HomeNavTab.entries.forEach { tab ->
            val isActive = tab == selected
            val label = stringResource(tab.label)
            val a11y =
                if (isActive) {
                    stringResource(R.string.a11y_home_navbar_tab_active, label)
                } else {
                    stringResource(R.string.a11y_home_navbar_tab_inactive, label)
                }
            val tabClick = rememberSingleClickHandler { onTabSelect(tab) }
            NavigationBarItem(
                selected = isActive,
                onClick = tabClick,
                modifier =
                    Modifier
                        .heightIn(min = 48.dp)
                        .testTag(testTagForTab(tab))
                        .semantics {
                            role = Role.Tab
                            contentDescription = a11y
                        },
                icon = {
                    Icon(
                        painter = painterResource(tab.icon),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                    )
                },
                label = {
                    Text(
                        text = label,
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

const val TEST_TAG_HOME_BOTTOM_BAR: String = "home_bottom_bar"

fun testTagForTab(tab: HomeNavTab): String = "home_navbar_tab_${tab.name.lowercase()}"
