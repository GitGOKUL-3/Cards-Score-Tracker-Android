package com.cardscoretracker.pro

import android.app.Application
import com.cardscoretracker.pro.di.FirebaseModule
import com.google.firebase.FirebaseApp

/**
 * Custom Application class ensuring safe initialization of Firebase and offline settings.
 */
class CardScoreTrackerApp : Application() {

    override fun onCreate() {
        super.onCreate()
        // Ensure FirebaseApp is safely initialized
        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                FirebaseApp.initializeApp(this)
            }
            // Pre-initialize Firestore with persistent cache settings
            FirebaseModule.provideFirestore()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
