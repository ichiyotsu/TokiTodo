package com.tokitodo.app.ui.history

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tokitodo.app.data.FocusSession
import com.tokitodo.app.data.SessionKind
import com.tokitodo.app.data.Stats
import com.tokitodo.app.ui.components.SheetGrabber
import com.tokitodo.app.ui.formatClock
import com.tokitodo.app.ui.formatDate
import com.tokitodo.app.ui.formatMinuteOfDay
import com.tokitodo.app.ui.formatTime
import com.tokitodo.app.ui.theme.SheetShape
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
import kotlin.math.roundToInt

private enum class HistoryTab(val label: String) {
    Records("记录"),
    Overview("周纵览"),
    Summary("周总结"),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryCenterScreen(
    sessions: List<FocusSession>,
    contentPadding: PaddingValues,
    onBack: () -> Unit,
    onUpdateSession: (String, String, Int, String) -> Unit,
    onDeleteSession: (String) -> Unit,
    onClearSessions: () -> Unit,
) {
    var tab by remember { mutableIntStateOf(0) }
    var weekAnchor by remember { mutableStateOf(LocalDate.now()) }
    var editSession by remember { mutableStateOf<FocusSession?>(null) }
    var backfillOpen by remember { mutableStateOf(false) }
    var posterOpen by remember { mutableStateOf(false) }
    var confirmClear by remember { mutableStateOf(false) }
    var menuOpen by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    onClick = onBack,
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    modifier = Modifier.size(42.dp),
                ) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Rounded.ArrowBack,
                            contentDescription = "返回",
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "专注历史记录",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = "共 ${sessions.size} 段 · ${Stats.totalMinutes(sessions)} 分钟",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Box {
                    Surface(
                        onClick = { menuOpen = true },
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                        modifier = Modifier.size(42.dp),
                    ) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Rounded.DeleteSweep,
                                contentDescription = "清空记录",
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }
                    DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                        DropdownMenuItem(
                            text = { Text("清空所有记录") },
                            onClick = {
                                menuOpen = false
                                confirmClear = true
                            },
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.padding(horizontal = 18.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                HistoryTab.entries.forEachIndexed { index, item ->
                    val selected = tab == index
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                        color = if (selected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(
                                if (selected) {
                                    MaterialTheme.colorScheme.surfaceContainerLowest
                                } else {
                                    Color.Transparent
                                },
                            )
                            .clickable { tab = index }
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                }
            }

            Spacer(Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            ) {
                when (tab) {
                    0 -> RecordsTab(
                        sessions = sessions,
                        onOpenRecord = { editSession = it },
                    )

                    1 -> WeekOverviewTab(
                        sessions = sessions,
                        anchor = weekAnchor,
                        onShiftWeek = { weekAnchor = weekAnchor.plusWeeks(it.toLong()) },
                    )

                    else -> WeekSummaryTab(
                        sessions = sessions,
                        anchor = weekAnchor,
                        onShiftWeek = { weekAnchor = weekAnchor.plusWeeks(it.toLong()) },
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.97f))
                .padding(6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            FilledTonalButton(
                onClick = { backfillOpen = true },
                shape = CircleShape,
                modifier = Modifier.weight(1f),
            ) {
                Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("补记", style = MaterialTheme.typography.labelLarge)
            }
            FilledTonalButton(
                onClick = { posterOpen = true },
                shape = CircleShape,
                modifier = Modifier.weight(1f),
            ) {
                Icon(Icons.Rounded.Share, contentDescription = null, modifier = Modifier.size(17.dp))
                Spacer(Modifier.width(6.dp))
                Text("分享今日", style = MaterialTheme.typography.labelLarge)
            }
        }
    }

    editSession?.let { session ->
        RecordEditSheet(
            session = session,
            onDismiss = { editSession = null },
            onSave = { title, minutes, note ->
                onUpdateSession(session.id, title, minutes, note)
                editSession = null
            },
            onDelete = {
                onDeleteSession(session.id)
                editSession = null
            },
        )
    }

    if (backfillOpen) {
        BackfillSheet(onDismiss = { backfillOpen = false })
    }

    if (posterOpen) {
        FocusPosterSheet(
            sessions = sessions,
            onDismiss = { posterOpen = false },
        )
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
}

/* ----------------------------- records ----------------------------- */

@Composable
private fun RecordsTab(
    sessions: List<FocusSession>,
    onOpenRecord: (FocusSession) -> Unit,
) {
    val grouped = sessions
        .sortedByDescending { it.endedAt }
        .groupBy { Stats.dateOf(it.endedAt) }
    if (grouped.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = "还没有专注记录，完成一次专注就会出现在这里",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 40.dp),
            )
        }
        return
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        grouped.forEach { (date, records) ->
            val focusRecords = records.filter { it.kind == SessionKind.Focus }
            val done = focusRecords.count { it.completed }
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLowest,
                shadowElevation = 1.dp,
            ) {
                Column {
                    Row(
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 13.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = formatDate(date),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                text = weekdayLabel(date),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${records.sumOf { it.minutes }} 分钟",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                            )
                            Text(
                                text = "完成 $done / ${focusRecords.size}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    records.forEach { session ->
                        RecordRow(session = session, onClick = { onOpenRecord(session) })
                    }
                }
            }
        }
    }
}

@Composable
private fun RecordRow(session: FocusSession, onClick: () -> Unit) {
    val (icon, tint, label) = when (session.kind) {
        SessionKind.Lock -> Triple(Icons.Rounded.History, MaterialTheme.colorScheme.tertiary, "锁机")
        SessionKind.Nap -> Triple(Icons.Rounded.History, MaterialTheme.colorScheme.secondary, "小憩")
        SessionKind.Break -> Triple(Icons.Rounded.History, MaterialTheme.colorScheme.secondary, "休息")
        SessionKind.Focus -> Triple(
            Icons.Rounded.EditNote,
            MaterialTheme.colorScheme.primary,
            if (session.completed) "完成" else "未完成",
        )
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(tint.copy(alpha = 0.13f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(17.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = session.title.ifBlank { "自由专注" },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = "${formatTime(session.startedAt)} – ${formatTime(session.endedAt)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (session.note.isNotBlank()) {
                Text(
                    text = session.note,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "${session.minutes}′",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = when (session.kind) {
                    SessionKind.Focus -> label
                    SessionKind.Lock -> "锁机"
                    SessionKind.Nap -> "小憩"
                    SessionKind.Break -> "休息"
                },
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private fun weekdayLabel(date: LocalDate): String = when (date.dayOfWeek) {
    DayOfWeek.MONDAY -> "周一"
    DayOfWeek.TUESDAY -> "周二"
    DayOfWeek.WEDNESDAY -> "周三"
    DayOfWeek.THURSDAY -> "周四"
    DayOfWeek.FRIDAY -> "周五"
    DayOfWeek.SATURDAY -> "周六"
    DayOfWeek.SUNDAY -> "周日"
}

/* ----------------------------- week overview ----------------------------- */

@Composable
private fun WeekOverviewTab(
    sessions: List<FocusSession>,
    anchor: LocalDate,
    onShiftWeek: (Int) -> Unit,
) {
    val monday = anchor.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    val weekDays = (0..6).map { monday.plusDays(it.toLong()) }
    val weekSessions = sessions.filter { Stats.dateOf(it.endedAt) in weekDays }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(bottom = 100.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "${monday.monthValue}月${monday.dayOfMonth}日 – ${monday.plusDays(6).monthValue}月${monday.plusDays(6).dayOfMonth}日",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
            WeekArrow(back = true) { onShiftWeek(-1) }
            WeekArrow(back = false) { onShiftWeek(1) }
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = "本周 ${weekSessions.sumOf { it.minutes }} 分钟 · ${weekSessions.size} 段记录",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(14.dp))
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLowest,
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                weekDays.forEach { day ->
                    WeekDayTrack(
                        day = day,
                        sessions = sessions.filter { Stats.dateOf(it.endedAt) == day },
                    )
                    Spacer(Modifier.height(8.dp))
                }
                Row(modifier = Modifier.fillMaxWidth()) {
                    Spacer(Modifier.width(48.dp))
                    Text("0", style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(1f))
                    Text("6", style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(1f))
                    Text("12", style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(1f))
                    Text("18", style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(1f))
                    Text("24", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            LegendDot(MaterialTheme.colorScheme.primary, "专注")
            LegendDot(MaterialTheme.colorScheme.tertiary, "锁机")
            LegendDot(MaterialTheme.colorScheme.secondary, "休息 / 小憩")
        }
        Spacer(Modifier.height(10.dp))
        Text(
            text = "每一行是一天，色块代表这段时间在专注。点击记录页可以编辑名称、时长和心得。",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun WeekDayTrack(day: LocalDate, sessions: List<FocusSession>) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.width(40.dp)) {
            Text(
                text = "${day.dayOfMonth}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = weekdayLabel(day).removePrefix("周"),
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        BoxWithConstraints(
            modifier = Modifier
                .weight(1f)
                .height(20.dp)
                .clip(RoundedCornerShape(7.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.6f)),
        ) {
            val trackWidth = maxWidth
            sessions.forEach { session ->
                val start = LocalDateTime.ofInstant(
                    java.time.Instant.ofEpochMilli(session.startedAt),
                    ZoneId.systemDefault(),
                )
                val startMinute = start.hour * 60 + start.minute
                val startFraction = (startMinute / (24f * 60f)).coerceIn(0f, 1f)
                val widthFraction = (session.minutes / (24f * 60f)).coerceIn(0.005f, 1f - startFraction)
                val tint = when (session.kind) {
                    SessionKind.Focus -> MaterialTheme.colorScheme.primary
                    SessionKind.Lock -> MaterialTheme.colorScheme.tertiary
                    else -> MaterialTheme.colorScheme.secondary
                }
                Box(
                    modifier = Modifier
                        .offset(x = trackWidth * startFraction)
                        .width(trackWidth * widthFraction)
                        .height(20.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(tint.copy(alpha = 0.75f)),
                )
            }
        }
    }
}

@Composable
private fun WeekArrow(back: Boolean, onClick: () -> Unit) {
    Icon(
        imageVector = if (back) Icons.Rounded.ChevronLeft else Icons.Rounded.ChevronRight,
        contentDescription = if (back) "上一周" else "下一周",
        modifier = Modifier
            .clip(CircleShape)
            .clickable(onClick = onClick)
            .padding(6.dp)
            .size(20.dp),
        tint = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(7.dp).clip(CircleShape).background(color))
        Spacer(Modifier.width(5.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/* ----------------------------- week summary ----------------------------- */

@Composable
private fun WeekSummaryTab(
    sessions: List<FocusSession>,
    anchor: LocalDate,
    onShiftWeek: (Int) -> Unit,
) {
    val monday = anchor.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    val weekDays = (0..6).map { monday.plusDays(it.toLong()) }
    val weekSessions = sessions.filter { Stats.dateOf(it.endedAt) in weekDays }
    val totalMinutes = weekSessions.sumOf { it.minutes }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(bottom = 100.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "本周总结",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
            )
            WeekArrow(back = true) { onShiftWeek(-1) }
            WeekArrow(back = false) { onShiftWeek(1) }
        }
        Spacer(Modifier.height(10.dp))
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
        ) {
            Row(modifier = Modifier.padding(18.dp)) {
                SummaryMetric("${totalMinutes}", "分钟", "本周专注", Modifier.weight(1f))
                SummaryMetric(
                    "${weekSessions.count { it.kind == SessionKind.Focus }}",
                    "次",
                    "专注次数",
                    Modifier.weight(1f),
                )
                SummaryMetric(
                    "${if (weekSessions.isEmpty()) 0 else totalMinutes / 7}",
                    "分钟",
                    "日均",
                    Modifier.weight(1f),
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        weekDays.forEach { day ->
            val daySessions = weekSessions.filter { Stats.dateOf(it.endedAt) == day }
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLowest,
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${day.monthValue}月${day.dayOfMonth}日 ${weekdayLabel(day)}",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f),
                        )
                        Text(
                            text = "${daySessions.sumOf { it.minutes }} 分钟",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                    if (daySessions.isEmpty()) {
                        Text(
                            text = "无记录",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    } else {
                        daySessions.forEach { session ->
                            Row(
                                modifier = Modifier.padding(top = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary),
                                )
                                Spacer(Modifier.width(7.dp))
                                Text(
                                    text = session.title.ifBlank { "自由专注" },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                Text(
                                    text = "${session.minutes}′",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun SummaryMetric(value: String, unit: String, label: String, modifier: Modifier = Modifier) {
    Column(modifier) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = unit,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/* ----------------------------- record editor ----------------------------- */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecordEditSheet(
    session: FocusSession,
    onDismiss: () -> Unit,
    onSave: (String, Int, String) -> Unit,
    onDelete: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var title by remember { mutableStateOf(session.title.ifBlank { "自由专注" }) }
    var minutes by remember { mutableIntStateOf(session.minutes) }
    var note by remember { mutableStateOf(session.note) }
    var confirmDelete by remember { mutableStateOf(false) }
    val minMinutes = (session.minutes - 5).coerceAtLeast(1)
    val maxMinutes = session.minutes + 5

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
        ) {
            Text(
                text = "修改专注记录",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "${formatTime(session.startedAt)} – ${formatTime(session.endedAt)} · 时长最多调整 5 分钟",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(14.dp))
            OutlinedTextField(
                value = title,
                onValueChange = { title = it.take(30) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("名称") },
                shape = RoundedCornerShape(18.dp),
                singleLine = true,
            )
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerLow)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "时长",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                )
                StepChip(enabled = minutes > minMinutes) { minutes-- }
                Text(
                    text = "$minutes 分钟",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.width(88.dp),
                )
                StepChip(enabled = minutes < maxMinutes, increase = true) { minutes++ }
            }
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = note,
                onValueChange = { note = it.take(300) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("心得 / 备注") },
                placeholder = { Text("今天这段专注有什么感受？") },
                shape = RoundedCornerShape(18.dp),
                minLines = 3,
                maxLines = 6,
            )
            Spacer(Modifier.height(18.dp))
            Button(
                onClick = { onSave(title, minutes, note) },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = CircleShape,
            ) {
                Text("保存修改")
            }
            Spacer(Modifier.height(6.dp))
            TextButton(
                onClick = { confirmDelete = true },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("删除这条记录", color = MaterialTheme.colorScheme.error)
            }
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("删除这条记录？") },
            text = { Text("删除后统计会同步减少。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmDelete = false
                        onDelete()
                    },
                ) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) { Text("取消") }
            },
        )
    }
}

@Composable
private fun StepChip(enabled: Boolean, increase: Boolean = false, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = Modifier.size(30.dp),
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = if (increase) "+" else "−",
                style = MaterialTheme.typography.titleSmall,
                color = if (enabled) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
                },
            )
        }
    }
}

/* ----------------------------- backfill (reserved) ----------------------------- */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BackfillSheet(onDismiss: () -> Unit) {
    val context = LocalContext.current
    var tab by remember { mutableIntStateOf(0) }
    var duration by remember { mutableIntStateOf(25) }
    var note by remember { mutableStateOf("") }
    var wake by remember { mutableStateOf("07:30") }
    var sleep by remember { mutableStateOf("23:30") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        shape = SheetShape,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        dragHandle = { SheetGrabber() },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 26.dp),
        ) {
            Text(
                text = "补记",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "补记过去的专注或起床 / 睡眠打卡。当前版本暂不可保存。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("专注记录", "起床睡眠").forEachIndexed { index, label ->
                    val selected = tab == index
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelMedium,
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
                            .clickable { tab = index }
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                    )
                }
            }
            Spacer(Modifier.height(14.dp))
            if (tab == 0) {
                OutlinedTextField(
                    value = "今天",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("日期") },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "补记时长",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f),
                    )
                    StepChip(enabled = duration > 5) { duration -= 5 }
                    Text(
                        text = "$duration 分钟",
                        style = MaterialTheme.typography.titleSmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.width(88.dp),
                    )
                    StepChip(enabled = duration < 180, increase = true) { duration += 5 }
                }
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it.take(100) },
                    label = { Text("心得（可选）") },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth(),
                )
            } else {
                OutlinedTextField(
                    value = wake,
                    onValueChange = { wake = it.take(5) },
                    label = { Text("起床时间") },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = sleep,
                    onValueChange = { sleep = it.take(5) },
                    label = { Text("睡眠时间") },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
            }
            Spacer(Modifier.height(18.dp))
            Button(
                onClick = {
                    Toast.makeText(context, "补记功能暂不可用", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                ),
            ) {
                Text("确定（暂不可用）")
            }
        }
    }
}
