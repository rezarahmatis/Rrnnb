package com.example.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.PelagiaApplication
import com.example.audio.MusicPlayerController
import com.example.model.AppSettings
import com.example.model.AquariumConfiguration
import com.example.model.AquariumValidator
import com.example.model.AudioSpectrum
import com.example.model.CompatibilityStatus
import com.example.model.FishCatalog
import com.example.model.PerformanceMode
import com.example.model.PlaybackState
import com.example.model.Playlist
import com.example.model.SpeciesEntry
import com.example.model.Track
import com.example.model.WorldCatalog
import com.example.model.WorldEnvironment
import com.example.simulation.AquaticSimulationEngine
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class OverlayPanel {
    NONE,
    WORLDS,
    AQUARIUM,
    MUSIC,
    SETTINGS
}

@OptIn(ExperimentalCoroutinesApi::class)
class PelagiaViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as PelagiaApplication
    private val aquariumRepo = app.aquariumRepository
    private val musicRepo = app.musicRepository
    private val settingsRepo = app.settingsRepository
    private val musicScanner = app.musicScanner

    val simulationEngine: AquaticSimulationEngine = app.simulationEngine
    val audioSpectrum: StateFlow<AudioSpectrum> = app.audioAnalyzer.spectrumFlow

    private val playerController = MusicPlayerController(
        context = application,
        onTrackStarted = { trackId ->
            viewModelScope.launch { musicRepo.markTrackPlayed(trackId) }
        }
    )

    val playbackState: StateFlow<PlaybackState> = playerController.playbackState

    val settings: StateFlow<AppSettings> = settingsRepo.settingsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = AppSettings()
    )

    val savedAquariums: StateFlow<List<AquariumConfiguration>> = aquariumRepo.savedAquariums.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allTracks: StateFlow<List<Track>> = musicRepo.allTracks.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val favoriteTracks: StateFlow<List<Track>> = musicRepo.favoriteTracks.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val recentlyPlayedTracks: StateFlow<List<Track>> = musicRepo.recentlyPlayedTracks.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val playlists: StateFlow<List<Playlist>> = musicRepo.playlists.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _selectedPlaylistId = MutableStateFlow<Long?>(null)
    val selectedPlaylistId: StateFlow<Long?> = _selectedPlaylistId.asStateFlow()

    val selectedPlaylistTracks: StateFlow<List<Track>> = _selectedPlaylistId
        .flatMapLatest { id -> if (id != null) musicRepo.observePlaylistTracks(id) else flowOf(emptyList()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentWorld = MutableStateFlow(WorldCatalog.worlds.first())
    val currentWorld: StateFlow<WorldEnvironment> = _currentWorld.asStateFlow()

    private val _activeRoster = MutableStateFlow(WorldCatalog.worlds.first().defaultFish)
    val activeRoster: StateFlow<List<SpeciesEntry>> = _activeRoster.asStateFlow()

    private val _activeAquariumName = MutableStateFlow<String?>(null)
    val activeAquariumName: StateFlow<String?> = _activeAquariumName.asStateFlow()

    private val _activePanel = MutableStateFlow(OverlayPanel.NONE)
    val activePanel: StateFlow<OverlayPanel> = _activePanel.asStateFlow()

    private val _isImportingMusic = MutableStateFlow(false)
    val isImportingMusic: StateFlow<Boolean> = _isImportingMusic.asStateFlow()

    private val _statusBanner = MutableStateFlow<String?>(null)
    val statusBanner: StateFlow<String?> = _statusBanner.asStateFlow()

    init {
        playerController.connect()

        // Sync settings changes directly into simulation engine
        viewModelScope.launch {
            settingsRepo.settingsFlow.collect { s ->
                simulationEngine.applySettings(s)
            }
        }

        // Keep player metadata synchronized when favorites change
        viewModelScope.launch {
            musicRepo.allTracks.collect { tracks ->
                playerController.updateTrackMetadataInQueue(tracks)
            }
        }

        // Restore persisted active world, roster & starter presets
        viewModelScope.launch {
            aquariumRepo.ensureStarterPresets()
            val savedSettings = settingsRepo.settingsFlow.first()
            val savedRosterRaw = settingsRepo.savedActiveRosterFlow.first()
            val world = WorldCatalog.getById(savedSettings.activeWorldId)
            val roster = savedRosterRaw?.let { AquariumValidator.deserializeSpecies(it) }
                ?.takeIf { it.isNotEmpty() }
                ?: world.defaultFish

            _currentWorld.value = world
            _activeRoster.value = roster
            simulationEngine.applySettings(savedSettings)
            simulationEngine.applyWorld(world, roster)
        }
    }

    // ═══════════════════ NAVIGATION / PANELS ═══════════════════

    fun openPanel(panel: OverlayPanel) {
        _activePanel.value = panel
        if (panel != OverlayPanel.NONE && settings.value.ambientMode) {
            setAmbientMode(false)
        }
    }

    fun closePanel() {
        _activePanel.value = OverlayPanel.NONE
    }

    // ═══════════════════ WORLDS & MULTI-SPECIES ECOSYSTEM ═══════════════════

    fun selectWorld(world: WorldEnvironment) {
        _currentWorld.value = world
        _activeRoster.value = world.defaultFish
        _activeAquariumName.value = null
        simulationEngine.applyWorld(world, world.defaultFish)
        persistActiveEcosystem(world.id, world.defaultFish, null)
        showBanner("Entered ${world.name}")
    }

    fun addSpecies(speciesId: String, initialCount: Int = 2) {
        val sp = FishCatalog.getById(speciesId) ?: return
        val world = _currentWorld.value
        val curSettings = settings.value
        val compat = AquariumValidator.checkCompatibility(sp, world, curSettings.overrideCompatibility)
        if (compat == CompatibilityStatus.INCOMPATIBLE) {
            showBanner("${sp.name} prefers ${sp.compatibleHabitats.first().label}. Enable Cross-Habitat Override to mix.")
            return
        }

        val current = _activeRoster.value.toMutableList()
        val existingIdx = current.indexOfFirst { it.speciesId == speciesId }
        val cap = AquariumValidator.maxPopulationFor(curSettings.performanceMode)
        val totalActive = current.filter { it.enabled }.sumOf { it.count }

        if (existingIdx >= 0) {
            val entry = current[existingIdx]
            if (totalActive >= cap) {
                showBanner("Population cap ($cap) reached for ${curSettings.performanceMode.label}")
                return
            }
            val nextCount = (entry.count + 1).coerceAtMost(sp.recommendedMax)
            current[existingIdx] = entry.copy(count = nextCount, enabled = true)
        } else {
            val available = (cap - totalActive).coerceAtLeast(0)
            if (available <= 0) {
                showBanner("Population cap ($cap) reached for ${curSettings.performanceMode.label}")
                return
            }
            val countToAdd = initialCount.coerceAtMost(available).coerceAtMost(sp.recommendedMax)
            current.add(SpeciesEntry(speciesId = speciesId, count = countToAdd, enabled = true))
        }

        applyRosterUpdate(current)
    }

    fun updateSpeciesCount(speciesId: String, delta: Int) {
        val sp = FishCatalog.getById(speciesId) ?: return
        val current = _activeRoster.value.toMutableList()
        val idx = current.indexOfFirst { it.speciesId == speciesId }
        if (idx < 0) return

        val entry = current[idx]
        val cap = AquariumValidator.maxPopulationFor(settings.value.performanceMode)
        val otherActive = current.filterIndexed { i, e -> i != idx && e.enabled }.sumOf { it.count }

        val target = entry.count + delta
        if (target <= 0) {
            current.removeAt(idx)
        } else {
            val maxForSpecies = if (entry.enabled) {
                (cap - otherActive).coerceAtLeast(1).coerceAtMost(sp.recommendedMax)
            } else {
                sp.recommendedMax
            }
            if (delta > 0 && target > maxForSpecies) {
                showBanner(
                    if (entry.enabled && otherActive + target > cap)
                        "Population cap ($cap) reached for ${settings.value.performanceMode.label}"
                    else
                        "Max recommended for ${sp.name} is ${sp.recommendedMax}"
                )
                return
            }
            current[idx] = entry.copy(count = target.coerceIn(1, maxForSpecies))
        }
        applyRosterUpdate(current)
    }

    fun toggleSpeciesEnabled(speciesId: String) {
        val current = _activeRoster.value.toMutableList()
        val idx = current.indexOfFirst { it.speciesId == speciesId }
        if (idx < 0) return
        val entry = current[idx]
        if (!entry.enabled) {
            val cap = AquariumValidator.maxPopulationFor(settings.value.performanceMode)
            val activeTotal = current.filter { it.enabled }.sumOf { it.count }
            if (activeTotal + entry.count > cap) {
                showBanner("Re-enabling exceeds ${settings.value.performanceMode.label} cap ($cap)")
                return
            }
        }
        current[idx] = entry.copy(enabled = !entry.enabled)
        applyRosterUpdate(current)
    }

    fun removeSpecies(speciesId: String) {
        val updated = _activeRoster.value.filterNot { it.speciesId == speciesId }
        applyRosterUpdate(updated)
    }

    fun resetToWorldDefaultRoster() {
        val defaults = _currentWorld.value.defaultFish
        _activeAquariumName.value = null
        applyRosterUpdate(defaults)
        showBanner("Restored natural species for ${_currentWorld.value.name}")
    }

    private fun applyRosterUpdate(roster: List<SpeciesEntry>) {
        _activeRoster.value = roster
        simulationEngine.applyRoster(roster)
        persistActiveEcosystem(_currentWorld.value.id, roster, settings.value.activeAquariumId)
    }

    // ═══════════════════ SAVED AQUARIUMS ═══════════════════

    fun saveCurrentAquarium(name: String) {
        viewModelScope.launch {
            val id = aquariumRepo.saveConfiguration(
                name = name,
                worldId = _currentWorld.value.id,
                species = _activeRoster.value
            )
            _activeAquariumName.value = name.trim().ifBlank { "Custom Aquarium" }
            persistActiveEcosystem(_currentWorld.value.id, _activeRoster.value, id)
            showBanner("Saved \"${_activeAquariumName.value}\"")
        }
    }

    fun loadAquarium(config: AquariumConfiguration) {
        val world = WorldCatalog.getById(config.worldId)
        _currentWorld.value = world
        _activeRoster.value = config.species
        _activeAquariumName.value = config.name
        simulationEngine.applyWorld(world, config.species)
        persistActiveEcosystem(world.id, config.species, config.id)
        showBanner("Loaded \"${config.name}\"")
    }

    fun overwriteAquarium(config: AquariumConfiguration) {
        viewModelScope.launch {
            aquariumRepo.updateConfiguration(
                config.copy(
                    worldId = _currentWorld.value.id,
                    species = _activeRoster.value
                )
            )
            _activeAquariumName.value = config.name
            showBanner("Updated \"${config.name}\"")
        }
    }

    fun renameAquarium(id: Long, newName: String) {
        viewModelScope.launch {
            aquariumRepo.renameConfiguration(id, newName)
            showBanner("Renamed aquarium to \"$newName\"")
        }
    }

    fun deleteAquarium(id: Long) {
        viewModelScope.launch {
            aquariumRepo.deleteConfiguration(id)
            showBanner("Deleted saved aquarium")
        }
    }

    private fun persistActiveEcosystem(worldId: String, roster: List<SpeciesEntry>, aquariumId: Long?) {
        viewModelScope.launch {
            settingsRepo.setActiveWorldAndRoster(
                worldId = worldId,
                rosterSerialized = AquariumValidator.serializeSpecies(roster),
                aquariumId = aquariumId
            )
        }
    }

    // ═══════════════════ MUSIC LIBRARY & PLAYBACK ═══════════════════

    fun importAudioFiles(uris: List<Uri>) {
        if (uris.isEmpty()) return
        viewModelScope.launch {
            _isImportingMusic.value = true
            try {
                val scanned = musicScanner.importAudioFiles(uris)
                val inserted = musicRepo.importTracks(scanned)
                showBanner(
                    when {
                        inserted > 0 -> "Added $inserted local ${if (inserted == 1) "track" else "tracks"}"
                        scanned.isNotEmpty() -> "Selected tracks are already in your library"
                        else -> "No supported audio files found"
                    }
                )
            } finally {
                _isImportingMusic.value = false
            }
        }
    }

    fun importMusicFolder(treeUri: Uri?) {
        if (treeUri == null) return
        viewModelScope.launch {
            _isImportingMusic.value = true
            try {
                val scanned = musicScanner.importMusicFolder(treeUri)
                val inserted = musicRepo.importTracks(scanned)
                showBanner(
                    when {
                        inserted > 0 -> "Imported $inserted ${if (inserted == 1) "track" else "tracks"} from folder"
                        scanned.isNotEmpty() -> "Folder tracks are already in your library"
                        else -> "No audio files found in selected folder"
                    }
                )
            } finally {
                _isImportingMusic.value = false
            }
        }
    }

    fun playTracks(tracks: List<Track>, startIndex: Int) {
        if (tracks.isEmpty()) return
        playerController.playQueue(tracks, startIndex)
    }

    fun togglePlayPause() {
        val state = playbackState.value
        if (state.currentTrack == null && allTracks.value.isNotEmpty()) {
            playerController.playQueue(allTracks.value, 0)
        } else {
            playerController.togglePlayPause()
        }
    }

    fun nextTrack() = playerController.next()
    fun previousTrack() = playerController.previous()
    fun seekTo(positionMs: Long) = playerController.seekTo(positionMs)
    fun setVolume(volume: Float) = playerController.setVolume(volume)
    fun toggleShuffle() = playerController.toggleShuffle()
    fun cycleRepeatMode() = playerController.cycleRepeatMode()

    fun toggleFavorite(track: Track) {
        viewModelScope.launch { musicRepo.toggleFavorite(track) }
    }

    fun deleteTrack(trackId: Long) {
        viewModelScope.launch { musicRepo.deleteTrack(trackId) }
    }

    fun createPlaylist(name: String) {
        viewModelScope.launch {
            musicRepo.createPlaylist(name)
            showBanner("Created playlist \"$name\"")
        }
    }

    fun deletePlaylist(playlistId: Long) {
        viewModelScope.launch {
            if (_selectedPlaylistId.value == playlistId) _selectedPlaylistId.value = null
            musicRepo.deletePlaylist(playlistId)
        }
    }

    fun selectPlaylist(playlistId: Long?) {
        _selectedPlaylistId.value = playlistId
    }

    fun addTrackToPlaylist(playlistId: Long, trackId: Long) {
        viewModelScope.launch {
            musicRepo.addTrackToPlaylist(playlistId, trackId)
            showBanner("Added track to playlist")
        }
    }

    fun removeTrackFromPlaylist(playlistId: Long, trackId: Long) {
        viewModelScope.launch {
            musicRepo.removeTrackFromPlaylist(playlistId, trackId)
        }
    }

    // ═══════════════════ SETTINGS & IMMERSIVE MODES ═══════════════════

    fun setPerformanceMode(mode: PerformanceMode) {
        viewModelScope.launch { settingsRepo.setPerformanceMode(mode) }
    }

    fun setAmbientMode(enabled: Boolean) {
        if (enabled) _activePanel.value = OverlayPanel.NONE
        viewModelScope.launch { settingsRepo.setAmbientMode(enabled) }
    }

    fun setFullscreenMode(enabled: Boolean) {
        viewModelScope.launch { settingsRepo.setFullscreenMode(enabled) }
    }

    fun setKeepScreenOn(enabled: Boolean) {
        viewModelScope.launch { settingsRepo.setKeepScreenOn(enabled) }
    }

    fun setReducedMotion(enabled: Boolean) {
        viewModelScope.launch { settingsRepo.setReducedMotion(enabled) }
    }

    fun setHapticFeedback(enabled: Boolean) {
        viewModelScope.launch { settingsRepo.setHapticFeedback(enabled) }
    }

    fun setAudioReactivity(strength: Float) {
        viewModelScope.launch { settingsRepo.setAudioReactivity(strength) }
    }

    fun setOverrideCompatibility(enabled: Boolean) {
        viewModelScope.launch { settingsRepo.setOverrideCompatibility(enabled) }
    }

    private fun showBanner(message: String) {
        viewModelScope.launch {
            _statusBanner.value = message
            delay(2800L)
            if (_statusBanner.value == message) {
                _statusBanner.value = null
            }
        }
    }

    override fun onCleared() {
        playerController.release()
        super.onCleared()
    }
}
