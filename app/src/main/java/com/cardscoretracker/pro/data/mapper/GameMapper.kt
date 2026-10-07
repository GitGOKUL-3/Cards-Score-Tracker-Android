package com.cardscoretracker.pro.data.mapper

import com.cardscoretracker.pro.data.dto.GameDto
import com.cardscoretracker.pro.data.dto.PlayerScoreDto
import com.cardscoretracker.pro.data.dto.RoundDto
import com.cardscoretracker.pro.model.Game
import com.cardscoretracker.pro.model.GameMode
import com.cardscoretracker.pro.model.PlayerScore
import com.cardscoretracker.pro.model.Round

// ─────────────────────────────────────────────────────────────────────────────
// PlayerScore Mappers
// ─────────────────────────────────────────────────────────────────────────────

fun PlayerScoreDto.toDomain(): PlayerScore = PlayerScore(
    id = id,
    playerName = playerName,
    score = score,
    chanceUsed = chanceUsed,
    runningTotal = runningTotal
)

fun PlayerScore.toDto(): PlayerScoreDto = PlayerScoreDto(
    id = id,
    playerName = playerName,
    score = score,
    chanceUsed = chanceUsed,
    runningTotal = runningTotal
)

// ─────────────────────────────────────────────────────────────────────────────
// Round Mappers
// ─────────────────────────────────────────────────────────────────────────────

fun RoundDto.toDomain(): Round {
    val scores = if (roundScores.isNotEmpty()) {
        roundScores
    } else {
        playerPoints.associate { it.playerName to it.score }
    }
    return Round(
        id = id,
        gameId = gameId,
        roundNumber = roundNumber,
        isDoubled = isDoubled,
        roundScores = scores,
        playerScores = playerPoints.map { it.toDomain() },
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun Round.toDto(): RoundDto {
    val scores = if (roundScores.isNotEmpty()) {
        roundScores
    } else {
        playerScores.associate { it.playerName to it.score }
    }
    return RoundDto(
        id = id,
        gameId = gameId,
        roundNumber = roundNumber,
        isDoubled = isDoubled,
        roundScores = scores,
        playerPoints = playerScores.map { it.toDto() },
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Game Mappers
// ─────────────────────────────────────────────────────────────────────────────

fun GameDto.toDomain(): Game {
    val resolvedMode = try {
        GameMode.valueOf(gameMode)
    } catch (_: IllegalArgumentException) {
        GameMode.entries.find { it.displayName.equals(gameMode, ignoreCase = true) }
            ?: GameMode.MODE_240
    }

    return Game(
        id = id,
        gameMode = resolvedMode,
        playerNames = playerNames,
        loserName = loserName,
        totalRounds = totalRounds,
        rounds = rounds.map { it.toDomain() },
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun Game.toDto(): GameDto {
    val finalScores = rounds.lastOrNull()?.playerScores?.associate { it.playerName to it.runningTotal }
        ?: emptyMap()

    return GameDto(
        id = id,
        gameMode = gameMode.name,
        playerNames = playerNames,
        loserName = loserName,
        totalRounds = totalRounds,
        rounds = rounds.map { it.toDto() },
        finalScores = finalScores,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
