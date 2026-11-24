package com.example.data.repository

import com.example.data.db.AquariumDao
import com.example.data.db.SavedAquariumEntity
import com.example.model.AquariumConfiguration
import com.example.model.AquariumValidator
import com.example.model.SpeciesEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AquariumRepository(private val dao: AquariumDao) {

    val savedAquariums: Flow<List<AquariumConfiguration>> = dao.observeAll().map { rows ->
        rows.map { row ->
            AquariumConfiguration(
                id = row.id,
                name = row.name,
                worldId = row.worldId,
                species = AquariumValidator.deserializeSpecies(row.speciesSerialized),
                createdAt = row.createdAt,
                updatedAt = row.updatedAt
            )
        }
    }

    suspend fun ensureStarterPresets() {
        if (dao.count() > 0) return
        val now = System.currentTimeMillis()
        dao.insert(
            SavedAquariumEntity(
                name = "My Japanese Garden",
                worldId = "japanese_koi_garden",
                speciesSerialized = AquariumValidator.serializeSpecies(
                    listOf(
                        SpeciesEntry("koi", 10),
                        SpeciesEntry("goldfish", 4),
                        SpeciesEntry("common_carp", 2)
                    )
                ),
                createdAt = now - 3000L,
                updatedAt = now - 3000L
            )
        )
        dao.insert(
            SavedAquariumEntity(
                name = "Sunlit Coral Terrace",
                worldId = "coral_reef",
                speciesSerialized = AquariumValidator.serializeSpecies(
                    listOf(
                        SpeciesEntry("clownfish", 5),
                        SpeciesEntry("blue_tang", 3),
                        SpeciesEntry("damselfish", 5),
                        SpeciesEntry("butterflyfish", 3),
                        SpeciesEntry("pufferfish", 1)
                    )
                ),
                createdAt = now - 2000L,
                updatedAt = now - 2000L
            )
        )
        dao.insert(
            SavedAquariumEntity(
                name = "Midnight Reef",
                worldId = "bioluminescent_ocean",
                speciesSerialized = AquariumValidator.serializeSpecies(
                    listOf(
                        SpeciesEntry("lanternfish", 8),
                        SpeciesEntry("bioluminescent_fish", 5),
                        SpeciesEntry("jellyfish", 3),
                        SpeciesEntry("deep_sea_squid", 2)
                    )
                ),
                createdAt = now - 1000L,
                updatedAt = now - 1000L
            )
        )
    }

    suspend fun saveConfiguration(name: String, worldId: String, species: List<SpeciesEntry>): Long {
        val now = System.currentTimeMillis()
        return dao.insert(
            SavedAquariumEntity(
                name = name.trim().ifBlank { "Custom Aquarium" },
                worldId = worldId,
                speciesSerialized = AquariumValidator.serializeSpecies(species),
                createdAt = now,
                updatedAt = now
            )
        )
    }

    suspend fun updateConfiguration(config: AquariumConfiguration) {
        dao.update(
            SavedAquariumEntity(
                id = config.id,
                name = config.name.trim().ifBlank { "Custom Aquarium" },
                worldId = config.worldId,
                speciesSerialized = AquariumValidator.serializeSpecies(config.species),
                createdAt = config.createdAt,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun renameConfiguration(id: Long, newName: String) {
        val clean = newName.trim()
        if (clean.isNotEmpty()) {
            dao.rename(id, clean)
        }
    }

    suspend fun deleteConfiguration(id: Long) {
        dao.deleteById(id)
    }
}
