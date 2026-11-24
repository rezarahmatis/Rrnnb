package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.SetMeal
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.rendering.AquaticSurfaceView
import com.example.ui.components.AudioHarmonicIndicator
import com.example.ui.components.GlassPillSurface
import com.example.ui.theme.AbyssBlack
import com.example.ui.theme.BioluminescentCyan
import com.example.ui.theme.MistSilver
import com.example.ui.theme.PelagicGlass
import com.example.ui.theme.SubtleGlassStroke
import com.example.viewmodel.OverlayPanel
import com.example.viewmodel.PelagiaViewModel
import kotlinx.coroutines.delay

@Composable
fun MainAquaticScreen(
    viewModel: PelagiaViewModel,
    onRequestImportFiles: () -> Unit,
    onRequestImportFolder: () -> Unit
) {
    val currentWorld by viewModel.currentWorld.collectAsStateWithLifecycle()
    val activeRoster by viewModel.activeRoster.collectAsStateWithLifecycle()
    val activeAquariumName by viewModel.activeAquariumName.collectAsStateWithLifecycle()
    val savedAquariums by viewModel.savedAquariums.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val playbackState by viewModel.playbackState.collectAsStateWithLifecycle()
    val audioSpectrum by viewModel.audioSpectrum.collectAsStateWithLifecycle()
    val activePanel by viewModel.activePanel.collectAsStateWithLifecycle()
    val statusBanner by viewModel.statusBanner.collectAsStateWithLifecycle()

    val allTracks by viewModel.allTracks.collectAsStateWithLifecycle()
    val favoriteTracks by viewModel.favoriteTracks.collectAsStateWithLifecycle()
    val recentTracks by viewModel.recentlyPlayedTracks.collectAsStateWithLifecycle()
    val playlists by viewModel.playlists.collectAsStateWithLifecycle()
    val selectedPlaylistId by viewModel.selectedPlaylistId.collectAsStateWithLifecycle()
    val selectedPlaylistTracks by viewModel.selectedPlaylistTracks.collectAsStateWithLifecycle()
    val isImportingMusic by viewModel.isImportingMusic.collectAsStateWithLifecycle()

    // Handle Android Back button to dismiss open panels or exit Ambient Mode
    BackHandler(enabled = activePanel != OverlayPanel.NONE || settings.ambientMode) {
        when {
            activePanel != OverlayPanel.NONE -> viewModel.closePanel()
            settings.ambientMode -> viewModel.setAmbientMode(false)
        }
    }

    // Brief gentle hint when entering Ambient Mode
    var showAmbientHint by remember { mutableStateOf(false) }
    LaunchedEffect(settings.ambientMode) {
        if (settings.ambientMode) {
            showAmbientHint = true
            delay(2600L)
            showAmbientHint = false
        } else {
            showAmbientHint = false
        }
    }

    val totalActiveFish = activeRoster.filter { it.enabled }.sumOf { it.count }

    Box(modifier = Modifier.fillMaxSize()) {
        // 1. Fullscreen OpenGL ES 3.0 Real-Time Aquatic World
        AquaticSurfaceView(
            engine = viewModel.simulationEngine,
            onExitAmbient = { viewModel.setAmbientMode(false) },
            modifier = Modifier
                .fillMaxSize()
                .testTag("aquatic_opengl_canvas")
        )

        // 2. Minimal Floating Glassmorphic HUD (hidden in Ambient Mode or when a sheet is open)
        AnimatedVisibility(
            visible = !settings.ambientMode && activePanel == OverlayPanel.NONE,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Top Header Pill: World & Ecosystem Identity + Hide UI
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    GlassPillSurface(
                        cornerRadius = 22.dp,
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .clip(RoundedCornerShape(22.dp))
                            .clickable { viewModel.openPanel(OverlayPanel.WORLDS) }
                            .testTag("world_header_pill")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f, fill = false)) {
                                Text(
                                    text = currentWorld.name,
                                    style = MaterialTheme.typography.titleLarge,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = activeAquariumName?.let { "$it • $totalActiveFish creatures" }
                                        ?: "${currentWorld.habitat.label} • $totalActiveFish creatures",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MistSilver,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            AudioHarmonicIndicator(
                                spectrum = audioSpectrum,
                                isPlaying = playbackState.isPlaying
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    GlassPillSurface(cornerRadius = 24.dp) {
                        IconButton(
                            onClick = { viewModel.setAmbientMode(true) },
                            modifier = Modifier.testTag("ambient_mode_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.VisibilityOff,
                                contentDescription = stringResource(R.string.action_ambient),
                                tint = Color.White
                            )
                        }
                    }
                }

                // Bottom Floating Console (centered with max width for tablets/foldables)
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                        .widthIn(max = 560.dp)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Minimal Now Playing Floating Strip
                    GlassPillSurface(
                        cornerRadius = 22.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(22.dp))
                            .clickable { viewModel.openPanel(OverlayPanel.MUSIC) }
                            .testTag("mini_player_card")
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 16.dp, end = 10.dp, top = 8.dp, bottom = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = playbackState.currentTrack?.title
                                            ?: stringResource(R.string.no_track_selected),
                                        style = MaterialTheme.typography.titleSmall,
                                        color = Color.White,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = playbackState.currentTrack?.artist
                                            ?: if (allTracks.isEmpty()) "Tap to import MP3, WAV, OGG or M4A"
                                            else "${allTracks.size} tracks ready offline",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MistSilver,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                IconButton(
                                    onClick = { viewModel.previousTrack() },
                                    modifier = Modifier.testTag("hud_prev_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.SkipPrevious,
                                        contentDescription = stringResource(R.string.action_previous),
                                        tint = Color.White
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(BioluminescentCyan)
                                        .clickable {
                                            if (allTracks.isEmpty() && playbackState.currentTrack == null) {
                                                viewModel.openPanel(OverlayPanel.MUSIC)
                                            } else {
                                                viewModel.togglePlayPause()
                                            }
                                        }
                                        .testTag("hud_play_pause_button"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (playbackState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                        contentDescription = if (playbackState.isPlaying)
                                            stringResource(R.string.action_pause) else stringResource(R.string.action_play),
                                        tint = AbyssBlack
                                    )
                                }

                                IconButton(
                                    onClick = { viewModel.nextTrack() },
                                    modifier = Modifier.testTag("hud_next_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.SkipNext,
                                        contentDescription = stringResource(R.string.action_next),
                                        tint = Color.White
                                    )
                                }
                            }

                            val progress = if (playbackState.durationMs > 0L) {
                                (playbackState.positionMs.toFloat() / playbackState.durationMs.toFloat()).coerceIn(0f, 1f)
                            } else 0f

                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(2.5.dp),
                                color = BioluminescentCyan,
                                trackColor = Color.Transparent
                            )
                        }
                    }

                    // Minimal 4-Destination Sanctuary Dock
                    GlassPillSurface(
                        cornerRadius = 26.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            DockActionItem(
                                icon = Icons.Default.Public,
                                label = stringResource(R.string.action_worlds),
                                tag = "dock_worlds_button",
                                onClick = { viewModel.openPanel(OverlayPanel.WORLDS) }
                            )
                            DockActionItem(
                                icon = Icons.Default.SetMeal,
                                label = stringResource(R.string.action_aquarium),
                                tag = "dock_aquarium_button",
                                onClick = { viewModel.openPanel(OverlayPanel.AQUARIUM) }
                            )
                            DockActionItem(
                                icon = Icons.Default.LibraryMusic,
                                label = stringResource(R.string.action_music),
                                tag = "dock_music_button",
                                onClick = { viewModel.openPanel(OverlayPanel.MUSIC) }
                            )
                            DockActionItem(
                                icon = Icons.Default.Tune,
                                label = stringResource(R.string.action_settings),
                                tag = "dock_settings_button",
                                onClick = { viewModel.openPanel(OverlayPanel.SETTINGS) }
                            )
                        }
                    }
                }
            }
        }

        // 3. Brief Ambient Mode Hint
        AnimatedVisibility(
            visible = showAmbientHint,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 28.dp)
        ) {
            GlassPillSurface(cornerRadius = 20.dp) {
                Text(
                    text = stringResource(R.string.action_exit_ambient),
                    style = MaterialTheme.typography.bodySmall,
                    color = MistSilver,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }

        // 4. Floating Status Feedback Toast
        AnimatedVisibility(
            visible = statusBanner != null,
            enter = fadeIn() + slideInVertically { -it / 2 },
            exit = fadeOut() + slideOutVertically { -it / 2 },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 74.dp)
        ) {
            GlassPillSurface(
                cornerRadius = 18.dp,
                containerColor = PelagicGlass,
                borderColor = BioluminescentCyan.copy(alpha = 0.45f)
            ) {
                Text(
                    text = statusBanner.orEmpty(),
                    style = MaterialTheme.typography.labelLarge,
                    color = BioluminescentCyan,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }

        // 5. Dimmed Backdrop when a Sheet is Open
        AnimatedVisibility(
            visible = activePanel != OverlayPanel.NONE,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x6602080E))
                    .clickable { viewModel.closePanel() }
            )
        }

        // 6. Dedicated Overlay Sheets (Worlds, Aquarium/Fish Studio, Music Player, Settings)
        AnimatedVisibility(
            visible = activePanel != OverlayPanel.NONE,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .widthIn(max = 680.dp)
                .fillMaxWidth()
        ) {
            when (activePanel) {
                OverlayPanel.WORLDS -> WorldSelectorSheet(
                    currentWorld = currentWorld,
                    onSelectWorld = viewModel::selectWorld,
                    onClose = viewModel::closePanel
                )

                OverlayPanel.AQUARIUM -> FishSelectorSheet(
                    currentWorld = currentWorld,
                    activeRoster = activeRoster,
                    activeAquariumName = activeAquariumName,
                    savedAquariums = savedAquariums,
                    settings = settings,
                    onAddSpecies = { viewModel.addSpecies(it) },
                    onUpdateCount = viewModel::updateSpeciesCount,
                    onToggleEnabled = viewModel::toggleSpeciesEnabled,
                    onRemoveSpecies = viewModel::removeSpecies,
                    onResetDefault = viewModel::resetToWorldDefaultRoster,
                    onSaveAquarium = viewModel::saveCurrentAquarium,
                    onLoadAquarium = viewModel::loadAquarium,
                    onOverwriteAquarium = viewModel::overwriteAquarium,
                    onRenameAquarium = viewModel::renameAquarium,
                    onDeleteAquarium = viewModel::deleteAquarium,
                    onToggleOverrideCompatibility = viewModel::setOverrideCompatibility,
                    onClose = viewModel::closePanel
                )

                OverlayPanel.MUSIC -> MusicLibrarySheet(
                    playbackState = playbackState,
                    audioSpectrum = audioSpectrum,
                    allTracks = allTracks,
                    favoriteTracks = favoriteTracks,
                    recentTracks = recentTracks,
                    playlists = playlists,
                    selectedPlaylistId = selectedPlaylistId,
                    selectedPlaylistTracks = selectedPlaylistTracks,
                    isImporting = isImportingMusic,
                    onImportFiles = onRequestImportFiles,
                    onImportFolder = onRequestImportFolder,
                    onPlayTracks = viewModel::playTracks,
                    onTogglePlayPause = viewModel::togglePlayPause,
                    onNext = viewModel::nextTrack,
                    onPrevious = viewModel::previousTrack,
                    onSeek = viewModel::seekTo,
                    onVolumeChange = viewModel::setVolume,
                    onToggleShuffle = viewModel::toggleShuffle,
                    onCycleRepeat = viewModel::cycleRepeatMode,
                    onToggleFavorite = viewModel::toggleFavorite,
                    onDeleteTrack = viewModel::deleteTrack,
                    onCreatePlaylist = viewModel::createPlaylist,
                    onDeletePlaylist = viewModel::deletePlaylist,
                    onSelectPlaylist = viewModel::selectPlaylist,
                    onAddTrackToPlaylist = viewModel::addTrackToPlaylist,
                    onRemoveTrackFromPlaylist = viewModel::removeTrackFromPlaylist,
                    onClose = viewModel::closePanel
                )

                OverlayPanel.SETTINGS -> SettingsSheet(
                    settings = settings,
                    audioSpectrum = audioSpectrum,
                    onPerformanceModeChange = viewModel::setPerformanceMode,
                    onAmbientModeChange = viewModel::setAmbientMode,
                    onFullscreenChange = viewModel::setFullscreenMode,
                    onKeepScreenOnChange = viewModel::setKeepScreenOn,
                    onReducedMotionChange = viewModel::setReducedMotion,
                    onHapticChange = viewModel::setHapticFeedback,
                    onAudioReactivityChange = viewModel::setAudioReactivity,
                    onOverrideCompatibilityChange = viewModel::setOverrideCompatibility,
                    onClose = viewModel::closePanel
                )

                OverlayPanel.NONE -> Unit
            }
        }
    }
}

@Composable
private fun DockActionItem(
    icon: ImageVector,
    label: String,
    tag: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
            .testTag(tag),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = Color.White,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MistSilver
        )
    }
}
