package com.impressionlab.data

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import java.io.File

/**
 * All practice data lives on the filesystem, mirroring the app's mental model:
 *
 * filesDir/characters/<Character>/<Reference name>/
 *     reference.<ext>            <- the imported voice line
 *     Attempt N ; <Reference name>.m4a
 *     notes.txt
 */
object VoiceRepo {

    private val attemptRegex = Regex("^Attempt (\\d+) ;")

    fun charactersRoot(context: Context): File =
        File(context.filesDir, "characters").apply { mkdirs() }

    fun characters(context: Context): List<File> =
        charactersRoot(context).listFiles { f -> f.isDirectory }
            ?.sortedBy { it.name.lowercase() } ?: emptyList()

    fun createCharacter(context: Context, name: String): File? {
        val clean = sanitize(name)
        if (clean.isBlank()) return null
        val dir = File(charactersRoot(context), clean)
        dir.mkdirs()
        return dir
    }

    fun references(characterDir: File): List<File> =
        characterDir.listFiles { f -> f.isDirectory }
            ?.sortedBy { it.name.lowercase() } ?: emptyList()

    /** Copies the picked audio into its own reference folder. Returns the folder, or null on failure. */
    fun importReference(context: Context, characterDir: File, uri: Uri): File? {
        val displayName = queryDisplayName(context, uri) ?: "Voice line"
        val ext = displayName.substringAfterLast('.', "").lowercase().ifBlank { "m4a" }
        val base = sanitize(displayName.substringBeforeLast('.').ifBlank { displayName })
            .ifBlank { "Voice line" }

        var dir = File(characterDir, base)
        var suffix = 2
        while (dir.exists()) {
            dir = File(characterDir, "$base ($suffix)")
            suffix++
        }
        dir.mkdirs()

        val out = File(dir, "reference.$ext")
        return try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                out.outputStream().use { input.copyTo(it) }
            } ?: run { dir.deleteRecursively(); return null }
            dir
        } catch (e: Exception) {
            dir.deleteRecursively()
            null
        }
    }

    fun referenceAudio(referenceDir: File): File? =
        referenceDir.listFiles()?.firstOrNull { it.isFile && it.name.startsWith("reference.") }

    fun notesFile(referenceDir: File): File = File(referenceDir, "notes.txt")

    fun readNotes(referenceDir: File): String =
        notesFile(referenceDir).takeIf { it.exists() }?.readText() ?: ""

    fun writeNotes(referenceDir: File, text: String) {
        notesFile(referenceDir).writeText(text)
    }

    fun attempts(referenceDir: File): List<File> =
        referenceDir.listFiles()
            ?.filter { it.isFile && attemptRegex.containsMatchIn(it.name) }
            ?.sortedBy { attemptNumber(it) } ?: emptyList()

    fun attemptNumber(file: File): Int =
        attemptRegex.find(file.name)?.groupValues?.get(1)?.toIntOrNull() ?: 0

    /** Next auto-named attempt file: "Attempt N ; <reference name>.m4a" */
    fun newAttemptFile(referenceDir: File): File {
        val next = (attempts(referenceDir).maxOfOrNull { attemptNumber(it) } ?: 0) + 1
        return File(referenceDir, "Attempt $next ; ${referenceDir.name}.m4a")
    }

    fun sanitize(name: String): String =
        name.trim().replace(Regex("[\\\\/:*?\"<>|]"), "_").take(60)

    private fun queryDisplayName(context: Context, uri: Uri): String? =
        context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
            ?.use { cursor -> if (cursor.moveToFirst()) cursor.getString(0) else null }
}
