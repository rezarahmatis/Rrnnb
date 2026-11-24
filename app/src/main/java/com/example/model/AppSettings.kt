package com.example.model

enum class PerformanceMode(
    val label: String,
    val description: String,
    val shaderQuality: Int,
    val particleScale: Float,
    val vegetationScale: Float
) {
    HIGH_QUALITY(
        label = "High Quality",
        description = "Full volumetric light rays, crisp caustics, lush flora & up to 110 creatures",
        shaderQuality = 2,
        particleScale = 1.0f,
        vegetationScale = 1.0f
    ),
    BALANCED(
        label = "Balanced",
        description = "Silky 60 FPS craft with balanced lighting, particles & up to 75 creatures",
        shaderQuality = 1,
        particleScale = 0.68f,
        vegetationScale = 0.78f
    ),
    BATTERY_SAVER(
        label = "Battery Saver",
        description = "Optimized shader complexity, fewer particles & lighter simulation for long sessions",
        shaderQuality = 0,
        particleScale = 0.38f,
        vegetationScale = 0.50f
    )
}

data class AppSettings(
    val performanceMode: PerformanceMode = PerformanceMode.BALANCED,
    val ambientMode: Boolean = false,
    val fullscreenMode: Boolean = true,
    val keepScreenOn: Boolean = true,
    val reducedMotion: Boolean = false,
    val hapticFeedback: Boolean = true,
    val audioReactivityStrength: Float = 0.75f,
    val overrideCompatibility: Boolean = false,
    val activeWorldId: String = "japanese_koi_garden",
    val activeAquariumId: Long? = null
)
