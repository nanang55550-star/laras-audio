package com.example.model

import androidx.annotation.DrawableRes

enum class AudioFormat(val label: String, val isLossless: Boolean) {
    FLAC("FLAC", true),
    WAV("WAV", true),
    MP3("MP3", false),
    M4A("M4A", false),
    OGG("OGG", false),
    OTHER("AUDIO", false);

    companion object {
        fun fromExtensionOrMime(extension: String?, mimeType: String?): AudioFormat {
            val ext = extension?.lowercase()?.trim()
            val mime = mimeType?.lowercase()?.trim()
            return when {
                ext == "flac" || mime?.contains("flac") == true -> FLAC
                ext == "wav" || mime?.contains("wav") == true || mime?.contains("x-wav") == true -> WAV
                ext == "mp3" || mime?.contains("mpeg") == true || mime?.contains("mp3") == true -> MP3
                ext == "m4a" || ext == "aac" || mime?.contains("mp4") == true || mime?.contains("aac") == true -> M4A
                ext == "ogg" || ext == "opus" || mime?.contains("ogg") == true || mime?.contains("opus") == true -> OGG
                else -> OTHER
            }
        }
    }
}

enum class FormatFilter(val label: String) {
    ALL("Semua"),
    FLAC("FLAC"),
    WAV("WAV"),
    MP3("MP3"),
    OTHER("Lainnya")
}

enum class SongSortOrder(val label: String) {
    NEWEST_FIRST("Terbaru (Atas)"),
    OLDEST_FIRST("Terbaru (Bawah) / Terlama"),
    TITLE_AZ("Judul (A–Z)"),
    TITLE_ZA("Judul (Z–A)"),
    ARTIST_AZ("Artis (A–Z)"),
    DURATION_SHORTEST("Durasi Terpendek"),
    DURATION_LONGEST("Durasi Terpanjang");

    companion object {
        fun fromStorageValue(raw: String?): SongSortOrder {
            if (raw.isNullOrBlank() || raw == "DEFAULT") {
                return NEWEST_FIRST
            }
            return entries.firstOrNull { it.name == raw } ?: NEWEST_FIRST
        }
    }
}

data class AudioTrack(
    val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Long,
    val uriString: String,
    val format: AudioFormat,
    val qualityLabel: String,
    @DrawableRes val artworkResId: Int? = null,
    val artworkUri: String? = null,
    val isBundled: Boolean = false,
    val isFavorite: Boolean = false,
    val lyricsRaw: String? = null,
    val folderSource: String = "Penyimpanan Perangkat",
    val dateAddedMillis: Long = 0L
)

fun sortTracks(tracks: List<AudioTrack>, sortOrder: SongSortOrder): List<AudioTrack> = when (sortOrder) {
    SongSortOrder.NEWEST_FIRST -> tracks.sortedByDescending { it.dateAddedMillis }
    SongSortOrder.OLDEST_FIRST -> tracks.sortedBy { it.dateAddedMillis }
    SongSortOrder.TITLE_AZ -> tracks.sortedBy { it.title.lowercase() }
    SongSortOrder.TITLE_ZA -> tracks.sortedByDescending { it.title.lowercase() }
    SongSortOrder.ARTIST_AZ -> tracks.sortedBy { it.artist.lowercase() }
    SongSortOrder.DURATION_SHORTEST -> tracks.sortedBy { it.durationMs }
    SongSortOrder.DURATION_LONGEST -> tracks.sortedByDescending { it.durationMs }
}

fun formatDuration(ms: Long): String {
    if (ms <= 0L) return "00:00"
    val totalSeconds = ms / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        String.format("%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
}
