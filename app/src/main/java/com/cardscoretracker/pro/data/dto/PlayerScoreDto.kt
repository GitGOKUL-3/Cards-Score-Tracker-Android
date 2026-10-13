package com.cardscoretracker.pro.data.dto

import com.google.firebase.firestore.DocumentId

/**
 * Firestore DTO for individual player score records within a round.
 * Can be embedded within [RoundDto] or used in a subcollection.
 */
data class PlayerScoreDto(
    @DocumentId
    val id: String = "",
    val playerName: String = "",
    val score: Int = 0,
    val chanceUsed: Boolean = false,
    val runningTotal: Int = 0
)
