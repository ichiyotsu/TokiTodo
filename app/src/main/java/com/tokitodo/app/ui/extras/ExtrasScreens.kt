package com.tokitodo.app.ui.extras

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MailOutline
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.WavingHand
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tokitodo.app.data.CountdownEvent
import com.tokitodo.app.data.FocusSession
import com.tokitodo.app.data.SessionKind
import com.tokitodo.app.data.Wish
import com.tokitodo.app.ui.components.CircleAction
import com.tokitodo.app.ui.components.DetailScaffold
import com.tokitodo.app.ui.components.EmptyState
import com.tokitodo.app.ui.components.SectionCard
import com.tokitodo.app.ui.components.SheetGrabber
import com.tokitodo.app.ui.formatDate
import com.tokitodo.app.ui.formatDuration
import com.tokitodo.app.ui.formatTime
import com.tokitodo.app.ui.relativeDayLabel
import com.tokitodo.app.ui.theme.LocalDarkTheme
import com.tokitodo.app.ui.theme.SheetShape
import com.tokitodo.app.ui.theme.collectionTint
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit

/* ----------------------------- history ----------------------------- */

@Composable
fun HistoryScreen(
    sessions: List<FocusSession>,
    contentPadding: PaddingValues,
    onBack: () -> Unit,
    onDeleteSession: (String) -> Unit,
    onClearSessions: () -> Unit,
) {
    var confirmClear by remember { mutableStateOf(false) }
    var pendingDelete by remember { mutableStateOf<FocusSession?>(null) }
    val grouped = sessions
        .sortedByDescending { it.endedAt }
        .groupBy { session ->
            Instant.ofEpochMilli(session.endedAt)
                .atZone(java.time.ZoneId.systemDefault())
                .toLocalDate()
        }

    DetailScaffold(
        title = "专注历史记录",
        contentPadding = contentPadding,
        onBack = onBack,
        actions = {
            CircleAction(
                icon = Icons.Rounded.DeleteSweep,
                contentDescription = "清空记录",
                onClick = { confirmClear = true },
            )
        },
    ) {
        if (sessions.isEmpty()) {
            Spacer(Modifier.height(60.dp))
            EmptyState(
                icon = Icons.Outlined.MailOutline,
                title = "还没有专注记录",
                hint = "每次专注、锁机和小憩都会出现在这里",
                actionLabel = "知道了",
                onAction = onBack,
            )
        } else {
            Text(
                text = "共 ${sessions.size} 段记录 · 累计 ${formatDuration(sessions.sumOf { it.minutes })}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 4.dp, top = 4.dp),
            )
            grouped.forEach { (date, records) ->
                SectionCard {
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 18.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = formatDate(date),
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f),
                            )
                            Text(
                                text = "${records.sumOf { it.minutes }} 分钟",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                        records.forEach { session ->
                            HistoryRow(
                                session = session,
                                onClick = { pendingDelete = session },
                            )
                        }
                    }
                }
            }
        }
    }

    if (confirmClear) {
        AlertDialog(
            onDismissRequest = { confirmClear = false },
            title = { Text("清空全部记录？") },
            text = { Text("统计会归零，待办和设置保留。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmClear = false
                        onClearSessions()
                    },
                ) {
                    Text("清空", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmClear = false }) { Text("取消") }
            },
        )
    }

    pendingDelete?.let { session ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("删除这条记录？") },
            text = {
                Text(
                    "${formatTime(session.endedAt)} · ${session.title.ifBlank { "专注" }} · ${session.minutes} 分钟",
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        pendingDelete = null
                        onDeleteSession(session.id)
                    },
                ) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) { Text("取消") }
            },
        )
    }
}

@Composable
private fun HistoryRow(session: FocusSession, onClick: () -> Unit) {
    val (icon, tint) = when (session.kind) {
        SessionKind.Lock -> Icons.Rounded.Lock to MaterialTheme.colorScheme.tertiary
        SessionKind.Nap -> Icons.Rounded.Spa to MaterialTheme.colorScheme.secondary
        SessionKind.Break -> Icons.Rounded.Timer to MaterialTheme.colorScheme.secondary
        SessionKind.Focus -> Icons.Rounded.AutoAwesome to MaterialTheme.colorScheme.primary
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(tint.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(17.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = session.title.ifBlank {
                    when (session.kind) {
                        SessionKind.Lock -> "锁机"
                        SessionKind.Nap -> "小憩"
                        SessionKind.Break -> "休息"
                        SessionKind.Focus -> "自由专注"
                    }
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = formatTime(session.endedAt),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            text = "${session.minutes}′",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

/* ----------------------------- countdown ----------------------------- */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CountdownScreen(
    events: List<CountdownEvent>,
    contentPadding: PaddingValues,
    onBack: () -> Unit,
    onAdd: (String, Long, String) -> Unit,
    onDelete: (String) -> Unit,
) {
    var addOpen by remember { mutableStateOf(false) }
    var pendingDelete by remember { mutableStateOf<CountdownEvent?>(null) }
    val today = LocalDate.now()
    val sorted = events.sortedBy { it.targetEpochDay }
    val upcoming = sorted.filter { it.targetEpochDay >= today.toEpochDay() }
    val hero = upcoming.firstOrNull() ?: sorted.lastOrNull()

    DetailScaffold(
        title = "未来倒计时",
        contentPadding = contentPadding,
        onBack = onBack,
        actions = {
            CircleAction(
                icon = Icons.Rounded.Add,
                contentDescription = "添加倒计时",
                onClick = { addOpen = true },
            )
        },
    ) {
        if (hero == null) {
            Spacer(Modifier.height(70.dp))
            EmptyState(
                icon = Icons.Rounded.EmojiEvents,
                title = "还没有倒计时",
                hint = "把考试、生日或旅行放进来，让日子有个方向",
                actionLabel = "添加一个",
                onAction = { addOpen = true },
            )
        } else {
            val target = LocalDate.ofEpochDay(hero.targetEpochDay)
            val delta = ChronoUnit.DAYS.between(today, target)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF356859), Color(0xFF7FA98E), Color(0xFFD9C48D)),
                        ),
                    ),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = hero.emoji, fontSize = 26.sp)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = hero.title,
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Spacer(Modifier.weight(1f))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = if (delta >= 0) "$delta" else "${-delta}",
                            style = MaterialTheme.typography.displayLarge.copy(
                                fontSize = 74.sp,
                                fontWeight = FontWeight.Bold,
                            ),
                            color = Color.White,
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = if (delta >= 0) "天后" else "天前",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White.copy(alpha = 0.85f),
                            modifier = Modifier.padding(bottom = 14.dp),
                        )
                    }
                    Text(
                        text = "${formatDate(target)} · ${relativeDayLabel(hero.targetEpochDay)}",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(alpha = 0.8f),
                    )
                }
            }

            if (sorted.size > 1) {
                Text(
                    text = "全部日子",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 4.dp, top = 6.dp),
                )
                SectionCard {
                    Column {
                        sorted.forEach { event ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { pendingDelete = event }
                                    .padding(horizontal = 18.dp, vertical = 13.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(text = event.emoji, fontSize = 20.sp)
                                Spacer(Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = event.title,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurface,
                                    )
                                    Text(
                                        text = formatDate(LocalDate.ofEpochDay(event.targetEpochDay)),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                                Text(
                                    text = relativeDayLabel(event.targetEpochDay),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            }
                        }
                    }
                }
            }
            Text(
                text = "点击某一行可以删除它。倒计时只在本机保存，不需要联网。",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.padding(horizontal = 4.dp),
            )
        }
    }

    if (addOpen) {
        AddCountdownSheet(
            onDismiss = { addOpen = false },
            onSave = { title, epochDay, emoji ->
                onAdd(title, epochDay, emoji)
                addOpen = false
            },
        )
    }

    pendingDelete?.let { event ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("删除这个倒计时？") },
            text = { Text("${event.emoji} ${event.title} · ${formatDate(LocalDate.ofEpochDay(event.targetEpochDay))}") },
            confirmButton = {
                TextButton(
                    onClick = {
                        pendingDelete = null
                        onDelete(event.id)
                    },
                ) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) { Text("取消") }
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddCountdownSheet(
    onDismiss: () -> Unit,
    onSave: (String, Long, String) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var title by remember { mutableStateOf("") }
    var emoji by remember { mutableStateOf("🎯") }
    var epochDay by remember { mutableStateOf(LocalDate.now().plusDays(30).toEpochDay()) }
    var dateOpen by remember { mutableStateOf(false) }
    val emojis = listOf("🎯", "📚", "🎂", "✈️", "🏆", "🌱")

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
                .padding(horizontal = 24.dp)
                .padding(bottom = 28.dp),
        ) {
            Text(
                text = "添加倒计时",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(16.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                emojis.forEach { option ->
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(
                                if (option == emoji) {
                                    MaterialTheme.colorScheme.primaryContainer
                                } else {
                                    MaterialTheme.colorScheme.surfaceContainerLow
                                },
                            )
                            .clickable { emoji = option },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(text = option, fontSize = 20.sp)
                    }
                }
            }
            Spacer(Modifier.height(14.dp))
            OutlinedTextField(
                value = title,
                onValueChange = { title = it.take(20) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("要期待的日子") },
                placeholder = { Text("例如：期末周结束") },
                shape = RoundedCornerShape(18.dp),
                singleLine = true,
            )
            Spacer(Modifier.height(14.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerLow)
                    .clickable { dateOpen = true }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "目标日期",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = formatDate(LocalDate.ofEpochDay(epochDay)),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            Spacer(Modifier.height(22.dp))
            Button(
                onClick = { onSave(title.trim().ifBlank { "一个重要日子" }, epochDay, emoji) },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                ),
            ) {
                Text("保存", style = MaterialTheme.typography.titleSmall)
            }
        }
    }

    if (dateOpen) {
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = epochDay * 86_400_000L,
        )
        DatePickerDialog(
            onDismissRequest = { dateOpen = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        pickerState.selectedDateMillis?.let { millis ->
                            epochDay = LocalDate.ofEpochDay(millis / 86_400_000L).toEpochDay()
                        }
                        dateOpen = false
                    },
                ) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(onClick = { dateOpen = false }) { Text("取消") }
            },
        ) {
            DatePicker(state = pickerState)
        }
    }
}

/* ----------------------------- wish wall ----------------------------- */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WishWallScreen(
    wishes: List<Wish>,
    contentPadding: PaddingValues,
    onBack: () -> Unit,
    onAdd: (String) -> Unit,
    onToggle: (String) -> Unit,
    onDelete: (String) -> Unit,
) {
    var addOpen by remember { mutableStateOf(false) }
    var pendingDelete by remember { mutableStateOf<Wish?>(null) }
    val dark = LocalDarkTheme.current
    var text by remember { mutableStateOf("") }

    DetailScaffold(
        title = "自习室 · 许愿墙",
        contentPadding = contentPadding,
        onBack = onBack,
        actions = {
            CircleAction(
                icon = Icons.Rounded.Add,
                contentDescription = "写一个愿望",
                onClick = { addOpen = true },
            )
        },
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(26.dp))
                .background(
                    Brush.linearGradient(
                        listOf(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                            MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.55f),
                        ),
                    ),
                )
                .padding(20.dp),
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Rounded.WavingHand,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "把想做的事写下来",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "专注之前看一眼，让自己知道为什么坐在这里。许愿墙只保存在本机。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        if (wishes.isEmpty()) {
            EmptyState(
                icon = Icons.Rounded.Spa,
                title = "许愿墙还是空的",
                hint = "写下一个小愿望，完成后可以打勾",
                actionLabel = "写下第一个愿望",
                onAction = { addOpen = true },
            )
        } else {
            Text(
                text = "${wishes.count { it.done }} / ${wishes.size} 已经实现",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 4.dp),
            )
            wishes.forEachIndexed { index, wish ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
                        .background(collectionTint(index, dark))
                        .clickable { pendingDelete = wish }
                        .padding(horizontal = 18.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Surface(
                        onClick = { onToggle(wish.id) },
                        shape = CircleShape,
                        color = if (wish.done) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.8f)
                        },
                        contentColor = if (wish.done) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.size(30.dp),
                    ) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Rounded.Check,
                                contentDescription = "标记完成",
                                modifier = Modifier.size(17.dp),
                            )
                        }
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = wish.text,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            textDecoration = if (wish.done) TextDecoration.LineThrough else null,
                        )
                        Text(
                            text = if (wish.done) {
                                "已经实现"
                            } else {
                                "写于 " + formatDate(
                                    Instant.ofEpochMilli(wish.createdAt)
                                        .atZone(java.time.ZoneId.systemDefault())
                                        .toLocalDate(),
                                )
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
            Text(
                text = "点击卡片可以删除。",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                modifier = Modifier.padding(start = 4.dp),
            )
        }
    }

    if (addOpen) {
        ModalBottomSheet(
            onDismissRequest = { addOpen = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            shape = SheetShape,
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
            dragHandle = { SheetGrabber() },
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 28.dp),
            ) {
                Text(
                    text = "写一个愿望",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.height(14.dp))
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it.take(80) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("例如：这学期读完 12 本书") },
                    minLines = 2,
                    shape = RoundedCornerShape(18.dp),
                )
                Spacer(Modifier.height(18.dp))
                Button(
                    onClick = {
                        onAdd(text)
                        text = ""
                        addOpen = false
                    },
                    enabled = text.isNotBlank(),
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = CircleShape,
                ) {
                    Text("挂到墙上", style = MaterialTheme.typography.titleSmall)
                }
            }
        }
    }

    pendingDelete?.let { wish ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("删除这个愿望？") },
            text = { Text(wish.text) },
            confirmButton = {
                TextButton(
                    onClick = {
                        pendingDelete = null
                        onDelete(wish.id)
                    },
                ) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) { Text("取消") }
            },
        )
    }
}

/* ----------------------------- vip ----------------------------- */

@Composable
fun VipScreen(
    contentPadding: PaddingValues,
    onBack: () -> Unit,
    onGoTodos: () -> Unit,
) {
    DetailScaffold(
        title = "季度卡",
        contentPadding = contentPadding,
        onBack = onBack,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(32.dp))
                .background(
                    Brush.linearGradient(
                        listOf(Color(0xFF2F4F44), Color(0xFF5E7F63), Color(0xFFC9A86A)),
                    ),
                ),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White.copy(alpha = 0.22f))
                            .padding(horizontal = 7.dp, vertical = 3.dp),
                    ) {
                        Text(
                            text = "VIP",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "TokiToDo 完整版",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                    )
                }
                Spacer(Modifier.weight(1f))
                Text(
                    text = "当前版本已包含全部功能",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "这是本地构建的完整版本，没有内购，也不会过期。",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.82f),
                )
            }
        }
        SectionCard {
            Column(Modifier.padding(20.dp)) {
                Text(
                    text = "包含的功能",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.height(12.dp))
                listOf(
                    "无限待办与待办集",
                    "插画封面与四套主题配色",
                    "日 / 周 / 月 / 自定义统计",
                    "月度热力图与专注历史",
                    "锁机时段与学霸模式",
                    "本地备份导出，无广告",
                ).forEach { line ->
                    Row(
                        modifier = Modifier.padding(vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                Icons.Rounded.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(14.dp),
                            )
                        }
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = line,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                Spacer(Modifier.height(18.dp))
                Button(
                    onClick = onGoTodos,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                    ),
                ) {
                    Text("回到待办", style = MaterialTheme.typography.titleSmall)
                }
            }
        }
    }
}

/* ----------------------------- study mode ----------------------------- */

@Composable
fun StudyModeScreen(
    enabled: Boolean,
    contentPadding: PaddingValues,
    onBack: () -> Unit,
    onToggle: (Boolean) -> Unit,
) {
    DetailScaffold(
        title = "学霸模式",
        contentPadding = contentPadding,
        onBack = onBack,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(190.dp)
                .clip(RoundedCornerShape(32.dp))
                .background(
                    Brush.linearGradient(
                        if (enabled) {
                            listOf(Color(0xFF2F6355), Color(0xFF6AA88F), Color(0xFFF2D9A0))
                        } else {
                            listOf(Color(0xFF4E635C), Color(0xFF8FA79B), Color(0xFFD9D2BE))
                        },
                    ),
                ),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
            ) {
                Icon(
                    imageVector = Icons.Rounded.AutoAwesome,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(30.dp),
                )
                Spacer(Modifier.weight(1f))
                Text(
                    text = if (enabled) "学霸模式已开启" else "给专注加一道锁",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = if (enabled) {
                        "接下来开始的专注不能暂停，也不建议退出。"
                    } else {
                        "开启后，专注过程中不能暂停；想结束需要先完成或主动放弃。"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.85f),
                )
            }
        }
        SectionCard {
            Column(Modifier.padding(20.dp)) {
                Text(
                    text = "学霸模式会做什么",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.height(10.dp))
                listOf(
                    "专注计时中隐藏暂停按钮",
                    "放弃专注需要二次确认",
                    "锁机卡片会提醒你当前处于冲刺状态",
                    "不会限制你使用其他应用，真正的锁机请到「锁机」页",
                ).forEach { line ->
                    Row(
                        modifier = Modifier.padding(vertical = 5.dp),
                        verticalAlignment = Alignment.Top,
                    ) {
                        Icon(
                            Icons.Rounded.AutoAwesome,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                            modifier = Modifier.size(13.dp).padding(top = 3.dp),
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = line,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                Spacer(Modifier.height(18.dp))
                Button(
                    onClick = { onToggle(!enabled) },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (enabled) {
                            MaterialTheme.colorScheme.surfaceContainerHigh
                        } else {
                            MaterialTheme.colorScheme.primary
                        },
                        contentColor = if (enabled) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.onPrimary
                        },
                    ),
                ) {
                    Icon(
                        imageVector = if (enabled) Icons.Rounded.Lock else Icons.Rounded.AutoAwesome,
                        contentDescription = null,
                        modifier = Modifier.size(17.dp),
                    )
                    Spacer(Modifier.width(7.dp))
                    Text(
                        text = if (enabled) "关闭学霸模式" else "开启学霸模式",
                        style = MaterialTheme.typography.titleSmall,
                    )
                }
            }
        }
        SectionCard {
            Column(Modifier.padding(20.dp)) {
                Text(
                    text = "和锁机的区别",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "学霸模式约束的是计时器本身；锁机会把整个应用切到锁机页面，并要求你放下手机。" +
                        "需要更强的自控时，去「锁机」页开一次快速锁机。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
