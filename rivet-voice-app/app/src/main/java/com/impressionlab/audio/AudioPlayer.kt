package com.impressionlab.audio

import android.media.MediaPlayer
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import java.io.File

/** A single shared MediaPlayer wrapper exposing Compose state for the currently playing file. */
class AudioPlayer {

    var current by mutableStateOf<File?>(null)
        private set
    var isPlaying by mutableStateOf(false)
        private set
    var positionMs by mutableIntStateOf(0)
        private set
    var durationMs by mutableIntStateOf(0)
        private set

    private var player: MediaPlayer? = null

    fun isPlayingFile(file: File?): Boolean =
        file != null && isPlaying && current?.absolutePath == file.absolutePath

    fun isCurrent(file: File?): Boolean =
        file != null && current?.absolutePath == file.absolutePath

    /** Play the file, or pause/resume it if it is already the current one. */
    fun toggle(file: File) {
        val p = player
        if (p != null && isCurrent(file)) {
            if (p.isPlaying) {
                p.pause()
                isPlaying = false
            } else {
                p.start()
                isPlaying = true
            }
            return
        }
        stop()
        try {
            val mp = MediaPlayer()
            mp.setDataSource(file.absolutePath)
            mp.setOnCompletionListener {
                isPlaying = false
                positionMs = durationMs
            }
            mp.prepare()
            mp.start()
            player = mp
            current = file
            durationMs = mp.duration
            positionMs = 0
            isPlaying = true
        } catch (e: Exception) {
            stop()
        }
    }

    fun seekTo(ms: Int) {
        val clamped = ms.coerceIn(0, durationMs)
        try {
            player?.seekTo(clamped)
            positionMs = clamped
        } catch (_: Exception) {
        }
    }

    fun stop() {
        try {
            player?.release()
        } catch (_: Exception) {
        }
        player = null
        current = null
        isPlaying = false
        positionMs = 0
        durationMs = 0
    }

    /** Run from a LaunchedEffect; keeps positionMs updated while playing. */
    suspend fun observeProgress() {
        while (true) {
            val p = player
            if (p != null && isPlaying) {
                positionMs = try {
                    p.currentPosition
                } catch (_: Exception) {
                    positionMs
                }
            }
            delay(66)
        }
    }
}
