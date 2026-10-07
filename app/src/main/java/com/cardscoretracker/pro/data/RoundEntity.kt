package com.cardscoretracker.pro.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "rounds",
    foreignKeys = [
        ForeignKey(
            entity = GameEntity::class,
            parentColumns = ["id"],
            childColumns = ["gameId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("gameId")]
)
data class RoundEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val gameId: Long = 0L,
    val roundNumber: Int = 0,
    val isDoubled: Boolean = false
)
