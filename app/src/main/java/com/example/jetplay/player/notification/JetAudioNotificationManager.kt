//package com.example.jetplay.player.notification
//
//import android.app.Notification
//import android.app.NotificationChannel
//import android.app.NotificationManager
//import android.content.Context
//import androidx.core.app.NotificationCompat
//import androidx.core.app.NotificationManagerCompat
//import androidx.media3.common.util.UnstableApi
//import androidx.media3.exoplayer.ExoPlayer
//import androidx.media3.session.MediaSession
//import androidx.media3.session.MediaSessionService
//import androidx.media3.ui.PlayerNotificationManager
//import com.example.jetplay.R
//import dagger.hilt.android.qualifiers.ApplicationContext
//import jakarta.inject.Inject
//
//
//private const val NOTIFICATION_ID = 101
//private const val NOTIFICATION_CHANNEL_NAME = "notification channel 1"
//private const val NOTIFICATION_CHANNEL_ID = "notification channel id 1"
//
//@UnstableApi
//@Suppress("DEPRECATION")
//class JetAudioNotificationManager @Inject constructor(
//    @ApplicationContext private val context: Context,
//    private val exoPlayer: ExoPlayer,
//) {
//    private val notificationManager: NotificationManagerCompat =
//        NotificationManagerCompat.from(context)
//    private var playerNotificationManager: PlayerNotificationManager? = null
//
//
//    init {
//        createNotificationChannel()
//    }
//
//    @UnstableApi
//    fun startNotificationService(
//        mediaSessionService: MediaSessionService,
//        mediaSession: MediaSession,
//    ) {
//        buildNotification(mediaSession)
//        startForeGroundNotificationService(mediaSessionService)
//    }
//
//    private fun startForeGroundNotificationService(mediaSessionService: MediaSessionService) {
//        val notification = Notification.Builder(context, NOTIFICATION_CHANNEL_ID)
//            .setCategory(Notification.CATEGORY_SERVICE)
//            .build()
//        mediaSessionService.startForeground(NOTIFICATION_ID, notification)
//    }
//
//
//
//
//    @UnstableApi
//    private fun buildNotification(mediaSession: MediaSession) {
//        playerNotificationManager = PlayerNotificationManager.Builder(
//            context,
//            NOTIFICATION_ID,
//            NOTIFICATION_CHANNEL_ID
//        )
//            .setMediaDescriptionAdapter(
//                JetAudioNotificationAdapter(
//                    context = context,
//                    pendingIntent = mediaSession.sessionActivity
//                )
//            )
//            .setSmallIconResourceId(R.drawable.microphone)
//            .build()
//
//            .also { it ->
//
//
//                it.setUseFastForwardActionInCompactView(true)
//                it.setUseRewindActionInCompactView(true)
//                it.setUseNextActionInCompactView(true)
//                it.setPriority(NotificationCompat.PRIORITY_LOW)
//                it.setPlayer(exoPlayer)
//
//            }
//    }
//
//    @UnstableApi
//    private fun createNotificationChannel() {
//        val channel = NotificationChannel(
//            NOTIFICATION_CHANNEL_ID,
//            NOTIFICATION_CHANNEL_NAME,
//            NotificationManager.IMPORTANCE_LOW
//        )
//        notificationManager.createNotificationChannel(channel)
//    }
//}
//




package com.example.jetplay.player.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.annotation.OptIn
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService

import androidx.media3.ui.PlayerNotificationManager
import com.example.jetplay.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

private const val NOTIFICATION_ID = 101
private const val NOTIFICATION_CHANNEL_ID = "jet_audio_playback_channel"
private const val NOTIFICATION_CHANNEL_NAME = "JetPlay Audio Playback"


@UnstableApi
class JetAudioNotificationManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val exoPlayer: ExoPlayer,
) {

    private val notificationManager by lazy {
        NotificationManagerCompat.from(context)
    }

    private var playerNotificationManager: PlayerNotificationManager? = null

    init {
        createNotificationChannel()
    }


    @OptIn(UnstableApi::class)
    fun startNotificationService(
        service: MediaSessionService,
        mediaSession: MediaSession
    ) {
        if (playerNotificationManager == null) {
            buildNotification(mediaSession)
        }
        playerNotificationManager?.setPlayer(exoPlayer)

        val notification = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setContentTitle(context.getString(R.string.app_name))
            .setContentText("Playing audio…")
            .setSmallIcon(R.drawable.microphone)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(Notification.CATEGORY_SERVICE)
            .setOngoing(true)
            .build()

        // Start as a foreground service for playback
        service.startForeground(NOTIFICATION_ID, notification)
    }


    @OptIn(UnstableApi::class)
    private fun buildNotification(mediaSession: MediaSession) {
        playerNotificationManager = PlayerNotificationManager.Builder(
            context,
            NOTIFICATION_ID,
            NOTIFICATION_CHANNEL_ID
        )
            .setMediaDescriptionAdapter(
                JetAudioNotificationAdapter(
                    context = context,
                    pendingIntent = mediaSession.sessionActivity
                )
            )
            .setSmallIconResourceId(R.drawable.microphone)
            .setChannelImportance(NotificationManager.IMPORTANCE_LOW)
            .build()
            .apply {
                setUseFastForwardAction(true)
                setUseRewindAction(true)
                setUseNextAction(true)
                setUsePreviousAction(true)
                setUsePlayPauseActions(true)
                setUseChronometer(false)
                setPlayer(exoPlayer)

                setPriority(NotificationCompat.PRIORITY_LOW)

            }
    }

    /**
     * Stops the foreground notification and releases resources.
     */
    fun stopNotification(service: MediaSessionService) {
        playerNotificationManager?.setPlayer(null)
        playerNotificationManager = null

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            service.stopForeground(MediaSessionService.STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION")
            service.stopForeground(true)
        }

        notificationManager.cancel(NOTIFICATION_ID)
    }

    /**
     * Creates the playback notification channel.
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows playback controls and track information"
                setShowBadge(false)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }

            val systemManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            systemManager.createNotificationChannel(channel)
        }
    }
}
