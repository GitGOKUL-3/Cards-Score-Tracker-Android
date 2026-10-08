package com.cardscoretracker.pro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.cardscoretracker.pro.ui.auth.AuthScreen
import com.cardscoretracker.pro.ui.auth.AuthViewModel
import com.cardscoretracker.pro.ui.screens.GameScreen
import com.cardscoretracker.pro.ui.screens.HistoryDetailScreen
import com.cardscoretracker.pro.ui.screens.HistoryScreen
import com.cardscoretracker.pro.ui.screens.PlayerSetupScreen
import com.cardscoretracker.pro.ui.theme.CardScoreTrackerProTheme
import com.cardscoretracker.pro.viewmodel.GameViewModel
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val gameViewModel: GameViewModel = viewModel()
            val authViewModel: AuthViewModel = viewModel()
            val isDarkTheme by gameViewModel.isDarkTheme.collectAsStateWithLifecycle()

            CardScoreTrackerProTheme(darkTheme = isDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CardScoreTrackerApp(
                        gameViewModel = gameViewModel,
                        authViewModel = authViewModel,
                        isDarkTheme = isDarkTheme
                    )
                }
            }
        }
    }
}

@Composable
fun CardScoreTrackerApp(
    gameViewModel: GameViewModel,
    authViewModel: AuthViewModel,
    isDarkTheme: Boolean
) {
    val navController = rememberNavController()
    val isUserLoggedIn = FirebaseAuth.getInstance().currentUser != null
    val startDestination = if (isUserLoggedIn) "setup" else "login"

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable("login") {
            AuthScreen(
                authViewModel = authViewModel,
                isDarkTheme = isDarkTheme,
                onLoginSuccess = {
                    navController.navigate("setup") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }
        composable("setup") {
            PlayerSetupScreen(
                viewModel = gameViewModel,
                onGameStarted = { navController.navigate("game") },
                onHistoryClicked = { navController.navigate("history") },
                onLogoutClicked = {
                    authViewModel.logout()
                    navController.navigate("login") {
                        popUpTo("setup") { inclusive = true }
                    }
                }
            )
        }
        composable("game") {
            GameScreen(
                viewModel = gameViewModel,
                isDarkTheme = isDarkTheme,
                onGameOver = { navController.navigate("setup") {
                    popUpTo("setup") { inclusive = true }
                }},
                onBack = { navController.popBackStack() }
            )
        }
        composable("history") {
            HistoryScreen(
                viewModel = gameViewModel,
                isDarkTheme = isDarkTheme,
                onGameClick = { gameId -> navController.navigate("history_detail/$gameId") },
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            route = "history_detail/{gameId}",
            arguments = listOf(navArgument("gameId") { type = NavType.StringType })
        ) { backStackEntry ->
            val gameId = backStackEntry.arguments?.getString("gameId") ?: ""
            HistoryDetailScreen(
                gameId = gameId,
                viewModel = gameViewModel,
                isDarkTheme = isDarkTheme,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
