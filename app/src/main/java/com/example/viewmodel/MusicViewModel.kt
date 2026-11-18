package com.example.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.MusicRepository
import com.example.data.PlaylistWithTracks
import com.example.data.db.EqPreferences
import com.example.model.AudioFormat
import com.example.model.AudioTrack
import com.example.model.EqualizerPreset
import com.example.model.EqualizerState
import com.example.model.FormatFilter
import com.example.model.LrcParser
import com.example.model.LyricLine
import com.example.model.SleepTimerState
import com.example.model.SongSortOrder
import com.example.player.AudioEffectsController
import com.example.player.PlaybackController
import com.example.player.PlaybackSnapshot
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MusicViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = MusicRepository(application.applicationContext)
    private val playbackController = PlaybackController(application.applicationContext)

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _formatFilter = MutableStateFlow(FormatFilter.ALL)
    val formatFilter: StateFlow<FormatFilter> = _formatFilter.asStateFlow()

    private val _sortOrder = MutableStateFlow(SongSortOrder.DEFAULT)
    val sortOrder: StateFlow<SongSortOrder> = _sortOrder.asStateFlow()

    private val _hasAudioPermission = MutableStateFlow(false)
    val hasAudioPermission: StateFlow<Boolean> = _hasAudioPermission.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _uiMessages = MutableSharedFlow<String>(extraBufferCapacity = 8)
    val uiMessages = _uiMessages.asSharedFlow()

    val allTracks: StateFlow<List<AudioTrack>> = repository.allTracks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredTracks: StateFlow<List<AudioTrack>> = combine(
        allTracks,
        _searchQuery,
        _formatFilter,
        _sortOrder
    ) { tracks, query, filter, sort ->
        val q = query.trim().lowercase()
        val filtered = tracks.filter { track ->
            val matchesFormat = when (filter) {
                FormatFilter.ALL -> true
                FormatFilter.FLAC -> track.format == AudioFormat.FLAC
                FormatFilter.WAV -> track.format == AudioFormat.WAV
                FormatFilter.MP3 -> track.format == AudioFormat.MP3
                FormatFilter.OTHER -> track.format == AudioFormat.M4A || track.format == AudioFormat.OGG || track.format == AudioFormat.OTHER
            }
            val matchesQuery = q.isEmpty() ||
                track.title.lowercase().contains(q) ||
                track.artist.lowercase().contains(q) ||
                track.album.lowercase().contains(q)
            matchesFormat && matchesQuery
        }

        when (sort) {
            SongSortOrder.DEFAULT -> filtered
            SongSortOrder.TITLE_AZ -> filtered.sortedBy { it.title.lowercase() }
            SongSortOrder.TITLE_ZA -> filtered.sortedByDescending { it.title.lowercase() }
            SongSortOrder.ARTIST_AZ -> filtered.sortedBy { it.artist.lowercase() }
            SongSortOrder.DURATION_SHORTEST -> filtered.sortedBy { it.durationMs }
            SongSortOrder.DURATION_LONGEST -> filtered.sortedByDescending { it.durationMs }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteTracks: StateFlow<List<AudioTrack>> = allTracks.map { list ->
        list.filter { it.isFavorite }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val playlists: StateFlow<List<PlaylistWithTracks>> = repository.playlists
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val playbackState: StateFlow<PlaybackSnapshot> = playbackController.playbackState
    val sleepTimerState: StateFlow<SleepTimerState> = playbackController.sleepTimerState

    val currentTrack: StateFlow<AudioTrack?> = combine(
        allTracks,
        playbackState
    ) { tracks, snapshot ->
        tracks.firstOrNull { it.id == snapshot.currentTrackId }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val queueTracks: StateFlow<List<AudioTrack>> = combine(
        allTracks,
        playbackState
    ) { tracks, snapshot ->
        val map = tracks.associateBy { it.id }
        snapshot.queueTrackIds.mapNotNull { map[it] }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentLyrics: StateFlow<List<LyricLine>> = currentTrack.map { track ->
        LrcParser.parse(track?.lyricsRaw)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeLyricIndex: StateFlow<Int> = combine(
        currentLyrics,
        playbackState
    ) { lines, snapshot ->
        LrcParser.findActiveIndex(lines, snapshot.positionMs)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), -1)

    val equalizerState: StateFlow<EqualizerState> = combine(
        repository.eqPreferences,
        repository.customEqPresets,
        AudioEffectsController.isHardwareSupported,
        AudioEffectsController.bandMeta
    ) { eqPrefs, customPresets, hwSupported, bandMeta ->
        AudioEffectsController.updateSettings(
            enabled = eqPrefs.isEnabled,
            bandLevelsMb = eqPrefs.bandLevelsMb,
            bassBoostStrength = eqPrefs.bassBoostStrength,
            virtualizerStrength = eqPrefs.virtualizerStrength
        )
        EqualizerState(
            isEnabled = eqPrefs.isEnabled,
            minLevelMb = bandMeta.minLevelMb,
            maxLevelMb = bandMeta.maxLevelMb,
            bands = AudioEffectsController.buildBands(eqPrefs.bandLevelsMb),
            bassBoostStrength = eqPrefs.bassBoostStrength,
            virtualizerStrength = eqPrefs.virtualizerStrength,
            activePresetName = eqPrefs.activePresetName,
            presets = EqualizerState.builtInPresets() + customPresets,
            isHardwareSupported = hwSupported
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), EqualizerState())

    init {
        playbackController.connect()
        viewModelScope.launch {
            val shuffle = repository.shuffleEnabled.first()
            val repeat = repository.repeatMode.first()
            val savedSort = repository.songSortOrder.first()
            _sortOrder.value = savedSort
            playbackController.setShuffleAndRepeat(shuffle, repeat)
        }
    }

    fun setSortOrder(order: SongSortOrder) {
        _sortOrder.value = order
        viewModelScope.launch {
            repository.setSongSortOrder(order)
            _uiMessages.tryEmit("Urutan diubah ke: ${order.label}")
        }
    }

    override fun onCleared() {
        playbackController.disconnect()
        super.onCleared()
    }

    fun onPermissionUpdated(granted: Boolean) {
        _hasAudioPermission.value = granted
        if (granted) {
            scanDeviceAudio(showMessage = false)
        }
    }

    fun scanDeviceAudio(showMessage: Boolean = true) {
        viewModelScope.launch {
            _isScanning.value = true
            val count = repository.refreshDeviceAudio()
            _isScanning.value = false
            if (showMessage) {
                _uiMessages.tryEmit(
                    if (count > 0) "$count lagu perangkat berhasil dipindai"
                    else "Tidak ditemukan berkas audio baru di penyimpanan perangkat"
                )
            }
        }
    }

    fun importAudioUri(uri: Uri) {
        viewModelScope.launch {
            val track = repository.importAudioFile(uri)
            if (track != null) {
                _uiMessages.tryEmit("Berhasil mengimpor \"${track.title}\" (${track.format.label})")
                playTrackFromList(track, allTracks.value + track)
            } else {
                _uiMessages.tryEmit("Gagal membaca metadata berkas audio")
            }
        }
    }

    fun importLyricsForCurrentTrack(uri: Uri) {
        val track = currentTrack.value ?: return
        viewModelScope.launch {
            val ok = repository.importLyricsForTrack(track.id, uri)
            _uiMessages.tryEmit(
                if (ok) "Lirik tersinkronisasi berhasil dimuat untuk \"${track.title}\""
                else "File lirik tidak valid atau kosong"
            )
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectFormatFilter(filter: FormatFilter) {
        _formatFilter.value = filter
    }

    fun playTrackFromList(track: AudioTrack, list: List<AudioTrack>) {
        val queue = if (list.isEmpty()) listOf(track) else list
        val index = queue.indexOfFirst { it.id == track.id }.coerceAtLeast(0)
        playbackController.playQueue(
            tracks = queue,
            startIndex = index,
            shuffle = playbackState.value.shuffleEnabled,
            repeatMode = playbackState.value.repeatMode
        )
    }

    fun shuffleAndPlayAll() {
        val tracks = filteredTracks.value.ifEmpty { allTracks.value }
        if (tracks.isEmpty()) return
        viewModelScope.launch { repository.setShuffleEnabled(true) }
        val randomStart = tracks.indices.random()
        playbackController.playQueue(
            tracks = tracks,
            startIndex = randomStart,
            shuffle = true,
            repeatMode = playbackState.value.repeatMode
        )
    }

    fun playAll() {
        val tracks = filteredTracks.value.ifEmpty { allTracks.value }
        if (tracks.isEmpty()) return
        playbackController.playQueue(
            tracks = tracks,
            startIndex = 0,
            shuffle = false,
            repeatMode = playbackState.value.repeatMode
        )
    }

    fun togglePlayPause() = playbackController.togglePlayPause()
    fun seekTo(positionMs: Long) = playbackController.seekTo(positionMs)
    fun skipNext() = playbackController.skipNext()
    fun skipPrevious() = playbackController.skipPrevious()

    fun toggleShuffle() {
        val newShuffle = playbackController.toggleShuffle()
        viewModelScope.launch { repository.setShuffleEnabled(newShuffle) }
    }

    fun cycleRepeatMode() {
        val newRepeat = playbackController.cycleRepeatMode()
        viewModelScope.launch { repository.setRepeatMode(newRepeat) }
    }

    fun jumpToQueueIndex(index: Int) = playbackController.jumpToQueueIndex(index)
    fun removeFromQueue(index: Int) = playbackController.removeFromQueue(index)

    fun addTrackToQueue(track: AudioTrack) {
        playbackController.addTrackToQueue(track)
        _uiMessages.tryEmit("\"${track.title}\" ditambahkan ke antrean")
    }

    fun toggleFavorite(track: AudioTrack) {
        viewModelScope.launch {
            repository.toggleFavorite(track.id, track.isFavorite)
            _uiMessages.tryEmit(
                if (!track.isFavorite) "Ditambahkan ke Favorit"
                else "Dihapus dari Favorit"
            )
        }
    }

    // Playlist Management
    fun createPlaylist(name: String, description: String = "", initialTrackId: String? = null) {
        if (name.isBlank()) return
        viewModelScope.launch {
            val playlistId = repository.createPlaylist(name, description)
            if (initialTrackId != null) {
                repository.addTrackToPlaylist(playlistId, initialTrackId)
            }
            _uiMessages.tryEmit("Daftar putar \"$name\" dibuat")
        }
    }

    fun renamePlaylist(playlistId: Long, newName: String, newDescription: String = "") {
        if (newName.isBlank()) return
        viewModelScope.launch {
            repository.renamePlaylist(playlistId, newName, newDescription)
            _uiMessages.tryEmit("Daftar putar diperbarui")
        }
    }

    fun deletePlaylist(playlistId: Long) {
        viewModelScope.launch {
            repository.deletePlaylist(playlistId)
            _uiMessages.tryEmit("Daftar putar dihapus")
        }
    }

    fun addTrackToPlaylist(playlistId: Long, playlistName: String, track: AudioTrack) {
        viewModelScope.launch {
            repository.addTrackToPlaylist(playlistId, track.id)
            _uiMessages.tryEmit("\"${track.title}\" ditambahkan ke \"$playlistName\"")
        }
    }

    fun removeTrackFromPlaylist(playlistId: Long, trackId: String) {
        viewModelScope.launch {
            repository.removeTrackFromPlaylist(playlistId, trackId)
        }
    }

    // Equalizer Controls
    fun setEqualizerEnabled(enabled: Boolean) {
        val current = equalizerState.value
        saveEqState(
            EqPreferences(
                isEnabled = enabled,
                bandLevelsMb = current.bands.map { it.levelMb },
                bassBoostStrength = current.bassBoostStrength,
                virtualizerStrength = current.virtualizerStrength,
                activePresetName = current.activePresetName
            )
        )
    }

    fun setEqualizerBandLevel(bandIndex: Int, levelMb: Int) {
        val current = equalizerState.value
        val updatedLevels = current.bands.mapIndexed { idx, band ->
            if (idx == bandIndex) levelMb.coerceIn(current.minLevelMb, current.maxLevelMb) else band.levelMb
        }
        saveEqState(
            EqPreferences(
                isEnabled = true,
                bandLevelsMb = updatedLevels,
                bassBoostStrength = current.bassBoostStrength,
                virtualizerStrength = current.virtualizerStrength,
                activePresetName = "Kustom"
            )
        )
    }

    fun setBassBoost(strength: Int) {
        val current = equalizerState.value
        saveEqState(
            EqPreferences(
                isEnabled = true,
                bandLevelsMb = current.bands.map { it.levelMb },
                bassBoostStrength = strength.coerceIn(0, 1000),
                virtualizerStrength = current.virtualizerStrength,
                activePresetName = "Kustom"
            )
        )
    }

    fun setVirtualizer(strength: Int) {
        val current = equalizerState.value
        saveEqState(
            EqPreferences(
                isEnabled = true,
                bandLevelsMb = current.bands.map { it.levelMb },
                bassBoostStrength = current.bassBoostStrength,
                virtualizerStrength = strength.coerceIn(0, 1000),
                activePresetName = "Kustom"
            )
        )
    }

    fun applyEqualizerPreset(preset: EqualizerPreset) {
        saveEqState(
            EqPreferences(
                isEnabled = true,
                bandLevelsMb = preset.bandLevelsMb,
                bassBoostStrength = preset.bassBoostStrength,
                virtualizerStrength = preset.virtualizerStrength,
                activePresetName = preset.name
            )
        )
    }

    fun saveCurrentAsCustomPreset(name: String) {
        if (name.isBlank()) return
        val current = equalizerState.value
        val levels = current.bands.map { it.levelMb }
        viewModelScope.launch {
            repository.saveCustomEqPreset(
                name = name,
                bandLevelsMb = levels,
                bassBoostStrength = current.bassBoostStrength,
                virtualizerStrength = current.virtualizerStrength
            )
            saveEqState(
                EqPreferences(
                    isEnabled = current.isEnabled,
                    bandLevelsMb = levels,
                    bassBoostStrength = current.bassBoostStrength,
                    virtualizerStrength = current.virtualizerStrength,
                    activePresetName = name.trim()
                )
            )
            _uiMessages.tryEmit("Preset \"$name\" disimpan")
        }
    }

    fun deleteCustomEqPreset(preset: EqualizerPreset) {
        if (!preset.isCustom) return
        viewModelScope.launch {
            repository.deleteCustomEqPreset(preset.id)
            _uiMessages.tryEmit("Preset \"${preset.name}\" dihapus")
        }
    }

    private fun saveEqState(eq: EqPreferences) {
        AudioEffectsController.updateSettings(
            enabled = eq.isEnabled,
            bandLevelsMb = eq.bandLevelsMb,
            bassBoostStrength = eq.bassBoostStrength,
            virtualizerStrength = eq.virtualizerStrength
        )
        viewModelScope.launch { repository.saveEqPreferences(eq) }
    }

    // Sleep Timer Controls
    fun startSleepCountdown(minutes: Int) {
        playbackController.startSleepCountdown(minutes)
        _uiMessages.tryEmit("Sleep timer aktif untuk $minutes menit")
    }

    fun startSleepEndOfTrack() {
        playbackController.startSleepEndOfTrack()
        _uiMessages.tryEmit("Pemutaran akan berhenti setelah lagu ini selesai")
    }

    fun cancelSleepTimer() {
        playbackController.cancelSleepTimer()
        _uiMessages.tryEmit("Sleep timer dibatalkan")
    }
}
