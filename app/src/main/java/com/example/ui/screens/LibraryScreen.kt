package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.model.AudioTrack
import com.example.model.FormatFilter
import com.example.ui.components.TrackListItem
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
fun LibraryScreen(
    tracks: List<AudioTrack>,
    totalCount: Int,
    currentTrackId: String?,
    isPlaying: Boolean,
    searchQuery: String,
    selectedFilter: FormatFilter,
    bottomContentPadding: PaddingValues,
    onSearchQueryChange: (String) -> Unit,
    onSelectFilter: (FormatFilter) -> Unit,
    onOpenSettings: () -> Unit,
    onPlayTrack: (AudioTrack) -> Unit,
    onPlayAll: () -> Unit,
    onShuffleAll: () -> Unit,
    onToggleFavorite: (AudioTrack) -> Unit,
    onAddToPlaylist: (AudioTrack) -> Unit,
    onAddToQueue: (AudioTrack) -> Unit,
    modifier: Modifier = Modifier
) {
    var isSearchExpanded by rememberSaveable { mutableStateOf(false) }
    val searchVisible = isSearchExpanded || searchQuery.isNotEmpty()
    val focusRequester = remember { FocusRequester() }

    BackHandler(enabled = searchVisible) {
        onSearchQueryChange("")
        isSearchExpanded = false
    }

    LaunchedEffect(isSearchExpanded) {
        if (isSearchExpanded) {
            runCatching { focusRequester.requestFocus() }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        WarmGold.copy(alpha = 0.08f),
                        ObsidianVoid,
                        ObsidianVoid
                    )
                )
            ),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 10.dp,
            bottom = bottomContentPadding.calculateBottomPadding() + 16.dp
        )
    ) {
        // Compact Editorial Header with Search Toggle & Settings
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Laras Audio",
                        style = MaterialTheme.typography.headlineMedium,
                        color = IvoryText
                    )
                    Text(
                        text = "$totalCount Lagu Offline · MP3 · FLAC · WAV",
                        style = MaterialTheme.typography.labelSmall,
                        color = WarmGold
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = if (searchVisible) WarmGold.copy(alpha = 0.18f) else ObsidianCard,
                        border = BorderStroke(
                            1.dp,
                            if (searchVisible) WarmGold.copy(alpha = 0.6f) else SubtleOutline
                        )
                    ) {
                        IconButton(
                            onClick = {
                                if (searchVisible) {
                                    onSearchQueryChange("")
                                    isSearchExpanded = false
                                } else {
                                    isSearchExpanded = true
                                }
                            },
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("library_search_toggle")
                        ) {
                            Icon(
                                imageVector = if (searchVisible) Icons.Rounded.Close else Icons.Rounded.Search,
                                contentDescription = if (searchVisible) "Tutup pencarian" else "Cari lagu",
                                tint = if (searchVisible) WarmGold else SoftGold
                            )
                        }
                    }

                    Surface(
                        shape = CircleShape,
                        color = ObsidianCard,
                        border = BorderStroke(1.dp, SubtleOutline)
                    ) {
                        IconButton(
                            onClick = onOpenSettings,
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("library_settings_button")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Settings,
                                contentDescription = "Buka pengaturan aplikasi",
                                tint = SoftGold
                            )
                        }
                    }
                }
            }
        }

        // Expandable Search Bar
        item {
            AnimatedVisibility(
                visible = searchVisible,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                        .focusRequester(focusRequester)
                        .testTag("library_search_input"),
                    placeholder = {
                        Text("Cari judul lagu, artis, atau album…", color = WarmGray)
                    },
                    leadingIcon = {
                        Icon(Icons.Rounded.Search, contentDescription = null, tint = WarmGold)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchQueryChange("") }) {
                                Icon(Icons.Rounded.Close, contentDescription = "Bersihkan pencarian", tint = WarmGray)
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = WarmGold,
                        unfocusedBorderColor = SubtleOutline,
                        focusedContainerColor = ObsidianCard,
                        unfocusedContainerColor = ObsidianCard,
                        focusedTextColor = IvoryText,
                        unfocusedTextColor = IvoryText
                    )
                )
            }
        }

        // Format Filter Chips
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp)
            ) {
                items(FormatFilter.entries) { filter ->
                    val selected = filter == selectedFilter
                    FilterChip(
                        selected = selected,
                        onClick = { onSelectFilter(filter) },
                        label = {
                            Text(
                                text = filter.label,
                                style = MaterialTheme.typography.labelLarge
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = WarmGold,
                            selectedLabelColor = OnGoldDark,
                            containerColor = ObsidianCard,
                            labelColor = WarmGray
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = selected,
                            borderColor = SubtleOutline,
                            selectedBorderColor = WarmGold
                        ),
                        modifier = Modifier.testTag("format_filter_${filter.name}")
                    )
                }
            }
        }

        // Compact Control Strip: Track count + Play / Shuffle pills
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${tracks.size} Trek",
                    style = MaterialTheme.typography.labelMedium,
                    color = WarmGray
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onPlayAll,
                        enabled = tracks.isNotEmpty(),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        border = BorderStroke(1.dp, WarmGold.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .height(40.dp)
                            .testTag("play_all_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.PlayArrow,
                            contentDescription = null,
                            tint = SoftGold,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Putar", style = MaterialTheme.typography.labelLarge, color = SoftGold)
                    }

                    OutlinedButton(
                        onClick = onShuffleAll,
                        enabled = tracks.isNotEmpty(),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        border = BorderStroke(1.dp, SubtleOutline),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .height(40.dp)
                            .testTag("shuffle_all_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Shuffle,
                            contentDescription = null,
                            tint = IvoryText,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Acak", style = MaterialTheme.typography.labelLarge, color = IvoryText)
                    }
                }
            }
        }

        item {
            HorizontalDivider(
                thickness = 0.6.dp,
                color = SubtleOutline.copy(alpha = 0.6f),
                modifier = Modifier.padding(bottom = 2.dp)
            )
        }

        if (tracks.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 44.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Rounded.LibraryMusic,
                            contentDescription = null,
                            tint = WarmGold.copy(alpha = 0.55f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Tidak ada lagu yang cocok",
                            style = MaterialTheme.typography.titleMedium,
                            color = IvoryText
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Ubah filter format atau pindai musik perangkat lewat menu Pengaturan.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = WarmGray
                        )
                    }
                }
            }
        } else {
            itemsIndexed(tracks, key = { _, track -> track.id }) { index, track ->
                TrackListItem(
                    track = track,
                    isCurrent = track.id == currentTrackId,
                    isPlaying = isPlaying && track.id == currentTrackId,
                    onClick = { onPlayTrack(track) },
                    onToggleFavorite = { onToggleFavorite(track) },
                    onAddToPlaylist = { onAddToPlaylist(track) },
                    onAddToQueue = { onAddToQueue(track) },
                    compactStyle = true
                )
                if (index < tracks.lastIndex) {
                    HorizontalDivider(
                        thickness = 0.5.dp,
                        color = SubtleOutline.copy(alpha = 0.5f),
                        modifier = Modifier.padding(start = 64.dp)
                    )
                }
            }
        }
    }
}
