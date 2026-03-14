package com.cardscoretracker.pro.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

data class GameWithRoundsAndScores(
    val game: GameEntity,
    val rounds: List<RoundWithScores>
)

data class RoundWithScores(
    val round: RoundEntity,
    val scores: List<PlayerScoreEntity>
)

@Dao
interface GameDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGame(game: GameEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRound(round: RoundEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlayerScore(score: PlayerScoreEntity)

    @Query("SELECT * FROM games ORDER BY dateTime DESC")
    fun getAllGames(): Flow<List<GameEntity>>

    @Query("SELECT * FROM games WHERE id = :gameId")
    suspend fun getGameById(gameId: Long): GameEntity?

    @Query("SELECT * FROM rounds WHERE gameId = :gameId ORDER BY roundNumber ASC")
    suspend fun getRoundsForGame(gameId: Long): List<RoundEntity>

    @Query("SELECT * FROM player_scores WHERE roundId = :roundId")
    suspend fun getScoresForRound(roundId: Long): List<PlayerScoreEntity>

    @Transaction
    suspend fun getGameWithDetails(gameId: Long): GameWithRoundsAndScores? {
        val game = getGameById(gameId) ?: return null
        val rounds = getRoundsForGame(gameId)
        val roundsWithScores = rounds.map { round ->
            RoundWithScores(
                round = round,
                scores = getScoresForRound(round.id)
            )
        }
        return GameWithRoundsAndScores(game, roundsWithScores)
    }

    @Query("DELETE FROM games WHERE id = :gameId")
    suspend fun deleteGame(gameId: Long)
}
