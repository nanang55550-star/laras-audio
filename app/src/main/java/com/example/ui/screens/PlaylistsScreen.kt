package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.PlaylistPlay
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.PlaylistWithTracks
import com.example.model.AudioTrack
import com.example.ui.components.AlbumArtwork
import com.example.ui.components.TrackListItem
import com.example.ui.theme.IvoryText
import com.example.ui.theme.ObsidianCard
import com.example.ui.theme.ObsidianElevated
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.ObsidianVoid
import com.example.ui.theme.OnGoldDark
import com.example.ui.theme.SoftGold
import com.example.ui.theme.SubtleOutline
import com.example.ui.theme.WarmGold
import com.example.ui.theme.WarmGray

@Composable
fun PlaylistsScreen(
    playlists: List<PlaylistWithTracks>,
    favoriteTracks: List<AudioTrack>,
    allTracks: List<AudioTrack>,
    currentTrackId: String?,
    isPlaying: Boolean,
    bottomContentPadding: PaddingValues,
    onCreatePlaylist: (String, String) -> Unit,
    onRenamePlaylist: (Long, String, String) -> Unit,
    onDeletePlaylist: (Long) -> Unit,
    onAddTrackToPlaylist: (Long, String, AudioTrack) -> Unit,
    onRemoveTrackFromPlaylist: (Long, String) -> Unit,
    onPlayTrackList: (AudioTrack, List<AudioTrack>) -> Unit,
    onToggleFavorite: (AudioTrack) -> Unit,
    onAddToPlaylistSheet: (AudioTrack) -> Unit,
    onAddToQueue: (AudioTrack) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedPlaylistId by remember { mutableStateOf<Long?>(null) }
    var viewingFavorites by remember { mutableStateOf(false) }
    var showCreateDialog by remember { mutableStateOf(false) }

    val selectedPlaylist = playlists.firstOrNull { it.id == selectedPlaylistId }

    if (viewingFavorites) {
        BackHandler { viewingFavorites = false }
        PlaylistDetailContent(
            title = "Lagu Favorit",
            subtitle = "${favoriteTracks.size} lagu tersimpan",
            tracks = favoriteTracks,
            allTracks = allTracks,
            currentTrackId = currentTrackId,
            isPlaying = isPlaying,
            isSystemFavorites = true,
            bottomContentPadding = bottomContentPadding,
            onBack = { viewingFavorites = false },
            onPlayTrack = { track -> onPlayTrackList(track, favoriteTracks) },
            onPlayAll = { favoriteTracks.firstOrNull()?.let { onPlayTrackList(it, favoriteTracks) } },
            onShuffleAll = { favoriteTracks.randomOrNull()?.let { onPlayTrackList(it, favoriteTracks) } },
            onToggleFavorite = onToggleFavorite,
            onAddToPlaylistSheet = onAddToPlaylistSheet,
            onAddToQueue = onAddToQueue,
            onAddExistingTrack = {},
            onRemoveTrack = null,
            onRename = { _, _ -> },
            onDelete = {}
        )
        return
    }

    if (selectedPlaylist != null) {
        BackHandler { selectedPlaylistId = null }
        PlaylistDetailContent(
            title = selectedPlaylist.name,
            subtitle = selectedPlaylist.description.ifBlank { "${selectedPlaylist.tracks.size} lagu" },
            tracks = selectedPlaylist.tracks,
            allTracks = allTracks,
            currentTrackId = currentTrackId,
            isPlaying = isPlaying,
            isSystemFavorites = false,
            bottomContentPadding = bottomContentPadding,
            onBack = { selectedPlaylistId = null },
            onPlayTrack = { track -> onPlayTrackList(track, selectedPlaylist.tracks) },
            onPlayAll = { selectedPlaylist.tracks.firstOrNull()?.let { onPlayTrackList(it, selectedPlaylist.tracks) } },
            onShuffleAll = { selectedPlaylist.tracks.randomOrNull()?.let { onPlayTrackList(it, selectedPlaylist.tracks) } },
            onToggleFavorite = onToggleFavorite,
            onAddToPlaylistSheet = onAddToPlaylistSheet,
            onAddToQueue = onAddToQueue,
            onAddExistingTrack = { track ->
                onAddTrackToPlaylist(selectedPlaylist.id, selectedPlaylist.name, track)
            },
            onRemoveTrack = { track ->
                onRemoveTrackFromPlaylist(selectedPlaylist.id, track.id)
            },
            onRename = { newName, newDesc ->
                onRenamePlaylist(selectedPlaylist.id, newName, newDesc)
            },
            onDelete = {
                onDeletePlaylist(selectedPlaylist.id)
                selectedPlaylistId = null
            }
        )
        return
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(WarmGold.copy(alpha = 0.06f), ObsidianVoid, ObsidianVoid)
                )
            ),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 12.dp,
            bottom = bottomContentPadding.calculateBottomPadding() + 16.dp
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Daftar Putar",
                        style = MaterialTheme.typography.displaySmall,
                        color = IvoryText
                    )
                    Text(
                        text = "Kelola koleksi suasana dengar Anda",
                        style = MaterialTheme.typography.bodyMedium,
                        color = WarmGray
                    )
                }
                Button(
                    onClick = { showCreateDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = WarmGold, contentColor = OnGoldDark),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .height(48.dp)
                        .testTag("create_playlist_button")
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Buat Baru", style = MaterialTheme.typography.labelLarge)
                }
            }
        }

        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .clickable { viewingFavorites = true }
                    .testTag("favorites_playlist_card"),
                shape = RoundedCornerShape(18.dp),
                color = ObsidianCard,
                border = BorderStroke(1.dp, WarmGold.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(WarmGold.copy(alpha = 0.16f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Favorite,
                            contentDescription = "Daftar Lagu Favorit",
                            tint = WarmGold,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Lagu Favorit",
                            style = MaterialTheme.typography.titleMedium,
                            color = SoftGold
                        )
                        Text(
                            text = "${favoriteTracks.size} lagu ditandai",
                            style = MaterialTheme.typography.bodyMedium,
                            color = WarmGray
                        )
                    }
                    Icon(
                        imageVector = Icons.Rounded.PlayArrow,
                        contentDescription = null,
                        tint = WarmGold
                    )
                }
            }
        }

        if (playlists.isEmpty()) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    color = ObsidianCard.copy(alpha = 0.65f),
                    border = BorderStroke(0.75.dp, SubtleOutline)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.PlaylistPlay,
                            contentDescription = null,
                            tint = WarmGold.copy(alpha = 0.65f),
                            modifier = Modifier.size(44.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Belum Ada Daftar Putar Kustom",
                            style = MaterialTheme.typography.titleMedium,
                            color = IvoryText
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Ketuk \"Buat Baru\" untuk menyusun daftar putar berdasarkan suasana atau format musik.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = WarmGray
                        )
                    }
                }
            }
        } else {
            items(playlists, key = { it.id }) { playlist ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { selectedPlaylistId = playlist.id }
                        .testTag("playlist_card_${playlist.id}"),
                    shape = RoundedCornerShape(16.dp),
                    color = ObsidianCard,
                    border = BorderStroke(0.75.dp, SubtleOutline)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AlbumArtwork(
                            track = playlist.tracks.firstOrNull(),
                            size = 54.dp,
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = playlist.name,
                                style = MaterialTheme.typography.titleMedium,
                                color = IvoryText,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = if (playlist.description.isNotBlank()) {
                                    "${playlist.tracks.size} lagu · ${playlist.description}"
                                } else {
                                    "${playlist.tracks.size} lagu"
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = WarmGray,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.PlaylistPlay,
                            contentDescription = "Buka daftar putar ${playlist.name}",
                            tint = WarmGold
                        )
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        PlaylistFormDialog(
            dialogTitle = "Buat Daftar Putar Baru",
            initialName = "",
            initialDescription = "",
            confirmLabel = "Simpan",
            onDismiss = { showCreateDialog = false },
            onConfirm = { name, desc ->
                onCreatePlaylist(name, desc)
                showCreateDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlaylistDetailContent(
    title: String,
    subtitle: String,
    tracks: List<AudioTrack>,
    allTracks: List<AudioTrack>,
    currentTrackId: String?,
    isPlaying: Boolean,
    isSystemFavorites: Boolean,
    bottomContentPadding: PaddingValues,
    onBack: () -> Unit,
    onPlayTrack: (AudioTrack) -> Unit,
    onPlayAll: () -> Unit,
    onShuffleAll: () -> Unit,
    onToggleFavorite: (AudioTrack) -> Unit,
    onAddToPlaylistSheet: (AudioTrack) -> Unit,
    onAddToQueue: (AudioTrack) -> Unit,
    onAddExistingTrack: (AudioTrack) -> Unit,
    onRemoveTrack: ((AudioTrack) -> Unit)?,
    onRename: (String, String) -> Unit,
    onDelete: () -> Unit
) {
    var showAddTracksSheet by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianVoid),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 8.dp,
            bottom = bottomContentPadding.calculateBottomPadding() + 16.dp
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("playlist_detail_back")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Kembali ke daftar putar",
                        tint = IvoryText
                    )
                }

                if (!isSystemFavorites) {
                    Row {
                        IconButton(
                            onClick = { showRenameDialog = true },
                            modifier = Modifier.minimumInteractiveComponentSize()
                        ) {
                            Icon(Icons.Rounded.Edit, contentDescription = "Ubah nama daftar putar", tint = WarmGold)
                        }
                        IconButton(
                            onClick = { showDeleteConfirm = true },
                            modifier = Modifier.minimumInteractiveComponentSize()
                        ) {
                            Icon(Icons.Rounded.DeleteOutline, contentDescription = "Hapus daftar putar", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }

        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineMedium,
                    color = IvoryText
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = WarmGray
                )
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onPlayAll,
                        enabled = tracks.isNotEmpty(),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = WarmGold, contentColor = OnGoldDark),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Rounded.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Putar", style = MaterialTheme.typography.labelLarge)
                    }
                    OutlinedButton(
                        onClick = onShuffleAll,
                        enabled = tracks.isNotEmpty(),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        border = BorderStroke(1.dp, SubtleOutline),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Rounded.Shuffle, contentDescription = null, tint = IvoryText, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Acak", color = IvoryText)
                    }
                    if (!isSystemFavorites) {
                        OutlinedButton(
                            onClick = { showAddTracksSheet = true },
                            modifier = Modifier
                                .height(48.dp)
                                .testTag("playlist_add_tracks_button"),
                            border = BorderStroke(1.dp, WarmGold.copy(alpha = 0.55f)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Rounded.Add, contentDescription = "Tambah lagu", tint = WarmGold)
                        }
                    }
                }
            }
        }

        if (tracks.isEmpty()) {
            item {
                Text(
                    text = if (isSystemFavorites) {
                        "Ketuk ikon hati pada lagu mana saja untuk menambahkannya ke Favorit."
                    } else {
                        "Daftar putar ini masih kosong. Ketuk tombol + di atas untuk menambahkan lagu."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = WarmGray,
                    modifier = Modifier.padding(vertical = 32.dp)
                )
            }
        } else {
            items(tracks, key = { it.id }) { track ->
                TrackListItem(
                    track = track,
                    isCurrent = track.id == currentTrackId,
                    isPlaying = isPlaying && track.id == currentTrackId,
                    onClick = { onPlayTrack(track) },
                    onToggleFavorite = { onToggleFavorite(track) },
                    onAddToPlaylist = { onAddToPlaylistSheet(track) },
                    onAddToQueue = { onAddToQueue(track) },
                    onRemoveFromPlaylist = onRemoveTrack?.let { remove -> { remove(track) } }
                )
            }
        }
    }

    if (showAddTracksSheet && !isSystemFavorites) {
        val currentIds = tracks.map { it.id }.toSet()
        val available = allTracks.filterNot { it.id in currentIds }
        ModalBottomSheet(
            onDismissRequest = { showAddTracksSheet = false },
            containerColor = ObsidianSurface,
            contentColor = IvoryText
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 28.dp)
            ) {
                Text(
                    text = "Pilih Lagu untuk Ditambahkan",
                    style = MaterialTheme.typography.headlineSmall,
                    color = IvoryText
                )
                Spacer(modifier = Modifier.height(12.dp))
                if (available.isEmpty()) {
                    Text(
                        text = "Semua lagu di pustaka sudah ada di daftar putar ini.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = WarmGray,
                        modifier = Modifier.padding(vertical = 20.dp)
                    )
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(available, key = { it.id }) { track ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { onAddExistingTrack(track) },
                                color = ObsidianCard,
                                border = BorderStroke(0.75.dp, SubtleOutline)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    AlbumArtwork(track = track, size = 40.dp, shape = RoundedCornerShape(8.dp))
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(track.title, style = MaterialTheme.typography.titleSmall, color = IvoryText)
                                        Text(track.artist, style = MaterialTheme.typography.bodyMedium, color = WarmGray)
                                    }
                                    Icon(Icons.Rounded.Add, contentDescription = "Tambah", tint = WarmGold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showRenameDialog) {
        PlaylistFormDialog(
            dialogTitle = "Ubah Daftar Putar",
            initialName = title,
            initialDescription = subtitle,
            confirmLabel = "Perbarui",
            onDismiss = { showRenameDialog = false },
            onConfirm = { newName, newDesc ->
                onRename(newName, newDesc)
                showRenameDialog = false
            }
        )
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            containerColor = ObsidianElevated,
            title = { Text("Hapus Daftar Putar?", color = IvoryText) },
            text = { Text("Daftar putar \"$title\" akan dihapus. Berkas audio asli tetap aman.", color = WarmGray) },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteConfirm = false
                    onDelete()
                }) {
                    Text("Hapus", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Batal", color = WarmGold)
                }
            }
        )
    }
}

@Composable
fun PlaylistFormDialog(
    dialogTitle: String,
    initialName: String,
    initialDescription: String,
    confirmLabel: String,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var description by remember { mutableStateOf(initialDescription) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ObsidianElevated,
        title = { Text(dialogTitle, color = IvoryText) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama Daftar Putar") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = WarmGold,
                        unfocusedBorderColor = SubtleOutline,
                        focusedTextColor = IvoryText,
                        unfocusedTextColor = IvoryText,
                        focusedLabelColor = WarmGold,
                        unfocusedLabelColor = WarmGray
                    ),
                    modifier = Modifier.testTag("playlist_name_input")
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Catatan (Opsional)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = WarmGold,
                        unfocusedBorderColor = SubtleOutline,
                        focusedTextColor = IvoryText,
                        unfocusedTextColor = IvoryText,
                        focusedLabelColor = WarmGold,
                        unfocusedLabelColor = WarmGray
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (name.isNotBlank()) onConfirm(name, description) },
                enabled = name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = WarmGold, contentColor = OnGoldDark),
                modifier = Modifier.testTag("playlist_save_button")
            ) {
                Text(confirmLabel)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal", color = WarmGray)
            }
        }
    )
}
