package com.example.simulation

import com.example.model.AudioSpectrum
import com.example.model.BehaviorArchetype
import com.example.model.BodyPlan
import com.example.model.WorldEnvironment
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Biologically inspired steering & locomotion behaviors differentiated by [BehaviorArchetype].
 * Operates strictly on primitive floats to prevent GC pauses during 60 FPS updates.
 */
object FishBehavior {

    private const val TWO_PI = 6.2831853f

    fun stepFish(
        fish: Fish,
        pool: Array<Fish>,
        activeCount: Int,
        dt: Float,
        time: Float,
        aspect: Float,
        world: WorldEnvironment,
        audio: AudioSpectrum,
        audioStrength: Float,
        ripples: Array<WaterRipple>,
        reducedMotion: Boolean
    ) {
        val sp = fish.species ?: return
        val motionScale = if (reducedMotion) 0.50f else 1.0f
        val musicBoost = 1.0f + (audio.energy * 0.34f + audio.bass * 0.22f) *
            audioStrength * world.audioReactivityScale

        fish.ax = 0f
        fish.ay = 0f
        fish.az = 0f

        // 1. Natural idle pauses for species that hover & observe
        fish.nextIdleCooldown -= dt
        if (fish.idleTimer > 0f) {
            fish.idleTimer -= dt
        } else if (fish.nextIdleCooldown <= 0f) {
            val canPause = sp.behavior == BehaviorArchetype.KOI_GRACEFUL ||
                sp.behavior == BehaviorArchetype.BETTA_SOLITARY ||
                sp.behavior == BehaviorArchetype.REEF_TERRITORIAL ||
                sp.behavior == BehaviorArchetype.BENTHIC_FORAGER ||
                sp.behavior == BehaviorArchetype.DEEP_DRIFTER
            if (canPause && sin(time * 1.7f + fish.colorSeed * 41f) > 0.35f) {
                fish.idleTimer = 1.2f + fish.colorSeed * 2.4f
            }
            fish.nextIdleCooldown = 5.5f + fish.colorSeed * 9.0f
        }

        val idleFactor = if (fish.idleTimer > 0f && fish.scatterTimer <= 0f) 0.28f else 1.0f

        // 2. Species-scoped boids flocking (separation, alignment, cohesion)
        var sepX = 0f; var sepY = 0f; var sepZ = 0f
        var aliX = 0f; var aliY = 0f
        var cohX = 0f; var cohY = 0f
        var neighborCount = 0

        val perceptionRadius = when (sp.behavior) {
            BehaviorArchetype.TETRA_SCHOOL -> 0.42f
            BehaviorArchetype.PELAGIC_CRUISER -> 0.56f
            BehaviorArchetype.KOI_GRACEFUL -> 0.48f
            BehaviorArchetype.BETTA_SOLITARY -> 0.42f
            else -> 0.38f
        }
        val sepRadius = (fish.size * 2.6f).coerceAtLeast(0.11f)
        val perceptionSq = perceptionRadius * perceptionRadius
        val sepSq = sepRadius * sepRadius

        for (i in 0 until activeCount) {
            val other = pool[i]
            if (!other.active || other === fish) continue
            val otherSp = other.species ?: continue

            val dx = fish.x - other.x
            val dy = fish.y - other.y
            val dz = (fish.z - other.z) * 0.6f
            val distSq = dx * dx + dy * dy + dz * dz
            if (distSq <= 0.00001f || distSq > perceptionSq) continue

            val sameSpecies = otherSp.id == sp.id

            if (distSq < sepSq) {
                val dist = sqrt(distSq)
                val invDist = 1f / dist
                val territorialGain = if (sp.behavior == BehaviorArchetype.BETTA_SOLITARY) 2.2f else 1.0f
                val push = (sepRadius - dist) * invDist * territorialGain
                sepX += dx * push
                sepY += dy * push
                sepZ += dz * push * 0.35f
            }

            if (sameSpecies && sp.schoolingWeight > 0.12f) {
                aliX += other.vx
                aliY += other.vy
                cohX += other.x
                cohY += other.y
                neighborCount++
            }
        }

        fish.ax += sepX * 1.45f
        fish.ay += sepY * 1.45f
        fish.az += sepZ * 0.65f

        if (neighborCount > 0) {
            val invN = 1f / neighborCount
            val alignWeight = sp.schoolingWeight * (if (sp.behavior == BehaviorArchetype.TETRA_SCHOOL) 2.6f else 1.15f)
            val cohWeight = sp.schoolingWeight * (if (sp.behavior == BehaviorArchetype.TETRA_SCHOOL) 1.55f else 0.65f)
            fish.ax += (aliX * invN - fish.vx) * alignWeight
            fish.ay += (aliY * invN - fish.vy) * alignWeight
            fish.ax += (cohX * invN - fish.x) * cohWeight
            fish.ay += (cohY * invN - fish.y) * cohWeight
        }

        // 3. Archetype-specific organic locomotion
        val seedPhase = fish.colorSeed * 37.3f
        when (sp.behavior) {
            BehaviorArchetype.JELLYFISH_PULSE -> {
                fish.pulseTimer += dt * (1.55f + audio.bass * 0.65f * audioStrength)
                val pulse = sin(fish.pulseTimer)
                val thrust = if (pulse > 0f) pulse * pulse * 0.16f else -0.025f
                val driftX = sin(time * 0.32f + seedPhase) * 0.04f + world.currentStrength * 0.03f
                fish.ax += (driftX - fish.vx) * 1.1f
                fish.ay += (thrust - fish.vy) * 1.6f
            }

            BehaviorArchetype.RAY_GLIDER -> {
                fish.wanderAngle += sin(time * 0.28f + seedPhase) * dt * 0.38f
                val desiredVx = cos(fish.wanderAngle) * sp.baseSpeed * musicBoost
                val desiredVy = sin(fish.wanderAngle) * sp.baseSpeed * 0.32f
                fish.ax += (desiredVx - fish.vx) * 0.85f
                fish.ay += (desiredVy - fish.vy) * 0.85f
            }

            BehaviorArchetype.SHARK_ARC -> {
                val arcDir = if (fish.colorSeed > 0.5f) 1f else -1f
                fish.wanderAngle += (0.14f * arcDir + sin(time * 0.22f + seedPhase) * 0.18f) * dt
                val desiredVx = cos(fish.wanderAngle) * sp.baseSpeed * musicBoost
                val desiredVy = sin(fish.wanderAngle) * sp.baseSpeed * 0.22f
                fish.ax += (desiredVx - fish.vx) * 0.72f
                fish.ay += (desiredVy - fish.vy) * 0.72f
            }

            BehaviorArchetype.REEF_TERRITORIAL -> {
                fish.wanderAngle += sin(time * 1.25f + seedPhase) * dt * 1.6f
                val desiredVx = cos(fish.wanderAngle) * sp.baseSpeed * idleFactor * musicBoost
                val desiredVy = sin(fish.wanderAngle) * sp.baseSpeed * 0.55f * idleFactor
                fish.ax += (desiredVx - fish.vx) * 1.6f
                fish.ay += (desiredVy - fish.vy) * 1.6f
                val hx = fish.homeX - fish.x
                val hy = fish.homeY - fish.y
                val hDistSq = hx * hx + hy * hy
                if (hDistSq > 0.22f) {
                    fish.ax += hx * 0.65f
                    fish.ay += hy * 0.65f
                }
            }

            BehaviorArchetype.BENTHIC_FORAGER -> {
                fish.wanderAngle += sin(time * 0.65f + seedPhase) * dt * 0.85f
                val desiredVx = cos(fish.wanderAngle) * sp.baseSpeed * idleFactor
                val desiredVy = sin(time * 0.45f + seedPhase) * sp.baseSpeed * 0.22f
                fish.ax += (desiredVx - fish.vx) * 1.25f
                fish.ay += (desiredVy - fish.vy) * 1.25f
            }

            BehaviorArchetype.DEEP_DRIFTER -> {
                val irregular = sin(time * 0.45f + seedPhase) * cos(time * 0.19f + seedPhase * 0.5f)
                fish.wanderAngle += irregular * dt * 0.75f
                val desiredVx = cos(fish.wanderAngle) * sp.baseSpeed * idleFactor * musicBoost
                val desiredVy = sin(fish.wanderAngle) * sp.baseSpeed * 0.42f
                fish.ax += (desiredVx - fish.vx) * 0.95f
                fish.ay += (desiredVy - fish.vy) * 0.95f
            }

            BehaviorArchetype.BETTA_SOLITARY -> {
                fish.wanderAngle += sin(time * 0.52f + seedPhase) * dt * 0.72f
                val desiredVx = cos(fish.wanderAngle) * sp.baseSpeed * idleFactor * musicBoost
                val desiredVy = sin(fish.wanderAngle) * sp.baseSpeed * 0.48f * idleFactor
                fish.ax += (desiredVx - fish.vx) * 1.15f
                fish.ay += (desiredVy - fish.vy) * 1.15f
            }

            BehaviorArchetype.TETRA_SCHOOL -> {
                val globalSchoolWave = sin(time * 0.58f + sp.id.hashCode() * 0.01f) * 0.55f
                fish.wanderAngle += (globalSchoolWave + sin(time * 1.4f + seedPhase) * 0.7f) * dt
                val desiredVx = cos(fish.wanderAngle) * sp.baseSpeed * musicBoost
                val desiredVy = sin(fish.wanderAngle) * sp.baseSpeed * 0.44f * musicBoost
                fish.ax += (desiredVx - fish.vx) * 1.9f
                fish.ay += (desiredVy - fish.vy) * 1.9f
            }

            BehaviorArchetype.KOI_GRACEFUL,
            BehaviorArchetype.PELAGIC_CRUISER -> {
                fish.wanderAngle += sin(time * 0.42f + seedPhase) * dt * 0.62f
                val desiredVx = cos(fish.wanderAngle) * sp.baseSpeed * idleFactor * musicBoost
                val desiredVy = sin(fish.wanderAngle) * sp.baseSpeed * 0.38f * idleFactor
                fish.ax += (desiredVx - fish.vx) * 1.05f
                fish.ay += (desiredVy - fish.vy) * 1.05f
            }
        }

        // Subtle ambient water current
        fish.ax += world.currentStrength * 0.045f * sin(time * 0.3f + fish.y * 1.8f)

        // 4. Touch ripple & user interaction
        if (fish.scatterTimer > 0f) fish.scatterTimer -= dt
        if (fish.curiosityTimer > 0f) fish.curiosityTimer -= dt

        for (r in ripples) {
            if (!r.active) continue
            val dx = fish.x - r.x
            val dy = fish.y - r.y
            val distSq = dx * dx + dy * dy
            val interactRadius = 0.62f + r.radius * 0.5f
            if (distSq < interactRadius * interactRadius && distSq > 0.0001f) {
                val dist = sqrt(distSq)
                val falloff = (1f - dist / interactRadius) * r.strength
                if (sp.skittishness > sp.curiosity || dist < 0.22f) {
                    val fleeForce = falloff * (0.95f + sp.skittishness * 1.35f)
                    fish.ax += (dx / dist) * fleeForce
                    fish.ay += (dy / dist) * fleeForce
                    fish.scatterTimer = 0.75f
                } else {
                    val approachForce = falloff * sp.curiosity * 0.55f
                    fish.ax -= (dx / dist) * approachForce
                    fish.ay -= (dy / dist) * approachForce
                    fish.curiosityTimer = 1.4f
                }
            }
        }

        // 5. Soft boundary & preferred depth zone containment
        val targetMinY = 1.0f - sp.depthMax * 1.88f
        val targetMaxY = 1.0f - sp.depthMin * 1.88f
        if (fish.y < targetMinY) {
            fish.ay += (targetMinY - fish.y) * 1.85f
        } else if (fish.y > targetMaxY) {
            fish.ay -= (fish.y - targetMaxY) * 1.85f
        }

        val marginX = aspect + 0.08f
        val turnZoneX = aspect - 0.16f
        if (fish.x > turnZoneX) {
            val factor = ((fish.x - turnZoneX) / 0.34f).coerceIn(0f, 1.5f)
            fish.ax -= factor * (0.85f + sp.baseSpeed * 2.2f)
            fish.wanderAngle = lerpAngle(fish.wanderAngle, Math.PI.toFloat(), dt * 1.5f)
        } else if (fish.x < -turnZoneX) {
            val factor = ((-turnZoneX - fish.x) / 0.34f).coerceIn(0f, 1.5f)
            fish.ax += factor * (0.85f + sp.baseSpeed * 2.2f)
            fish.wanderAngle = lerpAngle(fish.wanderAngle, 0f, dt * 1.5f)
        }
        if (fish.x > marginX + 0.3f) fish.x = marginX + 0.3f
        if (fish.x < -(marginX + 0.3f)) fish.x = -(marginX + 0.3f)

        val desiredZ = 0.18f + 0.68f * (0.5f + 0.5f * sin(time * 0.16f + seedPhase))
        fish.az += (desiredZ - fish.z) * 0.32f

        // 6. Integrate velocity with species-specific turning inertia
        val responsiveness = (1.0f - sp.turnInertia * 0.68f).coerceIn(0.18f, 0.92f)
        fish.vx += fish.ax * dt * responsiveness * 2.4f
        fish.vy += fish.ay * dt * responsiveness * 2.4f
        fish.vz += fish.az * dt * 0.8f
        fish.vz *= 0.94f

        val speed = sqrt(fish.vx * fish.vx + fish.vy * fish.vy)
        val maxSpeed = sp.baseSpeed * (if (fish.scatterTimer > 0f) 2.25f else 1.38f) * musicBoost * motionScale
        val minSpeed = if (sp.behavior == BehaviorArchetype.JELLYFISH_PULSE || fish.idleTimer > 0f) {
            sp.baseSpeed * 0.12f * motionScale
        } else {
            sp.baseSpeed * 0.42f * motionScale
        }

        if (speed > maxSpeed && speed > 0.0001f) {
            val scale = maxSpeed / speed
            fish.vx *= scale
            fish.vy *= scale
        } else if (speed < minSpeed && speed > 0.0001f) {
            val scale = minSpeed / speed
            fish.vx *= scale
            fish.vy *= scale
        }

        fish.x += fish.vx * dt * motionScale
        fish.y += fish.vy * dt * motionScale
        fish.z = (fish.z + fish.vz * dt * motionScale).coerceIn(0.08f, 0.96f)
        fish.y = fish.y.coerceIn(-0.96f, 0.94f)

        val finalSpeed = sqrt(fish.vx * fish.vx + fish.vy * fish.vy)
        fish.currentSpeed = finalSpeed

        // 7. Smooth heading & tail propulsion cadence
        if (sp.behavior == BehaviorArchetype.JELLYFISH_PULSE) {
            val tilt = (-fish.vx * 2.2f).coerceIn(-0.42f, 0.42f)
            fish.heading = lerpAngle(fish.heading, (Math.PI.toFloat() * 0.5f) + tilt, dt * 3.0f)
            fish.swimPhase = fish.pulseTimer
            fish.tailAmp = 0.55f + 0.35f * sin(fish.pulseTimer)
        } else if (sp.bodyPlan == BodyPlan.SEAHORSE) {
            val faceAngle = if (fish.vx >= 0f) 0f else Math.PI.toFloat()
            fish.heading = lerpAngle(fish.heading, faceAngle, dt * 2.5f)
            fish.swimPhase = (fish.swimPhase + dt * 4.5f * motionScale) % TWO_PI
            fish.tailAmp = 0.25f
        } else if (finalSpeed > 0.004f) {
            val targetHeading = atan2(fish.vy, fish.vx)
            val turnRate = (1.15f - sp.turnInertia) * 14.0f
            fish.heading = lerpAngle(fish.heading, targetHeading, (dt * turnRate).coerceIn(0.02f, 0.32f))
            val strokeFreq = 3.2f + (finalSpeed / sp.baseSpeed.coerceAtLeast(0.03f)) * 5.2f +
                audio.mid * 1.4f * audioStrength
            fish.swimPhase = (fish.swimPhase + dt * strokeFreq * motionScale) % TWO_PI
            val targetAmp = (0.35f + (finalSpeed / maxSpeed.coerceAtLeast(0.05f)) * 0.65f).coerceIn(0.25f, 1.0f)
            fish.tailAmp += (targetAmp - fish.tailAmp) * (dt * 4.5f).coerceIn(0f, 1f)
        }
    }

    private fun lerpAngle(current: Float, target: Float, t: Float): Float {
        var diff = (target - current) % TWO_PI
        if (diff > Math.PI) diff -= TWO_PI
        if (diff < -Math.PI) diff += TWO_PI
        return current + diff * t.coerceIn(0f, 1f)
    }
}
