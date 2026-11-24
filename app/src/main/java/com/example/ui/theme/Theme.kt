package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val PelagiaColorScheme = darkColorScheme(
    primary = BioluminescentCyan,
    onPrimary = AbyssBlack,
    primaryContainer = BioluminescentCyanMuted,
    onPrimaryContainer = PearlWhite,
    secondary = WarmCoralGold,
    onSecondary = AbyssBlack,
    secondaryContainer = PelagicCardElevated,
    onSecondaryContainer = PearlWhite,
    tertiary = SeafoamMint,
    onTertiary = AbyssBlack,
    background = AbyssBlack,
    onBackground = PearlWhite,
    surface = DeepTrenchSurface,
    onSurface = PearlWhite,
    surfaceVariant = PelagicCard,
    onSurfaceVariant = MistSilver,
    surfaceContainerHigh = PelagicCardElevated,
    outline = SubtleGlassStroke,
    outlineVariant = SlateMuted,
    error = SoftErrorRose,
    onError = AbyssBlack
)

private val PelagiaShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

@Composable
fun PelagiaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = PelagiaColorScheme,
        typography = PelagiaTypography,
        shapes = PelagiaShapes,
        content = content
    )
}
