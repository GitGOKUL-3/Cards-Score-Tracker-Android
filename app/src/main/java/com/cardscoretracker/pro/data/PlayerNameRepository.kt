package com.cardscoretracker.pro.data

import android.content.Context
import android.content.SharedPreferences

/**
 * Persists previously used player names using SharedPreferences.
 * Names are stored as a comma-separated string, deduplicated, max 30 entries.
 */
object PlayerNameRepository {

    private const val PREFS_NAME = "card_score_player_names"
    private const val KEY_NAMES = "known_player_names"
    private const val MAX_NAMES = 30

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /** Returns all previously used player names, most-recently-used first. */
    fun getSavedNames(context: Context): List<String> {
        val raw = prefs(context).getString(KEY_NAMES, "") ?: ""
        return if (raw.isBlank()) emptyList()
        else raw.split(",").map { it.trim() }.filter { it.isNotEmpty() }
    }

    /** Saves a list of player names used in a game (prepends them so they appear first). */
    fun saveNames(context: Context, names: List<String>) {
        val existing = getSavedNames(context).toMutableList()
        // Prepend new names (case-insensitive dedup), keep order MRU first
        names.reversed().forEach { newName ->
            val trimmed = newName.trim()
            if (trimmed.isNotEmpty()) {
                existing.removeAll { it.equals(trimmed, ignoreCase = true) }
                existing.add(0, trimmed)
            }
        }
        val trimmed = existing.take(MAX_NAMES)
        prefs(context).edit().putString(KEY_NAMES, trimmed.joinToString(",")).apply()
    }
}
