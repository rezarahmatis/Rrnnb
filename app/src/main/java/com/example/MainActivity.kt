package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.screens.MainAquaticScreen
import com.example.ui.theme.PelagiaTheme
import com.example.viewmodel.PelagiaViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: PelagiaViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val initialInsetsController = WindowInsetsControllerCompat(window, window.decorView)
        initialInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        initialInsetsController.isAppearanceLightStatusBars = false
        initialInsetsController.isAppearanceLightNavigationBars = false

        setContent {
            PelagiaTheme {
                val settings by viewModel.settings.collectAsStateWithLifecycle()
                val playbackState by viewModel.playbackState.collectAsStateWithLifecycle()

                // Storage Access Framework: import multiple local audio files (MP3, WAV, OGG, M4A/AAC, FLAC)
                val openAudioFilesLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.OpenMultipleDocuments()
                ) { uris ->
                    if (uris.isNotEmpty()) {
                        viewModel.importAudioFiles(uris)
                    }
                }

                // Storage Access Framework: import entire local music folder recursively
                val openMusicFolderLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.OpenDocumentTree()
                ) { treeUri ->
                    if (treeUri != null) {
                        viewModel.importMusicFolder(treeUri)
                    }
                }

                // Request notification permission on Android 13+ when media playback begins
                val notificationPermissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) { _ -> }

                LaunchedEffect(playbackState.isPlaying) {
                    if (playbackState.isPlaying && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        val granted = ContextCompat.checkSelfPermission(
                            this@MainActivity,
                            Manifest.permission.POST_NOTIFICATIONS
                        ) == PackageManager.PERMISSION_GRANTED
                        if (!granted) {
                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                }

                // Apply Fullscreen & Ambient immersive system bars after layout settles
                LaunchedEffect(settings.fullscreenMode, settings.ambientMode) {
                    window.decorView.post {
                        val controller = WindowInsetsControllerCompat(window, window.decorView)
                        if (settings.fullscreenMode || settings.ambientMode) {
                            controller.hide(WindowInsetsCompat.Type.systemBars())
                        } else {
                            controller.show(WindowInsetsCompat.Type.systemBars())
                        }
                    }
                }

                // Apply Screen Always On while playing or in Ambient Mode
                LaunchedEffect(settings.keepScreenOn, playbackState.isPlaying, settings.ambientMode) {
                    val shouldKeepOn = settings.keepScreenOn && (playbackState.isPlaying || settings.ambientMode)
                    if (shouldKeepOn) {
                        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                    } else {
                        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                    }
                }

                MainAquaticScreen(
                    viewModel = viewModel,
                    onRequestImportFiles = {
                        openAudioFilesLauncher.launch(
                            arrayOf(
                                "audio/mpeg",
                                "audio/wav",
                                "audio/x-wav",
                                "audio/ogg",
                                "audio/mp4",
                                "audio/aac",
                                "audio/flac",
                                "audio/*"
                            )
                        )
                    },
                    onRequestImportFolder = {
                        openMusicFolderLauncher.launch(null)
                    }
                )
            }
        }
    }
}
