package com.tokitodo.app.ui.stats

import android.app.TimePickerDialog
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Sort
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tokitodo.app.data.FocusSession
import com.tokitodo.app.data.SessionKind
import com.tokitodo.app.data.Stats
import com.tokitodo.app.ui.components.BarDatum
import com.tokitodo.app.ui.components.CircleAction
import com.tokitodo.app.ui.components.FocusBarChart
import com.tokitodo.app.ui.components.LineChart
import com.tokitodo.app.ui.components.Metric
import com.tokitodo.app.ui.components.PillAction
import com.tokitodo.app.ui.components.PillSegmented
import com.tokitodo.app.ui.components.SectionCard
import com.tokitodo.app.ui.formatDate
import com.tokitodo.app.ui.formatDuration
import com.tokitodo.app.ui.history.FocusPosterSheet
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.TemporalAdjusters
import kotlin.math.max

private val sectionOrder = listOf(
    "summary",
    "pie",
    "hourly",
    "monthly",
    "wake",
    "foreground",
    "sleep",
    "interruptions",
    "yearly",
    "heatmap",
)

private data class AnalyticsPie(val label: String, val minutes: Int, val color: Color)

/**
 * 通用数据统计页。待办 / 待办集 / 统计 Tab 共用同一套内容，
 * 只是通过 [showSummary] 决定是否展示累计与当日数据。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    sessions: List<FocusSession>,
    title: String? = null,
    contentPadding: PaddingValues,
    showBack: Boolean = false,
    showSummary: Boolean = true,
    onBack: () -> Unit = {},
    onOpenHistory: () -> Unit = {},
    onOpenWhitelist: (() -> Unit)? = null,
    reminderMinute: Int? = null,
    onSetReminder: ((Int?) -> Unit)? = null,
) {
    val context = LocalContext.current
    val today = LocalDate.now()
    var day by remember { mutableStateOf(today) }
    var pieAnchor by remember { mutableStateOf(today) }
    var pieRange by remember { mutableIntStateOf(0) }
    var customStart by remember { mutableStateOf(today.minusDays(6)) }
    var customEnd by remember { mutableStateOf(today) }
    var customPick by remember { mutableStateOf<Int?>(null) }
    var hourlyMonth by remember { mutableStateOf(YearMonth.from(today)) }
    var monthlyMonth by remember { mutableStateOf(YearMonth.from(today)) }
    var wakeMonth by remember { mutableStateOf(YearMonth.from(today)) }
    var foregroundMonth by remember { mutableStateOf(YearMonth.from(today)) }
    var sleepMonth by remember { mutableStateOf(YearMonth.from(today)) }
    var interruptionMonth by remember { mutableStateOf(YearMonth.from(today)) }
    var yearlyYear by remember { mutableStateOf(today.year) }
    var weekAnchor by remember { mutableStateOf(today) }
    var shareOpen by remember { mutableStateOf(false) }
    var sortOpen by remember { mutableStateOf(false) }
    var order by remember { mutableStateOf(sectionOrder) }

    val counted = Stats.counted(sessions)
    val totalMinutes = Stats.totalMinutes(sessions)
    val totalCount = Stats.totalCount(sessions)
    val average = Stats.dailyAverage(sessions, today)
    val dayMinutes = Stats.minutesOn(sessions, day)
    val dayCount = Stats.countOn(sessions, day)

    val pieEnd = when (pieRange) {
        0 -> pieAnchor
        1 -> pieAnchor.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
        2 -> YearMonth.from(pieAnchor).atEndOfMonth()
        else -> customEnd
    }
    val pieStart = when (pieRange) {
        0 -> pieAnchor
        1 -> pieAnchor.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        2 -> YearMonth.from(pieAnchor).atDay(1)
        else -> customStart
    }
    val pieSessions = counted.filter {
        val date = Stats.dateOf(it.endedAt)
        !date.isBefore(pieStart) && !date.isAfter(pieEnd)
    }
    val pie = pieSessions.groupBy { it.title.ifBlank { "自由专注" } }
        .map { (label, list) -> label to list.sumOf { it.minutes } }
        .sortedByDescending { it.second }
        .take(6)
        .mapIndexed { index, (label, minutes) ->
            AnalyticsPie(
                label = label,
                minutes = minutes,
                color = pieColors[index % pieColors.size],
            )
        }
    val pieTotal = pie.sumOf { it.minutes }.coerceAtLeast(1)

    val hourlyBuckets = IntArray(8)
    counted.filter { YearMonth.from(Stats.dateOf(it.endedAt)) == hourlyMonth }
        .forEach { hourlyBuckets[Stats.hourOf(it.endedAt) / 3] += it.minutes }

    val monthDays = Stats.month(sessions, monthlyMonth)
    val wakeBuckets = wakeBuckets(sessions, wakeMonth)
    val sleepBuckets = sleepBuckets(sessions, sleepMonth)
    val foregroundValues = foregroundByDay(sessions, foregroundMonth)
    val interruptions = interruptionBuckets(sessions, interruptionMonth)
    val yearMonths = (1..12).map { month ->
        Stats.monthTotal(sessions, YearMonth.of(yearlyYear, month))
    }
    val weekStart = weekAnchor.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    val weekValues = (0..6).map { offset ->
        Stats.minutesOn(sessions, weekStart.plusDays(offset.toLong()))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    0f to MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.42f),
                    0.28f to MaterialTheme.colorScheme.surface,
                ),
            )
            .verticalScroll(rememberScrollState())
            .padding(contentPadding)
            .padding(bottom = 150.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            if (showBack || !title.isNullOrBlank()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (showBack) {
                        CircleAction(
                            icon = Icons.Rounded.ArrowBack,
                            contentDescription = "返回",
                            onClick = onBack,
                            container = MaterialTheme.colorScheme.surfaceContainerLow,
                            size = 42,
                        )
                        Spacer(Modifier.width(10.dp))
                    }
                    if (!title.isNullOrBlank()) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                        )
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (onSetReminder != null) {
                    CircleAction(
                        icon = Icons.Rounded.Notifications,
                        contentDescription = "定时提醒",
                        onClick = {
                            val initial = reminderMinute ?: (9 * 60)
                            TimePickerDialog(
                                context,
                                { _, hour, minute ->
                                    onSetReminder(hour * 60 + minute)
                                },
                                initial / 60,
                                initial % 60,
                                true,
                            ).show()
                        },
                        container = MaterialTheme.colorScheme.surfaceContainerLow,
                        size = 42,
                    )
                    Spacer(Modifier.width(6.dp))
                }
                if (onOpenWhitelist != null) {
                    CircleAction(
                        icon = Icons.Rounded.Apps,
                        contentDescription = "独立白名单",
                        onClick = onOpenWhitelist,
                        container = MaterialTheme.colorScheme.surfaceContainerLow,
                        size = 42,
                    )
                    Spacer(Modifier.width(6.dp))
                }
                CircleAction(
                    icon = Icons.Rounded.Sort,
                    contentDescription = "排序",
                    onClick = { sortOpen = true },
                    container = MaterialTheme.colorScheme.surfaceContainerLow,
                    size = 42,
                )
                Spacer(Modifier.width(6.dp))
                CircleAction(
                    icon = Icons.Rounded.Share,
                    contentDescription = "分享",
                    onClick = { shareOpen = true },
                    container = MaterialTheme.colorScheme.surfaceContainerLow,
                    size = 42,
                )
                Spacer(Modifier.width(6.dp))
                CircleAction(
                    icon = Icons.Rounded.History,
                    contentDescription = "专注历史",
                    onClick = onOpenHistory,
                    container = MaterialTheme.colorScheme.surfaceContainerLow,
                    size = 42,
                )
            }
        }

        order.forEach { key ->
            when (key) {
                "summary" -> if (showSummary) {
                    SectionBlock {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Metric(
                                value = "$totalCount",
                                label = "累计专注",
                                unit = "次",
                                modifier = Modifier.weight(1f),
                            )
                            Metric(
                                value = "$totalMinutes",
                                label = "总时长",
                                unit = "分钟",
                                modifier = Modifier.weight(1f),
                            )
                            Metric(
                                value = "$average",
                                label = "日均时长",
                                unit = "分钟",
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                    SectionBlock {
                        SectionHeader(
                            title = "当日专注",
                            onPrev = { day = day.minusDays(1) },
                            onNext = { day = day.plusDays(1) },
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "${formatDate(day)} · 专注 $dayCount 次 · $dayMinutes 分钟",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }

                "pie" -> SectionBlock {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        SectionHeader(
                            title = "专注时长分布",
                            onPrev = { pieAnchor = pieAnchor.minusDays(7) },
                            onNext = { pieAnchor = pieAnchor.plusDays(7) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    PillSegmented(
                        options = listOf("日", "周", "月", "自定义"),
                        selectedIndex = pieRange,
                        onSelect = { pieRange = it },
                    )
                    if (pieRange == 3) {
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            DateChip("开始", customStart, Modifier.weight(1f)) { customPick = 0 }
                            DateChip("结束", customEnd, Modifier.weight(1f)) { customPick = 1 }
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    if (pie.isEmpty()) {
                        EmptyAnalytics("这个时间段还没有专注记录")
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier.size(132.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Canvas(Modifier.size(132.dp)) {
                                    var start = -90f
                                    pie.forEach { slice ->
                                        val sweep = 360f * slice.minutes / pieTotal
                                        drawArc(
                                            color = slice.color,
                                            startAngle = start,
                                            sweepAngle = sweep,
                                            useCenter = true,
                                        )
                                        start += sweep
                                    }
                                }
                            }
                            Spacer(Modifier.width(14.dp))
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                pie.forEach { slice ->
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            Modifier
                                                .size(7.dp)
                                                .clip(CircleShape)
                                                .background(slice.color),
                                        )
                                        Spacer(Modifier.width(6.dp))
                                        Text(
                                            text = slice.label,
                                            style = MaterialTheme.typography.labelSmall,
                                            modifier = Modifier.weight(1f),
                                            maxLines = 1,
                                        )
                                        Text(
                                            text = "${(slice.minutes * 100 / pieTotal)}%",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                "hourly" -> SectionBlock {
                    SectionHeader(
                        title = "本月专注时段分布",
                        onPrev = { hourlyMonth = hourlyMonth.minusMonths(1) },
                        onNext = { hourlyMonth = hourlyMonth.plusMonths(1) },
                    )
                    Spacer(Modifier.height(10.dp))
                    FocusBarChart(
                        bars = hourlyBuckets.mapIndexed { index, value ->
                            BarDatum(
                                label = if (index % 2 == 0) "${index * 3}时" else "",
                                value = value,
                                highlightLabel = "${index * 3}:00–${index * 3 + 3}:00 · $value 分钟",
                            )
                        },
                        height = 142.dp,
                    )
                }

                "monthly" -> SectionBlock {
                    SectionHeader(
                        title = "月度专注统计",
                        onPrev = { monthlyMonth = monthlyMonth.minusMonths(1) },
                        onNext = { monthlyMonth = monthlyMonth.plusMonths(1) },
                    )
                    Spacer(Modifier.height(10.dp))
                    LineChart(
                        values = monthDays.map { it.second },
                        labels = monthDays.mapIndexed { index, pair ->
                            if (index % 5 == 0 || index == monthDays.lastIndex) "${pair.first.dayOfMonth}" else ""
                        },
                        height = 150.dp,
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "共 ${monthDays.sumOf { it.second }} 分钟 · 活跃 ${monthDays.count { it.second > 0 }} 天",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                "wake" -> SectionBlock {
                    SectionHeader(
                        title = "起床打卡分布",
                        onPrev = { wakeMonth = wakeMonth.minusMonths(1) },
                        onNext = { wakeMonth = wakeMonth.plusMonths(1) },
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "按每天首次专注时间估算",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(8.dp))
                    FocusBarChart(
                        bars = wakeBuckets.mapIndexed { index, value ->
                            BarDatum("${index + 4}时", value, "${index + 4}:00 · $value 天")
                        },
                        height = 132.dp,
                    )
                }

                "foreground" -> SectionBlock {
                    SectionHeader(
                        title = "App 前台运行时间",
                        onPrev = { foregroundMonth = foregroundMonth.minusMonths(1) },
                        onNext = { foregroundMonth = foregroundMonth.plusMonths(1) },
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "按当天首末专注记录的时间跨度估算",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(8.dp))
                    LineChart(
                        values = foregroundValues.map { it.second },
                        labels = foregroundValues.mapIndexed { index, pair ->
                            if (index % 5 == 0 || index == foregroundValues.lastIndex) "${pair.first.dayOfMonth}" else ""
                        },
                        height = 132.dp,
                    )
                }

                "sleep" -> SectionBlock {
                    SectionHeader(
                        title = "睡眠打卡分布",
                        onPrev = { sleepMonth = sleepMonth.minusMonths(1) },
                        onNext = { sleepMonth = sleepMonth.plusMonths(1) },
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "按每天最后一次专注结束时间估算",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(8.dp))
                    FocusBarChart(
                        bars = sleepBuckets.mapIndexed { index, value ->
                            BarDatum("${(index + 20) % 24}时", value, "${(index + 20) % 24}:00 · $value 天")
                        },
                        height = 132.dp,
                    )
                }

                "interruptions" -> SectionBlock {
                    SectionHeader(
                        title = "本月打卡打断原因",
                        onPrev = { interruptionMonth = interruptionMonth.minusMonths(1) },
                        onNext = { interruptionMonth = interruptionMonth.plusMonths(1) },
                    )
                    Spacer(Modifier.height(10.dp))
                    if (interruptions.isEmpty()) {
                        EmptyAnalytics("本月没有未完成的专注记录")
                    } else {
                        FocusBarChart(
                            bars = interruptions.map { (label, value) ->
                                BarDatum(label, value, "$label · $value 次")
                            },
                            height = 132.dp,
                        )
                    }
                }

                "yearly" -> SectionBlock {
                    SectionHeader(
                        title = "年度专注统计",
                        onPrev = { yearlyYear -= 1 },
                        onNext = { yearlyYear += 1 },
                    )
                    Spacer(Modifier.height(10.dp))
                    LineChart(
                        values = yearMonths,
                        labels = (1..12).map { if (it % 2 == 1) "${it}月" else "" },
                        height = 150.dp,
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "${yearlyYear}年累计 ${yearMonths.sum()} 分钟",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                "heatmap" -> SectionBlock {
                    SectionHeader(
                        title = "本周专注热力图",
                        onPrev = { weekAnchor = weekAnchor.minusWeeks(1) },
                        onNext = { weekAnchor = weekAnchor.plusWeeks(1) },
                    )
                    Spacer(Modifier.height(10.dp))
                    WeekHeatmap(values = weekValues)
                    if (showSummary) {
                        Spacer(Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                            Metric(
                                value = "$totalCount",
                                label = "累计专注",
                                unit = "次",
                                modifier = Modifier.weight(1f),
                            )
                            Metric(
                                value = "$totalMinutes",
                                label = "累计时长",
                                unit = "分钟",
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))
        PillAction(
            label = "回顾这个项目的专注历史",
            icon = Icons.Rounded.History,
            onClick = onOpenHistory,
            modifier = Modifier.padding(horizontal = 18.dp),
        )
    }

    if (shareOpen) {
        FocusPosterSheet(sessions = sessions, onDismiss = { shareOpen = false })
    }

    if (sortOpen) {
        AlertDialog(
            onDismissRequest = { sortOpen = false },
            title = { Text("调整内容排序") },
            text = {
                Column {
                    order.forEachIndexed { index, key ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = sectionTitle(key),
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.weight(1f),
                            )
                            Icon(
                                Icons.Rounded.ArrowUpward,
                                contentDescription = "上移",
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .clickable(enabled = index > 0) {
                                        order = order.toMutableList().apply {
                                            add(index - 1, removeAt(index))
                                        }
                                    }
                                    .padding(6.dp)
                                    .size(17.dp),
                            )
                            Icon(
                                Icons.Rounded.ArrowDownward,
                                contentDescription = "下移",
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .clickable(enabled = index < order.lastIndex) {
                                        order = order.toMutableList().apply {
                                            add(index + 1, removeAt(index))
                                        }
                                    }
                                    .padding(6.dp)
                                    .size(17.dp),
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { sortOpen = false }) { Text("完成") }
            },
        )
    }

    if (customPick != null) {
        val initial = if (customPick == 0) customStart else customEnd
        val state = rememberDatePickerState(
            initialSelectedDateMillis = initial.toEpochDay() * 86_400_000L,
        )
        DatePickerDialog(
            onDismissRequest = { customPick = null },
            confirmButton = {
                TextButton(
                    onClick = {
                        state.selectedDateMillis?.let { millis ->
                            val date = LocalDate.ofEpochDay(millis / 86_400_000L)
                            if (customPick == 0) {
                                customStart = date
                                if (customEnd.isBefore(date)) customEnd = date
                            } else {
                                customEnd = date
                                if (customStart.isAfter(date)) customStart = date
                            }
                        }
                        customPick = null
                    },
                ) { Text("确定") }
            },
            dismissButton = {
                TextButton(onClick = { customPick = null }) { Text("取消") }
            },
        ) {
            DatePicker(state = state)
        }
    }
}

@Composable
private fun SectionBlock(content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit) {
    SectionCard(modifier = Modifier.padding(horizontal = 18.dp, vertical = 5.dp)) {
        Column(Modifier.padding(16.dp), content = content)
    }
}

@Composable
private fun SectionHeader(
    title: String,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        SmallArrow(back = true, onClick = onPrev)
        Spacer(Modifier.width(2.dp))
        SmallArrow(back = false, onClick = onNext)
    }
}

@Composable
private fun SmallArrow(back: Boolean, onClick: () -> Unit) {
    Icon(
        imageVector = if (back) Icons.Rounded.ChevronLeft else Icons.Rounded.ChevronRight,
        contentDescription = if (back) "上一个" else "下一个",
        modifier = Modifier
            .clip(CircleShape)
            .clickable(onClick = onClick)
            .padding(5.dp)
            .size(18.dp),
        tint = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun EmptyAnalytics(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 18.dp),
        textAlign = TextAlign.Center,
    )
}

@Composable
private fun DateChip(label: String, date: LocalDate, modifier: Modifier, onClick: () -> Unit) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = "${date.monthValue}/${date.dayOfMonth}",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun WeekHeatmap(values: List<Int>) {
    val max = max(values.maxOrNull() ?: 0, 1)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        listOf("一", "二", "三", "四", "五", "六", "日").forEachIndexed { index, label ->
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(
                            if (values.getOrElse(index) { 0 } == 0) {
                                MaterialTheme.colorScheme.surfaceContainerHigh
                            } else {
                                MaterialTheme.colorScheme.primary.copy(
                                    alpha = 0.18f + 0.68f * values[index] / max,
                                )
                            },
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "${values.getOrElse(index) { 0 }}",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (values.getOrElse(index) { 0 } > 0) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

private fun sectionTitle(key: String): String = when (key) {
    "summary" -> "累计与当日"
    "pie" -> "专注时长分布"
    "hourly" -> "本月时段分布"
    "monthly" -> "月度专注统计"
    "wake" -> "起床打卡分布"
    "foreground" -> "App 前台运行时间"
    "sleep" -> "睡眠打卡分布"
    "interruptions" -> "打断原因"
    "yearly" -> "年度专注统计"
    else -> "本周热力图"
}

private val pieColors = listOf(
    Color(0xFF356859),
    Color(0xFFA9502F),
    Color(0xFF7FA98E),
    Color(0xFFC9A86A),
    Color(0xFF8C7CB0),
    Color(0xFF5B7FA3),
)

private fun wakeBuckets(sessions: List<FocusSession>, month: YearMonth): List<Int> {
    val buckets = IntArray(9)
    Stats.counted(sessions)
        .filter { YearMonth.from(Stats.dateOf(it.endedAt)) == month }
        .groupBy { Stats.dateOf(it.endedAt) }
        .forEach { (_, daySessions) ->
            val hour = daySessions.minOf { Stats.hourOf(it.startedAt) }
            if (hour in 4..12) buckets[hour - 4] += 1
        }
    return buckets.toList()
}

private fun sleepBuckets(sessions: List<FocusSession>, month: YearMonth): List<Int> {
    val buckets = IntArray(8)
    Stats.counted(sessions)
        .filter { YearMonth.from(Stats.dateOf(it.endedAt)) == month }
        .groupBy { Stats.dateOf(it.endedAt) }
        .forEach { (_, daySessions) ->
            val hour = daySessions.maxOf { Stats.hourOf(it.endedAt) }
            val slot = when {
                hour >= 20 -> hour - 20
                hour <= 3 -> hour + 4
                else -> -1
            }
            if (slot in 0..7) buckets[slot] += 1
        }
    return buckets.toList()
}

private fun foregroundByDay(sessions: List<FocusSession>, month: YearMonth): List<Pair<LocalDate, Int>> {
    return Stats.month(sessions, month).map { (date, _) ->
        val daySessions = Stats.counted(sessions).filter { Stats.dateOf(it.endedAt) == date }
        val minutes = if (daySessions.isEmpty()) {
            0
        } else {
            val first = daySessions.minOf { it.startedAt }
            val last = daySessions.maxOf { it.endedAt }
            ((last - first) / 60_000L).toInt().coerceAtLeast(0)
        }
        date to minutes
    }
}

private fun interruptionBuckets(
    sessions: List<FocusSession>,
    month: YearMonth,
): List<Pair<String, Int>> {
    return sessions
        .filter {
            it.kind == SessionKind.Focus &&
                !it.completed &&
                YearMonth.from(Stats.dateOf(it.endedAt)) == month
        }
        .groupBy { it.title.ifBlank { "自由专注" } }
        .map { (title, list) -> title to list.size }
        .sortedByDescending { it.second }
        .take(5)
}
