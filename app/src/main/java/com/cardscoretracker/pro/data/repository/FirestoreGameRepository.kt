package com.cardscoretracker.pro.data.repository

import com.cardscoretracker.pro.data.dto.GameDto
import com.cardscoretracker.pro.data.dto.RoundDto
import com.cardscoretracker.pro.data.mapper.toDomain
import com.cardscoretracker.pro.data.mapper.toDto
import com.cardscoretracker.pro.di.FirebaseModule
import com.cardscoretracker.pro.model.Game
import com.cardscoretracker.pro.model.Round
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.MetadataChanges
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.Source
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.tasks.await

/**
 * Concrete implementation of [GameRepository] backed by Firebase Firestore.
 * All game data is isolated under `users/{currentUserId}/games` and rounds under
 * `users/{currentUserId}/games/{gameId}/rounds`.
 */
class FirestoreGameRepository(
    private val firestore: FirebaseFirestore = FirebaseModule.provideFirestore(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : GameRepository {

    companion object {
        const val COLLECTION_USERS = "users"
        const val SUBCOLLECTION_GAMES = "games"
        const val SUBCOLLECTION_ROUNDS = "rounds"
    }

    /**
     * Resolves the user-isolated collection reference for games:
     * Path: `users/{userId}/games`
     */
    private fun getGamesCollection(): CollectionReference {
        val uid = auth.currentUser?.uid ?: "anonymous_user"
        return firestore.collection(COLLECTION_USERS)
            .document(uid)
            .collection(SUBCOLLECTION_GAMES)
    }

    // ─── Reactive Streams ─────────────────────────────────────────────────────

    override fun getAllGames(): Flow<List<Game>> = callbackFlow {
        val query = getGamesCollection()
            .orderBy("createdAt", Query.Direction.DESCENDING)

        val registration = query.addSnapshotListener(MetadataChanges.INCLUDE) { snapshot, error ->
            if (error != null) {
                if (error.code == FirebaseFirestoreException.Code.UNAVAILABLE) {
                    return@addSnapshotListener
                }
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val games = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(GameDto::class.java)?.toDomain()
                }
                trySend(games)
            }
        }

        awaitClose {
            registration.remove()
        }
    }

    override fun getGame(gameId: String): Flow<Game?> = callbackFlow {
        val docRef = getGamesCollection().document(gameId)

        val registration = docRef.addSnapshotListener(MetadataChanges.INCLUDE) { snapshot, error ->
            if (error != null) {
                if (error.code == FirebaseFirestoreException.Code.UNAVAILABLE) {
                    return@addSnapshotListener
                }
                close(error)
                return@addSnapshotListener
            }
            val game = snapshot?.takeIf { it.exists() }
                ?.toObject(GameDto::class.java)
                ?.toDomain()
            trySend(game)
        }

        awaitClose {
            registration.remove()
        }
    }

    override fun getRounds(gameId: String): Flow<List<Round>> = callbackFlow {
        val collectionRef = getGamesCollection()
            .document(gameId)
            .collection(SUBCOLLECTION_ROUNDS)
            .orderBy("roundNumber", Query.Direction.ASCENDING)

        val registration = collectionRef.addSnapshotListener(MetadataChanges.INCLUDE) { snapshot, error ->
            if (error != null) {
                if (error.code == FirebaseFirestoreException.Code.UNAVAILABLE) {
                    return@addSnapshotListener
                }
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                val rounds = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(RoundDto::class.java)?.toDomain()
                }
                trySend(rounds)
            }
        }

        awaitClose {
            registration.remove()
        }
    }

    override fun getGameWithRounds(gameId: String): Flow<Game?> {
        return combine(getGame(gameId), getRounds(gameId)) { game, rounds ->
            game?.copy(rounds = rounds)
        }
    }

    // ─── One-shot Reads ───────────────────────────────────────────────────────

    override suspend fun getGameById(gameId: String): Game? {
        val docRef = getGamesCollection().document(gameId)
        val snapshot = try {
            docRef.get().await()
        } catch (_: FirebaseFirestoreException) {
            try {
                docRef.get(Source.CACHE).await()
            } catch (_: Exception) {
                null
            }
        } catch (_: Exception) {
            null
        }

        return snapshot?.takeIf { it.exists() }
            ?.toObject(GameDto::class.java)
            ?.toDomain()
    }

    override suspend fun getGameWithDetails(gameId: String): Game? {
        val game = getGameById(gameId) ?: return null

        val roundsQuery = getGamesCollection()
            .document(gameId)
            .collection(SUBCOLLECTION_ROUNDS)
            .orderBy("roundNumber", Query.Direction.ASCENDING)

        val roundsSnapshot = try {
            roundsQuery.get().await()
        } catch (_: FirebaseFirestoreException) {
            try {
                roundsQuery.get(Source.CACHE).await()
            } catch (_: Exception) {
                null
            }
        } catch (_: Exception) {
            null
        }

        val rounds = roundsSnapshot?.documents?.mapNotNull { doc ->
            doc.toObject(RoundDto::class.java)?.toDomain()
        } ?: emptyList()

        return game.copy(rounds = rounds)
    }

    // ─── Mutations ────────────────────────────────────────────────────────────

    override suspend fun createGame(game: Game): String {
        val gameDocRef = if (game.id.isNotBlank()) {
            getGamesCollection().document(game.id)
        } else {
            getGamesCollection().document()
        }

        val gameDto = game.toDto().copy(id = gameDocRef.id)
        gameDocRef.set(gameDto).await()

        // If game already has initial rounds, write them into the user's sub-collection
        if (game.rounds.isNotEmpty()) {
            val batch = firestore.batch()
            game.rounds.forEach { round ->
                val roundDocRef = gameDocRef.collection(SUBCOLLECTION_ROUNDS).document()
                val roundDto = round.toDto().copy(id = roundDocRef.id, gameId = gameDocRef.id)
                batch.set(roundDocRef, roundDto)
            }
            batch.commit().await()
        }

        return gameDocRef.id
    }

    override suspend fun addRound(gameId: String, round: Round): String {
        val gameDocRef = getGamesCollection().document(gameId)
        val roundDocRef = gameDocRef.collection(SUBCOLLECTION_ROUNDS).document()

        val roundDto = round.toDto().copy(
            id = roundDocRef.id,
            gameId = gameId
        )

        // 1. Save the round in the sub-collection (offline-cached immediately)
        roundDocRef.set(roundDto).await()

        // 2. Update parent game document metadata
        val updates = mutableMapOf<String, Any>(
            "totalRounds" to round.roundNumber,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        if (round.roundScores.isNotEmpty()) {
            updates["finalScores"] = round.roundScores
        }
        gameDocRef.update(updates).await()

        return roundDocRef.id
    }

    override suspend fun updateGameStatus(
        gameId: String,
        loserName: String,
        totalRounds: Int?,
        finalScores: Map<String, Int>?
    ) {
        val updates = mutableMapOf<String, Any>(
            "loserName" to loserName,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        if (totalRounds != null) updates["totalRounds"] = totalRounds
        if (finalScores != null) updates["finalScores"] = finalScores

        getGamesCollection()
            .document(gameId)
            .update(updates)
            .await()
    }

    override suspend fun deleteGame(gameId: String) {
        val gameDocRef = getGamesCollection().document(gameId)
        val roundsSnapshot = try {
            gameDocRef.collection(SUBCOLLECTION_ROUNDS).get().await()
        } catch (_: FirebaseFirestoreException) {
            try {
                gameDocRef.collection(SUBCOLLECTION_ROUNDS).get(Source.CACHE).await()
            } catch (_: Exception) {
                null
            }
        } catch (_: Exception) {
            null
        }

        val batch = firestore.batch()
        roundsSnapshot?.documents?.forEach { roundDoc ->
            batch.delete(roundDoc.reference)
        }
        batch.delete(gameDocRef)
        batch.commit().await()
    }
}
