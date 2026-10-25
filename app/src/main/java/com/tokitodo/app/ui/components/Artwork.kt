package com.tokitodo.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.tokitodo.app.ui.theme.ArtPalettes
import com.tokitodo.app.ui.theme.TodoArt

/**
 * 待办卡片插画：用 Canvas 画出来的「一张小海报」。
 *
 * 每张卡片 = 天空渐变 + 光晕 + 一种母题（云 / 玻璃楼 / 飞鸟 / 山丘 / 极光 / 沙丘 / 星夜），
 * 底部叠一层很轻的暗角，保证白色标题在任何一张图上都够清楚。
 */
@Composable
fun Artwork(
    art: TodoArt,
    modifier: Modifier = Modifier,
    scrim: Boolean = true,
) {
    val palette = ArtPalettes.getValue(art)
    Box(modifier) {
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(palette.top, palette.mid, palette.bottom),
                    startY = 0f,
                    endY = h,
                ),
            )
            when (art) {
                TodoArt.Sky -> {
                    sunGlow(w * 0.80f, h * 0.22f, w * 0.55f, palette.glow)
                    softCloud(w * 0.16f, h * 0.72f, w * 0.58f, h * 0.32f, Color.White, 0.75f)
                    softCloud(w * 0.86f, h * 0.28f, w * 0.36f, h * 0.22f, Color.White, 0.5f)
                    birdFlock(w * 0.70f, h * 0.40f, w * 0.019f, Color.White, 0.62f)
                }

                TodoArt.Dusk -> {
                    sunGlow(w * 0.68f, h * 0.38f, w * 0.46f, palette.glow)
                    drawCircle(
                        color = Color(0xFFFFE9C9).copy(alpha = 0.72f),
                        radius = h * 0.10f,
                        center = Offset(w * 0.68f, h * 0.40f),
                    )
                    mistBand(w, h, h * 0.62f, Color.White, 0.13f)
                    glassTower(w, h)
                }

                TodoArt.Mint -> {
                    sunGlow(w * 0.16f, h * 0.24f, w * 0.42f, palette.glow)
                    mistBand(w, h, h * 0.72f, Color.White, 0.15f)
                    birdFlock(w * 0.64f, h * 0.34f, w * 0.014f, Color(0xFF123B33), 0.52f)
                    birdFlock(w * 0.40f, h * 0.50f, w * 0.010f, Color(0xFF123B33), 0.36f)
                }

                TodoArt.Blossom -> {
                    sunGlow(w * 0.30f, h * 0.26f, w * 0.48f, palette.glow)
                    softCloud(w * 0.44f, h * 0.80f, w * 0.84f, h * 0.42f, Color(0xFFFFF1F6), 0.82f)
                    softCloud(w * 0.94f, h * 0.60f, w * 0.36f, h * 0.24f, Color(0xFFFFE3EE), 0.45f)
                }

                TodoArt.Forest -> {
                    sunGlow(w * 0.74f, h * 0.24f, w * 0.50f, palette.glow)
                    hills(w, h, palette)
                    mistBand(w, h, h * 0.58f, Color(0xFFF4FAE6), 0.12f)
                }

                TodoArt.Aurora -> {
                    stars(w, h, count = 26, seed = 3)
                    auroraRibbon(w, h, 0.28f, Color(0xFF8FE3C4), 0.55f)
                    auroraRibbon(w, h, 0.44f, Color(0xFF98C7FF), 0.38f)
                    auroraRibbon(w, h, 0.60f, Color(0xFFF7B7D8), 0.28f)
                    hills(w, h, palette, dark = true)
                }

                TodoArt.Dune -> {
                    sunGlow(w * 0.30f, h * 0.30f, w * 0.52f, palette.glow)
                    drawCircle(
                        color = Color(0xFFFFF1D8).copy(alpha = 0.85f),
                        radius = h * 0.09f,
                        center = Offset(w * 0.30f, h * 0.32f),
                    )
                    dunes(w, h, palette)
                }

                TodoArt.Berry -> {
                    stars(w, h, count = 20, seed = 7)
                    drawCircle(
                        color = Color(0xFFFDF1D8),
                        radius = h * 0.09f,
                        center = Offset(w * 0.76f, h * 0.26f),
                    )
                    drawCircle(
                        color = palette.top.copy(alpha = 0.92f),
                        radius = h * 0.075f,
                        center = Offset(w * 0.80f, h * 0.23f),
                    )
                    hills(w, h, palette, dark = true)
                }
            }
            if (scrim) {
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.16f), Color.Black.copy(alpha = 0.46f)),
                        startY = h * 0.22f,
                        endY = h,
                    ),
                )
                drawRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color.Black.copy(alpha = 0.30f), Color.Transparent),
                        startX = 0f,
                        endX = w * 0.9f,
                    ),
                )
            }
        }
    }
}

private fun DrawScope.sunGlow(cx: Float, cy: Float, radius: Float, color: Color) {
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(color.copy(alpha = 0.75f), color.copy(alpha = 0.12f), Color.Transparent),
            center = Offset(cx, cy),
            radius = radius,
        ),
        radius = radius,
        center = Offset(cx, cy),
    )
}

private fun DrawScope.mistBand(w: Float, h: Float, y: Float, color: Color, alpha: Float) {
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(Color.Transparent, color.copy(alpha = alpha), Color.Transparent),
            startY = y - h * 0.18f,
            endY = y + h * 0.22f,
        ),
        topLeft = Offset(0f, y - h * 0.18f),
        size = Size(w, h * 0.40f),
    )
}

private fun DrawScope.softCloud(
    cx: Float,
    cy: Float,
    width: Float,
    height: Float,
    color: Color,
    alpha: Float,
) {
    val w = width
    val h = height
    drawOval(
        color = color.copy(alpha = alpha * 0.16f),
        topLeft = Offset(cx - w * 0.55f, cy - h * 0.10f),
        size = Size(w * 1.10f, h * 0.72f),
    )
    drawOval(
        color = color.copy(alpha = alpha * 0.24f),
        topLeft = Offset(cx - w * 0.36f, cy - h * 0.34f),
        size = Size(w * 0.78f, h * 0.78f),
    )
    drawOval(
        color = color.copy(alpha = alpha * 0.30f),
        topLeft = Offset(cx - w * 0.14f, cy - h * 0.46f),
        size = Size(w * 0.46f, h * 0.66f),
    )
    drawOval(
        color = color.copy(alpha = alpha * 0.20f),
        topLeft = Offset(cx + w * 0.04f, cy - h * 0.24f),
        size = Size(w * 0.42f, h * 0.52f),
    )
}

private fun DrawScope.birdFlock(cx: Float, cy: Float, s: Float, color: Color, alpha: Float) {
    val birds = listOf(
        -2.6f to 0.10f,
        -1.4f to -0.34f,
        0f to -0.12f,
        1.5f to 0.22f,
        2.9f to -0.05f,
        0.8f to -0.62f,
    )
    birds.forEachIndexed { index, (dx, dy) ->
        val scale = s * (0.7f + (index % 3) * 0.18f)
        val x = cx + dx * s * 1.7f
        val y = cy + dy * s * 1.6f
        val path = Path().apply {
            moveTo(x - scale, y)
            quadraticBezierTo(x - scale * 0.45f, y - scale * 0.85f, x, y)
            quadraticBezierTo(x + scale * 0.45f, y - scale * 0.85f, x + scale, y)
        }
        drawPath(
            path = path,
            color = color.copy(alpha = alpha),
            style = Stroke(width = scale * 0.24f, cap = StrokeCap.Round),
        )
    }
}

/** 黄昏玻璃楼：两块斜切的体块 + 网格玻璃。 */
private fun DrawScope.glassTower(w: Float, h: Float) {
    // 后方较矮的体块
    val backBlock = Path().apply {
        moveTo(w * 0.72f, h)
        lineTo(w * 0.78f, h * 0.48f)
        lineTo(w * 1.02f, h * 0.58f)
        lineTo(w * 1.02f, h)
        close()
    }
    drawPath(
        path = backBlock,
        brush = Brush.verticalGradient(
            colors = listOf(Color(0xFF8FA6BA).copy(alpha = 0.45f), Color(0xFF5B7488).copy(alpha = 0.62f)),
            startY = h * 0.48f,
            endY = h,
        ),
    )
    val tower = Path().apply {
        moveTo(w * 0.56f, h)
        lineTo(w * 0.71f, h * 0.16f)
        lineTo(w * 0.91f, h * 0.32f)
        lineTo(w * 0.99f, h)
        close()
    }
    drawPath(
        path = tower,
        brush = Brush.verticalGradient(
            colors = listOf(Color(0xFFC7E2E6).copy(alpha = 0.92f), Color(0xFF668FA2).copy(alpha = 0.96f)),
            startY = h * 0.16f,
            endY = h,
        ),
    )
    // 玻璃网格
    val grid = Color.White.copy(alpha = 0.30f)
    for (i in 1..6) {
        val t = i / 7f
        val x1 = w * (0.56f + 0.43f * t)
        val x2 = w * (0.71f + 0.20f * t)
        val top = h * (0.16f + 0.16f * t)
        drawLine(grid, Offset(x1, h), Offset(x2, top), strokeWidth = 1.4f)
    }
    for (i in 1..7) {
        val y = h * (1f - i * 0.115f)
        drawLine(grid, Offset(w * 0.56f, y), Offset(w * 0.99f, y + h * 0.045f), strokeWidth = 1.3f)
    }
    // 受光的棱边
    drawLine(
        color = Color(0xFFFFF2DD).copy(alpha = 0.75f),
        start = Offset(w * 0.71f, h * 0.16f),
        end = Offset(w * 0.91f, h * 0.32f),
        strokeWidth = 2.4f,
    )
    drawLine(
        color = Color(0xFFFFF2DD).copy(alpha = 0.35f),
        start = Offset(w * 0.56f, h),
        end = Offset(w * 0.71f, h * 0.16f),
        strokeWidth = 1.6f,
    )
}

/** 层叠山丘，带一点雾。 */
private fun DrawScope.hills(w: Float, h: Float, palette: com.tokitodo.app.ui.theme.ArtPalette, dark: Boolean = false) {
    val back = Path().apply {
        moveTo(0f, h * 0.82f)
        cubicTo(w * 0.22f, h * 0.58f, w * 0.42f, h * 0.76f, w * 0.62f, h * 0.68f)
        cubicTo(w * 0.78f, h * 0.62f, w * 0.90f, h * 0.72f, w, h * 0.64f)
        lineTo(w, h)
        lineTo(0f, h)
        close()
    }
    drawPath(
        path = back,
        brush = Brush.verticalGradient(
            colors = listOf(
                (if (dark) Color(0xFF152B24) else palette.mid).copy(alpha = 0.75f),
                (if (dark) Color(0xFF0B1814) else palette.bottom).copy(alpha = 0.95f),
            ),
            startY = h * 0.55f,
            endY = h,
        ),
    )
    val front = Path().apply {
        moveTo(0f, h)
        lineTo(0f, h * 0.86f)
        cubicTo(w * 0.30f, h * 0.74f, w * 0.54f, h * 0.96f, w * 0.78f, h * 0.84f)
        cubicTo(w * 0.88f, h * 0.79f, w * 0.94f, h * 0.84f, w, h * 0.80f)
        lineTo(w, h)
        close()
    }
    drawPath(
        path = front,
        brush = Brush.verticalGradient(
            colors = listOf(
                (if (dark) Color(0xFF0D1D18) else palette.top).copy(alpha = 0.82f),
                (if (dark) Color(0xFF07100D) else palette.mid).copy(alpha = 0.95f),
            ),
            startY = h * 0.7f,
            endY = h,
        ),
    )
}

private fun DrawScope.dunes(w: Float, h: Float, palette: com.tokitodo.app.ui.theme.ArtPalette) {
    val layers = listOf(
        Triple(0.72f, 0.16f, Color(0xFFD9A46C).copy(alpha = 0.55f)),
        Triple(0.82f, 0.22f, palette.bottom.copy(alpha = 0.85f)),
        Triple(0.92f, 0.14f, palette.mid.copy(alpha = 0.92f)),
    )
    layers.forEachIndexed { index, (baseY, amplitude, color) ->
        val path = Path().apply {
            moveTo(0f, h)
            lineTo(0f, h * baseY)
            cubicTo(
                w * 0.26f, h * (baseY - amplitude),
                w * 0.52f, h * (baseY + amplitude * 0.6f),
                w * 0.76f, h * (baseY - amplitude * 0.5f),
            )
            cubicTo(w * 0.88f, h * (baseY - amplitude * 0.8f), w * 0.95f, h * baseY, w, h * (baseY - amplitude * 0.2f))
            lineTo(w, h)
            close()
        }
        drawPath(path, color)
        if (index == 2) {
            drawPath(
                path = path,
                brush = Brush.verticalGradient(
                    colors = listOf(Color.Transparent, Color(0xFF5A3A1F).copy(alpha = 0.25f)),
                    startY = h * 0.76f,
                    endY = h,
                ),
            )
        }
    }
}

private fun DrawScope.stars(w: Float, h: Float, count: Int, seed: Int) {
    var value = seed * 7919
    repeat(count) {
        value = (value * 1103515245 + 12345) and 0x7fffffff
        val x = (value % 1000) / 1000f * w
        value = (value * 1103515245 + 12345) and 0x7fffffff
        val y = (value % 1000) / 1000f * h * 0.62f
        val radius = 0.8f + (value % 3) * 0.9f
        drawCircle(
            color = Color.White.copy(alpha = 0.25f + (value % 5) * 0.08f),
            radius = radius,
            center = Offset(x, y),
        )
    }
}

private fun DrawScope.auroraRibbon(w: Float, h: Float, yBase: Float, color: Color, alpha: Float) {
    val path = Path().apply {
        moveTo(0f, h * (yBase + 0.10f))
        cubicTo(w * 0.24f, h * (yBase - 0.18f), w * 0.44f, h * (yBase + 0.22f), w * 0.66f, h * yBase)
        cubicTo(w * 0.80f, h * (yBase - 0.12f), w * 0.90f, h * (yBase + 0.14f), w, h * (yBase - 0.02f))
    }
    drawPath(
        path = path,
        brush = Brush.horizontalGradient(
            colors = listOf(Color.Transparent, color.copy(alpha = alpha), Color.Transparent),
        ),
        style = Stroke(width = h * 0.11f, cap = StrokeCap.Round),
    )
    drawPath(
        path = path,
        brush = Brush.horizontalGradient(
            colors = listOf(Color.Transparent, Color.White.copy(alpha = alpha * 0.45f), Color.Transparent),
        ),
        style = Stroke(width = h * 0.035f, cap = StrokeCap.Round),
    )
}
