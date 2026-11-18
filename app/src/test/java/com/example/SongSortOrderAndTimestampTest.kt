package com.example

import com.example.data.LocalAudioScanner
import com.example.model.AudioFormat
import com.example.model.AudioTrack
import com.example.model.SongSortOrder
import com.example.model.sortTracks
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class SongSortOrderAndTimestampTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private fun track(
        id: String,
        title: String = id,
        dateAddedMillis: Long = 0L,
        durationMs: Long = 10_000L
    ) = AudioTrack(
        id = id,
        title = title,
        artist = "Artist $id",
        album = "Album $id",
        durationMs = durationMs,
        uriString = "content://media/external/audio/media/$id",
        format = AudioFormat.FLAC,
        qualityLabel = "FLAC",
        dateAddedMillis = dateAddedMillis
    )

    @Test
    fun `sortTracks NEWEST_FIRST puts newest timestamp at top and bundled 0L at bottom`() {
        val bundled = track("bundled", dateAddedMillis = 0L)
        val oldest = track("oldest", dateAddedMillis = 1_700_000_000_000L)
        val middle = track("middle", dateAddedMillis = 1_720_000_000_000L)
        val newest = track("newest", dateAddedMillis = 1_750_000_000_000L)

        val sorted = sortTracks(listOf(bundled, oldest, newest, middle), SongSortOrder.NEWEST_FIRST)

        assertEquals(listOf("newest", "middle", "oldest", "bundled"), sorted.map { it.id })
    }

    @Test
    fun `sortTracks OLDEST_FIRST puts oldest timestamp at top and newest at bottom`() {
        val bundled = track("bundled", dateAddedMillis = 0L)
        val oldest = track("oldest", dateAddedMillis = 1_700_000_000_000L)
        val middle = track("middle", dateAddedMillis = 1_720_000_000_000L)
        val newest = track("newest", dateAddedMillis = 1_750_000_000_000L)

        val sorted = sortTracks(listOf(newest, middle, oldest, bundled), SongSortOrder.OLDEST_FIRST)

        assertEquals(listOf("bundled", "oldest", "middle", "newest"), sorted.map { it.id })
    }

    @Test
    fun `fromStorageValue migrates legacy DEFAULT and invalid values safely to NEWEST_FIRST`() {
        assertEquals(SongSortOrder.NEWEST_FIRST, SongSortOrder.fromStorageValue(null))
        assertEquals(SongSortOrder.NEWEST_FIRST, SongSortOrder.fromStorageValue(""))
        assertEquals(SongSortOrder.NEWEST_FIRST, SongSortOrder.fromStorageValue("DEFAULT"))
        assertEquals(SongSortOrder.NEWEST_FIRST, SongSortOrder.fromStorageValue("UNKNOWN_OLD_VALUE"))
        assertEquals(SongSortOrder.NEWEST_FIRST, SongSortOrder.fromStorageValue("NEWEST_FIRST"))
        assertEquals(SongSortOrder.OLDEST_FIRST, SongSortOrder.fromStorageValue("OLDEST_FIRST"))
        assertEquals(SongSortOrder.TITLE_AZ, SongSortOrder.fromStorageValue("TITLE_AZ"))
    }

    @Test
    fun `resolveTimestampMillis prioritizes DATE_MODIFIED then DATE_ADDED then File lastModified`() {
        assertEquals(
            1_750_000_123_000L,
            LocalAudioScanner.resolveTimestampMillis(
                dateModifiedSec = 1_750_000_123L,
                dateAddedSec = 1_700_000_000L,
                filePath = null
            )
        )

        assertEquals(
            1_700_000_456_000L,
            LocalAudioScanner.resolveTimestampMillis(
                dateModifiedSec = 0L,
                dateAddedSec = 1_700_000_456L,
                filePath = null
            )
        )

        val file: File = tempFolder.newFile("sample.mp3")
        val expectedLastModified = 1_712_345_678_000L
        file.setLastModified(expectedLastModified)
        assertEquals(
            expectedLastModified,
            LocalAudioScanner.resolveTimestampMillis(
                dateModifiedSec = 0L,
                dateAddedSec = 0L,
                filePath = file.absolutePath
            )
        )

        assertEquals(
            0L,
            LocalAudioScanner.resolveTimestampMillis(
                dateModifiedSec = 0L,
                dateAddedSec = 0L,
                filePath = "/non/existent/path/audio.mp3"
            )
        )
    }
}
