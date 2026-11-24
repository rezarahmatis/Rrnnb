# Pelagia

An interactive native Android aquatic music experience powered by OpenGL ES 3.0, Jetpack Compose, and AndroidX Media3.

## Features

- **38 Aquatic Worlds**: 15 Freshwater, 12 Tropical & Ocean, and 11 Exotic & Atmospheric environments with distinct water palettes, lighting rays, caustics, fog, currents, bioluminescence, and flora.
- **74 Realistic Fish & Aquatic Species**: 27 Freshwater, 21 Tropical/Reef, 10 Large Ocean, 8 Deep Sea, and 8 Other Aquatic Animals with procedural GPU anatomy and species-specific flocking, cruising, territorial, pulsing, and benthic behaviors.
- **Multi-Species Aquariums**: Combine multiple species per world, adjust counts, validate habitat compatibility with optional override, and save, load, rename, or delete custom aquarium configurations.
- **Interactive Water & Touch**: Tap or drag across the water for refractive ripples, particle displacement, flora sway, and natural fish curiosity or scatter responses.
- **Native Music Playback & Audio Reactivity**: AndroidX Media3 / ExoPlayer with MediaSession background playback, lock-screen & notification controls, playlists, shuffle/repeat, Storage Access Framework (SAF) local audio import (MP3, WAV, OGG, M4A, AAC), and zero-permission real-time 3-band PCM audio reactivity.
- **Performance & Immersion**: High Quality, Balanced, and Battery Saver modes, Ambient Hide-UI mode, fullscreen edge-to-edge layout, keep-screen-on during playback, reduced motion, and haptic feedback toggles.

## Tech Stack

- **Kotlin** & **Jetpack Compose** (Material 3)
- **OpenGL ES 3.0** (GLSL ES 3.00 procedural shaders for water, caustics, flora, fish anatomy, shadows, and particles)
- **AndroidX Media3** (ExoPlayer + MediaSessionService + custom PCM `AudioProcessor`)
- **Room** (local playlists, music library metadata, and saved aquariums)
- **DataStore** (lightweight preferences)
- **Coroutines & Flow**

## Building

### Local Build (Android Studio / Gradle)

1. Open the project in Android Studio (JDK 17+).
2. Run Gradle sync and build an APK:
   ```bash
   ./gradlew assembleDebug
   ```
3. Install the generated APK from `app/build/outputs/apk/debug/`.

### GitHub Actions

Push to `main` or trigger the **Build Android APK** workflow manually in the **Actions** tab to build and download debug and unsigned release APK artifacts.
