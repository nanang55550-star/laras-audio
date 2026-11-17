package com.example.model

data class EqualizerBand(
    val index: Int,
    val centerFreqHz: Int,
    val label: String,
    val levelMb: Int
)

data class EqualizerPreset(
    val id: String,
    val name: String,
    val bandLevelsMb: List<Int>,
    val bassBoostStrength: Int,
    val virtualizerStrength: Int,
    val isCustom: Boolean = false
)

data class EqualizerState(
    val isEnabled: Boolean = false,
    val minLevelMb: Int = -1500,
    val maxLevelMb: Int = 1500,
    val bands: List<EqualizerBand> = defaultBands(),
    val bassBoostStrength: Int = 0,
    val virtualizerStrength: Int = 0,
    val activePresetName: String = "Datar",
    val presets: List<EqualizerPreset> = builtInPresets(),
    val isHardwareSupported: Boolean = true
) {
    companion object {
        fun defaultBands(levels: List<Int> = listOf(0, 0, 0, 0, 0)): List<EqualizerBand> {
            val freqs = listOf(60, 230, 910, 3600, 14000)
            val labels = listOf("60Hz", "230Hz", "910Hz", "3.6k", "14k")
            return freqs.indices.map { i ->
                EqualizerBand(
                    index = i,
                    centerFreqHz = freqs[i],
                    label = labels[i],
                    levelMb = levels.getOrElse(i) { 0 }
                )
            }
        }

        fun builtInPresets(): List<EqualizerPreset> = listOf(
            EqualizerPreset("flat", "Datar", listOf(0, 0, 0, 0, 0), 0, 0),
            EqualizerPreset("bass", "Bass Dalam", listOf(750, 500, 50, -100, 150), 650, 150),
            EqualizerPreset("vocal", "Vokal Jernih", listOf(-250, 150, 600, 500, 150), 100, 250),
            EqualizerPreset("acoustic", "Akustik", listOf(350, 200, 250, 450, 300), 250, 300),
            EqualizerPreset("classical", "Klasik", listOf(400, 200, -100, 350, 550), 150, 400),
            EqualizerPreset("rock", "Rock", listOf(550, 300, -150, 400, 600), 450, 250),
            EqualizerPreset("night", "Malam Tenang", listOf(200, 100, 150, -150, -300), 200, 100)
        )
    }
}
