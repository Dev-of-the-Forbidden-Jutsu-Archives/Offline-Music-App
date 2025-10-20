@file:OptIn(SavedStateHandleSaveableApi::class)

package com.example.jetplay.ui.audio

import androidx.compose.runtime.mutableStateOf
import androidx.core.net.toUri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.SavedStateHandleSaveableApi
import androidx.lifecycle.viewmodel.compose.saveable
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.example.jetplay.data.local.model.Audio
import com.example.jetplay.data.repository.AudioRepository
import com.example.jetplay.player.service.JetAudioServiceHandler
import com.example.jetplay.player.service.JetAudioState
import com.example.jetplay.player.service.PlayerEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject


private val audioDummy = Audio(
    "".toUri(), displayName = "", id = 0, artist = "", data = "", duration = 0, title = ""
)

@HiltViewModel
class AudioViewModel @Inject constructor(
    private val audioServiceHandler: JetAudioServiceHandler,
    private val repository: AudioRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    var duration by savedStateHandle.saveable { mutableStateOf(0L) }
    @OptIn(SavedStateHandleSaveableApi::class)
    var progress by savedStateHandle.saveable { mutableStateOf(0f) }
    var progressString by savedStateHandle.saveable { mutableStateOf("00:00") }
    var isplaying by savedStateHandle.saveable { mutableStateOf(false) }
    var currentSelectedAudio by savedStateHandle.saveable { mutableStateOf(audioDummy) }
    var audiaList by savedStateHandle.saveable { mutableStateOf(listOf<Audio>()) }


    private val _uiState = MutableStateFlow<UIState>(UIState.Initial)
    val uiState: StateFlow<UIState> = _uiState

    init{
        LoadAudioData()
    }

    init {
        viewModelScope.launch {
            audioServiceHandler.audioState.collectLatest { mediaState ->
                when (mediaState) {
                    is JetAudioState.Buffering -> caculateProgressValue(mediaState.progress)
                    is JetAudioState.CurrentPlaying -> {
                        currentSelectedAudio = audiaList[mediaState.mediaItemIndex]
                    }

                    JetAudioState.Initial -> _uiState.value = UIState.Initial
                    is JetAudioState.Playing -> isplaying = mediaState.isPlaying
                    is JetAudioState.Progress -> caculateProgressValue(mediaState.progress)
                    is JetAudioState.Ready -> {
                        duration = mediaState.duration
                        _uiState.value = UIState.Ready

                    }
                }


            }
        }
    }

    private fun LoadAudioData() {
        viewModelScope.launch {
            val audio = repository.getAudioData()
            audiaList = audio
            setMediaItems()
        }
    }

    private fun setMediaItems() {
        audiaList.map { audio ->
            MediaItem.Builder()
                .setUri(audio.uri)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setAlbumArtist(audio.artist)
                        .setDisplayTitle(audio.title)
                        .setSubtitle(audio.displayName)
                        .setArtworkUri(audio.uri)
                        .build()
                )
                .build()
        }.also{
            audioServiceHandler.setMediaItemList(it)
        }
    }


    private fun caculateProgressValue(currentProgress: Long) {
        progress =
            if (currentProgress > 0) ((currentProgress.toFloat() / duration.toFloat()) * 100f)
            else 0f
        progressString = formDuration(currentProgress)

    }

    fun onUiEvents(uiEvents: UIEvents)= viewModelScope.launch {
        when (uiEvents) {
            UIEvents.Backward -> audioServiceHandler.onPlayerEvent(PlayerEvent.Backward)
            UIEvents.Forward -> audioServiceHandler.onPlayerEvent(PlayerEvent.Forward)
            UIEvents.PlayOrPause -> audioServiceHandler.onPlayerEvent(PlayerEvent.PlayPause)
            is UIEvents.SeekTo -> {
                audioServiceHandler.onPlayerEvent(
                    PlayerEvent.SeekTo,
                    seekPosition = ((duration * uiEvents.position)/100f).toLong()
                )
            }
            UIEvents.SeekToNext -> audioServiceHandler.onPlayerEvent(PlayerEvent.SeekToNext)
            is UIEvents.SelectedAudioChange -> {
                audioServiceHandler.onPlayerEvent(
                    PlayerEvent.SelectedAudioChange,
                    selectedAudioIndex = uiEvents.index
                )
            }
            UIEvents.Stop -> TODO()
            is UIEvents.UpdateProgress ->{
                audioServiceHandler.onPlayerEvent(
                    PlayerEvent.UpdateProgress(
                        newProgress = uiEvents.newProgress
                    )
                )
                progress = uiEvents.newProgress
            }
        }

    }


    fun formDuration(duration: Long): String {
        val minutes = TimeUnit.MINUTES.convert(duration, TimeUnit.MILLISECONDS)
        val seconds = (TimeUnit.SECONDS.convert(
            duration,
            TimeUnit.MILLISECONDS
        ) - minutes * TimeUnit.SECONDS.convert(1, TimeUnit.MINUTES))

        return String.format("%02d:%02d", minutes, seconds)


    }

    override fun onCleared() {
        viewModelScope.launch {
            audioServiceHandler.onPlayerEvent(PlayerEvent.Stop)
        }
        super.onCleared()

    }

}


sealed class UIEvents {
    object PlayOrPause : UIEvents()
    data class SelectedAudioChange(val index: Int) : UIEvents()
    data class SeekTo(val position: Float) : UIEvents()
    object SeekToNext : UIEvents()
    object Forward : UIEvents()
    object Backward : UIEvents()
    data class UpdateProgress(val newProgress: Float) : UIEvents()
    object Stop : UIEvents()


}


sealed class UIState {
    object Initial : UIState()
    object Ready : UIState()


}