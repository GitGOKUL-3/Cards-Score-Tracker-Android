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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cardscoretracker.pro.model.GameMode
import com.cardscoretracker.pro.ui.theme.*
import com.cardscoretracker.pro.viewmodel.GameViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerSetupScreen(
    viewModel: GameViewModel,
    onGameStarted: () -> Unit,
    onHistoryClicked: () -> Unit
) {
    var selectedMode by remember { mutableStateOf(GameMode.MODE_240) }
    var playerNames by remember { mutableStateOf(listOf("", "")) }
    var errorMessage by remember { mutableStateOf("") }
    val savedNames by viewModel.savedPlayerNames.collectAsStateWithLifecycle()
    val isDarkTheme by viewModel.isDarkTheme.collectAsStateWithLifecycle()

    val textColor    = if (isDarkTheme) White else IndigoText
    val subTextColor  = if (isDarkTheme) LightGray else IndigoSoft
    val gradientBrush = if (isDarkTheme) {
        Brush.verticalGradient(colors = listOf(DeepBlue, RoyalBlue, Color(0xFF1E3A6E)))
    } else {
        Brush.verticalGradient(colors = listOf(LightBgStart, LightBgMid, LightBgEnd))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = gradientBrush)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 56.dp, bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "🃏 Card Score",
                        style = MaterialTheme.typography.displayMedium,
                        color = textColor,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "Tracker Pro",
                        style = MaterialTheme.typography.headlineLarge,
                        color = VibrantOrange,
                        fontWeight = FontWeight.Bold
                    )
                }
                // Theme toggle button
                val btnBg = if (isDarkTheme) White.copy(alpha = 0.15f) else LightIconBg
                IconButton(
                    onClick = { viewModel.toggleTheme() },
                    modifier = Modifier
                        .size(48.dp)
                        .background(color = btnBg, shape = CircleShape)
                ) {
                    Text(
                        text = if (isDarkTheme) "☀️" else "🌙",
                        fontSize = 20.sp
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = onHistoryClicked,
                    modifier = Modifier
                        .size(48.dp)
                        .background(color = btnBg, shape = CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = "History",
                        tint = textColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Game Mode Selection
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDarkTheme) White.copy(alpha = 0.1f) else LightCard
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = if (isDarkTheme) 0.dp else 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "Select Game Mode",
                        style = MaterialTheme.typography.titleMedium,
                        color = textColor,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    GameMode.entries.forEach { mode ->
                        GameModeChip(
                            mode = mode,
                            isSelected = selectedMode == mode,
                            isDarkTheme = isDarkTheme,
                            onClick = { selectedMode = mode }
                        )
                        if (mode != GameMode.entries.last()) Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Players Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDarkTheme) White.copy(alpha = 0.1f) else LightCard
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = if (isDarkTheme) 0.dp else 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Players",
                            style = MaterialTheme.typography.titleMedium,
                            color = textColor,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "${playerNames.size} players",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isDarkTheme) LightBlue else VibrantOrange
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    playerNames.forEachIndexed { index, name ->
                        PlayerNameField(
                            index = index,
                            value = name,
                            savedNames = savedNames,
                            otherNames = playerNames.filterIndexed { i, _ -> i != index },
                            isDarkTheme = isDarkTheme,
                            onValueChange = { newName ->
                                playerNames = playerNames.toMutableList().also { it[index] = newName }
                            },
                            onRemove = if (playerNames.size > 2) {
                                { playerNames = playerNames.toMutableList().also { it.removeAt(index) } }
                            } else null
                        )
                        if (index < playerNames.size - 1) Spacer(modifier = Modifier.height(10.dp))
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Add Player Button
                    OutlinedButton(
                        onClick = { playerNames = playerNames + "" },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.5.dp, if (isDarkTheme) VibrantOrange.copy(alpha = 0.7f) else IndigoText),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = if (isDarkTheme) VibrantOrange else IndigoText
                        )
                    ) {
                        Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add Player", fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            // Error
            AnimatedVisibility(visible = errorMessage.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = ErrorRed.copy(alpha = 0.2f))
                ) {
                    Text(
                        text = errorMessage,
                        color = Color(0xFFFF8080),
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Start Game Button
            Button(
                onClick = {
                    errorMessage = ""
                    val validNames = playerNames.map { it.trim() }
                    when {
                        validNames.any { it.isEmpty() } -> errorMessage = "Please enter all player names"
                        validNames.size != validNames.distinct().size -> errorMessage = "Player names must be unique"
                        validNames.size < 2 -> errorMessage = "Minimum 2 players required"
                        else -> {
                            viewModel.startGame(selectedMode, validNames)
                            onGameStarted()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = VibrantOrange
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(22.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Start Game",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun GameModeChip(
    mode: GameMode,
    isSelected: Boolean,
    isDarkTheme: Boolean,
    onClick: () -> Unit
) {
    val (emoji, description) = when (mode) {
        GameMode.MODE_240 -> "🟥" to "Reach 240 = Loser • 3 Chances"
        GameMode.MODE_7S  -> "🟦" to "7 Rounds • Double first & last"
        GameMode.MODE_5S  -> "🟩" to "5 Rounds • Double first & last"
    }

    val chipBg = when {
        isSelected && isDarkTheme  -> VibrantOrange.copy(alpha = 0.25f)
        isSelected && !isDarkTheme -> VibrantOrange.copy(alpha = 0.12f)
        !isSelected && isDarkTheme -> White.copy(alpha = 0.07f)
        else                       -> LightChipBg
    }
    val chipBorder = when {
        isSelected  -> BorderStroke(2.dp, VibrantOrange)
        isDarkTheme -> BorderStroke(1.dp, White.copy(alpha = 0.2f))
        else        -> BorderStroke(1.5.dp, LightCardBorder)
    }
    val titleColor = when {
        isSelected  -> VibrantOrange
        isDarkTheme -> White
        else        -> IndigoText
    }
    val descColor = when {
        isSelected  -> if (isDarkTheme) LightOrange else VibrantOrange.copy(alpha = 0.7f)
        isDarkTheme -> LightGray
        else        -> IndigoSoft
    }

    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = chipBg,
        border = chipBorder
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = emoji, fontSize = 20.sp)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = mode.displayName,
                    color = titleColor,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = description,
                    color = descColor,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = VibrantOrange,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun PlayerNameField(
    index: Int,
    value: String,
    savedNames: List<String>,
    otherNames: List<String>,
    isDarkTheme: Boolean,
    onValueChange: (String) -> Unit,
    onRemove: (() -> Unit)?
) {
    val suggestions = remember(value, savedNames, otherNames) {
        if (value.isBlank()) {
            savedNames.filter { saved ->
                otherNames.none { it.equals(saved, ignoreCase = true) }
            }.take(8)
        } else {
            savedNames.filter { saved ->
                saved.contains(value.trim(), ignoreCase = true) &&
                !saved.equals(value.trim(), ignoreCase = true) &&
                otherNames.none { it.equals(saved, ignoreCase = true) }
            }.take(6)
        }
    }

    val labelColor    = if (isDarkTheme) LightGray else IndigoSoft
    val textColor     = if (isDarkTheme) White else IndigoText
    val focusedBorder = VibrantOrange
    val idleBorder    = if (isDarkTheme) White.copy(alpha = 0.3f) else LightInputBorder
    val badgeColor    = if (isDarkTheme) MediumBlue.copy(alpha = 0.4f) else VibrantOrange.copy(alpha = 0.15f)
    val badgeText     = if (isDarkTheme) White else IndigoText
    val chipBg        = if (isDarkTheme) MediumBlue.copy(alpha = 0.35f) else LightChipBg
    val chipBorder    = if (isDarkTheme) LightBlue.copy(alpha = 0.4f) else LightCardBorder
    val chipIcon      = if (isDarkTheme) LightBlue else VibrantOrange
    val chipText      = if (isDarkTheme) White else IndigoText

    Column {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Player ${index + 1}", color = labelColor) },
            leadingIcon = {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(badgeColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${index + 1}",
                        color = badgeText,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            },
            trailingIcon = if (onRemove != null) {
                {
                    IconButton(onClick = onRemove) {
                        Icon(Icons.Default.Close, contentDescription = "Remove", tint = ErrorRed.copy(alpha = 0.8f))
                    }
                }
            } else null,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor   = focusedBorder,
                unfocusedBorderColor = idleBorder,
                focusedTextColor     = textColor,
                unfocusedTextColor   = textColor,
                cursorColor          = VibrantOrange
            )
        )

        // Suggestion chips
        AnimatedVisibility(
            visible = suggestions.isNotEmpty(),
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                suggestions.forEach { suggestion ->
                    SuggestionChip(
                        onClick = { onValueChange(suggestion) },
                        label = {
                            Text(
                                text = suggestion,
                                fontSize = 12.sp,
                                color = chipText,
                                fontWeight = FontWeight.Medium
                            )
                        },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = chipIcon
                            )
                        },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = chipBg,
                            iconContentColor = chipIcon
                        ),
                        border = SuggestionChipDefaults.suggestionChipBorder(
                            enabled = true,
                            borderColor = chipBorder
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }
        }
    }
}
