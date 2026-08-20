package com.cardscoretracker.pro.data

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

object ExcelExporter {

    // ──────────────────────────────────────────────────────────────────────────
    // PUBLIC API
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Saves the game as a CSV file directly to the Downloads folder on the device.
     * Uses MediaStore (Scoped Storage) — correct approach for API 29+.
     */
    fun downloadAsCsv(context: Context, gameDetails: GameWithRoundsAndScores) {
        try {
            val csv      = buildCsv(gameDetails)
            val fileName = buildFileName(gameDetails)

            val resolver = context.contentResolver
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "text/csv")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }

            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            if (uri != null) {
                resolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(csv.toByteArray(Charsets.UTF_8))
                }
                Toast.makeText(context, "✅ Saved to Downloads/$fileName", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(context, "Download failed: Could not create file", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Download failed: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Exports the game as a CSV file and launches a system share sheet
     * so the user can send it via WhatsApp, Gmail, Google Drive, etc.
     *
     * Writes to internal cache dir and exposes via FileProvider for max compatibility.
     * (MediaStore URIs are rejected by many apps like WhatsApp — FileProvider is correct.)
     */
    fun shareAsCsv(context: Context, gameDetails: GameWithRoundsAndScores) {
        try {
            val csv      = buildCsv(gameDetails)
            val fileName = buildFileName(gameDetails)

            // Write CSV to internal cache — no storage permissions needed
            val shareDir = File(context.cacheDir, "share").apply { mkdirs() }
            val csvFile  = File(shareDir, fileName)
            csvFile.writeText(csv, Charsets.UTF_8)

            // Expose via FileProvider so other apps can read it
            val uri: Uri = androidx.core.content.FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                csvFile
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Card Score — $fileName")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Share Score Sheet"))
        } catch (e: Exception) {
            Toast.makeText(context, "Share failed: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    // HELPERS
    // ──────────────────────────────────────────────────────────────────────────

    private fun buildFileName(gameDetails: GameWithRoundsAndScores): String {
        val dateStr = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
            .format(Date(gameDetails.game.dateTime))
        return "game_${dateStr}.csv"
    }

    private fun buildCsv(gameDetails: GameWithRoundsAndScores): String {
        val game   = gameDetails.game
        val rounds = gameDetails.rounds

        val playerNames: List<String> = rounds
            .flatMap { it.scores.map { s -> s.playerName } }
            .distinct()

        val modeName = when (game.gameMode) {
            "MODE_240" -> "240 Mode"
            "MODE_7S"  -> "7s Mode"
            "MODE_5S"  -> "5s Mode"
            else       -> game.gameMode
        }
        val dateStr = SimpleDateFormat("dd MMM yyyy  hh:mm a", Locale.getDefault())
            .format(Date(game.dateTime))

        val sb = StringBuilder()

        // Header info
        sb.appendLine("Game Report")
        sb.appendLine("Mode,$modeName")
        sb.appendLine("Date,$dateStr")
        sb.appendLine("Loser,${game.loserName}")
        sb.appendLine()

        // Column headers
        sb.append("Round")
        playerNames.forEach { sb.append(",").append(csvEscape(it)) }
        sb.appendLine()

        // Round rows
        rounds.forEach { round ->
            sb.append(round.round.roundNumber)
            if (round.round.isDoubled) sb.append(" (×2)")
            playerNames.forEach { name ->
                val score = round.scores.find { it.playerName == name }?.score ?: ""
                sb.append(",").append(score)
            }
            sb.appendLine()
        }

        // Totals
        sb.appendLine()
        sb.append("TOTAL")
        playerNames.forEach { name ->
            val total = rounds.sumOf { r ->
                r.scores.find { it.playerName == name }?.score ?: 0
            }
            sb.append(",").append(total)
        }
        sb.appendLine()

        return sb.toString()
    }

    private fun csvEscape(value: String): String =
        if (value.contains(",") || value.contains("\"") || value.contains("\n"))
            "\"${value.replace("\"", "\"\"")}\""
        else value
}
