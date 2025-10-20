
package com.example.jetplay.player.service


import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class JetAudioServiceHandler @Inject constructor(
    private val exoPlayer: ExoPlayer
) : Player.Listener {
    private val _audioState = MutableStateFlow<JetAudioState>(JetAudioState.Initial)
    val audioState: StateFlow<JetAudioState> = _audioState.asStateFlow()

    private var job: Job? = null
    // It's good practice to make the scope injectable or pass it in, but for this context, this is fine.
    private val scope = CoroutineScope(Dispatchers.Main)

    init {
        exoPlayer.addListener(this)
    }

    fun addMediaItem(mediaItem: MediaItem) {
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
    }

    fun setMediaItemList(mediaItems: List<MediaItem>) {
        exoPlayer.setMediaItems(mediaItems)
        exoPlayer.prepare()
    }

    // FIX #2: Removed the unnecessary outer scope.launch
    fun onPlayerEvent(
        playerEvent: PlayerEvent,
        selectedAudioIndex: Int = -1,
        seekPosition: Long = 0
    ) {
        when (playerEvent) {
            PlayerEvent.Backward -> exoPlayer.seekBack()
            PlayerEvent.Forward -> exoPlayer.seekForward()
            PlayerEvent.SeekToNext -> exoPlayer.seekToNext()
            PlayerEvent.PlayPause -> playOrPause()
            PlayerEvent.Stop -> stopProgressUpdate()

            is PlayerEvent.SeekTo -> exoPlayer.seekTo(seekPosition)

            is PlayerEvent.SelectedAudioChange -> {
                if (selectedAudioIndex == exoPlayer.currentMediaItemIndex) {
                    playOrPause()
                } else {
                    exoPlayer.seekToDefaultPosition(selectedAudioIndex)
                    // The onIsPlayingChanged callback will handle the state update automatically
                    exoPlayer.playWhenReady = true
                }
            }

            is PlayerEvent.UpdateProgress -> {
                exoPlayer.seekTo(
                    (exoPlayer.duration * playerEvent.newProgress).toLong()
                )
            }
        }
    }

    override fun onPlaybackStateChanged(playbackState: Int) {
        when (playbackState) {
            ExoPlayer.STATE_BUFFERING -> _audioState.value = JetAudioState.Buffering(exoPlayer.currentPosition)
            ExoPlayer.STATE_READY -> _audioState.value = JetAudioState.Ready(exoPlayer.duration)
        }
    }

    // FIX #1: Correctly handle the isPlaying state without overwriting it.
    override fun onIsPlayingChanged(isPlaying: Boolean) {
        // ONLY emit the Playing state here.
        _audioState.value = JetAudioState.Playing(isPlaying = isPlaying)

        if (isPlaying) {
            // Relaunch the progress updater from the main thread
            scope.launch { startProgressUpdate() }
        } else {
            stopProgressUpdate()
        }
    }

    // This is the correct place to handle the current playing item.
    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
        super.onMediaItemTransition(mediaItem, reason)
        _audioState.value = JetAudioState.CurrentPlaying(exoPlayer.currentMediaItemIndex)
    }

    private fun playOrPause() {
        if (exoPlayer.isPlaying) {
            exoPlayer.pause()
        } else {
            exoPlayer.play()
        }
    }

    private suspend fun startProgressUpdate() {
        job?.cancel() // Cancel any existing job
        job = scope.launch {
            while (true) {
                delay(500)
                _audioState.value = JetAudioState.Progress(exoPlayer.currentPosition)
            }
        }
    }

    private fun stopProgressUpdate() {
        job?.cancel()
    }
}


// --- Definitions (Unchanged) ---

sealed class PlayerEvent {
    object PlayPause : PlayerEvent()
    object SelectedAudioChange : PlayerEvent()
    object SeekToNext : PlayerEvent()
    object Forward : PlayerEvent()
    object Backward : PlayerEvent()
    object Stop : PlayerEvent()
    object SeekTo : PlayerEvent()
    data class UpdateProgress(val newProgress: Float) : PlayerEvent()
}

sealed class JetAudioState {
    object Initial : JetAudioState()
    data class Ready(val duration: Long) : JetAudioState()
    data class Progress(val progress: Long) : JetAudioState()
    data class Buffering(val progress: Long) : JetAudioState()
    data class Playing(val isPlaying: Boolean) : JetAudioState()
    data class CurrentPlaying(val mediaItemIndex: Int) : JetAudioState()
}
