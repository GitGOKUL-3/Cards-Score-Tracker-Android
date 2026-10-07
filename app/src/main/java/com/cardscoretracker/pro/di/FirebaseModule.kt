package com.cardscoretracker.pro.di

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.PersistentCacheSettings

/**
 * Dependency Injection / Provider module for Firebase services.
 * Configures Firestore with offline persistent cache settings.
 */
object FirebaseModule {

    private const val CACHE_SIZE_100_MB = 104857600L // 100MB cache limit

    @Volatile
    private var firestoreInstance: FirebaseFirestore? = null

    /**
     * Returns a singleton [FirebaseFirestore] instance with persistent local cache
     * set to 100MB.
     */
    fun provideFirestore(): FirebaseFirestore {
        return firestoreInstance ?: synchronized(this) {
            firestoreInstance ?: FirebaseFirestore.getInstance().apply {
                try {
                    val settings = FirebaseFirestoreSettings.Builder()
                        .setLocalCacheSettings(
                            PersistentCacheSettings.newBuilder()
                                .setSizeBytes(CACHE_SIZE_100_MB)
                                .build()
                        )
                        .build()
                    firestoreSettings = settings
                } catch (_: Exception) {
                    // Ignored if settings were already initialized on this instance
                }
            }.also { firestoreInstance = it }
        }
    }
}
