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
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material.icons.rounded.VolumeOff
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.window.Dialog
import androidx.core.view.WindowCompat
import com.tokitodo.app.data.AmbientMode
import com.tokitodo.app.data.AmbientPlayer
import com.tokitodo.app.data.TimerMode
import com.tokitodo.app.data.TimerPhase
import com.tokitodo.app.data.TimerState
import com.tokitodo.app.ui.components.Artwork
import com.tokitodo.app.ui.components.ProgressRing
import com.tokitodo.app.ui.components.SheetGrabber
import com.tokitodo.app.ui.formatClock
import com.tokitodo.app.ui.formatDuration
import com.tokitodo.app.ui.theme.LocalDarkTheme
import com.tokitodo.app.ui.theme.SheetShape

/**
 * 独立的全屏计时页。
 * 中间是巨型圆环倒计时，底部是五个功能的工具条：
 * 屏幕常亮 / 背景音 / 循环 / 4 分钟暂停 / 跳过。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimerScreen(
    timer: TimerState,
    showLockCountdown: Boolean = true,
    allowEmergencyUnlock: Boolean = true,
    keepAwakeDefault: Boolean = true,
    onKeepAwakeChange: (Boolean) -> Unit = {},
    onPause: () -> Unit,
    onResume: () -> Unit,
    onDismissPauseReminder: () -> Unit,
    onAbandon: () -> Unit,
    onFinishEarly: () -> Unit,
    onSkipRest: () -> Unit,
    onCycleChanged: (Boolean, Int, Int, Int, Boolean) -> Unit,
) {
    var confirmAbandon by remember { mutableStateOf(false) }
    var skipStudyOpen by remember { mutableStateOf(false) }
    var skipRestOpen by remember { mutableStateOf(false) }
    var cycleSheetOpen by remember { mutableStateOf(false) }
    var ambientSheetOpen by remember { mutableStateOf(false) }
    var ambientMode by remember { mutableStateOf<AmbientMode?>(null) }
    var keepAwake by remember { mutableStateOf(keepAwakeDefault) }
    var showPauseDialog by remember { mutableStateOf(false) }
    val view = LocalView.current
    val dark = LocalDarkTheme.current
    val context = LocalContext.current
    val navigationSurface = MaterialTheme.colorScheme.surface.toArgb()
    val isLock = timer.mode == TimerMode.Lock
    val isNap = timer.mode == TimerMode.Nap
    val isRest = timer.phase == TimerPhase.Rest && timer.mode == TimerMode.Focus
    val isPaused = timer.pauseRemainingSeconds > 0

    DisposableEffect(view) {
        view.keepScreenOn = keepAwake || isLock
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

    DisposableEffect(Unit) {
        onDispose { AmbientPlayer.stop() }
    }

    LaunchedEffect(timer.pauseReminder) {
        if (timer.pauseReminder) showPauseDialog = true
    }

    BackHandler {
        when {
            isLock -> Unit
            isRest -> skipRestOpen = true
            else -> confirmAbandon = true
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
                            Color.Black.copy(alpha = 0.44f),
                            Color.Black.copy(alpha = 0.30f),
                            Color.Black.copy(alpha = 0.78f),
                        ),
                    ),
                ),
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Spacer(Modifier.weight(1f))
                Row(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.16f))
                        .border(1.dp, Color.White.copy(alpha = 0.25f), CircleShape)
                        .padding(horizontal = 13.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = when {
                            isLock -> Icons.Rounded.Lock
                            isNap -> Icons.Rounded.Spa
                            isRest -> Icons.Rounded.Spa
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
                            isRest -> "休息中"
                            timer.strict -> "严格模式 · 专注中"
                            else -> "专注中"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White,
                    )
                }
                Spacer(Modifier.weight(1f))
            }

            Spacer(Modifier.weight(0.8f))

            Text(
                text = when {
                    isLock -> "把手机放下"
                    isRest -> "休息一下"
                    timer.noTimer -> "现在只做这一件事"
                    else -> timer.title
                },
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
                    isRest -> "离开屏幕，让眼睛和呼吸慢下来"
                    timer.noTimer -> "没有倒计时，完成后点「跳过」结束"
                    timer.countUp -> "正计时 · 已经专注 ${formatDuration(timer.elapsedMinutes)}"
                    timer.cycleEnabled -> buildString {
                        append("第 ${(timer.cycleDone + 1).coerceAtMost(if (timer.cycleTotal < 0) Int.MAX_VALUE else timer.cycleTotal)} 轮")
                        if (timer.cycleTotal < 0) append(" · 无限循环") else append(" / ${timer.cycleTotal} 轮")
                    }

                    else -> "还剩 ${formatDuration((timer.displaySeconds + 59) / 60)}"
                },
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.74f),
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(24.dp))

            Box(contentAlignment = Alignment.Center) {
                if (isNap) BreathingHalo()
                ProgressRing(
                    progress = if (isPaused) 0f else timer.progress,
                    size = 292.dp,
                    stroke = 10.dp,
                    track = Color.White.copy(alpha = 0.16f),
                    accent = if (isRest) Color(0xFFB9ECD9) else Color.White.copy(alpha = 0.92f),
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        if (isLock && !showLockCountdown) {
                            Text(
                                text = "锁机",
                                style = MaterialTheme.typography.displayLarge.copy(fontSize = 46.sp),
                                color = Color.White,
                            )
                        } else if (isPaused) {
                            Text(
                                text = formatClock(timer.pauseRemainingSeconds),
                                style = MaterialTheme.typography.displayLarge.copy(
                                    fontSize = 58.sp,
                                    fontWeight = FontWeight.SemiBold,
                                ),
                                color = Color.White,
                            )
                            Text(
                                text = "暂停中",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White.copy(alpha = 0.72f),
                            )
                        } else if (timer.noTimer) {
                            Text(
                                text = "专注",
                                style = MaterialTheme.typography.displayLarge.copy(fontSize = 54.sp),
                                color = Color.White,
                            )
                            Text(
                                text = "不计时",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White.copy(alpha = 0.72f),
                            )
                        } else {
                            Text(
                                text = formatClock(timer.displaySeconds),
                                style = MaterialTheme.typography.displayLarge.copy(
                                    fontSize = 62.sp,
                                    fontWeight = FontWeight.SemiBold,
                                ),
                                color = Color.White,
                            )
                            Text(
                                text = when {
                                    timer.countUp -> "正向计时"
                                    isRest -> "休息中"
                                    else -> "${(timer.progress * 100).toInt()}% 完成"
                                },
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White.copy(alpha = 0.72f),
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.weight(0.7f))

            if (isLock) {
                LockActions(allowEmergencyUnlock = allowEmergencyUnlock, onLongPress = { confirmAbandon = true })
            } else if (isNap) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    TimerAction(
                        icon = if (timer.running) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                        label = if (timer.running) "暂停" else "继续",
                        active = false,
                        onClick = { if (timer.running) onPause() else onResume() },
                    )
                    Spacer(Modifier.width(10.dp))
                    TimerAction(
                        icon = Icons.Rounded.Check,
                        label = "结束小憩",
                        active = false,
                        onClick = onFinishEarly,
                    )
                }
            } else {
                TimerToolbar(
                    keepAwake = keepAwake,
                    ambientOn = ambientMode != null,
                    cycleEnabled = timer.cycleEnabled,
                    paused = isPaused,
                    strict = timer.strict,
                    onKeepAwake = {
                        keepAwake = !keepAwake
                        onKeepAwakeChange(keepAwake)
                        view.keepScreenOn = keepAwake
                    },
                    onAmbient = { ambientSheetOpen = true },
                    onCycle = { cycleSheetOpen = true },
                    onPause = {
                        if (timer.strict) {
                            // 严格模式不允许暂停；按钮已置灰。
                        } else if (isPaused) {
                            onResume()
                        } else {
                            onPause()
                        }
                    },
                    onSkip = {
                        if (isRest) skipRestOpen = true else skipStudyOpen = true
                    },
                )
            }

            Spacer(Modifier.height(20.dp))
            Text(
                text = when {
                    isLock -> "长按下方按钮才能放弃 · 已经坚持了 ${timer.elapsedMinutes} 分钟"
                    isNap -> "小憩不会计入专注时长"
                    isPaused -> "四分钟后会提醒你回来"
                    timer.strict -> "严格模式：不可暂停，可提前完成或取消"
                    else -> "保持呼吸 · 完成${if (timer.countUp) "后点「跳过」结束" else "后会回到主页"}"
                },
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.55f),
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(10.dp))
        }

        if (isPaused) {
            PauseOverlay(
                seconds = timer.pauseRemainingSeconds,
                onResume = {
                    showPauseDialog = false
                    onResume()
                },
            )
        }
    }

    if (showPauseDialog) {
        AlertDialog(
            onDismissRequest = {
                showPauseDialog = false
                onDismissPauseReminder()
            },
            title = { Text("休息结束，尽快恢复学习状态") },
            text = { Text("四分钟到了。回到计时页，继续完成这一轮专注。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showPauseDialog = false
                        onDismissPauseReminder()
                        onResume()
                    },
                ) {
                    Text("继续专注")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showPauseDialog = false
                        onDismissPauseReminder()
                    },
                ) {
                    Text("稍后")
                }
            },
        )
    }

    if (cycleSheetOpen) {
        CycleSheet(
            timer = timer,
            onDismiss = { cycleSheetOpen = false },
            onApply = { enabled, study, breakMinutes, count, infinite ->
                onCycleChanged(enabled, study, breakMinutes, count, infinite)
                cycleSheetOpen = false
            },
        )
    }

    if (ambientSheetOpen) {
        AmbientSheet(
            current = ambientMode,
            onDismiss = { ambientSheetOpen = false },
            onSelect = { mode ->
                ambientMode = mode
                if (mode == null) AmbientPlayer.stop() else AmbientPlayer.start(mode)
                ambientSheetOpen = false
            },
        )
    }

    if (skipStudyOpen) {
        SkipStudyDialog(
            onDismiss = { skipStudyOpen = false },
            onAbandon = {
                skipStudyOpen = false
                onAbandon()
            },
            onFinish = {
                skipStudyOpen = false
                onFinishEarly()
            },
        )
    }

    if (skipRestOpen) {
        AlertDialog(
            onDismissRequest = { skipRestOpen = false },
            title = { Text("跳过这段休息？") },
            text = {
                Text(
                    if (timer.cycleTotal > 0 && timer.cycleDone >= timer.cycleTotal) {
                        "已经是最后一轮，跳过休息会直接完成这件待办。"
                    } else {
                        "跳过休息后会直接进入下一轮学习。"
                    },
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        skipRestOpen = false
                        onSkipRest()
                    },
                ) {
                    Text("跳过休息")
                }
            },
            dismissButton = {
                TextButton(onClick = { skipRestOpen = false }) { Text("继续休息") }
            },
        )
    }

    if (confirmAbandon) {
        AlertDialog(
            onDismissRequest = { confirmAbandon = false },
            title = { Text(if (isLock) "提前结束锁机？" else "结束这次专注？") },
            text = {
                Text(
                    if (timer.elapsedMinutes >= 1) {
                        "已经专注 ${timer.elapsedMinutes} 分钟，会记进统计数据，但不算完成。"
                    } else {
                        "还没有满 1 分钟，这次不会记进统计。"
                    },
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmAbandon = false
                        onAbandon()
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
}

/* ----------------------------- toolbar ----------------------------- */

@Composable
private fun TimerToolbar(
    keepAwake: Boolean,
    ambientOn: Boolean,
    cycleEnabled: Boolean,
    paused: Boolean,
    strict: Boolean,
    onKeepAwake: () -> Unit,
    onAmbient: () -> Unit,
    onCycle: () -> Unit,
    onPause: () -> Unit,
    onSkip: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(Color.Black.copy(alpha = 0.28f))
            .border(1.dp, Color.White.copy(alpha = 0.16f), RoundedCornerShape(28.dp))
            .padding(horizontal = 6.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TimerAction(
            icon = if (keepAwake) Icons.Rounded.WbSunny else Icons.Rounded.LightMode,
            label = "常亮",
            active = keepAwake,
            onClick = onKeepAwake,
        )
        TimerAction(
            icon = if (ambientOn) Icons.Rounded.MusicNote else Icons.Rounded.VolumeOff,
            label = "背景音",
            active = ambientOn,
            onClick = onAmbient,
        )
        TimerAction(
            icon = Icons.Rounded.Repeat,
            label = "循环",
            active = cycleEnabled,
            onClick = onCycle,
        )
        TimerAction(
            icon = Icons.Rounded.Pause,
            label = if (paused) "继续" else "暂停",
            active = paused,
            enabled = !strict,
            onClick = onPause,
        )
        TimerAction(
            icon = Icons.Rounded.SkipNext,
            label = "跳过",
            active = false,
            onClick = onSkip,
        )
    }
}

@Composable
private fun TimerAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    active: Boolean,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .width(60.dp)
            .clip(RoundedCornerShape(18.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(
                    when {
                        active -> Color.White
                        enabled -> Color.White.copy(alpha = 0.12f)
                        else -> Color.White.copy(alpha = 0.06f)
                    },
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = when {
                    active -> MaterialTheme.colorScheme.primary
                    enabled -> Color.White.copy(alpha = 0.9f)
                    else -> Color.White.copy(alpha = 0.35f)
                },
                modifier = Modifier.size(19.dp),
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = if (enabled) Color.White.copy(alpha = 0.82f) else Color.White.copy(alpha = 0.35f),
            maxLines = 1,
        )
    }
}

/* ----------------------------- pause / sheets ----------------------------- */

@Composable
private fun PauseOverlay(seconds: Int, onResume: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.42f)),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Surface(
            modifier = Modifier
                .padding(horizontal = 22.dp)
                .padding(bottom = 132.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLowest,
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "暂停中 · ${formatClock(seconds)}",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "这四分钟不算专注。休息结束会提醒你回来。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(14.dp))
                Button(
                    onClick = onResume,
                    shape = CircleShape,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                    ),
                ) {
                    Text("继续专注", style = MaterialTheme.typography.titleSmall)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AmbientSheet(
    current: AmbientMode?,
    onDismiss: () -> Unit,
    onSelect: (AmbientMode?) -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        shape = SheetShape,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        dragHandle = { SheetGrabber() },
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 30.dp),
        ) {
            Text(
                text = "背景音",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "现场合成的自然声，不会下载任何音频。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(16.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                AmbientMode.entries.forEach { mode ->
                    val selected = current == mode
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(
                                if (selected) {
                                    MaterialTheme.colorScheme.primaryContainer
                                } else {
                                    MaterialTheme.colorScheme.surfaceContainerLow
                                },
                            )
                            .clickable { onSelect(mode) }
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            Icons.Rounded.MusicNote,
                            contentDescription = null,
                            tint = if (selected) {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else {
                                MaterialTheme.colorScheme.primary
                            },
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = mode.label,
                            style = MaterialTheme.typography.titleSmall,
                            color = if (selected) {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
                            modifier = Modifier.weight(1f),
                        )
                        if (selected) {
                            Icon(
                                Icons.Rounded.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                        }
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerLow)
                        .clickable { onSelect(null) }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        Icons.Rounded.VolumeOff,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = "关闭背景音",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CycleSheet(
    timer: TimerState,
    onDismiss: () -> Unit,
    onApply: (Boolean, Int, Int, Int, Boolean) -> Unit,
) {
    var enabled by remember { mutableStateOf(timer.cycleEnabled) }
    var study by remember { mutableStateOf(timer.cycleStudyMinutes) }
    var rest by remember { mutableStateOf(timer.cycleBreakMinutes) }
    var infinite by remember { mutableStateOf(timer.cycleTotal < 0) }
    var count by remember { mutableStateOf(if (timer.cycleTotal < 0) 4 else timer.cycleTotal) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        shape = SheetShape,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        dragHandle = { SheetGrabber() },
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 30.dp),
        ) {
            Text(
                text = "循环专注",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "学习 → 休息 → 学习，自动交替。修改会从下一轮开始使用。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(14.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerLow)
                    .clickable { enabled = !enabled }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "开启循环",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                )
                Switch(checked = enabled, onCheckedChange = { enabled = it })
            }
            Spacer(Modifier.height(12.dp))
            CycleStepper("学习", study, "分钟", enabled) { study = it.coerceIn(5, 180) }
            CycleStepper("休息", rest, "分钟", enabled) { rest = it.coerceIn(1, 60) }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerLow)
                    .clickable(enabled = enabled) { infinite = !infinite }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "无限循环",
                    style = MaterialTheme.typography.titleSmall,
                    color = if (enabled) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier.weight(1f),
                )
                Switch(
                    checked = infinite,
                    enabled = enabled,
                    onCheckedChange = { infinite = it },
                )
            }
            if (!infinite) {
                CycleStepper("周期", count, "轮", enabled) { count = it.coerceIn(1, 24) }
            }
            Spacer(Modifier.height(18.dp))
            Button(
                onClick = {
                    onApply(
                        enabled,
                        study,
                        rest,
                        count.coerceAtLeast(1),
                        infinite,
                    )
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                ),
            ) {
                Text("应用循环", style = MaterialTheme.typography.titleSmall)
            }
        }
    }
}

@Composable
private fun CycleStepper(
    label: String,
    value: Int,
    unit: String,
    enabled: Boolean,
    onChange: (Int) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = if (enabled) {
                MaterialTheme.colorScheme.onSurface
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            modifier = Modifier.weight(1f),
        )
        StepButton(enabled = enabled && value > 1, increase = false) { onChange(value - 1) }
        Text(
            text = "$value $unit",
            style = MaterialTheme.typography.titleSmall,
            color = if (enabled) {
                MaterialTheme.colorScheme.onSurface
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            textAlign = TextAlign.Center,
            modifier = Modifier.width(90.dp),
        )
        StepButton(enabled = enabled, increase = true) { onChange(value + 1) }
    }
}

@Composable
private fun StepButton(enabled: Boolean, increase: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = Modifier.size(30.dp),
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Icon(
                imageVector = if (increase) Icons.Rounded.Add else Icons.Rounded.Remove,
                contentDescription = null,
                modifier = Modifier.size(15.dp),
                tint = if (enabled) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                },
            )
        }
    }
}

@Composable
private fun SkipStudyDialog(
    onDismiss: () -> Unit,
    onAbandon: () -> Unit,
    onFinish: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLowest,
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = "跳过当前学习？",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "放弃会保留已专注时长但不标记完成；提前完成会标记完成并回到主页；取消则继续计时。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(18.dp))
                Button(
                    onClick = onFinish,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = CircleShape,
                ) {
                    Text("提前完成")
                }
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = onAbandon,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    ),
                ) {
                    Text("放弃本轮")
                }
                Spacer(Modifier.height(4.dp))
                TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                    Text("取消，继续计时")
                }
            }
        }
    }
}

/* ----------------------------- lock / nap ----------------------------- */

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
private fun BreathingHalo() {
    val transition = rememberInfiniteTransition(label = "breathing")
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
            .size(292.dp)
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
