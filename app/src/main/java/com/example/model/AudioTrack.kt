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
    DEFAULT("Terbaru / Bawaan"),
    TITLE_AZ("Judul (A–Z)"),
    TITLE_ZA("Judul (Z–A)"),
    ARTIST_AZ("Artis (A–Z)"),
    DURATION_SHORTEST("Durasi Terpendek"),
    DURATION_LONGEST("Durasi Terpanjang")
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
    val folderSource: String = "Penyimpanan Perangkat"
)

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
