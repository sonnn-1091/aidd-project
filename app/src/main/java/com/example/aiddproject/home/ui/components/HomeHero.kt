package com.example.aiddproject.home.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
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
 *  - Event info block (date / venue / livestream tagline) — Phase 11 (T107)
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
            Spacer(Modifier.height(24.dp))
            EventInfoBlock(modifier = Modifier.padding(horizontal = 20.dp))
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

/**
 * Countdown row — Figma `6885:8988`. Each unit (DAYS / HOURS / MINUTES) is a
 * column whose digits row contains TWO 32×56dp digit cells side-by-side.
 * Cell styling matches `6885:8992`: 0.5dp `SaaCream` border, 8dp corner
 * radius, vertical white→10%-white gradient at 50% opacity.
 *
 * Note on the digit font: Figma uses a "Digital Numbers" display family that
 * isn't bundled in the app. We substitute [FontFamily.Monospace] as the
 * closest in-stock approximation; constitution Principle II forbids us from
 * inventing visual values, so this is documented here as a deliberate
 * substitution rather than a guess.
 */
@Composable
private fun CountdownRow(countdown: CountdownState) {
    val daysLabel = stringResource(R.string.home_countdown_days_label)
    val hoursLabel = stringResource(R.string.home_countdown_hours_label)
    val minutesLabel = stringResource(R.string.home_countdown_min_label)
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
        CountdownCell(value = countdown.days, label = daysLabel)
        CountdownCell(value = countdown.hours, label = hoursLabel)
        CountdownCell(value = countdown.minutes, label = minutesLabel)
    }
}

@Composable
private fun CountdownCell(
    value: Int,
    label: String,
) {
    // Pad to at least 2 digits — the Figma reference only shows 2 cells per
    // unit, but a pre-event countdown across multiple months can exceed
    // 99 days, so we render however many cells the value needs (each digit
    // gets its own box) rather than truncating.
    val padded = value.toString().padStart(2, '0')
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            padded.forEach { digit -> DigitBox(digit = digit) }
        }
        Text(
            text = label,
            color = Color.White,
            fontSize = 18.sp,
            lineHeight = 24.sp,
            fontWeight = FontWeight.Normal,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun DigitBox(digit: Char) {
    Box(
        modifier =
            Modifier
                .size(width = 32.dp, height = 56.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(DigitBoxGradient)
                .border(0.5.dp, SaaCream, RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = digit.toString(),
            color = Color.White,
            fontSize = 32.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Normal,
        )
    }
}

private val DigitBoxGradient: Brush =
    Brush.verticalGradient(
        colors =
            listOf(
                Color.White.copy(alpha = 0.5f),
                Color.White.copy(alpha = 0.05f),
            ),
    )

/**
 * Event-info block — Figma `6885:9016`. Three rows:
 *  1. "Thời gian: <date>"   — label 14sp R300 white, value 18sp R400 cream.
 *  2. "Địa điểm: <venue>"   — same pattern.
 *  3. Livestream tagline    — 14sp R400 white, full-width line wrap allowed.
 *
 * Date and venue values are brand-fixed (`home_event_date_value`,
 * `home_event_location_value`); labels and the livestream tagline are
 * localized.
 */
@Composable
private fun EventInfoBlock(modifier: Modifier = Modifier) {
    val timeLabel = stringResource(R.string.home_event_time_label)
    val dateValue = stringResource(R.string.home_event_date_value)
    val locationLabel = stringResource(R.string.home_event_location_label)
    val locationValue = stringResource(R.string.home_event_location_value)
    val livestream = stringResource(R.string.home_event_livestream)
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        EventInfoRow(label = timeLabel, value = dateValue)
        EventInfoRow(label = locationLabel, value = locationValue)
        Text(
            text = livestream,
            color = Color.White,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            fontWeight = FontWeight.Normal,
        )
    }
}

@Composable
private fun EventInfoRow(
    label: String,
    value: String,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = label,
            color = Color.White,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            fontWeight = FontWeight.Light,
        )
        Text(
            text = value,
            color = SaaCream,
            fontSize = 18.sp,
            lineHeight = 24.sp,
            fontWeight = FontWeight.Normal,
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
