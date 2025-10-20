package com.example.jetplay.ui.audio

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.example.jetplay.data.local.model.Audio


@Composable
fun HomeScreen(
    progress: Float,
    onProcess: (Float) -> Unit,
    isAudioPlaying: Boolean,
    currentPlayingAudio: Audio,
    audioList: List<Audio>,
    onStart: () -> Unit,
    onItemClick: (Int) -> Unit,
    onNext: () -> Unit
) {
    Scaffold(
        bottomBar = {
            BottomBarPlayer(
                progress = progress,
                onProcess = onProcess,
                audio = currentPlayingAudio,
                onStart = onStart,
                onNext = onNext,
                isAudioPlaying = isAudioPlaying
            )
        }

    ) {
        LazyColumn(
            contentPadding = it
        ) {
            itemsIndexed(audioList) { index, audio ->
                AudioItem(
                    audio = audio,
                    onItemClick = { onItemClick(index) }
                )

            }
        }


    }

}

@Composable
fun AudioItem(

    audio: Audio,
    onItemClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
            .clickable { onItemClick() }
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Spacer(Modifier.size(4.dp))
                Text(
                    text = audio.displayName,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.basicMarquee(),
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                )
                Spacer(modifier = Modifier.size(4.dp))
                Text(
                    text = audio.artist,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Clip
                )
            }
            Text(text = TimeStampToDuration(audio.duration.toLong()))
            Spacer(modifier = Modifier.size(8.dp))
        }
    }

}


private fun TimeStampToDuration(position: Long): String {
    val totalSeconds = Math.floor(position / 1E3).toInt()
    val minutes = totalSeconds / 60
    val remainingSeconds = totalSeconds - (minutes * 60)

    return if (position < 0) "--:--" else "%d:%02d".format(minutes, remainingSeconds)


}


@Composable
fun BottomBarPlayer(
    progress: Float,
    onProcess: (Float) -> Unit,
    audio: Audio,
    isAudioPlaying: Boolean,
    onStart: () -> Unit,
    onNext: () -> Unit,
) {
    BottomAppBar {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ArtistInfo(
                    modifier = Modifier.weight(1f),
                    audio = audio
                )
                MediaplayerController(
                    isAudioPlaying = isAudioPlaying,
                    onStart = onStart,
                    onNext = onNext
                )

                Slider(
                    value = progress,
                    onValueChange = {
                        onProcess(it)
                    },
                    valueRange = 0f..100f
                )
            }



        }

    }

}

@Composable
fun MediaplayerController(
    isAudioPlaying: Boolean,
    onStart: () -> Unit,
    onNext: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .height(56.dp)
            .padding(4.dp)
    ) {
        PlayerIcon(icon = if (isAudioPlaying) Icons.Default.Pause else Icons.Default.PlayArrow) {
            onStart()
        }
        Spacer(modifier = Modifier.size(4.dp))
        PlayerIcon(icon = Icons.Default.SkipNext) {
            onNext()
        }

    }

}


@Composable
fun ArtistInfo(
    modifier: Modifier = Modifier,
    audio: Audio
) {
    Row(
        modifier = modifier.padding(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        PlayerIcon(
            icon = Icons.Default.MusicNote,
            borderStroke = BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.onSurface,

            )
        ) {}
        Spacer(modifier = modifier.size(4.dp))
        Column(
        ) {
            Text(
                text = audio.title,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                modifier = Modifier.basicMarquee()
            )
            Spacer(modifier = Modifier.size(4.dp))
            Text(
                text = audio.artist,
                fontWeight = FontWeight.Normal,
                style = MaterialTheme.typography.bodySmall,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1

            )

        }

    }

}

@Composable
fun PlayerIcon(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    borderStroke: BorderStroke? = null,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    color: Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit
) {
    Surface(
        shape = CircleShape,
        border = borderStroke,
        modifier = modifier
            .clip(CircleShape)
            .clickable { onClick() },
        contentColor = color,
        color = backgroundColor
    ) {
        Box(modifier = Modifier.padding(4.dp), contentAlignment = Alignment.Center) {
            Icon(imageVector = icon, contentDescription = null)

        }

    }

}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview(){
    HomeScreen(
        progress = 30f,
        onProcess = {},
        isAudioPlaying = true,
        currentPlayingAudio = Audio(
            "".toUri(), displayName = "aditya", id = 0, artist = "sai", data = "", duration = 0, title = ""
        ),
        audioList = listOf(
            Audio(
                "".toUri(), displayName = "aditya", id = 0, artist = "sai", data = "", duration = 300000, title = ""
            ),
            Audio(
                "".toUri(), displayName = "alekhya", id = 0, artist = "kamarsu", data = "", duration = 0, title = ""
            ),
        ),
        onStart = {},
        onItemClick = {},
        onNext = {}


    )

}