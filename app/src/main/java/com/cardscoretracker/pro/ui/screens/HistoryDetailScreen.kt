package com.cardscoretracker.pro.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.os.Environment
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.tooling.preview.Preview
import com.cardscoretracker.pro.data.ExcelExporter
import com.cardscoretracker.pro.data.GameWithRoundsAndScores
import com.cardscoretracker.pro.model.GameMode
import com.cardscoretracker.pro.ui.theme.*


import com.cardscoretracker.pro.viewmodel.GameViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryDetailScreen(
    gameId: Long,
    viewModel: GameViewModel,
    isDarkTheme: Boolean,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var gameDetails by remember { mutableStateOf<GameWithRoundsAndScores?>(null) }
    var isLoading by remember { mutableStateOf(true) }


    LaunchedEffect(gameId) {
        gameDetails = viewModel.getGameWithDetails(gameId)
        isLoading = false
    }

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
                        text = "Game Details",
                        color = textColor,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = textColor)
                    }
                },
                actions = {
                    if (gameDetails != null) {
                        // Download to device
                        IconButton(onClick = {
                            ExcelExporter.downloadAsCsv(context, gameDetails!!)
                        }) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = "Download as CSV",
                                tint = textColor
                            )
                        }
                        // Share with other apps
                        IconButton(onClick = {
                            ExcelExporter.shareAsCsv(context, gameDetails!!)
                        }) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share Score Sheet",
                                tint = textColor
                            )
                        }
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
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = VibrantOrange
                    )
                }
                gameDetails == null -> {
                    Text(
                        text = "Game not found",
                        color = textColor,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                else -> {
                    GameDetailContent(gameDetails = gameDetails!!, context = context, isDarkTheme = isDarkTheme)
                }
            }
        }
    }
}

@Composable
private fun GameDetailContent(
    gameDetails: GameWithRoundsAndScores,
    context: Context,
    isDarkTheme: Boolean
) {
    val game = gameDetails.game
    val rounds = gameDetails.rounds
    val scope = rememberCoroutineScope()

    val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
    val dateStr = dateFormat.format(Date(game.dateTime))

    val modeName = when (game.gameMode) {
        GameMode.MODE_240.name -> "240 Mode"
        GameMode.MODE_7S.name -> "7s Mode"
        GameMode.MODE_5S.name -> "5s Mode"
        else -> game.gameMode
    }
    val modeEmoji = when (game.gameMode) {
        GameMode.MODE_240.name -> "🟥"
        GameMode.MODE_7S.name -> "🟦"
        GameMode.MODE_5S.name -> "🟩"
        else -> "🃏"
    }

    // Collect unique player names from scores
    val playerNames = rounds.flatMap { it.scores }.map { it.playerName }.distinct()

    // Calculate final totals
    val finalTotals = playerNames.associateWith { name ->
        rounds.sumOf { round -> round.scores.find { it.playerName == name }?.score ?: 0 }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Game Info Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = if (isDarkTheme) DarkCard.copy(alpha = 0.9f) else LightCard),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = modeEmoji, fontSize = 32.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = modeName,
                            color = if (isDarkTheme) White else IndigoText,
                            fontWeight = FontWeight.ExtraBold,
                            style = MaterialTheme.typography.headlineMedium
                        )
                        Text(
                            text = dateStr,
                            color = if (isDarkTheme) LightGray else IndigoSoft,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = if (isDarkTheme) White.copy(alpha = 0.1f) else LightCardBorder)
                Spacer(modifier = Modifier.height(16.dp))

                // Loser highlight
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = LoserRed.copy(alpha = 0.2f)),
                    border = BorderStroke(1.5.dp, LoserRed.copy(alpha = 0.6f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text("🔥", fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "LOSER",
                                color = LoserRedLight,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 12.sp,
                                letterSpacing = 2.sp
                            )
                            Text(
                                text = "${game.loserName} You are lost ",
                                color = if (isDarkTheme) White else IndigoText,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleLarge,
                                textAlign = TextAlign.Center
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("🔥", fontSize = 24.sp)
                    }
                }
            }
        }

        // Final Scores Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = if (isDarkTheme) DarkCard.copy(alpha = 0.9f) else LightCard),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "🏆 Final Scores",
                    color = if (isDarkTheme) White else IndigoText,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(14.dp))

                val sortedPlayers = finalTotals.entries.sortedByDescending { it.value }
                sortedPlayers.forEachIndexed { index, (name, total) ->
                    val isLoser = name == game.loserName
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${index + 1}",
                            color = if (isDarkTheme) LightGray else IndigoSoft,
                            modifier = Modifier.width(24.dp),
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        if (isLoser) Text("🔥", fontSize = 16.sp)
                        else Spacer(modifier = Modifier.width(20.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = name,
                            color = if (isLoser) LoserRedLight else if (isDarkTheme) White else IndigoText,
                            fontWeight = if (isLoser) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "$total",
                            color = if (isLoser) LoserRedLight else VibrantOrange,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp
                        )
                    }
                    if (index < sortedPlayers.size - 1) {
                        HorizontalDivider(
                            color = if (isDarkTheme) White.copy(alpha = 0.06f) else LightCardBorder,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }
            }
        }

        // Round-by-Round Breakdown
        if (rounds.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = if (isDarkTheme) DarkCard.copy(alpha = 0.9f) else LightCard),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "📊 Round Breakdown",
                        color = if (isDarkTheme) White else IndigoText,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    // Header
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            "Round",
                            color = if (isDarkTheme) LightGray else IndigoSoft,
                            fontSize = 12.sp,
                            modifier = Modifier.width(60.dp)
                        )
                        playerNames.forEach { name ->
                            Text(
                                text = name,
                                color = if (isDarkTheme) LightBlue else VibrantOrange,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    HorizontalDivider(
                        color = if (isDarkTheme) White.copy(alpha = 0.1f) else LightCardBorder,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    rounds.forEach { roundWithScores ->
                        val round = roundWithScores.round
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 5.dp)
                        ) {
                            Row(
                                modifier = Modifier.width(60.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "R${round.roundNumber}",
                                    color = if (round.isDoubled) VibrantOrange else LightGray,
                                    fontSize = 13.sp,
                                    fontWeight = if (round.isDoubled) FontWeight.Bold else FontWeight.Normal
                                )
                                if (round.isDoubled) {
                                    Text("×2", color = VibrantOrange, fontSize = 9.sp)
                                }
                            }
                            playerNames.forEach { name ->
                                val score =
                                    roundWithScores.scores.find { it.playerName == name }?.score
                                        ?: 0
                                Text(
                                    text = "$score",
                                    color = if (isDarkTheme) White else IndigoText,
                                    fontSize = 13.sp,
                                    modifier = Modifier.weight(1f),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    HorizontalDivider(
                        color = if (isDarkTheme) White.copy(alpha = 0.1f) else LightCardBorder,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    // Totals
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            "Total",
                            color = VibrantOrange,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.width(60.dp)
                        )
                        playerNames.forEach { name ->
                            val isLoser = name == game.loserName
                            Text(
                                text = "${finalTotals[name] ?: 0}",
                                color = if (isLoser) LoserRedLight else VibrantOrange,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}