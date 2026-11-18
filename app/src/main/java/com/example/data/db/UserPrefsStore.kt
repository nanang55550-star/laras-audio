package com.example.data.db

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.model.SongSortOrder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "laras_audio_prefs")

data class EqPreferences(
    val isEnabled: Boolean = false,
    val bandLevelsMb: List<Int> = listOf(0, 0, 0, 0, 0),
    val bassBoostStrength: Int = 0,
    val virtualizerStrength: Int = 0,
    val activePresetName: String = "Datar"
)

class UserPrefsStore(private val context: Context) {
    private object Keys {
        val EQ_ENABLED = booleanPreferencesKey("eq_enabled")
        val EQ_BANDS_CSV = stringPreferencesKey("eq_bands_csv")
        val BASS_BOOST = intPreferencesKey("bass_boost")
        val VIRTUALIZER = intPreferencesKey("virtualizer")
        val EQ_PRESET_NAME = stringPreferencesKey("eq_preset_name")
        val SHUFFLE_ENABLED = booleanPreferencesKey("shuffle_enabled")
        val REPEAT_MODE = intPreferencesKey("repeat_mode")
        val SONG_SORT_ORDER = stringPreferencesKey("song_sort_order")
    }

    val eqPreferences: Flow<EqPreferences> = context.dataStore.data.map { prefs ->
        val bandsCsv = prefs[Keys.EQ_BANDS_CSV]
        val levels = bandsCsv
            ?.split(",")
            ?.mapNotNull { it.trim().toIntOrNull() }
            ?.takeIf { it.isNotEmpty() }
            ?: listOf(0, 0, 0, 0, 0)
        EqPreferences(
            isEnabled = prefs[Keys.EQ_ENABLED] ?: false,
            bandLevelsMb = levels,
            bassBoostStrength = prefs[Keys.BASS_BOOST] ?: 0,
            virtualizerStrength = prefs[Keys.VIRTUALIZER] ?: 0,
            activePresetName = prefs[Keys.EQ_PRESET_NAME] ?: "Datar"
        )
    }

    val shuffleEnabled: Flow<Boolean> = context.dataStore.data.map { it[Keys.SHUFFLE_ENABLED] ?: false }
    val repeatMode: Flow<Int> = context.dataStore.data.map { it[Keys.REPEAT_MODE] ?: 0 }
    val songSortOrder: Flow<SongSortOrder> = context.dataStore.data.map { prefs ->
        val name = prefs[Keys.SONG_SORT_ORDER]
        runCatching { SongSortOrder.valueOf(name ?: "") }.getOrDefault(SongSortOrder.DEFAULT)
    }

    suspend fun saveEqPreferences(eq: EqPreferences) {
        context.dataStore.edit { prefs ->
            prefs[Keys.EQ_ENABLED] = eq.isEnabled
            prefs[Keys.EQ_BANDS_CSV] = eq.bandLevelsMb.joinToString(",")
            prefs[Keys.BASS_BOOST] = eq.bassBoostStrength
            prefs[Keys.VIRTUALIZER] = eq.virtualizerStrength
            prefs[Keys.EQ_PRESET_NAME] = eq.activePresetName
        }
    }

    suspend fun setShuffleEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.SHUFFLE_ENABLED] = enabled }
    }

    suspend fun setRepeatMode(mode: Int) {
        context.dataStore.edit { it[Keys.REPEAT_MODE] = mode }
    }

    suspend fun setSongSortOrder(order: SongSortOrder) {
        context.dataStore.edit { it[Keys.SONG_SORT_ORDER] = order.name }
    }
}
