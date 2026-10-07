package com.cardscoretracker.pro.data.dto

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

/**
 * Firestore DTO for a game round.
 * Supports both Map<String, Int> for fast round score lookups
 * and List<PlayerScoreDto> for embedded detailed player points.
 */
data class RoundDto(
    @DocumentId
    val id: String = "",
    val gameId: String = "",
    val roundNumber: Int = 0,
    val isDoubled: Boolean = false,
    val roundScores: Map<String, Int> = emptyMap(),
    val playerPoints: List<PlayerScoreDto> = emptyList(),
    @ServerTimestamp
    val createdAt: Date? = null,
    @ServerTimestamp
    val updatedAt: Date? = null
)
