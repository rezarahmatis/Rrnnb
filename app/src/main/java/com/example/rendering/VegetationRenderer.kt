package com.example.rendering

import android.opengl.GLES30
import com.example.model.AudioSpectrum
import com.example.model.VegetationStyle
import com.example.model.WorldEnvironment
import com.example.simulation.FloraStalk

/**
 * Procedural GPU renderer for 10 distinct aquatic flora & underwater architecture styles
 * (lotus/reeds, river grass, mangrove roots, coral reefs, seagrass, giant kelp,
 * glacial ice spires, hydrothermal vents, crystal spires, and sunken temple ruins).
 */
class VegetationRenderer {

    private var program = 0
    private var vao = 0
    private var vbo = 0

    private var uAspect = -1
    private var uTime = -1
    private var uStyle = -1
    private var uFloraColor = -1
    private var uDeepColor = -1
    private var uRayColor = -1
    private var uBiolum = -1
    private var uFogDensity = -1
    private var uAudioGlow = -1
    private var uStalkData = -1 // x, baseY, width, height
    private var uStalkMeta = -1 // depth, bendOffset, phase, tintVar

    fun init() {
        program = ShaderUtils.createProgram(VERTEX_SHADER, FRAGMENT_SHADER)
        if (program == 0) return

        uAspect = GLES30.glGetUniformLocation(program, "uAspect")
        uTime = GLES30.glGetUniformLocation(program, "uTime")
        uStyle = GLES30.glGetUniformLocation(program, "uStyle")
        uFloraColor = GLES30.glGetUniformLocation(program, "uFloraColor")
        uDeepColor = GLES30.glGetUniformLocation(program, "uDeepColor")
        uRayColor = GLES30.glGetUniformLocation(program, "uRayColor")
        uBiolum = GLES30.glGetUniformLocation(program, "uBiolum")
        uFogDensity = GLES30.glGetUniformLocation(program, "uFogDensity")
        uAudioGlow = GLES30.glGetUniformLocation(program, "uAudioGlow")
        uStalkData = GLES30.glGetUniformLocation(program, "uStalkData")
        uStalkMeta = GLES30.glGetUniformLocation(program, "uStalkMeta")

        // Unit quad: x in [-1, 1], y in [0, 1]
        val quad = floatArrayOf(
            -1f, 0f,
            1f, 0f,
            -1f, 1f,
            1f, 1f
        )
        val buffer = ShaderUtils.createFloatBuffer(quad)
        val vaos = IntArray(1)
        val vbos = IntArray(1)
        GLES30.glGenVertexArrays(1, vaos, 0)
        GLES30.glGenBuffers(1, vbos, 0)
        vao = vaos[0]
        vbo = vbos[0]

        GLES30.glBindVertexArray(vao)
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, vbo)
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, quad.size * 4, buffer, GLES30.GL_STATIC_DRAW)
        GLES30.glEnableVertexAttribArray(0)
        GLES30.glVertexAttribPointer(0, 2, GLES30.GL_FLOAT, false, 8, 0)
        GLES30.glBindVertexArray(0)
    }

    fun draw(
        time: Float,
        aspect: Float,
        world: WorldEnvironment,
        audio: AudioSpectrum,
        audioStrength: Float,
        flora: Array<FloraStalk>,
        activeCount: Int
    ) {
        if (program == 0 || activeCount <= 0 || world.vegetationStyle == VegetationStyle.NONE_ABYSSAL) return

        GLES30.glUseProgram(program)
        GLES30.glUniform1f(uAspect, aspect)
        GLES30.glUniform1f(uTime, time)
        GLES30.glUniform1i(uStyle, world.vegetationStyle.shaderId)
        GLES30.glUniform3f(uFloraColor, world.floraColor.r, world.floraColor.g, world.floraColor.b)
        GLES30.glUniform3f(uDeepColor, world.deepColor.r, world.deepColor.g, world.deepColor.b)
        GLES30.glUniform3f(uRayColor, world.rayColor.r, world.rayColor.g, world.rayColor.b)
        GLES30.glUniform1f(uBiolum, world.bioluminescence)
        GLES30.glUniform1f(uFogDensity, world.fogDensity)
        GLES30.glUniform1f(uAudioGlow, audio.energy * audioStrength * world.audioReactivityScale)

        GLES30.glBindVertexArray(vao)
        for (i in 0 until activeCount) {
            val f = flora[i]
            val depthScale = 0.65f + f.depth * 0.55f
            val width = f.width * depthScale * when (world.vegetationStyle) {
                VegetationStyle.CORAL_GARDEN -> 2.4f
                VegetationStyle.MANGROVE_ROOTS -> 2.2f
                VegetationStyle.SUNKEN_RUINS -> 2.0f
                VegetationStyle.ICE_FORMATIONS, VegetationStyle.CRYSTAL_SPIRES -> 1.9f
                VegetationStyle.VOLCANIC_VENTS -> 2.3f
                VegetationStyle.LOTUS_AND_REEDS -> 1.8f
                else -> 1.45f
            }
            val height = f.height * depthScale * when (world.vegetationStyle) {
                VegetationStyle.GIANT_KELP -> 1.35f
                VegetationStyle.MANGROVE_ROOTS -> 1.25f
                VegetationStyle.SUNKEN_RUINS -> 1.15f
                VegetationStyle.CORAL_GARDEN -> 0.72f
                VegetationStyle.VOLCANIC_VENTS -> 0.80f
                else -> 0.92f
            }

            GLES30.glUniform4f(uStalkData, f.x, f.baseY, width, height)
            GLES30.glUniform4f(uStalkMeta, f.depth, f.bendOffset, f.phase, f.tintVariation)
            GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, 4)
        }
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

            layout(location = 0) in vec2 aPos; // x in [-1, 1], y in [0, 1]

            uniform float uAspect;
            uniform int uStyle;
            uniform vec4 uStalkData; // x, baseY, width, height
            uniform vec4 uStalkMeta; // depth, bendOffset, phase, tintVar

            out vec2 vUv; // x in [-1, 1], y in [0, 1]

            void main() {
                vUv = aPos;
                float isRigid = (uStyle == 6 || uStyle == 7 || uStyle == 8 || uStyle == 9) ? 0.08 : 1.0;
                // Mangrove hangs from surface down for half of stalks
                bool isHangingRoot = (uStyle == 2 && uStalkMeta.w > 0.45);
                float yFactor = aPos.y;
                float bend = uStalkMeta.y * (yFactor * yFactor) * isRigid;

                float worldX = uStalkData.x + aPos.x * uStalkData.z + bend;
                float worldY = isHangingRoot
                    ? (1.06 - yFactor * uStalkData.w)
                    : (uStalkData.y + yFactor * uStalkData.w);

                gl_Position = vec4(worldX / uAspect, worldY, 0.0, 1.0);
            }
        """

        private const val FRAGMENT_SHADER = """#version 300 es
            precision highp float;
            precision highp int;

            in vec2 vUv;
            out vec4 fragColor;

            uniform float uTime;
            uniform int uStyle;
            uniform vec3 uFloraColor;
            uniform vec3 uDeepColor;
            uniform vec3 uRayColor;
            uniform float uBiolum;
            uniform float uFogDensity;
            uniform float uAudioGlow;
            uniform vec4 uStalkMeta; // depth, bendOffset, phase, tintVar

            void main() {
                float x = vUv.x;
                float y = vUv.y;
                float seed = uStalkMeta.w;
                float alpha = 0.0;
                vec3 baseCol = uFloraColor * (0.72 + 0.56 * seed);
                float emissive = 0.0;

                if (uStyle == 0) {
                    // Lotus & Reeds: slender reed blades + rounded lotus pad crown
                    float stemCurve = sin(y * 3.0 + uStalkMeta.z) * 0.12;
                    float stemW = mix(0.11, 0.035, y);
                    float stem = smoothstep(stemW + 0.03, stemW, abs(x - stemCurve));
                    float padDist = length(vec2((x - stemCurve) * 1.15, (y - 0.90) * 4.4));
                    float pad = smoothstep(0.62, 0.52, padDist) * step(0.45, seed);
                    // Delicate lotus petal tint on select stalks
                    if (pad > 0.1 && seed > 0.78) {
                        baseCol = mix(baseCol, vec3(0.86, 0.54, 0.62), 0.55);
                    }
                    alpha = max(stem * smoothstep(1.0, 0.88, y), pad);
                } else if (uStyle == 1 || uStyle == 4) {
                    // River Grass / Seagrass Meadow: triple tapered swaying blades
                    float b1 = abs(x - sin(y * 3.6 + uStalkMeta.z) * 0.16);
                    float b2 = abs(x + 0.34 - sin(y * 4.2 + uStalkMeta.z + 1.2) * 0.18);
                    float b3 = abs(x - 0.36 - sin(y * 3.2 + uStalkMeta.z + 2.4) * 0.15);
                    float w1 = mix(0.16, 0.015, y);
                    float w2 = mix(0.13, 0.012, clamp(y * 1.15, 0.0, 1.0));
                    float g1 = smoothstep(w1 + 0.03, w1, b1);
                    float g2 = smoothstep(w2 + 0.03, w2, b2) * step(y, 0.86);
                    float g3 = smoothstep(w2 + 0.03, w2, b3) * step(y, 0.78);
                    alpha = max(g1, max(g2, g3));
                } else if (uStyle == 2) {
                    // Mangrove Roots: gnarled branching submerged roots
                    float r1 = abs(x - sin(y * 4.5 + uStalkMeta.z) * 0.22);
                    float r2 = abs(x - (0.35 * y + sin(y * 5.0 + seed * 8.0) * 0.12));
                    float w1 = mix(0.24, 0.06, y);
                    float w2 = mix(0.14, 0.04, y);
                    alpha = max(smoothstep(w1 + 0.04, w1, r1), smoothstep(w2 + 0.04, w2, r2));
                    baseCol = mix(baseCol, vec3(0.24, 0.18, 0.12), 0.45);
                } else if (uStyle == 3) {
                    // Coral Reef: organic branching staghorn & sea-fan clusters
                    float trunk = smoothstep(0.18, 0.11, abs(x)) * smoothstep(0.88, 0.2, y);
                    float br1 = smoothstep(0.13, 0.07, abs(x - (y - 0.25) * 0.72)) * step(0.24, y) * smoothstep(0.95, 0.80, y);
                    float br2 = smoothstep(0.13, 0.07, abs(x + (y - 0.32) * 0.68)) * step(0.30, y) * smoothstep(0.90, 0.75, y);
                    float br3 = smoothstep(0.11, 0.06, abs(x - (y - 0.55) * 0.55 + 0.2)) * step(0.52, y) * smoothstep(0.96, 0.84, y);
                    alpha = max(max(trunk, br1), max(br2, br3));
                    vec3 reefAccent = mix(vec3(0.76, 0.36, 0.44), vec3(0.32, 0.78, 0.76), seed);
                    baseCol = mix(baseCol, reefAccent, 0.48);
                    emissive = uBiolum * smoothstep(0.3, 0.95, y) * 0.65;
                } else if (uStyle == 5) {
                    // Giant Kelp: central stipe with alternating undulating leaf fronds
                    float spine = sin(y * 5.0 + uStalkMeta.z) * 0.14;
                    float stipe = smoothstep(0.08, 0.04, abs(x - spine));
                    float frondWave = sin(y * 22.0 + uStalkMeta.z * 2.0);
                    float frondReach = 0.55 * (1.0 - y * 0.35) * (0.5 + 0.5 * frondWave);
                    float leaf = smoothstep(frondReach + 0.05, frondReach * 0.6, abs(x - spine));
                    alpha = max(stipe, leaf * smoothstep(0.05, 0.22, y) * smoothstep(1.0, 0.88, y));
                } else if (uStyle == 6 || uStyle == 8) {
                    // Glacial Ice Spires / Fantasy Crystal Spires: faceted polygonal prisms
                    float width = mix(0.58, 0.02, pow(y, 0.85));
                    float facet = smoothstep(width + 0.03, width - 0.01, abs(x));
                    float sideCrystal = smoothstep(mix(0.36, 0.01, clamp(y * 1.45, 0.0, 1.0)) + 0.03,
                                                   mix(0.36, 0.01, clamp(y * 1.45, 0.0, 1.0)) - 0.01,
                                                   abs(x - 0.38)) * step(y, 0.68);
                    alpha = max(facet, sideCrystal) * 0.84;
                    float facetHighlight = smoothstep(-0.1, 0.25, x) * 0.35 + (1.0 - y) * 0.1;
                    baseCol = mix(baseCol, uRayColor, facetHighlight + 0.22);
                    emissive = (uStyle == 8 ? 0.55 : 0.18) * (0.5 + 0.5 * y) + uBiolum * 0.3;
                } else if (uStyle == 7) {
                    // Volcanic Hydrothermal Vents: jagged basalt chimneys with magma glow fissures
                    float chimneyW = mix(0.68, 0.24, y) + sin(y * 14.0 + seed * 10.0) * 0.05;
                    alpha = smoothstep(chimneyW + 0.04, chimneyW - 0.02, abs(x));
                    float crack = smoothstep(0.14, 0.01, abs(x - sin(y * 9.0 + seed * 6.0) * 0.12)) * smoothstep(0.05, 0.92, y);
                    baseCol = mix(vec3(0.09, 0.07, 0.08), vec3(0.98, 0.38, 0.10), crack * (0.65 + 0.35 * sin(uTime * 1.8 + seed * 7.0)));
                    emissive = crack * (0.75 + uAudioGlow * 0.55);
                } else if (uStyle == 9) {
                    // Sunken Ruins: weathered stone pillar with fluted lines and mossy top
                    float colW = 0.42 + 0.06 * smoothstep(0.84, 0.96, y) + 0.08 * smoothstep(0.14, 0.0, y);
                    float brokenTop = 0.86 + sin(x * 7.0 + seed * 9.0) * 0.12;
                    alpha = smoothstep(colW + 0.03, colW - 0.01, abs(x)) * smoothstep(brokenTop + 0.04, brokenTop - 0.02, y);
                    float flute = sin(x * 24.0) * 0.08;
                    baseCol = baseCol + vec3(flute);
                    float runeGlow = smoothstep(0.08, 0.01, abs(x)) * smoothstep(0.42, 0.72, y) * uBiolum;
                    emissive = runeGlow * (0.65 + uAudioGlow * 0.5);
                }

                if (alpha < 0.02) discard;

                // Top-down underwater lighting & depth fog integration
                float topLight = 0.48 + 0.52 * y;
                vec3 lit = baseCol * topLight + uRayColor * (emissive * 0.65 + y * 0.12);
                float fog = clamp((1.0 - uStalkMeta.x) * (0.45 + uFogDensity * 0.55), 0.08, 0.82);
                vec3 finalCol = mix(lit, uDeepColor * 1.15, fog);

                float finalAlpha = alpha * mix(0.45, 0.92, uStalkMeta.x);
                fragColor = vec4(finalCol, finalAlpha);
            }
        """
    }
}
