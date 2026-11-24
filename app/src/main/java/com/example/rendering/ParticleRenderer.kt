package com.example.rendering

import android.opengl.GLES30
import com.example.model.AudioSpectrum
import com.example.model.WorldEnvironment
import com.example.simulation.AquaticParticle
import java.nio.FloatBuffer

/**
 * Batched GPU point-sprite renderer for rising glossy air bubbles and drifting
 * suspended marine snow / bioluminescent plankton motes. Zero per-frame heap allocations.
 */
class ParticleRenderer(private val maxParticles: Int = 220) {

    private var program = 0
    private var vao = 0
    private var vbo = 0

    // Per particle: x, y, z, size, alpha, isBubble (6 floats)
    private val floatsPerParticle = 6
    private val stagingArray = FloatArray(maxParticles * floatsPerParticle)
    private val stagingBuffer: FloatBuffer = ShaderUtils.allocateFloatBuffer(maxParticles * floatsPerParticle)

    private var uAspect = -1
    private var uViewportHeight = -1
    private var uParticleColor = -1
    private var uBiolum = -1
    private var uAudioGlow = -1

    fun init() {
        program = ShaderUtils.createProgram(VERTEX_SHADER, FRAGMENT_SHADER)
        if (program == 0) return

        uAspect = GLES30.glGetUniformLocation(program, "uAspect")
        uViewportHeight = GLES30.glGetUniformLocation(program, "uViewportHeight")
        uParticleColor = GLES30.glGetUniformLocation(program, "uParticleColor")
        uBiolum = GLES30.glGetUniformLocation(program, "uBiolum")
        uAudioGlow = GLES30.glGetUniformLocation(program, "uAudioGlow")

        val vaos = IntArray(1)
        val vbos = IntArray(1)
        GLES30.glGenVertexArrays(1, vaos, 0)
        GLES30.glGenBuffers(1, vbos, 0)
        vao = vaos[0]
        vbo = vbos[0]

        GLES30.glBindVertexArray(vao)
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vbo)
        GLES30.glBufferData(
            GLES30.GL_ARRAY_BUFFER,
            maxParticles * floatsPerParticle * 4,
            null,
            GLES30.GL_DYNAMIC_DRAW
        )

        val stride = floatsPerParticle * 4
        // location 0: vec3 (x, y, z)
        GLES30.glEnableVertexAttribArray(0)
        GLES30.glVertexAttribPointer(0, 3, GLES30.GL_FLOAT, false, stride, 0)
        // location 1: vec3 (size, alpha, isBubble)
        GLES30.glEnableVertexAttribArray(1)
        GLES30.glVertexAttribPointer(1, 3, GLES30.GL_FLOAT, false, stride, 12)

        GLES30.glBindVertexArray(0)
    }

    fun draw(
        aspect: Float,
        viewportHeight: Float,
        world: WorldEnvironment,
        audio: AudioSpectrum,
        audioStrength: Float,
        particles: Array<AquaticParticle>,
        activeCount: Int
    ) {
        if (program == 0 || activeCount <= 0) return
        val count = activeCount.coerceAtMost(maxParticles)

        var idx = 0
        for (i in 0 until count) {
            val p = particles[i]
            stagingArray[idx++] = p.x
            stagingArray[idx++] = p.y
            stagingArray[idx++] = p.z
            stagingArray[idx++] = p.size
            stagingArray[idx++] = p.alpha
            stagingArray[idx++] = if (p.isBubble) 1.0f else 0.0f
        }

        stagingBuffer.clear()
        stagingBuffer.put(stagingArray, 0, count * floatsPerParticle)
        stagingBuffer.flip()

        GLES30.glUseProgram(program)
        GLES30.glUniform1f(uAspect, aspect)
        GLES30.glUniform1f(uViewportHeight, viewportHeight)
        GLES30.glUniform3f(uParticleColor, world.particleColor.r, world.particleColor.g, world.particleColor.b)
        GLES30.glUniform1f(uBiolum, world.bioluminescence)
        GLES30.glUniform1f(uAudioGlow, (audio.treble * 0.6f + audio.energy * 0.4f) * audioStrength * world.audioReactivityScale)

        GLES30.glBindVertexArray(vao)
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vbo)
        GLES30.glBufferSubData(GLES30.GL_ARRAY_BUFFER, 0, count * floatsPerParticle * 4, stagingBuffer)
        GLES30.glDrawArrays(GLES30.GL_POINTS, 0, count)
        GLES30.glBindVertexArray(0)
    }

    fun resetHandles() {
        program = 0
        vao = 0
        vbo = 0
    }

    fun release() {
        if (program != 0) GLES30.glDeleteProgram(program)
        if (vbo != 0) GLES30.glDeleteBuffers(1, intArrayOf(vbo), 0)
        if (vao != 0) GLES30.glDeleteVertexArrays(1, intArrayOf(vao), 0)
        program = 0
        vao = 0
        vbo = 0
    }

    companion object {
        private const val VERTEX_SHADER = """#version 300 es
            precision highp float;
            precision highp int;

            layout(location = 0) in vec3 aPosDepth;
            layout(location = 1) in vec3 aMeta; // size, alpha, isBubble

            uniform float uAspect;
            uniform float uViewportHeight;

            out float vAlpha;
            out float vIsBubble;
            out float vDepth;

            void main() {
                gl_Position = vec4(aPosDepth.x / uAspect, aPosDepth.y, 0.0, 1.0);
                float depthScale = 0.55 + aPosDepth.z * 0.85;
                gl_PointSize = clamp(aMeta.x * uViewportHeight * depthScale, 2.5, 28.0);
                vAlpha = aMeta.y * (0.45 + 0.55 * aPosDepth.z);
                vIsBubble = aMeta.z;
                vDepth = aPosDepth.z;
            }
        """

        private const val FRAGMENT_SHADER = """#version 300 es
            precision highp float;
            precision highp int;

            in float vAlpha;
            in float vIsBubble;
            in float vDepth;
            out vec4 fragColor;

            uniform vec3 uParticleColor;
            uniform float uBiolum;
            uniform float uAudioGlow;

            void main() {
                vec2 p = gl_PointCoord * 2.0 - 1.0;
                float r = length(p);
                if (r > 1.0) discard;

                if (vIsBubble > 0.5) {
                    // Glossy refractive air bubble with specular highlight
                    float rim = smoothstep(0.55, 0.92, r) * smoothstep(1.0, 0.88, r);
                    float spec = smoothstep(0.32, 0.06, length(p - vec2(-0.32, -0.34)));
                    float interior = (1.0 - r * r) * 0.12;
                    vec3 col = mix(uParticleColor, vec3(0.96, 0.99, 1.0), 0.7 + spec * 0.3);
                    float a = (rim * 0.65 + spec * 0.85 + interior) * vAlpha;
                    fragColor = vec4(col, clamp(a, 0.0, 0.92));
                } else {
                    // Soft organic marine snow or glowing bioluminescent plankton
                    float soft = exp(-r * r * (3.6 - uBiolum * 1.4));
                    float glowBoost = 1.0 + uBiolum * (0.8 + uAudioGlow * 0.75);
                    vec3 col = uParticleColor * glowBoost;
                    float a = soft * vAlpha * (0.52 + uBiolum * 0.42);
                    fragColor = vec4(col, clamp(a, 0.0, 0.92));
                }
            }
        """
    }
}
