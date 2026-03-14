package com.cardscoretracker.pro.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "games")
data class GameEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val gameMode: String,
    val dateTime: Long,
    val playerNames: String, // JSON array of names
    val loserName: String,
    val totalRounds: Int
)
