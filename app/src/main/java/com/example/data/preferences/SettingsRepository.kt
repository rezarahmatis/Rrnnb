package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.model.AppSettings
import com.example.model.PerformanceMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.pelagiaDataStore: DataStore<Preferences> by preferencesDataStore(name = "pelagia_settings")

class SettingsRepository(private val context: Context) {

    private object Keys {
        val PERFORMANCE_MODE = stringPreferencesKey("performance_mode")
        val AMBIENT_MODE = booleanPreferencesKey("ambient_mode")
        val FULLSCREEN_MODE = booleanPreferencesKey("fullscreen_mode")
        val KEEP_SCREEN_ON = booleanPreferencesKey("keep_screen_on")
        val REDUCED_MOTION = booleanPreferencesKey("reduced_motion")
        val HAPTIC_FEEDBACK = booleanPreferencesKey("haptic_feedback")
        val AUDIO_REACTIVITY = floatPreferencesKey("audio_reactivity")
        val OVERRIDE_COMPATIBILITY = booleanPreferencesKey("override_compatibility")
        val ACTIVE_WORLD_ID = stringPreferencesKey("active_world_id")
        val ACTIVE_AQUARIUM_ID = longPreferencesKey("active_aquarium_id")
        val ACTIVE_ROSTER = stringPreferencesKey("active_roster")
    }

    val settingsFlow: Flow<AppSettings> = context.pelagiaDataStore.data.map { prefs ->
        val modeName = prefs[Keys.PERFORMANCE_MODE] ?: PerformanceMode.BALANCED.name
        val mode = runCatching { PerformanceMode.valueOf(modeName) }.getOrDefault(PerformanceMode.BALANCED)
        AppSettings(
            performanceMode = mode,
            ambientMode = prefs[Keys.AMBIENT_MODE] ?: false,
            fullscreenMode = prefs[Keys.FULLSCREEN_MODE] ?: true,
            keepScreenOn = prefs[Keys.KEEP_SCREEN_ON] ?: true,
            reducedMotion = prefs[Keys.REDUCED_MOTION] ?: false,
            hapticFeedback = prefs[Keys.HAPTIC_FEEDBACK] ?: true,
            audioReactivityStrength = prefs[Keys.AUDIO_REACTIVITY] ?: 0.75f,
            overrideCompatibility = prefs[Keys.OVERRIDE_COMPATIBILITY] ?: false,
            activeWorldId = prefs[Keys.ACTIVE_WORLD_ID] ?: "japanese_koi_garden",
            activeAquariumId = prefs[Keys.ACTIVE_AQUARIUM_ID]?.takeIf { it > 0L }
        )
    }

    val savedActiveRosterFlow: Flow<String?> = context.pelagiaDataStore.data.map { prefs ->
        prefs[Keys.ACTIVE_ROSTER]
    }

    suspend fun setPerformanceMode(mode: PerformanceMode) {
        context.pelagiaDataStore.edit { it[Keys.PERFORMANCE_MODE] = mode.name }
    }

    suspend fun setAmbientMode(enabled: Boolean) {
        context.pelagiaDataStore.edit { it[Keys.AMBIENT_MODE] = enabled }
    }

    suspend fun setFullscreenMode(enabled: Boolean) {
        context.pelagiaDataStore.edit { it[Keys.FULLSCREEN_MODE] = enabled }
    }

    suspend fun setKeepScreenOn(enabled: Boolean) {
        context.pelagiaDataStore.edit { it[Keys.KEEP_SCREEN_ON] = enabled }
    }

    suspend fun setReducedMotion(enabled: Boolean) {
        context.pelagiaDataStore.edit { it[Keys.REDUCED_MOTION] = enabled }
    }

    suspend fun setHapticFeedback(enabled: Boolean) {
        context.pelagiaDataStore.edit { it[Keys.HAPTIC_FEEDBACK] = enabled }
    }

    suspend fun setAudioReactivity(strength: Float) {
        context.pelagiaDataStore.edit { it[Keys.AUDIO_REACTIVITY] = strength.coerceIn(0f, 1.5f) }
    }

    suspend fun setOverrideCompatibility(enabled: Boolean) {
        context.pelagiaDataStore.edit { it[Keys.OVERRIDE_COMPATIBILITY] = enabled }
    }

    suspend fun setActiveWorldAndRoster(worldId: String, rosterSerialized: String, aquariumId: Long? = null) {
        context.pelagiaDataStore.edit {
            it[Keys.ACTIVE_WORLD_ID] = worldId
            it[Keys.ACTIVE_ROSTER] = rosterSerialized
            it[Keys.ACTIVE_AQUARIUM_ID] = aquariumId ?: -1L
        }
    }
}
