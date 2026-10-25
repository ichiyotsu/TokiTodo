package com.tokitodo.app.ui.timer

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Color as AndroidColor
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.tokitodo.app.data.TimerMode
import com.tokitodo.app.data.TimerState
import com.tokitodo.app.ui.components.Artwork
import com.tokitodo.app.ui.components.ProgressRing
import com.tokitodo.app.ui.formatClock
import com.tokitodo.app.ui.formatDuration
import com.tokitodo.app.ui.theme.LocalDarkTheme

/**
 * 全屏计时界面：专注 / 小憩 / 锁机共用一张海报底色。
 * 锁机模式没有暂停，放弃需要长按确认。
 */
@Composable
fun TimerScreen(
    timer: TimerState,
    showLockCountdown: Boolean = true,
    allowEmergencyUnlock: Boolean = true,
    onToggle: () -> Unit,
    onExtend: (Int) -> Unit,
    onFinish: () -> Unit,
    onAbandon: () -> Unit,
) {
    var confirmAbandon by remember { mutableStateOf(false) }
    var showStrictHint by remember { mutableStateOf(false) }
    val view = LocalView.current
    val dark = LocalDarkTheme.current
    val context = LocalContext.current
    val navigationSurface = MaterialTheme.colorScheme.surface.toArgb()
    DisposableEffect(view) {
        view.keepScreenOn = true
        val window = context.findActivity()?.window
        val controller = window?.let { WindowCompat.getInsetsController(it, view) }
        val previousNavigationColor = window?.navigationBarColor
        controller?.isAppearanceLightStatusBars = false
        controller?.isAppearanceLightNavigationBars = false
        @Suppress("DEPRECATION")
        window?.navigationBarColor = AndroidColor.rgb(12, 18, 16)
        onDispose {
            view.keepScreenOn = false
            controller?.isAppearanceLightStatusBars = !dark
            controller?.isAppearanceLightNavigationBars = !dark
            @Suppress("DEPRECATION")
            window?.navigationBarColor = previousNavigationColor ?: navigationSurface
        }
    }

    val isLock = timer.mode == TimerMode.Lock
    val isNap = timer.mode == TimerMode.Nap

    BackHandler {
        when {
            isLock -> Unit
            timer.mode == TimerMode.Focus -> confirmAbandon = true
            else -> onAbandon()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .blur(18.dp),
        ) {
            Artwork(art = timer.art, modifier = Modifier.fillMaxSize(), scrim = false)
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Black.copy(alpha = 0.42f),
                            Color.Black.copy(alpha = 0.30f),
                            Color.Black.copy(alpha = 0.72f),
                        ),
                    ),
                ),
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (!isLock) {
                    IconButton(onClick = { confirmAbandon = true }) {
                        Icon(
                            Icons.Rounded.Close,
                            contentDescription = "结束本次专注",
                            tint = Color.White.copy(alpha = 0.9f),
                        )
                    }
                } else {
                    Spacer(Modifier.width(48.dp))
                }
                Spacer(Modifier.weight(1f))
                Row(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.16f))
                        .border(1.dp, Color.White.copy(alpha = 0.25f), CircleShape)
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = when {
                            isLock -> Icons.Rounded.Lock
                            isNap -> Icons.Rounded.Spa
                            else -> Icons.Rounded.AutoAwesome
                        },
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(13.dp),
                    )
                    Spacer(Modifier.width(5.dp))
                    Text(
                        text = when {
                            isLock -> "锁机中"
                            isNap -> "小憩中"
                            timer.mode == TimerMode.Break -> "休息中"
                            timer.strict -> "学霸模式 · 专注中"
                            else -> "专注中"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White,
                    )
                }
                Spacer(Modifier.weight(1f))
                if (!isLock) {
                    IconButton(
                        onClick = {
                            if (timer.strict) showStrictHint = true else onFinish()
                        },
                    ) {
                        Icon(
                            Icons.Rounded.Check,
                            contentDescription = "提前完成",
                            tint = Color.White.copy(alpha = 0.9f),
                        )
                    }
                } else {
                    Spacer(Modifier.width(48.dp))
                }
            }

            Spacer(Modifier.weight(0.9f))

            Text(
                text = if (isLock) "把手机放下" else timer.title,
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                textAlign = TextAlign.Center,
                maxLines = 2,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = when {
                    isLock -> "这段时间不属于通知，也不属于别人"
                    isNap -> "慢慢吸气，慢慢呼气"
                    else -> "还剩 ${formatDuration((timer.remainingSeconds + 59) / 60)}"
                },
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.72f),
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(26.dp))

            Box(contentAlignment = Alignment.Center) {
                if (isNap) {
                    BreathingHalo()
                }
                ProgressRing(
                    progress = timer.progress,
                    size = 268.dp,
                    stroke = 9.dp,
                    track = Color.White.copy(alpha = 0.18f),
                    accent = Color.White.copy(alpha = 0.92f),
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (isLock && !showLockCountdown) "锁机" else formatClock(timer.remainingSeconds),
                            style = MaterialTheme.typography.displayLarge.copy(
                                fontSize = if (isLock && !showLockCountdown) 44.sp else 60.sp,
                                fontWeight = FontWeight.SemiBold,
                            ),
                            color = Color.White,
                        )
                        Text(
                            text = if (isLock) "请放下手机" else "${(timer.progress * 100).toInt()}% 完成",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White.copy(alpha = 0.7f),
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            if (timer.strict && !isLock) {
                Row(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.13f))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        Icons.Rounded.Lock,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.size(12.dp),
                    )
                    Spacer(Modifier.width(5.dp))
                    Text(
                        text = "学霸模式：不可暂停",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.8f),
                    )
                }
            }

            Spacer(Modifier.weight(1f))

            if (isLock) {
                LockActions(
                    allowEmergencyUnlock = allowEmergencyUnlock,
                    onLongPress = { confirmAbandon = true },
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    OutlinePill(
                        label = if (isNap) "结束小憩" else "放弃",
                        onClick = { confirmAbandon = true },
                    )
                    BigRoundButton(
                        icon = if (timer.running) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                        enabled = !(timer.strict && !isNap),
                        onClick = {
                            if (timer.strict && timer.running && !isNap) {
                                showStrictHint = true
                            } else {
                                onToggle()
                            }
                        },
                    )
                    OutlinePill(
                        label = "+5 分钟",
                        icon = Icons.Rounded.Add,
                        onClick = { onExtend(5) },
                    )
                }
            }

            Spacer(Modifier.height(26.dp))
            Text(
                text = when {
                    isLock -> "长按下方按钮才能放弃 · 你已经坚持了 ${timer.elapsedMinutes} 分钟"
                    isNap -> "小憩不会计入专注时长"
                    else -> "已完成 ${timer.elapsedMinutes} 分钟 · 保持呼吸"
                },
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.55f),
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(14.dp))
        }
    }

    if (confirmAbandon) {
        AlertDialog(
            onDismissRequest = { confirmAbandon = false },
            title = { Text(if (isLock) "提前结束锁机？" else "结束这次专注？") },
            text = {
                Text(
                    if (timer.elapsedMinutes >= 1) {
                        "已经专注 ${timer.elapsedMinutes} 分钟，会记进统计数据。"
                    } else {
                        "还没有满 1 分钟，这次不会记进统计。"
                    },
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmAbandon = false
                        if (isLock) onAbandon() else onFinish()
                    },
                ) {
                    Text("结束", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmAbandon = false }) { Text("继续") }
            },
        )
    }

    if (showStrictHint) {
        AlertDialog(
            onDismissRequest = { showStrictHint = false },
            title = { Text("学霸模式运行中") },
            text = { Text("学霸模式开启时不能暂停。想休息的话，先结束这次专注；它已经坚持了 ${timer.elapsedMinutes} 分钟。") },
            confirmButton = {
                TextButton(onClick = { showStrictHint = false }) { Text("继续专注") }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showStrictHint = false
                        confirmAbandon = true
                    },
                ) {
                    Text("结束专注")
                }
            },
        )
    }
}

@Composable
private fun BigRoundButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = if (enabled) Color.White else Color.White.copy(alpha = 0.30f),
        contentColor = if (enabled) {
            MaterialTheme.colorScheme.primary
        } else {
            Color.White.copy(alpha = 0.8f)
        },
        shadowElevation = 6.dp,
        modifier = Modifier.size(78.dp),
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = "开始或暂停", modifier = Modifier.size(34.dp))
        }
    }
}

@Composable
private fun OutlinePill(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = Color.White.copy(alpha = 0.20f),
        contentColor = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.40f)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 15.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (icon != null) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
            }
            Text(label, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun LockActions(allowEmergencyUnlock: Boolean, onLongPress: () -> Unit) {
    if (!allowEmergencyUnlock) {
        Row(
            modifier = Modifier
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.12f))
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                Icons.Rounded.Lock,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.85f),
                modifier = Modifier.size(16.dp),
            )
            Spacer(Modifier.width(8.dp))
            Text(
                "紧急解锁已关闭，请等待锁机结束",
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = 0.85f),
            )
        }
    } else {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.13f))
                .border(1.dp, Color.White.copy(alpha = 0.22f), CircleShape)
                .combinedClickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {},
                    onLongClick = onLongPress,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Rounded.Lock,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.size(16.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "长按 3 秒放弃锁机",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.85f),
                )
            }
        }
    }
}

@Composable
private fun BreathingHalo() {    val transition = rememberInfiniteTransition(label = "breathing")
    val scale by transition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(4200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "breathingScale",
    )
    val alpha by transition.animateFloat(
        initialValue = 0.10f,
        targetValue = 0.22f,
        animationSpec = infiniteRepeatable(
            animation = tween(4200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "breathingAlpha",
    )
    Canvas(
        modifier = Modifier
            .size(268.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
    ) {
        drawCircle(Color.White.copy(alpha = alpha))
    }
}

private fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
