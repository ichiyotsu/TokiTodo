package com.tokitodo.app.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalContext

val lightScheme = lightColorScheme(
    primary = primaryLight,
    onPrimary = onPrimaryLight,
    primaryContainer = primaryContainerLight,
    onPrimaryContainer = onPrimaryContainerLight,
    secondary = secondaryLight,
    onSecondary = onSecondaryLight,
    secondaryContainer = secondaryContainerLight,
    onSecondaryContainer = onSecondaryContainerLight,
    tertiary = tertiaryLight,
    onTertiary = onTertiaryLight,
    tertiaryContainer = tertiaryContainerLight,
    onTertiaryContainer = onTertiaryContainerLight,
    error = errorLight,
    onError = onErrorLight,
    errorContainer = errorContainerLight,
    onErrorContainer = onErrorContainerLight,
    background = backgroundLight,
    onBackground = onBackgroundLight,
    surface = surfaceLight,
    onSurface = onSurfaceLight,
    surfaceVariant = surfaceVariantLight,
    onSurfaceVariant = onSurfaceVariantLight,
    outline = outlineLight,
    outlineVariant = outlineVariantLight,
    scrim = scrimLight,
    inverseSurface = inverseSurfaceLight,
    inverseOnSurface = inverseOnSurfaceLight,
    inversePrimary = inversePrimaryLight,
    surfaceDim = surfaceDimLight,
    surfaceBright = surfaceBrightLight,
    surfaceContainerLowest = surfaceContainerLowestLight,
    surfaceContainerLow = surfaceContainerLowLight,
    surfaceContainer = surfaceContainerLight,
    surfaceContainerHigh = surfaceContainerHighLight,
    surfaceContainerHighest = surfaceContainerHighestLight,
)

val darkScheme = darkColorScheme(
    primary = primaryDark,
    onPrimary = onPrimaryDark,
    primaryContainer = primaryContainerDark,
    onPrimaryContainer = onPrimaryContainerDark,
    secondary = secondaryDark,
    onSecondary = onSecondaryDark,
    secondaryContainer = secondaryContainerDark,
    onSecondaryContainer = onSecondaryContainerDark,
    tertiary = tertiaryDark,
    onTertiary = onTertiaryDark,
    tertiaryContainer = tertiaryContainerDark,
    onTertiaryContainer = onTertiaryContainerDark,
    error = errorDark,
    onError = onErrorDark,
    errorContainer = errorContainerDark,
    onErrorContainer = onErrorContainerDark,
    background = backgroundDark,
    onBackground = onBackgroundDark,
    surface = surfaceDark,
    onSurface = onSurfaceDark,
    surfaceVariant = surfaceVariantDark,
    onSurfaceVariant = onSurfaceVariantDark,
    outline = outlineDark,
    outlineVariant = outlineVariantDark,
    scrim = scrimDark,
    inverseSurface = inverseSurfaceDark,
    inverseOnSurface = inverseOnSurfaceDark,
    inversePrimary = inversePrimaryDark,
    surfaceDim = surfaceDimDark,
    surfaceBright = surfaceBrightDark,
    surfaceContainerLowest = surfaceContainerLowestDark,
    surfaceContainerLow = surfaceContainerLowDark,
    surfaceContainer = surfaceContainerDark,
    surfaceContainerHigh = surfaceContainerHighDark,
    surfaceContainerHighest = surfaceContainerHighestDark,
)

val LocalDarkTheme = staticCompositionLocalOf { false }

private fun ColorScheme.trueBlack(): ColorScheme = copy(
    background = Color.Black,
    surface = Color.Black,
    surfaceContainerLowest = Color.Black,
    surfaceContainerLow = Color(0xFF0F1210),
    surfaceContainer = Color(0xFF141815),
    surfaceContainerHigh = Color(0xFF1A1F1B),
    surfaceContainerHighest = Color(0xFF222723),
)

private fun ColorScheme.withPreset(preset: ColorPreset, dark: Boolean): ColorScheme = when (preset) {
    ColorPreset.Pine -> this
    ColorPreset.Persimmon -> if (dark) {
        copy(
            primary = Color(0xFFFFB59F),
            onPrimary = Color(0xFF5F1600),
            primaryContainer = Color(0xFF7E2A17),
            onPrimaryContainer = Color(0xFFFFDBCF),
            tertiary = Color(0xFF9CD0BE),
            onTertiary = Color(0xFF00382D),
            tertiaryContainer = Color(0xFF1C5145),
            onTertiaryContainer = Color(0xFFB9ECD9),
        )
    } else {
        copy(
            primary = Color(0xFF9C4A2F),
            onPrimary = Color.White,
            primaryContainer = Color(0xFFFFDBCF),
            onPrimaryContainer = Color(0xFF3B0A00),
            tertiary = Color(0xFF356859),
            onTertiary = Color.White,
            tertiaryContainer = Color(0xFFB9ECD9),
            onTertiaryContainer = Color(0xFF00201A),
        )
    }

    ColorPreset.Ocean -> if (dark) {
        copy(
            primary = Color(0xFFA8C8EA),
            onPrimary = Color(0xFF0E314F),
            primaryContainer = Color(0xFF274A6E),
            onPrimaryContainer = Color(0xFFCFE4FF),
            tertiary = Color(0xFFFFB59F),
            onTertiary = Color(0xFF5F1600),
            tertiaryContainer = Color(0xFF7E2A17),
            onTertiaryContainer = Color(0xFFFFDBCF),
        )
    } else {
        copy(
            primary = Color(0xFF2F5C86),
            onPrimary = Color.White,
            primaryContainer = Color(0xFFCFE4FF),
            onPrimaryContainer = Color(0xFF0B2E4F),
            tertiary = Color(0xFFA34E33),
            onTertiary = Color.White,
            tertiaryContainer = Color(0xFFFFDBCF),
            onTertiaryContainer = Color(0xFF3B0A00),
        )
    }

    ColorPreset.Wisteria -> if (dark) {
        copy(
            primary = Color(0xFFCFBBF5),
            onPrimary = Color(0xFF362153),
            primaryContainer = Color(0xFF4D3769),
            onPrimaryContainer = Color(0xFFEADDFF),
            tertiary = Color(0xFFFFB59F),
            onTertiary = Color(0xFF5F1600),
            tertiaryContainer = Color(0xFF7E2A17),
            onTertiaryContainer = Color(0xFFFFDBCF),
        )
    } else {
        copy(
            primary = Color(0xFF66508E),
            onPrimary = Color.White,
            primaryContainer = Color(0xFFEADDFF),
            onPrimaryContainer = Color(0xFF2F1A4A),
            tertiary = Color(0xFFA34E33),
            onTertiary = Color.White,
            tertiaryContainer = Color(0xFFFFDBCF),
            onTertiaryContainer = Color(0xFF3B0A00),
        )
    }
}

@Composable
fun TokiToDoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    amoled: Boolean = false,
    colorPreset: ColorPreset = ColorPreset.Pine,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val base = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)

        darkTheme -> darkScheme
        else -> lightScheme
    }
    val presetBase = if (dynamicColor) base else base.withPreset(colorPreset, darkTheme)
    val scheme = if (dynamicColor) {
        presetBase
    } else {
        val seed = presetBase.primary
        val blend: (Color, Float) -> Color = { color, amount -> lerp(color, seed, amount) }
        presetBase.copy(
            background = blend(presetBase.background, 0.03f),
            surface = blend(presetBase.surface, 0.03f),
            surfaceContainerLowest = blend(presetBase.surfaceContainerLowest, 0.02f),
            surfaceContainerLow = blend(presetBase.surfaceContainerLow, 0.05f),
            surfaceContainer = blend(presetBase.surfaceContainer, 0.07f),
            surfaceContainerHigh = blend(presetBase.surfaceContainerHigh, 0.09f),
            surfaceContainerHighest = blend(presetBase.surfaceContainerHighest, 0.11f),
        )
    }.let { if (amoled && darkTheme) it.trueBlack() else it }

    CompositionLocalProvider(LocalDarkTheme provides darkTheme) {
        MaterialTheme(
            colorScheme = scheme,
            typography = TokiTypography,
            shapes = TokiShapes,
            content = content,
        )
    }
}
