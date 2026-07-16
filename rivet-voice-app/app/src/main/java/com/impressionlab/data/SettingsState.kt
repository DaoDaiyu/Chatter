package com.impressionlab.data

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.io.File

/** User preferences (color scheme, dark mode, custom background) backed by SharedPreferences. */
class SettingsState(private val context: Context) {

    companion object {
        const val DARK_SYSTEM = "system"
        const val DARK_LIGHT = "light"
        const val DARK_DARK = "dark"
        private const val KEY_PALETTE = "palette"
        private const val KEY_DARK = "dark_mode"
        private const val KEY_BG_VERSION = "bg_version"
    }

    private val prefs = context.getSharedPreferences("impressionlab", Context.MODE_PRIVATE)

    var paletteName by mutableStateOf(prefs.getString(KEY_PALETTE, "Rivet Rust")!!)
        private set

    var darkMode by mutableStateOf(prefs.getString(KEY_DARK, DARK_SYSTEM)!!)
        private set

    /** Bumped every time the background image changes, so decoders re-read the file. */
    var backgroundVersion by mutableIntStateOf(prefs.getInt(KEY_BG_VERSION, 0))
        private set

    val backgroundFile: File get() = File(context.filesDir, "background_image")
    val hasBackground: Boolean get() = backgroundVersion > 0 && backgroundFile.exists()

    fun setPalette(name: String) {
        paletteName = name
        prefs.edit().putString(KEY_PALETTE, name).apply()
    }

    fun updateDarkMode(mode: String) {
        darkMode = mode
        prefs.edit().putString(KEY_DARK, mode).apply()
    }

    fun setBackground(uri: Uri): Boolean {
        return try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                backgroundFile.outputStream().use { input.copyTo(it) }
            } ?: return false
            backgroundVersion += 1
            prefs.edit().putInt(KEY_BG_VERSION, backgroundVersion).apply()
            true
        } catch (e: Exception) {
            false
        }
    }

    fun clearBackground() {
        backgroundFile.delete()
        backgroundVersion = 0
        prefs.edit().putInt(KEY_BG_VERSION, 0).apply()
    }
}
