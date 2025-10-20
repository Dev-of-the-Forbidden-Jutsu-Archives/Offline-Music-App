package com.example.jetplay.di

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import com.example.jetplay.player.notification.JetAudioNotificationManager
import com.example.jetplay.player.service.JetAudioServiceHandler
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.UnstableApi
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import androidx.media3.session.MediaSession
import com.example.jetplay.MainActivity

@Module
@InstallIn(SingletonComponent::class)
object MediaModule {

    @Provides
    @Singleton
    fun provideAudioAttributes(): AudioAttributes = AudioAttributes.Builder()
        .setContentType(androidx.media3.common.C.AUDIO_CONTENT_TYPE_MUSIC)
        .setUsage(androidx.media3.common.C.USAGE_MEDIA)
        .build()


    @androidx.media3.common.util.UnstableApi
    @Provides
    @Singleton
    @UnstableApi
    fun provideExoPlayer(
        @ApplicationContext context: Context,
        audioAttributes: AudioAttributes

    ): ExoPlayer = ExoPlayer.Builder(context)
        .setAudioAttributes(audioAttributes, true)
        .setTrackSelector(DefaultTrackSelector(context))
        .setHandleAudioBecomingNoisy(true)
        .build()

//    @Provides
//    @Singleton
//    fun provideMediaSession(
//        @ApplicationContext context: Context,
//        player: ExoPlayer
//    ): MediaSession =MediaSession.Builder(context, player).build()

    @Provides
    @Singleton
    fun provideMediaSession(
        @ApplicationContext context: Context,
        player: ExoPlayer
    ): MediaSession {
        // Intent to open your app (MainActivity)
        val sessionIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
        }

        // PendingIntent wrapping that intent
        val sessionActivityPendingIntent = PendingIntent.getActivity(
            context,
            0,
            sessionIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Build and return MediaSession with the sessionActivity set
        return MediaSession.Builder(context, player)
            .setSessionActivity(sessionActivityPendingIntent) // ✅ important line
            .build()
    }


    @OptIn(androidx.media3.common.util.UnstableApi::class)
    @Provides
    @Singleton
    fun provideNotificationManager(
        @ApplicationContext context: Context,
        player: ExoPlayer
    ): JetAudioNotificationManager = JetAudioNotificationManager(
        context = context,
        exoPlayer = player
    )


    @Provides
    @Singleton
    fun provideServiceHandler(exoPlayer: ExoPlayer): JetAudioServiceHandler =
        JetAudioServiceHandler(exoPlayer)




}