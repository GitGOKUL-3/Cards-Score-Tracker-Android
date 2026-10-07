package com.cardscoretracker.pro.model

import java.util.Date

/**
 * Domain model representing a single round of a game.
 */
data class Round(
    val id: String = "",
    val gameId: String = "",
    val roundNumber: Int = 1,
    val isDoubled: Boolean = false,
    val roundScores: Map<String, Int> = emptyMap(),
    val playerScores: List<PlayerScore> = emptyList(),
    val createdAt: Date? = null,
    val updatedAt: Date? = null
)
