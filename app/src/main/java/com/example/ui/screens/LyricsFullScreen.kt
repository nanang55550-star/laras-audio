package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FileOpen
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Lyrics
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AudioTrack
import com.example.model.LyricLine
import com.example.model.formatDuration
import com.example.ui.components.AlbumArtwork
import com.example.ui.theme.DimGray
import com.example.ui.theme.IvoryText
import com.example.ui.theme.ObsidianCard
import com.example.ui.theme.ObsidianElevated
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.ObsidianVoid
import com.example.ui.theme.OnGoldDark
import com.example.ui.theme.PlayfairDisplayFamily
import com.example.ui.theme.SoftGold
import com.example.ui.theme.SubtleOutline
import com.example.ui.theme.WarmGold
import com.example.ui.theme.WarmGray

@Composable
fun LyricsFullScreen(
    track: AudioTrack,
    lyrics: List<LyricLine>,
    activeIndex: Int,
    isPlaying: Boolean,
    positionMs: Long,
    durationMs: Long,
    onClose: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onTogglePlayPause: () -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    onImportLrcFile: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onClose)

    val listState = rememberLazyListState()
    val effectiveDuration = durationMs.takeIf { it > 0L } ?: track.durationMs.coerceAtLeast(1L)
    var isDraggingSeek by remember { mutableStateOf(false) }
    var dragFraction by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(activeIndex, lyrics.size) {
        if (activeIndex >= 0 && activeIndex < lyrics.size) {
            listState.animateScrollToItem(
                index = activeIndex,
                scrollOffset = -220
            )
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        ObsidianSurface,
                        ObsidianVoid,
                        WarmGold.copy(alpha = 0.08f)
                    )
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Top Header Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("lyrics_close_button")
            ) {
                Icon(
                    imageVector = Icons.Rounded.KeyboardArrowDown,
                    contentDescription = "Tutup lirik layar penuh",
                    tint = IvoryText,
                    modifier = Modifier.size(30.dp)
                )
            }

            AlbumArtwork(track = track, size = 40.dp, shape = RoundedCornerShape(10.dp))
            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = track.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = IvoryText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = track.artist,
                    style = MaterialTheme.typography.bodyMedium,
                    color = WarmGray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            OutlinedButton(
                onClick = onImportLrcFile,
                border = BorderStroke(1.dp, WarmGold.copy(alpha = 0.55f)),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .height(40.dp)
                    .testTag("lyrics_import_lrc_button")
            ) {
                Icon(
                    imageVector = Icons.Rounded.FileOpen,
                    contentDescription = null,
                    tint = SoftGold,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(".LRC", style = MaterialTheme.typography.labelLarge, color = SoftGold)
            }
        }

        // Interactive Hint Pill
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = ObsidianCard.copy(alpha = 0.85f),
                border = BorderStroke(0.75.dp, SubtleOutline)
            ) {
                Text(
                    text = "Ketuk baris lirik mana saja untuk melompat ke durasi tersebut",
                    style = MaterialTheme.typography.labelSmall,
                    color = WarmGold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                )
            }
        }

        // Lyrics Scroll Area
        if (lyrics.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 28.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Lyrics,
                        contentDescription = null,
                        tint = WarmGold.copy(alpha = 0.6f),
                        modifier = Modifier.size(56.dp)
                    )
                    Text(
                        text = "Belum Ada Lirik Tersinkronisasi",
                        style = MaterialTheme.typography.headlineSmall,
                        color = IvoryText
                    )
                    Text(
                        text = "Untuk lagu lokal tanpa file .lrc pendamping, Anda dapat memuat file lirik (.lrc) secara langsung dari perangkat.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = WarmGray
                    )
                    Button(
                        onClick = onImportLrcFile,
                        colors = ButtonDefaults.buttonColors(containerColor = WarmGold, contentColor = OnGoldDark),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("lyrics_empty_import_button")
                    ) {
                        Icon(Icons.Rounded.FileOpen, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Muat File Lirik (.lrc)", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 96.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                itemsIndexed(lyrics) { index, line ->
                    val isActive = index == activeIndex
                    val isPassed = activeIndex >= 0 && index < activeIndex

                    val textColor by animateColorAsState(
                        targetValue = when {
                            isActive -> SoftGold
                            isPassed -> WarmGray.copy(alpha = 0.72f)
                            else -> DimGray
                        },
                        label = "lyric_color"
                    )
                    val scale by animateFloatAsState(
                        targetValue = if (isActive) 1.04f else 0.97f,
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                        label = "lyric_scale"
                    )

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                            }
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { onSeekTo(line.timestampMs) }
                            .testTag("lyric_line_$index"),
                        shape = RoundedCornerShape(16.dp),
                        color = if (isActive) WarmGold.copy(alpha = 0.12f) else androidx.compose.ui.graphics.Color.Transparent,
                        border = if (isActive) BorderStroke(1.dp, WarmGold.copy(alpha = 0.35f)) else null
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = line.text,
                                fontFamily = PlayfairDisplayFamily,
                                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                                fontSize = if (isActive) 23.sp else 19.sp,
                                lineHeight = if (isActive) 31.sp else 26.sp,
                                color = textColor,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = formatDuration(line.timestampMs),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isActive) WarmGold else DimGray.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }
        }

        // Bottom Compact Playback Controls
        Surface(
            color = ObsidianSurface.copy(alpha = 0.94f),
            border = BorderStroke(0.75.dp, SubtleOutline),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                val sliderVal = if (isDraggingSeek) dragFraction else (positionMs.toFloat() / effectiveDuration.toFloat()).coerceIn(0f, 1f)
                Slider(
                    value = sliderVal,
                    onValueChange = {
                        isDraggingSeek = true
                        dragFraction = it
                    },
                    onValueChangeFinished = {
                        isDraggingSeek = false
                        onSeekTo((dragFraction * effectiveDuration).toLong())
                    },
                    colors = SliderDefaults.colors(
                        thumbColor = WarmGold,
                        activeTrackColor = WarmGold,
                        inactiveTrackColor = ObsidianElevated
                    )
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = formatDuration(if (isDraggingSeek) (dragFraction * effectiveDuration).toLong() else positionMs),
                        style = MaterialTheme.typography.labelSmall,
                        color = WarmGray
                    )
                    Text(
                        text = formatDuration(effectiveDuration),
                        style = MaterialTheme.typography.labelSmall,
                        color = WarmGray
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onSkipPrevious,
                        modifier = Modifier.minimumInteractiveComponentSize()
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.SkipPrevious,
                            contentDescription = "Lagu sebelumnya",
                            tint = IvoryText,
                            modifier = Modifier.size(30.dp)
                        )
                    }

                    IconButton(
                        onClick = onTogglePlayPause,
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(WarmGold)
                            .testTag("lyrics_play_pause_button")
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                            contentDescription = if (isPlaying) "Jeda" else "Putar",
                            tint = OnGoldDark,
                            modifier = Modifier.size(30.dp)
                        )
                    }

                    IconButton(
                        onClick = onSkipNext,
                        modifier = Modifier.minimumInteractiveComponentSize()
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.SkipNext,
                            contentDescription = "Lagu berikutnya",
                            tint = IvoryText,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }
            }
        }
    }
}
