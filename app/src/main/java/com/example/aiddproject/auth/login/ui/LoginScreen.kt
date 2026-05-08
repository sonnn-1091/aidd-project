package com.example.aiddproject.auth.login.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.aiddproject.R
import com.example.aiddproject.auth.login.ui.components.GoogleSignInButton
import com.example.aiddproject.auth.login.ui.components.LanguageSelector
import com.example.aiddproject.core.locale.Language
import com.example.aiddproject.core.locale.LocaleViewModel

/**
 * Stateful entry point for the Login route. Owns the HiltViewModel + Scaffold;
 * delegates layout to [LoginScreenContent] so previews and UI tests can drive a
 * deterministic state without DI.
 */
@Composable
fun LoginScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToAccessDenied: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel(),
    localeViewModel: LocaleViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val language by localeViewModel.language.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    // T065: probe Google Play Services availability once on enter. The CTA stays
    // disabled and an actionable error_oauth_play_services snackbar fires when missing.
    // Demo mode bypasses the check entirely so emulators without GMS still flow through.
    LaunchedEffect(Unit) {
        val available =
            com.example.aiddproject.BuildConfig.DEMO_MODE ||
                com.google.android.gms.common.GoogleApiAvailability
                    .getInstance()
                    .isGooglePlayServicesAvailable(context) ==
                com.google.android.gms.common.ConnectionResult.SUCCESS
        viewModel.setPlayServicesAvailable(available)
        if (!available) {
            snackbarHostState.showSnackbar(
                message = context.getString(LoginError.PlayServicesUnavailable.messageRes),
                duration = SnackbarDuration.Indefinite,
            )
        }
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                LoginEvent.NavigateToHome -> onNavigateToHome()
                LoginEvent.NavigateToAccessDenied -> onNavigateToAccessDenied()
                is LoginEvent.ShowError -> {
                    snackbarHostState.currentSnackbarData?.dismiss()
                    snackbarHostState.showSnackbar(
                        message = context.getString(event.error.messageRes),
                        duration = SnackbarDuration.Short,
                    )
                }
            }
        }
    }

    LoginScreenContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onSignInTap = { viewModel.onSignInTap(context) },
        language = language,
        onLanguageSelected = { localeViewModel.setLanguage(it) },
    )
}

/**
 * Stateless layout matching MoMorph frame `8HGlvYGJWq` (Login). The 375×812dp design
 * canvas maps to a column of: header (logo + language pill on a top gradient) →
 * ROOT FURTHER tagline → localized description → Google CTA → footer copyright,
 * all stacked over a full-bleed keyvisual background.
 */
@Composable
fun LoginScreenContent(
    state: LoginUiState,
    snackbarHostState: SnackbarHostState,
    onSignInTap: () -> Unit,
    modifier: Modifier = Modifier,
    language: Language = Language.Default,
    onLanguageSelected: (Language) -> Unit = {},
) {
    Scaffold(
        modifier =
            modifier
                .fillMaxSize()
                .testTag(TEST_TAG_SCREEN),
        containerColor = Color.Transparent,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            // Background keyvisual — full-bleed under the entire screen.
            Image(
                painter = painterResource(R.drawable.bg_keyvisual),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
            // Header gradient overlay — darkens the top to ensure logo + language
            // pill remain legible regardless of the keyvisual underneath.
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .background(
                            Brush.verticalGradient(
                                colors =
                                    listOf(
                                        Color(0xFF00101A).copy(alpha = 0.9f),
                                        Color.Transparent,
                                    ),
                            ),
                        ),
            )

            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .systemBarsPadding(),
                horizontalAlignment = Alignment.Start,
            ) {
                // ── Header: SAA logo (left) + language pill (right) ──────────────
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(start = 20.dp, end = 12.dp, top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Image(
                        painter = painterResource(R.drawable.ic_logo_saa),
                        contentDescription = null,
                        modifier =
                            Modifier
                                .testTag(TEST_TAG_LOGO)
                                .width(48.dp)
                                .height(44.dp),
                    )
                    Spacer(Modifier.weight(1f))
                    LanguageSelector(
                        selected = language,
                        onSelect = onLanguageSelected,
                    )
                }

                // ── Tagline image (ROOT FURTHER) ─────────────────────────────────
                Spacer(Modifier.height(104.dp))
                Image(
                    painter = painterResource(R.drawable.ic_logo_root_further),
                    contentDescription = stringResource(R.string.brand_root_further),
                    modifier =
                        Modifier
                            .padding(start = 20.dp)
                            .width(247.dp)
                            .height(109.dp),
                )

                // ── Localized invitation description ─────────────────────────────
                Spacer(Modifier.height(32.dp))
                Text(
                    text = stringResource(R.string.login_description),
                    color = Color.White,
                    style =
                        MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 14.sp,
                            lineHeight = 20.sp,
                            letterSpacing = 0.25.sp,
                            fontWeight = FontWeight.Light,
                        ),
                    modifier =
                        Modifier
                            .padding(horizontal = 20.dp)
                            .width(335.dp),
                )

                // ── Sign-in CTA, anchored low on the screen ─────────────────────
                Spacer(Modifier.weight(1f))
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    GoogleSignInButton(
                        isLoading = state.isLoading,
                        enabled = state.playServicesAvailable,
                        onClick = onSignInTap,
                    )
                }

                // ── Footer copyright ─────────────────────────────────────────────
                Spacer(Modifier.weight(0.4f))
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(R.string.login_copyright),
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        style =
                            MaterialTheme.typography.bodySmall.copy(
                                fontSize = 12.sp,
                                lineHeight = 16.sp,
                            ),
                    )
                }
            }
        }
    }
}

const val TEST_TAG_SCREEN: String = "login_screen"
const val TEST_TAG_LOGO: String = "login_logo_saa"
