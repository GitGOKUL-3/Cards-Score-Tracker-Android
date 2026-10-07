package com.cardscoretracker.pro.data.repository

import com.cardscoretracker.pro.model.Game
import com.cardscoretracker.pro.model.Round
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface defining CRUD operations and real-time reactive streams
 * for games and their sub-collection rounds.
 */
interface GameRepository {

    // ─── Reactive Streams ─────────────────────────────────────────────────────

    /** Real-time flow of all games ordered by creation time descending. */
    fun getAllGames(): Flow<List<Game>>

    /** Real-time flow of a single game document by its ID. */
    fun getGame(gameId: String): Flow<Game?>

    /** Real-time flow of rounds for a given game from the rounds sub-collection. */
    fun getRounds(gameId: String): Flow<List<Round>>

    /** Real-time flow combining a game document with its sub-collection rounds. */
    fun getGameWithRounds(gameId: String): Flow<Game?>

    // ─── One-shot Reads ───────────────────────────────────────────────────────

    /** Fetches a single game document once. */
    suspend fun getGameById(gameId: String): Game?

    /** Fetches a game along with all its rounds from the sub-collection. */
    suspend fun getGameWithDetails(gameId: String): Game?

    // ─── Mutations ────────────────────────────────────────────────────────────

    /**
     * Creates a new game document in the "games" collection.
     * If [game.rounds] is not empty, also populates the "rounds" sub-collection.
     * @return The auto-generated or assigned game ID.
     */
    suspend fun createGame(game: Game): String

    /**
     * Adds a new round to the "rounds" sub-collection under `games/{gameId}/rounds`.
     * Also updates `totalRounds` and `updatedAt` on the parent game document.
     * @return The generated round ID.
     */
    suspend fun addRound(gameId: String, round: Round): String

    /**
     * Updates the status of an ongoing or completed game.
     */
    suspend fun updateGameStatus(
        gameId: String,
        loserName: String,
        totalRounds: Int? = null,
        finalScores: Map<String, Int>? = null
    )

    /**
     * Deletes a game document and all documents within its "rounds" sub-collection atomically.
     */
    suspend fun deleteGame(gameId: String)
}
