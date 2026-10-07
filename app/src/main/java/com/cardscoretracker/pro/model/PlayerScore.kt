package com.cardscoretracker.pro.model

/**
 * Domain model representing a player's score and stats for a single round.
 */
data class PlayerScore(
    val id: String = "",
    val playerName: String = "",
    val score: Int = 0,
    val chanceUsed: Boolean = false,
    val runningTotal: Int = 0
)
