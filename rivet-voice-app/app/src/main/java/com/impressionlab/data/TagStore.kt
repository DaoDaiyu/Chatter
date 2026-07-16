package com.impressionlab.data

import android.content.Context
import androidx.compose.ui.graphics.Color
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/**
 * Tags label what an attempt was drilling (attack, pitch, resonance, …).
 *
 * - The global tag list lives in SharedPreferences, seeded with drill-type defaults.
 * - Per-attempt tags live in a tags.json sidecar inside each reference folder,
 *   keyed by attempt file name, so audio files never need renaming.
 */
object TagStore {

    val DEFAULT_TAGS = listOf(
        "Shadow", "Hum", "Attack", "Rhythm", "Pitch", "Resonance", "Full take", "Improv",
    )

    private const val KEY_TAGS = "global_tags"

    private val CHIP_COLORS = listOf(
        Color(0xFFE8722A), Color(0xFF8A6BF2), Color(0xFF2FA98C), Color(0xFF3E7CB1),
        Color(0xFFD9A511), Color(0xFFEC4899), Color(0xFF22D3EE), Color(0xFF65A30D),
        Color(0xFFDC3545), Color(0xFF0E7DB8),
    )

    /** Stable color per tag name so chips look consistent everywhere. */
    fun colorFor(tag: String): Color {
        val index = ((tag.lowercase().hashCode() % CHIP_COLORS.size) + CHIP_COLORS.size) % CHIP_COLORS.size
        return CHIP_COLORS[index]
    }

    private fun prefs(context: Context) =
        context.getSharedPreferences("impressionlab", Context.MODE_PRIVATE)

    fun globalTags(context: Context): List<String> {
        val raw = prefs(context).getString(KEY_TAGS, null) ?: return DEFAULT_TAGS
        return try {
            val arr = JSONArray(raw)
            List(arr.length()) { arr.getString(it) }.ifEmpty { DEFAULT_TAGS }
        } catch (e: Exception) {
            DEFAULT_TAGS
        }
    }

    fun addGlobalTag(context: Context, name: String): List<String> {
        val clean = name.trim().take(24)
        val current = globalTags(context)
        if (clean.isEmpty() || current.any { it.equals(clean, ignoreCase = true) }) return current
        val updated = current + clean
        prefs(context).edit().putString(KEY_TAGS, JSONArray(updated).toString()).apply()
        return updated
    }

    private fun tagsFile(referenceDir: File) = File(referenceDir, "tags.json")

    /** attempt file name -> tags */
    fun readAll(referenceDir: File): Map<String, List<String>> {
        val file = tagsFile(referenceDir)
        if (!file.exists()) return emptyMap()
        return try {
            val obj = JSONObject(file.readText())
            obj.keys().asSequence().associateWith { key ->
                val arr = obj.getJSONArray(key)
                List(arr.length()) { arr.getString(it) }
            }
        } catch (e: Exception) {
            emptyMap()
        }
    }

    fun setTags(referenceDir: File, attemptFileName: String, tags: List<String>) {
        val all = readAll(referenceDir).toMutableMap()
        if (tags.isEmpty()) all.remove(attemptFileName) else all[attemptFileName] = tags
        val obj = JSONObject()
        all.forEach { (name, list) -> obj.put(name, JSONArray(list)) }
        tagsFile(referenceDir).writeText(obj.toString())
    }

    fun removeEntry(referenceDir: File, attemptFileName: String) =
        setTags(referenceDir, attemptFileName, emptyList())
}
