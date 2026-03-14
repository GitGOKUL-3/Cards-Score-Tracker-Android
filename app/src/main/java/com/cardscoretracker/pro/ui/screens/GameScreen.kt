package com.cardscoretracker.pro.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cardscoretracker.pro.model.GameMode
import com.cardscoretracker.pro.model.Player
import com.cardscoretracker.pro.ui.theme.*

import com.cardscoretracker.pro.viewmodel.GameViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    viewModel: GameViewModel,
    isDarkTheme: Boolean,
    onGameOver: () -> Unit,
    onBack: () -> Unit
) {
    val state by viewModel.gameState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val scope = rememberCoroutineScope()

    // Loser dialog
    if (state.showLoserDialog) {
        LoserDialog(
            message = state.loserMessage,

            onDismiss = {
                viewModel.dismissLoserDialog()
                onGameOver()
            }
        )
    }

    val textColor    = if (isDarkTheme) White else IndigoText
    val subTextColor  = if (isDarkTheme) LightBlue else IndigoSoft
    val cardColor     = if (isDarkTheme) DarkCard.copy(alpha = 0.85f) else LightCard
    val gradientBrush = if (isDarkTheme) {
        Brush.verticalGradient(colors = listOf(DeepBlue, Color(0xFF0F2460), Color(0xFF0A1A40)))
    } else {
        Brush.verticalGradient(colors = listOf(LightBgStart, LightBgMid, LightBgEnd))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = state.gameMode.displayName,
                            style = MaterialTheme.typography.titleLarge,
                            color = textColor,
                            fontWeight = FontWeight.Bold
                        )
                        val subtitle = when (state.gameMode) {
                            GameMode.MODE_240 -> "Round ${state.currentRound}"
                            else -> "Round ${state.currentRound} / ${state.totalRounds}"
                        }
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = subTextColor
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = textColor)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                ),
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Round indicator for 7s/5s
                if (state.gameMode != GameMode.MODE_240) {
                    RoundProgressIndicator(
                        currentRound = state.currentRound,
                        totalRounds = state.totalRounds,
                        isDarkTheme = isDarkTheme
                    )
                }

                // Score cards for each player
                state.players.forEach { player ->
                    PlayerScoreCard(
                        player = player,
                        gameMode = state.gameMode,
                        currentRound = state.currentRound,
                        totalRounds = state.totalRounds,
                        inputValue = state.roundInputs[player.id] ?: "",
                        chanceUsedThisRound = state.chanceUsedThisRound.contains(player.id),
                        isDarkTheme = isDarkTheme,
                        onInputChange = { viewModel.updateRoundInput(player.id, it) },
                        onUseChance = { viewModel.useChance(player.id) }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Submit Round Button
                Button(
                    onClick = {
                        keyboardController?.hide()
                        viewModel.submitRound()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = VibrantOrange),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Submit Round ${state.currentRound}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Round History
                if (state.roundHistory.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    RoundHistorySection(
                        roundHistory = state.roundHistory,
                        players = state.players,
                        isDarkTheme = isDarkTheme
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun RoundProgressIndicator(currentRound: Int, totalRounds: Int, isDarkTheme: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = if (isDarkTheme) White.copy(alpha = 0.1f) else LightCard)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Progress", color = if (isDarkTheme) LightGray else IndigoSoft, style = MaterialTheme.typography.bodyMedium)
                Text(
                    "${minOf(currentRound, totalRounds)} / $totalRounds",
                    color = if (isDarkTheme) LightBlue else IndigoText,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { minOf(currentRound - 1, totalRounds).toFloat() / totalRounds },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = VibrantOrange,
                trackColor = White.copy(alpha = 0.2f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                for (i in 1..totalRounds) {
                    val isFirst = i == 1
                    val isLast = i == totalRounds
                    val isDouble = isFirst || isLast
                    val isDone = i < currentRound
                    val isCurrent = i == currentRound
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(28.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                when {
                                    isDone -> SuccessGreen.copy(alpha = 0.7f)
                                    isCurrent -> VibrantOrange
                                    else -> White.copy(alpha = 0.15f)
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isDouble) "2x" else "$i",
                            color = White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlayerScoreCard(
    player: Player,
    gameMode: GameMode,
    currentRound: Int,
    totalRounds: Int,
    inputValue: String,
    chanceUsedThisRound: Boolean,
    isDarkTheme: Boolean,
    onInputChange: (String) -> Unit,
    onUseChance: () -> Unit
) {
    val isDoubledRound = (gameMode == GameMode.MODE_7S || gameMode == GameMode.MODE_5S) &&
            (currentRound == 1 || currentRound == totalRounds)

    val progressFraction = if (gameMode == GameMode.MODE_240) {
        (player.totalScore / 240f).coerceIn(0f, 1f)
    } else 0f

    val progressColor = when {
        progressFraction > 0.85f -> ErrorRed
        progressFraction > 0.6f -> WarningAmber
        else -> SuccessGreen
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDarkTheme) DarkCard.copy(alpha = 0.85f) else LightCard
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Player header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                if (isDarkTheme) MediumBlue.copy(alpha = 0.4f) else VibrantOrange.copy(alpha = 0.2f),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = player.name.first().uppercaseChar().toString(),
                            color = if (isDarkTheme) White else IndigoText,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = player.name,
                            color = if (isDarkTheme) White else IndigoText,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                        if (gameMode == GameMode.MODE_240) {
                            Text(
                                text = "Chances: ${player.chances}/3",
                                color = if (player.chances >= 3) ErrorRed else if (isDarkTheme) LightBlue else IndigoSoft,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${player.totalScore}",
                        color = if (gameMode == GameMode.MODE_240 && player.totalScore >= 200) ErrorRed else VibrantOrange,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 28.sp
                    )
                    if (gameMode == GameMode.MODE_240) {
                        Text(
                            text = "/ 240",
                            color = if (isDarkTheme) LightGray else IndigoSoft,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            // Progress bar for 240 mode
            if (gameMode == GameMode.MODE_240) {
                Spacer(modifier = Modifier.height(10.dp))
                LinearProgressIndicator(
                    progress = { progressFraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = progressColor,
                    trackColor = if (isDarkTheme) White.copy(alpha = 0.15f) else LightCardBorder
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Score input row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (chanceUsedThisRound) {
                    // Show locked "Chance Used" display instead of text field
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        color = WarningAmber.copy(alpha = 0.15f),
                        border = BorderStroke(1.5.dp, WarningAmber.copy(alpha = 0.6f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text("⚡", fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Chance Used — 20 pts",
                                color = WarningAmber,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                } else {
                    OutlinedTextField(
                        value = inputValue,
                        onValueChange = { v ->
                            if (v.all { it.isDigit() } && (v.toIntOrNull() ?: 0) <= 130) onInputChange(v)
                        },
                        modifier = Modifier.weight(1f),
                        label = {
                            Text(
                                if (isDoubledRound) "Score (×2 round)" else "Score this round",
                                color = if (isDarkTheme) LightGray else IndigoSoft,
                                fontSize = 12.sp
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor   = VibrantOrange,
                            unfocusedBorderColor = if (isDarkTheme) White.copy(alpha = 0.3f) else LightInputBorder,
                            focusedTextColor     = if (isDarkTheme) White else IndigoText,
                            unfocusedTextColor   = if (isDarkTheme) White else IndigoText,
                            cursorColor          = VibrantOrange
                        ),
                        suffix = if (isDoubledRound && inputValue.isNotEmpty()) {
                            {
                                Text(
                                    "= ${(inputValue.toIntOrNull() ?: 0) * 2}",
                                    color = VibrantOrange,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        } else null
                    )
                }

                // Chance button (240 mode only)
                if (gameMode == GameMode.MODE_240) {
                    val chancesLeft = 3 - player.chances
                    // Button is disabled if: no chances left OR already used chance this round
                    val canUseChance = chancesLeft > 0 && !chanceUsedThisRound
                    Button(
                        onClick = onUseChance,
                        enabled = canUseChance,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = when {
                                chanceUsedThisRound -> WarningAmber.copy(alpha = 0.4f)
                                chancesLeft > 0 -> WarningAmber
                                else -> MediumGray
                            },
                            disabledContainerColor = MediumGray.copy(alpha = 0.4f)
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(if (chanceUsedThisRound) "✅" else "⚡", fontSize = 16.sp)
                            Text(
                                if (chanceUsedThisRound) "Used" else "+20",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = White
                            )
                            Text(
                                "$chancesLeft left",
                                fontSize = 9.sp,
                                color = White.copy(0.8f)
                            )
                        }
                    }
                }
            }

            // Double round badge
            if (isDoubledRound) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = VibrantOrange.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = "⚡ Double Points Round!",
                            color = VibrantOrange,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RoundHistorySection(
    roundHistory: List<com.cardscoretracker.pro.viewmodel.RoundRecord>,
    players: List<Player>,
    isDarkTheme: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = if (isDarkTheme) DarkCard.copy(alpha = 0.85f) else LightCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "📊 Score History",
                color = if (isDarkTheme) White else IndigoText,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Header row
            Row(modifier = Modifier.fillMaxWidth()) {
                Text("Round", color = if (isDarkTheme) LightGray else IndigoSoft, fontSize = 12.sp, modifier = Modifier.width(56.dp))
                players.forEach { player ->
                    Text(
                        text = player.name,
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
                modifier = Modifier.padding(vertical = 6.dp)
            )

            roundHistory.reversed().forEach { record ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Row(modifier = Modifier.width(56.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "R${record.roundNumber}",
                            color = if (record.isDoubled) VibrantOrange else LightGray,
                            fontSize = 12.sp,
                            fontWeight = if (record.isDoubled) FontWeight.Bold else FontWeight.Normal
                        )
                        if (record.isDoubled) {
                            Text("×2", color = VibrantOrange, fontSize = 9.sp)
                        }
                    }
                    players.forEach { player ->
                        Text(
                            text = "${record.scores[player.name] ?: 0}",
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
                modifier = Modifier.padding(vertical = 6.dp)
            )

            // Totals row
            Row(modifier = Modifier.fillMaxWidth()) {
                Text("Total", color = VibrantOrange, fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(56.dp))
                players.forEach { player ->
                    Text(
                        text = "${player.totalScore}",
                        color = VibrantOrange,
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

@Composable
private fun LoserDialog(
    message: String,
    onDismiss: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "loser_anim")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = DarkCard),
            elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🔥",
                    fontSize = 64.sp,
                    modifier = Modifier.scale(scale)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "GAME OVER!",
                    color = ErrorRed,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 24.sp,
                    letterSpacing = 2.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = message,
                    color = White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "(Loser)",
                    color = LoserRedLight,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))



                Spacer(modifier = Modifier.height(10.dp))

                // New Game button
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = VibrantOrange)
                ) {
                    Text("New Game", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}
