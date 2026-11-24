package com.example.simulation

import com.example.model.AudioSpectrum
import com.example.model.WorldEnvironment
import kotlin.math.abs
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

class WaterRipple {
    var active: Boolean = false
    var x: Float = 0f
    var y: Float = 0f
    var radius: Float = 0f
    var strength: Float = 0f
    var age: Float = 0f
    var maxAge: Float = 2.6f
}

class AquaticParticle {
    var x: Float = 0f
    var y: Float = 0f
    var z: Float = 0.5f
    var vx: Float = 0f
    var vy: Float = 0f
    var size: Float = 0.01f
    var alpha: Float = 0.5f
    var phase: Float = 0f
    var isBubble: Boolean = false
}

class FloraStalk {
    var x: Float = 0f
    var baseY: Float = -1.0f
    var height: Float = 0.5f
    var width: Float = 0.06f
    var depth: Float = 0.5f
    var phase: Float = 0f
    var bendOffset: Float = 0f
    var bendVelocity: Float = 0f
    var tintVariation: Float = 0.5f
}

/**
 * Pooled particles (bubbles, marine snow, bioluminescent motes), interactive ripples, and reactive vegetation.
 */
class ParticleSystem(
    val maxParticles: Int = 220,
    val maxRipples: Int = 8,
    val maxFlora: Int = 46
) {
    val particles: Array<AquaticParticle> = Array(maxParticles) { AquaticParticle() }
    val ripples: Array<WaterRipple> = Array(maxRipples) { WaterRipple() }
    val flora: Array<FloraStalk> = Array(maxFlora) { FloraStalk() }

    var activeParticleCount: Int = 140
        private set
    var activeFloraCount: Int = 30
        private set

    private var rippleCursor: Int = 0
    private val rng = Random(91_337)

    fun configureForWorld(
        world: WorldEnvironment,
        particleScale: Float,
        vegetationScale: Float,
        aspect: Float
    ) {
        activeParticleCount = (maxParticles * world.particleDensity.coerceIn(0.25f, 1.0f) * particleScale)
            .toInt().coerceIn(28, maxParticles)

        val bubbleRatio = (world.bubbleRate * 0.42f).coerceIn(0.10f, 0.48f)
        for (i in 0 until maxParticles) {
            val p = particles[i]
            p.x = (rng.nextFloat() * 2f - 1f) * aspect
            p.y = rng.nextFloat() * 2f - 1f
            p.z = 0.1f + rng.nextFloat() * 0.88f
            p.phase = rng.nextFloat() * 6.283185f
            p.isBubble = i < (activeParticleCount * bubbleRatio).toInt()
            if (p.isBubble) {
                p.size = 0.008f + rng.nextFloat() * 0.016f
                p.vy = 0.09f + rng.nextFloat() * 0.16f
                p.vx = (rng.nextFloat() - 0.5f) * 0.02f
                p.alpha = 0.42f + rng.nextFloat() * 0.40f
            } else {
                p.size = 0.005f + rng.nextFloat() * 0.013f
                p.vy = (rng.nextFloat() - 0.5f) * 0.022f
                p.vx = (rng.nextFloat() - 0.5f) * 0.028f
                p.alpha = 0.24f + rng.nextFloat() * 0.48f
            }
        }

        activeFloraCount = (maxFlora * world.vegetationDensity * vegetationScale)
            .toInt().coerceIn(0, maxFlora)

        for (i in 0 until maxFlora) {
            val f = flora[i]
            val spreadT = if (activeFloraCount > 1) i.toFloat() / (activeFloraCount - 1) else 0.5f
            f.x = ((spreadT * 2f - 1f) * aspect * 1.05f) + (rng.nextFloat() - 0.5f) * 0.16f
            f.baseY = -1.04f
            f.depth = 0.12f + rng.nextFloat() * 0.85f
            f.height = 0.34f + rng.nextFloat() * 0.92f
            f.width = 0.045f + rng.nextFloat() * 0.075f
            f.phase = rng.nextFloat() * 6.283185f
            f.bendOffset = 0f
            f.bendVelocity = 0f
            f.tintVariation = rng.nextFloat()
        }

        flora.sortBy { it.depth }
    }

    fun addRipple(worldX: Float, worldY: Float, intensity: Float = 1.0f) {
        val r = ripples[rippleCursor]
        rippleCursor = (rippleCursor + 1) % maxRipples
        r.active = true
        r.x = worldX
        r.y = worldY
        r.radius = 0.02f
        r.strength = intensity.coerceIn(0.3f, 1.3f)
        r.age = 0f
        r.maxAge = 2.4f

        for (i in 0 until activeFloraCount) {
            val f = flora[i]
            val dx = f.x - worldX
            if (abs(dx) < 0.72f) {
                val push = (1f - abs(dx) / 0.72f) * (if (dx >= 0f) 1f else -1f) * 0.55f * intensity
                f.bendVelocity += push
            }
        }

        var spawnedBubbles = 0
        for (i in 0 until activeParticleCount) {
            val p = particles[i]
            val dx = p.x - worldX
            val dy = p.y - worldY
            val distSq = dx * dx + dy * dy
            if (distSq < 0.25f && distSq > 0.0001f) {
                val dist = sqrt(distSq)
                val impulse = (1f - dist / 0.5f) * 0.18f * intensity
                p.vx += (dx / dist) * impulse
                p.vy += (dy / dist) * impulse
            } else if (p.isBubble && spawnedBubbles < 4 && p.y > 0.85f) {
                p.x = worldX + (rng.nextFloat() - 0.5f) * 0.10f
                p.y = worldY + (rng.nextFloat() - 0.5f) * 0.08f
                spawnedBubbles++
            }
        }
    }

    fun update(
        dt: Float,
        time: Float,
        aspect: Float,
        world: WorldEnvironment,
        audio: AudioSpectrum,
        audioStrength: Float,
        reducedMotion: Boolean
    ) {
        val motionScale = if (reducedMotion) 0.45f else 1.0f
        val musicLift = 1.0f + (audio.treble * 0.45f + audio.bass * 0.35f) *
            audioStrength * world.audioReactivityScale

        for (r in ripples) {
            if (!r.active) continue
            r.age += dt
            r.radius += dt * (0.48f + world.turbulence * 0.18f)
            val life = (1f - r.age / r.maxAge).coerceAtLeast(0f)
            r.strength = life * life
            if (r.age >= r.maxAge) {
                r.active = false
                r.strength = 0f
            }
        }

        for (i in 0 until activeParticleCount) {
            val p = particles[i]
            if (p.isBubble) {
                val wobble = sin(time * 2.8f + p.phase) * 0.035f * motionScale
                p.x += (p.vx + wobble + world.currentStrength * 0.04f) * dt * motionScale
                p.y += (p.vy * musicLift) * dt * motionScale
                p.vx *= 0.96f
                if (p.y > 1.08f) {
                    p.y = -1.08f
                    p.x = (rng.nextFloat() * 2f - 1f) * aspect
                }
            } else {
                val driftX = sin(time * 0.45f + p.phase) * 0.028f + world.currentStrength * 0.055f
                val driftY = sin(time * 0.35f + p.phase * 1.4f) * 0.018f + audio.bass * 0.025f * audioStrength
                p.x += (p.vx + driftX) * dt * motionScale
                p.y += (p.vy + driftY) * dt * motionScale
                p.vx *= 0.97f
                p.vy *= 0.97f

                if (p.x > aspect + 0.1f) p.x = -aspect - 0.08f
                if (p.x < -aspect - 0.1f) p.x = aspect + 0.08f
                if (p.y > 1.08f) p.y = -1.06f
                if (p.y < -1.08f) p.y = 1.06f
            }
        }

        for (i in 0 until activeFloraCount) {
            val f = flora[i]
            val targetBend = sin(time * (0.95f + world.currentStrength * 0.8f) + f.phase) *
                (0.14f + world.currentStrength * 0.16f + audio.bass * 0.09f * audioStrength) * motionScale
            val springForce = (targetBend - f.bendOffset) * 5.2f
            f.bendVelocity = (f.bendVelocity + springForce * dt) * 0.91f
            f.bendOffset = (f.bendOffset + f.bendVelocity * dt).coerceIn(-0.65f, 0.65f)
        }
    }
}
