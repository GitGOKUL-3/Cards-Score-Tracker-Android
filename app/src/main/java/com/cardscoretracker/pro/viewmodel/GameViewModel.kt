package com.cardscoretracker.pro.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cardscoretracker.pro.data.AppDatabase
import com.cardscoretracker.pro.data.GameEntity
import com.cardscoretracker.pro.data.GameRepository
import com.cardscoretracker.pro.data.GameWithRoundsAndScores
import com.cardscoretracker.pro.data.PlayerNameRepository
import com.cardscoretracker.pro.data.PlayerScoreEntity
import com.cardscoretracker.pro.data.RoundEntity
import com.cardscoretracker.pro.data.ThemePreferences
import com.cardscoretracker.pro.model.GameMode
import com.cardscoretracker.pro.model.Player
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class GameState(
    val gameMode: GameMode = GameMode.MODE_240,
    val players: List<Player> = emptyList(),
    val currentRound: Int = 1,
    val totalRounds: Int = 0,              // 0 = unlimited (240 mode)
    val roundInputs: Map<Int, String> = emptyMap(),       // playerId -> input string
    val chanceUsedThisRound: Set<Int> = emptySet(),        // playerIds who used chance this round
    val winnerSelectedThisRound: Int? = null,              // Feature 2: playerId of round winner (0 pts)
    val isGameOver: Boolean = false,
    val loserName: String = "",
    val loserMessage: String = "",
    val roundHistory: List<RoundRecord> = emptyList(),
    val showLoserDialog: Boolean = false,
    val showWinnerRequiredError: Boolean = false            // Bug fix: must select winner each round
)

data class RoundRecord(
    val roundNumber: Int,
    val scores: Map<String, Int>,          // playerName -> score for this round
    val isDoubled: Boolean = false
)

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: GameRepository = GameRepository(
        AppDatabase.getDatabase(application).gameDao()
    )

    private val _gameState = MutableStateFlow(GameState())
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    // Previously used player names for autocomplete suggestions
    private val _savedPlayerNames = MutableStateFlow<List<String>>(emptyList())
    val savedPlayerNames: StateFlow<List<String>> = _savedPlayerNames.asStateFlow()

    // Theme preference
    private val _isDarkTheme = MutableStateFlow(ThemePreferences.isDarkTheme(application))
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    val allGames = repository.allGames

    init {
        _savedPlayerNames.value = PlayerNameRepository.getSavedNames(application)
    }

    fun toggleTheme() {
        val newValue = !_isDarkTheme.value
        _isDarkTheme.value = newValue
        ThemePreferences.setDarkTheme(getApplication(), newValue)
    }

    // ─── Setup ───────────────────────────────────────────────────────────────

    fun startGame(mode: GameMode, playerNames: List<String>) {
        val players = playerNames.mapIndexed { index, name ->
            Player(id = index, name = name)
        }
        val totalRounds = when (mode) {
            GameMode.MODE_7S  -> 7
            GameMode.MODE_5S  -> 5
            GameMode.MODE_240 -> 0
        }
        _gameState.value = GameState(
            gameMode = mode,
            players = players,
            currentRound = 1,
            totalRounds = totalRounds,
            roundInputs = players.associate { it.id to "" }
        )
    }

    // ─── Input Handling ───────────────────────────────────────────────────────

    fun updateRoundInput(playerId: Int, value: String) {
        val current = _gameState.value
        // Don't allow overwriting a locked winner entry via text field
        if (current.winnerSelectedThisRound == playerId) return
        _gameState.value = current.copy(
            roundInputs = current.roundInputs + (playerId to value)
        )
    }

    // ─── Feature 2: Round Winner ──────────────────────────────────────────────

    /**
     * Marks [playerId] as the winner for this round (score = 0).
     * Toggling the same player deselects them. Selecting a different player
     * automatically deselects the previous winner and restores their input.
     */
    fun markAsRoundWinner(playerId: Int) {
        val current = _gameState.value
        val isAlreadyWinner = current.winnerSelectedThisRound == playerId

        if (isAlreadyWinner) {
            // Deselect — clear the 0 and restore blank input
            _gameState.value = current.copy(
                winnerSelectedThisRound = null,
                roundInputs = current.roundInputs + (playerId to "")
            )
        } else {
            // Select new winner; restore previous winner's input to blank, clear any error
            val previousWinnerId = current.winnerSelectedThisRound
            var updatedInputs = current.roundInputs + (playerId to "0")
            if (previousWinnerId != null) {
                updatedInputs = updatedInputs + (previousWinnerId to "")
            }
            _gameState.value = current.copy(
                winnerSelectedThisRound = playerId,
                roundInputs = updatedInputs,
                showWinnerRequiredError = false  // clear error as soon as winner is chosen
            )
        }
    }

    // ─── Feature 1: Mistake Correction / Reset Round ──────────────────────────

    /**
     * Clears all score inputs for the current round and undoes any chance usage.
     * The dealer index is NOT changed — only input data is reset.
     */
    fun resetCurrentRoundData() {
        val current = _gameState.value

        // Undo chances that were used this round
        val revertedPlayers = current.players.map { player ->
            if (current.chanceUsedThisRound.contains(player.id)) {
                player.copy(chances = (player.chances - 1).coerceAtLeast(0))
            } else {
                player
            }
        }

        _gameState.value = current.copy(
            players = revertedPlayers,
            roundInputs = current.players.associate { it.id to "" },
            chanceUsedThisRound = emptySet(),
            winnerSelectedThisRound = null,
            showWinnerRequiredError = false       // clear winner error on round reset
        )
    }

    // ─── Chance (240 Mode only) ───────────────────────────────────────────────

    fun useChance(playerId: Int) {
        val current = _gameState.value
        if (current.gameMode != GameMode.MODE_240) return
        val player = current.players.find { it.id == playerId } ?: return
        if (player.chances >= 3) return
        // Don't allow if chance already used this round for this player
        if (current.chanceUsedThisRound.contains(playerId)) return

        // Mark chance used this round and auto-set score input to "20"
        _gameState.value = current.copy(
            chanceUsedThisRound = current.chanceUsedThisRound + playerId,
            roundInputs = current.roundInputs + (playerId to "20")
        )
    }

    // ─── Submit Round ─────────────────────────────────────────────────────────

    fun submitRound() {
        val current = _gameState.value
        val inputs = current.roundInputs

        // Validate all inputs are filled
        val allFilled = current.players.all { p ->
            inputs[p.id]?.isNotBlank() == true && inputs[p.id]?.toIntOrNull() != null
        }
        if (!allFilled) return

        // All modes: require exactly one round winner before submitting
        if (current.winnerSelectedThisRound == null) {
            _gameState.value = current.copy(showWinnerRequiredError = true)
            return
        }

        val roundNumber = current.currentRound
        val isDoubled = isRoundDoubled(current.gameMode, roundNumber, current.totalRounds)

        // Feature 2: winner gets 0 regardless of input; others use their input value
        val roundScores = mutableMapOf<String, Int>()
        val updatedPlayers = current.players.map { player ->
            val rawScore = inputs[player.id]!!.toInt()
            val actualScore = if (isDoubled) rawScore * 2 else rawScore
            roundScores[player.name] = actualScore
            val usedChance = current.chanceUsedThisRound.contains(player.id)
            player.copy(
                totalScore = player.totalScore + actualScore,
                chances = if (usedChance) player.chances + 1 else player.chances
            )
        }

        val roundRecord = RoundRecord(
            roundNumber = roundNumber,
            scores = roundScores,
            isDoubled = isDoubled
        )

        val newHistory = current.roundHistory + roundRecord

        when (current.gameMode) {
            GameMode.MODE_240 -> {
                // Check if any player reached 240
                val over240 = updatedPlayers.filter { it.totalScore >= 240 }
                if (over240.isNotEmpty()) {
                    val loser = over240.maxByOrNull { it.totalScore }!!
                    val finalPlayers = updatedPlayers.map { p ->
                        if (p.id == loser.id) p.copy(isLoser = true) else p
                    }
                    val newState = current.copy(
                        players = finalPlayers,
                        currentRound = roundNumber + 1,
                        roundInputs = updatedPlayers.associate { it.id to "" },
                        chanceUsedThisRound = emptySet(),
                        winnerSelectedThisRound = null,
                        roundHistory = newHistory,
                        isGameOver = true,
                        loserName = loser.name,
                        loserMessage = "${loser.name} has lost!",
                        showLoserDialog = true
                    )
                    _gameState.value = newState
                    saveGameToDb(newState)
                } else {
                    _gameState.value = current.copy(
                        players = updatedPlayers,
                        currentRound = roundNumber + 1,
                        roundInputs = updatedPlayers.associate { it.id to "" },
                        chanceUsedThisRound = emptySet(),
                        winnerSelectedThisRound = null,
                        roundHistory = newHistory
                    )
                }
            }
            GameMode.MODE_7S, GameMode.MODE_5S -> {
                val isLastRound = roundNumber == current.totalRounds
                if (isLastRound) {
                    val loser = updatedPlayers.maxByOrNull { it.totalScore }!!
                    val finalPlayers = updatedPlayers.map { p ->
                        if (p.id == loser.id) p.copy(isLoser = true) else p
                    }
                    val newState = current.copy(
                        players = finalPlayers,
                        currentRound = roundNumber + 1,
                        roundInputs = updatedPlayers.associate { it.id to "" },
                        chanceUsedThisRound = emptySet(),
                        winnerSelectedThisRound = null,
                        roundHistory = newHistory,
                        isGameOver = true,
                        loserName = loser.name,
                        loserMessage = "${loser.name} has lost!",
                        showLoserDialog = true
                    )
                    _gameState.value = newState
                    saveGameToDb(newState)
                } else {
                    _gameState.value = current.copy(
                        players = updatedPlayers,
                        currentRound = roundNumber + 1,
                        roundInputs = updatedPlayers.associate { it.id to "" },
                        chanceUsedThisRound = emptySet(),
                        winnerSelectedThisRound = null,
                        roundHistory = newHistory
                    )
                }
            }
        }
    }

    private fun isRoundDoubled(mode: GameMode, round: Int, totalRounds: Int): Boolean {
        return when (mode) {
            GameMode.MODE_240 -> false
            GameMode.MODE_7S, GameMode.MODE_5S -> round == 1 || round == totalRounds
        }
    }

    fun dismissLoserDialog() {
        _gameState.value = _gameState.value.copy(showLoserDialog = false)
    }

    /** Called by UI after the winner-required snackbar is shown. */
    fun clearWinnerError() {
        _gameState.value = _gameState.value.copy(showWinnerRequiredError = false)
    }

    fun resetGame() {
        _gameState.value = GameState()
    }

    // ─── Persistence ──────────────────────────────────────────────────────────

    private fun saveGameToDb(state: GameState) {
        viewModelScope.launch {
            // Persist player names for future autocomplete suggestions
            val playerNamesList = state.players.map { it.name }
            PlayerNameRepository.saveNames(getApplication(), playerNamesList)
            _savedPlayerNames.value = PlayerNameRepository.getSavedNames(getApplication())

            val playerNamesJson = state.players.joinToString(",", "[", "]") { "\"${it.name}\"" }
            val gameEntity = GameEntity(
                gameMode = state.gameMode.name,
                dateTime = System.currentTimeMillis(),
                playerNames = playerNamesJson,
                loserName = state.loserName,
                totalRounds = state.roundHistory.size
            )
            val gameId = repository.insertGame(gameEntity)

            state.roundHistory.forEach { roundRecord ->
                val roundEntity = RoundEntity(
                    gameId = gameId,
                    roundNumber = roundRecord.roundNumber,
                    isDoubled = roundRecord.isDoubled
                )
                val roundId = repository.insertRound(roundEntity)

                state.players.forEach { player ->
                    val score = roundRecord.scores[player.name] ?: 0
                    // Calculate running total up to this round
                    val runningTotal = state.roundHistory
                        .filter { it.roundNumber <= roundRecord.roundNumber }
                        .sumOf { it.scores[player.name] ?: 0 }
                    repository.insertPlayerScore(
                        PlayerScoreEntity(
                            roundId = roundId,
                            playerName = player.name,
                            score = score,
                            runningTotal = runningTotal
                        )
                    )
                }
            }
        }
    }

    // ─── History ──────────────────────────────────────────────────────────────

    suspend fun getGameWithDetails(gameId: Long): GameWithRoundsAndScores? {
        return repository.getGameWithDetails(gameId)
    }
}
