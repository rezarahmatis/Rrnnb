package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.model.AudioSpectrum
import com.example.model.BodyPlan
import com.example.model.FishSpecies
import com.example.model.PatternType
import com.example.model.RgbColor
import com.example.model.VegetationStyle
import com.example.model.WorldEnvironment
import com.example.ui.theme.BioluminescentCyan
import com.example.ui.theme.PelagicGlass
import com.example.ui.theme.SubtleGlassStroke
import kotlin.math.sin

fun RgbColor.toComposeColor(alpha: Float = 1f): Color =
    Color(r.coerceIn(0f, 1f), g.coerceIn(0f, 1f), b.coerceIn(0f, 1f), alpha)

fun formatDurationMs(ms: Long): String {
    val totalSec = (ms / 1000L).coerceAtLeast(0L)
    val mins = totalSec / 60L
    val secs = totalSec % 60L
    return "%d:%02d".format(mins, secs)
}

@Composable
fun GlassPillSurface(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 24.dp,
    containerColor: Color = PelagicGlass,
    borderColor: Color = SubtleGlassStroke,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(cornerRadius),
        color = containerColor,
        border = BorderStroke(1.dp, borderColor),
        content = content
    )
}

/**
 * Offline procedural art thumbnail representing an aquatic world's distinct water palette,
 * diagonal light rays, caustics, bioluminescent motes, and signature flora silhouette.
 */
@Composable
fun WorldArtThumbnail(
    world: WorldEnvironment,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // 3-stop water column gradient
        drawRect(
            brush = Brush.verticalGradient(
                colorStops = arrayOf(
                    0.0f to world.surfaceColor.toComposeColor(),
                    0.52f to world.midColor.toComposeColor(),
                    1.0f to world.deepColor.toComposeColor()
                )
            )
        )

        // Volumetric diagonal rays
        if (world.rayIntensity > 0.1f) {
            val rayCol = world.rayColor.toComposeColor(alpha = (world.rayIntensity * 0.28f).coerceAtMost(0.32f))
            val slant = world.rayAngle * w * 0.6f
            for (i in 0..2) {
                val startX = w * (0.18f + i * 0.28f)
                val beamW = w * (0.14f + (i % 2) * 0.06f)
                val path = Path().apply {
                    moveTo(startX, 0f)
                    lineTo(startX + beamW, 0f)
                    lineTo(startX + beamW + slant, h * 0.85f)
                    lineTo(startX + slant * 0.8f, h * 0.85f)
                    close()
                }
                drawPath(
                    path = path,
                    brush = Brush.verticalGradient(
                        colors = listOf(rayCol, Color.Transparent),
                        startY = 0f,
                        endY = h * 0.88f
                    )
                )
            }
        }

        // Top surface shimmer band
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    world.causticColor.toComposeColor(0.34f * world.causticIntensity),
                    Color.Transparent
                ),
                startY = 0f,
                endY = h * 0.24f
            )
        )

        // Signature flora / architecture silhouette
        drawWorldFloraPreview(world, w, h)

        // Bioluminescent or suspended water motes
        val moteCol = world.particleColor.toComposeColor(
            alpha = (0.35f + world.bioluminescence * 0.55f).coerceIn(0.25f, 0.9f)
        )
        val seed = world.id.hashCode()
        for (i in 0..8) {
            val px = ((seed * 31 + i * 73) and 0xFF) / 255f * w
            val py = ((seed * 17 + i * 109) and 0xFF) / 255f * h * 0.85f
            val r = 1.4f + (i % 3) * 1.1f
            drawCircle(color = moteCol, radius = r, center = Offset(px, py))
        }
    }
}

private fun DrawScope.drawWorldFloraPreview(world: WorldEnvironment, w: Float, h: Float) {
    val floraCol = world.floraColor.toComposeColor(0.82f)
    when (world.vegetationStyle) {
        VegetationStyle.LOTUS_AND_REEDS -> {
            for (i in 0..4) {
                val x = w * (0.12f + i * 0.19f)
                val topY = h * (0.42f + (i % 2) * 0.14f)
                drawLine(floraCol, Offset(x, h), Offset(x + 4f, topY), strokeWidth = 3f, cap = StrokeCap.Round)
                if (i % 2 == 0) {
                    drawOval(floraCol, Offset(x - 10f, topY - 4f), Size(24f, 8f))
                }
            }
        }
        VegetationStyle.CORAL_GARDEN -> {
            for (i in 0..3) {
                val cx = w * (0.15f + i * 0.24f)
                val branchH = h * (0.28f + (i % 2) * 0.12f)
                drawLine(floraCol, Offset(cx, h), Offset(cx, h - branchH), strokeWidth = 5f, cap = StrokeCap.Round)
                drawLine(floraCol, Offset(cx, h - branchH * 0.5f), Offset(cx - 10f, h - branchH * 0.92f), strokeWidth = 4f, cap = StrokeCap.Round)
                drawLine(floraCol, Offset(cx, h - branchH * 0.45f), Offset(cx + 10f, h - branchH * 0.85f), strokeWidth = 4f, cap = StrokeCap.Round)
            }
        }
        VegetationStyle.GIANT_KELP, VegetationStyle.RIVER_GRASS, VegetationStyle.SEAGRASS_MEADOW -> {
            for (i in 0..6) {
                val baseX = w * (0.08f + i * 0.14f)
                val bladeH = h * (0.35f + (i % 3) * 0.18f)
                val path = Path().apply {
                    moveTo(baseX, h)
                    quadraticTo(baseX + 10f, h - bladeH * 0.5f, baseX - 5f, h - bladeH)
                }
                drawPath(path, floraCol, style = Stroke(width = 3.5f, cap = StrokeCap.Round))
            }
        }
        VegetationStyle.MANGROVE_ROOTS -> {
            for (i in 0..3) {
                val rx = w * (0.14f + i * 0.25f)
                val path = Path().apply {
                    moveTo(rx, 0f)
                    quadraticTo(rx + 14f, h * 0.45f, rx - 8f, h)
                }
                drawPath(path, floraCol, style = Stroke(width = 4.5f, cap = StrokeCap.Round))
            }
        }
        VegetationStyle.ICE_FORMATIONS, VegetationStyle.CRYSTAL_SPIRES -> {
            for (i in 0..3) {
                val cx = w * (0.15f + i * 0.24f)
                val tipY = h * (0.36f + (i % 2) * 0.16f)
                val crystal = Path().apply {
                    moveTo(cx - 14f, h)
                    lineTo(cx, tipY)
                    lineTo(cx + 14f, h)
                    close()
                }
                drawPath(crystal, floraCol)
            }
        }
        VegetationStyle.VOLCANIC_VENTS -> {
            val magma = Color(0xFFFF6E2E)
            for (i in 0..2) {
                val cx = w * (0.20f + i * 0.30f)
                val vent = Path().apply {
                    moveTo(cx - 18f, h)
                    lineTo(cx - 7f, h * 0.58f)
                    lineTo(cx + 7f, h * 0.58f)
                    lineTo(cx + 18f, h)
                    close()
                }
                drawPath(vent, Color(0xFF1A1113))
                drawLine(magma, Offset(cx, h), Offset(cx, h * 0.62f), strokeWidth = 3f, cap = StrokeCap.Round)
            }
        }
        VegetationStyle.SUNKEN_RUINS -> {
            for (i in 0..2) {
                val cx = w * (0.20f + i * 0.30f)
                val topY = h * (0.40f + (i % 2) * 0.14f)
                drawRect(floraCol, Offset(cx - 8f, topY), Size(16f, h - topY))
            }
        }
        VegetationStyle.NONE_ABYSSAL -> Unit
    }
}

/**
 * Clean natural vector preview of any aquatic species in the Fish Selector library.
 */
@Composable
fun FishSpeciesThumbnail(
    species: FishSpecies,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w * 0.52f
        val cy = h * 0.50f
        val primary = species.primaryColor.toComposeColor()
        val secondary = species.secondaryColor.toComposeColor()
        val accent = species.accentColor.toComposeColor()
        val finCol = species.finColor.toComposeColor(0.82f)

        // Subtle pelagic radial backing glow
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    if (species.glowIntensity > 0.3f) accent.copy(alpha = 0.25f) else primary.copy(alpha = 0.14f),
                    Color.Transparent
                ),
                center = Offset(cx, cy),
                radius = w * 0.48f
            ),
            radius = w * 0.48f,
            center = Offset(cx, cy)
        )

        when (species.bodyPlan) {
            BodyPlan.FLAT_RAY -> {
                val ray = Path().apply {
                    moveTo(w * 0.80f, cy)
                    lineTo(w * 0.48f, h * 0.14f)
                    lineTo(w * 0.28f, cy)
                    lineTo(w * 0.48f, h * 0.86f)
                    close()
                }
                drawPath(ray, Brush.linearGradient(listOf(primary, secondary)))
                drawLine(primary, Offset(w * 0.28f, cy), Offset(w * 0.08f, cy + 4f), strokeWidth = 3f, cap = StrokeCap.Round)
            }

            BodyPlan.JELLYFISH_BELL -> {
                drawArc(
                    brush = Brush.verticalGradient(listOf(primary, secondary.copy(alpha = 0.7f))),
                    startAngle = 180f,
                    sweepAngle = 180f,
                    useCenter = true,
                    topLeft = Offset(w * 0.24f, h * 0.16f),
                    size = Size(w * 0.52f, h * 0.42f)
                )
                for (k in -2..2) {
                    val tx = w * 0.50f + k * (w * 0.085f)
                    val path = Path().apply {
                        moveTo(tx, h * 0.38f)
                        quadraticTo(tx + 5f, h * 0.62f, tx - 4f, h * 0.88f)
                    }
                    drawPath(path, finCol, style = Stroke(width = 2.4f, cap = StrokeCap.Round))
                }
            }

            BodyPlan.SEA_TURTLE -> {
                // Flippers
                drawOval(finCol, Offset(w * 0.42f, h * 0.12f), Size(w * 0.16f, h * 0.32f))
                drawOval(finCol, Offset(w * 0.42f, h * 0.56f), Size(w * 0.16f, h * 0.32f))
                // Shell & head
                drawOval(Brush.linearGradient(listOf(primary, secondary)), Offset(w * 0.24f, h * 0.26f), Size(w * 0.44f, h * 0.48f))
                drawOval(finCol, Offset(w * 0.66f, h * 0.41f), Size(w * 0.18f, h * 0.18f))
            }

            BodyPlan.CEPHALOPOD -> {
                drawOval(Brush.horizontalGradient(listOf(secondary, primary)), Offset(w * 0.44f, h * 0.28f), Size(w * 0.42f, h * 0.44f))
                for (k in -1..1) {
                    val y0 = cy + k * (h * 0.12f)
                    val arm = Path().apply {
                        moveTo(w * 0.46f, y0)
                        quadraticTo(w * 0.28f, y0 + k * 6f, w * 0.10f, y0 - k * 3f)
                    }
                    drawPath(arm, finCol, style = Stroke(width = 3.2f, cap = StrokeCap.Round))
                }
            }

            BodyPlan.SEAHORSE -> {
                val path = Path().apply {
                    moveTo(w * 0.58f, h * 0.22f)
                    quadraticTo(w * 0.36f, h * 0.42f, w * 0.54f, h * 0.62f)
                    quadraticTo(w * 0.64f, h * 0.78f, w * 0.44f, h * 0.84f)
                }
                drawPath(path, primary, style = Stroke(width = 9f, cap = StrokeCap.Round))
                drawLine(primary, Offset(w * 0.58f, h * 0.24f), Offset(w * 0.76f, h * 0.26f), strokeWidth = 4f, cap = StrokeCap.Round)
            }

            BodyPlan.DEEP_DISC -> {
                val tail = Path().apply {
                    moveTo(w * 0.30f, cy)
                    lineTo(w * 0.12f, h * 0.30f)
                    lineTo(w * 0.12f, h * 0.70f)
                    close()
                }
                drawPath(tail, finCol)
                val disc = Path().apply {
                    moveTo(w * 0.82f, cy)
                    quadraticTo(w * 0.52f, h * 0.08f, w * 0.26f, cy)
                    quadraticTo(w * 0.52f, h * 0.92f, w * 0.82f, cy)
                    close()
                }
                drawPath(disc, Brush.verticalGradient(listOf(primary, secondary)))
                if (species.pattern == PatternType.VERTICAL_BARS) {
                    drawLine(secondary, Offset(w * 0.56f, h * 0.22f), Offset(w * 0.56f, h * 0.78f), strokeWidth = 4f)
                    drawLine(accent, Offset(w * 0.40f, h * 0.28f), Offset(w * 0.40f, h * 0.72f), strokeWidth = 3f)
                }
                drawCircle(Color(0xFF0A1015), radius = 2.6f, center = Offset(w * 0.69f, cy - 2f))
            }

            else -> {
                // Fusiform / Slender / Shark / Reef / Eel / Flowing Fin
                val bodyHeight = when (species.bodyPlan) {
                    BodyPlan.ELONGATED_EEL -> h * 0.20f
                    BodyPlan.SLENDER_SCHOOLING -> h * 0.28f
                    BodyPlan.SHARK_PELAGIC -> h * 0.30f
                    else -> h * 0.40f
                }
                val tailSpread = if (species.bodyPlan == BodyPlan.FLOWING_FIN) h * 0.34f else h * 0.22f
                val tail = Path().apply {
                    moveTo(w * 0.30f, cy)
                    lineTo(w * 0.08f, cy - tailSpread)
                    lineTo(w * 0.16f, cy)
                    lineTo(w * 0.08f, cy + tailSpread)
                    close()
                }
                drawPath(tail, finCol)

                if (species.bodyPlan == BodyPlan.SHARK_PELAGIC) {
                    val dorsal = Path().apply {
                        moveTo(w * 0.56f, cy - bodyHeight * 0.42f)
                        lineTo(w * 0.42f, h * 0.12f)
                        lineTo(w * 0.38f, cy - bodyHeight * 0.38f)
                        close()
                    }
                    drawPath(dorsal, finCol)
                }

                val bodyLeft = w * 0.24f
                val bodyW = w * 0.64f
                drawOval(
                    brush = Brush.verticalGradient(listOf(primary, secondary)),
                    topLeft = Offset(bodyLeft, cy - bodyHeight * 0.5f),
                    size = Size(bodyW, bodyHeight)
                )

                when (species.pattern) {
                    PatternType.HORIZONTAL_STRIPE -> {
                        drawLine(
                            color = secondary,
                            start = Offset(bodyLeft + 6f, cy),
                            end = Offset(bodyLeft + bodyW - 8f, cy),
                            strokeWidth = 3.2f,
                            cap = StrokeCap.Round
                        )
                    }
                    PatternType.VERTICAL_BARS -> {
                        for (b in 0..2) {
                            val bx = bodyLeft + bodyW * (0.28f + b * 0.22f)
                            drawLine(secondary, Offset(bx, cy - bodyHeight * 0.4f), Offset(bx, cy + bodyHeight * 0.4f), strokeWidth = 3.2f)
                        }
                    }
                    PatternType.KOI_CALICO -> {
                        drawCircle(secondary, radius = bodyHeight * 0.30f, center = Offset(bodyLeft + bodyW * 0.62f, cy - 2f))
                        drawCircle(accent, radius = bodyHeight * 0.20f, center = Offset(bodyLeft + bodyW * 0.38f, cy + 2f))
                    }
                    PatternType.BIOLUMINESCENT -> {
                        for (d in 0..3) {
                            drawCircle(
                                color = accent,
                                radius = 2.0f,
                                center = Offset(bodyLeft + bodyW * (0.25f + d * 0.16f), cy + bodyHeight * 0.12f)
                            )
                        }
                    }
                    PatternType.SPOTTED -> {
                        for (d in 0..3) {
                            drawCircle(
                                color = secondary,
                                radius = 2.2f,
                                center = Offset(bodyLeft + bodyW * (0.28f + d * 0.15f), cy - bodyHeight * 0.1f + (d % 2) * 4f)
                            )
                        }
                    }
                    else -> Unit
                }

                drawCircle(Color(0xFF0A1015), radius = 2.4f, center = Offset(bodyLeft + bodyW * 0.80f, cy - bodyHeight * 0.10f))
            }
        }
    }
}

/**
 * Minimal, quiet 3-bar audio harmonic indicator (bass / mid / treble).
 */
@Composable
fun AudioHarmonicIndicator(
    spectrum: AudioSpectrum,
    isPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    val bars = listOf(
        (if (isPlaying) 0.25f + spectrum.bass * 0.75f else 0.18f).coerceIn(0.15f, 1f),
        (if (isPlaying) 0.25f + spectrum.mid * 0.75f else 0.26f).coerceIn(0.15f, 1f),
        (if (isPlaying) 0.25f + spectrum.treble * 0.75f else 0.18f).coerceIn(0.15f, 1f)
    )
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        bars.forEach { level ->
            Box(
                modifier = Modifier
                    .width(2.5.dp)
                    .height((4 + level * 14).dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        if (isPlaying) BioluminescentCyan else BioluminescentCyan.copy(alpha = 0.38f)
                    )
            )
        }
    }
}
