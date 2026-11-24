package com.example.rendering

import android.opengl.GLES30
import com.example.model.AudioSpectrum
import com.example.model.BodyPlan
import com.example.model.WorldEnvironment
import com.example.simulation.FishSchool
import kotlin.math.cos

/**
 * Anatomical procedural OpenGL ES 3.0 renderer for all 13 aquatic body plans and 8 natural surface patterns.
 * Draws back-to-front sorted creatures with spinal undulation, animated fins, realistic countershading,
 * soft cast shadow offset, subtle bioluminescence, and depth-based atmospheric fog.
 */
class FishRenderer {

    private var program = 0
    private var vao = 0
    private var vbo = 0

    private var uAspect = -1
    private var uTime = -1
    private var uPosRotScale = -1 // x, y, heading, size
    private var uAnimMeta = -1    // swimPhase, tailAmp, depthZ, colorSeed
    private var uShapeMeta = -1   // bodyPlan, patternType, aspectRatio, flipY
    private var uPrimary = -1
    private var uSecondary = -1
    private var uAccent = -1
    private var uFinColor = -1
    private var uWaterFogColor = -1
    private var uRayColor = -1
    private var uEnvParams = -1   // fogDensity, glowIntensity, audioPulse, isShadowPass

    fun init() {
        program = ShaderUtils.createProgram(VERTEX_SHADER, FRAGMENT_SHADER)
        if (program == 0) return

        uAspect = GLES30.glGetUniformLocation(program, "uAspect")
        uTime = GLES30.glGetUniformLocation(program, "uTime")
        uPosRotScale = GLES30.glGetUniformLocation(program, "uPosRotScale")
        uAnimMeta = GLES30.glGetUniformLocation(program, "uAnimMeta")
        uShapeMeta = GLES30.glGetUniformLocation(program, "uShapeMeta")
        uPrimary = GLES30.glGetUniformLocation(program, "uPrimary")
        uSecondary = GLES30.glGetUniformLocation(program, "uSecondary")
        uAccent = GLES30.glGetUniformLocation(program, "uAccent")
        uFinColor = GLES30.glGetUniformLocation(program, "uFinColor")
        uWaterFogColor = GLES30.glGetUniformLocation(program, "uWaterFogColor")
        uRayColor = GLES30.glGetUniformLocation(program, "uRayColor")
        uEnvParams = GLES30.glGetUniformLocation(program, "uEnvParams")

        // Unit quad in [-1, 1] x [-1, 1]
        val quad = floatArrayOf(
            -1f, -1f,
            1f, -1f,
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
        shaderQuality: Int,
        school: FishSchool
    ) {
        if (program == 0 || school.activeCount <= 0) return

        GLES30.glUseProgram(program)
        GLES30.glUniform1f(uAspect, aspect)
        GLES30.glUniform1f(uTime, time)
        GLES30.glUniform3f(
            uWaterFogColor,
            (world.midColor.r + world.deepColor.r) * 0.5f,
            (world.midColor.g + world.deepColor.g) * 0.5f,
            (world.midColor.b + world.deepColor.b) * 0.5f
        )
        GLES30.glUniform3f(uRayColor, world.rayColor.r, world.rayColor.g, world.rayColor.b)

        val audioPulse = (audio.energy * 0.55f + audio.bass * 0.45f) * audioStrength * world.audioReactivityScale
        val drawShadows = shaderQuality >= 1

        GLES30.glBindVertexArray(vao)
        val count = school.activeCount
        for (idx in 0 until count) {
            val fish = school.pool[school.depthOrder[idx]]
            if (!fish.active) continue
            val sp = fish.species ?: continue

            val depthScale = 0.56f + fish.z * 0.64f
            val worldSize = fish.size * depthScale
            val isUpright = sp.bodyPlan == BodyPlan.JELLYFISH_BELL || sp.bodyPlan == BodyPlan.SEAHORSE
            val flipY = if (!isUpright && cos(fish.heading) < 0f) -1.0f else 1.0f

            GLES30.glUniform4f(
                uAnimMeta,
                fish.swimPhase,
                fish.tailAmp,
                fish.z,
                fish.colorSeed
            )
            GLES30.glUniform4f(
                uShapeMeta,
                sp.bodyPlan.shaderId.toFloat(),
                sp.pattern.shaderId.toFloat(),
                sp.aspectRatio,
                flipY
            )
            GLES30.glUniform3f(uPrimary, sp.primaryColor.r, sp.primaryColor.g, sp.primaryColor.b)
            GLES30.glUniform3f(uSecondary, sp.secondaryColor.r, sp.secondaryColor.g, sp.secondaryColor.b)
            GLES30.glUniform3f(uAccent, sp.accentColor.r, sp.accentColor.g, sp.accentColor.b)
            GLES30.glUniform3f(uFinColor, sp.finColor.r, sp.finColor.g, sp.finColor.b)

            // Soft subtle shadow offset beneath larger / closer creatures
            if (drawShadows && fish.z > 0.35f) {
                GLES30.glUniform4f(
                    uPosRotScale,
                    fish.x + 0.018f * depthScale,
                    fish.y - 0.032f * depthScale,
                    fish.heading,
                    worldSize * 0.96f
                )
                GLES30.glUniform4f(uEnvParams, world.fogDensity, 0f, audioPulse, 1.0f)
                GLES30.glDrawArrays(GLES30.GL_TRIANGLE_STRIP, 0, 4)
            }

            // Main creature body pass
            GLES30.glUniform4f(
                uPosRotScale,
                fish.x,
                fish.y,
                fish.heading,
                worldSize
            )
            val totalGlow = (sp.glowIntensity + world.bioluminescence * sp.glowIntensity * 0.5f).coerceIn(0f, 1.5f)
            GLES30.glUniform4f(uEnvParams, world.fogDensity, totalGlow, audioPulse, 0.0f)
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

            layout(location = 0) in vec2 aPos;

            uniform float uAspect;
            uniform vec4 uPosRotScale; // x, y, heading, size
            uniform vec4 uShapeMeta;   // bodyPlan, patternType, aspectRatio, flipY

            out vec2 vLocal; // local creature space: +x nose, -x tail, +y dorsal, -y ventral

            void main() {
                vLocal = vec2(aPos.x, aPos.y * uShapeMeta.w);
                float ar = clamp(uShapeMeta.z, 0.7, 4.6);
                float halfLen = uPosRotScale.w * (0.72 + ar * 0.24);
                float halfWid = uPosRotScale.w * (0.76 + (1.0 / ar) * 0.42);

                vec2 scaled = vec2(aPos.x * halfLen, aPos.y * halfWid);
                float c = cos(uPosRotScale.z);
                float s = sin(uPosRotScale.z);
                vec2 rot = vec2(scaled.x * c - scaled.y * s, scaled.x * s + scaled.y * c);
                vec2 world = uPosRotScale.xy + rot;

                gl_Position = vec4(world.x / uAspect, world.y, 0.0, 1.0);
            }
        """

        private const val FRAGMENT_SHADER = """#version 300 es
            precision highp float;
            precision highp int;

            in vec2 vLocal;
            out vec4 fragColor;

            uniform float uTime;
            uniform vec4 uAnimMeta;  // swimPhase, tailAmp, depthZ, colorSeed
            uniform vec4 uShapeMeta; // bodyPlan, patternType, aspectRatio, flipY
            uniform vec3 uPrimary;
            uniform vec3 uSecondary;
            uniform vec3 uAccent;
            uniform vec3 uFinColor;
            uniform vec3 uWaterFogColor;
            uniform vec3 uRayColor;
            uniform vec4 uEnvParams; // fogDensity, glowIntensity, audioPulse, isShadowPass

            float hash(vec2 p) {
                return fract(sin(dot(p, vec2(127.1, 311.7))) * 43758.5453);
            }

            float vnoise(vec2 p) {
                vec2 i = floor(p);
                vec2 f = fract(p);
                f = f * f * (3.0 - 2.0 * f);
                float a = hash(i);
                float b = hash(i + vec2(1.0, 0.0));
                float c = hash(i + vec2(0.0, 1.0));
                float d = hash(i + vec2(1.0, 1.0));
                return mix(mix(a, b, f.x), mix(c, d, f.x), f.y);
            }

            void main() {
                int bodyPlan = int(uShapeMeta.x + 0.5);
                int pattern = int(uShapeMeta.y + 0.5);
                float phase = uAnimMeta.x;
                float tailAmp = uAnimMeta.y;
                float depthZ = uAnimMeta.z;
                float seed = uAnimMeta.w;
                bool isShadow = uEnvParams.w > 0.5;

                // Spinal undulation: stiffer near nose (+x), sinuous toward tail (-x)
                float flexWeight = smoothstep(0.55, -0.95, vLocal.x);
                float waveFreq = (bodyPlan == 7) ? 9.0 : ((bodyPlan == 5) ? 4.5 : 5.4);
                float waveAmp = (bodyPlan == 7) ? 0.24 : ((bodyPlan == 8 || bodyPlan == 9 || bodyPlan == 12) ? 0.05 : 0.16);
                float spineOffset = sin(phase - vLocal.x * waveFreq) * waveAmp * tailAmp * flexWeight;

                vec2 p = vec2(vLocal.x, vLocal.y - spineOffset);

                float bodyMask = 0.0;
                float finMask = 0.0;
                float eyePresence = 1.0;
                vec2 eyePos = vec2(0.46, 0.07);
                float eyeRadius = 0.042;
                float bodyThickness = 0.0; // 0 at edge, 1 at center ridge for 3D shading

                if (bodyPlan == 0) {
                    // CARP_FUSIFORM (Koi, Carp, Trout, Bass, Parrotfish)
                    float t = clamp((p.x + 0.52) / 1.24, 0.0, 1.0); // 0 tail base -> 1 nose
                    float halfH = 0.27 * sin(pow(t, 0.68) * 3.14159);
                    if (p.x > -0.52 && p.x < 0.72) {
                        float d = abs(p.y) - halfH;
                        bodyMask = smoothstep(0.032, -0.012, d);
                        bodyThickness = clamp(1.0 - abs(p.y) / max(halfH, 0.01), 0.0, 1.0);
                    }
                    // Forked graceful caudal tail
                    if (p.x <= -0.44 && p.x > -0.96) {
                        float tt = (-0.44 - p.x) / 0.52;
                        float tailSpan = mix(0.06, 0.34, tt);
                        float forkNotch = smoothstep(0.28, 0.98, tt) * 0.14;
                        float outer = smoothstep(tailSpan + 0.03, tailSpan - 0.02, abs(p.y));
                        float inner = smoothstep(forkNotch - 0.02, forkNotch + 0.04, abs(p.y));
                        finMask = max(finMask, outer * inner);
                    }
                    // Swept pectoral & dorsal fins
                    float pec = smoothstep(0.03, -0.01, length(vec2((p.x - 0.12) * 2.8, (p.y + 0.24 - sin(phase) * 0.03) * 3.2)) - 0.36);
                    float dorsal = smoothstep(0.03, -0.01, abs(p.y - (halfH + 0.07 * sin(clamp((p.x + 0.25) * 2.2, 0.0, 3.14159)))) - 0.06)
                                 * step(-0.28, p.x) * step(p.x, 0.22);
                    finMask = max(finMask, max(pec * 0.85, dorsal * 0.75));
                    eyePos = vec2(0.50, 0.065);
                    eyeRadius = 0.038;

                } else if (bodyPlan == 1) {
                    // SLENDER_SCHOOLING (Tetras, Danio, Minnow, Anthias, Mackerel)
                    float t = clamp((p.x + 0.56) / 1.32, 0.0, 1.0);
                    float halfH = 0.21 * sin(pow(t, 0.72) * 3.14159);
                    if (p.x > -0.56 && p.x < 0.76) {
                        bodyMask = smoothstep(0.028, -0.01, abs(p.y) - halfH);
                        bodyThickness = clamp(1.0 - abs(p.y) / max(halfH, 0.01), 0.0, 1.0);
                    }
                    if (p.x <= -0.48 && p.x > -0.96) {
                        float tt = (-0.48 - p.x) / 0.48;
                        float span = mix(0.04, 0.28, tt);
                        float notch = smoothstep(0.25, 0.98, tt) * 0.13;
                        finMask = max(finMask, smoothstep(span + 0.025, span - 0.015, abs(p.y)) *
                                               smoothstep(notch - 0.02, notch + 0.03, abs(p.y)));
                    }
                    float dorsal = smoothstep(0.025, -0.01, length(vec2((p.x + 0.05) * 3.2, (p.y - 0.19) * 3.8)) - 0.28);
                    finMask = max(finMask, dorsal * 0.75);
                    eyePos = vec2(0.54, 0.045);
                    eyeRadius = 0.040;

                } else if (bodyPlan == 2) {
                    // DEEP_DISC (Angelfish, Discus, Tangs, Butterflyfish, Moorish Idol)
                    float t = clamp((p.x + 0.48) / 1.18, 0.0, 1.0);
                    float halfH = 0.46 * sin(pow(t, 0.65) * 3.14159);
                    if (p.x > -0.48 && p.x < 0.70) {
                        bodyMask = smoothstep(0.032, -0.012, abs(p.y) - halfH);
                        bodyThickness = clamp(1.0 - abs(p.y) / max(halfH, 0.01), 0.0, 1.0);
                    }
                    // Trailing upper/lower sail fins
                    float sailX = clamp((-p.x + 0.12) / 0.75, 0.0, 1.0);
                    float sailSpan = mix(0.26, 0.76, sin(sailX * 2.2));
                    float sails = smoothstep(sailSpan + 0.04, sailSpan - 0.02, abs(p.y)) * step(-0.62, p.x) * step(p.x, 0.14);
                    // Compact caudal fin
                    if (p.x <= -0.42 && p.x > -0.88) {
                        float tt = (-0.42 - p.x) / 0.46;
                        float span = mix(0.08, 0.28, tt);
                        finMask = max(finMask, smoothstep(span + 0.025, span - 0.015, abs(p.y)));
                    }
                    finMask = max(finMask, sails * 0.85);
                    eyePos = vec2(0.42, 0.09);
                    eyeRadius = 0.042;

                } else if (bodyPlan == 3) {
                    // FLOWING_FIN (Betta, Goldfish, Lionfish, Mandarin)
                    float t = clamp((p.x + 0.42) / 1.12, 0.0, 1.0);
                    float halfH = 0.26 * sin(pow(t, 0.68) * 3.14159);
                    if (p.x > -0.42 && p.x < 0.70) {
                        bodyMask = smoothstep(0.03, -0.012, abs(p.y) - halfH);
                        bodyThickness = clamp(1.0 - abs(p.y) / max(halfH, 0.01), 0.0, 1.0);
                    }
                    // Lush billowing veil tail & dorsal/anal curtains
                    float ruffle = sin(p.y * 18.0 + phase * 1.4) * 0.04;
                    if (p.x <= -0.26 && p.x > -0.98) {
                        float tt = (-0.26 - p.x) / 0.72;
                        float span = mix(0.14, 0.66, pow(tt, 0.65)) + ruffle;
                        float edge = smoothstep(span + 0.04, span - 0.02, abs(p.y)) * smoothstep(1.0, 0.84, tt);
                        finMask = max(finMask, edge * 0.88);
                    }
                    float topVeil = smoothstep(0.48 + ruffle, 0.40, abs(p.y - 0.14)) * step(-0.55, p.x) * step(p.x, 0.18);
                    finMask = max(finMask, topVeil * 0.72);
                    eyePos = vec2(0.46, 0.06);
                    eyeRadius = 0.040;

                } else if (bodyPlan == 4) {
                    // REEF_OVAL (Clownfish, Damselfish, Triggerfish, Pufferfish, Boxfish)
                    float t = clamp((p.x + 0.50) / 1.22, 0.0, 1.0);
                    float halfH = 0.34 * sin(pow(t, 0.66) * 3.14159);
                    if (p.x > -0.50 && p.x < 0.72) {
                        bodyMask = smoothstep(0.03, -0.012, abs(p.y) - halfH);
                        bodyThickness = clamp(1.0 - abs(p.y) / max(halfH, 0.01), 0.0, 1.0);
                    }
                    if (p.x <= -0.44 && p.x > -0.90) {
                        float tt = (-0.44 - p.x) / 0.46;
                        float span = mix(0.08, 0.29, pow(tt, 0.75)) * smoothstep(1.02, 0.86, tt);
                        finMask = max(finMask, smoothstep(span + 0.03, span - 0.015, abs(p.y)));
                    }
                    float dorsal = smoothstep(0.03, -0.01, length(vec2((p.x + 0.04) * 2.0, (p.y - 0.30) * 3.5)) - 0.42);
                    float pec = smoothstep(0.03, -0.01, length(vec2((p.x - 0.06) * 2.8, (p.y + 0.16) * 3.0)) - 0.30);
                    finMask = max(finMask, max(dorsal * 0.8, pec * 0.75));
                    eyePos = vec2(0.46, 0.075);
                    eyeRadius = 0.042;

                } else if (bodyPlan == 5) {
                    // FLAT_RAY (Manta Ray, Eagle Ray, Stingray) — top-down diamond wings + whip tail
                    float wingFlap = sin(phase - abs(p.y) * 3.5) * 0.06 * abs(p.y);
                    vec2 rp = vec2(p.x + wingFlap, p.y);
                    float wingSpan = smoothstep(0.68, 0.05, rp.x) * smoothstep(-0.46, 0.10, rp.x) * 0.88;
                    float diamond = abs(rp.y) - wingSpan;
                    bodyMask = smoothstep(0.04, -0.02, diamond) * step(-0.44, rp.x) * step(rp.x, 0.68);
                    bodyThickness = clamp(1.0 - abs(rp.y) / max(wingSpan, 0.05), 0.0, 1.0);
                    // Graceful slender whip tail
                    float tailWhip = abs(p.y - sin(phase - p.x * 5.0) * 0.05) - mix(0.038, 0.008, clamp((-0.36 - p.x) / 0.62, 0.0, 1.0));
                    float tail = smoothstep(0.02, -0.005, tailWhip) * step(-0.98, p.x) * step(p.x, -0.34);
                    bodyMask = max(bodyMask, tail);
                    eyePos = vec2(0.44, 0.14);
                    eyeRadius = 0.030;

                } else if (bodyPlan == 6) {
                    // SHARK_PELAGIC (Sharks, Tuna, Barracuda, Swordfish)
                    float t = clamp((p.x + 0.62) / 1.46, 0.0, 1.0);
                    float halfH = 0.21 * sin(pow(t, 0.62) * 3.14159);
                    if (p.x > -0.62 && p.x < 0.84) {
                        bodyMask = smoothstep(0.028, -0.01, abs(p.y) - halfH);
                        bodyThickness = clamp(1.0 - abs(p.y) / max(halfH, 0.01), 0.0, 1.0);
                    }
                    // Iconic swept shark dorsal fin
                    float dx = p.x - 0.04;
                    float dy = p.y - 0.16;
                    float dorsal = step(0.0, dy) * step(dy, 0.34) * smoothstep(0.03, -0.01, abs(dx + dy * 0.55) - mix(0.16, 0.02, dy / 0.34));
                    // Swept pectoral fin
                    float pdy = -p.y - 0.12;
                    float pec = step(0.0, pdy) * step(pdy, 0.32) * smoothstep(0.03, -0.01, abs((p.x - 0.18) + pdy * 0.65) - mix(0.14, 0.02, pdy / 0.32));
                    // Crescent lunate tail fin
                    if (p.x <= -0.54 && p.x > -0.98) {
                        float tt = (-0.54 - p.x) / 0.44;
                        float upperBias = (p.y > 0.0) ? 1.15 : 0.88;
                        float span = mix(0.05, 0.42 * upperBias, tt);
                        float notch = smoothstep(0.18, 0.96, tt) * 0.20;
                        finMask = max(finMask, smoothstep(span + 0.025, span - 0.015, abs(p.y)) *
                                               smoothstep(notch - 0.02, notch + 0.03, abs(p.y)));
                    }
                    finMask = max(finMask, max(dorsal, pec));
                    eyePos = vec2(0.58, 0.055);
                    eyeRadius = 0.032;

                } else if (bodyPlan == 7) {
                    // ELONGATED_EEL (Oarfish, Gulper Eel, Viperfish, Catfish)
                    float t = clamp((p.x + 0.92) / 1.72, 0.0, 1.0);
                    float halfH = mix(0.02, 0.17, pow(t, 0.55)) * smoothstep(1.0, 0.86, t);
                    if (p.x > -0.92 && p.x < 0.80) {
                        bodyMask = smoothstep(0.028, -0.01, abs(p.y) - halfH);
                        bodyThickness = clamp(1.0 - abs(p.y) / max(halfH, 0.01), 0.0, 1.0);
                        // Continuous flowing dorsal ribbon fin
                        float crest = smoothstep(0.025, -0.01, abs(p.y - (halfH + 0.07)) - 0.055) * smoothstep(0.05, 0.2, t) * smoothstep(0.95, 0.78, t);
                        finMask = max(finMask, crest * 0.85);
                    }
                    eyePos = vec2(0.64, 0.04);
                    eyeRadius = 0.034;

                } else if (bodyPlan == 8) {
                    // JELLYFISH_BELL — pulsating translucent umbrella dome + trailing silk tentacles
                    // Here +x is upward (heading ~ pi/2)
                    float pulse = sin(phase) * 0.14;
                    float bellX = (p.x - 0.26) / (0.42 + pulse * 0.18);
                    float bellY = p.y / (0.52 - pulse * 0.16);
                    float dome = length(vec2(bellX, bellY));
                    float bell = smoothstep(1.04, 0.88, dome) * smoothstep(-0.18, 0.08, bellX);
                    bodyMask = bell * 0.78;
                    bodyThickness = clamp(1.0 - dome, 0.0, 1.0);
                    // 5 flowing silk tentacles trailing toward -x
                    if (p.x < 0.26 && p.x > -0.96) {
                        float tt = (0.26 - p.x) / 1.22;
                        for (int k = -2; k <= 2; k++) {
                            float fk = float(k);
                            float tentY = fk * 0.14 * (1.0 - tt * 0.35)
                                        + sin(tt * 8.5 - phase * 1.3 + fk * 1.4) * (0.04 + tt * 0.10);
                            float w = mix(0.032, 0.008, tt);
                            float tent = smoothstep(w + 0.015, w, abs(p.y - tentY)) * smoothstep(1.0, 0.72, tt);
                            finMask = max(finMask, tent * 0.72);
                        }
                    }
                    eyePresence = 0.0;

                } else if (bodyPlan == 9) {
                    // SEA_TURTLE — oval mosaic shell, head, sweeping front & rear flippers
                    float shellD = length(vec2((p.x + 0.06) / 0.46, p.y / 0.34));
                    bodyMask = smoothstep(1.04, 0.92, shellD);
                    bodyThickness = clamp(1.0 - shellD, 0.0, 1.0);
                    // Head extending forward
                    float headD = length(vec2((p.x - 0.50) / 0.18, p.y / 0.12));
                    bodyMask = max(bodyMask, smoothstep(1.05, 0.90, headD));
                    // Sweeping front flippers with gentle stroke
                    float stroke = sin(phase) * 0.22;
                    float fy = abs(p.y) - 0.26;
                    float frontFlip = step(0.0, fy) * step(fy, 0.46) *
                        smoothstep(0.03, -0.01, abs((p.x - 0.16) + fy * (0.55 + stroke)) - mix(0.14, 0.04, fy / 0.46));
                    float rearFlip = smoothstep(0.03, -0.01, length(vec2((p.x + 0.54) * 3.8, (abs(p.y) - 0.18) * 4.5)) - 0.46);
                    finMask = max(frontFlip, rearFlip);
                    eyePos = vec2(0.58, 0.055);
                    eyeRadius = 0.028;

                } else if (bodyPlan == 10) {
                    // CEPHALOPOD (Octopus, Deep Sea Squid, Cuttlefish)
                    float mantle = length(vec2((p.x - 0.24) / 0.48, p.y / 0.26));
                    bodyMask = smoothstep(1.04, 0.90, mantle);
                    bodyThickness = clamp(1.0 - mantle, 0.0, 1.0);
                    // Trailing undulating arms toward -x
                    if (p.x < 0.0 && p.x > -0.96) {
                        float tt = (-p.x) / 0.96;
                        for (int k = -2; k <= 2; k++) {
                            float fk = float(k);
                            float armY = fk * 0.095 * (1.0 + tt * 0.5)
                                       + sin(tt * 7.5 - phase * 1.4 + fk * 1.1) * (0.04 + tt * 0.08);
                            float w = mix(0.055, 0.012, tt);
                            finMask = max(finMask, smoothstep(w + 0.02, w, abs(p.y - armY)) * smoothstep(1.0, 0.80, tt));
                        }
                    }
                    eyePos = vec2(0.08, 0.11);
                    eyeRadius = 0.042;

                } else if (bodyPlan == 11) {
                    // SEAHORSE — upright S-curve silhouette
                    float sy = p.y; // vertical axis
                    float sx = p.x;
                    float curveX = sin(sy * 3.8 + 0.6) * 0.18;
                    float bodyW = mix(0.05, 0.20, smoothstep(-0.75, 0.15, sy)) * smoothstep(0.82, 0.40, sy);
                    bodyMask = smoothstep(bodyW + 0.03, bodyW - 0.01, abs(sx - curveX)) * step(-0.78, sy) * step(sy, 0.72);
                    float snout = smoothstep(0.03, -0.01, abs(sy - 0.56) - 0.045) * step(0.10, sx) * step(sx, 0.46);
                    bodyMask = max(bodyMask, snout);
                    bodyThickness = 0.75;
                    float flutter = smoothstep(0.03, -0.01, length(vec2((sx + 0.22) * 4.5, (sy - 0.02) * 3.8)) - 0.36);
                    finMask = flutter * 0.80;
                    eyePos = vec2(0.16, 0.58);
                    eyeRadius = 0.036;

                } else {
                    // BENTHIC_CRUSTACEAN (Shrimp, Crab, Giant Isopod, Snail)
                    float shell = length(vec2(p.x / 0.56, p.y / 0.30));
                    bodyMask = smoothstep(1.04, 0.90, shell);
                    bodyThickness = clamp(1.0 - shell, 0.0, 1.0);
                    float tailFan = smoothstep(0.03, -0.01, length(vec2((p.x + 0.60) * 3.2, p.y * 3.4)) - 0.42);
                    finMask = tailFan * 0.82;
                    eyePos = vec2(0.42, 0.10);
                    eyeRadius = 0.032;
                }

                float totalAlpha = max(bodyMask, finMask * 0.78);
                if (totalAlpha < 0.02) discard;

                // Soft underwater cast shadow pass
                if (isShadow) {
                    fragColor = vec4(uWaterFogColor * 0.22, totalAlpha * 0.24 * depthZ);
                    return;
                }

                // ═══════════════════ SURFACE PATTERNING ═══════════════════
                vec3 col = mix(uSecondary, uPrimary, smoothstep(-0.32, 0.26, p.y));
                vec2 patUv = vec2(p.x * 4.2 + seed * 9.0, p.y * 4.2 + seed * 5.0);

                if (pattern == 1) {
                    // KOI_CALICO: organic hand-painted ink & vermilion patches
                    float n1 = vnoise(patUv * 1.15);
                    float n2 = vnoise(patUv * 1.85 + vec2(4.2, 1.7));
                    col = uPrimary;
                    col = mix(col, uSecondary, smoothstep(0.46, 0.62, n1));
                    col = mix(col, uAccent, smoothstep(0.66, 0.78, n2) * 0.88);
                } else if (pattern == 2) {
                    // VERTICAL_BARS (Clownfish, Moorish Idol, Tiger Barb, Perch, Lionfish)
                    float bar = sin(p.x * 16.5 + seed * 1.2);
                    float barMask = smoothstep(0.18, 0.48, bar);
                    float edgeBorder = smoothstep(0.05, 0.18, bar) * (1.0 - barMask);
                    col = mix(uPrimary, uSecondary, barMask);
                    col = mix(col, uAccent, edgeBorder * 0.65);
                } else if (pattern == 3) {
                    // HORIZONTAL_STRIPE (Neon/Cardinal Tetra, Zebra Danio, Surgeonfish)
                    float stripe = smoothstep(0.10, 0.02, abs(p.y - 0.02));
                    float lowerBand = smoothstep(-0.02, -0.14, p.y) * smoothstep(0.25, -0.35, p.x);
                    col = mix(col, uSecondary, stripe * 0.92);
                    col = mix(col, uAccent, lowerBand * 0.72);
                } else if (pattern == 4) {
                    // SPOTTED (Trout, Whale Shark, Eagle Ray, Triggerfish, Pufferfish)
                    vec2 cell = fract(patUv * 1.7) - 0.5;
                    float spot = smoothstep(0.26, 0.12, length(cell));
                    col = mix(col, uSecondary, spot * 0.75 * smoothstep(-0.22, 0.12, p.y));
                } else if (pattern == 5) {
                    // BIOLUMINESCENT (Lanternfish, Anglerfish, Jellyfish, Deep Sea)
                    float pulse = 0.65 + 0.35 * sin(uTime * 2.2 + seed * 12.0 + p.x * 5.0);
                    float dots = smoothstep(0.22, 0.05, length(vec2(fract(p.x * 6.5) - 0.5, (p.y + 0.08) * 5.5)));
                    float rimGlow = smoothstep(0.55, 0.0, bodyThickness) * 0.55;
                    col = mix(col, uSecondary, rimGlow);
                    col += uAccent * (dots * 0.95 + rimGlow * 0.5) * pulse * (0.7 + uEnvParams.z * 0.65);
                } else if (pattern == 6) {
                    // MANDARIN_SWIRL (Mandarin Fish, Discus, Cuttlefish, Wrasse)
                    float sw = sin(p.x * 12.0 + sin(p.y * 10.0 + seed * 6.0) * 1.6);
                    col = mix(uPrimary, uSecondary, smoothstep(-0.2, 0.45, sw));
                    col = mix(col, uAccent, smoothstep(0.65, 0.92, sw) * 0.65);
                } else if (pattern == 7) {
                    // MOTTLED (Sea Turtle, Octopus, Oscar, Catfish)
                    float m = vnoise(patUv * 1.9);
                    col = mix(uPrimary, uSecondary, smoothstep(0.35, 0.72, m));
                    col = mix(col, uAccent, smoothstep(0.70, 0.88, m) * 0.45);
                }

                // Blend translucent fins separately from solid muscular body
                vec3 surfaceCol = mix(uFinColor, col, smoothstep(0.05, 0.55, bodyMask));

                // Subtle anatomical dorsal-to-ventral 3D shading + iridescent scale highlight
                float dorsalLight = 0.68 + 0.36 * smoothstep(-0.35, 0.38, p.y);
                float ridgeSpec = pow(bodyThickness, 2.2) * 0.20 * smoothstep(-0.1, 0.45, p.y);
                surfaceCol = surfaceCol * dorsalLight + uRayColor * ridgeSpec;

                // Subtle natural eye (small, biologically proportionate, never cartoonish)
                if (eyePresence > 0.5 && bodyMask > 0.4) {
                    float eyeDist = length(p - eyePos);
                    if (eyeDist < eyeRadius) {
                        vec3 iris = mix(vec3(0.06, 0.08, 0.10), uAccent, 0.18);
                        float pupil = smoothstep(eyeRadius * 0.62, eyeRadius * 0.38, eyeDist);
                        float glint = smoothstep(eyeRadius * 0.32, 0.0, length(p - (eyePos + vec2(0.01, 0.012))));
                        surfaceCol = mix(iris, vec3(0.02, 0.03, 0.04), pupil) + vec3(0.72) * glint;
                    }
                }

                // Bioluminescent emission boost in dark / exotic worlds
                float glow = uEnvParams.y;
                if (glow > 0.02) {
                    surfaceCol += mix(uSecondary, uAccent, 0.5) * glow * (0.28 + uEnvParams.z * 0.32);
                }

                // Water depth atmospheric perspective fog (farther fish soften into the water)
                float fogAmount = clamp((1.0 - depthZ) * (0.42 + uEnvParams.x * 0.52) * (1.0 - glow * 0.35), 0.04, 0.80);
                vec3 finalCol = mix(surfaceCol, uWaterFogColor * 1.15, fogAmount);

                // Translucent fins vs opaque body alpha
                float finalAlpha = mix(finMask * 0.68, 0.96, smoothstep(0.1, 0.6, bodyMask));
                if (bodyPlan == 8) {
                    finalAlpha *= 0.82; // ethereal jellyfish translucency
                }
                finalAlpha *= mix(0.62, 1.0, depthZ);

                fragColor = vec4(finalCol, clamp(finalAlpha, 0.0, 0.98));
            }
        """
    }
}
