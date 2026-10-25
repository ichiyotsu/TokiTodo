package com.tokitodo.app.ui

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer

/**
 * 统一动效开关。MainActivity 根据「减弱动效」注入，
 * 所有弹簧转场与按压反馈都读这里。
 */
val LocalMotionEnabled = staticCompositionLocalOf { true }

/** M3E 的几档动效参数。 */
object TokiMotion {
    val spatial = spring<Float>(dampingRatio = 0.82f, stiffness = 380f)
    val emphasized = spring<Float>(dampingRatio = 0.62f, stiffness = 420f)
    val overshoot = spring<Float>(dampingRatio = 0.5f, stiffness = 460f)
    val emphasizedEasing = CubicBezierEasing(0.2f, 0f, 0f, 1f)

    const val quick = 140
    const val standard = 220
    const val expressive = 340
    const val slow = 520
}

/** 按压回弹：按下略缩小，松开弹回。 */
@Composable
fun Modifier.pressScale(
    interactionSource: InteractionSource,
    pressedScale: Float = 0.96f,
): Modifier {
    val enabled = LocalMotionEnabled.current
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (enabled && pressed) pressedScale else 1f,
        animationSpec = TokiMotion.spatial,
        label = "pressScale",
    )
    return graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}
