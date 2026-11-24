package com.example.data.repository

import com.example.data.db.MusicDao
import com.example.data.db.PlaylistEntity
import com.example.data.db.PlaylistTrackCrossRef
import com.example.data.db.TrackEntity
import com.example.model.Playlist
import com.example.model.Track
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MusicRepository(private val dao: MusicDao) {

    val allTracks: Flow<List<Track>> = dao.observeAllTracks().map { list -> list.map { it.toDomain() } }

    val favoriteTracks: Flow<List<Track>> = dao.observeFavoriteTracks().map { list -> list.map { it.toDomain() } }

    val recentlyPlayedTracks: Flow<List<Track>> = dao.observeRecentlyPlayed().map { list -> list.map { it.toDomain() } }

    val playlists: Flow<List<Playlist>> = dao.observePlaylists().map { rows ->
        rows.map { Playlist(id = it.id, name = it.name, trackCount = it.trackCount, createdAt = it.createdAt) }
    }

    fun observePlaylistTracks(playlistId: Long): Flow<List<Track>> =
        dao.observePlaylistTracks(playlistId).map { list -> list.map { it.toDomain() } }

    suspend fun importTracks(tracks: List<Track>): Int {
        if (tracks.isEmpty()) return 0
        val entities = tracks.map {
            TrackEntity(
                uri = it.uri,
                title = it.title,
                artist = it.artist,
                album = it.album,
                durationMs = it.durationMs,
                mimeType = it.mimeType,
                artworkPath = it.artworkPath,
                isFavorite = it.isFavorite,
                lastPlayedAt = it.lastPlayedAt,
                dateAdded = it.dateAdded
            )
        }
        val insertedIds = dao.insertTracks(entities)
        return insertedIds.count { it > 0L }
    }

    suspend fun toggleFavorite(track: Track) {
        dao.setFavorite(track.id, !track.isFavorite)
    }

    suspend fun markTrackPlayed(trackId: Long) {
        dao.markPlayed(trackId)
    }

    suspend fun deleteTrack(trackId: Long) {
        dao.deleteTrack(trackId)
    }

    suspend fun createPlaylist(name: String): Long {
        return dao.insertPlaylist(PlaylistEntity(name = name.trim().ifBlank { "Untitled Playlist" }))
    }

    suspend fun deletePlaylist(playlistId: Long) {
        dao.deletePlaylist(playlistId)
    }

    suspend fun addTrackToPlaylist(playlistId: Long, trackId: Long) {
        dao.addTrackToPlaylist(PlaylistTrackCrossRef(playlistId = playlistId, trackId = trackId))
    }

    suspend fun removeTrackFromPlaylist(playlistId: Long, trackId: Long) {
        dao.removeTrackFromPlaylist(playlistId, trackId)
    }

    private fun TrackEntity.toDomain() = Track(
        id = id,
        uri = uri,
        title = title,
        artist = artist,
        album = album,
        durationMs = durationMs,
        mimeType = mimeType,
        artworkPath = artworkPath,
        isFavorite = isFavorite,
        lastPlayedAt = lastPlayedAt,
        dateAdded = dateAdded
    )
}
