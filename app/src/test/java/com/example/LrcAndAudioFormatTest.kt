package com.example

import com.example.model.AudioFormat
import com.example.model.LrcParser
import com.example.model.formatDuration
import org.junit.Assert.assertEquals
import org.junit.Test

class LrcAndAudioFormatTest {

    @Test
    fun `parse lrc handles standard and millisecond tags and sorts`() {
        val raw = """
            [ti:Nocturne Emas]
            [00:12.500]Baris kedua
            [00:04.20]Baris pertama
            [00:20.00][00:30.00]Baris berulang
        """.trimIndent()

        val lines = LrcParser.parse(raw)
        assertEquals(4, lines.size)
        assertEquals(4200L, lines[0].timestampMs)
        assertEquals("Baris pertama", lines[0].text)
        assertEquals(12500L, lines[1].timestampMs)
        assertEquals("Baris kedua", lines[1].text)
        assertEquals(20000L, lines[2].timestampMs)
        assertEquals(30000L, lines[3].timestampMs)
    }

    @Test
    fun `findActiveIndex returns accurate line index across playback positions`() {
        val lines = LrcParser.parse(
            """
            [00:02.00]Satu
            [00:08.00]Dua
            [00:15.50]Tiga
            """.trimIndent()
        )

        assertEquals(-1, LrcParser.findActiveIndex(lines, 1000L))
        assertEquals(0, LrcParser.findActiveIndex(lines, 2000L))
        assertEquals(0, LrcParser.findActiveIndex(lines, 7999L))
        assertEquals(1, LrcParser.findActiveIndex(lines, 8000L))
        assertEquals(2, LrcParser.findActiveIndex(lines, 18000L))
    }

    @Test
    fun `audio format detection supports flac wav and mp3`() {
        assertEquals(AudioFormat.FLAC, AudioFormat.fromExtensionOrMime("flac", null))
        assertEquals(AudioFormat.WAV, AudioFormat.fromExtensionOrMime("wav", "audio/x-wav"))
        assertEquals(AudioFormat.MP3, AudioFormat.fromExtensionOrMime("mp3", "audio/mpeg"))
        assertEquals("01:05", formatDuration(65_000L))
    }
}
