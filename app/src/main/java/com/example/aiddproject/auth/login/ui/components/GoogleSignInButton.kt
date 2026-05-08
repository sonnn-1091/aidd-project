package com.example.aiddproject.auth.login.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aiddproject.R
import com.example.aiddproject.ui.theme.SaaCream
import com.example.aiddproject.ui.theme.SaaInk

/**
 * The single CTA on the Login screen (FR-004, FR-015).
 *
 * Visual spec from MoMorph node `6885:8969` (Figma frame `8HGlvYGJWq`):
 *   246×40dp, padding 12dp, 4dp corner radius, gap 8dp, label-then-icon order,
 *   bg `#FFEA9E` (SaaCream), label `#00101A` (SaaInk), Montserrat 14sp/500.
 *
 * - The label "LOGIN With Google" is brand-fixed (FR-015) and stays visible during
 *   loading; the spinner replaces only the trailing Google G icon (FR-004).
 * - Disabled while in flight or when Google Play Services is unavailable.
 * - Accessibility: while loading, a polite live region announces "Signing in"
 *   (`a11y_cta_loading`) so TalkBack users hear the state change without re-tapping.
 */
@Composable
fun GoogleSignInButton(
    isLoading: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val idleLabel = stringResource(R.string.a11y_cta_idle)
    val loadingLabel = stringResource(R.string.a11y_cta_loading)
    Button(
        onClick = onClick,
        enabled = enabled && !isLoading,
        shape = RoundedCornerShape(4.dp),
        colors =
            ButtonDefaults.buttonColors(
                containerColor = SaaCream,
                contentColor = SaaInk,
                disabledContainerColor = SaaCream.copy(alpha = 0.6f),
                disabledContentColor = SaaInk.copy(alpha = 0.6f),
            ),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
        modifier =
            modifier
                .testTag(TEST_TAG)
                .width(246.dp)
                .defaultMinSize(minHeight = 48.dp)
                .semantics {
                    contentDescription = if (isLoading) loadingLabel else idleLabel
                    liveRegion = LiveRegionMode.Polite
                },
    ) {
        Text(
            text = stringResource(R.string.login_cta_label),
            style =
                MaterialTheme.typography.labelLarge.copy(
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    fontWeight = FontWeight.Medium,
                    color = SaaInk,
                ),
        )
        Spacer(Modifier.width(8.dp))
        if (isLoading) {
            CircularProgressIndicator(
                modifier =
                    Modifier
                        .size(18.dp)
                        .testTag(SPINNER_TEST_TAG),
                color = SaaInk,
                strokeWidth = ProgressIndicatorDefaults.CircularStrokeWidth,
            )
        } else {
            Icon(
                painter = painterResource(R.drawable.ic_google_g),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.size(24.dp),
            )
        }
    }
}

const val TEST_TAG: String = "google_sign_in_button"
const val SPINNER_TEST_TAG: String = "google_sign_in_button_spinner"
