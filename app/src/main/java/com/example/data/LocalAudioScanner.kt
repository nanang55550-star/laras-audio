package com.example.data

import android.content.ContentUris
import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.provider.OpenableColumns
import com.example.model.AudioFormat
import com.example.model.AudioTrack
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class LocalAudioScanner(private val context: Context) {

    suspend fun scanDeviceAudio(): List<AudioTrack> = withContext(Dispatchers.IO) {
        val tracks = mutableListOf<AudioTrack>()
        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }

        val projection = buildList {
            add(MediaStore.Audio.Media._ID)
            add(MediaStore.Audio.Media.TITLE)
            add(MediaStore.Audio.Media.ARTIST)
            add(MediaStore.Audio.Media.ALBUM)
            add(MediaStore.Audio.Media.ALBUM_ID)
            add(MediaStore.Audio.Media.DURATION)
            add(MediaStore.Audio.Media.MIME_TYPE)
            add(MediaStore.Audio.Media.DISPLAY_NAME)
            add(MediaStore.Audio.Media.DATE_MODIFIED)
            add(MediaStore.Audio.Media.DATE_ADDED)
            @Suppress("DEPRECATION")
            add(MediaStore.Audio.Media.DATA)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                add(MediaStore.Audio.Media.RELATIVE_PATH)
            }
            add(MediaStore.Audio.Media.IS_MUSIC)
        }.toTypedArray()

        val selection = "${MediaStore.Audio.Media.DURATION} > 1000"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

        runCatching {
            context.contentResolver.query(
                collection,
                projection,
                selection,
                null,
                sortOrder
            )?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val albumCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                val albumIdCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
                val durationCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val mimeCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE)
                val displayNameCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
                val dateModifiedCol = cursor.getColumnIndex(MediaStore.Audio.Media.DATE_MODIFIED)
                val dateAddedCol = cursor.getColumnIndex(MediaStore.Audio.Media.DATE_ADDED)
                @Suppress("DEPRECATION")
                val dataCol = cursor.getColumnIndex(MediaStore.Audio.Media.DATA)
                val relativePathCol = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) cursor.getColumnIndex(MediaStore.Audio.Media.RELATIVE_PATH) else -1

                val albumArtBase = Uri.parse("content://media/external/audio/albumart")

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idCol)
                    val title = cursor.getString(titleCol)?.takeIf { it.isNotBlank() } ?: "Tanpa Judul"
                    val rawArtist = cursor.getString(artistCol)
                    val artist = if (rawArtist.isNullOrBlank() || rawArtist == "<unknown>") "Artis Tidak Diketahui" else rawArtist
                    val rawAlbum = cursor.getString(albumCol)
                    val album = if (rawAlbum.isNullOrBlank() || rawAlbum == "<unknown>") "Album Lokal" else rawAlbum
                    val albumId = cursor.getLong(albumIdCol)
                    val duration = cursor.getLong(durationCol)
                    val mime = cursor.getString(mimeCol)
                    val displayName = cursor.getString(displayNameCol)
                    val dateModifiedSec = if (dateModifiedCol >= 0) cursor.getLong(dateModifiedCol) else 0L
                    val dateAddedSec = if (dateAddedCol >= 0) cursor.getLong(dateAddedCol) else 0L
                    val ext = displayName?.substringAfterLast('.', "")
                    val format = AudioFormat.fromExtensionOrMime(ext, mime)

                    val contentUri = ContentUris.withAppendedId(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        id
                    )
                    val artworkUri = if (albumId > 0L) {
                        ContentUris.withAppendedId(albumArtBase, albumId).toString()
                    } else null

                    val filePath = if (dataCol >= 0) cursor.getString(dataCol) else null
                    val relativePath = if (relativePathCol >= 0) cursor.getString(relativePathCol) else null
                    val dateAddedMillis = resolveTimestampMillis(
                        dateModifiedSec = dateModifiedSec,
                        dateAddedSec = dateAddedSec,
                        filePath = filePath
                    )

                    val folderSource = when {
                        !relativePath.isNullOrBlank() -> relativePath.trim().removeSuffix("/").ifBlank { "Penyimpanan Lokal" }
                        !filePath.isNullOrBlank() -> {
                            val file = File(filePath)
                            val parent = file.parentFile?.name
                            if (parent.isNullOrBlank() || parent.equals("0", ignoreCase = true) || parent.equals("emulated", ignoreCase = true)) {
                                "Penyimpanan Lokal"
                            } else {
                                parent
                            }
                        }
                        else -> "Penyimpanan Perangkat"
                    }

                    val sidecarLyrics = readSidecarLrcIfExists(filePath)

                    val qualityLabel = when (format) {
                        AudioFormat.FLAC -> "FLAC · Lossless"
                        AudioFormat.WAV -> "WAV · PCM"
                        AudioFormat.MP3 -> "MP3 · Audio"
                        AudioFormat.M4A -> "M4A · AAC"
                        AudioFormat.OGG -> "OGG · Vorbis/Opus"
                        AudioFormat.OTHER -> format.label
                    }

                    tracks.add(
                        AudioTrack(
                            id = "media_$id",
                            title = title,
                            artist = artist,
                            album = album,
                            durationMs = duration,
                            uriString = contentUri.toString(),
                            format = format,
                            qualityLabel = qualityLabel,
                            artworkUri = artworkUri,
                            isBundled = false,
                            lyricsRaw = sidecarLyrics,
                            folderSource = folderSource,
                            dateAddedMillis = dateAddedMillis
                        )
                    )
                }
            }
        }
        tracks
    }

    suspend fun extractImportedAudioTrack(uri: Uri): AudioTrack? = withContext(Dispatchers.IO) {
        runCatching {
            val resolver = context.contentResolver
            var displayName: String? = null
            resolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { c ->
                if (c.moveToFirst()) {
                    val idx = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (idx >= 0) displayName = c.getString(idx)
                }
            }

            val retriever = MediaMetadataRetriever()
            var title: String? = null
            var artist: String? = null
            var album: String? = null
            var durationMs = 0L
            var bitrate: String? = null

            try {
                retriever.setDataSource(context, uri)
                title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
                artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
                album = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)
                durationMs = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
                bitrate = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)
            } finally {
                runCatching { retriever.release() }
            }

            val mimeType = resolver.getType(uri)
            val ext = displayName?.substringAfterLast('.', "")
            val format = AudioFormat.fromExtensionOrMime(ext, mimeType)
            val cleanName = displayName?.substringBeforeLast('.')?.takeIf { it.isNotBlank() } ?: "Audio Impor"
            val kbps = bitrate?.toLongOrNull()?.div(1000)
            val quality = if (kbps != null && kbps > 0) "${format.label} · $kbps kbps" else format.label

            AudioTrack(
                id = "import_${uri.toString().hashCode().toUInt()}",
                title = title?.takeIf { it.isNotBlank() } ?: cleanName,
                artist = artist?.takeIf { it.isNotBlank() } ?: "Artis Lokal",
                album = album?.takeIf { it.isNotBlank() } ?: "Berkas Impor",
                durationMs = durationMs.coerceAtLeast(1_000L),
                uriString = uri.toString(),
                format = format,
                qualityLabel = quality,
                isBundled = false,
                folderSource = "Impor",
                dateAddedMillis = System.currentTimeMillis()
            )
        }.getOrNull()
    }

    suspend fun readTextFromUri(uri: Uri): String? = withContext(Dispatchers.IO) {
        runCatching {
            context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
        }.getOrNull()
    }

    private fun readSidecarLrcIfExists(audioPath: String?): String? {
        if (audioPath.isNullOrBlank()) return null
        return runCatching {
            val audioFile = File(audioPath)
            val lrcFile = File(audioFile.parentFile, "${audioFile.nameWithoutExtension}.lrc")
            if (lrcFile.exists() && lrcFile.canRead()) {
                lrcFile.readText()
            } else null
        }.getOrNull()
    }

    companion object {
        internal fun resolveTimestampMillis(
            dateModifiedSec: Long,
            dateAddedSec: Long,
            filePath: String?
        ): Long {
            val mediaStoreMillis = when {
                dateModifiedSec > 0L -> dateModifiedSec * 1000L
                dateAddedSec > 0L -> dateAddedSec * 1000L
                else -> 0L
            }
            if (mediaStoreMillis > 0L) return mediaStoreMillis

            if (!filePath.isNullOrBlank()) {
                val lastModified = runCatching { File(filePath).lastModified() }.getOrDefault(0L)
                if (lastModified > 0L) return lastModified
            }
            return 0L
        }
    }
}
