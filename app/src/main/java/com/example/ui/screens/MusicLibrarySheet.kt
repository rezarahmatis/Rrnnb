package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.R
import com.example.model.AudioSpectrum
import com.example.model.PlaybackState
import com.example.model.Playlist
import com.example.model.RepeatMode
import com.example.model.Track
import com.example.ui.components.AudioHarmonicIndicator
import com.example.ui.components.formatDurationMs
import com.example.ui.theme.AbyssBlack
import com.example.ui.theme.BioluminescentCyan
import com.example.ui.theme.DeepTrenchSurface
import com.example.ui.theme.MistSilver
import com.example.ui.theme.PelagicCard
import com.example.ui.theme.PelagicCardElevated
import com.example.ui.theme.SoftErrorRose
import com.example.ui.theme.SubtleGlassStroke
import com.example.ui.theme.WarmCoralGold
import java.io.File

private enum class LibraryTab(val label: String) {
    ALL("Library"),
    FAVORITES("Favorites"),
    RECENT("Recent"),
    PLAYLISTS("Playlists")
}

@Composable
fun MusicLibrarySheet(
    playbackState: PlaybackState,
    audioSpectrum: AudioSpectrum,
    allTracks: List<Track>,
    favoriteTracks: List<Track>,
    recentTracks: List<Track>,
    playlists: List<Playlist>,
    selectedPlaylistId: Long?,
    selectedPlaylistTracks: List<Track>,
    isImporting: Boolean,
    onImportFiles: () -> Unit,
    onImportFolder: () -> Unit,
    onPlayTracks: (List<Track>, Int) -> Unit,
    onTogglePlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSeek: (Long) -> Unit,
    onVolumeChange: (Float) -> Unit,
    onToggleShuffle: () -> Unit,
    onCycleRepeat: () -> Unit,
    onToggleFavorite: (Track) -> Unit,
    onDeleteTrack: (Long) -> Unit,
    onCreatePlaylist: (String) -> Unit,
    onDeletePlaylist: (Long) -> Unit,
    onSelectPlaylist: (Long?) -> Unit,
    onAddTrackToPlaylist: (Long, Long) -> Unit,
    onRemoveTrackFromPlaylist: (Long, Long) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var activeTab by remember { mutableStateOf(LibraryTab.ALL) }
    var searchQuery by remember { mutableStateOf("") }
    var showCreatePlaylistDialog by remember { mutableStateOf(false) }
    var trackPendingPlaylistAdd by remember { mutableStateOf<Track?>(null) }

    val visibleTracks = remember(activeTab, allTracks, favoriteTracks, recentTracks, selectedPlaylistTracks, searchQuery) {
        val source = when (activeTab) {
            LibraryTab.ALL -> allTracks
            LibraryTab.FAVORITES -> favoriteTracks
            LibraryTab.RECENT -> recentTracks
            LibraryTab.PLAYLISTS -> selectedPlaylistTracks
        }
        if (searchQuery.isBlank()) source else source.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
                it.artist.contains(searchQuery, ignoreCase = true) ||
                it.album.contains(searchQuery, ignoreCase = true)
        }
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight(0.90f),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        color = DeepTrenchSurface.copy(alpha = 0.97f),
        border = BorderStroke(1.dp, SubtleGlassStroke)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Aquatic Music Player",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Offline Media3 • Real-time audio-reactive water & life",
                        style = MaterialTheme.typography.bodySmall,
                        color = MistSilver
                    )
                }
                IconButton(
                    onClick = onClose,
                    modifier = Modifier.testTag("close_music_sheet_button")
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close music player")
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                // 1. Now Playing Control Deck
                item(key = "now_playing_card") {
                    NowPlayingDeck(
                        state = playbackState,
                        spectrum = audioSpectrum,
                        onTogglePlayPause = onTogglePlayPause,
                        onNext = onNext,
                        onPrevious = onPrevious,
                        onSeek = onSeek,
                        onVolumeChange = onVolumeChange,
                        onToggleShuffle = onToggleShuffle,
                        onCycleRepeat = onCycleRepeat,
                        onToggleFavorite = { playbackState.currentTrack?.let(onToggleFavorite) }
                    )
                }

                // 2. Local SAF Import Actions
                item(key = "import_actions") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        FilledTonalButton(
                            onClick = onImportFiles,
                            enabled = !isImporting,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("import_audio_files_button"),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = BioluminescentCyan.copy(alpha = 0.18f),
                                contentColor = BioluminescentCyan
                            )
                        ) {
                            if (isImporting) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = BioluminescentCyan
                                )
                            } else {
                                Icon(Icons.Default.AudioFile, contentDescription = null, modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Import Files", maxLines = 1)
                        }
                        OutlinedButton(
                            onClick = onImportFolder,
                            enabled = !isImporting,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("import_music_folder_button"),
                            border = BorderStroke(1.dp, SubtleGlassStroke)
                        ) {
                            Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Import Folder", maxLines = 1)
                        }
                    }
                }

                // 3. Library Tabs + Search
                item(key = "tabs_and_search") {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(LibraryTab.entries) { tab ->
                                val count = when (tab) {
                                    LibraryTab.ALL -> allTracks.size
                                    LibraryTab.FAVORITES -> favoriteTracks.size
                                    LibraryTab.RECENT -> recentTracks.size
                                    LibraryTab.PLAYLISTS -> playlists.size
                                }
                                FilterChip(
                                    selected = activeTab == tab,
                                    onClick = {
                                        activeTab = tab
                                        if (tab != LibraryTab.PLAYLISTS) onSelectPlaylist(null)
                                    },
                                    label = { Text("${tab.label} ($count)") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = BioluminescentCyan.copy(alpha = 0.22f),
                                        selectedLabelColor = BioluminescentCyan
                                    )
                                )
                            }
                        }

                        if (activeTab != LibraryTab.PLAYLISTS || selectedPlaylistId != null) {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("music_search_input"),
                                placeholder = { Text("Search songs, artists, albums…", color = MistSilver) },
                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MistSilver) },
                                trailingIcon = if (searchQuery.isNotEmpty()) {
                                    {
                                        IconButton(onClick = { searchQuery = "" }) {
                                            Icon(Icons.Default.Close, contentDescription = "Clear search", tint = MistSilver)
                                        }
                                    }
                                } else null,
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = BioluminescentCyan,
                                    unfocusedBorderColor = SubtleGlassStroke,
                                    focusedContainerColor = PelagicCard.copy(alpha = 0.7f),
                                    unfocusedContainerColor = PelagicCard.copy(alpha = 0.5f)
                                )
                            )
                        }
                    }
                }

                // 4. Playlist List OR Selected Playlist Header
                if (activeTab == LibraryTab.PLAYLISTS && selectedPlaylistId == null) {
                    item(key = "create_playlist_row") {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Your Playlists", style = MaterialTheme.typography.titleMedium)
                            TextButton(
                                onClick = { showCreatePlaylistDialog = true },
                                modifier = Modifier.testTag("create_playlist_button")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("New Playlist")
                            }
                        }
                    }

                    if (playlists.isEmpty()) {
                        item(key = "empty_playlists") {
                            EmptyLibraryCard(
                                title = "No playlists yet",
                                subtitle = "Create curated soundscapes for koi ponds, coral reefs, or midnight deep-sea sessions."
                            )
                        }
                    } else {
                        items(playlists, key = { "pl_${it.id}" }) { playlist ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSelectPlaylist(playlist.id) },
                                shape = RoundedCornerShape(16.dp),
                                color = PelagicCard,
                                border = BorderStroke(1.dp, SubtleGlassStroke)
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(BioluminescentCyan.copy(alpha = 0.16f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.AutoMirrored.Filled.QueueMusic,
                                            contentDescription = null,
                                            tint = BioluminescentCyan
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(playlist.name, style = MaterialTheme.typography.titleSmall)
                                        Text(
                                            "${playlist.trackCount} ${if (playlist.trackCount == 1) "track" else "tracks"}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MistSilver
                                        )
                                    }
                                    IconButton(onClick = { onDeletePlaylist(playlist.id) }) {
                                        Icon(Icons.Default.DeleteOutline, contentDescription = "Delete playlist", tint = MistSilver)
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // If inside a selected playlist, show back header
                    if (activeTab == LibraryTab.PLAYLISTS && selectedPlaylistId != null) {
                        val activePl = playlists.firstOrNull { it.id == selectedPlaylistId }
                        item(key = "playlist_detail_header") {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(onClick = { onSelectPlaylist(null) }) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back to playlists")
                                }
                                Text(
                                    text = activePl?.name ?: "Playlist",
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.weight(1f)
                                )
                                if (visibleTracks.isNotEmpty()) {
                                    TextButton(onClick = { onPlayTracks(visibleTracks, 0) }) {
                                        Text("Play All")
                                    }
                                }
                            }
                        }
                    }

                    if (visibleTracks.isEmpty()) {
                        item(key = "empty_tracks") {
                            EmptyLibraryCard(
                                title = if (allTracks.isEmpty()) stringResource(R.string.empty_music_title) else "No matching tracks",
                                subtitle = if (allTracks.isEmpty()) stringResource(R.string.empty_music_subtitle) else "Try clearing your filter or importing more local MP3, WAV, OGG, or M4A tracks."
                            )
                        }
                    } else {
                        itemsIndexed(visibleTracks, key = { _, t -> "track_${t.id}" }) { index, track ->
                            val isCurrent = playbackState.currentTrack?.id == track.id
                            TrackRowItem(
                                track = track,
                                isCurrent = isCurrent,
                                isPlaying = isCurrent && playbackState.isPlaying,
                                isInPlaylistView = activeTab == LibraryTab.PLAYLISTS && selectedPlaylistId != null,
                                onPlay = { onPlayTracks(visibleTracks, index) },
                                onToggleFavorite = { onToggleFavorite(track) },
                                onAddToPlaylist = { trackPendingPlaylistAdd = track },
                                onRemove = {
                                    if (activeTab == LibraryTab.PLAYLISTS && selectedPlaylistId != null) {
                                        onRemoveTrackFromPlaylist(selectedPlaylistId, track.id)
                                    } else {
                                        onDeleteTrack(track.id)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showCreatePlaylistDialog) {
        var plName by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showCreatePlaylistDialog = false },
            containerColor = DeepTrenchSurface,
            title = { Text("New Playlist", style = MaterialTheme.typography.titleLarge) },
            text = {
                OutlinedTextField(
                    value = plName,
                    onValueChange = { plName = it },
                    singleLine = true,
                    label = { Text("Playlist name") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onCreatePlaylist(plName)
                        showCreatePlaylistDialog = false
                    },
                    enabled = plName.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BioluminescentCyan,
                        contentColor = AbyssBlack
                    )
                ) { Text("Create") }
            },
            dismissButton = {
                TextButton(onClick = { showCreatePlaylistDialog = false }) { Text("Cancel") }
            }
        )
    }

    trackPendingPlaylistAdd?.let { track ->
        AlertDialog(
            onDismissRequest = { trackPendingPlaylistAdd = null },
            containerColor = DeepTrenchSurface,
            title = { Text("Add to Playlist", style = MaterialTheme.typography.titleLarge) },
            text = {
                if (playlists.isEmpty()) {
                    Text(
                        "Create a playlist in the Playlists tab first to organize your local tracks.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MistSilver
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        playlists.forEach { pl ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onAddTrackToPlaylist(pl.id, track.id)
                                        trackPendingPlaylistAdd = null
                                    },
                                shape = RoundedCornerShape(12.dp),
                                color = PelagicCard
                            ) {
                                Text(
                                    text = pl.name,
                                    style = MaterialTheme.typography.titleSmall,
                                    modifier = Modifier.padding(14.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { trackPendingPlaylistAdd = null }) { Text("Done") }
            }
        )
    }
}

@Composable
private fun NowPlayingDeck(
    state: PlaybackState,
    spectrum: AudioSpectrum,
    onTogglePlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSeek: (Long) -> Unit,
    onVolumeChange: (Float) -> Unit,
    onToggleShuffle: () -> Unit,
    onCycleRepeat: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    val track = state.currentTrack
    val duration = state.durationMs.coerceAtLeast(1L)
    val progress = (state.positionMs.toFloat() / duration.toFloat()).coerceIn(0f, 1f)

    Surface(
        shape = RoundedCornerShape(22.dp),
        color = PelagicCardElevated,
        border = BorderStroke(1.dp, SubtleGlassStroke)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Album Art Thumbnail
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(BioluminescentCyan.copy(alpha = 0.28f), DeepTrenchSurface)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    val artFile = track?.artworkPath?.let { File(it) }?.takeIf { it.exists() }
                    if (artFile != null) {
                        AsyncImage(
                            model = artFile,
                            contentDescription = "Album artwork",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = BioluminescentCyan,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = track?.title ?: "No Track Playing",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = track?.let {
                            if (it.album.isNotBlank()) "${it.artist} • ${it.album}" else it.artist
                        } ?: "Import local music below",
                        style = MaterialTheme.typography.bodySmall,
                        color = MistSilver,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    AudioHarmonicIndicator(spectrum = spectrum, isPlaying = state.isPlaying)
                }

                if (track != null) {
                    IconButton(
                        onClick = onToggleFavorite,
                        modifier = Modifier.testTag("now_playing_favorite_button")
                    ) {
                        Icon(
                            imageVector = if (track.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Toggle favorite",
                            tint = if (track.isFavorite) WarmCoralGold else MistSilver
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Scrubber
            Slider(
                value = progress,
                onValueChange = { frac -> onSeek((frac * duration).toLong()) },
                enabled = track != null && state.durationMs > 0L,
                colors = SliderDefaults.colors(
                    thumbColor = BioluminescentCyan,
                    activeTrackColor = BioluminescentCyan,
                    inactiveTrackColor = SubtleGlassStroke
                )
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = formatDurationMs(state.positionMs),
                    style = MaterialTheme.typography.labelSmall,
                    color = MistSilver
                )
                Text(
                    text = formatDurationMs(state.durationMs),
                    style = MaterialTheme.typography.labelSmall,
                    color = MistSilver
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Transport Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onToggleShuffle,
                    modifier = Modifier.testTag("shuffle_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Shuffle,
                        contentDescription = "Toggle shuffle",
                        tint = if (state.shuffleEnabled) BioluminescentCyan else MistSilver
                    )
                }
                IconButton(
                    onClick = onPrevious,
                    modifier = Modifier.testTag("sheet_prev_button")
                ) {
                    Icon(Icons.Default.SkipPrevious, contentDescription = "Previous track", modifier = Modifier.size(30.dp))
                }
                FilledIconButton(
                    onClick = onTogglePlayPause,
                    modifier = Modifier
                        .size(56.dp)
                        .testTag("sheet_play_pause_button"),
                    shape = CircleShape,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = BioluminescentCyan,
                        contentColor = AbyssBlack
                    )
                ) {
                    Icon(
                        imageVector = if (state.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (state.isPlaying) "Pause" else "Play",
                        modifier = Modifier.size(30.dp)
                    )
                }
                IconButton(
                    onClick = onNext,
                    modifier = Modifier.testTag("sheet_next_button")
                ) {
                    Icon(Icons.Default.SkipNext, contentDescription = "Next track", modifier = Modifier.size(30.dp))
                }
                IconButton(
                    onClick = onCycleRepeat,
                    modifier = Modifier.testTag("repeat_button")
                ) {
                    Icon(
                        imageVector = if (state.repeatMode == RepeatMode.ONE) Icons.Default.RepeatOne else Icons.Default.Repeat,
                        contentDescription = "Cycle repeat mode",
                        tint = if (state.repeatMode != RepeatMode.OFF) BioluminescentCyan else MistSilver
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Volume Slider
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                    contentDescription = "Playback volume",
                    tint = MistSilver,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Slider(
                    value = state.volume,
                    onValueChange = onVolumeChange,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("volume_slider"),
                    colors = SliderDefaults.colors(
                        thumbColor = MistSilver,
                        activeTrackColor = MistSilver,
                        inactiveTrackColor = SubtleGlassStroke
                    )
                )
            }
        }
    }
}

@Composable
private fun TrackRowItem(
    track: Track,
    isCurrent: Boolean,
    isPlaying: Boolean,
    isInPlaylistView: Boolean,
    onPlay: () -> Unit,
    onToggleFavorite: () -> Unit,
    onAddToPlaylist: () -> Unit,
    onRemove: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onPlay)
            .testTag("track_row_${track.id}"),
        shape = RoundedCornerShape(14.dp),
        color = if (isCurrent) BioluminescentCyan.copy(alpha = 0.14f) else PelagicCard.copy(alpha = 0.78f),
        border = BorderStroke(1.dp, if (isCurrent) BioluminescentCyan.copy(alpha = 0.45f) else SubtleGlassStroke)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(PelagicCardElevated),
                contentAlignment = Alignment.Center
            ) {
                val artFile = track.artworkPath?.let { File(it) }?.takeIf { it.exists() }
                if (artFile != null) {
                    AsyncImage(
                        model = artFile,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = if (isCurrent) BioluminescentCyan else MistSilver,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = track.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = if (isCurrent) BioluminescentCyan else MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${track.artist} • ${formatDurationMs(track.durationMs)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MistSilver,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            IconButton(onClick = onToggleFavorite) {
                Icon(
                    imageVector = if (track.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Favorite ${track.title}",
                    tint = if (track.isFavorite) WarmCoralGold else MistSilver,
                    modifier = Modifier.size(20.dp)
                )
            }

            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Track options", tint = MistSilver)
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Add to Playlist") },
                        leadingIcon = { Icon(Icons.AutoMirrored.Filled.PlaylistAdd, contentDescription = null) },
                        onClick = { menuExpanded = false; onAddToPlaylist() }
                    )
                    DropdownMenuItem(
                        text = { Text(if (isInPlaylistView) "Remove from Playlist" else "Remove from Library", color = SoftErrorRose) },
                        leadingIcon = { Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = SoftErrorRose) },
                        onClick = { menuExpanded = false; onRemove() }
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyLibraryCard(title: String, subtitle: String) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = PelagicCard,
        border = BorderStroke(1.dp, SubtleGlassStroke)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.MusicNote,
                contentDescription = null,
                tint = BioluminescentCyan.copy(alpha = 0.72f),
                modifier = Modifier.size(34.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(title, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MistSilver
            )
        }
    }
}
