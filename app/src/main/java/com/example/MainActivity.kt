package com.example

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.QueueMusic
import androidx.compose.material.icons.rounded.Equalizer
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.AudioTrack
import com.example.ui.components.AddToPlaylistSheet
import com.example.ui.components.MiniPlayerBar
import com.example.ui.components.QueueSheet
import com.example.ui.components.SleepTimerSheet
import com.example.ui.screens.EqualizerScreen
import com.example.ui.screens.LibraryScreen
import com.example.ui.screens.LyricsFullScreen
import com.example.ui.screens.NowPlayingScreen
import com.example.ui.screens.PlaylistFormDialog
import com.example.ui.screens.PlaylistsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.IvoryText
import com.example.ui.theme.LarasAudioTheme
import com.example.ui.theme.ObsidianElevated
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.ObsidianVoid
import com.example.ui.theme.OnGoldDark
import com.example.ui.theme.SoftGold
import com.example.ui.theme.SubtleOutline
import com.example.ui.theme.WarmGold
import com.example.ui.theme.WarmGray
import com.example.viewmodel.MusicViewModel

private enum class MainTab(val label: String, val icon: ImageVector) {
    LIBRARY("Pustaka", Icons.Rounded.LibraryMusic),
    PLAYLISTS("Daftar Putar", Icons.AutoMirrored.Rounded.QueueMusic),
    EQUALIZER("Studio Suara", Icons.Rounded.Equalizer)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT)
        )
        setContent {
            LarasAudioTheme {
                LarasAudioApp()
            }
        }
    }
}

@Composable
fun LarasAudioApp(
    viewModel: MusicViewModel = viewModel()
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    val allTracks by viewModel.allTracks.collectAsStateWithLifecycle()
    val filteredTracks by viewModel.filteredTracks.collectAsStateWithLifecycle()
    val favoriteTracks by viewModel.favoriteTracks.collectAsStateWithLifecycle()
    val playlists by viewModel.playlists.collectAsStateWithLifecycle()
    val playbackState by viewModel.playbackState.collectAsStateWithLifecycle()
    val currentTrack by viewModel.currentTrack.collectAsStateWithLifecycle()
    val queueTracks by viewModel.queueTracks.collectAsStateWithLifecycle()
    val currentLyrics by viewModel.currentLyrics.collectAsStateWithLifecycle()
    val activeLyricIndex by viewModel.activeLyricIndex.collectAsStateWithLifecycle()
    val equalizerState by viewModel.equalizerState.collectAsStateWithLifecycle()
    val sleepTimerState by viewModel.sleepTimerState.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val formatFilter by viewModel.formatFilter.collectAsStateWithLifecycle()
    val hasAudioPermission by viewModel.hasAudioPermission.collectAsStateWithLifecycle()
    val isScanning by viewModel.isScanning.collectAsStateWithLifecycle()

    var activeTab by rememberSaveable { mutableStateOf(MainTab.LIBRARY) }
    var showNowPlaying by rememberSaveable { mutableStateOf(false) }
    var showFullLyrics by rememberSaveable { mutableStateOf(false) }
    var showSettings by rememberSaveable { mutableStateOf(false) }
    var showSleepTimerSheet by remember { mutableStateOf(false) }
    var showQueueSheet by remember { mutableStateOf(false) }
    var pendingPlaylistTrack by remember { mutableStateOf<AudioTrack?>(null) }
    var showNewPlaylistForTrackDialog by remember { mutableStateOf<AudioTrack?>(null) }

    val audioPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_AUDIO
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        viewModel.onPermissionUpdated(granted)
    }

    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
            viewModel.importAudioUri(uri)
        }
    }

    val lrcPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            viewModel.importLyricsForCurrentTrack(uri)
        }
    }

    LaunchedEffect(Unit) {
        val granted = ContextCompat.checkSelfPermission(
            context,
            audioPermission
        ) == PackageManager.PERMISSION_GRANTED
        viewModel.onPermissionUpdated(granted)
    }

    LaunchedEffect(Unit) {
        viewModel.uiMessages.collect { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianVoid)
    ) {
        val isExpanded = maxWidth >= 840.dp

        if (isExpanded) {
            Row(modifier = Modifier.fillMaxSize()) {
                NavigationRail(
                    containerColor = ObsidianSurface,
                    contentColor = IvoryText,
                    modifier = Modifier.statusBarsPadding()
                ) {
                    MainTab.entries.forEach { tab ->
                        NavigationRailItem(
                            selected = activeTab == tab,
                            onClick = { activeTab = tab },
                            icon = { Icon(tab.icon, contentDescription = tab.label) },
                            label = { Text(tab.label) },
                            colors = NavigationRailItemDefaults.colors(
                                selectedIconColor = OnGoldDark,
                                selectedTextColor = SoftGold,
                                indicatorColor = WarmGold,
                                unselectedIconColor = WarmGray,
                                unselectedTextColor = WarmGray
                            ),
                            modifier = Modifier.testTag("nav_tab_${tab.name}")
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .statusBarsPadding()
                ) {
                    MainTabContent(
                        activeTab = activeTab,
                        viewModel = viewModel,
                        filteredTracks = filteredTracks,
                        allTracks = allTracks,
                        favoriteTracks = favoriteTracks,
                        playlists = playlists,
                        currentTrack = currentTrack,
                        isPlaying = playbackState.isPlaying,
                        searchQuery = searchQuery,
                        formatFilter = formatFilter,
                        equalizerState = equalizerState,
                        bottomContentPadding = PaddingValues(bottom = 24.dp),
                        onOpenSettings = { showSettings = true },
                        onPlayTrack = { track, list ->
                            viewModel.playTrackFromList(track, list)
                        },
                        onOpenAddToPlaylist = { pendingPlaylistTrack = it }
                    )
                }

                val active = currentTrack
                if (active != null) {
                    Box(
                        modifier = Modifier
                            .weight(0.9f)
                            .fillMaxHeight()
                            .background(ObsidianSurface)
                    ) {
                        if (showFullLyrics) {
                            LyricsFullScreen(
                                track = active,
                                lyrics = currentLyrics,
                                activeIndex = activeLyricIndex,
                                isPlaying = playbackState.isPlaying,
                                positionMs = playbackState.positionMs,
                                durationMs = playbackState.durationMs,
                                onClose = { showFullLyrics = false },
                                onSeekTo = viewModel::seekTo,
                                onTogglePlayPause = viewModel::togglePlayPause,
                                onSkipNext = viewModel::skipNext,
                                onSkipPrevious = viewModel::skipPrevious,
                                onImportLrcFile = { lrcPickerLauncher.launch(arrayOf("text/*", "application/octet-stream", "*/*")) }
                            )
                        } else {
                            NowPlayingScreen(
                                track = active,
                                isPlaying = playbackState.isPlaying,
                                positionMs = playbackState.positionMs,
                                durationMs = playbackState.durationMs,
                                shuffleEnabled = playbackState.shuffleEnabled,
                                repeatMode = playbackState.repeatMode,
                                sleepTimerState = sleepTimerState,
                                lyrics = currentLyrics,
                                activeLyricIndex = activeLyricIndex,
                                onClose = {},
                                onTogglePlayPause = viewModel::togglePlayPause,
                                onSeekTo = viewModel::seekTo,
                                onSkipNext = viewModel::skipNext,
                                onSkipPrevious = viewModel::skipPrevious,
                                onToggleShuffle = viewModel::toggleShuffle,
                                onCycleRepeat = viewModel::cycleRepeatMode,
                                onToggleFavorite = { viewModel.toggleFavorite(active) },
                                onOpenLyrics = { showFullLyrics = true },
                                onOpenEqualizer = { activeTab = MainTab.EQUALIZER },
                                onOpenSleepTimer = { showSleepTimerSheet = true },
                                onOpenQueue = { showQueueSheet = true },
                                onAddToPlaylist = { pendingPlaylistTrack = active }
                            )
                        }
                    }
                }
            }
        } else {
            Scaffold(
                containerColor = ObsidianVoid,
                contentWindowInsets = WindowInsets(0, 0, 0, 0),
                bottomBar = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        val active = currentTrack
                        if (active != null) {
                            MiniPlayerBar(
                                track = active,
                                isPlaying = playbackState.isPlaying,
                                positionMs = playbackState.positionMs,
                                durationMs = playbackState.durationMs,
                                sleepTimerState = sleepTimerState,
                                onOpenPlayer = { showNowPlaying = true },
                                onOpenLyrics = { showFullLyrics = true },
                                onTogglePlayPause = viewModel::togglePlayPause,
                                onSkipNext = viewModel::skipNext
                            )
                        }
                        NavigationBar(
                            containerColor = ObsidianSurface,
                            tonalElevation = 8.dp
                        ) {
                            MainTab.entries.forEach { tab ->
                                NavigationBarItem(
                                    selected = activeTab == tab,
                                    onClick = { activeTab = tab },
                                    icon = { Icon(tab.icon, contentDescription = tab.label) },
                                    label = {
                                        Text(
                                            text = tab.label,
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = OnGoldDark,
                                        selectedTextColor = SoftGold,
                                        indicatorColor = WarmGold,
                                        unselectedIconColor = WarmGray,
                                        unselectedTextColor = WarmGray
                                    ),
                                    modifier = Modifier.testTag("nav_tab_${tab.name}")
                                )
                            }
                        }
                    }
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                ) {
                    MainTabContent(
                        activeTab = activeTab,
                        viewModel = viewModel,
                        filteredTracks = filteredTracks,
                        allTracks = allTracks,
                        favoriteTracks = favoriteTracks,
                        playlists = playlists,
                        currentTrack = currentTrack,
                        isPlaying = playbackState.isPlaying,
                        searchQuery = searchQuery,
                        formatFilter = formatFilter,
                        equalizerState = equalizerState,
                        bottomContentPadding = innerPadding,
                        onOpenSettings = { showSettings = true },
                        onPlayTrack = { track, list ->
                            viewModel.playTrackFromList(track, list)
                        },
                        onOpenAddToPlaylist = { pendingPlaylistTrack = it }
                    )
                }
            }

            // Full-Screen Now Playing Overlay
            AnimatedVisibility(
                visible = showNowPlaying && currentTrack != null,
                enter = slideInVertically { it },
                exit = slideOutVertically { it }
            ) {
                currentTrack?.let { active ->
                    NowPlayingScreen(
                        track = active,
                        isPlaying = playbackState.isPlaying,
                        positionMs = playbackState.positionMs,
                        durationMs = playbackState.durationMs,
                        shuffleEnabled = playbackState.shuffleEnabled,
                        repeatMode = playbackState.repeatMode,
                        sleepTimerState = sleepTimerState,
                        lyrics = currentLyrics,
                        activeLyricIndex = activeLyricIndex,
                        onClose = { showNowPlaying = false },
                        onTogglePlayPause = viewModel::togglePlayPause,
                        onSeekTo = viewModel::seekTo,
                        onSkipNext = viewModel::skipNext,
                        onSkipPrevious = viewModel::skipPrevious,
                        onToggleShuffle = viewModel::toggleShuffle,
                        onCycleRepeat = viewModel::cycleRepeatMode,
                        onToggleFavorite = { viewModel.toggleFavorite(active) },
                        onOpenLyrics = { showFullLyrics = true },
                        onOpenEqualizer = {
                            showNowPlaying = false
                            activeTab = MainTab.EQUALIZER
                        },
                        onOpenSleepTimer = { showSleepTimerSheet = true },
                        onOpenQueue = { showQueueSheet = true },
                        onAddToPlaylist = { pendingPlaylistTrack = active }
                    )
                }
            }

            // Full-Screen Interactive Lyrics Overlay
            AnimatedVisibility(
                visible = showFullLyrics && currentTrack != null,
                enter = slideInVertically { it },
                exit = slideOutVertically { it }
            ) {
                currentTrack?.let { active ->
                    LyricsFullScreen(
                        track = active,
                        lyrics = currentLyrics,
                        activeIndex = activeLyricIndex,
                        isPlaying = playbackState.isPlaying,
                        positionMs = playbackState.positionMs,
                        durationMs = playbackState.durationMs,
                        onClose = { showFullLyrics = false },
                        onSeekTo = viewModel::seekTo,
                        onTogglePlayPause = viewModel::togglePlayPause,
                        onSkipNext = viewModel::skipNext,
                        onSkipPrevious = viewModel::skipPrevious,
                        onImportLrcFile = {
                            lrcPickerLauncher.launch(arrayOf("text/*", "application/octet-stream", "*/*"))
                        }
                    )
                }
            }
        }

        // Settings Screen Overlay (accessible from both compact & expanded layouts)
        AnimatedVisibility(
            visible = showSettings,
            enter = slideInVertically { it },
            exit = slideOutVertically { it }
        ) {
            SettingsScreen(
                hasAudioPermission = hasAudioPermission,
                isScanning = isScanning,
                totalTracksCount = allTracks.size,
                equalizerEnabled = equalizerState.isEnabled,
                equalizerPresetName = equalizerState.activePresetName,
                sleepTimerState = sleepTimerState,
                onBack = { showSettings = false },
                onRequestScanPermission = { permissionLauncher.launch(audioPermission) },
                onRescanDevice = { viewModel.scanDeviceAudio(showMessage = true) },
                onImportAudioFile = { audioPickerLauncher.launch(arrayOf("audio/*")) },
                onOpenEqualizer = {
                    showSettings = false
                    activeTab = MainTab.EQUALIZER
                },
                onOpenSleepTimer = { showSleepTimerSheet = true }
            )
        }

        // Shared Sheets & Dialogs
        pendingPlaylistTrack?.let { targetTrack ->
            AddToPlaylistSheet(
                track = targetTrack,
                playlists = playlists,
                onDismiss = { pendingPlaylistTrack = null },
                onSelectPlaylist = { playlist ->
                    viewModel.addTrackToPlaylist(playlist.id, playlist.name, targetTrack)
                    pendingPlaylistTrack = null
                },
                onCreateNewPlaylist = {
                    val trackForNew = targetTrack
                    pendingPlaylistTrack = null
                    showNewPlaylistForTrackDialog = trackForNew
                }
            )
        }

        showNewPlaylistForTrackDialog?.let { trackForNew ->
            PlaylistFormDialog(
                dialogTitle = "Buat Daftar Putar Baru",
                initialName = "",
                initialDescription = "",
                confirmLabel = "Simpan & Tambah",
                onDismiss = { showNewPlaylistForTrackDialog = null },
                onConfirm = { name, desc ->
                    viewModel.createPlaylist(name, desc, initialTrackId = trackForNew.id)
                    showNewPlaylistForTrackDialog = null
                }
            )
        }

        if (showSleepTimerSheet) {
            SleepTimerSheet(
                sleepTimerState = sleepTimerState,
                onDismiss = { showSleepTimerSheet = false },
                onSelectMinutes = viewModel::startSleepCountdown,
                onSelectEndOfTrack = viewModel::startSleepEndOfTrack,
                onCancelTimer = viewModel::cancelSleepTimer
            )
        }

        if (showQueueSheet) {
            QueueSheet(
                queueTracks = queueTracks,
                currentQueueIndex = playbackState.currentQueueIndex,
                onDismiss = { showQueueSheet = false },
                onJumpToIndex = viewModel::jumpToQueueIndex,
                onRemoveAtIndex = viewModel::removeFromQueue
            )
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 16.dp)
        )
    }
}

@Composable
private fun MainTabContent(
    activeTab: MainTab,
    viewModel: MusicViewModel,
    filteredTracks: List<AudioTrack>,
    allTracks: List<AudioTrack>,
    favoriteTracks: List<AudioTrack>,
    playlists: List<com.example.data.PlaylistWithTracks>,
    currentTrack: AudioTrack?,
    isPlaying: Boolean,
    searchQuery: String,
    formatFilter: com.example.model.FormatFilter,
    equalizerState: com.example.model.EqualizerState,
    bottomContentPadding: PaddingValues,
    onOpenSettings: () -> Unit,
    onPlayTrack: (AudioTrack, List<AudioTrack>) -> Unit,
    onOpenAddToPlaylist: (AudioTrack) -> Unit
) {
    when (activeTab) {
        MainTab.LIBRARY -> {
            LibraryScreen(
                tracks = filteredTracks,
                totalCount = allTracks.size,
                currentTrackId = currentTrack?.id,
                isPlaying = isPlaying,
                searchQuery = searchQuery,
                selectedFilter = formatFilter,
                bottomContentPadding = bottomContentPadding,
                onSearchQueryChange = viewModel::updateSearchQuery,
                onSelectFilter = viewModel::selectFormatFilter,
                onOpenSettings = onOpenSettings,
                onPlayTrack = { track -> onPlayTrack(track, filteredTracks) },
                onPlayAll = viewModel::playAll,
                onShuffleAll = viewModel::shuffleAndPlayAll,
                onToggleFavorite = viewModel::toggleFavorite,
                onAddToPlaylist = onOpenAddToPlaylist,
                onAddToQueue = viewModel::addTrackToQueue
            )
        }

        MainTab.PLAYLISTS -> {
            PlaylistsScreen(
                playlists = playlists,
                favoriteTracks = favoriteTracks,
                allTracks = allTracks,
                currentTrackId = currentTrack?.id,
                isPlaying = isPlaying,
                bottomContentPadding = bottomContentPadding,
                onCreatePlaylist = { name, desc -> viewModel.createPlaylist(name, desc) },
                onRenamePlaylist = viewModel::renamePlaylist,
                onDeletePlaylist = viewModel::deletePlaylist,
                onAddTrackToPlaylist = viewModel::addTrackToPlaylist,
                onRemoveTrackFromPlaylist = viewModel::removeTrackFromPlaylist,
                onPlayTrackList = onPlayTrack,
                onToggleFavorite = viewModel::toggleFavorite,
                onAddToPlaylistSheet = onOpenAddToPlaylist,
                onAddToQueue = viewModel::addTrackToQueue
            )
        }

        MainTab.EQUALIZER -> {
            EqualizerScreen(
                state = equalizerState,
                bottomContentPadding = bottomContentPadding,
                onToggleEnabled = viewModel::setEqualizerEnabled,
                onBandLevelChange = viewModel::setEqualizerBandLevel,
                onSelectPreset = viewModel::applyEqualizerPreset,
                onBassBoostChange = viewModel::setBassBoost,
                onVirtualizerChange = viewModel::setVirtualizer,
                onSaveCustomPreset = viewModel::saveCurrentAsCustomPreset,
                onDeleteCustomPreset = viewModel::deleteCustomEqPreset
            )
        }
    }
}
