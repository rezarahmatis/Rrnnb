package com.example.simulation

import com.example.audio.AudioAnalyzer
import com.example.model.AppSettings
import com.example.model.AquariumValidator
import com.example.model.AudioSpectrum
import com.example.model.SpeciesEntry
import com.example.model.WorldCatalog
import com.example.model.WorldEnvironment

/**
 * Thread-safe simulation coordinator shared between the Compose UI / ViewModel layer
 * and the OpenGL ES 3.0 render thread.
 */
class AquaticSimulationEngine(
    private val audioAnalyzer: AudioAnalyzer
) {
    private val lock = Any()

    val fishSchool = FishSchool(maxCapacity = 128)
    val particleSystem = ParticleSystem(maxParticles = 220, maxRipples = 8, maxFlora = 46)

    @Volatile
    var currentWorld: WorldEnvironment = WorldCatalog.worlds.first()
        private set

    @Volatile
    var currentSettings: AppSettings = AppSettings()
        private set

    private var currentRoster: List<SpeciesEntry> = currentWorld.defaultFish
    private var aspectRatio: Float = 0.5625f
    private var simTime: Float = 0f

    // Pending touch ripples queued from the UI thread (fixed ring buffer, zero allocation)
    private val pendingTouchX = FloatArray(16)
    private val pendingTouchY = FloatArray(16)
    private val pendingTouchStrength = FloatArray(16)
    private var pendingTouchCount = 0

    // Flags to rebuild pools safely on the GL thread
    private var needsWorldRebuild = true
    private var needsFishRebuild = true

    // Cached smoothed audio snapshot for rendering
    var smoothedAudio: AudioSpectrum = AudioSpectrum()
        private set

    val elapsedTime: Float
        get() = simTime

    fun setViewportAspect(aspect: Float) {
        synchronized(lock) {
            val clamped = aspect.coerceIn(0.35f, 3.0f)
            if (kotlin.math.abs(clamped - aspectRatio) > 0.05f) {
                aspectRatio = clamped
                needsWorldRebuild = true
            }
        }
    }

    fun applyWorld(world: WorldEnvironment, roster: List<SpeciesEntry>) {
        synchronized(lock) {
            currentWorld = world
            currentRoster = roster
            needsWorldRebuild = true
            needsFishRebuild = true
        }
    }

    fun applyRoster(roster: List<SpeciesEntry>) {
        synchronized(lock) {
            currentRoster = roster
            needsFishRebuild = true
        }
    }

    fun applySettings(settings: AppSettings) {
        synchronized(lock) {
            val modeChanged = settings.performanceMode != currentSettings.performanceMode
            currentSettings = settings
            if (modeChanged) {
                needsWorldRebuild = true
                needsFishRebuild = true
            }
        }
    }

    fun enqueueTouchRipple(normX: Float, normY: Float, intensity: Float = 1.0f) {
        synchronized(lock) {
            if (pendingTouchCount < pendingTouchX.size) {
                pendingTouchX[pendingTouchCount] = (normX * 2f - 1f) * aspectRatio
                pendingTouchY[pendingTouchCount] = 1f - normY * 2f
                pendingTouchStrength[pendingTouchCount] = intensity
                pendingTouchCount++
            }
        }
    }

    /**
     * Advances simulation by [dtSeconds] on the GL thread.
     */
    fun step(dtSeconds: Float): Float {
        val dt = dtSeconds.coerceIn(0.001f, 0.05f)
        val rawAudio = audioAnalyzer.getLatestSpectrum()
        smoothedAudio = rawAudio

        synchronized(lock) {
            val world = currentWorld
            val settings = currentSettings

            if (needsWorldRebuild) {
                particleSystem.configureForWorld(
                    world = world,
                    particleScale = settings.performanceMode.particleScale,
                    vegetationScale = settings.performanceMode.vegetationScale,
                    aspect = aspectRatio
                )
                needsWorldRebuild = false
            }

            if (needsFishRebuild) {
                val maxCap = AquariumValidator.maxPopulationFor(settings.performanceMode)
                fishSchool.rebuildPopulation(
                    roster = currentRoster,
                    maxAllowed = maxCap,
                    aspect = aspectRatio
                )
                needsFishRebuild = false
            }

            // Drain queued touch ripples
            for (i in 0 until pendingTouchCount) {
                particleSystem.addRipple(
                    worldX = pendingTouchX[i],
                    worldY = pendingTouchY[i],
                    intensity = pendingTouchStrength[i]
                )
            }
            pendingTouchCount = 0

            simTime += dt

            particleSystem.update(
                dt = dt,
                time = simTime,
                aspect = aspectRatio,
                world = world,
                audio = rawAudio,
                audioStrength = settings.audioReactivityStrength,
                reducedMotion = settings.reducedMotion
            )

            fishSchool.update(
                dt = dt,
                time = simTime,
                aspect = aspectRatio,
                world = world,
                audio = rawAudio,
                audioStrength = settings.audioReactivityStrength,
                ripples = particleSystem.ripples,
                reducedMotion = settings.reducedMotion
            )

            return aspectRatio
        }
    }
}
