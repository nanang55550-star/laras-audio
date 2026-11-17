package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val ColorElevatedHighest = androidx.compose.ui.graphics.Color(0xFF2E2E3D)

private val LarasDarkColorScheme = darkColorScheme(
    primary = WarmGold,
    onPrimary = OnGoldDark,
    primaryContainer = DeepGold.copy(alpha = 0.24f),
    onPrimaryContainer = SoftGold,
    secondary = GoldMuted,
    onSecondary = IvoryText,
    secondaryContainer = ObsidianElevated,
    onSecondaryContainer = SoftGold,
    tertiary = BadgeWav,
    onTertiary = OnGoldDark,
    background = ObsidianVoid,
    onBackground = IvoryText,
    surface = ObsidianSurface,
    onSurface = IvoryText,
    surfaceVariant = ObsidianCard,
    onSurfaceVariant = WarmGray,
    surfaceContainerLowest = ObsidianVoid,
    surfaceContainerLow = ObsidianSurface,
    surfaceContainer = ObsidianCard,
    surfaceContainerHigh = ObsidianElevated,
    surfaceContainerHighest = ColorElevatedHighest,
    outline = SubtleOutline,
    outlineVariant = SubtleOutline.copy(alpha = 0.55f),
    error = ErrorRose,
    onError = OnGoldDark
)

val LarasShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

@Composable
fun LarasAudioTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LarasDarkColorScheme,
        typography = LarasTypography,
        shapes = LarasShapes,
        content = content
    )
}
