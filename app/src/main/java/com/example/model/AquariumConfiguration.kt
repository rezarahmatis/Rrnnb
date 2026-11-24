package com.example.model

data class SpeciesEntry(
    val speciesId: String,
    val count: Int,
    val enabled: Boolean = true
)

data class AquariumConfiguration(
    val id: Long = 0L,
    val name: String,
    val worldId: String,
    val species: List<SpeciesEntry>,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val totalActiveFish: Int
        get() = species.filter { it.enabled }.sumOf { it.count }
}

enum class CompatibilityStatus(val label: String) {
    NATURAL("Natural Habitat"),
    OVERRIDE_ALLOWED("Cross-Habitat Allowed"),
    INCOMPATIBLE("Different Habitat")
}

object AquariumValidator {
    fun maxPopulationFor(mode: PerformanceMode): Int = when (mode) {
        PerformanceMode.HIGH_QUALITY -> 110
        PerformanceMode.BALANCED -> 75
        PerformanceMode.BATTERY_SAVER -> 42
    }

    fun checkCompatibility(
        species: FishSpecies,
        world: WorldEnvironment,
        overrideEnabled: Boolean
    ): CompatibilityStatus {
        return when {
            species.isNaturallyCompatibleWith(world) -> CompatibilityStatus.NATURAL
            overrideEnabled -> CompatibilityStatus.OVERRIDE_ALLOWED
            else -> CompatibilityStatus.INCOMPATIBLE
        }
    }

    fun serializeSpecies(entries: List<SpeciesEntry>): String =
        entries.joinToString(";") { "${it.speciesId}:${it.count}:${if (it.enabled) 1 else 0}" }

    fun deserializeSpecies(raw: String): List<SpeciesEntry> {
        if (raw.isBlank()) return emptyList()
        return raw.split(";").mapNotNull { token ->
            val parts = token.split(":")
            if (parts.size < 2) return@mapNotNull null
            val id = parts[0].trim()
            val count = parts[1].toIntOrNull()?.coerceIn(1, 40) ?: return@mapNotNull null
            val enabled = parts.getOrNull(2)?.let { it != "0" } ?: true
            if (FishCatalog.getById(id) != null) SpeciesEntry(id, count, enabled) else null
        }
    }
}
