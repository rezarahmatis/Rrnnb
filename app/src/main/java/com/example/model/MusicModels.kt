package com.example.model

data class Track(
    val id: Long = 0L,
    val uri: String,
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Long,
    val mimeType: String = "",
    val artworkPath: String? = null,
    val isFavorite: Boolean = false,
    val lastPlayedAt: Long = 0L,
    val dateAdded: Long = System.currentTimeMillis()
)

data class Playlist(
    val id: Long = 0L,
    val name: String,
    val trackCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

enum class RepeatMode {
    OFF, ALL, ONE
}

data class PlaybackState(
    val currentTrack: Track? = null,
    val isPlaying: Boolean = false,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val volume: Float = 1.0f,
    val shuffleEnabled: Boolean = false,
    val repeatMode: RepeatMode = RepeatMode.OFF,
    val queueSize: Int = 0
)

data class AudioSpectrum(
    val bass: Float = 0f,
    val mid: Float = 0f,
    val treble: Float = 0f,
    val energy: Float = 0f,
    val beatPulse: Float = 0f,
    val isAnalyzing: Boolean = false
)
