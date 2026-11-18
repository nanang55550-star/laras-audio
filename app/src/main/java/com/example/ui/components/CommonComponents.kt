package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.PlaylistAdd
import androidx.compose.material.icons.automirrored.rounded.QueueMusic
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Lyrics
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.data.PlaylistWithTracks
import com.example.model.AudioFormat
import com.example.model.AudioTrack
import com.example.model.SleepTimerMode
import com.example.model.SleepTimerState
import com.example.model.formatDuration
import com.example.ui.theme.BadgeFlac
import com.example.ui.theme.BadgeMp3
import com.example.ui.theme.BadgeOther
import com.example.ui.theme.BadgeWav
import com.example.ui.theme.DimGray
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
import kotlin.math.roundToInt

@Composable
fun AlbumArtwork(
    track: AudioTrack?,
    size: Dp,
    shape: Shape = RoundedCornerShape(14.dp),
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(shape)
            .background(
                Brush.linearGradient(
                    listOf(ObsidianElevated, ObsidianCard)
                )
            )
            .border(1.dp, SubtleOutline, shape),
        contentAlignment = Alignment.Center
    ) {
        when {
            track?.artworkResId != null -> {
                Image(
                    painter = painterResource(id = track.artworkResId),
                    contentDescription = "Sampul album ${track.title}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            !track?.artworkUri.isNullOrBlank() -> {
                AsyncImage(
                    model = track?.artworkUri,
                    contentDescription = "Sampul album ${track?.title.orEmpty()}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            else -> {
                Icon(
                    imageVector = Icons.Rounded.MusicNote,
                    contentDescription = "Ikon musik bawaan",
                    tint = WarmGold.copy(alpha = 0.78f),
                    modifier = Modifier.size(size * 0.45f)
                )
            }
        }
    }
}

@Composable
fun FormatBadge(
    format: AudioFormat,
    modifier: Modifier = Modifier
) {
    val accent = when (format) {
        AudioFormat.FLAC -> BadgeFlac
        AudioFormat.WAV -> BadgeWav
        AudioFormat.MP3 -> BadgeMp3
        else -> BadgeOther
    }
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(6.dp),
        color = accent.copy(alpha = 0.14f),
        border = BorderStroke(0.75.dp, accent.copy(alpha = 0.45f))
    ) {
        Text(
            text = format.label,
            style = MaterialTheme.typography.labelSmall,
            color = accent,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

@Composable
fun TrackListItem(
    track: AudioTrack,
    isCurrent: Boolean,
    isPlaying: Boolean,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    onAddToPlaylist: () -> Unit,
    onAddToQueue: () -> Unit,
    onRemoveFromPlaylist: (() -> Unit)? = null,
    compactStyle: Boolean = false,
    modifier: Modifier = Modifier
) {
    var menuExpanded by remember { mutableStateOf(false) }

    val containerColor = when {
        compactStyle && isCurrent -> WarmGold.copy(alpha = 0.08f)
        compactStyle -> Color.Transparent
        isCurrent -> ObsidianElevated
        else -> ObsidianCard.copy(alpha = 0.78f)
    }
    val containerShape = if (compactStyle) RoundedCornerShape(12.dp) else RoundedCornerShape(16.dp)
    val containerBorder = if (compactStyle) {
        null
    } else {
        BorderStroke(
            width = if (isCurrent) 1.dp else 0.6.dp,
            color = if (isCurrent) WarmGold.copy(alpha = 0.65f) else SubtleOutline.copy(alpha = 0.5f)
        )
    }
    val artSize = if (compactStyle) 48.dp else 54.dp

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(containerShape)
            .clickable(onClick = onClick)
            .testTag("track_item_${track.id}"),
        shape = containerShape,
        color = containerColor,
        border = containerBorder
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = if (compactStyle) 4.dp else 12.dp,
                    vertical = if (compactStyle) 9.dp else 10.dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (compactStyle && isCurrent) {
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height(34.dp)
                        .clip(CircleShape)
                        .background(WarmGold)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }

            Box(contentAlignment = Alignment.Center) {
                AlbumArtwork(
                    track = track,
                    size = artSize,
                    shape = RoundedCornerShape(if (compactStyle) 10.dp else 12.dp)
                )
                if (isCurrent) {
                    Box(
                        modifier = Modifier
                            .size(artSize)
                            .clip(RoundedCornerShape(if (compactStyle) 10.dp else 12.dp))
                            .background(ObsidianVoid.copy(alpha = 0.58f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Rounded.GraphicEq else Icons.Rounded.Pause,
                            contentDescription = if (isPlaying) "Sedang diputar" else "Dijeda",
                            tint = WarmGold,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = track.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isCurrent) SoftGold else IvoryText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(if (compactStyle) 2.dp else 3.dp))
                if (compactStyle) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = track.artist,
                            style = MaterialTheme.typography.bodyMedium,
                            color = WarmGray,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        Text(
                            text = "·",
                            style = MaterialTheme.typography.bodyMedium,
                            color = DimGray
                        )
                        FormatBadge(format = track.format)
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = ObsidianElevated,
                            border = BorderStroke(0.5.dp, SubtleOutline)
                        ) {
                            Text(
                                text = "📁 ${track.folderSource}",
                                style = MaterialTheme.typography.labelSmall,
                                color = SoftGold,
                                maxLines = 1,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                        Text(
                            text = formatDuration(track.durationMs),
                            style = MaterialTheme.typography.labelSmall,
                            color = DimGray
                        )
                        if (!track.lyricsRaw.isNullOrBlank()) {
                            Text(
                                text = "LIRIK",
                                style = MaterialTheme.typography.labelSmall,
                                color = WarmGold.copy(alpha = 0.85f)
                            )
                        }
                    }
                } else {
                    Text(
                        text = track.artist,
                        style = MaterialTheme.typography.bodyMedium,
                        color = WarmGray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(5.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FormatBadge(format = track.format)
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = ObsidianElevated,
                            border = BorderStroke(0.5.dp, SubtleOutline)
                        ) {
                            Text(
                                text = "📁 ${track.folderSource}",
                                style = MaterialTheme.typography.labelSmall,
                                color = SoftGold,
                                maxLines = 1,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                        Text(
                            text = formatDuration(track.durationMs),
                            style = MaterialTheme.typography.labelSmall,
                            color = DimGray
                        )
                        if (!track.lyricsRaw.isNullOrBlank()) {
                            Text(
                                text = "LIRIK",
                                style = MaterialTheme.typography.labelSmall,
                                color = WarmGold.copy(alpha = 0.85f)
                            )
                        }
                    }
                }
            }

            IconButton(
                onClick = onToggleFavorite,
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("favorite_button_${track.id}")
            ) {
                Icon(
                    imageVector = if (track.isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                    contentDescription = if (track.isFavorite) "Hapus dari favorit" else "Tambah ke favorit",
                    tint = if (track.isFavorite) WarmGold else WarmGray,
                    modifier = Modifier.size(20.dp)
                )
            }

            Box {
                IconButton(
                    onClick = { menuExpanded = true },
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("track_more_button_${track.id}")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.MoreVert,
                        contentDescription = "Opsi lainnya untuk ${track.title}",
                        tint = WarmGray,
                        modifier = Modifier.size(20.dp)
                    )
                }

                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                    containerColor = ObsidianElevated
                ) {
                    DropdownMenuItem(
                        text = { Text("Putar Sekarang", color = IvoryText) },
                        leadingIcon = {
                            Icon(Icons.Rounded.PlayArrow, contentDescription = null, tint = WarmGold)
                        },
                        onClick = {
                            menuExpanded = false
                            onClick()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Tambah ke Antrean", color = IvoryText) },
                        leadingIcon = {
                            Icon(Icons.AutoMirrored.Rounded.QueueMusic, contentDescription = null, tint = WarmGold)
                        },
                        onClick = {
                            menuExpanded = false
                            onAddToQueue()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Tambah ke Daftar Putar", color = IvoryText) },
                        leadingIcon = {
                            Icon(Icons.AutoMirrored.Rounded.PlaylistAdd, contentDescription = null, tint = WarmGold)
                        },
                        onClick = {
                            menuExpanded = false
                            onAddToPlaylist()
                        }
                    )
                    if (onRemoveFromPlaylist != null) {
                        DropdownMenuItem(
                            text = { Text("Hapus dari Daftar Putar", color = MaterialTheme.colorScheme.error) },
                            leadingIcon = {
                                Icon(Icons.Rounded.Close, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            },
                            onClick = {
                                menuExpanded = false
                                onRemoveFromPlaylist()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MiniPlayerBar(
    track: AudioTrack,
    isPlaying: Boolean,
    positionMs: Long,
    durationMs: Long,
    sleepTimerState: SleepTimerState,
    onOpenPlayer: () -> Unit,
    onOpenLyrics: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onSkipNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    val safeDuration = durationMs.takeIf { it > 0L } ?: track.durationMs.coerceAtLeast(1L)
    val progress = (positionMs.toFloat() / safeDuration.toFloat()).coerceIn(0f, 1f)

    val pulseTransition = rememberInfiniteTransition(label = "mini_pulse")
    val glowAlpha by pulseTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onOpenPlayer)
            .testTag("mini_player_bar"),
        shape = RoundedCornerShape(18.dp),
        color = ObsidianElevated.copy(alpha = 0.96f),
        border = BorderStroke(
            1.dp,
            WarmGold.copy(alpha = if (isPlaying) glowAlpha * 0.55f else 0.25f)
        ),
        tonalElevation = 8.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.5.dp),
                color = WarmGold,
                trackColor = SubtleOutline.copy(alpha = 0.5f)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AlbumArtwork(
                    track = track,
                    size = 44.dp,
                    shape = RoundedCornerShape(10.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = track.title,
                            style = MaterialTheme.typography.titleSmall,
                            color = IvoryText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        if (sleepTimerState.isActive) {
                            Icon(
                                imageVector = Icons.Rounded.Bedtime,
                                contentDescription = "Sleep timer aktif",
                                tint = WarmGold,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                    Text(
                        text = "${track.artist} · ${track.format.label}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = WarmGray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(
                    onClick = onOpenLyrics,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("mini_player_lyrics_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Lyrics,
                        contentDescription = "Buka lirik layar penuh",
                        tint = SoftGold
                    )
                }

                IconButton(
                    onClick = onTogglePlayPause,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .background(WarmGold, CircleShape)
                        .size(40.dp)
                        .testTag("mini_player_play_pause")
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                        contentDescription = if (isPlaying) "Jeda" else "Putar",
                        tint = OnGoldDark
                    )
                }

                IconButton(
                    onClick = onSkipNext,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("mini_player_next")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.SkipNext,
                        contentDescription = "Lagu berikutnya",
                        tint = IvoryText
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddToPlaylistSheet(
    track: AudioTrack,
    playlists: List<PlaylistWithTracks>,
    onDismiss: () -> Unit,
    onSelectPlaylist: (PlaylistWithTracks) -> Unit,
    onCreateNewPlaylist: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
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
                text = "Tambah ke Daftar Putar",
                style = MaterialTheme.typography.headlineSmall,
                color = IvoryText
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${track.title} · ${track.artist}",
                style = MaterialTheme.typography.bodyMedium,
                color = WarmGray,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onCreateNewPlaylist,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("sheet_create_playlist_button"),
                colors = ButtonDefaults.buttonColors(containerColor = WarmGold, contentColor = OnGoldDark),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Rounded.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Buat Daftar Putar Baru", style = MaterialTheme.typography.labelLarge)
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (playlists.isEmpty()) {
                Text(
                    text = "Belum ada daftar putar. Buat satu sekarang untuk mengelompokkan lagu favorit Anda.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = WarmGray,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(playlists, key = { it.id }) { playlist ->
                        val alreadyInside = track.id in playlist.trackIds
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .clickable(enabled = !alreadyInside) { onSelectPlaylist(playlist) },
                            color = ObsidianCard,
                            border = BorderStroke(0.75.dp, SubtleOutline)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = playlist.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = IvoryText
                                    )
                                    Text(
                                        text = "${playlist.tracks.size} lagu",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = WarmGray
                                    )
                                }
                                Text(
                                    text = if (alreadyInside) "Sudah Ada" else "Tambah",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (alreadyInside) DimGray else WarmGold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SleepTimerSheet(
    sleepTimerState: SleepTimerState,
    onDismiss: () -> Unit,
    onSelectMinutes: (Int) -> Unit,
    onSelectEndOfTrack: () -> Unit,
    onCancelTimer: () -> Unit
) {
    var customMinutes by remember { mutableFloatStateOf(25f) }
    val presets = listOf(10, 15, 30, 45, 60)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = ObsidianSurface,
        contentColor = IvoryText
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Bedtime, contentDescription = null, tint = WarmGold)
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Sleep Timer",
                    style = MaterialTheme.typography.headlineSmall,
                    color = IvoryText
                )
            }

            Text(
                text = "Volume akan meredup perlahan dalam 10 detik terakhir sebelum musik dijeda otomatis.",
                style = MaterialTheme.typography.bodyMedium,
                color = WarmGray
            )

            if (sleepTimerState.isActive) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = WarmGold.copy(alpha = 0.14f),
                    border = BorderStroke(1.dp, WarmGold.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = if (sleepTimerState.mode == SleepTimerMode.END_OF_TRACK) {
                                    "Berhenti di akhir lagu ini"
                                } else {
                                    "Sisa waktu: ${formatDuration(sleepTimerState.remainingMs)}"
                                },
                                style = MaterialTheme.typography.titleMedium,
                                color = SoftGold
                            )
                            if (sleepTimerState.isFadingOut) {
                                Text(
                                    text = "Meredupkan suara secara halus…",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = WarmGold
                                )
                            }
                        }
                        OutlinedButton(
                            onClick = {
                                onCancelTimer()
                                onDismiss()
                            },
                            border = BorderStroke(1.dp, WarmGold),
                            modifier = Modifier.testTag("sleep_timer_cancel_button")
                        ) {
                            Text("Matikan", color = WarmGold)
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                presets.forEach { mins ->
                    FilledTonalButton(
                        onClick = {
                            onSelectMinutes(mins)
                            onDismiss()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("sleep_preset_$mins"),
                        contentPadding = PaddingValues(horizontal = 4.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = ObsidianCard,
                            contentColor = IvoryText
                        )
                    ) {
                        Text("${mins}m", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }

            OutlinedButton(
                onClick = {
                    onSelectEndOfTrack()
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("sleep_end_of_track"),
                border = BorderStroke(1.dp, SubtleOutline),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Berhenti Setelah Lagu Ini Selesai", color = SoftGold)
            }

            HorizontalDivider(color = SubtleOutline.copy(alpha = 0.6f))

            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Durasi Kustom",
                        style = MaterialTheme.typography.titleSmall,
                        color = IvoryText
                    )
                    Text(
                        text = "${customMinutes.roundToInt()} menit",
                        style = MaterialTheme.typography.labelMedium,
                        color = WarmGold
                    )
                }
                Slider(
                    value = customMinutes,
                    onValueChange = { customMinutes = it },
                    valueRange = 5f..120f,
                    colors = SliderDefaults.colors(
                        thumbColor = WarmGold,
                        activeTrackColor = WarmGold,
                        inactiveTrackColor = ObsidianElevated
                    )
                )
                Button(
                    onClick = {
                        onSelectMinutes(customMinutes.roundToInt())
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("sleep_apply_custom"),
                    colors = ButtonDefaults.buttonColors(containerColor = WarmGold, contentColor = OnGoldDark),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Aktifkan ${customMinutes.roundToInt()} Menit", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QueueSheet(
    queueTracks: List<AudioTrack>,
    currentQueueIndex: Int,
    onDismiss: () -> Unit,
    onJumpToIndex: (Int) -> Unit,
    onRemoveAtIndex: (Int) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
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
                text = "Antrean Putar (${queueTracks.size})",
                style = MaterialTheme.typography.headlineSmall,
                color = IvoryText
            )
            Spacer(modifier = Modifier.height(12.dp))
            if (queueTracks.isEmpty()) {
                Text(
                    text = "Belum ada lagu di dalam antrean.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = WarmGray,
                    modifier = Modifier.padding(vertical = 24.dp)
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    itemsIndexed(queueTracks) { index, track ->
                        val active = index == currentQueueIndex
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { onJumpToIndex(index) },
                            color = if (active) ObsidianElevated else ObsidianCard,
                            border = BorderStroke(
                                0.75.dp,
                                if (active) WarmGold.copy(alpha = 0.7f) else SubtleOutline
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AlbumArtwork(track = track, size = 42.dp, shape = RoundedCornerShape(8.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = track.title,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = if (active) SoftGold else IvoryText,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${track.artist} · ${track.format.label}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = WarmGray,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                if (queueTracks.size > 1 && !active) {
                                    IconButton(
                                        onClick = { onRemoveAtIndex(index) },
                                        modifier = Modifier.minimumInteractiveComponentSize()
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Close,
                                            contentDescription = "Hapus dari antrean",
                                            tint = WarmGray,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
