package com.tokitodo.app.ui.theme

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import dev.chrisbanes.haze.HazeInput
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.blur.HazeBlurStyle
import dev.chrisbanes.haze.blur.HazeColorEffect
import dev.chrisbanes.haze.blur.hazeBlur

/**
 * 统一的毛玻璃配方：调用方只决定模糊半径和 tint，
 * Haze 的输入与兼容回退都收在这一个入口里。
 */
fun Modifier.tokiGlass(
    hazeState: HazeState,
    blurRadius: Dp,
    tints: List<Color>,
    fallback: Color,
    noise: Float = 0.02f,
): Modifier = hazeBlur(
    input = HazeInput.Sources(hazeState),
    style = HazeBlurStyle {
        blurRadius(blurRadius)
        noiseFactor(noise)
        colorEffects(tints.map { tint -> HazeColorEffect.tint(tint) })
        fallbackColorEffect(HazeColorEffect.tint(fallback))
    },
)
