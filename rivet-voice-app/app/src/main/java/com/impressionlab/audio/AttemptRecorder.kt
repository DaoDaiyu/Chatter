package com.impressionlab.audio

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.io.File

/** Records attempts as AAC/.m4a with Compose-observable recording state. */
class AttemptRecorder(private val context: Context) {

    var isRecording by mutableStateOf(false)
        private set
    var startedAtMs by mutableLongStateOf(0L)
        private set

    private var recorder: MediaRecorder? = null
    private var outFile: File? = null

    fun start(file: File): Boolean {
        if (isRecording) stop()
        return try {
            val r = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }
            r.setAudioSource(MediaRecorder.AudioSource.MIC)
            r.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            r.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            r.setAudioEncodingBitRate(160_000)
            r.setAudioSamplingRate(44_100)
            r.setOutputFile(file.absolutePath)
            r.prepare()
            r.start()
            recorder = r
            outFile = file
            startedAtMs = System.currentTimeMillis()
            isRecording = true
            true
        } catch (e: Exception) {
            file.delete()
            recorder?.release()
            recorder = null
            false
        }
    }

    /** Stops and finalizes the recording. Returns the file, or null if it was too short/failed. */
    fun stop(): File? {
        val r = recorder ?: return null
        val file = outFile
        var ok = true
        try {
            r.stop()
        } catch (e: Exception) {
            // stop() throws if nothing valid was captured
            ok = false
            file?.delete()
        }
        r.release()
        recorder = null
        outFile = null
        isRecording = false
        return if (ok) file else null
    }

    fun maxAmplitude(): Int = try {
        recorder?.maxAmplitude ?: 0
    } catch (_: Exception) {
        0
    }
}
