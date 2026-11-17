package com.example.model

data class LyricLine(
    val timestampMs: Long,
    val text: String
)

object LrcParser {
    private val timeTagRegex = Regex("""\[(\d{1,3}):(\d{2})(?:[.:](\d{1,3}))?]""")

    fun parse(rawContent: String?): List<LyricLine> {
        if (rawContent.isNullOrBlank()) return emptyList()
        val parsed = mutableListOf<LyricLine>()

        rawContent.lineSequence().forEach { line ->
            val trimmed = line.trim()
            if (trimmed.isEmpty()) return@forEach

            val matches = timeTagRegex.findAll(trimmed).toList()
            if (matches.isEmpty()) return@forEach

            val lyricText = trimmed.replace(timeTagRegex, "").trim()
            if (lyricText.isEmpty()) return@forEach

            for (match in matches) {
                val minutes = match.groupValues[1].toLongOrNull() ?: continue
                val seconds = match.groupValues[2].toLongOrNull() ?: continue
                val fracStr = match.groupValues.getOrNull(3).orEmpty()
                val millis = when (fracStr.length) {
                    0 -> 0L
                    1 -> (fracStr.toLongOrNull() ?: 0L) * 100L
                    2 -> (fracStr.toLongOrNull() ?: 0L) * 10L
                    else -> fracStr.take(3).toLongOrNull() ?: 0L
                }
                val totalMs = minutes * 60_000L + seconds * 1_000L + millis
                parsed.add(LyricLine(timestampMs = totalMs, text = lyricText))
            }
        }

        return parsed.sortedBy { it.timestampMs }
    }

    fun findActiveIndex(lines: List<LyricLine>, positionMs: Long): Int {
        if (lines.isEmpty()) return -1
        if (positionMs < lines.first().timestampMs) return -1

        var low = 0
        var high = lines.lastIndex
        var result = 0
        while (low <= high) {
            val mid = (low + high) ushr 1
            if (lines[mid].timestampMs <= positionMs) {
                result = mid
                low = mid + 1
            } else {
                high = mid - 1
            }
        }
        return result
    }
}
