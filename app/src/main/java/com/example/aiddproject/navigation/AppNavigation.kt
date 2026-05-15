package com.example.aiddproject.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.aiddproject.auth.login.data.AuthRepository
import com.example.aiddproject.auth.login.ui.LoginScreen
import com.example.aiddproject.awarddetail.ui.AwardDetailScreen
import com.example.aiddproject.core.auth.rememberAuthRedirectController
import com.example.aiddproject.core.session.SessionGate
import com.example.aiddproject.home.ui.HomeScreen
import com.example.aiddproject.kudos.compose.ui.WriteKudoScreen
import com.example.aiddproject.kudos.notifications.ui.NotificationsScreen
import com.example.aiddproject.kudos.search.ui.SearchSunnerScreen
import com.example.aiddproject.kudos.standards.ui.CommunityStandardsScreen
import com.example.aiddproject.kudos.ui.KudosScreen
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

/**
 * NavHost. Start destination is [Routes.GATE], which observes the persisted
 * Supabase session and forwards to Home (auto-login per US2 of Login) or
 * Login. Home then fans out to every other primary surface; the destination
 * screens that aren't built yet (Awards overview, Kudos feed, etc.) render
 * labeled placeholders so the graph is fully navigable end-to-end.
 *
 * The [AuthRedirectController] is collected at the NavHost root so 401/403
 * responses on any authenticated screen bounce through [handleAuthRedirect]:
 *  - 401 → `signOut()` + replace stack with Login (Login then surfaces an
 *    `error_oauth_session_expired` snackbar via the controller's
 *    `sessionExpiredHint` flow).
 *  - 403 → replace stack with Access Denied.
 */
@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Routes.GATE,
) {
    val authRedirectController = rememberAuthRedirectController()
    val authRepository = rememberAuthRepository()
    LaunchedEffect(authRedirectController) {
        authRedirectController.events.collect { event ->
            handleAuthRedirect(
                event = event,
                signOut = { authRepository.signOut() },
                navigateToLogin = {
                    navController.navigate(Routes.LOGIN) {
                        clearGateBackstack()
                    }
                },
                navigateToAccessDenied = {
                    navController.navigate(Routes.ACCESS_DENIED) {
                        clearGateBackstack()
                    }
                },
            )
        }
    }
    NavHost(navController = navController, startDestination = startDestination) {
        composable(Routes.GATE) {
            SessionGate(
                onAuthenticated = {
                    navController.navigate(Routes.HOME) {
                        clearGateBackstack()
                    }
                },
                onUnauthenticated = {
                    navController.navigate(Routes.LOGIN) {
                        clearGateBackstack()
                    }
                },
            )
        }
        composable(Routes.LOGIN) {
            LoginScreen(
                onNavigateToHome = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onNavigateToAccessDenied = {
                    navController.navigate(Routes.ACCESS_DENIED) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
            )
        }
        composable(Routes.HOME) {
            HomeScreen(
                onNavigateToAwardsOverview = { navController.navigate(Routes.AWARDS_OVERVIEW) },
                onNavigateToKudosOverview = { navController.navigate(Routes.KUDOS_OVERVIEW) },
                onNavigateToKudosFeed = { navController.navigate(Routes.KUDOS_FEED) },
                onNavigateToKudosDetail = { navController.navigate(Routes.KUDOS_DETAIL) },
                onNavigateToWriteKudo = { navController.navigate(Routes.writeKudo()) },
                onNavigateToAwardDetail = { award ->
                    navController.navigate(Routes.awardDetail(award.id))
                },
                onNavigateToSearch = { navController.navigate(Routes.SEARCH) },
                onNavigateToProfile = { navController.navigate(Routes.PROFILE) },
                onNavigateToNotifications = { navController.navigate(Routes.NOTIFICATIONS) },
            )
        }
        composable(Routes.ACCESS_DENIED) { AccessDeniedPlaceholder() }

        // Home-feature outbound placeholders (UI implement-ui pass) — replaced
        // by real screens in subsequent feature plans.
        //
        // AWARDS_OVERVIEW now resolves to the real `AwardDetailScreen` — tapping
        // the Awards bottom-nav tab from Home lands on the Award Detail with no
        // `awardId`, and the VM's init coroutine falls back to first-by-
        // `sort_order` per FR-001 + Resolved Q1 (c-QM3_zjkG spec § Phase 8 T101).
        composable(Routes.AWARDS_OVERVIEW) {
            AwardDetailScreen(
                onNavigateToHome = {
                    navController.popBackStack(Routes.HOME, inclusive = false)
                },
                onNavigateToKudosOverview = { navController.navigate(Routes.KUDOS_OVERVIEW) },
                onNavigateToProfile = { navController.navigate(Routes.PROFILE) },
                onNavigateToSearch = { navController.navigate(Routes.SEARCH) },
                onNavigateToNotifications = { navController.navigate(Routes.NOTIFICATIONS) },
            )
        }
        composable(Routes.KUDOS_OVERVIEW) { entry ->
            KudosScreen(
                onNavigateToHome = {
                    navController.popBackStack(Routes.HOME, inclusive = false)
                },
                onNavigateToAwardsOverview = { navController.navigate(Routes.AWARDS_OVERVIEW) },
                onNavigateToProfile = { navController.navigate(Routes.PROFILE) },
                onNavigateToSearch = { navController.navigate(Routes.SEARCH) },
                onNavigateToSendKudos = { navController.navigate(Routes.writeKudo()) },
                onNavigateToKudosDetail = { navController.navigate(Routes.KUDOS_DETAIL) },
                onNavigateToAllKudos = { navController.navigate(Routes.KUDOS_FEED) },
                onNavigateToSecretBoxOpen = { navController.navigate(Routes.SECRET_BOX_OPEN) },
                onNavigateToNotifications = { navController.navigate(Routes.NOTIFICATIONS) },
                submitSignalSavedStateHandle = entry.savedStateHandle,
            )
        }
        composable(Routes.KUDOS_FEED) { PlaceholderScreen(label = "Kudos feed") }
        composable(Routes.KUDOS_DETAIL) { PlaceholderScreen(label = "Kudos detail") }
        composable(
            route = Routes.WRITE_KUDO_PATTERN,
            arguments =
                listOf(
                    navArgument(Routes.WRITE_KUDO_ARG_RECIPIENT) {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    },
                ),
        ) {
            WriteKudoScreen(
                onSubmitted = {
                    // Cross-screen submit signal — see KudosScreen for the
                    // observer side. Sets the flag on the PREVIOUS entry's
                    // savedStateHandle (the hub) then pops.
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set(KUDO_SUBMITTED_FLAG, true)
                    navController.popBackStack()
                },
                onNavigateBack = { navController.popBackStack() },
                onNavigateToCommunityStandards = {
                    navController.navigate(Routes.COMMUNITY_STANDARDS)
                },
                onSelectBottomTab = { tab ->
                    when (tab) {
                        com.example.aiddproject.home.ui.components.HomeNavTab.Saa2025 ->
                            navController.popBackStack(Routes.HOME, inclusive = false)
                        com.example.aiddproject.home.ui.components.HomeNavTab.Awards ->
                            navController.navigate(Routes.AWARDS_OVERVIEW)
                        com.example.aiddproject.home.ui.components.HomeNavTab.Kudos ->
                            navController.popBackStack()
                        com.example.aiddproject.home.ui.components.HomeNavTab.Profile ->
                            navController.navigate(Routes.PROFILE)
                    }
                },
            )
        }
        composable(Routes.COMMUNITY_STANDARDS) {
            CommunityStandardsScreen(onNavigateBack = { navController.popBackStack() })
        }
        composable(Routes.SECRET_BOX_OPEN) { PlaceholderScreen(label = "Open Secret Box") }
        composable(Routes.SEARCH) {
            SearchSunnerScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToProfile = { userId ->
                    // Profile screen is not yet parameterized; pass userId
                    // via savedStateHandle so the future Profile impl can
                    // read it. (Plan FR-010 + Profile-spec handshake.)
                    navController.currentBackStackEntry
                        ?.savedStateHandle
                        ?.set("userId", userId)
                    navController.navigate(Routes.PROFILE)
                },
                onSelectBottomTab = { tab ->
                    when (tab) {
                        com.example.aiddproject.home.ui.components.HomeNavTab.Saa2025 ->
                            navController.popBackStack(Routes.HOME, inclusive = false)
                        com.example.aiddproject.home.ui.components.HomeNavTab.Awards ->
                            navController.navigate(Routes.AWARDS_OVERVIEW)
                        com.example.aiddproject.home.ui.components.HomeNavTab.Kudos ->
                            navController.navigate(Routes.KUDOS_OVERVIEW)
                        com.example.aiddproject.home.ui.components.HomeNavTab.Profile ->
                            navController.navigate(Routes.PROFILE)
                    }
                },
            )
        }
        composable(Routes.PROFILE) { PlaceholderScreen(label = "Profile") }
        composable(
            route = Routes.AWARD_DETAIL_PATTERN,
            arguments = listOf(navArgument("awardId") { type = NavType.StringType }),
        ) {
            // `awardId` flows in via the back-stack-entry arguments and is
            // surfaced to the VM through Hilt's SavedStateHandle injection —
            // no manual extraction needed here.
            AwardDetailScreen(
                onNavigateToHome = {
                    navController.popBackStack(Routes.HOME, inclusive = false)
                },
                onNavigateToKudosOverview = { navController.navigate(Routes.KUDOS_OVERVIEW) },
                onNavigateToProfile = { navController.navigate(Routes.PROFILE) },
                onNavigateToSearch = { navController.navigate(Routes.SEARCH) },
                onNavigateToNotifications = { navController.navigate(Routes.NOTIFICATIONS) },
            )
        }
        composable(Routes.NOTIFICATIONS) {
            NotificationsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToKudoDetail = { _, _ -> navController.navigate(Routes.KUDOS_DETAIL) },
                onNavigateToSecretBoxOpen = { navController.navigate(Routes.SECRET_BOX_OPEN) },
                onNavigateToProfile = { navController.navigate(Routes.PROFILE) },
                onNavigateToCommunityStandards = { navController.navigate(Routes.COMMUNITY_STANDARDS) },
                onNavigateToAdminReview = { navController.navigate(Routes.ADMIN_REVIEW) },
            )
        }
        composable(Routes.ADMIN_REVIEW) { PlaceholderScreen(label = "Admin Review Content") }
    }
}

/**
 * Cross-screen submit signal — the Viết Kudo composer sets this flag
 * on the previous back-stack entry's savedStateHandle on success, and
 * KudosScreen observes it to trigger a hub refresh. First cross-screen
 * savedStateHandle use in the codebase — see plan § Notes.
 */
const val KUDO_SUBMITTED_FLAG: String = "kudoSubmitted"

/**
 * Clears the splash gate from the back-stack so a system back press from Home/Login
 * doesn't return to a stale resolving-session screen.
 */
private fun NavOptionsBuilder.clearGateBackstack() {
    popUpTo(Routes.GATE) { inclusive = true }
    launchSingleTop = true
}

@Composable
private fun AccessDeniedPlaceholder() {
    PlaceholderScreen(label = "Access denied")
}

@Composable
private fun PlaceholderScreen(label: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = label)
    }
}

/**
 * Hilt entry-point for the singleton [AuthRepository], used here for the
 * `signOut()` side effect on `SessionExpired` (T073). LoginViewModel already
 * owns its own injected copy for the user-initiated sign-in path; we don't
 * route through it because the redirect is decoupled from any in-flight
 * Login/Home VM lifecycle.
 */
@Composable
internal fun rememberAuthRepository(): AuthRepository {
    val context = LocalContext.current.applicationContext
    return EntryPointAccessors
        .fromApplication(context, AuthRepositoryEntryPoint::class.java)
        .authRepository()
}

@EntryPoint
@InstallIn(SingletonComponent::class)
internal interface AuthRepositoryEntryPoint {
    fun authRepository(): AuthRepository
}
