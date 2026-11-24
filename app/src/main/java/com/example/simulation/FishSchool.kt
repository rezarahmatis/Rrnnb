package com.example.simulation

import com.example.model.AudioSpectrum
import com.example.model.FishCatalog
import com.example.model.SpeciesEntry
import com.example.model.WorldEnvironment
import kotlin.random.Random

/**
 * Pooled multi-species fish population manager.
 * Supports arbitrary mixes of species simultaneously with zero per-frame allocations.
 */
class FishSchool(val maxCapacity: Int = 128) {

    val pool: Array<Fish> = Array(maxCapacity) { Fish() }
    var activeCount: Int = 0
        private set

    // Pre-allocated index buffer for back-to-front depth sorting
    val depthOrder: IntArray = IntArray(maxCapacity) { it }

    private val rng = Random(42_108)

    fun rebuildPopulation(
        roster: List<SpeciesEntry>,
        maxAllowed: Int,
        aspect: Float
    ) {
        val cap = maxAllowed.coerceAtMost(maxCapacity)
        var slot = 0

        for (entry in roster) {
            if (!entry.enabled || entry.count <= 0) continue
            val species = FishCatalog.getById(entry.speciesId) ?: continue

            val clusterX = (rng.nextFloat() * 2f - 1f) * (aspect * 0.65f)
            val clusterY = 1.0f - (species.depthMin + rng.nextFloat() * (species.depthMax - species.depthMin)) * 1.8f

            for (n in 0 until entry.count) {
                if (slot >= cap) break
                val spread = if (species.schoolingWeight > 0.6f) 0.28f else 0.95f
                val startX = (clusterX + (rng.nextFloat() * 2f - 1f) * spread).coerceIn(-aspect * 0.9f, aspect * 0.9f)
                val minY = 1.0f - species.depthMax * 1.85f
                val maxY = 1.0f - species.depthMin * 1.85f
                val startY = (clusterY + (rng.nextFloat() * 2f - 1f) * spread * 0.55f).coerceIn(minY, maxY)
                val startZ = 0.14f + rng.nextFloat() * 0.78f
                val startHeading = if (rng.nextBoolean()) {
                    (rng.nextFloat() - 0.5f) * 0.45f
                } else {
                    Math.PI.toFloat() + (rng.nextFloat() - 0.5f) * 0.45f
                }
                val sizeVariance = 0.84f + rng.nextFloat() * 0.34f
                val seed = rng.nextFloat()

                pool[slot].reset(species, startX, startY, startZ, startHeading, sizeVariance, seed)
                depthOrder[slot] = slot
                slot++
            }
        }

        for (i in slot until maxCapacity) {
            pool[i].active = false
            pool[i].species = null
        }
        activeCount = slot
    }

    fun update(
        dt: Float,
        time: Float,
        aspect: Float,
        world: WorldEnvironment,
        audio: AudioSpectrum,
        audioStrength: Float,
        ripples: Array<WaterRipple>,
        reducedMotion: Boolean
    ) {
        for (i in 0 until activeCount) {
            val fish = pool[i]
            if (!fish.active) continue
            FishBehavior.stepFish(
                fish = fish,
                pool = pool,
                activeCount = activeCount,
                dt = dt,
                time = time,
                aspect = aspect,
                world = world,
                audio = audio,
                audioStrength = audioStrength,
                ripples = ripples,
                reducedMotion = reducedMotion
            )
        }

        // Fast insertion sort on nearly-sorted depth indices (O(N) typical, zero allocations)
        for (i in 1 until activeCount) {
            val key = depthOrder[i]
            val keyZ = pool[key].z
            var j = i - 1
            while (j >= 0 && pool[depthOrder[j]].z > keyZ) {
                depthOrder[j + 1] = depthOrder[j]
                j--
            }
            depthOrder[j + 1] = key
        }
    }
}
