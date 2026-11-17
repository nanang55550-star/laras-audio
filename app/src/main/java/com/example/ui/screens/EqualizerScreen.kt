package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BookmarkAdd
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.model.EqualizerBand
import com.example.model.EqualizerPreset
import com.example.model.EqualizerState
import com.example.ui.theme.DimGray
import com.example.ui.theme.IvoryText
import com.example.ui.theme.ObsidianCard
import com.example.ui.theme.ObsidianElevated
import com.example.ui.theme.ObsidianVoid
import com.example.ui.theme.OnGoldDark
import com.example.ui.theme.SoftGold
import com.example.ui.theme.SubtleOutline
import com.example.ui.theme.WarmGold
import com.example.ui.theme.WarmGray
import kotlin.math.roundToInt

@Composable
fun EqualizerScreen(
    state: EqualizerState,
    bottomContentPadding: PaddingValues,
    onToggleEnabled: (Boolean) -> Unit,
    onBandLevelChange: (Int, Int) -> Unit,
    onSelectPreset: (EqualizerPreset) -> Unit,
    onBassBoostChange: (Int) -> Unit,
    onVirtualizerChange: (Int) -> Unit,
    onSaveCustomPreset: (String) -> Unit,
    onDeleteCustomPreset: (EqualizerPreset) -> Unit,
    modifier: Modifier = Modifier
) {
    var showSavePresetDialog by remember { mutableStateOf(false) }

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
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Studio Suara",
                        style = MaterialTheme.typography.displaySmall,
                        color = IvoryText
                    )
                    Text(
                        text = "Preset aktif: ${state.activePresetName}",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (state.isEnabled) WarmGold else WarmGray
                    )
                }

                Switch(
                    checked = state.isEnabled,
                    onCheckedChange = onToggleEnabled,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = OnGoldDark,
                        checkedTrackColor = WarmGold,
                        uncheckedThumbColor = WarmGray,
                        uncheckedTrackColor = ObsidianElevated
                    ),
                    modifier = Modifier.testTag("eq_master_switch")
                )
            }
        }

        item {
            FrequencyCurveCard(
                bands = state.bands,
                minMb = state.minLevelMb,
                maxMb = state.maxLevelMb,
                isEnabled = state.isEnabled
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Preset Equalizer",
                        style = MaterialTheme.typography.titleMedium,
                        color = IvoryText
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(
                            onClick = { showSavePresetDialog = true },
                            modifier = Modifier.testTag("eq_save_preset_button")
                        ) {
                            Icon(Icons.Rounded.BookmarkAdd, contentDescription = null, tint = WarmGold, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Simpan", color = WarmGold)
                        }
                        TextButton(
                            onClick = {
                                EqualizerState.builtInPresets().firstOrNull()?.let(onSelectPreset)
                            },
                            modifier = Modifier.testTag("eq_reset_button")
                        ) {
                            Icon(Icons.Rounded.RestartAlt, contentDescription = null, tint = WarmGray, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Reset", color = WarmGray)
                        }
                    }
                }

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(state.presets, key = { it.id }) { preset ->
                        val selected = preset.name == state.activePresetName
                        FilterChip(
                            selected = selected,
                            onClick = { onSelectPreset(preset) },
                            label = {
                                Text(preset.name, style = MaterialTheme.typography.labelLarge)
                            },
                            trailingIcon = if (preset.isCustom) {
                                {
                                    Icon(
                                        imageVector = Icons.Rounded.Close,
                                        contentDescription = "Hapus preset ${preset.name}",
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clickable { onDeleteCustomPreset(preset) }
                                    )
                                }
                            } else null,
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
                            modifier = Modifier.testTag("eq_preset_${preset.id}")
                        )
                    }
                }
            }
        }

        item {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = ObsidianCard,
                border = BorderStroke(1.dp, SubtleOutline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 18.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    state.bands.forEach { band ->
                        VerticalBandFader(
                            band = band,
                            minMb = state.minLevelMb,
                            maxMb = state.maxLevelMb,
                            enabled = state.isEnabled,
                            onValueChange = { newMb -> onBandLevelChange(band.index, newMb) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        item {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = ObsidianCard,
                border = BorderStroke(1.dp, SubtleOutline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.GraphicEq, contentDescription = null, tint = WarmGold)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Penguatan Ruang & Bass",
                            style = MaterialTheme.typography.titleMedium,
                            color = IvoryText
                        )
                    }

                    EffectSliderRow(
                        label = "Bass Boost",
                        value = state.bassBoostStrength,
                        enabled = state.isEnabled,
                        onValueChange = onBassBoostChange,
                        testTag = "eq_bass_boost_slider"
                    )

                    EffectSliderRow(
                        label = "Virtualizer (Panggung Suara)",
                        value = state.virtualizerStrength,
                        enabled = state.isEnabled,
                        onValueChange = onVirtualizerChange,
                        testTag = "eq_virtualizer_slider"
                    )
                }
            }
        }

        if (!state.isHardwareSupported) {
            item {
                Text(
                    text = "Catatan: Perangkat/emulator ini membatasi efek hardware Equalizer; profil kurva tetap disimpan dan diterapkan bila audio session mendukung.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = DimGray
                )
            }
        }
    }

    if (showSavePresetDialog) {
        var presetName by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showSavePresetDialog = false },
            containerColor = ObsidianElevated,
            title = { Text("Simpan Preset Kustom", color = IvoryText) },
            text = {
                OutlinedTextField(
                    value = presetName,
                    onValueChange = { presetName = it },
                    label = { Text("Nama Preset") },
                    placeholder = { Text("Misal: Jazz Malam") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = WarmGold,
                        unfocusedBorderColor = SubtleOutline,
                        focusedTextColor = IvoryText,
                        unfocusedTextColor = IvoryText
                    ),
                    modifier = Modifier.testTag("eq_preset_name_input")
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (presetName.isNotBlank()) {
                            onSaveCustomPreset(presetName)
                            showSavePresetDialog = false
                        }
                    },
                    enabled = presetName.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = WarmGold, contentColor = OnGoldDark)
                ) {
                    Text("Simpan")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSavePresetDialog = false }) {
                    Text("Batal", color = WarmGray)
                }
            }
        )
    }
}

@Composable
private fun FrequencyCurveCard(
    bands: List<EqualizerBand>,
    minMb: Int,
    maxMb: Int,
    isEnabled: Boolean
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = ObsidianCard,
        border = BorderStroke(
            1.dp,
            if (isEnabled) WarmGold.copy(alpha = 0.4f) else SubtleOutline
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 18.dp)
        ) {
            val w = size.width
            val h = size.height
            val centerY = h / 2f

            // Reference 0 dB line
            drawLine(
                color = SubtleOutline,
                start = Offset(0f, centerY),
                end = Offset(w, centerY),
                strokeWidth = 1.5f
            )

            if (bands.size < 2) return@Canvas
            val range = (maxMb - minMb).coerceAtLeast(1).toFloat()

            val points = bands.mapIndexed { idx, band ->
                val x = (idx.toFloat() / (bands.size - 1).toFloat()) * w
                val normalized = ((band.levelMb - minMb) / range).coerceIn(0f, 1f)
                val y = h - (normalized * h)
                Offset(x, y)
            }

            val linePath = Path().apply {
                moveTo(points.first().x, points.first().y)
                for (i in 0 until points.lastIndex) {
                    val p0 = points[i]
                    val p1 = points[i + 1]
                    val cx = (p0.x + p1.x) / 2f
                    cubicTo(cx, p0.y, cx, p1.y, p1.x, p1.y)
                }
            }

            val fillPath = Path().apply {
                addPath(linePath)
                lineTo(points.last().x, h)
                lineTo(points.first().x, h)
                close()
            }

            val strokeColor = if (isEnabled) WarmGold else WarmGray.copy(alpha = 0.45f)

            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    listOf(strokeColor.copy(alpha = 0.28f), Color.Transparent)
                )
            )

            drawPath(
                path = linePath,
                color = strokeColor,
                style = Stroke(width = 4f, cap = StrokeCap.Round)
            )

            points.forEach { pt ->
                drawCircle(
                    color = if (isEnabled) SoftGold else WarmGray,
                    radius = 6f,
                    center = pt
                )
            }
        }
    }
}

@Composable
private fun VerticalBandFader(
    band: EqualizerBand,
    minMb: Int,
    maxMb: Int,
    enabled: Boolean,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val dbValue = band.levelMb / 100f
    val dbText = if (dbValue >= 0f) String.format("+%.1f", dbValue) else String.format("%.1f", dbValue)
    val range = (maxMb - minMb).coerceAtLeast(1).toFloat()
    val fraction = ((band.levelMb - minMb) / range).coerceIn(0f, 1f)

    Column(
        modifier = modifier.padding(horizontal = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "${dbText}dB",
            style = MaterialTheme.typography.labelSmall,
            color = if (enabled) SoftGold else WarmGray
        )
        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .width(48.dp)
                .height(180.dp)
                .clip(RoundedCornerShape(14.dp))
                .pointerInput(enabled, minMb, maxMb) {
                    detectTapGestures { offset ->
                        val norm = (1f - (offset.y / size.height.toFloat())).coerceIn(0f, 1f)
                        val newMb = (minMb + norm * range).roundToInt()
                        onValueChange(newMb)
                    }
                }
                .pointerInput(enabled, minMb, maxMb) {
                    detectDragGestures { change, _ ->
                        change.consume()
                        val norm = (1f - (change.position.y / size.height.toFloat())).coerceIn(0f, 1f)
                        val newMb = (minMb + norm * range).roundToInt()
                        onValueChange(newMb)
                    }
                }
                .testTag("eq_band_slider_${band.index}"),
            contentAlignment = Alignment.BottomCenter
        ) {
            // Track background
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .fillMaxHeight()
                    .clip(CircleShape)
                    .background(ObsidianElevated)
            )
            // Active fill from bottom
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .fillMaxHeight(fraction.coerceAtLeast(0.03f))
                    .clip(CircleShape)
                    .background(if (enabled) WarmGold else WarmGray.copy(alpha = 0.45f))
            )
            // Thumb knob
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = (156.dp * fraction))
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(if (enabled) SoftGold else WarmGray)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = band.label,
            style = MaterialTheme.typography.labelMedium,
            color = WarmGray
        )
    }
}

@Composable
private fun EffectSliderRow(
    label: String,
    value: Int,
    enabled: Boolean,
    onValueChange: (Int) -> Unit,
    testTag: String
) {
    val pct = (value / 10f).roundToInt()
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, style = MaterialTheme.typography.bodyLarge, color = IvoryText)
            Text(
                text = "$pct%",
                style = MaterialTheme.typography.labelMedium,
                color = if (enabled) WarmGold else WarmGray
            )
        }
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.roundToInt()) },
            valueRange = 0f..1000f,
            colors = SliderDefaults.colors(
                thumbColor = if (enabled) WarmGold else WarmGray,
                activeTrackColor = if (enabled) WarmGold else WarmGray,
                inactiveTrackColor = ObsidianElevated
            ),
            modifier = Modifier.testTag(testTag)
        )
    }
}
