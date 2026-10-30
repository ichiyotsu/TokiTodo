package com.tokitodo.app.ui.theme

import androidx.compose.foundation.background
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/** TokiUI 0.1.0 One UI shell — page palette, radii and background. */
object TokiUi {
    val Primary = Color(0xFF0381FE)
    val LightBackground = Color(0xFFF2F2F2)
    val DarkBackground = Color(0xFF080808)
    val LightSurface = Color(0xFFFFFFFF)
    val DarkSurface = Color(0xFF121212)
    val LightGradient = listOf(Color(0xFFEEF5FF), Color(0xFFF8EEF6))
    val DarkGradient = listOf(Color(0xFF080808), Color(0xFF15101A))
}

val tokiUiLightScheme = lightColorScheme(
    primary = TokiUi.Primary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD6E9FF),
    onPrimaryContainer = Color(0xFF001B3D),
    secondary = Color(0xFF6F5B7E),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF0DBFF),
    onSecondaryContainer = Color(0xFF29123B),
    tertiary = Color(0xFF9A4C72),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFD8E6),
    onTertiaryContainer = Color(0xFF3D0026),
    background = TokiUi.LightBackground,
    onBackground = Color.Black,
    surface = TokiUi.LightSurface,
    onSurface = Color.Black,
    surfaceVariant = Color(0xFFE8E8E8),
    onSurfaceVariant = Color(0xFF656565),
    outline = Color(0xFFBDBDBD),
    outlineVariant = Color(0xFFDADADA),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFFAFAFA),
    surfaceContainer = Color(0xFFF2F2F2),
    surfaceContainerHigh = Color(0xFFE8E8E8),
    surfaceContainerHighest = Color(0xFFDDDDDD),
    inverseSurface = Color(0xFF303030),
    inverseOnSurface = Color(0xFFF5F5F5),
    inversePrimary = Color(0xFF9FCBFF),
)

val tokiUiDarkScheme = darkColorScheme(
    primary = Color(0xFF4FA3FF),
    onPrimary = Color(0xFF002F5C),
    primaryContainer = Color(0xFF004A85),
    onPrimaryContainer = Color(0xFFD6E9FF),
    secondary = Color(0xFFD8BDE8),
    onSecondary = Color(0xFF3E2A4D),
    secondaryContainer = Color(0xFF564064),
    onSecondaryContainer = Color(0xFFF0DBFF),
    tertiary = Color(0xFFFFB0CE),
    onTertiary = Color(0xFF5D1138),
    tertiaryContainer = Color(0xFF7B294E),
    onTertiaryContainer = Color(0xFFFFD8E6),
    background = TokiUi.DarkBackground,
    onBackground = Color(0xFFFAFAFA),
    surface = TokiUi.DarkSurface,
    onSurface = Color(0xFFFAFAFA),
    surfaceVariant = Color(0xFF2A2A2A),
    onSurfaceVariant = Color(0xFFA8A8A8),
    outline = Color(0xFF3A3A3A),
    outlineVariant = Color(0xFF3A3A3A),
    surfaceContainerLowest = Color(0xFF080808),
    surfaceContainerLow = Color(0xFF121212),
    surfaceContainer = Color(0xFF1A1A1A),
    surfaceContainerHigh = Color(0xFF2A2A2A),
    surfaceContainerHighest = Color(0xFF333333),
    inverseSurface = Color(0xFFFAFAFA),
    inverseOnSurface = Color(0xFF303030),
    inversePrimary = TokiUi.Primary,
)

@Composable
fun Modifier.tokiUiBackground(): Modifier = background(
    Brush.verticalGradient(
        if (LocalDarkTheme.current) TokiUi.DarkGradient else TokiUi.LightGradient,
    ),
)
