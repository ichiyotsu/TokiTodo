package com.tokitodo.app.ui.lock

import android.app.TimePickerDialog
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bedtime
import androidx.compose.material.icons.outlined.MailOutline
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tokitodo.app.data.AppSettings
import com.tokitodo.app.data.LockSchedule
import com.tokitodo.app.ui.components.CircleAction
import com.tokitodo.app.ui.components.EmptyState
import com.tokitodo.app.ui.components.PageTopBar
import com.tokitodo.app.ui.components.PillAction
import com.tokitodo.app.ui.components.SectionCard
import com.tokitodo.app.ui.components.SheetGrabber
import com.tokitodo.app.ui.formatMinuteOfDay
import com.tokitodo.app.ui.theme.LockCardShape
import com.tokitodo.app.ui.theme.LocalDarkTheme
import com.tokitodo.app.ui.theme.SheetShape
import com.tokitodo.app.ui.weekdayShort

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LockScreen(
    schedules: List<LockSchedule>,
    settings: AppSettings,
    contentPadding: PaddingValues,
    onStartLock: (Int) -> Unit,
    onAddSchedule: (String, Int, Int, Set<Int>) -> Unit,
    onToggleSchedule: (String, Boolean) -> Unit,
    onDeleteSchedule: (String) -> Unit,
    onUpdateSettings: ((AppSettings) -> AppSettings) -> Unit,
    onOpenWhitelist: () -> Unit,
) {
    var lockOpen by remember { mutableStateOf(false) }
    var settingsOpen by remember { mutableStateOf(false) }
    var permissionOpen by remember { mutableStateOf(false) }
    var menuOpen by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                if (LocalDarkTheme.current) {
                    Color(0xFF070B0A)
                } else {
                    MaterialTheme.colorScheme.surface
                },
            )
            .padding(contentPadding)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 152.dp),
    ) {
        PageTopBar(
            actions = {
                PillAction(
                    label = "必开权限",
                    icon = Icons.Rounded.Settings,
                    onClick = { permissionOpen = true },
                    container = MaterialTheme.colorScheme.surfaceContainerLowest,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                CircleAction(
                    icon = Icons.Rounded.Lock,
                    contentDescription = "开始锁机",
                    onClick = { lockOpen = true },
                )
                CircleAction(
                    icon = Icons.Rounded.Apps,
                    contentDescription = "锁机白名单",
                    onClick = onOpenWhitelist,
                )
                Box {
                    CircleAction(
                        icon = Icons.Rounded.MoreVert,
                        contentDescription = "更多",
                        onClick = { menuOpen = true },
                    )
                    DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                        DropdownMenuItem(
                            text = { Text("锁机设置") },
                            onClick = {
                                menuOpen = false
                                settingsOpen = true
                            },
                        )
                        DropdownMenuItem(
                            text = { Text("权限说明") },
                            onClick = {
                                menuOpen = false
                                permissionOpen = true
                            },
                        )
                        DropdownMenuItem(
                            text = { Text("自定义锁机时间段") },
                            onClick = {
                                menuOpen = false
                            },
                            enabled = false,
                        )
                    }
                }
            },
        )

        Spacer(Modifier.height(14.dp))
        QuickLockCard(onClick = { lockOpen = true })
        Spacer(Modifier.height(12.dp))
        ScheduleUnavailableCard()

        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp),
            horizontalArrangement = Arrangement.End,
        ) {
            PillAction(
                label = "提升强制性选项 | 锁机设置",
                icon = Icons.Rounded.Settings,
                onClick = { settingsOpen = true },
                container = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
            )
        }

    }
    if (lockOpen) {
        QuickLockSheet(
            onDismiss = { lockOpen = false },
            onStart = { minutes ->
                lockOpen = false
                onStartLock(minutes)
            },
        )
    }
    if (settingsOpen) {
        LockSettingsSheet(
            settings = settings,
            onDismiss = { settingsOpen = false },
            onUpdate = onUpdateSettings,
        )
    }
    if (permissionOpen) {
        PermissionDialog(onDismiss = { permissionOpen = false })
    }
}

/* ----------------------------- cards ----------------------------- */

@Composable
private fun NapCard(onClick: () -> Unit) {
    GradientCard(
        brush = Brush.linearGradient(
            listOf(Color(0xFF2FBF9E), Color(0xFF68D6AD)),
        ),
        onClick = onClick,
        height = 92.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.22f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Rounded.Spa,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(23.dp),
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "小憩",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                )
                Text(
                    text = "放松身心的深呼吸，感受禅修般的宁静",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.85f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.8f),
            )
        }
    }
}

@Composable
private fun QuickLockCard(onClick: () -> Unit) {
    GradientCard(
        brush = Brush.linearGradient(
            listOf(Color(0xFFFF7059), Color(0xFFFFA07A)),
        ),
        onClick = onClick,
        height = 140.dp,
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 22.dp)
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.18f))
                    .border(1.dp, Color.White.copy(alpha = 0.28f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Rounded.Lock,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(26.dp),
                )
            }
            Column(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 84.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.Lock,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "立刻开始快速锁机",
                        style = MaterialTheme.typography.titleLarge.copy(fontSize = 19.sp),
                        color = Color.White,
                    )
                }
                Spacer(Modifier.height(7.dp))
                Text(
                    text = "选择锁机时长，并立刻开始锁机",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.88f),
                )
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.24f))
                        .padding(horizontal = 13.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "开始锁机",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                    )
                    Icon(
                        imageVector = Icons.Rounded.ChevronRight,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(15.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun ScheduleUnavailableCard() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(LockCardShape)
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(horizontal = 18.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainerHigh),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Rounded.Settings,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(21.dp),
            )
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "自定义锁机时间段",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "暂不支持该功能",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun QuickScheduleCard(onClick: () -> Unit) {
    GradientCard(
        brush = Brush.linearGradient(
            listOf(Color(0xFF3B82F6), Color(0xFF6FB6FF)),
        ),
        onClick = onClick,
        height = 104.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.22f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(25.dp),
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "添加自定义锁机时间段",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "在指定时间范围内锁住手机，不做熬夜手机党",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.85f),
                )
            }
        }
    }
}

@Composable
private fun GradientCard(
    brush: Brush,
    onClick: () -> Unit,
    height: androidx.compose.ui.unit.Dp,
    content: @Composable androidx.compose.foundation.layout.BoxScope.() -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .height(height)
            .shadow(8.dp, LockCardShape, clip = false)
            .clip(LockCardShape)
            .background(brush)
            .clickable(
                interactionSource = interaction,
                indication = androidx.compose.material3.ripple(color = Color.White),
                onClick = onClick,
            ),
    ) {
        content()
    }
}

@Composable
private fun LockIllustration(modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        drawCircle(
            brush = Brush.radialGradient(
                listOf(Color.White.copy(alpha = 0.22f), Color.Transparent),
                center = Offset(w * 0.55f, h * 0.55f),
                radius = w * 0.5f,
            ),
            radius = w * 0.5f,
            center = Offset(w * 0.55f, h * 0.55f),
        )
        // 锁钩
        drawArc(
            color = Color(0xFF7A4A28).copy(alpha = 0.55f),
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(w * 0.27f, h * 0.16f),
            size = Size(w * 0.46f, h * 0.50f),
            style = Stroke(width = w * 0.11f, cap = StrokeCap.Round),
        )
        drawArc(
            color = Color(0xFFF2C87C),
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(w * 0.30f, h * 0.18f),
            size = Size(w * 0.40f, h * 0.44f),
            style = Stroke(width = w * 0.085f, cap = StrokeCap.Round),
        )
        // 锁体
        drawRoundRect(
            brush = Brush.verticalGradient(
                listOf(Color(0xFFFFD98A), Color(0xFFF0A94B)),
                startY = h * 0.40f,
                endY = h * 0.95f,
            ),
            topLeft = Offset(w * 0.16f, h * 0.40f),
            size = Size(w * 0.68f, h * 0.55f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.14f),
        )
        drawRoundRect(
            color = Color.White.copy(alpha = 0.35f),
            topLeft = Offset(w * 0.22f, h * 0.45f),
            size = Size(w * 0.30f, h * 0.07f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.03f),
        )
        // 钥匙孔
        drawCircle(
            color = Color(0xFF8A532B),
            radius = w * 0.075f,
            center = Offset(w * 0.50f, h * 0.64f),
        )
        drawRoundRect(
            color = Color(0xFF8A532B),
            topLeft = Offset(w * 0.468f, h * 0.66f),
            size = Size(w * 0.064f, h * 0.13f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.03f),
        )
        for (i in 0 until 14) {
            val x = w * (0.20f + (i * 0.047f) % 0.58f)
            val y = h * (0.46f + (i * 0.063f) % 0.42f)
            drawCircle(Color.White.copy(alpha = 0.13f), radius = 1.2f, center = Offset(x, y))
        }
    }
}

/* ----------------------------- schedule card ----------------------------- */

@Composable
private fun ScheduleCard(
    schedule: LockSchedule,
    onToggle: (Boolean) -> Unit,
    onDelete: () -> Unit,
) {
    var confirm by remember { mutableStateOf(false) }
    val daysText = if (schedule.days.size == 7) {
        "每天"
    } else {
        schedule.days.sorted().joinToString(" ") { "周${weekdayShort(it)}" }
    }
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        shadowElevation = 1.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        if (schedule.enabled) {
                            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.16f)
                        } else {
                            MaterialTheme.colorScheme.surfaceContainerHigh
                        },
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Rounded.Lock,
                    contentDescription = null,
                    tint = if (schedule.enabled) {
                        MaterialTheme.colorScheme.tertiary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier.size(20.dp),
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { confirm = true },
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${formatMinuteOfDay(schedule.startMinute)} – ${formatMinuteOfDay(schedule.endMinute)}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = schedule.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Text(
                    text = daysText,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                )
            }
            Switch(checked = schedule.enabled, onCheckedChange = onToggle)
        }
    }
    if (confirm) {
        AlertDialog(
            onDismissRequest = { confirm = false },
            title = { Text("删除这个锁机时段？") },
            text = { Text("删除后这个时间段不再自动锁机，已经开始的锁机会继续。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirm = false
                        onDelete()
                    },
                ) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirm = false }) { Text("取消") }
            },
        )
    }
}

/* ----------------------------- sheets ----------------------------- */

@Composable
private fun NapSheet(onDismiss: () -> Unit, onStart: (Int) -> Unit) {
    var minutes by remember { mutableIntStateOf(10) }
    ModalBottomSheetShell(onDismiss = onDismiss) {
        Text(
            text = "小憩",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "几分钟的深呼吸就够了。小憩结束会记录在专注历史里，但不计入专注时长。",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(20.dp))
        DurationRow(options = listOf(5, 10, 15, 20), value = minutes, onSelect = { minutes = it })
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = { onStart(minutes) },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
            ),
        ) {
            Text("开始 $minutes 分钟小憩", style = MaterialTheme.typography.titleSmall)
        }
    }
}

@Composable
private fun QuickLockSheet(onDismiss: () -> Unit, onStart: (Int) -> Unit) {
    var minutes by remember { mutableIntStateOf(30) }
    ModalBottomSheetShell(onDismiss = onDismiss) {
        Text(
            text = "快速锁机",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "锁机期间会停留在锁机页面。系统级顶层锁机需要悬浮窗授权，当前版本先使用应用内全屏锁机。时长可设置为 2–60 分钟。",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(20.dp))
        DurationRow(
            options = listOf(5, 10, 15, 20, 30, 45, 60),
            value = minutes,
            onSelect = { minutes = it },
        )
        Spacer(Modifier.height(14.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                onClick = { minutes = (minutes - 1).coerceAtLeast(2) },
                enabled = minutes > 2,
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                modifier = Modifier.size(36.dp),
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Icon(Icons.Rounded.Remove, contentDescription = "减少", modifier = Modifier.size(17.dp))
                }
            }
            Text(
                text = "$minutes 分钟",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.width(130.dp),
            )
            Surface(
                onClick = { minutes = (minutes + 1).coerceAtMost(60) },
                enabled = minutes < 60,
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                modifier = Modifier.size(36.dp),
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Icon(Icons.Rounded.Add, contentDescription = "增加", modifier = Modifier.size(17.dp))
                }
            }
        }
        Spacer(Modifier.height(18.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f))
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                Icons.Rounded.Lock,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.size(18.dp),
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = "请提前放下手机，把这段时间留给自己。",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onTertiaryContainer,
            )
        }
        Spacer(Modifier.height(22.dp))
        Button(
            onClick = { onStart(minutes) },
            modifier = Modifier.fillMaxWidth().height(54.dp),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.tertiary,
                contentColor = MaterialTheme.colorScheme.onTertiary,
            ),
        ) {
            Text("锁机 $minutes 分钟", style = MaterialTheme.typography.titleSmall)
        }
    }
}

@Composable
private fun DurationRow(options: List<Int>, value: Int, onSelect: (Int) -> Unit) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        options.forEach { option ->
            val selected = option == value
            Text(
                text = "$option 分钟",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = if (selected) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(
                        if (selected) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceContainerLow
                        },
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                    ) { onSelect(option) }
                    .padding(horizontal = 14.dp, vertical = 10.dp),
            )
        }
    }
}

@Composable
private fun ScheduleSheet(
    onDismiss: () -> Unit,
    onSave: (String, Int, Int, Set<Int>) -> Unit,
) {
    val context = LocalContext.current
    var label by remember { mutableStateOf("夜间锁机") }
    var start by remember { mutableIntStateOf(23 * 60) }
    var end by remember { mutableIntStateOf(7 * 60) }
    var days by remember { mutableStateOf(setOf(1, 2, 3, 4, 5, 6, 7)) }

    fun pick(initial: Int, onPicked: (Int) -> Unit) {
        TimePickerDialog(
            context,
            { _, hour, minute -> onPicked(hour * 60 + minute) },
            initial / 60,
            initial % 60,
            true,
        ).show()
    }

    ModalBottomSheetShell(onDismiss = onDismiss) {
        Text(
            text = "自定义锁机时间段",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "时间可以跨过零点，例如 23:00 – 07:00。到达开始时间后会进入锁机页面。",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(18.dp))
        OutlinedTextField(
            value = label,
            onValueChange = { label = it.take(12) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("名称") },
            shape = RoundedCornerShape(18.dp),
            singleLine = true,
        )
        Spacer(Modifier.height(14.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            TimeField(
                label = "开始",
                value = formatMinuteOfDay(start),
                modifier = Modifier.weight(1f),
                onClick = { pick(start) { start = it } },
            )
            TimeField(
                label = "结束",
                value = formatMinuteOfDay(end),
                modifier = Modifier.weight(1f),
                onClick = { pick(end) { end = it } },
            )
        }
        Spacer(Modifier.height(18.dp))
        Text(
            text = "重复",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(8.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            (1..7).forEach { day ->
                val selected = days.contains(day)
                Text(
                    text = "周${weekdayShort(day)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (selected) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(
                            if (selected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.surfaceContainerLow
                            },
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                        ) {
                            days = if (selected) days - day else days + day
                        }
                        .padding(horizontal = 11.dp, vertical = 8.dp),
                )
            }
        }
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = { onSave(label, start, end, days) },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = CircleShape,
            enabled = days.isNotEmpty(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
            ),
        ) {
            Text("保存时间段", style = MaterialTheme.typography.titleSmall)
        }
    }
}

@Composable
private fun TimeField(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.SemiBold,
            ),
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun LockSettingsSheet(
    settings: AppSettings,
    onDismiss: () -> Unit,
    onUpdate: ((AppSettings) -> AppSettings) -> Unit,
) {
    ModalBottomSheetShell(onDismiss = onDismiss) {
        Text(
            text = "锁机设置",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "锁机是比学霸模式更严格的自控功能：它不依赖你的意志力，而是直接把你留在锁机页面。",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(18.dp))
        SectionCard {
            ToggleLine(
                title = "锁机时屏蔽通知",
                subtitle = "减少通知带来的分心",
                checked = settings.lockBlockNotifications,
                onCheckedChange = { value ->
                    onUpdate { it.copy(lockBlockNotifications = value) }
                },
            )
            ToggleLine(
                title = "允许紧急解锁",
                subtitle = "关闭后只能等待锁机结束",
                checked = settings.lockAllowEmergency,
                onCheckedChange = { value ->
                    onUpdate { it.copy(lockAllowEmergency = value) }
                },
            )
            ToggleLine(
                title = "显示剩余时间",
                subtitle = "锁机页显示倒计时，关闭后只显示一句话",
                checked = settings.lockShowCountdown,
                onCheckedChange = { value ->
                    onUpdate { it.copy(lockShowCountdown = value) }
                },
            )
            ToggleLine(
                title = "严格模式",
                subtitle = "专注时禁止暂停，只能提前完成或取消",
                checked = settings.strictMode,
                onCheckedChange = { value -> onUpdate { it.copy(strictMode = value) } },
            )
        }
        Spacer(Modifier.height(20.dp))
        TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
            Text("完成")
        }
    }
}

@Composable
private fun ToggleLine(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModalBottomSheetShell(
    onDismiss: () -> Unit,
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = SheetShape,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        dragHandle = { SheetGrabber() },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 28.dp),
            content = content,
        )
    }
}

@Composable
private fun PermissionDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("关于锁机权限") },
        text = {
            Text(
                "应用内的锁机页面不需要任何权限。若希望离开 TokiTodo 之后仍然被拉回锁机页，" +
                    "需要在系统设置里授予「使用情况访问」或「无障碍」权限；要覆盖在其他应用之上，" +
                    "还需要「悬浮窗」权限。这些权限完全由你决定，TokiTodo 不会上传任何使用数据。",
            )
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("知道了") }
        },
    )
}
