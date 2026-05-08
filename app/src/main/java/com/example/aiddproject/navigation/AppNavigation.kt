package com.example.aiddproject.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.aiddproject.auth.login.ui.LoginScreen
import com.example.aiddproject.core.session.SessionGate

/**
 * NavHost. Start destination is [Routes.GATE], which observes the persisted Supabase
 * session and forwards to Home (auto-login per US2 of Login) or Login. The Home
 * destination is a placeholder for now — real Home implementation lands with the Home
 * feature.
 */
@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Routes.GATE,
) {
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
        composable(Routes.HOME) { PlaceholderScreen(label = "Home") }
        composable(Routes.ACCESS_DENIED) { PlaceholderScreen(label = "Access denied") }
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
private fun PlaceholderScreen(label: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = label)
    }
}
