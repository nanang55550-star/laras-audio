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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AudioFile
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.BuildConfig
import com.example.model.SleepTimerMode
import com.example.model.SleepTimerState
import com.example.model.formatDuration
import com.example.ui.theme.BadgeWav
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
fun SettingsScreen(
    hasAudioPermission: Boolean,
    isScanning: Boolean,
    totalTracksCount: Int,
    equalizerEnabled: Boolean,
    equalizerPresetName: String,
    sleepTimerState: SleepTimerState,
    onBack: () -> Unit,
    onRequestScanPermission: () -> Unit,
    onRescanDevice: () -> Unit,
    onImportAudioFile: () -> Unit,
    onOpenEqualizer: () -> Unit,
    onOpenSleepTimer: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBack)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(WarmGold.copy(alpha = 0.08f), ObsidianVoid, ObsidianVoid)
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("settings_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Kembali ke Pustaka",
                        tint = IvoryText
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        text = "Pengaturan",
                        style = MaterialTheme.typography.headlineMedium,
                        color = IvoryText
                    )
                    Text(
                        text = "Penyimpanan lokal, sumber audio & preferensi",
                        style = MaterialTheme.typography.bodyMedium,
                        color = WarmGray
                    )
                }
            }
        }

        // Local Storage & Audio Access Section
        item {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = ObsidianCard,
                border = BorderStroke(1.dp, WarmGold.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(WarmGold.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Storage,
                                    contentDescription = null,
                                    tint = WarmGold
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Akses Musik Penyimpanan Lokal",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = SoftGold
                                )
                                Text(
                                    text = "$totalTracksCount trek tersedia di pustaka",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = WarmGray
                                )
                            }
                        }

                        if (isScanning) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = WarmGold,
                                strokeWidth = 2.dp
                            )
                        }
                    }

                    // Permission status chip
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = ObsidianElevated,
                        border = BorderStroke(0.75.dp, SubtleOutline),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (hasAudioPermission) Icons.Rounded.CheckCircle else Icons.Rounded.Lock,
                                contentDescription = null,
                                tint = if (hasAudioPermission) BadgeWav else WarmGold,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (hasAudioPermission) {
                                        "Izin Akses Audio Aktif"
                                    } else {
                                        "Izin Akses Audio Belum Diaktifkan"
                                    },
                                    style = MaterialTheme.typography.titleSmall,
                                    color = IvoryText
                                )
                                Text(
                                    text = if (hasAudioPermission) {
                                        "Aplikasi dapat memindai MP3, FLAC, WAV, M4A, dan OGG di perangkat."
                                    } else {
                                        "Izinkan pemindaian audio perangkat atau gunakan Impor File secara selektif."
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = WarmGray
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = if (hasAudioPermission) onRescanDevice else onRequestScanPermission,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("scan_device_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = WarmGold,
                                contentColor = OnGoldDark
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (hasAudioPermission) "Pindai Ulang" else "Pindai Perangkat",
                                style = MaterialTheme.typography.labelLarge
                            )
                        }

                        OutlinedButton(
                            onClick = onImportAudioFile,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("import_audio_button"),
                            border = BorderStroke(1.dp, WarmGold.copy(alpha = 0.55f)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.FolderOpen,
                                contentDescription = null,
                                tint = SoftGold,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Impor File", color = SoftGold, style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }
        }

        // Quick Audio Preferences
        item {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = ObsidianCard,
                border = BorderStroke(1.dp, SubtleOutline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Kontrol Audio & Lirik",
                        style = MaterialTheme.typography.titleMedium,
                        color = IvoryText
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    SettingsActionRow(
                        icon = Icons.Rounded.GraphicEq,
                        title = "Equalizer Kustom",
                        subtitle = if (equalizerEnabled) "Aktif · Preset $equalizerPresetName" else "Nonaktif · Ketuk untuk mengatur",
                        onClick = onOpenEqualizer,
                        testTag = "settings_equalizer_row"
                    )

                    HorizontalDivider(
                        color = SubtleOutline.copy(alpha = 0.6f),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    SettingsActionRow(
                        icon = Icons.Rounded.Bedtime,
                        title = "Sleep Timer",
                        subtitle = when (sleepTimerState.mode) {
                            SleepTimerMode.OFF -> "Mati · Ketuk untuk menjadwalkan"
                            SleepTimerMode.END_OF_TRACK -> "Aktif · Berhenti setelah lagu ini"
                            SleepTimerMode.COUNTDOWN -> "Aktif · Sisa ${formatDuration(sleepTimerState.remainingMs)}"
                        },
                        onClick = onOpenSleepTimer,
                        testTag = "settings_sleep_timer_row"
                    )

                    HorizontalDivider(
                        color = SubtleOutline.copy(alpha = 0.6f),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.AudioFile,
                            contentDescription = null,
                            tint = WarmGold,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Format Audio & Lirik Didukung",
                                style = MaterialTheme.typography.titleSmall,
                                color = IvoryText
                            )
                            Text(
                                text = "MP3, FLAC, WAV, M4A, OGG, serta file lirik .LRC (otomatis jika bernama sama atau impor di layar Lirik).",
                                style = MaterialTheme.typography.bodyMedium,
                                color = WarmGray
                            )
                        }
                    }
                }
            }
        }

        // About Section
        item {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = ObsidianCard.copy(alpha = 0.8f),
                border = BorderStroke(0.75.dp, SubtleOutline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Info,
                        contentDescription = null,
                        tint = WarmGold,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Laras Audio · v${BuildConfig.VERSION_NAME}",
                            style = MaterialTheme.typography.titleSmall,
                            color = IvoryText
                        )
                        Text(
                            text = "Pemutar musik offline murni tanpa koneksi internet maupun iklan.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = WarmGray
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsActionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 4.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = WarmGold,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.titleSmall, color = IvoryText)
            Text(text = subtitle, style = MaterialTheme.typography.bodyMedium, color = WarmGray)
        }
        Text(
            text = "Buka",
            style = MaterialTheme.typography.labelLarge,
            color = WarmGold
        )
    }
}
