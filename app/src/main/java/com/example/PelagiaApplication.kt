package com.example

import android.app.Application
import com.example.audio.AudioAnalyzer
import com.example.audio.LocalMusicScanner
import com.example.data.db.PelagiaDatabase
import com.example.data.preferences.SettingsRepository
import com.example.data.repository.AquariumRepository
import com.example.data.repository.MusicRepository
import com.example.simulation.AquaticSimulationEngine

/**
 * Application container providing lightweight manual constructor injection across
 * the Media3 playback service, OpenGL simulation engine, Room database, and DataStore.
 */
class PelagiaApplication : Application() {

    val audioAnalyzer: AudioAnalyzer by lazy { AudioAnalyzer() }
    val simulationEngine: AquaticSimulationEngine by lazy { AquaticSimulationEngine(audioAnalyzer) }

    val database: PelagiaDatabase by lazy { PelagiaDatabase.getInstance(this) }
    val aquariumRepository: AquariumRepository by lazy { AquariumRepository(database.aquariumDao()) }
    val musicRepository: MusicRepository by lazy { MusicRepository(database.musicDao()) }
    val settingsRepository: SettingsRepository by lazy { SettingsRepository(this) }
    val musicScanner: LocalMusicScanner by lazy { LocalMusicScanner(this) }
}
