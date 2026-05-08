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
import com.example.aiddproject.auth.login.ui.LoginScreen
import com.example.aiddproject.core.auth.AuthRedirectController
import com.example.aiddproject.core.session.SessionGate
import com.example.aiddproject.home.ui.HomeScreen
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
 */
@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Routes.GATE,
) {
    val authRedirectController = rememberAuthRedirectController()
    LaunchedEffect(authRedirectController) {
        // Phase 6 (T073) replaces this no-op with the real navigate-to-Login /
        // navigate-to-AccessDenied behavior. Wired now so the controller's coroutine
        // scope starts collecting at NavHost composition.
        authRedirectController.events.collect { /* no-op */ }
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
                onNavigateToWriteKudo = { navController.navigate(Routes.WRITE_KUDO) },
                onNavigateToAwardDetail = { award ->
                    navController.navigate(Routes.awardDetail(award.id))
                },
                onNavigateToSearch = { navController.navigate(Routes.SEARCH) },
                onNavigateToProfile = { navController.navigate(Routes.PROFILE) },
            )
        }
        composable(Routes.ACCESS_DENIED) { AccessDeniedPlaceholder() }

        // Home-feature outbound placeholders (UI implement-ui pass) — replaced
        // by real screens in subsequent feature plans.
        composable(Routes.AWARDS_OVERVIEW) { PlaceholderScreen(label = "Awards overview") }
        composable(Routes.KUDOS_OVERVIEW) { PlaceholderScreen(label = "Kudos overview") }
        composable(Routes.KUDOS_FEED) { PlaceholderScreen(label = "Kudos feed") }
        composable(Routes.KUDOS_DETAIL) { PlaceholderScreen(label = "Kudos detail") }
        composable(Routes.WRITE_KUDO) { PlaceholderScreen(label = "Write a Kudo") }
        composable(Routes.SEARCH) { PlaceholderScreen(label = "Search") }
        composable(Routes.PROFILE) { PlaceholderScreen(label = "Profile") }
        composable(
            route = Routes.AWARD_DETAIL_PATTERN,
            arguments = listOf(navArgument("awardId") { type = NavType.StringType }),
        ) { backStackEntry ->
            val awardId = backStackEntry.arguments?.getString("awardId").orEmpty()
            PlaceholderScreen(label = "Award detail: $awardId")
        }
    }
}

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
 * AuthRedirectController is a `@Singleton` (not a ViewModel), so we resolve it through
 * a Hilt entry-point off the application context — the standard pattern for reaching
 * SingletonComponent bindings from a composable that isn't a `@HiltViewModel`.
 */
@Composable
private fun rememberAuthRedirectController(): AuthRedirectController {
    val context = LocalContext.current.applicationContext
    return EntryPointAccessors
        .fromApplication(context, AuthRedirectControllerEntryPoint::class.java)
        .authRedirectController()
}

@EntryPoint
@InstallIn(SingletonComponent::class)
internal interface AuthRedirectControllerEntryPoint {
    fun authRedirectController(): AuthRedirectController
}
