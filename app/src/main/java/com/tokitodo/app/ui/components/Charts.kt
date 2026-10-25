package com.tokitodo.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.tokitodo.app.ui.TokiMotion
import kotlin.math.max

data class BarDatum(val label: String, val value: Int, val highlightLabel: String? = null)

/**
 * 专注时长柱状图。柱子用竖向渐变 + 圆角，空值留一枚淡色底座，
 * 点按柱子会把数值浮在上面。
 */
@Composable
fun FocusBarChart(
    bars: List<BarDatum>,
    modifier: Modifier = Modifier,
    height: Dp = 168.dp,
    accent: Color = MaterialTheme.colorScheme.primary,
    highlightIndex: Int? = null,
    onHighlight: (Int?) -> Unit = {},
    showLabels: Boolean = true,
) {
    val maxValue = max(bars.maxOfOrNull { it.value } ?: 0, 1)
    val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    val baseColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
    val animatedMax by animateFloatAsState(
        targetValue = maxValue.toFloat(),
        animationSpec = tween(520),
        label = "chartMax",
    )

    Column(modifier) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(height)
                    .pointerInput(bars.size) {
                        detectTapGestures { offset ->
                            if (bars.isEmpty()) return@detectTapGestures
                            val slot = size.width.toFloat() / bars.size
                            val index = (offset.x / slot).toInt().coerceIn(0, bars.lastIndex)
                            onHighlight(if (highlightIndex == index) null else index)
                        }
                    },
            ) {
                val w = size.width
                val h = size.height
                val plotTop = 18.dp.toPx()
                val plotBottom = h - 8.dp.toPx()
                val plotHeight = plotBottom - plotTop
                // 网格
                for (i in 0..2) {
                    val y = plotTop + plotHeight * i / 2f
                    drawLine(
                        color = gridColor.copy(alpha = if (i == 2) 0.7f else 0.4f),
                        start = Offset(0f, y),
                        end = Offset(w, y),
                        strokeWidth = 1.dp.toPx(),
                    )
                }
                if (bars.isEmpty()) return@Canvas
                val slot = w / bars.size
                val barWidth = (slot * 0.46f).coerceAtMost(18.dp.toPx()).coerceAtLeast(2.5f)
                val radius = minOf(barWidth / 2f, 7.dp.toPx())
                bars.forEachIndexed { index, bar ->
                    val cx = slot * index + slot / 2f
                    val ratio = (bar.value.toFloat() / animatedMax).coerceIn(0f, 1f)
                    val barHeight = (plotHeight * ratio).coerceAtLeast(if (bar.value > 0) 5.dp.toPx() else 2.dp.toPx())
                    val top = plotBottom - barHeight
                    if (bar.value > 0) {
                        drawRoundRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(accent, accent.copy(alpha = 0.45f)),
                                startY = top,
                                endY = plotBottom,
                            ),
                            topLeft = Offset(cx - barWidth / 2f, top),
                            size = Size(barWidth, barHeight),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(radius, radius),
                        )
                        if (highlightIndex == index) {
                            drawRoundRect(
                                color = accent.copy(alpha = 0.22f),
                                topLeft = Offset(cx - slot / 2f + 1f, plotTop),
                                size = Size(slot - 2f, plotBottom - plotTop),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(8.dp.toPx()),
                            )
                            drawRoundRect(
                                brush = Brush.verticalGradient(
                                    colors = listOf(accent, accent.copy(alpha = 0.45f)),
                                    startY = top,
                                    endY = plotBottom,
                                ),
                                topLeft = Offset(cx - barWidth / 2f, top),
                                size = Size(barWidth, barHeight),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(radius, radius),
                            )
                        }
                    } else {
                        drawRoundRect(
                            color = baseColor.copy(alpha = 0.45f),
                            topLeft = Offset(cx - barWidth / 2f, plotBottom - 2.dp.toPx()),
                            size = Size(barWidth, 2.dp.toPx()),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(radius, radius),
                        )
                    }
                }
            }
            if (highlightIndex != null && highlightIndex in bars.indices) {
                val bar = bars[highlightIndex]
                Text(
                    text = bar.highlightLabel ?: "${bar.value} 分钟",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(end = 2.dp, top = 0.dp),
                )
            }
        }
        if (showLabels && bars.isNotEmpty()) {
            Row(modifier = Modifier.fillMaxWidth()) {
                bars.forEach { bar ->
                    Text(
                        text = bar.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

/** 四时段水平条：凌晨 / 上午 / 下午 / 夜晚。 */
@Composable
fun TimeOfDayBars(
    values: List<Int>,
    labels: List<String>,
    modifier: Modifier = Modifier,
) {
    val total = values.sum().coerceAtLeast(1)
    val colors = MaterialTheme.colorScheme
    val tints = listOf(colors.primary, colors.tertiary, colors.secondary, colors.primary.copy(alpha = 0.65f))
    Column(modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        values.forEachIndexed { index, value ->
            val ratio = value.toFloat() / total
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = labels.getOrElse(index) { "" },
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.onSurfaceVariant,
                    modifier = Modifier.width(44.dp),
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(12.dp),
                ) {
                    Canvas(Modifier.fillMaxWidth().height(12.dp)) {
                        drawRoundRect(
                            color = colors.surfaceContainerHigh,
                            size = Size(size.width, size.height),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(size.height / 2f),
                        )
                        if (ratio > 0f) {
                            drawRoundRect(
                                brush = Brush.horizontalGradient(
                                    listOf(tints[index % tints.size], tints[index % tints.size].copy(alpha = 0.55f)),
                                ),
                                size = Size((size.width * ratio).coerceAtLeast(size.height), size.height),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(size.height / 2f),
                            )
                        }
                    }
                }
                Spacer(Modifier.width(10.dp))
                Text(
                    text = if (value > 0) "${value}′" else "—",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.onSurfaceVariant,
                    modifier = Modifier.width(36.dp),
                    textAlign = TextAlign.End,
                )
            }
        }
    }
}

/** 月历热力图：每格颜色随当日专注时长加深。 */
@Composable
fun MonthHeatmap(
    days: List<Pair<java.time.LocalDate, Int>>,
    modifier: Modifier = Modifier,
    onSelect: (java.time.LocalDate) -> Unit = {},
) {
    val colors = MaterialTheme.colorScheme
    val max = max(days.maxOfOrNull { it.second } ?: 0, 1)
    val first = days.firstOrNull()?.first ?: return
    val leading = (first.dayOfWeek.value - 1)
    val cells = ArrayList<Pair<java.time.LocalDate, Int>?>(leading)
    repeat(leading) { cells.add(null) }
    cells.addAll(days)

    Column(modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(modifier = Modifier.fillMaxWidth()) {
            listOf("一", "二", "三", "四", "五", "六", "日").forEach { label ->
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.onSurfaceVariant.copy(alpha = 0.65f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f),
                )
            }
        }
        cells.chunked(7).forEach { week ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                week.forEach { cell ->
                    Box(modifier = Modifier.weight(1f)) {
                        if (cell == null) {
                            Spacer(Modifier.height(36.dp))
                        } else {
                            val intensity = cell.second.toFloat() / max
                            val strength = if (cell.second == 0) 0f else 0.16f + intensity * 0.74f
                            Canvas(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(36.dp)
                                    .pointerInput(cell) {
                                        detectTapGestures { onSelect(cell.first) }
                                    },
                            ) {
                                drawRoundRect(
                                    color = if (strength == 0f) {
                                        colors.surfaceContainerHigh.copy(alpha = 0.7f)
                                    } else {
                                        colors.primary.copy(alpha = strength)
                                    },
                                    size = size,
                                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(9.dp.toPx()),
                                )
                            }
                            Text(
                                text = cell.first.dayOfMonth.toString(),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (intensity > 0.55f) colors.onPrimary else colors.onSurfaceVariant,
                                modifier = Modifier.padding(start = 5.dp, top = 3.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

/** 环形进度（今日目标 / 计时器共用）。 */
@Composable
fun ProgressRing(
    progress: Float,
    modifier: Modifier = Modifier,
    size: Dp = 72.dp,
    stroke: Dp = 7.dp,
    track: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
    accent: Color = MaterialTheme.colorScheme.primary,
    content: (@Composable () -> Unit)? = null,
) {
    val animated = animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(660),
        label = "ring",
    )
    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(Modifier.size(size)) {
            val strokePx = stroke.toPx()
            val inset = strokePx / 2f
            drawArc(
                color = track,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = Size(this.size.width - strokePx, this.size.height - strokePx),
                style = Stroke(width = strokePx, cap = StrokeCap.Round),
            )
            if (animated.value > 0.001f) {
                drawArc(
                    brush = Brush.sweepGradient(listOf(accent, accent.copy(alpha = 0.55f), accent)),
                    startAngle = -90f,
                    sweepAngle = 360f * animated.value,
                    useCenter = false,
                    topLeft = Offset(inset, inset),
                    size = Size(this.size.width - strokePx, this.size.height - strokePx),
                    style = Stroke(width = strokePx, cap = StrokeCap.Round),
                )
            }
        }
        content?.invoke()
    }
}

/** 展示用的渐变条（目标进度、VIP 卡）。 */
@Composable
fun GradientBar(
    progress: Float,
    modifier: Modifier = Modifier,
    colors: List<Color> = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.tertiary),
    track: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
) {
    Canvas(modifier.fillMaxWidth().height(8.dp)) {
        drawRoundRect(
            color = track,
            size = Size(size.width, size.height),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(size.height / 2f),
        )
        val width = (size.width * progress.coerceIn(0f, 1f)).coerceAtLeast(if (progress > 0f) size.height else 0f)
        if (width > 0f) {
            drawRoundRect(
                brush = Brush.horizontalGradient(colors),
                size = Size(width, size.height),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(size.height / 2f),
            )
        }
    }
}
