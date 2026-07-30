package moe.horizn.mobile

import android.app.*
import android.content.Intent
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat

// Minimal foreground service to keep the process (and therefore the
// WebView's JS/WebSocket) alive while the app is backgrounded.
// UNTESTED — written without a compiler/emulator available in this
// environment. Sanity-check package name, notification channel setup on
// the target API level, and permission grants (POST_NOTIFICATIONS on
// API 33+) before relying on this.
class KeepAliveService : Service() {
    private var wakeLock: PowerManager.WakeLock? = null
    private val channelId = "horizon_keepalive"

    override fun onCreate() {
        super.onCreate()
        val manager = getSystemService(NotificationManager::class.java)
        val channel = NotificationChannel(
            channelId,
            "Horizon connection",
            NotificationManager.IMPORTANCE_LOW
        )
        manager.createNotificationChannel(channel)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Horizon")
            .setContentText("Staying connected to F-List chat")
            .setSmallIcon(android.R.drawable.ic_dialog_info) // TODO: real app icon
            .setOngoing(true)
            .build()

        startForeground(1, notification)

        val pm = getSystemService(POWER_SERVICE) as PowerManager
        wakeLock = pm.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "Horizon::KeepAliveWakeLock"
        ).apply { acquire(10 * 60 * 1000L /*10 min safety cap, renew via re-start*/) }

        return START_STICKY
    }

    override fun onDestroy() {
        wakeLock?.let { if (it.isHeld) it.release() }
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
