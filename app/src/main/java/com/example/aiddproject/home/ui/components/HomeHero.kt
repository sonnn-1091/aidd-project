package com.example.aiddproject.home.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aiddproject.R
import com.example.aiddproject.core.ui.rememberSingleClickHandler
import com.example.aiddproject.home.domain.states.CountdownState
import com.example.aiddproject.ui.theme.SaaCream
import com.example.aiddproject.ui.theme.SaaInk

/**
 * Hero block — `mms_2_content` (`6885:8983`):
 *  - ROOT FURTHER tagline image (`ic_logo_root_further`, 247×109dp)
 *  - Countdown (DAYS / HOURS / MINUTES) with "Coming soon" when pre-event
 *  - ABOUT AWARD + ABOUT KUDOS Material buttons (always visible per Q-Home-9)
 *
 * Countdown uses a Polite live region so TalkBack re-announces only on minute
 * changes (the live-region key follows minutes, not seconds — spec § Behavioral
 * Accessibility).
 */
@Composable
fun HomeHero(
    countdown: CountdownState,
    onAboutAwardClick: () -> Unit,
    onAboutKudosClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            painter = painterResource(R.drawable.ic_logo_root_further),
            contentDescription = stringResource(R.string.brand_root_further),
            modifier =
                Modifier
                    .width(247.dp)
                    .height(109.dp),
        )
        Spacer(Modifier.height(8.dp))
        if (countdown.isPreEvent) {
            Text(
                text = stringResource(R.string.home_coming_soon),
                color = Color.White,
                style = MaterialTheme.typography.labelLarge,
            )
            Spacer(Modifier.height(8.dp))
            CountdownRow(countdown = countdown)
            Spacer(Modifier.height(20.dp))
        } else {
            Spacer(Modifier.height(28.dp))
        }
        // Both buttons remain visible regardless of `isKudosAvailable` (Q-Home-9).
        // Each click is wrapped via `rememberSingleClickHandler` (TR-005) so a
        // finger-bounce double-tap can't push two destinations on the back stack.
        val aboutAwardClick = rememberSingleClickHandler(onClick = onAboutAwardClick)
        val aboutKudosClick = rememberSingleClickHandler(onClick = onAboutKudosClick)
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(horizontal = 20.dp),
        ) {
            HeroButton(
                label = stringResource(R.string.home_btn_about_award),
                onClick = aboutAwardClick,
                modifier = Modifier.weight(1f),
            )
            HeroButton(
                label = stringResource(R.string.home_btn_about_kudos),
                onClick = aboutKudosClick,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun CountdownRow(countdown: CountdownState) {
    val daysLabel = stringResource(R.string.home_countdown_days_label)
    val hoursLabel = stringResource(R.string.home_countdown_hours_label)
    val minutesLabel = stringResource(R.string.home_countdown_min_label)
    // Single contentDescription on the live-region root, keyed on the displayed
    // (days, hours, minutes) tuple — minute-granularity. The engine ticks every
    // 1s but the value only changes on minute boundaries, so this string is
    // identical across same-minute ticks and TalkBack does not re-announce
    // (spec § Behavioral Accessibility).
    val a11yLabel =
        "${countdown.days} $daysLabel, ${countdown.hours} $hoursLabel, ${countdown.minutes} $minutesLabel"
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier =
            Modifier.semantics(mergeDescendants = true) {
                liveRegion = LiveRegionMode.Polite
                contentDescription = a11yLabel
            },
    ) {
        CountdownCell(
            value = countdown.days,
            label = daysLabel,
        )
        CountdownCell(
            value = countdown.hours,
            label = hoursLabel,
        )
        CountdownCell(
            value = countdown.minutes,
            label = minutesLabel,
        )
    }
}

@Composable
private fun CountdownCell(
    value: Int,
    label: String,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value.toString().padStart(2, '0'),
            color = SaaCream,
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = label,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun HeroButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(4.dp),
        colors =
            ButtonDefaults.buttonColors(
                containerColor = SaaCream,
                contentColor = SaaInk,
            ),
        modifier = modifier.height(40.dp),
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(Modifier.width(4.dp))
        Icon(
            painter = painterResource(R.drawable.ic_chevron_right),
            contentDescription = null,
            tint = SaaInk,
            modifier = Modifier.size(16.dp),
        )
    }
}
