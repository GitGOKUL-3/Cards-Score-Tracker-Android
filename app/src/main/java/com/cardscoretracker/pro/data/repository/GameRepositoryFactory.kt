package com.cardscoretracker.pro.data.repository

import com.google.firebase.firestore.FirebaseFirestore

/**
 * Manual Dependency Injection Factory / Service Locator for providing [GameRepository].
 * Allows seamless repository instantiation without requiring external DI frameworks (e.g., Hilt).
 */
object GameRepositoryFactory {

    @Volatile
    private var repositoryInstance: GameRepository? = null

    /**
     * Returns the singleton instance of [GameRepository].
     */
    fun getRepository(
        firestore: FirebaseFirestore = com.cardscoretracker.pro.di.FirebaseModule.provideFirestore()
    ): GameRepository {
        return repositoryInstance ?: synchronized(this) {
            repositoryInstance ?: FirestoreGameRepository(firestore).also {
                repositoryInstance = it
            }
        }
    }

    /**
     * Allows replacing the repository with a mock or test fake during unit/instrumentation tests.
     */
    fun setRepositoryForTesting(repository: GameRepository?) {
        repositoryInstance = repository
    }
}
