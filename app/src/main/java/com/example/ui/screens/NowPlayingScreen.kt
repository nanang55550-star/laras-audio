package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.PlaylistAdd
import androidx.compose.material.icons.automirrored.rounded.QueueMusic
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.Equalizer
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Lyrics
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.RepeatOne
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.media3.common.Player
import com.example.model.AudioTrack
import com.example.model.LyricLine
import com.example.model.SleepTimerMode
import com.example.model.SleepTimerState
import com.example.model.formatDuration
import com.example.ui.components.AlbumArtwork
import com.example.ui.components.FormatBadge
import com.example.ui.theme.IvoryText
import com.example.ui.theme.ObsidianCard
import com.example.ui.theme.ObsidianElevated
import com.example.ui.theme.ObsidianVoid
import com.example.ui.theme.OnGoldDark
import com.example.ui.theme.SoftGold
import com.example.ui.theme.SubtleOutline
import com.example.ui.theme.WarmGold
import com.example.ui.theme.WarmGray

@Composable
fun NowPlayingScreen(
    track: AudioTrack,
    isPlaying: Boolean,
    positionMs: Long,
    durationMs: Long,
    shuffleEnabled: Boolean,
    repeatMode: Int,
    sleepTimerState: SleepTimerState,
    lyrics: List<LyricLine>,
    activeLyricIndex: Int,
    onClose: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    onToggleShuffle: () -> Unit,
    onCycleRepeat: () -> Unit,
    onToggleFavorite: () -> Unit,
    onOpenLyrics: () -> Unit,
    onOpenEqualizer: () -> Unit,
    onOpenSleepTimer: () -> Unit,
    onOpenQueue: () -> Unit,
    onAddToPlaylist: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onClose)

    val effectiveDuration = durationMs.takeIf { it > 0L } ?: track.durationMs.coerceAtLeast(1L)
    var isDragging by remember { mutableStateOf(false) }
    var dragFraction by remember { mutableFloatStateOf(0f) }

    val activeLineText = lyrics.getOrNull(activeLyricIndex)?.text
        ?: lyrics.firstOrNull()?.text
        ?: "Ketuk untuk melihat atau memuat lirik (.lrc)"

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        WarmGold.copy(alpha = 0.16f),
                        ObsidianVoid,
                        ObsidianVoid
                    )
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onClose,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("now_playing_close")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.KeyboardArrowDown,
                        contentDescription = "Tutup layar pemutar",
                        tint = IvoryText,
                        modifier = Modifier.size(30.dp)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "SEDANG DIPUTAR",
                        style = MaterialTheme.typography.labelSmall,
                        color = WarmGold
                    )
                    Text(
                        text = track.album,
                        style = MaterialTheme.typography.bodyMedium,
                        color = WarmGray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(
                    onClick = onAddToPlaylist,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("now_playing_add_playlist")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.PlaylistAdd,
                        contentDescription = "Tambah ke daftar putar",
                        tint = IvoryText
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Framed Album Artwork with Gold Halo
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = ObsidianCard,
                border = BorderStroke(1.5.dp, WarmGold.copy(alpha = 0.45f)),
                shadowElevation = 18.dp,
                modifier = Modifier.size(276.dp)
            ) {
                AlbumArtwork(
                    track = track,
                    size = 276.dp,
                    shape = RoundedCornerShape(28.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Title + Favorite + Quality Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = track.title,
                        style = MaterialTheme.typography.headlineMedium,
                        color = IvoryText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = track.artist,
                        style = MaterialTheme.typography.titleMedium,
                        color = WarmGray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("now_playing_favorite")
                ) {
                    Icon(
                        imageVector = if (track.isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                        contentDescription = if (track.isFavorite) "Hapus dari favorit" else "Tambah ke favorit",
                        tint = if (track.isFavorite) WarmGold else WarmGray,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FormatBadge(format = track.format)
                Text(
                    text = track.qualityLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = WarmGray
                )
                if (sleepTimerState.isActive) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = WarmGold.copy(alpha = 0.16f),
                        modifier = Modifier.clickable(onClick = onOpenSleepTimer)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Rounded.Bedtime, contentDescription = null, tint = WarmGold, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (sleepTimerState.mode == SleepTimerMode.END_OF_TRACK) {
                                    "Akhir Lagu"
                                } else {
                                    formatDuration(sleepTimerState.remainingMs)
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = SoftGold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Interactive Synced Lyrics Preview Card (opens Full-Screen Interactive Lyrics)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .clickable(onClick = onOpenLyrics)
                    .testTag("now_playing_open_lyrics_card"),
                shape = RoundedCornerShape(16.dp),
                color = ObsidianCard.copy(alpha = 0.88f),
                border = BorderStroke(1.dp, WarmGold.copy(alpha = 0.35f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Lyrics,
                        contentDescription = null,
                        tint = WarmGold,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = activeLineText,
                            style = MaterialTheme.typography.bodyLarge,
                            color = SoftGold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Buka Lirik Layar Penuh (Ketuk untuk lompat durasi)",
                            style = MaterialTheme.typography.labelSmall,
                            color = WarmGray
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Seekbar + Mono Timestamps
            val sliderVal = if (isDragging) dragFraction else (positionMs.toFloat() / effectiveDuration.toFloat()).coerceIn(0f, 1f)
            Slider(
                value = sliderVal,
                onValueChange = {
                    isDragging = true
                    dragFraction = it
                },
                onValueChangeFinished = {
                    isDragging = false
                    onSeekTo((dragFraction * effectiveDuration).toLong())
                },
                colors = SliderDefaults.colors(
                    thumbColor = SoftGold,
                    activeTrackColor = WarmGold,
                    inactiveTrackColor = ObsidianElevated
                ),
                modifier = Modifier.testTag("now_playing_seekbar")
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = formatDuration(if (isDragging) (dragFraction * effectiveDuration).toLong() else positionMs),
                    style = MaterialTheme.typography.labelMedium,
                    color = WarmGray
                )
                Text(
                    text = formatDuration(effectiveDuration),
                    style = MaterialTheme.typography.labelMedium,
                    color = WarmGray
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Main Transport Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onToggleShuffle,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("now_playing_shuffle")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Shuffle,
                        contentDescription = if (shuffleEnabled) "Acak aktif" else "Acak nonaktif",
                        tint = if (shuffleEnabled) WarmGold else WarmGray
                    )
                }

                IconButton(
                    onClick = onSkipPrevious,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("now_playing_previous")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.SkipPrevious,
                        contentDescription = "Lagu sebelumnya",
                        tint = IvoryText,
                        modifier = Modifier.size(34.dp)
                    )
                }

                IconButton(
                    onClick = onTogglePlayPause,
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(WarmGold)
                        .testTag("now_playing_play_pause")
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                        contentDescription = if (isPlaying) "Jeda" else "Putar",
                        tint = OnGoldDark,
                        modifier = Modifier.size(34.dp)
                    )
                }

                IconButton(
                    onClick = onSkipNext,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("now_playing_next")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.SkipNext,
                        contentDescription = "Lagu berikutnya",
                        tint = IvoryText,
                        modifier = Modifier.size(34.dp)
                    )
                }

                IconButton(
                    onClick = onCycleRepeat,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("now_playing_repeat")
                ) {
                    Icon(
                        imageVector = if (repeatMode == Player.REPEAT_MODE_ONE) Icons.Rounded.RepeatOne else Icons.Rounded.Repeat,
                        contentDescription = when (repeatMode) {
                            Player.REPEAT_MODE_ONE -> "Ulangi satu lagu"
                            Player.REPEAT_MODE_ALL -> "Ulangi semua lagu"
                            else -> "Pengulangan mati"
                        },
                        tint = if (repeatMode != Player.REPEAT_MODE_OFF) WarmGold else WarmGray
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Quick Action Pills Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                QuickToolButton(
                    icon = Icons.Rounded.Lyrics,
                    label = "Lirik",
                    active = lyrics.isNotEmpty(),
                    onClick = onOpenLyrics,
                    testTag = "now_playing_lyrics_btn"
                )
                QuickToolButton(
                    icon = Icons.Rounded.Equalizer,
                    label = "Equalizer",
                    active = false,
                    onClick = onOpenEqualizer,
                    testTag = "now_playing_eq_btn"
                )
                QuickToolButton(
                    icon = Icons.Rounded.Bedtime,
                    label = "Timer",
                    active = sleepTimerState.isActive,
                    onClick = onOpenSleepTimer,
                    testTag = "now_playing_timer_btn"
                )
                QuickToolButton(
                    icon = Icons.AutoMirrored.Rounded.QueueMusic,
                    label = "Antrean",
                    active = false,
                    onClick = onOpenQueue,
                    testTag = "now_playing_queue_btn"
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun QuickToolButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    active: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .testTag(testTag),
        shape = RoundedCornerShape(14.dp),
        color = if (active) WarmGold.copy(alpha = 0.16f) else ObsidianCard,
        border = BorderStroke(0.75.dp, if (active) WarmGold.copy(alpha = 0.5f) else SubtleOutline)
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (active) WarmGold else IvoryText,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = if (active) SoftGold else WarmGray,
                textAlign = TextAlign.Center
            )
        }
    }
}
