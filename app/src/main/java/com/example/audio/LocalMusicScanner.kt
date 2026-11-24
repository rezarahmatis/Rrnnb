package com.example.audio

import android.content.Context
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.DocumentsContract
import android.provider.OpenableColumns
import com.example.model.Track
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

/**
 * Offline local music scanner using Android Storage Access Framework (SAF).
 * Imports individual/multiple audio files (MP3, WAV, OGG, M4A, AAC, FLAC, OPUS)
 * or recursively scans a user-chosen local music folder with zero broad storage permissions.
 */
class LocalMusicScanner(private val context: Context) {

    private val supportedExtensions = setOf("mp3", "wav", "ogg", "m4a", "aac", "flac", "opus")

    suspend fun importAudioFiles(uris: List<Uri>): List<Track> = withContext(Dispatchers.IO) {
        uris.mapNotNull { uri ->
            persistReadPermission(uri)
            extractTrack(uri)
        }
    }

    suspend fun importMusicFolder(treeUri: Uri): List<Track> = withContext(Dispatchers.IO) {
        persistReadPermission(treeUri)
        val rootDocId = DocumentsContract.getTreeDocumentId(treeUri)
        val results = mutableListOf<Track>()
        scanFolderRecursive(treeUri, rootDocId, results, depth = 0)
        results
    }

    private fun scanFolderRecursive(
        treeUri: Uri,
        parentDocId: String,
        out: MutableList<Track>,
        depth: Int
    ) {
        if (depth > 4 || out.size >= 400) return
        val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, parentDocId)
        val projection = arrayOf(
            DocumentsContract.Document.COLUMN_DOCUMENT_ID,
            DocumentsContract.Document.COLUMN_DISPLAY_NAME,
            DocumentsContract.Document.COLUMN_MIME_TYPE
        )
        runCatching {
            context.contentResolver.query(childrenUri, projection, null, null, null)?.use { cursor ->
                val idIdx = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DOCUMENT_ID)
                val nameIdx = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
                val mimeIdx = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_MIME_TYPE)

                while (cursor.moveToNext() && out.size < 400) {
                    val docId = if (idIdx >= 0) cursor.getString(idIdx) else continue
                    val name = if (nameIdx >= 0) cursor.getString(nameIdx).orEmpty() else ""
                    val mime = if (mimeIdx >= 0) cursor.getString(mimeIdx).orEmpty() else ""

                    if (mime == DocumentsContract.Document.MIME_TYPE_DIR) {
                        scanFolderRecursive(treeUri, docId, out, depth + 1)
                    } else if (isSupportedAudio(mime, name)) {
                        val docUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, docId)
                        extractTrack(docUri, fallbackName = name, fallbackMime = mime)?.let { out.add(it) }
                    }
                }
            }
        }
    }

    private fun isSupportedAudio(mime: String, fileName: String): Boolean {
        if (mime.startsWith("audio/")) return true
        val ext = fileName.substringAfterLast('.', "").lowercase()
        return ext in supportedExtensions
    }

    private fun persistReadPermission(uri: Uri) {
        runCatching {
            context.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        }
    }

    private fun extractTrack(
        uri: Uri,
        fallbackName: String? = null,
        fallbackMime: String? = null
    ): Track? {
        val displayName = fallbackName ?: queryDisplayName(uri) ?: uri.lastPathSegment.orEmpty()
        val mime = fallbackMime ?: context.contentResolver.getType(uri).orEmpty()
        if (!isSupportedAudio(mime, displayName) && mime.isNotBlank()) return null

        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(context, uri)
            val rawTitle = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
            val rawArtist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
            val rawAlbum = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)
            val durationMs = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                ?.toLongOrNull() ?: 0L

            val cleanTitle = rawTitle?.trim()?.takeIf { it.isNotBlank() }
                ?: displayName.substringBeforeLast('.').ifBlank { "Untitled Audio" }
            val cleanArtist = rawArtist?.trim()?.takeIf { it.isNotBlank() } ?: "Local Audio"
            val cleanAlbum = rawAlbum?.trim().orEmpty()

            val artworkPath = retriever.embeddedPicture?.let { bytes ->
                saveArtworkCache(uri.toString(), bytes)
            }

            Track(
                uri = uri.toString(),
                title = cleanTitle,
                artist = cleanArtist,
                album = cleanAlbum,
                durationMs = durationMs,
                mimeType = mime,
                artworkPath = artworkPath
            )
        } catch (_: Exception) {
            // Fallback even if MediaMetadataRetriever fails on uncommon container
            Track(
                uri = uri.toString(),
                title = displayName.substringBeforeLast('.').ifBlank { "Untitled Audio" },
                artist = "Local Audio",
                album = "",
                durationMs = 0L,
                mimeType = mime
            )
        } finally {
            runCatching { retriever.release() }
        }
    }

    private fun queryDisplayName(uri: Uri): String? {
        return runCatching {
            context.contentResolver.query(
                uri,
                arrayOf(OpenableColumns.DISPLAY_NAME),
                null,
                null,
                null
            )?.use { cursor ->
                if (cursor.moveToFirst()) cursor.getString(0) else null
            }
        }.getOrNull()
    }

    private fun saveArtworkCache(uriKey: String, bytes: ByteArray): String? {
        return runCatching {
            val dir = File(context.filesDir, "artwork").apply { if (!exists()) mkdirs() }
            val file = File(dir, "art_${uriKey.hashCode().toUInt()}.jpg")
            if (!file.exists()) {
                FileOutputStream(file).use { it.write(bytes) }
            }
            file.absolutePath
        }.getOrNull()
    }
}
