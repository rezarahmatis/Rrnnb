package com.example

import com.example.audio.AudioAnalyzer
import com.example.model.AquariumValidator
import com.example.model.CompatibilityStatus
import com.example.model.FishCatalog
import com.example.model.PerformanceMode
import com.example.model.SpeciesEntry
import com.example.model.WorldCatalog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

  @Test
  fun catalogs_areCompleteAndInternallyConsistent() {
    assertEquals(38, WorldCatalog.worlds.size)
    assertEquals(38, WorldCatalog.worlds.map { it.id }.toSet().size)

    assertEquals(74, FishCatalog.species.size)
    assertEquals(74, FishCatalog.species.map { it.id }.toSet().size)

    WorldCatalog.worlds.forEach { world ->
      assertTrue("World ${world.id} must have default fish", world.defaultFish.isNotEmpty())
      world.defaultFish.forEach { entry ->
        assertNotNull("Unknown species ${entry.speciesId} in world ${world.id}", FishCatalog.getById(entry.speciesId))
        assertTrue("Default count must be positive for ${entry.speciesId}", entry.count > 0)
      }
    }
  }

  @Test
  fun aquariumValidator_checksCompatibilitySerializationAndCaps() {
    val koiGarden = WorldCatalog.getById("japanese_koi_garden")
    val koi = FishCatalog.getById("koi")!!
    val anglerfish = FishCatalog.getById("anglerfish")!!

    assertEquals(
      CompatibilityStatus.NATURAL,
      AquariumValidator.checkCompatibility(koi, koiGarden, overrideEnabled = false)
    )
    assertEquals(
      CompatibilityStatus.INCOMPATIBLE,
      AquariumValidator.checkCompatibility(anglerfish, koiGarden, overrideEnabled = false)
    )
    assertEquals(
      CompatibilityStatus.OVERRIDE_ALLOWED,
      AquariumValidator.checkCompatibility(anglerfish, koiGarden, overrideEnabled = true)
    )

    val entries = listOf(
      SpeciesEntry(speciesId = "koi", count = 8, enabled = true),
      SpeciesEntry(speciesId = "goldfish", count = 4, enabled = false),
    )
    val serialized = AquariumValidator.serializeSpecies(entries)
    assertEquals(entries, AquariumValidator.deserializeSpecies(serialized))

    assertTrue(AquariumValidator.maxPopulationFor(PerformanceMode.HIGH_QUALITY) >
        AquariumValidator.maxPopulationFor(PerformanceMode.BALANCED))
    assertTrue(AquariumValidator.maxPopulationFor(PerformanceMode.BALANCED) >
        AquariumValidator.maxPopulationFor(PerformanceMode.BATTERY_SAVER))
  }

  @Test
  fun audioAnalyzer_updatesAndResetsSpectrum() {
    val analyzer = AudioAnalyzer()
    analyzer.submitBands(bass = 0.8f, mid = 0.5f, treble = 0.3f)
    val updated = analyzer.getLatestSpectrum()
    assertTrue(updated.energy > 0f)
    assertTrue(updated.isAnalyzing)
    analyzer.reset()
  }
}
