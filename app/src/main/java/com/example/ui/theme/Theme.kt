package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.data.model.UserSettings

private fun buildLightColorScheme(accentPalette: String): ColorScheme {
    val (primary, onPrimary, primaryContainer, onPrimaryContainer) = when (accentPalette) {
        UserSettings.PALETTE_LAVENDER -> Quad(
            LavPalettePrimary, LavPaletteOnPrimary, LavPalettePrimaryContainer, LavPaletteOnPrimaryContainer
        )
        UserSettings.PALETTE_SAGE -> Quad(
            SagePalettePrimary, SagePaletteOnPrimary, SagePalettePrimaryContainer, SagePaletteOnPrimaryContainer
        )
        UserSettings.PALETTE_OCEAN -> Quad(
            OceanPalettePrimary, OceanPaletteOnPrimary, OceanPalettePrimaryContainer, OceanPaletteOnPrimaryContainer
        )
        else -> Quad(
            RosePrimary, RoseOnPrimary, RosePrimaryContainer, RoseOnPrimaryContainer
        )
    }

    val (secondary, onSecondary, secondaryContainer, onSecondaryContainer) = when (accentPalette) {
        UserSettings.PALETTE_LAVENDER -> Quad(
            LavPaletteSecondary, LavPaletteOnSecondary, LavPaletteSecondaryContainer, LavPaletteOnSecondaryContainer
        )
        UserSettings.PALETTE_SAGE -> Quad(
            SagePaletteSecondary, SagePaletteOnSecondary, SagePaletteSecondaryContainer, SagePaletteOnSecondaryContainer
        )
        UserSettings.PALETTE_OCEAN -> Quad(
            OceanPaletteSecondary, OceanPaletteOnSecondary, OceanPaletteSecondaryContainer, OceanPaletteOnSecondaryContainer
        )
        else -> Quad(
            TealSecondary, TealOnSecondary, TealSecondaryContainer, TealOnSecondaryContainer
        )
    }

    return lightColorScheme(
        primary = primary,
        onPrimary = onPrimary,
        primaryContainer = primaryContainer,
        onPrimaryContainer = onPrimaryContainer,
        secondary = secondary,
        onSecondary = onSecondary,
        secondaryContainer = secondaryContainer,
        onSecondaryContainer = onSecondaryContainer,
        tertiary = LavenderTertiary,
        onTertiary = LavenderOnTertiary,
        tertiaryContainer = LavenderTertiaryContainer,
        onTertiaryContainer = LavenderOnTertiaryContainer,
        background = LightBackground,
        onBackground = LightTextPrimary,
        surface = LightSurface,
        onSurface = LightTextPrimary,
        surfaceVariant = LightSurfaceVariant,
        onSurfaceVariant = LightTextSecondary,
        surfaceContainerLowest = LightSurface,
        surfaceContainerLow = LightSurfaceContainerLow,
        surfaceContainer = LightSurfaceContainer,
        surfaceContainerHigh = LightSurfaceContainerHigh,
        surfaceContainerHighest = LightSurfaceVariant,
        outline = LightOutline,
        error = LightError,
        onError = LightOnError,
        errorContainer = LightErrorContainer,
        onErrorContainer = LightOnErrorContainer
    )
}

private fun buildDarkColorScheme(accentPalette: String): ColorScheme {
    val (primary, onPrimary, primaryContainer, onPrimaryContainer) = when (accentPalette) {
        UserSettings.PALETTE_LAVENDER -> Quad(
            Color(0xFFCBB6F8), Color(0xFF2E1B52), Color(0xFF563D82), Color(0xFFEDE2FF)
        )
        UserSettings.PALETTE_SAGE -> Quad(
            Color(0xFF6DD5C9), Color(0xFF002924), Color(0xFF195F57), Color(0xFFC4F0EA)
        )
        UserSettings.PALETTE_OCEAN -> Quad(
            Color(0xFF67D4EE), Color(0xFF002833), Color(0xFF0B586E), Color(0xFFCFFAFE)
        )
        else -> Quad(
            ColorPeriodPredicted, RoseOnPrimaryContainer, Color(0xFF8C253B), RosePrimaryContainer
        )
    }

    val (secondary, onSecondary, secondaryContainer, onSecondaryContainer) = when (accentPalette) {
        UserSettings.PALETTE_LAVENDER -> Quad(
            Color(0xFFFF9AAD), Color(0xFF420817), Color(0xFF822D40), Color(0xFFFFDCE2)
        )
        UserSettings.PALETTE_SAGE -> Quad(
            Color(0xFFFFA593), Color(0xFF3E0F05), Color(0xFF7D3425), Color(0xFFFFDFD8)
        )
        UserSettings.PALETTE_OCEAN -> Quad(
            Color(0xFFFFA994), Color(0xFF3F1006), Color(0xFF7F3422), Color(0xFFFFE1DA)
        )
        else -> Quad(
            ColorFertileWindow, TealOnSecondaryContainer, Color(0xFF185C55), TealSecondaryContainer
        )
    }

    return darkColorScheme(
        primary = primary,
        onPrimary = onPrimary,
        primaryContainer = primaryContainer,
        onPrimaryContainer = onPrimaryContainer,
        secondary = secondary,
        onSecondary = onSecondary,
        secondaryContainer = secondaryContainer,
        onSecondaryContainer = onSecondaryContainer,
        tertiary = LavenderTertiaryContainer,
        onTertiary = LavenderOnTertiaryContainer,
        tertiaryContainer = LavenderTertiary,
        onTertiaryContainer = LavenderTertiaryContainer,
        background = DarkBackground,
        onBackground = DarkTextPrimary,
        surface = DarkSurface,
        onSurface = DarkTextPrimary,
        surfaceVariant = DarkSurfaceVariant,
        onSurfaceVariant = DarkTextSecondary,
        surfaceContainerLowest = DarkBackground,
        surfaceContainerLow = DarkSurfaceContainerLow,
        surfaceContainer = DarkSurfaceContainer,
        surfaceContainerHigh = DarkSurfaceContainerHigh,
        surfaceContainerHighest = DarkSurfaceVariant,
        outline = DarkOutline,
        error = DarkError,
        onError = DarkOnError,
        errorContainer = DarkErrorContainer,
        onErrorContainer = DarkOnErrorContainer
    )
}

private data class Quad(
    val first: Color,
    val second: Color,
    val third: Color,
    val fourth: Color
)

@Composable
fun resolveIsDarkTheme(appTheme: String?): Boolean {
    return when (appTheme) {
        UserSettings.THEME_DARK -> true
        UserSettings.THEME_LIGHT -> false
        else -> isSystemInDarkTheme()
    }
}

@Composable
fun MyApplicationTheme(
    appTheme: String = UserSettings.THEME_LIGHT,
    accentPalette: String = UserSettings.PALETTE_ROSE,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val darkTheme = resolveIsDarkTheme(appTheme)
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> buildDarkColorScheme(accentPalette)
        else -> buildLightColorScheme(accentPalette)
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
