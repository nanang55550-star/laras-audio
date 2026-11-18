package com.example.data

import android.content.Context
import android.net.Uri
import com.example.data.db.AppDatabase
import com.example.data.db.CustomEqPresetEntity
import com.example.data.db.CustomLyricEntity
import com.example.data.db.EqPreferences
import com.example.data.db.FavoriteTrackEntity
import com.example.data.db.ImportedTrackEntity
import com.example.data.db.PlaylistEntity
import com.example.data.db.PlaylistTrackEntity
import com.example.data.db.UserPrefsStore
import com.example.model.AudioFormat
import com.example.model.AudioTrack
import com.example.model.EqualizerPreset
import com.example.model.SongSortOrder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

data class PlaylistWithTracks(
    val id: Long,
    val name: String,
    val description: String,
    val trackIds: List<String>,
    val tracks: List<AudioTrack>
)

class MusicRepository(private val context: Context) {
    private val dao = AppDatabase.getInstance(context).musicDao()
    private val prefs = UserPrefsStore(context)
    private val scanner = LocalAudioScanner(context)

    private val bundledTracks = BundledCatalog.getSampleTracks(context)
    private val deviceTracksFlow = MutableStateFlow<List<AudioTrack>>(emptyList())

    val eqPreferences: Flow<EqPreferences> = prefs.eqPreferences
    val shuffleEnabled: Flow<Boolean> = prefs.shuffleEnabled
    val repeatMode: Flow<Int> = prefs.repeatMode
    val songSortOrder: Flow<SongSortOrder> = prefs.songSortOrder

    val allTracks: Flow<List<AudioTrack>> = combine(
        deviceTracksFlow,
        dao.observeImportedTracks(),
        dao.observeFavoriteIds(),
        dao.observeCustomLyrics()
    ) { deviceTracks, importedEntities, favoriteIds, customLyrics ->
        val favSet = favoriteIds.toSet()
        val lyricsMap = customLyrics.associate { it.trackId to it.rawLrc }
        val importedTracks = importedEntities.map { entity ->
            AudioTrack(
                id = entity.id,
                title = entity.title,
                artist = entity.artist,
                album = entity.album,
                durationMs = entity.durationMs,
                uriString = entity.uriString,
                format = runCatching { AudioFormat.valueOf(entity.formatName) }.getOrDefault(AudioFormat.OTHER),
                qualityLabel = entity.qualityLabel,
                isBundled = false
            )
        }

        (importedTracks + deviceTracks + bundledTracks)
            .distinctBy { it.id }
            .map { track ->
                track.copy(
                    isFavorite = track.id in favSet,
                    lyricsRaw = lyricsMap[track.id] ?: track.lyricsRaw
                )
            }
    }

    val playlists: Flow<List<PlaylistWithTracks>> = combine(
        dao.observePlaylists(),
        dao.observeAllPlaylistTracks(),
        allTracks
    ) { playlistEntities, crossRefs, tracks ->
        val trackMap = tracks.associateBy { it.id }
        val refsByPlaylist = crossRefs.groupBy { it.playlistId }
        playlistEntities.map { entity ->
            val refs = refsByPlaylist[entity.id].orEmpty().sortedBy { it.position }
            val resolvedTracks = refs.mapNotNull { trackMap[it.trackId] }
            PlaylistWithTracks(
                id = entity.id,
                name = entity.name,
                description = entity.description,
                trackIds = refs.map { it.trackId },
                tracks = resolvedTracks
            )
        }
    }

    val customEqPresets: Flow<List<EqualizerPreset>> = dao.observeCustomEqPresets().map { list ->
        list.map { entity ->
            EqualizerPreset(
                id = "custom_${entity.id}",
                name = entity.name,
                bandLevelsMb = entity.bandLevelsCsv.split(",").mapNotNull { it.trim().toIntOrNull() },
                bassBoostStrength = entity.bassBoostStrength,
                virtualizerStrength = entity.virtualizerStrength,
                isCustom = true
            )
        }
    }

    suspend fun refreshDeviceAudio(): Int {
        val scanned = scanner.scanDeviceAudio()
        deviceTracksFlow.value = scanned
        return scanned.size
    }

    suspend fun importAudioFile(uri: Uri): AudioTrack? {
        val track = scanner.extractImportedAudioTrack(uri) ?: return null
        dao.upsertImportedTrack(
            ImportedTrackEntity(
                id = track.id,
                title = track.title,
                artist = track.artist,
                album = track.album,
                durationMs = track.durationMs,
                uriString = track.uriString,
                formatName = track.format.name,
                qualityLabel = track.qualityLabel
            )
        )
        return track
    }

    suspend fun importLyricsForTrack(trackId: String, uri: Uri): Boolean {
        val raw = scanner.readTextFromUri(uri) ?: return false
        if (raw.isBlank()) return false
        dao.upsertCustomLyric(CustomLyricEntity(trackId = trackId, rawLrc = raw))
        return true
    }

    suspend fun toggleFavorite(trackId: String, currentlyFavorite: Boolean) {
        if (currentlyFavorite) {
            dao.removeFavorite(trackId)
        } else {
            dao.addFavorite(FavoriteTrackEntity(trackId = trackId))
        }
    }

    suspend fun createPlaylist(name: String, description: String = ""): Long {
        return dao.insertPlaylist(PlaylistEntity(name = name.trim(), description = description.trim()))
    }

    suspend fun renamePlaylist(playlistId: Long, newName: String, newDescription: String = "") {
        dao.updatePlaylist(playlistId, newName.trim(), newDescription.trim())
    }

    suspend fun deletePlaylist(playlistId: Long) {
        dao.deletePlaylist(playlistId)
    }

    suspend fun addTrackToPlaylist(playlistId: Long, trackId: String) {
        val pos = dao.getNextTrackPosition(playlistId)
        dao.addTrackToPlaylist(
            PlaylistTrackEntity(
                playlistId = playlistId,
                trackId = trackId,
                position = pos
            )
        )
    }

    suspend fun removeTrackFromPlaylist(playlistId: Long, trackId: String) {
        dao.removeTrackFromPlaylist(playlistId, trackId)
    }

    suspend fun saveEqPreferences(eq: EqPreferences) {
        prefs.saveEqPreferences(eq)
    }

    suspend fun saveCustomEqPreset(
        name: String,
        bandLevelsMb: List<Int>,
        bassBoostStrength: Int,
        virtualizerStrength: Int
    ) {
        dao.insertCustomEqPreset(
            CustomEqPresetEntity(
                name = name.trim(),
                bandLevelsCsv = bandLevelsMb.joinToString(","),
                bassBoostStrength = bassBoostStrength,
                virtualizerStrength = virtualizerStrength
            )
        )
    }

    suspend fun deleteCustomEqPreset(presetIdString: String) {
        val numericId = presetIdString.removePrefix("custom_").toLongOrNull() ?: return
        dao.deleteCustomEqPreset(numericId)
    }

    suspend fun setShuffleEnabled(enabled: Boolean) = prefs.setShuffleEnabled(enabled)
    suspend fun setRepeatMode(mode: Int) = prefs.setRepeatMode(mode)
    suspend fun setSongSortOrder(order: SongSortOrder) = prefs.setSongSortOrder(order)
}
