package com.example.rendering

import android.opengl.GLES30
import com.example.model.AudioSpectrum
import com.example.model.WorldEnvironment
import com.example.simulation.WaterRipple

/**
 * Fullscreen OpenGL ES 3.0 shader renderer for layered water depth, volumetric light shafts,
 * animated caustics, surface reflections, touch ripple refraction, and atmospheric fog.
 */
class WaterRenderer {

    private var program = 0
    private var vao = 0
    private var vbo = 0

    // Cached uniform locations
    private var uTime = -1
    private var uAspect = -1
    private var uQuality = -1
    private var uSurfaceColor = -1
    private var uMidColor = -1
    private var uDeepColor = -1
    private var uRayColor = -1
    private var uCausticColor = -1
    private var uRayIntensity = -1
    private var uRayAngle = -1
    private var uCausticIntensity = -1
    private var uFogDensity = -1
    private var uCurrent = -1
    private var uBiolum = -1
    private var uAudio = -1
    private var uRipples = -1

    // Pre-allocated ripple uniform buffer (8 ripples * 4 floats)
    private val rippleUniformData = FloatArray(32)

    fun init() {
        program = ShaderUtils.createProgram(VERTEX_SHADER, FRAGMENT_SHADER)
        if (program == 0) return

        uTime = GLES30.glGetUniformLocation(program, "uTime")
        uAspect = GLES30.glGetUniformLocation(program, "uAspect")
        uQuality = GLES30.glGetUniformLocation(program, "uQuality")
        uSurfaceColor = GLES30.glGetUniformLocation(program, "uSurfaceColor")
        uMidColor = GLES30.glGetUniformLocation(program, "uMidColor")
        uDeepColor = GLES30.glGetUniformLocation(program, "uDeepColor")
        uRayColor = GLES30.glGetUniformLocation(program, "uRayColor")
        uCausticColor = GLES30.glGetUniformLocation(program, "uCausticColor")
        uRayIntensity = GLES30.glGetUniformLocation(program, "uRayIntensity")
        uRayAngle = GLES30.glGetUniformLocation(program, "uRayAngle")
        uCausticIntensity = GLES30.glGetUniformLocation(program, "uCausticIntensity")
        uFogDensity = GLES30.glGetUniformLocation(program, "uFogDensity")
        uCurrent = GLES30.glGetUniformLocation(program, "uCurrent")
        uBiolum = GLES30.glGetUniformLocation(program, "uBiolum")
        uAudio = GLES30.glGetUniformLocation(program, "uAudio")
        uRipples = GLES30.glGetUniformLocation(program, "uRipples")

        val vertices = floatArrayOf(
            -1f, -1f,
            1f, -1f,
            -1f, 1f,
            1f, 1f
        )
        val buffer = ShaderUtils.createFloatBuffer(vertices)

        val vaos = IntArray(1)
        val vbos = IntArray(1)
        GLES30.glGenVertexArrays(1, vaos, 0)
        GLES30.glGenBuffers(1, vbos, 0)
        vao = vaos[0]
        vbo = vbos[0]

        GLES30.glBindVertexArray(vao)
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vbo)
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, vertices.size * 4, buffer, GLES30.GL_STATIC_DRAW)
        GLES30.glEnableVertexAttribArray(0)
        GLES30.glVertexAttribPointer(0, 2, GLES30.GL_FLOAT, false, 8, 0)
        GLES30.glBindVertexArray(0)
    }

    fun draw(
        time: Float,
        aspect: Float,
        quality: Int,
        world: WorldEnvironment,
        audio: AudioSpectrum,
        audioStrength: Float,
        ripples: Array<WaterRipple>
    ) {
        if (program == 0) return

        GLES30.glUseProgram(program)
        GLES30.glUniform1f(uTime, time)
        GLES30.glUniform1f(uAspect, aspect)
        GLES30.glUniform1i(uQuality, quality)

        GLES30.glUniform3f(uSurfaceColor, world.surfaceColor.r, world.surfaceColor.g, world.surfaceColor.b)
        GLES30.glUniform3f(uMidColor, world.midColor.r, world.midColor.g, world.midColor.b)
        GLES30.glUniform3f(uDeepColor, world.deepColor.r, world.deepColor.g, world.deepColor.b)
        GLES30.glUniform3f(uRayColor, world.rayColor.r, world.rayColor.g, world.rayColor.b)
        GLES30.glUniform3f(uCausticColor, world.causticColor.r, world.causticColor.g, world.causticColor.b)

        GLES30.glUniform1f(uRayIntensity, world.rayIntensity)
        GLES30.glUniform1f(uRayAngle, world.rayAngle)
        GLES30.glUniform1f(uCausticIntensity, world.causticIntensity)
        GLES30.glUniform1f(uFogDensity, world.fogDensity)
        GLES30.glUniform1f(uCurrent, world.currentStrength)
        GLES30.glUniform1f(uBiolum, world.bioluminescence)

        val react = audioStrength * world.audioReactivityScale
        GLES30.glUniform4f(
            uAudio,
            audio.bass * react,
            audio.mid * react,
            audio.treble * react,
            audio.energy * react
        )

        val maxRipples = ripples.size.coerceAtMost(8)
        for (i in 0 until 8) {
            val base = i * 4
            if (i < maxRipples && ripples[i].active) {
                rippleUniformData[base] = ripples[i].x
                rippleUniformData[base + 1] = ripples[i].y
                rippleUniformData[base + 2] = ripples[i].radius
                rippleUniformData[base + 3] = ripples[i].strength
            } else {
                rippleUniformData[base] = 0f
                rippleUniformData[base + 1] = 0f
                rippleUniformData[base + 2] = 0f
                rippleUniformData[base + 3] = 0f
            }
        }
        GLES30.glUniform4fv(uRipples, 8, rippleUniformData, 0)

        GLES30.glBindVertexArray(vao)
        GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, 4)
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

            layout(location = 0) in vec2 aPos;
            out vec2 vNdc;
            void main() {
                vNdc = aPos;
                gl_Position = vec4(aPos, 0.0, 1.0);
            }
        """

        private const val FRAGMENT_SHADER = """#version 300 es
            precision highp float;
            precision highp int;

            in vec2 vNdc;
            out vec4 fragColor;

            uniform float uTime;
            uniform float uAspect;
            uniform int uQuality;
            uniform vec3 uSurfaceColor;
            uniform vec3 uMidColor;
            uniform vec3 uDeepColor;
            uniform vec3 uRayColor;
            uniform vec3 uCausticColor;
            uniform float uRayIntensity;
            uniform float uRayAngle;
            uniform float uCausticIntensity;
            uniform float uFogDensity;
            uniform float uCurrent;
            uniform float uBiolum;
            uniform vec4 uAudio; // bass, mid, treble, energy
            uniform vec4 uRipples[8];

            float hash21(vec2 p) {
                p = fract(p * vec2(123.34, 456.21));
                p += dot(p, p + 45.32);
                return fract(p.x * p.y);
            }

            float noise(vec2 p) {
                vec2 i = floor(p);
                vec2 f = fract(p);
                f = f * f * (3.0 - 2.0 * f);
                float a = hash21(i);
                float b = hash21(i + vec2(1.0, 0.0));
                float c = hash21(i + vec2(0.0, 1.0));
                float d = hash21(i + vec2(1.0, 1.0));
                return mix(mix(a, b, f.x), mix(c, d, f.x), f.y);
            }

            // Smooth underwater caustics interference pattern
            float causticLayer(vec2 p, float t) {
                vec2 i = p;
                float c = 1.0;
                float inten = 0.0055;
                int iters = (uQuality >= 2) ? 4 : ((uQuality == 1) ? 3 : 2);
                for (int n = 0; n < 4; n++) {
                    if (n >= iters) break;
                    float tn = t * (1.0 - (2.8 / float(n + 1)));
                    i = p + vec2(
                        cos(tn - i.x) + sin(tn + i.y),
                        sin(tn - i.y) + cos(tn + i.x)
                    );
                    c += 1.0 / max(length(vec2(
                        p.x / (sin(i.x + tn) / inten),
                        p.y / (cos(i.y + tn) / inten)
                    )), 0.001);
                }
                c /= float(iters);
                c = 1.16 - pow(c, 1.38);
                return clamp(pow(abs(c), 7.5), 0.0, 1.0);
            }

            void main() {
                vec2 worldPos = vec2(vNdc.x * uAspect, vNdc.y);
                float depth = clamp(0.5 - vNdc.y * 0.5, 0.0, 1.0);

                // Interactive ripple distortion & subtle specular ring
                vec2 distort = vec2(0.0);
                float rippleHighlight = 0.0;
                for (int i = 0; i < 8; i++) {
                    float strength = uRipples[i].w;
                    if (strength <= 0.005) continue;
                    vec2 diff = worldPos - uRipples[i].xy;
                    float dist = length(diff);
                    float ringDist = dist - uRipples[i].z;
                    float env = exp(-ringDist * ringDist * 46.0) * strength;
                    float wave = sin(ringDist * 42.0 - uTime * 3.0) * env;
                    vec2 dir = diff / max(dist, 0.001);
                    distort += dir * wave * 0.032;
                    rippleHighlight += max(0.0, wave) * 0.38 + env * 0.14;
                }

                vec2 p = worldPos + distort;
                float flow = uTime * (0.28 + uCurrent * 0.45);
                float bassPulse = uAudio.x * 0.25;
                float midPulse = uAudio.y * 0.22;
                float treblePulse = uAudio.z * 0.24;

                // 1. Natural 3-stop water column color gradient
                vec3 upperBlend = mix(uSurfaceColor, uMidColor, smoothstep(0.0, 0.52, depth));
                vec3 col = mix(upperBlend, uDeepColor, smoothstep(0.42, 1.0, depth));

                // Subtle organic depth current modulation
                float waterBodyWave = noise(vec2(p.x * 1.4 + flow * 0.35, p.y * 1.8 - flow * 0.12));
                col += (uSurfaceColor - uDeepColor) * (waterBodyWave - 0.5) * 0.075;

                // 2. Soft undulating water surface reflection canopy near top edge
                float surfaceWave = sin(p.x * 5.2 + uTime * 1.35) * 0.018
                                  + cos(p.x * 9.6 - uTime * 1.9) * 0.009;
                float surfaceMask = smoothstep(0.24 + surfaceWave, -0.02, depth);
                float shimmer = 0.65 + 0.35 * sin(p.x * 14.0 + uTime * 2.1 + distort.x * 18.0);
                col += mix(uSurfaceColor, uRayColor, 0.55) * surfaceMask * (0.22 + 0.12 * shimmer + treblePulse * 0.14);

                // 3. Volumetric diagonal underwater light rays
                if (uRayIntensity > 0.02) {
                    float rayX = p.x - (1.0 - p.y) * uRayAngle;
                    float r1 = sin(rayX * 4.6 + uTime * 0.38) * 0.5 + 0.5;
                    float r2 = sin(rayX * 8.9 - uTime * 0.27 + 1.4) * 0.5 + 0.5;
                    float r3 = (uQuality >= 1)
                        ? (sin(rayX * 15.2 + uTime * 0.52 + 2.7) * 0.5 + 0.5)
                        : 0.5;
                    float shafts = pow(r1 * 0.55 + r2 * 0.30 + r3 * 0.15, 2.6);
                    float rayFade = exp(-depth * (1.55 + uFogDensity * 1.15)) * smoothstep(1.0, 0.15, depth);
                    float rayBoost = 1.0 + bassPulse * 0.55 + midPulse * 0.40;
                    col += uRayColor * shafts * rayFade * uRayIntensity * 0.42 * rayBoost;
                }

                // 4. Projected animated underwater caustics
                if (uCausticIntensity > 0.02) {
                    vec2 cUv = vec2(p.x * 2.15 + flow * 0.16, p.y * 1.85 - flow * 0.08);
                    float cVal = causticLayer(cUv, uTime * 0.42 + bassPulse * 0.2);
                    float upperCaustic = cVal * smoothstep(0.75, 0.0, depth) * 0.34;
                    // Floor projected caustic glow
                    float floorMask = smoothstep(0.68, 1.0, depth);
                    vec2 floorUv = vec2(p.x * 3.1 + flow * 0.25, depth * 3.5);
                    float floorCaustic = (uQuality >= 1)
                        ? causticLayer(floorUv, uTime * 0.52) * floorMask * 0.22
                        : cVal * floorMask * 0.12;
                    float causticPower = uCausticIntensity * (1.0 + treblePulse * 0.45 + bassPulse * 0.25);
                    col += uCausticColor * (upperCaustic + floorCaustic) * causticPower;
                }

                // 5. Bioluminescent ambient water mist for deep/exotic worlds
                if (uBiolum > 0.05) {
                    float bioNoise = noise(vec2(p.x * 2.4 - uTime * 0.18, p.y * 2.2 + uTime * 0.14));
                    float bioGlow = smoothstep(0.45, 0.92, bioNoise) * uBiolum;
                    col += uCausticColor * bioGlow * (0.14 + uAudio.w * 0.22);
                }

                // 6. Soft seafloor terrain silhouette & ambient bottom bounce
                float floorWave = -0.88 + sin(p.x * 2.1 + 0.6) * 0.055 + cos(p.x * 4.3) * 0.025;
                float seabed = smoothstep(floorWave + 0.14, floorWave - 0.08, vNdc.y);
                vec3 seabedCol = uDeepColor * 0.62 + uCausticColor * (0.04 * uCausticIntensity);
                col = mix(col, seabedCol, seabed * 0.58);

                // 7. Interactive ripple water highlight
                col += mix(uCausticColor, uRayColor, 0.6) * rippleHighlight * (0.35 + uCausticIntensity * 0.25);

                // 8. Cinematic atmospheric vignette (subtle depth framing)
                float vignette = 1.0 - dot(vNdc * vec2(0.48, 0.54), vNdc * vec2(0.48, 0.54));
                col *= clamp(vignette + 0.18, 0.52, 1.04);

                fragColor = vec4(col, 1.0);
            }
        """
    }
}
