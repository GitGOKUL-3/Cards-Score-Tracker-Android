package com.cardscoretracker.pro.model

import java.util.Date

/**
 * Domain model representing a complete game session.
 */
data class Game(
    val id: String = "",
    val gameMode: GameMode = GameMode.MODE_240,
    val playerNames: List<String> = emptyList(),
    val loserName: String = "",
    val totalRounds: Int = 0,
    val rounds: List<Round> = emptyList(),
    val createdAt: Date? = null,
    val updatedAt: Date? = null
)
