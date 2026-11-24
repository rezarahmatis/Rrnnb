package com.example.audio

import android.content.ComponentName
import android.content.Context
import android.net.Uri
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.model.PlaybackState
import com.example.model.RepeatMode
import com.example.model.Track
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File

/**
 * Reactive controller wrapping AndroidX Media3 [MediaController] bound to [PlaybackService].
 */
class MusicPlayerController(
    private val context: Context,
    private val onTrackStarted: (Long) -> Unit
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var controller: MediaController? = null
    private var positionJob: Job? = null

    private var currentQueue: List<Track> = emptyList()

    private val _playbackState = MutableStateFlow(PlaybackState())
    val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            syncState()
            if (isPlaying) startPositionPolling() else stopPositionPolling()
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            syncState()
            val trackId = mediaItem?.mediaId?.toLongOrNull()
            if (trackId != null) {
                onTrackStarted(trackId)
            }
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            syncState()
        }

        override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
            syncState()
        }

        override fun onRepeatModeChanged(repeatMode: Int) {
            syncState()
        }
    }

    fun connect() {
        if (controllerFuture != null) return
        val sessionToken = SessionToken(context, ComponentName(context, PlaybackService::class.java))
        val future = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture = future
        future.addListener(
            {
                runCatching {
                    val ctrl = future.get()
                    controller = ctrl
                    ctrl.addListener(playerListener)
                    syncState()
                    if (ctrl.isPlaying) startPositionPolling()
                }
            },
            ContextCompat.getMainExecutor(context)
        )
    }

    fun playQueue(tracks: List<Track>, startIndex: Int = 0) {
        if (tracks.isEmpty()) return
        currentQueue = tracks
        val safeIndex = startIndex.coerceIn(0, tracks.lastIndex)
        val mediaItems = tracks.map { track ->
            val metadataBuilder = MediaMetadata.Builder()
                .setTitle(track.title)
                .setArtist(track.artist)
                .setAlbumTitle(track.album)
            if (!track.artworkPath.isNullOrBlank()) {
                val artFile = File(track.artworkPath)
                if (artFile.exists()) {
                    metadataBuilder.setArtworkUri(Uri.fromFile(artFile))
                }
            }
            MediaItem.Builder()
                .setMediaId(track.id.toString())
                .setUri(Uri.parse(track.uri))
                .setMediaMetadata(metadataBuilder.build())
                .build()
        }

        val ctrl = controller
        if (ctrl != null) {
            ctrl.setMediaItems(mediaItems, safeIndex, 0L)
            ctrl.prepare()
            ctrl.play()
            onTrackStarted(tracks[safeIndex].id)
            syncState()
        } else {
            connect()
            scope.launch {
                repeat(20) {
                    delay(120L)
                    val ready = controller
                    if (ready != null) {
                        ready.setMediaItems(mediaItems, safeIndex, 0L)
                        ready.prepare()
                        ready.play()
                        onTrackStarted(tracks[safeIndex].id)
                        syncState()
                        return@launch
                    }
                }
            }
        }
    }

    fun togglePlayPause() {
        val ctrl = controller ?: return
        if (ctrl.isPlaying) {
            ctrl.pause()
        } else {
            if (ctrl.mediaItemCount > 0) {
                ctrl.play()
            }
        }
        syncState()
    }

    fun next() {
        val ctrl = controller ?: return
        if (ctrl.hasNextMediaItem()) {
            ctrl.seekToNextMediaItem()
        } else if (ctrl.mediaItemCount > 0) {
            ctrl.seekTo(0, 0L)
        }
        ctrl.play()
        syncState()
    }

    fun previous() {
        val ctrl = controller ?: return
        if (ctrl.currentPosition > 3500L) {
            ctrl.seekTo(0L)
        } else if (ctrl.hasPreviousMediaItem()) {
            ctrl.seekToPreviousMediaItem()
        } else if (ctrl.mediaItemCount > 0) {
            ctrl.seekTo(ctrl.mediaItemCount - 1, 0L)
        }
        syncState()
    }

    fun seekTo(positionMs: Long) {
        val ctrl = controller ?: return
        ctrl.seekTo(positionMs.coerceAtLeast(0L))
        _playbackState.update { it.copy(positionMs = positionMs.coerceAtLeast(0L)) }
    }

    fun setVolume(volume: Float) {
        val clamped = volume.coerceIn(0f, 1f)
        controller?.volume = clamped
        _playbackState.update { it.copy(volume = clamped) }
    }

    fun toggleShuffle() {
        val ctrl = controller ?: return
        ctrl.shuffleModeEnabled = !ctrl.shuffleModeEnabled
        syncState()
    }

    fun cycleRepeatMode() {
        val ctrl = controller ?: return
        val nextMode = when (ctrl.repeatMode) {
            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
            Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
            else -> Player.REPEAT_MODE_OFF
        }
        ctrl.repeatMode = nextMode
        syncState()
    }

    fun updateTrackMetadataInQueue(updatedTracks: List<Track>) {
        if (currentQueue.isEmpty()) return
        val byId = updatedTracks.associateBy { it.id }
        currentQueue = currentQueue.map { byId[it.id] ?: it }
        syncState()
    }

    private fun startPositionPolling() {
        positionJob?.cancel()
        positionJob = scope.launch {
            while (isActive) {
                val ctrl = controller
                if (ctrl != null) {
                    _playbackState.update {
                        it.copy(
                            positionMs = ctrl.currentPosition.coerceAtLeast(0L),
                            durationMs = ctrl.duration.coerceAtLeast(it.currentTrack?.durationMs ?: 0L)
                        )
                    }
                }
                delay(400L)
            }
        }
    }

    private fun stopPositionPolling() {
        positionJob?.cancel()
        positionJob = null
    }

    private fun syncState() {
        val ctrl = controller ?: return
        val mediaId = ctrl.currentMediaItem?.mediaId?.toLongOrNull()
        val activeTrack = currentQueue.firstOrNull { it.id == mediaId }
            ?: ctrl.currentMediaItem?.let { item ->
                Track(
                    id = mediaId ?: 0L,
                    uri = item.localConfiguration?.uri?.toString().orEmpty(),
                    title = item.mediaMetadata.title?.toString().orEmpty().ifBlank { "Untitled Track" },
                    artist = item.mediaMetadata.artist?.toString().orEmpty().ifBlank { "Unknown Artist" },
                    album = item.mediaMetadata.albumTitle?.toString().orEmpty(),
                    durationMs = ctrl.duration.coerceAtLeast(0L)
                )
            }

        val repeat = when (ctrl.repeatMode) {
            Player.REPEAT_MODE_ALL -> RepeatMode.ALL
            Player.REPEAT_MODE_ONE -> RepeatMode.ONE
            else -> RepeatMode.OFF
        }

        _playbackState.value = PlaybackState(
            currentTrack = activeTrack,
            isPlaying = ctrl.isPlaying,
            positionMs = ctrl.currentPosition.coerceAtLeast(0L),
            durationMs = ctrl.duration.coerceAtLeast(activeTrack?.durationMs ?: 0L),
            volume = ctrl.volume,
            shuffleEnabled = ctrl.shuffleModeEnabled,
            repeatMode = repeat,
            queueSize = ctrl.mediaItemCount
        )
    }

    fun release() {
        stopPositionPolling()
        controller?.removeListener(playerListener)
        controllerFuture?.let { MediaController.releaseFuture(it) }
        controller = null
        controllerFuture = null
    }
}
