package com.example.jetplay.player.service

import android.content.Intent
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.example.jetplay.player.notification.JetAudioNotificationManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@UnstableApi
@AndroidEntryPoint
class JetAudioService : MediaSessionService() {

    @Inject lateinit var mediaSession: MediaSession
    @Inject lateinit var notificationManager: JetAudioNotificationManager

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        // Safely start the playback notification
        notificationManager.startNotificationService(
            service = this,
            mediaSession = mediaSession
        )
        return START_STICKY // Keeps the service running while playing
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession {
        // Return the active MediaSession to clients (e.g. controllers)
        return mediaSession
    }

    override fun onDestroy() {
        super.onDestroy()

        // Stop and release ExoPlayer and session properly
        mediaSession.player.apply {
            playWhenReady = false
            stop()
        }

        // Stop the ongoing foreground notification
        notificationManager.stopNotification(this)

        // Release the session last
        mediaSession.release()
    }
}
