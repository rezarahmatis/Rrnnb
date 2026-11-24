package com.example.rendering

import android.opengl.GLES30
import android.opengl.GLSurfaceView
import com.example.model.PerformanceMode
import com.example.simulation.AquaticSimulationEngine
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * Primary OpenGL ES 3.0 renderer coordinating layered aquatic rendering:
 * 1. Water column, volumetric rays, caustics, surface shimmer & touch ripple distortion
 * 2. Background & midground aquatic vegetation / terrain architecture
 * 3. Depth-sorted anatomical fish & aquatic animals with cast shadows
 * 4. Rising air bubbles & suspended marine snow / bioluminescent plankton
 */
class AquaticGLRenderer(
    private val engine: AquaticSimulationEngine
) : GLSurfaceView.Renderer {

    private val waterRenderer = WaterRenderer()
    private val vegetationRenderer = VegetationRenderer()
    private val fishRenderer = FishRenderer()
    private val particleRenderer = ParticleRenderer(maxParticles = engine.particleSystem.maxParticles)

    private var viewportWidth = 1f
    private var viewportHeight = 1f
    private var lastFrameNanos = 0L
    private var initStage = 0

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES30.glClearColor(0.015f, 0.045f, 0.075f, 1.0f)
        GLES30.glDisable(GLES30.GL_DEPTH_TEST)
        GLES30.glEnable(GLES30.GL_BLEND)
        GLES30.glBlendFunc(GLES30.GL_SRC_ALPHA, GLES30.GL_ONE_MINUS_SRC_ALPHA)

        // Reset stale handles if the EGL context was recreated and compile the water shader first
        // so the initial frame renders rapidly without blocking WindowManager SurfaceSyncGroup.
        waterRenderer.resetHandles()
        vegetationRenderer.resetHandles()
        fishRenderer.resetHandles()
        particleRenderer.resetHandles()

        waterRenderer.init()
        initStage = 1
        lastFrameNanos = System.nanoTime()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        val safeW = width.coerceAtLeast(1)
        val safeH = height.coerceAtLeast(1)
        viewportWidth = safeW.toFloat()
        viewportHeight = safeH.toFloat()
        GLES30.glViewport(0, 0, safeW, safeH)
        engine.setViewportAspect(viewportWidth / viewportHeight)
    }

    override fun onDrawFrame(gl: GL10?) {
        val now = System.nanoTime()
        val dt = if (lastFrameNanos == 0L) 0.016f else (now - lastFrameNanos) * 1e-9f
        lastFrameNanos = now

        val settings = engine.currentSettings
        // Gentle frame pacing in Battery Saver mode (~30fps target to save GPU/battery)
        if (initStage >= 4 && settings.performanceMode == PerformanceMode.BATTERY_SAVER && dt < 0.028f) {
            try {
                Thread.sleep(((0.030f - dt) * 1000f).toLong().coerceIn(1L, 18L))
            } catch (_: InterruptedException) {
            }
        }

        val aspect = engine.step(dt)
        val world = engine.currentWorld
        val audio = engine.smoothedAudio
        val time = engine.elapsedTime
        val audioStrength = settings.audioReactivityStrength
        val shaderQuality = settings.performanceMode.shaderQuality

        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT)

        // Pass 1: Water column, sunbeams, caustics & interactive ripples
        GLES30.glDisable(GLES30.GL_BLEND)
        waterRenderer.draw(
            time = time,
            aspect = aspect,
            quality = shaderQuality,
            world = world,
            audio = audio,
            audioStrength = audioStrength,
            ripples = engine.particleSystem.ripples
        )

        // Progressively compile remaining shader passes after frame 1 has already been presented
        when (initStage) {
            1 -> {
                initStage = 2
                return
            }
            2 -> {
                vegetationRenderer.init()
                initStage = 3
            }
            3 -> {
                fishRenderer.init()
                initStage = 4
            }
            4 -> {
                particleRenderer.init()
                initStage = 5
            }
        }

        // Enable smooth alpha compositing for layered aquatic life & flora
        GLES30.glEnable(GLES30.GL_BLEND)
        GLES30.glBlendFunc(GLES30.GL_SRC_ALPHA, GLES30.GL_ONE_MINUS_SRC_ALPHA)

        // Pass 2: Underwater flora, coral, kelp, ruins, ice & volcanic vents
        if (initStage >= 3) {
            vegetationRenderer.draw(
                time = time,
                aspect = aspect,
                world = world,
                audio = audio,
                audioStrength = audioStrength,
                flora = engine.particleSystem.flora,
                activeCount = engine.particleSystem.activeFloraCount
            )
        }

        // Pass 3: Depth-sorted realistic multi-species fish & marine animals
        if (initStage >= 4) {
            fishRenderer.draw(
                time = time,
                aspect = aspect,
                world = world,
                audio = audio,
                audioStrength = audioStrength,
                shaderQuality = shaderQuality,
                school = engine.fishSchool
            )
        }

        // Pass 4: Rising air bubbles & suspended plankton / bioluminescent motes
        if (initStage >= 5) {
            particleRenderer.draw(
                aspect = aspect,
                viewportHeight = viewportHeight,
                world = world,
                audio = audio,
                audioStrength = audioStrength,
                particles = engine.particleSystem.particles,
                activeCount = engine.particleSystem.activeParticleCount
            )
        }
    }

    fun release() {
        waterRenderer.release()
        vegetationRenderer.release()
        fishRenderer.release()
        particleRenderer.release()
    }
}
