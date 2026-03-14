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
import com.cardscoretracker.pro.ui.screens.GameScreen
import com.cardscoretracker.pro.ui.screens.HistoryDetailScreen
import com.cardscoretracker.pro.ui.screens.HistoryScreen
import com.cardscoretracker.pro.ui.screens.PlayerSetupScreen
import com.cardscoretracker.pro.ui.theme.CardScoreTrackerProTheme
import com.cardscoretracker.pro.viewmodel.GameViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: GameViewModel = viewModel()
            val isDarkTheme by viewModel.isDarkTheme.collectAsStateWithLifecycle()

            CardScoreTrackerProTheme(darkTheme = isDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CardScoreTrackerApp(viewModel = viewModel, isDarkTheme = isDarkTheme)
                }
            }
        }
    }
}

@Composable
fun CardScoreTrackerApp(viewModel: GameViewModel, isDarkTheme: Boolean) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "setup"
    ) {
        composable("setup") {
            PlayerSetupScreen(
                viewModel = viewModel,
                onGameStarted = { navController.navigate("game") },
                onHistoryClicked = { navController.navigate("history") }
            )
        }
        composable("game") {
            GameScreen(
                viewModel = viewModel,
                isDarkTheme = isDarkTheme,
                onGameOver = { navController.navigate("setup") {
                    popUpTo("setup") { inclusive = true }
                }},
                onBack = { navController.popBackStack() }
            )
        }
        composable("history") {
            HistoryScreen(
                viewModel = viewModel,
                isDarkTheme = isDarkTheme,
                onGameClick = { gameId -> navController.navigate("history_detail/$gameId") },
                onBack = { navController.popBackStack() }
            )
        }
        composable(
            route = "history_detail/{gameId}",
            arguments = listOf(navArgument("gameId") { type = NavType.LongType })
        ) { backStackEntry ->
            val gameId = backStackEntry.arguments?.getLong("gameId") ?: 0L
            HistoryDetailScreen(
                gameId = gameId,
                viewModel = viewModel,
                isDarkTheme = isDarkTheme,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
