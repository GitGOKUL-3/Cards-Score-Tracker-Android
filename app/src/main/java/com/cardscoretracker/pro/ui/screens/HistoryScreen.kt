package com.cardscoretracker.pro.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cardscoretracker.pro.data.GameEntity
import com.cardscoretracker.pro.model.GameMode
import com.cardscoretracker.pro.ui.theme.*
import com.cardscoretracker.pro.viewmodel.GameViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: GameViewModel,
    isDarkTheme: Boolean,
    onGameClick: (Long) -> Unit,
    onBack: () -> Unit
) {
    val games by viewModel.allGames.collectAsStateWithLifecycle(initialValue = emptyList())

    val textColor = if (isDarkTheme) White else IndigoText
    val gradientBrush = if (isDarkTheme) {
        Brush.verticalGradient(colors = listOf(DeepBlue, Color(0xFF0F2460), Color(0xFF0A1A40)))
    } else {
        Brush.verticalGradient(colors = listOf(LightBgStart, LightBgMid, LightBgEnd))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Game History",
                        color = textColor,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = textColor)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                modifier = Modifier.background(brush = gradientBrush)
            )
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(brush = gradientBrush)
                .padding(paddingValues)
        ) {
            if (games.isEmpty()) {
                EmptyHistoryView(isDarkTheme = isDarkTheme)
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    items(games) { game ->
                        GameHistoryCard(
                            game = game,
                            isDarkTheme = isDarkTheme,
                            onClick = { onGameClick(game.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyHistoryView(isDarkTheme: Boolean) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("🃏", fontSize = 64.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No games yet!",
            color = if (isDarkTheme) White else IndigoText,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Start a game to see your history here",
            color = LightGray,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun GameHistoryCard(
    game: GameEntity,
    isDarkTheme: Boolean,
    onClick: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
    val dateStr = dateFormat.format(Date(game.dateTime))

    val modeEmoji = when (game.gameMode) {
        GameMode.MODE_240.name -> "🟥"
        GameMode.MODE_7S.name -> "🟦"
        GameMode.MODE_5S.name -> "🟩"
        else -> "🃏"
    }
    val modeName = when (game.gameMode) {
        GameMode.MODE_240.name -> "240 Mode"
        GameMode.MODE_7S.name -> "7s Mode"
        GameMode.MODE_5S.name -> "5s Mode"
        else -> game.gameMode
    }

    // Parse player names from JSON
    val playerNames = game.playerNames
        .removePrefix("[").removeSuffix("]")
        .split(",")
        .map { it.trim().removeSurrounding("\"") }
        .filter { it.isNotEmpty() }

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = if (isDarkTheme) DarkCard.copy(alpha = 0.9f) else LightCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = modeEmoji, fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = modeName,
                            color = if (isDarkTheme) White else IndigoText,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = dateStr,
                            color = if (isDarkTheme) LightGray else IndigoSoft,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = LightGray,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = if (isDarkTheme) White.copy(alpha = 0.08f) else LightCardBorder)
            Spacer(modifier = Modifier.height(12.dp))

            // Players
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                playerNames.take(4).forEach { name ->
                    val isLoser = name == game.loserName
                    val chipBg = when {
                        isLoser     -> LoserRed.copy(alpha = 0.25f)
                        isDarkTheme -> MediumBlue.copy(alpha = 0.2f)
                        else        -> VibrantOrange.copy(alpha = 0.1f)
                    }
                    val chipBorder = if (isLoser) BorderStroke(1.dp, LoserRed.copy(alpha = 0.6f))
                                     else if (!isDarkTheme) BorderStroke(1.dp, LightCardBorder)
                                     else null
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = chipBg,
                        border = chipBorder
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (isLoser) Text("🔥", fontSize = 12.sp)
                            Text(
                                text = name,
                                color = if (isLoser) LoserRedLight else if (isDarkTheme) LightBlue else IndigoText,
                                fontSize = 12.sp,
                                fontWeight = if (isLoser) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
                if (playerNames.size > 4) {
                    Text("+${playerNames.size - 4}", color = if (isDarkTheme) LightGray else IndigoSoft, fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = LoserRedLight, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Loser: ${game.loserName} 🔥",
                    color = LoserRedLight,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
