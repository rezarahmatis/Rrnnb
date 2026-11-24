package com.example.model

enum class FishCategory(val label: String) {
    FRESHWATER("Freshwater"),
    TROPICAL_REEF("Tropical & Reef"),
    LARGE_OCEAN("Large Ocean"),
    DEEP_SEA("Deep Sea"),
    OTHER_AQUATIC("Other Aquatic")
}

enum class BodyPlan(val shaderId: Int) {
    CARP_FUSIFORM(0),
    SLENDER_SCHOOLING(1),
    DEEP_DISC(2),
    FLOWING_FIN(3),
    REEF_OVAL(4),
    FLAT_RAY(5),
    SHARK_PELAGIC(6),
    ELONGATED_EEL(7),
    JELLYFISH_BELL(8),
    SEA_TURTLE(9),
    CEPHALOPOD(10),
    SEAHORSE(11),
    BENTHIC_CRUSTACEAN(12)
}

enum class PatternType(val shaderId: Int) {
    SOLID_GRADIENT(0),
    KOI_CALICO(1),
    VERTICAL_BARS(2),
    HORIZONTAL_STRIPE(3),
    SPOTTED(4),
    BIOLUMINESCENT(5),
    MANDARIN_SWIRL(6),
    MOTTLED(7)
}

enum class BehaviorArchetype {
    KOI_GRACEFUL,
    TETRA_SCHOOL,
    BETTA_SOLITARY,
    REEF_TERRITORIAL,
    PELAGIC_CRUISER,
    RAY_GLIDER,
    SHARK_ARC,
    JELLYFISH_PULSE,
    DEEP_DRIFTER,
    BENTHIC_FORAGER
}

data class FishSpecies(
    val id: String,
    val name: String,
    val scientificName: String,
    val description: String,
    val category: FishCategory,
    val compatibleHabitats: Set<HabitatType>,
    val bodyPlan: BodyPlan,
    val pattern: PatternType,
    val behavior: BehaviorArchetype,
    val primaryColor: RgbColor,
    val secondaryColor: RgbColor,
    val accentColor: RgbColor,
    val finColor: RgbColor,
    val baseSize: Float,
    val aspectRatio: Float,
    val baseSpeed: Float,
    val turnInertia: Float,
    val schoolingWeight: Float,
    val depthMin: Float,
    val depthMax: Float,
    val curiosity: Float,
    val skittishness: Float,
    val glowIntensity: Float = 0.0f,
    val recommendedMax: Int = 16
) {
    fun isNaturallyCompatibleWith(world: WorldEnvironment): Boolean {
        if (world.habitat == HabitatType.EXOTIC) return true
        return world.habitat in compatibleHabitats
    }
}

object FishCatalog {
    private val FW = setOf(HabitatType.FRESHWATER, HabitatType.BRACKISH, HabitatType.EXOTIC)
    private val REEF = setOf(HabitatType.TROPICAL_MARINE, HabitatType.EXOTIC)
    private val OCEAN = setOf(HabitatType.OPEN_OCEAN, HabitatType.TROPICAL_MARINE, HabitatType.COLD_MARINE, HabitatType.EXOTIC)
    private val DEEP = setOf(HabitatType.DEEP_SEA, HabitatType.OPEN_OCEAN, HabitatType.COLD_MARINE, HabitatType.EXOTIC)
    private val MARINE_ALL = setOf(HabitatType.TROPICAL_MARINE, HabitatType.OPEN_OCEAN, HabitatType.COLD_MARINE, HabitatType.BRACKISH, HabitatType.EXOTIC)
    private val ALL_HABITATS = HabitatType.entries.toSet()

    val species: List<FishSpecies> = listOf(
        // ═══════════════════════ FRESHWATER FISH (27) ═══════════════════════
        FishSpecies(
            id = "koi", name = "Koi", scientificName = "Cyprinus rubrofuscus",
            description = "Graceful ornamental carp gliding in calm, wide curves with calico markings.",
            category = FishCategory.FRESHWATER, compatibleHabitats = FW,
            bodyPlan = BodyPlan.CARP_FUSIFORM, pattern = PatternType.KOI_CALICO,
            behavior = BehaviorArchetype.KOI_GRACEFUL,
            primaryColor = RgbColor(0.95f, 0.94f, 0.90f), secondaryColor = RgbColor(0.88f, 0.26f, 0.12f),
            accentColor = RgbColor(0.12f, 0.12f, 0.14f), finColor = RgbColor(0.96f, 0.90f, 0.82f),
            baseSize = 0.125f, aspectRatio = 2.4f, baseSpeed = 0.12f, turnInertia = 0.86f,
            schoolingWeight = 0.45f, depthMin = 0.15f, depthMax = 0.72f, curiosity = 0.82f, skittishness = 0.20f
        ),
        FishSpecies(
            id = "goldfish", name = "Goldfish", scientificName = "Carassius auratus",
            description = "Luminous amber-gold pond swimmer with delicate trailing fins.",
            category = FishCategory.FRESHWATER, compatibleHabitats = FW,
            bodyPlan = BodyPlan.FLOWING_FIN, pattern = PatternType.KOI_CALICO,
            behavior = BehaviorArchetype.KOI_GRACEFUL,
            primaryColor = RgbColor(0.95f, 0.48f, 0.12f), secondaryColor = RgbColor(0.98f, 0.84f, 0.56f),
            accentColor = RgbColor(0.84f, 0.22f, 0.08f), finColor = RgbColor(0.98f, 0.74f, 0.42f),
            baseSize = 0.082f, aspectRatio = 2.0f, baseSpeed = 0.14f, turnInertia = 0.76f,
            schoolingWeight = 0.50f, depthMin = 0.18f, depthMax = 0.78f, curiosity = 0.72f, skittishness = 0.35f
        ),
        FishSpecies(
            id = "common_carp", name = "Common Carp", scientificName = "Cyprinus carpio",
            description = "Bronze-scaled river and pond carp with steady, deliberate momentum.",
            category = FishCategory.FRESHWATER, compatibleHabitats = FW,
            bodyPlan = BodyPlan.CARP_FUSIFORM, pattern = PatternType.SOLID_GRADIENT,
            behavior = BehaviorArchetype.KOI_GRACEFUL,
            primaryColor = RgbColor(0.54f, 0.44f, 0.28f), secondaryColor = RgbColor(0.76f, 0.68f, 0.48f),
            accentColor = RgbColor(0.32f, 0.26f, 0.16f), finColor = RgbColor(0.60f, 0.48f, 0.30f),
            baseSize = 0.135f, aspectRatio = 2.4f, baseSpeed = 0.11f, turnInertia = 0.88f,
            schoolingWeight = 0.35f, depthMin = 0.35f, depthMax = 0.88f, curiosity = 0.50f, skittishness = 0.30f
        ),
        FishSpecies(
            id = "grass_carp", name = "Grass Carp", scientificName = "Ctenopharyngodon idella",
            description = "Sleek silver-olive herbivore patrolling vegetated shallows.",
            category = FishCategory.FRESHWATER, compatibleHabitats = FW,
            bodyPlan = BodyPlan.CARP_FUSIFORM, pattern = PatternType.SOLID_GRADIENT,
            behavior = BehaviorArchetype.KOI_GRACEFUL,
            primaryColor = RgbColor(0.46f, 0.50f, 0.40f), secondaryColor = RgbColor(0.78f, 0.80f, 0.74f),
            accentColor = RgbColor(0.28f, 0.32f, 0.24f), finColor = RgbColor(0.56f, 0.58f, 0.50f),
            baseSize = 0.14f, aspectRatio = 2.7f, baseSpeed = 0.13f, turnInertia = 0.86f,
            schoolingWeight = 0.32f, depthMin = 0.25f, depthMax = 0.75f, curiosity = 0.40f, skittishness = 0.42f
        ),
        FishSpecies(
            id = "guppy", name = "Guppy", scientificName = "Poecilia reticulata",
            description = "Agile iridescent micro-swimmer with a soft fanned tail.",
            category = FishCategory.FRESHWATER, compatibleHabitats = FW,
            bodyPlan = BodyPlan.FLOWING_FIN, pattern = PatternType.SOLID_GRADIENT,
            behavior = BehaviorArchetype.TETRA_SCHOOL,
            primaryColor = RgbColor(0.44f, 0.62f, 0.72f), secondaryColor = RgbColor(0.86f, 0.46f, 0.32f),
            accentColor = RgbColor(0.24f, 0.78f, 0.74f), finColor = RgbColor(0.92f, 0.56f, 0.36f),
            baseSize = 0.045f, aspectRatio = 2.2f, baseSpeed = 0.20f, turnInertia = 0.54f,
            schoolingWeight = 0.72f, depthMin = 0.10f, depthMax = 0.55f, curiosity = 0.35f, skittishness = 0.78f,
            recommendedMax = 24
        ),
        FishSpecies(
            id = "molly", name = "Molly", scientificName = "Poecilia sphenops",
            description = "Velvet-dark brackish and freshwater livebearer with smooth turns.",
            category = FishCategory.FRESHWATER, compatibleHabitats = FW,
            bodyPlan = BodyPlan.REEF_OVAL, pattern = PatternType.MOTTLED,
            behavior = BehaviorArchetype.TETRA_SCHOOL,
            primaryColor = RgbColor(0.18f, 0.20f, 0.24f), secondaryColor = RgbColor(0.76f, 0.68f, 0.46f),
            accentColor = RgbColor(0.32f, 0.36f, 0.40f), finColor = RgbColor(0.28f, 0.30f, 0.34f),
            baseSize = 0.056f, aspectRatio = 2.1f, baseSpeed = 0.17f, turnInertia = 0.64f,
            schoolingWeight = 0.62f, depthMin = 0.16f, depthMax = 0.66f, curiosity = 0.45f, skittishness = 0.62f
        ),
        FishSpecies(
            id = "platy", name = "Platy", scientificName = "Xiphophorus maculatus",
            description = "Warm coral-tinted freshwater swimmer active around aquatic stems.",
            category = FishCategory.FRESHWATER, compatibleHabitats = FW,
            bodyPlan = BodyPlan.REEF_OVAL, pattern = PatternType.SOLID_GRADIENT,
            behavior = BehaviorArchetype.TETRA_SCHOOL,
            primaryColor = RgbColor(0.88f, 0.42f, 0.22f), secondaryColor = RgbColor(0.96f, 0.74f, 0.46f),
            accentColor = RgbColor(0.22f, 0.18f, 0.16f), finColor = RgbColor(0.90f, 0.56f, 0.32f),
            baseSize = 0.052f, aspectRatio = 1.9f, baseSpeed = 0.17f, turnInertia = 0.62f,
            schoolingWeight = 0.60f, depthMin = 0.20f, depthMax = 0.70f, curiosity = 0.44f, skittishness = 0.65f
        ),
        FishSpecies(
            id = "betta", name = "Betta", scientificName = "Betta splendens",
            description = "Solitary, regal labyrinth fish with silk-like cascading fins.",
            category = FishCategory.FRESHWATER, compatibleHabitats = FW,
            bodyPlan = BodyPlan.FLOWING_FIN, pattern = PatternType.SOLID_GRADIENT,
            behavior = BehaviorArchetype.BETTA_SOLITARY,
            primaryColor = RgbColor(0.16f, 0.30f, 0.68f), secondaryColor = RgbColor(0.72f, 0.16f, 0.28f),
            accentColor = RgbColor(0.26f, 0.74f, 0.80f), finColor = RgbColor(0.78f, 0.22f, 0.36f),
            baseSize = 0.080f, aspectRatio = 1.9f, baseSpeed = 0.09f, turnInertia = 0.80f,
            schoolingWeight = 0.05f, depthMin = 0.15f, depthMax = 0.65f, curiosity = 0.76f, skittishness = 0.25f,
            recommendedMax = 6
        ),
        FishSpecies(
            id = "freshwater_angelfish", name = "Angelfish (Freshwater)", scientificName = "Pterophyllum scalare",
            description = "Tall, silver-striped Amazonian cichlid gliding upright among reeds.",
            category = FishCategory.FRESHWATER, compatibleHabitats = FW,
            bodyPlan = BodyPlan.DEEP_DISC, pattern = PatternType.VERTICAL_BARS,
            behavior = BehaviorArchetype.KOI_GRACEFUL,
            primaryColor = RgbColor(0.82f, 0.84f, 0.80f), secondaryColor = RgbColor(0.18f, 0.20f, 0.22f),
            accentColor = RgbColor(0.76f, 0.66f, 0.36f), finColor = RgbColor(0.74f, 0.78f, 0.76f),
            baseSize = 0.095f, aspectRatio = 1.25f, baseSpeed = 0.10f, turnInertia = 0.84f,
            schoolingWeight = 0.45f, depthMin = 0.22f, depthMax = 0.78f, curiosity = 0.62f, skittishness = 0.32f
        ),
        FishSpecies(
            id = "discus", name = "Discus", scientificName = "Symphysodon aequifasciatus",
            description = "Serene circular Amazon swimmer etched with turquoise striations.",
            category = FishCategory.FRESHWATER, compatibleHabitats = FW,
            bodyPlan = BodyPlan.DEEP_DISC, pattern = PatternType.MANDARIN_SWIRL,
            behavior = BehaviorArchetype.KOI_GRACEFUL,
            primaryColor = RgbColor(0.76f, 0.36f, 0.22f), secondaryColor = RgbColor(0.24f, 0.68f, 0.74f),
            accentColor = RgbColor(0.88f, 0.66f, 0.36f), finColor = RgbColor(0.32f, 0.66f, 0.72f),
            baseSize = 0.098f, aspectRatio = 1.22f, baseSpeed = 0.09f, turnInertia = 0.88f,
            schoolingWeight = 0.56f, depthMin = 0.28f, depthMax = 0.76f, curiosity = 0.55f, skittishness = 0.40f
        ),
        FishSpecies(
            id = "neon_tetra", name = "Neon Tetra", scientificName = "Paracheirodon innesi",
            description = "Swift schooling characin flashing an electric cyan lateral band.",
            category = FishCategory.FRESHWATER, compatibleHabitats = FW,
            bodyPlan = BodyPlan.SLENDER_SCHOOLING, pattern = PatternType.HORIZONTAL_STRIPE,
            behavior = BehaviorArchetype.TETRA_SCHOOL,
            primaryColor = RgbColor(0.68f, 0.76f, 0.80f), secondaryColor = RgbColor(0.12f, 0.88f, 0.96f),
            accentColor = RgbColor(0.90f, 0.18f, 0.20f), finColor = RgbColor(0.78f, 0.86f, 0.92f),
            baseSize = 0.042f, aspectRatio = 2.6f, baseSpeed = 0.24f, turnInertia = 0.46f,
            schoolingWeight = 0.94f, depthMin = 0.22f, depthMax = 0.76f, curiosity = 0.18f, skittishness = 0.88f,
            glowIntensity = 0.24f, recommendedMax = 30
        ),
        FishSpecies(
            id = "cardinal_tetra", name = "Cardinal Tetra", scientificName = "Paracheirodon axelrodi",
            description = "Synchronized shoaler with a full-length crimson ventral shimmer.",
            category = FishCategory.FRESHWATER, compatibleHabitats = FW,
            bodyPlan = BodyPlan.SLENDER_SCHOOLING, pattern = PatternType.HORIZONTAL_STRIPE,
            behavior = BehaviorArchetype.TETRA_SCHOOL,
            primaryColor = RgbColor(0.88f, 0.14f, 0.18f), secondaryColor = RgbColor(0.14f, 0.82f, 0.98f),
            accentColor = RgbColor(0.94f, 0.24f, 0.22f), finColor = RgbColor(0.74f, 0.82f, 0.90f),
            baseSize = 0.044f, aspectRatio = 2.6f, baseSpeed = 0.24f, turnInertia = 0.48f,
            schoolingWeight = 0.94f, depthMin = 0.24f, depthMax = 0.78f, curiosity = 0.18f, skittishness = 0.88f,
            glowIntensity = 0.22f, recommendedMax = 30
        ),
        FishSpecies(
            id = "zebra_danio", name = "Zebra Danio", scientificName = "Danio rerio",
            description = "Brisk silver-and-indigo stream schooler darting near currents.",
            category = FishCategory.FRESHWATER, compatibleHabitats = FW,
            bodyPlan = BodyPlan.SLENDER_SCHOOLING, pattern = PatternType.HORIZONTAL_STRIPE,
            behavior = BehaviorArchetype.TETRA_SCHOOL,
            primaryColor = RgbColor(0.82f, 0.84f, 0.78f), secondaryColor = RgbColor(0.18f, 0.26f, 0.44f),
            accentColor = RgbColor(0.88f, 0.86f, 0.72f), finColor = RgbColor(0.76f, 0.78f, 0.74f),
            baseSize = 0.046f, aspectRatio = 2.8f, baseSpeed = 0.26f, turnInertia = 0.44f,
            schoolingWeight = 0.88f, depthMin = 0.12f, depthMax = 0.56f, curiosity = 0.25f, skittishness = 0.82f,
            recommendedMax = 24
        ),
        FishSpecies(
            id = "cherry_barb", name = "Cherry Barb", scientificName = "Puntius titteya",
            description = "Deep ruby-bronze stream fish weaving calmly through foliage.",
            category = FishCategory.FRESHWATER, compatibleHabitats = FW,
            bodyPlan = BodyPlan.SLENDER_SCHOOLING, pattern = PatternType.HORIZONTAL_STRIPE,
            behavior = BehaviorArchetype.TETRA_SCHOOL,
            primaryColor = RgbColor(0.76f, 0.22f, 0.20f), secondaryColor = RgbColor(0.38f, 0.12f, 0.12f),
            accentColor = RgbColor(0.88f, 0.44f, 0.34f), finColor = RgbColor(0.80f, 0.32f, 0.28f),
            baseSize = 0.050f, aspectRatio = 2.4f, baseSpeed = 0.21f, turnInertia = 0.56f,
            schoolingWeight = 0.78f, depthMin = 0.28f, depthMax = 0.78f, curiosity = 0.30f, skittishness = 0.75f
        ),
        FishSpecies(
            id = "tiger_barb", name = "Tiger Barb", scientificName = "Puntigrus tetrazona",
            description = "Golden-amber banded swimmer forming lively tight formations.",
            category = FishCategory.FRESHWATER, compatibleHabitats = FW,
            bodyPlan = BodyPlan.REEF_OVAL, pattern = PatternType.VERTICAL_BARS,
            behavior = BehaviorArchetype.TETRA_SCHOOL,
            primaryColor = RgbColor(0.88f, 0.72f, 0.42f), secondaryColor = RgbColor(0.14f, 0.16f, 0.18f),
            accentColor = RgbColor(0.86f, 0.30f, 0.18f), finColor = RgbColor(0.86f, 0.36f, 0.24f),
            baseSize = 0.058f, aspectRatio = 1.9f, baseSpeed = 0.22f, turnInertia = 0.54f,
            schoolingWeight = 0.84f, depthMin = 0.24f, depthMax = 0.74f, curiosity = 0.38f, skittishness = 0.68f
        ),
        FishSpecies(
            id = "gourami", name = "Gourami", scientificName = "Trichopodus leerii",
            description = "Pearl-speckled meditative labyrinth swimmer with soft amber hues.",
            category = FishCategory.FRESHWATER, compatibleHabitats = FW,
            bodyPlan = BodyPlan.REEF_OVAL, pattern = PatternType.SPOTTED,
            behavior = BehaviorArchetype.KOI_GRACEFUL,
            primaryColor = RgbColor(0.68f, 0.56f, 0.52f), secondaryColor = RgbColor(0.86f, 0.46f, 0.24f),
            accentColor = RgbColor(0.90f, 0.88f, 0.84f), finColor = RgbColor(0.78f, 0.62f, 0.52f),
            baseSize = 0.082f, aspectRatio = 2.0f, baseSpeed = 0.11f, turnInertia = 0.80f,
            schoolingWeight = 0.35f, depthMin = 0.16f, depthMax = 0.64f, curiosity = 0.65f, skittishness = 0.34f
        ),
        FishSpecies(
            id = "rainbowfish", name = "Rainbowfish", scientificName = "Melanotaenia boesemani",
            description = "Two-tone blue-and-saffron shoaler shimmering in sunlit currents.",
            category = FishCategory.FRESHWATER, compatibleHabitats = FW,
            bodyPlan = BodyPlan.SLENDER_SCHOOLING, pattern = PatternType.SOLID_GRADIENT,
            behavior = BehaviorArchetype.TETRA_SCHOOL,
            primaryColor = RgbColor(0.24f, 0.52f, 0.76f), secondaryColor = RgbColor(0.92f, 0.64f, 0.22f),
            accentColor = RgbColor(0.46f, 0.80f, 0.86f), finColor = RgbColor(0.88f, 0.68f, 0.34f),
            baseSize = 0.068f, aspectRatio = 2.3f, baseSpeed = 0.21f, turnInertia = 0.58f,
            schoolingWeight = 0.82f, depthMin = 0.18f, depthMax = 0.66f, curiosity = 0.40f, skittishness = 0.66f
        ),
        FishSpecies(
            id = "oscar_fish", name = "Oscar Fish", scientificName = "Astronotus ocellatus",
            description = "Intelligent velvet-charcoal cichlid accented with burnt orange marbling.",
            category = FishCategory.FRESHWATER, compatibleHabitats = FW,
            bodyPlan = BodyPlan.CARP_FUSIFORM, pattern = PatternType.MOTTLED,
            behavior = BehaviorArchetype.BETTA_SOLITARY,
            primaryColor = RgbColor(0.20f, 0.20f, 0.18f), secondaryColor = RgbColor(0.86f, 0.36f, 0.14f),
            accentColor = RgbColor(0.94f, 0.56f, 0.22f), finColor = RgbColor(0.24f, 0.22f, 0.20f),
            baseSize = 0.125f, aspectRatio = 1.9f, baseSpeed = 0.12f, turnInertia = 0.84f,
            schoolingWeight = 0.18f, depthMin = 0.28f, depthMax = 0.80f, curiosity = 0.88f, skittishness = 0.15f,
            recommendedMax = 6
        ),
        FishSpecies(
            id = "pleco", name = "Pleco", scientificName = "Hypostomus plecostomus",
            description = "Armored bottom-dwelling catfish grazing serenely over submerged stones.",
            category = FishCategory.FRESHWATER, compatibleHabitats = FW,
            bodyPlan = BodyPlan.CARP_FUSIFORM, pattern = PatternType.SPOTTED,
            behavior = BehaviorArchetype.BENTHIC_FORAGER,
            primaryColor = RgbColor(0.28f, 0.24f, 0.20f), secondaryColor = RgbColor(0.52f, 0.44f, 0.34f),
            accentColor = RgbColor(0.18f, 0.16f, 0.14f), finColor = RgbColor(0.36f, 0.32f, 0.26f),
            baseSize = 0.105f, aspectRatio = 2.6f, baseSpeed = 0.08f, turnInertia = 0.88f,
            schoolingWeight = 0.10f, depthMin = 0.72f, depthMax = 0.94f, curiosity = 0.20f, skittishness = 0.45f
        ),
        FishSpecies(
            id = "catfish", name = "Catfish", scientificName = "Silurus glanis",
            description = "Dusky river-floor cruiser navigating deep basins with quiet poise.",
            category = FishCategory.FRESHWATER, compatibleHabitats = FW,
            bodyPlan = BodyPlan.ELONGATED_EEL, pattern = PatternType.MOTTLED,
            behavior = BehaviorArchetype.BENTHIC_FORAGER,
            primaryColor = RgbColor(0.30f, 0.32f, 0.28f), secondaryColor = RgbColor(0.56f, 0.56f, 0.48f),
            accentColor = RgbColor(0.20f, 0.22f, 0.18f), finColor = RgbColor(0.34f, 0.36f, 0.32f),
            baseSize = 0.138f, aspectRatio = 3.1f, baseSpeed = 0.10f, turnInertia = 0.90f,
            schoolingWeight = 0.15f, depthMin = 0.68f, depthMax = 0.93f, curiosity = 0.28f, skittishness = 0.38f
        ),
        FishSpecies(
            id = "loach", name = "Loach", scientificName = "Chromobotia macracanthus",
            description = "Playful banded bottom swimmer darting gracefully between stems.",
            category = FishCategory.FRESHWATER, compatibleHabitats = FW,
            bodyPlan = BodyPlan.SLENDER_SCHOOLING, pattern = PatternType.VERTICAL_BARS,
            behavior = BehaviorArchetype.BENTHIC_FORAGER,
            primaryColor = RgbColor(0.88f, 0.54f, 0.20f), secondaryColor = RgbColor(0.14f, 0.14f, 0.16f),
            accentColor = RgbColor(0.92f, 0.30f, 0.16f), finColor = RgbColor(0.90f, 0.34f, 0.18f),
            baseSize = 0.084f, aspectRatio = 2.7f, baseSpeed = 0.18f, turnInertia = 0.62f,
            schoolingWeight = 0.68f, depthMin = 0.64f, depthMax = 0.92f, curiosity = 0.40f, skittishness = 0.60f
        ),
        FishSpecies(
            id = "pike", name = "Pike", scientificName = "Esox lucius",
            description = "Long olive-mottled ambush predator suspending motionless near reeds.",
            category = FishCategory.FRESHWATER, compatibleHabitats = FW,
            bodyPlan = BodyPlan.SHARK_PELAGIC, pattern = PatternType.SPOTTED,
            behavior = BehaviorArchetype.BETTA_SOLITARY,
            primaryColor = RgbColor(0.32f, 0.42f, 0.26f), secondaryColor = RgbColor(0.72f, 0.74f, 0.54f),
            accentColor = RgbColor(0.58f, 0.64f, 0.42f), finColor = RgbColor(0.56f, 0.44f, 0.30f),
            baseSize = 0.160f, aspectRatio = 3.4f, baseSpeed = 0.15f, turnInertia = 0.90f,
            schoolingWeight = 0.05f, depthMin = 0.26f, depthMax = 0.78f, curiosity = 0.42f, skittishness = 0.22f,
            recommendedMax = 5
        ),
        FishSpecies(
            id = "trout", name = "Trout", scientificName = "Oncorhynchus mykiss",
            description = "Spotted alpine stream swimmer flashing soft rose-silver iridescence.",
            category = FishCategory.FRESHWATER, compatibleHabitats = FW + HabitatType.COLD_MARINE,
            bodyPlan = BodyPlan.CARP_FUSIFORM, pattern = PatternType.SPOTTED,
            behavior = BehaviorArchetype.PELAGIC_CRUISER,
            primaryColor = RgbColor(0.42f, 0.56f, 0.54f), secondaryColor = RgbColor(0.84f, 0.56f, 0.60f),
            accentColor = RgbColor(0.16f, 0.22f, 0.22f), finColor = RgbColor(0.58f, 0.66f, 0.64f),
            baseSize = 0.118f, aspectRatio = 2.8f, baseSpeed = 0.20f, turnInertia = 0.74f,
            schoolingWeight = 0.54f, depthMin = 0.20f, depthMax = 0.74f, curiosity = 0.36f, skittishness = 0.56f
        ),
        FishSpecies(
            id = "bass", name = "Bass", scientificName = "Micropterus salmoides",
            description = "Stout olive-green lake cruiser patrolling submerged shadows.",
            category = FishCategory.FRESHWATER, compatibleHabitats = FW,
            bodyPlan = BodyPlan.CARP_FUSIFORM, pattern = PatternType.HORIZONTAL_STRIPE,
            behavior = BehaviorArchetype.KOI_GRACEFUL,
            primaryColor = RgbColor(0.34f, 0.46f, 0.30f), secondaryColor = RgbColor(0.76f, 0.80f, 0.70f),
            accentColor = RgbColor(0.18f, 0.24f, 0.16f), finColor = RgbColor(0.42f, 0.50f, 0.36f),
            baseSize = 0.120f, aspectRatio = 2.4f, baseSpeed = 0.15f, turnInertia = 0.82f,
            schoolingWeight = 0.28f, depthMin = 0.30f, depthMax = 0.80f, curiosity = 0.50f, skittishness = 0.40f
        ),
        FishSpecies(
            id = "perch", name = "Perch", scientificName = "Perca fluviatilis",
            description = "Olive-gold barred lake fish with subtle crimson pelvic fins.",
            category = FishCategory.FRESHWATER, compatibleHabitats = FW,
            bodyPlan = BodyPlan.REEF_OVAL, pattern = PatternType.VERTICAL_BARS,
            behavior = BehaviorArchetype.TETRA_SCHOOL,
            primaryColor = RgbColor(0.52f, 0.58f, 0.36f), secondaryColor = RgbColor(0.22f, 0.26f, 0.18f),
            accentColor = RgbColor(0.80f, 0.30f, 0.18f), finColor = RgbColor(0.78f, 0.32f, 0.20f),
            baseSize = 0.088f, aspectRatio = 2.3f, baseSpeed = 0.16f, turnInertia = 0.72f,
            schoolingWeight = 0.68f, depthMin = 0.26f, depthMax = 0.78f, curiosity = 0.42f, skittishness = 0.54f
        ),
        FishSpecies(
            id = "minnow", name = "Minnow", scientificName = "Phoxinus phoxinus",
            description = "Delicate silver stream shoaler turning together in glistening clouds.",
            category = FishCategory.FRESHWATER, compatibleHabitats = FW,
            bodyPlan = BodyPlan.SLENDER_SCHOOLING, pattern = PatternType.HORIZONTAL_STRIPE,
            behavior = BehaviorArchetype.TETRA_SCHOOL,
            primaryColor = RgbColor(0.74f, 0.78f, 0.76f), secondaryColor = RgbColor(0.38f, 0.44f, 0.38f),
            accentColor = RgbColor(0.84f, 0.88f, 0.86f), finColor = RgbColor(0.76f, 0.80f, 0.78f),
            baseSize = 0.042f, aspectRatio = 2.9f, baseSpeed = 0.25f, turnInertia = 0.46f,
            schoolingWeight = 0.92f, depthMin = 0.15f, depthMax = 0.68f, curiosity = 0.16f, skittishness = 0.90f,
            recommendedMax = 30
        ),
        FishSpecies(
            id = "white_cloud_minnow", name = "White Cloud Mountain Minnow", scientificName = "Tanichthys albonubes",
            description = "Cool-water alpine minnow displaying a subtle golden lateral gleam.",
            category = FishCategory.FRESHWATER, compatibleHabitats = FW + HabitatType.COLD_MARINE,
            bodyPlan = BodyPlan.SLENDER_SCHOOLING, pattern = PatternType.HORIZONTAL_STRIPE,
            behavior = BehaviorArchetype.TETRA_SCHOOL,
            primaryColor = RgbColor(0.62f, 0.66f, 0.60f), secondaryColor = RgbColor(0.88f, 0.76f, 0.40f),
            accentColor = RgbColor(0.82f, 0.28f, 0.24f), finColor = RgbColor(0.84f, 0.38f, 0.32f),
            baseSize = 0.044f, aspectRatio = 2.7f, baseSpeed = 0.23f, turnInertia = 0.48f,
            schoolingWeight = 0.90f, depthMin = 0.14f, depthMax = 0.65f, curiosity = 0.22f, skittishness = 0.84f,
            recommendedMax = 28
        ),

        // ═══════════════════════ TROPICAL / REEF FISH (21) ═══════════════════════
        FishSpecies(
            id = "clownfish", name = "Clownfish", scientificName = "Amphiprion ocellaris",
            description = "Vivid tangerine-and-white anemone dweller darting in short, lively loops.",
            category = FishCategory.TROPICAL_REEF, compatibleHabitats = REEF,
            bodyPlan = BodyPlan.REEF_OVAL, pattern = PatternType.VERTICAL_BARS,
            behavior = BehaviorArchetype.REEF_TERRITORIAL,
            primaryColor = RgbColor(0.94f, 0.44f, 0.12f), secondaryColor = RgbColor(0.96f, 0.96f, 0.96f),
            accentColor = RgbColor(0.10f, 0.10f, 0.12f), finColor = RgbColor(0.92f, 0.42f, 0.14f),
            baseSize = 0.062f, aspectRatio = 1.9f, baseSpeed = 0.15f, turnInertia = 0.58f,
            schoolingWeight = 0.42f, depthMin = 0.42f, depthMax = 0.86f, curiosity = 0.64f, skittishness = 0.54f
        ),
        FishSpecies(
            id = "blue_tang", name = "Blue Tang", scientificName = "Acanthurus coeruleus",
            description = "Royal sapphire reef grazer curving smoothly across sunlit coral heads.",
            category = FishCategory.TROPICAL_REEF, compatibleHabitats = REEF,
            bodyPlan = BodyPlan.DEEP_DISC, pattern = PatternType.SOLID_GRADIENT,
            behavior = BehaviorArchetype.PELAGIC_CRUISER,
            primaryColor = RgbColor(0.14f, 0.40f, 0.86f), secondaryColor = RgbColor(0.10f, 0.24f, 0.62f),
            accentColor = RgbColor(0.92f, 0.84f, 0.22f), finColor = RgbColor(0.22f, 0.52f, 0.92f),
            baseSize = 0.088f, aspectRatio = 1.55f, baseSpeed = 0.20f, turnInertia = 0.70f,
            schoolingWeight = 0.62f, depthMin = 0.18f, depthMax = 0.74f, curiosity = 0.48f, skittishness = 0.56f
        ),
        FishSpecies(
            id = "yellow_tang", name = "Yellow Tang", scientificName = "Zebrasoma flavescens",
            description = "Radiant sunlit lemon surgeonfish drifting in luminous reef trios.",
            category = FishCategory.TROPICAL_REEF, compatibleHabitats = REEF,
            bodyPlan = BodyPlan.DEEP_DISC, pattern = PatternType.SOLID_GRADIENT,
            behavior = BehaviorArchetype.PELAGIC_CRUISER,
            primaryColor = RgbColor(0.96f, 0.84f, 0.18f), secondaryColor = RgbColor(0.92f, 0.74f, 0.12f),
            accentColor = RgbColor(0.98f, 0.96f, 0.82f), finColor = RgbColor(0.96f, 0.86f, 0.26f),
            baseSize = 0.084f, aspectRatio = 1.5f, baseSpeed = 0.19f, turnInertia = 0.70f,
            schoolingWeight = 0.64f, depthMin = 0.18f, depthMax = 0.72f, curiosity = 0.50f, skittishness = 0.55f
        ),
        FishSpecies(
            id = "regal_tang", name = "Regal Tang", scientificName = "Paracanthurus hepatus",
            description = "Cobalt-blue palette surgeonfish marked by jet-black sweep and gold tail.",
            category = FishCategory.TROPICAL_REEF, compatibleHabitats = REEF,
            bodyPlan = BodyPlan.REEF_OVAL, pattern = PatternType.HORIZONTAL_STRIPE,
            behavior = BehaviorArchetype.PELAGIC_CRUISER,
            primaryColor = RgbColor(0.12f, 0.34f, 0.86f), secondaryColor = RgbColor(0.08f, 0.10f, 0.14f),
            accentColor = RgbColor(0.96f, 0.82f, 0.16f), finColor = RgbColor(0.96f, 0.82f, 0.16f),
            baseSize = 0.090f, aspectRatio = 1.75f, baseSpeed = 0.20f, turnInertia = 0.70f,
            schoolingWeight = 0.58f, depthMin = 0.20f, depthMax = 0.72f, curiosity = 0.45f, skittishness = 0.62f
        ),
        FishSpecies(
            id = "butterflyfish", name = "Butterflyfish", scientificName = "Chaetodon auriga",
            description = "Delicate pearl-and-gold coral browser pairing gracefully over reef ledges.",
            category = FishCategory.TROPICAL_REEF, compatibleHabitats = REEF,
            bodyPlan = BodyPlan.DEEP_DISC, pattern = PatternType.VERTICAL_BARS,
            behavior = BehaviorArchetype.REEF_TERRITORIAL,
            primaryColor = RgbColor(0.94f, 0.92f, 0.84f), secondaryColor = RgbColor(0.94f, 0.74f, 0.18f),
            accentColor = RgbColor(0.12f, 0.12f, 0.14f), finColor = RgbColor(0.94f, 0.78f, 0.24f),
            baseSize = 0.078f, aspectRatio = 1.45f, baseSpeed = 0.15f, turnInertia = 0.68f,
            schoolingWeight = 0.50f, depthMin = 0.24f, depthMax = 0.78f, curiosity = 0.52f, skittishness = 0.58f
        ),
        FishSpecies(
            id = "marine_angelfish", name = "Marine Angelfish", scientificName = "Pomacanthus imperator",
            description = "Majestic royal-blue and gold striped reef sovereign.",
            category = FishCategory.TROPICAL_REEF, compatibleHabitats = REEF,
            bodyPlan = BodyPlan.DEEP_DISC, pattern = PatternType.HORIZONTAL_STRIPE,
            behavior = BehaviorArchetype.KOI_GRACEFUL,
            primaryColor = RgbColor(0.16f, 0.34f, 0.72f), secondaryColor = RgbColor(0.92f, 0.76f, 0.20f),
            accentColor = RgbColor(0.14f, 0.72f, 0.86f), finColor = RgbColor(0.92f, 0.74f, 0.22f),
            baseSize = 0.105f, aspectRatio = 1.55f, baseSpeed = 0.14f, turnInertia = 0.82f,
            schoolingWeight = 0.25f, depthMin = 0.24f, depthMax = 0.78f, curiosity = 0.64f, skittishness = 0.36f
        ),
        FishSpecies(
            id = "moorish_idol", name = "Moorish Idol", scientificName = "Zanclus cornutus",
            description = "Sculptural banded reef icon trailing a ribbon-like dorsal filament.",
            category = FishCategory.TROPICAL_REEF, compatibleHabitats = REEF,
            bodyPlan = BodyPlan.DEEP_DISC, pattern = PatternType.VERTICAL_BARS,
            behavior = BehaviorArchetype.KOI_GRACEFUL,
            primaryColor = RgbColor(0.94f, 0.94f, 0.90f), secondaryColor = RgbColor(0.10f, 0.10f, 0.12f),
            accentColor = RgbColor(0.92f, 0.74f, 0.18f), finColor = RgbColor(0.94f, 0.92f, 0.84f),
            baseSize = 0.088f, aspectRatio = 1.32f, baseSpeed = 0.15f, turnInertia = 0.74f,
            schoolingWeight = 0.54f, depthMin = 0.22f, depthMax = 0.74f, curiosity = 0.56f, skittishness = 0.48f
        ),
        FishSpecies(
            id = "damselfish", name = "Damselfish", scientificName = "Chrysiptera cyanea",
            description = "Electric cerulean reef sprite shimmering in dense coral canopies.",
            category = FishCategory.TROPICAL_REEF, compatibleHabitats = REEF,
            bodyPlan = BodyPlan.REEF_OVAL, pattern = PatternType.SOLID_GRADIENT,
            behavior = BehaviorArchetype.TETRA_SCHOOL,
            primaryColor = RgbColor(0.10f, 0.56f, 0.94f), secondaryColor = RgbColor(0.08f, 0.34f, 0.78f),
            accentColor = RgbColor(0.94f, 0.84f, 0.24f), finColor = RgbColor(0.24f, 0.68f, 0.96f),
            baseSize = 0.048f, aspectRatio = 1.95f, baseSpeed = 0.22f, turnInertia = 0.50f,
            schoolingWeight = 0.84f, depthMin = 0.20f, depthMax = 0.78f, curiosity = 0.28f, skittishness = 0.82f,
            recommendedMax = 24
        ),
        FishSpecies(
            id = "sergeant_major", name = "Sergeant Major", scientificName = "Abudefduf saxatilis",
            description = "Sun-gilded reef schooler marked with crisp charcoal vertical chevrons.",
            category = FishCategory.TROPICAL_REEF, compatibleHabitats = REEF,
            bodyPlan = BodyPlan.REEF_OVAL, pattern = PatternType.VERTICAL_BARS,
            behavior = BehaviorArchetype.TETRA_SCHOOL,
            primaryColor = RgbColor(0.86f, 0.84f, 0.54f), secondaryColor = RgbColor(0.16f, 0.18f, 0.22f),
            accentColor = RgbColor(0.92f, 0.88f, 0.42f), finColor = RgbColor(0.74f, 0.76f, 0.64f),
            baseSize = 0.062f, aspectRatio = 1.9f, baseSpeed = 0.20f, turnInertia = 0.58f,
            schoolingWeight = 0.82f, depthMin = 0.14f, depthMax = 0.64f, curiosity = 0.44f, skittishness = 0.66f
        ),
        FishSpecies(
            id = "wrasse", name = "Wrasse", scientificName = "Thalassoma lunare",
            description = "Emerald-and-magenta reef weaver gliding with brisk pectoral strokes.",
            category = FishCategory.TROPICAL_REEF, compatibleHabitats = REEF,
            bodyPlan = BodyPlan.SLENDER_SCHOOLING, pattern = PatternType.MANDARIN_SWIRL,
            behavior = BehaviorArchetype.REEF_TERRITORIAL,
            primaryColor = RgbColor(0.18f, 0.68f, 0.56f), secondaryColor = RgbColor(0.76f, 0.28f, 0.52f),
            accentColor = RgbColor(0.94f, 0.84f, 0.26f), finColor = RgbColor(0.26f, 0.74f, 0.70f),
            baseSize = 0.074f, aspectRatio = 2.6f, baseSpeed = 0.22f, turnInertia = 0.56f,
            schoolingWeight = 0.42f, depthMin = 0.26f, depthMax = 0.82f, curiosity = 0.48f, skittishness = 0.62f
        ),
        FishSpecies(
            id = "goby", name = "Goby", scientificName = "Valenciennea strigata",
            description = "Pearl-bodied reef floor sentinel hovering calmly over sandy ripples.",
            category = FishCategory.TROPICAL_REEF, compatibleHabitats = REEF,
            bodyPlan = BodyPlan.SLENDER_SCHOOLING, pattern = PatternType.SOLID_GRADIENT,
            behavior = BehaviorArchetype.BENTHIC_FORAGER,
            primaryColor = RgbColor(0.86f, 0.86f, 0.80f), secondaryColor = RgbColor(0.90f, 0.76f, 0.30f),
            accentColor = RgbColor(0.24f, 0.72f, 0.88f), finColor = RgbColor(0.84f, 0.86f, 0.84f),
            baseSize = 0.056f, aspectRatio = 2.8f, baseSpeed = 0.14f, turnInertia = 0.62f,
            schoolingWeight = 0.30f, depthMin = 0.70f, depthMax = 0.94f, curiosity = 0.38f, skittishness = 0.72f
        ),
        FishSpecies(
            id = "blenny", name = "Blenny", scientificName = "Ecsenius bicolor",
            description = "Two-tone charcoal-and-amber reef crevice browser.",
            category = FishCategory.TROPICAL_REEF, compatibleHabitats = REEF,
            bodyPlan = BodyPlan.SLENDER_SCHOOLING, pattern = PatternType.SOLID_GRADIENT,
            behavior = BehaviorArchetype.BENTHIC_FORAGER,
            primaryColor = RgbColor(0.26f, 0.28f, 0.36f), secondaryColor = RgbColor(0.90f, 0.56f, 0.20f),
            accentColor = RgbColor(0.86f, 0.68f, 0.36f), finColor = RgbColor(0.88f, 0.58f, 0.26f),
            baseSize = 0.054f, aspectRatio = 2.9f, baseSpeed = 0.15f, turnInertia = 0.58f,
            schoolingWeight = 0.24f, depthMin = 0.68f, depthMax = 0.92f, curiosity = 0.56f, skittishness = 0.60f
        ),
        FishSpecies(
            id = "triggerfish", name = "Triggerfish", scientificName = "Balistoides conspicillum",
            description = "Distinctive spotted reef cruiser propelled by undulating dorsal and anal fins.",
            category = FishCategory.TROPICAL_REEF, compatibleHabitats = REEF,
            bodyPlan = BodyPlan.REEF_OVAL, pattern = PatternType.SPOTTED,
            behavior = BehaviorArchetype.REEF_TERRITORIAL,
            primaryColor = RgbColor(0.14f, 0.16f, 0.20f), secondaryColor = RgbColor(0.92f, 0.92f, 0.86f),
            accentColor = RgbColor(0.92f, 0.74f, 0.20f), finColor = RgbColor(0.86f, 0.82f, 0.56f),
            baseSize = 0.096f, aspectRatio = 1.75f, baseSpeed = 0.15f, turnInertia = 0.76f,
            schoolingWeight = 0.16f, depthMin = 0.28f, depthMax = 0.82f, curiosity = 0.62f, skittishness = 0.28f
        ),
        FishSpecies(
            id = "lionfish", name = "Lionfish", scientificName = "Pterois volitans",
            description = "Sculptural banded predator floating deliberately with fanned pectoral rays.",
            category = FishCategory.TROPICAL_REEF, compatibleHabitats = REEF,
            bodyPlan = BodyPlan.FLOWING_FIN, pattern = PatternType.VERTICAL_BARS,
            behavior = BehaviorArchetype.BETTA_SOLITARY,
            primaryColor = RgbColor(0.76f, 0.24f, 0.18f), secondaryColor = RgbColor(0.94f, 0.90f, 0.82f),
            accentColor = RgbColor(0.32f, 0.12f, 0.10f), finColor = RgbColor(0.88f, 0.76f, 0.68f),
            baseSize = 0.105f, aspectRatio = 1.85f, baseSpeed = 0.08f, turnInertia = 0.88f,
            schoolingWeight = 0.08f, depthMin = 0.34f, depthMax = 0.84f, curiosity = 0.45f, skittishness = 0.16f,
            recommendedMax = 6
        ),
        FishSpecies(
            id = "pufferfish", name = "Pufferfish", scientificName = "Arothron nigropunctatus",
            description = "Unruffled hovering reef navigator paddling with tiny fluttering fins.",
            category = FishCategory.TROPICAL_REEF, compatibleHabitats = REEF,
            bodyPlan = BodyPlan.REEF_OVAL, pattern = PatternType.SPOTTED,
            behavior = BehaviorArchetype.KOI_GRACEFUL,
            primaryColor = RgbColor(0.76f, 0.72f, 0.54f), secondaryColor = RgbColor(0.24f, 0.22f, 0.18f),
            accentColor = RgbColor(0.90f, 0.84f, 0.46f), finColor = RgbColor(0.84f, 0.82f, 0.66f),
            baseSize = 0.080f, aspectRatio = 1.65f, baseSpeed = 0.11f, turnInertia = 0.78f,
            schoolingWeight = 0.15f, depthMin = 0.30f, depthMax = 0.80f, curiosity = 0.78f, skittishness = 0.26f
        ),
        FishSpecies(
            id = "boxfish", name = "Boxfish", scientificName = "Ostracion cubicus",
            description = "Geometric saffron-spotted reef drifter moving like a calm submarine.",
            category = FishCategory.TROPICAL_REEF, compatibleHabitats = REEF,
            bodyPlan = BodyPlan.REEF_OVAL, pattern = PatternType.SPOTTED,
            behavior = BehaviorArchetype.REEF_TERRITORIAL,
            primaryColor = RgbColor(0.92f, 0.78f, 0.22f), secondaryColor = RgbColor(0.18f, 0.16f, 0.12f),
            accentColor = RgbColor(0.94f, 0.88f, 0.46f), finColor = RgbColor(0.90f, 0.82f, 0.42f),
            baseSize = 0.068f, aspectRatio = 1.60f, baseSpeed = 0.10f, turnInertia = 0.76f,
            schoolingWeight = 0.18f, depthMin = 0.34f, depthMax = 0.82f, curiosity = 0.70f, skittishness = 0.32f
        ),
        FishSpecies(
            id = "cardinalfish", name = "Cardinalfish", scientificName = "Pterapogon kauderni",
            description = "Silver-and-ebony nocturnal reef fish pausing gracefully in shadowy overhangs.",
            category = FishCategory.TROPICAL_REEF, compatibleHabitats = REEF,
            bodyPlan = BodyPlan.DEEP_DISC, pattern = PatternType.VERTICAL_BARS,
            behavior = BehaviorArchetype.TETRA_SCHOOL,
            primaryColor = RgbColor(0.84f, 0.86f, 0.90f), secondaryColor = RgbColor(0.10f, 0.12f, 0.16f),
            accentColor = RgbColor(0.94f, 0.96f, 1.0f), finColor = RgbColor(0.76f, 0.80f, 0.86f),
            baseSize = 0.056f, aspectRatio = 1.65f, baseSpeed = 0.13f, turnInertia = 0.66f,
            schoolingWeight = 0.78f, depthMin = 0.34f, depthMax = 0.84f, curiosity = 0.32f, skittishness = 0.68f
        ),
        FishSpecies(
            id = "parrotfish", name = "Parrotfish", scientificName = "Scarus taeniopterus",
            description = "Turquoise-and-rose coral grazer cruising steadily along sunlit shelves.",
            category = FishCategory.TROPICAL_REEF, compatibleHabitats = REEF,
            bodyPlan = BodyPlan.CARP_FUSIFORM, pattern = PatternType.SOLID_GRADIENT,
            behavior = BehaviorArchetype.PELAGIC_CRUISER,
            primaryColor = RgbColor(0.18f, 0.72f, 0.68f), secondaryColor = RgbColor(0.86f, 0.48f, 0.56f),
            accentColor = RgbColor(0.92f, 0.82f, 0.34f), finColor = RgbColor(0.28f, 0.76f, 0.74f),
            baseSize = 0.108f, aspectRatio = 2.35f, baseSpeed = 0.17f, turnInertia = 0.78f,
            schoolingWeight = 0.48f, depthMin = 0.24f, depthMax = 0.78f, curiosity = 0.44f, skittishness = 0.46f
        ),
        FishSpecies(
            id = "surgeonfish", name = "Surgeonfish", scientificName = "Acanthurus lineatus",
            description = "Striped blue-and-gold shoaler sweeping briskly over ocean drop-offs.",
            category = FishCategory.TROPICAL_REEF, compatibleHabitats = REEF + HabitatType.OPEN_OCEAN,
            bodyPlan = BodyPlan.REEF_OVAL, pattern = PatternType.HORIZONTAL_STRIPE,
            behavior = BehaviorArchetype.PELAGIC_CRUISER,
            primaryColor = RgbColor(0.90f, 0.76f, 0.24f), secondaryColor = RgbColor(0.18f, 0.54f, 0.86f),
            accentColor = RgbColor(0.14f, 0.22f, 0.34f), finColor = RgbColor(0.28f, 0.62f, 0.88f),
            baseSize = 0.088f, aspectRatio = 1.95f, baseSpeed = 0.21f, turnInertia = 0.68f,
            schoolingWeight = 0.74f, depthMin = 0.18f, depthMax = 0.68f, curiosity = 0.38f, skittishness = 0.60f
        ),
        FishSpecies(
            id = "anthias", name = "Anthias", scientificName = "Pseudanthias squamipinnis",
            description = "Luminous apricot-and-rose reef dancers forming radiant sunlit clouds.",
            category = FishCategory.TROPICAL_REEF, compatibleHabitats = REEF,
            bodyPlan = BodyPlan.SLENDER_SCHOOLING, pattern = PatternType.SOLID_GRADIENT,
            behavior = BehaviorArchetype.TETRA_SCHOOL,
            primaryColor = RgbColor(0.96f, 0.48f, 0.28f), secondaryColor = RgbColor(0.92f, 0.28f, 0.48f),
            accentColor = RgbColor(0.98f, 0.78f, 0.38f), finColor = RgbColor(0.96f, 0.56f, 0.44f),
            baseSize = 0.048f, aspectRatio = 2.3f, baseSpeed = 0.23f, turnInertia = 0.48f,
            schoolingWeight = 0.90f, depthMin = 0.16f, depthMax = 0.68f, curiosity = 0.24f, skittishness = 0.82f,
            recommendedMax = 28
        ),
        FishSpecies(
            id = "mandarin_fish", name = "Mandarin Fish", scientificName = "Synchiropus splendidus",
            description = "Ornate blue, teal, and ochre dragonet hovering gently near coral branches.",
            category = FishCategory.TROPICAL_REEF, compatibleHabitats = REEF,
            bodyPlan = BodyPlan.FLOWING_FIN, pattern = PatternType.MANDARIN_SWIRL,
            behavior = BehaviorArchetype.REEF_TERRITORIAL,
            primaryColor = RgbColor(0.14f, 0.52f, 0.88f), secondaryColor = RgbColor(0.92f, 0.46f, 0.16f),
            accentColor = RgbColor(0.20f, 0.84f, 0.68f), finColor = RgbColor(0.24f, 0.72f, 0.86f),
            baseSize = 0.064f, aspectRatio = 2.2f, baseSpeed = 0.10f, turnInertia = 0.74f,
            schoolingWeight = 0.22f, depthMin = 0.56f, depthMax = 0.90f, curiosity = 0.48f, skittishness = 0.54f,
            glowIntensity = 0.16f
        ),

        // ═══════════════════════ LARGE OCEAN FISH (10) ═══════════════════════
        FishSpecies(
            id = "tuna", name = "Tuna", scientificName = "Thunnus albacares",
            description = "Hydrodynamic silver-blue pelagic cruiser cutting clean sweeping arcs.",
            category = FishCategory.LARGE_OCEAN, compatibleHabitats = OCEAN,
            bodyPlan = BodyPlan.SHARK_PELAGIC, pattern = PatternType.SOLID_GRADIENT,
            behavior = BehaviorArchetype.PELAGIC_CRUISER,
            primaryColor = RgbColor(0.22f, 0.36f, 0.54f), secondaryColor = RgbColor(0.78f, 0.84f, 0.90f),
            accentColor = RgbColor(0.92f, 0.80f, 0.28f), finColor = RgbColor(0.88f, 0.78f, 0.30f),
            baseSize = 0.145f, aspectRatio = 2.9f, baseSpeed = 0.26f, turnInertia = 0.88f,
            schoolingWeight = 0.76f, depthMin = 0.18f, depthMax = 0.72f, curiosity = 0.25f, skittishness = 0.42f
        ),
        FishSpecies(
            id = "mackerel", name = "Mackerel", scientificName = "Scomber scombrus",
            description = "Iridescent steel-blue shoaler moving in hypnotic synchronized waves.",
            category = FishCategory.LARGE_OCEAN, compatibleHabitats = OCEAN,
            bodyPlan = BodyPlan.SLENDER_SCHOOLING, pattern = PatternType.VERTICAL_BARS,
            behavior = BehaviorArchetype.TETRA_SCHOOL,
            primaryColor = RgbColor(0.32f, 0.50f, 0.64f), secondaryColor = RgbColor(0.80f, 0.86f, 0.90f),
            accentColor = RgbColor(0.16f, 0.30f, 0.42f), finColor = RgbColor(0.68f, 0.76f, 0.82f),
            baseSize = 0.068f, aspectRatio = 3.1f, baseSpeed = 0.27f, turnInertia = 0.56f,
            schoolingWeight = 0.95f, depthMin = 0.14f, depthMax = 0.68f, curiosity = 0.15f, skittishness = 0.78f,
            recommendedMax = 28
        ),
        FishSpecies(
            id = "barracuda", name = "Barracuda", scientificName = "Sphyraena barracuda",
            description = "Sleek chrome-silver ocean sentinel gliding with poised precision.",
            category = FishCategory.LARGE_OCEAN, compatibleHabitats = OCEAN,
            bodyPlan = BodyPlan.SHARK_PELAGIC, pattern = PatternType.VERTICAL_BARS,
            behavior = BehaviorArchetype.PELAGIC_CRUISER,
            primaryColor = RgbColor(0.62f, 0.70f, 0.76f), secondaryColor = RgbColor(0.84f, 0.88f, 0.92f),
            accentColor = RgbColor(0.30f, 0.38f, 0.44f), finColor = RgbColor(0.54f, 0.62f, 0.68f),
            baseSize = 0.155f, aspectRatio = 3.6f, baseSpeed = 0.22f, turnInertia = 0.90f,
            schoolingWeight = 0.44f, depthMin = 0.18f, depthMax = 0.66f, curiosity = 0.38f, skittishness = 0.26f
        ),
        FishSpecies(
            id = "swordfish", name = "Swordfish", scientificName = "Xiphias gladius",
            description = "Oceanic billfish slicing open blue waters in vast stately trajectories.",
            category = FishCategory.LARGE_OCEAN, compatibleHabitats = OCEAN,
            bodyPlan = BodyPlan.SHARK_PELAGIC, pattern = PatternType.SOLID_GRADIENT,
            behavior = BehaviorArchetype.SHARK_ARC,
            primaryColor = RgbColor(0.24f, 0.36f, 0.52f), secondaryColor = RgbColor(0.76f, 0.82f, 0.88f),
            accentColor = RgbColor(0.14f, 0.22f, 0.34f), finColor = RgbColor(0.28f, 0.40f, 0.56f),
            baseSize = 0.185f, aspectRatio = 3.8f, baseSpeed = 0.25f, turnInertia = 0.93f,
            schoolingWeight = 0.14f, depthMin = 0.20f, depthMax = 0.70f, curiosity = 0.24f, skittishness = 0.20f,
            recommendedMax = 5
        ),
        FishSpecies(
            id = "manta_ray", name = "Manta Ray", scientificName = "Mobula birostris",
            description = "Winged pelagic giant soaring effortlessly through sunbeams in slow arcs.",
            category = FishCategory.LARGE_OCEAN, compatibleHabitats = OCEAN,
            bodyPlan = BodyPlan.FLAT_RAY, pattern = PatternType.SOLID_GRADIENT,
            behavior = BehaviorArchetype.RAY_GLIDER,
            primaryColor = RgbColor(0.16f, 0.22f, 0.30f), secondaryColor = RgbColor(0.84f, 0.88f, 0.92f),
            accentColor = RgbColor(0.32f, 0.42f, 0.54f), finColor = RgbColor(0.20f, 0.28f, 0.38f),
            baseSize = 0.205f, aspectRatio = 1.15f, baseSpeed = 0.14f, turnInertia = 0.94f,
            schoolingWeight = 0.28f, depthMin = 0.18f, depthMax = 0.72f, curiosity = 0.56f, skittishness = 0.12f,
            recommendedMax = 5
        ),
        FishSpecies(
            id = "eagle_ray", name = "Eagle Ray", scientificName = "Aetobatus narinari",
            description = "White-ringed spotted ray banking smoothly across turquoise shallows.",
            category = FishCategory.LARGE_OCEAN, compatibleHabitats = OCEAN,
            bodyPlan = BodyPlan.FLAT_RAY, pattern = PatternType.SPOTTED,
            behavior = BehaviorArchetype.RAY_GLIDER,
            primaryColor = RgbColor(0.20f, 0.28f, 0.38f), secondaryColor = RgbColor(0.88f, 0.90f, 0.92f),
            accentColor = RgbColor(0.84f, 0.88f, 0.92f), finColor = RgbColor(0.26f, 0.34f, 0.44f),
            baseSize = 0.165f, aspectRatio = 1.20f, baseSpeed = 0.15f, turnInertia = 0.92f,
            schoolingWeight = 0.36f, depthMin = 0.28f, depthMax = 0.82f, curiosity = 0.48f, skittishness = 0.22f,
            recommendedMax = 6
        ),
        FishSpecies(
            id = "stingray", name = "Stingray", scientificName = "Taeniura lymma",
            description = "Graceful sandy-toned coastal ray rippling softly above the seabed.",
            category = FishCategory.LARGE_OCEAN, compatibleHabitats = OCEAN,
            bodyPlan = BodyPlan.FLAT_RAY, pattern = PatternType.SPOTTED,
            behavior = BehaviorArchetype.RAY_GLIDER,
            primaryColor = RgbColor(0.56f, 0.52f, 0.42f), secondaryColor = RgbColor(0.24f, 0.66f, 0.82f),
            accentColor = RgbColor(0.76f, 0.72f, 0.60f), finColor = RgbColor(0.62f, 0.58f, 0.48f),
            baseSize = 0.145f, aspectRatio = 1.25f, baseSpeed = 0.12f, turnInertia = 0.90f,
            schoolingWeight = 0.20f, depthMin = 0.58f, depthMax = 0.92f, curiosity = 0.44f, skittishness = 0.28f,
            recommendedMax = 6
        ),
        FishSpecies(
            id = "hammerhead_shark", name = "Hammerhead Shark", scientificName = "Sphyrna lewini",
            description = "Distinctive bronze-grey pelagic navigator sweeping vast ocean curves.",
            category = FishCategory.LARGE_OCEAN, compatibleHabitats = OCEAN,
            bodyPlan = BodyPlan.SHARK_PELAGIC, pattern = PatternType.SOLID_GRADIENT,
            behavior = BehaviorArchetype.SHARK_ARC,
            primaryColor = RgbColor(0.38f, 0.42f, 0.46f), secondaryColor = RgbColor(0.78f, 0.80f, 0.82f),
            accentColor = RgbColor(0.24f, 0.28f, 0.32f), finColor = RgbColor(0.34f, 0.38f, 0.44f),
            baseSize = 0.210f, aspectRatio = 3.3f, baseSpeed = 0.18f, turnInertia = 0.94f,
            schoolingWeight = 0.25f, depthMin = 0.24f, depthMax = 0.76f, curiosity = 0.34f, skittishness = 0.10f,
            recommendedMax = 4
        ),
        FishSpecies(
            id = "reef_shark", name = "Reef Shark", scientificName = "Carcharhinus melanopterus",
            description = "Sleek blacktip reef patroller moving with calm, unhurried authority.",
            category = FishCategory.LARGE_OCEAN, compatibleHabitats = OCEAN,
            bodyPlan = BodyPlan.SHARK_PELAGIC, pattern = PatternType.SOLID_GRADIENT,
            behavior = BehaviorArchetype.SHARK_ARC,
            primaryColor = RgbColor(0.42f, 0.46f, 0.50f), secondaryColor = RgbColor(0.84f, 0.86f, 0.88f),
            accentColor = RgbColor(0.10f, 0.12f, 0.14f), finColor = RgbColor(0.36f, 0.40f, 0.46f),
            baseSize = 0.195f, aspectRatio = 3.2f, baseSpeed = 0.19f, turnInertia = 0.93f,
            schoolingWeight = 0.18f, depthMin = 0.22f, depthMax = 0.74f, curiosity = 0.42f, skittishness = 0.10f,
            recommendedMax = 4
        ),
        FishSpecies(
            id = "whale_shark", name = "Whale Shark", scientificName = "Rhincodon typus",
            description = "Gentle star-speckled ocean colossus drifting seraphically through open blue.",
            category = FishCategory.LARGE_OCEAN, compatibleHabitats = OCEAN,
            bodyPlan = BodyPlan.SHARK_PELAGIC, pattern = PatternType.SPOTTED,
            behavior = BehaviorArchetype.SHARK_ARC,
            primaryColor = RgbColor(0.24f, 0.34f, 0.44f), secondaryColor = RgbColor(0.84f, 0.88f, 0.92f),
            accentColor = RgbColor(0.76f, 0.82f, 0.86f), finColor = RgbColor(0.26f, 0.36f, 0.46f),
            baseSize = 0.265f, aspectRatio = 3.0f, baseSpeed = 0.11f, turnInertia = 0.97f,
            schoolingWeight = 0.05f, depthMin = 0.20f, depthMax = 0.68f, curiosity = 0.32f, skittishness = 0.04f,
            recommendedMax = 2
        ),

        // ═══════════════════════ DEEP SEA (8) ═══════════════════════
        FishSpecies(
            id = "anglerfish", name = "Anglerfish", scientificName = "Melanocetus johnsonii",
            description = "Velvet-obsidian bathypelagic hunter carrying a soft glowing cyan lure.",
            category = FishCategory.DEEP_SEA, compatibleHabitats = DEEP,
            bodyPlan = BodyPlan.REEF_OVAL, pattern = PatternType.BIOLUMINESCENT,
            behavior = BehaviorArchetype.DEEP_DRIFTER,
            primaryColor = RgbColor(0.12f, 0.10f, 0.14f), secondaryColor = RgbColor(0.24f, 0.18f, 0.24f),
            accentColor = RgbColor(0.42f, 0.94f, 1.0f), finColor = RgbColor(0.18f, 0.16f, 0.22f),
            baseSize = 0.100f, aspectRatio = 1.75f, baseSpeed = 0.06f, turnInertia = 0.88f,
            schoolingWeight = 0.05f, depthMin = 0.45f, depthMax = 0.88f, curiosity = 0.35f, skittishness = 0.12f,
            glowIntensity = 0.95f, recommendedMax = 5
        ),
        FishSpecies(
            id = "lanternfish", name = "Lanternfish", scientificName = "Myctophum punctatum",
            description = "Silver-indigo twilight vertical migrator lined with pale blue photophores.",
            category = FishCategory.DEEP_SEA, compatibleHabitats = DEEP,
            bodyPlan = BodyPlan.SLENDER_SCHOOLING, pattern = PatternType.BIOLUMINESCENT,
            behavior = BehaviorArchetype.TETRA_SCHOOL,
            primaryColor = RgbColor(0.18f, 0.28f, 0.46f), secondaryColor = RgbColor(0.42f, 0.86f, 1.0f),
            accentColor = RgbColor(0.56f, 0.96f, 1.0f), finColor = RgbColor(0.26f, 0.46f, 0.66f),
            baseSize = 0.060f, aspectRatio = 2.6f, baseSpeed = 0.15f, turnInertia = 0.64f,
            schoolingWeight = 0.84f, depthMin = 0.24f, depthMax = 0.82f, curiosity = 0.26f, skittishness = 0.62f,
            glowIntensity = 0.85f, recommendedMax = 20
        ),
        FishSpecies(
            id = "viperfish", name = "Viperfish", scientificName = "Chauliodus sloani",
            description = "Iridescent abyssal ribbon predator glimmering faintly in dark currents.",
            category = FishCategory.DEEP_SEA, compatibleHabitats = DEEP,
            bodyPlan = BodyPlan.ELONGATED_EEL, pattern = PatternType.BIOLUMINESCENT,
            behavior = BehaviorArchetype.DEEP_DRIFTER,
            primaryColor = RgbColor(0.16f, 0.22f, 0.30f), secondaryColor = RgbColor(0.36f, 0.78f, 0.92f),
            accentColor = RgbColor(0.48f, 0.92f, 0.96f), finColor = RgbColor(0.22f, 0.30f, 0.40f),
            baseSize = 0.132f, aspectRatio = 3.8f, baseSpeed = 0.08f, turnInertia = 0.88f,
            schoolingWeight = 0.10f, depthMin = 0.42f, depthMax = 0.88f, curiosity = 0.24f, skittishness = 0.22f,
            glowIntensity = 0.78f, recommendedMax = 6
        ),
        FishSpecies(
            id = "gulper_eel", name = "Gulper Eel", scientificName = "Eurypharynx pelecanoides",
            description = "Sinuous deep-trench ribbon gliding with a glowing amber tail tip.",
            category = FishCategory.DEEP_SEA, compatibleHabitats = DEEP,
            bodyPlan = BodyPlan.ELONGATED_EEL, pattern = PatternType.BIOLUMINESCENT,
            behavior = BehaviorArchetype.DEEP_DRIFTER,
            primaryColor = RgbColor(0.14f, 0.12f, 0.18f), secondaryColor = RgbColor(0.28f, 0.20f, 0.30f),
            accentColor = RgbColor(0.96f, 0.56f, 0.42f), finColor = RgbColor(0.20f, 0.16f, 0.24f),
            baseSize = 0.175f, aspectRatio = 4.2f, baseSpeed = 0.08f, turnInertia = 0.90f,
            schoolingWeight = 0.05f, depthMin = 0.46f, depthMax = 0.90f, curiosity = 0.20f, skittishness = 0.18f,
            glowIntensity = 0.72f, recommendedMax = 4
        ),
        FishSpecies(
            id = "deep_sea_squid", name = "Deep Sea Squid", scientificName = "Vampyroteuthis infernalis",
            description = "Translucent crimson-violet cephalopod jetting gently through abyssal snow.",
            category = FishCategory.DEEP_SEA, compatibleHabitats = DEEP,
            bodyPlan = BodyPlan.CEPHALOPOD, pattern = PatternType.BIOLUMINESCENT,
            behavior = BehaviorArchetype.DEEP_DRIFTER,
            primaryColor = RgbColor(0.46f, 0.18f, 0.34f), secondaryColor = RgbColor(0.68f, 0.34f, 0.56f),
            accentColor = RgbColor(0.44f, 0.90f, 1.0f), finColor = RgbColor(0.58f, 0.28f, 0.48f),
            baseSize = 0.130f, aspectRatio = 2.5f, baseSpeed = 0.12f, turnInertia = 0.80f,
            schoolingWeight = 0.20f, depthMin = 0.28f, depthMax = 0.84f, curiosity = 0.46f, skittishness = 0.42f,
            glowIntensity = 0.86f, recommendedMax = 6
        ),
        FishSpecies(
            id = "giant_isopod", name = "Giant Isopod", scientificName = "Bathynomus giganteus",
            description = "Ancient pale-lavender armored crustacean traversing deep seafloor basins.",
            category = FishCategory.DEEP_SEA, compatibleHabitats = DEEP,
            bodyPlan = BodyPlan.BENTHIC_CRUSTACEAN, pattern = PatternType.SOLID_GRADIENT,
            behavior = BehaviorArchetype.BENTHIC_FORAGER,
            primaryColor = RgbColor(0.56f, 0.50f, 0.58f), secondaryColor = RgbColor(0.38f, 0.34f, 0.42f),
            accentColor = RgbColor(0.72f, 0.68f, 0.76f), finColor = RgbColor(0.46f, 0.42f, 0.50f),
            baseSize = 0.085f, aspectRatio = 2.0f, baseSpeed = 0.06f, turnInertia = 0.88f,
            schoolingWeight = 0.10f, depthMin = 0.78f, depthMax = 0.95f, curiosity = 0.18f, skittishness = 0.30f,
            recommendedMax = 6
        ),
        FishSpecies(
            id = "oarfish", name = "Oarfish", scientificName = "Regalecus glesne",
            description = "Mythic silver-ribbon deep-ocean monarch undulating with crimson dorsal crest.",
            category = FishCategory.DEEP_SEA, compatibleHabitats = DEEP,
            bodyPlan = BodyPlan.ELONGATED_EEL, pattern = PatternType.BIOLUMINESCENT,
            behavior = BehaviorArchetype.DEEP_DRIFTER,
            primaryColor = RgbColor(0.76f, 0.82f, 0.88f), secondaryColor = RgbColor(0.88f, 0.22f, 0.24f),
            accentColor = RgbColor(0.58f, 0.88f, 1.0f), finColor = RgbColor(0.90f, 0.24f, 0.26f),
            baseSize = 0.245f, aspectRatio = 4.6f, baseSpeed = 0.09f, turnInertia = 0.95f,
            schoolingWeight = 0.04f, depthMin = 0.26f, depthMax = 0.76f, curiosity = 0.30f, skittishness = 0.15f,
            glowIntensity = 0.58f, recommendedMax = 2
        ),
        FishSpecies(
            id = "bioluminescent_fish", name = "Bioluminescent Fish", scientificName = "Anomalops katoptron",
            description = "Nocturnal turquoise-glowing drifter leaving soft luminous calm in dark water.",
            category = FishCategory.DEEP_SEA, compatibleHabitats = DEEP + HabitatType.EXOTIC,
            bodyPlan = BodyPlan.REEF_OVAL, pattern = PatternType.BIOLUMINESCENT,
            behavior = BehaviorArchetype.TETRA_SCHOOL,
            primaryColor = RgbColor(0.14f, 0.32f, 0.52f), secondaryColor = RgbColor(0.24f, 0.96f, 0.92f),
            accentColor = RgbColor(0.44f, 0.98f, 1.0f), finColor = RgbColor(0.28f, 0.84f, 0.94f),
            baseSize = 0.072f, aspectRatio = 2.1f, baseSpeed = 0.14f, turnInertia = 0.68f,
            schoolingWeight = 0.76f, depthMin = 0.20f, depthMax = 0.80f, curiosity = 0.45f, skittishness = 0.48f,
            glowIntensity = 1.0f, recommendedMax = 18
        ),

        // ═══════════════════════ OTHER AQUATIC ANIMALS (8) ═══════════════════════
        FishSpecies(
            id = "jellyfish", name = "Jellyfish", scientificName = "Chrysaora fuscescens",
            description = "Ethereal pulsing medusa drifting vertically with trailing silk tentacles.",
            category = FishCategory.OTHER_AQUATIC, compatibleHabitats = MARINE_ALL + HabitatType.DEEP_SEA,
            bodyPlan = BodyPlan.JELLYFISH_BELL, pattern = PatternType.BIOLUMINESCENT,
            behavior = BehaviorArchetype.JELLYFISH_PULSE,
            primaryColor = RgbColor(0.74f, 0.58f, 0.90f), secondaryColor = RgbColor(0.42f, 0.84f, 0.96f),
            accentColor = RgbColor(0.94f, 0.78f, 0.98f), finColor = RgbColor(0.68f, 0.78f, 0.98f),
            baseSize = 0.115f, aspectRatio = 0.85f, baseSpeed = 0.055f, turnInertia = 0.92f,
            schoolingWeight = 0.22f, depthMin = 0.15f, depthMax = 0.82f, curiosity = 0.10f, skittishness = 0.18f,
            glowIntensity = 0.72f, recommendedMax = 12
        ),
        FishSpecies(
            id = "sea_turtle", name = "Sea Turtle", scientificName = "Chelonia mydas",
            description = "Ancient mosaic-shelled voyager gliding with slow, graceful flipper strokes.",
            category = FishCategory.OTHER_AQUATIC, compatibleHabitats = OCEAN,
            bodyPlan = BodyPlan.SEA_TURTLE, pattern = PatternType.MOTTLED,
            behavior = BehaviorArchetype.RAY_GLIDER,
            primaryColor = RgbColor(0.36f, 0.46f, 0.34f), secondaryColor = RgbColor(0.64f, 0.54f, 0.34f),
            accentColor = RgbColor(0.24f, 0.30f, 0.22f), finColor = RgbColor(0.48f, 0.52f, 0.38f),
            baseSize = 0.170f, aspectRatio = 1.45f, baseSpeed = 0.11f, turnInertia = 0.94f,
            schoolingWeight = 0.12f, depthMin = 0.16f, depthMax = 0.74f, curiosity = 0.62f, skittishness = 0.14f,
            recommendedMax = 4
        ),
        FishSpecies(
            id = "seahorse", name = "Seahorse", scientificName = "Hippocampus kuda",
            description = "Upright golden-ochre meadow dweller drifting gently among swaying blades.",
            category = FishCategory.OTHER_AQUATIC, compatibleHabitats = REEF + HabitatType.BRACKISH,
            bodyPlan = BodyPlan.SEAHORSE, pattern = PatternType.MOTTLED,
            behavior = BehaviorArchetype.REEF_TERRITORIAL,
            primaryColor = RgbColor(0.84f, 0.66f, 0.28f), secondaryColor = RgbColor(0.64f, 0.44f, 0.18f),
            accentColor = RgbColor(0.92f, 0.82f, 0.48f), finColor = RgbColor(0.86f, 0.72f, 0.36f),
            baseSize = 0.072f, aspectRatio = 0.72f, baseSpeed = 0.05f, turnInertia = 0.84f,
            schoolingWeight = 0.24f, depthMin = 0.44f, depthMax = 0.86f, curiosity = 0.42f, skittishness = 0.38f
        ),
        FishSpecies(
            id = "octopus", name = "Octopus", scientificName = "Octopus vulgaris",
            description = "Fluid, expressive cephalopod gliding softly across reef ledges and kelp.",
            category = FishCategory.OTHER_AQUATIC, compatibleHabitats = MARINE_ALL,
            bodyPlan = BodyPlan.CEPHALOPOD, pattern = PatternType.MOTTLED,
            behavior = BehaviorArchetype.BENTHIC_FORAGER,
            primaryColor = RgbColor(0.68f, 0.34f, 0.28f), secondaryColor = RgbColor(0.48f, 0.24f, 0.26f),
            accentColor = RgbColor(0.86f, 0.62f, 0.48f), finColor = RgbColor(0.72f, 0.42f, 0.34f),
            baseSize = 0.125f, aspectRatio = 1.9f, baseSpeed = 0.11f, turnInertia = 0.82f,
            schoolingWeight = 0.04f, depthMin = 0.56f, depthMax = 0.92f, curiosity = 0.84f, skittishness = 0.28f,
            recommendedMax = 4
        ),
        FishSpecies(
            id = "cuttlefish", name = "Cuttlefish", scientificName = "Sepia officinalis",
            description = "Hypnotic hovering cephalopod rippling with subtle pearlescent waves.",
            category = FishCategory.OTHER_AQUATIC, compatibleHabitats = MARINE_ALL,
            bodyPlan = BodyPlan.CEPHALOPOD, pattern = PatternType.MANDARIN_SWIRL,
            behavior = BehaviorArchetype.KOI_GRACEFUL,
            primaryColor = RgbColor(0.54f, 0.48f, 0.62f), secondaryColor = RgbColor(0.82f, 0.76f, 0.64f),
            accentColor = RgbColor(0.38f, 0.82f, 0.88f), finColor = RgbColor(0.66f, 0.70f, 0.78f),
            baseSize = 0.100f, aspectRatio = 2.0f, baseSpeed = 0.10f, turnInertia = 0.84f,
            schoolingWeight = 0.16f, depthMin = 0.40f, depthMax = 0.86f, curiosity = 0.78f, skittishness = 0.34f,
            glowIntensity = 0.24f, recommendedMax = 6
        ),
        FishSpecies(
            id = "shrimp", name = "Shrimp", scientificName = "Lysmata amboinensis",
            description = "Delicate translucent scarlet-striped cleaner shrimp gliding near flora.",
            category = FishCategory.OTHER_AQUATIC, compatibleHabitats = ALL_HABITATS,
            bodyPlan = BodyPlan.BENTHIC_CRUSTACEAN, pattern = PatternType.HORIZONTAL_STRIPE,
            behavior = BehaviorArchetype.BENTHIC_FORAGER,
            primaryColor = RgbColor(0.88f, 0.32f, 0.24f), secondaryColor = RgbColor(0.96f, 0.92f, 0.84f),
            accentColor = RgbColor(0.94f, 0.78f, 0.36f), finColor = RgbColor(0.92f, 0.56f, 0.46f),
            baseSize = 0.048f, aspectRatio = 2.4f, baseSpeed = 0.13f, turnInertia = 0.54f,
            schoolingWeight = 0.48f, depthMin = 0.66f, depthMax = 0.94f, curiosity = 0.30f, skittishness = 0.80f
        ),
        FishSpecies(
            id = "crab", name = "Crab", scientificName = "Portunus pelagicus",
            description = "Blue-and-amber coastal swimmer skimming quietly across sandy basins.",
            category = FishCategory.OTHER_AQUATIC, compatibleHabitats = ALL_HABITATS,
            bodyPlan = BodyPlan.BENTHIC_CRUSTACEAN, pattern = PatternType.MOTTLED,
            behavior = BehaviorArchetype.BENTHIC_FORAGER,
            primaryColor = RgbColor(0.32f, 0.48f, 0.62f), secondaryColor = RgbColor(0.76f, 0.58f, 0.38f),
            accentColor = RgbColor(0.86f, 0.78f, 0.64f), finColor = RgbColor(0.42f, 0.56f, 0.68f),
            baseSize = 0.068f, aspectRatio = 1.45f, baseSpeed = 0.08f, turnInertia = 0.72f,
            schoolingWeight = 0.10f, depthMin = 0.82f, depthMax = 0.96f, curiosity = 0.34f, skittishness = 0.58f
        ),
        FishSpecies(
            id = "snail", name = "Snail", scientificName = "Pomacea bridgesii",
            description = "Golden-shelled pond dweller tracing tranquil paths along the floor.",
            category = FishCategory.OTHER_AQUATIC, compatibleHabitats = FW,
            bodyPlan = BodyPlan.BENTHIC_CRUSTACEAN, pattern = PatternType.SOLID_GRADIENT,
            behavior = BehaviorArchetype.BENTHIC_FORAGER,
            primaryColor = RgbColor(0.76f, 0.62f, 0.32f), secondaryColor = RgbColor(0.48f, 0.36f, 0.20f),
            accentColor = RgbColor(0.88f, 0.78f, 0.52f), finColor = RgbColor(0.68f, 0.58f, 0.40f),
            baseSize = 0.048f, aspectRatio = 1.35f, baseSpeed = 0.03f, turnInertia = 0.92f,
            schoolingWeight = 0.05f, depthMin = 0.86f, depthMax = 0.97f, curiosity = 0.08f, skittishness = 0.10f
        )
    )

    private val byId = species.associateBy { it.id }

    fun getById(id: String): FishSpecies? = byId[id]
}
