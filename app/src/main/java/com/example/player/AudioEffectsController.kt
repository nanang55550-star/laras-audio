package com.example.player

import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.Virtualizer
import com.example.model.EqualizerBand
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object AudioEffectsController {
    private var equalizer: Equalizer? = null
    private var bassBoost: BassBoost? = null
    private var virtualizer: Virtualizer? = null
    private var currentSessionId: Int = 0

    private var cachedEnabled: Boolean = false
    private var cachedBandLevelsMb: List<Int> = listOf(0, 0, 0, 0, 0)
    private var cachedBassStrength: Int = 0
    private var cachedVirtualizerStrength: Int = 0

    private val _isHardwareSupported = MutableStateFlow(true)
    val isHardwareSupported: StateFlow<Boolean> = _isHardwareSupported.asStateFlow()

    private val _bandMeta = MutableStateFlow(EqualizerBandMeta())
    val bandMeta: StateFlow<EqualizerBandMeta> = _bandMeta.asStateFlow()

    data class EqualizerBandMeta(
        val minLevelMb: Int = -1500,
        val maxLevelMb: Int = 1500,
        val centerFreqsHz: List<Int> = listOf(60, 230, 910, 3600, 14000)
    )

    @Synchronized
    fun bindToAudioSession(audioSessionId: Int) {
        if (audioSessionId == 0 || audioSessionId == currentSessionId) return
        releaseInternal()
        currentSessionId = audioSessionId

        try {
            val eq = Equalizer(0, audioSessionId)
            val range = eq.bandLevelRange
            val minMb = range.getOrElse(0) { (-1500).toShort() }.toInt()
            val maxMb = range.getOrElse(1) { 1500.toShort() }.toInt()
            val bandCount = eq.numberOfBands.toInt().coerceAtLeast(1)
            val freqs = (0 until bandCount).map { idx ->
                (eq.getCenterFreq(idx.toShort()) / 1000).coerceAtLeast(20)
            }
            _bandMeta.value = EqualizerBandMeta(
                minLevelMb = minMb,
                maxLevelMb = maxMb,
                centerFreqsHz = if (freqs.size >= 5) freqs.take(5) else listOf(60, 230, 910, 3600, 14000)
            )
            equalizer = eq
            _isHardwareSupported.value = true
        } catch (_: Throwable) {
            equalizer = null
            _isHardwareSupported.value = false
        }

        try {
            bassBoost = BassBoost(0, audioSessionId)
        } catch (_: Throwable) {
            bassBoost = null
        }

        try {
            virtualizer = Virtualizer(0, audioSessionId)
        } catch (_: Throwable) {
            virtualizer = null
        }

        applyAllCached()
    }

    @Synchronized
    fun updateSettings(
        enabled: Boolean,
        bandLevelsMb: List<Int>,
        bassBoostStrength: Int,
        virtualizerStrength: Int
    ) {
        cachedEnabled = enabled
        cachedBandLevelsMb = bandLevelsMb
        cachedBassStrength = bassBoostStrength.coerceIn(0, 1000)
        cachedVirtualizerStrength = virtualizerStrength.coerceIn(0, 1000)
        applyAllCached()
    }

    private fun applyAllCached() {
        equalizer?.let { eq ->
            runCatching {
                eq.enabled = cachedEnabled
                val range = eq.bandLevelRange
                val minMb = range[0].toInt()
                val maxMb = range[1].toInt()
                val bandCount = eq.numberOfBands.toInt()
                for (i in 0 until bandCount) {
                    val target = cachedBandLevelsMb.getOrElse(i) { 0 }.coerceIn(minMb, maxMb)
                    eq.setBandLevel(i.toShort(), target.toShort())
                }
            }
        }

        bassBoost?.let { bb ->
            runCatching {
                bb.enabled = cachedEnabled && cachedBassStrength > 0
                if (bb.strengthSupported) {
                    bb.setStrength(cachedBassStrength.toShort())
                }
            }
        }

        virtualizer?.let { virt ->
            runCatching {
                virt.enabled = cachedEnabled && cachedVirtualizerStrength > 0
                if (virt.strengthSupported) {
                    virt.setStrength(cachedVirtualizerStrength.toShort())
                }
            }
        }
    }

    fun buildBands(levels: List<Int>): List<EqualizerBand> {
        val meta = _bandMeta.value
        return meta.centerFreqsHz.mapIndexed { idx, freq ->
            val label = if (freq >= 1000) {
                val khz = freq / 1000f
                if (khz % 1f == 0f) "${khz.toInt()}k" else String.format("%.1fk", khz)
            } else {
                "${freq}Hz"
            }
            EqualizerBand(
                index = idx,
                centerFreqHz = freq,
                label = label,
                levelMb = levels.getOrElse(idx) { 0 }.coerceIn(meta.minLevelMb, meta.maxLevelMb)
            )
        }
    }

    @Synchronized
    fun release() {
        releaseInternal()
        currentSessionId = 0
    }

    private fun releaseInternal() {
        runCatching { equalizer?.release() }
        runCatching { bassBoost?.release() }
        runCatching { virtualizer?.release() }
        equalizer = null
        bassBoost = null
        virtualizer = null
    }
}
