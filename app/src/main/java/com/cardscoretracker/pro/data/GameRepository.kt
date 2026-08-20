package com.cardscoretracker.pro.data

import kotlinx.coroutines.flow.Flow

/**
 * Repository that mediates between [GameViewModel] and [GameDao].
 *
 * This is a pure pass-through layer: every function delegates directly to the DAO
 * with no added logic. It exists so future steps can add Supabase sync, caching,
 * or other data-source logic here without touching the ViewModel.
 *
 * No Supabase operations are performed here.
 */
class GameRepository(private val dao: GameDao) {

    /** Live stream of all games, ordered newest-first. Backed by Room. */
    val allGames: Flow<List<GameEntity>> = dao.getAllGames()

    /** Inserts a completed game record and returns the generated row id. */
    suspend fun insertGame(game: GameEntity): Long = dao.insertGame(game)

    /** Inserts a round record and returns the generated row id. */
    suspend fun insertRound(round: RoundEntity): Long = dao.insertRound(round)

    /** Inserts a single player score record for a round. */
    suspend fun insertPlayerScore(score: PlayerScoreEntity) = dao.insertPlayerScore(score)

    /**
     * Returns the full game detail (game + rounds + per-round player scores)
     * for the given [gameId], or null if not found.
     */
    suspend fun getGameWithDetails(gameId: Long): GameWithRoundsAndScores? =
        dao.getGameWithDetails(gameId)
}
