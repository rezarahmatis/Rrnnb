package com.example.simulation

import com.example.model.FishSpecies
import kotlin.math.cos
import kotlin.math.sin

/**
 * Pooled mutable simulation state for one aquatic creature.
 * Pre-allocated inside [FishSchool] so the render loop performs zero heap allocations per frame.
 */
class Fish {
    var active: Boolean = false
    var species: FishSpecies? = null

    // Normalized world coordinates:
    // x in [-aspect, +aspect], y in [-1, +1] (+1=surface, -1=floor), z in [0, 1] (0=far, 1=near)
    var x: Float = 0f
    var y: Float = 0f
    var z: Float = 0.5f

    var vx: Float = 0f
    var vy: Float = 0f
    var vz: Float = 0f

    var ax: Float = 0f
    var ay: Float = 0f
    var az: Float = 0f

    var heading: Float = 0f
    var swimPhase: Float = 0f
    var tailAmp: Float = 0.5f
    var currentSpeed: Float = 0.1f

    var size: Float = 0.08f
    var colorSeed: Float = 0f
    var wanderAngle: Float = 0f

    // Home anchor for territorial reef species
    var homeX: Float = 0f
    var homeY: Float = 0f

    // Behavioral timers
    var idleTimer: Float = 0f
    var nextIdleCooldown: Float = 6f
    var scatterTimer: Float = 0f
    var curiosityTimer: Float = 0f
    var pulseTimer: Float = 0f

    fun reset(
        species: FishSpecies,
        startX: Float,
        startY: Float,
        startZ: Float,
        startHeading: Float,
        sizeScale: Float,
        seed: Float
    ) {
        this.active = true
        this.species = species
        this.x = startX
        this.y = startY
        this.z = startZ
        this.heading = startHeading
        val initSpeed = species.baseSpeed * 0.7f
        this.vx = cos(startHeading) * initSpeed
        this.vy = sin(startHeading) * initSpeed * 0.25f
        this.vz = 0f
        this.ax = 0f
        this.ay = 0f
        this.az = 0f
        this.swimPhase = seed * 6.283185f
        this.tailAmp = 0.45f
        this.currentSpeed = initSpeed
        this.size = species.baseSize * sizeScale
        this.colorSeed = seed
        this.wanderAngle = startHeading
        this.homeX = startX
        this.homeY = startY
        this.idleTimer = 0f
        this.nextIdleCooldown = 4f + seed * 10f
        this.scatterTimer = 0f
        this.curiosityTimer = 0f
        this.pulseTimer = seed * 6.283185f
    }
}
