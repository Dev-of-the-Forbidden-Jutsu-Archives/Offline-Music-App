package com.example.jetplay


import android.Manifest
import android.os.Build
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.util.UnstableApi
import com.example.jetplay.player.service.JetAudioService
import com.example.jetplay.ui.audio.AudioViewModel
import com.example.jetplay.ui.audio.HomeScreen
import com.example.jetplay.ui.audio.UIEvents
import com.example.jetplay.ui.theme.JetPlayTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: AudioViewModel by viewModels()
    private var isServiceRunning = false

    @OptIn(ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            JetPlayTheme {
                val permissions = if (Build.VERSION.SDK_INT >= 33) {
                    listOf(
                        Manifest.permission.READ_MEDIA_AUDIO,
                        Manifest.permission.READ_MEDIA_VIDEO,
                        Manifest.permission.READ_MEDIA_IMAGES,
                        Manifest.permission.POST_NOTIFICATIONS


                    )
                } else {
                    listOf(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
                val permissionState = rememberMultiplePermissionsState(
                    permissions = permissions
                )
                val lifecycleOwner = LocalLifecycleOwner.current
                DisposableEffect(key1 = lifecycleOwner) {
                    val observer = LifecycleEventObserver { _, event ->
                        if (event == Lifecycle.Event.ON_START) {
                            permissionState.launchMultiplePermissionRequest()
                        }
                    }
                    lifecycleOwner.lifecycle.addObserver(observer)
                    onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
                }
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    HomeScreen(
                        progress = viewModel.progress,
                        onProcess = {viewModel.onUiEvents(UIEvents.SeekTo(it))},
                        isAudioPlaying = viewModel.isplaying,
                        audioList = viewModel.audiaList,
                        currentPlayingAudio = viewModel.currentSelectedAudio,
                        onStart = {
                            viewModel.onUiEvents(UIEvents.PlayOrPause)
                        },
                        onItemClick = {
                            viewModel.onUiEvents(UIEvents.SelectedAudioChange(it))
                            StartService()
                        },
                        onNext = {
                            viewModel.onUiEvents(UIEvents.SeekToNext)
                        }

                    )
                }


            }
        }
    }


    @androidx.annotation.OptIn(UnstableApi::class)
    private fun StartService(){
        val intent = Intent(this, JetAudioService::class.java)
        if (!isServiceRunning) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }
        } else {
            startService(intent)
        }
        isServiceRunning = true
    }


}


