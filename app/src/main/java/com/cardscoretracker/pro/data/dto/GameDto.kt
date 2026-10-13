package com.cardscoretracker.pro.data.dto

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

/**
 * Firestore DTO for a game document in the "games" collection.
 * Contains game configuration, metadata, embedded rounds, and server timestamps.
 */
data class GameDto(
    @DocumentId
    val id: String = "",
    val gameMode: String = "",
    val playerNames: List<String> = emptyList(),
    val loserName: String = "",
    val totalRounds: Int = 0,
    val rounds: List<RoundDto> = emptyList(),
    val finalScores: Map<String, Int> = emptyMap(),
    @ServerTimestamp
    val createdAt: Date? = null,
    @ServerTimestamp
    val updatedAt: Date? = null
)
